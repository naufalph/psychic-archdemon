<template>
  <Transition name="fade">
    <div
      v-if="message"
      class="fixed bottom-6 left-1/2 -translate-x-1/2 z-[70] bg-ink-700 text-white text-[13px] font-medium px-5 py-2.5 rounded-full shadow-lg"
    >
      {{ message }}
    </div>
  </Transition>
</template>

<script setup>
defineProps({ message: { type: String, default: '' } })
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease-in-out;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
