<template>
  <div class="w-full">
    <label :for="inputId" class="block text-body-sm font-semibold text-ink-900 mb-2">
      {{ label }}
    </label>
    <div class="relative group">
      <div
        v-if="icon"
        class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-ink-300 group-focus-within:text-ink-900 transition-colors"
      >
        <component :is="icon" class="w-5 h-5" />
      </div>
      <input
        :id="inputId"
        :value="modelValue"
        :class="inputClasses"
        v-bind="$attrs"
        @input="$emit('update:modelValue', $event.target.value)"
      />
    </div>
    <p v-if="error" class="mt-1.5 text-caption text-red-500">{{ error }}</p>
  </div>
</template>

<script setup>
import { computed, useId } from 'vue'

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

// $attrs are bound to the <input> explicitly; inherited onto the wrapper too, type="text" there
// picks up the forms-plugin input styles and draws a second box around the field.
defineOptions({ inheritAttrs: false })

// The label was rendered unassociated, so it read as decoration: screen readers announced an
// unlabelled field and clicking it did not focus the input. An explicit id/for pair fixes both.
// A caller-supplied id still wins, since $attrs is bound after this one.
const inputId = useId()

const inputClasses = computed(() => {
  const baseClasses =
    'w-full box-border h-12 px-4 rounded-xl border bg-white text-body text-ink-900 placeholder-ink-300 focus:outline-none focus:ring-2 transition-colors duration-200'
  const errorClasses = props.error
    ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
    : 'border-hairline hover:border-ink-200 focus:border-ink-900 focus:ring-ink-200/40'
  const iconPadding = props.icon ? 'pl-10' : ''
  return `${baseClasses} ${errorClasses} ${iconPadding}`
})
</script>
