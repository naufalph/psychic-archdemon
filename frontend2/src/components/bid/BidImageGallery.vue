<template>
  <div v-if="images && images.length > 0">
    <h3 class="text-lg font-bold text-black mb-4">{{ title }}</h3>
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div
        v-for="(image, index) in images"
        :key="index"
        @click="openLightbox(index)"
        class="relative aspect-square bg-gray-100 rounded-2xl overflow-hidden cursor-pointer hover:shadow-lg transition group"
      >
        <img
          :src="image.imageUrl"
          :alt="image.fileName || `Image ${index + 1}`"
          class="w-full h-full object-cover group-hover:scale-105 transition"
        />
        <div class="absolute bottom-0 left-0 right-0 bg-black/50 text-white text-xs px-2 py-1 truncate">
          {{ image.fileName || `Image ${index + 1}` }}
        </div>
      </div>
    </div>

    <TransitionRoot :show="isLightboxOpen" as="template">
      <Dialog @close="closeLightbox" class="relative z-50">
        <TransitionChild
          enter="ease-out duration-300"
          enter-from="opacity-0"
          enter-to="opacity-100"
          leave="ease-in duration-200"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <div class="fixed inset-0 bg-black/90" />
        </TransitionChild>

        <div class="fixed inset-0 overflow-y-auto">
          <div class="flex min-h-full items-center justify-center p-4">
            <TransitionChild
              enter="ease-out duration-300"
              enter-from="opacity-0 scale-95"
              enter-to="opacity-100 scale-100"
              leave="ease-in duration-200"
              leave-from="opacity-100 scale-100"
              leave-to="opacity-0 scale-95"
            >
              <DialogPanel class="w-full max-w-5xl">
                <div class="relative">
                  <button
                    @click="closeLightbox"
                    class="absolute top-4 right-4 bg-black/50 hover:bg-black/70 text-white rounded-full p-2 transition"
                  >
                    <X :size="24" />
                  </button>

                  <img
                    :src="images[currentImageIndex]?.imageUrl"
                    :alt="images[currentImageIndex]?.fileName"
                    class="w-full h-auto rounded-2xl"
                  />

                  <div v-if="images.length > 1" class="flex justify-between mt-4">
                    <button
                      @click="previousImage"
                      :disabled="currentImageIndex === 0"
                      class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-full disabled:opacity-30"
                    >
                      ← Previous
                    </button>
                    <p class="text-white text-sm self-center">{{ currentImageIndex + 1 }} / {{ images.length }}</p>
                    <button
                      @click="nextImage"
                      :disabled="currentImageIndex === images.length - 1"
                      class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-full disabled:opacity-30"
                    >
                      Next →
                    </button>
                  </div>
                </div>
              </DialogPanel>
            </TransitionChild>
          </div>
        </div>
      </Dialog>
    </TransitionRoot>
  </div>

  <div v-else class="text-center py-12 bg-gray-50 rounded-2xl">
    <p class="text-gray-500">{{ emptyMessage }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Dialog, DialogPanel, TransitionRoot, TransitionChild } from '@headlessui/vue'
import { X } from 'lucide-vue-next'

defineProps({
  images: { type: Array, default: () => [] },
  title: { type: String, required: true },
  emptyMessage: { type: String, default: 'No images available' }
})

const isLightboxOpen = ref(false)
const currentImageIndex = ref(0)

const openLightbox = index => {
  currentImageIndex.value = index
  isLightboxOpen.value = true
}

const closeLightbox = () => {
  isLightboxOpen.value = false
}

const nextImage = () => {
  if (currentImageIndex.value < images.length - 1) {
    currentImageIndex.value++
  }
}

const previousImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}
</script>
