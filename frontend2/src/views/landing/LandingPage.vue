<template>
  <div class="min-h-screen bg-white overflow-x-hidden">
    <!-- Hero -->
    <div
      class="relative w-full flex flex-col overflow-hidden bg-no-repeat"
      :style="{ backgroundImage: `url(${heroBg})`, backgroundSize: 'cover', backgroundPosition: 'left bottom' }"
    >
      <!-- Hero navbar -->
      <div
        class="relative min-h-[72px] flex flex-wrap gap-4 items-center justify-between box-border px-5 md:px-12 py-3 max-w-[1328px] mx-auto w-full"
      >
        <div class="flex items-baseline gap-0.5">
          <span class="text-[22px] font-extrabold tracking-[-.03em] text-white">rumantra</span>
          <span class="text-[22px] font-extrabold text-brand-gold">.</span>
        </div>
        <div class="flex items-center flex-wrap gap-2">
          <a
            href="#ruang-proyek"
            class="h-11 flex items-center px-3 text-caption font-semibold text-white hover:opacity-65 transition-opacity"
          >
            {{ tv.nav.howItWorks }}
          </a>
          <a
            href="#mitra"
            class="h-11 flex items-center px-3 text-caption font-semibold text-white hover:opacity-65 transition-opacity"
          >
            {{ tv.nav.findArchitect }}
          </a>
          <a
            href="#estimasi"
            class="h-11 flex items-center px-3 mr-3 text-caption font-semibold text-white hover:opacity-65 transition-opacity"
          >
            {{ tv.nav.estimateCost }}
          </a>
          <router-link
            to="/login"
            class="h-11 flex items-center px-5 border border-white rounded-full text-caption font-bold text-white hover:bg-white/10 transition-colors"
          >
            {{ tv.nav.login }}
          </router-link>
          <router-link
            to="/brief-proyek"
            class="h-11 flex items-center px-5 rounded-full text-caption font-bold text-ink-900 bg-white hover:opacity-90 transition-opacity shadow-soft"
          >
            {{ tv.nav.cta }}
          </router-link>
        </div>
      </div>

      <!-- Hero copy + showcase -->
      <div class="relative flex flex-col justify-center min-h-[600px] md:min-h-[760px] max-w-[1328px] mx-auto w-full">
        <div class="relative box-border px-5 md:px-12 pt-9 md:pt-16 pb-10">
          <h1
            class="font-bold tracking-[-.045em] leading-[1.05] m-0 mb-5 text-white"
            style="font-size: clamp(36px, 5vw, 88px)"
          >
            {{ tv.hero.title }}
          </h1>
          <p class="text-body-lg leading-relaxed font-medium text-white/90 mb-8 max-w-[72ch]">
            {{ tv.hero.subline }}
          </p>
          <router-link
            to="/brief-proyek"
            class="inline-flex h-14 items-center px-8 border border-white rounded-full text-body-lg font-bold text-white hover:bg-white/10 transition-colors"
          >
            {{ tv.hero.cta }}
          </router-link>
        </div>

        <div class="relative flex items-end box-border px-5 md:px-12 pb-12">
          <HeroShowcase />
        </div>
      </div>
    </div>

    <!-- Tipe Proyek -->
    <div id="tipe-proyek" class="box-border px-5 md:px-12 py-16 md:py-24 max-w-[1328px] mx-auto scroll-mt-[72px]">
      <div class="flex flex-wrap items-baseline justify-between gap-4 mb-10">
        <h2 class="text-[36px] font-bold tracking-[-.025em] m-0 text-ink-900">{{ tv.tipeProyek.title }}</h2>
        <a
          href="#ruang-proyek"
          class="inline-flex items-center gap-2 flex-shrink-0 text-body-lg font-semibold text-ink-900 hover:opacity-60 transition-opacity"
        >
          {{ tv.tipeProyek.viewAll }} <ChevronRight :size="18" :stroke-width="2.4" />
        </a>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
        <router-link
          v-for="tipe in projectTypeCards"
          :key="tipe.key"
          :to="tipe.to"
          class="relative rounded-[20px] overflow-hidden bg-ink-900 min-h-[360px] flex flex-col transition-transform duration-300 hover:-translate-y-1.5 hover:shadow-[0_32px_64px_-40px_rgba(0,0,0,.32)]"
        >
          <div
            class="relative flex-1 min-h-[240px] bg-cover bg-center"
            :style="tipe.image ? { backgroundImage: `url(${tipe.image})` } : { background: tipe.gradient }"
          ></div>
          <div class="p-7 pb-8" :style="{ background: tipe.footerGradient }">
            <h3 class="text-[34px] font-bold tracking-[-.03em] leading-[1.08] m-0 mb-2 text-white">
              {{ tipe.title }}
            </h3>
            <p class="text-body leading-relaxed m-0 text-white/80 line-clamp-2">{{ tipe.desc }}</p>
          </div>
        </router-link>
      </div>
    </div>

    <!-- Estimasi -->
    <div id="estimasi" class="box-border px-5 md:px-12 py-16 md:py-24 max-w-[1328px] mx-auto scroll-mt-[72px]">
      <div class="flex flex-wrap items-baseline justify-between gap-4 mb-10">
        <h2 class="text-[36px] font-bold tracking-[-.025em] m-0 text-ink-900">{{ tv.estimasi.title }}</h2>
        <router-link
          to="/brief-proyek"
          class="inline-flex items-center gap-2 flex-shrink-0 text-body-lg font-semibold text-ink-900 hover:opacity-60 transition-opacity"
        >
          {{ tv.estimasi.continueCta }} <ChevronRight :size="18" :stroke-width="2.4" />
        </router-link>
      </div>

      <BudgetEstimator />
    </div>

    <!-- Mitra -->
    <div id="mitra" class="box-border px-5 md:px-12 py-16 md:py-24 scroll-mt-[72px] bg-brand-cream">
      <div
        class="max-w-[1328px] mx-auto bg-white rounded-[20px] p-8 md:p-14 flex flex-wrap items-center justify-between gap-10"
      >
        <div class="flex-1 basis-[360px] min-w-0">
          <h3
            class="font-bold tracking-[-.03em] leading-[1.1] m-0 mb-3 text-ink-900 max-w-[20ch]"
            style="font-size: clamp(28px, 3.4vw, 44px)"
          >
            {{ tv.mitra.title }}
          </h3>
          <p class="text-body-lg leading-relaxed text-ink-500 m-0 max-w-[56ch]">{{ tv.mitra.subtitle }}</p>
        </div>
        <div class="flex items-center flex-shrink-0">
          <div
            v-for="(g, i) in mitraGradients"
            :key="i"
            class="relative flex-shrink-0 w-[110px] h-[148px] md:w-[132px] md:h-[176px] rounded-2xl overflow-hidden shadow-[0_18px_40px_-28px_rgba(0,0,0,.45)] transition-transform duration-300 hover:-translate-y-2"
            :class="i > 0 ? '-ml-8' : ''"
            :style="{ background: g, boxShadow: '0 0 0 4px #FDF6EE, 0 18px 40px -28px rgba(0,0,0,.45)' }"
          ></div>
          <div
            class="relative flex-shrink-0 w-[110px] h-[148px] md:w-[132px] md:h-[176px] -ml-8 rounded-2xl flex flex-col items-center justify-center gap-0.5"
            style="
              background: linear-gradient(135deg, #185c93, #0d2f4d);
              box-shadow:
                0 0 0 4px #fdf6ee,
                0 18px 40px -28px rgba(0, 0, 0, 0.45);
            "
          >
            <span class="text-[28px] font-bold tracking-[-.03em] text-white leading-none">+20</span>
            <span class="text-caption font-semibold text-white/80">{{ tv.mitra.more }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Ruang Proyek -->
    <div id="ruang-proyek" class="box-border px-5 md:px-12 py-16 md:py-24 max-w-[1328px] mx-auto scroll-mt-[72px]">
      <h2 class="text-[36px] font-bold tracking-[-.025em] m-0 mb-10 max-w-[720px] text-ink-900">
        {{ tv.ruangProyek.title }}
      </h2>

      <div class="border border-hairline rounded-3xl overflow-hidden bg-surface-alt mb-5">
        <div class="flex items-center justify-between gap-6 px-7 py-5 bg-ink-900 border-b border-white/[.14] flex-wrap">
          <div class="flex items-center gap-4 flex-wrap">
            <span class="text-body-lg font-bold tracking-[-.01em] text-white">{{ tv.ruangProyek.projectName }}</span>
            <span class="text-body-sm text-white/70">{{ tv.ruangProyek.projectOwner }}</span>
          </div>
          <div class="flex gap-1">
            <span class="h-9 flex items-center px-3.5 rounded-full bg-white text-ink-900 text-caption font-semibold">
              {{ tv.ruangProyek.tabDiskusi }}
            </span>
            <span class="h-9 flex items-center px-3.5 rounded-full text-caption font-semibold text-white/70">
              {{ tv.ruangProyek.tabKontrak }}
            </span>
            <span class="h-9 flex items-center px-3.5 rounded-full text-caption font-semibold text-white/70">
              {{ tv.ruangProyek.tabFile }}
            </span>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 p-7">
          <div class="bg-white border border-hairline rounded-2xl p-6">
            <p class="text-caption-sm font-semibold uppercase tracking-[.06em] text-ink-500 mb-5">
              {{ tv.ruangProyek.stageLabel }}
            </p>
            <div class="flex flex-col gap-4">
              <div class="flex items-center gap-3">
                <span
                  class="w-[22px] h-[22px] rounded-full bg-ink-900 text-white flex items-center justify-center text-micro font-semibold"
                  >&check;</span
                >
                <span class="text-body-sm text-ink-500">{{ tv.ruangProyek.stage1 }}</span>
              </div>
              <div class="flex items-center gap-3">
                <span
                  class="w-[22px] h-[22px] rounded-full bg-accent-blue text-white flex items-center justify-center text-micro font-semibold"
                  >2</span
                >
                <span class="text-body-sm font-semibold text-ink-900">{{ tv.ruangProyek.stage2 }}</span>
              </div>
              <div class="flex items-center gap-3">
                <span
                  class="w-[22px] h-[22px] rounded-full border border-ink-200 flex items-center justify-center text-micro font-semibold text-ink-500"
                  >3</span
                >
                <span class="text-body-sm text-ink-500">{{ tv.ruangProyek.stage3 }}</span>
              </div>
              <div class="flex items-center gap-3">
                <span
                  class="w-[22px] h-[22px] rounded-full border border-ink-200 flex items-center justify-center text-micro font-semibold text-ink-500"
                  >4</span
                >
                <span class="text-body-sm text-ink-500">{{ tv.ruangProyek.stage4 }}</span>
              </div>
            </div>
          </div>

          <div class="bg-white border border-hairline rounded-2xl p-6">
            <p class="text-caption-sm font-semibold uppercase tracking-[.06em] text-ink-500 mb-5">
              {{ tv.ruangProyek.reviewLabel }}
            </p>
            <div class="flex items-center gap-4 pb-5 border-b border-hairline mb-5">
              <div
                class="w-16 h-16 rounded-xl flex-shrink-0"
                style="background: repeating-linear-gradient(135deg, #eceae5 0 6px, #f7f6f3 6px 12px)"
              ></div>
              <div class="flex-1">
                <p class="text-body-sm font-semibold m-0 mb-1 text-ink-900">{{ tv.ruangProyek.item1Title }}</p>
                <p class="text-caption text-ink-500 m-0">{{ tv.ruangProyek.item1Meta }}</p>
              </div>
              <div class="flex gap-2 flex-shrink-0">
                <span
                  class="h-10 flex items-center px-4 rounded-full bg-ink-900 text-white text-caption font-semibold"
                  >{{ tv.ruangProyek.viewBtn }}</span
                >
                <span
                  class="h-10 flex items-center px-4 rounded-full border border-hairline text-caption font-semibold"
                  >{{ tv.ruangProyek.reviseBtn }}</span
                >
              </div>
            </div>
            <div class="flex items-center gap-4">
              <div
                class="w-16 h-16 rounded-xl flex-shrink-0"
                style="background: repeating-linear-gradient(135deg, #eceae5 0 6px, #f7f6f3 6px 12px)"
              ></div>
              <div class="flex-1">
                <p class="text-body-sm font-semibold m-0 mb-1 text-ink-900">{{ tv.ruangProyek.item2Title }}</p>
                <p class="text-caption text-ink-500 m-0">{{ tv.ruangProyek.item2Meta }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-10 gap-x-6 mt-12">
        <div>
          <h3 class="text-h3 text-ink-900 mb-2">{{ tv.ruangProyek.benefit1Title }}</h3>
          <p class="text-body-sm text-ink-500 leading-relaxed">{{ tv.ruangProyek.benefit1Desc }}</p>
        </div>
        <div>
          <h3 class="text-h3 text-ink-900 mb-2">{{ tv.ruangProyek.benefit2Title }}</h3>
          <p class="text-body-sm text-ink-500 leading-relaxed">{{ tv.ruangProyek.benefit2Desc }}</p>
        </div>
        <div>
          <h3 class="text-h3 text-ink-900 mb-2">{{ tv.ruangProyek.benefit3Title }}</h3>
          <p class="text-body-sm text-ink-500 leading-relaxed">{{ tv.ruangProyek.benefit3Desc }}</p>
        </div>
      </div>
    </div>

    <!-- Pembayaran -->
    <div class="box-border px-5 md:px-12 py-16 md:py-24" style="background: linear-gradient(135deg, #2f7dc0, #0d2f4d)">
      <div class="max-w-[1328px] mx-auto">
        <h2 class="text-[36px] font-bold tracking-[-.025em] m-0 mb-10 max-w-[720px] text-white">
          {{ tv.pembayaran.title }}
        </h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10 gap-x-8">
          <div v-for="(step, i) in pembayaranSteps" :key="i">
            <div
              class="w-9 h-9 rounded-full bg-white text-ink-900 flex items-center justify-center text-body-sm font-semibold mb-5"
            >
              {{ i + 1 }}
            </div>
            <h3 class="text-h3 text-white mb-2">{{ step.title }}</h3>
            <p class="text-body-sm text-white leading-relaxed">{{ step.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- FAQ -->
    <div class="box-border px-5 md:px-12 py-16 md:py-24 bg-surface-alt">
      <div class="max-w-[1328px] mx-auto">
        <h2 class="text-[36px] font-bold tracking-[-.025em] m-0 mb-10 text-ink-900">{{ tv.faq.title }}</h2>
        <LandingFaq :items="tv.faq.items" class="max-w-[820px]" />
      </div>
    </div>

    <!-- Penutup -->
    <div
      class="box-border px-5 md:px-12 py-16 md:py-24 text-center"
      style="background: linear-gradient(160deg, #3d2114 0%, #1a1a1a 60%, #0a0a0a 100%)"
    >
      <h2 class="text-[36px] font-bold tracking-[-.025em] m-0 mb-4 text-white">{{ tv.penutup.title }}</h2>
      <p class="text-body-lg text-white mx-auto mb-3 max-w-[620px] leading-relaxed">{{ tv.penutup.desc1 }}</p>
      <p class="text-body-lg text-white mx-auto mb-8 max-w-[620px] leading-relaxed">{{ tv.penutup.desc2 }}</p>
      <router-link
        to="/brief-proyek"
        class="inline-flex h-[52px] items-center px-8 bg-white text-ink-900 rounded-full font-semibold text-body-lg hover:opacity-90 transition-opacity"
      >
        {{ tv.penutup.cta }}
      </router-link>
      <p class="text-body-sm text-white mx-auto mt-5 max-w-[560px] leading-relaxed">{{ tv.penutup.freeNote }}</p>
      <div class="flex justify-center mt-7">
        <router-link
          to="/signup?role=ARCHITECT"
          class="inline-flex items-center gap-2 h-12 px-6 border border-white rounded-full text-body font-bold text-white hover:bg-white/10 transition-colors"
        >
          {{ tv.penutup.architectCta }} <ChevronRight :size="18" :stroke-width="2.4" />
        </router-link>
      </div>
    </div>

    <LandingFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { landingAPI } from '@/services/api'
import HeroShowcase from '@/components/landing/HeroShowcase.vue'
import BudgetEstimator from '@/components/landing/BudgetEstimator.vue'
import LandingFaq from '@/components/landing/LandingFaq.vue'
import LandingFooter from '@/components/landing/LandingFooter.vue'
import heroBg from '@/assets/images/landing/hero-background.png'

const { t, locale } = useI18n()
const tv = computed(() => t.value.landing.v2)

const tipeCards = computed(() => [
  {
    key: 'hunian',
    category: 'RESIDENTIAL',
    title: tv.value.tipeProyek.hunian.title,
    desc: tv.value.tipeProyek.hunian.desc,
    gradient: 'linear-gradient(135deg,#9B5E3C,#3D2114)',
    footerGradient: 'linear-gradient(135deg,#7C4728,#3D2114)'
  },
  {
    key: 'komersil',
    category: 'COMMERCIAL',
    title: tv.value.tipeProyek.komersil.title,
    desc: tv.value.tipeProyek.komersil.desc,
    gradient: 'linear-gradient(135deg,#2F7DC0,#0d2f4d)',
    footerGradient: 'linear-gradient(135deg,#185C93,#0d2f4d)'
  },
  {
    key: 'industrial',
    category: 'INDUSTRIAL',
    title: tv.value.tipeProyek.industrial.title,
    desc: tv.value.tipeProyek.industrial.desc,
    gradient: 'linear-gradient(135deg,#333333,#0A0A0A)',
    footerGradient: 'linear-gradient(135deg,#1C1C1C,#0A0A0A)'
  },
  {
    key: 'lainnya',
    category: 'MIXED_USE',
    title: tv.value.tipeProyek.lainnya.title,
    desc: tv.value.tipeProyek.lainnya.desc,
    gradient: 'linear-gradient(135deg,#B39069,#6A3D22)',
    footerGradient: 'linear-gradient(135deg,#C5A17A,#6A3D22)'
  }
])

const presets = ref([])

const localizedPreset = (preset, field) =>
  (locale.value === 'en' ? preset[`${field}En`] : preset[`${field}Id`]) || preset[`${field}En`] || ''

// Presets are the cards when the superuser has any; the four fixed category cards are the fallback
// so the section never renders empty. A preset borrows its category's gradient until it has a photo.
const projectTypeCards = computed(() => {
  const fixed = tipeCards.value
  if (!presets.value.length) {
    return fixed.map(c => ({ ...c, to: { path: '/brief-proyek', query: { kategori: c.category } } }))
  }
  const fallbackStyle = fixed[fixed.length - 1]
  return presets.value.map(preset => {
    const style = fixed.find(c => c.category === preset.buildingFunction) || fallbackStyle
    return {
      key: preset.slug,
      to: { path: '/brief-proyek', query: { preset: preset.slug } },
      title: localizedPreset(preset, 'label'),
      desc: localizedPreset(preset, 'defaultDescription') || localizedPreset(preset, 'eyebrow'),
      image: preset.imageLargeUrl || preset.imageUrl,
      gradient: style.gradient,
      footerGradient: style.footerGradient
    }
  })
})

onMounted(async () => {
  try {
    const res = await landingAPI.getPresets()
    presets.value = res.data?.data || []
  } catch {
    // The fixed category cards cover a failed request
  }
})

const mitraGradients = [
  'linear-gradient(135deg,#9B5E3C,#3D2114)',
  'linear-gradient(135deg,#2F7DC0,#0d2f4d)',
  'linear-gradient(135deg,#B39069,#6A3D22)',
  'linear-gradient(135deg,#333333,#0A0A0A)'
]

const pembayaranSteps = computed(() => [
  { title: tv.value.pembayaran.step1Title, desc: tv.value.pembayaran.step1Desc },
  { title: tv.value.pembayaran.step2Title, desc: tv.value.pembayaran.step2Desc },
  { title: tv.value.pembayaran.step3Title, desc: tv.value.pembayaran.step3Desc },
  { title: tv.value.pembayaran.step4Title, desc: tv.value.pembayaran.step4Desc }
])
</script>
