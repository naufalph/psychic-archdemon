<template>
  <div class="w-full">
    <label class="block text-sm font-medium text-gray-700 mb-1 ml-1">
      {{ label }}
    </label>
    <div class="relative group">
      <div
        v-if="icon"
        class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400 group-focus-within:text-brand-green transition-colors"
      >
        <component :is="icon" class="w-5 h-5" />
      </div>
      <input
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        :class="inputClasses"
        v-bind="$attrs"
      />
    </div>
    <p v-if="error" class="mt-1 text-sm text-red-500 ml-1">{{ error }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: {
    type: String,
    required: true
  },
  modelValue: {
    type: [String, Number],
    default: ''
  },
  error: {
    type: String,
    default: ''
  },
  icon: {
    type: Object,
    default: null
  }
})

defineEmits(['update:modelValue'])

const inputClasses = computed(() => {
  const baseClasses =
    'w-full px-4 py-3 rounded-2xl border-2 bg-white text-gray-900 placeholder-gray-400 focus:outline-none transition-all duration-200'
  const errorClasses = props.error ? 'border-red-300 focus:border-red-500' : 'border-gray-200 focus:border-brand-green'
  const iconPadding = props.icon ? 'pl-10' : ''
  return `${baseClasses} ${errorClasses} ${iconPadding}`
})
</script>
