<template>
  <div
    class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4"
    @click.self="$emit('close')"
  >
    <div
      role="dialog"
      aria-modal="true"
      :aria-label="t.projectWorkspace?.payoutDetailsLabel"
      class="bg-white rounded-2xl shadow-2xl w-full max-w-[448px] p-6">
      <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
        {{ t.projectWorkspace?.confirmPayoutEyebrow }}
      </p>
      <h3 class="text-base font-bold text-gray-900">{{ t.projectWorkspace?.payoutDetailsLabel }}</h3>
      <p class="text-sm text-gray-500 mt-0.5">
        {{ t.projectWorkspace?.disbursementAmountLabel }}: {{ formatAmount(phase?.amount) }}
      </p>

      <select
        v-model="form.channelCode"
        class="w-full mt-4 rounded-lg border border-border-gray px-3 py-2.5 text-sm"
      >
        <option value="">{{ t.projectWorkspace?.selectBankPlaceholder }}</option>
        <option v-for="(label, code) in banks" :key="code" :value="code">{{ label }}</option>
      </select>

      <input
        v-model="form.accountNumber"
        class="w-full mt-2 rounded-lg border border-border-gray px-3 py-2.5 text-sm"
        :placeholder="t.projectWorkspace?.accountNumberPlaceholder || t.projectWorkspace?.accountNumberLabel"
      />
      <input
        v-model="form.accountHolderName"
        class="w-full mt-2 rounded-lg border border-border-gray px-3 py-2.5 text-sm"
        :placeholder="t.projectWorkspace?.accountHolderPlaceholder || t.projectWorkspace?.accountHolderLabel"
      />

      <p class="text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg p-2.5 mt-3">
        {{ t.projectWorkspace?.confirmPayoutWarning }}
      </p>

      <div class="flex items-center gap-2 mt-4">
        <button
          class="flex-1 px-4 py-2.5 rounded-lg bg-green-600 hover:bg-green-700 text-white text-sm font-semibold disabled:opacity-50"
          :disabled="!valid || busy"
          @click="$emit('submit', { ...form })"
        >
          {{ busy ? t.projectWorkspace?.submitting : t.projectWorkspace?.confirmPayoutBtn }}
        </button>
        <button class="px-4 py-2.5 text-sm text-gray-500" @click="$emit('close')">
          {{ t.projectWorkspace?.cancel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'

defineProps({
  phase: { type: Object, default: null },
  busy: { type: Boolean, default: false },
  t: { type: Object, required: true },
  formatAmount: { type: Function, required: true }
})
defineEmits(['close', 'submit'])

const banks = {
  ID_BCA: 'BCA',
  ID_MANDIRI: 'Mandiri',
  ID_BNI: 'BNI',
  ID_BRI: 'BRI',
  ID_PERMATA: 'Permata',
  ID_CIMB: 'CIMB Niaga',
  ID_DANAMON: 'Danamon'
}

const form = reactive({ channelCode: '', accountNumber: '', accountHolderName: '' })
const valid = computed(
  () => form.channelCode && form.accountNumber.trim() && form.accountHolderName.trim()
)
</script>
