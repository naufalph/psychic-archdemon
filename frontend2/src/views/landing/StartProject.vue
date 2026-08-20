<template>
  <div class="min-h-screen bg-white overflow-x-hidden flex flex-col">
    <Navbar />

    <!-- Header -->
    <section class="max-w-[820px] mx-auto px-10 pt-16 pb-8 text-center">
      <h1 class="text-section-h1 text-ink-900 mb-3">{{ t.mulaiProyek.title }}</h1>
      <p class="text-body text-ink-500 mb-5">{{ t.mulaiProyek.subline }}</p>
      <button
        type="button"
        class="inline-flex items-center gap-2 bg-white text-ink-900 border-2 border-ink-200 rounded-full px-6 py-3 text-caption font-bold cursor-pointer hover:border-ink-900 transition-colors"
        @click="formMode = !formMode"
      >
        <PencilLine class="w-4 h-4" />
        {{ formMode ? t.mulaiProyek.toggleToBrowse : t.mulaiProyek.toggleToForm }}
      </button>
    </section>

    <section class="max-w-[1440px] mx-auto px-10 pb-24 w-full">
      <!-- Search + chips (shared between modes, matches Home) -->
      <div class="mb-5">
        <SearchInput v-model="catalog.search" :placeholder="t.landing.jelajahi.searchPlaceholder" />
      </div>
      <div class="flex justify-center gap-2.5 flex-wrap mb-8">
        <CategoryChip
          v-for="cat in CATEGORIES"
          :key="cat.value"
          :label="t.landing.jelajahi[cat.labelKey]"
          :active="catalog.category === cat.value"
          @click="catalog.setCategory(cat.value)"
        />
      </div>

      <!-- Form mode -->
      <div
        v-if="formMode"
        class="max-w-[640px] mx-auto mb-20 bg-white border border-hairline rounded-card p-10 shadow-soft"
      >
        <h3 class="text-h3 text-ink-900 mb-5">{{ t.mulaiProyek.form.heading }}</h3>
        <div class="flex flex-col gap-5">
          <div>
            <label class="block text-micro font-bold uppercase tracking-[0.06em] text-ink-500 mb-2">
              {{ t.mulaiProyek.form.projectScope }}
            </label>
            <div class="flex flex-wrap gap-2">
              <CategoryChip
                v-for="scope in PROJECT_SCOPES"
                :key="scope.value"
                :label="localeLabel(scope)"
                :active="form.projectScope === scope.value"
                @click="form.projectScope = scope.value"
              />
            </div>
          </div>
          <div>
            <label class="block text-micro font-bold uppercase tracking-[0.06em] text-ink-500 mb-2">
              {{ t.mulaiProyek.form.projectType }}
            </label>
            <div class="flex flex-wrap gap-2">
              <CategoryChip
                v-for="cat in PROJECT_CATEGORIES"
                :key="cat.value"
                :label="localeLabel(cat)"
                :active="form.category === cat.value"
                @click="selectCategory(cat.value)"
              />
            </div>
          </div>
          <BaseInput
            v-model="form.title"
            :label="t.mulaiProyek.form.title"
            :placeholder="t.mulaiProyek.form.titlePlaceholder"
          />

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <BaseInput
              v-model="form.location"
              :label="t.mulaiProyek.form.location"
              :placeholder="t.mulaiProyek.form.locationPlaceholder"
            />
            <div class="w-full">
              <label class="block text-sm font-medium text-gray-700 mb-1 ml-1">
                {{ t.mulaiProyek.form.lotSize }}
              </label>
              <input
                v-model="lotSizeInput"
                type="number"
                min="1"
                inputmode="numeric"
                :placeholder="t.mulaiProyek.form.lotSizePlaceholder"
                class="w-full px-4 py-3 rounded-2xl border-2 border-gray-200 bg-white text-gray-900 placeholder-gray-400 focus:outline-none focus:border-brand-green transition-all duration-200"
              />
            </div>
          </div>
          <div>
            <BaseInput
              v-model="budgetInput"
              :label="t.mulaiProyek.form.budget"
              :placeholder="t.mulaiProyek.form.budgetPlaceholder"
            />
            <p class="text-micro-sm text-ink-400 mt-2">{{ t.mulaiProyek.form.budgetHint }}</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1 ml-1">
              {{ t.mulaiProyek.form.description }}
            </label>
            <textarea
              v-model="form.description"
              rows="4"
              :placeholder="t.mulaiProyek.form.descriptionPlaceholder"
              class="w-full px-4 py-3 rounded-2xl border-2 border-gray-200 bg-white text-gray-900 placeholder-gray-400 focus:outline-none focus:border-brand-green transition-all duration-200"
            />
          </div>
          <BaseInput
            v-model="form.phoneNumber"
            :label="t.mulaiProyek.form.phone"
            :placeholder="t.mulaiProyek.form.phonePlaceholder"
            :error="phoneError"
            type="tel"
          />

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              {{ t.mulaiProyek.form.startDate }}
            </label>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <label
                v-for="option in START_DATE_OPTIONS"
                :key="option.value"
                :class="[
                  'flex items-center gap-3 p-4 rounded-2xl border-2 cursor-pointer transition',
                  form.startDateType === option.value
                    ? 'border-ink-900 bg-surface-muted'
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <input v-model="form.startDateType" type="radio" :value="option.value" class="hidden" />
                <span
                  :class="[
                    'w-4 h-4 rounded-full border-2 flex items-center justify-center flex-shrink-0',
                    form.startDateType === option.value ? 'border-ink-900' : 'border-gray-300'
                  ]"
                >
                  <span v-if="form.startDateType === option.value" class="w-2 h-2 rounded-full bg-ink-900" />
                </span>
                <span class="text-body-sm font-medium text-ink-900">
                  {{ t.mulaiProyek.form[option.labelKey] }}
                </span>
              </label>
            </div>
            <input
              v-if="form.startDateType === 'SPECIFIC_DATE'"
              v-model="form.expectedStartDate"
              type="date"
              :min="minStartDate"
              class="mt-3 w-full px-4 py-3 rounded-2xl border-2 border-gray-200 bg-white text-gray-900 focus:outline-none focus:border-brand-green transition-all duration-200"
            />
          </div>

          <BaseAlert v-if="submitError" variant="error">{{ submitError }}</BaseAlert>

          <BaseButton full-width :disabled="submitting" @click="submitForm">
            {{
              submitting
                ? t.mulaiProyek.form.submitting
                : isReturningClient
                  ? t.mulaiProyek.form.submitAuthed
                  : t.mulaiProyek.form.submit
            }}
          </BaseButton>
          <p v-if="!isReturningClient" class="text-center text-micro-sm text-ink-400">
            {{ t.mulaiProyek.form.microcopy }}
          </p>
        </div>
      </div>

      <!-- Browse mode -->
      <template v-else>
        <div v-if="isLoading" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="i in 8"
            :key="i"
            class="rounded-card overflow-hidden border border-hairline bg-white animate-pulse"
          >
            <div class="bg-gray-200" style="aspect-ratio: 1/1"></div>
            <div class="p-3 space-y-2">
              <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              <div class="h-3 bg-gray-200 rounded w-3/4"></div>
            </div>
          </div>
        </div>
        <div v-else-if="filteredProjects.length === 0" class="py-20 text-center">
          <p class="text-body-sm text-ink-300">{{ t.landing.jelajahi.emptyState }}</p>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4">
          <ImageCard v-for="project in filteredProjects" :key="project.id" :project="project" />
          <AddDesignTile :label="t.landing.jelajahi.addDesign" @click="formMode = true" />
        </div>
      </template>
    </section>

    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { PencilLine } from 'lucide-vue-next'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseAlert from '@/components/ui/BaseAlert.vue'
import SearchInput from '@/components/landing/SearchInput.vue'
import CategoryChip from '@/components/landing/CategoryChip.vue'
import ImageCard from '@/components/landing/ImageCard.vue'
import AddDesignTile from '@/components/landing/AddDesignTile.vue'
import { useI18n } from '@/composables/useI18n'
import { useProjectBrief } from '@/composables/useProjectBrief'
import { projectAPI, landingAPI } from '@/services/api'
import { useCatalogStore } from '@/stores/catalog'
import { useAuthStore } from '@/stores/auth'
import { CATEGORIES, filterProjects } from '@/utils/catalogFormat'
import { PROJECT_SCOPES, PROJECT_CATEGORIES, LEGACY_TYPE_MAP, isValidSubCategory } from '@/constants/projectTaxonomy'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const catalog = useCatalogStore()
const authStore = useAuthStore()
const { saveToken } = useProjectBrief()

const formMode = ref(false)
const projects = ref([])
const isLoading = ref(true)

const START_DATE_OPTIONS = [
  { value: 'IMMEDIATELY', labelKey: 'startImmediately' },
  { value: 'SPECIFIC_DATE', labelKey: 'startSpecificDate' }
]

const BUDGET_RANGE_PERCENT = 25
const PHONE_PATTERN = /^\+?[0-9\s-]{10,16}$/

const presets = ref([])
const activePreset = ref(null)
const submitting = ref(false)
const submitError = ref('')
const phoneError = ref('')

const form = reactive({
  projectScope: 'NEW_BUILD',
  category: 'RESIDENTIAL',
  subCategory: null,
  title: '',
  location: '',
  lotSize: null,
  description: '',
  phoneNumber: '',
  startDateType: 'IMMEDIATELY',
  expectedStartDate: ''
})

const lotSizeInput = ref('')
const budgetInput = ref('')

const isReturningClient = computed(() => authStore.isAuthenticated && authStore.hasRole('CLIENT'))

const minStartDate = computed(() => new Date().toISOString().split('T')[0])

const parseDigits = value => parseInt(String(value).replace(/[^0-9]/g, ''), 10) || null

watch(budgetInput, value => {
  const parsed = parseDigits(value)
  const formatted = parsed ? parsed.toLocaleString('id-ID') : ''
  if (formatted !== value) budgetInput.value = formatted
})

const localeLabel = entry => (locale.value === 'id' ? entry.labelId : entry.labelEn)

const selectCategory = value => {
  form.category = value
  if (!isValidSubCategory(value, form.subCategory)) form.subCategory = null
}

const localized = (preset, field) =>
  (locale.value === 'en' ? preset[`${field}En`] : preset[`${field}Id`]) || preset[`${field}En`] || ''

const applyPreset = preset => {
  activePreset.value = preset
  form.projectScope = preset.projectScope || 'NEW_BUILD'
  form.category = preset.buildingFunction || 'RESIDENTIAL'
  // Carried silently: the mini-form never asks for a sub-category, but the preset knows one
  // and ProjectCreate should not make the user pick it again.
  form.subCategory = preset.subCategory || null
  form.title = localized(preset, 'defaultTitle')
  form.description = localized(preset, 'defaultDescription')
  lotSizeInput.value = preset.defaultLotSize != null ? String(preset.defaultLotSize) : ''
  budgetInput.value =
    preset.defaultDesignBudget != null ? Number(preset.defaultDesignBudget).toLocaleString('id-ID') : ''
}

const buildPayload = () => {
  const total = parseDigits(budgetInput.value)
  const spread = total ? Math.round((total * BUDGET_RANGE_PERCENT) / 100) : 0

  return {
    presetSlug: activePreset.value?.slug || null,
    buildingFunction: form.category,
    projectScope: form.projectScope,
    subCategory: form.subCategory,
    title: form.title.trim(),
    location: form.location.trim(),
    lotSize: parseDigits(lotSizeInput.value),
    description: form.description.trim(),
    phoneNumber: form.phoneNumber.trim(),
    designBudgetTotal: total,
    designBudgetMin: total ? total - spread : null,
    designBudgetMax: total ? total + spread : null,
    startDateType: form.startDateType,
    expectedStartDate: form.startDateType === 'SPECIFIC_DATE' && form.expectedStartDate ? form.expectedStartDate : null
  }
}

const validate = () => {
  phoneError.value = ''
  const phone = form.phoneNumber.trim()
  if (phone && !PHONE_PATTERN.test(phone)) {
    phoneError.value = t.value.mulaiProyek.form.phoneInvalid
    return false
  }
  return true
}

async function submitForm() {
  submitError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    const res = await landingAPI.createBrief(buildPayload())
    const claimToken = res.data?.data?.claimToken
    if (!claimToken) throw new Error('Missing claim token')

    saveToken(claimToken)

    // The token rides on the URL so it survives the email-verification round-trip,
    // including onto a different browser where localStorage would be empty.
    const target = `/client/projects/create?brief=${encodeURIComponent(claimToken)}`
    if (isReturningClient.value) {
      router.push(target)
    } else {
      // Anyone arriving from a project brief is a client, so skip the role-picker step
      router.push(`/signup?role=CLIENT&redirect=${encodeURIComponent(target)}`)
    }
  } catch (err) {
    submitError.value = err.response?.data?.message || t.value.mulaiProyek.form.submitError
  } finally {
    submitting.value = false
  }
}

const filteredProjects = computed(() =>
  filterProjects(projects.value, { search: catalog.search, category: catalog.category })
)

onMounted(async () => {
  const [previews, presetList] = await Promise.allSettled([projectAPI.getPublicPreviews(), landingAPI.getPresets()])

  if (previews.status === 'fulfilled') projects.value = previews.value.data?.data || []
  if (presetList.status === 'fulfilled') presets.value = presetList.value.data?.data || []
  isLoading.value = false

  const presetSlug = route.query.preset
  if (presetSlug) {
    const match = presets.value.find(p => p.slug === presetSlug)
    if (match) {
      applyPreset(match)
      formMode.value = true
      return
    }
  }

  // Legacy ?type= links (and existing e2e specs) predate the three-level taxonomy, so map
  // the old single value onto the scope/category/sub-category it corresponds to.
  const legacy = LEGACY_TYPE_MAP[route.query.type]
  if (legacy) {
    form.projectScope = legacy.projectScope
    form.category = legacy.category
    form.subCategory = legacy.subCategory
    formMode.value = true
  }
})
</script>
