import { TEST_USERS, API_BASE_URL } from './fixtures.js'

/**
 * Log in via the backend API and inject the token into localStorage.
 * This is the fast path for tests that need auth state but don't need
 * to test the login UI itself.
 */
export const loginAs = async (page, role) => {
  const user = TEST_USERS[role.toLowerCase()]
  if (!user) throw new Error(`Unknown test role: ${role}`)

  const response = await page.request.post(`${API_BASE_URL}/rmtr/users/login`, {
    data: { email: user.email, password: user.password }
  })

  if (!response.ok()) {
    const body = await response.text()
    throw new Error(`Login API failed for ${role}: ${response.status()} — ${body}`)
  }

  const body = await response.json()
  const token = body.data?.token
  if (!token) throw new Error(`No token in login response for ${role}`)

  // Set token before navigating so the router guard finds it immediately
  await page.goto('/')
  await page.evaluate(t => localStorage.setItem('auth_token', t), token)

  return token
}

export const loginAsClient = page => loginAs(page, 'client')
export const loginAsArchitect = page => loginAs(page, 'architect')
export const loginAsSuperuser = page => loginAs(page, 'superuser')

export const clearAuth = async page => {
  await page.evaluate(() => localStorage.removeItem('auth_token'))
}
