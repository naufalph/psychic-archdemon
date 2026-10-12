<template>
  <div class="space-y-6">
    <div v-if="!embedded" class="flex justify-between items-center">
      <div>
        <h2 class="text-3xl font-bold text-black">{{ t.clientDashboard.myProjects }}</h2>
        <p class="text-gray-500 mt-1">{{ t.clientDashboard.myProjectsSubtitle }}</p>
      </div>
      <router-link :to="{ name: 'ProjectBrief', query: { baru: '1' } }">
        <button
          class="bg-ink-900 hover:bg-ink-500 text-white px-6 py-3 rounded-full font-medium transition flex items-center gap-2"
        >
          <Plus :size="20" />
          {{ t.clientDashboard.newProject }}
        </button>
      </router-link>
    </div>

    <div v-if="loading" :class="gridClass">
      <div v-for="n in embedded ? 2 : 6" :key="n" class="bg-white rounded-3xl border border-gray-200 p-8 animate-pulse">
        <div class="h-6 bg-gray-200 rounded w-3/4 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/2 mb-6" />
        <div class="h-20 bg-gray-200 rounded mb-6" />
        <div class="flex gap-2">
          <div class="h-8 bg-gray-200 rounded w-20" />
          <div class="h-8 bg-gray-200 rounded w-20" />
        </div>
      </div>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <p class="text-red-600 mb-4">{{ error }}</p>
      <button class="text-ink-900 hover:underline" @click="fetchProjects">Try again</button>
    </div>

    <p
      v-else-if="embedded && shownProjects.length === 0"
      class="bg-white border border-hairline rounded-[20px] px-6 py-10 text-center text-[15px] text-ink-400 m-0"
    >
      {{ t.clientDashboard.noSubmittedProjects }}
    </p>

    <div v-else-if="shownProjects.length === 0" class="text-center py-20">
      <Building2 :size="64" class="text-gray-300 mx-auto mb-4" />
      <h3 class="text-xl font-bold text-gray-900 mb-2">{{ t.clientDashboard.noProjectsYet }}</h3>
      <p class="text-gray-500 mb-6">{{ t.clientDashboard.noProjectsMessage }}</p>
      <router-link :to="{ name: 'ProjectBrief', query: { baru: '1' } }">
        <button class="bg-ink-900 hover:bg-ink-500 text-white px-6 py-3 rounded-full font-medium transition">
          {{ t.clientDashboard.createProject }}
        </button>
      </router-link>
    </div>

    <div v-else :class="gridClass">
      <ProjectCard v-for="project in shownProjects" :key="project.id" :project="project" variant="client" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/composables/useI18n'
import { Plus, Building2 } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import ProjectCard from '@/components/project/ProjectCard.vue'

// On the dashboard the list sits beside the activity column, under the draft card that already
// covers unsent briefs, so it shows only the latest submitted projects without its own heading
const props = defineProps({
  embedded: { type: Boolean, default: false },
  limit: { type: Number, default: 0 }
})

const { t } = useI18n()
const projectsStore = useProjectsStore()
const { projects, loading, error } = storeToRefs(projectsStore)

const shownProjects = computed(() => {
  if (!props.embedded) return projects.value
  const submitted = [...projects.value]
    .filter(p => p.status !== 'DRAFT')
    .sort((a, b) => new Date(b.updatedAt || b.createdAt || 0) - new Date(a.updatedAt || a.createdAt || 0))
  return props.limit ? submitted.slice(0, props.limit) : submitted
})

const gridClass = computed(() =>
  props.embedded ? 'grid grid-cols-1 sm:grid-cols-2 gap-5' : 'grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6'
)

const fetchProjects = async () => {
  try {
    await projectsStore.fetchMyProjects()
  } catch (err) {
    console.error('Failed to fetch projects:', err)
  }
}

onMounted(() => {
  fetchProjects()
})
</script>
