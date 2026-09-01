<template>
  <div class="min-h-screen bg-surface-alt">
    <div v-if="loading" class="flex items-center justify-center py-32 text-gray-500">
      {{ t.projectWorkspace?.loading }}
    </div>

    <div v-else-if="error" class="flex flex-col items-center justify-center py-32 gap-3">
      <p class="text-sm text-red-600">{{ error }}</p>
      <button class="text-sm font-semibold text-brand-brown" @click="loadAll">
        {{ t.projectWorkspace?.tryAgain }}
      </button>
    </div>

    <template v-else>
      <div class="bg-white border-b border-border-gray px-6 py-4 sticky top-0 z-10">
        <div class="max-w-7xl mx-auto flex items-center justify-between gap-4">
          <div class="flex items-center gap-4 min-w-0">
            <button class="text-gray-500 hover:text-black" @click="router.back()">
              <ArrowLeft :size="20" />
            </button>
            <div class="min-w-0">
              <p class="text-xs text-gray-400 uppercase font-bold tracking-wide">
                {{ t.projectWorkspace?.eyebrow }}
              </p>
              <h1 class="text-lg font-bold text-black truncate">
                {{ project?.title || t.projectWorkspace?.titleFallback }}
              </h1>
            </div>
          </div>
          <div class="flex items-center gap-3 shrink-0">
            <span class="text-sm text-gray-500 flex items-center gap-1.5">
              <Layers :size="14" />
              {{ disbursedCount }} / {{ phases.length }} {{ t.projectWorkspace?.phasesDone }}
            </span>
            <span
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap"
              :class="isCompleted ? 'bg-green-100 text-green-700' : 'bg-blue-50 text-blue-700'"
            >
              <span class="w-1.5 h-1.5 rounded-full" :class="isCompleted ? 'bg-green-600' : 'bg-blue-500'" />
              {{ isCompleted ? t.projectWorkspace?.completedBadge : t.projectWorkspace?.inProgressBadge }}
            </span>
          </div>
        </div>
      </div>

      <WorkspaceTabs v-model="tab" :chat-open="chatOpen" :t="t" @toggle-chat="chatOpen = !chatOpen" />

      <div class="max-w-7xl mx-auto p-6 flex justify-center gap-6 items-start">
        <div class="flex-1 max-w-[860px] min-w-0">
          <SummaryTab
            v-if="tab === 'summary'"
            :t="t"
            :project="project"
            :cover-image="coverImage"
            :phases="phases"
            :sorted-phases="sortedPhases"
            :needs-action="needsAction"
            :total-amount="totalAmount"
            :paid-amount="paidAmount"
            :remaining-amount="remainingAmount"
            :progress-percent="progressPercent"
            :disbursed-count="disbursedCount"
            :status-key="statusKey"
            :phase-fallback-title="phaseFallbackTitle"
            :format-amount="formatAmount"
            :format-date="formatDate"
            @go-contract="goToContract"
            @go-phases="tab = 'phases'"
            @go-phase="goToPhase"
          />

          <PhasesTab
            v-else-if="tab === 'phases'"
            :t="t"
            :is-client="true"
            :sorted-phases="sortedPhases"
            :open-phases="openPhases"
            :open-logs="openLogs"
            :phase-logs="phaseLogs"
            :logs-loading="logsLoading"
            :action-loading="actionLoading"
            :dispute-open-for="disputeOpenFor"
            :dispute-reason="disputeReason"
            :status-key="statusKey"
            :revisions-left="revisionsLeft"
            :show-revision-badge="showRevisionBadge"
            :deadline-label="deadlineLabel"
            :deliverable-items="deliverableItems"
            :phase-fallback-title="phaseFallbackTitle"
            :format-amount="formatAmount"
            :format-date="formatDate"
            :format-date-time="formatDateTime"
            :format-log-action="formatLogAction"
            @toggle="togglePhase"
            @toggle-log="toggleLog"
            @create-invoice="billPhase"
            @pay-now="payNow"
            @approve-phase="p => (approveModal = { phase: p, item: null })"
            @approve-item="(p, i) => (approveModal = { phase: p, item: i })"
            @request-revision="p => (revisionModal = p)"
            @open-dispute="p => (disputeOpenFor = p.id)"
            @cancel-dispute="cancelDispute"
            @submit-dispute="submitDispute"
            @update:dispute-reason="v => (disputeReason = v)"
            @open-files="(p, i) => (filesModal = { phase: p, item: i })"
          />

          <ContractTab
            v-else
            :t="t"
            :contract="contract"
            :architect-initials="architectInitials"
            :format-amount="formatAmount"
            :format-date="formatDate"
            :format-log-action="formatLogAction"
          />
        </div>

        <aside v-if="chatOpen" class="w-[340px] shrink-0 sticky top-24">
          <div
            class="bg-white border border-border-gray rounded-xl flex flex-col overflow-hidden"
            style="height: calc(100vh - 160px)"
          >
            <div class="px-4 py-3 border-b border-gray-100 flex items-center gap-3">
              <span
                class="w-8 h-8 rounded-full bg-ink-700 text-white flex items-center justify-center text-xs font-bold"
              >
                {{ architectInitials }}
              </span>
              <div class="min-w-0">
                <p class="text-sm font-semibold text-gray-900 truncate">{{ architectName }}</p>
                <p class="text-xs text-green-600 flex items-center gap-1">
                  <span class="w-1.5 h-1.5 rounded-full bg-green-500" />
                  {{ t.projectWorkspace?.activeOnProject }}
                </p>
              </div>
            </div>
            <ChatPanel v-if="conversationId" :conversation-id="conversationId" class="flex-1 min-h-0" />
            <p v-else class="p-4 text-xs text-gray-400">
              {{ t.projectWorkspace?.chatUnavailableHint }}
            </p>
          </div>
        </aside>
      </div>
    </template>

    <ApproveModal
      v-if="approveModal"
      :target-name="
        approveModal.item ? deliverableLabel(approveModal.item.name, t) : phaseFallbackTitle(approveModal.phase)
      "
      :busy="actionLoading === approveModal.phase.id"
      :t="t"
      @close="approveModal = null"
      @confirm="confirmApprove"
    />

    <RevisionModal
      v-if="revisionModal"
      :busy="actionLoading === revisionModal.id"
      :t="t"
      @close="revisionModal = null"
      @submit="submitRevision"
    />

    <FilesModal
      v-if="filesModal"
      :item="filesModal.item"
      :phase-title="phaseFallbackTitle(filesModal.phase)"
      :t="t"
      :format-date-time="formatDateTime"
      :files-by-round="filesByRound"
      @close="filesModal = null"
      @preview="openLightbox"
    />

    <ImageLightbox
      v-if="lightbox"
      :files="lightbox.files"
      :index="lightbox.index"
      :t="t"
      @close="lightbox = null"
      @step="stepLightbox"
    />

    <WorkspaceToast :message="toast" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Layers } from 'lucide-vue-next'
import { phaseAPI } from '@/services/api'
import ChatPanel from '@/components/chat/ChatPanel.vue'
import WorkspaceTabs from '@/components/workspace/WorkspaceTabs.vue'
import WorkspaceToast from '@/components/workspace/WorkspaceToast.vue'
import SummaryTab from '@/components/workspace/SummaryTab.vue'
import PhasesTab from '@/components/workspace/PhasesTab.vue'
import ContractTab from '@/components/workspace/ContractTab.vue'
import ApproveModal from '@/components/workspace/ApproveModal.vue'
import RevisionModal from '@/components/workspace/RevisionModal.vue'
import FilesModal from '@/components/workspace/FilesModal.vue'
import ImageLightbox from '@/components/workspace/ImageLightbox.vue'
import { useProjectWorkspace } from '@/components/workspace/useProjectWorkspace'
import { isImage, deliverableLabel } from '@/components/workspace/workspaceMaps'

const route = useRoute()
const router = useRouter()
const projectId = route.params.id || route.params.projectId

const ws = useProjectWorkspace(projectId, 'client')
const {
  t,
  tab,
  chatOpen,
  phases,
  sortedPhases,
  contract,
  loading,
  error,
  openPhases,
  openLogs,
  phaseLogs,
  logsLoading,
  actionLoading,
  toast,
  showToast,
  project,
  conversationId,
  architectName,
  architectInitials,
  coverImage,
  disbursedCount,
  totalAmount,
  paidAmount,
  remainingAmount,
  progressPercent,
  statusKey,
  revisionsLeft,
  showRevisionBadge,
  deadlineLabel,
  needsAction,
  deliverableItems,
  filesByRound,
  formatAmount,
  formatDate,
  formatDateTime,
  formatLogAction,
  phaseFallbackTitle,
  fetchLogs,
  loadAll,
  togglePhase,
  goToPhase,
  goToContract,
  run
} = ws

const approveModal = ref(null)
const revisionModal = ref(null)
const filesModal = ref(null)
const lightbox = ref(null)
const disputeOpenFor = ref(null)
const disputeReason = ref('')

const isCompleted = computed(() => project.value?.status === 'COMPLETED')

// Switching tabs clears every overlay, so none survives into a view that no longer owns it.
watch(tab, () => {
  approveModal.value = null
  revisionModal.value = null
  filesModal.value = null
  lightbox.value = null
  disputeOpenFor.value = null
})

const toggleLog = phase => {
  openLogs[phase.id] = !openLogs[phase.id]
  if (openLogs[phase.id]) fetchLogs(phase.id)
}

const billPhase = phase =>
  run(
    phase.id,
    async () => {
      const res = await phaseAPI.billPhase(phase.id)
      const link = res.data.data?.paymentLink || res.data?.paymentLink
      if (link) window.open(link, '_blank')
    },
    'billError'
  )

const payNow = phase => {
  if (phase.paymentLink) window.open(phase.paymentLink, '_blank')
  else billPhase(phase)
}

const confirmApprove = async () => {
  const { phase, item } = approveModal.value
  const w = t.value.projectWorkspace || {}
  await run(
    phase.id,
    async () => {
      if (item) await phaseAPI.approveDeliverableItem(phase.id, item.index)
      else await phaseAPI.approveDeliverable(phase.id)
    },
    'approveError'
  )
  approveModal.value = null
  const remaining = deliverableItems(sortedPhases.value.find(p => p.id === phase.id) || phase).filter(
    d => d.status !== 'APPROVED'
  ).length
  showToast(
    item && remaining > 0
      ? (w.toastItemApproved || 'Approved · {name}').replace('{name}', deliverableLabel(item.name, t.value))
      : w.toastPhaseApproved
  )
}

const submitRevision = async notes => {
  const phase = revisionModal.value
  await run(phase.id, () => phaseAPI.requestRevision(phase.id, { notes }), 'revisionError')
  revisionModal.value = null
}

const cancelDispute = () => {
  disputeOpenFor.value = null
  disputeReason.value = ''
}

const submitDispute = async phase => {
  if (!disputeReason.value.trim()) return
  await run(phase.id, () => phaseAPI.disputeDeliverable(phase.id, { reason: disputeReason.value }), 'disputeError')
  cancelDispute()
}

const openLightbox = file => {
  const images = (filesModal.value?.item?.files || []).filter(f => isImage(f.fileType))
  const index = Math.max(
    0,
    images.findIndex(f => f.id === file.id)
  )
  lightbox.value = { files: images, index }
}

const stepLightbox = delta => {
  const n = lightbox.value.files.length
  lightbox.value.index = (lightbox.value.index + delta + n) % n
}

onMounted(loadAll)
</script>
