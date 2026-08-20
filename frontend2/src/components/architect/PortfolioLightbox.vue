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
        class="fixed inset-0 z-50 flex flex-col bg-black"
        tabindex="-1"
        @keydown.esc="$emit('close')"
      >
        <!-- Top bar -->
        <div class="flex items-center justify-between px-6 py-4 bg-black/80 backdrop-blur-sm shrink-0">
          <div class="min-w-0">
            <h2 class="text-white font-bold text-lg truncate">{{ portfolio.title }}</h2>
            <div class="flex items-center gap-3 text-white/50 text-sm mt-0.5 flex-wrap">
              <span v-if="portfolio.projectType">{{ portfolio.projectType }}</span>
              <span v-if="portfolio.projectType && projectYear">·</span>
              <span v-if="projectYear">{{ projectYear }}</span>
              <span v-if="portfolio.location" class="flex items-center gap-1">
                <span v-if="projectYear">·</span>
                <MapPin :size="13" />
                {{ portfolio.location }}
              </span>
            </div>
          </div>
          <button
            class="ml-4 shrink-0 w-9 h-9 rounded-full bg-white/10 hover:bg-white/20 transition flex items-center justify-center text-white"
            @click="$emit('close')"
          >
            <X :size="18" />
          </button>
        </div>

        <!-- Image area -->
        <div class="flex-1 relative overflow-hidden flex items-center justify-center bg-black min-h-0">
          <!-- Backdrop click to close -->
          <div class="absolute inset-0" @click="$emit('close')" />

          <Transition :name="slideDirection" mode="out-in">
            <img
              v-if="currentImage"
              :key="currentIndex"
              :src="currentImage.largeUrl || currentImage.mediumUrl || currentImage.originalUrl"
              :alt="`${portfolio.title} - image ${currentIndex + 1}`"
              class="relative z-10 max-h-full max-w-full object-contain select-none"
              @click.stop
            />
            <div v-else class="relative z-10 w-full h-full flex items-center justify-center text-white/20">
              <ImageIcon :size="80" />
            </div>
          </Transition>

          <!-- Prev/Next -->
          <button
            v-if="images.length > 1"
            class="absolute left-4 z-20 w-11 h-11 rounded-full bg-black/50 hover:bg-black/80 transition flex items-center justify-center text-white"
            @click.stop="prev"
          >
            <ChevronLeft :size="22" />
          </button>
          <button
            v-if="images.length > 1"
            class="absolute right-4 z-20 w-11 h-11 rounded-full bg-black/50 hover:bg-black/80 transition flex items-center justify-center text-white"
            @click.stop="next"
          >
            <ChevronRight :size="22" />
          </button>

          <!-- Counter -->
          <div
            v-if="images.length > 1"
            class="absolute bottom-4 left-1/2 -translate-x-1/2 z-20 bg-black/60 text-white/80 text-xs px-3 py-1.5 rounded-full"
          >
            {{ currentIndex + 1 }} / {{ images.length }}
          </div>
        </div>

        <!-- Thumbnail strip -->
        <div v-if="images.length > 1" class="shrink-0 bg-black/80 px-4 py-3 flex gap-2 overflow-x-auto">
          <button
            v-for="(img, i) in images"
            :key="i"
            class="shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 transition"
            :class="i === currentIndex ? 'border-white' : 'border-transparent opacity-50 hover:opacity-80'"
            @click="goTo(i)"
          >
            <img :src="img.mediumUrl || img.originalUrl" :alt="`thumb ${i + 1}`" class="w-full h-full object-cover" />
          </button>
        </div>

        <!-- Info panel -->
        <div v-if="portfolio.description" class="shrink-0 bg-black/70 px-6 py-4 max-h-32 overflow-y-auto">
          <p class="text-white/70 text-sm leading-relaxed">{{ portfolio.description }}</p>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { X, ChevronLeft, ChevronRight, MapPin, ImageIcon } from 'lucide-vue-next'

const props = defineProps({
  portfolio: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])

const currentIndex = ref(0)
const slideDirection = ref('slide-right')

const images = computed(() => props.portfolio?.images || [])
const currentImage = computed(() => images.value[currentIndex.value] || null)

const projectYear = computed(() => {
  if (props.portfolio?.projectDate) {
    return new Date(props.portfolio.projectDate).getFullYear()
  }
  return null
})

watch(
  () => props.portfolio,
  () => {
    currentIndex.value = 0
  }
)

const prev = () => {
  slideDirection.value = 'slide-right'
  currentIndex.value = currentIndex.value === 0 ? images.value.length - 1 : currentIndex.value - 1
}

const next = () => {
  slideDirection.value = 'slide-left'
  currentIndex.value = currentIndex.value === images.value.length - 1 ? 0 : currentIndex.value + 1
}

const goTo = i => {
  slideDirection.value = i > currentIndex.value ? 'slide-left' : 'slide-right'
  currentIndex.value = i
}

const handleKey = e => {
  if (!props.portfolio) return
  if (e.key === 'Escape') emit('close')
  if (e.key === 'ArrowLeft') prev()
  if (e.key === 'ArrowRight') next()
}

onMounted(() => document.addEventListener('keydown', handleKey))
onUnmounted(() => document.removeEventListener('keydown', handleKey))
</script>

<style scoped>
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.2s ease;
}

.slide-left-enter-from {
  transform: translateX(30px);
  opacity: 0;
}
.slide-left-leave-to {
  transform: translateX(-30px);
  opacity: 0;
}
.slide-right-enter-from {
  transform: translateX(-30px);
  opacity: 0;
}
.slide-right-leave-to {
  transform: translateX(30px);
  opacity: 0;
}
</style>
