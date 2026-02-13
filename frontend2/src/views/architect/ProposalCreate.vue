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
                  Select the services and documents you commit to deliver for this project
                </p>
                <DeliverablesSelector v-model="formData.deliverables" />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Attach Relevant Portfolios</label>
                <p class="text-xs text-gray-500 mb-3">Select portfolios that demonstrate your relevant experience</p>
                <PortfolioSelector v-model="formData.portfolioIds" />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Concept Sketches</label>

                <div v-if="existingConceptSketches.length > 0" class="mb-4">
                  <p class="text-xs text-gray-500 mb-2">Existing images ({{ existingConceptSketches.length }}/3)</p>
                  <div class="grid grid-cols-3 gap-3">
                    <div
                      v-for="image in existingConceptSketches"
                      :key="image.id"
                      class="relative group rounded-lg overflow-hidden border-2 border-gray-200"
                    >
                      <img :src="image.url" :alt="image.name" class="w-full h-24 object-cover" />
                      <button
                        type="button"
                        @click="deleteExistingImage(image.id, 'concept')"
                        class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition hover:bg-red-600"
                        :title="'Delete ' + image.name"
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

                <div v-if="existingConceptSketches.length < 3">
                  <p class="text-xs text-gray-500 mb-2">
                    Upload new images ({{ conceptSketches.length }}/{{ 3 - existingConceptSketches.length }} slots
                    available)
                  </p>
                  <MultiImageUploader
                    v-model="conceptSketches"
                    label=""
                    :max-files="3 - existingConceptSketches.length"
                  />
                </div>
                <p v-else class="text-sm text-amber-600 mt-2">
                  Limit reached. Delete existing images to upload new ones.
                </p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Mood Boards & Inspiration</label>

                <div v-if="existingMoodBoards.length > 0" class="mb-4">
                  <p class="text-xs text-gray-500 mb-2">Existing images ({{ existingMoodBoards.length }})</p>
                  <div class="grid grid-cols-3 gap-3">
                    <div
                      v-for="image in existingMoodBoards"
                      :key="image.id"
                      class="relative group rounded-lg overflow-hidden border-2 border-gray-200"
                    >
                      <img :src="image.url" :alt="image.name" class="w-full h-24 object-cover" />
                      <button
                        type="button"
                        @click="deleteExistingImage(image.id, 'mood')"
                        class="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition hover:bg-red-600"
                        :title="'Delete ' + image.name"
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

                <div>
                  <p class="text-xs text-gray-500 mb-2">Upload new images</p>
                  <MultiImageUploader v-model="moodBoards" label="" :max-files="5" />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"> Proposal Document (PDF) </label>
                <p class="text-xs text-gray-500 mb-3">Upload a detailed proposal document (optional, max 10MB)</p>

                <div v-if="existingAttachments.length > 0" class="mb-4">
                  <p class="text-xs text-gray-500 mb-2">Existing attachments</p>
                  <div class="space-y-2">
                    <div
                      v-for="attachment in existingAttachments"
                      :key="attachment.id"
                      class="border border-gray-200 rounded-xl p-4 flex items-center gap-3"
                    >
                      <FileText :size="24" class="text-[#7C4728]" />
                      <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-gray-900 truncate">{{ attachment.fileName }}</p>
                        <p class="text-xs text-gray-500">{{ (attachment.fileSize / 1024 / 1024).toFixed(2) }} MB</p>
                      </div>
                      <button
                        type="button"
                        @click="deleteExistingAttachment(attachment.id)"
                        class="p-1 rounded-full hover:bg-red-50 transition"
                        :title="'Delete ' + attachment.fileName"
                      >
                        <X :size="20" class="text-red-600" />
                      </button>
                    </div>
                  </div>
                </div>

                <div v-if="existingAttachments.length === 0">
                  <FileUploader v-model="pdfAttachment" label="" />
                </div>
                <p v-else class="text-sm text-amber-600 mt-2">
                  Only one PDF attachment allowed. Delete existing to upload new one.
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
import { ArrowLeft, FileText, Loader, Send, X } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import { useProjectsStore } from '@/stores/projects'
import MultiImageUploader from '@/components/upload/MultiImageUploader.vue'
import FileUploader from '@/components/upload/FileUploader.vue'
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
  portfolioIds: []
})

const conceptSketches = ref([])
const moodBoards = ref([])
const existingConceptSketches = ref([])
const existingMoodBoards = ref([])
const pdfAttachment = ref(null)
const existingAttachments = ref([])
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
    if (type === 'concept') {
      await bidsStore.deleteConceptSketch(imageId)
      existingConceptSketches.value = existingConceptSketches.value.filter(img => img.id !== imageId)
    } else {
      await bidsStore.deleteMoodBoard(imageId)
      existingMoodBoards.value = existingMoodBoards.value.filter(img => img.id !== imageId)
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to delete image'
    console.error('Failed to delete image:', err)
  }
}

const deleteExistingAttachment = async attachmentId => {
  try {
    await bidsStore.deleteAttachment(attachmentId)
    existingAttachments.value = existingAttachments.value.filter(att => att.id !== attachmentId)
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to delete attachment'
    console.error('Failed to delete attachment:', err)
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
      deliverables: formData.value.deliverables
    }
    await bidsStore.updateBidDetails(bid.id, bidDetailData)

    if (formData.value.portfolioIds.length > 0) {
      await bidsStore.linkPortfolios(bid.id, formData.value.portfolioIds)
    }

    const newConceptSketches = conceptSketches.value.filter(file => file instanceof File)
    if (newConceptSketches.length > 0) {
      await bidsStore.uploadConceptSketches(bid.id, newConceptSketches)
    }

    const newMoodBoards = moodBoards.value.filter(file => file instanceof File)
    if (newMoodBoards.length > 0) {
      await bidsStore.uploadMoodBoards(bid.id, newMoodBoards)
    }

    if (pdfAttachment.value instanceof File) {
      await bidsStore.uploadAttachment(bid.id, pdfAttachment.value)
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

      if (existingDraft.conceptSketches && existingDraft.conceptSketches.length > 0) {
        existingConceptSketches.value = existingDraft.conceptSketches.map(sketch => ({
          id: sketch.id,
          url: sketch.imageUrl,
          name: sketch.fileName || 'Concept Sketch'
        }))
      }

      if (existingDraft.moodBoards && existingDraft.moodBoards.length > 0) {
        existingMoodBoards.value = existingDraft.moodBoards.map(board => ({
          id: board.id,
          url: board.imageUrl,
          name: board.fileName || 'Mood Board'
        }))
      }

      if (existingDraft.attachments && existingDraft.attachments.length > 0) {
        existingAttachments.value = existingDraft.attachments
      }

      conceptSketches.value = []
      moodBoards.value = []
    }
  } catch (err) {
    projectError.value = err.response?.data?.message || 'Failed to load project details'
    console.error('Failed to fetch project:', err)
  } finally {
    projectLoading.value = false
  }
})
</script>
