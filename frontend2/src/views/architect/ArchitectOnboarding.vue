<template>
  <div class="min-h-screen bg-white text-ink-900">
    <DashboardTopbar>
      <AccountMenu role-label="arsitek" @logout="logout" />
    </DashboardTopbar>

    <main class="px-6 pt-[clamp(32px,5vw,64px)] pb-[clamp(56px,7vw,96px)] box-border">
      <div v-if="key === 'welcome'" class="max-w-[1232px] mx-auto">
        <OnbWelcome @next="go(1)" />
      </div>
      <div v-else-if="finished" class="max-w-[880px] mx-auto">
        <OnbDone />
      </div>
      <div v-else class="max-w-[880px] mx-auto">
        <OnbProgress :current="progressIndex" class="mb-[clamp(32px,5vw,56px)]" />
        <OnbProfesi v-if="key === 'profesi'" @back="go(-1)" @next="go(1)" />
        <OnbJenis v-else-if="key === 'jenis'" @back="go(-1)" @next="go(1)" />
        <OnbDataDiri v-else-if="key === 'dataDiri'" @back="go(-1)" @next="go(1)" @edit-jenis="goTo('jenis')" />
        <OnbKeahlian v-else-if="key === 'keahlian'" :saving="saving" :error="saveError" @back="go(-1)" @next="finish" />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DashboardTopbar from '@/components/layout/DashboardTopbar.vue'
import AccountMenu from '@/components/layout/AccountMenu.vue'
import OnbProgress from '@/components/onboarding/OnbProgress.vue'
import OnbWelcome from '@/components/onboarding/OnbWelcome.vue'
import OnbProfesi from '@/components/onboarding/OnbProfesi.vue'
import OnbJenis from '@/components/onboarding/OnbJenis.vue'
import OnbDataDiri from '@/components/onboarding/OnbDataDiri.vue'
import OnbKeahlian from '@/components/onboarding/OnbKeahlian.vue'
import OnbDone from '@/components/onboarding/OnbDone.vue'
import { ONBOARDING_FLOW } from '@/constants/architectOnboarding'
import { useArchitectOnboarding } from '@/composables/useArchitectOnboarding'
import { useAuthStore } from '@/stores/auth'
import { architectAPI } from '@/services/api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { state, profilePayload } = useArchitectOnboarding()

// The step lives in ?langkah= so the browser back button walks the flow
const index = computed(() => {
  const n = Number(route.query.langkah)
  return Number.isInteger(n) && n >= 0 && n < ONBOARDING_FLOW.length ? n : 0
})
const key = computed(() => ONBOARDING_FLOW[index.value])
const progressIndex = computed(() => index.value - 1)

const firstIncomplete = () => {
  if (!state.profesi.length) return ONBOARDING_FLOW.indexOf('profesi')
  if (!state.jenis) return ONBOARDING_FLOW.indexOf('jenis')
  return null
}

const goTo = name => router.push({ query: { ...route.query, langkah: ONBOARDING_FLOW.indexOf(name) } })
const go = delta => {
  if (delta < 0 && index.value === 0) return router.push('/')
  router.push({ query: { ...route.query, langkah: Math.max(0, index.value + delta) } })
}

const finished = ref(false)
const saving = ref(false)
const saveError = ref('')
const finish = async () => {
  saving.value = true
  saveError.value = ''
  try {
    await architectAPI.updateFullProfile(profilePayload())
    finished.value = true
    window.scrollTo({ top: 0 })
  } catch (error) {
    saveError.value = error.response?.data?.message || 'Profil belum tersimpan. Coba lagi sebentar lagi.'
  } finally {
    saving.value = false
  }
}

const logout = async () => {
  await authStore.logout()
  router.push('/')
}

onMounted(async () => {
  // A deep link past an unanswered step goes back to that step instead of rendering half-empty copy
  const missing = firstIncomplete()
  if (missing !== null && index.value > missing) router.replace({ query: { ...route.query, langkah: missing } })

  if (!state.nama && authStore.user?.firstName) {
    state.nama = [authStore.user.firstName, authStore.user.lastName].filter(Boolean).join(' ')
  }
})
</script>
