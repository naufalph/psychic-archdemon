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

      <div v-if="loading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded mb-6" />
      </div>

      <div v-else-if="error" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <div class="flex flex-col items-center gap-4">
          <AlertCircle :size="64" class="text-red-400" />
          <p class="text-red-600 mb-2 text-lg font-medium">{{ error }}</p>
          <button @click="router.push({ name: 'OpportunityList' })" class="text-[#7C4728] hover:underline font-medium">
            Back to Opportunities
          </button>
        </div>
      </div>

      <div v-else-if="project" class="space-y-6">
        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-black mb-2">{{ project.title }}</h1>
              <p class="text-gray-500">{{ project.location }} • {{ project.buildingType }}</p>
            </div>
            <ProjectStatusBadge :status="project.status" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Design Budget Range</p>
              <p class="text-2xl font-bold text-black">
                {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
              </p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Build Area</p>
              <p class="text-2xl font-bold text-black">{{ project.estimatedBuildArea }} m²</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Floors</p>
              <p class="text-2xl font-bold text-black">{{ project.numberOfFloors }}</p>
            </div>
          </div>

          <div class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">Scope of Work</h2>
            <p class="text-gray-700 leading-relaxed">{{ project.scopeOfWork }}</p>
          </div>

          <div v-if="project.deliverables && project.deliverables.length > 0" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">Deliverables</h2>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="deliverable in project.deliverables"
                :key="deliverable"
                class="bg-[#F5E6D3] text-[#7C4728] px-4 py-2 rounded-full text-sm font-medium border border-[#C5A17A]/20"
              >
                {{ deliverable.replace(/_/g, ' ') }}
              </span>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Category</p>
              <p class="text-base text-gray-900">{{ project.category }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Expected Start Date</p>
              <p class="text-base text-gray-900">{{ formatDate(project.expectedStartDate) }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Land Ownership</p>
              <p class="text-base text-gray-900">{{ project.hasOwnedLand ? 'Yes' : 'No' }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Legal Documentation</p>
              <p class="text-base text-gray-900">{{ project.hasLegalDocuments ? 'Complete' : 'Incomplete' }}</p>
            </div>
          </div>

          <div v-if="project.designPreferences" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">Design Preferences</h2>
            <p class="text-gray-700 leading-relaxed">{{ project.designPreferences }}</p>
          </div>

          <div v-if="project.biddingDeadline" class="pt-6 border-t border-gray-100 mb-8">
            <BiddingCountdown :deadline="project.biddingDeadline" size="lg" />
          </div>

          <div v-if="existingBid" class="space-y-4">
            <div
              class="bg-blue-50 border border-blue-200 rounded-2xl p-6 flex items-start gap-4"
              v-if="existingBid.status === 'PENDING'"
            >
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                <Send :size="24" class="text-blue-600" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-blue-900 mb-1">Proposal Submitted</p>
                <p class="text-sm text-blue-700">Your bid is pending review by the client.</p>
              </div>
            </div>

            <div
              class="bg-yellow-50 border border-yellow-200 rounded-2xl p-6 flex items-start gap-4"
              v-if="existingBid.status === 'DRAFT'"
            >
              <div class="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center flex-shrink-0">
                <FileText :size="24" class="text-yellow-600" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-yellow-900 mb-1">Draft Saved</p>
                <p class="text-sm text-yellow-700">You have an unfinished proposal for this project.</p>
              </div>
            </div>

            <button
              v-if="existingBid.status === 'DRAFT'"
              @click="goToProposal"
              class="w-full bg-[#7C4728] text-white py-4 px-6 rounded-2xl hover:bg-[#5a3419] transition flex items-center justify-center gap-3 text-lg font-bold"
            >
              <Send :size="24" />
              Continue Draft
            </button>

            <button
              v-if="existingBid.status === 'PENDING'"
              disabled
              class="w-full bg-gray-300 text-gray-500 py-4 px-6 rounded-2xl cursor-not-allowed flex items-center justify-center gap-3 text-lg font-bold"
            >
              <Send :size="24" />
              Proposal Submitted
            </button>
          </div>

          <button
            v-else
            @click="goToProposal"
            :disabled="project.status !== 'OPEN'"
            class="w-full bg-[#7C4728] text-white py-4 px-6 rounded-2xl hover:bg-[#5a3419] transition flex items-center justify-center gap-3 text-lg font-bold disabled:bg-gray-300 disabled:cursor-not-allowed disabled:text-gray-500"
          >
            <Send :size="24" />
            Submit Proposal
          </button>
        </div>

        <div
          v-if="project.files && project.files.length > 0"
          class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
        >
          <h2 class="text-2xl font-bold text-black mb-6">Project Files</h2>
          <div class="space-y-3">
            <a
              v-for="file in project.files"
              :key="file.id"
              :href="file.fileUrl"
              target="_blank"
              class="flex items-center gap-3 p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition border border-gray-200"
            >
              <FileText :size="24" class="text-[#7C4728]" />
              <div class="flex-1">
                <p class="font-medium text-gray-900">{{ file.fileName }}</p>
                <p class="text-sm text-gray-500">{{ file.fileType }}</p>
              </div>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Send, FileText, AlertCircle } from 'lucide-vue-next'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'

const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const project = ref(null)
const loading = ref(false)
const error = ref(null)
const existingBid = ref(null)

const formatCurrency = value => {
  if (!value) return 'N/A'
  const millions = value / 1000000
  if (millions >= 1) {
    return `Rp ${millions.toFixed(0)}M`
  }
  const thousands = value / 1000
  return `Rp ${thousands.toFixed(0)}K`
}

const formatDate = dateString => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
}

const goToProposal = () => {
  router.push({
    name: 'ProposalCreate',
    params: { projectId: route.params.projectId }
  })
}

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    project.value = await projectsStore.fetchProjectForArchitect(route.params.projectId)

    await bidsStore.fetchMyBids()
    existingBid.value = bidsStore.myBids.find(bid => bid.projectId === parseInt(route.params.projectId))
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load project details'
    console.error('Failed to fetch project:', err)
  } finally {
    loading.value = false
  }
})
</script>
