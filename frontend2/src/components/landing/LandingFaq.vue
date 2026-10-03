<template>
  <div class="border-t border-ink-200">
    <div v-for="(item, i) in items" :key="item.q" class="border-b border-ink-200">
      <button
        class="w-full flex justify-between items-center gap-6 text-left py-6 bg-transparent border-none text-body-lg font-semibold text-ink-900 cursor-pointer"
        @click="openIndex = openIndex === i ? -1 : i"
      >
        {{ item.q }}
        <span class="flex-shrink-0 text-2xl font-normal text-ink-500">{{ openIndex === i ? '−' : '+' }}</span>
      </button>
      <p v-if="openIndex === i" class="text-body-lg text-ink-500 leading-relaxed mb-6 mt-0">
        {{ item.a }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  items: {
    type: Array,
    required: true
  }
})

const openIndex = ref(0)
</script>
