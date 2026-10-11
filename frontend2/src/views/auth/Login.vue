<template>
  <div class="min-h-screen bg-white flex flex-col">
    <div
      class="min-h-[72px] flex items-center justify-between box-border px-5 md:px-12 py-3 max-w-[1328px] mx-auto w-full"
    >
      <router-link to="/" class="flex items-baseline gap-0.5">
        <span class="text-[22px] font-extrabold tracking-[-.03em] text-ink-900">rumantra</span>
      </router-link>
      <router-link
        to="/"
        class="h-11 inline-flex items-center gap-2 px-4 rounded-full border border-hairline bg-white text-caption font-semibold text-ink-900 hover:border-ink-500 transition-colors"
      >
        <ArrowLeft :size="16" />
        {{ t.common.back }}
      </router-link>
    </div>

    <div class="flex-1 flex items-center justify-center box-border px-5 md:px-12 py-10 md:py-16">
      <div
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
            <h3 class="text-[28px] font-bold tracking-[-.03em] leading-[1.15] m-0 mb-3">{{ tl.panelTitle }}</h3>
            <p class="text-[15px] leading-relaxed text-white/85 m-0">{{ tl.panelSubtitle }}</p>
          </div>
          <div class="flex flex-col gap-4 mt-auto">
            <div v-for="perk in tl.perks" :key="perk" class="flex gap-3 items-start">
              <span class="flex-none w-6 h-6 rounded-full bg-white text-ink-900 flex items-center justify-center">
                <Check :size="13" :stroke-width="3" />
              </span>
              <span class="text-[15px] leading-normal">{{ perk }}</span>
            </div>
          </div>
        </div>

        <div class="flex-[1_1_380px] px-6 md:px-10 py-10 box-border flex flex-col">
          <div class="flex gap-1 p-1 border border-hairline rounded-full mb-7">
            <span
              class="flex-1 h-10 rounded-full text-caption font-bold bg-ink-900 text-white flex items-center justify-center"
            >
              {{ tl.tabLogin }}
            </span>
            <router-link
              :to="signupLink"
              class="flex-1 h-10 rounded-full text-caption font-bold text-ink-400 hover:text-ink-900 transition-colors flex items-center justify-center"
            >
              {{ tl.tabRegister }}
            </router-link>
          </div>

          <h1 class="text-[24px] font-bold tracking-[-.025em] m-0 mb-2 text-ink-900">{{ tl.title }}</h1>
          <p class="text-[15px] leading-relaxed text-ink-400 m-0 mb-6">{{ tl.subtitle }}</p>

          <div
            v-if="route.query.verified === 'true'"
            class="mb-5 px-4 py-3 rounded-xl bg-surface-blue text-caption leading-normal text-accent-blue"
          >
            {{ tl.verified }}
          </div>

          <div class="flex flex-col gap-3">
            <button type="button" :class="socialBtn" @click="handleGoogleLogin">
              <svg width="20" height="20" viewBox="0 0 48 48" aria-hidden="true">
                <path
                  fill="#EA4335"
                  d="M24 9.5c3.5 0 6.6 1.2 9 3.5l6.7-6.7C35.6 2.4 30.2 0 24 0 14.6 0 6.6 5.4 2.7 13.3l7.8 6.1C12.4 13.6 17.7 9.5 24 9.5z"
                />
                <path
                  fill="#4285F4"
                  d="M46.5 24.5c0-1.6-.1-3.1-.4-4.5H24v9h12.7c-.6 3-2.3 5.5-4.8 7.2l7.5 5.8c4.4-4.1 7.1-10.1 7.1-17.5z"
                />
                <path
                  fill="#FBBC05"
                  d="M10.5 28.6c-.5-1.4-.8-3-.8-4.6s.3-3.2.8-4.6l-7.8-6.1C1 16.6 0 20.2 0 24s1 7.4 2.7 10.7l7.8-6.1z"
                />
                <path
                  fill="#34A853"
                  d="M24 48c6.5 0 11.9-2.1 15.9-5.8l-7.5-5.8c-2.1 1.4-4.8 2.3-8.4 2.3-6.3 0-11.6-4.1-13.5-9.9l-7.8 6.1C6.6 42.6 14.6 48 24 48z"
                />
              </svg>
              {{ tl.googleCta }}
            </button>
            <button type="button" :class="socialBtn" @click="handleLinkedInLogin">
              <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
                <rect width="24" height="24" rx="4" fill="#0A66C2" />
                <path
                  fill="#fff"
                  d="M7.1 9.5h-2.6V19h2.6V9.5zM5.8 5a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM19.5 13.6c0-2.6-1.4-4.3-3.7-4.3-1.3 0-2.2.7-2.6 1.4V9.5h-2.5V19h2.6v-5c0-1.3.6-2.2 1.7-2.2 1.1 0 1.8.8 1.8 2.2v5h2.7v-5.4z"
                />
              </svg>
              {{ tl.linkedinCta }}
            </button>
          </div>
          <LegalConsentNotice ref="legalConsentNotice" class="mt-3" />

          <div class="flex items-center gap-3 my-5 text-caption text-ink-400">
            <span class="flex-1 h-px bg-hairline"></span>{{ tl.orEmail }}<span class="flex-1 h-px bg-hairline"></span>
          </div>

          <form class="flex flex-col gap-5" novalidate @submit.prevent="handleLogin">
            <div>
              <label :for="ids.email" :class="labelCls">{{ tl.email }}</label>
              <input
                :id="ids.email"
                v-model="formData.email"
                type="email"
                autocomplete="email"
                :placeholder="tl.emailPlaceholder"
                :class="[inputCls, errors.email && 'border-[#B42318]']"
              />
              <p v-if="errors.email" class="text-caption-sm text-[#B42318] mt-1.5 mb-0">{{ errors.email }}</p>
            </div>
            <div>
              <div class="flex items-baseline justify-between mb-2.5">
                <label :for="ids.password" class="text-body font-medium text-ink-700">{{ tl.password }}</label>
                <a href="#" class="text-caption font-semibold text-accent-blue hover:underline">
                  {{ tl.forgotPassword }}
                </a>
              </div>
              <input
                :id="ids.password"
                v-model="formData.password"
                type="password"
                autocomplete="current-password"
                :placeholder="tl.passwordPlaceholder"
                :class="[inputCls, errors.password && 'border-[#B42318]']"
              />
              <p v-if="errors.password" class="text-caption-sm text-[#B42318] mt-1.5 mb-0">
                {{ errors.password }}
              </p>
            </div>

            <p v-if="errorMessage" class="text-caption leading-normal text-[#B42318] m-0">{{ errorMessage }}</p>

            <button type="submit" :class="primaryBtn" :disabled="isLoading">
              <span
                v-if="isLoading"
                class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
              ></span>
              {{ tl.signIn }}
            </button>
          </form>

          <p class="text-center text-caption text-ink-400 mt-6 mb-0">
            {{ tl.noAccount }}
            <router-link :to="signupLink" class="font-semibold text-ink-900 hover:underline">
              {{ tl.signUpHere }}
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, useId } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Check } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useProjectBrief } from '@/composables/useProjectBrief'
import { useI18n } from '@/composables/useI18n'
import heroBg from '@/assets/images/landing/hero-background.jpg'
import LegalConsentNotice from '@/components/legal/LegalConsentNotice.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { t } = useI18n()
const legalConsentNotice = ref(null)
const { pendingBriefPath } = useProjectBrief()
const tl = computed(() => t.value.auth.login)

const ids = { email: useId(), password: useId() }
const labelCls = 'block text-body font-medium text-ink-700 mb-2.5'
const inputCls =
  'w-full box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors'
const primaryBtn =
  'w-full h-[52px] flex items-center justify-center gap-3 rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed'
const socialBtn =
  'w-full h-[52px] flex items-center justify-center gap-3 rounded-full border border-hairline bg-white text-ink-900 text-[15px] font-bold hover:border-ink-900 transition-colors'

const signupLink = computed(() => ({
  path: '/signup',
  query: route.query.redirect ? { redirect: route.query.redirect } : {}
}))

const isLoading = ref(false)
const errorMessage = ref('')

const formData = ref({
  email: '',
  password: ''
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

const handleEmailVerification = async token => {
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

const getRedirectPath = user => {
  const roles = user?.registeredRoles || []

  // Priority 2: Use lastLoginRole from database
  if (user?.lastLoginRole && roles.includes(user.lastLoginRole)) {
    return user.lastLoginRole === 'ARCHITECT' ? '/architect/dashboard' : '/client/dashboard'
  }

  // Priority 3: Single role
  if (roles.length === 1) {
    return roles[0] === 'ARCHITECT' ? '/architect/dashboard' : '/client/dashboard'
  }

  // Priority 4: Default to CLIENT if user has both roles
  if (roles.includes('CLIENT')) {
    return '/client/dashboard'
  }

  if (roles.includes('ARCHITECT')) {
    return '/architect/dashboard'
  }

  // Priority 5: No roles
  return '/'
}

const handleOAuthCallback = () => {
  const token = route.query.token
  const email = route.query.email
  const id = route.query.id
  const rolesParam = route.query.roles
  const needsClientOnboarding = route.query.needsClientOnboarding

  if (token && email && id) {
    const registeredRoles = rolesParam ? rolesParam.split(',') : []

    localStorage.setItem('auth_token', token)
    authStore.user = {
      id,
      email,
      registeredRoles,
      needsClientOnboarding: needsClientOnboarding === 'true' ? true : needsClientOnboarding === 'false' ? false : null
    }
    authStore.token = token

    const redirectPath = pendingBriefPath(authStore.user) || getRedirectPath(authStore.user)
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
      const redirectPath = route.query.redirect || pendingBriefPath(result.user) || getRedirectPath(result.user)
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
    const acceptances = legalConsentNotice.value?.acceptances || []
    await authStore.loginWithGoogle(null, acceptances)
  } catch (error) {
    console.error('Google login failed:', error)
    errorMessage.value = 'Google login failed. Please try again.'
  }
}

const handleLinkedInLogin = async () => {
  try {
    const acceptances = legalConsentNotice.value?.acceptances || []
    await authStore.loginWithLinkedIn(null, acceptances)
  } catch (error) {
    console.error('LinkedIn login failed:', error)
    errorMessage.value = 'LinkedIn login failed. Please try again.'
  }
}
</script>
