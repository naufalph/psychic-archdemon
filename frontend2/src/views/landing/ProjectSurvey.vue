<template>
  <div class="min-h-screen bg-white text-ink-900">
    <DashboardTopbar>
      <AccountMenu show-dashboard @dashboard="openDashboard" @logout="authStore.logout()" />
    </DashboardTopbar>

    <main class="px-6 pt-[clamp(32px,5vw,64px)] pb-[clamp(56px,7vw,96px)] box-border">
      <div class="max-w-[880px] mx-auto">
        <div v-if="key !== 'done' && saveStatus" class="flex justify-end mb-3 -mt-2" aria-live="polite">
          <span class="inline-flex items-center gap-1.5 text-caption-sm font-medium" :class="saveStatus.cls">
            <component
              :is="saveStatus.icon"
              :size="14"
              :stroke-width="2.4"
              :class="saveStatus.spin ? 'animate-spin' : ''"
            />
            {{ saveStatus.label }}
          </span>
        </div>
        <div
          v-if="key !== 'done'"
          class="h-1 rounded-full bg-hairline overflow-hidden mb-[clamp(32px,5vw,56px)]"
          role="progressbar"
          :aria-valuenow="survey.state.step + 1"
          aria-valuemin="1"
          :aria-valuemax="totalSteps"
          :aria-label="`Langkah ${survey.state.step + 1} dari ${totalSteps}`"
        >
          <div
            class="h-full bg-ink-900 rounded-full transition-[width] duration-[400ms] ease-[cubic-bezier(.2,.7,.2,1)]"
            :style="{ width: progress }"
          ></div>
        </div>

        <div v-if="key !== 'done'" class="mb-8 max-w-[680px]">
          <h1 class="text-[clamp(30px,4vw,44px)] font-bold tracking-[-.035em] leading-[1.1] m-0 mb-3">
            {{ page.title }}
          </h1>
          <p v-if="page.sub" class="text-[17px] leading-relaxed text-ink-400 m-0">{{ page.sub }}</p>
        </div>

        <SurveyLineup v-if="key === 'lineup'" @notify="notifyService = $event" />
        <SurveyScope v-else-if="key === 'scope'" />
        <SurveyProperty v-else-if="key === 'property'" />
        <SurveyAccount v-else-if="key === 'akun'" @login="authOpen = true" />
        <SurveyLocation v-else-if="key === 'alamat'" />
        <SurveyArea v-else-if="key === 'luas'" />
        <SurveyNeeds v-else-if="key === 'kebutuhan'" />
        <SurveyDeliverables v-else-if="key === 'deliv'" />
        <SurveyFee v-else-if="key === 'fee'" />
        <SurveyStart v-else-if="key === 'mulai'" />
        <SurveyReview v-else-if="key === 'review'" :needs-phone="needsPhone" :phone-error="phoneError" />
        <SurveyDone v-else-if="key === 'done'" />

        <p v-if="submitError" class="mt-6 mb-0 text-[15px] leading-normal text-[#B42318]">{{ submitError }}</p>

        <div
          v-if="key !== 'done'"
          class="flex items-center justify-between gap-4 flex-wrap mt-10 pt-6 border-t border-hairline"
        >
          <div class="flex">
            <div
              v-if="key === 'lineup'"
              class="flex items-baseline flex-wrap gap-1.5 text-[15px] leading-normal text-ink-400"
            >
              <span>Anda penyedia layanan?</span>
              <router-link
                to="/signup?role=ARCHITECT"
                class="font-semibold text-ink-900 border-b border-hairline hover:border-ink-900"
              >
                Gabung di sini
              </router-link>
            </div>
            <button
              v-else
              type="button"
              class="h-[52px] inline-flex items-center gap-2 pl-3.5 pr-5 rounded-full text-body font-bold text-ink-900 hover:bg-surface-alt transition-colors"
              @click="survey.go(survey.state.step - 1)"
            >
              <ChevronLeft :size="18" :stroke-width="2.2" /> Kembali
            </button>
          </div>

          <div class="flex items-center gap-4 flex-wrap ml-auto">
            <span v-if="needHint" class="text-caption text-ink-400">{{ needHint }}</span>
            <button
              v-if="showNext"
              type="button"
              class="h-[52px] px-8 rounded-full bg-ink-900 text-white text-[17px] font-bold inline-flex items-center gap-3 transition-opacity"
              :class="canContinue && !submitting ? 'hover:opacity-85' : 'opacity-35 cursor-not-allowed'"
              :disabled="!canContinue || submitting"
              @click="goNext"
            >
              <span
                v-if="submitting"
                class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
              ></span>
              {{ key === 'review' ? 'Kirim untuk verifikasi' : 'Lanjutkan' }}
            </button>
          </div>
        </div>
      </div>
    </main>

    <BriefAuthModal :open="authOpen" @close="authOpen = false" @authenticated="onAuthenticated" />
    <SurveyNotifyModal :service="notifyService" @close="notifyService = null" />
    <SurveyLeaveModal :open="leaveOpen" :save-failed="saveFailed" @stay="leaveOpen = false" @leave="goToDashboard" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronLeft, Check, CloudOff, Loader2 } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useProjectsStore } from '@/stores/projects'
import { useClientProfileStore } from '@/stores/clientProfile'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { useProjectBrief } from '@/composables/useProjectBrief'
import { landingAPI } from '@/services/api'
import { isValidCategory } from '@/constants/projectTaxonomy'
import {
  SURVEY_FLOW,
  SURVEY_PAGES,
  SURVEY_INTERIOR_PAGES,
  SURVEY_NEED_HINTS,
  groupForCategory
} from '@/constants/projectSurvey'
import BriefAuthModal from '@/components/brief/BriefAuthModal.vue'
import SurveyLineup from '@/components/survey/SurveyLineup.vue'
import SurveyScope from '@/components/survey/SurveyScope.vue'
import SurveyProperty from '@/components/survey/SurveyProperty.vue'
import SurveyAccount from '@/components/survey/SurveyAccount.vue'
import SurveyLocation from '@/components/survey/SurveyLocation.vue'
import SurveyArea from '@/components/survey/SurveyArea.vue'
import SurveyNeeds from '@/components/survey/SurveyNeeds.vue'
import SurveyDeliverables from '@/components/survey/SurveyDeliverables.vue'
import SurveyFee from '@/components/survey/SurveyFee.vue'
import SurveyStart from '@/components/survey/SurveyStart.vue'
import SurveyReview from '@/components/survey/SurveyReview.vue'
import SurveyDone from '@/components/survey/SurveyDone.vue'
import SurveyNotifyModal from '@/components/survey/SurveyNotifyModal.vue'
import DashboardTopbar from '@/components/layout/DashboardTopbar.vue'
import AccountMenu from '@/components/layout/AccountMenu.vue'
import SurveyLeaveModal from '@/components/survey/SurveyLeaveModal.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const projectsStore = useProjectsStore()
const clientProfileStore = useClientProfileStore()
const survey = useProjectSurvey()
const { saveToken, storedToken, clearToken } = useProjectBrief()

const authOpen = ref(false)
const notifyService = ref(null)
const leaveOpen = ref(false)
const submitting = ref(false)
const submitError = ref('')
const phoneError = ref('')
const saving = ref(false)
const saveFailed = ref(false)
// The finished screen is not persisted: a reload after submitting starts a fresh brief
const finished = ref(false)

const AKUN_STEP = SURVEY_FLOW.indexOf('akun')
const totalSteps = SURVEY_FLOW.length - 1

const key = computed(() => (finished.value ? 'done' : survey.stepKey.value))

const page = computed(() => (survey.interiorOnly.value && SURVEY_INTERIOR_PAGES[key.value]) || SURVEY_PAGES[key.value])

const progress = computed(() => `${Math.round(((survey.state.step + 1) / totalSteps) * 100)}%`)

const signedInClient = computed(() => authStore.isAuthenticated && authStore.isClient)
const needsPhone = computed(() => signedInClient.value && !clientProfileStore.profilePhone)

const PHONE_PATTERN = /^\+?[0-9\s-]{10,16}$/

const canContinue = computed(() => {
  if (key.value === 'review') return !needsPhone.value || PHONE_PATTERN.test(survey.state.phone.trim())
  return !!survey.valid.value[key.value]
})

const showNext = computed(() => key.value !== 'akun' || signedInClient.value)

const needHint = computed(() => {
  if (canContinue.value || key.value === 'akun') return ''
  if (key.value === 'scope' && survey.scopeLabels.value.length) return SURVEY_NEED_HINTS.scopeInterior
  return SURVEY_NEED_HINTS[key.value] || ''
})

// Saves the brief as a DRAFT project in the account. Saves run one at a time so two quick
// steps cannot both create a draft before the first one has returned its id.
let saveChain = Promise.resolve()

const persistDraft = async () => {
  if (!signedInClient.value || finished.value) return
  const data = survey.buildProjectData()
  if (survey.state.draftId) {
    try {
      await projectsStore.updateDraftProject(survey.state.draftId, data)
      return
    } catch (err) {
      // The draft was submitted, deleted, or belongs to another account: start a new one
      if (![400, 403, 404, 409].includes(err.response?.status)) throw err
      survey.state.draftId = null
    }
  }
  const draft = await projectsStore.createDraftProject(data)
  survey.state.draftId = draft.id
}

const queueSave = () => {
  const run = saveChain.then(async () => {
    saving.value = true
    try {
      await persistDraft()
      saveFailed.value = false
    } catch (err) {
      saveFailed.value = true
      throw err
    } finally {
      saving.value = false
    }
  })
  saveChain = run.catch(() => {})
  return run
}

// Background saves never block the visitor; a failure only shows in the status line
const autosave = () => queueSave().catch(() => {})

const saveStatus = computed(() => {
  if (!signedInClient.value || survey.state.step < AKUN_STEP) return null
  if (saving.value) return { label: 'Menyimpan…', icon: Loader2, spin: true, cls: 'text-ink-400' }
  if (saveFailed.value) return { label: 'Belum tersimpan', icon: CloudOff, cls: 'text-[#B42318]' }
  if (survey.state.draftId) return { label: 'Tersimpan di akun Anda', icon: Check, cls: 'text-ink-400' }
  return null
})

const submit = async () => {
  submitting.value = true
  submitError.value = ''
  phoneError.value = ''
  try {
    if (needsPhone.value) {
      try {
        await clientProfileStore.updateProfile({ phoneNumber: survey.state.phone.trim() })
      } catch (err) {
        phoneError.value = err.response?.data?.message || 'Nomor telepon tidak dapat disimpan. Periksa kembali.'
        return
      }
    }

    await queueSave()
    await projectsStore.submitProject(survey.state.draftId, [...survey.refs])
    survey.reset()
    finished.value = true
    window.scrollTo(0, 0)
  } catch (err) {
    submitError.value =
      err.response?.data?.message?.replace(/^PROFILE_INCOMPLETE:\s*/, '') ||
      'Brief belum terkirim. Periksa koneksi Anda lalu coba lagi.'
  } finally {
    submitting.value = false
  }
}

const goNext = () => {
  if (!canContinue.value || submitting.value) return
  if (key.value === 'review') {
    submit()
    return
  }
  if (survey.state.step >= AKUN_STEP) autosave()
  survey.go(survey.state.step + 1)
}

const dashboardPath = () => (authStore.isClient ? '/client/dashboard' : '/architect/dashboard')

const goToDashboard = async () => {
  leaveOpen.value = false
  // Leaving is when the latest answers matter most, so the save finishes before navigating
  if (!finished.value && survey.state.step >= AKUN_STEP) await autosave()
  router.push(dashboardPath())
}

// A sent brief has nothing left to lose, so only an unfinished one asks before leaving
const openDashboard = () => {
  if (finished.value) goToDashboard()
  else leaveOpen.value = true
}

// A visitor who reaches the account step signed out leaves a server-side record of their
// answers, so briefs abandoned before sign-in still count toward traction. Best effort only.
const recordAnonymousBrief = async () => {
  if (storedToken()) return
  try {
    const res = await landingAPI.createBrief(survey.briefSnapshot())
    saveToken(res.data?.data?.claimToken)
  } catch {
    // Rate limit or network: the survey carries on without the record
  }
}

watch(
  () => key.value === 'akun' && !signedInClient.value,
  atAccountSignedOut => {
    if (atAccountSignedOut) recordAnonymousBrief()
  },
  { immediate: true }
)

// Sign-up already links the brief to the new account; a login or OAuth return claims it here
const claimPendingBrief = async () => {
  const token = storedToken()
  if (!token || !signedInClient.value) return
  try {
    await landingAPI.claimBrief(token)
    clearToken()
  } catch (err) {
    // A missing or someone else's brief can never be claimed, so stop retrying it
    if ([400, 404, 409].includes(err.response?.status)) clearToken()
  }
}

const onAuthenticated = async () => {
  authOpen.value = false
  if (!authStore.isClient) return
  claimPendingBrief()
  if (await openDraftFromRoute()) return
  survey.goTo('alamat')
  autosave()
}

// Steps after "akun" assume a signed-in client; a stale session or a logout sends the visitor
// back to the account step instead of letting them fill in a brief they cannot submit
watch(
  () => [survey.state.step, signedInClient.value],
  ([step, signedIn]) => {
    if (!finished.value && step > AKUN_STEP && !signedIn) survey.go(AKUN_STEP)
  },
  { immediate: true }
)

watch(
  () => key.value === 'review' && signedInClient.value,
  onReview => {
    if (onReview) clientProfileStore.fetchProfile().catch(() => {})
  },
  { immediate: true }
)

const localized = (preset, field) => preset[`${field}Id`] || preset[`${field}En`] || ''

// A landing "Tipe Proyek" card opens this page with ?preset=, carrying the superuser-authored
// defaults for that kind of project
const applyPreset = async slug => {
  let preset
  try {
    const res = await landingAPI.getPresets()
    preset = (res.data?.data || []).find(p => p.slug === slug)
  } catch {
    return
  }
  if (!preset) return
  const state = survey.state
  state.presetSlug = preset.slug
  if (preset.projectScope) state.scopes = { [preset.projectScope]: true }
  if (isValidCategory(preset.buildingFunction)) {
    state.groupId = groupForCategory(preset.buildingFunction)?.id || ''
    state.category = preset.buildingFunction
    state.subCategory = preset.subCategory || ''
  }
  state.presetTitle = localized(preset, 'defaultTitle')
  state.vision = localized(preset, 'defaultDescription')
  if (preset.defaultLotSize) state.land = String(preset.defaultLotSize)
  if (preset.defaultDesignBudget) state.designFee = Number(preset.defaultDesignBudget).toLocaleString('id-ID')
}

// Opens /brief-proyek?draft=<id> from the dashboard. This browser's own copy wins when it is
// the same draft, since it also holds answers the project has no column for.
const openDraftFromRoute = async () => {
  const id = Number(route.query.draft)
  if (!id || !signedInClient.value) return false
  router.replace({ query: {} })
  if (survey.state.draftId === id) return true
  try {
    const project = await projectsStore.fetchProjectById(id)
    if (project.status !== 'DRAFT') {
      router.replace({ name: 'ProjectDetail', params: { id } })
      return true
    }
    survey.hydrateFromProject(project)
  } catch {
    submitError.value = 'Draft tidak dapat dibuka. Anda bisa melanjutkan brief di bawah ini.'
  }
  return true
}

onMounted(async () => {
  claimPendingBrief()
  // "Proyek baru" from the dashboard starts a clean brief; an unfinished one is already a
  // draft in the account, so clearing this browser's copy loses nothing
  if (route.query.baru) {
    survey.reset()
    router.replace({ query: {} })
    return
  }
  if (await openDraftFromRoute()) return
  // A signed-in client past the account step should always have the draft in their account
  if (signedInClient.value && survey.state.step > AKUN_STEP && !survey.state.draftId) autosave()
  // Landing links only seed a fresh survey; they never overwrite answers already in progress
  if (survey.state.step > 0) return
  const preset = route.query.preset
  if (typeof preset === 'string' && preset) {
    survey.reset()
    await applyPreset(preset)
    return
  }
  const category = String(route.query.kategori || '').toUpperCase()
  if (isValidCategory(category)) {
    survey.state.groupId = groupForCategory(category)?.id || ''
    survey.state.category = ''
    survey.state.subCategory = ''
  }
})
</script>
