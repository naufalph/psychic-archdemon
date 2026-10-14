<template>
  <div>
    <h2 class="text-2xl font-bold text-black mb-4">{{ t.bidDetail.paymentSchedule }}</h2>

    <div class="flex items-center gap-3 mb-5 flex-wrap">
      <span
        class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-green-100 text-green-700 border border-green-200 cursor-default"
        :title="t.bidDetail.matchingDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-green-500 inline-block" />
        {{ t.bidDetail.matchingDeliverable }}
      </span>
      <span
        class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-blue-100 text-blue-700 border border-blue-200 cursor-default"
        :title="t.bidDetail.additionalDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-blue-500 inline-block" />
        {{ t.bidDetail.additionalDeliverable }}
      </span>
      <span
        class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-red-50 text-red-400 border border-red-200 cursor-default"
        :title="t.bidDetail.missingDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-red-300 inline-block" />
        {{ t.bidDetail.missingDeliverable }}
      </span>
    </div>

    <div class="space-y-3">
      <div
        v-for="phase in phases"
        :key="phase.phaseNumber"
        class="rounded-2xl border border-gray-100 p-4 bg-surface-alt"
      >
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-ink-900 text-white">
              {{ t.clientFinalization.phase }} {{ phase.phaseNumber }}
            </span>
            <span class="text-sm font-bold text-black">{{
              phase.title || `${t.clientFinalization.phase} ${phase.phaseNumber}`
            }}</span>
          </div>
          <span class="text-sm font-bold text-ink-900">{{ formatCurrency(phase.amount) }}</span>
        </div>
        <div v-if="phase.deliverables?.length" class="flex flex-wrap gap-1 mb-2">
          <span
            v-for="d in phase.deliverables"
            :key="d"
            :class="[
              'text-xs px-2 py-0.5 rounded-full border font-medium',
              isMatchingDeliverable(d)
                ? 'bg-green-100 text-green-700 border-green-200'
                : 'bg-blue-100 text-blue-700 border-blue-200'
            ]"
            :title="isMatchingDeliverable(d) ? t.bidDetail.matchingDeliverable : t.bidDetail.additionalDeliverable"
          >
            {{ t.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
          </span>
        </div>
        <div class="flex items-center gap-3 mt-1">
          <p v-if="phase.revisionRounds != null" class="text-xs text-gray-500">
            {{ phase.revisionRounds }}
            {{ phase.revisionRounds !== 1 ? t.clientFinalization.revisionRounds : t.clientFinalization.revisionRound }}
          </p>
          <p v-if="phase.estimatedDays" class="text-xs text-gray-500">
            {{ phase.estimatedDays }} {{ t.bidDetail.days }}
          </p>
        </div>
      </div>
    </div>

    <div v-if="missingDeliverables.length" class="mt-4 pt-4 border-t border-dashed border-gray-200">
      <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">
        {{ t.bidDetail.uncoveredTitle }}
      </p>
      <div class="flex flex-wrap gap-1">
        <span
          v-for="d in missingDeliverables"
          :key="d"
          class="text-xs px-2 py-0.5 rounded-full border bg-red-50 text-red-400 border-red-200 font-medium"
          :title="t.bidDetail.missingDeliverable"
        >
          {{ t.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
        </span>
      </div>
    </div>

    <div class="mt-3 pt-3 border-t border-gray-100 flex justify-between text-sm">
      <span class="text-gray-500 font-bold">{{ t.clientFinalization.total }}</span>
      <span class="font-bold text-ink-900">{{ formatCurrency(total) }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '@/composables/useI18n'

const props = defineProps({
  phases: { type: Array, required: true },
  projectDeliverables: { type: Array, default: () => [] },
  total: { type: [Number, String], default: null }
})

const { t } = useI18n()

const isMatchingDeliverable = d => props.projectDeliverables.includes(d)

const missingDeliverables = computed(() => {
  const covered = props.phases.flatMap(p => p.deliverables || [])
  return props.projectDeliverables.filter(d => !covered.includes(d))
})

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}
</script>
