<template>
  <div class="flex items-center gap-2.5 px-4 py-3 rounded-xl border border-hairline">
    <Clock :size="16" class="text-ink-900 shrink-0" />
    <span v-if="remaining.expired" class="text-caption font-normal text-ink-500">{{ copy.biddingClosed }}</span>
    <span v-else class="text-caption font-normal text-ink-500">
      {{ copy.closesIn }}
      <strong class="font-semibold text-ink-900">{{ value }}</strong>
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Clock } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useCountdown } from '@/composables/useCountdown'

const props = defineProps({
  deadline: { type: [String, Date], required: true }
})

const { t } = useI18n()
const copy = computed(() => t.value.projectDetailV2)
const { remaining } = useCountdown(props.deadline)

const value = computed(() =>
  copy.value.closesInValue
    .replace('{d}', remaining.value.days)
    .replace('{h}', String(remaining.value.hours).padStart(2, '0'))
)
</script>
