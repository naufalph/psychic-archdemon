<template>
  <div class="min-h-screen bg-surface-alt flex flex-col">
    <div
      class="min-h-[72px] flex items-center justify-between box-border px-5 md:px-12 py-3 max-w-[1328px] mx-auto w-full"
    >
      <router-link to="/" class="flex items-baseline gap-0.5">
        <span class="text-[22px] font-extrabold tracking-[-.03em] text-ink-900">rumantra</span>
      </router-link>
      <router-link
        v-if="!registered"
        to="/"
        class="h-11 inline-flex items-center gap-2 px-4 rounded-full border border-hairline bg-white text-caption font-semibold text-ink-900 hover:border-ink-500 transition-colors"
      >
        <ArrowLeft :size="16" />
        {{ t.common.back }}
      </router-link>
    </div>

    <div class="flex-1 flex items-center justify-center box-border px-5 md:px-12 py-10 md:py-16">
      <div
        v-if="!registered"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0 }"
        class="w-full max-w-[920px] bg-white rounded-[20px] border border-hairline overflow-hidden flex flex-wrap"
      >
        <div
          class="flex-[1_1_320px] bg-no-repeat text-white p-10 box-border flex flex-col gap-8"
          :style="{ backgroundImage: `url(${heroBg})`, backgroundSize: 'cover', backgroundPosition: 'left top' }"
        >
          <div class="flex items-baseline gap-0.5">
            <span class="text-[22px] font-extrabold tracking-[-.03em]">rumantra</span>
          </div>
          <div>
            <h3 class="text-[28px] font-bold tracking-[-.03em] leading-[1.15] m-0 mb-3">{{ tlLogin.panelTitle }}</h3>
            <p class="text-[15px] leading-relaxed text-white/85 m-0">{{ tlLogin.panelSubtitle }}</p>
          </div>
          <div class="flex flex-col gap-4 mt-auto">
            <div v-for="perk in tlLogin.perks" :key="perk" class="flex gap-3 items-start">
              <span class="flex-none w-6 h-6 rounded-full bg-white text-ink-900 flex items-center justify-center">
                <Check :size="13" :stroke-width="3" />
              </span>
              <span class="text-[15px] leading-normal">{{ perk }}</span>
            </div>
          </div>
        </div>

        <div class="flex-[1_1_380px] px-6 md:px-10 py-10 box-border flex flex-col">
          <div class="flex gap-1 p-1 border border-hairline rounded-full mb-7">
            <router-link
              :to="loginLink"
              class="flex-1 h-10 rounded-full text-caption font-bold text-ink-400 hover:text-ink-900 transition-colors flex items-center justify-center"
            >
              {{ tlLogin.tabLogin }}
            </router-link>
            <span
              class="flex-1 h-10 rounded-full text-caption font-bold bg-ink-900 text-white flex items-center justify-center"
            >
              {{ tlLogin.tabRegister }}
            </span>
          </div>

          <h1 class="text-[24px] font-bold tracking-[-.025em] m-0 mb-2 text-ink-900">{{ ts.registerTitle }}</h1>
          <p class="text-[15px] leading-relaxed text-ink-400 m-0 mb-6">{{ ts.registerSubtitle }}</p>

          <div class="mb-6">
            <p :class="labelCls">{{ ts.registerAs }}</p>
            <div class="grid grid-cols-2 gap-2" role="radiogroup" :aria-label="ts.registerAs">
              <button
                v-for="role in roleOptions"
                :key="role.value"
                type="button"
                role="radio"
                :aria-checked="selectedRole === role.value"
                class="h-12 flex items-center justify-center gap-2 rounded-xl border text-[15px] font-semibold transition-colors"
                :class="
                  selectedRole === role.value
                    ? 'border-ink-900 bg-ink-900 text-white'
                    : 'border-hairline bg-white text-ink-700 hover:border-ink-500'
                "
                @click="selectedRole = role.value"
              >
                <component :is="role.icon" :size="16" />
                {{ role.label }}
              </button>
            </div>
          </div>

          <div class="flex flex-col gap-3">
            <button type="button" :class="socialBtn" @click="handleGoogleLogin">
              <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
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
              {{ tlLogin.googleCta }}
            </button>
            <button type="button" :class="socialBtn" @click="handleLinkedInLogin">
              <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
                <rect width="24" height="24" rx="4" fill="#0A66C2" />
                <path
                  fill="#fff"
                  d="M7.1 9.5h-2.6V19h2.6V9.5zM5.8 5a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM19.5 13.6c0-2.6-1.4-4.3-3.7-4.3-1.3 0-2.2.7-2.6 1.4V9.5h-2.5V19h2.6v-5c0-1.3.6-2.2 1.7-2.2 1.1 0 1.8.8 1.8 2.2v5h2.7v-5.4z"
                />
              </svg>
              {{ tlLogin.linkedinCta }}
            </button>
          </div>
          <LegalConsentNotice ref="legalConsentNotice" class="mt-3" />

          <div class="flex items-center gap-3 my-5 text-caption text-ink-400">
            <span class="flex-1 h-px bg-hairline"></span>{{ ts.orEmail }}<span class="flex-1 h-px bg-hairline"></span>
          </div>

          <form class="flex flex-col gap-5" novalidate @submit.prevent="handleSignup">
            <div class="grid grid-cols-2 gap-3">
              <div v-for="field in nameFields" :key="field.key">
                <label :for="ids[field.key]" :class="labelCls">{{ ts[field.key] }}</label>
                <input
                  :id="ids[field.key]"
                  v-model="formData[field.key]"
                  type="text"
                  :autocomplete="field.autocomplete"
                  :class="[inputCls, errors[field.key] && 'border-[#B42318]']"
                />
                <p v-if="errors[field.key]" :class="errorCls">{{ errors[field.key] }}</p>
              </div>
            </div>
            <div v-for="field in credentialFields" :key="field.key">
              <label :for="ids[field.key]" :class="labelCls">{{ ts[field.key] }}</label>
              <input
                :id="ids[field.key]"
                v-model="formData[field.key]"
                :type="field.type"
                :autocomplete="field.autocomplete"
                :placeholder="field.placeholder"
                :class="[inputCls, errors[field.key] && 'border-[#B42318]']"
              />
              <p v-if="errors[field.key]" :class="errorCls">{{ errors[field.key] }}</p>
            </div>

            <div>
              <LegalAcceptance ref="legalAcceptance" v-model="formData.agreeTerms" />
              <p v-if="errors.agreeTerms" :class="errorCls">{{ errors.agreeTerms }}</p>
            </div>

            <p v-if="errorMessage" class="text-caption leading-normal text-[#B42318] m-0">{{ errorMessage }}</p>

            <button type="submit" :class="primaryBtn" :disabled="isLoading">
              <span
                v-if="isLoading"
                class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
              ></span>
              {{ ts.createAccount }}
            </button>
          </form>

          <p class="text-center text-caption text-ink-400 mt-6 mb-0">
            {{ ts.alreadyHave }}
            <router-link :to="loginLink" class="font-semibold text-ink-900 hover:underline">
              {{ ts.signInHere }}
            </router-link>
          </p>
        </div>
      </div>

      <div
        v-else
        v-motion
        :initial="{ opacity: 0, scale: 0.96 }"
        :enter="{ opacity: 1, scale: 1 }"
        class="w-full bg-white rounded-[20px] border border-hairline p-10 md:p-14 max-w-[600px] mx-auto text-center relative overflow-hidden"
      >
        <ConfettiExplosion
          v-if="showConfetti"
          :particle-count="100"
          :force="0.3"
          :duration="3000"
          :colors="['#185C93', '#2F7DC0', '#C5A17A', '#0A0A0A']"
        />

        <div class="w-16 h-16 bg-surface-blue rounded-full flex items-center justify-center mx-auto mb-8">
          <CheckCircle2 class="w-8 h-8 text-accent-blue" />
        </div>

        <h1 class="text-[36px] font-bold tracking-[-.025em] leading-[1.15] m-0 mb-3 text-ink-900">
          {{ ts.success.title }}
        </h1>
        <p class="text-body-lg text-ink-500 m-0 mb-6">{{ ts.success.subtitle }}</p>
        <p class="text-body text-ink-500 mb-10 max-w-md mx-auto leading-relaxed">
          {{ ts.success.message }}
        </p>

        <div class="flex items-center justify-center gap-3 text-accent-blue">
          <div class="w-5 h-5 border-2 border-accent-blue border-t-transparent rounded-full animate-spin"></div>
          <span class="text-body-sm font-semibold">{{ ts.success.redirecting }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, useId } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectBrief } from '@/composables/useProjectBrief'
import { useLegalStore } from '@/stores/legal'
import { useI18n } from '@/composables/useI18n'
import heroBg from '@/assets/images/landing/hero-background.jpg'
import LegalAcceptance from '@/components/legal/LegalAcceptance.vue'
import LegalConsentNotice from '@/components/legal/LegalConsentNotice.vue'
import ConfettiExplosion from 'vue-confetti-explosion'
import { Building2, PenTool, CheckCircle2, ArrowLeft, Check } from 'lucide-vue-next'

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

const ts = computed(() => t.value.auth.signup)
const tlLogin = computed(() => t.value.auth.login)

const roleOptions = computed(() => [
  { value: 'CLIENT', icon: Building2, label: ts.value.roleClient },
  { value: 'ARCHITECT', icon: PenTool, label: ts.value.roleArchitect }
])

const ids = {
  firstName: useId(),
  lastName: useId(),
  email: useId(),
  password: useId(),
  confirmPassword: useId()
}
const nameFields = [
  { key: 'firstName', autocomplete: 'given-name' },
  { key: 'lastName', autocomplete: 'family-name' }
]
const credentialFields = computed(() => [
  { key: 'email', type: 'email', autocomplete: 'email', placeholder: tlLogin.value.emailPlaceholder },
  { key: 'password', type: 'password', autocomplete: 'new-password' },
  { key: 'confirmPassword', type: 'password', autocomplete: 'new-password' }
])
const labelCls = 'block text-body font-medium text-ink-700 mb-2.5 mt-0'
const inputCls =
  'w-full box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors'
const errorCls = 'text-caption-sm text-[#B42318] mt-1.5 mb-0'
const primaryBtn =
  'w-full h-[52px] flex items-center justify-center gap-3 rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed'
const socialBtn =
  'w-full h-[52px] flex items-center justify-center gap-3 rounded-full border border-hairline bg-white text-ink-900 text-[15px] font-bold hover:border-ink-900 transition-colors'

const loginLink = computed(() => ({
  path: '/login',
  query: route.query.redirect ? { redirect: route.query.redirect } : {}
}))

const registered = ref(false)
const selectedRole = ref('CLIENT')
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
  const role = String(route.query.role || '').toUpperCase()
  if (role === 'CLIENT' || role === 'ARCHITECT') selectedRole.value = role

  if (route.query.termsUpdated === 'true') {
    legalStore.invalidate('ACCOUNT_TC', locale.value)
    legalStore.invalidate('PRIVACY_POLICY', locale.value)
    formData.value.agreeTerms = false
    errorMessage.value = t.value.auth.signup.legal.staleTerms
  }
})

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
    registered.value = true
    showConfetti.value = true

    setTimeout(() => {
      // Always go to login after signup (both ARCHITECT and CLIENT)
      router.push(loginLink.value)
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
    await authStore.loginWithGoogle(selectedRole.value, acceptances)
  } catch (error) {
    console.error('Google login failed:', error)
    errorMessage.value = 'Google login failed. Please try again.'
  }
}

const handleLinkedInLogin = async () => {
  try {
    const acceptances = legalConsentNotice.value?.acceptances || []
    await authStore.loginWithLinkedIn(selectedRole.value, acceptances)
  } catch (error) {
    console.error('LinkedIn login failed:', error)
    errorMessage.value = 'LinkedIn login failed. Please try again.'
  }
}
</script>
