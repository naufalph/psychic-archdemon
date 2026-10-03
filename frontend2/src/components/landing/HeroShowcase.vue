<template>
  <div class="w-full flex flex-wrap gap-4 items-stretch min-h-[360px] md:min-h-[404px]">
    <!-- Project summary card -->
    <div
      class="flex-1 basis-[300px] min-w-0 grid grid-rows-[auto_78px_1fr_104px] rounded-[20px] border border-black/[.08] bg-white p-8 shadow-[0_24px_60px_-48px_rgba(0,0,0,.35)]"
    >
      <div class="justify-self-start">
        <button
          class="inline-flex items-center gap-2 h-[34px] pl-3.5 pr-1.5 rounded-full border border-white/[.18] bg-ink-900/[.62] backdrop-blur-sm text-white text-caption font-bold hover:bg-ink-900/[.78] transition-colors"
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
        <div
          class="absolute inset-0 flex items-center justify-center text-center px-4"
          :style="placeholderStyle(heroProj)"
        >
          <span class="text-caption-sm font-semibold text-white/80">{{ activeProject.slotLabel }}</span>
        </div>
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
      <div
        aria-hidden="true"
        class="absolute -top-[40%] left-0 w-[45%] h-[180%] pointer-events-none mix-blend-multiply hero-sheen"
      ></div>

      <div
        :key="'offer-' + heroProj + '-' + heroOffer"
        class="flex-1 basis-[260px] min-w-0 grid grid-rows-[auto_78px_auto_auto_1fr_104px] hero-flip"
      >
        <div class="justify-self-start">
          <button
            class="inline-flex items-center gap-2 h-[34px] pl-3.5 pr-1.5 rounded-full border border-white/[.18] bg-ink-900/[.62] backdrop-blur-sm text-white text-caption font-bold hover:bg-ink-900/[.78] transition-colors"
            @click="nextOffer"
          >
            <span>{{ t.landing.v2.hero.offerPrefix }}{{ heroOffer + 1 }}</span>
            <span
              class="inline-flex items-center justify-center w-[22px] h-[22px] rounded-full bg-white flex-shrink-0"
            >
              <ChevronRight :size="12" :stroke-width="3" class="text-ink-900" />
            </span>
          </button>
        </div>

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
          <span class="text-caption-sm font-semibold text-white/80">Eksplorasi massa</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const HERO_PROJECTS = [
  {
    title: 'Kosan Premium 12 Kamar',
    total: 'Rp1,8 M',
    desain: 'Rp144 Jt',
    slotLabel: 'Foto lokasi / referensi',
    offers: [
      {
        name: 'Aryadiza Gunawan',
        rating: '★ 4,9',
        chips: ['Arsitek freelance', '8 tahun pengalaman', 'Anggota IAI', 'Arsitektur ITB'],
        biaya: 'Rp105 Jt',
        durasi: '6 minggu',
        revisi: '3 kali'
      },
      {
        name: 'RR Studio',
        rating: '',
        chips: ['Firma arsitektur', '20 tahun pengalaman', 'IAI certified'],
        biaya: 'Rp160 Jt',
        durasi: '8 minggu',
        revisi: 'Unlimited'
      }
    ]
  },
  {
    title: 'American-style Diner',
    total: 'Rp—',
    desain: 'Rp—',
    slotLabel: 'Foto lokasi / referensi',
    offers: [
      {
        name: 'Nama arsitek',
        rating: '',
        chips: ['Tipe praktik', 'Pengalaman', 'Sertifikasi'],
        biaya: 'Rp—',
        durasi: '— minggu',
        revisi: '—'
      },
      {
        name: 'Nama arsitek',
        rating: '',
        chips: ['Tipe praktik', 'Pengalaman', 'Sertifikasi'],
        biaya: 'Rp—',
        durasi: '— minggu',
        revisi: '—'
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

const PROJECT_GRADIENTS = ['linear-gradient(135deg,#7C4728,#3D2114)', 'linear-gradient(135deg,#185C93,#0d2f4d)']
const OFFER_GRADIENTS = ['linear-gradient(135deg,#C5A17A,#7C4728)', 'linear-gradient(135deg,#2F7DC0,#185C93)']

const placeholderStyle = i => ({ background: PROJECT_GRADIENTS[i % PROJECT_GRADIENTS.length] })
const offerImageStyle = computed(() => ({ background: OFFER_GRADIENTS[heroOffer.value % OFFER_GRADIENTS.length] }))

const nextProject = () => {
  heroProj.value = (heroProj.value + 1) % HERO_PROJECTS.length
  heroOffer.value = 0
}

const nextOffer = () => {
  heroOffer.value = (heroOffer.value + 1) % activeProject.value.offers.length
}

onMounted(() => {
  timer = setInterval(() => {
    if (paused.value) return
    nextOffer()
  }, 10000)
})

onUnmounted(() => clearInterval(timer))
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
