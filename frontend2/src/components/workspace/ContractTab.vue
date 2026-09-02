<template>
  <div class="flex flex-col gap-4">
    <!-- Payment schedule -->
    <div id="payment-schedule" class="bg-white border border-border-gray rounded-xl p-5 scroll-mt-24">
      <div class="flex items-center justify-between mb-1">
        <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
          {{ t.projectWorkspace?.paymentScheduleTitle || t.projectWorkspace?.paymentPhasesTitle }}
        </p>
        <span class="text-xs text-gray-500">
          {{ (t.projectWorkspace?.percentComplete || '{pct}% complete').replace('{pct}', percentPaid) }}
        </span>
      </div>
      <p class="text-sm text-gray-500 mb-4">{{ t.projectWorkspace?.contractLede }}</p>

      <div v-if="schedule.length" class="text-sm overflow-x-auto">
        <div class="min-w-[600px]">
          <div
            class="grid gap-3 pb-2 border-b border-border-gray text-xs font-bold uppercase text-gray-400"
            :style="gridStyle"
          >
            <span>{{ t.projectWorkspace?.colPhase }}</span>
            <span>{{ t.projectWorkspace?.colDeadline }}</span>
            <span>{{ t.projectWorkspace?.colAmount }}</span>
            <span>{{ t.projectWorkspace?.colStatus }}</span>
            <span class="text-right">{{ t.projectWorkspace?.colAction }}</span>
          </div>
          <div
            v-for="(row, index) in schedule"
            :key="row.phaseId"
            class="grid gap-3 py-3.5 border-b border-gray-50 items-center"
            :style="gridStyle"
          >
            <div class="min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">{{ row.title }}</p>
              <p class="text-xs text-gray-400">
                {{
                  (t.projectWorkspace?.shareOfContract || '{share}% of contract value').replace(
                    '{share}',
                    Math.round(Number(row.share || 0))
                  )
                }}
              </p>
            </div>
            <span class="text-sm text-gray-600">{{ formatDate(row.dueDate) }}</span>
            <span class="text-sm font-semibold text-gray-900">{{ formatAmount(row.amount) }}</span>
            <span class="text-xs font-semibold text-gray-500">
              {{ statusLabels[rowStatus(row, index)] || row.status }}
            </span>
            <div class="text-right min-w-0">
              <button
                v-if="actionFor(row, index)"
                class="px-3.5 py-1.5 rounded-full text-xs font-semibold text-white disabled:opacity-50"
                :class="actionFor(row, index).class"
                :disabled="busy === row.phaseId"
                :aria-label="`${actionFor(row, index).label} · ${row.title}`"
                @click="$emit(actionFor(row, index).event, row)"
              >
                {{ busy === row.phaseId ? t.projectWorkspace?.submitting : actionFor(row, index).label }}
              </button>
              <span v-else class="text-gray-300">&mdash;</span>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-4 pt-4 border-t border-border-gray">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm font-semibold text-gray-700">
            {{ t.projectWorkspace?.totalProjectValue }}
          </span>
          <span class="text-base font-bold text-gray-900">{{ formatAmount(totalValue) }}</span>
        </div>
        <PaymentProgress
          :t="t"
          :total="totalValue"
          :paid="paidValue"
          :disbursed="disbursedValue"
          :is-client="isClient"
          :format-amount="formatAmount"
        />
      </div>
    </div>

    <!-- Agreement terms -->
    <div class="bg-white border border-border-gray rounded-xl p-5">
      <p class="text-xs font-bold uppercase tracking-wider text-gray-400 mb-3">
        {{ t.projectWorkspace?.agreementTermsLabel }}
      </p>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div v-for="term in terms" :key="term.label" class="bg-gray-50 rounded-lg px-4 py-3">
          <p class="text-xs text-gray-400">{{ term.label }}</p>
          <p class="text-sm font-semibold text-gray-800">{{ term.value }}</p>
        </div>
      </div>

      <div
        v-if="winningBid"
        class="mt-4 rounded-xl p-4 border"
        style="background: rgba(245, 230, 211, 0.4); border-color: rgba(197, 161, 122, 0.3)"
      >
        <div class="flex items-center gap-3">
          <span
            class="w-10 h-10 rounded-full bg-ink-700 text-white shrink-0 flex items-center justify-center text-sm font-bold"
          >
            {{ architectInitials }}
          </span>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-gray-900 truncate">
              {{ winningBid.architectName }}
            </p>
            <p class="text-xs text-gray-500 truncate">
              {{ [winningBid.companyName, winningBid.city].filter(Boolean).join(' · ') }}
            </p>
          </div>
          <div class="text-right shrink-0">
            <p class="text-xs text-gray-400">{{ t.projectWorkspace?.bidAmountLabel }}</p>
            <p class="font-bold text-brand-brown">{{ formatAmount(winningBid.bidAmount) }}</p>
          </div>
        </div>
        <div
          class="mt-3 pt-3 flex items-center gap-2 text-xs text-gray-600"
          style="border-top: 1px solid rgba(197, 161, 122, 0.2)"
        >
          <Clock class="w-3.5 h-3.5" />
          <span>
            {{ winningBid.timelineDays }} {{ t.projectWorkspace?.workDaysSuffix }} &middot; {{ winningBid.phaseCount }}
            {{ t.projectWorkspace?.phasesWord || 'phases' }} &middot; {{ winningBid.revisionsPerPhase }}
            {{ t.projectWorkspace?.revisionsWord }}
          </span>
        </div>
      </div>
    </div>

    <!-- Transaction history -->
    <div class="bg-white border border-border-gray rounded-xl p-5">
      <p class="text-xs font-bold uppercase tracking-wider text-gray-400 mb-1">
        {{ t.projectWorkspace?.transactionHistoryLabel }}
      </p>
      <div v-if="transactions.length">
        <div v-for="(tx, i) in transactions" :key="i" class="flex items-center gap-3 py-3 border-t border-gray-50">
          <span class="text-xs text-gray-400 w-[88px] shrink-0">{{ formatDate(tx.at) }}</span>
          <span class="text-sm text-gray-700 flex-1 min-w-0 truncate">
            {{ formatLogAction(tx.action) }}
            <span v-if="tx.phaseTitle" class="text-gray-400">&middot; {{ tx.phaseTitle }}</span>
          </span>
          <span v-if="tx.reference" class="text-xs text-gray-400 shrink-0 hidden sm:inline">
            {{ tx.reference }}
          </span>
          <span
            class="text-sm font-semibold shrink-0"
            :class="tx.direction === 'OUT' ? 'text-gray-900' : 'text-green-700'"
          >
            {{ tx.direction === 'OUT' ? '−' : '+' }} {{ formatAmount(tx.amount) }}
          </span>
        </div>
      </div>
      <p v-else class="text-sm text-gray-400 py-3">{{ t.projectWorkspace?.noTransactions }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Clock } from 'lucide-vue-next'
import PaymentProgress from './PaymentProgress.vue'

const props = defineProps({
  t: { type: Object, required: true },
  isClient: { type: Boolean, default: true },
  busy: { type: [Number, String], default: null },
  contract: { type: Object, default: null },
  architectInitials: { type: String, default: '?' },
  formatAmount: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  formatLogAction: { type: Function, required: true }
})

defineEmits(['create-invoice', 'pay-now', 'request-payout'])

const gridStyle = 'grid-template-columns: 1.7fr 0.9fr 1fr 0.9fr 1.1fr'

const schedule = computed(() => props.contract?.paymentSchedule || [])
const transactions = computed(() => props.contract?.transactions || [])
const winningBid = computed(() => props.contract?.winningBid || null)
const totalValue = computed(() => Number(props.contract?.totalValue || 0))
const disbursedValue = computed(() => Number(props.contract?.disbursedValue || 0))
const paidValue = computed(() => Number(props.contract?.paidValue || 0))
const percentPaid = computed(() => (totalValue.value > 0 ? Math.round((paidValue.value / totalValue.value) * 100) : 0))
const statusLabels = computed(() => props.t.projectWorkspace?.statusLabels || {})

const terms = computed(() => {
  const w = props.t.projectWorkspace || {}
  const a = props.contract?.agreementTerms
  if (!a) return []
  const fmt = (tpl, n) => (tpl || '{n}').replace('{n}', n)
  return [
    { label: w.termScope, value: a.scopeOfWork || '-' },
    { label: w.termFee, value: w.termFeeValue },
    { label: w.termRevisions, value: fmt(w.termRevisionsValue, a.revisionsPerPhase) },
    { label: w.termTimeline, value: fmt(w.termTimelineValue, a.timelineDays) },
    { label: w.termIp, value: w.termIpValue },
    { label: w.termDispute, value: w.termDisputeValue }
  ]
})

/**
 * Phases are billed in order, so a PENDING row whose predecessors are still open is not
 * started rather than payable -- the same derivation the phase accordion makes.
 */
const rowStatus = (row, index) =>
  row.status === 'PENDING' && schedule.value.slice(0, index).some(r => !['APPROVED', 'DISBURSED'].includes(r.status))
    ? 'NOT_STARTED'
    : row.status

/**
 * Every money action lives here rather than on the phase accordion, so the schedule row is the
 * one place a phase is billed, paid or disbursed.
 */
const actionFor = (row, index) => {
  const w = props.t.projectWorkspace || {}
  const status = rowStatus(row, index)
  if (props.isClient) {
    if (status === 'PENDING')
      return { label: w.createInvoice, class: 'bg-ink-700 hover:bg-ink-500', event: 'create-invoice' }
    if (status === 'BILLED') return { label: w.payNow, class: 'bg-blue-700 hover:bg-blue-900', event: 'pay-now' }
    return null
  }
  if (status === 'APPROVED')
    return { label: w.requestPayout, class: 'bg-green-600 hover:bg-green-700', event: 'request-payout' }
  return null
}
</script>
