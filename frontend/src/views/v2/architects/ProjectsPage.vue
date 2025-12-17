<template>
  <div class="min-h-screen bg-white">
    <div class="container mx-auto px-15">
      <div class="flex flex-col gap-3">
        <div class="flex justify-between items-center py-4 px-15">
          <div class="flex items-center gap-8">
            <h1 class="text-xl font-bold">LOGO</h1>
            <nav class="hidden md:flex items-center gap-8">
              <button class="text-lg">{{ $t('nav.home') }}</button>
              <button class="text-lg">{{ $t('nav.architect') }}</button>
              <div class="relative">
                <button class="text-lg flex items-center gap-2">
                  {{ $t('nav.explore') }}
                  <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M7 10l5 5 5-5z" />
                  </svg>
                </button>
              </div>
            </nav>
          </div>
          <div class="flex items-center gap-3">
            <button
              class="px-4 py-2 border border-[#C5A25A] text-[#C5A25A] rounded-lg hover:bg-[#C5A25A] hover:text-white transition-colors"
            >
              {{ $t('nav.becomeArchitect') }}
            </button>
            <button
              class="px-4 py-2 border border-[#C5A25A] text-[#C5A25A] rounded-lg hover:bg-[#C5A25A] hover:text-white transition-colors"
            >
              {{ $t('nav.makeProject') }}
            </button>
            <button class="px-4 py-2 bg-[#C5A25A] text-white rounded-lg hover:bg-[#a38842] transition-colors">
              {{ $t('nav.signIn') }}
            </button>
          </div>
        </div>

        <h2 class="text-4xl font-bold px-15">{{ $t('projects.search') }}</h2>

        <div class="flex items-center gap-6 px-15 py-3">
          <div
            class="flex-1 flex items-center gap-2 bg-[#4A6073] bg-opacity-75 border border-[#707070] rounded-xl px-3 py-4"
          >
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 32 32">
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
              :placeholder="$t('projects.searchPlaceholder')"
              class="flex-1 bg-transparent text-white placeholder-white placeholder-opacity-50 outline-none"
              @keyup.enter="handleSearch"
            />
          </div>
          <button
            class="px-6 py-4 bg-[#C5A25A] text-white rounded-lg text-lg font-semibold hover:bg-[#a38842] transition-colors"
            @click="handleSearch"
          >
            {{ $t('projects.searchButton') }}
          </button>
        </div>

        <div class="flex justify-between items-center px-15">
          <h3 class="text-4xl font-bold">{{ $t('projects.latestResults') }}</h3>
          <div class="flex items-center gap-5">
            <span class="text-2xl font-medium">{{ $t('projects.sortBy') }}</span>
            <div class="relative">
              <button
                class="flex items-center gap-6 border border-black rounded-xl px-3 py-2 min-w-[120px]"
                @click="toggleSortDropdown"
              >
                <span class="text-2xl font-medium">{{ getSortLabel(sortBy) }}</span>
                <svg class="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M7 10l5 5 5-5z" />
                </svg>
              </button>
              <div
                v-if="showSortDropdown"
                class="absolute right-0 mt-2 bg-white border border-gray-300 rounded-lg shadow-lg z-10"
              >
                <button
                  v-for="option in sortOptions"
                  :key="option.value"
                  class="block w-full text-left px-4 py-2 hover:bg-gray-100"
                  @click="selectSort(option.value)"
                >
                  {{ option.label }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <hr class="border-black mx-15" />

        <div v-if="loading" class="flex justify-center items-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-[#C5A25A]"></div>
        </div>

        <div v-else-if="error" class="text-center py-12 px-15">
          <p class="text-red-600 text-xl">{{ error }}</p>
        </div>

        <div v-else-if="projects.length === 0" class="text-center py-12 px-15">
          <p class="text-gray-500 text-xl">{{ $t('projects.noProjects') }}</p>
        </div>

        <div v-else class="grid grid-cols-1 gap-4 px-15 pb-8">
          <ProjectCard
            v-for="project in projects"
            :key="project.id"
            :project="project"
            :entry-count="getEntryCount(project)"
            :is-featured="isFeatured(project)"
            :is-guaranteed="isGuaranteed(project)"
            :is-urgent="isUrgent(project)"
            :rating="getRating(project)"
            :comment-count="getCommentCount(project)"
            @click="navigateToProject(project.id)"
          />
        </div>
      </div>
    </div>

    <footer class="bg-[#0A0A0A] text-white py-16 px-15 mt-12">
      <div class="container mx-auto">
        <div class="flex justify-between gap-6 mb-4">
          <div class="flex flex-col gap-4">
            <h2 class="text-2xl font-bold">Logo</h2>
            <div class="flex gap-4">
              <button class="w-10 h-10 bg-gray-700 rounded-full"></button>
              <button class="w-10 h-10 bg-gray-700 rounded-full"></button>
              <button class="w-10 h-10 bg-gray-700 rounded-full"></button>
              <button class="w-10 h-10 bg-gray-700 rounded-full"></button>
            </div>
          </div>
          <div class="flex flex-col gap-2">
            <h3 class="text-2xl font-bold">Category</h3>
            <div class="text-sm space-y-1">
              <p>{{ $t('footer.home') }}</p>
              <p>{{ $t('footer.architect') }}</p>
              <p>{{ $t('footer.explore') }}</p>
              <p>{{ $t('footer.customerStories') }}</p>
              <p>{{ $t('footer.article') }}</p>
              <p>{{ $t('footer.faqs') }}</p>
              <p>{{ $t('footer.knowledge') }}</p>
              <p>{{ $t('footer.tools') }}</p>
            </div>
          </div>
          <div class="flex flex-col gap-2">
            <h3 class="text-2xl font-bold">Company</h3>
            <div class="text-sm space-y-1">
              <p>{{ $t('footer.aboutUs') }}</p>
              <p>{{ $t('footer.career') }}</p>
              <p>{{ $t('footer.contactUs') }}</p>
            </div>
          </div>
          <div class="flex flex-col gap-2">
            <h3 class="text-2xl font-bold">Contact Ass</h3>
            <div class="text-sm space-y-1">
              <p>{{ $t('footer.email') }}</p>
              <p>{{ $t('footer.address') }}</p>
            </div>
          </div>
        </div>
        <hr class="border-gray-700 my-4" />
        <div class="flex justify-center gap-4 text-xs">
          <span>{{ $t('footer.termsConditions') }}</span>
          <span>|</span>
          <span>{{ $t('footer.policy') }}</span>
          <span>|</span>
          <span>{{ $t('footer.privacy') }}</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { projectAPI } from '@/services/api'
import ProjectCard from './components/ProjectCard.vue'

const { t } = useI18n()
const router = useRouter()

const searchQuery = ref('')
const sortBy = ref('createdAt')
const sortDirection = ref('desc')
const showSortDropdown = ref(false)
const projects = ref([])
const loading = ref(false)
const error = ref(null)

const sortOptions = [
  { value: 'createdAt', label: t('projects.sortLatest') },
  { value: 'budgetMax', label: t('projects.sortBudgetHigh') },
  { value: 'budgetMin', label: t('projects.sortBudgetLow') }
]

const fetchProjects = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await projectAPI.getOpenProjects({
      sortBy: sortBy.value,
      sortDirection: sortDirection.value
    })
    projects.value = response.data.data || []
  } catch (err) {
    console.error('Error fetching projects:', err)
    error.value = t('projects.errorFetching')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchProjects()
}

const toggleSortDropdown = () => {
  showSortDropdown.value = !showSortDropdown.value
}

const selectSort = value => {
  sortBy.value = value
  if (value === 'budgetMax') {
    sortDirection.value = 'desc'
  } else if (value === 'budgetMin') {
    sortDirection.value = 'asc'
  } else {
    sortDirection.value = 'desc'
  }
  showSortDropdown.value = false
  fetchProjects()
}

const getSortLabel = value => {
  const option = sortOptions.find(opt => opt.value === value)
  return option ? option.label : t('projects.sortLatest')
}

const getEntryCount = project => {
  return Math.floor(Math.random() * 100) + 1
}

const isFeatured = project => {
  return Math.random() > 0.5
}

const isGuaranteed = project => {
  return Math.random() > 0.5
}

const isUrgent = project => {
  const now = new Date()
  const created = new Date(project.createdAt)
  const diffHours = (now - created) / (1000 * 60 * 60)
  return diffHours < 24
}

const getRating = project => {
  return Math.random() * 2 + 3
}

const getCommentCount = project => {
  return Math.floor(Math.random() * 50)
}

const navigateToProject = projectId => {
  router.push({ name: 'ProjectDetail', params: { id: projectId } })
}

onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
