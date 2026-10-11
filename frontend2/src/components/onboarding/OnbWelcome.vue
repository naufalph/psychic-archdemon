<template>
  <div class="flex flex-col gap-12">
    <section
      class="relative overflow-hidden rounded-[20px] bg-brand-cream"
      :style="{ background: `left top / cover no-repeat url(${heroBg})` }"
    >
      <div class="relative px-6 py-12 md:px-16 md:pt-[72px] md:pb-20 flex flex-col gap-6">
        <h1 class="welcome-title m-0 text-white font-bold tracking-[-.045em] leading-[1.05]">
          <span class="block">Waktunya fokus berkarya.</span>
          <span class="block">Kami yang carikan proyeknya.</span>
        </h1>
        <p class="m-0 text-white text-[clamp(17px,1.6vw,20px)] leading-[1.55] max-w-[760px]">
          Mari siapkan profil Anda. Setelah selesai, Anda bisa menerima brief proyek dari klien dan mengirim penawaran
          dengan syarat Anda sendiri.
        </p>
        <button
          type="button"
          class="self-start inline-flex h-14 items-center gap-2 px-8 mt-2 border border-white rounded-full bg-transparent text-body-lg font-bold text-white cursor-pointer hover:bg-white/10 transition-colors"
          @click="$emit('next')"
        >
          Mulai <ChevronRight :size="18" :stroke-width="2.4" />
        </button>
      </div>
    </section>

    <section aria-labelledby="onb-cara-kerja" class="flex flex-col gap-8">
      <h2 id="onb-cara-kerja" class="m-0 text-[28px] font-bold tracking-[-.025em]">Cara kerja rumantra untuk Anda</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-8">
        <div v-for="point in points" :key="point.title" class="flex flex-col gap-4">
          <span class="w-11 h-11 rounded-xl bg-surface-alt flex items-center justify-center">
            <component :is="point.icon" :size="22" :stroke-width="2.2" />
          </span>
          <h3 class="m-0 text-[20px] font-semibold tracking-[-.01em] leading-[1.3]">{{ point.title }}</h3>
          <p class="m-0 text-[15px] leading-[1.55] text-ink-500">{{ point.desc }}</p>
        </div>
      </div>
    </section>

    <OnbFooter next-label="Mulai" @next="$emit('next')">
      <template #start>
        <p class="m-0 text-[15px] text-ink-500 flex items-center gap-2.5">
          <Info :size="18" :stroke-width="2.2" class="flex-none" />Siapkan nomor KTP dan NPWP Anda sebelum mulai.
        </p>
      </template>
    </OnbFooter>
  </div>
</template>

<script setup>
import { ChevronRight, Info, LayoutDashboard, Lightbulb, SearchCheck, Wallet } from 'lucide-vue-next'
import OnbFooter from './OnbFooter.vue'
import { WELCOME_POINTS } from '@/constants/architectOnboarding'
import heroBg from '@/assets/images/landing/hero-background.jpg'

defineEmits(['next'])

const ICONS = { SearchCheck, Lightbulb, Wallet, LayoutDashboard }
const points = WELCOME_POINTS.map(p => ({ ...p, icon: ICONS[p.icon] }))
</script>

<style scoped>
.welcome-title {
  font-size: clamp(36px, 5vw, 88px);
}

/* One sentence per line from tablet up: the landing hero size, shrunk only as far as the
   longer sentence (~12.5em wide) needs to fit the banner's text column. */
@media (min-width: 768px) {
  .welcome-title span {
    white-space: nowrap;
  }
  .welcome-title {
    font-size: min(clamp(36px, 5vw, 88px), calc((min(100vw - 48px, 1232px) - 128px) / 12.9));
  }
}
</style>
