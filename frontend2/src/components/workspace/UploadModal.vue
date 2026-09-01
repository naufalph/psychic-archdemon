<template>
  <div
    class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4"
    @click.self="$emit('close')"
  >
    <div
      role="dialog"
      aria-modal="true"
      :aria-label="t.projectWorkspace?.uploadFileLabel"
      class="bg-white rounded-2xl shadow-2xl w-full max-w-[448px] p-6">
      <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
        {{ t.projectWorkspace?.uploadFileLabel }}
      </p>
      <h3 class="text-base font-bold text-gray-900 truncate">{{ item?.name }}</h3>

      <label
        class="mt-4 flex items-center gap-2 border border-border-gray rounded-lg px-3 py-2.5 cursor-pointer hover:border-brand-gold"
      >
        <input type="file" class="hidden" @change="onPick" />
        <Paperclip class="w-4 h-4 text-gray-400 shrink-0" />
        <span class="text-[13px] truncate" :class="file ? 'text-gray-900' : 'text-gray-400'">
          {{ file?.name || t.projectWorkspace?.chooseFileLabel }}
        </span>
      </label>

      <input
        v-model="description"
        class="w-full mt-2 rounded-lg border border-border-gray px-3 py-2.5 text-[13px]"
        :placeholder="t.projectWorkspace?.fileDescPlaceholder"
      />

      <div class="flex items-center gap-2 mt-4">
        <button
          class="flex-1 px-4 py-2.5 rounded-lg bg-ink-700 hover:bg-ink-500 text-white text-sm font-semibold disabled:opacity-50"
          :disabled="!file || busy"
          @click="$emit('submit', { file, description })"
        >
          {{ busy ? t.projectWorkspace?.uploading : t.projectWorkspace?.uploadBtn }}
        </button>
        <button class="px-4 py-2.5 text-sm text-gray-500" @click="$emit('close')">
          {{ t.projectWorkspace?.cancel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Paperclip } from 'lucide-vue-next'

defineProps({
  item: { type: Object, default: null },
  busy: { type: Boolean, default: false },
  t: { type: Object, required: true }
})
defineEmits(['close', 'submit'])

const file = ref(null)
const description = ref('')
const onPick = e => (file.value = e.target.files?.[0] || null)
</script>
