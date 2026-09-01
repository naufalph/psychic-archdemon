<template>
  <div
    class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4"
    @click.self="$emit('close')"
  >
    <div
      role="dialog"
      aria-modal="true"
      :aria-label="t.projectWorkspace?.filesModalEyebrow"
      class="bg-white rounded-2xl shadow-2xl w-full max-w-[560px] flex flex-col max-h-[85vh]">
      <div class="px-6 py-5 border-b border-gray-100 flex items-start justify-between gap-4">
        <div class="min-w-0">
          <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
            {{ t.projectWorkspace?.filesModalEyebrow }}
          </p>
          <h3 class="text-base font-bold text-gray-900 truncate">{{ item?.name }}</h3>
          <p class="text-xs text-gray-500">
            {{
              (t.projectWorkspace?.filesTaggedTo || '{n} file(s) · {phase}')
                .replace('{n}', item?.files?.length || 0)
                .replace('{phase}', phaseTitle)
            }}
          </p>
        </div>
        <button class="text-gray-400 hover:text-gray-900 shrink-0" @click="$emit('close')">
          <X class="w-5 h-5" />
        </button>
      </div>

      <div class="px-6 py-4 overflow-y-auto flex flex-col gap-4">
        <div v-for="group in groups" :key="group.round">
          <p class="text-[10px] font-bold uppercase tracking-[0.08em] text-gray-400 mb-2">
            {{
              group.round === 0
                ? t.projectWorkspace?.initialDelivery
                : `${t.projectWorkspace?.revisionRound} ${group.round}`
            }}
          </p>
          <div
            v-for="file in group.files"
            :key="file.id"
            class="border border-border-gray rounded-lg px-3 py-2.5 mb-2 flex items-center gap-3"
          >
            <span
              class="w-9 h-9 rounded bg-gray-100 shrink-0 flex items-center justify-center text-[10px] font-bold text-gray-500"
            >
              {{ fileExtension(file.filePath) }}
            </span>
            <div class="min-w-0 flex-1">
              <p class="text-[13px] font-medium text-gray-900 truncate">
                {{ file.description || fileNameFromPath(file.filePath) }}
              </p>
              <p class="text-xs text-gray-400">{{ formatDateTime(file.uploadedAt) }}</p>
            </div>
            <button
              v-if="isImage(file.fileType)"
              class="px-3 py-1.5 rounded-full border border-border-gray hover:border-brand-gold text-xs font-semibold flex items-center gap-1.5"
              @click="$emit('preview', file)"
            >
              <Eye class="w-3.5 h-3.5" />
              {{ t.projectWorkspace?.previewBtn }}
            </button>
            <a
              :href="file.filePath"
              target="_blank"
              rel="noopener"
              class="text-xs font-semibold text-brand-brown hover:underline flex items-center gap-1.5"
            >
              <Download class="w-3.5 h-3.5" />
              {{ t.projectWorkspace?.downloadLabel }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { X, Eye, Download } from 'lucide-vue-next'
import { isImage, fileExtension, fileNameFromPath } from './workspaceMaps'

const props = defineProps({
  item: { type: Object, default: null },
  phaseTitle: { type: String, default: '' },
  t: { type: Object, required: true },
  formatDateTime: { type: Function, required: true },
  filesByRound: { type: Function, required: true }
})
defineEmits(['close', 'preview'])

const groups = computed(() => props.filesByRound(props.item))
</script>
