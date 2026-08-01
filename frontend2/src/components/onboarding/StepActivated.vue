<template>
  <div class="flex flex-col items-center justify-center min-h-[60vh] text-center">
    <div
      v-motion
      :initial="{ opacity: 0, scale: 0.8 }"
      :enter="{ opacity: 1, scale: 1, transition: { duration: 800 } }"
      class="max-w-2xl mx-auto space-y-8"
    >
      <div class="relative">
        <div
          class="w-32 h-32 mx-auto rounded-full bg-gradient-to-r from-brand-brown to-brand-brown-light flex items-center justify-center animate-bounce"
        >
          <svg class="w-16 h-16 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>

        <div
          v-for="i in 20"
          :key="i"
          :class="[
            'absolute w-2 h-2 rounded-full',
            i % 3 === 0 ? 'bg-brand-brown' : i % 3 === 1 ? 'bg-brand-brown-light' : 'bg-yellow-400'
          ]"
          :style="confettiStyle(i)"
        />
      </div>

      <div class="space-y-4">
        <h1 class="text-6xl md:text-7xl font-black tracking-tighter text-black">{{ t.onboarding.activated.title }}</h1>
        <p class="text-xl text-black/70 tracking-tight max-w-xl mx-auto">{{ t.onboarding.activated.subtitle }}</p>
      </div>

      <div class="bg-gradient-to-r from-brand-brown/10 to-brand-brown-light/10 rounded-3xl p-8 space-y-4">
        <h3 class="text-2xl font-bold text-black">{{ t.onboarding.activated.whatsNext }}</h3>
        <div class="text-left max-w-md mx-auto space-y-3">
          <div class="flex items-start gap-3">
            <div class="w-6 h-6 rounded-full bg-brand-brown flex items-center justify-center flex-shrink-0 mt-0.5">
              <span class="text-white text-xs font-bold">1</span>
            </div>
            <p class="text-black/70">{{ t.onboarding.activated.step1 }}</p>
          </div>
          <div class="flex items-start gap-3">
            <div class="w-6 h-6 rounded-full bg-brand-brown flex items-center justify-center flex-shrink-0 mt-0.5">
              <span class="text-white text-xs font-bold">2</span>
            </div>
            <p class="text-black/70">{{ t.onboarding.activated.step2 }}</p>
          </div>
          <div class="flex items-start gap-3">
            <div class="w-6 h-6 rounded-full bg-brand-brown flex items-center justify-center flex-shrink-0 mt-0.5">
              <span class="text-white text-xs font-bold">3</span>
            </div>
            <p class="text-black/70">{{ t.onboarding.activated.step3 }}</p>
          </div>
        </div>
      </div>

      <div class="flex flex-col items-center gap-3 pt-4">
        <button
          @click="goToDashboard"
          class="px-8 py-4 bg-brand-brown text-white rounded-full font-semibold hover:bg-brand-brown-dark transition-all hover:scale-105 text-lg"
        >
          {{ t.onboarding.activated.dashboard }}
        </button>
        <p class="text-sm text-black/40 tracking-wide">
          {{ t.onboarding.activated.redirecting.replace('{n}', countdown) }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOnboardingStore } from '@/stores/onboarding'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const router = useRouter()
const store = useOnboardingStore()
const authStore = useAuthStore()
const countdown = ref(5)
let intervalId = null

const confettiStyle = index => {
  const angle = (index * 360) / 20
  const distance = 100 + Math.random() * 50
  const x = Math.cos((angle * Math.PI) / 180) * distance
  const y = Math.sin((angle * Math.PI) / 180) * distance

  return {
    left: '50%',
    top: '50%',
    transform: `translate(${x}px, ${y}px)`,
    animation: `confetti-fall 2s ease-out ${index * 0.05}s forwards`
  }
}

const goToDashboard = async () => {
  clearInterval(intervalId)
  await authStore.fetchUserData()
  await router.push('/architect/dashboard')
  store.clearOnboardingData()
}

onMounted(async () => {
  await authStore.fetchUserData()

  intervalId = setInterval(async () => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(intervalId)
      await router.push('/architect/dashboard')
      store.clearOnboardingData()
    }
  }, 1000)
})

onUnmounted(() => {
  clearInterval(intervalId)
})
</script>

<style scoped>
@keyframes confetti-fall {
  0% {
    opacity: 1;
    transform: translate(0, 0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(var(--x, 0), var(--y, 100px)) scale(0.5);
  }
}
</style>
