import { ref } from 'vue'
import { validateFiles, getFilePreviewUrl, revokeFilePreviewUrl } from '@/services/uploadService'

export function useFileUpload(options = {}) {
  const { maxFiles = 10, multiple = false } = options

  const files = ref([])
  const previews = ref([])
  const isDragging = ref(false)
  const error = ref(null)

  const addFiles = newFiles => {
    error.value = null
    try {
      const fileArray = Array.isArray(newFiles) ? newFiles : Array.from(newFiles)

      if (!multiple && fileArray.length > 1) {
        throw new Error('Only one file is allowed')
      }

      const filesToAdd = multiple
        ? fileArray.slice(0, maxFiles - files.value.length)
        : fileArray.slice(0, 1)

      validateFiles(filesToAdd, options)

      filesToAdd.forEach(file => {
        files.value.push(file)
        previews.value.push(getFilePreviewUrl(file))
      })
    } catch (err) {
      error.value = err.message
      throw err
    }
  }

  const removeFile = index => {
    if (previews.value[index]) {
      revokeFilePreviewUrl(previews.value[index])
    }
    files.value.splice(index, 1)
    previews.value.splice(index, 1)
  }

  const clear = () => {
    previews.value.forEach(url => revokeFilePreviewUrl(url))
    files.value = []
    previews.value = []
    error.value = null
  }

  const handleDrop = e => {
    e.preventDefault()
    isDragging.value = false
    if (e.dataTransfer.files) {
      addFiles(e.dataTransfer.files)
    }
  }

  const handleDragOver = e => {
    e.preventDefault()
    isDragging.value = true
  }

  const handleDragLeave = e => {
    e.preventDefault()
    isDragging.value = false
  }

  return {
    files,
    previews,
    isDragging,
    error,
    addFiles,
    removeFile,
    clear,
    handleDrop,
    handleDragOver,
    handleDragLeave
  }
}
