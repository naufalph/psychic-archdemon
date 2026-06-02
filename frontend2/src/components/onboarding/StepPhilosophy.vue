<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-2xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">Define Your Philosophy</h2>
        <p class="text-lg text-black/60 tracking-tight">Share your design principles and approach to architecture.</p>
      </div>

      <div class="space-y-4 bg-white rounded-3xl p-8 shadow-sm border border-black/5">
        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight"> Design Philosophy </label>
          <textarea
            v-model="philosophy"
            rows="6"
            placeholder="Describe your architectural philosophy and design approach..."
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all resize-none"
          />
          <p class="text-xs text-black/40">{{ philosophy.length }} characters</p>
        </div>

        <div v-if="store.error" class="p-3 bg-red-50 border border-red-200 rounded-2xl text-sm text-red-600">
          {{ store.error }}
        </div>
      </div>

      <div class="flex justify-between items-center pt-4">
        <button @click="emit('back')" class="px-6 py-3 text-black/60 hover:text-black transition-colors">Back</button>
        <button
          @click="handleNext"
          :disabled="!isFormValid"
          :class="[
            'px-8 py-3 rounded-full font-semibold transition-all',
            isFormValid
              ? 'bg-[#7C4728] text-white hover:bg-[#6A3D22] hover:scale-105'
              : 'bg-black/10 text-black/30 cursor-not-allowed'
          ]"
        >
          Continue
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useOnboardingStore } from '@/stores/onboarding'

const emit = defineEmits(['next', 'back'])
const store = useOnboardingStore()

const philosophy = ref(store.profile.philosophy || '')

const isFormValid = computed(() => {
  return philosophy.value.trim().length > 0
})

const handleNext = () => {
  if (isFormValid.value) {
    store.updateProfile({ philosophy: philosophy.value })
    emit('next')
  }
}
</script>
