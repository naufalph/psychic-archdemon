<template>
  <div class="min-h-screen bg-[#F4F5F7]">
    <div v-if="loading && !bid" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <div class="w-10 h-10 border-2 border-[#C5A17A] border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p class="text-gray-500">{{ t.finalization?.loading }}</p>
      </div>
    </div>

    <div v-else-if="error" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button @click="loadData" class="text-[#7C4728] hover:underline">{{ t.finalization?.tryAgain }}</button>
      </div>
    </div>

    <div v-else-if="bid">
      <!-- Header -->
      <div class="bg-white border-b border-gray-200 px-6 py-4">
        <div class="max-w-7xl mx-auto flex items-center justify-between">
          <div class="flex items-center gap-4">
            <button @click="router.push('/architect/bids')" class="text-gray-500 hover:text-black transition">
              <ArrowLeft :size="20" />
            </button>
            <div>
              <div class="flex items-center gap-2">
                <span
                  class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-700"
                >
                  <Clock :size="12" />
                  {{ t.finalization?.badge }}
                </span>
              </div>
              <h1 class="text-lg font-bold text-black mt-1">{{ project?.title || 'Project Finalization' }}</h1>
            </div>
          </div>
          <div v-if="project?.biddingDeadline" class="text-right text-sm text-gray-500">
            <p class="text-xs font-bold uppercase text-gray-400">{{ t.finalization?.biddingDeadline }}</p>
            <p>{{ formatDate(project.biddingDeadline) }}</p>
          </div>
        </div>
      </div>

      <!-- Both confirmed banner -->
      <div
        v-if="project?.clientConfirmed && project?.architectConfirmed"
        class="bg-green-50 border-b border-green-200 px-6 py-3"
      >
        <div class="max-w-7xl mx-auto flex items-center gap-2 text-green-700 text-sm font-bold">
          <CheckCircle :size="16" />
          {{ t.finalization?.bothConfirmed }}
        </div>
      </div>

      <!-- Main Content -->
      <div class="max-w-7xl mx-auto px-6 py-6">
        <div class="grid grid-cols-1 lg:grid-cols-5 gap-6 h-[calc(100vh-160px)]">
          <!-- LEFT: Bid Summary -->
          <div class="lg:col-span-3 overflow-y-auto space-y-4 pr-2">
            <!-- Key Terms -->
            <div class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
              <div class="flex items-start justify-between mb-4">
                <div>
                  <h2 class="text-xl font-bold text-black">{{ bid.architectName }}</h2>
                  <p v-if="bid.architectCompany" class="text-gray-500 text-sm">{{ bid.architectCompany }}</p>
                </div>
                <span
                  class="inline-flex items-center gap-1 px-3 py-1 bg-amber-100 text-amber-700 rounded-full text-xs font-bold"
                >
                  <CheckCircle :size="12" />
                  {{ t.finalization?.yourProposal }}
                </span>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div class="bg-[#FDF6EE] rounded-2xl p-4">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.finalization?.proposedPrice }}</p>
                  <p class="text-2xl font-bold text-[#7C4728]">{{ formatCurrency(bid.bidAmount) }}</p>
                </div>
                <div class="bg-gray-50 rounded-2xl p-4">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.finalization?.timeline }}</p>
                  <p class="text-2xl font-bold text-black">
                    {{ bid.proposedTimelineDays }}
                    <span class="text-sm font-normal text-gray-500">{{ t.finalization?.days }}</span>
                  </p>
                </div>
              </div>
            </div>

            <!-- Payment Schedule -->
            <div
              v-if="bid.details?.phases?.length"
              class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft"
            >
              <h3 class="font-bold text-black mb-4">{{ t.finalization?.paymentSchedule || 'Payment Schedule' }}</h3>
              <div class="space-y-3">
                <div
                  v-for="phase in bid.details.phases"
                  :key="phase.phaseNumber"
                  class="rounded-2xl border border-gray-100 p-4 bg-[#FDF6EE]"
                >
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-[#7C4728] text-white">
                        Phase {{ phase.phaseNumber }}
                      </span>
                      <span class="text-sm font-bold text-black">{{ phase.title || `Phase ${phase.phaseNumber}` }}</span>
                    </div>
                    <span class="text-sm font-bold text-[#7C4728]">{{ formatCurrency(phase.amount) }}</span>
                  </div>
                  <div v-if="phase.deliverables?.length" class="flex flex-wrap gap-1 mb-2">
                    <span
                      v-for="d in phase.deliverables"
                      :key="d"
                      class="text-xs px-2 py-0.5 bg-white border border-gray-200 rounded-full text-gray-600"
                    >{{ d.replace(/_/g, ' ') }}</span>
                  </div>
                  <p v-if="phase.revisionRounds != null" class="text-xs text-gray-500">
                    {{ phase.revisionRounds }} revision round{{ phase.revisionRounds !== 1 ? 's' : '' }}
                  </p>
                </div>
              </div>
              <div class="mt-3 pt-3 border-t border-gray-100 flex justify-between text-sm">
                <span class="text-gray-500 font-bold">Total</span>
                <span class="font-bold text-[#7C4728]">{{ formatCurrency(bid.bidAmount) }}</span>
              </div>
            </div>

            <!-- Concept Statement -->
            <div
              v-if="bid.details?.conceptStatement"
              class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft"
            >
              <h3 class="font-bold text-black mb-3">{{ t.finalization?.designConcept }}</h3>
              <p class="text-gray-700 text-sm leading-relaxed whitespace-pre-line">
                {{ bid.details.conceptStatement }}
              </p>
            </div>

            <!-- Portfolio References -->
            <div
              v-if="bid.portfolioReferences?.length"
              class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft"
            >
              <h3 class="font-bold text-black mb-3">{{ t.finalization?.portfolioRefs }}</h3>
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
                  <div
                    v-else
                    class="w-full h-full flex items-center justify-center text-gray-400 text-xs text-center p-2"
                  >
                    {{ porto.title }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- RIGHT: Chat + Action Panel -->
          <div class="lg:col-span-2 flex flex-col gap-4">
            <!-- Chat Panel -->
            <div
              class="flex-1 bg-white rounded-3xl border border-gray-200 shadow-soft overflow-hidden flex flex-col min-h-0"
            >
              <div class="px-5 py-4 border-b border-gray-100">
                <div class="flex items-center gap-2">
                  <h3 class="font-bold text-black">{{ t.finalization?.discussion }}</h3>
                  <span v-if="itSupportRequested" class="text-xs text-[#7C4728] font-medium">· IT Support invited</span>
                </div>
                <p class="text-xs text-gray-500 mt-0.5">{{ t.finalization?.discussionSubtitle }}</p>
              </div>

              <div class="flex-1 min-h-0" v-if="bid.conversationId">
                <ChatPanel :conversation-id="bid.conversationId" class="h-full" />
              </div>
              <div v-else class="flex-1 flex items-center justify-center text-gray-400 text-sm">
                {{ t.finalization?.chatUnavailable }}
              </div>
            </div>

            <div class="text-center">
              <button
                @click="openSupportChat"
                :disabled="supportLoading || itSupportRequested"
                class="text-xs text-gray-400 hover:text-gray-600 underline disabled:cursor-not-allowed"
              >
                {{ supportLoading ? 'Opening...' : itSupportRequested ? 'IT Support invited' : 'Request IT Support' }}
              </button>
            </div>

            <!-- Architect Action Panel -->
            <div class="bg-white rounded-3xl border border-gray-200 p-5 shadow-soft">
              <div v-if="project?.architectConfirmed" class="flex items-center gap-2 text-green-700">
                <CheckCircle :size="16" />
                <div>
                  <p class="text-sm font-bold">{{ t.finalization?.confirmed }}</p>
                  <p class="text-xs text-gray-500">
                    {{
                      project?.clientConfirmed ? t.finalization?.clientAlsoConfirmed : t.finalization?.awaitingClient
                    }}
                  </p>
                </div>
              </div>
              <div v-else>
                <p class="text-xs text-gray-500 mb-3 leading-relaxed">{{ t.finalization?.confirmPrompt }}</p>
                <p v-if="project?.clientConfirmed" class="text-xs text-blue-600 font-bold mb-3">
                  {{ t.finalization?.clientConfirmedStatus }}
                </p>
                <p v-else class="text-xs text-gray-400 mb-3">{{ t.finalization?.awaitingClientStatus }}</p>
                <button
                  @click="handleConfirm"
                  :disabled="actionLoading"
                  class="w-full px-5 py-3.5 bg-[#7C4728] text-white rounded-full font-bold hover:bg-black transition flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <CheckCircle :size="18" />
                  {{ actionLoading ? t.finalization?.confirming : t.finalization?.confirmButton }}
                </button>
              </div>
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
import { ArrowLeft, Clock, CheckCircle } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import { projectAPI, supportAPI } from '@/services/api'
import { useI18n } from '@/composables/useI18n'
import ChatPanel from '@/components/chat/ChatPanel.vue'

const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()
const { t, getT } = useI18n()

const bid = ref(null)
const project = ref(null)
const loading = ref(false)
const error = ref(null)
const actionLoading = ref(false)
const itSupportRequested = ref(false)
const supportLoading = ref(false)

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

    await bidsStore.fetchMyBids()
    const acceptedBid = bidsStore.myBids.find(b => b.projectId === Number(projectId) && b.status === 'ACCEPTED')

    if (!acceptedBid) {
      error.value = getT('finalization.noAcceptedBid')
      return
    }

    bid.value = acceptedBid

    const response = await projectAPI.getProjectForArchitect(projectId)
    project.value = response.data.data
  } catch (err) {
    error.value = err.response?.data?.message || getT('finalization.loadError')
    console.error('Failed to load negotiation data:', err)
  } finally {
    loading.value = false
  }
}

const handleConfirm = async () => {
  if (!confirm(getT('finalization.confirmDialog'))) return
  actionLoading.value = true
  try {
    const response = await projectAPI.architectConfirmNegotiation(route.params.projectId)
    project.value = response.data.data
    if (project.value.status === 'IN_PROGRESS') {
      router.push(`/architect/opportunities/${route.params.projectId}`)
    }
  } catch (err) {
    alert(err.response?.data?.message || getT('finalization.confirmError'))
  } finally {
    actionLoading.value = false
  }
}

const openSupportChat = async () => {
  if (!bid.value?.id) return
  supportLoading.value = true
  try {
    await supportAPI.createSupportConversation(route.params.projectId, bid.value.id)
    itSupportRequested.value = true
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to open support chat')
  } finally {
    supportLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>
