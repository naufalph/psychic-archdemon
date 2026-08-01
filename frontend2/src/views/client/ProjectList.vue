<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-3xl font-bold text-black">{{ t.clientDashboard.myProjects }}</h2>
        <p class="text-gray-500 mt-1">{{ t.clientDashboard.myProjectsSubtitle }}</p>
      </div>
      <router-link :to="{ name: 'ProjectCreate' }">
        <button
          class="bg-brand-brown hover:bg-black text-white px-6 py-3 rounded-full font-medium transition flex items-center gap-2"
        >
          <Plus :size="20" />
          {{ t.clientDashboard.newProject }}
        </button>
      </router-link>
    </div>

    <div v-if="loading" class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
      <div v-for="n in 6" :key="n" class="bg-white rounded-3xl border border-gray-200 p-8 animate-pulse">
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
      <button @click="fetchProjects" class="text-brand-brown hover:underline">Try again</button>
    </div>

    <div v-else-if="projects.length === 0" class="text-center py-20">
      <Building2 :size="64" class="text-gray-300 mx-auto mb-4" />
      <h3 class="text-xl font-bold text-gray-900 mb-2">{{ t.clientDashboard.noProjectsYet }}</h3>
      <p class="text-gray-500 mb-6">{{ t.clientDashboard.noProjectsMessage }}</p>
      <router-link :to="{ name: 'ProjectCreate' }">
        <button class="bg-brand-brown hover:bg-black text-white px-6 py-3 rounded-full font-medium transition">
          {{ t.clientDashboard.createProject }}
        </button>
      </router-link>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
      <ProjectCard v-for="project in projects" :key="project.id" :project="project" variant="client" />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/composables/useI18n'
import { Plus, Building2 } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import ProjectCard from '@/components/project/ProjectCard.vue'

const { t } = useI18n()
const projectsStore = useProjectsStore()
const { projects, loading, error } = storeToRefs(projectsStore)

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
