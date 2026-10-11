<template>
  <span
    v-if="on"
    class="flex-none w-[22px] h-[22px] rounded-md bg-ink-900 text-white flex items-center justify-center"
    :class="size === 'sm' ? '!w-5 !h-5' : ''"
  >
    <Check :size="size === 'sm' ? 13 : 14" :stroke-width="3.2" />
  </span>
  <span
    v-else
    class="flex-none w-[22px] h-[22px] box-border rounded-md border border-hairline bg-white"
    :class="size === 'sm' ? '!w-5 !h-5' : ''"
  ></span>
</template>

<script setup>
import { Check } from 'lucide-vue-next'

defineProps({
  on: { type: Boolean, default: false },
  size: { type: String, default: 'md' }
})
</script>
