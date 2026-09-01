<template>
  <div
    class="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4"
    @click.self="$emit('close')"
  >
    <div
      role="dialog"
      aria-modal="true"
      :aria-label="t.projectWorkspace?.approveModalTitle"
      class="bg-white rounded-2xl shadow-2xl w-full max-w-[448px] overflow-hidden">
      <div class="bg-green-600 px-6 py-5 flex items-start gap-3">
        <span class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center shrink-0">
          <ShieldCheck class="w-5 h-5 text-white" />
        </span>
        <div class="min-w-0">
          <p class="text-xs font-bold uppercase tracking-wider text-green-100">
            {{ t.projectWorkspace?.confirmApprovalEyebrow }}
          </p>
          <h3 class="text-lg font-bold text-white">{{ t.projectWorkspace?.approveModalTitle }}</h3>
          <p class="text-sm text-green-100 truncate">{{ targetName }}</p>
        </div>
      </div>

      <div class="p-6 flex flex-col gap-4">
        <div class="rounded-lg bg-amber-50 border border-amber-200 p-3">
          <p class="text-sm font-bold text-amber-700">
            {{ t.projectWorkspace?.approveModalWarningTitle }}
          </p>
          <ul class="mt-1 text-xs leading-relaxed text-amber-800 list-disc pl-4">
            <li v-html="t.projectWorkspace?.approveModalItem1" />
            <li v-html="t.projectWorkspace?.approveModalItem2" />
            <li v-html="t.projectWorkspace?.approveModalItem3" />
          </ul>
        </div>

        <div class="rounded-lg bg-gray-50 border border-border-gray p-3 text-xs text-gray-600">
          <span class="font-semibold">{{ t.projectWorkspace?.approveModalConfirmLabel }}</span>
          {{ t.projectWorkspace?.approveModalConfirmDesc }}
        </div>

        <div class="flex gap-2">
          <button
            class="flex-1 px-4 py-2.5 rounded-lg bg-white border border-border-gray text-sm font-semibold"
            @click="$emit('close')"
          >
            {{ t.projectWorkspace?.cancel }}
          </button>
          <button
            class="flex-1 px-4 py-2.5 rounded-lg bg-green-600 hover:bg-green-700 text-white text-sm font-bold disabled:opacity-50"
            :disabled="busy"
            @click="$emit('confirm')"
          >
            {{ busy ? t.projectWorkspace?.approving : t.projectWorkspace?.confirmApproveBtn }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ShieldCheck } from 'lucide-vue-next'

defineProps({
  targetName: { type: String, default: '' },
  busy: { type: Boolean, default: false },
  t: { type: Object, required: true }
})
defineEmits(['close', 'confirm'])
</script>
