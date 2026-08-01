<template>
  <div class="relative">
    <svg
      class="absolute left-5 top-1/2 -translate-y-1/2 text-ink-300 w-5 h-5"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
      />
    </svg>
    <input
      :value="modelValue"
      type="text"
      :placeholder="placeholder"
      class="w-full py-4 pl-14 pr-6 border border-hairline rounded-2xl text-body bg-surface-muted outline-none transition-all focus:bg-white focus:border-ink-900 focus:shadow-[0_0_0_4px_rgba(0,0,0,0.03)]"
      @input="$emit('update:modelValue', $event.target.value)"
      @keydown.enter="$emit('enter', modelValue)"
    />
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  }
})

defineEmits(['update:modelValue', 'enter'])
</script>
