const MAX_FILE_SIZE = 5 * 1024 * 1024
const VALID_IMAGE_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
const VALID_PDF_TYPES = ['application/pdf']

export const validateFile = (file, options = {}) => {
  const { maxSize = MAX_FILE_SIZE, allowedTypes = VALID_IMAGE_TYPES } = options

  if (!file) {
    throw new Error('No file provided')
  }

  if (!allowedTypes.includes(file.type)) {
    const typesString = allowedTypes
      .map(type => type.split('/')[1].toUpperCase())
      .join(', ')
    throw new Error(`Invalid file type. Allowed types: ${typesString}`)
  }

  if (file.size > maxSize) {
    const sizeMB = (maxSize / (1024 * 1024)).toFixed(0)
    throw new Error(`File size exceeds ${sizeMB}MB limit`)
  }

  return true
}

export const validateFiles = (files, options = {}) => {
  if (!files || files.length === 0) {
    throw new Error('No files provided')
  }

  files.forEach((file, index) => {
    try {
      validateFile(file, options)
    } catch (error) {
      throw new Error(`File ${index + 1}: ${error.message}`)
    }
  })

  return true
}

export const createFormDataWithFiles = (files, fieldName = 'files') => {
  const formData = new FormData()

  if (Array.isArray(files)) {
    files.forEach(file => {
      formData.append(fieldName, file)
    })
  } else {
    formData.append(fieldName, files)
  }

  return formData
}

export const createFormDataWithJSON = (data, files = null, filesFieldName = 'files') => {
  const formData = new FormData()

  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))

  if (files) {
    if (Array.isArray(files)) {
      files.forEach(file => {
        formData.append(filesFieldName, file)
      })
    } else {
      formData.append(filesFieldName, files)
    }
  }

  return formData
}

export const getFilePreviewUrl = file => {
  if (!file) return null
  return URL.createObjectURL(file)
}

export const revokeFilePreviewUrl = url => {
  if (url) {
    URL.revokeObjectURL(url)
  }
}
