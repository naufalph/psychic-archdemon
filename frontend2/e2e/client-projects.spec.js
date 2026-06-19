import { test, expect } from '@playwright/test'
import { ROUTES } from './helpers/fixtures.js'
import { loginAsClient } from './helpers/auth.js'

test.describe('Client Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsClient(page)
    await page.goto(ROUTES.clientDashboard)
  })

  test('renders dashboard without crashing', async ({ page }) => {
    await expect(page).toHaveURL(ROUTES.clientDashboard)
    // Dashboard should show some content (not a blank page or error)
    await expect(page.locator('main, [class*="dashboard"], h1, h2').first()).toBeVisible({ timeout: 10000 })
  })

  test('navbar is present', async ({ page }) => {
    await expect(page.locator('nav, header').first()).toBeVisible()
  })
})

test.describe('Client Projects List', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsClient(page)
    await page.goto(ROUTES.clientProjects)
  })

  test('projects page loads', async ({ page }) => {
    await expect(page).toHaveURL(ROUTES.clientProjects)
    await expect(page.locator('main, [class*="project"]').first()).toBeVisible({ timeout: 10000 })
  })

  test('has create project button or link', async ({ page }) => {
    const createBtn = page
      .getByRole('button', { name: /create|new project/i })
      .or(page.getByRole('link', { name: /create|new project/i }))
    await expect(createBtn.first()).toBeVisible({ timeout: 10000 })
  })
})

test.describe('Create Project Form', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsClient(page)
    await page.goto(ROUTES.clientProjectCreate)
  })

  test('create project page loads', async ({ page }) => {
    await expect(page).toHaveURL(url => url.pathname.includes('create'))
    await expect(page.locator('form, [class*="form"]').first()).toBeVisible({ timeout: 10000 })
  })

  test('shows validation errors on empty submit', async ({ page }) => {
    const submitBtn = page.getByRole('button', { name: /submit|create|next/i }).first()
    await expect(submitBtn).toBeVisible({ timeout: 10000 })
    await submitBtn.click()
    // Should stay on the same page (validation prevents submit)
    await expect(page).toHaveURL(url => url.pathname.includes('create'))
  })
})

test.describe('Client Navigation', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsClient(page)
  })

  test('can navigate from dashboard to projects', async ({ page }) => {
    await page.goto(ROUTES.clientDashboard)
    const projectsLink = page.getByRole('link', { name: /projects/i }).first()
    await expect(projectsLink).toBeVisible({ timeout: 10000 })
    await projectsLink.click()
    await expect(page).toHaveURL(/\/client\/projects/)
  })

  test('client profile page loads', async ({ page }) => {
    await page.goto(ROUTES.clientProfile)
    await expect(page.locator('main, form, [class*="profile"]').first()).toBeVisible({ timeout: 10000 })
  })
})
