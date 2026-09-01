<template>
  <div class="fixed inset-0 z-[60] bg-black/95 flex flex-col" @click.self="$emit('close')">
    <div class="bg-black/80 px-6 py-3 flex items-center justify-between gap-4">
      <div class="flex items-center gap-3 min-w-0">
        <span class="text-xs font-bold text-gray-400">{{ index + 1 }} / {{ files.length }}</span>
        <span class="text-sm font-semibold text-white truncate">
          {{ fileNameFromPath(current?.filePath) }}
        </span>
      </div>
      <div class="flex items-center gap-3 shrink-0">
        <a
          :href="current?.filePath"
          target="_blank"
          rel="noopener"
          class="px-3 py-1.5 rounded-full border border-white/30 text-white text-xs font-semibold hover:bg-white/10"
        >
          {{ t.projectWorkspace?.downloadLabel }}
        </a>
        <button class="text-white/70 hover:text-white" @click="$emit('close')">
          <X class="w-5 h-5" />
        </button>
      </div>
    </div>

    <div class="flex-1 flex items-center justify-center gap-4 px-4 min-h-0">
      <button
        v-if="files.length > 1"
        class="p-3 rounded-full bg-black/50 text-white hover:bg-black/70 shrink-0"
        @click="$emit('step', -1)"
      >
        <ChevronLeft class="w-5 h-5" />
      </button>
      <img
        :src="current?.filePath"
        :alt="fileNameFromPath(current?.filePath)"
        class="max-h-full max-w-full object-contain"
      />
      <button
        v-if="files.length > 1"
        class="p-3 rounded-full bg-black/50 text-white hover:bg-black/70 shrink-0"
        @click="$emit('step', 1)"
      >
        <ChevronRight class="w-5 h-5" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { X, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { fileNameFromPath } from './workspaceMaps'

const props = defineProps({
  files: { type: Array, default: () => [] },
  index: { type: Number, default: 0 },
  t: { type: Object, required: true }
})
defineEmits(['close', 'step'])

const current = computed(() => props.files[props.index] || null)
</script>
