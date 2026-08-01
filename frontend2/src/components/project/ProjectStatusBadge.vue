<template>
  <span :class="badgeClasses">
    {{ statusText }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const statusConfig = {
  PENDING_APPROVAL: {
    text: 'Pending Validation',
    classes: 'bg-yellow-100 text-yellow-700 border-yellow-200'
  },
  OPEN: {
    text: 'Open for Bidding',
    classes: 'bg-green-100 text-green-700 border-green-200'
  },
  BIDDING_CLOSED: {
    text: 'Bidding Closed',
    classes: 'bg-gray-100 text-gray-700 border-gray-200'
  },
  NEGOTIATION: {
    text: 'Finalization',
    classes: 'bg-amber-100 text-amber-700 border-amber-200'
  },
  IN_PROGRESS: {
    text: 'In Progress',
    classes: 'bg-blue-100 text-blue-700 border-blue-200'
  },
  COMPLETED: {
    text: 'Completed',
    classes: 'bg-brand-brown text-white border-brand-brown'
  },
  CANCELLED: {
    text: 'Cancelled',
    classes: 'bg-gray-100 text-gray-500 border-gray-200'
  },
  REJECTED: {
    text: 'Rejected',
    classes: 'bg-red-100 text-red-700 border-red-200'
  },
  // Legacy values kept for backwards compatibility
  CLOSED: { text: 'Closed', classes: 'bg-gray-100 text-gray-700 border-gray-200' },
  AWARDED: { text: 'Awarded', classes: 'bg-brand-brown text-white border-brand-brown' },
  PENDING: { text: 'Pending Validation', classes: 'bg-yellow-100 text-yellow-700 border-yellow-200' }
}

const statusText = computed(() => statusConfig[props.status]?.text || props.status)

const badgeClasses = computed(() => {
  const baseClasses = 'inline-flex items-center px-4 py-1.5 rounded-full text-xs font-bold border'
  const statusClasses = statusConfig[props.status]?.classes || 'bg-gray-100 text-gray-700'
  return `${baseClasses} ${statusClasses}`
})
</script>
