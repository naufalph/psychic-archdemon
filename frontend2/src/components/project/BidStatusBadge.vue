<template>
  <span :class="badgeClasses" class="px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap">
    {{ badgeText }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: value => ['DRAFT', 'PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'].includes(value)
  }
})

const badgeClasses = computed(() => {
  switch (props.status) {
    case 'DRAFT':
      return 'bg-yellow-100 text-yellow-800 border border-yellow-300'
    case 'PENDING':
      return 'bg-blue-100 text-blue-800 border border-blue-300'
    case 'ACCEPTED':
      return 'bg-green-100 text-green-800 border border-green-300'
    case 'REJECTED':
      return 'bg-red-100 text-red-800 border border-red-300'
    case 'WITHDRAWN':
      return 'bg-gray-100 text-gray-600 border border-gray-300'
    default:
      return 'bg-gray-100 text-gray-800 border border-gray-300'
  }
})

const badgeText = computed(() => t.value.bidStatus?.[props.status] || props.status)
</script>
