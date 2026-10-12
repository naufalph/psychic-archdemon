<template>
  <div
    class="h-1 rounded-full bg-hairline overflow-hidden"
    role="progressbar"
    :aria-valuenow="current + 1"
    aria-valuemin="1"
    :aria-valuemax="ONBOARDING_TOTAL_STEPS"
    :aria-label="`Langkah ${current + 1} dari ${ONBOARDING_TOTAL_STEPS}`"
  >
    <div
      class="h-full bg-ink-900 rounded-full transition-[width] duration-[400ms] ease-[cubic-bezier(.2,.7,.2,1)]"
      :style="{ width: `${Math.round(((current + 1) / ONBOARDING_TOTAL_STEPS) * 100)}%` }"
    ></div>
  </div>
</template>

<script setup>
import { ONBOARDING_TOTAL_STEPS } from '@/constants/architectOnboarding'

defineProps({ current: { type: Number, required: true } })
</script>
