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
          <BidImageGallery
            :images="currentBid.conceptSketches"
            title="Concept Sketches"
            empty-message="No concept sketches available"
          />
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <BidImageGallery
            :images="currentBid.moodBoards"
            title="Mood Boards"
            empty-message="No mood boards available"
          />
        </div>

        <div
          v-if="currentBid.attachments && currentBid.attachments.length > 0"
          class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
        >
          <h2 class="text-2xl font-bold text-black mb-4">Proposal Documents</h2>
          <div class="space-y-3">
            <div
              v-for="attachment in currentBid.attachments"
              :key="attachment.id"
              class="flex items-center justify-between p-4 bg-gray-50 rounded-2xl border border-gray-200"
            >
              <div class="flex items-center gap-3">
                <FileText :size="24" class="text-[#7C4728]" />
                <div>
                  <p class="font-medium text-gray-900">{{ attachment.fileName }}</p>
                  <p class="text-sm text-gray-500">{{ formatFileSize(attachment.fileSize) }}</p>
                </div>
              </div>
              <button
                @click="openPDF(attachment.fileUrl)"
                class="px-4 py-2 bg-[#7C4728] text-white rounded-full text-sm font-medium hover:bg-black transition"
              >
                View PDF
              </button>
            </div>
          </div>
        </div>

        <div
          v-if="currentBid.portfolios && currentBid.portfolios.length > 0"
          class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
        >
          <h2 class="text-2xl font-bold text-black mb-4">Related Portfolio Projects</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div
              v-for="portfolio in currentBid.portfolios"
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, FileText, Check, Trophy } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import BidImageGallery from '@/components/bid/BidImageGallery.vue'
import BidStatusBadge from '@/components/project/BidStatusBadge.vue'

const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()

const { currentBid, loading, error } = storeToRefs(bidsStore)

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const formatFileSize = bytes => {
  if (!bytes) return 'N/A'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const formatDate = dateString => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const openPDF = url => {
  window.open(url, '_blank')
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
    await bidsStore.acceptBid(route.params.projectId, route.params.bidId)
    await fetchBid()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept proposal')
  }
}

onMounted(() => {
  fetchBid()
})
</script>
