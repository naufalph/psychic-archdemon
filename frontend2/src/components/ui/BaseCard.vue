<template>
  <div v-motion :initial="{ opacity: 1, y: 0 }" :hover="hoverEffect ? { y: -5 } : {}" :class="cardClasses">
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  hoverEffect: {
    type: Boolean,
    default: true
  }
})

const cardClasses = computed(() => {
  const baseClasses = 'bg-white rounded-3xl shadow-soft border border-gray-100 p-6 md:p-8 transition-all duration-300'
  const hoverClasses = props.hoverEffect ? 'hover:shadow-xl' : ''
  return `${baseClasses} ${hoverClasses}`
})
</script>
