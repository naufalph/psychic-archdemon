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
              <span
                class="w-1.5 h-1.5 rounded-full"
                :class="isCompleted ? 'bg-green-600' : 'bg-blue-500'"
              />
              {{ isCompleted ? t.projectWorkspace?.completedBadge : t.projectWorkspace?.inProgressBadge }}
            </span>
          </div>
        </div>
      </div>

      <WorkspaceTabs
        v-model="tab"
        :chat-open="chatOpen"
        :t="t"
        @toggle-chat="chatOpen = !chatOpen"
      />

      <div class="max-w-7xl mx-auto p-6 flex justify-center gap-6 items-start">
        <div class="flex-1 max-w-[860px] min-w-0">
          <SummaryTab
            v-if="tab === 'summary'"
            :t="t"
            :is-client="false"
            :project="project"
            :cover-image="coverImage"
            :phases="phases"
            :sorted-phases="sortedPhases"
            :needs-action="needsAction"
            :total-amount="totalAmount"
            :progress-percent="progressPercent"
            :disbursed-count="disbursedCount"
            :focus-phase="focusPhase"
            :action-loading="actionLoading"
            :revisions-left="revisionsLeft"
            :show-revision-badge="showRevisionBadge"
            :deadline-label="deadlineLabel"
            :deliverable-items="deliverableItems"
            :phase-description="phaseDescription"
            :status-key="statusKey"
            :phase-fallback-title="phaseFallbackTitle"
            :format-amount="formatAmount"
            :format-date="formatDate"
            @go-contract="goToContract"
            @go-phases="tab = 'phases'"
            @go-phase="goToPhase"
            @submit-review="submitForReview"
          />

          <PhasesTab
            v-else-if="tab === 'phases'"
            :t="t"
            :is-client="false"
            :sorted-phases="sortedPhases"
            :open-phases="openPhases"
            :open-logs="openLogs"
            :phase-logs="phaseLogs"
            :logs-loading="logsLoading"
            :action-loading="actionLoading"
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
            @submit-review="submitForReview"
            @go-contract="goToContract"
            @upload-to-item="(p, i) => (uploadModal = { phase: p, item: i })"
            @open-files="(p, i) => (filesModal = { phase: p, item: i })"
          />

          <ContractTab
            v-else
            :t="t"
            :is-client="false"
            :busy="actionLoading"
            :contract="contract"
            :architect-initials="architectInitials"
            :format-amount="formatAmount"
            :format-date="formatDate"
            :format-log-action="formatLogAction"
            @request-payout="row => (payoutModal = phaseFor(row) || null)"
          />
        </div>

        <aside v-if="chatOpen" class="w-[340px] shrink-0 sticky top-24">
          <div
            class="bg-white border border-border-gray rounded-xl flex flex-col overflow-hidden"
            style="height: calc(100vh - 160px)"
          >
            <div class="px-4 py-3 border-b border-gray-100">
              <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
                {{ t.projectWorkspace?.chatTitle }}
              </p>
            </div>
            <ChatPanel
              v-if="conversationId"
              :conversation-id="conversationId"
              class="flex-1 min-h-0"
            />
            <p v-else class="p-4 text-xs text-gray-400">
              {{ t.projectWorkspace?.chatUnavailableHint }}
            </p>
          </div>
        </aside>
      </div>
    </template>

    <UploadModal
      v-if="uploadModal"
      :item="uploadModal.item"
      :busy="uploadLoading === uploadModal.phase.id"
      :t="t"
      @close="uploadModal = null"
      @submit="doUpload"
    />

    <PayoutModal
      v-if="payoutModal"
      :phase="payoutModal"
      :busy="actionLoading === payoutModal.id"
      :t="t"
      :format-amount="formatAmount"
      @close="payoutModal = null"
      @submit="doPayout"
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
import UploadModal from '@/components/workspace/UploadModal.vue'
import PayoutModal from '@/components/workspace/PayoutModal.vue'
import FilesModal from '@/components/workspace/FilesModal.vue'
import ImageLightbox from '@/components/workspace/ImageLightbox.vue'
import { useProjectWorkspace } from '@/components/workspace/useProjectWorkspace'
import { isImage } from '@/components/workspace/workspaceMaps'

const route = useRoute()
const router = useRouter()
const projectId = route.params.id || route.params.projectId

const ws = useProjectWorkspace(projectId, 'architect')
const {
  t, tab, chatOpen, phases, sortedPhases, contract, loading, error,
  openPhases, openLogs, phaseLogs, logsLoading, actionLoading, uploadLoading,
  toast, showToast, project, conversationId, architectInitials, coverImage,
  disbursedCount, totalAmount, progressPercent,
  statusKey, revisionsLeft, showRevisionBadge, deadlineLabel, needsAction,
  deliverableItems, phaseDescription, focusPhase, filesByRound, formatAmount, formatDate, formatDateTime,
  formatLogAction, phaseFallbackTitle, fetchLogs, refreshPhases, refreshContract,
  loadAll, togglePhase, goToPhase, goToContract, run
} = ws

const uploadModal = ref(null)
const payoutModal = ref(null)
const filesModal = ref(null)
const lightbox = ref(null)

/** Contract rows carry only the phase id; the actions need the phase itself. */
const phaseFor = row => sortedPhases.value.find(p => p.id === row.phaseId) || null

const isCompleted = computed(() => project.value?.status === 'COMPLETED')

watch(tab, () => {
  uploadModal.value = null
  payoutModal.value = null
  filesModal.value = null
  lightbox.value = null
})

const toggleLog = phase => {
  openLogs[phase.id] = !openLogs[phase.id]
  if (openLogs[phase.id]) fetchLogs(phase.id)
}

const submitForReview = phase =>
  run(phase.id, () => phaseAPI.submitForReview(phase.id), 'submitReviewError')

/** Files are tagged to the deliverable they belong to, not left loose on the phase. */
const doUpload = async ({ file, description }) => {
  const { phase, item } = uploadModal.value
  uploadLoading.value = phase.id
  try {
    const formData = new FormData()
    formData.append('file', file)
    if (description) formData.append('description', description)
    formData.append('deliverableIndex', item.index)
    await phaseAPI.uploadDeliverableFile(phase.id, formData)
    uploadModal.value = null
    await refreshPhases()
    await refreshContract()
    delete phaseLogs[phase.id]
    fetchLogs(phase.id)
  } catch (err) {
    showToast(err.response?.data?.message || t.value.projectWorkspace?.uploadError)
  } finally {
    uploadLoading.value = null
  }
}

const doPayout = async form => {
  const phase = payoutModal.value
  await run(phase.id, () => phaseAPI.disburse(phase.id, form), 'payoutError')
  payoutModal.value = null
}

const openLightbox = file => {
  const images = (filesModal.value?.item?.files || []).filter(f => isImage(f.fileType))
  const index = Math.max(0, images.findIndex(f => f.id === file.id))
  lightbox.value = { files: images, index }
}

const stepLightbox = delta => {
  const n = lightbox.value.files.length
  lightbox.value.index = (lightbox.value.index + delta + n) % n
}

onMounted(loadAll)
</script>
