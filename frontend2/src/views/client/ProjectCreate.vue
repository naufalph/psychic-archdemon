<template>
  <div class="min-h-screen bg-surface-alt py-12">
    <div class="max-w-4xl mx-auto px-6">
      <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden">
        <div class="bg-brand-brown p-8 text-white">
          <h1 class="text-3xl font-bold flex items-center gap-3">
            <Home :size="32" />
            {{ existingProjectId ? 'Continue Draft Project' : 'Post New Project' }}
          </h1>
          <p class="text-white/80 mt-2">Define your requirements to find the perfect architect</p>
        </div>

        <form @submit.prevent="handleSubmit" class="p-8 space-y-10">
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
                <p class="text-sm font-semibold text-amber-900">Phone number required</p>
                <p class="text-sm text-amber-800 mt-1">
                  Add your phone number so architects and our team can reach you. This will be saved to your
                  profile too.
                </p>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Phone Number<span class="text-red-500">*</span></label
              >
              <input
                v-model="formData.phoneNumber"
                type="tel"
                placeholder="+62 812 3456 7890"
                class="w-full px-4 py-3 border-2 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none transition"
                :class="phoneError ? 'border-red-300' : 'border-gray-200'"
              />
              <p v-if="phoneError" class="mt-1 text-sm text-red-600">{{ phoneError }}</p>
            </div>
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm">Part 0</span>
              <h2 class="text-xl font-bold text-black">Project Images</h2>
            </div>
            <p class="text-xs text-gray-500">
              Upload photos, mood boards, or inspiration images for your project (max 10 images)
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
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm">Part 1</span>
              <h2 class="text-xl font-bold text-black">General Information</h2>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Project Title<span class="text-red-500">*</span></label
              >
              <input
                v-model="formData.title"
                required
                type="text"
                placeholder="e.g., Modern Student Housing in Depok"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none transition"
              />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Location<span class="text-red-500">*</span></label
                >
                <input
                  v-model="formData.location"
                  required
                  type="text"
                  placeholder="City, Area"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Lot Size (m²)<span class="text-red-500">*</span></label
                >
                <input
                  v-model.number="formData.lotSize"
                  required
                  type="number"
                  placeholder="e.g., 200"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Number of Floors<span class="text-red-500">*</span></label
                >
                <input
                  v-model.number="formData.numberOfFloors"
                  required
                  type="number"
                  min="1"
                  placeholder="e.g., 2"
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
                />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Building Type<span class="text-red-500">*</span></label
              >
              <select
                v-model="formData.buildingType"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
              >
                <option value="RESIDENTIAL">Residential Home</option>
                <option value="STUDENT_HOUSING">Student Housing (Kost)</option>
                <option value="VILLA">Villa / Resort</option>
                <option value="COMMERCIAL">Commercial / Office</option>
                <option value="RENOVATION">Renovation</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Detailed Requirements<span class="text-red-500">*</span></label
              >
              <textarea
                v-model="formData.description"
                required
                rows="4"
                placeholder="Describe number of rooms, style preference (e.g., Industrial, Tropical), timeline constraints..."
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
              />
            </div>
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm">Part 2</span>
              <h2 class="text-xl font-bold text-black">Required Deliverables</h2>
            </div>

            <DeliverablesSelector v-model="formData.deliverables" />
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm">Part 3</span>
              <h2 class="text-xl font-bold text-black">Budgeting</h2>
            </div>

            <div class="space-y-8">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Total Construction Budget</label>
                <p class="text-xs text-gray-500 mb-2">
                  Estimate for construction, material, and labor (Fisik Bangunan)
                </p>
                <div class="relative">
                  <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 font-medium">IDR</span>
                  <input
                    v-model="formData.constructionBudget"
                    type="text"
                    placeholder="e.g., 2.000.000.000 (optional)"
                    @input="formatConstructionBudget"
                    class="w-full pl-16 pr-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none text-right font-medium"
                  />
                </div>
              </div>

              <BudgetRangeSlider
                v-model="formData.designBudget"
                label="Design Budget (Architect Fee)"
                description="This is the main reference for architects to bid"
                :step="1000000"
                :required="true"
                :range-percent="25"
                hint="<strong>IAI Guideline:</strong> According to the Indonesian Institute of Architects (IAI), the design fee is typically around <strong>5% - 7%</strong> of the total construction budget."
              />
            </div>
          </section>

          <section class="space-y-6">
            <div class="flex items-center gap-2 border-b border-gray-100 pb-3">
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm">Part 4</span>
              <h2 class="text-xl font-bold text-black">Expected Start Date</h2>
            </div>
            <p class="text-xs text-gray-500">Let architects know when you expect construction to begin.</p>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <label
                :class="[
                  'flex items-center gap-4 p-5 rounded-2xl border-2 cursor-pointer transition',
                  formData.startDateType === 'IMMEDIATELY'
                    ? 'border-brand-brown bg-brand-tan/30'
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <input type="radio" v-model="formData.startDateType" value="IMMEDIATELY" class="hidden" />
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
                  <p class="font-semibold text-gray-900">Immediately</p>
                  <p class="text-xs text-gray-500 mt-0.5">Project can start as soon as an architect is hired</p>
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
                <input type="radio" v-model="formData.startDateType" value="SPECIFIC_DATE" class="hidden" />
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
                  <p class="font-semibold text-gray-900">On a Specific Date</p>
                  <p class="text-xs text-gray-500 mt-0.5">You have a target date in mind</p>
                </div>
              </label>
            </div>

            <div v-if="formData.startDateType === 'SPECIFIC_DATE'">
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Target Start Date <span class="text-red-500">*</span>
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
              <span class="bg-brand-tan text-brand-brown font-bold px-3 py-1 rounded-full text-sm">Part 5</span>
              <h2 class="text-xl font-bold text-black">Bid Deadline</h2>
            </div>
            <p class="text-xs text-gray-500">
              Set the closing date for architect bids. After this date the project closes automatically and any
              unselected bids are refunded.
            </p>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Bidding Closes On <span class="text-red-500">*</span>
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
                {{ biddingDaysLeft }} day{{ biddingDaysLeft === 1 ? '' : 's' }} of bidding time
              </p>
            </div>
          </section>

          <BaseAlert v-if="error" variant="error">
            {{ error }}
          </BaseAlert>

          <div class="flex gap-4 pt-6 border-t border-gray-100">
            <button
              type="button"
              @click="saveDraftAndLeave"
              :disabled="isSavingDraft"
              class="px-6 py-3 text-gray-700 bg-white border-2 border-gray-300 rounded-full hover:bg-gray-50 transition font-medium disabled:opacity-50"
            >
              {{ isSavingDraft ? 'Saving...' : 'Cancel' }}
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="flex-1 px-6 py-3 text-white bg-brand-brown rounded-full hover:bg-black shadow-md hover:shadow-lg transition-all font-bold flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Loader v-if="loading" :size="20" class="animate-spin" />
              <CheckSquare v-else :size="20" />
              {{ loading ? 'Posting...' : 'Post Project' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Home, CheckSquare, Loader } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { useClientProfileStore } from '@/stores/clientProfile'
import DeliverablesSelector from '@/components/project/DeliverablesSelector.vue'
import BudgetRangeSlider from '@/components/project/BudgetRangeSlider.vue'
import MultiImageUploader from '@/components/upload/MultiImageUploader.vue'
import BaseAlert from '@/components/ui/BaseAlert.vue'

const router = useRouter()
const projectsStore = useProjectsStore()
const clientProfileStore = useClientProfileStore()

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
  buildingType: 'RESIDENTIAL',
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
  buildingFunction: formData.value.buildingType,
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
    phoneError.value = 'Phone number is required so architects and our team can reach you.'
    return false
  }
  if (!/^\+?[0-9\s-]{10,16}$/.test(value)) {
    phoneError.value = 'Please enter a valid phone number.'
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
      formData.value.buildingType = existingDraft.buildingFunction || 'RESIDENTIAL'
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
})
</script>
