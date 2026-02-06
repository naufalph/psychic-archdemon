<template>
  <div class="min-h-screen bg-[#F4F5F7]">
    <div
      class="absolute inset-0 opacity-[0.03] pointer-events-none"
      style="
        background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px);
        background-size: 50px 50px;
      "
    ></div>

    <div class="relative z-10 max-w-7xl mx-auto px-6 py-12">
      <div class="mb-8">
        <button
          @click="router.push({ name: 'ArchitectDashboard' })"
          class="mb-4 flex items-center gap-2 text-gray-600 hover:text-black transition"
        >
          <ArrowLeft :size="20" />
          Back to Dashboard
        </button>
        <h1 class="text-4xl font-bold text-black mb-2">Available Opportunities</h1>
        <p class="text-gray-500">Browse open projects and submit your proposals</p>
      </div>

      <div v-if="quota" class="bg-white rounded-2xl border border-gray-200 p-6 mb-8 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-[#F5E6D3] rounded-full flex items-center justify-center">
            <Coins :size="24" class="text-[#7C4728]" />
          </div>
          <div>
            <p class="text-sm text-gray-500">Bid Tokens Available</p>
            <p class="text-2xl font-bold text-[#7C4728]">{{ quota.tokensRemaining }} / {{ quota.tokensAllocated }}</p>
          </div>
        </div>
        <button class="bg-[#7C4728] hover:bg-black text-white px-6 py-2 rounded-full text-sm font-medium transition">
          Purchase Tokens
        </button>
      </div>

      <div v-if="loading" class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        <div v-for="n in 6" :key="n" class="bg-white rounded-3xl border border-gray-200 p-8 animate-pulse">
          <div class="h-6 bg-gray-200 rounded w-3/4 mb-4" />
          <div class="h-4 bg-gray-200 rounded w-1/2 mb-6" />
          <div class="h-20 bg-gray-200 rounded" />
        </div>
      </div>

      <div v-else-if="openProjects.length === 0" class="text-center py-20">
        <Search :size="64" class="text-gray-300 mx-auto mb-4" />
        <h3 class="text-xl font-bold text-gray-900 mb-2">No open projects</h3>
        <p class="text-gray-500">Check back later for new opportunities</p>
      </div>

      <div v-else class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        <ProjectCard
          v-for="project in openProjects"
          :key="project.id"
          :project="project"
          variant="architect"
          :show-proposal-count="false"
          @submit-proposal="handleSubmitProposal"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, Search, Coins } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import ProjectCard from '@/components/project/ProjectCard.vue'

const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const { openProjects, loading } = storeToRefs(projectsStore)
const { quota } = storeToRefs(bidsStore)

const handleSubmitProposal = projectId => {
  router.push({ name: 'ProposalCreate', params: { projectId } })
}

onMounted(async () => {
  try {
    await Promise.all([projectsStore.fetchOpenProjects(), bidsStore.fetchQuota()])
  } catch (err) {
    console.error('Failed to fetch data:', err)
  }
})
</script>
