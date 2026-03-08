<template>
  <div class="min-h-screen bg-[#F4F5F7] py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button
        @click="router.push({ name: 'OpportunityList' })"
        class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition"
      >
        <ArrowLeft :size="20" />
        Back to Opportunities
      </button>

      <div v-if="projectLoading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
      </div>

      <div v-else-if="projectError" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600 mb-4">{{ projectError }}</p>
        <button @click="router.push({ name: 'OpportunityList' })" class="text-[#7C4728] hover:underline">
          Back to Opportunities
        </button>
      </div>

      <div v-else-if="project" class="lg:grid lg:grid-cols-3 gap-6">
        <div class="lg:col-span-1 mb-6 lg:mb-0">
          <div class="lg:sticky lg:top-6 bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
            <h2 class="text-xl font-bold text-black mb-4">Project Summary</h2>

            <div class="space-y-4">
              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">Project</p>
                <p class="font-bold text-gray-900">{{ project.title }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">Location</p>
                <p class="text-gray-900">{{ project.location }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">Design Budget</p>
                <p class="text-gray-900 font-medium">
                  {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
                </p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">Build Area</p>
                <p class="text-gray-900">{{ project.estimatedBuildArea }} m²</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">Building Type</p>
                <p class="text-gray-900">{{ project.buildingType }}</p>
              </div>

              <div v-if="project.deliverables && project.deliverables.length > 0">
                <p class="text-xs text-gray-500 uppercase font-bold mb-2">Deliverables</p>
                <div class="flex flex-wrap gap-1">
                  <span
                    v-for="deliverable in project.deliverables"
                    :key="deliverable"
                    class="bg-[#F5E6D3] text-[#7C4728] px-2 py-1 rounded-full text-xs font-medium"
                  >
                    {{ deliverable.replace(/_/g, ' ') }}
                  </span>
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
            <div class="bg-[#7C4728] p-8 text-white">
              <h1 class="text-3xl font-bold flex items-center gap-3">
                <FileText :size="32" />
                {{ existingBidId ? 'Update Proposal' : 'Submit Proposal' }}
              </h1>
              <p class="text-white/80 mt-2">
                {{
                  existingBidId ? 'Continue editing your draft proposal' : 'Showcase your expertise and win the project'
                }}
              </p>
            </div>

            <form @submit.prevent="handleSubmit" class="p-8 space-y-8">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2"
                    >Bid Amount (IDR)<span class="text-red-500">*</span></label
                  >
                  <input
                    v-model.number="formData.bidAmount"
                    required
                    type="number"
                    placeholder="e.g., 50000000"
                    class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
                  />
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Proposed Timeline (days)</label>
                  <input
                    v-model.number="formData.proposedTimelineDays"
                    type="number"
                    placeholder="e.g., 60"
                    class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">About your studio</label>
                <textarea
                  v-model="formData.proposal"
                  rows="6"
                  placeholder="Tell the client about your studio's expertise, experience, and approach..."
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
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
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
                  @input="validateWordCount"
                />
                <p class="text-xs text-gray-500 mt-1">{{ wordCount }}/200 words</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"> Deliverables You'll Provide </label>
                <p class="text-xs text-gray-500 mb-3">
                  Select the services and documents you commit to deliver for this project. Set revision rounds inline
                  per phase.
                </p>
                <DeliverablesSelector v-model="formData.deliverables" v-model:revisions="formData.revisions" />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Attach Relevant Portfolios</label>
                <p class="text-xs text-gray-500 mb-3">Select portfolios that demonstrate your relevant experience</p>
                <PortfolioSelector v-model="formData.portfolioIds" />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Facade Images <span class="text-gray-400 font-normal">(exterior views, max 5)</span></label
                >
                <div v-if="existingFacade.length > 0" class="mb-4">
                  <p class="text-xs text-gray-500 mb-2">Existing ({{ existingFacade.length }}/5)</p>
                  <div class="grid grid-cols-3 gap-3">
                    <div
                      v-for="image in existingFacade"
                      :key="image.id"
                      class="relative group rounded-lg overflow-hidden border-2 border-gray-200"
                    >
                      <img :src="image.url" :alt="image.name" class="w-full h-24 object-cover" />
                      <button
                        type="button"
                        @click="deleteExistingImage(image.id, 'facade')"
                        class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition hover:bg-red-600"
                      >
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </button>
                      <p class="absolute bottom-0 left-0 right-0 bg-black/50 text-white text-xs px-2 py-1 truncate">
                        {{ image.name }}
                      </p>
                    </div>
                  </div>
                </div>
                <div v-if="existingFacade.length < 5">
                  <p class="text-xs text-gray-500 mb-2">
                    Upload new ({{ facadeImages.length }}/{{ 5 - existingFacade.length }} slots available)
                  </p>
                  <MultiImageUploader v-model="facadeImages" label="" :max-files="5 - existingFacade.length" />
                </div>
                <p v-else class="text-sm text-amber-600 mt-2">
                  Limit reached. Delete existing images to upload new ones.
                </p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Interior Images <span class="text-gray-400 font-normal">(interior spaces, max 5)</span></label
                >
                <div v-if="existingInterior.length > 0" class="mb-4">
                  <p class="text-xs text-gray-500 mb-2">Existing ({{ existingInterior.length }}/5)</p>
                  <div class="grid grid-cols-3 gap-3">
                    <div
                      v-for="image in existingInterior"
                      :key="image.id"
                      class="relative group rounded-lg overflow-hidden border-2 border-gray-200"
                    >
                      <img :src="image.url" :alt="image.name" class="w-full h-24 object-cover" />
                      <button
                        type="button"
                        @click="deleteExistingImage(image.id, 'interior')"
                        class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition hover:bg-red-600"
                      >
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </button>
                      <p class="absolute bottom-0 left-0 right-0 bg-black/50 text-white text-xs px-2 py-1 truncate">
                        {{ image.name }}
                      </p>
                    </div>
                  </div>
                </div>
                <div v-if="existingInterior.length < 5">
                  <p class="text-xs text-gray-500 mb-2">
                    Upload new ({{ interiorImages.length }}/{{ 5 - existingInterior.length }} slots available)
                  </p>
                  <MultiImageUploader v-model="interiorImages" label="" :max-files="5 - existingInterior.length" />
                </div>
                <p v-else class="text-sm text-amber-600 mt-2">
                  Limit reached. Delete existing images to upload new ones.
                </p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Massing Images <span class="text-gray-400 font-normal">(3D form studies, max 5)</span></label
                >
                <div v-if="existingMassing.length > 0" class="mb-4">
                  <p class="text-xs text-gray-500 mb-2">Existing ({{ existingMassing.length }}/5)</p>
                  <div class="grid grid-cols-3 gap-3">
                    <div
                      v-for="image in existingMassing"
                      :key="image.id"
                      class="relative group rounded-lg overflow-hidden border-2 border-gray-200"
                    >
                      <img :src="image.url" :alt="image.name" class="w-full h-24 object-cover" />
                      <button
                        type="button"
                        @click="deleteExistingImage(image.id, 'massing')"
                        class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition hover:bg-red-600"
                      >
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </button>
                      <p class="absolute bottom-0 left-0 right-0 bg-black/50 text-white text-xs px-2 py-1 truncate">
                        {{ image.name }}
                      </p>
                    </div>
                  </div>
                </div>
                <div v-if="existingMassing.length < 5">
                  <p class="text-xs text-gray-500 mb-2">
                    Upload new ({{ massingImages.length }}/{{ 5 - existingMassing.length }} slots available)
                  </p>
                  <MultiImageUploader v-model="massingImages" label="" :max-files="5 - existingMassing.length" />
                </div>
                <p v-else class="text-sm text-amber-600 mt-2">
                  Limit reached. Delete existing images to upload new ones.
                </p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >Zoning Images <span class="text-gray-400 font-normal">(site plan diagrams, max 5)</span></label
                >
                <div v-if="existingZoning.length > 0" class="mb-4">
                  <p class="text-xs text-gray-500 mb-2">Existing ({{ existingZoning.length }}/5)</p>
                  <div class="grid grid-cols-3 gap-3">
                    <div
                      v-for="image in existingZoning"
                      :key="image.id"
                      class="relative group rounded-lg overflow-hidden border-2 border-gray-200"
                    >
                      <img :src="image.url" :alt="image.name" class="w-full h-24 object-cover" />
                      <button
                        type="button"
                        @click="deleteExistingImage(image.id, 'zoning')"
                        class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition hover:bg-red-600"
                      >
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </button>
                      <p class="absolute bottom-0 left-0 right-0 bg-black/50 text-white text-xs px-2 py-1 truncate">
                        {{ image.name }}
                      </p>
                    </div>
                  </div>
                </div>
                <div v-if="existingZoning.length < 5">
                  <p class="text-xs text-gray-500 mb-2">
                    Upload new ({{ zoningImages.length }}/{{ 5 - existingZoning.length }} slots available)
                  </p>
                  <MultiImageUploader v-model="zoningImages" label="" :max-files="5 - existingZoning.length" />
                </div>
                <p v-else class="text-sm text-amber-600 mt-2">
                  Limit reached. Delete existing images to upload new ones.
                </p>
              </div>

              <div v-if="uploadProgress > 0" class="bg-gray-50 rounded-2xl p-6">
                <UploadProgress :progress="uploadProgress" label="Uploading files..." />
              </div>

              <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
                {{ error }}
              </div>

              <div class="flex gap-4 pt-6 border-t border-gray-100">
                <button
                  type="button"
                  @click="router.push({ name: 'OpportunityList' })"
                  class="px-6 py-3 text-gray-700 bg-white border-2 border-gray-300 rounded-full hover:bg-gray-50 transition font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  :disabled="loading || uploadProgress > 0"
                  class="flex-1 px-6 py-3 text-white bg-[#7C4728] rounded-full hover:bg-black shadow-md hover:shadow-lg transition-all font-bold flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  <Loader v-if="loading" :size="20" class="animate-spin" />
                  <Send v-else :size="20" />
                  {{ loading ? 'Submitting...' : 'Submit Proposal' }}
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, FileText, Loader, Send } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import { useProjectsStore } from '@/stores/projects'
import MultiImageUploader from '@/components/upload/MultiImageUploader.vue'
import UploadProgress from '@/components/upload/UploadProgress.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'
import DeliverablesSelector from '@/components/project/DeliverablesSelector.vue'
import PortfolioSelector from '@/components/architect/PortfolioSelector.vue'

const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()
const projectsStore = useProjectsStore()

const { loading, uploadProgress } = storeToRefs(bidsStore)

const formData = ref({
  bidAmount: null,
  proposedTimelineDays: null,
  proposal: '',
  conceptStatement: '',
  deliverables: [],
  portfolioIds: [],
  revisions: {
    siteAnalysisRevisions: null,
    designRevisions: null,
    permitsDocRevisions: null,
    specializedServicesRevisions: null,
    constructionSupportRevisions: null
  }
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

const handleSubmit = async () => {
  error.value = null

  if (wordCount.value > 200) {
    error.value = 'Concept statement must not exceed 200 words'
    return
  }

  try {
    let bid

    const bidData = {
      projectId: route.params.projectId,
      bidAmount: formData.value.bidAmount,
      proposedTimelineDays: formData.value.proposedTimelineDays,
      proposal: formData.value.proposal,
      deliverables: formData.value.deliverables
    }

    bid = existingBidId.value ? { id: existingBidId.value } : await bidsStore.createDraftBid(bidData)

    const bidDetailData = {
      conceptStatement: formData.value.conceptStatement,
      deliverables: formData.value.deliverables,
      ...formData.value.revisions
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
    project.value = await projectsStore.fetchProjectForArchitect(route.params.projectId)

    await bidsStore.fetchMyBids()
    const existingDraft = bidsStore.myBids.find(
      bid => bid.projectId === parseInt(route.params.projectId) && bid.status === 'DRAFT'
    )

    if (existingDraft) {
      existingBidId.value = existingDraft.id
      formData.value.bidAmount = existingDraft.bidAmount
      formData.value.proposedTimelineDays = existingDraft.proposedTimelineDays
      formData.value.proposal = existingDraft.proposal || ''
      formData.value.conceptStatement = existingDraft.details?.conceptStatement || ''
      formData.value.deliverables = existingDraft.details?.deliverables || []
      formData.value.portfolioIds = existingDraft.portfolios?.map(p => p.id) || []
      formData.value.revisions = {
        siteAnalysisRevisions: existingDraft.details?.siteAnalysisRevisions ?? null,
        designRevisions: existingDraft.details?.designRevisions ?? null,
        permitsDocRevisions: existingDraft.details?.permitsDocRevisions ?? null,
        specializedServicesRevisions: existingDraft.details?.specializedServicesRevisions ?? null,
        constructionSupportRevisions: existingDraft.details?.constructionSupportRevisions ?? null
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
