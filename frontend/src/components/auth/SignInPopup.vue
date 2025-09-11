<template>
  <!-- Modal Overlay -->
  <div
    v-if="isVisible"
    class="fixed inset-0 z-50 overflow-y-auto"
    @click.self="closeModal"
  >
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div
        class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
        @click="closeModal"
      ></div>

      <!-- Modal positioning -->
      <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>

      <!-- Modal content -->
      <div class="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
        <!-- Sign Up Pop-Up (Social Login) -->
        <div v-if="currentView === 'signup'" class="w-full max-w-md mx-auto">
          <div class="text-center mb-6">
            <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ $t('auth.signup.title') }}</h2>
            <p class="text-sm text-gray-600">
              {{ $t('auth.signup.alreadyHaveAccount') }}
              <button 
                @click="switchToSignIn" 
                class="text-primary-600 hover:text-primary-700 font-medium"
              >
                {{ $t('auth.signIn') }}
              </button>
            </p>
          </div>

          <div class="space-y-3">
            <!-- LinkedIn -->
              <button
                @click="handleSocialLogin('linkedin')"
                class="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                <svg class="w-5 h-5 mr-3 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
                </svg>
                {{ $t('auth.signup.socialLogin.linkedin') }}
              </button>

            <!-- Email -->
            <button
              @click="switchToEmailSignup"
              class="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              <svg class="w-5 h-5 mr-3 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
              {{ $t('auth.signup.socialLogin.email') }}
            </button>

            <!-- Google -->
            <button
              @click="handleSocialLogin('google')"
              class="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              <svg class="w-5 h-5 mr-3" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              {{ $t('auth.signup.socialLogin.google') }}
            </button>

            <!-- Facebook -->
            <button
              @click="handleSocialLogin('facebook')"
              class="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
            >
              <svg class="w-5 h-5 mr-3 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
              </svg>
              {{ $t('auth.signup.socialLogin.facebook') }}
            </button>
          </div>

          <div class="mt-6 text-center">
            <p class="text-xs text-gray-500">
              {{ $t('auth.signup.termsAndPrivacy.prefix') }}
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.terms') }} </a> 
              {{ $t('auth.signup.termsAndPrivacy.and') }} 
              <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.privacy') }}</a>
            </p>
          </div>
        </div>

        <!-- Email Sign Up Form -->
        <div v-else-if="currentView === 'emailSignup'" class="w-full max-w-md mx-auto">
          <div class="flex items-center mb-6">
            <button 
              @click="switchToSignup" 
              class="mr-3 p-1 rounded-full hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
              </svg>
            </button>
            <h2 class="text-xl font-bold text-gray-900">{{ $t('auth.emailSignup.title') }}</h2>
          </div>

          <form @submit.prevent="handleEmailSignup" class="space-y-4">
            <div>
              <label for="email" class="form-label">Email</label>
              <input
                id="email"
                v-model="emailForm.email"
                type="email"
                required
                class="form-input"
                placeholder="nama@email.com"
              >
              <p v-if="emailForm.errors.email" class="form-error">{{ emailForm.errors.email }}</p>
            </div>

            <div class="relative">
              <label for="password" class="form-label">Password</label>
              <input
                id="password"
                v-model="emailForm.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="form-input pr-10"
                placeholder="Password"
              >
              <button
                type="button"
                @click="togglePasswordVisibility"
                class="absolute inset-y-0 right-0 top-6 pr-3 flex items-center"
              >
                <svg v-if="showPassword" class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"/>
                </svg>
                <svg v-else class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
              </button>
              <p v-if="emailForm.errors.password" class="form-error">{{ emailForm.errors.password }}</p>
            </div>

            <button
              type="submit"
              :disabled="isLoading"
              class="w-full btn btn-primary"
            >
              <span v-if="isLoading" class="spinner mr-2"></span>
              Buat Akun
            </button>
          </form>

            <div class="mt-4 text-center">
              <p class="text-xs text-gray-500">
                {{ $t('auth.signup.termsAndPrivacy.prefix') }}
                <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.terms') }}</a> 
                {{ $t('auth.signup.termsAndPrivacy.and') }}
                <a href="#" class="text-primary-600 hover:text-primary-700">{{ $t('auth.signup.termsAndPrivacy.privacy') }}</a>
              </p>
            </div>
        </div>

        <!-- Sign In Form -->
        <div v-else-if="currentView === 'signin'" class="w-full max-w-md mx-auto">
          <div class="text-center mb-6">
            <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ $t('auth.signin.title') }}</h2>
            <p class="text-sm text-gray-600">
              {{ $t('auth.signin.noAccount') }}
              <button 
                @click="switchToSignup" 
                class="text-primary-600 hover:text-primary-700 font-medium"
              >
                {{ $t('auth.signin.signUp') }}
              </button>
            </p>
          </div>

          <form @submit.prevent="handleSignIn" class="space-y-4">
            <div>
              <label for="signin-email" class="form-label">Email</label>
              <input
                id="signin-email"
                v-model="signinForm.email"
                type="email"
                required
                class="form-input"
                placeholder="nama@email.com"
              >
              <p v-if="signinForm.errors.email" class="form-error">{{ signinForm.errors.email }}</p>
            </div>

            <div class="relative">
              <label for="signin-password" class="form-label">Password</label>
              <input
                id="signin-password"
                v-model="signinForm.password"
                :type="showSigninPassword ? 'text' : 'password'"
                required
                class="form-input pr-10"
                placeholder="Password"
              >
              <button
                type="button"
                @click="toggleSigninPasswordVisibility"
                class="absolute inset-y-0 right-0 top-6 pr-3 flex items-center"
              >
                <svg v-if="showSigninPassword" class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"/>
                </svg>
                <svg v-else class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
              </button>
              <p v-if="signinForm.errors.password" class="form-error">{{ signinForm.errors.password }}</p>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <input
                  id="remember-me"
                  v-model="signinForm.rememberMe"
                  type="checkbox"
                  class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                >
                <label for="remember-me" class="ml-2 block text-sm text-gray-900">
                  {{ $t('auth.signin.rememberMe') }}
                </label>
              </div>

              <div class="text-sm">
                <a href="#" class="font-medium text-primary-600 hover:text-primary-500">
                  {{ $t('auth.signin.forgotPassword') }}
                </a>
              </div>
            </div>

            <button
              type="submit"
              :disabled="isLoading"
              class="w-full btn btn-primary"
            >
              <span v-if="isLoading" class="spinner mr-2"></span>
              Masuk
            </button>
          </form>

          <div class="mt-6">
            <div class="relative">
              <div class="absolute inset-0 flex items-center">
                <div class="w-full border-t border-gray-300" />
              </div>
              <div class="relative flex justify-center text-sm">
                <span class="px-2 bg-white text-gray-500">{{ $t('auth.signin.socialLogin.or') }}</span>
              </div>
            </div>

            <div class="mt-6 grid grid-cols-2 gap-3">
              <button
                @click="handleSocialLogin('google')"
                class="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              >
                <svg class="w-5 h-5" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
              </button>

              <button
                @click="handleSocialLogin('facebook')"
                class="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              >
                <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Close button -->
        <div class="absolute top-0 right-0 pt-4 pr-4">
          <button
            @click="closeModal"
            class="bg-white rounded-md text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
          >
            <span class="sr-only">Close</span>
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'pinia'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'SignInPopup',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    initialView: {
      type: String,
      default: 'signup', // 'signup', 'signin', 'emailSignup'
      validator: (value) => ['signup', 'signin', 'emailSignup'].includes(value)
    }
  },
  emits: ['close', 'success'],
  data() {
    return {
      currentView: this.initialView,
      isLoading: false,
      showPassword: false,
      showSigninPassword: false,
      emailForm: {
        email: '',
        password: '',
        errors: {}
      },
      signinForm: {
        email: '',
        password: '',
        rememberMe: false,
        errors: {}
      }
    }
  },
  computed: {
    isVisible() {
      return this.visible
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.currentView = this.initialView
        this.resetForms()
      }
    },
    initialView(newVal) {
      this.currentView = newVal
    }
  },
  methods: {
    ...mapActions(useAuthStore, ['login', 'register']),

    closeModal() {
      this.$emit('close')
    },

    switchToSignup() {
      this.currentView = 'signup'
      this.resetForms()
    },

    switchToSignIn() {
      this.currentView = 'signin'
      this.resetForms()
    },

    switchToEmailSignup() {
      this.currentView = 'emailSignup'
      this.resetForms()
    },

    togglePasswordVisibility() {
      this.showPassword = !this.showPassword
    },

    toggleSigninPasswordVisibility() {
      this.showSigninPassword = !this.showSigninPassword
    },

    resetForms() {
      this.emailForm = {
        email: '',
        password: '',
        errors: {}
      }
      this.signinForm = {
        email: '',
        password: '',
        rememberMe: false,
        errors: {}
      }
      this.isLoading = false
    },

    validateEmailForm() {
      const errors = {}
      
      if (!this.emailForm.email) {
        errors.email = 'Email harus diisi'
      } else if (!/\S+@\S+\.\S+/.test(this.emailForm.email)) {
        errors.email = 'Format email tidak valid'
      }

      if (!this.emailForm.password) {
        errors.password = 'Password harus diisi'
      } else if (this.emailForm.password.length < 6) {
        errors.password = 'Password minimal 6 karakter'
      }

      this.emailForm.errors = errors
      return Object.keys(errors).length === 0
    },

    validateSigninForm() {
      const errors = {}
      
      if (!this.signinForm.email) {
        errors.email = 'Email harus diisi'
      } else if (!/\S+@\S+\.\S+/.test(this.signinForm.email)) {
        errors.email = 'Format email tidak valid'
      }

      if (!this.signinForm.password) {
        errors.password = 'Password harus diisi'
      }

      this.signinForm.errors = errors
      return Object.keys(errors).length === 0
    },

    async handleEmailSignup() {
      if (!this.validateEmailForm()) {
        return
      }

      this.isLoading = true

      try {
        const userData = {
          email: this.emailForm.email,
          password: this.emailForm.password,
          role: 'CLIENT' // Default role for marketplace
        }

        await this.register(userData)
        
        this.$emit('success', { type: 'register', user: userData })
        this.closeModal()
        
        // Show success message
        this.$toast?.success('Akun berhasil dibuat! Selamat datang!')
        
      } catch (error) {
        console.error('Registration failed:', error)
        
        if (error.response?.status === 422) {
          // Handle validation errors from server
          const serverErrors = error.response.data.errors || {}
          this.emailForm.errors = { ...this.emailForm.errors, ...serverErrors }
        } else if (error.response?.status === 409) {
          this.emailForm.errors.email = 'Email sudah terdaftar'
        } else {
          this.emailForm.errors.general = 'Terjadi kesalahan. Silakan coba lagi.'
        }
      } finally {
        this.isLoading = false
      }
    },

    async handleSignIn() {
      if (!this.validateSigninForm()) {
        return
      }

      this.isLoading = true

      try {
        const credentials = {
          email: this.signinForm.email,
          password: this.signinForm.password
        }

        const result = await this.login(credentials)
        
        this.$emit('success', { type: 'login', user: result.user })
        this.closeModal()
        
        // Show success message
        this.$toast?.success(`Selamat datang kembali, ${result.user.firstName || result.user.email}!`)
        
      } catch (error) {
        console.error('Login failed:', error)
        
        if (error.response?.status === 401) {
          this.signinForm.errors.general = 'Email atau password salah'
        } else if (error.response?.status === 423) {
          this.signinForm.errors.general = 'Akun terkunci. Silakan coba lagi nanti.'
        } else {
          this.signinForm.errors.general = 'Terjadi kesalahan. Silakan coba lagi.'
        }
      } finally {
        this.isLoading = false
      }
    },

    async handleSocialLogin(provider) {
      this.isLoading = true

      try {
        // In a real implementation, you would integrate with the social provider's SDK
        console.log(`Initiating ${provider} login...`)
        
        // For demo purposes, we'll simulate the social login
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // This would typically redirect to the provider's OAuth flow
        window.location.href = `/api/auth/${provider}`
        
      } catch (error) {
        console.error(`${provider} login failed:`, error)
        this.$toast?.error(`Gagal masuk dengan ${provider}. Silakan coba lagi.`)
      } finally {
        this.isLoading = false
      }
    }
  },

  mounted() {
    // Handle escape key
    const handleEscape = (e) => {
      if (e.key === 'Escape' && this.isVisible) {
        this.closeModal()
      }
    }

    document.addEventListener('keydown', handleEscape)
    
  },

  beforeUnmount() {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && this.isVisible) {
        this.closeModal()
      }
    }
    document.removeEventListener('keydown', handleEscape)
  }
}
</script>

<style scoped>
/* Additional custom styles if needed */
.spinner {
  border-top-color: transparent;
}

/* Smooth transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* Focus styles for better accessibility */
button:focus,
input:focus {
  outline: none;
}

/* Custom scrollbar for modal content */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
