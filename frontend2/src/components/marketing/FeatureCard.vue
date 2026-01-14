<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :visible="{ opacity: 1, y: 0 }"
    :delay="delay"
    class="bg-white rounded-3xl shadow-soft border border-gray-100 p-8 hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
  >
    <div class="w-14 h-14 rounded-2xl bg-brand-tan flex items-center justify-center mb-6">
      <component :is="icon" :size="28" class="text-brand-brown" />
    </div>
    <h3 class="text-xl font-bold mb-3 text-gray-900">{{ title }}</h3>
    <p class="text-gray-600 leading-relaxed">{{ description }}</p>
  </div>
</template>

<script setup>
defineProps({
  icon: {
    type: Object,
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
  delay: {
    type: Number,
    default: 0
  }
})
</script>
