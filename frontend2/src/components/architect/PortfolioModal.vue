<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="isOpen" class="fixed inset-0 z-50 overflow-y-auto" @click.self="handleBackdropClick">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm"></div>

        <div class="flex min-h-screen items-end md:items-center justify-center p-0 md:p-4">
          <Transition
            enter-active-class="transition ease-out duration-300"
            enter-from-class="opacity-0 translate-y-4 md:translate-y-0 md:scale-95"
            enter-to-class="opacity-100 translate-y-0 md:scale-100"
            leave-active-class="transition ease-in duration-200"
            leave-from-class="opacity-100 translate-y-0 md:scale-100"
            leave-to-class="opacity-0 translate-y-4 md:translate-y-0 md:scale-95"
          >
            <div v-if="isOpen" class="relative bg-white w-full md:max-w-3xl md:rounded-2xl rounded-t-3xl shadow-xl">
              <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 md:rounded-t-2xl rounded-t-3xl z-10">
                <div class="flex items-center justify-between">
                  <h2 class="text-2xl font-black tracking-tight text-black">
                    {{ isEditMode ? t.portfolio.modal.editTitle : t.portfolio.modal.createTitle }}
                  </h2>
                  <button
                    @click="handleClose"
                    class="w-10 h-10 rounded-full hover:bg-gray-100 transition-colors flex items-center justify-center"
                  >
                    <svg class="w-5 h-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </div>

              <form
                @submit.prevent="handleSubmit"
                class="p-6 space-y-6 max-h-[calc(100vh-200px)] md:max-h-[calc(100vh-160px)] overflow-y-auto"
              >
                <div class="space-y-2">
                  <label class="block text-sm font-semibold text-black/70 tracking-tight">
                    {{ t.portfolio.form.title }}
                  </label>
                  <input
                    v-model="formData.title"
                    type="text"
                    :placeholder="t.portfolio.form.titlePlaceholder"
                    class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all"
                    :class="{ 'border-red-300': errors.title }"
                  />
                  <p v-if="errors.title" class="text-xs text-red-600">{{ errors.title }}</p>
                </div>

                <div class="space-y-2">
                  <label class="block text-sm font-semibold text-black/70 tracking-tight">
                    {{ t.portfolio.form.description }}
                  </label>
                  <textarea
                    v-model="formData.description"
                    rows="4"
                    :placeholder="t.portfolio.form.descriptionPlaceholder"
                    class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all resize-none"
                  />
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div class="space-y-2">
                    <label class="block text-sm font-semibold text-black/70 tracking-tight">
                      {{ t.portfolio.form.projectDate }}
                    </label>
                    <input
                      v-model="formData.projectDate"
                      type="date"
                      class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all"
                      :class="{ 'border-red-300': errors.projectDate }"
                      :max="today"
                    />
                    <p v-if="errors.projectDate" class="text-xs text-red-600">{{ errors.projectDate }}</p>
                  </div>

                  <div class="space-y-2">
                    <label class="block text-sm font-semibold text-black/70 tracking-tight">
                      {{ t.portfolio.form.location }}
                    </label>
                    <input
                      v-model="formData.location"
                      type="text"
                      :placeholder="t.portfolio.form.locationPlaceholder"
                      class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all"
                    />
                  </div>
                </div>

                <div class="space-y-2">
                  <label class="block text-sm font-semibold text-black/70 tracking-tight">
                    {{ t.portfolio.form.projectType }}
                  </label>
                  <select
                    v-model="formData.projectType"
                    class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-[#7C4728] focus:ring-2 focus:ring-[#7C4728]/20 outline-none transition-all bg-white"
                    :class="{ 'border-red-300': errors.projectType }"
                  >
                    <option value="" disabled>{{ t.portfolio.form.projectTypePlaceholder }}</option>
                    <option v-for="type in PROJECT_TYPES" :key="type" :value="type">
                      {{ type }}
                    </option>
                  </select>
                  <p v-if="errors.projectType" class="text-xs text-red-600">{{ errors.projectType }}</p>
                </div>

                <div class="flex items-center gap-3">
                  <input
                    id="isBuilt"
                    v-model="formData.isBuilt"
                    type="checkbox"
                    class="w-5 h-5 rounded border-gray-300 text-[#7C4728] focus:ring-[#7C4728]"
                  />
                  <label for="isBuilt" class="text-sm font-medium text-black/70 cursor-pointer">
                    {{ t.portfolio.form.isBuilt }}
                  </label>
                </div>

                <PortfolioImageManager
                  :images="existingImages"
                  :new-files="newFiles"
                  :portfolio-id="portfolio?.id"
                  :is-uploading="isUploading"
                  :upload-progress="uploadProgress"
                  @update:new-files="newFiles = $event"
                  @delete-image="handleDeleteImage"
                  @upload-images="handleUploadImages"
                />
                <p v-if="errors.images" class="text-xs text-red-600">{{ errors.images }}</p>

                <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-2xl text-sm text-red-600">
                  {{ error }}
                </div>
              </form>

              <div class="sticky bottom-0 bg-white border-t border-gray-200 px-6 py-4 md:rounded-b-2xl">
                <div class="flex justify-end items-center gap-4">
                  <button
                    type="button"
                    @click="handleClose"
                    class="px-6 py-3 text-black/60 hover:text-black transition-colors font-semibold"
                  >
                    {{ t.portfolio.modal.cancel }}
                  </button>
                  <button
                    @click="handleSubmit"
                    :disabled="!isFormValid || isLoading"
                    :class="[
                      'px-8 py-3 rounded-full font-semibold transition-all flex items-center gap-2',
                      isFormValid && !isLoading
                        ? 'bg-[#7C4728] text-white hover:bg-[#6A3D22] hover:scale-105'
                        : 'bg-black/10 text-black/30 cursor-not-allowed'
                    ]"
                  >
                    <svg v-if="isLoading" class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                      <path
                        class="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      />
                    </svg>
                    {{ isLoading ? 'Saving...' : t.portfolio.modal.save }}
                  </button>
                </div>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useI18n } from '@/composables/useI18n'
import { PROJECT_TYPES } from '@/constants/onboarding'
import PortfolioImageManager from './PortfolioImageManager.vue'

const props = defineProps({
  portfolio: {
    type: Object,
    default: null
  },
  isOpen: {
    type: Boolean,
    required: true
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['close', 'save'])

const { t, getT } = useI18n()

const formData = ref({
  title: '',
  description: '',
  projectDate: '',
  location: '',
  projectType: '',
  isBuilt: true
})

const existingImages = ref([])
const newFiles = ref([])
const isUploading = ref(false)
const uploadProgress = ref(0)
const hasUnsavedChanges = ref(false)

const errors = ref({
  title: '',
  projectDate: '',
  projectType: '',
  images: ''
})

const isEditMode = computed(() => props.portfolio !== null)
const today = computed(() => new Date().toISOString().split('T')[0])

const totalImageCount = computed(() => {
  return existingImages.value.length + newFiles.value.length
})

const isFormValid = computed(() => {
  const hasTitle = formData.value.title.trim().length > 0
  const hasDate = formData.value.projectDate.length > 0
  const hasType = formData.value.projectType.length > 0
  const hasImages = totalImageCount.value > 0

  return hasTitle && hasDate && hasType && hasImages
})

const resetForm = () => {
  formData.value = {
    title: '',
    description: '',
    projectDate: '',
    location: '',
    projectType: '',
    isBuilt: true
  }
  existingImages.value = []
  newFiles.value = []
  errors.value = {
    title: '',
    projectDate: '',
    projectType: '',
    images: ''
  }
  hasUnsavedChanges.value = false
}

watch(
  () => props.portfolio,
  portfolio => {
    if (portfolio) {
      formData.value = {
        title: portfolio.title || '',
        description: portfolio.description || '',
        projectDate: portfolio.projectDate || '',
        location: portfolio.location || '',
        projectType: portfolio.projectType || '',
        isBuilt: portfolio.isBuilt !== false
      }
      existingImages.value = portfolio.images || []
      newFiles.value = []
    } else {
      resetForm()
    }
    hasUnsavedChanges.value = false
  },
  { immediate: true }
)

watch(
  formData,
  () => {
    hasUnsavedChanges.value = true
  },
  { deep: true }
)

const validateForm = () => {
  errors.value = {
    title: '',
    projectDate: '',
    projectType: '',
    images: ''
  }

  let isValid = true

  if (!formData.value.title.trim()) {
    errors.value.title = getT('portfolio.validation.titleRequired')
    isValid = false
  }

  if (!formData.value.projectDate) {
    errors.value.projectDate = getT('portfolio.validation.projectDateRequired')
    isValid = false
  }

  if (!formData.value.projectType) {
    errors.value.projectType = getT('portfolio.validation.projectTypeRequired')
    isValid = false
  }

  if (totalImageCount.value === 0) {
    errors.value.images = getT('portfolio.validation.imagesRequired')
    isValid = false
  }

  return isValid
}

const handleDeleteImage = async imageId => {
  emit('save', {
    action: 'deleteImage',
    imageId,
    portfolioId: props.portfolio?.id
  })
}

const handleUploadImages = async files => {
  if (!props.portfolio?.id) return

  isUploading.value = true
  uploadProgress.value = 0

  emit('save', {
    action: 'uploadImages',
    files,
    portfolioId: props.portfolio.id
  })

  isUploading.value = false
  uploadProgress.value = 0
  newFiles.value = []
}

const handleSubmit = async () => {
  if (!validateForm()) return

  if (isEditMode.value) {
    const updateData = {
      title: formData.value.title,
      description: formData.value.description,
      projectDate: formData.value.projectDate,
      location: formData.value.location,
      projectType: formData.value.projectType,
      isBuilt: formData.value.isBuilt
    }

    emit('save', {
      action: 'update',
      portfolioId: props.portfolio.id,
      data: updateData,
      newImages: newFiles.value
    })
  } else {
    const formDataToSubmit = new FormData()
    formDataToSubmit.append('title', formData.value.title)
    formDataToSubmit.append('description', formData.value.description || '')
    formDataToSubmit.append('projectDate', formData.value.projectDate)
    formDataToSubmit.append('location', formData.value.location || '')
    formDataToSubmit.append('projectType', formData.value.projectType)
    formDataToSubmit.append('isBuilt', formData.value.isBuilt)

    newFiles.value.forEach(file => {
      formDataToSubmit.append('images', file)
    })

    emit('save', {
      action: 'create',
      formData: formDataToSubmit
    })
  }
}

const handleBackdropClick = () => {
  handleClose()
}

const handleClose = () => {
  if (hasUnsavedChanges.value) {
    if (confirm(getT('portfolio.modal.unsavedChanges'))) {
      emit('close')
      resetForm()
    }
  } else {
    emit('close')
    resetForm()
  }
}
</script>
