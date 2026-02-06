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

        <div class="pt-2">
          <button
            @click="handleEnhance"
            :disabled="!philosophy.trim() || store.isLoading"
            :class="[
              'w-full px-6 py-3 rounded-2xl font-semibold transition-all flex items-center justify-center gap-2',
              philosophy.trim() && !store.isLoading
                ? 'bg-gradient-to-r from-[#7C4728] to-[#9B5E3C] text-white hover:shadow-lg'
                : 'bg-black/5 text-black/30 cursor-not-allowed'
            ]"
          >
            <svg v-if="!store.isLoading" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
              />
            </svg>
            <svg v-else class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            {{ store.isLoading ? 'Enhancing...' : 'Elevate Tone with AI' }}
          </button>
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

const handleEnhance = async () => {
  store.profile.philosophy = philosophy.value
  await store.enhancePhilosophy()
  philosophy.value = store.profile.philosophy
}

const handleNext = () => {
  if (isFormValid.value) {
    store.updateProfile({ philosophy: philosophy.value })
    emit('next')
  }
}
</script>
