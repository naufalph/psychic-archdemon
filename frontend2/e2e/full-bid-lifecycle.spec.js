import { test, expect } from '@playwright/test'
import { TEST_USERS, API_BASE_URL } from './helpers/fixtures.js'
import { resetArchitectQuota, ensureArchitectIdentityComplete } from './helpers/db.js'
import { createApprovedOpenProject, submitBid, acceptBid } from './helpers/scenario.js'

// Critical-path test across all three roles: client creates a project, superuser
// approves it, an architect bids on it, and the client accepts the bid (project
// reaches NEGOTIATION). Tests run serially and share state via the variables below.
test.describe.serial('Full bid lifecycle: create -> approve -> bid -> accept', () => {
  const projectTitle = `E2E Bid Lifecycle ${Date.now()}`
  const bidAmount = 50000000
  let projectId

  test.beforeAll(() => {
    resetArchitectQuota(TEST_USERS.architect.email)
    ensureArchitectIdentityComplete(TEST_USERS.architect.email)
  })

  test('client creates a project and superuser approves it', async ({ page }) => {
    projectId = await createApprovedOpenProject(page, projectTitle)
  })

  test('architect submits a bid', async ({ page }) => {
    await submitBid(page, projectId, { bidAmount, phases: [{ amount: bidAmount, estimatedDays: 14 }] })
  })

  test('client accepts the bid and project moves to negotiation', async ({ page }) => {
    await acceptBid(page, projectId)

    const response = await page.request.get(`${API_BASE_URL}/rmtr/projects/${projectId}`, {
      headers: { Authorization: `Bearer ${await page.evaluate(() => localStorage.getItem('auth_token'))}` }
    })
    const body = await response.json()
    expect(body.data.status).toBe('NEGOTIATION')
  })
})
