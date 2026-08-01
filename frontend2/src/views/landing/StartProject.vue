<template>
  <div class="min-h-screen bg-white overflow-x-hidden flex flex-col">
    <Navbar />

    <!-- Header -->
    <section class="max-w-[820px] mx-auto px-10 pt-16 pb-8 text-center">
      <h1 class="text-section-h1 text-ink-900 mb-3">{{ t.mulaiProyek.title }}</h1>
      <p class="text-body text-ink-500 mb-5">{{ t.mulaiProyek.subline }}</p>
      <button
        type="button"
        class="inline-flex items-center gap-2 bg-white text-ink-900 border-2 border-ink-200 rounded-full px-6 py-3 text-caption font-bold cursor-pointer hover:border-ink-900 transition-colors"
        @click="formMode = !formMode"
      >
        <PencilLine class="w-4 h-4" />
        {{ formMode ? t.mulaiProyek.toggleToBrowse : t.mulaiProyek.toggleToForm }}
      </button>
    </section>

    <section class="max-w-[1440px] mx-auto px-10 pb-24 w-full">
      <!-- Search + chips (shared between modes, matches Home) -->
      <div class="mb-5">
        <SearchInput v-model="catalog.search" :placeholder="t.landing.jelajahi.searchPlaceholder" />
      </div>
      <div class="flex justify-center gap-2.5 flex-wrap mb-8">
        <CategoryChip
          v-for="cat in CATEGORIES"
          :key="cat.value"
          :label="t.landing.jelajahi[cat.labelKey]"
          :active="catalog.category === cat.value"
          @click="catalog.setCategory(cat.value)"
        />
      </div>

      <!-- Form mode -->
      <div
        v-if="formMode"
        class="max-w-[640px] mx-auto mb-20 bg-white border border-hairline rounded-card p-10 shadow-soft"
      >
        <h3 class="text-h3 text-ink-900 mb-5">{{ t.mulaiProyek.form.heading }}</h3>
        <div class="flex flex-col gap-5">
          <div>
            <label class="block text-micro font-bold uppercase tracking-[0.06em] text-ink-500 mb-2">
              {{ t.mulaiProyek.form.projectType }}
            </label>
            <div class="flex flex-wrap gap-2">
              <CategoryChip
                v-for="cat in PROJECT_TYPE_OPTIONS"
                :key="cat.value"
                :label="t.landing.jelajahi[cat.labelKey]"
                :active="form.projectTypes.includes(cat.value)"
                @click="toggleInArray(form.projectTypes, cat.value)"
              />
            </div>
          </div>
          <div>
            <label class="block text-micro font-bold uppercase tracking-[0.06em] text-ink-500 mb-2">
              {{ t.mulaiProyek.form.designStyle }}
            </label>
            <div class="flex flex-wrap gap-2">
              <CategoryChip
                v-for="style in DESIGN_STYLE_OPTIONS"
                :key="style.value"
                :label="t.mulaiProyek.form.styles[style.labelKey]"
                :active="form.designStyles.includes(style.value)"
                @click="toggleInArray(form.designStyles, style.value)"
              />
            </div>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <BaseInput
              v-model="form.location"
              :label="t.mulaiProyek.form.location"
              :placeholder="t.mulaiProyek.form.locationPlaceholder"
            />
            <BaseInput
              v-model="form.budget"
              :label="t.mulaiProyek.form.budget"
              :placeholder="t.mulaiProyek.form.budgetPlaceholder"
            />
          </div>
          <BaseInput
            v-model="form.contact"
            :label="t.mulaiProyek.form.contact"
            :placeholder="t.mulaiProyek.form.contactPlaceholder"
          />
          <BaseButton v-if="!submitted" full-width @click="submitForm">{{ t.mulaiProyek.form.submit }}</BaseButton>
          <p v-else class="text-center text-body-sm font-semibold text-brand-green py-3">
            {{ t.mulaiProyek.form.submitted }}
          </p>
        </div>
      </div>

      <!-- Browse mode -->
      <template v-else>
        <div v-if="isLoading" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4">
          <div
            v-for="i in 8"
            :key="i"
            class="rounded-card overflow-hidden border border-hairline bg-white animate-pulse"
          >
            <div class="bg-gray-200" style="aspect-ratio: 1/1"></div>
            <div class="p-3 space-y-2">
              <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              <div class="h-3 bg-gray-200 rounded w-3/4"></div>
            </div>
          </div>
        </div>
        <div v-else-if="filteredProjects.length === 0" class="py-20 text-center">
          <p class="text-body-sm text-ink-300">{{ t.landing.jelajahi.emptyState }}</p>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4">
          <ImageCard v-for="project in filteredProjects" :key="project.id" :project="project" />
          <AddDesignTile :label="t.landing.jelajahi.addDesign" @click="formMode = true" />
        </div>
      </template>
    </section>

    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { PencilLine } from 'lucide-vue-next'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import SearchInput from '@/components/landing/SearchInput.vue'
import CategoryChip from '@/components/landing/CategoryChip.vue'
import ImageCard from '@/components/landing/ImageCard.vue'
import AddDesignTile from '@/components/landing/AddDesignTile.vue'
import { useI18n } from '@/composables/useI18n'
import { projectAPI } from '@/services/api'
import { useCatalogStore } from '@/stores/catalog'
import { CATEGORIES, filterProjects } from '@/utils/catalogFormat'

const { t } = useI18n()
const catalog = useCatalogStore()

const formMode = ref(false)
const projects = ref([])
const isLoading = ref(true)

const PROJECT_TYPE_OPTIONS = CATEGORIES.filter(c => c.value !== 'semua')
const DESIGN_STYLE_OPTIONS = [
  { value: 'modern', labelKey: 'modern' },
  { value: 'minimalis', labelKey: 'minimalis' },
  { value: 'tropis', labelKey: 'tropis' },
  { value: 'industrial', labelKey: 'industrial' },
  { value: 'klasik', labelKey: 'klasik' }
]

const form = reactive({
  projectTypes: [],
  designStyles: [],
  location: '',
  budget: '',
  contact: ''
})
const submitted = ref(false)

function toggleInArray(arr, value) {
  const i = arr.indexOf(value)
  if (i === -1) arr.push(value)
  else arr.splice(i, 1)
}

function submitForm() {
  // No backend endpoint exists yet for the manual brief flow — acknowledge locally.
  submitted.value = true
}

const filteredProjects = computed(() =>
  filterProjects(projects.value, { search: catalog.search, category: catalog.category })
)

onMounted(async () => {
  try {
    const res = await projectAPI.getPublicPreviews()
    projects.value = res.data?.data || []
  } catch {
    // silently fail — page still works without projects
  } finally {
    isLoading.value = false
  }
})
</script>
