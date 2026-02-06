import { OAUTH_TEST_USERS, ROUTES } from './fixtures'

export const hasGoogleCredentials = () => {
  return OAUTH_TEST_USERS.google.email && OAUTH_TEST_USERS.google.password
}

export const hasLinkedInCredentials = () => {
  return OAUTH_TEST_USERS.linkedin.email && OAUTH_TEST_USERS.linkedin.password
}

export const loginWithGoogleUI = async (page, role = 'CLIENT') => {
  if (!hasGoogleCredentials()) {
    throw new Error('Google OAuth credentials not configured')
  }

  const startPage = role === 'ARCHITECT' ? ROUTES.signup : ROUTES.login
  await page.goto(startPage)

  if (role === 'ARCHITECT') {
    await page.getByText('Offer Services').click()
    await page.waitForTimeout(500)
  }

  const googleButton = page.getByRole('button', { name: /google/i })
  await googleButton.click()

  await page.waitForURL(/accounts\.google\.com/, { timeout: 10000 })

  await page.fill('input[type="email"]', OAUTH_TEST_USERS.google.email)
  await page.click('#identifierNext')
  await page.waitForTimeout(2000)

  await page.fill('input[type="password"]', OAUTH_TEST_USERS.google.password)
  await page.click('#passwordNext')

  await page.waitForURL(/\/auth\/callback/, { timeout: 30000 })
}

export const simulateOAuthCallback = async (page, params) => {
  const {
    success = true,
    token = 'mock-jwt-token',
    email = 'test@example.com',
    id = '123',
    roles = 'CLIENT',
    error = ''
  } = params

  const queryParams = new URLSearchParams()
  queryParams.set('success', success.toString())

  if (success) {
    queryParams.set('token', token)
    queryParams.set('email', email)
    queryParams.set('id', id)
    queryParams.set('roles', roles)
  } else if (error) {
    queryParams.set('error', error)
  }

  await page.goto(`${ROUTES.authCallback}?${queryParams.toString()}`)
}

export const waitForOAuthRedirect = async (page, expectedPath, timeout = 15000) => {
  await page.waitForURL(url => url.pathname === expectedPath, { timeout })
}
