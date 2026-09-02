<template>
  <div class="flex flex-col gap-3">
    <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
      {{ t.projectWorkspace?.paymentPhasesTitle }}
    </p>

    <div
      v-for="(phase, index) in sortedPhases"
      :id="`phase-${phase.id}`"
      :key="phase.id"
      class="bg-white border border-border-gray rounded-xl overflow-hidden scroll-mt-24"
    >
      <div v-if="openPhases[phase.id]" class="h-[3px] bg-brand-gold" />

      <button
        class="w-full px-5 py-4 flex items-center gap-3 hover:bg-gray-50 text-left"
        @click="$emit('toggle', phase)"
      >
        <span
          class="w-8 h-8 rounded-full shrink-0 flex items-center justify-center text-[13px] font-bold"
          :class="statusStyles[statusKey(phase, index)]?.icon"
        >
          <CheckCircle v-if="phase.status === 'DISBURSED'" class="w-4 h-4" />
          <Lock v-else-if="statusKey(phase, index) === 'NOT_STARTED'" class="w-4 h-4" />
          <template v-else>{{ phase.phaseNumber }}</template>
        </span>

        <div class="flex-1 min-w-0">
          <p class="text-sm font-semibold text-gray-900 truncate">{{ phaseFallbackTitle(phase) }}</p>
          <p class="text-xs text-gray-500 truncate">
            {{ formatAmount(phase.amount) }} &middot; {{ t.projectWorkspace?.deadline }}
            {{ formatDate(phase.dueDate) }} &middot; {{ deadlineLabel(phase) }}
          </p>
        </div>

        <span
          class="px-2.5 py-1 rounded-full text-xs font-bold flex items-center gap-1.5 shrink-0"
          :class="[statusStyles[statusKey(phase, index)]?.bg, statusStyles[statusKey(phase, index)]?.text]"
        >
          <span class="w-1.5 h-1.5 rounded-full" :class="statusStyles[statusKey(phase, index)]?.dot" />
          {{ statusLabels[statusKey(phase, index)] }}
        </span>

        <component :is="openPhases[phase.id] ? ChevronUp : ChevronDown" class="w-4 h-4 text-gray-400 shrink-0" />
      </button>

      <div v-if="openPhases[phase.id]">
        <div v-if="phaseDescription(phase)" class="px-5 py-4 border-b border-gray-100">
          <p class="text-xs font-bold uppercase tracking-wider text-gray-400 mb-1">
            {{ t.projectWorkspace?.descriptionLabel }}
          </p>
          <p class="text-sm text-gray-600 leading-relaxed">{{ phaseDescription(phase) }}</p>
        </div>

        <div
          v-if="showRevisionBadge(phase)"
          class="px-5 py-3 border-b flex items-center gap-2"
          :class="revisionsLeft(phase) > 0 ? 'bg-purple-50 border-purple-100' : 'bg-red-50 border-red-100'"
        >
          <RotateCcw class="w-4 h-4 shrink-0" :class="revisionsLeft(phase) > 0 ? 'text-purple-600' : 'text-red-500'" />
          <span class="text-xs font-semibold" :class="revisionsLeft(phase) > 0 ? 'text-purple-800' : 'text-red-800'">
            <template v-if="revisionsLeft(phase) > 0">
              {{ t.projectWorkspace?.revisionsLeftLabel }}: {{ revisionsLeft(phase) }} {{ t.projectWorkspace?.of }}
              {{ phase.maxRevisions }}
            </template>
            <template v-else>
              {{ t.projectWorkspace?.revisionsExhausted }} &middot; 0 {{ t.projectWorkspace?.of }}
              {{ phase.maxRevisions }}
            </template>
          </span>
        </div>

        <div class="px-5 py-4 border-b border-gray-100">
          <PhaseActions
            :status-key="statusKey(phase, index)"
            :is-client="isClient"
            :deliverables="deliverableItems(phase)"
            :revisions-left="revisionsLeft(phase)"
            :busy="actionLoading === phase.id"
            :dispute-open="disputeOpenFor === phase.id"
            :dispute-reason="disputeReason"
            :t="t"
            @approve-phase="$emit('approve-phase', phase)"
            @open-dispute="$emit('open-dispute', phase)"
            @cancel-dispute="$emit('cancel-dispute')"
            @submit-dispute="$emit('submit-dispute', phase)"
            @update:dispute-reason="$emit('update:disputeReason', $event)"
            @submit-review="$emit('submit-review', phase)"
            @go-contract="$emit('go-contract')"
          />
        </div>

        <div class="px-5 py-4 border-b border-gray-100">
          <DeliverablesTable
            :items="deliverableItems(phase)"
            :phase-status-key="statusKey(phase, index)"
            :is-client="isClient"
            :busy="actionLoading === phase.id"
            :revisions-left="revisionsLeft(phase)"
            :t="t"
            :format-date-time="formatDateTime"
            @approve="$emit('approve-item', phase, $event)"
            @upload="$emit('upload-to-item', phase, $event)"
            @open-files="$emit('open-files', phase, $event)"
            @request-revision="$emit('request-revision', phase, $event)"
          />
        </div>

        <div class="px-5 py-4">
          <button
            class="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-gray-400"
            @click="$emit('toggle-log', phase)"
          >
            {{ t.projectWorkspace?.activityLogLabel }}
            <span class="text-brand-brown normal-case tracking-normal font-semibold">
              {{
                openLogs[phase.id]
                  ? t.projectWorkspace?.hideLabel || 'Hide'
                  : `${t.projectWorkspace?.showLabel || 'Show'} (${(phaseLogs[phase.id] || []).length})`
              }}
            </span>
          </button>

          <div v-if="openLogs[phase.id]" class="mt-3">
            <p v-if="logsLoading[phase.id]" class="text-xs text-gray-400">
              {{ t.projectWorkspace?.loadingActivity }}
            </p>
            <p v-else-if="!(phaseLogs[phase.id] || []).length" class="text-xs text-gray-400">
              {{ t.projectWorkspace?.noActivityRecorded }}
            </p>
            <div
              v-for="log in phaseLogs[phase.id] || []"
              :key="log.id"
              class="flex items-center gap-3 py-2 border-b border-gray-50"
            >
              <span
                class="w-6 h-6 rounded-full shrink-0 flex items-center justify-center text-[10px] font-bold"
                :class="logIconClass(log.actorType)"
              >
                {{ (log.actorType || '?')[0] }}
              </span>
              <span class="text-xs font-semibold text-gray-700 flex-1 min-w-0 truncate">
                {{ formatLogAction(log.action) }}
              </span>
              <span v-if="log.fromStatus && log.toStatus" class="text-xs text-gray-400 shrink-0 hidden sm:inline">
                {{ log.fromStatus }} &rarr; {{ log.toStatus }}
              </span>
              <span class="text-xs text-gray-400 shrink-0">{{ formatDateTime(log.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChevronUp, ChevronDown, CheckCircle, Lock, RotateCcw } from 'lucide-vue-next'
import PhaseActions from './PhaseActions.vue'
import DeliverablesTable from './DeliverablesTable.vue'
import { statusStyles, logIconClass, deliverableLabel } from './workspaceMaps'

const props = defineProps({
  t: { type: Object, required: true },
  isClient: { type: Boolean, default: true },
  sortedPhases: { type: Array, default: () => [] },
  openPhases: { type: Object, required: true },
  openLogs: { type: Object, required: true },
  phaseLogs: { type: Object, required: true },
  logsLoading: { type: Object, required: true },
  actionLoading: { type: [Number, String], default: null },
  disputeOpenFor: { type: [Number, String], default: null },
  disputeReason: { type: String, default: '' },
  statusKey: { type: Function, required: true },
  revisionsLeft: { type: Function, required: true },
  showRevisionBadge: { type: Function, required: true },
  deadlineLabel: { type: Function, required: true },
  deliverableItems: { type: Function, required: true },
  phaseFallbackTitle: { type: Function, required: true },
  formatAmount: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  formatDateTime: { type: Function, required: true },
  formatLogAction: { type: Function, required: true }
})
defineEmits([
  'toggle',
  'toggle-log',
  'approve-phase',
  'request-revision',
  'open-dispute',
  'cancel-dispute',
  'submit-dispute',
  'update:disputeReason',
  'submit-review',
  'go-contract',
  'approve-item',
  'upload-to-item',
  'open-files'
])

const statusLabels = computed(() => props.t.projectWorkspace?.statusLabels || {})

// phase.description is a comma-joined snapshot of the same taxonomy codes the deliverable rows
// carry, frozen at phase creation. Rebuild it from those rows so it reads in the user's language
// rather than splitting the stored string, which a free-text deliverable name could contain.
const phaseDescription = phase => {
  const named = props.deliverableItems(phase).filter(item => item.name)
  return named.length ? named.map(item => deliverableLabel(item.name, props.t)).join(', ') : phase.description
}
</script>
