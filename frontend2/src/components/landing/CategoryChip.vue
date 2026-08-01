<template>
  <button
    type="button"
    class="px-5 py-2.5 rounded-full text-caption font-medium border transition-all whitespace-nowrap flex-shrink-0"
    :class="
      active ? 'bg-ink-900 text-white border-ink-900' : 'bg-white text-ink-400 border-hairline hover:border-ink-900'
    "
    @click="$emit('click')"
  >
    {{ label }}
  </button>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  active: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click'])
</script>
