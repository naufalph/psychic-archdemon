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
    isAuthenticated: state => !!state.token && !!state.user,
    userRoles: state => state.user?.registeredRoles || [],
    hasRole: state => role => state.user?.registeredRoles?.includes(role) || false,
    isAdmin: state => state.user?.registeredRoles?.includes('ADMIN') || false,
    isArchitect: state => state.user?.registeredRoles?.includes('ARCHITECT') || false,
    isClient: state => state.user?.registeredRoles?.includes('CLIENT') || false,
    primaryRole: state => {
      const roles = state.user?.registeredRoles || []
      if (roles.includes('ADMIN')) return 'ADMIN'
      if (roles.includes('ARCHITECT')) return 'ARCHITECT'
      if (roles.includes('CLIENT')) return 'CLIENT'
      return null
    },
    userName: state =>
      state.user?.firstName && state.user?.lastName
        ? `${state.user.firstName} ${state.user.lastName}`
        : state.user?.email || '',
    isLocked: state => {
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
          await this.fetchUserData()
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

    // Fetch user data from backend using token
    async fetchUserData() {
      if (!this.token) return null

      try {
        const response = await authAPI.getCurrentUser()
        const { data: userData } = response.data

        this.user = userData
        return this.user
      } catch (error) {
        console.error('Failed to fetch user data:', error)
        this.clearAuth()
        throw error
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
          email: authResponse.email,
          firstName: authResponse.firstName,
          lastName: authResponse.lastName,
          registeredRoles: authResponse.registeredRoles || [],
          needsArchitectOnboarding: authResponse.needsArchitectOnboarding,
          needsClientOnboarding: authResponse.needsClientOnboarding,
          lastLoginRole: authResponse.lastLoginRole
        }

        // Persist only token to localStorage (NOT user data)
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

    // Verify email (does not auto-login, user must sign in after verification)
    async verifyEmail(token) {
      this.isLoading = true

      try {
        const response = await authAPI.verifyEmail(token)

        const { success, message } = response.data

        if (!success) {
          throw new Error(message || 'Email verification failed')
        }

        return { success: true, message }
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
    async loginWithGoogle(role = 'CLIENT', acceptances = []) {
      try {
        const response = await authAPI.getGoogleAuthUrl(role, acceptances)
        const authUrl = response.data

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
          email: authResponse.email,
          firstName: authResponse.firstName,
          lastName: authResponse.lastName,
          registeredRoles: authResponse.registeredRoles || [],
          needsArchitectOnboarding: authResponse.needsArchitectOnboarding,
          needsClientOnboarding: authResponse.needsClientOnboarding,
          lastLoginRole: authResponse.lastLoginRole
        }

        // Persist only token to localStorage (NOT user data)
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
    async loginWithLinkedIn(role = 'CLIENT', acceptances = []) {
      try {
        const response = await authAPI.getLinkedInAuthUrl(role, acceptances)
        const authUrl = response.data

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
          email: authResponse.email,
          firstName: authResponse.firstName,
          lastName: authResponse.lastName,
          registeredRoles: authResponse.registeredRoles || [],
          needsArchitectOnboarding: authResponse.needsArchitectOnboarding,
          needsClientOnboarding: authResponse.needsClientOnboarding,
          lastLoginRole: authResponse.lastLoginRole
        }

        // Persist only token to localStorage (NOT user data)
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

      // Remove only token from localStorage
      localStorage.removeItem('auth_token')

      // Reset state
      this.isInitialized = false
    },

    // Update last login role
    async updateLastLoginRole(role) {
      try {
        await authAPI.updateLastLoginRole(role)

        if (this.user) {
          this.user.lastLoginRole = role
        }
      } catch (error) {
        console.error('Failed to update last login role:', error)
      }
    },

    // Handle login failure attempts
    handleLoginFailure() {
      this.loginAttempts++

      // Lock account after 5 failed attempts for 15 minutes
      if (this.loginAttempts >= 5) {
        this.lockoutTime = Date.now() + 15 * 60 * 1000 // 15 minutes
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
    },

    // Activate a role (ARCHITECT or CLIENT)
    async activateRole(role) {
      this.isLoading = true

      try {
        const response = await authAPI.activateRole(role)
        const { data } = response.data

        if (this.user) {
          Object.assign(this.user, data)
        }

        return { success: true, user: this.user }
      } catch (error) {
        console.error('Role activation failed:', error)
        if (error.response?.data?.message?.includes('already has')) {
          await this.fetchUserData()
          return { success: true, user: this.user, alreadyActivated: true }
        }
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Force refresh user data from backend (useful for debugging and recovery)
    async refreshUserData() {
      if (!this.token) return null

      try {
        await this.fetchUserData()
        return { success: true, user: this.user }
      } catch (error) {
        console.error('Failed to refresh user data:', error)
        throw error
      }
    }
  }
})
