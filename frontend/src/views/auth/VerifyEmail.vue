<template>
  <div
    class="min-h-screen bg-gradient-to-br from-primary-50 to-white flex items-center justify-center px-4 sm:px-6 lg:px-8"
  >
    <div class="max-w-md w-full">
      <!-- Card Container -->
      <div class="bg-white rounded-2xl shadow-xl p-8 sm:p-10">
        <!-- Loading State -->
        <div v-if="status === 'verifying'" class="text-center">
          <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg
              class="animate-spin h-8 w-8 text-primary-600"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              ></path>
            </svg>
          </div>
          <h1 class="verify-title mb-4">Verifying Your Email</h1>
          <p class="verify-description">Please wait while we verify your email address...</p>
        </div>

        <!-- Success State -->
        <div v-else-if="status === 'success'" class="text-center">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
          <h1 class="verify-title text-green-600 mb-4">Email Verified Successfully!</h1>
          <p class="verify-description mb-8">
            Your account has been activated. You can now access all features of Rumantra.
          </p>

          <div class="space-y-3">
            <button
              @click="goToDashboard"
              class="w-full bg-primary-600 text-white font-medium py-3 px-6 rounded-lg hover:bg-primary-700 transition-colors duration-200 verify-button"
            >
              Go to Dashboard
            </button>
            <button
              @click="goToLogin"
              class="w-full bg-white text-primary-600 font-medium py-3 px-6 rounded-lg border-2 border-primary-600 hover:bg-primary-50 transition-colors duration-200 verify-button"
            >
              Go to Login
            </button>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="status === 'error'" class="text-center">
          <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </div>
          <h1 class="verify-title text-red-600 mb-4">Verification Failed</h1>
          <p class="verify-description mb-8">{{ errorMessage }}</p>

          <div class="space-y-3">
            <button
              v-if="canResendVerification"
              @click="resendVerificationEmail"
              :disabled="isResending"
              class="w-full bg-primary-600 text-white font-medium py-3 px-6 rounded-lg hover:bg-primary-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors duration-200 verify-button"
            >
              <span v-if="isResending">Sending...</span>
              <span v-else>Resend Verification Email</span>
            </button>
            <button
              @click="goToHome"
              class="w-full bg-white text-gray-700 font-medium py-3 px-6 rounded-lg border-2 border-gray-300 hover:bg-gray-50 transition-colors duration-200 verify-button"
            >
              Go to Home
            </button>
          </div>
        </div>
      </div>

      <!-- Footer Text -->
      <p class="mt-8 text-center verify-footer">
        Need help?
        <a href="mailto:support@rumantra.com" class="text-primary-600 hover:text-primary-700 font-medium">
          Contact Support
        </a>
      </p>
    </div>
  </div>
</template>

<script>
import { authAPI } from '@/services/api'

export default {
  name: 'VerifyEmail',
  data() {
    return {
      status: 'verifying', // 'verifying' | 'success' | 'error'
      errorMessage: '',
      token: '',
      canResendVerification: false,
      isResending: false,
      userEmail: ''
    }
  },
  mounted() {
    this.verifyEmail()
  },
  methods: {
    async verifyEmail() {
      // Get token from URL query parameters
      this.token = this.$route.query.token

      if (!this.token) {
        this.status = 'error'
        this.errorMessage = 'Verification token is missing. Please check your email for the correct verification link.'
        return
      }

      try {
        // Call the verification API
        const response = await authAPI.verifyEmail(this.token)

        if (response.data.success) {
          this.status = 'success'

          // If the response includes auth data, you could auto-login here
          // For now, we'll just redirect to login
        } else {
          this.status = 'error'
          this.errorMessage = response.data.message || 'Verification failed. Please try again.'
          this.canResendVerification = true
        }
      } catch (error) {
        this.status = 'error'

        // Parse error message from API response
        if (error.response?.data?.message) {
          this.errorMessage = error.response.data.message

          // Check if it's an expired token error
          if (
            this.errorMessage.toLowerCase().includes('expired') ||
            this.errorMessage.toLowerCase().includes('invalid')
          ) {
            this.canResendVerification = true
          }
        } else if (error.response?.status === 400) {
          this.errorMessage = 'Invalid or expired verification link. Please request a new verification email.'
          this.canResendVerification = true
        } else if (error.response?.status === 404) {
          this.errorMessage = 'Verification link not found. It may have already been used or expired.'
          this.canResendVerification = true
        } else {
          this.errorMessage = 'An error occurred during verification. Please try again later.'
        }

        console.error('Email verification error:', error)
      }
    },

    async resendVerificationEmail() {
      // You might want to prompt for email or extract it from somewhere
      const email = prompt('Please enter your email address to resend the verification link:')

      if (!email) {
        return
      }

      this.isResending = true

      try {
        const response = await authAPI.resendVerification(email)

        if (response.data.success) {
          alert('Verification email has been resent! Please check your inbox.')
          this.canResendVerification = false
        } else {
          alert(response.data.message || 'Failed to resend verification email.')
        }
      } catch (error) {
        console.error('Resend verification error:', error)
        alert(error.response?.data?.message || 'Failed to resend verification email. Please try again.')
      } finally {
        this.isResending = false
      }
    },

    goToDashboard() {
      this.$router.push('/dashboard')
    },

    goToLogin() {
      this.$router.push('/login')
    },

    goToHome() {
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
/* Typography using design system */
.verify-title {
  font-family: 'Inter', sans-serif;
  font-size: 32px;
  font-weight: 500;
  color: #000000;
}

.verify-description {
  font-family: 'Inter', sans-serif;
  font-size: 16px;
  font-weight: 400;
  color: #6b7280;
}

.verify-button {
  font-family: 'Inter', sans-serif;
  font-size: 16px;
  font-weight: 500;
}

.verify-footer {
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 400;
  color: #6b7280;
}

/* Animations */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .verify-title {
    font-size: 24px;
  }

  .verify-description {
    font-size: 14px;
  }
}
</style>
