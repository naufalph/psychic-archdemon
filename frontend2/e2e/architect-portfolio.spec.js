import { test, expect } from '@playwright/test'
import { ROUTES } from './helpers/fixtures.js'
import { loginAsArchitect } from './helpers/auth.js'

test.describe('Architect Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsArchitect(page)
    await page.goto(ROUTES.architectDashboard)
  })

  test('renders dashboard without crashing', async ({ page }) => {
    await expect(page).toHaveURL(ROUTES.architectDashboard)
    await expect(page.locator('main, [class*="dashboard"], h1, h2').first()).toBeVisible({ timeout: 10000 })
  })

  test('navbar is present', async ({ page }) => {
    await expect(page.locator('nav, header').first()).toBeVisible()
  })
})

test.describe('Architect Portfolios', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsArchitect(page)
    await page.goto(ROUTES.architectPortfolios)
  })

  test('portfolios page loads', async ({ page }) => {
    await expect(page).toHaveURL(ROUTES.architectPortfolios)
    await expect(page.locator('main, [class*="portfolio"]').first()).toBeVisible({ timeout: 10000 })
  })

  test('has add portfolio button', async ({ page }) => {
    const addBtn = page
      .getByRole('button', { name: /add|new|create|upload/i })
      .or(page.getByRole('link', { name: /add|new|create/i }))
    await expect(addBtn.first()).toBeVisible({ timeout: 10000 })
  })
})

test.describe('Opportunity List', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsArchitect(page)
    await page.goto(ROUTES.architectOpportunities)
  })

  test('opportunities page loads', async ({ page }) => {
    await expect(page).toHaveURL(ROUTES.architectOpportunities)
    await expect(page.locator('main, [class*="opportunit"]').first()).toBeVisible({ timeout: 10000 })
  })

  test('shows project cards or empty state', async ({ page }) => {
    // Either project cards are visible, or an empty state message
    const hasCards = page.locator('[class*="card"], [class*="project"]').first()
    const hasEmpty = page.getByText(/no projects|no opportunities|empty/i).first()
    await expect(hasCards.or(hasEmpty)).toBeVisible({ timeout: 10000 })
  })
})

test.describe('My Bids', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsArchitect(page)
    await page.goto(ROUTES.architectBids)
  })

  test('bids page loads', async ({ page }) => {
    await expect(page).toHaveURL(ROUTES.architectBids)
    await expect(page.locator('main').first()).toBeVisible({ timeout: 10000 })
  })
})

test.describe('Architect Profile', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsArchitect(page)
    await page.goto(ROUTES.architectProfile)
  })

  test('profile page loads', async ({ page }) => {
    await expect(page).toHaveURL(ROUTES.architectProfile)
    await expect(page.locator('main, form, [class*="profile"]').first()).toBeVisible({ timeout: 10000 })
  })
})

test.describe('Architect Navigation', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsArchitect(page)
  })

  test('can navigate from dashboard to opportunities', async ({ page }) => {
    await page.goto(ROUTES.architectDashboard)
    const oppsLink = page.getByRole('link', { name: /opportunit/i }).first()
    await expect(oppsLink).toBeVisible({ timeout: 10000 })
    await oppsLink.click()
    await expect(page).toHaveURL(/\/architect\/opportunities/)
  })

  test('can navigate from dashboard to portfolios', async ({ page }) => {
    await page.goto(ROUTES.architectDashboard)
    const portfolioLink = page.getByRole('link', { name: /portfolio/i }).first()
    await expect(portfolioLink).toBeVisible({ timeout: 10000 })
    await portfolioLink.click()
    await expect(page).toHaveURL(/\/architect\/portfolios/)
  })
})
