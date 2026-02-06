<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-2xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">Review Your Profile</h2>
        <p class="text-lg text-black/60 tracking-tight">
          Confirm your professional details before creating your portfolio.
        </p>
      </div>

      <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5 space-y-6">
        <div class="space-y-4">
          <div class="space-y-1">
            <p class="text-xs text-black/40 uppercase tracking-wider">Practice Name</p>
            <p class="text-xl font-semibold text-black">{{ store.profile.name }}</p>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-1">
              <p class="text-xs text-black/40 uppercase tracking-wider">City</p>
              <p class="text-lg font-medium text-black">{{ store.profile.city }}</p>
            </div>
            <div class="space-y-1">
              <p class="text-xs text-black/40 uppercase tracking-wider">Experience</p>
              <p class="text-lg font-medium text-black">{{ store.profile.experienceRange }}</p>
            </div>
          </div>

          <div class="space-y-1">
            <p class="text-xs text-black/40 uppercase tracking-wider">Design Philosophy</p>
            <p class="text-base text-black/70 leading-relaxed">{{ store.profile.philosophy }}</p>
          </div>

          <div class="space-y-2">
            <p class="text-xs text-black/40 uppercase tracking-wider">Expertise</p>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="expertise in store.profile.expertise"
                :key="expertise"
                class="px-3 py-1 bg-[#7C4728]/10 text-[#7C4728] rounded-full text-sm font-medium"
              >
                {{ expertise }}
              </span>
            </div>
          </div>
        </div>

        <div class="pt-6 border-t border-black/10 flex gap-3">
          <button
            @click="handleEdit"
            class="flex-1 px-4 py-3 border border-black/10 rounded-2xl text-black/60 hover:bg-black/5 transition-all"
          >
            Edit Profile
          </button>
        </div>
      </div>

      <div class="flex justify-between items-center pt-4">
        <button @click="emit('back')" class="px-6 py-3 text-black/60 hover:text-black transition-colors">Back</button>
        <button
          @click="handleNext"
          :disabled="store.isLoading"
          class="px-8 py-3 bg-[#7C4728] text-white rounded-full font-semibold hover:bg-[#6A3D22] transition-all hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ store.isLoading ? 'Saving...' : 'Proceed to Portfolio' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useOnboardingStore } from '@/stores/onboarding'

const emit = defineEmits(['next', 'back'])
const store = useOnboardingStore()

const handleEdit = () => {
  store.goToStep('IDENTITY')
}

const handleNext = async () => {
  try {
    await store.saveProfileToBackend()
    emit('next')
  } catch (error) {
    console.error('Failed to save profile:', error)
  }
}
</script>
