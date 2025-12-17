<!--
  BaseButton Component - V2 Design System

  A reusable button component with gold theme matching Figma specs.

  @example Solid button (default)
  <BaseButton @click="handleSubmit">
    Sign Up
  </BaseButton>

  @example Outline button
  <BaseButton variant="outline" @click="handleCancel">
    Cancel
  </BaseButton>

  @example Loading state
  <BaseButton :loading="isSubmitting" @click="handleSubmit">
    Submit
  </BaseButton>

  @example Different sizes
  <BaseButton size="lg">Large Button</BaseButton>
  <BaseButton size="md">Medium Button</BaseButton>
  <BaseButton size="sm">Small Button</BaseButton>

  @example Disabled
  <BaseButton :disabled="true">
    Disabled
  </BaseButton>

  @example Full width
  <BaseButton block>
    Full Width Button
  </BaseButton>
-->

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    class="font-poppins font-semibold rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2"
    :class="[sizeClasses, variantClasses, blockClass, disabledClasses]"
    @click="handleClick"
  >
    <!-- Loading Spinner -->
    <span v-if="loading" class="inline-block mr-2">
      <svg class="animate-spin" :class="spinnerSize" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path
          class="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        ></path>
      </svg>
    </span>

    <!-- Button Content -->
    <span :class="{ 'opacity-0': loading && !loadingText }">
      <slot>{{ loadingText && loading ? loadingText : 'Button' }}</slot>
    </span>
  </button>
</template>

<script setup>
import { computed } from 'vue'

/**
 * Props
 * @property {String} type - Button type: 'button', 'submit', 'reset'. Default: 'button'
 * @property {String} variant - Button variant: 'solid', 'outline'. Default: 'solid'
 * @property {String} size - Button size: 'xs', 'sm', 'md', 'lg'. Default: 'md'
 * @property {Boolean} disabled - Whether button is disabled. Default: false
 * @property {Boolean} loading - Whether button is in loading state. Default: false
 * @property {String} loadingText - Text to show when loading (optional)
 * @property {Boolean} block - Whether button should take full width. Default: false
 */
const props = defineProps({
  type: {
    type: String,
    default: 'button',
    validator: value => ['button', 'submit', 'reset'].includes(value)
  },
  variant: {
    type: String,
    default: 'solid',
    validator: value => ['solid', 'outline'].includes(value)
  },
  size: {
    type: String,
    default: 'md',
    validator: value => ['xs', 'sm', 'md', 'lg'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: ''
  },
  block: {
    type: Boolean,
    default: false
  }
})

/**
 * Emits
 * @event click - Emitted when button is clicked
 */
const emit = defineEmits(['click'])

// Size classes
const sizeClasses = computed(() => {
  const sizes = {
    xs: 'px-3 py-1.5 text-xs leading-5',
    sm: 'px-4 py-2 text-sm leading-6',
    md: 'px-4 py-2.5 text-base leading-6',
    lg: 'px-6 py-3 text-lg leading-7'
  }
  return sizes[props.size] || sizes.md
})

// Variant classes (Gold theme)
const variantClasses = computed(() => {
  if (props.variant === 'outline') {
    return 'border-2 border-[#C5A25A] text-[#C5A25A] bg-white hover:bg-[#C5A25A] hover:text-white focus:ring-[#C5A25A]'
  }
  // Solid (default)
  return 'bg-[#C5A25A] text-white hover:bg-[#B89350] active:bg-[#A68446] focus:ring-[#C5A25A]'
})

// Block (full width) class
const blockClass = computed(() => {
  return props.block ? 'w-full' : ''
})

// Disabled classes
const disabledClasses = computed(() => {
  if (props.disabled || props.loading) {
    return 'opacity-50 cursor-not-allowed hover:bg-[#C5A25A]'
  }
  return ''
})

// Spinner size
const spinnerSize = computed(() => {
  const sizes = {
    xs: 'h-3 w-3',
    sm: 'h-4 w-4',
    md: 'h-4 w-4',
    lg: 'h-5 w-5'
  }
  return sizes[props.size] || sizes.md
})

/**
 * Handles button click
 */
const handleClick = event => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.font-poppins {
  font-family: 'Poppins', sans-serif;
}
</style>
