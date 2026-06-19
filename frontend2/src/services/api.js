import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import en from '@/locales/en'
import id from '@/locales/id'

const localeMessages = { en, id }

const translateErrorCode = code => {
  const currentLocale = localStorage.getItem('locale') || 'id'
  return (
    localeMessages[currentLocale]?.errors?.[code] || localeMessages['en']?.errors?.[code] || null
  )
}

// Create axios instance with default configuration
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/',
  timeout: 30000, // 30 seconds
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json'
  }
})

// Request interceptor to add auth token
api.interceptors.request.use(
  config => {
    // Get token from localStorage directly to avoid circular dependency
    const token = localStorage.getItem('auth_token')

    // Add auth token if available
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // Add request timestamp for debugging
    if (import.meta.env.DEV) {
      config.metadata = { startTime: new Date() }
      console.log(
        `[API Request] ${config.method?.toUpperCase()} ${config.url}`,
        config.data || config.params
      )
    }

    return config
  },
  error => {
    console.error('[API Request Error]', error)
    return Promise.reject(error)
  }
)

// Response interceptor to handle common responses
api.interceptors.response.use(
  response => {
    // Log response time in development
    if (import.meta.env.DEV && response.config.metadata) {
      const duration = new Date() - response.config.metadata.startTime
      console.log(
        `[API Response] ${response.config.method?.toUpperCase()} ${response.config.url} - ${response.status} (${duration}ms)`
      )
    }

    return response
  },
  async error => {
    const authStore = useAuthStore()
    const originalRequest = error.config

    // Log error in development
    if (import.meta.env.DEV) {
      console.error('[API Error]', error.response?.status, error.response?.data)
    }

    // Translate BusinessException errorCode into a localized message
    if (error.response?.data?.errorCode) {
      const translated = translateErrorCode(error.response.data.errorCode)
      if (translated) {
        error.response.data.message = translated
      }
    }

    // Handle 401 Unauthorized errors (JWT token expired or invalid)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      // For stateless JWT, clear auth and redirect to login
      console.warn('JWT token expired or invalid, redirecting to login')

      // Clear auth data using the store method
      authStore.clearAuth()

      // Redirect to login if not already there
      if (window.location.pathname !== '/login' && window.location.pathname !== '/') {
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
      }

      return Promise.reject(error)
    }

    // Handle 403 Forbidden errors
    if (error.response?.status === 403) {
      console.warn('Access forbidden - insufficient permissions')
      // You might want to show a notification or redirect
    }

    // Handle 404 Not Found errors
    if (error.response?.status === 404) {
      console.warn('Resource not found')
    }

    // Handle 422 Validation errors
    if (error.response?.status === 422) {
      console.warn('Validation error:', error.response.data)
    }

    // Handle 429 Rate Limiting
    if (error.response?.status === 429) {
      console.warn('Rate limit exceeded')
      // You might want to implement retry logic here
    }

    // Handle 500 Server errors
    if (error.response?.status >= 500) {
      console.error('Server error occurred')
      // You might want to show a generic error message
    }

    // Network errors (no response)
    if (!error.response) {
      console.error('Network error - no response received')
      // Handle offline scenarios
    }

    return Promise.reject(error)
  }
)

// Helper function to handle file uploads
export const uploadFile = async (endpoint, file, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)

  return api.post(endpoint, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: progressEvent => {
      if (onProgress && progressEvent.total) {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(progress)
      }
    }
  })
}

// Helper function to download files
export const downloadFile = async (endpoint, filename) => {
  try {
    const response = await api.get(endpoint, {
      responseType: 'blob'
    })

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)

    return response
  } catch (error) {
    console.error('File download failed:', error)
    throw error
  }
}

// API endpoints organized by resource
export const authAPI = {
  // Email login with credentials
  login: credentials => api.post('/rmtr/users/login', credentials),

  // Email registration with email verification
  register: userData => api.post('/rmtr/users/register', userData),

  // Email verification callback
  verifyEmail: token => api.get(`/rmtr/users/verify-email?token=${token}`),

  // Resend email verification
  resendVerification: email => api.post(`/rmtr/users/resend-verification?email=${email}`),

  // Google OAuth endpoints
  getGoogleAuthUrl: role => api.get('/rmtr/users/oauth2/google', { params: { role } }),
  googleCallback: code => api.get(`/rmtr/users/oauth2/callback/google?code=${code}`),

  // LinkedIn OAuth endpoints
  getLinkedInAuthUrl: role => api.get('/rmtr/users/oauth2/linkedin', { params: { role } }),
  linkedinCallback: code => api.get(`/rmtr/users/oauth2/callback/linkedin?code=${code}`),

  // Get current authenticated user
  getCurrentUser: () => api.get('/rmtr/users/me'),

  // Role activation
  activateRole: role => api.post(`/rmtr/users/me/activate-role?role=${role}`),

  // Update last login role
  updateLastLoginRole: role => api.put('/rmtr/users/me/last-login-role', null, { params: { role } })
}

export const userAPI = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: profileData => api.put('/user/profile', profileData),
  uploadAvatar: (file, onProgress) => uploadFile('/user/avatar', file, onProgress),
  getSettings: () => api.get('/user/settings'),
  updateSettings: settings => api.put('/user/settings', settings)
}

export const notificationAPI = {
  getAll: () => api.get('/rmtr/notifications'),
  getUnread: () => api.get('/rmtr/notifications/unread'),
  getUnreadCount: () => api.get('/rmtr/notifications/unread-count'),
  markAsRead: notificationId => api.put(`/rmtr/notifications/${notificationId}/read`),
  markAllAsRead: () => api.put('/rmtr/notifications/read-all')
}

export const projectAPI = {
  getAll: params => api.get('/rmtr/projects', { params }),
  getById: id => api.get(`/rmtr/projects/${id}`),
  getProjectForArchitect: id => api.get(`/rmtr/projects/${id}/for-architect`),
  create: formData =>
    api.post('/rmtr/projects', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  update: (id, projectData) => api.put(`/rmtr/projects/${id}`, projectData),
  delete: id => api.delete(`/rmtr/projects/${id}`),
  getOpenProjects: (params = {}) =>
    api.get('/rmtr/projects/open', { params: { ...params, excludeOwnProjects: true } }),
  uploadFiles: (id, files, onProgress) => {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return api.post(`/rmtr/projects/${id}/files`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: progressEvent => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    })
  },
  confirmNegotiation: projectId => api.post(`/rmtr/projects/${projectId}/confirm-negotiation`),
  rejectNegotiation: projectId => api.post(`/rmtr/projects/${projectId}/reject-negotiation`),
  architectConfirmNegotiation: projectId =>
    api.post(`/rmtr/projects/${projectId}/architect-confirm-negotiation`),
  initializePhases: projectId => api.post(`/rmtr/projects/${projectId}/initialize-phases`),
  getPublicPreviews: () => api.get('/rmtr/projects/public-preview')
}

export const chatAPI = {
  getMyConversations: () => api.get('/rmtr/chat/conversations'),
  getConversation: id => api.get(`/rmtr/chat/conversations/${id}`),
  getMessages: (id, page = 0, size = 50) =>
    api.get(`/rmtr/chat/conversations/${id}/messages`, { params: { page, size } }),
  sendMessage: data => api.post('/rmtr/chat/messages', data),
  markRead: msgId => api.put(`/rmtr/chat/messages/${msgId}/read`),
  markAllRead: conversationId => api.put(`/rmtr/chat/conversations/${conversationId}/read-all`)
}

export const architectAPI = {
  getById: id => api.get(`/architects/${id}`),
  getProfile: () => api.get('/rmtr/architects/profile'),
  getPortfolio: id => api.get(`/architects/${id}/portfolio`),
  updatePortfolio: portfolioData => api.put('/architects/portfolio', portfolioData),
  updateOnboardingProfile: profileData =>
    api.put('/rmtr/architects/onboarding-profile', profileData),
  updateFullProfile: profileData => api.put('/rmtr/architects/profile', profileData),
  sendPhoneOtp: data => api.post('/rmtr/architects/phone/send-otp', data),
  verifyPhoneOtp: data => api.post('/rmtr/architects/phone/verify-otp', data)
}

export const portfolioAPI = {
  getAll: () => api.get('/rmtr/porto'),
  getById: id => api.get(`/rmtr/porto/${id}`),
  create: formData =>
    api.post('/rmtr/porto', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  update: (id, data) => api.put(`/rmtr/porto/${id}`, data),
  delete: id => api.delete(`/rmtr/porto/${id}`),
  addImages: (id, images) => {
    const formData = new FormData()
    images.forEach(file => formData.append('images', file))
    return api.post(`/rmtr/porto/${id}/images`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  deleteImage: imageId => api.delete(`/rmtr/porto/images/${imageId}`)
}

export const clientAPI = {
  getProfile: () => api.get('/rmtr/clients/profile'),
  updateProfile: profileData => api.put('/rmtr/clients/profile', profileData)
}

export const bidAPI = {
  getMyBids: () => api.get('/rmtr/bids/my-bids'),
  getBid: id => api.get(`/rmtr/bids/${id}`),
  getProjectBids: projectId => api.get(`/rmtr/projects/${projectId}/bids`),
  createDraftBid: bidData => api.post('/rmtr/bids', bidData),
  updateDraftBid: (bidId, bidData) => api.put(`/rmtr/bids/${bidId}`, bidData),
  updateBidDetails: (bidId, detailsData) => api.put(`/rmtr/bids/${bidId}/details`, detailsData),
  submitBid: bidId => api.post(`/rmtr/bids/${bidId}/submit`),
  withdrawBid: bidId => api.put(`/rmtr/bids/${bidId}/withdraw`),
  acceptBid: bidId => api.post(`/rmtr/bids/${bidId}/accept`),
  getQuota: () => api.get('/rmtr/bids/quota'),
  linkPortfolios: (bidId, portfolioIds) =>
    api.post(`/rmtr/bids/${bidId}/portfolios`, { portfolioIds }),
  uploadBidImages: (bidId, imageType, files, onProgress) => {
    const fd = new FormData()
    files.forEach(f => fd.append('images', f))
    return api.post(`/rmtr/bids/${bidId}/images/${imageType}`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: e => {
        if (onProgress && e.total) onProgress(Math.round((e.loaded * 100) / e.total))
      }
    })
  },
  deleteImage: imageId => api.delete(`/rmtr/bids/images/${imageId}`)
}

export const supportAPI = {
  createSupportConversation: (projectId, bidId) =>
    api.post('/rmtr/support/conversations', { projectId, bidId }),
  getSupportConversations: () => api.get('/rmtr/support/conversations')
}

export const tokenPurchaseAPI = {
  getPricing: () => api.get('/rmtr/tokens/pricing'),
  initiatePurchase: quantity => api.post('/rmtr/tokens', { quantity }),
  getPurchaseById: id => api.get(`/rmtr/tokens/${id}`),
  getPurchaseHistory: (page = 0, size = 10) =>
    api.get('/rmtr/tokens/history', { params: { page, size } })
}

export const paymentAPI = {
  getProjectPhasePayments: projectId => api.get(`/rmtr/payments/projects/${projectId}`),
  initiatePhasePayment: phaseId => api.post(`/rmtr/payments/phases/${phaseId}`)
}

export const phaseAPI = {
  getPhases: projectId => api.get(`/rmtr/projects/${projectId}/phases`),
  getPhase: (projectId, phaseId) => api.get(`/rmtr/projects/${projectId}/phases/${phaseId}`),
  billPhase: phaseId => api.post(`/rmtr/phases/${phaseId}/bill`),
  uploadDeliverable: (phaseId, data) => api.post(`/rmtr/phases/${phaseId}/deliverables`, data),
  uploadDeliverableFile: (phaseId, formData) =>
    api.post(`/rmtr/phases/${phaseId}/deliverables/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  submitForReview: phaseId => api.post(`/rmtr/phases/${phaseId}/submit-for-review`),
  approveDeliverable: phaseId => api.post(`/rmtr/phases/${phaseId}/approve`),
  requestRevision: (phaseId, data = {}) =>
    api.post(`/rmtr/phases/${phaseId}/request-revision`, data),
  disputeDeliverable: (phaseId, data) => api.post(`/rmtr/phases/${phaseId}/dispute`, data),
  disburse: (phaseId, data) => api.post(`/rmtr/phases/${phaseId}/disburse`, data),
  getLogs: phaseId => api.get(`/rmtr/phases/${phaseId}/logs`)
}

// Default export
export default api
