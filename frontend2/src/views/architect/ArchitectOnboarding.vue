<template>
  <div class="min-h-screen bg-[#FAFAFA]">
    <nav class="py-8 px-6 md:px-12 sticky top-0 bg-[#FAFAFA]/90 backdrop-blur-xl z-50 border-b border-black/5">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <div class="flex items-center gap-4">
          <div class="text-2xl font-black text-[#7C4728]">Rumantra</div>
          <span class="text-[10px] uppercase tracking-[0.4em] text-black/40"> Studio Onboarding </span>
        </div>
        <ProgressBar :current-step="store.currentStep" />
      </div>
    </nav>

    <main class="container mx-auto px-6 md:px-12 pb-24">
      <StepWelcome v-if="currentStep === 'WELCOME'" @next="store.nextStep()" />
      <StepIdentity v-if="currentStep === 'IDENTITY'" @next="store.nextStep()" @back="store.previousStep()" />
      <StepPhilosophy v-if="currentStep === 'PHILOSOPHY'" @next="store.nextStep()" @back="store.previousStep()" />
      <StepExpertise v-if="currentStep === 'EXPERTISE'" @next="store.nextStep()" @back="store.previousStep()" />
      <StepProfileConfirm
        v-if="currentStep === 'PROFILE_CONFIRM'"
        @next="store.nextStep()"
        @back="store.previousStep()"
      />
      <StepPortfolioIntro
        v-if="currentStep === 'PORTFOLIO_INTRO'"
        @next="store.nextStep()"
        @back="store.previousStep()"
      />
      <StepPortfolioProject
        v-if="currentStep === 'PORTFOLIO_PROJECT'"
        @next="handlePortfolioProjectNext"
        @back="store.previousStep()"
      />
      <StepReview v-if="currentStep === 'REVIEW'" @next="store.nextStep()" @back="store.previousStep()" />
      <StepActivated v-if="currentStep === 'ACTIVATED'" />
    </main>

    <Teleport to="body">
      <div v-if="showRecoveryModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-6">
        <div
          v-motion
          :initial="{ opacity: 0, scale: 0.9 }"
          :enter="{ opacity: 1, scale: 1 }"
          class="bg-white rounded-3xl p-8 max-w-md w-full space-y-6"
        >
          <div class="space-y-2">
            <h3 class="text-2xl font-bold text-black">Continue Onboarding?</h3>
            <p class="text-black/70">We found your previous progress. Would you like to continue where you left off?</p>
          </div>

          <div class="flex gap-3">
            <button
              @click="startFresh"
              class="flex-1 px-6 py-3 border border-black/10 rounded-2xl text-black/60 hover:bg-black/5 transition-all"
            >
              Start Fresh
            </button>
            <button
              @click="continueOnboarding"
              class="flex-1 px-6 py-3 bg-[#7C4728] text-white rounded-2xl font-semibold hover:bg-[#6A3D22] transition-all"
            >
              Continue
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div
        v-if="store.error"
        class="fixed bottom-6 right-6 bg-red-50 border-2 border-red-200 rounded-2xl p-4 shadow-lg z-50 max-w-md"
      >
        <div class="flex items-start gap-3">
          <svg class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <div class="flex-1">
            <p class="text-sm font-semibold text-red-800">Error</p>
            <p class="text-sm text-red-700">{{ store.error }}</p>
          </div>
          <button @click="store.error = null" class="text-red-500 hover:text-red-700">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOnboardingStore } from '@/stores/onboarding'
import { useAuthStore } from '@/stores/auth'
import ProgressBar from '@/components/onboarding/ProgressBar.vue'
import StepWelcome from '@/components/onboarding/StepWelcome.vue'
import StepIdentity from '@/components/onboarding/StepIdentity.vue'
import StepPhilosophy from '@/components/onboarding/StepPhilosophy.vue'
import StepExpertise from '@/components/onboarding/StepExpertise.vue'
import StepProfileConfirm from '@/components/onboarding/StepProfileConfirm.vue'
import StepPortfolioIntro from '@/components/onboarding/StepPortfolioIntro.vue'
import StepPortfolioProject from '@/components/onboarding/StepPortfolioProject.vue'
import StepReview from '@/components/onboarding/StepReview.vue'
import StepActivated from '@/components/onboarding/StepActivated.vue'

const router = useRouter()
const store = useOnboardingStore()
const authStore = useAuthStore()

const showRecoveryModal = ref(false)
const currentStep = computed(() => store.currentStep)

const handlePortfolioProjectNext = () => {
  if (store.portfolio.length === 0) {
    store.error = 'Please add at least one project'
    return
  }
  store.nextStep()
}

const continueOnboarding = () => {
  store.loadFromLocalStorage()
  showRecoveryModal.value = false
}

const startFresh = () => {
  store.clearOnboardingData()
  showRecoveryModal.value = false
}

onMounted(() => {
  const savedData = localStorage.getItem('onboarding_progress')

  if (savedData) {
    try {
      const { currentStep: savedStep } = JSON.parse(savedData)

      if (savedStep && savedStep !== 'WELCOME' && savedStep !== 'ACTIVATED') {
        showRecoveryModal.value = true
      }
    } catch (error) {
      console.error('Failed to parse saved onboarding data:', error)
    }
  }
})
</script>
