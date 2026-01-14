<template>
  <div v-if="deadline" :class="countdownClasses">
    <Clock :size="16" />
    <span v-if="!remaining.expired">
      {{ remaining.days }}d {{ remaining.hours }}h {{ remaining.minutes }}m {{ remaining.seconds }}s
    </span>
    <span v-else class="font-bold">Bidding Closed</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Clock } from 'lucide-vue-next'
import { useCountdown } from '@/composables/useCountdown'

const props = defineProps({
  deadline: {
    type: [String, Date],
    required: true
  },
  size: {
    type: String,
    default: 'sm',
    validator: value => ['sm', 'md', 'lg'].includes(value)
  }
})

const { remaining } = useCountdown(props.deadline)

const countdownClasses = computed(() => {
  const baseClasses = 'flex items-center gap-2 font-medium'
  const sizeClasses = {
    sm: 'text-xs',
    md: 'text-sm',
    lg: 'text-base'
  }
  const colorClasses = remaining.value.expired ? 'text-red-600' : 'text-gray-600'
  return `${baseClasses} ${sizeClasses[props.size]} ${colorClasses}`
})
</script>
