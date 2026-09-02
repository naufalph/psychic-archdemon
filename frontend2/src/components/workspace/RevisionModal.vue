<template>
  <div class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4" @click.self="$emit('close')">
    <div
      role="dialog"
      aria-modal="true"
      :aria-label="t.projectWorkspace?.revisionConfirmTitle"
      class="bg-white rounded-2xl shadow-2xl w-full max-w-[448px] p-6"
    >
      <div class="flex items-start gap-3">
        <span class="w-9 h-9 rounded-full bg-amber-100 text-amber-700 shrink-0 flex items-center justify-center">
          <AlertTriangle class="w-4.5 h-4.5" />
        </span>
        <div class="min-w-0">
          <h3 class="text-base font-bold text-gray-900">
            {{ t.projectWorkspace?.revisionConfirmTitle }}
          </h3>
          <p class="text-sm text-gray-500 mt-0.5">
            {{
              (t.projectWorkspace?.revisionConfirmWarning || 'This consumes 1 of {max} revisions. {left} will remain.')
                .replace('{max}', maxRevisions)
                .replace('{left}', Math.max(0, revisionsLeft - 1))
            }}
          </p>
        </div>
      </div>

      <div class="mt-4 rounded-lg bg-gray-50 border border-border-gray p-3">
        <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
          {{ t.projectWorkspace?.revisionBasketTitle }}
        </p>
        <ul class="mt-1.5 flex flex-col gap-2">
          <li v-for="item in items" :key="item.index">
            <p class="text-sm font-medium text-gray-800 truncate">
              {{ deliverableLabel(item.name, t) }}
            </p>
            <p class="text-sm text-gray-600 whitespace-pre-line">{{ item.notes }}</p>
          </li>
        </ul>
        <!-- One round covers every item above, however many there are. -->
        <p class="text-xs text-gray-500 mt-2.5 pt-2.5 border-t border-border-gray">
          {{ (t.projectWorkspace?.revisionPooledHint || '').replace('{n}', items.length) }}
        </p>
      </div>

      <div class="flex items-center gap-2 mt-4">
        <button
          class="flex-1 px-4 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-white text-sm font-semibold disabled:opacity-50"
          :disabled="busy"
          @click="$emit('submit')"
        >
          {{ busy ? t.projectWorkspace?.submitting : t.projectWorkspace?.revisionConfirmSubmit }}
        </button>
        <button class="px-4 py-2.5 text-sm text-gray-500" @click="$emit('close')">
          {{ t.projectWorkspace?.revisionModalCancel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { AlertTriangle } from 'lucide-vue-next'
import { deliverableLabel } from './workspaceMaps'

defineProps({
  busy: { type: Boolean, default: false },
  items: { type: Array, default: () => [] },
  revisionsLeft: { type: Number, default: 0 },
  maxRevisions: { type: Number, default: 0 },
  t: { type: Object, required: true }
})
defineEmits(['close', 'submit'])
</script>
