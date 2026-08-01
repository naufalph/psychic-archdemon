<template>
  <div class="min-h-screen bg-surface-alt py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button @click="saveDraftAndLeave" class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition">
        <ArrowLeft :size="20" />
        {{ t.proposalCreate?.backToOpportunities || 'Back to Opportunities' }}
      </button>

      <div v-if="projectLoading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
      </div>

      <div v-else-if="projectError" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600 mb-4">{{ projectError }}</p>
        <button @click="router.push({ name: 'OpportunityList' })" class="text-brand-brown hover:underline">
          {{ t.proposalCreate?.backToOpportunities || 'Back to Opportunities' }}
        </button>
      </div>

      <div v-else-if="project" class="lg:grid lg:grid-cols-3 gap-6">
        <div class="lg:col-span-1 mb-6 lg:mb-0">
          <div class="lg:sticky lg:top-6 bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
            <h2 class="text-xl font-bold text-black mb-4">
              {{ t.proposalCreate?.projectSummary || 'Project Summary' }}
            </h2>

            <div class="space-y-4">
              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.proposalCreate?.project || 'Project' }}
                </p>
                <p class="font-bold text-gray-900">{{ project.title }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.proposalCreate?.location || 'Location' }}
                </p>
                <p class="text-gray-900">{{ project.location }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.proposalCreate?.designBudget || 'Design Budget' }}
                </p>
                <p class="text-gray-900 font-medium">
                  {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
                </p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.proposalCreate?.buildArea || 'Build Area' }}
                </p>
                <p class="text-gray-900">{{ project.estimatedBuildArea }} m²</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.proposalCreate?.buildingType || 'Building Type' }}
                </p>
                <p class="text-gray-900">{{ project.buildingType }}</p>
              </div>

              <div v-if="project.deliverables && project.deliverables.length > 0">
                <p class="text-xs text-gray-500 uppercase font-bold mb-2">
                  {{ t.proposalCreate?.deliverables || 'Deliverables' }}
                </p>
                <div class="space-y-2">
                  <div v-for="group in groupedProjectDeliverables" :key="group.categoryKey">
                    <p class="text-xs text-gray-400 font-semibold mb-1">
                      {{ t.proposalCreate?.deliverableCategories?.[group.categoryKey] }}
                    </p>
                    <div class="flex flex-wrap gap-1">
                      <span
                        v-for="d in group.items"
                        :key="d"
                        class="bg-brand-tan text-brand-brown px-2 py-0.5 rounded-full text-xs font-medium"
                      >
                        {{ t.proposalCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="project.biddingDeadline" class="pt-4 border-t border-gray-100">
                <BiddingCountdown :deadline="project.biddingDeadline" size="sm" />
              </div>
            </div>
          </div>
        </div>

        <div class="lg:col-span-2">
          <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden">
            <div class="bg-brand-brown p-8 text-white">
              <h1 class="text-3xl font-bold flex items-center gap-3">
                <FileText :size="32" />
                {{
                  existingBidId
                    ? t.proposalCreate?.updateTitle || 'Update Proposal'
                    : t.proposalCreate?.title || 'Submit Proposal'
                }}
              </h1>
              <p class="text-white/80 mt-2">
                {{
                  existingBidId
                    ? t.proposalCreate?.updateSubtitle || 'Continue editing your draft proposal'
                    : t.proposalCreate?.subtitle || 'Showcase your expertise and win the project'
                }}
              </p>
            </div>

            <div
              v-if="profileStore.profile && !isIdentityComplete"
              class="mx-8 mt-6 p-5 bg-amber-50 border border-amber-200 rounded-2xl flex gap-4 items-start"
            >
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
              <div class="flex-1">
                <p class="text-sm font-semibold text-amber-900">
                  {{ t.proposalCreate?.identityIncompleteTitle || 'Identity verification required to submit' }}
                </p>
                <p class="text-sm text-amber-800 mt-1">
                  {{
                    t.proposalCreate?.identityIncompleteDesc ||
                    'Complete your KTP, NPWP, full name, and WhatsApp OTP verification before submitting a bid. You can still save a draft.'
                  }}
                </p>
                <router-link
                  to="/architect/profile"
                  class="inline-block mt-2 text-sm font-semibold text-amber-900 underline hover:text-brand-brown"
                >
                  {{ t.proposalCreate?.identityIncompleteAction || 'Complete Profile' }} →
                </router-link>
              </div>
            </div>

            <form @submit.prevent="handleSubmit" class="p-8 space-y-8">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2"
                    >{{ t.proposalCreate?.bidAmount || 'Bid Amount (IDR)' }}<span class="text-red-500">*</span></label
                  >
                  <input
                    v-model.number="formData.bidAmount"
                    required
                    type="number"
                    placeholder="e.g., 50000000"
                    class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">About your studio</label>
                <textarea
                  v-model="formData.proposal"
                  rows="6"
                  placeholder="Tell the client about your studio's expertise, experience, and approach..."
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Concept Statement<span class="text-red-500">*</span>
                </label>
                <p class="text-xs text-gray-500 mb-3">
                  Describe your design plan, concept, and approach for this specific project (max 200 words)
                </p>
                <textarea
                  v-model="formData.conceptStatement"
                  rows="6"
                  required
                  placeholder="Explain your design concept, key features, and how you'll address the client's needs..."
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                  @input="validateWordCount"
                />
                <p class="text-xs text-gray-500 mt-1">{{ wordCount }}/200 words</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">{{
                  t.proposalCreate?.paymentPhases || 'Payment Phases'
                }}</label>
                <p class="text-xs text-gray-500 mb-3">
                  {{
                    t.proposalCreate?.paymentPhasesHelp ||
                    'Define payment phases. Phase 0 is free (pre-project). Phase 1+ must total your bid amount.'
                  }}
                </p>
                <PaymentPhaseBuilder v-model="formData.phases" :bid-amount="formData.bidAmount" />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Attach Relevant Portfolios</label>
                <p class="text-xs text-gray-500 mb-3">Select portfolios that demonstrate your relevant experience</p>
                <PortfolioSelector v-model="formData.portfolioIds" />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Facade Images <span class="text-gray-400 font-normal">(exterior views, max 5)</span>
                </label>
                <MultiImageUploader
                  v-model="facadeImages"
                  :max-files="5"
                  :existing-images="existingFacade"
                  @delete-existing="id => deleteExistingImage(id, 'facade')"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Interior Images <span class="text-gray-400 font-normal">(interior spaces, max 5)</span>
                </label>
                <MultiImageUploader
                  v-model="interiorImages"
                  :max-files="5"
                  :existing-images="existingInterior"
                  @delete-existing="id => deleteExistingImage(id, 'interior')"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Massing Images <span class="text-gray-400 font-normal">(3D form studies, max 5)</span>
                </label>
                <MultiImageUploader
                  v-model="massingImages"
                  :max-files="5"
                  :existing-images="existingMassing"
                  @delete-existing="id => deleteExistingImage(id, 'massing')"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Zoning Images <span class="text-gray-400 font-normal">(site plan diagrams, max 5)</span>
                </label>
                <MultiImageUploader
                  v-model="zoningImages"
                  :max-files="5"
                  :existing-images="existingZoning"
                  @delete-existing="id => deleteExistingImage(id, 'zoning')"
                />
              </div>

              <div v-if="uploadProgress > 0" class="bg-gray-50 rounded-2xl p-6">
                <UploadProgress :progress="uploadProgress" label="Uploading files..." />
              </div>

              <div ref="errorRef" v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-xl space-y-2">
                <p class="text-sm font-semibold text-red-700">{{ error }}</p>
                <ul v-if="identityMissing.length" class="text-xs text-red-600 list-disc list-inside space-y-0.5">
                  <li v-for="item in identityMissing" :key="item">{{ item }}</li>
                </ul>
                <router-link
                  v-if="!isIdentityComplete"
                  to="/architect/profile"
                  class="inline-block text-sm font-semibold text-brand-brown underline"
                >
                  {{ t.value.proposalCreate?.completeProfileLink || 'Complete your profile →' }}
                </router-link>
              </div>

              <div class="flex gap-4 pt-6 border-t border-gray-100">
                <button
                  type="button"
                  @click="saveDraftAndLeave"
                  class="px-6 py-3 text-gray-700 bg-white border-2 border-gray-300 rounded-full hover:bg-gray-50 transition font-medium"
                >
                  {{ t.proposalCreate?.cancelBtn || 'Cancel' }}
                </button>
                <button
                  type="submit"
                  :disabled="(loading && !isSavingDraft) || uploadProgress > 0"
                  class="flex-1 px-6 py-3 text-white bg-brand-brown rounded-full hover:bg-black shadow-md hover:shadow-lg transition-all font-bold flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  <Loader v-if="loading && !isSavingDraft" :size="20" class="animate-spin" />
                  <Send v-else :size="20" />
                  {{
                    loading && !isSavingDraft
                      ? t.proposalCreate?.submitting || 'Submitting...'
                      : t.proposalCreate?.submitBtn || 'Submit Proposal'
                  }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, FileText, Loader, Send } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import { useProjectsStore } from '@/stores/projects'
import { useArchitectProfileStore } from '@/stores/architectProfile'
import { useI18n } from '@/composables/useI18n'
import MultiImageUploader from '@/components/upload/MultiImageUploader.vue'
import UploadProgress from '@/components/upload/UploadProgress.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'
import PaymentPhaseBuilder from '@/components/project/PaymentPhaseBuilder.vue'
import PortfolioSelector from '@/components/architect/PortfolioSelector.vue'

const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()
const projectsStore = useProjectsStore()
const profileStore = useArchitectProfileStore()
const { t } = useI18n()

const identityMissing = computed(() => {
  const p = profileStore.profile
  if (!p) return ['profile']
  const missing = []
  if (!p.ktpNum || !p.ktpNum.trim()) missing.push('KTP')
  if (!p.npwp || !p.npwp.trim()) missing.push('NPWP')
  if (!p.fullnameKtp || !p.fullnameKtp.trim()) missing.push('Nama Lengkap sesuai KTP')
  if (!p.phoneVerified) missing.push('Verifikasi nomor HP')
  return missing
})

const isIdentityComplete = computed(() => identityMissing.value.length === 0)

const DELIVERABLE_CATEGORIES = [
  { categoryKey: 'siteAnalysis', items: ['SITE_ANALYSIS', 'ZONING_STUDY'] },
  {
    categoryKey: 'designPhases',
    items: ['CONCEPT_DESIGN', 'SCHEMATIC_DESIGN', 'DESIGN_DEVELOPMENT', 'CONSTRUCTION_DOCS']
  },
  { categoryKey: 'permits', items: ['IMB_PERMIT', 'SLF_CERT', 'ENVIRONMENTAL_PERMIT'] },
  { categoryKey: 'specialized', items: ['INTERIOR_DESIGN', 'LANDSCAPE_DESIGN', 'MEP_DESIGN', 'STRUCTURAL_DESIGN'] },
  { categoryKey: 'construction', items: ['SUPERVISION', 'AS_BUILT'] }
]

const { loading, uploadProgress } = storeToRefs(bidsStore)

const formData = ref({
  bidAmount: null,
  proposal: '',
  conceptStatement: '',
  phases: [],
  portfolioIds: []
})

const facadeImages = ref([])
const interiorImages = ref([])
const massingImages = ref([])
const zoningImages = ref([])
const existingFacade = ref([])
const existingInterior = ref([])
const existingMassing = ref([])
const existingZoning = ref([])
const error = ref(null)
const existingBidId = ref(null)

const project = ref(null)
const projectLoading = ref(false)
const projectError = ref(null)

const formatCurrency = value => {
  if (!value) return 'N/A'
  const millions = value / 1000000
  if (millions >= 1) {
    return `Rp ${millions.toFixed(0)}M`
  }
  const thousands = value / 1000
  return `Rp ${thousands.toFixed(0)}K`
}

const groupedProjectDeliverables = computed(() => {
  const deliverables = project.value?.deliverables || []
  return DELIVERABLE_CATEGORIES.map(group => ({
    categoryKey: group.categoryKey,
    items: group.items.filter(d => deliverables.includes(d))
  })).filter(group => group.items.length > 0)
})

const wordCount = computed(() => {
  const text = formData.value.conceptStatement || ''
  return text
    .trim()
    .split(/\s+/)
    .filter(word => word.length > 0).length
})

const validateWordCount = () => {
  if (wordCount.value > 200) {
    error.value = 'Concept statement must not exceed 200 words'
  } else if (error.value === 'Concept statement must not exceed 200 words') {
    error.value = null
  }
}

const deleteExistingImage = async (imageId, type) => {
  try {
    await bidsStore.deleteBidImage(imageId)
    if (type === 'facade') {
      existingFacade.value = existingFacade.value.filter(img => img.id !== imageId)
    } else if (type === 'interior') {
      existingInterior.value = existingInterior.value.filter(img => img.id !== imageId)
    } else if (type === 'massing') {
      existingMassing.value = existingMassing.value.filter(img => img.id !== imageId)
    } else if (type === 'zoning') {
      existingZoning.value = existingZoning.value.filter(img => img.id !== imageId)
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to delete image'
    console.error('Failed to delete image:', err)
  }
}

const isSavingDraft = ref(false)

const saveDraftAndLeave = async () => {
  isSavingDraft.value = true
  try {
    if (formData.value.bidAmount) {
      let bid
      const bidData = {
        projectId: route.params.projectId,
        bidAmount: formData.value.bidAmount,
        proposal: formData.value.proposal
      }
      if (existingBidId.value) {
        await bidsStore.updateDraftBid(existingBidId.value, bidData)
        bid = { id: existingBidId.value }
      } else {
        bid = await bidsStore.createDraftBid(bidData)
        existingBidId.value = bid.id
      }
      await bidsStore.updateBidDetails(bid.id, {
        conceptStatement: formData.value.conceptStatement,
        phases: formData.value.phases
      })

      if (formData.value.portfolioIds.length > 0) {
        await bidsStore.linkPortfolios(bid.id, formData.value.portfolioIds)
      }

      const uploadIfAny = async (type, newFiles, existingRef, fileRef) => {
        const fresh = newFiles.filter(f => f instanceof File)
        if (!fresh.length) return
        try {
          const uploaded = await bidsStore.uploadBidImages(bid.id, type, fresh)
          existingRef.value = [
            ...existingRef.value,
            ...uploaded.map(img => ({ id: img.id, url: img.imageUrl, name: img.fileName }))
          ]
          fileRef.value = []
        } catch {
          // per-type failure doesn't abort the others
        }
      }

      await uploadIfAny('FACADE', facadeImages.value, existingFacade, facadeImages)
      await uploadIfAny('INTERIOR', interiorImages.value, existingInterior, interiorImages)
      await uploadIfAny('MASSING', massingImages.value, existingMassing, massingImages)
      await uploadIfAny('ZONING', zoningImages.value, existingZoning, zoningImages)
    }
  } catch {
    // Silent — draft save is best-effort, always navigate away
  } finally {
    isSavingDraft.value = false
  }
  router.push({ name: 'OpportunityList' })
}

const errorRef = ref(null)

const scrollToError = async () => {
  await nextTick()
  errorRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const handleSubmit = async () => {
  error.value = null

  if (!isIdentityComplete.value) {
    await profileStore.fetchProfile()
  }
  if (!isIdentityComplete.value) {
    error.value =
      t.value.proposalCreate?.identityIncompleteError ||
      'Harap lengkapi data identitas berikut di halaman Profil sebelum mengirim penawaran:'
    await scrollToError()
    return
  }

  if (wordCount.value > 200) {
    error.value = t('proposal.wordCountExceeded')
    return
  }

  const phasesTotal = formData.value.phases.reduce((sum, p) => sum + (Number(p.amount) || 0), 0)
  if (formData.value.phases.length > 0 && Math.abs(phasesTotal - Number(formData.value.bidAmount)) >= 1) {
    error.value = t('proposal.phasesTotalMismatch')
    return
  }

  try {
    let bid

    const bidData = {
      projectId: route.params.projectId,
      bidAmount: formData.value.bidAmount,
      proposal: formData.value.proposal,
      deliverables: formData.value.deliverables
    }

    if (existingBidId.value) {
      await bidsStore.updateDraftBid(existingBidId.value, bidData)
      bid = { id: existingBidId.value }
    } else {
      bid = await bidsStore.createDraftBid(bidData)
      existingBidId.value = bid.id
    }

    const bidDetailData = {
      conceptStatement: formData.value.conceptStatement,
      phases: formData.value.phases
    }
    await bidsStore.updateBidDetails(bid.id, bidDetailData)

    if (formData.value.portfolioIds.length > 0) {
      await bidsStore.linkPortfolios(bid.id, formData.value.portfolioIds)
    }

    const newFacade = facadeImages.value.filter(f => f instanceof File)
    if (newFacade.length > 0) {
      await bidsStore.uploadBidImages(bid.id, 'FACADE', newFacade)
    }

    const newInterior = interiorImages.value.filter(f => f instanceof File)
    if (newInterior.length > 0) {
      await bidsStore.uploadBidImages(bid.id, 'INTERIOR', newInterior)
    }

    const newMassing = massingImages.value.filter(f => f instanceof File)
    if (newMassing.length > 0) {
      await bidsStore.uploadBidImages(bid.id, 'MASSING', newMassing)
    }

    const newZoning = zoningImages.value.filter(f => f instanceof File)
    if (newZoning.length > 0) {
      await bidsStore.uploadBidImages(bid.id, 'ZONING', newZoning)
    }

    await bidsStore.submitBid(bid.id)

    router.push({ name: 'MyBids' })
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to submit proposal. Please try again.'
    console.error('Failed to submit proposal:', err)
  }
}

onMounted(async () => {
  projectLoading.value = true
  projectError.value = null
  try {
    await profileStore.fetchProfile()
    project.value = await projectsStore.fetchProjectForArchitect(route.params.projectId)

    await bidsStore.fetchMyBids()
    const existingDraft = bidsStore.myBids.find(
      bid => bid.projectId === parseInt(route.params.projectId) && bid.status === 'DRAFT'
    )

    if (existingDraft) {
      existingBidId.value = existingDraft.id
      formData.value.bidAmount = existingDraft.bidAmount
      formData.value.proposal = existingDraft.proposal || ''
      formData.value.conceptStatement = existingDraft.details?.conceptStatement || ''
      formData.value.phases = existingDraft.details?.phases || []

      try {
        const fullDraft = await bidsStore.fetchBidById(existingDraft.id)
        formData.value.portfolioIds = fullDraft.portfolioReferences?.map(p => p.id) || []
      } catch {
        formData.value.portfolioIds = []
      }

      existingFacade.value = (existingDraft.facadeImages || []).map(img => ({
        id: img.id,
        url: img.imageUrl,
        name: img.fileName || 'Facade'
      }))
      existingInterior.value = (existingDraft.interiorImages || []).map(img => ({
        id: img.id,
        url: img.imageUrl,
        name: img.fileName || 'Interior'
      }))
      existingMassing.value = (existingDraft.massingImages || []).map(img => ({
        id: img.id,
        url: img.imageUrl,
        name: img.fileName || 'Massing'
      }))
      existingZoning.value = (existingDraft.zoningImages || []).map(img => ({
        id: img.id,
        url: img.imageUrl,
        name: img.fileName || 'Zoning'
      }))
    }
  } catch (err) {
    projectError.value = err.response?.data?.message || 'Failed to load project details'
    console.error('Failed to fetch project:', err)
  } finally {
    projectLoading.value = false
  }
})
</script>
