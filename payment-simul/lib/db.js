import pg from 'pg'
import { config } from './config.js'

const pool = new pg.Pool({ ...config.db, max: 4 })

/**
 * Xendit reference ids embed System.currentTimeMillis(), so they cannot be
 * reconstructed from a project/phase id — they can only be read back from the
 * row that stored them. Everything below exists to recover those ids.
 *
 * This module is read-only by design. Advancing state is the job of the real
 * webhook handlers; writing here would reproduce the inconsistent half-state
 * that manual SQL patching already causes.
 */
const query = async (sql, label) => {
  try {
    const { rows } = await pool.query(sql)
    return rows
  } catch (err) {
    throw new Error(
      `Query "${label}" failed against ${config.db.host}:${config.db.port}/${config.db.database}. ` +
        `Is the dev database up (docker compose -f docker/dev-database.yml up -d)? ${err.message}`
    )
  }
}

export const listTokenPurchases = () =>
  query(
    `SELECT tp.id, tp.xendit_reference_id, tp.total_amount, tp.quantity,
            tp.status, u.email
     FROM rmtr_token_purchase tp
     JOIN rmtr_architect a ON a.id = tp.architect_id
     JOIN rmtr_user u ON u.id = a.user_id
     WHERE tp.xendit_reference_id IS NOT NULL
     ORDER BY tp.id DESC LIMIT 25`,
    'token purchases'
  )

export const listWorkspacePhasePayments = () =>
  query(
    `SELECT pp.id, pp.xendit_reference_id, pp.amount, pp.status,
            pp.project_id, ph.phase_number, ph.status AS phase_status
     FROM rmtr_phase_payment pp
     JOIN rmtr_project_phase ph ON ph.id = pp.project_phase_id
     WHERE pp.xendit_reference_id LIKE 'proj_phase_%'
     ORDER BY pp.id DESC LIMIT 25`,
    'workspace phase payments'
  )

export const listDashboardPhasePayments = () =>
  query(
    `SELECT pp.id, pp.xendit_reference_id, pp.amount, pp.status,
            pp.project_id, bpp.phase_number
     FROM rmtr_phase_payment pp
     LEFT JOIN rmtr_bid_payment_phase bpp ON bpp.id = pp.phase_id
     WHERE pp.xendit_reference_id LIKE 'phase_payment_%'
     ORDER BY pp.id DESC LIMIT 25`,
    'dashboard phase payments'
  )

export const listDisbursements = () =>
  query(
    `SELECT d.id, d.xendit_reference_id, d.xendit_payout_id, d.amount,
            d.status, ph.phase_number, ph.project_id
     FROM rmtr_project_phase_disbursement d
     JOIN rmtr_project_phase ph ON ph.id = d.phase_id
     ORDER BY d.id DESC LIMIT 25`,
    'disbursements'
  )

export const closePool = () => pool.end()
