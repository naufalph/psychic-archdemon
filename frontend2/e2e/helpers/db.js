import { execFileSync } from 'child_process'

const DB_HOST = process.env.E2E_DB_HOST || 'localhost'
const DB_PORT = process.env.E2E_DB_PORT || '5432'
const DB_USER = process.env.E2E_DB_USER || 'postgres'
const DB_PASSWORD = process.env.E2E_DB_PASSWORD || 'password'
const DB_NAME = process.env.E2E_DB_NAME || 'rumantra-db'

/**
 * The dev database runs in a container, which already ships a psql. Going through
 * `docker exec` means the suite needs no Postgres client on the host -- one less thing
 * to install per developer, and per OS. Set E2E_DB_PSQL=host to use a host psql
 * instead, which is what you want when E2E_DB_HOST points somewhere other than the
 * dev container.
 */
const DB_CONTAINER = process.env.E2E_DB_CONTAINER || 'rumantra-db'
const USE_HOST_PSQL = process.env.E2E_DB_PSQL === 'host'

const psqlCommand = extraArgs => {
  const psqlArgs = ['-U', DB_USER, '-d', DB_NAME, ...extraArgs]
  if (USE_HOST_PSQL) {
    return {
      file: 'psql',
      args: ['-h', DB_HOST, '-p', DB_PORT, ...psqlArgs],
      env: { ...process.env, PGPASSWORD: DB_PASSWORD }
    }
  }
  return {
    file: 'docker',
    args: ['exec', '-e', `PGPASSWORD=${DB_PASSWORD}`, DB_CONTAINER, 'psql', ...psqlArgs],
    env: process.env
  }
}

const describeTarget = () => (USE_HOST_PSQL ? `${DB_HOST}:${DB_PORT}` : `container ${DB_CONTAINER}`)

const runSql = sql => {
  const { file, args, env } = psqlCommand(['-v', 'ON_ERROR_STOP=1', '-c', sql])
  try {
    execFileSync(file, args, { env, stdio: ['ignore', 'pipe', 'pipe'] })
  } catch (err) {
    throw new Error(
      `Failed to run setup SQL against the dev database (${describeTarget()}). ` +
        `Is Postgres running (docker compose -f docker/dev-database.yml up -d)? ` +
        `Original error: ${err.stderr?.toString() || err.message}`
    )
  }
}

export const querySql = sql => {
  const { file, args, env } = psqlCommand(['-t', '-A', '-c', sql])
  try {
    return execFileSync(file, args, { env }).toString().trim()
  } catch (err) {
    throw new Error(
      `Failed to query the dev database (${describeTarget()}). ` +
        `Original error: ${err.stderr?.toString() || err.message}`
    )
  }
}

/**
 * FREE-tier architects get 1 bid token/year with no API-level top-up path
 * (real allocation only happens via Xendit webhooks). Reset it directly in
 * the dev DB so the bid-submission test step is repeatable across runs.
 */
export const resetArchitectQuota = (email, tokens = 10) => {
  runSql(`
    UPDATE rmtr_bid_quota SET tokens_remaining = ${tokens}, tokens_allocated = ${tokens}
    WHERE architect_id = (
      SELECT a.id FROM rmtr_architect a JOIN rmtr_user u ON u.id = a.user_id WHERE u.email = '${email}'
    );
  `)
}

/**
 * Bid submission is gated on KTP/NPWP/full-name/phone identity verification.
 * Test architects have no real identity docs, so seed placeholder values
 * directly rather than running the WhatsApp OTP/KTP upload flow in e2e.
 */
export const ensureArchitectIdentityComplete = email => {
  runSql(`
    UPDATE rmtr_architect SET
      ktp_num = '3174012345678901',
      is_ktp_verified = true,
      npwp = '123456789012345',
      is_npwp_verified = true,
      fullname_ktp = 'E2E Test Architect',
      phone_num = '081234567890',
      is_phone_verified = true
    WHERE user_id = (SELECT id FROM rmtr_user WHERE email = '${email}');
  `)
}

/**
 * Posting a project is gated on the client having a phone number
 * (ProjectService.validateClientProfile), which registration does not collect. Seed one so
 * a freshly registered test client can reach the submit step.
 */
export const ensureClientProfileComplete = email => {
  runSql(`
    UPDATE rmtr_client SET phone_num = '081234567890'
    WHERE user_id = (SELECT id FROM rmtr_user WHERE email = '${email}')
      AND (phone_num IS NULL OR phone_num = '');
  `)
}

/**
 * After billing via the workspace Contract & Payment tab ("Bayar Sekarang"), the backend
 * (PaymentService.initiatePhasePayment) creates a real rmtr_phase_payment row
 * keyed by the BidPaymentPhase id, not the ProjectPhase id — join through
 * rmtr_bid_payment_phase by phase_number to find it. Read the reference id
 * back so a synthetic webhook can target the exact same row instead of a real
 * Xendit checkout.
 *
 * Note: ProjectWorkspace.vue's own "Buat Invoice" button (POST
 * /rmtr/phases/{phaseId}/bill) goes through PhasePaymentService instead, which
 * never populates this BidPaymentPhase join (see phase-invoice-billing.spec.js).
 * This helper only covers the /payments path used below.
 */
export const getPhasePaymentReferenceId = (projectId, phaseNumber) => {
  const id = querySql(`
    SELECT pp.xendit_reference_id FROM rmtr_phase_payment pp
    JOIN rmtr_bid_payment_phase bpp ON bpp.id = pp.phase_id
    WHERE pp.project_id = ${projectId} AND bpp.phase_number = ${phaseNumber}
    ORDER BY pp.id DESC LIMIT 1;
  `)
  if (!id)
    throw new Error(
      `No rmtr_phase_payment row found for project_id=${projectId} phase_number=${phaseNumber}`
    )
  return id
}

/**
 * Advances a phase past the Xendit payment step. Real payment needs a webhook from Xendit,
 * which a local test cannot receive, so the paid state is set directly.
 */
export const setPhaseStatus = (phaseId, status) =>
  runSql(`UPDATE rmtr_project_phase SET status = '${status}' WHERE id = ${phaseId};`)
