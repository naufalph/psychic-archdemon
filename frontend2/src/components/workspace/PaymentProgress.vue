<template>
  <div>
    <div class="flex h-2 rounded-full bg-gray-100 overflow-hidden">
      <div
        v-for="seg in segments"
        :key="seg.key"
        class="h-full transition-[width] duration-500 first:rounded-l-full last:rounded-r-full"
        :class="seg.bar"
        :style="{ width: `${animated ? seg.percent : 0}%` }"
      />
    </div>

    <div class="mt-3 flex flex-col gap-1.5">
      <div v-for="row in legend" :key="row.key" class="flex items-center gap-2 text-sm">
        <span class="w-2 h-2 rounded-full shrink-0" :class="row.dot" />
        <span class="text-gray-500 flex-1 min-w-0 truncate">{{ row.label }}</span>
        <span class="font-semibold text-gray-900 shrink-0">{{ formatAmount(row.amount) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  t: { type: Object, required: true },
  total: { type: Number, default: 0 },
  paid: { type: Number, default: 0 },
  disbursed: { type: Number, default: 0 },
  isClient: { type: Boolean, default: true },
  formatAmount: { type: Function, required: true }
})

/**
 * How far a payout has travelled is the architect's business, not the client's: the client is
 * only ever shown what has left their account. Splitting `paid` into disbursed + escrow is
 * therefore gated on role, not merely styled differently.
 */
const escrow = computed(() => Math.max(0, props.paid - props.disbursed))
const unfunded = computed(() => Math.max(0, props.total - props.paid))
const pct = amount => (props.total > 0 ? (amount / props.total) * 100 : 0)

const segments = computed(() =>
  props.isClient
    ? [{ key: 'paid', bar: 'bg-green-500', percent: pct(props.paid) }]
    : [
        { key: 'disbursed', bar: 'bg-green-500', percent: pct(props.disbursed) },
        { key: 'escrow', bar: 'bg-brand-gold', percent: pct(escrow.value) }
      ]
)

const legend = computed(() => {
  const w = props.t.projectWorkspace || {}
  const unfundedRow = {
    key: 'unfunded',
    dot: 'bg-gray-200',
    label: w.notYetFundedLabel,
    amount: unfunded.value
  }
  if (props.isClient) {
    return [{ key: 'paid', dot: 'bg-green-500', label: w.paidToDateLabel, amount: props.paid }, unfundedRow]
  }
  return [
    {
      key: 'disbursed',
      dot: 'bg-green-500',
      label: w.disbursedToYouLabel,
      amount: props.disbursed
    },
    { key: 'escrow', dot: 'bg-brand-gold', label: w.heldInEscrowLabel, amount: escrow.value },
    unfundedRow
  ]
})

// Start collapsed so the fill animates in on mount rather than appearing already full.
const animated = ref(false)
onMounted(() => requestAnimationFrame(() => (animated.value = true)))
</script>
