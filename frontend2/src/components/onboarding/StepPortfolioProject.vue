<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-3xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">{{ t.onboarding.portfolioProject.title }}</h2>
        <p class="text-lg text-black/60 tracking-tight">{{ t.onboarding.portfolioProject.subtitle }}</p>
      </div>

      <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5 space-y-6">
        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">{{ t.onboarding.portfolioProject.name }}</label>
          <input
            v-model="projectData.name"
            type="text"
            :placeholder="t.onboarding.portfolioProject.namePlaceholder"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all"
          />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <label class="block text-sm font-semibold text-black/70 tracking-tight">{{ t.onboarding.portfolioProject.type }}</label>
            <select
              v-model="projectData.type"
              class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all bg-white"
            >
              <option value="" disabled>{{ t.onboarding.portfolioProject.typePlaceholder }}</option>
              <option v-for="type in PROJECT_TYPES" :key="type" :value="type">
                {{ type }}
              </option>
            </select>
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-semibold text-black/70 tracking-tight">{{ t.onboarding.portfolioProject.year }}</label>
            <input
              v-model.number="projectData.year"
              type="number"
              :min="1950"
              :max="new Date().getFullYear()"
              placeholder="2024"
              class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all"
            />
          </div>
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">{{ t.onboarding.portfolioProject.challenge }}</label>
          <textarea
            v-model="projectData.story.problem"
            rows="3"
            :placeholder="t.onboarding.portfolioProject.challengePlaceholder"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all resize-none"
          />
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">{{ t.onboarding.portfolioProject.solution }}</label>
          <textarea
            v-model="projectData.story.decision"
            rows="3"
            :placeholder="t.onboarding.portfolioProject.solutionPlaceholder"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all resize-none"
          />
        </div>

        <div class="space-y-3">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">{{ t.onboarding.portfolioProject.images }}</label>

          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <div
              v-for="(image, index) in projectData.images"
              :key="index"
              class="aspect-square rounded-2xl overflow-hidden relative group bg-black/5"
            >
              <img :src="previewImage(image)" alt="Project image" class="w-full h-full object-cover" />
              <button
                @click="removeImage(index)"
                class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <label
              v-if="projectData.images.length < 8"
              class="aspect-square rounded-2xl border-2 border-dashed border-black/20 hover:border-[#7C4728] hover:bg-[#7C4728]/5 transition-all flex items-center justify-center cursor-pointer group"
            >
              <input ref="fileInput" type="file" accept="image/*" multiple class="hidden" @change="handleImageUpload" />
              <svg
                class="w-8 h-8 text-black/40 group-hover:text-[#7C4728] transition-colors"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
            </label>
          </div>

          <p class="text-xs text-black/40">
            {{ t.onboarding.portfolioProject.imagesCount.replace('{n}', projectData.images.length) }}
          </p>

          <div v-if="uploadError" class="p-3 bg-red-50 border border-red-200 rounded-2xl text-sm text-red-600">
            {{ uploadError }}
          </div>
        </div>
      </div>

      <div v-if="store.error" class="p-4 bg-red-50 border border-red-200 rounded-2xl text-sm text-red-600">
        {{ store.error }}
      </div>

      <div class="flex justify-between items-center pt-4">
        <button @click="handleBack" class="px-6 py-3 text-black/60 hover:text-black transition-colors">{{ t.onboarding.portfolioProject.back }}</button>
        <button
          @click="handleSave"
          :disabled="!isFormValid || store.isLoading"
          :class="[
            'px-8 py-3 rounded-full font-semibold transition-all flex items-center gap-2',
            isFormValid && !store.isLoading
              ? 'bg-[#7C4728] text-white hover:bg-[#6A3D22] hover:scale-105'
              : 'bg-black/10 text-black/30 cursor-not-allowed'
          ]"
        >
          <svg v-if="store.isLoading" class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path
              class="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          {{ store.isLoading ? t.onboarding.portfolioProject.saving : t.onboarding.portfolioProject.save }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useOnboardingStore } from '@/stores/onboarding'
import { PROJECT_TYPES } from '@/constants/onboarding'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const emit = defineEmits(['next', 'back'])
const store = useOnboardingStore()

const fileInput = ref(null)
const uploadError = ref('')

const projectData = reactive({
  name: store.currentProject?.name || '',
  type: store.currentProject?.type || '',
  year: store.currentProject?.year || new Date().getFullYear(),
  story: {
    problem: store.currentProject?.story?.problem || '',
    decision: store.currentProject?.story?.decision || ''
  },
  images: store.currentProject?.images || []
})

const isFormValid = computed(() => {
  return (
    projectData.name.trim() &&
    projectData.type &&
    projectData.year >= 1950 &&
    projectData.year <= new Date().getFullYear() &&
    projectData.images.length > 0
  )
})

const handleImageUpload = event => {
  uploadError.value = ''
  const files = Array.from(event.target.files)

  const validFiles = files.filter(file => {
    if (!file.type.startsWith('image/')) {
      uploadError.value = 'Only image files are allowed'
      return false
    }
    if (file.size > 10 * 1024 * 1024) {
      uploadError.value = 'Images must be less than 10MB'
      return false
    }
    return true
  })

  const remainingSlots = 8 - projectData.images.length
  const filesToAdd = validFiles.slice(0, remainingSlots)

  projectData.images.push(...filesToAdd)
  store.updateProject(projectData)

  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const previewImage = file => {
  return URL.createObjectURL(file)
}

const removeImage = index => {
  projectData.images.splice(index, 1)
  store.updateProject(projectData)
}

const handleSave = async () => {
  if (!isFormValid.value) return

  try {
    store.updateProject(projectData)
    await store.saveProject()
    emit('next')
  } catch (error) {
    console.error('Failed to save project:', error)
  }
}

const handleBack = () => {
  store.currentProject = null
  emit('back')
}

onMounted(() => {
  if (!store.currentProject) {
    store.createProject()
  }
})
</script>
