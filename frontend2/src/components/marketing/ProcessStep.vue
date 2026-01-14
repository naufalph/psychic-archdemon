<template>
  <div class="flex gap-6">
    <div class="flex-shrink-0">
      <div class="w-12 h-12 rounded-full bg-brand-green text-white flex items-center justify-center font-bold text-lg">
        {{ stepNumber }}
      </div>
    </div>
    <div class="flex-1">
      <div class="flex items-center gap-3 mb-3">
        <component v-if="icon" :is="icon" :size="24" class="text-brand-green" />
        <h3 class="text-lg font-bold text-gray-900">{{ title }}</h3>
      </div>
      <p class="text-gray-600 leading-relaxed">{{ description }}</p>
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  stepNumber: {
    type: Number,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  icon: {
    type: Object,
    default: null
  }
})
</script>
