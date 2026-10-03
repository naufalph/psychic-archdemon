<template>
  <div class="flex flex-col gap-3">
    <div class="flex items-center justify-between gap-3">
      <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
        {{ t.projectWorkspace?.paymentPhasesTitle }}
      </p>
      <button
        v-if="cameFromTimeline"
        class="text-xs font-semibold text-brand-brown hover:text-[#0A0A0A] flex items-center gap-1"
        @click="$emit('back-to-timeline')"
      >
        &larr; {{ t.projectWorkspace?.backToTimeline || 'Back to timeline' }}
      </button>
    </div>

    <div
      v-for="(phase, index) in sortedPhases"
      :id="`phase-${phase.id}`"
      :key="phase.id"
      class="bg-white border rounded-xl overflow-hidden scroll-mt-24 transition-[box-shadow,border-color] duration-[800ms] ease-[cubic-bezier(0.16,1,0.3,1)]"
      :class="
        flashPhaseId === phase.id
          ? 'border-brand-gold shadow-[0_0_0_4px_rgba(197,161,122,0.25),0_20px_40px_-8px_rgba(0,0,0,0.08)]'
          : 'border-border-gray shadow-none'
      "
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
            {{ formatAmount(phase.amount) }} &middot;
            <template v-if="phase.dueDate">
              {{ t.projectWorkspace?.deadline }} {{ formatDate(phase.dueDate) }} &middot;
            </template>
            {{ deadlineLabel(phase) }}
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
        <div
          v-if="schedule.byPhaseId[phase.id]"
          class="px-5 py-3.5 border-y border-gray-100 bg-surface grid grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] gap-4 items-center"
        >
          <div>
            <p class="text-[11px] font-bold tracking-[0.08em] uppercase text-gray-400">
              {{ w.targetFromBidLabel || 'Target · from bid' }}
            </p>
            <p class="text-[13px] leading-[18px] text-gray-700 mt-0.5">{{ targetLong(phase) }}</p>
          </div>
          <div>
            <p class="text-[11px] font-bold tracking-[0.08em] uppercase text-gray-400">
              {{ w.actualLabel || 'Actual' }}
            </p>
            <p class="text-[13px] leading-[18px] text-gray-900 font-semibold mt-0.5">{{ actualLong(phase) }}</p>
          </div>
          <span
            v-if="schedule.byPhaseId[phase.id].chip"
            class="px-2.5 py-1 rounded-full text-xs font-bold whitespace-nowrap"
            :class="chipClasses[schedule.byPhaseId[phase.id].chip.kind]"
          >
            {{ chipText(schedule.byPhaseId[phase.id].chip, w) }}
          </span>
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
            :due-date="phase.dueDate"
            :deadline-label="deadlineLabel(phase)"
            :format-date="formatDate"
            :t="t"
            @approve-phase="$emit('approve-phase', phase)"
            @open-dispute="$emit('open-dispute', phase)"
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
                {{ statusLabels[log.fromStatus] || log.fromStatus }} &rarr;
                {{ statusLabels[log.toStatus] || log.toStatus }}
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
import { statusStyles, logIconClass } from './workspaceMaps'
import { chipText, chipClasses, formatDayMonth, formatRange } from './phaseSchedule'
import { useI18n } from '@/composables/useI18n'

const props = defineProps({
  t: { type: Object, required: true },
  isClient: { type: Boolean, default: true },
  sortedPhases: { type: Array, default: () => [] },
  schedule: { type: Object, required: true },
  cameFromTimeline: { type: Boolean, default: false },
  flashPhaseId: { type: [Number, String], default: null },
  openPhases: { type: Object, required: true },
  openLogs: { type: Object, required: true },
  phaseLogs: { type: Object, required: true },
  logsLoading: { type: Object, required: true },
  actionLoading: { type: [Number, String], default: null },
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
  'back-to-timeline',
  'toggle-log',
  'approve-phase',
  'request-revision',
  'open-dispute',
  'submit-review',
  'go-contract',
  'approve-item',
  'upload-to-item',
  'open-files'
])

const statusLabels = computed(() => props.t.projectWorkspace?.statusLabels || {})

const { locale } = useI18n()
const w = computed(() => props.t.projectWorkspace || {})

const targetLong = phase => {
  const r = props.schedule.byPhaseId[phase.id]
  return (w.value.targetRangeLong || '{range} · {d} days')
    .replace('{range}', formatRange(r.targetStart, r.targetEnd, locale.value))
    .replace('{d}', r.est)
}

const actualLong = phase => {
  const r = props.schedule.byPhaseId[phase.id]
  const fill = (key, fallback) =>
    (w.value[key] || fallback)
      .replace('{range}', formatRange(r.actualStart, r.actualEnd, locale.value))
      .replace('{date}', formatDayMonth(r.actualStart, locale.value))
      .replace('{end}', formatDayMonth(r.actualEnd, locale.value))
      .replace('{d}', r.workDays)
  if (r.state === 'done')
    return r.workDays != null
      ? fill('actualDoneLong', '{range} · submitted in {d} days')
      : fill('actualRange', '{range}')
  if (r.state === 'delivered')
    return fill('actualAwaitingLong', 'Since {date} · submitted in {d} days, awaiting approval')
  if (r.state === 'active') return fill('actualActiveLong', 'Since {date} · forecast {end}')
  return fill('actualForecastLong', 'Forecast {range}')
}
</script>
