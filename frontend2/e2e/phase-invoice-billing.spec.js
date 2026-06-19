import { test, expect } from '@playwright/test'
import { TEST_USERS } from './helpers/fixtures.js'
import { loginAsClient, loginAsArchitect } from './helpers/auth.js'
import { resetArchitectQuota, ensureArchitectIdentityComplete } from './helpers/db.js'
import { createApprovedOpenProject, submitBid, acceptBid } from './helpers/scenario.js'

// Regression test for a backend bug where PhasePaymentService.createInvoiceForPhase()
// never set rmtr_phase_payment.phase_id, violating its NOT NULL constraint — every
// click of ProjectWorkspace.vue's "Buat Invoice" button failed. Unlike
// full-project-execution.spec.js (which deliberately avoids this button and bills via
// the separate /payments BidPaymentPhase path), this test exercises the button itself.
test('client bills a phase via the workspace "Buat Invoice" button', async ({ page }) => {
  const projectTitle = `E2E Invoice Billing ${Date.now()}`
  const phaseAmount = 50000000

  resetArchitectQuota(TEST_USERS.architect.email)
  ensureArchitectIdentityComplete(TEST_USERS.architect.email)

  const projectId = await createApprovedOpenProject(page, projectTitle)
  await submitBid(page, projectTitle, {
    bidAmount: phaseAmount,
    phases: [{ amount: phaseAmount, estimatedDays: 10 }]
  })
  await acceptBid(page, projectId)

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

  const toggle = page.getByRole('button', { name: /Fase 1\b/ })
  for (let attempt = 0; attempt < 3; attempt++) {
    await toggle.click()
    await page.waitForTimeout(300)
    if (await page.getByRole('button', { name: 'Buat Invoice' }).isVisible().catch(() => false)) break
  }

  await page.getByRole('button', { name: 'Buat Invoice' }).click()
  await expect(page.getByText('Invoice Terkirim · Invoice Sent')).toBeVisible({ timeout: 10000 })

  expect(dialogMessage).toBeNull()
})
