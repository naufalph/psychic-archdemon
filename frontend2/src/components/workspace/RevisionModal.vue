<template>
  <div
    class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4"
    @click.self="$emit('close')"
  >
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-[448px] p-6">
      <h3 class="text-base font-bold text-gray-900">
        {{ t.projectWorkspace?.revisionModalTitle }}
      </h3>
      <p class="text-sm text-gray-500 mt-0.5">{{ t.projectWorkspace?.revisionModalHint }}</p>

      <textarea
        v-model="notes"
        rows="4"
        class="w-full mt-3 rounded-lg border border-border-gray p-2.5 text-sm"
        :placeholder="t.projectWorkspace?.revisionModalPlaceholder"
      />
      <p v-if="showError" class="text-xs text-red-500 mt-1">
        {{ t.projectWorkspace?.revisionModalRequired }}
      </p>

      <div class="flex items-center gap-2 mt-4">
        <button
          class="flex-1 px-4 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-white text-sm font-semibold disabled:opacity-50"
          :disabled="busy"
          @click="submit"
        >
          {{ busy ? t.projectWorkspace?.submitting : t.projectWorkspace?.revisionModalSubmit }}
        </button>
        <button class="px-4 py-2.5 text-sm text-gray-500" @click="$emit('close')">
          {{ t.projectWorkspace?.revisionModalCancel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  busy: { type: Boolean, default: false },
  t: { type: Object, required: true }
})
const emit = defineEmits(['close', 'submit'])

const notes = ref('')
const showError = ref(false)

const submit = () => {
  if (!notes.value.trim()) {
    showError.value = true
    return
  }
  emit('submit', notes.value)
}
</script>
