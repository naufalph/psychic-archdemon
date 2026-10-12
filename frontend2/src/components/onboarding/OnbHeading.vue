<template>
  <div class="mb-8 max-w-[680px]">
    <h1 class="text-[clamp(30px,4vw,44px)] font-bold tracking-[-.035em] leading-[1.1] m-0 mb-3">{{ title }}</h1>
    <p v-if="lead" class="text-[17px] leading-relaxed text-ink-400 m-0">{{ lead }}</p>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  lead: { type: String, default: '' }
})
</script>
