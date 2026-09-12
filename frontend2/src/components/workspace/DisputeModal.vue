<template>
  <div class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4" @click.self="$emit('close')">
    <div
      role="dialog"
      aria-modal="true"
      :aria-label="t.projectWorkspace?.disputeBtn"
      class="bg-white rounded-2xl shadow-2xl w-full max-w-[448px] p-6"
    >
      <div class="flex items-start gap-3">
        <span class="w-9 h-9 rounded-full bg-red-100 text-red-700 shrink-0 flex items-center justify-center">
          <AlertTriangle class="w-4.5 h-4.5" />
        </span>
        <div class="min-w-0">
          <h3 class="text-base font-bold text-gray-900">
            {{ t.projectWorkspace?.disputeModalTitle }}
          </h3>
          <p class="text-sm text-gray-500 mt-0.5">{{ targetName }} · {{ t.projectWorkspace?.disputeModalDesc }}</p>
        </div>
      </div>

      <label class="block mt-4 text-xs font-bold uppercase tracking-wider text-gray-500">
        {{ t.projectWorkspace?.disputeReasonLabel }}
      </label>
      <textarea
        :value="reason"
        rows="4"
        class="w-full mt-1.5 rounded-lg border border-border-gray p-2.5 text-sm resize-y"
        :placeholder="t.projectWorkspace?.disputeReasonPlaceholder"
        @input="$emit('update:reason', $event.target.value)"
      />

      <div class="flex gap-2 mt-4">
        <button
          class="flex-1 px-4 py-2.5 rounded-lg bg-white border border-border-gray text-sm font-semibold"
          @click="$emit('close')"
        >
          {{ t.projectWorkspace?.cancel }}
        </button>
        <button
          class="flex-1 px-4 py-2.5 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-bold disabled:opacity-50"
          :disabled="busy || !reason.trim()"
          @click="$emit('submit')"
        >
          {{ busy ? t.projectWorkspace?.submitting : t.projectWorkspace?.submitDispute }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { AlertTriangle } from 'lucide-vue-next'

defineProps({
  targetName: { type: String, default: '' },
  reason: { type: String, default: '' },
  busy: { type: Boolean, default: false },
  t: { type: Object, required: true }
})
defineEmits(['close', 'update:reason', 'submit'])
</script>
