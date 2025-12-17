<!--
  BaseCheckbox Component - V2 Design System

  A custom checkbox component matching Figma design specs.

  @example Basic usage
  <BaseCheckbox
    v-model="agreedToTerms"
    label="I have read and agree to the Terms and Conditions"
  />

  @example With custom size and color
  <BaseCheckbox
    v-model="agreedToPrivacy"
    label="I have read and agree to the Privacy Policy"
    size="lg"
    color-scheme="blue"
  />

  @example Disabled state
  <BaseCheckbox
    v-model="newsletter"
    label="I am interested in receiving news"
    :disabled="true"
  />
-->

<template>
  <div class="flex items-start gap-3">
    <!-- Checkbox Input (Hidden) -->
    <input :id="id" v-model="checked" type="checkbox" :disabled="disabled" class="hidden" @change="handleChange" />

    <!-- Custom Checkbox Visual -->
    <label
      :for="id"
      class="flex-shrink-0 flex items-center justify-center border-2 rounded-sm cursor-pointer transition-all"
      :class="[sizeClasses, disabled ? 'border-gray-300 bg-gray-100 cursor-not-allowed' : checkboxClasses]"
    >
      <!-- Checkmark SVG -->
      <svg
        v-show="checked"
        class="transition-opacity"
        :class="disabled ? 'opacity-50' : 'opacity-100'"
        :width="checkmarkSize"
        :height="checkmarkSize"
        viewBox="0 0 12 9"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M1 4.5L4.5 8L11 1"
          :stroke="disabled ? '#9CA3AF' : '#3B82F6'"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </label>

    <!-- Label Text -->
    <label
      :for="id"
      class="flex-1 font-poppins cursor-pointer select-none"
      :class="[labelClasses, disabled ? 'text-gray-400 cursor-not-allowed' : 'text-[#ABABAB]']"
    >
      {{ label }}
    </label>
  </div>
</template>

<script setup>
import { computed } from 'vue'

/**
 * Props
 * @property {String} id - Unique identifier (auto-generated if not provided)
 * @property {Boolean} modelValue - v-model binding value
 * @property {String} label - Label text displayed next to checkbox (required)
 * @property {String} size - Checkbox size: 'sm', 'md', 'lg'. Default: 'md'
 * @property {String} colorScheme - Color scheme: 'blue', 'green', 'teal', etc. Default: 'blue'
 * @property {Boolean} disabled - Whether checkbox is disabled. Default: false
 */
const props = defineProps({
  id: {
    type: String,
    default: () => `checkbox-${Math.random().toString(36).substr(2, 9)}`
  },
  modelValue: {
    type: Boolean,
    default: false
  },
  label: {
    type: String,
    required: true
  },
  size: {
    type: String,
    default: 'md',
    validator: value => ['sm', 'md', 'lg'].includes(value)
  },
  colorScheme: {
    type: String,
    default: 'blue'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

/**
 * Emits
 * @event update:modelValue - Emitted when checkbox state changes
 */
const emit = defineEmits(['update:modelValue'])

// Computed v-model
const checked = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

// Size classes for checkbox
const sizeClasses = computed(() => {
  const sizes = {
    sm: 'w-3 h-3',
    md: 'w-4 h-4',
    lg: 'w-5 h-5'
  }
  return sizes[props.size] || sizes.md
})

// Checkmark size
const checkmarkSize = computed(() => {
  const sizes = {
    sm: 8,
    md: 10,
    lg: 12
  }
  return sizes[props.size] || sizes.md
})

// Checkbox visual classes
const checkboxClasses = computed(() => {
  return checked.value ? 'border-blue-500 bg-blue-50' : 'border-[#E2E8F0] bg-white hover:border-blue-300'
})

// Label text classes
const labelClasses = computed(() => {
  const sizes = {
    sm: 'text-sm',
    md: 'text-base',
    lg: 'text-lg'
  }
  return `${sizes[props.size] || sizes.md} font-normal leading-6`
})

/**
 * Handles checkbox change
 */
const handleChange = event => {
  if (!props.disabled) {
    emit('update:modelValue', event.target.checked)
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.font-poppins {
  font-family: 'Poppins', sans-serif;
}
</style>
