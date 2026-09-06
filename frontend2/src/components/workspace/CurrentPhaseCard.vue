<template>
  <div v-if="phase" class="bg-white border border-border-gray rounded-xl overflow-hidden">
    <div class="h-[3px] bg-brand-gold" />

    <div class="px-5 py-4 flex items-center gap-3">
      <span
        class="w-8 h-8 rounded-full shrink-0 flex items-center justify-center text-[13px] font-bold"
        :class="statusStyles[statusKeyValue]?.icon"
      >
        <Lock v-if="statusKeyValue === 'NOT_STARTED'" class="w-4 h-4" />
        <template v-else>{{ phase.phaseNumber }}</template>
      </span>

      <div class="flex-1 min-w-0">
        <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
          {{ t.projectWorkspace?.currentPhaseLabel }}
        </p>
        <p class="text-sm font-semibold text-gray-900 truncate">{{ title }}</p>
        <p class="text-xs text-gray-500 truncate">
          {{ formatAmount(phase.amount) }} &middot;
          <template v-if="phase.dueDate">
            {{ t.projectWorkspace?.deadline }} {{ formatDate(phase.dueDate) }} &middot;
          </template>
          {{ deadlineLabel }}
        </p>
      </div>

      <span
        class="px-2.5 py-1 rounded-full text-xs font-bold flex items-center gap-1.5 shrink-0"
        :class="[statusStyles[statusKeyValue]?.bg, statusStyles[statusKeyValue]?.text]"
      >
        <span class="w-1.5 h-1.5 rounded-full" :class="statusStyles[statusKeyValue]?.dot" />
        {{ statusLabels[statusKeyValue] }}
      </span>
    </div>

    <div
      v-if="showBadge"
      class="px-5 py-3 border-t flex items-center gap-2"
      :class="revisionsLeft > 0 ? 'bg-purple-50 border-purple-100' : 'bg-red-50 border-red-100'"
    >
      <RotateCcw class="w-4 h-4 shrink-0" :class="revisionsLeft > 0 ? 'text-purple-600' : 'text-red-500'" />
      <span class="text-xs font-semibold" :class="revisionsLeft > 0 ? 'text-purple-800' : 'text-red-800'">
        <template v-if="revisionsLeft > 0">
          {{ t.projectWorkspace?.revisionsLeftLabel }}: {{ revisionsLeft }} {{ t.projectWorkspace?.of }}
          {{ phase.maxRevisions }}
        </template>
        <template v-else>
          {{ t.projectWorkspace?.revisionsExhausted }} &middot; 0 {{ t.projectWorkspace?.of }}
          {{ phase.maxRevisions }}
        </template>
      </span>
    </div>

    <div class="px-5 py-4 border-t border-gray-100">
      <PhaseActions
        :status-key="statusKeyValue"
        :is-client="isClient"
        :deliverables="deliverables"
        :revisions-left="revisionsLeft"
        :busy="busy"
        :dispute-open="disputeOpen"
        :dispute-reason="disputeReason"
        :due-date="phase.dueDate"
        :deadline-label="deadlineLabel"
        :format-date="formatDate"
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

    <!--
      Summary carries the deliverable list as prose; the table that acts on it lives one tab
      over, so this card names the work without repeating the controls.
    -->
    <div v-if="description" class="px-5 py-4 border-t border-gray-100">
      <p class="text-xs font-bold uppercase tracking-wider text-gray-400 mb-1">
        {{ t.projectWorkspace?.descriptionLabel }}
      </p>
      <p class="text-sm text-gray-600 leading-relaxed">{{ description }}</p>
    </div>

    <button
      class="w-full px-5 py-3 border-t border-gray-100 text-xs font-semibold text-brand-brown hover:bg-gray-50 text-left"
      @click="$emit('go-phase', phase.id)"
    >
      {{ t.projectWorkspace?.openDeliverablesLink }} &rarr;
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Lock, RotateCcw } from 'lucide-vue-next'
import { statusStyles } from './workspaceMaps'
import PhaseActions from './PhaseActions.vue'

const props = defineProps({
  t: { type: Object, required: true },
  isClient: { type: Boolean, default: true },
  phase: { type: Object, default: null },
  title: { type: String, default: '' },
  description: { type: String, default: '' },
  statusKeyValue: { type: String, default: 'PENDING' },
  deliverables: { type: Array, default: () => [] },
  revisionsLeft: { type: Number, default: 0 },
  showBadge: { type: Boolean, default: false },
  deadlineLabel: { type: String, default: '' },
  busy: { type: Boolean, default: false },
  disputeOpen: { type: Boolean, default: false },
  disputeReason: { type: String, default: '' },
  formatAmount: { type: Function, required: true },
  formatDate: { type: Function, required: true }
})
defineEmits([
  'approve-phase',
  'open-dispute',
  'cancel-dispute',
  'submit-dispute',
  'update:disputeReason',
  'submit-review',
  'go-contract',
  'go-phase'
])

const statusLabels = computed(() => props.t.projectWorkspace?.statusLabels || {})
</script>
