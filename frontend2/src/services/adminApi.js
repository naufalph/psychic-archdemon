import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json', Accept: 'application/json' }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('auth_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Projects
export const adminProjectsAPI = {
  getAll: status => api.get('/rmtr/admin/projects', { params: status ? { status } : {} }),
  getDetail: projectId => api.get(`/rmtr/admin/projects/${projectId}`),
  forceCancel: projectId => api.post(`/rmtr/admin/projects/${projectId}/force-cancel`),
  overrideNegotiation: projectId =>
    api.post(`/rmtr/admin/projects/${projectId}/override-negotiation`)
}

// Phases / Disputes
export const adminPhasesAPI = {
  getDisputed: () => api.get('/rmtr/admin/phases/disputed'),
  resolveDispute: (phaseId, decision, note) =>
    api.post(`/rmtr/admin/phases/${phaseId}/resolve-dispute`, { decision, note })
}

// Users
export const adminUsersAPI = {
  getAll: (page = 0, size = 20) => api.get('/rmtr/admin/users', { params: { page, size } }),
  deactivate: userId => api.post(`/rmtr/admin/users/${userId}/deactivate`),
  reactivate: userId => api.post(`/rmtr/admin/users/${userId}/reactivate`)
}

// Landing page content
const multipart = { headers: { 'Content-Type': 'multipart/form-data' } }

export const adminLandingAPI = {
  getHeroSlides: () => api.get('/rmtr/admin/landing/hero-slides'),
  createHeroSlide: formData => api.post('/rmtr/admin/landing/hero-slides', formData, multipart),
  updateHeroSlide: (slideId, formData) =>
    api.put(`/rmtr/admin/landing/hero-slides/${slideId}`, formData, multipart),
  deleteHeroSlide: slideId => api.delete(`/rmtr/admin/landing/hero-slides/${slideId}`),
  reorderHeroSlides: orderedIds =>
    api.put('/rmtr/admin/landing/hero-slides/reorder', { orderedIds }),

  getPresets: () => api.get('/rmtr/admin/landing/presets'),
  createPreset: payload => api.post('/rmtr/admin/landing/presets', payload),
  updatePreset: (presetId, payload) => api.put(`/rmtr/admin/landing/presets/${presetId}`, payload),
  deletePreset: presetId => api.delete(`/rmtr/admin/landing/presets/${presetId}`),
  reorderPresets: orderedIds => api.put('/rmtr/admin/landing/presets/reorder', { orderedIds })
}

// Existing superuser project endpoints (from ProjectController)
export const superuserProjectsAPI = {
  getPendingApproval: () =>
    api.get('/rmtr/admin/projects', { params: { status: 'PENDING_APPROVAL' } }),
  validate: (projectId, isValid, validationNotes) =>
    api.put(`/rmtr/projects/${projectId}/validate`, { isValid, validationNotes })
}
