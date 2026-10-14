<template>
  <span
    class="inline-flex items-center whitespace-nowrap rounded-full font-semibold text-ink-900"
    :class="[SIZES[size].pill, onImage ? 'bg-white/90 backdrop-blur-sm' : 'bg-surface-alt']"
  >
    <span class="rounded-full shrink-0" :class="[SIZES[size].dot, dotClass]" />
    {{ statusText }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '@/composables/useI18n'

/**
 * The one way a project status is shown. Statuses are told apart by the dot colour alone, so
 * every surface reads the same; only the size and the backdrop (over a photo or not) vary.
 */
const props = defineProps({
  status: {
    type: String,
    required: true
  },
  size: {
    type: String,
    default: 'md',
    validator: value => ['sm', 'md'].includes(value)
  },
  onImage: {
    type: Boolean,
    default: false
  }
})

const { t } = useI18n()

const SIZES = {
  sm: { pill: 'gap-1.5 px-2.5 py-1 text-micro', dot: 'w-1.5 h-1.5' },
  md: { pill: 'gap-2 h-[30px] px-3 text-caption-sm', dot: 'w-2 h-2' }
}

const WAITING = 'bg-amber-500'
const ENDED = 'bg-ink-300'

const DOT_COLORS = {
  DRAFT: WAITING,
  PENDING_APPROVAL: WAITING,
  NEGOTIATION: WAITING,
  OPEN: 'bg-brand-green',
  IN_PROGRESS: 'bg-accent-blue',
  COMPLETED: 'bg-ink-900',
  REJECTED: 'bg-red-500',
  BIDDING_CLOSED: ENDED,
  NEGOTIATION_EXPIRED: ENDED,
  CANCELLED: ENDED,
  DELETED: ENDED,
  // Legacy values kept for backwards compatibility
  PENDING: WAITING,
  AWARDED: 'bg-accent-blue',
  CLOSED: ENDED
}

const FALLBACK_TEXT = {
  DRAFT: 'Draft',
  PENDING_APPROVAL: 'Pending Validation',
  OPEN: 'Open for Bidding',
  BIDDING_CLOSED: 'Bidding Closed',
  NEGOTIATION: 'Finalization',
  NEGOTIATION_EXPIRED: 'Finalization Expired',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  REJECTED: 'Rejected',
  DELETED: 'Deleted',
  CLOSED: 'Closed',
  AWARDED: 'Awarded',
  PENDING: 'Pending Validation'
}

const dotClass = computed(() => DOT_COLORS[props.status] || ENDED)

const statusText = computed(() => t.value.projectStatus?.[props.status] || FALLBACK_TEXT[props.status] || props.status)
</script>
