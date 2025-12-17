<template>
  <div
    class="w-full max-w-[538px] bg-white rounded-2xl shadow-[0px_4px_4px_0px_rgba(0,0,0,0.3),0px_8px_12px_6px_rgba(0,0,0,0.15)] px-4 py-16"
  >
    <!-- Back Arrow -->
    <button type="button" class="mb-6 hover:opacity-70 transition-opacity" @click="handleBack">
      <img src="@/assets/images/sign/back-sign.svg" alt="Go back" class="w-10 h-10" />
    </button>

    <!-- Title -->
    <h2 class="text-center font-poppins font-semibold text-4xl leading-[1.5em] text-black mb-6">Sign Up</h2>

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

      <!-- Password Input -->
      <BaseInput
        v-model="formData.password"
        label="Password"
        type="password"
        placeholder="Input password here"
        :error-message="errors.password"
      />

      <!-- Confirm Password Input -->
      <BaseInput
        v-model="formData.confirmPassword"
        label="Confirm Password"
        type="password"
        placeholder="Input password here"
        :error-message="errors.confirmPassword"
      />

      <!-- Checkboxes -->
      <div class="space-y-3">
        <BaseCheckbox v-model="formData.agreedToTerms" label="I have read and agree to the Terms and Conditions" />
        <p v-if="errors.agreedToTerms" class="text-xs text-red-500 ml-7 -mt-2">
          {{ errors.agreedToTerms }}
        </p>

        <BaseCheckbox v-model="formData.agreedToPrivacy" label="I have read and agree to the Privacy Policy" />
        <p v-if="errors.agreedToPrivacy" class="text-xs text-red-500 ml-7 -mt-2">
          {{ errors.agreedToPrivacy }}
        </p>

        <BaseCheckbox
          v-model="formData.newsletter"
          label="I am interested in receiving news, discounts and promotions via email."
        />
      </div>

      <!-- Submit Button -->
      <BaseButton type="submit" :loading="isSubmitting" block> Sign Up </BaseButton>

      <!-- Divider -->
      <div class="relative">
        <div class="absolute inset-0 flex items-center">
          <div class="w-full border-t border-gray-300"></div>
        </div>
        <div class="relative flex justify-center text-sm">
          <span class="px-2 bg-white text-gray-500 font-poppins">Or continue with</span>
        </div>
      </div>

      <!-- OAuth Buttons -->
      <div class="grid grid-cols-2 gap-4">
        <!-- Google Button -->
        <BaseButton type="button" variant="outline" :disabled="isSubmitting" @click="handleGoogleSignup">
          <div class="flex items-center justify-center gap-2">
            <svg class="w-5 h-5" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              />
            </svg>
            <span>Google</span>
          </div>
        </BaseButton>

        <!-- LinkedIn Button -->
        <BaseButton type="button" variant="outline" :disabled="isSubmitting" @click="handleLinkedInSignup">
          <div class="flex items-center justify-center gap-2">
            <svg class="w-5 h-5" fill="#0A66C2" viewBox="0 0 24 24">
              <path
                d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"
              />
            </svg>
            <span>LinkedIn</span>
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
import BaseCheckbox from '@/components/v2/BaseCheckbox.vue'
import BaseButton from '@/components/v2/BaseButton.vue'

const router = useRouter()
const authStore = useAuthStore()

// Form data
const formData = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  agreedToTerms: false,
  agreedToPrivacy: false,
  newsletter: false
})

// Form errors
const errors = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  agreedToTerms: '',
  agreedToPrivacy: ''
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
  } else if (formData.password.length < 8) {
    errors.password = 'Password must be at least 8 characters long'
    isValid = false
  } else if (!/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_-]).*$/.test(formData.password)) {
    errors.password =
      'Password must contain at least one digit, one lowercase, one uppercase, and one special character'
    isValid = false
  }

  // Confirm password validation
  if (!formData.confirmPassword) {
    errors.confirmPassword = 'Please confirm your password'
    isValid = false
  } else if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = 'Passwords do not match'
    isValid = false
  }

  // Terms checkbox validation
  if (!formData.agreedToTerms) {
    errors.agreedToTerms = 'You must agree to the Terms and Conditions'
    isValid = false
  }

  // Privacy checkbox validation
  if (!formData.agreedToPrivacy) {
    errors.agreedToPrivacy = 'You must agree to the Privacy Policy'
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
    // Prepare registration data
    const registrationData = {
      email: formData.email,
      password: formData.password,
      role: 'CLIENT' // Default to CLIENT as discussed
    }

    // Call register action from auth store
    await authStore.register(registrationData)

    // Show success message
    successMessage.value = 'Registration successful! Please check your email to verify your account.'

    // Reset form
    Object.keys(formData).forEach(key => {
      if (typeof formData[key] === 'boolean') {
        formData[key] = false
      } else {
        formData[key] = ''
      }
    })
  } catch (error) {
    console.error('Registration error:', error)
    errorMessage.value = error.response?.data?.message || 'An error occurred during registration. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}

/**
 * Handle Google signup
 */
const handleGoogleSignup = async () => {
  try {
    await authStore.loginWithGoogle()
  } catch (error) {
    console.error('Google signup error:', error)
    errorMessage.value = 'Failed to initiate Google signup. Please try again.'
  }
}

/**
 * Handle LinkedIn signup
 */
const handleLinkedInSignup = async () => {
  try {
    await authStore.loginWithLinkedIn()
  } catch (error) {
    console.error('LinkedIn signup error:', error)
    errorMessage.value = 'Failed to initiate LinkedIn signup. Please try again.'
  }
}

/**
 * Handle back button click
 */
const handleBack = () => {
  router.back()
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.font-poppins {
  font-family: 'Poppins', sans-serif;
}
</style>
