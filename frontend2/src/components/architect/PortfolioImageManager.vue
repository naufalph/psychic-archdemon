<template>
  <div class="space-y-4">
    <label class="block text-sm font-semibold text-black/70 tracking-tight">
      {{ t.portfolio.form.images }}
    </label>

    <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
      <div
        v-for="(image, index) in displayImages"
        :key="image.id || `preview-${index}`"
        class="aspect-square rounded-2xl overflow-hidden relative group bg-black/5"
      >
        <img :src="getImageUrl(image)" :alt="`Image ${index + 1}`" class="w-full h-full object-cover" />
        <button
          @click="handleDeleteImage(image, index)"
          :disabled="isDeleting"
          class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center hover:bg-red-600 disabled:opacity-50"
        >
          <svg v-if="!isDeleting" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
          <svg v-else class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path
              class="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
        </button>
      </div>

      <label
        v-if="displayImages.length < 8"
        class="aspect-square rounded-2xl border-2 border-dashed border-black/20 hover:border-brand-brown hover:bg-brand-brown/5 transition-all flex flex-col items-center justify-center cursor-pointer group"
        @drop.prevent="handleDrop"
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        :class="{ 'border-brand-brown bg-brand-brown/5': isDragging }"
      >
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          multiple
          class="hidden"
          @change="handleFileSelect"
          :disabled="isUploading"
        />
        <svg
          v-if="!isUploading"
          class="w-8 h-8 text-black/40 group-hover:text-brand-brown transition-colors mb-2"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        <svg v-else class="w-8 h-8 text-brand-brown animate-spin mb-2" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        <span class="text-xs text-black/60 text-center px-2">
          {{ isUploading ? t.portfolio.imageManager.uploading : t.portfolio.imageManager.uploadZone }}
        </span>
      </label>
    </div>

    <div class="text-xs text-black/40 space-y-1">
      <p>{{ displayImages.length }}/8 {{ t.portfolio.imageManager.currentCount }}</p>
      <p>{{ t.portfolio.imageManager.maxSize }}</p>
    </div>

    <div v-if="uploadError" class="p-3 bg-red-50 border border-red-200 rounded-2xl text-sm text-red-600">
      {{ uploadError }}
    </div>

    <div v-if="uploadProgress > 0 && uploadProgress < 100" class="space-y-2">
      <div class="flex justify-between text-xs text-gray-600">
        <span>Uploading...</span>
        <span>{{ uploadProgress }}%</span>
      </div>
      <div class="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
        <div class="bg-brand-brown h-full transition-all duration-300" :style="{ width: `${uploadProgress}%` }"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from '@/composables/useI18n'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  newFiles: {
    type: Array,
    default: () => []
  },
  portfolioId: {
    type: Number,
    default: null
  },
  isUploading: {
    type: Boolean,
    default: false
  },
  uploadProgress: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:images', 'update:newFiles', 'deleteImage', 'uploadImages'])

const { t, getT } = useI18n()

const fileInput = ref(null)
const uploadError = ref('')
const isDragging = ref(false)
const isDeleting = ref(false)

const displayImages = computed(() => {
  const existing = props.images || []
  const previews = props.newFiles.map((file, index) => ({
    id: `preview-${index}`,
    file,
    preview: URL.createObjectURL(file)
  }))
  return [...existing, ...previews]
})

const getImageUrl = image => {
  if (image.preview) return image.preview
  if (image.mediumUrl) return image.mediumUrl
  if (image.largeUrl) return image.largeUrl
  if (image.originalUrl) return image.originalUrl
  return ''
}

const validateFiles = files => {
  const validFiles = []
  uploadError.value = ''

  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      uploadError.value = getT('portfolio.validation.invalidFileType')
      continue
    }
    if (file.size > 10 * 1024 * 1024) {
      uploadError.value = getT('portfolio.validation.fileTooLarge')
      continue
    }
    validFiles.push(file)
  }

  return validFiles
}

const handleFileSelect = event => {
  const files = Array.from(event.target.files)
  addFiles(files)
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const handleDrop = event => {
  isDragging.value = false
  const files = Array.from(event.dataTransfer.files)
  addFiles(files)
}

const addFiles = files => {
  const validFiles = validateFiles(files)
  const remainingSlots = 8 - displayImages.value.length
  const filesToAdd = validFiles.slice(0, remainingSlots)

  if (filesToAdd.length < validFiles.length) {
    uploadError.value = getT('portfolio.validation.maxImagesReached')
  }

  if (filesToAdd.length > 0) {
    const updatedNewFiles = [...props.newFiles, ...filesToAdd]
    emit('update:newFiles', updatedNewFiles)

    if (props.portfolioId) {
      emit('uploadImages', filesToAdd)
    }
  }
}

const handleDeleteImage = async (image, index) => {
  if (image.preview) {
    const newFilesIndex = index - props.images.length
    const updatedNewFiles = [...props.newFiles]
    updatedNewFiles.splice(newFilesIndex, 1)
    emit('update:newFiles', updatedNewFiles)
    URL.revokeObjectURL(image.preview)
  } else if (image.id) {
    try {
      isDeleting.value = true
      emit('deleteImage', image.id)
    } catch (error) {
      console.error('Delete image error:', error)
      uploadError.value = getT('portfolio.toast.imageDeleteError')
    } finally {
      isDeleting.value = false
    }
  }
}
</script>
