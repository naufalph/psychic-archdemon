<template>
  <div class="flex flex-col items-center justify-center min-h-[60vh] text-center">
    <div
      v-motion
      :initial="{ opacity: 0, y: 20 }"
      :enter="{ opacity: 1, y: 0, transition: { duration: 800 } }"
      class="max-w-2xl mx-auto space-y-8"
    >
      <div class="space-y-4">
        <h1 class="text-6xl md:text-7xl font-black tracking-tighter text-black">{{ t.onboarding.welcome.title }}</h1>
        <p class="text-xl text-black/60 tracking-tight max-w-xl mx-auto">{{ t.onboarding.welcome.subtitle }}</p>
      </div>

      <div class="pt-8">
        <button
          @click="handleNext"
          class="px-12 py-4 bg-brand-brown text-white rounded-full text-lg font-semibold tracking-tight hover:bg-brand-brown-dark transition-all duration-300 hover:scale-105"
        >
          {{ t.onboarding.welcome.enterStudio }}
        </button>
      </div>

      <p class="text-sm text-black/40 tracking-wide pt-4">{{ t.onboarding.welcome.duration }}</p>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()
const emit = defineEmits(['next'])

const handleNext = () => {
  emit('next')
}
</script>
