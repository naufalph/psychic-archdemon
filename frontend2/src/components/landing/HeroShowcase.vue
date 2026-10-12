<template>
  <div class="w-full flex flex-wrap gap-4 items-stretch min-h-[360px] md:min-h-[404px]">
    <!-- Project summary card -->
    <div
      class="flex-1 basis-[300px] min-w-0 grid grid-rows-[auto_78px_1fr_104px] rounded-[20px] border border-black/[.08] bg-white p-8 shadow-[0_24px_60px_-48px_rgba(0,0,0,.35)]"
    >
      <div class="justify-self-start">
        <button
          ref="projectPill"
          class="inline-flex items-center gap-2 h-[34px] pl-3.5 pr-1.5 rounded-full border border-ink-900/[.14] text-white text-caption font-bold hover:shadow-[inset_0_0_0_999px_rgba(0,0,0,.06)] transition-shadow"
          :style="projectPillStyle"
          @click="nextProject"
        >
          <span>{{ t.landing.v2.hero.yourProject }}</span>
          <span class="inline-flex items-center justify-center w-[22px] h-[22px] rounded-full bg-white flex-shrink-0">
            <ChevronRight :size="12" :stroke-width="3" class="text-ink-900" />
          </span>
        </button>
      </div>

      <h3 class="text-[30px] font-semibold tracking-[-.025em] leading-[1.12] m-0 text-ink-900 self-start pt-3.5">
        {{ activeProject.title }}
      </h3>

      <div class="relative my-4 rounded-[14px] overflow-hidden">
        <div class="absolute inset-0" :style="projectImageStyle"></div>
      </div>

      <div class="self-start border-t border-black/10">
        <div class="flex gap-8 pt-[18px]">
          <div>
            <p class="text-caption text-ink-500 mb-1">Budget total</p>
            <p class="text-[28px] font-semibold tracking-[-.025em] m-0 text-ink-900">{{ activeProject.total }}</p>
          </div>
          <div class="pl-8 border-l border-black/10">
            <p class="text-caption text-ink-500 mb-1">Budget desain</p>
            <p class="text-[28px] font-semibold tracking-[-.025em] m-0 text-ink-900">{{ activeProject.desain }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Offer card -->
    <div
      class="relative flex-[2.8] basis-[520px] min-w-0 flex flex-wrap gap-8 items-stretch rounded-[20px] border border-black/[.08] bg-white p-8 overflow-hidden shadow-[0_24px_60px_-48px_rgba(0,0,0,.35)]"
      @mouseenter="paused = true"
      @mouseleave="paused = false"
    >
      <!-- Outside the flipping page so the hole in the card stays put while the offer turns over -->
      <button
        ref="offerPill"
        class="absolute top-8 left-8 z-10 inline-flex items-center gap-2 h-[34px] pl-3.5 pr-1.5 rounded-full border border-ink-900/[.14] text-white text-caption font-bold hover:shadow-[inset_0_0_0_999px_rgba(0,0,0,.06)] transition-shadow"
        :style="offerPillStyle"
        @click="nextOffer"
      >
        <span>{{ t.landing.v2.hero.offerPrefix }}{{ heroOffer + 1 }}</span>
        <span class="inline-flex items-center justify-center w-[22px] h-[22px] rounded-full bg-white flex-shrink-0">
          <ChevronRight :size="12" :stroke-width="3" class="text-ink-900" />
        </span>
      </button>

      <div
        aria-hidden="true"
        class="absolute -top-[40%] left-0 w-[45%] h-[180%] pointer-events-none mix-blend-multiply hero-sheen"
      ></div>

      <div
        :key="'offer-' + heroProj + '-' + heroOffer"
        class="flex-1 basis-[260px] min-w-0 grid grid-rows-[auto_78px_auto_auto_1fr_104px] hero-flip"
      >
        <div class="h-[34px]" aria-hidden="true"></div>

        <div class="flex items-baseline justify-between gap-4 self-start pt-3.5">
          <h3 class="text-[30px] font-semibold tracking-[-.025em] leading-[1.12] m-0 text-ink-900">
            {{ activeOffer.name }}&nbsp;<span class="text-[20px] tracking-[-.4px] text-ink-900">{{
              activeOffer.rating
            }}</span>
          </h3>
        </div>

        <div class="flex items-center gap-2 flex-wrap pt-2.5">
          <span
            v-for="chip in activeOffer.chips"
            :key="chip"
            class="inline-flex items-center h-[26px] px-2.5 rounded-full border border-black/[.14] text-micro font-semibold tracking-[.02em] text-ink-500"
          >
            {{ chip }}
          </span>
        </div>

        <span></span>
        <span></span>

        <div class="flex gap-8 items-start self-start pt-[18px] border-t border-black/10">
          <div>
            <p class="text-caption text-ink-500 mb-1">Biaya desain</p>
            <p class="text-[28px] font-semibold tracking-[-.025em] m-0 text-ink-900 whitespace-nowrap">
              {{ activeOffer.biaya }}
            </p>
          </div>
          <div class="flex flex-col gap-2.5 pl-8 border-l border-black/10">
            <div class="flex items-baseline gap-2.5">
              <span class="text-caption text-ink-500">Durasi</span>
              <span class="text-[18px] font-semibold tracking-[-.015em] text-ink-900 whitespace-nowrap">{{
                activeOffer.durasi
              }}</span>
            </div>
            <div class="flex items-baseline gap-2.5">
              <span class="text-caption text-ink-500">Revisi</span>
              <span class="text-[18px] font-semibold tracking-[-.015em] text-ink-900">{{ activeOffer.revisi }}</span>
            </div>
          </div>
        </div>
      </div>

      <div
        :key="'img-' + heroProj + '-' + heroOffer"
        class="flex-[1.8] basis-[340px] min-w-0 relative min-h-[260px] rounded-2xl overflow-hidden hero-flip-img hero-float"
      >
        <div class="absolute inset-0 flex items-center justify-center text-center px-4" :style="offerImageStyle">
          <span v-if="!activeOffer.image" class="text-caption-sm font-semibold text-white/80">Eksplorasi massa</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import heroProject1 from '@/assets/images/landing/hero-project-1.jpg'
import heroProject2 from '@/assets/images/landing/hero-project-2.jpg'
import heroOfferKosan1 from '@/assets/images/landing/hero-offer-kosan-1.jpg'
import heroOfferKosan2 from '@/assets/images/landing/hero-offer-kosan-2.jpg'

const { t } = useI18n()

const props = defineProps({
  // The hero section's background image; the pills show the slice of it behind them so they read as holes in the card
  backdrop: { type: String, default: '' }
})

const HERO_PROJECTS = [
  {
    title: 'Kosan Premium 12 Kamar',
    total: 'Rp1,8 M',
    desain: 'Rp144 Jt',
    offers: [
      {
        name: 'Aryadiza Gunawan',
        image: heroOfferKosan1,
        rating: '★ 4,9',
        chips: ['Arsitek freelance', '8 tahun pengalaman', 'Anggota IAI', 'Arsitektur ITB'],
        biaya: 'Rp105 Jt',
        durasi: '6 minggu',
        revisi: '3 kali'
      },
      {
        name: 'RR Studio',
        image: heroOfferKosan2,
        rating: '',
        chips: ['Firma arsitektur', '20 tahun pengalaman', 'IAI certified'],
        biaya: 'Rp160 Jt',
        durasi: '8 minggu',
        revisi: 'Unlimited'
      }
    ]
  },
  {
    title: 'Cafe at Canggu',
    total: 'Rp850 Jt',
    desain: 'Rp68 Jt',
    offers: [
      {
        name: 'Made Wiratama',
        rating: '★ 4,8',
        chips: ['Arsitek freelance', '6 tahun pengalaman', 'Anggota IAI', 'Arsitektur Udayana'],
        biaya: 'Rp58 Jt',
        durasi: '5 minggu',
        revisi: '3 kali'
      },
      {
        name: 'Studio Ruang Tropis',
        rating: '',
        chips: ['Firma arsitektur', '12 tahun pengalaman', 'Spesialis F&B'],
        biaya: 'Rp72 Jt',
        durasi: '7 minggu',
        revisi: 'Unlimited'
      }
    ]
  }
]

const heroProj = ref(0)
const heroOffer = ref(0)
const paused = ref(false)
let timer = null

const activeProject = computed(() => HERO_PROJECTS[heroProj.value])
const activeOffer = computed(() => activeProject.value.offers[heroOffer.value])

const PROJECT_IMAGES = [heroProject1, heroProject2]
const OFFER_GRADIENTS = ['linear-gradient(135deg,#C5A17A,#7C4728)', 'linear-gradient(135deg,#2F7DC0,#185C93)']

const projectImageStyle = computed(() => ({
  background: `center / cover no-repeat url(${PROJECT_IMAGES[heroProj.value % PROJECT_IMAGES.length]})`
}))
const offerImageStyle = computed(() => ({
  background: activeOffer.value.image
    ? `center / cover no-repeat url(${activeOffer.value.image})`
    : OFFER_GRADIENTS[heroOffer.value % OFFER_GRADIENTS.length]
}))

const nextProject = () => {
  heroProj.value = (heroProj.value + 1) % HERO_PROJECTS.length
  heroOffer.value = 0
}

const nextOffer = () => {
  heroOffer.value = (heroOffer.value + 1) % activeProject.value.offers.length
}

const projectPill = ref(null)
const offerPill = ref(null)
const projectPillStyle = ref({})
const offerPillStyle = ref({})
let heroEl = null
let backdropSize = null
let resizeObserver = null

// Offsets rather than getBoundingClientRect, so the offer pill's flip animation does not skew the alignment
const offsetWithin = (el, ancestor) => {
  let x = 0
  let y = 0
  for (let node = el; node && node !== ancestor; node = node.offsetParent) {
    x += node.offsetLeft
    y += node.offsetTop
  }
  return { x, y }
}

// Mirrors the hero's `background-size: cover; background-position: left top`
const backdropStyleFor = el => {
  if (!el || !heroEl || !backdropSize) return {}
  const scale = Math.max(heroEl.clientWidth / backdropSize.w, heroEl.clientHeight / backdropSize.h)
  const { x, y } = offsetWithin(el, heroEl)
  return {
    backgroundImage: `url(${props.backdrop})`,
    backgroundSize: `${backdropSize.w * scale}px ${backdropSize.h * scale}px`,
    backgroundPosition: `${-x}px ${-y}px`,
    backgroundOrigin: 'border-box',
    backgroundRepeat: 'no-repeat'
  }
}

const alignPills = () => {
  projectPillStyle.value = backdropStyleFor(projectPill.value)
  offerPillStyle.value = backdropStyleFor(offerPill.value)
}

watch([heroProj, heroOffer], () => nextTick(alignPills))

onMounted(() => {
  timer = setInterval(() => {
    if (paused.value) return
    nextOffer()
  }, 10000)

  heroEl = projectPill.value?.closest('[data-hero-backdrop]')
  if (!props.backdrop || !heroEl) return
  const img = new Image()
  img.onload = () => {
    backdropSize = { w: img.naturalWidth, h: img.naturalHeight }
    alignPills()
  }
  img.src = props.backdrop
  resizeObserver = new ResizeObserver(alignPills)
  resizeObserver.observe(heroEl)
})

onUnmounted(() => {
  clearInterval(timer)
  resizeObserver?.disconnect()
})
</script>

<style scoped>
.hero-sheen {
  background: linear-gradient(90deg, rgba(42, 39, 36, 0) 0%, rgba(42, 39, 36, 0.055) 50%, rgba(42, 39, 36, 0) 100%);
  animation: heroSheen 9s ease-in-out infinite;
}
.hero-flip {
  animation: heroFlip 0.68s cubic-bezier(0.2, 0.75, 0.2, 1) both;
  backface-visibility: hidden;
  transform-origin: right center;
}
.hero-flip-img {
  animation: heroFlipImg 0.68s cubic-bezier(0.2, 0.75, 0.2, 1) both;
  backface-visibility: hidden;
  transform-origin: left center;
}
.hero-float {
  animation:
    heroFlipImg 0.68s cubic-bezier(0.2, 0.75, 0.2, 1) both,
    heroFloat 7s ease-in-out infinite;
}
@keyframes heroSheen {
  0% {
    transform: translateX(-130%) rotate(8deg);
  }
  55%,
  100% {
    transform: translateX(130%) rotate(8deg);
  }
}
@keyframes heroFloat {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
}
@keyframes heroFlip {
  0% {
    transform: perspective(1100px) rotateY(-74deg);
    opacity: 0.12;
  }
  50% {
    opacity: 1;
  }
  100% {
    transform: perspective(1100px) rotateY(0deg);
    opacity: 1;
  }
}
@keyframes heroFlipImg {
  0% {
    transform: perspective(1100px) rotateY(74deg);
    opacity: 0.12;
  }
  50% {
    opacity: 1;
  }
  100% {
    transform: perspective(1100px) rotateY(0deg);
    opacity: 1;
  }
}
</style>
