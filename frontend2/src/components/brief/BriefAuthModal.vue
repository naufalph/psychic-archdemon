<template>
  <BriefModal :open="open" panel-class="max-w-[920px] overflow-hidden flex flex-wrap" @close="$emit('close')">
    <button
      type="button"
      :aria-label="tv.close"
      class="absolute top-5 right-5 z-[2] w-11 h-11 rounded-full border border-hairline bg-white text-ink-900 flex items-center justify-center hover:border-ink-900 transition-colors"
      @click="$emit('close')"
    >
      <X :size="18" :stroke-width="2.2" />
    </button>

    <div class="flex-[1_1_320px] bg-ink-900 text-white p-10 box-border flex flex-col gap-8">
      <div class="flex items-baseline gap-0.5">
        <span class="text-[22px] font-extrabold tracking-[-.03em]">rumantra</span>
        <span class="text-[22px] font-extrabold text-brand-gold">.</span>
      </div>
      <div>
        <h3 class="text-[28px] font-bold tracking-[-.03em] leading-[1.15] m-0 mb-3">{{ tv.panelTitle }}</h3>
        <p class="text-[15px] leading-relaxed text-white/75 m-0">{{ tv.panelSubtitle }}</p>
      </div>
      <div class="flex flex-col gap-4 mt-auto">
        <div v-for="perk in tv.perks" :key="perk" class="flex gap-3 items-start">
          <span class="flex-none w-6 h-6 rounded-full bg-white/[.12] flex items-center justify-center">
            <Check :size="13" :stroke-width="3" />
          </span>
          <span class="text-[15px] leading-normal">{{ perk }}</span>
        </div>
      </div>
    </div>

    <div class="flex-[1_1_380px] pt-[84px] px-6 md:px-10 pb-10 box-border min-h-[560px] flex flex-col">
      <template v-if="view === 'verify'">
        <div class="my-auto flex flex-col items-start gap-4">
          <span class="w-14 h-14 rounded-full bg-surface-blue text-accent-blue flex items-center justify-center">
            <Mail :size="24" />
          </span>
          <h2 class="text-[24px] font-bold tracking-[-.025em] m-0 text-ink-900">{{ tv.verifyTitle }}</h2>
          <p class="text-[15px] leading-relaxed text-ink-400 m-0">{{ tv.verifyBody.replace('{email}', email) }}</p>
          <button type="button" :class="primaryBtn" class="mt-2" @click="switchTo('login')">
            {{ tv.verifyCta }}
          </button>
        </div>
      </template>

      <template v-else>
        <div class="flex gap-1 p-1 border border-hairline rounded-full mb-7">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            type="button"
            class="flex-1 h-10 rounded-full text-caption font-bold transition-colors"
            :class="view === tab.id ? 'bg-ink-900 text-white' : 'bg-transparent text-ink-400 hover:text-ink-900'"
            @click="switchTo(tab.id)"
          >
            {{ tab.label }}
          </button>
        </div>

        <h2 class="text-[24px] font-bold tracking-[-.025em] m-0 mb-2 text-ink-900">
          {{ view === 'login' ? tv.loginTitle : tv.registerTitle }}
        </h2>
        <p class="text-[15px] leading-relaxed text-ink-400 m-0 mb-6">
          {{ view === 'login' ? tv.loginSubtitle : tv.registerSubtitle }}
        </p>

        <div class="flex flex-col gap-3">
          <button type="button" :class="socialBtn" @click="social('google')">
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
            {{ tv.google }}
          </button>
          <button type="button" :class="socialBtn" @click="social('linkedin')">
            <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
              <rect width="24" height="24" rx="4" fill="#0A66C2" />
              <path
                fill="#fff"
                d="M7.1 9.5h-2.6V19h2.6V9.5zM5.8 5a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3zM19.5 13.6c0-2.6-1.4-4.3-3.7-4.3-1.3 0-2.2.7-2.6 1.4V9.5h-2.5V19h2.6v-5c0-1.3.6-2.2 1.7-2.2 1.1 0 1.8.8 1.8 2.2v5h2.7v-5.4z"
              />
            </svg>
            {{ tv.linkedin }}
          </button>
        </div>
        <LegalConsentNotice ref="legalConsentNotice" class="mt-3" />

        <div class="flex items-center gap-3 my-5 text-caption text-ink-400">
          <span class="flex-1 h-px bg-hairline"></span>{{ tv.orEmail }}<span class="flex-1 h-px bg-hairline"></span>
        </div>

        <form class="flex flex-col gap-5" @submit.prevent="view === 'login' ? submitLogin() : submitRegister()">
          <div v-if="view === 'register'">
            <label :for="ids.name" :class="labelCls">{{ tv.fullName }}</label>
            <input
              :id="ids.name"
              v-model="fullName"
              type="text"
              autocomplete="name"
              :placeholder="tv.fullNamePlaceholder"
              :class="inputCls"
            />
          </div>
          <div>
            <label :for="ids.email" :class="labelCls">{{ tv.email }}</label>
            <input
              :id="ids.email"
              v-model="email"
              type="email"
              autocomplete="email"
              :placeholder="tv.emailPlaceholder"
              :class="inputCls"
            />
          </div>
          <div>
            <label :for="ids.password" :class="labelCls">{{ tv.password }}</label>
            <input
              :id="ids.password"
              v-model="password"
              type="password"
              :autocomplete="view === 'login' ? 'current-password' : 'new-password'"
              :placeholder="view === 'login' ? tv.passwordPlaceholder : tv.newPasswordPlaceholder"
              :class="inputCls"
            />
            <template v-if="view === 'register'">
              <div class="flex gap-1 mt-2.5">
                <span
                  v-for="i in 4"
                  :key="i"
                  class="flex-1 h-1 rounded-full transition-colors"
                  :class="i <= strength ? strengthColor : 'bg-hairline'"
                ></span>
              </div>
              <p class="text-caption-sm text-ink-400 mt-1.5 mb-0">
                {{ password ? tv.strength[strength] : tv.passwordHint }}
              </p>
            </template>
          </div>

          <LegalAcceptance v-if="view === 'register'" ref="legalAcceptance" v-model="agreeTerms" />

          <p v-if="error" class="text-caption leading-normal text-[#B42318] m-0">{{ error }}</p>

          <button type="submit" :class="primaryBtn" :disabled="busy">
            <span
              v-if="busy"
              class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
            ></span>
            {{ view === 'login' ? tv.loginCta : tv.registerCta }}
          </button>
        </form>
      </template>
    </div>
  </BriefModal>
</template>

<script setup>
import { ref, computed, watch, useId } from 'vue'
import { useRouter } from 'vue-router'
import { X, Check, Mail } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useAuthStore } from '@/stores/auth'
import { useProjectBrief } from '@/composables/useProjectBrief'
import LegalAcceptance from '@/components/legal/LegalAcceptance.vue'
import LegalConsentNotice from '@/components/legal/LegalConsentNotice.vue'
import BriefModal from './BriefModal.vue'

const props = defineProps({ open: { type: Boolean, default: false } })
defineEmits(['close'])

const { t } = useI18n()
const tv = computed(() => t.value.brief.auth)
const router = useRouter()
const authStore = useAuthStore()
const { storedToken, pendingBriefPath } = useProjectBrief()

const PASSWORD_POLICY = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_-]).{8,}$/

const ids = { name: useId(), email: useId(), password: useId() }
const labelCls = 'block text-body font-medium text-ink-700 mb-2.5'
const inputCls =
  'w-full box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors'
const primaryBtn =
  'w-full h-[52px] flex items-center justify-center gap-3 rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed'
const socialBtn =
  'w-full h-[52px] flex items-center justify-center gap-3 rounded-full border border-hairline bg-white text-ink-900 text-[15px] font-bold hover:border-ink-900 transition-colors'

const view = ref('login')
const fullName = ref('')
const email = ref('')
const password = ref('')
const agreeTerms = ref(false)
const error = ref('')
const busy = ref(false)
const legalAcceptance = ref(null)
const legalConsentNotice = ref(null)

const tabs = computed(() => [
  { id: 'login', label: tv.value.tabLogin },
  { id: 'register', label: tv.value.tabRegister }
])

const strength = computed(() => {
  const pw = password.value
  return [pw.length >= 8, /[A-Z]/.test(pw) && /[a-z]/.test(pw), /\d/.test(pw), /[^A-Za-z0-9]/.test(pw)].filter(Boolean)
    .length
})
const strengthColor = computed(() =>
  strength.value <= 1 ? 'bg-[#B42318]' : strength.value <= 2 ? 'bg-brand-gold' : 'bg-ink-900'
)

const emailValid = computed(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim()))

watch([fullName, email, password, agreeTerms], () => {
  error.value = ''
})

watch(
  () => props.open,
  isOpen => {
    if (isOpen && view.value === 'verify') view.value = 'login'
  }
)

const switchTo = next => {
  view.value = next
  error.value = ''
}

const dashboardFor = user => {
  const roles = user?.registeredRoles || []
  if (roles.includes('CLIENT')) return '/client/dashboard'
  if (roles.includes('ARCHITECT')) return '/architect/dashboard'
  return '/'
}

const submitLogin = async () => {
  if (!emailValid.value) return (error.value = tv.value.errEmail)
  if (!password.value) return (error.value = tv.value.errPassword)

  busy.value = true
  try {
    const result = await authStore.login({ email: email.value.trim(), password: password.value })
    if (result?.success) router.push(pendingBriefPath(result.user) || dashboardFor(result.user))
  } catch (err) {
    error.value = err.response?.data?.message || err.message || tv.value.errLogin
  } finally {
    busy.value = false
  }
}

const submitRegister = async () => {
  const name = fullName.value.trim()
  if (!name) return (error.value = tv.value.errName)
  if (!emailValid.value) return (error.value = tv.value.errEmail)
  if (!PASSWORD_POLICY.test(password.value)) return (error.value = tv.value.errWeakPassword)
  if (!agreeTerms.value) return (error.value = tv.value.errTerms)

  const [firstName, ...rest] = name.split(/\s+/)
  busy.value = true
  try {
    await authStore.register({
      firstName,
      lastName: rest.join(' '),
      email: email.value.trim(),
      password: password.value,
      role: 'CLIENT',
      acceptances: legalAcceptance.value?.acceptances || [],
      // The verification email cannot carry the brief, so bind it to the account now
      landingBriefToken: storedToken()
    })
    view.value = 'verify'
  } catch (err) {
    error.value = err.response?.data?.message || tv.value.errRegister
  } finally {
    busy.value = false
  }
}

const social = async provider => {
  try {
    const acceptances = legalConsentNotice.value?.acceptances || []
    if (provider === 'google') await authStore.loginWithGoogle('CLIENT', acceptances)
    else await authStore.loginWithLinkedIn('CLIENT', acceptances)
  } catch {
    error.value = tv.value.errSocial
  }
}
</script>
