<template>
  <button
    type="button"
    class="group text-left rounded-card overflow-hidden border border-hairline bg-white cursor-pointer transition-all duration-300 hover:-translate-y-1.5 hover:shadow-[0_20px_30px_-10px_rgba(0,0,0,0.12)] hover:border-hairline-alt"
    @click="$emit('click')"
  >
    <div class="relative overflow-hidden bg-surface-muted flex items-center justify-center" style="aspect-ratio: 1/1">
      <component
        :is="icon"
        class="w-12 h-12 text-ink-300 group-hover:text-ink-900 transition-colors duration-300"
        stroke-width="1.25"
      />
    </div>
    <div class="p-3">
      <span class="text-nano font-extrabold uppercase tracking-wide text-ink-400 block mb-1">
        {{ eyebrow }}
      </span>
      <h3 class="text-caption font-bold text-ink-900 line-clamp-1 leading-snug">
        {{ label }}
      </h3>
    </div>
  </button>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  eyebrow: {
    type: String,
    required: true
  },
  icon: {
    type: [Object, Function],
    required: true
  }
})

defineEmits(['click'])
</script>
