<template>
  <div class="min-h-screen bg-white overflow-x-hidden">
    <Navbar />

    <!-- Hero -->
    <section
      class="py-16 px-10 max-w-[1440px] mx-auto grid grid-cols-1 lg:grid-cols-2 gap-20 items-center min-h-[520px]"
    >
      <!-- Left: copy -->
      <div class="max-w-xl">
        <h1 class="text-h1 text-ink-900 mb-5 max-w-[480px]">
          {{ t.landing.hero.title }}
        </h1>

        <p class="text-body-lg text-ink-400 font-light mb-8 max-w-[440px] leading-relaxed">
          {{ t.landing.hero.subline }}
        </p>

        <div class="flex items-center gap-6 flex-wrap">
          <router-link to="/signup">
            <BaseButton size="sm" class="shadow-[0_4px_12px_rgba(0,0,0,0.08)]">
              {{ t.landing.hero.cta }}
            </BaseButton>
          </router-link>
          <router-link
            to="/"
            class="flex items-center gap-1 text-body-sm font-semibold text-ink-500 hover:text-ink-900 transition-colors"
          >
            {{ t.landing.hero.learnMore }} <span>→</span>
          </router-link>
        </div>

        <p class="text-caption text-ink-300 font-medium mt-4 mb-6">
          {{ t.landing.hero.microcopy }}
        </p>
      </div>

      <!-- Right: proposal carousel -->
      <div>
        <ProposalCarousel :slides="heroSlides" :loading="isLoading" />
      </div>
    </section>

    <!-- Mulai Di Sini / Preset cards -->
    <section v-if="presets.length > 0" class="py-16 bg-surface-muted border-t border-hairline">
      <div class="max-w-[1440px] mx-auto px-10">
        <p class="text-micro font-semibold uppercase tracking-[0.05em] text-ink-400 mb-3">
          {{ t.landing.starters.eyebrow }}
        </p>
        <h2 class="text-h2 text-ink-900 mb-3">{{ t.landing.starters.title }}</h2>
        <p class="text-body-lg text-ink-400 font-light mb-10 max-w-lg">
          {{ t.landing.starters.subline }}
        </p>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-5 gap-4">
          <StarterCard
            v-for="preset in presets"
            :key="preset.slug"
            :label="presetLabel(preset)"
            :eyebrow="presetEyebrow(preset)"
            :icon="resolvePresetIcon(preset.iconName)"
            @click="goToPreset(preset.slug)"
          />
        </div>
      </div>
    </section>

    <!-- Jelajahi / Browse section -->
    <section class="pb-24">
      <!-- Rule-through "Sedang trending" divider -->
      <div class="relative text-center my-16 mx-10">
        <div class="absolute top-1/2 left-0 w-full h-px bg-hairline"></div>
        <p
          class="relative z-10 inline-block bg-white px-6 text-caption font-semibold text-ink-300 uppercase tracking-[0.15em]"
        >
          {{ t.landing.jelajahi.trending }}
        </p>
      </div>

      <div class="max-w-[1440px] mx-auto px-10 mb-10">
        <!-- Search input -->
        <div class="mb-6">
          <SearchInput
            v-model="catalog.search"
            :placeholder="t.landing.jelajahi.searchPlaceholder"
            @enter="goToStartProject"
          />
        </div>

        <!-- Filter chips -->
        <div class="flex gap-2.5 overflow-x-auto pb-1" style="scrollbar-width: none">
          <CategoryChip
            v-for="cat in CATEGORIES"
            :key="cat.value"
            :label="t.landing.jelajahi[cat.labelKey]"
            :active="catalog.category === cat.value"
            @click="catalog.setCategory(cat.value)"
          />
        </div>
      </div>

      <!-- Project grid -->
      <div class="max-w-[1440px] mx-auto px-10">
        <!-- Loading skeleton -->
        <div
          v-if="isLoading"
          class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-5"
        >
          <div
            v-for="i in 8"
            :key="i"
            class="rounded-card overflow-hidden border border-hairline bg-white animate-pulse"
          >
            <div class="bg-gray-200" style="aspect-ratio: 4/3"></div>
            <div class="p-4 space-y-2">
              <div class="h-4 bg-gray-200 rounded w-3/4"></div>
              <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              <div class="h-3 bg-gray-200 rounded w-2/3"></div>
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <div v-else-if="filteredProjects.length === 0" class="py-20 text-center">
          <p class="text-body-sm text-ink-300">{{ t.landing.jelajahi.emptyState }}</p>
        </div>

        <!-- Cards -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-5">
          <ImageCard v-for="project in filteredProjects" :key="project.id" :project="project" />
          <AddDesignTile :label="t.landing.jelajahi.addDesign" @click="goToStartProject" />
        </div>
      </div>
    </section>

    <!-- Cara Kerja / How It Works -->
    <section class="py-24 bg-surface-muted">
      <div class="max-w-[1440px] mx-auto px-10">
        <div
          class="inline-flex items-center gap-2 px-3 py-1.5 bg-white border border-hairline-alt rounded-full text-micro-sm font-semibold mb-12"
        >
          {{ t.landing.howItWorks.eyebrow }}
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-10">
          <div v-for="(step, i) in HOW_IT_WORKS_KEYS" :key="i" class="relative">
            <div class="text-numeral text-hairline mb-4 select-none">
              {{ String(i + 1).padStart(2, '0') }}
            </div>
            <h3 class="text-h3 text-ink-900 mb-3">
              {{ t.landing.howItWorks[step.titleKey] }}
            </h3>
            <p class="text-body-sm text-ink-400 leading-relaxed">
              {{ t.landing.howItWorks[step.descKey] }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Dirancang untuk ketenangan pikiran / Why -->
    <section class="py-24 bg-white border-t border-hairline">
      <div class="max-w-[1440px] mx-auto px-10">
        <p class="text-micro font-semibold uppercase tracking-[0.05em] text-ink-400 mb-10">
          {{ t.landing.why.label }}
        </p>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="(card, i) in WHY_CARDS"
            :key="i"
            class="p-10 border border-hairline-alt rounded-card hover:-translate-y-1 transition-transform duration-200 flex flex-col gap-4"
          >
            <div class="w-12 h-12 bg-ink-900 rounded-xl flex items-center justify-center mb-6">
              <component :is="card.icon" class="w-6 h-6 text-white" />
            </div>
            <h3 class="text-body-lg font-semibold text-ink-900 mb-3 tracking-[-0.02em]">
              {{ t.landing.why[card.titleKey] }}
            </h3>
            <p class="text-caption text-ink-400 leading-relaxed">
              {{ t.landing.why[card.descKey] }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="py-24 bg-surface-muted">
      <div class="max-w-[1440px] mx-auto px-10 text-center">
        <h2 class="text-h2 text-ink-900 mb-4">
          {{ t.landing.cta.title }}
        </h2>
        <p class="text-body-lg text-ink-400 font-light mb-10 max-w-lg mx-auto">
          {{ t.landing.cta.subline }}
        </p>
        <div class="flex items-center justify-center gap-4 flex-wrap">
          <router-link to="/signup">
            <BaseButton size="md" class="shadow-[0_4px_12px_rgba(0,0,0,0.08)]">
              {{ t.landing.cta.primary }}
            </BaseButton>
          </router-link>
          <router-link to="/signup?role=ARCHITECT">
            <BaseButton variant="outline" size="md">
              {{ t.landing.cta.secondary }}
            </BaseButton>
          </router-link>
        </div>
      </div>
    </section>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ShieldCheck, Tag, Lock, MessageSquare, Layers, MapPin } from 'lucide-vue-next'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import ProposalCarousel from '@/components/landing/ProposalCarousel.vue'
import SearchInput from '@/components/landing/SearchInput.vue'
import CategoryChip from '@/components/landing/CategoryChip.vue'
import ImageCard from '@/components/landing/ImageCard.vue'
import AddDesignTile from '@/components/landing/AddDesignTile.vue'
import StarterCard from '@/components/landing/StarterCard.vue'
import { resolvePresetIcon } from '@/components/landing/presetIcons'
import { useI18n } from '@/composables/useI18n'
import { projectAPI, landingAPI } from '@/services/api'
import { useCatalogStore } from '@/stores/catalog'
import { CATEGORIES, filterProjects } from '@/utils/catalogFormat'

const { t, locale } = useI18n()
const router = useRouter()
const catalog = useCatalogStore()

const projects = ref([])
const heroSlides = ref([])
const presets = ref([])
const isLoading = ref(true)

const HOW_IT_WORKS_KEYS = [
  { titleKey: 'step1Title', descKey: 'step1Desc' },
  { titleKey: 'step2Title', descKey: 'step2Desc' },
  { titleKey: 'step3Title', descKey: 'step3Desc' }
]

const WHY_CARDS = [
  { icon: ShieldCheck, titleKey: 'card1Title', descKey: 'card1Desc' },
  { icon: Tag, titleKey: 'card2Title', descKey: 'card2Desc' },
  { icon: Lock, titleKey: 'card3Title', descKey: 'card3Desc' },
  { icon: MessageSquare, titleKey: 'card4Title', descKey: 'card4Desc' },
  { icon: Layers, titleKey: 'card5Title', descKey: 'card5Desc' },
  { icon: MapPin, titleKey: 'card6Title', descKey: 'card6Desc' }
]

const filteredProjects = computed(() =>
  filterProjects(projects.value, { search: catalog.search, category: catalog.category })
)

const goToStartProject = () => router.push('/mulai-proyek')

const goToPreset = slug => router.push({ name: 'StartProject', query: { preset: slug } })

const presetLabel = preset => (locale.value === 'en' ? preset.labelEn : preset.labelId) || preset.labelEn
const presetEyebrow = preset =>
  (locale.value === 'en' ? preset.eyebrowEn : preset.eyebrowId) || t.value.landing.starters.eyebrow

onMounted(async () => {
  // silently fail on any request — the landing page works without projects, slides, or presets
  const [previews, slides, presetList] = await Promise.allSettled([
    projectAPI.getPublicPreviews(),
    landingAPI.getHeroSlides(),
    landingAPI.getPresets()
  ])

  if (previews.status === 'fulfilled') projects.value = previews.value.data?.data || []
  if (slides.status === 'fulfilled') heroSlides.value = slides.value.data?.data || []
  if (presetList.status === 'fulfilled') presets.value = presetList.value.data?.data || []

  isLoading.value = false
})
</script>
