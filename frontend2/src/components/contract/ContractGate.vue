<template>
  <div class="mb-4">
    <div v-if="accepted" class="flex items-start gap-2 rounded-2xl border border-green-200 bg-green-50 px-4 py-3">
      <CheckCircle :size="16" class="text-green-600 mt-0.5 shrink-0" />
      <div class="min-w-0 flex-1">
        <p class="text-sm font-bold text-green-700">{{ c.accepted }}</p>
        <p v-if="acceptance?.acceptedAt" class="text-xs text-gray-500 truncate">
          {{ acceptance.signatureName }} · {{ formatDate(acceptance.acceptedAt) }}
        </p>
      </div>
      <button type="button" class="text-xs font-bold text-brand-brown hover:underline shrink-0" @click="$emit('open')">
        {{ c.viewButton }}
      </button>
    </div>

    <template v-else>
      <button
        type="button"
        class="w-full px-5 py-3.5 bg-white text-brand-brown border-2 border-brand-brown rounded-full font-bold hover:bg-brand-cream transition flex items-center justify-center gap-2"
        @click="$emit('open')"
      >
        <FileText :size="18" />
        {{ c.openButton }}
      </button>
      <p class="text-xs text-gray-400 mt-2 text-center leading-relaxed">
        {{ c.prerequisiteHint }}
      </p>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { CheckCircle, FileText } from 'lucide-vue-next'

const props = defineProps({
  accepted: { type: Boolean, default: false },
  acceptance: { type: Object, default: null },
  t: { type: Object, required: true },
  formatDate: { type: Function, required: true }
})

defineEmits(['open'])

const c = computed(() => props.t.contractAgreement || {})
</script>
