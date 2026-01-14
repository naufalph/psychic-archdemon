import { ref, onMounted, onUnmounted } from 'vue'

export function useImageGallery(images = []) {
  const currentIndex = ref(0)
  const showLightbox = ref(false)

  const currentImage = ref(null)

  const open = (index = 0) => {
    if (images.length === 0) return
    currentIndex.value = index
    currentImage.value = images[index]
    showLightbox.value = true
  }

  const close = () => {
    showLightbox.value = false
    currentImage.value = null
  }

  const next = () => {
    if (images.length === 0) return
    currentIndex.value = (currentIndex.value + 1) % images.length
    currentImage.value = images[currentIndex.value]
  }

  const previous = () => {
    if (images.length === 0) return
    currentIndex.value = (currentIndex.value - 1 + images.length) % images.length
    currentImage.value = images[currentIndex.value]
  }

  const handleKeydown = e => {
    if (!showLightbox.value) return

    switch (e.key) {
      case 'Escape':
        close()
        break
      case 'ArrowRight':
        next()
        break
      case 'ArrowLeft':
        previous()
        break
    }
  }

  onMounted(() => {
    document.addEventListener('keydown', handleKeydown)
  })

  onUnmounted(() => {
    document.removeEventListener('keydown', handleKeydown)
  })

  return {
    currentIndex,
    currentImage,
    showLightbox,
    open,
    close,
    next,
    previous
  }
}
