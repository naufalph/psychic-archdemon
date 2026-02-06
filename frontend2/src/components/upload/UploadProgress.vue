<template>
  <div v-if="progress > 0 && progress < 100" class="w-full">
    <div class="flex items-center justify-between mb-2">
      <span class="text-sm font-medium text-gray-700">{{ label || 'Uploading...' }}</span>
      <span class="text-sm font-bold text-[#7C4728]">{{ progress }}%</span>
    </div>
    <div class="w-full h-2 bg-gray-200 rounded-full overflow-hidden">
      <div class="h-full bg-[#7C4728] transition-all duration-300 rounded-full" :style="{ width: `${progress}%` }" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  progress: {
    type: Number,
    required: true,
    validator: value => value >= 0 && value <= 100
  },
  label: {
    type: String,
    default: ''
  }
})
</script>
