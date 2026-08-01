<template>
  <div class="flex items-center justify-center mb-12">
    <div class="flex items-center gap-4">
      <div v-for="step in steps" :key="step.number" class="flex items-center gap-4">
        <div class="flex flex-col items-center gap-2">
          <div :class="stepCircleClasses(step.number)">
            <Check v-if="step.number < currentStep" :size="20" class="text-white" />
            <span v-else class="text-sm font-bold">{{ step.number }}</span>
          </div>
          <span :class="stepLabelClasses(step.number)">
            {{ step.label }}
          </span>
        </div>
        <div v-if="step.number < steps.length" :class="stepLineClasses(step.number)" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Check } from 'lucide-vue-next'

const props = defineProps({
  currentStep: {
    type: Number,
    required: true
  },
  steps: {
    type: Array,
    required: true,
    validator: steps => steps.every(step => step.number && step.label)
  }
})

const stepCircleClasses = stepNumber => {
  const baseClasses = 'w-12 h-12 rounded-full flex items-center justify-center transition-all'
  if (stepNumber < props.currentStep) {
    return `${baseClasses} bg-brand-brown text-white`
  } else if (stepNumber === props.currentStep) {
    return `${baseClasses} bg-brand-brown text-white ring-4 ring-brand-brown/20`
  } else {
    return `${baseClasses} bg-gray-200 text-gray-500`
  }
}

const stepLabelClasses = stepNumber => {
  const baseClasses = 'text-xs font-medium'
  return stepNumber <= props.currentStep ? `${baseClasses} text-gray-900` : `${baseClasses} text-gray-400`
}

const stepLineClasses = stepNumber => {
  const baseClasses = 'w-16 h-1 transition-all'
  return stepNumber < props.currentStep ? `${baseClasses} bg-brand-brown` : `${baseClasses} bg-gray-200`
}
</script>
