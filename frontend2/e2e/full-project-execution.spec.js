import { test, expect } from '@playwright/test'
import path from 'path'
import { fileURLToPath } from 'url'
import { TEST_USERS, API_BASE_URL } from './helpers/fixtures.js'
import { loginAsClient, loginAsArchitect } from './helpers/auth.js'
import { resetArchitectQuota, ensureArchitectIdentityComplete, getPhasePaymentReferenceId } from './helpers/db.js'
import { createApprovedOpenProject, submitBid, acceptBid , confirmThroughModal } from './helpers/scenario.js'
import { simulatePhasePaymentWebhook } from './helpers/xendit.js'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const DELIVERABLE_FIXTURE = path.resolve(__dirname, 'fixtures/test-deliverable.pdf')

const authedGet = async (page, url) => {
  const token = await page.evaluate(() => localStorage.getItem('auth_token'))
  const res = await page.request.get(url, { headers: { Authorization: `Bearer ${token}` } })
  return res.json()
}

// Continues from full-bid-lifecycle.spec.js's scope (stops at NEGOTIATION) through
// the rest of the project lifecycle: negotiation extras (chat, IT support), both
// parties confirming -> IN_PROGRESS, and the full pay/deliver/review cycle for
// every phase, ending once the last phase is APPROVED (not disbursed/completed —
// see plan notes on why completion is out of scope).
test.describe.serial('Full project execution: negotiation -> in-progress -> phase cycle', () => {
  const projectTitle = `E2E Project Execution ${Date.now()}`
  const phase1Amount = 30000000
  const phase2Amount = 20000000
  const bidAmount = phase1Amount + phase2Amount
  let projectId

  test.beforeAll(() => {
    resetArchitectQuota(TEST_USERS.architect.email)
    ensureArchitectIdentityComplete(TEST_USERS.architect.email)
  })

  test('setup: create project, bid with 2 phases, accept -> NEGOTIATION', async ({ page }) => {
    projectId = await createApprovedOpenProject(page, projectTitle)
    await submitBid(page, projectId, {
      bidAmount,
      phases: [
        { amount: phase1Amount, estimatedDays: 10 },
        { amount: phase2Amount, estimatedDays: 7 }
      ]
    })
    await acceptBid(page, projectId)
  })

  test('negotiation phase: chat and IT support work', async ({ page }) => {
    await loginAsClient(page)
    await page.goto(`/client/projects/${projectId}/finalization`)

    await expect(page.getByRole('heading', { name: 'Diskusi' })).toBeVisible({ timeout: 10000 })

    const chatInput = page.getByPlaceholder('Type a message...')
    await chatInput.fill('Hi, looking forward to working together!')
    await chatInput.press('Enter')
    await expect(page.getByText('Hi, looking forward to working together!')).toBeVisible({ timeout: 10000 })

    await page.getByRole('button', { name: 'Minta Dukungan IT' }).click()
    await expect(page.getByRole('button', { name: 'Dukungan IT diundang' })).toBeVisible({ timeout: 10000 })
  })

  test('both parties confirm negotiation terms -> IN_PROGRESS', async ({ page }) => {
    await loginAsClient(page)
    await page.goto(`/client/projects/${projectId}/finalization`)
    await confirmThroughModal(page, 'Konfirmasi & Lanjut ke Pembayaran')
    await expect(page.getByText('awaiting').or(page.getByText('Menunggu'))).toBeVisible({ timeout: 10000 })

    await loginAsArchitect(page)
    await page.goto(`/architect/projects/${projectId}/finalization`)
    await confirmThroughModal(page, 'Konfirmasi & Mulai Proyek')
    await expect(page).toHaveURL(new RegExp(`/architect/projects/${projectId}/workspace`), { timeout: 10000 })

    await loginAsClient(page)
    const body = await authedGet(page, `${API_BASE_URL}/rmtr/projects/${projectId}`)
    expect(body.data.status).toBe('IN_PROGRESS')
  })

  // The workspace opens on the Summary tab, so the phase accordion only exists once the
  // Phases & Deliverables tab is selected.
  const goToPhasesTab = async page => {
    const tab = page.getByRole('button', { name: 'Tahap & Deliverable', exact: true })
    await tab.waitFor({ state: 'visible', timeout: 15000 })
    await tab.click()
  }

  // Expansion is detected by the phase body actually rendering (its Deliverables section),
  // not by a class on the chevron -- the icon is swapped rather than rotated, and asserting
  // on visible content survives restyling.
  const phaseCard = (page, phaseNumber) =>
    page
      .locator('[id^="phase-"]')
      .filter({ has: page.getByRole('button', { name: new RegExp(`Fase ${phaseNumber}\\b`) }) })

  const expandPhase = async (page, phaseNumber) => {
    await goToPhasesTab(page)
    const toggle = page.getByRole('button', { name: new RegExp(`Fase ${phaseNumber}\\b`) })
    await toggle.waitFor({ state: 'visible', timeout: 15000 })
    // Several phases can be open at once, so the check has to be scoped to this phase's own
    // card -- a page-level lookup would see a sibling's body and skip the click.
    const card = page.locator('[id^="phase-"]').filter({ has: toggle })
    for (let attempt = 0; attempt < 3; attempt++) {
      if (await card.getByText('Deliverable', { exact: true }).first().isVisible().catch(() => false)) return
      await toggle.click()
      await page.waitForTimeout(300)
    }
    throw new Error(`Could not expand phase ${phaseNumber} card after 3 attempts`)
  }

  // Same stale-click symptom as expandPhase shows up on other buttons in this
  // workspace too — generic click-and-verify retry, used for every action here
  // that has a clearly checkable visible outcome.
  const clickUntil = async (locator, checkFn, attempts = 3) => {
    for (let attempt = 0; attempt < attempts; attempt++) {
      await locator.click()
      await locator.page().waitForTimeout(300)
      if (await checkFn()) return
    }
    throw new Error(`Click on "${await locator.innerText().catch(() => '?')}" had no visible effect after ${attempts} attempts`)
  }

  // The deliverable names the scenario helper puts on the bid; each one is a row of its own.
  const DELIVERABLE_NAMES = ['Site Plan', 'Floor Plan']

  // There is no "submit for review" button any more: completing the deliverable list *is* the
  // submission, so the phase flips to review on its own once the last row is answered. A row can
  // lose its upload button mid-loop for exactly that reason, which is not a failure.
  const uploadUntilDelivered = async (page, phaseNumber) => {
    const card = phaseCard(page, phaseNumber)
    for (const name of DELIVERABLE_NAMES) {
      // Answering the last outstanding row delivers the phase immediately, which closes the
      // upload path for the rows after it -- after a revision that can be the very first one.
      if (await card.getByText('Sedang Direview').first().isVisible().catch(() => false)) break
      const uploadBtn = card
        .getByRole('button', { name: new RegExp(`Deliverable: ${name}$`) })
        .first()
      if (!(await uploadBtn.isVisible().catch(() => false))) continue
      await uploadBtn.click()
      const uploadModal = page.getByRole('dialog')
      await page.locator('input[type="file"]').setInputFiles(DELIVERABLE_FIXTURE)
      await clickUntil(
        uploadModal.getByRole('button', { name: 'Unggah', exact: true }),
        async () => !(await uploadModal.isVisible())
      )
    }
    await expect(card.getByText('Sedang Direview').first()).toBeVisible({ timeout: 15000 })
  }

  for (const phaseNumber of [1, 2]) {
    test(`phase ${phaseNumber}: bill, pay, deliver${phaseNumber === 1 ? ', request revision,' : ''} and approve`, async ({
      page
    }) => {
      const amount = phaseNumber === 1 ? phase1Amount : phase2Amount

      // payPhase() does window.open(paymentLink) to the real Xendit checkout —
      // neutralize it so no popup/tab is created (closing a popup via Playwright
      // was observed to leave the original page's pending request hanging).
      await page.addInitScript(() => {
        window.open = () => null
      })

      // --- Client: bill the phase on the BidPaymentPhase path.
      // This used to click through /client/projects/:id/payments, which the Contract & Payment
      // tab replaced. Calling the endpoint that page called keeps the billing path under test
      // without depending on which screen exposes it.
      // (ProjectWorkspace.vue's own "Buat Invoice" button is the separate ProjectPhase billing
      // path, covered by phase-invoice-billing.spec.js.)
      const clientToken = await loginAsClient(page)
      const clientAuth = { Authorization: `Bearer ${clientToken}` }

      const summary = await page.request.get(`${API_BASE_URL}/rmtr/payments/projects/${projectId}`, {
        headers: clientAuth
      })
      expect(summary.ok(), await summary.text()).toBeTruthy()
      const bidPhase = ((await summary.json()).data ?? []).find(p => p.phaseNumber === phaseNumber)
      expect(bidPhase, `no bid payment phase ${phaseNumber}`).toBeTruthy()

      const invoice = await page.request.post(
        `${API_BASE_URL}/rmtr/payments/phases/${bidPhase.phaseId}`,
        { headers: clientAuth }
      )
      expect(invoice.ok(), await invoice.text()).toBeTruthy()
      getPhasePaymentReferenceId(projectId, phaseNumber)

      // --- Simulate Xendit "PAID" webhook directly (no real checkout) ---
      await simulatePhasePaymentWebhook(page.request, API_BASE_URL, projectId, phaseNumber, amount)

      // The summary's key-date rail is fed by a PAYMENT_RECEIVED log row, which this billing
      // route (the bid payment schedule) did not used to write at all -- so a missing start date
      // here means that path stopped recording that the phase was ever paid for.
      await page.goto(`/client/projects/${projectId}/workspace`)
      await expect(page.getByText(`Fase ${phaseNumber} dimulai`)).toBeVisible({ timeout: 10000 })

      await expandPhase(page, phaseNumber)
      await expect(phaseCard(page, phaseNumber).getByText('Pekerjaan Berlangsung').first()).toBeVisible({
        timeout: 10000
      })

      // --- Architect: upload deliverable and submit for review ---
      await loginAsArchitect(page)
      await page.goto(`/architect/projects/${projectId}/workspace`)
      await expandPhase(page, phaseNumber)
      await uploadUntilDelivered(page, phaseNumber)

      if (phaseNumber === 1) {
        // --- Client: request a revision (exercises the "or do revision" branch) ---
        await loginAsClient(page)
        await page.goto(`/client/projects/${projectId}/workspace`)
        await expandPhase(page, phaseNumber)
        // A revision is now composed from the deliverable rows: mark what needs redoing, write
        // the comment in the basket below the table, then confirm the round it will consume.
        const card = phaseCard(page, phaseNumber)
        await card.getByRole('button', { name: /^Revisi Deliverable:/ }).first().click()

        // Each marked deliverable carries its own instruction; the round is pooled, not the note.
        const basketComment = card.getByPlaceholder(/Mohon sesuaikan/).first()
        await expect(basketComment).toBeVisible()
        await basketComment.fill('Please adjust the floor plan slightly.')

        const revisionModal = page.getByRole('dialog')
        await clickUntil(card.getByRole('button', { name: 'Kirim permintaan revisi' }), () =>
          revisionModal.isVisible()
        )
        await clickUntil(revisionModal.getByRole('button', { name: 'Ya, minta revisi' }), async () => !(await revisionModal.isVisible()))

        // --- Architect: re-upload and resubmit ---
        await loginAsArchitect(page)
        await page.goto(`/architect/projects/${projectId}/workspace`)
        await expandPhase(page, phaseNumber)
        await uploadUntilDelivered(page, phaseNumber)
      }

      // --- Client: approve ---
      await loginAsClient(page)
      await page.goto(`/client/projects/${projectId}/workspace`)
      await expandPhase(page, phaseNumber)
      const approveModal = page.getByRole('dialog')
      const card = phaseCard(page, phaseNumber)

      // Approval is per deliverable now, and the phase approves itself on the last row -- there
      // is no phase-level button to press, just as there is no submit-for-review one.
      for (const name of DELIVERABLE_NAMES) {
        const approveBtn = card.getByRole('button', { name: `Setujui Deliverable: ${name}` })
        if (!(await approveBtn.isVisible().catch(() => false))) continue
        await clickUntil(approveBtn, () => approveModal.isVisible())
        await clickUntil(
          approveModal.getByRole('button', { name: 'Ya, Setujui Sekarang' }),
          async () => !(await approveModal.isVisible())
        )
      }

      // Every approved phase shows this banner, so scope the check to this phase's card.
      await expect(card.getByText('Pekerjaan Disetujui').first()).toBeVisible({ timeout: 15000 })
    })
  }

  test('end state: all phases approved, project still IN_PROGRESS (not completed)', async ({ page }) => {
    await loginAsClient(page)
    await page.goto(`/client/projects/${projectId}/workspace`)

    const phasesBody = await authedGet(page, `${API_BASE_URL}/rmtr/projects/${projectId}/phases`)
    for (const phase of phasesBody.data) {
      expect(phase.status).toBe('APPROVED')
    }

    const projectBody = await authedGet(page, `${API_BASE_URL}/rmtr/projects/${projectId}`)
    expect(projectBody.data.status).toBe('IN_PROGRESS')
  })
})
