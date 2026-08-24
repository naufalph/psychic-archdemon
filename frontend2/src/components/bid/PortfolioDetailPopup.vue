<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="portfolio"
        class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4 overflow-y-auto"
        @click.self="$emit('close')"
      >
        <div class="w-full max-w-2xl bg-white rounded-3xl shadow-2xl overflow-hidden my-8">
          <div class="relative aspect-video bg-gray-100">
            <img
              v-if="activeImage"
              :src="activeImage.largeUrl || activeImage.mediumUrl || activeImage.originalUrl"
              :alt="portfolio.title"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
              <ImageIcon :size="64" />
            </div>

            <button
              type="button"
              class="absolute top-4 right-4 w-9 h-9 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center text-white transition"
              @click="$emit('close')"
            >
              <X :size="18" />
            </button>

            <template v-if="images.length > 1">
              <button
                type="button"
                class="absolute left-3 top-1/2 -translate-y-1/2 w-9 h-9 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center text-white transition"
                @click="prevImage"
              >
                <ChevronLeft :size="18" />
              </button>
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 w-9 h-9 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center text-white transition"
                @click="nextImage"
              >
                <ChevronRight :size="18" />
              </button>
              <div
                class="absolute bottom-3 left-1/2 -translate-x-1/2 bg-black/60 text-white/80 text-xs px-3 py-1 rounded-full"
              >
                {{ activeIndex + 1 }} / {{ images.length }}
              </div>
            </template>
          </div>

          <div v-if="images.length > 1" class="flex gap-2 p-3 border-b border-gray-100 overflow-x-auto">
            <button
              v-for="(img, i) in images"
              :key="i"
              type="button"
              class="shrink-0 w-14 h-14 rounded-lg overflow-hidden border-2 transition"
              :class="i === activeIndex ? 'border-brand-brown' : 'border-transparent opacity-60 hover:opacity-100'"
              @click="activeIndex = i"
            >
              <img :src="img.mediumUrl || img.originalUrl" :alt="`thumb ${i + 1}`" class="w-full h-full object-cover" />
            </button>
          </div>

          <div class="p-6 space-y-5">
            <div class="flex items-start justify-between gap-4">
              <h2 class="text-xl font-bold text-black">{{ portfolio.title }}</h2>
              <span
                :class="[
                  'shrink-0 text-xs font-bold px-3 py-1 rounded-full',
                  portfolio.isBuilt ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'
                ]"
              >
                {{ portfolio.isBuilt ? t.portfolioPopup.built : t.portfolioPopup.concept }}
              </span>
            </div>

            <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
              <div v-if="portfolio.projectType" class="bg-gray-50 rounded-2xl p-3">
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.portfolio.form.projectType }}</p>
                <p class="text-sm font-semibold text-gray-900">{{ portfolio.projectType }}</p>
              </div>
              <div v-if="portfolio.location" class="bg-gray-50 rounded-2xl p-3">
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.portfolio.form.location }}</p>
                <p class="text-sm font-semibold text-gray-900">{{ portfolio.location }}</p>
              </div>
              <div v-if="portfolio.projectDate" class="bg-gray-50 rounded-2xl p-3">
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.portfolio.form.projectDate }}</p>
                <p class="text-sm font-semibold text-gray-900">{{ formattedDate }}</p>
              </div>
            </div>

            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-1.5">{{ t.portfolio.form.description }}</p>
              <p v-if="portfolio.description" class="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
                {{ portfolio.description }}
              </p>
              <p v-else class="text-sm text-gray-400 italic">{{ t.portfolioPopup.noDescription }}</p>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { X, ChevronLeft, ChevronRight, ImageIcon } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'

const props = defineProps({
  portfolio: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])

const { t } = useI18n()

const activeIndex = ref(0)

const images = computed(() => props.portfolio?.images || [])
const activeImage = computed(() => images.value[activeIndex.value] || null)

const formattedDate = computed(() => {
  if (!props.portfolio?.projectDate) return ''
  return new Date(props.portfolio.projectDate).toLocaleDateString('en-US', { year: 'numeric', month: 'long' })
})

watch(
  () => props.portfolio,
  () => {
    activeIndex.value = 0
  }
)

const prevImage = () => {
  activeIndex.value = activeIndex.value === 0 ? images.value.length - 1 : activeIndex.value - 1
}

const nextImage = () => {
  activeIndex.value = activeIndex.value === images.value.length - 1 ? 0 : activeIndex.value + 1
}

const handleKeydown = e => {
  if (!props.portfolio) return
  if (e.key === 'Escape') emit('close')
  if (e.key === 'ArrowLeft') prevImage()
  if (e.key === 'ArrowRight') nextImage()
}

onMounted(() => document.addEventListener('keydown', handleKeydown))
onUnmounted(() => document.removeEventListener('keydown', handleKeydown))
</script>
