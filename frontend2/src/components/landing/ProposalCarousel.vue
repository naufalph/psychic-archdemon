<template>
  <div
    class="relative w-full max-w-[560px] mx-auto"
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
        :key="slide.name"
        class="absolute inset-0 flex items-center justify-center"
        :style="slideStyle(i)"
      >
        <div class="bg-ink-900 rounded-[26px] p-3 w-full h-full">
          <div class="relative bg-white rounded-[14px] w-full h-full overflow-hidden flex items-center justify-center">
            <div
              class="w-full h-full flex items-center justify-center"
              :style="{ background: `linear-gradient(135deg, ${slide.accentFrom}, ${slide.accentTo})` }"
            >
              <component :is="slide.icon" class="w-16 h-16 text-white/90" stroke-width="1.25" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pop-up: architect avatar + name + badge, with rating/price/duration stats row -->
    <Transition
      enter-active-class="transition-all duration-500 [transition-timing-function:cubic-bezier(0.34,1.56,0.64,1)]"
      enter-from-class="opacity-0 scale-90 translate-y-3"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition-opacity duration-150"
      leave-to-class="opacity-0"
    >
      <div
        v-if="showCard"
        class="absolute left-1/2 -translate-x-1/2 bg-white rounded-2xl shadow-popover px-4 py-3 flex flex-col gap-1.5 animate-float"
        style="bottom: 24px; z-index: 20"
      >
        <div class="flex items-center gap-3">
          <div
            class="w-10 h-10 rounded-full bg-brand-tan text-brand-brown flex items-center justify-center font-bold text-body flex-shrink-0"
          >
            {{ active.initial }}
          </div>
          <div class="flex items-center gap-1.5">
            <span class="text-caption-sm font-bold text-ink-900 whitespace-nowrap">{{ active.name }}</span>
            <BadgeCheck class="w-3.5 h-3.5 text-brand-green flex-shrink-0" />
          </div>
        </div>
        <div class="flex items-center gap-1.5 text-micro text-ink-400 whitespace-nowrap" style="padding-left: 52px">
          <span class="text-brand-yellow">★</span>
          <span class="font-semibold text-ink-900">{{ active.rating }}</span>
          <span>·</span>
          <span>{{ active.price }}</span>
          <span>·</span>
          <span>{{ active.days }} hari</span>
        </div>
      </div>
    </Transition>

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
        :key="slide.name"
        type="button"
        :aria-label="`Ke proposal ${i + 1}`"
        :aria-current="i === activeIndex"
        class="rounded-full transition-all duration-300 cursor-pointer border-none p-0 bg-ink-900"
        :style="{ width: i === activeIndex ? '18px' : '8px', height: '8px', opacity: i === activeIndex ? 1 : 0.25 }"
        @click="goTo(i)"
      />
    </div>

    <div aria-live="polite" class="sr-only">
      {{ active.name }} — {{ active.price }}, rating {{ active.rating }}, {{ active.days }} hari pengerjaan
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ChevronLeft, ChevronRight, BadgeCheck, Home, Building2, Warehouse } from 'lucide-vue-next'

// accentFrom/accentTo feed an inline linear-gradient style, so they can't reference Tailwind
// classes or theme() — keep these hex values in sync with tailwind.config.js by hand.
const slides = [
  {
    name: 'Budi Santoso',
    initial: 'B',
    rating: '4.9',
    days: 30,
    price: 'Rp 22jt',
    icon: Home,
    accentFrom: '#7C4728',
    accentTo: '#C5A17A'
  },
  {
    name: 'Ani Rahayu',
    initial: 'A',
    rating: '4.7',
    days: 24,
    price: 'Rp 25jt',
    icon: Building2,
    accentFrom: '#185C93',
    accentTo: '#2F7DC0'
  },
  {
    name: 'Dimas P.',
    initial: 'D',
    rating: '4.6',
    days: 18,
    price: 'Rp 20jt',
    icon: Warehouse,
    accentFrom: '#1A7A2E',
    accentTo: '#10B981'
  }
]

const activeIndex = ref(0)
const active = computed(() => slides[activeIndex.value])

const showCard = ref(false)

let cardTimer = null
let autoplayTimer = null
let isPaused = false

function slideStyle(i) {
  let diff = i - activeIndex.value
  if (diff > 1) diff -= slides.length
  if (diff < -1) diff += slides.length

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
  const len = slides.length
  activeIndex.value = ((index % len) + len) % len
  restartTimer()
}

function startTimer() {
  const reducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  if (reducedMotion) return
  autoplayTimer = setInterval(() => {
    if (!isPaused) activeIndex.value = (activeIndex.value + 1) % slides.length
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

onMounted(() => {
  triggerCard()
  startTimer()
})

onUnmounted(() => {
  clearInterval(autoplayTimer)
  clearTimeout(cardTimer)
})
</script>
