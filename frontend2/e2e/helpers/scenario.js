import { expect } from '@playwright/test'
import { ROUTES } from './fixtures.js'
import { loginAsClient, loginAsArchitect, loginAsSuperuser } from './auth.js'

/**
 * Client creates a project, then superuser approves it (PENDING_APPROVAL -> OPEN).
 * Shared by every scenario that needs a biddable project as a starting point.
 * Returns the new project's id.
 */
export const createApprovedOpenProject = async (page, title) => {
  await loginAsClient(page)
  await page.goto(ROUTES.clientProjectCreate)

  await page.locator('input[placeholder="e.g., Modern Student Housing in Depok"]').fill(title)

  // Address is a Google Places autocomplete, which needs a live API key and can't be driven
  // deterministically in CI. Without a key it degrades to a plain text input, which is the
  // path this helper targets; city/province/coordinates are then legitimately left empty.
  await page
    .locator('input[placeholder^="Search an address"], input[placeholder^="Cari alamat"]')
    .fill('Jl. Test No. 1, Jakarta Selatan')

  await page.locator('input[type="number"][placeholder="e.g., 200"]').fill('200')
  // Build area is optional, but filling it keeps the fixture representative of a real brief.
  await page.locator('input[type="number"][placeholder="e.g., 120"]').fill('120')
  await page.locator('form select').nth(1).selectOption('RESIDENTIAL')
  await page
    .locator('textarea[placeholder^="Describe number of rooms"]')
    .fill('3 bedroom modern house, industrial style, e2e test project.')
  await page.locator('input[placeholder="e.g., 2.000.000.000"]').fill('300000000')

  await page.getByRole('button', { name: 'Post Project' }).click()
  await expect(page).toHaveURL(/\/client\/dashboard/)

  await page.goto(ROUTES.clientProjects)
  const card = page.locator('div').filter({ hasText: title }).last()
  await expect(card).toBeVisible({ timeout: 10000 })
  await card.click()
  await expect(page).toHaveURL(/\/client\/projects\/(\d+)/)
  const projectId = page.url().match(/\/client\/projects\/(\d+)/)[1]
  await expect(page.getByText('Pending Validation')).toBeVisible()

  await loginAsSuperuser(page)
  await page.goto(ROUTES.superuserProjectQueue)

  const row = page.locator('div.rounded-xl.border-gray-100').filter({ hasText: title })
  await expect(row).toBeVisible({ timeout: 10000 })
  await row.getByRole('button', { name: 'Setujui' }).click()
  await expect(row).toHaveCount(0, { timeout: 10000 })

  return projectId
}

/**
 * Architect submits a bid with one or more payment phases.
 * `phases` is an array of { amount, estimatedDays }, must sum to bidAmount.
 */
export const submitBid = async (page, projectTitle, { bidAmount, phases }) => {
  await loginAsArchitect(page)
  await page.goto(ROUTES.architectOpportunities)

  const card = page.locator('div').filter({ hasText: projectTitle }).last()
  await expect(card).toBeVisible({ timeout: 10000 })
  await card.click()
  await expect(page).toHaveURL(/\/architect\/opportunities\/\d+$/)

  await page.getByRole('button', { name: 'Buat Penawaran' }).click()
  await expect(page).toHaveURL(/\/bid$/)

  const numberInputs = page.locator('input[type="number"]')
  await numberInputs.nth(0).fill(String(bidAmount)) // Bid Amount
  await page
    .locator(
      'textarea[placeholder="Explain your design concept, key features, and how you\'ll address the client\'s needs..."]'
    )
    .fill('A modern, industrial-style residential design optimized for the site and client requirements.')

  // PaymentPhaseBuilder seeds one phase by default; add more via "+ Tambah Fase".
  for (let i = 1; i < phases.length; i++) {
    await page.getByRole('button', { name: '+ Tambah Fase' }).click()
  }

  for (let i = 0; i < phases.length; i++) {
    const amountInput = numberInputs.nth(1 + i * 3)
    const estimatedDaysInput = numberInputs.nth(1 + i * 3 + 2)
    await amountInput.fill(String(phases[i].amount))
    await estimatedDaysInput.fill(String(phases[i].estimatedDays))
  }

  await page.getByRole('button', { name: 'Kirim Penawaran' }).click()
  await expect(page).toHaveURL(/\/architect\/bids/, { timeout: 15000 })
}

/** Client accepts the submitted bid for the given project (project -> NEGOTIATION). */
export const acceptBid = async (page, projectId) => {
  await loginAsClient(page)
  await page.goto(`/client/projects/${projectId}`)

  await expect(page.getByText('Daftar Penawaran')).toBeVisible({ timeout: 10000 })

  page.once('dialog', dialog => dialog.accept())
  await page.getByRole('button', { name: 'Terima Penawaran' }).click()

  await expect(page).toHaveURL(new RegExp(`/client/projects/${projectId}/finalization`), { timeout: 10000 })
}
