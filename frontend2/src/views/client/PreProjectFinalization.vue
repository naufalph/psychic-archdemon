<template>
  <div class="min-h-screen bg-[#F4F5F7]">
    <div v-if="loading && !bid" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <div class="w-10 h-10 border-2 border-[#C5A17A] border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p class="text-gray-500">Loading negotiation...</p>
      </div>
    </div>

    <div v-else-if="error" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button @click="loadData" class="text-[#7C4728] hover:underline">Try again</button>
      </div>
    </div>

    <div v-else-if="bid">
      <!-- Header -->
      <div class="bg-white border-b border-gray-200 px-6 py-4">
        <div class="max-w-7xl mx-auto flex items-center justify-between">
          <div class="flex items-center gap-4">
            <button @click="router.back()" class="text-gray-500 hover:text-black transition">
              <ArrowLeft :size="20" />
            </button>
            <div>
              <div class="flex items-center gap-2">
                <span class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-700">
                  <Clock :size="12" />
                  Finalization
                </span>
              </div>
              <h1 class="text-lg font-bold text-black mt-1">{{ project?.title || 'Project Finalization' }}</h1>
            </div>
          </div>
          <div v-if="project?.biddingDeadline" class="text-right text-sm text-gray-500">
            <p class="text-xs font-bold uppercase text-gray-400">Bidding Deadline</p>
            <p>{{ formatDate(project.biddingDeadline) }}</p>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="max-w-7xl mx-auto px-6 py-6">
        <div class="grid grid-cols-1 lg:grid-cols-5 gap-6 h-[calc(100vh-160px)]">

          <!-- LEFT: Bid Summary (locked) -->
          <div class="lg:col-span-3 overflow-y-auto space-y-4 pr-2">

            <!-- Architect + Key Terms -->
            <div class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
              <div class="flex items-start justify-between mb-4">
                <div>
                  <h2 class="text-xl font-bold text-black">{{ bid.architectName }}</h2>
                  <p v-if="bid.architectCompany" class="text-gray-500 text-sm">{{ bid.architectCompany }}</p>
                </div>
                <span class="inline-flex items-center gap-1 px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-bold">
                  <CheckCircle :size="12" />
                  Winning Bid
                </span>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div class="bg-[#FDF6EE] rounded-2xl p-4">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">Proposed Price</p>
                  <p class="text-2xl font-bold text-[#7C4728]">{{ formatCurrency(bid.bidAmount) }}</p>
                </div>
                <div class="bg-gray-50 rounded-2xl p-4">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">Timeline</p>
                  <p class="text-2xl font-bold text-black">{{ bid.proposedTimelineDays }} <span class="text-sm font-normal text-gray-500">days</span></p>
                </div>
              </div>
            </div>

            <!-- Deliverables -->
            <div v-if="bid.details?.deliverables?.length" class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
              <h3 class="font-bold text-black mb-3">Deliverables</h3>
              <ul class="space-y-2">
                <li
                  v-for="item in bid.details.deliverables"
                  :key="item"
                  class="flex items-center gap-2 text-sm text-gray-700"
                >
                  <div class="w-1.5 h-1.5 rounded-full bg-[#C5A17A] flex-shrink-0" />
                  {{ item }}
                </li>
              </ul>
            </div>

            <!-- Revision Commitments -->
            <div v-if="hasRevisions" class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
              <h3 class="font-bold text-black mb-3">Revision Commitments</h3>
              <div class="grid grid-cols-2 gap-3">
                <div v-if="bid.details?.siteAnalysisRevisions != null" class="bg-gray-50 rounded-xl p-3">
                  <p class="text-xs text-gray-500 font-bold mb-0.5">Site Analysis</p>
                  <p class="text-lg font-bold text-black">{{ bid.details.siteAnalysisRevisions }} <span class="text-xs font-normal text-gray-500">rev</span></p>
                </div>
                <div v-if="bid.details?.designRevisions != null" class="bg-gray-50 rounded-xl p-3">
                  <p class="text-xs text-gray-500 font-bold mb-0.5">Design Phases</p>
                  <p class="text-lg font-bold text-black">{{ bid.details.designRevisions }} <span class="text-xs font-normal text-gray-500">rev</span></p>
                </div>
                <div v-if="bid.details?.permitsDocRevisions != null" class="bg-gray-50 rounded-xl p-3">
                  <p class="text-xs text-gray-500 font-bold mb-0.5">Permits & Docs</p>
                  <p class="text-lg font-bold text-black">{{ bid.details.permitsDocRevisions }} <span class="text-xs font-normal text-gray-500">rev</span></p>
                </div>
                <div v-if="bid.details?.specializedServicesRevisions != null" class="bg-gray-50 rounded-xl p-3">
                  <p class="text-xs text-gray-500 font-bold mb-0.5">Specialized</p>
                  <p class="text-lg font-bold text-black">{{ bid.details.specializedServicesRevisions }} <span class="text-xs font-normal text-gray-500">rev</span></p>
                </div>
                <div v-if="bid.details?.constructionSupportRevisions != null" class="bg-gray-50 rounded-xl p-3">
                  <p class="text-xs text-gray-500 font-bold mb-0.5">Construction</p>
                  <p class="text-lg font-bold text-black">{{ bid.details.constructionSupportRevisions }} <span class="text-xs font-normal text-gray-500">rev</span></p>
                </div>
              </div>
            </div>

            <!-- Concept Statement -->
            <div v-if="bid.details?.conceptStatement" class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
              <h3 class="font-bold text-black mb-3">Design Concept</h3>
              <p class="text-gray-700 text-sm leading-relaxed whitespace-pre-line">{{ bid.details.conceptStatement }}</p>
            </div>

            <!-- Portfolio References -->
            <div v-if="bid.portfolioReferences?.length" class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
              <h3 class="font-bold text-black mb-3">Portfolio References</h3>
              <div class="grid grid-cols-3 gap-3">
                <div
                  v-for="porto in bid.portfolioReferences"
                  :key="porto.id"
                  class="aspect-square bg-gray-100 rounded-2xl overflow-hidden"
                >
                  <img
                    v-if="porto.images?.[0]?.mediumUrl"
                    :src="porto.images[0].mediumUrl"
                    :alt="porto.title"
                    class="w-full h-full object-cover"
                  />
                  <div v-else class="w-full h-full flex items-center justify-center text-gray-400 text-xs text-center p-2">
                    {{ porto.title }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- RIGHT: Chat + CTAs -->
          <div class="lg:col-span-2 flex flex-col gap-4">

            <!-- Chat Panel -->
            <div class="flex-1 bg-white rounded-3xl border border-gray-200 shadow-soft overflow-hidden flex flex-col min-h-0">
              <div class="px-5 py-4 border-b border-gray-100">
                <h3 class="font-bold text-black">Discussion</h3>
                <p class="text-xs text-gray-500 mt-0.5">Chat with the architect about the terms</p>
              </div>

              <div class="flex-1 min-h-0" v-if="bid.conversationId">
                <ChatPanel :conversation-id="bid.conversationId" class="h-full" />
              </div>
              <div v-else class="flex-1 flex items-center justify-center text-gray-400 text-sm">
                Chat not available yet
              </div>
            </div>

            <!-- Action Buttons (Client only) -->
            <div v-if="isClient" class="bg-white rounded-3xl border border-gray-200 p-5 shadow-soft">
              <p class="text-xs text-gray-500 mb-4 leading-relaxed">
                Review the bid terms and discuss with the architect before confirming.
              </p>
              <div class="space-y-3">
                <button
                  @click="handleConfirm"
                  :disabled="actionLoading"
                  class="w-full px-5 py-3.5 bg-[#7C4728] text-white rounded-full font-bold hover:bg-black transition flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <CheckCircle :size="18" />
                  {{ actionLoading === 'confirm' ? 'Confirming...' : 'Confirm & Proceed to Payment' }}
                </button>
                <button
                  @click="showRejectDialog = true"
                  :disabled="actionLoading"
                  class="w-full px-5 py-3.5 bg-white text-red-600 border border-red-200 rounded-full font-bold hover:bg-red-50 transition flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <XCircle :size="18" />
                  Reject Terms & Reopen Bidding
                </button>
              </div>
            </div>

            <!-- Architect view -->
            <div v-else class="bg-amber-50 rounded-3xl border border-amber-200 p-5">
              <div class="flex items-center gap-2 text-amber-700">
                <Clock :size="16" />
                <p class="text-sm font-bold">Awaiting client confirmation</p>
              </div>
              <p class="text-xs text-amber-600 mt-1">The client is reviewing the terms and may reach out via chat.</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Reject Confirmation Dialog -->
    <div v-if="showRejectDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4">
      <div class="bg-white rounded-3xl p-8 max-w-md w-full shadow-2xl">
        <h3 class="text-xl font-bold text-black mb-2">Reject Terms & Reopen Bidding?</h3>
        <p class="text-gray-600 text-sm mb-6">
          This will reject the current bid, refund the architect's token, and reopen the project for new bids. This action cannot be undone.
        </p>
        <div class="flex gap-3">
          <button
            @click="showRejectDialog = false"
            class="flex-1 px-5 py-3 border border-gray-200 rounded-full font-bold text-gray-700 hover:bg-gray-50 transition"
          >
            Cancel
          </button>
          <button
            @click="handleReject"
            :disabled="actionLoading === 'reject'"
            class="flex-1 px-5 py-3 bg-red-600 text-white rounded-full font-bold hover:bg-red-700 transition disabled:opacity-50"
          >
            {{ actionLoading === 'reject' ? 'Rejecting...' : 'Yes, Reject & Reopen' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Clock, CheckCircle, XCircle } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import { useAuthStore } from '@/stores/auth'
import { projectAPI } from '@/services/api'
import ChatPanel from '@/components/chat/ChatPanel.vue'

const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()
const authStore = useAuthStore()

const bid = ref(null)
const project = ref(null)
const loading = ref(false)
const error = ref(null)
const actionLoading = ref(null)
const showRejectDialog = ref(false)

const isClient = computed(() => authStore.hasRole('CLIENT'))

const hasRevisions = computed(() => {
  const d = bid.value?.details
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
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const loadData = async () => {
  loading.value = true
  error.value = null
  try {
    const projectId = route.params.projectId

    if (isClient.value) {
      await bidsStore.fetchProjectBids(projectId)
      const acceptedBid = bidsStore.projectBids.find(b => b.status === 'ACCEPTED')

      if (!acceptedBid) {
        error.value = 'No accepted bid found for this project'
        return
      }

      bid.value = acceptedBid

      const response = await projectAPI.getById(projectId)
      project.value = response.data.data
    } else {
      // Architect view: find their accepted bid for this project
      await bidsStore.fetchMyBids()
      const acceptedBid = bidsStore.myBids.find(
        b => b.projectId === Number(projectId) && b.status === 'ACCEPTED'
      )

      if (!acceptedBid) {
        error.value = 'No accepted bid found for this project'
        return
      }

      bid.value = acceptedBid

      const response = await projectAPI.getProjectForArchitect(projectId)
      project.value = response.data.data
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load negotiation data'
    console.error('Failed to load negotiation data:', err)
  } finally {
    loading.value = false
  }
}

const handleConfirm = async () => {
  if (!confirm('Confirm the terms and proceed to payment?')) return
  actionLoading.value = 'confirm'
  try {
    await projectAPI.confirmNegotiation(route.params.projectId)
    router.push(`/client/projects/${route.params.projectId}`)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to confirm terms')
  } finally {
    actionLoading.value = null
  }
}

const handleReject = async () => {
  actionLoading.value = 'reject'
  try {
    await projectAPI.rejectNegotiation(route.params.projectId)
    showRejectDialog.value = false
    router.push({
      path: `/client/projects/${route.params.projectId}`,
      query: { toast: 'Bidding reopened successfully' }
    })
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to reject terms')
    showRejectDialog.value = false
  } finally {
    actionLoading.value = null
  }
}

onMounted(() => {
  loadData()
})
</script>
