import { test, expect } from '@playwright/test'
import { ROUTES, API_BASE_URL } from './helpers/fixtures.js'
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

test.describe('Delete Project', () => {
  // Regression guard for the status ledger work: deleting a project must remove it from the
  // client's list and keep it gone. The assertion is deliberately about *observable* absence,
  // not about how the row is stored, so it holds for both hard and soft delete.
  test('deleted project disappears from the client list and stays gone', async ({ page }) => {
    const token = await loginAsClient(page)
    const auth = { Authorization: `Bearer ${token}` }
    const title = `E2E delete probe ${Date.now()}`

    const created = await page.request.post(`${API_BASE_URL}/rmtr/projects`, {
      headers: auth,
      data: { title }
    })
    expect(created.ok(), await created.text()).toBeTruthy()
    const projectId = (await created.json()).data?.id
    expect(projectId).toBeTruthy()

    const listContains = async () => {
      const res = await page.request.get(`${API_BASE_URL}/rmtr/projects`, { headers: auth })
      expect(res.ok(), await res.text()).toBeTruthy()
      const projects = (await res.json()).data ?? []
      return projects.some(p => p.id === projectId)
    }

    expect(await listContains()).toBe(true)

    const deleted = await page.request.delete(`${API_BASE_URL}/rmtr/projects/${projectId}`, {
      headers: auth
    })
    expect(deleted.ok(), await deleted.text()).toBeTruthy()

    expect(await listContains()).toBe(false)
    // Re-fetch to catch a delete that only hides the row in a cached response.
    expect(await listContains()).toBe(false)
  })
})
