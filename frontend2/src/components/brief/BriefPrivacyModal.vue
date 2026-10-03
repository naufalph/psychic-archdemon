<template>
  <BriefModal :open="open" panel-class="max-w-[560px] p-8" @close="$emit('close')">
    <div class="flex items-start justify-between gap-4 mb-6">
      <h2 class="text-[24px] font-bold tracking-[-.025em] m-0 text-ink-900">{{ tv.title }}</h2>
      <button
        type="button"
        :aria-label="tv.close"
        class="flex-none w-11 h-11 rounded-full border border-hairline bg-white text-ink-900 flex items-center justify-center hover:border-ink-900 transition-colors"
        @click="$emit('close')"
      >
        <X :size="18" :stroke-width="2.2" />
      </button>
    </div>
    <div class="flex flex-col">
      <div v-for="point in tv.points" :key="point.title" class="py-4 border-t border-hairline">
        <div class="text-[15px] font-bold tracking-[-.015em] text-ink-900 mb-1">{{ point.title }}</div>
        <p class="text-[15px] leading-relaxed text-ink-400 m-0">{{ point.desc }}</p>
      </div>
    </div>
    <button
      type="button"
      class="mt-4 w-full h-12 rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity"
      @click="$emit('close')"
    >
      {{ tv.ok }}
    </button>
  </BriefModal>
</template>

<script setup>
import { computed } from 'vue'
import { X } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import BriefModal from './BriefModal.vue'

defineProps({ open: { type: Boolean, default: false } })
defineEmits(['close'])

const { t } = useI18n()
const tv = computed(() => t.value.brief.privacy)
</script>
