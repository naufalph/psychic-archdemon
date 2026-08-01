<template>
  <button :class="buttonClasses" :disabled="isLoading || disabled" v-bind="$attrs">
    <div
      v-if="isLoading"
      class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"
    ></div>
    <slot />
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: value => ['primary', 'secondary', 'outline', 'success', 'ghost'].includes(value)
  },
  size: {
    type: String,
    default: 'lg',
    validator: value => ['sm', 'md', 'lg'].includes(value)
  },
  fullWidth: {
    type: Boolean,
    default: false
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const baseStyles =
  'inline-flex items-center justify-center font-bold tracking-tight transition-all duration-300 rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed'

const variants = {
  primary: 'bg-black text-white hover:bg-gray-800 shadow-lg hover:shadow-xl focus:ring-black',
  secondary: 'bg-white text-black border-2 border-black hover:bg-black hover:text-white focus:ring-gray-500',
  outline: 'bg-white text-ink-900 border border-hairline-alt hover:border-ink-900 focus:ring-ink-200',
  success: 'bg-brand-green text-white hover:bg-green-600 focus:ring-green-500 shadow-glow',
  ghost: 'bg-transparent text-gray-600 hover:text-black hover:bg-gray-100'
}

const sizes = {
  sm: 'px-6 py-2.5 text-caption',
  md: 'px-8 py-3 text-body-sm',
  lg: 'px-10 py-3.5 text-base'
}

const buttonClasses = computed(() => {
  const classes = [baseStyles, variants[props.variant], sizes[props.size]]
  if (props.fullWidth) classes.push('w-full')
  return classes.join(' ')
})
</script>
