<template>
  <div class="w-full max-w-[538px] bg-white rounded-2xl shadow-[0px_4px_4px_0px_rgba(0,0,0,0.3),0px_8px_12px_6px_rgba(0,0,0,0.15)] px-4 py-16">
    <!-- Title -->
    <h2 class="text-center font-poppins font-semibold text-4xl leading-[1.5em] text-[#C5A25A] mb-6">
      Login
    </h2>

    <!-- Form -->
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- Email Input -->
      <BaseInput
        v-model="formData.email"
        label="Email"
        type="email"
        placeholder="Input here"
        :error-message="errors.email"
      />

      <!-- Password Input with Forgot Password -->
      <div class="space-y-[7px]">
        <BaseInput
          v-model="formData.password"
          label="Password"
          type="password"
          placeholder="Input password here"
          :error-message="errors.password"
        />
        <!-- Forgot Password Link -->
        <div class="flex justify-end">
          <a
            href="#"
            class="font-poppins font-normal text-sm leading-[1.29em] text-[#E11D48] hover:underline"
            @click.prevent="handleForgotPassword"
          >
            Forgot password?
          </a>
        </div>
      </div>

      <!-- Login Button -->
      <BaseButton
        type="submit"
        :loading="isSubmitting"
        block
      >
        Login
      </BaseButton>

      <!-- Sign Up Button -->
      <BaseButton
        type="button"
        variant="outline"
        block
        @click="handleSignUp"
      >
        Sign Up
      </BaseButton>

      <!-- Divider -->
      <div class="relative">
        <div class="absolute inset-0 flex items-center">
          <div class="w-full border-t border-[#646464]"></div>
        </div>
        <div class="relative flex justify-center text-sm">
          <span class="px-2 bg-white text-[#646464] font-poppins font-medium">Or</span>
        </div>
      </div>

      <!-- OAuth Buttons -->
      <div class="space-y-4">
        <!-- Google Button -->
        <BaseButton
          type="button"
          variant="outline"
          block
          :disabled="isSubmitting"
          @click="handleGoogleLogin"
        >
          <div class="flex items-center justify-center gap-3">
            <svg class="w-5 h-5" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            <span class="text-[#848484]">Login with Google</span>
          </div>
        </BaseButton>

        <!-- LinkedIn Button -->
        <BaseButton
          type="button"
          variant="outline"
          block
          :disabled="isSubmitting"
          @click="handleLinkedInLogin"
        >
          <div class="flex items-center justify-center gap-3">
            <svg class="w-5 h-5" fill="#0A66C2" viewBox="0 0 24 24">
              <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
            </svg>
            <span class="text-[#848484]">Login with LinkedIn</span>
          </div>
        </BaseButton>
      </div>

      <!-- Success/Error Messages -->
      <div v-if="successMessage" class="p-4 bg-green-50 border border-green-200 rounded-lg">
        <p class="text-sm text-green-800 font-poppins">{{ successMessage }}</p>
      </div>

      <div v-if="errorMessage" class="p-4 bg-red-50 border border-red-200 rounded-lg">
        <p class="text-sm text-red-800 font-poppins">{{ errorMessage }}</p>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import BaseInput from '@/components/v2/BaseInput.vue'
import BaseButton from '@/components/v2/BaseButton.vue'

const router = useRouter()
const authStore = useAuthStore()

// Form data
const formData = reactive({
  email: '',
  password: ''
})

// Form errors
const errors = reactive({
  email: '',
  password: ''
})

const isSubmitting = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

/**
 * Validate form fields
 */
const validateForm = () => {
  let isValid = true

  // Reset errors
  Object.keys(errors).forEach(key => {
    errors[key] = ''
  })

  // Email validation
  if (!formData.email) {
    errors.email = 'Email is required'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
    errors.email = 'Email must be valid'
    isValid = false
  }

  // Password validation
  if (!formData.password) {
    errors.password = 'Password is required'
    isValid = false
  }

  return isValid
}

/**
 * Handle form submission
 */
const handleSubmit = async () => {
  // Clear messages
  successMessage.value = ''
  errorMessage.value = ''

  // Validate form
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    // Prepare login data
    const loginData = {
      email: formData.email,
      password: formData.password
    }

    // Call login action from auth store
    await authStore.login(loginData)

    // Show success message
    successMessage.value = 'Login successful! Redirecting...'

    // Redirect based on user role or to home
    // This should be handled by router guards or based on user roles
    setTimeout(() => {
      router.push('/')
    }, 1000)
  } catch (error) {
    console.error('Login error:', error)
    errorMessage.value = error.message || error.response?.data?.message || 'Invalid email or password. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}

/**
 * Navigate to signup page
 */
const handleSignUp = () => {
  router.push('/signup')
}

/**
 * Handle forgot password action
 * TODO: Implement forgot password functionality
 */
const handleForgotPassword = () => {
  console.log('Forgot password clicked')
  // TODO: Navigate to forgot password page or show modal
  errorMessage.value = 'Forgot password feature is not yet implemented.'
}

/**
 * Handle Google login
 */
const handleGoogleLogin = async () => {
  try {
    await authStore.loginWithGoogle()
  } catch (error) {
    console.error('Google login error:', error)
    errorMessage.value = 'Failed to initiate Google login. Please try again.'
  }
}

/**
 * Handle LinkedIn login
 */
const handleLinkedInLogin = async () => {
  try {
    await authStore.loginWithLinkedIn()
  } catch (error) {
    console.error('LinkedIn login error:', error)
    errorMessage.value = 'Failed to initiate LinkedIn login. Please try again.'
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.font-poppins {
  font-family: 'Poppins', sans-serif;
}
</style>
