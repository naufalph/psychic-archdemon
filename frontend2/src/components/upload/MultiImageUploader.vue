<template>
  <div class="space-y-2">
    <label v-if="label" class="block text-xs font-bold text-gray-500 uppercase">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <div
      v-if="files.length === 0"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
      :class="dropzoneClasses"
    >
      <input ref="fileInput" type="file" accept="image/*" multiple @change="handleFileSelect" class="hidden" />
      <div @click="$refs.fileInput.click()" class="text-center py-8">
        <Upload :size="32" class="text-gray-400 mx-auto mb-3" />
        <p class="text-sm text-gray-600 font-medium mb-1">Drop images here or click to upload</p>
        <p class="text-xs text-gray-400">PNG, JPG, GIF up to 5MB each (max {{ maxFiles }} files)</p>
      </div>
    </div>

    <div v-else class="grid grid-cols-3 gap-3">
      <div
        v-if="files.length < maxFiles"
        @click="$refs.addInput.click()"
        class="aspect-square border-2 border-dashed border-gray-300 rounded-xl flex items-center justify-center cursor-pointer hover:border-[#C5A17A] transition"
      >
        <input ref="addInput" type="file" accept="image/*" multiple @change="handleFileSelect" class="hidden" />
        <Plus :size="32" class="text-gray-400" />
      </div>

      <div
        v-for="(preview, index) in previews"
        :key="index"
        class="aspect-square relative rounded-xl overflow-hidden border border-gray-200 group"
      >
        <img :src="preview" class="w-full h-full object-cover" />
        <button
          @click="removeFile(index)"
          class="absolute top-1 right-1 p-1 bg-white/90 rounded-full opacity-0 group-hover:opacity-100 transition"
        >
          <X :size="16" class="text-gray-700" />
        </button>
      </div>
    </div>

    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Upload, Plus, X } from 'lucide-vue-next'
import { useFileUpload } from '@/composables/useFileUpload'

const props = defineProps({
  label: String,
  required: Boolean,
  maxFiles: {
    type: Number,
    default: 10
  },
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const {
  files,
  previews,
  isDragging,
  error,
  addFiles,
  removeFile: removeFileFromComposable,
  handleDrop: dropHandler,
  handleDragOver,
  handleDragLeave
} = useFileUpload({
  multiple: true,
  maxFiles: props.maxFiles
})

const fileInput = ref(null)
const addInput = ref(null)

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

const removeFile = index => {
  removeFileFromComposable(index)
}

watch(
  files,
  newFiles => {
    emit('update:modelValue', newFiles)
  },
  { deep: true }
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
