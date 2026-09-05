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
      <button
        class="w-full px-4 py-2.5 rounded-lg bg-green-600 hover:bg-green-700 text-white text-sm font-semibold flex items-center justify-center gap-2 disabled:opacity-50"
        :disabled="busy"
        @click="$emit('approve-phase')"
      >
        <ThumbsUp class="w-4 h-4" />
        {{ t.projectWorkspace?.approveBtn }}
      </button>
      <!-- Revisions are composed per deliverable in the table below, not from a phase-level button. -->
      <p v-if="revisionsLeft > 0" class="text-xs text-gray-500 mt-2 text-center">
        {{ t.projectWorkspace?.reviseFromTableHint }}
      </p>
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

    <!--
      The delivery window: it runs from the day the client funded the phase until every
      deliverable is uploaded, so it belongs next to the upload progress rather than in the header.
    -->
    <div v-if="showCountdown" class="mt-3 flex items-baseline gap-2">
      <Clock class="w-4 h-4 self-center shrink-0" :class="overdue ? 'text-red-600' : 'text-gray-500'" />
      <span class="text-sm font-bold" :class="overdue ? 'text-red-600' : 'text-gray-800'">
        {{ deadlineLabel }}
      </span>
      <span class="text-xs text-gray-500 truncate">
        {{
          dueDate
            ? (t.projectWorkspace?.dueOnLabel || 'due {date}').replace('{date}', formatDate(dueDate))
            : t.projectWorkspace?.noDueDate
        }}
      </span>
    </div>

    <!-- Deliverable readiness: uploading a file is not a submission, so say what is still missing -->
    <div v-if="showReadiness" class="mt-3">
      <div class="flex items-center justify-between text-xs mb-1">
        <span class="font-semibold text-gray-600">
          {{
            (t.projectWorkspace?.deliverablesUploadedCount || '{done} of {total} uploaded')
              .replace('{done}', uploadedCount)
              .replace('{total}', deliverables.length)
          }}
        </span>
      </div>
      <div class="h-1.5 rounded-full bg-gray-100 overflow-hidden">
        <div
          class="h-full rounded-full transition-[width] duration-500"
          :class="allUploaded ? 'bg-green-500' : 'bg-brand-gold'"
          :style="{ width: `${(uploadedCount / deliverables.length) * 100}%` }"
        />
      </div>
      <p class="text-xs text-amber-700 font-semibold mt-2 flex items-center gap-1.5">
        <AlertTriangle class="w-3.5 h-3.5 shrink-0" />
        {{ readinessHint }}
      </p>
    </div>

    <!-- Single-button states -->
    <button
      v-if="panel.button"
      class="mt-3 px-4 py-2.5 rounded-lg text-sm font-semibold text-white disabled:opacity-50 disabled:cursor-not-allowed"
      :class="panel.button.class"
      :disabled="busy || submitBlocked"
      @click="$emit(panel.button.event)"
    >
      {{ busy ? t.projectWorkspace?.submitting : panel.button.label }}
    </button>

    <!-- Money states hand off to the contract tab, which owns invoicing and disbursement -->
    <button
      v-if="panel.link"
      class="mt-3 inline-flex items-center gap-1.5 text-sm font-semibold text-brand-brown hover:underline"
      @click="$emit('go-contract')"
    >
      {{ panel.link }}
      <ArrowRight class="w-3.5 h-3.5" />
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Lock, CheckCircle, ThumbsUp, AlertTriangle, ArrowRight, Clock } from 'lucide-vue-next'

const props = defineProps({
  statusKey: { type: String, required: true },
  isClient: { type: Boolean, default: true },
  deliverables: { type: Array, default: () => [] },
  revisionsLeft: { type: Number, default: 0 },
  busy: { type: Boolean, default: false },
  disputeOpen: { type: Boolean, default: false },
  disputeReason: { type: String, default: '' },
  dueDate: { type: String, default: null },
  deadlineLabel: { type: String, default: '' },
  formatDate: { type: Function, required: true },
  t: { type: Object, required: true }
})
defineEmits([
  'approve-phase',
  'open-dispute',
  'cancel-dispute',
  'submit-dispute',
  'update:disputeReason',
  'create-invoice',
  'pay-now',
  'submit-review',
  'request-payout',
  'go-contract'
])

const showDeliveredActions = computed(() => props.isClient && props.statusKey === 'DELIVERED')

const uploadedCount = computed(() => props.deliverables.filter(d => d.files?.length).length)
const allUploaded = computed(() => props.deliverables.length > 0 && uploadedCount.value === props.deliverables.length)
const showReadiness = computed(
  () => !props.isClient && props.statusKey === 'IN_PROGRESS' && props.deliverables.length > 0
)

const showCountdown = computed(() => props.statusKey === 'IN_PROGRESS')
const overdue = computed(() => {
  if (!props.dueDate) return false
  const due = new Date(props.dueDate)
  due.setHours(0, 0, 0, 0)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return due < today
})

/**
 * A phase whose bid named its deliverables delivers itself when the last one is uploaded, so
 * there is no button to press. Only a phase with no named list still needs a manual submission.
 */
const hasNamedDeliverables = computed(() => props.deliverables.some(d => d.index !== null))
const submitBlocked = computed(() => showReadiness.value && uploadedCount.value === 0)

const readinessHint = computed(() => {
  const w = props.t.projectWorkspace || {}
  if (uploadedCount.value === 0) return w.uploadAtLeastOne
  if (!allUploaded.value)
    return (w.deliverablesMissingCount || '{n} not uploaded').replace(
      '{n}',
      props.deliverables.length - uploadedCount.value
    )
  return w.notSubmittedYet
})

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
      link: c ? w.createInvoiceInContract : null
    },
    BILLED: {
      bg: 'bg-blue-50',
      border: 'border-blue-200',
      text: 'text-blue-700',
      title: w.invoiceSentTitle,
      desc: w.invoiceSentDesc,
      link: c ? w.payInContract : null
    },
    IN_PROGRESS: {
      bg: 'bg-sky-50',
      border: 'border-sky-200',
      text: 'text-sky-700',
      title: c ? w.workInProgressTitle : w.workPhaseActiveTitle,
      desc: c
        ? w.clientCountdownDesc || w.workInProgressDesc
        : hasNamedDeliverables.value
          ? w.autoDeliverDesc
          : w.markCompleteDesc || w.workInProgressDesc,
      button:
        c || hasNamedDeliverables.value
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
      link: c ? null : w.payoutInContract
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
