<template>
  <div class="flex items-center justify-center gap-3">
    <div v-for="(step, index) in steps" :key="step" class="flex items-center gap-3">
      <div
        :class="[
          'rounded-full transition-all duration-500',
          index <= currentStepIndex ? 'w-2.5 h-2.5 bg-brand-brown' : 'w-2 h-2 bg-black/20'
        ]"
      />
      <div
        v-if="index < steps.length - 1"
        :class="[
          'h-[1px] w-8 transition-all duration-500',
          index < currentStepIndex ? 'bg-brand-brown' : 'bg-black/10'
        ]"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentStep: {
    type: String,
    required: true
  }
})

const steps = [
  'WELCOME',
  'IDENTITY',
  'PHILOSOPHY',
  'EXPERTISE',
  'PROFILE_CONFIRM',
  'PORTFOLIO_INTRO',
  'PORTFOLIO_PROJECT',
  'REVIEW',
  'ACTIVATED'
]

const currentStepIndex = computed(() => steps.indexOf(props.currentStep))
</script>
