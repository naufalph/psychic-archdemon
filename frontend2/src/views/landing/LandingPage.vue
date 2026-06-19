<template>
  <div class="min-h-screen bg-white overflow-x-hidden">
    <Navbar />

    <!-- Hero -->
    <section
      class="py-16 px-10 max-w-[1440px] mx-auto grid grid-cols-1 lg:grid-cols-2 gap-20 items-center min-h-[520px]"
    >
      <!-- Left: copy -->
      <div class="max-w-xl">
        <div
          class="inline-flex items-center gap-2 px-3 py-1.5 bg-white border border-[#CCCCCC] rounded-full text-[11px] font-semibold mb-6"
        >
          ⭐ {{ t.landing.eyebrow }}
        </div>

        <h1 class="font-semibold leading-[1.1] text-[#0A0A0A] mb-5" style="font-size: 56px; letter-spacing: -0.03em">
          {{ t.landing.hero.title }}
        </h1>

        <p class="text-[18px] text-[#666666] font-light mb-8 max-w-[440px] leading-relaxed">
          {{ t.landing.hero.subline }}
        </p>

        <div class="flex items-center gap-6 flex-wrap">
          <router-link to="/signup">
            <button
              class="px-6 py-2.5 bg-[#0A0A0A] text-white rounded-full text-[14px] font-semibold hover:opacity-90 transition-all hover:-translate-y-px"
              style="box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08)"
            >
              {{ t.landing.hero.cta }}
            </button>
          </router-link>
          <router-link
            to="/"
            class="flex items-center gap-1 text-[15px] font-semibold text-[#333333] hover:text-[#0A0A0A] transition-colors"
          >
            {{ t.landing.hero.learnMore }} <span>→</span>
          </router-link>
        </div>

        <p class="text-[14px] text-[#888888] font-medium mt-4 mb-6">
          {{ t.landing.hero.microcopy }}
        </p>

        <!-- Trust metrics — commented out, still in development
        <div class="flex items-center gap-4 pt-6 border-t border-[#E8E8E8] text-[13px] text-[#666666]">
          <div class="flex items-center">
            <img
              v-for="i in 4"
              :key="i"
              :src="`https://picsum.photos/seed/trustav${i}/28/28`"
              class="w-7 h-7 rounded-full object-cover border-2 border-white"
              :style="i > 1 ? 'margin-left:-10px;' : ''"
              alt="user avatar"
            />
          </div>
          <div>
            <strong class="text-[#0A0A0A] font-bold">{{ t.landing.trust.rating }}</strong>
            {{ t.landing.trust.desc }}
          </div>
        </div>
        -->
      </div>

      <!-- Right: stepper mockup -->
      <div>
        <HeroStepper />
      </div>
    </section>

    <!-- Jelajahi / Browse section -->
    <section class="pb-24">
      <!-- Rule-through "Sedang trending" divider -->
      <div class="relative text-center my-16 mx-10">
        <div class="absolute top-1/2 left-0 w-full h-px bg-[#E8E8E8]"></div>
        <p
          class="relative z-10 inline-block bg-white px-6 text-[14px] font-semibold text-[#888888] uppercase tracking-[0.15em]"
        >
          {{ t.landing.jelajahi.trending }}
        </p>
      </div>

      <div class="max-w-[1440px] mx-auto px-10 mb-10">
        <!-- Search input -->
        <div class="relative mb-6">
          <svg
            class="absolute left-5 top-1/2 -translate-y-1/2 text-[#888888]"
            style="width: 20px; height: 20px"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="t.landing.jelajahi.searchPlaceholder"
            class="w-full py-4 pl-14 pr-6 border border-[#E8E8E8] rounded-2xl text-[16px] bg-[#F5F5F5] outline-none transition-all focus:bg-white focus:border-[#0A0A0A]"
            style="focus:box-shadow:0 0 0 4px rgba(0,0,0,0.03);"
          />
        </div>

        <!-- Filter chips -->
        <div class="flex gap-2.5 overflow-x-auto pb-1" style="scrollbar-width: none">
          <button
            v-for="cat in CATEGORIES"
            :key="cat.value"
            class="px-5 py-2.5 rounded-full text-[14px] font-medium border transition-all whitespace-nowrap flex-shrink-0"
            :class="
              activeCategory === cat.value
                ? 'bg-[#0A0A0A] text-white border-[#0A0A0A]'
                : 'bg-white text-[#666666] border-[#E8E8E8] hover:border-[#0A0A0A]'
            "
            @click="activeCategory = cat.value"
          >
            {{ t.landing.jelajahi[cat.labelKey] }}
          </button>
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
            class="rounded-[20px] overflow-hidden border border-[#E8E8E8] bg-white animate-pulse"
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
          <p class="text-[15px] text-[#888888]">{{ t.landing.jelajahi.emptyState }}</p>
        </div>

        <!-- Cards -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-5">
          <div
            v-for="project in filteredProjects"
            :key="project.id"
            class="group rounded-[20px] overflow-hidden border border-[#E8E8E8] bg-white cursor-pointer transition-all duration-300 hover:-translate-y-1.5 hover:shadow-[0_20px_30px_-10px_rgba(0,0,0,0.12)] hover:border-[#CCCCCC]"
          >
            <div class="relative overflow-hidden bg-[#F5F5F5]" style="aspect-ratio: 4/3">
              <img
                v-if="project.firstImageUrl"
                :src="project.firstImageUrl"
                :alt="project.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div v-else class="w-full h-full flex items-center justify-center">
                <svg class="w-12 h-12 text-[#CCCCCC]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="1.5"
                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
              </div>
              <span
                v-if="project.buildingFunction || project.projectCategory"
                class="absolute top-3 left-3 text-[10px] font-semibold px-2.5 py-1 rounded-full uppercase tracking-wide text-[#0A0A0A]"
                style="background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(4px)"
              >
                {{ project.buildingFunction || project.projectCategory }}
              </span>
              <span
                class="absolute top-3 right-3 text-[10px] font-semibold px-2.5 py-1 rounded-full uppercase tracking-wide"
                :class="statusBadgeClass(project.status)"
              >
                {{ formatStatus(project.status) }}
              </span>
            </div>
            <div class="p-4">
              <h3 class="text-[15px] font-semibold text-[#0A0A0A] mb-1 line-clamp-1" style="letter-spacing: -0.01em">
                {{ project.title }}
              </h3>
              <p v-if="project.location" class="text-[12px] text-[#888888] leading-snug line-clamp-1">
                📍 {{ project.location }}
              </p>
              <p v-if="project.budgetDisplay" class="text-[12px] font-semibold text-[#0A0A0A] mt-2">
                {{ formatBudget(project.budgetDisplay) }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Cara Kerja / How It Works -->
    <section class="py-24 bg-[#F5F5F5]">
      <div class="max-w-[1440px] mx-auto px-10">
        <div
          class="inline-flex items-center gap-2 px-3 py-1.5 bg-white border border-[#CCCCCC] rounded-full text-[11px] font-semibold mb-12"
        >
          {{ t.landing.howItWorks.eyebrow }}
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-10">
          <div v-for="(step, i) in HOW_IT_WORKS_KEYS" :key="i" class="relative">
            <div class="font-black leading-none mb-4 select-none" style="font-size: 64px; color: #e8e8e8">
              {{ String(i + 1).padStart(2, '0') }}
            </div>
            <h3 class="text-[20px] font-semibold text-[#0A0A0A] mb-3" style="letter-spacing: -0.02em">
              {{ t.landing.howItWorks[step.titleKey] }}
            </h3>
            <p class="text-[15px] text-[#666666] leading-relaxed">
              {{ t.landing.howItWorks[step.descKey] }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Dirancang untuk ketenangan pikiran / Why -->
    <section class="py-24 bg-white border-t border-[#E8E8E8]">
      <div class="max-w-[1440px] mx-auto px-10">
        <p class="text-[12px] font-semibold uppercase tracking-[0.05em] text-[#666666] mb-10">
          {{ t.landing.why.label }}
        </p>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="(card, i) in WHY_CARDS"
            :key="i"
            class="p-10 border border-[#CCCCCC] rounded-[20px] hover:-translate-y-1 transition-transform duration-200 flex flex-col gap-4"
          >
            <div class="w-12 h-12 bg-[#F5F5F5] rounded-xl flex items-center justify-center mb-6">
              <component :is="card.icon" class="w-6 h-6 text-[#0A0A0A]" />
            </div>
            <h3 class="text-[18px] font-semibold text-[#0A0A0A] mb-3" style="letter-spacing: -0.02em">
              {{ t.landing.why[card.titleKey] }}
            </h3>
            <p class="text-[14px] text-[#666666] leading-relaxed">
              {{ t.landing.why[card.descKey] }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="py-24 bg-[#F5F5F5]">
      <div class="max-w-[1440px] mx-auto px-10 text-center">
        <h2 class="font-semibold text-[#0A0A0A] mb-4 leading-[1.1]" style="font-size: 48px; letter-spacing: -0.03em">
          {{ t.landing.cta.title }}
        </h2>
        <p class="text-[18px] text-[#666666] font-light mb-10 max-w-lg mx-auto">
          {{ t.landing.cta.subline }}
        </p>
        <div class="flex items-center justify-center gap-4 flex-wrap">
          <router-link to="/signup">
            <button
              class="px-8 py-3 bg-[#0A0A0A] text-white rounded-full text-[15px] font-semibold hover:opacity-90 transition-all hover:-translate-y-px"
              style="box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08)"
            >
              {{ t.landing.cta.primary }}
            </button>
          </router-link>
          <router-link to="/signup?role=ARCHITECT">
            <button
              class="px-8 py-3 bg-white text-[#0A0A0A] border border-[#CCCCCC] rounded-full text-[15px] font-medium hover:border-[#0A0A0A] transition-colors"
            >
              {{ t.landing.cta.secondary }}
            </button>
          </router-link>
        </div>
      </div>
    </section>

    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ShieldCheck, Tag, Lock, MessageSquare, Layers, MapPin } from 'lucide-vue-next'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import HeroStepper from '@/components/landing/HeroStepper.vue'
import { useI18n } from '@/composables/useI18n'
import { projectAPI } from '@/services/api'

const { t } = useI18n()

const searchQuery = ref('')
const activeCategory = ref('semua')
const projects = ref([])
const isLoading = ref(true)

const CATEGORIES = [
  { value: 'semua', labelKey: 'filterAll' },
  { value: 'hunian', labelKey: 'filterHunian' },
  { value: 'komersil', labelKey: 'filterKomersil' },
  { value: 'industrial', labelKey: 'filterIndustrial' },
  { value: 'lainnya', labelKey: 'filterLainnya' }
]

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

const CATEGORY_MAP = {
  hunian: ['hunian', 'rumah', 'residential', 'villa', 'apartemen', 'apartment', 'townhouse'],
  komersil: [
    'komersil',
    'komersial',
    'commercial',
    'kantor',
    'office',
    'hotel',
    'mall',
    'retail',
    'kafe',
    'cafe',
    'restoran',
    'restaurant',
    'ruko'
  ],
  industrial: ['industri', 'industrial', 'pabrik', 'factory', 'gudang', 'warehouse', 'manufaktur']
}

const resolveCategory = project => {
  const haystack = `${project.buildingFunction || ''} ${project.projectCategory || ''}`.toLowerCase()
  for (const [cat, keywords] of Object.entries(CATEGORY_MAP)) {
    if (keywords.some(kw => haystack.includes(kw))) return cat
  }
  return 'lainnya'
}

const formatBudget = amount => {
  if (!amount) return null
  if (amount >= 1_000_000_000) return `Rp ${(amount / 1_000_000_000).toFixed(1).replace('.0', '')} M`
  if (amount >= 1_000_000) return `Rp ${(amount / 1_000_000).toFixed(0)} jt`
  return `Rp ${amount.toLocaleString('id-ID')}`
}

const formatStatus = status => {
  const map = { OPEN: 'Open', IN_PROGRESS: 'Berjalan', COMPLETED: 'Selesai' }
  return map[status] || status
}

const statusBadgeClass = status => {
  if (status === 'OPEN') return 'bg-green-100 text-green-800'
  if (status === 'IN_PROGRESS') return 'bg-blue-100 text-blue-800'
  if (status === 'COMPLETED') return 'bg-gray-100 text-gray-600'
  return 'bg-gray-100 text-gray-600'
}

const filteredProjects = computed(() => {
  let result = projects.value
  if (activeCategory.value !== 'semua') {
    result = result.filter(p => resolveCategory(p) === activeCategory.value)
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(
      p =>
        p.title?.toLowerCase().includes(q) ||
        p.location?.toLowerCase().includes(q) ||
        p.buildingFunction?.toLowerCase().includes(q)
    )
  }
  return result
})

onMounted(async () => {
  try {
    const res = await projectAPI.getPublicPreviews()
    projects.value = res.data?.data || []
  } catch {
    // silently fail — landing page works without projects
  } finally {
    isLoading.value = false
  }
})
</script>
