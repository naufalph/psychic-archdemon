<template>
  <div class="flex items-center justify-between gap-4 flex-wrap mt-10 pt-6 border-t border-hairline">
    <div class="flex">
      <slot name="start">
        <button
          type="button"
          class="h-[52px] inline-flex items-center gap-2 pl-3.5 pr-5 rounded-full text-body font-bold text-ink-900 hover:bg-surface-alt transition-colors"
          @click="$emit('back')"
        >
          <ChevronLeft :size="18" :stroke-width="2.2" /> Kembali
        </button>
      </slot>
    </div>

    <div class="flex items-center gap-4 flex-wrap ml-auto">
      <span v-if="disabled && hint" class="text-caption text-ink-400">{{ hint }}</span>
      <button
        type="button"
        class="h-[52px] px-8 rounded-full bg-ink-900 text-white text-[17px] font-bold inline-flex items-center gap-3 transition-opacity"
        :class="!disabled && !loading ? 'hover:opacity-85' : 'opacity-35 cursor-not-allowed'"
        :disabled="disabled || loading"
        @click="$emit('next')"
      >
        <span
          v-if="loading"
          class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
        ></span>
        {{ nextLabel }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ChevronLeft } from 'lucide-vue-next'

defineProps({
  nextLabel: { type: String, default: 'Lanjutkan' },
  hint: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false }
})
defineEmits(['back', 'next'])
</script>
