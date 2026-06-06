<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-3xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">{{ t.onboarding.expertise.title }}</h2>
        <p class="text-lg text-black/60 tracking-tight">{{ t.onboarding.expertise.subtitle }}</p>
      </div>

      <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5">
        <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
          <button
            v-for="tag in EXPERTISE_TAGS"
            :key="tag"
            @click="toggleTag(tag)"
            :class="[
              'px-4 py-3 rounded-2xl font-medium text-sm transition-all',
              selectedTags.includes(tag)
                ? 'bg-[#7C4728] text-white shadow-md scale-105'
                : 'bg-black/5 text-black/60 hover:bg-black/10'
            ]"
          >
            {{ tag }}
          </button>
        </div>

        <div v-if="selectedTags.length > 0" class="mt-6 pt-6 border-t border-black/10">
          <p class="text-sm text-black/60">
            Selected {{ selectedTags.length }} {{ selectedTags.length === 1 ? 'expertise' : 'expertises' }}
          </p>
        </div>
      </div>

      <div class="flex justify-between items-center pt-4">
        <button @click="emit('back')" class="px-6 py-3 text-black/60 hover:text-black transition-colors">{{ t.onboarding.expertise.back }}</button>
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
import { EXPERTISE_TAGS } from '@/constants/onboarding'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const emit = defineEmits(['next', 'back'])
const store = useOnboardingStore()

const selectedTags = ref([...store.profile.expertise])

const isFormValid = computed(() => selectedTags.value.length > 0)

const toggleTag = tag => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

const handleNext = () => {
  if (isFormValid.value) {
    store.updateExpertise(selectedTags.value)
    emit('next')
  }
}
</script>
