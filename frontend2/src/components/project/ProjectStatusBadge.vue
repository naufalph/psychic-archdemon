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
    required: true,
    validator: value => ['OPEN', 'CLOSED', 'AWARDED', 'PENDING'].includes(value)
  }
})

const statusConfig = {
  OPEN: {
    text: 'Open for Bidding',
    classes: 'bg-green-100 text-green-700 border-green-200'
  },
  CLOSED: {
    text: 'Closed',
    classes: 'bg-gray-100 text-gray-700 border-gray-200'
  },
  AWARDED: {
    text: 'Awarded',
    classes: 'bg-[#7C4728] text-white border-[#7C4728]'
  },
  PENDING: {
    text: 'Pending Validation',
    classes: 'bg-yellow-100 text-yellow-700 border-yellow-200'
  }
}

const statusText = computed(() => statusConfig[props.status]?.text || props.status)

const badgeClasses = computed(() => {
  const baseClasses = 'inline-flex items-center px-4 py-1.5 rounded-full text-xs font-bold border'
  const statusClasses = statusConfig[props.status]?.classes || 'bg-gray-100 text-gray-700'
  return `${baseClasses} ${statusClasses}`
})
</script>
