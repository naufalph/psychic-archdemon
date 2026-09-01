import { test, expect } from '@playwright/test'
import { TEST_USERS, API_BASE_URL } from './helpers/fixtures.js'
import { loginAsClient, loginAsArchitect } from './helpers/auth.js'
import { resetArchitectQuota, ensureArchitectIdentityComplete } from './helpers/db.js'
import { createApprovedOpenProject, submitBid, acceptBid } from './helpers/scenario.js'

test('contract endpoint returns schedule, terms, winning bid and ledger transactions', async ({ page }) => {
  const title = `Contract probe ${Date.now()}`
  resetArchitectQuota(TEST_USERS.architect.email)
  ensureArchitectIdentityComplete(TEST_USERS.architect.email)

  const projectId = await createApprovedOpenProject(page, title)
  await submitBid(page, projectId, {
    bidAmount: 90000000,
    phases: [
      { amount: 30000000, estimatedDays: 10, deliverables: ['Site Plan'] },
      { amount: 60000000, estimatedDays: 20, deliverables: ['Floor Plan', 'Elevations'] }
    ]
  })
  await acceptBid(page, projectId)

  const ct = await loginAsClient(page)
  await page.request.post(`${API_BASE_URL}/rmtr/projects/${projectId}/confirm-negotiation`, {
    headers: { Authorization: `Bearer ${ct}` }
  })
  const at = await loginAsArchitect(page)
  await page.request.post(`${API_BASE_URL}/rmtr/projects/${projectId}/architect-confirm-negotiation`, {
    headers: { Authorization: `Bearer ${at}` }
  })

  const clientAuth = { Authorization: `Bearer ${await loginAsClient(page)}` }

  // Create an invoice so there is a real ledger event to read back.
  const summary = await page.request.get(`${API_BASE_URL}/rmtr/payments/projects/${projectId}`, { headers: clientAuth })
  const bidPhase = ((await summary.json()).data ?? [])[0]
  const inv = await page.request.post(`${API_BASE_URL}/rmtr/payments/phases/${bidPhase.phaseId}`, { headers: clientAuth })
  expect(inv.ok(), await inv.text()).toBeTruthy()

  const res = await page.request.get(`${API_BASE_URL}/rmtr/projects/${projectId}/contract`, { headers: clientAuth })
  expect(res.ok(), await res.text()).toBeTruthy()
  const c = (await res.json()).data


  expect(Number(c.totalValue)).toBe(90000000)
  expect(c.paymentSchedule).toHaveLength(2)
  expect(c.paymentSchedule.map(p => Number(p.share))).toEqual([33.3, 66.7])
  expect(c.winningBid.architectName).toBeTruthy()
  expect(c.agreementTerms.phaseCount).toBe(2)
  // Transactions come from the append-only ledger, so creating the invoice must have left an
  // event behind -- the first event in a payment's life, which used to go unrecorded.
  expect(c.transactions.map(t => [t.action, t.direction])).toContainEqual(['INVOICE_CREATED', 'IN'])

  // The winning architect reads the same contract. This needs an explicit security rule: the
  // broad /rmtr/projects/** CLIENT-only matcher would otherwise 403 them.
  const archAuth = { Authorization: `Bearer ${await loginAsArchitect(page)}` }
  const asArchitect = await page.request.get(`${API_BASE_URL}/rmtr/projects/${projectId}/contract`, { headers: archAuth })
  expect(asArchitect.ok()).toBeTruthy()
})
