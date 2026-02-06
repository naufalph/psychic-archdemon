<template>
  <div class="flex flex-col items-center justify-center min-h-[60vh] text-center">
    <div
      v-motion
      :initial="{ opacity: 0, y: 20 }"
      :enter="{ opacity: 1, y: 0, transition: { duration: 800 } }"
      class="max-w-2xl mx-auto space-y-8"
    >
      <div class="space-y-4">
        <h1 class="text-6xl md:text-7xl font-black tracking-tighter text-black">Welcome to Your Studio</h1>
        <p class="text-xl text-black/60 tracking-tight max-w-xl mx-auto">
          Let's craft your architectural presence and showcase your vision to clients seeking exceptional design.
        </p>
      </div>

      <div class="pt-8">
        <button
          @click="handleNext"
          class="px-12 py-4 bg-[#7C4728] text-white rounded-full text-lg font-semibold tracking-tight hover:bg-[#6A3D22] transition-all duration-300 hover:scale-105"
        >
          Enter Studio
        </button>
      </div>

      <p class="text-sm text-black/40 tracking-wide pt-4">This will take approximately 10 minutes to complete</p>
    </div>
  </div>
</template>

<script setup>
const emit = defineEmits(['next'])

const handleNext = () => {
  emit('next')
}
</script>
