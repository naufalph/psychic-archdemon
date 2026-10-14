<template>
  <div class="max-w-[1280px] mx-auto px-4 md:px-6 pt-10 pb-16 flex flex-col gap-6">
    <nav class="flex items-center gap-1.5 text-caption font-normal text-ink-400 min-w-0">
      <RouterLink :to="crumbTo" class="hover:text-ink-900 transition shrink-0">{{ crumbLabel }}</RouterLink>
      <ChevronRight :size="14" :stroke-width="2.4" class="shrink-0" />
      <span class="text-ink-900 font-medium truncate">{{ title }}</span>
    </nav>
    <div class="grid grid-cols-1 lg:grid-cols-[minmax(0,1fr)_368px] gap-6 items-start">
      <slot />
      <slot name="rail" />
    </div>
  </div>
</template>

<script setup>
import { ChevronRight } from 'lucide-vue-next'

defineProps({
  crumbLabel: { type: String, required: true },
  crumbTo: { type: [String, Object], required: true },
  title: { type: String, default: '' }
})
</script>
