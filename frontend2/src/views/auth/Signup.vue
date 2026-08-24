<template>
  <div class="min-h-screen bg-surface-alt flex items-center justify-center p-6">
    <div
      class="absolute inset-0 opacity-[0.03] pointer-events-none"
      style="
        background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px);
        background-size: 50px 50px;
      "
    ></div>

    <div class="w-full max-w-5xl relative z-10">
      <router-link
        v-if="currentStep <= 2"
        to="/"
        class="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-black transition mb-4 font-medium"
      >
        <ArrowLeft :size="16" />
        {{ t.common.back }}
      </router-link>

      <div
        v-if="currentStep === 1"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0 }"
        class="text-center mb-12"
      >
        <router-link to="/" class="inline-block mb-8">
          <Logo class="h-10" />
        </router-link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-black">{{ t.auth.signup.roleSelection.title }}</h1>
        <p class="text-gray-500 text-lg">{{ t.auth.signup.roleSelection.subtitle }}</p>
      </div>

      <div v-if="currentStep === 1" class="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
        <div
          v-motion
          :initial="{ opacity: 0, x: -30 }"
          :enter="{ opacity: 1, x: 0 }"
          :delay="100"
          class="bg-white rounded-3xl p-12 border-2 border-gray-100 hover:border-brand-gold hover:shadow-xl transition-all cursor-pointer group"
          :class="{ 'border-brand-gold shadow-xl': selectedRole === 'CLIENT' }"
          @click="selectRole('CLIENT')"
        >
          <div
            class="w-16 h-16 bg-surface-alt rounded-2xl flex items-center justify-center mb-6 group-hover:bg-brand-gold/10 transition-colors"
          >
            <Building2 class="w-8 h-8 text-brand-gold" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-black">{{ t.auth.signup.roleSelection.client.title }}</h3>
          <p class="text-gray-500 leading-relaxed">{{ t.auth.signup.roleSelection.client.desc }}</p>
        </div>

        <div
          v-motion
          :initial="{ opacity: 0, x: 30 }"
          :enter="{ opacity: 1, x: 0 }"
          :delay="200"
          class="bg-white rounded-3xl p-12 border-2 border-gray-100 hover:border-brand-brown hover:shadow-xl transition-all cursor-pointer group"
          :class="{ 'border-brand-brown shadow-xl': selectedRole === 'ARCHITECT' }"
          @click="selectRole('ARCHITECT')"
        >
          <div
            class="w-16 h-16 bg-surface-alt rounded-2xl flex items-center justify-center mb-6 group-hover:bg-brand-brown/10 transition-colors"
          >
            <PenTool class="w-8 h-8 text-brand-brown" />
          </div>
          <h3 class="text-2xl font-bold mb-3 text-black">{{ t.auth.signup.roleSelection.architect.title }}</h3>
          <p class="text-gray-500 leading-relaxed">{{ t.auth.signup.roleSelection.architect.desc }}</p>
        </div>
      </div>

      <div
        v-if="currentStep === 2"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0 }"
        class="bg-white rounded-3xl shadow-2xl p-12 max-w-2xl mx-auto border border-gray-100"
      >
        <div class="text-center mb-10">
          <router-link to="/" class="inline-block mb-6">
            <Logo class="h-10" />
          </router-link>
          <h1 class="text-4xl font-bold mb-3 text-black">{{ t.auth.signup.title }}</h1>
          <p class="text-gray-500">{{ t.auth.signup.subtitle }}</p>
        </div>

        <form class="space-y-6" @submit.prevent="handleSignup">
          <div class="grid md:grid-cols-2 gap-6">
            <BaseInput
              v-model="formData.firstName"
              :label="t.auth.signup.firstName"
              :error="errors.firstName"
              type="text"
              autocomplete="given-name"
              required
            />
            <BaseInput
              v-model="formData.lastName"
              :label="t.auth.signup.lastName"
              :error="errors.lastName"
              type="text"
              autocomplete="family-name"
              required
            />
          </div>

          <BaseInput
            v-model="formData.email"
            :label="t.auth.signup.email"
            :error="errors.email"
            type="email"
            autocomplete="email"
            required
          />

          <BaseInput
            v-model="formData.password"
            :label="t.auth.signup.password"
            :error="errors.password"
            type="password"
            autocomplete="new-password"
            required
          />

          <BaseInput
            v-model="formData.confirmPassword"
            :label="t.auth.signup.confirmPassword"
            :error="errors.confirmPassword"
            type="password"
            autocomplete="new-password"
            required
          />

          <LegalAcceptance ref="legalAcceptance" v-model="formData.agreeTerms" />
          <p v-if="errors.agreeTerms" class="text-sm text-red-500 -mt-3">{{ errors.agreeTerms }}</p>

          <BaseAlert v-if="errorMessage" variant="error">{{ errorMessage }}</BaseAlert>

          <BaseButton
            type="submit"
            :full-width="true"
            :is-loading="isLoading"
            class="bg-brand-gold hover:bg-brand-gold-light text-white border-none"
          >
            {{ t.auth.signup.createAccount }}
          </BaseButton>

          <div class="relative my-8">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-200"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-4 bg-white text-gray-500">{{ t.auth.signup.orContinueWith }}</span>
            </div>
          </div>

          <LegalConsentNotice ref="legalConsentNotice" class="-mt-2 mb-2" />

          <div class="grid grid-cols-2 gap-4">
            <button
              type="button"
              class="flex items-center justify-center gap-3 px-6 py-3.5 border-2 border-gray-200 rounded-full hover:bg-gray-50 hover:border-gray-300 transition-all font-medium"
              @click="handleGoogleLogin"
            >
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
              Google
            </button>
            <button
              type="button"
              class="flex items-center justify-center gap-3 px-6 py-3.5 border-2 border-gray-200 rounded-full hover:bg-gray-50 hover:border-gray-300 transition-all font-medium"
              @click="handleLinkedInLogin"
            >
              <svg class="w-5 h-5" fill="#0A66C2" viewBox="0 0 24 24">
                <path
                  d="M20.447 20.452h-3.554v-5.569c0-1.328-.027-3.037-1.852-3.037-1.853 0-2.136 1.445-2.136 2.939v5.667H9.351V9h3.414v1.561h.046c.477-.9 1.637-1.85 3.37-1.85 3.601 0 4.267 2.37 4.267 5.455v6.286zM5.337 7.433c-1.144 0-2.063-.926-2.063-2.065 0-1.138.92-2.063 2.063-2.063 1.14 0 2.064.925 2.064 2.063 0 1.139-.925 2.065-2.064 2.065zm1.782 13.019H3.555V9h3.564v11.452zM22.225 0H1.771C.792 0 0 .774 0 1.729v20.542C0 23.227.792 24 1.771 24h20.451C23.2 24 24 23.227 24 22.271V1.729C24 .774 23.2 0 22.222 0h.003z"
                />
              </svg>
              LinkedIn
            </button>
          </div>

          <p class="text-center text-gray-500 text-sm">
            {{ t.auth.signup.alreadyHave }}
            <router-link
              :to="{ path: '/login', query: route.query.redirect ? { redirect: route.query.redirect } : {} }"
              class="text-brand-gold hover:text-brand-gold-light font-semibold"
            >
              {{ t.auth.signup.signInHere }}
            </router-link>
          </p>
        </form>
      </div>

      <div
        v-if="currentStep === 3"
        v-motion
        :initial="{ opacity: 0, scale: 0.9 }"
        :enter="{ opacity: 1, scale: 1 }"
        class="bg-white rounded-3xl shadow-2xl p-16 max-w-2xl mx-auto text-center border border-gray-100 relative overflow-hidden"
      >
        <ConfettiExplosion
          v-if="showConfetti"
          :particle-count="100"
          :force="0.3"
          :duration="3000"
          :colors="['#C5A17A', '#7C4728', '#10B981', '#FBBF24']"
        />

        <div class="w-24 h-24 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-8">
          <CheckCircle2 class="w-12 h-12 text-green-600" />
        </div>

        <h1 class="text-4xl font-bold mb-4 text-black">{{ t.auth.signup.success.title }}</h1>
        <p class="text-xl text-gray-500 mb-8">{{ t.auth.signup.success.subtitle }}</p>
        <p class="text-gray-600 mb-12 max-w-md mx-auto leading-relaxed">
          {{ t.auth.signup.success.message }}
        </p>

        <div class="flex items-center justify-center gap-3 text-brand-gold">
          <div class="w-5 h-5 border-2 border-brand-gold border-t-transparent rounded-full animate-spin"></div>
          <span class="font-medium">{{ t.auth.signup.success.redirecting }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectBrief } from '@/composables/useProjectBrief'
import { useLegalStore } from '@/stores/legal'
import { useI18n } from '@/composables/useI18n'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import Logo from '@/components/ui/Logo.vue'
import LegalAcceptance from '@/components/legal/LegalAcceptance.vue'
import LegalConsentNotice from '@/components/legal/LegalConsentNotice.vue'
import ConfettiExplosion from 'vue-confetti-explosion'
import { Building2, PenTool, CheckCircle2, ArrowLeft } from 'lucide-vue-next'
import BaseAlert from '@/components/ui/BaseAlert.vue'

const router = useRouter()
const route = useRoute()

// The brief token arrives nested inside ?redirect=/client/projects/create?brief=… — which
// anyone can craft, so only bind a brief this browser actually created. Otherwise an attacker
// could attach their own brief, and its phone number (which the create form writes to a fresh
// profile), to someone else's new account.
const briefTokenFromRedirect = () => {
  const redirect = route.query.redirect
  if (typeof redirect !== 'string') return null
  const match = redirect.match(/[?&]brief=([^&]+)/)
  if (!match) return null
  const token = decodeURIComponent(match[1])
  return token === storedToken() ? token : null
}
const authStore = useAuthStore()
const { storedToken } = useProjectBrief()
const legalStore = useLegalStore()
const { t, locale } = useI18n()
const legalAcceptance = ref(null)
const legalConsentNotice = ref(null)

const currentStep = ref(1)
const selectedRole = ref(null)
const showConfetti = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

const formData = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

const errors = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: ''
})

onMounted(() => {
  if (route.query.role) {
    selectedRole.value = route.query.role.toUpperCase()
    currentStep.value = 2
  }

  if (route.query.termsUpdated === 'true') {
    legalStore.invalidate('ACCOUNT_TC', locale.value)
    legalStore.invalidate('PRIVACY_POLICY', locale.value)
    formData.value.agreeTerms = false
    errorMessage.value = t.value.auth.signup.legal.staleTerms
  }
})

const selectRole = role => {
  selectedRole.value = role
  setTimeout(() => {
    currentStep.value = 2
  }, 300)
}

const validateForm = () => {
  errors.value = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreeTerms: ''
  }

  let isValid = true

  if (!formData.value.firstName.trim()) {
    errors.value.firstName = t.value.auth.signup.errors.required
    isValid = false
  }

  if (!formData.value.lastName.trim()) {
    errors.value.lastName = t.value.auth.signup.errors.required
    isValid = false
  }

  if (!formData.value.email.trim()) {
    errors.value.email = t.value.auth.signup.errors.required
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    errors.value.email = t.value.auth.signup.errors.invalidEmail
    isValid = false
  }

  if (!formData.value.password) {
    errors.value.password = t.value.auth.signup.errors.required
    isValid = false
  } else if (formData.value.password.length < 8) {
    errors.value.password = t.value.auth.signup.errors.weakPassword
    isValid = false
  } else if (!/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_-]).*$/.test(formData.value.password)) {
    errors.value.password =
      'Password must contain at least one digit, one lowercase, one uppercase, and one special character (@#$%^&+=!_-)'
    isValid = false
  }

  if (!formData.value.confirmPassword) {
    errors.value.confirmPassword = t.value.auth.signup.errors.required
    isValid = false
  } else if (formData.value.password !== formData.value.confirmPassword) {
    errors.value.confirmPassword = t.value.auth.signup.errors.passwordMismatch
    isValid = false
  }

  if (!formData.value.agreeTerms) {
    errors.value.agreeTerms = t.value.auth.signup.errors.agreeTermsRequired
    isValid = false
  }

  return isValid
}

const handleSignup = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const acceptances = legalAcceptance.value?.acceptances || []

    await authStore.register({
      firstName: formData.value.firstName,
      lastName: formData.value.lastName,
      email: formData.value.email,
      password: formData.value.password,
      role: selectedRole.value,
      acceptances,
      // Binds a landing brief to this account now, because the verification email
      // cannot carry the token and may be opened on another device.
      landingBriefToken: briefTokenFromRedirect()
    })

    isLoading.value = false
    currentStep.value = 3
    showConfetti.value = true

    setTimeout(() => {
      // Always go to login after signup (both ARCHITECT and CLIENT)
      router.push({ path: '/login', query: route.query.redirect ? { redirect: route.query.redirect } : {} })
    }, 5000)
  } catch (error) {
    console.error('Signup failed:', error)
    console.error('Error details:', error.response?.data)

    if (error.response?.status === 409 && error.response?.data?.errorCode === 'STALE_TERMS') {
      legalStore.invalidate('ACCOUNT_TC', locale.value)
      legalStore.invalidate('PRIVACY_POLICY', locale.value)
      formData.value.agreeTerms = false
      errorMessage.value = t.value.auth.signup.legal.staleTerms
    } else {
      errorMessage.value = error.response?.data?.message || 'Registration failed. Please try again.'
    }
    isLoading.value = false
  }
}

const handleGoogleLogin = async () => {
  try {
    const acceptances = legalConsentNotice.value?.acceptances || []
    await authStore.loginWithGoogle(selectedRole.value || 'CLIENT', acceptances)
  } catch (error) {
    console.error('Google login failed:', error)
    errorMessage.value = 'Google login failed. Please try again.'
  }
}

const handleLinkedInLogin = async () => {
  try {
    const acceptances = legalConsentNotice.value?.acceptances || []
    await authStore.loginWithLinkedIn(selectedRole.value || 'CLIENT', acceptances)
  } catch (error) {
    console.error('LinkedIn login failed:', error)
    errorMessage.value = 'LinkedIn login failed. Please try again.'
  }
}
</script>
