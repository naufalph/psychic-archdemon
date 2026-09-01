import { test, expect } from '@playwright/test'
import { TEST_USERS, API_BASE_URL } from './helpers/fixtures.js'
import { loginAsClient, loginAsArchitect } from './helpers/auth.js'
import { resetArchitectQuota, ensureArchitectIdentityComplete, setPhaseStatus, querySql } from './helpers/db.js'
import { createApprovedOpenProject, submitBid, acceptBid } from './helpers/scenario.js'

test('per-deliverable approval flips the phase only on the last one', async ({ page }) => {
  const title = `Deliverable probe ${Date.now()}`
  resetArchitectQuota(TEST_USERS.architect.email)
  ensureArchitectIdentityComplete(TEST_USERS.architect.email)

  const projectId = await createApprovedOpenProject(page, title)
  await submitBid(page, projectId, {
    bidAmount: 50000000,
    phases: [{ amount: 50000000, estimatedDays: 10, deliverables: ['Site Plan', 'Floor Plan'] }]
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
  const phases = await (await page.request.get(`${API_BASE_URL}/rmtr/projects/${projectId}/phases`, { headers: clientAuth })).json()
  const phaseId = phases.data[0].id

  setPhaseStatus(phaseId, 'IN_PROGRESS')

  // Architect uploads one file per deliverable, tagged by index.
  const archAuth = { Authorization: `Bearer ${await loginAsArchitect(page)}` }
  for (const idx of [0, 1]) {
    const up = await page.request.post(`${API_BASE_URL}/rmtr/phases/${phaseId}/deliverables/upload`, {
      headers: archAuth,
      multipart: {
        file: { name: `d${idx}.txt`, mimeType: 'text/plain', buffer: Buffer.from(`deliverable ${idx}`) },
        description: `file for deliverable ${idx}`,
        deliverableIndex: String(idx)
      }
    })
    expect(up.ok(), await up.text()).toBeTruthy()
  }
  const review = await page.request.post(`${API_BASE_URL}/rmtr/phases/${phaseId}/submit-for-review`, { headers: archAuth })
  expect(review.ok(), await review.text()).toBeTruthy()

  const cAuth = { Authorization: `Bearer ${await loginAsClient(page)}` }
  const readPhase = async () => {
    const r = await page.request.get(`${API_BASE_URL}/rmtr/projects/${projectId}/phases`, { headers: cAuth })
    return (await r.json()).data[0]
  }

  let p = await readPhase()
  expect(p.deliverableItems.every(i => i.status === 'PENDING')).toBe(true)
  // Each file landed under its own deliverable rather than loose on the phase.
  expect(p.deliverableItems.map(i => i.files.length)).toEqual([1, 1])

  let r = await page.request.post(`${API_BASE_URL}/rmtr/phases/${phaseId}/deliverables/0/approve`, { headers: cAuth })
  expect(r.ok(), await r.text()).toBeTruthy()
  p = await readPhase()
  expect(p.status).toBe('DELIVERED')

  r = await page.request.post(`${API_BASE_URL}/rmtr/phases/${phaseId}/deliverables/1/approve`, { headers: cAuth })
  expect(r.ok(), await r.text()).toBeTruthy()
  p = await readPhase()
  expect(p.status).toBe('APPROVED')

  // Re-approving after the phase has flipped is rejected, because the phase is no longer
  // DELIVERED. Either way no duplicate row can exist: the unique constraint guarantees that,
  // which is why the approve path needs no lock.
  r = await page.request.post(`${API_BASE_URL}/rmtr/phases/${phaseId}/deliverables/1/approve`, { headers: cAuth })
  expect(r.status()).toBe(409)

  const rows = querySql(
    `SELECT count(*) FROM rmtr_project_phase_deliverable_approval WHERE phase_id = ${phaseId};`
  )
  expect(rows).toBe('2')

  // One event per deliverable, plus one from the phase-level approval that the last
  // deliverable triggers -- it reuses the same action name.
  const events = querySql(
    `SELECT count(*) FROM rmtr_project_phase_log WHERE phase_id = ${phaseId} AND action = 'DELIVERABLE_APPROVED';`
  )
  expect(Number(events)).toBe(3)
})
