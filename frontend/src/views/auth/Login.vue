<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <!-- Logo and Title -->
      <div class="flex justify-center">
        <router-link to="/" class="flex items-center">
          <svg class="w-8 h-8 text-primary-600" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2L2 7L12 12L22 7L12 2Z"/>
            <path d="M2 17L12 22L22 17"/>
            <path d="M2 12L12 17L22 12"/>
          </svg>
          <span class="ml-2 text-2xl font-bold text-gray-900">ArchMatch</span>
        </router-link>
      </div>
      <h2 class="mt-6 text-center text-3xl font-bold text-gray-900">
        Sign in to your account
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        Or
        <router-link to="/register" class="font-medium text-primary-600 hover:text-primary-500">
          create a new account
        </router-link>
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <!-- Login Form -->
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Email Field -->
          <div>
            <label for="email" class="form-label">
              Email address
            </label>
            <div class="mt-1">
              <input
                id="email"
                v-model="form.email"
                type="email"
                autocomplete="email"
                required
                class="form-input"
                :class="{ 'border-red-300': errors.email }"
                @blur="validateField('email')"
                @input="clearError('email')"
              />
            </div>
            <p v-if="errors.email" class="form-error">{{ errors.email }}</p>
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="form-label">
              Password
            </label>
            <div class="mt-1 relative">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                required
                class="form-input pr-10"
                :class="{ 'border-red-300': errors.password }"
                @blur="validateField('password')"
                @input="clearError('password')"
              />
              <button
                type="button"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
                @click="showPassword = !showPassword"
              >
                <svg
                  v-if="!showPassword"
                  class="h-5 w-5 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
                <svg
                  v-else
                  class="h-5 w-5 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"/>
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="form-error">{{ errors.password }}</p>
          </div>

          <!-- Remember me and Forgot password -->
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <input
                id="remember-me"
                v-model="form.rememberMe"
                type="checkbox"
                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label for="remember-me" class="ml-2 block text-sm text-gray-900">
                Remember me
              </label>
            </div>

            <div class="text-sm">
              <router-link
                to="/forgot-password"
                class="font-medium text-primary-600 hover:text-primary-500"
              >
                Forgot your password?
              </router-link>
            </div>
          </div>

          <!-- Submit Button -->
          <div>
            <button
              type="submit"
              :disabled="isLoading || isLocked"
              class="w-full btn btn-primary py-3 text-lg"
              :class="{ 'opacity-50 cursor-not-allowed': isLoading || isLocked }"
            >
              <span v-if="isLoading" class="inline-flex items-center">
                <div class="spinner mr-2"></div>
                Signing in...
              </span>
              <span v-else-if="isLocked">
                Account Locked
              </span>
              <span v-else>
                Sign in
              </span>
            </button>
          </div>
        </form>

        <!-- Error Messages -->
        <div v-if="loginError" class="mt-6">
          <div class="rounded-md bg-red-50 p-4">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
                </svg>
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-red-800">
                  Sign in failed
                </h3>
                <div class="mt-2 text-sm text-red-700">
                  {{ loginError }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Lockout Warning -->
        <div v-if="loginAttempts > 2" class="mt-4">
          <div class="rounded-md bg-yellow-50 p-4">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-yellow-800">
                  Multiple failed attempts
                </h3>
                <div class="mt-2 text-sm text-yellow-700">
                  Account will be locked after {{ 5 - loginAttempts }} more failed attempts.
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Social Login (Optional) -->
        <div class="mt-6">
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white text-gray-500">Or continue with</span>
            </div>
          </div>

          <div class="mt-6 grid grid-cols-2 gap-3">
            <button
              type="button"
              class="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              @click="handleSocialLogin('google')"
            >
              <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              <span class="ml-2">Google</span>
            </button>

            <button
              type="button"
              class="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              @click="handleSocialLogin('github')"
            >
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
              <span class="ml-2">GitHub</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        email: '',
        password: '',
        rememberMe: false
      },
      errors: {},
      showPassword: false,
      loginError: '',
      isLoading: false
    }
  },
  computed: {
    ...mapState(useAuthStore, ['loginAttempts', 'isLocked'])
  },
  methods: {
    ...mapActions(useAuthStore, ['login']),

    validateField(field) {
      this.clearError(field)

      switch (field) {
        case 'email':
          if (!this.form.email) {
            this.errors.email = 'Email is required'
          } else if (!this.isValidEmail(this.form.email)) {
            this.errors.email = 'Please enter a valid email address'
          }
          break

        case 'password':
          if (!this.form.password) {
            this.errors.password = 'Password is required'
          } else if (this.form.password.length < 6) {
            this.errors.password = 'Password must be at least 6 characters'
          }
          break
      }
    },

    validateForm() {
      this.errors = {}
      this.validateField('email')
      this.validateField('password')
      return Object.keys(this.errors).length === 0
    },

    clearError(field) {
      if (this.errors[field]) {
        delete this.errors[field]
      }
      if (this.loginError) {
        this.loginError = ''
      }
    },

    isValidEmail(email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email)
    },

    async handleSubmit() {
      if (!this.validateForm() || this.isLoading || this.isLocked) {
        return
      }

      this.isLoading = true
      this.loginError = ''

      try {
        await this.login({
          email: this.form.email,
          password: this.form.password,
          rememberMe: this.form.rememberMe
        })

        // Success! Redirect to intended page or dashboard
        const redirectPath = this.$route.query.redirect || '/dashboard'
        this.$router.push(redirectPath)

        // Show success notification (optional)
        this.$emit('success', 'Successfully signed in!')
      } catch (error) {
        console.error('Login error:', error)

        if (error.response?.status === 401) {
          this.loginError = 'Invalid email or password'
        } else if (error.response?.status === 423) {
          this.loginError = 'Account is locked due to too many failed login attempts'
        } else if (error.response?.status === 403) {
          this.loginError = 'Account is not activated. Please check your email for activation instructions.'
        } else if (error.message.includes('locked')) {
          this.loginError = error.message
        } else {
          this.loginError = 'An error occurred during sign in. Please try again.'
        }
      } finally {
        this.isLoading = false
      }
    },

    async handleSocialLogin(provider) {
      try {
        // This would typically redirect to the OAuth provider
        console.log(`Initiating ${provider} login...`)

        // For now, show a placeholder message
        alert(`${provider} login is not yet implemented. This would redirect to ${provider} OAuth.`)

        // In a real implementation, you might do:
        // window.location.href = `/api/auth/${provider}`
      } catch (error) {
        console.error(`${provider} login error:`, error)
        this.loginError = `Failed to initiate ${provider} login`
      }
    }
  },

  mounted() {
    // Auto-focus on email field
    this.$nextTick(() => {
      const emailInput = this.$el.querySelector('#email')
      if (emailInput) {
        emailInput.focus()
      }
    })

    // Check for registration success message
    if (this.$route.query.registered === 'true') {
      // You could show a success message here
      console.log('Registration successful! Please sign in.')
    }

    // Check for email verification success
    if (this.$route.query.verified === 'true') {
      console.log('Email verified successfully! Please sign in.')
    }
  }
}
</script>

<style scoped>
/* Custom animations for error states */
.form-input.border-red-300 {
  animation: shake 0.6s ease-in-out;
}

@keyframes shake {
  0%, 20%, 50%, 80%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 70%, 90% {
    transform: translateX(-2px);
  }
  40%, 60% {
    transform: translateX(2px);
  }
}

/* Password visibility toggle */
.password-toggle {
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: #374151;
}

/* Loading state styles */
.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* Social login buttons */
button:hover svg {
  transform: scale(1.05);
  transition: transform 0.2s ease;
}

/* Focus styles for better accessibility */
.form-input:focus {
  ring: 2px;
  ring-color: #3b82f6;
  ring-offset: 2px;
  outline: none;
}

/* Custom checkbox styling */
input[type="checkbox"]:checked {
  background-color: #3b82f6;
  border-color: #3b82f6;
}
</style>
