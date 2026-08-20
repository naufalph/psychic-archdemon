<template>
  <div class="space-y-2">
    <label v-if="label" class="block text-xs font-bold text-gray-500 uppercase">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <div
      v-if="!file"
      :class="dropzoneClasses"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
    >
      <input ref="fileInput" type="file" accept="application/pdf" class="hidden" @change="handleFileSelect" />
      <div class="text-center py-8 cursor-pointer" @click="$refs.fileInput.click()">
        <FileText :size="32" class="text-gray-400 mx-auto mb-3" />
        <p class="text-sm text-gray-600 font-medium mb-1">Drop PDF here or click to upload</p>
        <p class="text-xs text-gray-400">PDF up to 10MB</p>
      </div>
    </div>

    <div v-else class="border border-gray-200 rounded-xl p-4 flex items-center gap-3">
      <FileText :size="24" class="text-brand-brown" />
      <div class="flex-1 min-w-0">
        <p class="text-sm font-medium text-gray-900 truncate">{{ file.name }}</p>
        <p class="text-xs text-gray-500">{{ formatFileSize(file.size) }}</p>
      </div>
      <button class="p-1 rounded-full hover:bg-gray-100 transition" type="button" @click="removeFile">
        <X :size="20" class="text-gray-600" />
      </button>
    </div>

    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { FileText, X } from 'lucide-vue-next'

const props = defineProps({
  label: { type: String, default: '' },
  required: Boolean,
  maxSize: {
    type: Number,
    default: 10 * 1024 * 1024
  },
  modelValue: {
    type: File,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const file = ref(props.modelValue)
const error = ref(null)
const isDragging = ref(false)
const fileInput = ref(null)

const dropzoneClasses = computed(() => [
  'border-2 border-dashed rounded-xl transition-all',
  isDragging.value ? 'border-brand-brown bg-brand-tan/20' : 'border-gray-300 hover:border-brand-gold'
])

const formatFileSize = bytes => {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

const validateFile = selectedFile => {
  error.value = null

  if (selectedFile.type !== 'application/pdf') {
    error.value = 'Only PDF files are allowed'
    return false
  }

  if (selectedFile.size > props.maxSize) {
    error.value = `File size must be less than ${formatFileSize(props.maxSize)}`
    return false
  }

  return true
}

const handleFileSelect = e => {
  const selectedFile = e.target.files[0]
  if (selectedFile && validateFile(selectedFile)) {
    file.value = selectedFile
    emit('update:modelValue', selectedFile)
  }
}

const handleDrop = e => {
  isDragging.value = false
  const selectedFile = e.dataTransfer.files[0]
  if (selectedFile && validateFile(selectedFile)) {
    file.value = selectedFile
    emit('update:modelValue', selectedFile)
  }
}

const removeFile = () => {
  file.value = null
  error.value = null
  emit('update:modelValue', null)
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
</script>
