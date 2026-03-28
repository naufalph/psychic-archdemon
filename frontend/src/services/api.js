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
  getGoogleAuthUrl: () => api.get('/rmtr/users/oauth2/google'),
  googleCallback: code => api.get(`/rmtr/users/oauth2/callback/google?code=${code}`),

  // LinkedIn OAuth endpoints
  getLinkedInAuthUrl: () => api.get('/rmtr/users/oauth2/linkedin'),
  linkedinCallback: code => api.get(`/rmtr/users/oauth2/callback/linkedin?code=${code}`)
}

export const userAPI = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: profileData => api.put('/user/profile', profileData),
  uploadAvatar: (file, onProgress) => uploadFile('/user/avatar', file, onProgress),
  getNotifications: () => api.get('/user/notifications'),
  markNotificationRead: id => api.patch(`/user/notifications/${id}/read`),
  getSettings: () => api.get('/user/settings'),
  updateSettings: settings => api.put('/user/settings', settings)
}

export const projectAPI = {
  getAll: params => api.get('/rmtr/projects', { params }),
  getById: id => api.get(`/rmtr/projects/${id}`),
  create: projectData => api.post('/rmtr/projects', projectData),
  update: (id, projectData) => api.put(`/rmtr/projects/${id}`, projectData),
  delete: id => api.delete(`/rmtr/projects/${id}`),
  getBids: id => api.get(`/rmtr/projects/${id}/bids`),
  createBid: (id, bidData) => api.post(`/rmtr/projects/${id}/bids`, bidData),
  acceptBid: (projectId, bidId) => api.patch(`/rmtr/projects/${projectId}/bids/${bidId}/accept`),
  rejectBid: (projectId, bidId) => api.patch(`/rmtr/projects/${projectId}/bids/${bidId}/reject`),
  getOpenProjects: params => api.get('/rmtr/projects/open', { params }),
  uploadFiles: (id, files, onProgress) => {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    return api.post(`/rmtr/projects/${id}/files`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress
    })
  }
}

export const architectAPI = {
  getAll: params => api.get('/rmtr/architects', { params }),
  getById: id => api.get(`/rmtr/architects/${id}`),
  getPortfolio: id => api.get(`/rmtr/architects/${id}/portfolio`),
  updatePortfolio: portfolioData => api.put('/rmtr/architects/portfolio', portfolioData),
  getReviews: id => api.get(`/rmtr/architects/${id}/reviews`),
  addReview: (id, reviewData) => api.post(`/rmtr/architects/${id}/reviews`, reviewData)
}

export const bidAPI = {
  getMyBids: () => api.get('/rmtr/bids/my-bids'),
  getBid: id => api.get(`/rmtr/bids/${id}`),
  updateBid: (id, bidData) => api.put(`/rmtr/bids/${id}`, bidData),
  deleteBid: id => api.delete(`/rmtr/bids/${id}`)
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

export const adminProjectAPI = {
  getAllProjects: () => api.get('/rmtr/projects/all'),
  getProjectById: id => api.get(`/rmtr/projects/${id}`),
  updateValidation: (projectId, validationData) =>
    api.put(`/rmtr/projects/${projectId}/validate`, validationData)
}

// Default export
export default api
