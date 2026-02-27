<template>
  <div class="min-h-screen bg-[#F4F5F7] py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button @click="router.back()" class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition">
        <ArrowLeft :size="20" />
        Back to Project
      </button>

      <div v-if="loading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded mb-6" />
      </div>

      <div v-else-if="error" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600 mb-4">{{ error }}</p>
        <button @click="fetchBid" class="text-[#7C4728] hover:underline">Try again</button>
      </div>

      <div v-else-if="currentBid" class="space-y-6">
        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-black mb-2">
                {{ currentBid.architectName || 'Architect' }}
              </h1>
              <p v-if="currentBid.architectCompany" class="text-gray-500">
                {{ currentBid.architectCompany }}
              </p>
              <p class="text-sm text-gray-400 mt-2">Submitted on {{ formatDate(currentBid.submittedAt) }}</p>
            </div>
            <BidStatusBadge :status="currentBid.status" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Proposed Cost</p>
              <p class="text-3xl font-bold text-[#7C4728]">{{ formatCurrency(currentBid.bidAmount) }}</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Timeline</p>
              <p class="text-3xl font-bold text-black">{{ currentBid.proposedTimelineDays }} days</p>
            </div>
          </div>
        </div>

        <div
          v-if="currentBid.details?.conceptStatement"
          class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
        >
          <h2 class="text-2xl font-bold text-black mb-4">Design Concept</h2>
          <p class="text-gray-700 leading-relaxed whitespace-pre-line">
            {{ currentBid.details.conceptStatement }}
          </p>
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <BidImageGallery :images="currentBid.facadeImages" title="Facade" empty-message="No facade images available" />
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <BidImageGallery
            :images="currentBid.interiorImages"
            title="Interior"
            empty-message="No interior images available"
          />
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <BidImageGallery
            :images="currentBid.massingImages"
            title="Massing"
            empty-message="No massing images available"
          />
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <BidImageGallery :images="currentBid.zoningImages" title="Zoning" empty-message="No zoning images available" />
        </div>

        <div
          v-if="hasRevisions"
          class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
        >
          <h2 class="text-2xl font-bold text-black mb-4">Revision Commitments</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div v-if="currentBid.details?.siteAnalysisRevisions != null" class="bg-gray-50 rounded-2xl p-4">
              <p class="text-xs text-gray-500 uppercase font-bold mb-1">Site Analysis & Planning</p>
              <p class="text-2xl font-bold text-black">{{ currentBid.details.siteAnalysisRevisions }}
                <span class="text-sm font-normal text-gray-500">revisions</span>
              </p>
            </div>
            <div v-if="currentBid.details?.designRevisions != null" class="bg-gray-50 rounded-2xl p-4">
              <p class="text-xs text-gray-500 uppercase font-bold mb-1">Design Phases</p>
              <p class="text-2xl font-bold text-black">{{ currentBid.details.designRevisions }}
                <span class="text-sm font-normal text-gray-500">revisions</span>
              </p>
            </div>
            <div v-if="currentBid.details?.permitsDocRevisions != null" class="bg-gray-50 rounded-2xl p-4">
              <p class="text-xs text-gray-500 uppercase font-bold mb-1">Permits & Documentation</p>
              <p class="text-2xl font-bold text-black">{{ currentBid.details.permitsDocRevisions }}
                <span class="text-sm font-normal text-gray-500">revisions</span>
              </p>
            </div>
            <div v-if="currentBid.details?.specializedServicesRevisions != null" class="bg-gray-50 rounded-2xl p-4">
              <p class="text-xs text-gray-500 uppercase font-bold mb-1">Specialized Services</p>
              <p class="text-2xl font-bold text-black">{{ currentBid.details.specializedServicesRevisions }}
                <span class="text-sm font-normal text-gray-500">revisions</span>
              </p>
            </div>
            <div v-if="currentBid.details?.constructionSupportRevisions != null" class="bg-gray-50 rounded-2xl p-4">
              <p class="text-xs text-gray-500 uppercase font-bold mb-1">Construction Support</p>
              <p class="text-2xl font-bold text-black">{{ currentBid.details.constructionSupportRevisions }}
                <span class="text-sm font-normal text-gray-500">revisions</span>
              </p>
            </div>
          </div>
        </div>

        <div
          v-if="currentBid.portfolioReferences && currentBid.portfolioReferences.length > 0"
          class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
        >
          <h2 class="text-2xl font-bold text-black mb-4">Related Portfolio Projects</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div
              v-for="portfolio in currentBid.portfolioReferences"
              :key="portfolio.id"
              class="bg-gray-50 rounded-2xl p-4 border border-gray-200 hover:border-[#C5A17A] transition"
            >
              <div v-if="portfolio.coverImage" class="aspect-video bg-gray-200 rounded-xl mb-3 overflow-hidden">
                <img :src="portfolio.coverImage" :alt="portfolio.projectName" class="w-full h-full object-cover" />
              </div>
              <p class="font-medium text-gray-900">{{ portfolio.projectName }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div class="flex gap-4">
            <button
              v-if="currentBid.status === 'PENDING'"
              @click="handleAcceptBid"
              class="flex-1 px-6 py-4 bg-[#7C4728] text-white rounded-full font-bold hover:bg-black transition shadow-md hover:shadow-lg flex items-center justify-center gap-2"
            >
              <Check :size="20" />
              Accept This Proposal
            </button>
            <button
              v-else-if="currentBid.status === 'ACCEPTED'"
              disabled
              class="flex-1 px-6 py-4 bg-green-600 text-white rounded-full font-bold flex items-center justify-center gap-2 cursor-not-allowed"
            >
              <Trophy :size="20" />
              Accepted
            </button>
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
import { ArrowLeft, Check, Trophy } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import BidImageGallery from '@/components/bid/BidImageGallery.vue'
import BidStatusBadge from '@/components/project/BidStatusBadge.vue'

const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()

const { currentBid, loading, error } = storeToRefs(bidsStore)

const hasRevisions = computed(() => {
  const d = currentBid.value?.details
  if (!d) return false
  return (
    d.siteAnalysisRevisions != null ||
    d.designRevisions != null ||
    d.permitsDocRevisions != null ||
    d.specializedServicesRevisions != null ||
    d.constructionSupportRevisions != null
  )
})

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const formatDate = dateString => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const fetchBid = async () => {
  try {
    await bidsStore.fetchBidById(route.params.bidId)
  } catch (err) {
    console.error('Failed to fetch bid details:', err)
  }
}

const handleAcceptBid = async () => {
  if (!confirm('Are you sure you want to accept this proposal?')) return

  try {
    await bidsStore.acceptBid(route.params.bidId)
    router.push(`/client/projects/${route.params.projectId}/finalization`)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept proposal')
  }
}

onMounted(() => {
  fetchBid()
})
</script>
