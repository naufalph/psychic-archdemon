<template>
  <div class="space-y-2">
    <label v-if="label" class="block text-xs font-bold text-gray-500 uppercase">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>

    <!-- Single hidden input always in DOM -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      multiple
      class="hidden"
      @change="handleFileSelect"
    />

    <!-- Empty state drop zone -->
    <div
      v-if="totalCount === 0"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
      @click="openPicker"
      :class="dropzoneClasses"
    >
      <div class="text-center py-8">
        <Upload :size="32" class="text-gray-400 mx-auto mb-3" />
        <p class="text-sm text-gray-600 font-medium mb-1">Drop images here or click to upload</p>
        <p class="text-xs text-gray-400">PNG, JPG, GIF up to 5MB each (max {{ maxFiles }} files)</p>
      </div>
    </div>

    <!-- Grid: existing saved images + new file previews + add button -->
    <div
      v-else
      class="grid grid-cols-3 gap-3"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
    >
      <!-- Existing images (already on backend) -->
      <div
        v-for="img in existingImages"
        :key="'ex-' + img.id"
        class="aspect-square relative rounded-xl overflow-hidden border border-gray-200 group"
      >
        <img :src="img.url" :alt="img.name" class="w-full h-full object-cover" />
        <button
          type="button"
          @click="$emit('delete-existing', img.id)"
          class="absolute top-1 right-1 p-1 bg-white/90 rounded-full opacity-0 group-hover:opacity-100 transition"
        >
          <X :size="16" class="text-gray-700" />
        </button>
      </div>

      <!-- New file previews (not yet uploaded) -->
      <div
        v-for="(preview, index) in previews"
        :key="'new-' + index"
        class="aspect-square relative rounded-xl overflow-hidden border border-gray-200 group"
      >
        <img :src="preview" class="w-full h-full object-cover" />
        <button
          type="button"
          @click="removeFile(index)"
          class="absolute top-1 right-1 p-1 bg-white/90 rounded-full opacity-0 group-hover:opacity-100 transition"
        >
          <X :size="16" class="text-gray-700" />
        </button>
      </div>

      <!-- Add more button (shown while below max) -->
      <div
        v-if="totalCount < maxFiles"
        @click="openPicker"
        class="aspect-square border-2 border-dashed border-gray-300 rounded-xl flex items-center justify-center cursor-pointer hover:border-[#C5A17A] transition"
      >
        <Plus :size="32" class="text-gray-400" />
      </div>
    </div>

    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
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
  },
  existingImages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'delete-existing'])

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

const totalCount = computed(() => props.existingImages.length + files.value.length)

const fileInputRef = ref(null)

const openPicker = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

const handleFileSelect = e => {
  if (e.target.files && e.target.files.length > 0) {
    const available = props.maxFiles - totalCount.value
    if (available <= 0) return
    try {
      addFiles(Array.from(e.target.files).slice(0, available))
    } catch (err) {
      console.error('File upload error:', err)
    }
  }
  e.target.value = ''
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
