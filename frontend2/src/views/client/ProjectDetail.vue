<template>
  <div class="min-h-screen bg-[#F4F5F7] py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button @click="router.push({ name: 'ClientDashboard' })" class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition">
        <ArrowLeft :size="20" />
        Back to Projects
      </button>

      <div v-if="loading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded mb-6" />
      </div>

      <div v-else-if="error" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600 mb-4">{{ error }}</p>
        <button @click="fetchProject" class="text-[#7C4728] hover:underline">Try again</button>
      </div>

      <div v-else-if="currentProject" class="space-y-6">
        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-black mb-2">{{ currentProject.title }}</h1>
              <p class="text-gray-500">{{ currentProject.location }} • {{ currentProject.buildingType }}</p>
            </div>
            <ProjectStatusBadge :status="currentProject.status" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Lot Size</p>
              <p class="text-2xl font-bold text-black">{{ currentProject.lotSize }} m²</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Design Budget</p>
              <p class="text-2xl font-bold text-black">{{ formatCurrency(currentProject.designBudget) }}</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Proposals</p>
              <p class="text-2xl font-bold text-[#7C4728]">{{ proposalCount }}</p>
            </div>
          </div>

          <div class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">Description</h2>
            <p class="text-gray-700 leading-relaxed">{{ currentProject.description }}</p>
          </div>

          <div v-if="currentProject.deliverables && currentProject.deliverables.length > 0">
            <h2 class="text-lg font-bold text-black mb-3">Required Deliverables</h2>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="deliverable in currentProject.deliverables"
                :key="deliverable"
                class="bg-[#F5E6D3] text-[#7C4728] px-4 py-2 rounded-full text-sm font-medium border border-[#C5A17A]/20"
              >
                {{ deliverable.replace(/_/g, ' ') }}
              </span>
            </div>
          </div>

          <div v-if="currentProject.biddingDeadline && currentProject.status === 'OPEN'" class="mt-8 pt-6 border-t border-gray-100">
            <BiddingCountdown :deadline="currentProject.biddingDeadline" size="md" />
          </div>
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <h2 class="text-2xl font-bold text-black mb-6">Received Proposals ({{ proposalCount }})</h2>

          <div v-if="proposalCount === 0" class="text-center py-12">
            <FileText :size="64" class="text-gray-300 mx-auto mb-4" />
            <h3 class="text-xl font-bold text-gray-900 mb-2">No proposals yet</h3>
            <p class="text-gray-500">Architects will submit their proposals soon</p>
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="proposal in currentProject.bids"
              :key="proposal.id"
              class="bg-gray-50 rounded-2xl p-6 border border-gray-200"
            >
              <div class="flex justify-between items-start mb-4">
                <div>
                  <h3 class="text-lg font-bold text-black">{{ proposal.architectName || 'Architect' }}</h3>
                  <p v-if="proposal.firmName" class="text-sm text-gray-500">{{ proposal.firmName }}</p>
                </div>
                <span v-if="proposal.status === 'ACCEPTED'" class="bg-[#7C4728] text-white px-4 py-1.5 rounded-full text-xs font-bold flex items-center gap-2">
                  <Trophy :size="14" />
                  Winner
                </span>
              </div>

              <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                <div>
                  <p class="text-xs text-gray-500 mb-1">Proposed Cost</p>
                  <p class="font-bold text-gray-900">{{ formatCurrency(proposal.proposedCost) }}</p>
                </div>
                <div>
                  <p class="text-xs text-gray-500 mb-1">Duration</p>
                  <p class="font-bold text-gray-900">{{ proposal.estimatedDuration }} days</p>
                </div>
                <div>
                  <p class="text-xs text-gray-500 mb-1">Status</p>
                  <p class="font-bold text-gray-900">{{ proposal.status }}</p>
                </div>
              </div>

              <p v-if="proposal.conceptDescription" class="text-sm text-gray-700 mb-4">
                {{ proposal.conceptDescription }}
              </p>

              <button
                v-if="proposal.status === 'PENDING' && currentProject.status === 'OPEN'"
                @click="handleAcceptBid(proposal.id)"
                class="bg-[#7C4728] hover:bg-black text-white px-6 py-2 rounded-full text-sm font-medium transition"
              >
                Accept Proposal
              </button>
            </div>
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
import { ArrowLeft, FileText, Trophy } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'

const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const { currentProject, loading, error } = storeToRefs(projectsStore)

const proposalCount = computed(() => currentProject.value?.bids?.length || 0)

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const fetchProject = async () => {
  try {
    await projectsStore.fetchProjectById(route.params.id)
  } catch (err) {
    console.error('Failed to fetch project:', err)
  }
}

const handleAcceptBid = async bidId => {
  if (!confirm('Are you sure you want to accept this proposal?')) return

  try {
    await bidsStore.acceptBid(route.params.id, bidId)
    await fetchProject()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept proposal')
  }
}

onMounted(() => {
  fetchProject()
})
</script>
