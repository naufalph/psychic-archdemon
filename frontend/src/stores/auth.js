import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('auth_token'),
    isLoading: false,
    isInitialized: false,
    loginAttempts: 0,
    lockoutTime: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token && !!state.user,
    userRole: (state) => state.user?.role || null,
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isArchitect: (state) => state.user?.role === 'ARCHITECT',
    isClient: (state) => state.user?.role === 'CLIENT',
    userName: (state) => state.user?.firstName && state.user?.lastName
      ? `${state.user.firstName} ${state.user.lastName}`
      : state.user?.email || '',
    isLocked: (state) => {
      if (!state.lockoutTime) return false
      return Date.now() < state.lockoutTime
    }
  },

  actions: {
    // Initialize authentication state
    async checkAuth() {
      if (this.isInitialized) return

      this.isLoading = true

      try {
        if (this.token) {
          // Set axios default header
          axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`

          // Verify token is still valid
          const response = await axios.get('/api/auth/me')
          this.user = response.data
        }
      } catch (error) {
        console.error('Auth check failed:', error)
        // Token is invalid, clear it
        this.clearAuth()
      } finally {
        this.isLoading = false
        this.isInitialized = true
      }
    },

    // Login user
    async login(credentials) {
      // Check if account is locked
      if (this.isLocked) {
        const remainingTime = Math.ceil((this.lockoutTime - Date.now()) / 60000)
        throw new Error(`Account locked. Try again in ${remainingTime} minutes.`)
      }

      this.isLoading = true

      try {
        const response = await axios.post('/api/auth/login', credentials)
        const { token, user } = response.data

        // Store authentication data
        this.token = token
        this.user = user

        // Persist token to localStorage
        localStorage.setItem('auth_token', token)

        // Set axios default header
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

        // Reset login attempts on successful login
        this.loginAttempts = 0
        this.lockoutTime = null

        return { success: true, user }
      } catch (error) {
        // Handle login failures
        this.handleLoginFailure(error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Register new user
    async register(userData) {
      this.isLoading = true

      try {
        const response = await axios.post('/api/auth/register', userData)
        const { token, user } = response.data

        // Store authentication data
        this.token = token
        this.user = user

        // Persist token to localStorage
        localStorage.setItem('auth_token', token)

        // Set axios default header
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

        return { success: true, user }
      } catch (error) {
        console.error('Registration failed:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Logout user
    async logout() {
      this.isLoading = true

      try {
        // Call logout endpoint to invalidate token on server
        if (this.token) {
          await axios.post('/api/auth/logout')
        }
      } catch (error) {
        // Even if server logout fails, we still clear local auth
        console.error('Server logout failed:', error)
      } finally {
        this.clearAuth()
        this.isLoading = false
      }
    },

    // Refresh authentication token
    async refreshToken() {
      if (!this.token) {
        throw new Error('No token to refresh')
      }

      try {
        const response = await axios.post('/api/auth/refresh', {
          token: this.token
        })

        const { token: newToken } = response.data

        // Update token
        this.token = newToken
        localStorage.setItem('auth_token', newToken)
        axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`

        return newToken
      } catch (error) {
        console.error('Token refresh failed:', error)
        // If refresh fails, clear auth
        this.clearAuth()
        throw error
      }
    },

    // Update user profile
    async updateProfile(profileData) {
      this.isLoading = true

      try {
        const response = await axios.put('/api/user/profile', profileData)
        this.user = { ...this.user, ...response.data }
        return response.data
      } catch (error) {
        console.error('Profile update failed:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Change password
    async changePassword(passwordData) {
      this.isLoading = true

      try {
        await axios.post('/api/auth/change-password', passwordData)
        return { success: true }
      } catch (error) {
        console.error('Password change failed:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Request password reset
    async requestPasswordReset(email) {
      this.isLoading = true

      try {
        await axios.post('/api/auth/forgot-password', { email })
        return { success: true }
      } catch (error) {
        console.error('Password reset request failed:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Reset password with token
    async resetPassword(resetData) {
      this.isLoading = true

      try {
        const response = await axios.post('/api/auth/reset-password', resetData)
        const { token, user } = response.data

        // Auto-login after password reset
        this.token = token
        this.user = user
        localStorage.setItem('auth_token', token)
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

        return { success: true, user }
      } catch (error) {
        console.error('Password reset failed:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Clear authentication data
    clearAuth() {
      this.user = null
      this.token = null

      // Remove from localStorage
      localStorage.removeItem('auth_token')

      // Remove axios default header
      delete axios.defaults.headers.common['Authorization']

      // Reset state
      this.isInitialized = false
    },

    // Handle login failure attempts
    handleLoginFailure(error) {
      this.loginAttempts++

      // Lock account after 5 failed attempts for 15 minutes
      if (this.loginAttempts >= 5) {
        this.lockoutTime = Date.now() + (15 * 60 * 1000) // 15 minutes
        localStorage.setItem('auth_lockout', this.lockoutTime.toString())
      }

      // Store login attempts
      localStorage.setItem('auth_attempts', this.loginAttempts.toString())
    },

    // Initialize lockout state from localStorage
    initializeLockoutState() {
      const storedAttempts = localStorage.getItem('auth_attempts')
      const storedLockout = localStorage.getItem('auth_lockout')

      if (storedAttempts) {
        this.loginAttempts = parseInt(storedAttempts)
      }

      if (storedLockout) {
        const lockoutTime = parseInt(storedLockout)
        if (Date.now() < lockoutTime) {
          this.lockoutTime = lockoutTime
        } else {
          // Lockout has expired, clear it
          localStorage.removeItem('auth_lockout')
          localStorage.removeItem('auth_attempts')
          this.loginAttempts = 0
          this.lockoutTime = null
        }
      }
    }
  }
})

// Initialize lockout state when store is created
const authStore = useAuthStore()
authStore.initializeLockoutState()
