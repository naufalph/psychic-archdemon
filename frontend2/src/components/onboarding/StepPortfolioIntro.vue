<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-4xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">{{ t.onboarding.portfolioIntro.title }}</h2>
        <p class="text-lg text-black/60 tracking-tight">{{ t.onboarding.portfolioIntro.subtitle }}</p>
      </div>

      <div v-if="store.portfolio.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div
          v-for="(project, index) in store.portfolio"
          :key="index"
          class="bg-white rounded-3xl overflow-hidden shadow-sm border border-black/5 group hover:shadow-md transition-all"
        >
          <div class="aspect-video bg-black/5 relative overflow-hidden">
            <div
              v-if="project.images && project.images.length > 0"
              class="w-full h-full flex items-center justify-center text-black/20"
            >
              <svg class="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            </div>
          </div>
          <div class="p-6 space-y-3">
            <h3 class="text-xl font-bold text-black">{{ project.name }}</h3>
            <div class="flex items-center gap-3 text-sm text-black/60">
              <span>{{ project.type }}</span>
              <span>•</span>
              <span>{{ project.year }}</span>
            </div>
            <button @click="deleteProject(index)" class="text-sm text-red-500 hover:text-red-600 transition-colors">
              {{ t.onboarding.portfolioIntro.remove }}
            </button>
          </div>
        </div>

        <button
          @click="handleAddProject"
          class="bg-white rounded-3xl border-2 border-dashed border-black/20 hover:border-brand-brown hover:bg-brand-brown/5 transition-all flex flex-col items-center justify-center min-h-[300px] group"
        >
          <div
            class="w-16 h-16 rounded-full bg-black/5 group-hover:bg-brand-brown/10 flex items-center justify-center mb-4 transition-all"
          >
            <svg
              class="w-8 h-8 text-black/40 group-hover:text-brand-brown transition-colors"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
          </div>
          <p class="text-lg font-semibold text-black/60 group-hover:text-brand-brown transition-colors">
            {{ t.onboarding.portfolioIntro.addAnother }}
          </p>
        </button>
      </div>

      <div v-else class="bg-white rounded-3xl border-2 border-dashed border-black/20 p-12 text-center">
        <div class="max-w-md mx-auto space-y-6">
          <div class="w-20 h-20 mx-auto rounded-full bg-black/5 flex items-center justify-center">
            <svg class="w-10 h-10 text-black/40" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
              />
            </svg>
          </div>
          <div class="space-y-2">
            <h3 class="text-2xl font-bold text-black">{{ t.onboarding.portfolioIntro.startPortfolio }}</h3>
            <p class="text-black/60">{{ t.onboarding.portfolioIntro.startDesc }}</p>
          </div>
          <button
            @click="handleAddProject"
            class="px-8 py-3 bg-brand-brown text-white rounded-full font-semibold hover:bg-brand-brown-dark transition-all hover:scale-105"
          >
            {{ t.onboarding.portfolioIntro.addFirst }}
          </button>
        </div>
      </div>

      <div class="pt-2">
        <BaseAlert v-if="store.portfolio.length === 0" variant="warning">
          Portofolio membantu menarik klien. Anda tetap bisa melanjutkan dan menambahkannya nanti di halaman profil.
        </BaseAlert>
      </div>

      <div class="flex justify-between items-center pt-4">
        <button @click="emit('back')" class="px-6 py-3 text-black/60 hover:text-black transition-colors">
          {{ t.onboarding.portfolioIntro.back }}
        </button>
        <div class="flex items-center gap-3">
          <button
            v-if="store.portfolio.length === 0"
            @click="emit('skip')"
            class="px-6 py-3 text-black/40 hover:text-black/70 transition-colors text-sm font-medium"
          >
            Lewati
          </button>
          <button
            @click="handleNext"
            class="px-8 py-3 bg-brand-brown text-white rounded-full font-semibold hover:bg-brand-brown-dark transition-all hover:scale-105"
          >
            {{ t.onboarding.portfolioIntro.continue }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useOnboardingStore } from '@/stores/onboarding'
import { useI18n } from '@/composables/useI18n'
import BaseAlert from '@/components/ui/BaseAlert.vue'

const { t } = useI18n()

const emit = defineEmits(['next', 'back', 'skip'])
const store = useOnboardingStore()

const handleAddProject = () => {
  store.createProject()
  emit('next')
}

const deleteProject = index => {
  if (confirm(t.value.onboarding.portfolioIntro.removeConfirm)) {
    store.deleteProject(index)
  }
}

const handleNext = () => {
  emit('next')
}
</script>
