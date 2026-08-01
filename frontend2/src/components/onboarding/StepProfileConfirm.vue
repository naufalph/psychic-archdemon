<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-2xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">{{ t.onboarding.profileConfirm.title }}</h2>
        <p class="text-lg text-black/60 tracking-tight">{{ t.onboarding.profileConfirm.subtitle }}</p>
      </div>

      <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5 space-y-6">
        <div class="space-y-4">
          <div class="space-y-1">
            <p class="text-xs text-black/40 uppercase tracking-wider">{{ t.onboarding.profileConfirm.practiceName }}</p>
            <p class="text-xl font-semibold text-black">{{ store.profile.name }}</p>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-1">
              <p class="text-xs text-black/40 uppercase tracking-wider">{{ t.onboarding.profileConfirm.city }}</p>
              <p class="text-lg font-medium text-black">{{ store.profile.city }}</p>
            </div>
            <div class="space-y-1">
              <p class="text-xs text-black/40 uppercase tracking-wider">{{ t.onboarding.profileConfirm.experience }}</p>
              <p class="text-lg font-medium text-black">{{ store.profile.experienceRange }}</p>
            </div>
          </div>

          <div class="space-y-1">
            <p class="text-xs text-black/40 uppercase tracking-wider">{{ t.onboarding.profileConfirm.philosophy }}</p>
            <p class="text-base text-black/70 leading-relaxed">{{ store.profile.philosophy }}</p>
          </div>

          <div class="space-y-2">
            <p class="text-xs text-black/40 uppercase tracking-wider">{{ t.onboarding.profileConfirm.expertise }}</p>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="expertise in store.profile.expertise"
                :key="expertise"
                class="px-3 py-1 bg-brand-brown/10 text-brand-brown rounded-full text-sm font-medium"
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
            {{ t.onboarding.profileConfirm.editProfile }}
          </button>
        </div>
      </div>

      <div class="flex justify-between items-center pt-4">
        <button @click="emit('back')" class="px-6 py-3 text-black/60 hover:text-black transition-colors">
          {{ t.onboarding.profileConfirm.back }}
        </button>
        <button
          @click="handleNext"
          :disabled="store.isLoading"
          class="px-8 py-3 bg-brand-brown text-white rounded-full font-semibold hover:bg-brand-brown-dark transition-all hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ store.isLoading ? t.onboarding.profileConfirm.saving : t.onboarding.profileConfirm.proceed }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useOnboardingStore } from '@/stores/onboarding'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

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
