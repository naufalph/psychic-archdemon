<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-2xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">Establish Your Identity</h2>
        <p class="text-lg text-black/60 tracking-tight">Tell us about your practice and professional background.</p>
      </div>

      <div class="space-y-6 bg-white rounded-3xl p-8 shadow-sm border border-black/5">
        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight"> Practice Name </label>
          <input
            v-model="formData.name"
            type="text"
            placeholder="e.g., Studio Archipelago"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all"
          />
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight"> City </label>
          <input
            v-model="formData.city"
            type="text"
            placeholder="e.g., Jakarta"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all"
          />
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight"> Years of Experience </label>
          <select
            v-model="formData.experienceRange"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all bg-white"
          >
            <option value="" disabled>Select experience range</option>
            <option v-for="option in EXPERIENCE_OPTIONS" :key="option" :value="option">
              {{ option }}
            </option>
          </select>
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
import { EXPERIENCE_OPTIONS } from '@/constants/onboarding'

const emit = defineEmits(['next', 'back'])
const store = useOnboardingStore()

const formData = ref({
  name: store.profile.name || '',
  city: store.profile.city || '',
  experienceRange: store.profile.experienceRange || ''
})

const isFormValid = computed(() => {
  return formData.value.name.trim() && formData.value.city.trim() && formData.value.experienceRange
})

const handleNext = () => {
  if (isFormValid.value) {
    store.updateProfile(formData.value)
    emit('next')
  }
}
</script>
