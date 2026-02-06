import { test, expect } from '@playwright/test'
import { ROUTES } from './helpers/fixtures'
import { simulateOAuthCallback, waitForOAuthRedirect } from './helpers/oauth'

test.describe('OAuth Callback Component', () => {
  test('shows success state and redirects to client dashboard', async ({ page }) => {
    await simulateOAuthCallback(page, {
      success: true,
      token: 'mock-jwt-token',
      email: 'test@example.com',
      id: '123',
      roles: 'CLIENT'
    })

    await expect(page.getByText('Login Successful!')).toBeVisible()
    await waitForOAuthRedirect(page, ROUTES.clientDashboard, 5000)
  })

  test('shows success state and redirects to architect dashboard', async ({ page }) => {
    await simulateOAuthCallback(page, {
      success: true,
      token: 'mock-jwt-token',
      email: 'architect@example.com',
      id: '456',
      roles: 'ARCHITECT'
    })

    await expect(page.getByText('Login Successful!')).toBeVisible()
    await waitForOAuthRedirect(page, ROUTES.architectDashboard, 5000)
  })

  test('shows error state when success=false', async ({ page }) => {
    await simulateOAuthCallback(page, {
      success: false,
      error: 'OAuth authentication failed'
    })

    await expect(page.getByText('Login Failed')).toBeVisible()
    await expect(page.getByText('OAuth authentication failed')).toBeVisible()
    await expect(page.getByRole('button', { name: /return to login/i })).toBeVisible()
  })

  test('return to login button navigates correctly', async ({ page }) => {
    await simulateOAuthCallback(page, {
      success: false,
      error: 'Test error'
    })

    await page.getByRole('button', { name: /return to login/i }).click()
    await expect(page).toHaveURL(ROUTES.login)
  })

  test('stores auth data in localStorage on success', async ({ page }) => {
    await simulateOAuthCallback(page, {
      success: true,
      token: 'test-jwt-token-123',
      email: 'store-test@example.com',
      id: '789',
      roles: 'CLIENT'
    })

    const token = await page.evaluate(() => localStorage.getItem('auth_token'))
    expect(token).toBe('test-jwt-token-123')
  })
})

test.describe('Signup Page OAuth Buttons', () => {
  test('signup page shows OAuth buttons after role selection', async ({ page }) => {
    await page.goto(ROUTES.signup)

    await page.getByText('Offer Services').click()
    await page.waitForTimeout(500)

    await expect(page.getByRole('button', { name: /google/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /linkedin/i })).toBeVisible()
  })
})

test.describe('Login Page OAuth Buttons', () => {
  test('login page shows OAuth buttons', async ({ page }) => {
    await page.goto(ROUTES.login)

    await expect(page.getByRole('button', { name: /google/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /linkedin/i })).toBeVisible()
  })
})
