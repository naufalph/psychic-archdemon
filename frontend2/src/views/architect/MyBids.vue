<template>
  <div class="min-h-screen bg-[#F4F5F7]">
    <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px); background-size: 50px 50px"></div>

    <div class="relative z-10 max-w-7xl mx-auto px-6 py-12">
      <button @click="router.push({ name: 'ArchitectDashboard' })" class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition">
        <ArrowLeft :size="20" />
        Back to Dashboard
      </button>

      <div class="mb-8">
        <h1 class="text-4xl font-bold text-black mb-2">My Bids</h1>
        <p class="text-gray-500">Track your submitted proposals</p>
      </div>

      <div v-if="loading" class="space-y-4">
        <div v-for="n in 3" :key="n" class="bg-white rounded-3xl border border-gray-200 p-8 animate-pulse">
          <div class="h-6 bg-gray-200 rounded w-1/2 mb-4" />
          <div class="h-4 bg-gray-200 rounded w-1/4" />
        </div>
      </div>

      <div v-else-if="myBids.length === 0" class="text-center py-20 bg-white rounded-3xl border border-gray-200">
        <FileText :size="64" class="text-gray-300 mx-auto mb-4" />
        <h3 class="text-xl font-bold text-gray-900 mb-2">No bids yet</h3>
        <p class="text-gray-500 mb-6">Submit your first proposal to get started</p>
        <router-link :to="{ name: 'OpportunityList' }">
          <button class="bg-[#7C4728] hover:bg-black text-white px-6 py-3 rounded-full font-medium transition">
            Browse Opportunities
          </button>
        </router-link>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="bid in myBids"
          :key="bid.id"
          class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
        >
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="text-xl font-bold text-black">{{ bid.project?.title || 'Project' }}</h3>
              <p class="text-sm text-gray-500">{{ bid.project?.location }}</p>
            </div>
            <span :class="statusBadgeClass(bid.status)">
              {{ bid.status }}
            </span>
          </div>

          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
            <div>
              <p class="text-xs text-gray-500 mb-1">Proposed Cost</p>
              <p class="font-bold text-gray-900">IDR {{ formatNumber(bid.proposedCost) }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 mb-1">Duration</p>
              <p class="font-bold text-gray-900">{{ bid.estimatedDuration }} days</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 mb-1">Submitted</p>
              <p class="font-bold text-gray-900">{{ formatDate(bid.submittedAt) }}</p>
            </div>
          </div>

          <p v-if="bid.conceptDescription" class="text-sm text-gray-700">
            {{ bid.conceptDescription }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, FileText } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'

const router = useRouter()
const bidsStore = useBidsStore()

const { myBids, loading } = storeToRefs(bidsStore)

const statusBadgeClass = status => {
  const base = 'px-4 py-1.5 rounded-full text-xs font-bold'
  const variants = {
    PENDING: `${base} bg-yellow-100 text-yellow-700`,
    ACCEPTED: `${base} bg-green-100 text-green-700`,
    REJECTED: `${base} bg-red-100 text-red-700`
  }
  return variants[status] || `${base} bg-gray-100 text-gray-700`
}

const formatNumber = num => {
  return new Intl.NumberFormat('id-ID').format(num)
}

const formatDate = dateString => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('id-ID')
}

onMounted(async () => {
  try {
    await bidsStore.fetchMyBids()
  } catch (err) {
    console.error('Failed to fetch bids:', err)
  }
})
</script>
