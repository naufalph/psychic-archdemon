<template>
  <div class="min-h-screen bg-surface-alt py-12">
    <div class="max-w-4xl mx-auto px-6">
      <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden">
        <div class="bg-brand-brown p-8 text-white">
          <h1 class="text-3xl font-bold flex items-center gap-3">
            <Home :size="32" />
            {{ existingProjectId ? t.projectCreate.titleDraft : t.projectCreate.titleNew }}
          </h1>
          <p class="text-white/80 mt-2">{{ t.projectCreate.subtitle }}</p>
        </div>

        <form class="p-8 space-y-10" @submit.prevent="handleSubmit">
          <section v-if="showPhoneField" class="space-y-4 p-5 bg-amber-50 border border-amber-200 rounded-2xl">
            <div class="flex gap-3 items-start">
              <svg
                class="w-5 h-5 text-amber-500 flex-shrink-0 mt-0.5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <div>
                <p class="text-sm font-semibold text-amber-900">{{ t.projectCreate.phoneRequiredTitle }}</p>
                <p class="text-sm text-amber-800 mt-1">
                  {{ t.projectCreate.phoneRequiredBody }}
                </p>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >{{ t.projectCreate.phoneLabel }}<span class="text-red-500">*</span></label
              >
              <input
                v-model="formData.phoneNumber"
                type="tel"
                :placeholder="t.mulaiProyek.form.phonePlaceholder"
                class="w-full px-4 py-3 border-2 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none transition"
                :class="phoneError ? 'border-red-300' : 'border-gray-200'"
              />
              <p v-if="phoneError" class="mt-1 text-sm text-red-600">{{ phoneError }}</p>
            </div>
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm"
                >{{ t.projectCreate.part }} 0</span
              >
              <h2 class="text-xl font-bold text-black">{{ t.projectCreate.partImages }}</h2>
            </div>
            <p class="text-xs text-gray-500">
              {{ t.projectCreate.imagesHint }}
            </p>
            <MultiImageUploader
              v-model="coverImages"
              label=""
              :max-files="10"
              :existing-images="existingImages"
              @delete-existing="handleDeleteExistingImage"
            />
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm"
                >{{ t.projectCreate.part }} 1</span
              >
              <h2 class="text-xl font-bold text-black">{{ t.projectCreate.partGeneral }}</h2>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >{{ t.projectCreate.projectTitle }}<span class="text-red-500">*</span></label
              >
              <input
                v-model="formData.title"
                required
                type="text"
                :placeholder="t.projectCreate.projectTitlePlaceholder"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none transition"
              />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >{{ t.projectCreate.location }}<span class="text-red-500">*</span></label
                >
                <input
                  v-model="formData.location"
                  required
                  type="text"
                  :placeholder="t.projectCreate.locationPlaceholder"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >{{ t.projectCreate.lotSize }}<span class="text-red-500">*</span></label
                >
                <input
                  v-model.number="formData.lotSize"
                  required
                  type="number"
                  :placeholder="t.projectCreate.lotSizePlaceholder"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >{{ t.projectCreate.numberOfFloors }}<span class="text-red-500">*</span></label
                >
                <input
                  v-model.number="formData.numberOfFloors"
                  required
                  type="number"
                  min="1"
                  :placeholder="t.projectCreate.numberOfFloorsPlaceholder"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
                />
              </div>
            </div>

            <div class="grid md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >{{ t.projectCreate.projectScope }}<span class="text-red-500">*</span></label
                >
                <select
                  v-model="formData.projectScope"
                  required
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                >
                  <option :value="null" disabled>{{ t.projectCreate.selectScope }}</option>
                  <option v-for="scope in PROJECT_SCOPES" :key="scope.value" :value="scope.value">
                    {{ localeLabel(scope) }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >{{ t.projectCreate.category }}<span class="text-red-500">*</span></label
                >
                <select
                  v-model="formData.category"
                  required
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                  @change="onCategoryChange"
                >
                  <option :value="null" disabled>{{ t.projectCreate.selectCategory }}</option>
                  <option v-for="cat in PROJECT_CATEGORIES" :key="cat.value" :value="cat.value">
                    {{ localeLabel(cat) }}
                  </option>
                </select>
                <p v-if="categoryDescription" class="mt-1 text-sm text-gray-500">
                  {{ categoryDescription }}
                </p>
              </div>
            </div>

            <div v-if="availableSubCategories.length > 0">
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >{{ t.projectCreate.subCategory }}<span class="text-red-500">*</span></label
              >
              <select
                v-model="formData.subCategory"
                required
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
              >
                <option :value="null" disabled>{{ t.projectCreate.selectSubCategory }}</option>
                <option v-for="sub in availableSubCategories" :key="sub.value" :value="sub.value">
                  {{ localeLabel(sub) }}
                </option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >{{ t.projectCreate.detailedRequirements }}<span class="text-red-500">*</span></label
              >
              <textarea
                v-model="formData.description"
                required
                rows="4"
                :placeholder="t.projectCreate.detailedRequirementsPlaceholder"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
              />
            </div>
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm"
                >{{ t.projectCreate.part }} 2</span
              >
              <h2 class="text-xl font-bold text-black">{{ t.projectCreate.partDeliverables }}</h2>
            </div>

            <DeliverablesSelector v-model="formData.deliverables" />
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm"
                >{{ t.projectCreate.part }} 3</span
              >
              <h2 class="text-xl font-bold text-black">{{ t.projectCreate.partBudget }}</h2>
            </div>

            <div class="space-y-8">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">{{
                  t.projectCreate.constructionBudget
                }}</label>
                <p class="text-xs text-gray-500 mb-2">
                  {{ t.projectCreate.constructionBudgetHint }}
                </p>
                <div class="relative">
                  <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 font-medium">IDR</span>
                  <input
                    v-model="formData.constructionBudget"
                    type="text"
                    :placeholder="t.projectCreate.constructionBudgetPlaceholder"
                    class="w-full pl-16 pr-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none text-right font-medium"
                    @input="formatConstructionBudget"
                  />
                </div>
              </div>

              <BudgetRangeSlider
                v-model="formData.designBudget"
                :label="t.projectCreate.designBudgetLabel"
                :description="t.projectCreate.designBudgetDescription"
                :step="1000000"
                :required="true"
                :range-percent="25"
                :hint="t.projectCreate.designBudgetHint"
              />
            </div>
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm"
                >{{ t.projectCreate.part }} 4</span
              >
              <h2 class="text-xl font-bold text-black">{{ t.projectCreate.partStartDate }}</h2>
            </div>
            <p class="text-xs text-gray-500">{{ t.projectCreate.startDateHint }}</p>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <label
                :class="[
                  'flex items-center gap-4 p-5 rounded-2xl border-2 cursor-pointer transition',
                  formData.startDateType === 'IMMEDIATELY'
                    ? 'border-brand-brown bg-brand-tan/30'
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <input v-model="formData.startDateType" type="radio" value="IMMEDIATELY" class="hidden" />
                <div
                  :class="[
                    'w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0',
                    formData.startDateType === 'IMMEDIATELY' ? 'border-brand-brown' : 'border-gray-300'
                  ]"
                >
                  <div
                    v-if="formData.startDateType === 'IMMEDIATELY'"
                    class="w-2.5 h-2.5 rounded-full bg-brand-brown"
                  />
                </div>
                <div>
                  <p class="font-semibold text-gray-900">{{ t.projectCreate.immediately }}</p>
                  <p class="text-xs text-gray-500 mt-0.5">{{ t.projectCreate.immediatelyDesc }}</p>
                </div>
              </label>

              <label
                :class="[
                  'flex items-center gap-4 p-5 rounded-2xl border-2 cursor-pointer transition',
                  formData.startDateType === 'SPECIFIC_DATE'
                    ? 'border-brand-brown bg-brand-tan/30'
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <input v-model="formData.startDateType" type="radio" value="SPECIFIC_DATE" class="hidden" />
                <div
                  :class="[
                    'w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0',
                    formData.startDateType === 'SPECIFIC_DATE' ? 'border-brand-brown' : 'border-gray-300'
                  ]"
                >
                  <div
                    v-if="formData.startDateType === 'SPECIFIC_DATE'"
                    class="w-2.5 h-2.5 rounded-full bg-brand-brown"
                  />
                </div>
                <div>
                  <p class="font-semibold text-gray-900">{{ t.projectCreate.specificDate }}</p>
                  <p class="text-xs text-gray-500 mt-0.5">{{ t.projectCreate.specificDateDesc }}</p>
                </div>
              </label>
            </div>

            <div v-if="formData.startDateType === 'SPECIFIC_DATE'">
              <label class="block text-sm font-medium text-gray-700 mb-2">
                {{ t.projectCreate.targetStartDate }} <span class="text-red-500">*</span>
              </label>
              <input
                v-model="formData.expectedStartDate"
                type="date"
                :min="minStartDate"
                required
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none transition"
              />
            </div>
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm"
                >{{ t.projectCreate.part }} 5</span
              >
              <h2 class="text-xl font-bold text-black">{{ t.projectCreate.partDeadline }}</h2>
            </div>
            <p class="text-xs text-gray-500">{{ t.projectCreate.biddingDeadlineHint }}</p>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                {{ t.projectCreate.biddingClosesOn }} <span class="text-red-500">*</span>
              </label>
              <input
                v-model="formData.biddingDeadline"
                type="date"
                required
                :min="minBiddingDeadline"
                :max="maxBiddingDeadline"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none transition"
              />
              <p v-if="biddingDaysLeft !== null" class="mt-2 text-sm font-medium text-brand-brown">
                {{ biddingDaysLeft }}
                {{ biddingDaysLeft === 1 ? t.projectCreate.biddingDayLeft : t.projectCreate.biddingDaysLeft }}
              </p>
            </div>
          </section>

          <BaseAlert v-if="error" variant="error">
            {{ error }}
          </BaseAlert>

          <div class="flex gap-4 pt-6 border-t border-gray-100">
            <button
              type="button"
              :disabled="isSavingDraft"
              class="px-6 py-3 text-gray-700 bg-white border-2 border-gray-300 rounded-full hover:bg-gray-50 transition font-medium disabled:opacity-50"
              @click="saveDraftAndLeave"
            >
              {{ isSavingDraft ? t.projectCreate.saving : t.projectCreate.cancel }}
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="flex-1 px-6 py-3 text-white bg-brand-brown rounded-full hover:bg-black shadow-md hover:shadow-lg transition-all font-bold flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Loader v-if="loading" :size="20" class="animate-spin" />
              <CheckSquare v-else :size="20" />
              {{ loading ? t.projectCreate.posting : t.projectCreate.postProject }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from '@/composables/useI18n'
import { PROJECT_SCOPES, PROJECT_CATEGORIES, subCategoriesFor, isValidSubCategory } from '@/constants/projectTaxonomy'
import { Home, CheckSquare, Loader } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { useClientProfileStore } from '@/stores/clientProfile'
import { useProjectBrief } from '@/composables/useProjectBrief'
import { landingAPI } from '@/services/api'
import DeliverablesSelector from '@/components/project/DeliverablesSelector.vue'
import BudgetRangeSlider from '@/components/project/BudgetRangeSlider.vue'
import MultiImageUploader from '@/components/upload/MultiImageUploader.vue'
import BaseAlert from '@/components/ui/BaseAlert.vue'

const route = useRoute()
const { t, locale } = useI18n()
const router = useRouter()
const projectsStore = useProjectsStore()
const clientProfileStore = useClientProfileStore()
const { tokenFromRoute, clearToken } = useProjectBrief()

const existingProjectId = ref(null)
const isSavingDraft = ref(false)
const phoneFieldForced = ref(false)
const phoneError = ref('')

const showPhoneField = computed(
  () => phoneFieldForced.value || (clientProfileStore.hasProfile && !clientProfileStore.profilePhone)
)

const formData = ref({
  title: '',
  location: '',
  phoneNumber: '',
  lotSize: null,
  numberOfFloors: 1,
  projectScope: null,
  category: null,
  subCategory: null,
  description: '',
  constructionBudget: '',
  designBudget: {
    total: 0,
    min: 0,
    max: 0
  },
  deliverables: [],
  startDateType: 'IMMEDIATELY',
  expectedStartDate: '',
  biddingDeadline: (() => {
    const d = new Date()
    d.setDate(d.getDate() + 21)
    return d.toISOString().split('T')[0]
  })()
})

const availableSubCategories = computed(() => subCategoriesFor(formData.value.category))

const localeLabel = entry => (locale.value === 'id' ? entry.labelId : entry.labelEn)

const categoryDescription = computed(() => {
  const cat = PROJECT_CATEGORIES.find(c => c.value === formData.value.category)
  if (!cat) return ''
  return (locale.value === 'id' ? cat.descriptionId : cat.descriptionEn) || ''
})

// A sub-category only means something under its own category, so drop it when the
// category moves rather than submitting a value that no longer belongs.
const onCategoryChange = () => {
  if (!isValidSubCategory(formData.value.category, formData.value.subCategory)) {
    formData.value.subCategory = null
  }
}

const minStartDate = computed(() => new Date().toISOString().split('T')[0])

const minBiddingDeadline = computed(() => {
  const d = new Date()
  d.setDate(d.getDate() + 7)
  return d.toISOString().split('T')[0]
})

const maxBiddingDeadline = computed(() => {
  const d = new Date()
  d.setDate(d.getDate() + 56)
  return d.toISOString().split('T')[0]
})

const biddingDaysLeft = computed(() => {
  if (!formData.value.biddingDeadline) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const deadline = new Date(formData.value.biddingDeadline)
  const diff = Math.round((deadline - today) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : null
})

const coverImages = ref([])
const existingImages = ref([])
const loading = ref(false)
const error = ref(null)

const formatConstructionBudget = event => {
  const value = event.target.value.replace(/[^0-9]/g, '')
  if (value) {
    formData.value.constructionBudget = parseInt(value, 10).toLocaleString('id-ID')
  } else {
    formData.value.constructionBudget = ''
  }
}

const parseConstructionBudget = () => {
  if (!formData.value.constructionBudget) return null
  return parseInt(formData.value.constructionBudget.replace(/[^0-9]/g, ''), 10) || null
}

const buildProjectData = () => ({
  title: formData.value.title,
  location: formData.value.location,
  budgetTotal: parseConstructionBudget(),
  designBudgetMin: formData.value.designBudget.min,
  designBudgetMax: formData.value.designBudget.max,
  buildingFunction: formData.value.category,
  projectScope: formData.value.projectScope,
  subCategory: formData.value.subCategory,
  estimatedBuildArea: formData.value.lotSize,
  numberOfFloors: formData.value.numberOfFloors,
  scopeOfWork: formData.value.description,
  deliverables: formData.value.deliverables,
  startDateType: formData.value.startDateType,
  expectedStartDate:
    formData.value.startDateType === 'SPECIFIC_DATE' && formData.value.expectedStartDate
      ? formData.value.expectedStartDate
      : null,
  biddingDeadline: formData.value.biddingDeadline || null
})

const validatePhoneField = () => {
  phoneError.value = ''
  if (!showPhoneField.value) return true

  const value = formData.value.phoneNumber?.trim() || ''
  if (!value) {
    phoneError.value = t.value.projectCreate.phoneRequiredError
    return false
  }
  if (!/^\+?[0-9\s-]{10,16}$/.test(value)) {
    phoneError.value = t.value.projectCreate.phoneInvalidError
    return false
  }
  return true
}

const persistDraft = async () => {
  if (!formData.value.title?.trim()) return

  const projectData = buildProjectData()
  if (existingProjectId.value) {
    await projectsStore.updateDraftProject(existingProjectId.value, projectData)
  } else {
    const draft = await projectsStore.createDraftProject(projectData)
    existingProjectId.value = draft.id
  }

  if (showPhoneField.value && formData.value.phoneNumber?.trim()) {
    try {
      await clientProfileStore.updateProfile({ phoneNumber: formData.value.phoneNumber.trim() })
    } catch {
      // Best-effort — submission will still enforce this before posting
    }
  }

  const freshImages = coverImages.value.filter(f => f instanceof File)
  if (freshImages.length > 0) {
    try {
      const uploaded = await projectsStore.uploadProjectFiles(existingProjectId.value, freshImages)
      existingImages.value = [
        ...existingImages.value,
        ...uploaded.map(file => ({ id: file.id, url: file.filePath, name: file.fileName }))
      ]
      coverImages.value = []
    } catch {
      // Image upload failure doesn't block the rest of the draft save
    }
  }
}

const handleDeleteExistingImage = async imageId => {
  try {
    await projectsStore.deleteProjectFile(existingProjectId.value, imageId)
    existingImages.value = existingImages.value.filter(img => img.id !== imageId)
  } catch (err) {
    console.error('Failed to delete image:', err)
  }
}

const saveDraftAndLeave = async () => {
  isSavingDraft.value = true
  try {
    await persistDraft()
  } catch {
    // Silent — draft save is best-effort, always navigate away
  } finally {
    isSavingDraft.value = false
  }
  router.push({ name: 'ClientDashboard' })
}

const handleSubmit = async () => {
  loading.value = true
  error.value = null

  if (!validatePhoneField()) {
    loading.value = false
    return
  }

  try {
    await persistDraft()
    const newProject = await projectsStore.submitProject(existingProjectId.value, coverImages.value)
    router.push({ name: 'ClientDashboard' })
    return newProject
  } catch (err) {
    const message = err.response?.data?.message || 'Failed to create project. Please try again.'
    if (message.startsWith('PROFILE_INCOMPLETE:')) {
      error.value = message.replace('PROFILE_INCOMPLETE:', '').trim()
      phoneFieldForced.value = true
      await clientProfileStore.fetchProfile()
    } else {
      error.value = message
    }
    console.error('Failed to create project:', err)
  } finally {
    loading.value = false
  }
}

const applyLandingBrief = async () => {
  const claimToken = tokenFromRoute(route)

  let brief
  try {
    // Without a token the user likely verified their email elsewhere; fall back to the
    // brief that was bound to their account at signup.
    const res = claimToken ? await landingAPI.claimBrief(claimToken) : await landingAPI.consumeMyBrief()
    brief = res.data?.data
  } catch (err) {
    // An already-claimed or unknown token must never block the form
    console.error('Failed to load landing brief:', err)
    clearToken()
    return
  }
  if (!brief) {
    clearToken()
    return
  }

  // A server-side draft always wins — the brief only fills gaps it left behind
  if (!formData.value.title) formData.value.title = brief.title || ''
  if (!formData.value.location) formData.value.location = brief.location || ''
  if (!formData.value.description) formData.value.description = brief.description || ''
  if (!formData.value.lotSize) formData.value.lotSize = brief.lotSize ?? null
  if (!formData.value.phoneNumber) formData.value.phoneNumber = brief.phoneNumber || ''
  if (!formData.value.projectScope) formData.value.projectScope = brief.projectScope || null
  if (!formData.value.category) formData.value.category = brief.buildingFunction || null
  if (!formData.value.subCategory) formData.value.subCategory = brief.subCategory || null
  if (brief.designBudgetTotal && !formData.value.designBudget.total) {
    formData.value.designBudget = {
      total: brief.designBudgetTotal,
      min: brief.designBudgetMin ?? brief.designBudgetTotal,
      max: brief.designBudgetMax ?? brief.designBudgetTotal
    }
  }
  if (brief.startDateType && formData.value.startDateType === 'IMMEDIATELY') {
    formData.value.startDateType = brief.startDateType
    if (brief.expectedStartDate) formData.value.expectedStartDate = brief.expectedStartDate
  }

  clearToken()
}

onMounted(async () => {
  try {
    await clientProfileStore.fetchProfile()
  } catch (err) {
    console.error('Failed to fetch client profile:', err)
  }

  try {
    await projectsStore.fetchMyProjects()
    const existingDraft = projectsStore.projects.find(p => p.status === 'DRAFT')
    if (existingDraft) {
      existingProjectId.value = existingDraft.id
      formData.value.title = existingDraft.title || ''
      formData.value.location = existingDraft.location || ''
      formData.value.lotSize = existingDraft.estimatedBuildArea ?? null
      formData.value.numberOfFloors = existingDraft.numberOfFloors ?? 1
      formData.value.projectScope = existingDraft.projectScope || null
      formData.value.category = existingDraft.buildingFunction || null
      formData.value.subCategory = existingDraft.subCategory || null
      formData.value.description = existingDraft.scopeOfWork || ''
      formData.value.constructionBudget = existingDraft.budgetTotal
        ? Number(existingDraft.budgetTotal).toLocaleString('id-ID')
        : ''
      formData.value.designBudget = {
        total: existingDraft.designBudgetMax || 0,
        min: existingDraft.designBudgetMin || 0,
        max: existingDraft.designBudgetMax || 0
      }
      formData.value.deliverables = existingDraft.deliverables || []
      formData.value.startDateType = existingDraft.startDateType || 'IMMEDIATELY'
      formData.value.expectedStartDate = existingDraft.expectedStartDate || ''
      if (existingDraft.biddingDeadline) {
        formData.value.biddingDeadline = existingDraft.biddingDeadline.split('T')[0]
      }
      existingImages.value = (existingDraft.files || [])
        .filter(f => f.fileType?.startsWith('image/'))
        .map(f => ({ id: f.id, url: f.filePath, name: f.fileName }))
    }
  } catch (err) {
    console.error('Failed to check for draft project:', err)
  }

  await applyLandingBrief()
})
</script>
