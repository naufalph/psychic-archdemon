import { defineStore } from 'pinia'
import { authAPI } from '@/services/api'

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
          // Token validation will be handled by the request interceptor
          // For now, just mark as initialized if token exists
          // TODO: Add a proper token validation endpoint
          this.isInitialized = true
        }
      } catch (error) {
        console.error('Auth check failed:', error)
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
        const response = await authAPI.login(credentials)

        // Extract data from ApiResponse wrapper
        const { data: authResponse } = response.data

        // Store authentication data
        this.token = authResponse.token
        this.user = {
          id: authResponse.id,
          email: authResponse.email
        }

        // Persist token to localStorage
        localStorage.setItem('auth_token', authResponse.token)

        // Reset login attempts on successful login
        this.loginAttempts = 0
        this.lockoutTime = null

        return { success: true, user: this.user }
      } catch (error) {
        // Handle login failures
        this.handleLoginFailure(error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Register new user (requires email verification)
    async register(userData) {
      this.isLoading = true

      try {
        const response = await authAPI.register(userData)

        // Registration returns a message about email verification
        // No token is returned until email is verified
        const { data: message } = response.data

        return { success: true, message }
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
        // Since JWT tokens are stateless, just clear local storage
        // No server-side logout endpoint needed
        this.clearAuth()
      } catch (error) {
        console.error('Logout failed:', error)
      } finally {
        this.isLoading = false
      }
    },

    // JWT tokens don't need refresh for this implementation
    async refreshToken() {
      // JWT tokens are stateless and don't require refresh
      // If the token is expired, user will need to login again
      throw new Error('Token refresh not supported with stateless JWT')
    },


    // Verify email and auto-login
    async verifyEmail(token) {
      this.isLoading = true

      try {
        const response = await authAPI.verifyEmail(token)

        // Extract data from ApiResponse wrapper
        const { data: authResponse } = response.data

        // Store authentication data for auto-login
        this.token = authResponse.token
        this.user = {
          id: authResponse.id,
          email: authResponse.email
        }

        // Persist token to localStorage
        localStorage.setItem('auth_token', authResponse.token)

        return { success: true, user: this.user }
      } catch (error) {
        console.error('Email verification failed:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Resend verification email
    async resendVerification(email) {
      this.isLoading = true

      try {
        const response = await authAPI.resendVerification(email)
        const { data: message } = response.data

        return { success: true, message }
      } catch (error) {
        console.error('Resend verification failed:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Handle Google OAuth login
    async loginWithGoogle() {
      try {
        const response = await authAPI.getGoogleAuthUrl()
        const authUrl = response.data

        // Redirect to Google OAuth
        window.location.href = authUrl
      } catch (error) {
        console.error('Google login failed:', error)
        throw error
      }
    },

    // Handle Google OAuth callback
    async handleGoogleCallback(code) {
      this.isLoading = true

      try {
        const response = await authAPI.googleCallback(code)

        // Extract data from ApiResponse wrapper
        const { data: authResponse } = response.data

        // Store authentication data
        this.token = authResponse.token
        this.user = {
          id: authResponse.id,
          email: authResponse.email
        }

        // Persist token to localStorage
        localStorage.setItem('auth_token', authResponse.token)

        return { success: true, user: this.user }
      } catch (error) {
        console.error('Google callback failed:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Handle LinkedIn OAuth login
    async loginWithLinkedIn() {
      try {
        const response = await authAPI.getLinkedInAuthUrl()
        const authUrl = response.data

        // Redirect to LinkedIn OAuth
        window.location.href = authUrl
      } catch (error) {
        console.error('LinkedIn login failed:', error)
        throw error
      }
    },

    // Handle LinkedIn OAuth callback
    async handleLinkedInCallback(code) {
      this.isLoading = true

      try {
        const response = await authAPI.linkedinCallback(code)

        // Extract data from ApiResponse wrapper
        const { data: authResponse } = response.data

        // Store authentication data
        this.token = authResponse.token
        this.user = {
          id: authResponse.id,
          email: authResponse.email
        }

        // Persist token to localStorage
        localStorage.setItem('auth_token', authResponse.token)

        return { success: true, user: this.user }
      } catch (error) {
        console.error('LinkedIn callback failed:', error)
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
