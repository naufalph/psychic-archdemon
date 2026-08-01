<template>
  <button
    type="button"
    class="w-full border-2 border-dashed border-hairline-alt rounded-card flex flex-col items-center justify-center gap-1.5 text-ink-300 transition-all duration-200 hover:border-ink-900 hover:text-ink-900"
    style="aspect-ratio: 4/3"
    @click="$emit('click')"
  >
    <Plus class="w-6 h-6" />
    <span class="text-micro font-semibold">{{ label }}</span>
  </button>
</template>

<script setup>
import { Plus } from 'lucide-vue-next'

defineProps({
  label: {
    type: String,
    default: 'Tambah Desain'
  }
})

defineEmits(['click'])
</script>
