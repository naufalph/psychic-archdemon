import { expect } from '@playwright/test'
import { API_BASE_URL } from './fixtures.js'
import { loginAsClient, loginAsArchitect, loginAsSuperuser } from './auth.js'

/**
 * Client creates a project, then superuser approves it (PENDING_APPROVAL -> OPEN).
 * Shared by every scenario that needs a biddable project as a starting point.
 * Returns the new project's id.
 */
export const createApprovedOpenProject = async (page, title) => {
  const clientToken = await loginAsClient(page)
  const clientAuth = { Authorization: `Bearer ${clientToken}` }

  // Setup goes through the API rather than the create form on purpose. The form's Location is
  // derived from city/province, which only the Google Places callback populates — so a
  // UI-driven setup depends on a live Maps API and breaks whenever a field is added.
  // The create form itself is covered by client-projects.spec.js.
  const draft = await page.request.post(`${API_BASE_URL}/rmtr/projects`, {
    headers: clientAuth,
    data: {
      title,
      location: 'Depok, Jawa Barat',
      fullAddress: 'Jl. Margonda Raya No. 1, Depok',
      city: 'Depok',
      province: 'Jawa Barat',
      projectScope: 'NEW_BUILD',
      projectCategory: 'RESIDENTIAL',
      buildingFunction: 'RESIDENTIAL',
      subCategory: 'HOUSE',
      lotSize: 200,
      estimatedBuildArea: 120,
      numberOfFloors: 2,
      scopeOfWork: '3 bedroom modern house, industrial style, e2e test project.',
      designBudgetMin: 300000000,
      designBudgetMax: 500000000
    }
  })
  expect(draft.ok(), await draft.text()).toBeTruthy()
  const projectId = (await draft.json()).data.id

  // submitProject is multipart with an optional files part; send it empty.
  const submitted = await page.request.post(
    `${API_BASE_URL}/rmtr/projects/${projectId}/submit`,
    { headers: clientAuth, multipart: {} }
  )
  expect(submitted.ok(), await submitted.text()).toBeTruthy()

  const superToken = await loginAsSuperuser(page)
  const validated = await page.request.put(
    `${API_BASE_URL}/rmtr/projects/${projectId}/validate`,
    { headers: { Authorization: `Bearer ${superToken}` }, data: { isValid: true } }
  )
  expect(validated.ok(), await validated.text()).toBeTruthy()

  return String(projectId)
}

export const submitBid = async (page, projectId, { bidAmount, phases }) => {
  const token = await loginAsArchitect(page)
  const auth = { Authorization: `Bearer ${token}` }

  // Driven through the API for the same reason as project setup: this is arrangement, not the
  // thing under test. The bid form's own UI flow is covered by full-bid-lifecycle.spec.js.
  const draft = await page.request.post(`${API_BASE_URL}/rmtr/bids`, {
    headers: auth,
    data: {
      projectId: Number(projectId),
      bidAmount,
      proposal:
        'A modern, industrial-style residential design optimized for the site and client requirements.'
    }
  })
  expect(draft.ok(), await draft.text()).toBeTruthy()
  const bidId = (await draft.json()).data.id

  const details = await page.request.put(`${API_BASE_URL}/rmtr/bids/${bidId}/details`, {
    headers: auth,
    data: {
      conceptStatement: 'Industrial-tropical hybrid, oriented for cross ventilation.',
      phases: phases.map((phase, i) => ({
        phaseNumber: i + 1,
        title: `Fase ${i + 1}`,
        // Real deliverable names: the workspace tags uploaded files to their index in this list.
        deliverables: phase.deliverables ?? ['Site Plan', 'Floor Plan'],
        amount: phase.amount,
        revisionRounds: phase.revisionRounds ?? 2,
        estimatedDays: phase.estimatedDays
      }))
    }
  })
  expect(details.ok(), await details.text()).toBeTruthy()

  const submitted = await page.request.post(`${API_BASE_URL}/rmtr/bids/${bidId}/submit`, {
    headers: auth
  })
  expect(submitted.ok(), await submitted.text()).toBeTruthy()

  return bidId
}

/** Client accepts the submitted bid for the given project (project -> NEGOTIATION). */
export const acceptBid = async (page, projectId) => {
  const token = await loginAsClient(page)
  const auth = { Authorization: `Bearer ${token}` }

  const list = await page.request.get(`${API_BASE_URL}/rmtr/projects/${projectId}/bids`, {
    headers: auth
  })
  expect(list.ok(), await list.text()).toBeTruthy()
  const bids = (await list.json()).data ?? []
  const pending = bids.find(b => b.status === 'PENDING') ?? bids[0]
  expect(pending, `no bid to accept on project ${projectId}`).toBeTruthy()

  const accepted = await page.request.post(`${API_BASE_URL}/rmtr/bids/${pending.id}/accept`, {
    headers: auth
  })
  expect(accepted.ok(), await accepted.text()).toBeTruthy()
}

/**
 * Confirmation moved from a native dialog to an in-page modal, so the page button and the
 * modal's confirm button share a label. Click the page one, then the modal one if it appears.
 */
export const confirmThroughModal = async (page, name) => {
  const buttons = page.getByRole('button', { name })
  await buttons.first().click()
  const modalButton = buttons.last()
  if (await modalButton.isVisible().catch(() => false)) {
    const count = await buttons.count()
    if (count > 1) await modalButton.click()
  }
}
