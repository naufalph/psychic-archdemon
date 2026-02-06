<template>
  <div class="space-y-2">
    <label v-if="label" class="block text-xs font-bold text-gray-500 uppercase">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <div
      v-if="!preview"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
      :class="dropzoneClasses"
    >
      <input ref="fileInput" type="file" accept="image/*" @change="handleFileSelect" class="hidden" />
      <div @click="$refs.fileInput.click()" class="text-center py-8">
        <Upload :size="32" class="text-gray-400 mx-auto mb-3" />
        <p class="text-sm text-gray-600 font-medium mb-1">Drop image here or click to upload</p>
        <p class="text-xs text-gray-400">PNG, JPG, GIF up to 5MB</p>
      </div>
    </div>

    <div v-else class="relative group">
      <img :src="preview" :alt="label" class="w-full h-48 object-cover rounded-xl border border-gray-200" />
      <button
        @click="clear"
        class="absolute top-2 right-2 p-2 bg-white/90 rounded-full opacity-0 group-hover:opacity-100 transition"
      >
        <X :size="20" class="text-gray-700" />
      </button>
    </div>

    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Upload, X } from 'lucide-vue-next'
import { useFileUpload } from '@/composables/useFileUpload'

const props = defineProps({
  label: String,
  required: Boolean,
  modelValue: {
    type: File,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const {
  files,
  previews,
  isDragging,
  error,
  addFiles,
  clear: clearFiles,
  handleDrop: dropHandler,
  handleDragOver,
  handleDragLeave
} = useFileUpload({
  multiple: false,
  maxFiles: 1
})

const fileInput = ref(null)
const preview = ref(null)

const handleFileSelect = e => {
  if (e.target.files && e.target.files.length > 0) {
    try {
      addFiles(e.target.files)
    } catch (err) {
      console.error('File upload error:', err)
    }
  }
}

const handleDrop = e => {
  dropHandler(e)
}

const clear = () => {
  clearFiles()
  preview.value = null
  emit('update:modelValue', null)
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

watch(files, newFiles => {
  if (newFiles.length > 0) {
    emit('update:modelValue', newFiles[0])
  }
})

watch(previews, newPreviews => {
  preview.value = newPreviews.length > 0 ? newPreviews[0] : null
})

watch(
  () => props.modelValue,
  newValue => {
    if (!newValue && preview.value) {
      clear()
    }
  }
)

const dropzoneClasses = ref([
  'border-2 border-dashed rounded-xl cursor-pointer transition-all',
  isDragging.value ? 'border-[#7C4728] bg-[#F5E6D3]/20' : 'border-gray-300 hover:border-[#C5A17A]'
])

watch(isDragging, newValue => {
  dropzoneClasses.value = [
    'border-2 border-dashed rounded-xl cursor-pointer transition-all',
    newValue ? 'border-[#7C4728] bg-[#F5E6D3]/20' : 'border-gray-300 hover:border-[#C5A17A]'
  ]
})
</script>
