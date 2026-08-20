<template>
  <div class="min-h-screen bg-surface-alt">
    <div v-if="loading && !bid" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <div class="w-10 h-10 border-2 border-brand-gold border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p class="text-gray-500">{{ t.finalization.loading }}</p>
      </div>
    </div>

    <div v-else-if="error" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button class="text-brand-brown hover:underline" @click="loadData">{{ t.finalization.tryAgain }}</button>
      </div>
    </div>

    <div v-else-if="bid">
      <!-- Header -->
      <div class="bg-white border-b border-gray-200 px-6 py-4">
        <div class="max-w-7xl mx-auto flex items-center justify-between">
          <div class="flex items-center gap-4">
            <button class="text-gray-500 hover:text-black transition" @click="router.back()">
              <ArrowLeft :size="20" />
            </button>
            <div>
              <div class="flex items-center gap-2">
                <span
                  class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-700"
                >
                  <Clock :size="12" />
                  {{ t.finalization.badge }}
                </span>
              </div>
              <h1 class="text-lg font-bold text-black mt-1">{{ project?.title || t.finalization.badge }}</h1>
            </div>
          </div>
          <div v-if="project?.biddingDeadline" class="text-right text-sm text-gray-500">
            <p class="text-xs font-bold uppercase text-gray-400">{{ t.finalization.biddingDeadline }}</p>
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
          {{ t.finalization.bothConfirmed }}
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
                <span
                  class="inline-flex items-center gap-1 px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-bold"
                >
                  <CheckCircle :size="12" />
                  {{ t.clientFinalization.winningBid }}
                </span>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div class="bg-brand-cream rounded-2xl p-4">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.finalization.proposedPrice }}</p>
                  <p class="text-2xl font-bold text-brand-brown">{{ formatCurrency(bid.bidAmount) }}</p>
                </div>
                <div class="bg-gray-50 rounded-2xl p-4">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.finalization.timeline }}</p>
                  <p class="text-2xl font-bold text-black">
                    {{ bid.proposedTimelineDays }}
                    <span class="text-sm font-normal text-gray-500">{{ t.finalization.days }}</span>
                  </p>
                </div>
              </div>
            </div>

            <!-- Payment Schedule -->
            <div v-if="bid.details?.phases?.length" class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
              <h3 class="font-bold text-black mb-4">{{ t.clientFinalization.paymentSchedule }}</h3>
              <div class="space-y-3">
                <div
                  v-for="phase in bid.details.phases"
                  :key="phase.phaseNumber"
                  class="rounded-2xl border border-gray-100 p-4 bg-brand-cream"
                >
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-brand-brown text-white">
                        {{ t.clientFinalization.phase }} {{ phase.phaseNumber }}
                      </span>
                      <span class="text-sm font-bold text-black">{{
                        phase.title || `${t.clientFinalization.phase} ${phase.phaseNumber}`
                      }}</span>
                    </div>
                    <span class="text-sm font-bold text-brand-brown">{{ formatCurrency(phase.amount) }}</span>
                  </div>
                  <div v-if="phase.deliverables?.length" class="flex flex-wrap gap-1 mb-2">
                    <span
                      v-for="d in phase.deliverables"
                      :key="d"
                      class="text-xs px-2 py-0.5 bg-white border border-gray-200 rounded-full text-gray-600"
                      >{{ d.replace(/_/g, ' ') }}</span
                    >
                  </div>
                  <p v-if="phase.revisionRounds != null" class="text-xs text-gray-500">
                    {{ phase.revisionRounds }}
                    {{
                      phase.revisionRounds !== 1
                        ? t.clientFinalization.revisionRounds
                        : t.clientFinalization.revisionRound
                    }}
                  </p>
                </div>
              </div>
              <div class="mt-3 pt-3 border-t border-gray-100 flex justify-between text-sm">
                <span class="text-gray-500 font-bold">{{ t.clientFinalization.total }}</span>
                <span class="font-bold text-brand-brown">{{ formatCurrency(bid.bidAmount) }}</span>
              </div>
            </div>

            <!-- Concept Statement -->
            <div
              v-if="bid.details?.conceptStatement"
              class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft"
            >
              <h3 class="font-bold text-black mb-3">{{ t.finalization.designConcept }}</h3>
              <p class="text-gray-700 text-sm leading-relaxed whitespace-pre-line">
                {{ bid.details.conceptStatement }}
              </p>
            </div>

            <!-- Portfolio References -->
            <div
              v-if="bid.portfolioReferences?.length"
              class="bg-white rounded-3xl border border-gray-200 p-6 shadow-soft"
            >
              <h3 class="font-bold text-black mb-3">{{ t.finalization.portfolioRefs }}</h3>
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

          <!-- RIGHT: Chat + CTAs -->
          <div class="lg:col-span-2 flex flex-col gap-4">
            <!-- Chat Panel -->
            <div
              class="flex-1 bg-white rounded-3xl border border-gray-200 shadow-soft overflow-hidden flex flex-col min-h-0"
            >
              <div class="px-5 py-4 border-b border-gray-100">
                <div class="flex items-center gap-2">
                  <h3 class="font-bold text-black">{{ t.finalization.discussion }}</h3>
                  <span v-if="itSupportRequested" class="text-xs text-brand-brown font-medium"
                    >· {{ t.clientFinalization.itSupportInvited }}</span
                  >
                </div>
                <p class="text-xs text-gray-500 mt-0.5">{{ t.clientFinalization.discussionSubtitle }}</p>
              </div>

              <div v-if="bid.conversationId" class="flex-1 min-h-0">
                <ChatPanel :conversation-id="bid.conversationId" class="h-full" />
              </div>
              <div v-else class="flex-1 flex items-center justify-center text-gray-400 text-sm">
                {{ t.finalization.chatUnavailable }}
              </div>
            </div>

            <div class="text-center">
              <button
                :disabled="supportLoading || itSupportRequested"
                class="text-xs text-gray-400 hover:text-gray-600 underline disabled:cursor-not-allowed"
                @click="openSupportChat"
              >
                {{
                  supportLoading
                    ? t.support.opening
                    : itSupportRequested
                      ? t.clientFinalization.itSupportInvited
                      : t.support.requestSupport
                }}
              </button>
            </div>

            <!-- Client action panel -->
            <div v-if="isClient" class="bg-white rounded-3xl border border-gray-200 p-5 shadow-soft">
              <div v-if="project?.clientConfirmed" class="flex items-center gap-2 text-green-700">
                <CheckCircle :size="16" />
                <div>
                  <p class="text-sm font-bold">{{ t.clientFinalization.clientPanel.confirmed }}</p>
                  <p class="text-xs text-gray-500">
                    {{
                      project?.architectConfirmed
                        ? t.clientFinalization.clientPanel.architectAlsoConfirmed
                        : t.clientFinalization.clientPanel.awaitingArchitect
                    }}
                  </p>
                </div>
              </div>
              <div v-else>
                <p class="text-xs text-gray-500 mb-4 leading-relaxed">
                  {{ t.clientFinalization.clientPanel.confirmPrompt }}
                </p>
                <div class="space-y-3">
                  <button
                    :disabled="actionLoading"
                    class="w-full px-5 py-3.5 bg-brand-brown text-white rounded-full font-bold hover:bg-black transition flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                    @click="handleConfirm"
                  >
                    <CheckCircle :size="18" />
                    {{
                      actionLoading === 'confirm'
                        ? t.clientFinalization.clientPanel.confirming
                        : t.clientFinalization.clientPanel.confirmButton
                    }}
                  </button>
                  <button
                    :disabled="actionLoading"
                    class="w-full px-5 py-3.5 bg-white text-red-600 border border-red-200 rounded-full font-bold hover:bg-red-50 transition flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                    @click="showRejectDialog = true"
                  >
                    <XCircle :size="18" />
                    {{ t.clientFinalization.clientPanel.rejectButton }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Architect action panel -->
            <div v-else class="bg-white rounded-3xl border border-gray-200 p-5 shadow-soft">
              <div v-if="project?.architectConfirmed" class="flex items-center gap-2 text-green-700">
                <CheckCircle :size="16" />
                <div>
                  <p class="text-sm font-bold">{{ t.clientFinalization.architectPanel.confirmed }}</p>
                  <p class="text-xs text-gray-500">
                    {{
                      project?.clientConfirmed
                        ? t.clientFinalization.architectPanel.clientAlsoConfirmed
                        : t.clientFinalization.architectPanel.awaitingClient
                    }}
                  </p>
                </div>
              </div>
              <div v-else>
                <p class="text-xs text-gray-500 mb-4 leading-relaxed">
                  {{ t.clientFinalization.architectPanel.confirmPrompt }}
                </p>
                <button
                  :disabled="actionLoading"
                  class="w-full px-5 py-3.5 bg-brand-brown text-white rounded-full font-bold hover:bg-black transition flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                  @click="handleArchitectConfirm"
                >
                  <CheckCircle :size="18" />
                  {{
                    actionLoading === 'architect-confirm'
                      ? t.clientFinalization.clientPanel.confirming
                      : t.clientFinalization.architectPanel.confirmButton
                  }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Reject Confirmation Dialog -->
    <div v-if="showRejectDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 px-4">
      <div class="bg-white rounded-3xl p-8 max-w-md w-full shadow-2xl">
        <h3 class="text-xl font-bold text-black mb-2">{{ t.clientFinalization.rejectDialog.title }}</h3>
        <p class="text-gray-600 text-sm mb-6">
          {{ t.clientFinalization.rejectDialog.message }}
        </p>
        <div class="flex gap-3">
          <button
            class="flex-1 px-5 py-3 border border-gray-200 rounded-full font-bold text-gray-700 hover:bg-gray-50 transition"
            @click="showRejectDialog = false"
          >
            {{ t.clientFinalization.rejectDialog.cancel }}
          </button>
          <button
            :disabled="actionLoading === 'reject'"
            class="flex-1 px-5 py-3 bg-red-600 text-white rounded-full font-bold hover:bg-red-700 transition disabled:opacity-50"
            @click="handleReject"
          >
            {{
              actionLoading === 'reject'
                ? t.clientFinalization.rejectDialog.rejecting
                : t.clientFinalization.rejectDialog.confirm
            }}
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
import { useI18n } from '@/composables/useI18n'
import { useBidsStore } from '@/stores/bids'
import { useAuthStore } from '@/stores/auth'
import { projectAPI, supportAPI } from '@/services/api'
import ChatPanel from '@/components/chat/ChatPanel.vue'

const { t } = useI18n()
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
const itSupportRequested = ref(false)
const supportLoading = ref(false)

const isClient = computed(() => authStore.hasRole('CLIENT'))

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
      const acceptedBid = bidsStore.myBids.find(b => b.projectId === Number(projectId) && b.status === 'ACCEPTED')

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
    const response = await projectAPI.confirmNegotiation(route.params.projectId)
    project.value = response.data.data
    if (project.value.status === 'IN_PROGRESS') {
      router.push(`/client/projects/${route.params.projectId}/payments`)
    }
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to confirm terms')
  } finally {
    actionLoading.value = null
  }
}

const handleArchitectConfirm = async () => {
  if (!confirm('Confirm the terms and proceed with this project?')) return
  actionLoading.value = 'architect-confirm'
  try {
    const response = await projectAPI.architectConfirmNegotiation(route.params.projectId)
    project.value = response.data.data
    if (project.value.status === 'IN_PROGRESS') {
      router.push(`/architect/projects/${route.params.projectId}`)
    }
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
