<template>
  <div class="flex flex-col gap-4">
    <!-- Payment schedule -->
    <div
      id="payment-schedule"
      class="bg-white border border-border-gray rounded-xl p-5 scroll-mt-24"
    >
      <div class="flex items-center justify-between mb-1">
        <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
          {{ t.projectWorkspace?.paymentScheduleTitle || t.projectWorkspace?.paymentPhasesTitle }}
        </p>
        <span class="text-xs text-gray-500">
          {{
            (t.projectWorkspace?.percentComplete || '{pct}% complete').replace('{pct}', percentPaid)
          }}
        </span>
      </div>
      <p class="text-sm text-gray-500 mb-4">{{ t.projectWorkspace?.contractLede }}</p>

      <div v-if="schedule.length" class="text-sm">
        <div
          class="grid gap-3 pb-2 border-b border-border-gray text-xs font-bold uppercase text-gray-400"
          :style="gridStyle"
        >
          <span>{{ t.projectWorkspace?.colPhase }}</span>
          <span>{{ t.projectWorkspace?.colDeadline }}</span>
          <span>{{ t.projectWorkspace?.colAmount }}</span>
          <span class="text-right">{{ t.projectWorkspace?.colStatus }}</span>
        </div>
        <div
          v-for="row in schedule"
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
          <span class="text-xs font-semibold text-gray-500 text-right">
            {{ statusLabels[row.status] || row.status }}
          </span>
        </div>
      </div>

      <div class="mt-4 pt-4 border-t border-border-gray">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-semibold text-gray-700">
            {{ t.projectWorkspace?.totalProjectValue }}
          </span>
          <span class="text-base font-bold text-gray-900">{{ formatAmount(totalValue) }}</span>
        </div>
        <div class="flex items-center gap-3">
          <div class="flex-1 h-1.5 rounded-full bg-gray-100 overflow-hidden">
            <div
              class="h-full bg-green-500 rounded-full transition-[width] duration-500"
              :style="{ width: `${animatedPercent}%` }"
            />
          </div>
          <span class="text-xs text-gray-500 shrink-0">
            {{ formatAmount(disbursedValue) }} {{ t.projectWorkspace?.disbursedLabel }}
          </span>
        </div>
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
            {{ winningBid.timelineDays }} {{ t.projectWorkspace?.workDaysSuffix }} &middot;
            {{ winningBid.phaseCount }} {{ t.projectWorkspace?.phasesWord || 'phases' }} &middot;
            {{ winningBid.revisionsPerPhase }} {{ t.projectWorkspace?.revisionsWord }}
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
        <div
          v-for="(tx, i) in transactions"
          :key="i"
          class="flex items-center gap-3 py-3 border-t border-gray-50"
        >
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
import { ref, computed, onMounted } from 'vue'
import { Clock } from 'lucide-vue-next'

const props = defineProps({
  t: { type: Object, required: true },
  contract: { type: Object, default: null },
  architectInitials: { type: String, default: '?' },
  formatAmount: { type: Function, required: true },
  formatDate: { type: Function, required: true },
  formatLogAction: { type: Function, required: true }
})

const gridStyle = 'grid-template-columns: 1.6fr 1fr 1fr 1.1fr'

const schedule = computed(() => props.contract?.paymentSchedule || [])
const transactions = computed(() => props.contract?.transactions || [])
const winningBid = computed(() => props.contract?.winningBid || null)
const totalValue = computed(() => Number(props.contract?.totalValue || 0))
const disbursedValue = computed(() => Number(props.contract?.disbursedValue || 0))
const paidValue = computed(() => Number(props.contract?.paidValue || 0))
const percentPaid = computed(() =>
  totalValue.value > 0 ? Math.round((paidValue.value / totalValue.value) * 100) : 0
)
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

const animatedPercent = ref(0)
onMounted(() =>
  requestAnimationFrame(() => {
    animatedPercent.value =
      totalValue.value > 0 ? (disbursedValue.value / totalValue.value) * 100 : 0
  })
)
</script>
