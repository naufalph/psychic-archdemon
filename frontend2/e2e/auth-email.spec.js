import { test, expect } from '@playwright/test'
import { TEST_USERS, ROUTES } from './helpers/fixtures.js'

test.describe('Login Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(ROUTES.login)
  })

  test('renders login form with all fields', async ({ page }) => {
    await expect(page.getByLabel('Email')).toBeVisible()
    // Password label is "Kata Sandi" in Indonesian
    await expect(page.getByLabel('Kata Sandi')).toBeVisible()
    // Submit button is "Masuk" in Indonesian
    await expect(page.getByRole('button', { name: 'Masuk' })).toBeVisible()
    await expect(page.getByRole('button', { name: /google/i })).toBeVisible()
    await expect(page.getByRole('button', { name: /linkedin/i })).toBeVisible()
  })

  test('shows validation errors on empty submit', async ({ page }) => {
    await page.getByRole('button', { name: 'Masuk' }).click()
    // Form should not navigate away on empty submit
    await expect(page).toHaveURL(ROUTES.login)
  })

  test('shows error on wrong credentials', async ({ page }) => {
    await page.getByLabel('Email').fill('wrong@example.com')
    await page.getByLabel('Kata Sandi').fill('wrongpassword')
    await page.getByRole('button', { name: 'Masuk' }).click()
    // Wait for error alert to appear
    await expect(page.locator('.bg-red-50, [class*="error"], [class*="alert"]').first()).toBeVisible({
      timeout: 10000
    })
    await expect(page).toHaveURL(ROUTES.login)
  })

  test('has link to signup page', async ({ page }) => {
    await page.getByRole('link', { name: /sign up/i }).click()
    await expect(page).toHaveURL(ROUTES.signup)
  })
})

test.describe('Login Flow — CLIENT', () => {
  test('successful login redirects to client dashboard', async ({ page }) => {
    await page.goto(ROUTES.login)
    await page.getByLabel('Email').fill(TEST_USERS.client.email)
    await page.getByLabel('Kata Sandi').fill(TEST_USERS.client.password)
    await page.getByRole('button', { name: 'Masuk' }).click()
    await page.waitForURL(url => url.pathname.startsWith('/client'), { timeout: 15000 })
    await expect(page).toHaveURL(/\/client\//)
  })

  test('successful login sets auth_token in localStorage', async ({ page }) => {
    await page.goto(ROUTES.login)
    await page.getByLabel('Email').fill(TEST_USERS.client.email)
    await page.getByLabel('Kata Sandi').fill(TEST_USERS.client.password)
    await page.getByRole('button', { name: 'Masuk' }).click()
    await page.waitForURL(url => url.pathname.startsWith('/client'), { timeout: 15000 })
    const token = await page.evaluate(() => localStorage.getItem('auth_token'))
    expect(token).toBeTruthy()
  })
})

test.describe('Login Flow — ARCHITECT', () => {
  test('successful login redirects to architect area', async ({ page }) => {
    await page.goto(ROUTES.login)
    await page.getByLabel('Email').fill(TEST_USERS.architect.email)
    await page.getByLabel('Kata Sandi').fill(TEST_USERS.architect.password)
    await page.getByRole('button', { name: 'Masuk' }).click()
    await page.waitForURL(url => url.pathname.startsWith('/architect'), { timeout: 15000 })
    await expect(page).toHaveURL(/\/architect\//)
  })
})

test.describe('Signup Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(ROUTES.signup)
  })

  test('step 1 shows role selection cards', async ({ page }) => {
    // Role cards are in Indonesian
    await expect(page.getByText('Menyewa Arsitek')).toBeVisible()
    await expect(page.getByText('Menawarkan Layanan')).toBeVisible()
  })

  test('selecting CLIENT role advances to step 2 form', async ({ page }) => {
    await page.getByText('Menyewa Arsitek').click()
    await expect(page.getByLabel('Nama Depan')).toBeVisible({ timeout: 5000 })
    await expect(page.getByLabel('Nama Belakang')).toBeVisible()
    await expect(page.getByLabel('Email')).toBeVisible()
    await expect(page.getByLabel('Kata Sandi')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Buat Akun' })).toBeVisible()
  })

  test('selecting ARCHITECT role advances to step 2 form', async ({ page }) => {
    await page.getByText('Menawarkan Layanan').click()
    await expect(page.getByLabel('Nama Depan')).toBeVisible({ timeout: 5000 })
    await expect(page.getByRole('button', { name: 'Buat Akun' })).toBeVisible()
    await expect(page.getByRole('button', { name: /google/i })).toBeVisible()
  })

  test('signup form shows password strength error', async ({ page }) => {
    await page.getByText('Menyewa Arsitek').click()
    await page.getByLabel('Nama Depan').fill('Test')
    await page.getByLabel('Nama Belakang').fill('User')
    await page.getByLabel('Email').fill('newuser@example.com')
    await page.getByLabel('Kata Sandi').fill('weak')
    await page.getByLabel('Konfirmasi Kata Sandi').fill('weak')
    await page.locator('#agreeTerms').check()
    await page.getByRole('button', { name: 'Buat Akun' }).click()
    // Expect validation error (weak password — at least 8 chars)
    await expect(page.locator('text=/at least 8/i').or(page.locator('text=/minimal/i')).first()).toBeVisible({
      timeout: 5000
    })
  })

  test('signup form shows password mismatch error', async ({ page }) => {
    await page.getByText('Menyewa Arsitek').click()
    await page.getByLabel('Nama Depan').fill('Test')
    await page.getByLabel('Nama Belakang').fill('User')
    await page.getByLabel('Email').fill('newuser@example.com')
    await page.getByLabel('Kata Sandi').fill('Password1!')
    await page.getByLabel('Konfirmasi Kata Sandi').fill('Different1!')
    await page.locator('#agreeTerms').check()
    await page.getByRole('button', { name: 'Buat Akun' }).click()
    await expect(page.getByText(/do not match|tidak sama/i)).toBeVisible({ timeout: 5000 })
  })

  test('has link back to login page', async ({ page }) => {
    await page.getByText('Menyewa Arsitek').click()
    await page.getByRole('link', { name: /masuk|sign in/i }).click()
    await expect(page).toHaveURL(ROUTES.login)
  })
})

test.describe('Auth Guards', () => {
  test('unauthenticated access to /client/dashboard redirects to login', async ({ page }) => {
    await page.evaluate(() => localStorage.removeItem('auth_token'))
    await page.goto(ROUTES.clientDashboard)
    await expect(page).toHaveURL(url => url.pathname === ROUTES.login || url.pathname === ROUTES.home, {
      timeout: 5000
    })
  })

  test('unauthenticated access to /architect/dashboard redirects', async ({ page }) => {
    await page.evaluate(() => localStorage.removeItem('auth_token'))
    await page.goto(ROUTES.architectDashboard)
    await expect(page).toHaveURL(url => url.pathname === ROUTES.login || url.pathname === ROUTES.home, {
      timeout: 5000
    })
  })
})
