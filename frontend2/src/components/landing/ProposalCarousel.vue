<template>
  <div
    class="relative w-full max-w-[416px] mx-auto"
    style="height: 600px"
    role="region"
    aria-roledescription="carousel"
    aria-label="Proposal arsitek"
    @mouseenter="pauseTimer"
    @mouseleave="resumeTimer"
  >
    <div class="relative" style="height: 520px">
      <div
        v-for="(slide, i) in slides"
        :key="slide.id ?? i"
        class="absolute inset-0 flex items-center justify-center"
        :style="slideStyle(i)"
      >
        <div class="bg-ink-900 rounded-[26px] p-3 w-full h-full">
          <div class="relative bg-white rounded-[14px] w-full h-full overflow-hidden flex items-center justify-center">
            <img
              v-if="slide.imageUrl"
              :src="slide.imageUrl"
              :alt="slide.architectName"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center bg-surface-muted">
              <ImageIcon class="w-10 h-10 text-hairline-alt" stroke-width="1.25" />
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="slides.length === 0"
        class="absolute inset-0 bg-ink-900 rounded-[26px] p-3"
        :class="{ 'animate-pulse': loading }"
      >
        <div class="bg-surface-muted rounded-[14px] w-full h-full flex items-center justify-center">
          <ImageIcon class="w-10 h-10 text-hairline-alt" stroke-width="1.25" />
        </div>
      </div>
    </div>

    <Transition
      enter-active-class="transition-all duration-500 [transition-timing-function:cubic-bezier(0.34,1.56,0.64,1)]"
      enter-from-class="opacity-0 scale-90 translate-y-3"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition-opacity duration-150"
      leave-to-class="opacity-0"
    >
      <div
        v-if="showCard && active"
        class="absolute left-1/2 -translate-x-1/2 bg-white rounded-2xl shadow-popover px-4 py-3 flex items-start gap-3 max-w-[340px]"
        style="bottom: 24px; z-index: 20"
      >
        <div
          class="w-10 h-10 rounded-full bg-brand-tan text-brand-brown flex items-center justify-center font-bold text-body flex-shrink-0"
        >
          {{ active.avatarInitial || active.architectName?.charAt(0)?.toUpperCase() }}
        </div>
        <div class="min-w-0">
          <div class="flex items-center gap-1.5">
            <span class="text-caption-sm font-bold text-ink-900 truncate">{{ active.architectName }}</span>
            <BadgeCheck v-if="active.verified" class="w-3.5 h-3.5 text-brand-green flex-shrink-0" />
          </div>

          <div v-if="active.rating" class="flex items-center gap-1 mt-1">
            <Star
              v-for="s in 5"
              :key="s"
              class="w-3 h-3 flex-shrink-0"
              :class="s <= Math.round(Number(active.rating)) ? 'text-brand-gold fill-brand-gold' : 'text-hairline-alt'"
            />
            <span class="text-micro-sm font-semibold text-ink-500 ml-0.5">{{ Number(active.rating).toFixed(1) }}</span>
          </div>

          <p v-if="active.reviewQuote" class="text-micro-sm text-ink-400 italic leading-snug mt-1.5">
            “{{ active.reviewQuote }}”
          </p>
          <p v-if="active.reviewerName" class="text-micro-sm text-ink-300 mt-1">— {{ active.reviewerName }}</p>
        </div>
      </div>
    </Transition>

    <template v-if="slides.length > 1">
      <button
        type="button"
        aria-label="Proposal sebelumnya"
        class="absolute w-9 h-9 rounded-full bg-white border border-ink-200 shadow-soft flex items-center justify-center cursor-pointer hover:bg-surface-muted transition-colors"
        style="left: -4px; top: 260px; z-index: 25"
        @click="goTo(activeIndex - 1)"
      >
        <ChevronLeft class="w-[18px] h-[18px] text-ink-900" />
      </button>
      <button
        type="button"
        aria-label="Proposal berikutnya"
        class="absolute w-9 h-9 rounded-full bg-white border border-ink-200 shadow-soft flex items-center justify-center cursor-pointer hover:bg-surface-muted transition-colors"
        style="right: -4px; top: 260px; z-index: 25"
        @click="goTo(activeIndex + 1)"
      >
        <ChevronRight class="w-4.5 h-4.5 text-ink-900" />
      </button>

      <div class="absolute left-1/2 -translate-x-1/2 flex gap-2" style="bottom: -12px; z-index: 25">
        <button
          v-for="(slide, i) in slides"
          :key="slide.id ?? i"
          type="button"
          :aria-label="`Ke proposal ${i + 1}`"
          :aria-current="i === activeIndex"
          class="rounded-full transition-all duration-300 cursor-pointer border-none p-0 bg-ink-900"
          :style="{ width: i === activeIndex ? '18px' : '8px', height: '8px', opacity: i === activeIndex ? 1 : 0.25 }"
          @click="goTo(i)"
        />
      </div>
    </template>

    <div v-if="active" aria-live="polite" class="sr-only">
      Menampilkan proposal {{ activeIndex + 1 }} dari {{ slides.length }}: {{ active.architectName }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ChevronLeft, ChevronRight, BadgeCheck, Star, Image as ImageIcon } from 'lucide-vue-next'

const props = defineProps({
  slides: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

const activeIndex = ref(0)
const active = computed(() => props.slides[activeIndex.value] ?? null)

const showCard = ref(false)

let cardTimer = null
let autoplayTimer = null
let isPaused = false

function slideStyle(i) {
  const len = props.slides.length
  let diff = i - activeIndex.value
  if (diff > 1) diff -= len
  if (diff < -1) diff += len

  const base = {
    transition:
      'transform 800ms cubic-bezier(0.22,1,0.36,1), opacity 800ms cubic-bezier(0.22,1,0.36,1), filter 800ms cubic-bezier(0.22,1,0.36,1)'
  }

  if (diff === 0) {
    return { ...base, transform: 'translateX(0) scale(1) rotate(0deg)', opacity: 1, filter: 'blur(0px)', zIndex: 3 }
  }
  if (diff === -1) {
    return {
      ...base,
      transform: 'translateX(-55%) scale(0.82) rotate(-6deg)',
      opacity: 0.55,
      filter: 'blur(2px)',
      zIndex: 1
    }
  }
  return {
    ...base,
    transform: 'translateX(55%) scale(0.82) rotate(6deg)',
    opacity: 0.55,
    filter: 'blur(2px)',
    zIndex: 1
  }
}

function triggerCard() {
  showCard.value = false
  clearTimeout(cardTimer)
  cardTimer = setTimeout(() => (showCard.value = true), 300)
}

function goTo(index) {
  const len = props.slides.length
  if (len === 0) return
  activeIndex.value = ((index % len) + len) % len
  restartTimer()
}

function startTimer() {
  const reducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  if (reducedMotion) return
  autoplayTimer = setInterval(() => {
    if (!isPaused && props.slides.length > 1) {
      activeIndex.value = (activeIndex.value + 1) % props.slides.length
    }
  }, 4500)
}

function restartTimer() {
  clearInterval(autoplayTimer)
  startTimer()
}

function pauseTimer() {
  isPaused = true
}

function resumeTimer() {
  isPaused = false
}

watch(activeIndex, triggerCard)

// Editing a slide in the superuser editor swaps the array; keep the index in range
// and re-run the card animation so the change is visible immediately.
watch(
  () => props.slides,
  slides => {
    if (activeIndex.value >= slides.length) activeIndex.value = Math.max(0, slides.length - 1)
    triggerCard()
  }
)

onMounted(() => {
  triggerCard()
  startTimer()
})

onUnmounted(() => {
  clearInterval(autoplayTimer)
  clearTimeout(cardTimer)
})
</script>
