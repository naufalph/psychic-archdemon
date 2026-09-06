<template>
  <div class="flex flex-col gap-4">
    <!-- Hero: the whole card navigates to the contract tab -->
    <button
      class="bg-white border border-border-gray rounded-xl p-5 text-left hover:border-brand-gold hover:shadow-sm transition-all"
      @click="$emit('go-contract')"
    >
      <div class="flex gap-4">
        <div class="w-20 h-20 rounded-lg bg-gray-100 shrink-0 overflow-hidden">
          <img v-if="coverImage" :src="coverImage" alt="" class="w-full h-full object-cover" />
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <h2 class="text-base font-bold text-gray-900 truncate">
                {{ project?.title || t.projectWorkspace?.titleFallback }}
              </h2>
              <span class="text-xs font-medium text-brand-brown">
                {{ t.projectWorkspace?.viewDetail }}
              </span>
            </div>
            <div class="text-right shrink-0">
              <p class="text-xs text-gray-400">{{ t.projectWorkspace?.totalValue }}</p>
              <p class="font-bold text-gray-900">{{ formatAmount(totalAmount) }}</p>
            </div>
          </div>
          <div class="flex gap-3 text-sm text-gray-500 mt-1">
            <span v-if="project?.city" class="flex items-center gap-1">
              <MapPin class="w-3.5 h-3.5" />{{ project.city }}
            </span>
            <span v-if="project?.projectCategory" class="flex items-center gap-1">
              <Tag class="w-3.5 h-3.5" />{{ project.projectCategory }}
            </span>
          </div>
        </div>
      </div>
    </button>

    <PhaseProgressChart
      :t="t"
      :is-client="isClient"
      :sorted-phases="sortedPhases"
      :total-amount="totalAmount"
      :progress-percent="progressPercent"
      :disbursed-count="disbursedCount"
      :status-key="statusKey"
      :format-amount="formatAmount"
      :format-date="formatDate"
    />

    <!-- The phase actually in play; the deliverable table for it lives on the phases tab -->
    <CurrentPhaseCard
      v-if="focusPhase"
      :t="t"
      :is-client="isClient"
      :phase="focusPhase.phase"
      :title="phaseFallbackTitle(focusPhase.phase)"
      :description="phaseDescription(focusPhase.phase)"
      :status-key-value="statusKey(focusPhase.phase, focusPhase.index)"
      :deliverables="deliverableItems(focusPhase.phase)"
      :revisions-left="revisionsLeft(focusPhase.phase)"
      :show-badge="showRevisionBadge(focusPhase.phase)"
      :deadline-label="deadlineLabel(focusPhase.phase)"
      :busy="actionLoading === focusPhase.phase.id"
      :dispute-open="disputeOpenFor === focusPhase.phase.id"
      :dispute-reason="disputeReason"
      :format-amount="formatAmount"
      :format-date="formatDate"
      @approve-phase="$emit('approve-phase', $event)"
      @open-dispute="$emit('open-dispute', $event)"
      @cancel-dispute="$emit('cancel-dispute')"
      @submit-dispute="$emit('submit-dispute', $event)"
      @update:dispute-reason="$emit('update:disputeReason', $event)"
      @submit-review="$emit('submit-review', $event)"
      @go-contract="$emit('go-contract')"
      @go-phase="$emit('go-phase', $event)"
    />

    <!-- Needs your action -->
    <div class="bg-white border border-border-gray rounded-xl p-5">
      <p class="text-xs font-bold uppercase tracking-wider text-gray-400 mb-3">
        {{ t.projectWorkspace?.needsActionLabel }}
      </p>
      <div v-if="needsAction.length" class="flex flex-col gap-2">
        <button
          v-for="row in needsAction"
          :key="row.phase.id"
          class="w-full p-3 rounded-lg bg-gray-50 border border-border-gray hover:border-brand-gold hover:bg-white transition-colors flex items-center gap-3 text-left"
          @click="$emit(row.target === 'contract' ? 'go-contract' : 'go-phase', row.phase.id)"
        >
          <span
            class="w-8 h-8 rounded-lg bg-white border border-border-gray shrink-0 flex items-center justify-center text-xs font-bold text-gray-600"
          >
            {{ row.phase.phaseNumber }}
          </span>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-gray-900 truncate">{{ row.title }}</p>
            <p class="text-xs text-gray-500 truncate">{{ phaseFallbackTitle(row.phase) }}</p>
          </div>
          <span class="text-xs font-semibold text-brand-brown shrink-0">{{ row.cta }} &rarr;</span>
        </button>
      </div>
      <div v-else class="border-2 border-dashed border-border-gray rounded-lg py-6 text-center text-sm text-gray-400">
        {{ t.projectWorkspace?.nothingWaiting }}
      </div>
    </div>

    <!-- Payment phases -->
    <div class="bg-white border border-border-gray rounded-xl p-5">
      <div class="flex items-center justify-between mb-1">
        <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
          {{ t.projectWorkspace?.paymentPhasesTitle }}
        </p>
        <button class="text-xs font-semibold text-brand-brown" @click="$emit('go-phases')">
          {{ t.projectWorkspace?.allPhasesLink }} &rarr;
        </button>
      </div>
      <button
        v-for="(phase, index) in sortedPhases"
        :key="phase.id"
        class="w-full flex items-center gap-3 py-3 px-1 border-t border-gray-100 hover:bg-gray-50 text-left"
        @click="$emit('go-phase', phase.id)"
      >
        <span
          class="w-7 h-7 rounded-full shrink-0 flex items-center justify-center text-xs font-bold"
          :class="statusStyles[statusKey(phase, index)]?.icon"
        >
          {{ phase.phaseNumber }}
        </span>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-semibold text-gray-900 truncate">{{ phaseFallbackTitle(phase) }}</p>
          <p class="text-xs text-gray-500">
            {{ formatAmount(phase.amount) }} &middot; {{ t.projectWorkspace?.deadline }}
            {{ formatDate(phase.dueDate) }}
          </p>
        </div>
        <span class="text-xs font-semibold text-gray-500 shrink-0">
          {{ statusLabels[statusKey(phase, index)] }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { MapPin, Tag } from 'lucide-vue-next'
import { statusStyles } from './workspaceMaps'
import CurrentPhaseCard from './CurrentPhaseCard.vue'
import PhaseProgressChart from './PhaseProgressChart.vue'

const props = defineProps({
  t: { type: Object, required: true },
  isClient: { type: Boolean, default: true },
  project: { type: Object, default: null },
  coverImage: { type: String, default: null },
  phases: { type: Array, default: () => [] },
  sortedPhases: { type: Array, default: () => [] },
  needsAction: { type: Array, default: () => [] },
  totalAmount: { type: Number, default: 0 },
  progressPercent: { type: Number, default: 0 },
  disbursedCount: { type: Number, default: 0 },
  focusPhase: { type: Object, default: null },
  actionLoading: { type: [Number, String], default: null },
  disputeOpenFor: { type: [Number, String], default: null },
  disputeReason: { type: String, default: '' },
  statusKey: { type: Function, required: true },
  revisionsLeft: { type: Function, required: true },
  showRevisionBadge: { type: Function, required: true },
  deadlineLabel: { type: Function, required: true },
  deliverableItems: { type: Function, required: true },
  phaseDescription: { type: Function, required: true },
  phaseFallbackTitle: { type: Function, required: true },
  formatAmount: { type: Function, required: true },
  formatDate: { type: Function, required: true }
})
defineEmits([
  'go-contract',
  'go-phases',
  'go-phase',
  'approve-phase',
  'open-dispute',
  'cancel-dispute',
  'submit-dispute',
  'update:disputeReason',
  'submit-review'
])

const statusLabels = computed(() => props.t.projectWorkspace?.statusLabels || {})
</script>
