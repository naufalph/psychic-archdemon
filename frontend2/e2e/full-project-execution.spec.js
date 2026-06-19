import { test, expect } from '@playwright/test'
import path from 'path'
import { fileURLToPath } from 'url'
import { TEST_USERS, API_BASE_URL } from './helpers/fixtures.js'
import { loginAsClient, loginAsArchitect } from './helpers/auth.js'
import { resetArchitectQuota, ensureArchitectIdentityComplete, getPhasePaymentReferenceId } from './helpers/db.js'
import { createApprovedOpenProject, submitBid, acceptBid } from './helpers/scenario.js'
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
    await submitBid(page, projectTitle, {
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
    page.once('dialog', dialog => dialog.accept())
    await page.getByRole('button', { name: 'Konfirmasi & Lanjut ke Pembayaran' }).click()
    await expect(page.getByText('awaiting').or(page.getByText('Menunggu'))).toBeVisible({ timeout: 10000 })

    await loginAsArchitect(page)
    await page.goto(`/architect/projects/${projectId}/finalization`)
    page.once('dialog', dialog => dialog.accept())
    await page.getByRole('button', { name: 'Konfirmasi & Mulai Proyek' }).click()
    await expect(page).toHaveURL(new RegExp(`/architect/projects/${projectId}/workspace`), { timeout: 10000 })

    await loginAsClient(page)
    const body = await authedGet(page, `${API_BASE_URL}/rmtr/projects/${projectId}`)
    expect(body.data.status).toBe('IN_PROGRESS')
  })

  // Only one phase is ever expanded at a time in ProjectWorkspace (single
  // `expandedPhaseId` ref), so once a phase is expanded its action buttons are
  // unambiguous at the page level — no need to scope into a specific card div.
  // The very first click on this page after an SPA navigation is sometimes
  // swallowed (a stale click-outside listener left over from the previous
  // route, verified empirically) — retry until the chevron actually rotates.
  const expandPhase = async (page, phaseNumber) => {
    const toggle = page.getByRole('button', { name: new RegExp(`Fase ${phaseNumber}\\b`) })
    for (let attempt = 0; attempt < 3; attempt++) {
      await toggle.click()
      await page.waitForTimeout(300)
      const expanded = await toggle
        .locator('svg')
        .last()
        .evaluate(el => el.getAttribute('class')?.includes('rotate-180'))
        .catch(() => false)
      if (expanded) return
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

  const uploadAndSubmitForReview = async page => {
    await page.locator('input[type="file"]').setInputFiles(DELIVERABLE_FIXTURE)
    await clickUntil(page.getByRole('button', { name: 'Unggah' }), () =>
      page.getByRole('button', { name: 'Kirim untuk Review' }).isVisible()
    )
    // "Sedang Direview · Under Client Review" is the architect-side DELIVERED
    // banner (the client-side equivalent text, "Pekerjaan Dikirimkan", only
    // ever renders on the client's own workspace view).
    await clickUntil(page.getByRole('button', { name: 'Kirim untuk Review' }), () =>
      page.getByText('Sedang Direview').isVisible()
    )
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

      // --- Client: bill the phase via the BidPaymentPhase payment schedule page.
      // (ProjectWorkspace.vue's own "Buat Invoice" button is a separate billing
      // path, covered by phase-invoice-billing.spec.js — this test sticks to the
      // BidPaymentPhase path it was written against.)
      await loginAsClient(page)
      await page.goto(`/client/projects/${projectId}/payments`)

      const phaseRow = page.locator('div.rounded-xl.border-gray-200').filter({ hasText: `Fase ${phaseNumber}` })
      await expect(phaseRow).toBeVisible({ timeout: 10000 })

      let invoiceCreated = false
      for (let attempt = 0; attempt < 3 && !invoiceCreated; attempt++) {
        await phaseRow.getByRole('button', { name: 'Bayar Sekarang' }).click()
        await page.waitForTimeout(500)
        try {
          getPhasePaymentReferenceId(projectId, phaseNumber)
          invoiceCreated = true
        } catch {
          // retry — the click may have been swallowed (see expandPhase note above)
        }
      }
      if (!invoiceCreated) throw new Error(`Could not create invoice for phase ${phaseNumber}`)

      // --- Simulate Xendit "PAID" webhook directly (no real checkout) ---
      await simulatePhasePaymentWebhook(page.request, API_BASE_URL, projectId, phaseNumber, amount)

      await page.goto(`/client/projects/${projectId}/workspace`)
      await expandPhase(page, phaseNumber)
      await expect(page.getByText('Pekerjaan Berlangsung')).toBeVisible({ timeout: 10000 })

      // --- Architect: upload deliverable and submit for review ---
      await loginAsArchitect(page)
      await page.goto(`/architect/projects/${projectId}/workspace`)
      await expandPhase(page, phaseNumber)
      await uploadAndSubmitForReview(page)

      if (phaseNumber === 1) {
        // --- Client: request a revision (exercises the "or do revision" branch) ---
        await loginAsClient(page)
        await page.goto(`/client/projects/${projectId}/workspace`)
        await expandPhase(page, phaseNumber)
        // .first() (the outermost modal backdrop) is used rather than .last()
        // since it's guaranteed to contain every descendant matching the same
        // text, regardless of how deeply the heading/buttons are nested inside.
        const revisionModal = page.locator('div').filter({ hasText: 'Catatan Revisi' }).first()
        await clickUntil(page.getByRole('button', { name: 'Minta Revisi' }), () => revisionModal.isVisible())

        await revisionModal.getByPlaceholder(/Mohon sesuaikan/).fill('Please adjust the floor plan slightly.')
        await clickUntil(revisionModal.getByRole('button', { name: 'Minta Revisi' }), async () => !(await revisionModal.isVisible()))

        // --- Architect: re-upload and resubmit ---
        await loginAsArchitect(page)
        await page.goto(`/architect/projects/${projectId}/workspace`)
        await expandPhase(page, phaseNumber)
        await uploadAndSubmitForReview(page)
      }

      // --- Client: approve ---
      await loginAsClient(page)
      await page.goto(`/client/projects/${projectId}/workspace`)
      await expandPhase(page, phaseNumber)
      const approveModal = page.locator('div').filter({ hasText: 'Setujui Pekerjaan Fase Ini?' }).first()
      await clickUntil(page.getByRole('button', { name: 'Setujui · Approve' }), () => approveModal.isVisible())

      await clickUntil(approveModal.getByRole('button', { name: 'Ya, Setujui Sekarang' }), () =>
        page.getByText('Pekerjaan Disetujui').isVisible()
      )
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
