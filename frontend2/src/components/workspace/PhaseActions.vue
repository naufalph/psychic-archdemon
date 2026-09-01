<template>
  <div v-if="panel" class="p-3 rounded-lg border" :class="[panel.bg, panel.border]">
    <div class="flex items-start gap-2">
      <component :is="panel.icon" v-if="panel.icon" class="w-4 h-4 mt-0.5 shrink-0" :class="panel.text" />
      <div class="flex-1 min-w-0">
        <p class="text-sm font-semibold" :class="panel.text">{{ panel.title }}</p>
        <p class="text-xs leading-relaxed mt-0.5" :class="panel.text">{{ panel.desc }}</p>
      </div>
    </div>

    <!-- Client, work delivered: approve / revise / dispute -->
    <div v-if="showDeliveredActions && !disputeOpen" class="mt-3">
      <div class="flex flex-wrap gap-2">
        <button
          class="flex-1 min-w-[140px] px-4 py-2.5 rounded-lg bg-green-600 hover:bg-green-700 text-white text-sm font-semibold flex items-center justify-center gap-2 disabled:opacity-50"
          :disabled="busy"
          @click="$emit('approve-phase')"
        >
          <ThumbsUp class="w-4 h-4" />
          {{ t.projectWorkspace?.approveBtn }}
        </button>
        <button
          v-if="revisionsLeft > 0"
          class="flex-1 min-w-[140px] px-4 py-2.5 rounded-lg bg-white border-2 border-amber-300 text-amber-700 hover:bg-amber-50 text-sm font-semibold flex items-center justify-center gap-2"
          @click="$emit('request-revision')"
        >
          <RotateCcw class="w-4 h-4" />
          {{ t.projectWorkspace?.requestRevisionBtn }}
        </button>
      </div>
      <button
        class="w-full mt-2 px-3 py-1.5 rounded-lg border border-red-200 text-red-500 hover:bg-red-50 text-xs flex items-center justify-center gap-1.5"
        @click="$emit('open-dispute')"
      >
        <AlertTriangle class="w-3 h-3" />
        {{ t.projectWorkspace?.disputeBtn }}
      </button>
    </div>

    <!-- Inline dispute form, not a modal -->
    <div v-if="showDeliveredActions && disputeOpen" class="mt-3">
      <label class="text-xs font-bold uppercase tracking-wider text-gray-500">
        {{ t.projectWorkspace?.disputeReasonLabel }}
      </label>
      <textarea
        :value="disputeReason"
        rows="3"
        class="w-full mt-1 rounded-lg border border-border-gray p-2 text-sm"
        :placeholder="t.projectWorkspace?.disputeReasonPlaceholder"
        @input="$emit('update:disputeReason', $event.target.value)"
      />
      <div class="flex gap-2 mt-2">
        <button
          class="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-semibold disabled:opacity-50"
          :disabled="busy"
          @click="$emit('submit-dispute')"
        >
          {{ busy ? t.projectWorkspace?.submitting : t.projectWorkspace?.submitDispute }}
        </button>
        <button class="px-4 py-2 text-sm text-gray-500" @click="$emit('cancel-dispute')">
          {{ t.projectWorkspace?.cancel }}
        </button>
      </div>
    </div>

    <!-- Single-button states -->
    <button
      v-if="panel.button"
      class="mt-3 px-4 py-2.5 rounded-lg text-sm font-semibold text-white disabled:opacity-50"
      :class="panel.button.class"
      :disabled="busy"
      @click="$emit(panel.button.event)"
    >
      {{ busy ? t.projectWorkspace?.submitting : panel.button.label }}
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Lock, CheckCircle, ThumbsUp, RotateCcw, AlertTriangle } from 'lucide-vue-next'

const props = defineProps({
  statusKey: { type: String, required: true },
  isClient: { type: Boolean, default: true },
  revisionsLeft: { type: Number, default: 0 },
  busy: { type: Boolean, default: false },
  disputeOpen: { type: Boolean, default: false },
  disputeReason: { type: String, default: '' },
  t: { type: Object, required: true }
})
defineEmits([
  'approve-phase',
  'request-revision',
  'open-dispute',
  'cancel-dispute',
  'submit-dispute',
  'update:disputeReason',
  'create-invoice',
  'pay-now',
  'submit-review',
  'request-payout'
])

const showDeliveredActions = computed(() => props.isClient && props.statusKey === 'DELIVERED')

/** One panel per (status x role); the handoff's action matrix expressed once. */
const panel = computed(() => {
  const w = props.t.projectWorkspace || {}
  const c = props.isClient
  const map = {
    NOT_STARTED: {
      bg: 'bg-gray-50',
      border: 'border-border-gray',
      text: 'text-gray-500',
      icon: Lock,
      title: w.notStartedTitle,
      desc: w.notStartedDesc
    },
    PENDING: {
      bg: 'bg-amber-50',
      border: 'border-amber-200',
      text: 'text-amber-700',
      title: c ? w.paymentRequiredTitle : w.awaitingClientPaymentTitle || w.paymentRequiredTitle,
      desc: c ? w.paymentRequiredDesc : w.awaitingClientPaymentDesc || w.paymentRequiredDesc,
      button: c
        ? { label: w.createInvoice, class: 'bg-ink-700 hover:bg-ink-500', event: 'create-invoice' }
        : null
    },
    BILLED: {
      bg: 'bg-blue-50',
      border: 'border-blue-200',
      text: 'text-blue-700',
      title: w.invoiceSentTitle,
      desc: w.invoiceSentDesc,
      button: c
        ? { label: w.payNow, class: 'bg-blue-700 hover:bg-blue-900', event: 'pay-now' }
        : null
    },
    IN_PROGRESS: {
      bg: 'bg-sky-50',
      border: 'border-sky-200',
      text: 'text-sky-700',
      title: c ? w.workInProgressTitle : w.workPhaseActiveTitle,
      desc: c ? w.workInProgressDesc : w.markCompleteDesc || w.workInProgressDesc,
      button: c
        ? null
        : { label: w.submitForReviewBtn, class: 'bg-ink-700 hover:bg-ink-500', event: 'submit-review' }
    },
    DELIVERED: {
      bg: 'bg-purple-50',
      border: 'border-purple-200',
      text: 'text-purple-700',
      title: c ? w.workSubmittedTitle : w.underClientReviewTitle,
      desc: c ? w.workSubmittedDesc : w.underClientReviewDesc
    },
    APPROVED: {
      bg: 'bg-green-50',
      border: 'border-green-200',
      text: 'text-green-700',
      title: c ? w.workApprovedTitle : w.workApprovedExclaim,
      desc: c ? w.workApprovedDesc : w.workApprovedArchitectDesc || w.workApprovedDesc,
      button: c
        ? null
        : { label: w.requestPayout, class: 'bg-green-600 hover:bg-green-700', event: 'request-payout' }
    },
    DISPUTED: {
      bg: 'bg-red-50',
      border: 'border-red-200',
      text: 'text-red-700',
      icon: AlertTriangle,
      title: w.underDisputeTitle,
      desc: w.underDisputeDesc
    },
    DISBURSED: {
      bg: 'bg-gray-50',
      border: 'border-border-gray',
      text: 'text-gray-500',
      icon: CheckCircle,
      title: w.phaseCompleteDisbursed,
      desc: ''
    }
  }
  return map[props.statusKey] || null
})
</script>
