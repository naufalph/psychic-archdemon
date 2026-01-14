<template>
  <div class="min-h-screen bg-[#F4F5F7] flex items-center justify-center p-6">
    <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px); background-size: 50px 50px"></div>

    <div v-motion :initial="{ opacity: 0, y: 20 }" :enter="{ opacity: 1, y: 0 }" class="w-full max-w-md relative z-10">
      <div class="bg-white rounded-3xl shadow-2xl p-12 border border-gray-100">
        <div class="text-center mb-10">
          <router-link to="/" class="inline-block mb-6">
            <Logo class="h-10" />
          </router-link>
          <h1 class="text-4xl font-bold mb-3 text-black">{{ t.auth.login.title }}</h1>
          <p class="text-gray-500">{{ t.auth.login.subtitle }}</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-6">
          <BaseInput
            v-model="formData.email"
            :label="t.auth.login.email"
            :error="errors.email"
            type="email"
            autocomplete="email"
            required
          />

          <BaseInput
            v-model="formData.password"
            :label="t.auth.login.password"
            :error="errors.password"
            type="password"
            autocomplete="current-password"
            required
          />

          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <input
                v-model="formData.rememberMe"
                type="checkbox"
                id="rememberMe"
                class="w-4 h-4 rounded border-gray-300 text-[#C5A17A] focus:ring-[#C5A17A]"
              />
              <label for="rememberMe" class="text-sm text-gray-600 cursor-pointer">
                {{ t.auth.login.rememberMe }}
              </label>
            </div>
            <a href="#" class="text-sm text-[#C5A17A] hover:text-[#B39069] font-semibold">
              {{ t.auth.login.forgotPassword }}
            </a>
          </div>

          <p v-if="errorMessage" class="text-red-500 text-sm bg-red-50 p-4 rounded-2xl border border-red-200">
            {{ errorMessage }}
          </p>

          <BaseButton
            type="submit"
            :fullWidth="true"
            :isLoading="isLoading"
            class="bg-[#C5A17A] hover:bg-[#B39069] text-white border-none"
          >
            {{ t.auth.login.signIn }}
          </BaseButton>

          <div class="relative my-8">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-200"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-4 bg-white text-gray-500">{{ t.auth.login.orContinueWith }}</span>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <button
              type="button"
              @click="handleGoogleLogin"
              class="flex items-center justify-center gap-3 px-6 py-3.5 border-2 border-gray-200 rounded-full hover:bg-gray-50 hover:border-gray-300 transition-all font-medium"
            >
              <svg class="w-5 h-5" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Google
            </button>
            <button
              type="button"
              @click="handleLinkedInLogin"
              class="flex items-center justify-center gap-3 px-6 py-3.5 border-2 border-gray-200 rounded-full hover:bg-gray-50 hover:border-gray-300 transition-all font-medium"
            >
              <svg class="w-5 h-5" fill="#0A66C2" viewBox="0 0 24 24">
                <path d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"/>
              </svg>
              LinkedIn
            </button>
          </div>

          <p class="text-center text-gray-500 text-sm">
            {{ t.auth.login.noAccount }}
            <router-link to="/signup" class="text-[#C5A17A] hover:text-[#B39069] font-semibold">
              {{ t.auth.login.signUpHere }}
            </router-link>
          </p>
        </form>
      </div>

      <div v-if="route.query.verified === 'true'" class="mt-6 bg-green-50 border border-green-200 rounded-2xl p-4">
        <p class="text-green-800 text-sm text-center">
          Email verified successfully! Please sign in to continue.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import Logo from '@/components/ui/Logo.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { t } = useI18n()

const isLoading = ref(false)
const errorMessage = ref('')

const formData = ref({
  email: '',
  password: '',
  rememberMe: false
})

const errors = ref({
  email: '',
  password: ''
})

onMounted(() => {
  if (route.query.token) {
    handleEmailVerification(route.query.token)
  }

  if (route.query.success === 'true' && route.query.token) {
    handleOAuthCallback()
  }

  if (route.query.email) {
    formData.value.email = route.query.email
  }
})

const handleEmailVerification = async (token) => {
  isLoading.value = true
  try {
    await authStore.verifyEmail(token)
    router.replace({ query: { success: 'true' } })
  } catch (error) {
    console.error('Email verification failed:', error)
    errorMessage.value = error.response?.data?.message || 'Email verification failed'
  } finally {
    isLoading.value = false
  }
}

const getRedirectPath = (user) => {
  const roles = user?.registeredRoles || []

  const lastDashboardRole = localStorage.getItem('last_dashboard_role')
  if (lastDashboardRole && roles.includes(lastDashboardRole)) {
    return lastDashboardRole === 'ARCHITECT' ? '/architect/dashboard' : '/client/dashboard'
  }

  if (roles.includes('ARCHITECT')) {
    return '/architect/dashboard'
  }

  if (roles.includes('CLIENT')) {
    return '/client/dashboard'
  }

  return '/'
}

const handleOAuthCallback = () => {
  const token = route.query.token
  const email = route.query.email
  const id = route.query.id
  const rolesParam = route.query.roles

  if (token && email && id) {
    const registeredRoles = rolesParam ? rolesParam.split(',') : []

    localStorage.setItem('auth_token', token)
    authStore.user = { id, email, registeredRoles }
    authStore.token = token

    const redirectPath = getRedirectPath(authStore.user)
    router.push(redirectPath)
  }
}

const validateForm = () => {
  errors.value = {
    email: '',
    password: ''
  }

  let isValid = true

  if (!formData.value.email.trim()) {
    errors.value.email = t.value.auth.login.errors.required
    isValid = false
  }

  if (!formData.value.password) {
    errors.value.password = t.value.auth.login.errors.required
    isValid = false
  }

  return isValid
}

const handleLogin = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await authStore.login({
      email: formData.value.email,
      password: formData.value.password
    })

    if (result.success) {
      const redirectPath = route.query.redirect || getRedirectPath(result.user)
      router.push(redirectPath)
    }
  } catch (error) {
    console.error('Login failed:', error)
    errorMessage.value = error.response?.data?.message || t.value.auth.login.errors.invalidCredentials
  } finally {
    isLoading.value = false
  }
}

const handleGoogleLogin = async () => {
  try {
    await authStore.loginWithGoogle()
  } catch (error) {
    console.error('Google login failed:', error)
    errorMessage.value = 'Google login failed. Please try again.'
  }
}

const handleLinkedInLogin = async () => {
  try {
    await authStore.loginWithLinkedIn()
  } catch (error) {
    console.error('LinkedIn login failed:', error)
    errorMessage.value = 'LinkedIn login failed. Please try again.'
  }
}
</script>
