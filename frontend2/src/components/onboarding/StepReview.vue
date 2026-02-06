<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-4xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">Final Review</h2>
        <p class="text-lg text-black/60 tracking-tight">Review your complete profile before activation.</p>
      </div>

      <div class="space-y-6">
        <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5">
          <div class="flex justify-between items-start mb-6">
            <h3 class="text-2xl font-bold text-black">Professional Profile</h3>
            <button @click="store.goToStep('IDENTITY')" class="text-sm text-[#7C4728] hover:underline">Edit</button>
          </div>

          <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1">
                <p class="text-xs text-black/40 uppercase tracking-wider">Practice Name</p>
                <p class="text-lg font-semibold text-black">{{ store.profile.name }}</p>
              </div>
              <div class="space-y-1">
                <p class="text-xs text-black/40 uppercase tracking-wider">City</p>
                <p class="text-lg font-semibold text-black">{{ store.profile.city }}</p>
              </div>
            </div>

            <div class="space-y-1">
              <p class="text-xs text-black/40 uppercase tracking-wider">Experience</p>
              <p class="text-base text-black">{{ store.profile.experienceRange }}</p>
            </div>

            <div class="space-y-1">
              <p class="text-xs text-black/40 uppercase tracking-wider">Philosophy</p>
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
        </div>

        <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5">
          <div class="flex justify-between items-start mb-6">
            <h3 class="text-2xl font-bold text-black">Portfolio Projects ({{ store.portfolio.length }})</h3>
            <button @click="store.goToStep('PORTFOLIO_INTRO')" class="text-sm text-[#7C4728] hover:underline">
              Edit
            </button>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              v-for="(project, index) in store.portfolio"
              :key="index"
              class="border border-black/10 rounded-2xl p-4 space-y-2"
            >
              <h4 class="font-semibold text-black">{{ project.name }}</h4>
              <div class="flex items-center gap-2 text-sm text-black/60">
                <span>{{ project.type }}</span>
                <span>•</span>
                <span>{{ project.year }}</span>
              </div>
              <p v-if="project.story.problem" class="text-sm text-black/70 line-clamp-2">
                {{ project.story.problem }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-gradient-to-r from-[#7C4728]/10 to-[#9B5E3C]/10 rounded-3xl p-8 text-center space-y-4">
        <div class="w-16 h-16 mx-auto rounded-full bg-[#7C4728]/20 flex items-center justify-center">
          <svg class="w-8 h-8 text-[#7C4728]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h3 class="text-2xl font-bold text-black">Ready to Activate</h3>
        <p class="text-black/70 max-w-lg mx-auto">
          Your profile is complete. Click below to activate your architect presence and start connecting with clients.
        </p>
      </div>

      <div class="flex justify-between items-center pt-4">
        <button @click="emit('back')" class="px-6 py-3 text-black/60 hover:text-black transition-colors">Back</button>
        <button
          @click="handleActivate"
          class="px-12 py-4 bg-gradient-to-r from-[#7C4728] to-[#9B5E3C] text-white rounded-full text-lg font-bold hover:shadow-lg transition-all hover:scale-105"
        >
          Activate Presence
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useOnboardingStore } from '@/stores/onboarding'

const emit = defineEmits(['next', 'back'])
const store = useOnboardingStore()

const handleActivate = () => {
  emit('next')
}
</script>
