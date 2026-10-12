<template>
  <button
    type="button"
    :aria-pressed="on"
    class="min-h-[44px] px-4 py-2.5 rounded-xl border text-[15px] font-semibold text-left leading-[1.3] transition-all"
    :class="on ? 'bg-ink-900 border-ink-900 text-white' : 'bg-white border-hairline text-ink-900 hover:border-ink-900'"
  >
    <slot />
  </button>
</template>

<script setup>
defineProps({ on: { type: Boolean, default: false } })
</script>
