import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

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
  getAll: () => api.get('/api/notifications'),
  getUnread: () => api.get('/api/notifications/unread'),
  getUnreadCount: () => api.get('/api/notifications/unread-count'),
  markAsRead: notificationId => api.put(`/api/notifications/${notificationId}/read`),
  markAllAsRead: () => api.put('/api/notifications/read-all')
}

export const projectAPI = {
  getAll: params => api.get('/api/v1/projects', { params }),
  getById: id => api.get(`/api/v1/projects/${id}`),
  getProjectForArchitect: id => api.get(`/api/v1/projects/${id}/for-architect`),
  create: formData =>
    api.post('/api/v1/projects', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  update: (id, projectData) => api.put(`/api/v1/projects/${id}`, projectData),
  delete: id => api.delete(`/api/v1/projects/${id}`),
  getOpenProjects: (params = {}) =>
    api.get('/api/v1/projects/open', { params: { ...params, excludeOwnProjects: true } }),
  uploadFiles: (id, files, onProgress) => {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return api.post(`/api/v1/projects/${id}/files`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: progressEvent => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    })
  }
}

export const architectAPI = {
  getAll: params => api.get('/architects', { params }),
  getById: id => api.get(`/architects/${id}`),
  getProfile: () => api.get('/rmtr/architects/profile'),
  getPortfolio: id => api.get(`/architects/${id}/portfolio`),
  updatePortfolio: portfolioData => api.put('/architects/portfolio', portfolioData),
  updateOnboardingProfile: profileData =>
    api.put('/rmtr/architects/onboarding-profile', profileData),
  updateFullProfile: profileData => api.put('/rmtr/architects/profile', profileData),
  getReviews: id => api.get(`/architects/${id}/reviews`),
  addReview: (id, reviewData) => api.post(`/architects/${id}/reviews`, reviewData)
}

export const portfolioAPI = {
  getAll: () => api.get('/api/portos'),
  getById: id => api.get(`/api/portos/${id}`),
  create: formData =>
    api.post('/api/portos', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  update: (id, data) => api.put(`/api/portos/${id}`, data),
  delete: id => api.delete(`/api/portos/${id}`),
  addImages: (id, images) => {
    const formData = new FormData()
    images.forEach(file => formData.append('images', file))
    return api.post(`/api/portos/${id}/images`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  deleteImage: imageId => api.delete(`/api/portos/images/${imageId}`)
}

export const clientAPI = {
  getProfile: () => api.get('/api/v1/clients/profile'),
  updateProfile: profileData => api.put('/api/v1/clients/profile', profileData)
}

export const bidAPI = {
  getMyBids: () => api.get('/api/bids/my-bids'),
  getBid: id => api.get(`/api/bids/${id}`),
  getProjectBids: projectId => api.get(`/api/v1/projects/${projectId}/bids`),
  createDraftBid: bidData => api.post('/api/bids', bidData),
  updateBidDetails: (bidId, detailsData) => api.put(`/api/bids/${bidId}/details`, detailsData),
  submitBid: bidId => api.post(`/api/bids/${bidId}/submit`),
  withdrawBid: bidId => api.put(`/api/bids/${bidId}/withdraw`),
  acceptBid: (projectId, bidId) => api.post(`/api/bids/${bidId}/accept`),
  getQuota: () => api.get('/api/bids/quota'),
  linkPortfolios: (bidId, portfolioIds) =>
    api.post(`/api/bids/${bidId}/portfolios`, { portfolioIds }),
  uploadConceptSketches: (bidId, files, onProgress) => {
    const formData = new FormData()
    files.forEach(file => formData.append('images', file))
    return api.post(`/api/bids/${bidId}/concept-sketches`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: progressEvent => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    })
  },
  uploadMoodBoards: (bidId, files, onProgress) => {
    const formData = new FormData()
    files.forEach(file => formData.append('images', file))
    return api.post(`/api/bids/${bidId}/mood-boards`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: progressEvent => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    })
  },
  deleteImage: imageId => api.delete(`/api/bids/images/${imageId}`)
}

export const adminAPI = {
  getStats: () => api.get('/admin/stats'),
  getUsers: params => api.get('/admin/users', { params }),
  getUserById: id => api.get(`/admin/users/${id}`),
  updateUser: (id, userData) => api.put(`/admin/users/${id}`, userData),
  deleteUser: id => api.delete(`/admin/users/${id}`),
  getProjects: params => api.get('/admin/projects', { params }),
  deleteProject: id => api.delete(`/admin/projects/${id}`),
  getReports: () => api.get('/admin/reports'),
  generateReport: (reportType, params) =>
    api.post('/admin/reports/generate', { type: reportType, ...params })
}

export const tokenPurchaseAPI = {
  getPricing: () => api.get('/tokens/purchases/pricing'),
  initiatePurchase: quantity => api.post('/tokens/purchases', { quantity }),
  getPurchaseStatus: purchaseId => api.get(`/tokens/purchases/${purchaseId}`)
}

// Default export
export default api
