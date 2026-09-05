import { test, expect } from '@playwright/test'
import { TEST_USERS, API_BASE_URL } from './helpers/fixtures.js'
import { simulateInvoicePaidWebhook } from './helpers/xendit.js'
import { loginAsClient, loginAsArchitect } from './helpers/auth.js'
import { resetArchitectQuota, ensureArchitectIdentityComplete, querySql } from './helpers/db.js'
import { createApprovedOpenProject, submitBid, acceptBid , confirmThroughModal } from './helpers/scenario.js'

// Regression test for a backend bug where PhasePaymentService.createInvoiceForPhase()
// never set rmtr_phase_payment.phase_id, violating its NOT NULL constraint — every
// click of the workspace's "Buat Invoice" button failed. Unlike
// full-project-execution.spec.js (which deliberately avoids this button and bills via
// the separate /payments BidPaymentPhase path), this test exercises the button itself.
test('client bills a phase via the workspace "Buat Invoice" button', async ({ page }) => {
  const projectTitle = `E2E Invoice Billing ${Date.now()}`
  const phaseAmount = 50000000

  resetArchitectQuota(TEST_USERS.architect.email)
  ensureArchitectIdentityComplete(TEST_USERS.architect.email)

  const projectId = await createApprovedOpenProject(page, projectTitle)
  await submitBid(page, projectId, {
    bidAmount: phaseAmount,
    phases: [{ amount: phaseAmount, estimatedDays: 10 }]
  })
  await acceptBid(page, projectId)

  await loginAsClient(page)
  await page.goto(`/client/projects/${projectId}/finalization`)
  await confirmThroughModal(page, 'Konfirmasi & Lanjut ke Pembayaran')
  await expect(page.getByText('awaiting').or(page.getByText('Menunggu'))).toBeVisible({ timeout: 10000 })

  await loginAsArchitect(page)
  await page.goto(`/architect/projects/${projectId}/finalization`)
  await confirmThroughModal(page, 'Konfirmasi & Mulai Proyek')
  await expect(page).toHaveURL(new RegExp(`/architect/projects/${projectId}/workspace`), { timeout: 10000 })

  await loginAsClient(page)

  // billPhase() falls back to a native alert() on failure and window.open()s the
  // payment link on success — capture both so a regression surfaces as a clear
  // assertion failure instead of a hung dialog.
  let dialogMessage = null
  page.on('dialog', async dialog => {
    dialogMessage = dialog.message()
    await dialog.accept()
  })
  await page.addInitScript(() => {
    window.open = () => null
  })

  await page.goto(`/client/projects/${projectId}/workspace`)

  // Invoicing lives on the Contract & Payment tab, which owns every money action; the
  // phase accordion only links across to it.
  await page.getByRole('button', { name: 'Kontrak & Pembayaran' }).click()

  const invoiceBtn = page.getByRole('button', { name: /^Buat Invoice/ }).first()
  await expect(invoiceBtn).toBeVisible({ timeout: 10000 })
  // The schedule is the first card on the tab; scrolling to the top keeps the row clear of
  // the sticky header, which would otherwise intercept the click.
  await page.evaluate(() => window.scrollTo(0, 0))
  await invoiceBtn.click()

  await expect(page.getByText('Invoice Terkirim').first()).toBeVisible({ timeout: 10000 })

  expect(dialogMessage).toBeNull()

  // Paying the invoice is what starts the delivery clock: the phase's due date is stamped from
  // the bid's estimatedDays at that moment, never earlier -- the architect is not on the hook
  // for a phase the client has not funded.
  const externalId = querySql(
    `SELECT xendit_reference_id FROM rmtr_phase_payment WHERE project_id = ${projectId}`
  )
  expect(externalId).toMatch(/^proj_phase_/)

  await simulateInvoicePaidWebhook(page.request, API_BASE_URL, externalId, phaseAmount)

  const dueDate = querySql(
    `SELECT due_date FROM rmtr_project_phase WHERE project_id = ${projectId} AND phase_number = 1`
  )
  expect(dueDate).toBe(new Date(Date.now() + 10 * 86400000).toISOString().slice(0, 10))

  // The bid promised 10 days, so the workspace counts down from there instead of reading "Closed".
  // The phase header truncates hard at the default viewport width, which reads as "hidden";
  // the countdown is a desktop-width element.
  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto(`/client/projects/${projectId}/workspace`)
  const phasesTab = page.getByRole('button', { name: 'Tahap & Deliverable' })
  await phasesTab.waitFor({ state: 'visible', timeout: 15000 })
  await phasesTab.click()
  await expect(page.locator('[id^="phase-"]').getByText('10 hari tersisa').first()).toBeVisible({
    timeout: 10000
  })
})
