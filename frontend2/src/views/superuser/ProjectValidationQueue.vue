<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ t.projectValidationQueue.title }}</h1>
    <p class="text-gray-500 text-sm mb-8">{{ t.projectValidationQueue.subtitle }}</p>

    <div v-if="loading" class="space-y-3">
      <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-5 animate-pulse border border-gray-100">
        <div class="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
        <div class="h-3 bg-gray-100 rounded w-1/2"></div>
      </div>
    </div>

    <div v-else-if="projects.length === 0" class="bg-white rounded-xl border border-gray-100 p-12 text-center">
      <p class="text-gray-400 text-sm">{{ t.projectValidationQueue.noPending }}</p>
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="project in projects"
        :key="project.id"
        class="bg-white rounded-xl border border-gray-100 p-5 flex items-start justify-between gap-4"
      >
        <div class="flex-1 min-w-0">
          <p class="font-semibold text-gray-900 truncate">{{ project.title }}</p>
          <p class="text-sm text-gray-500 mt-0.5">
            {{ project.location }} · Budget IDR {{ formatCurrency(project.budgetTotal) }}
          </p>
          <p class="text-xs text-gray-400 mt-1">{{ t.projectValidationQueue.submittedOn }} {{ formatDate(project.createdAt) }}</p>
          <p v-if="project.validationNotes" class="text-xs text-gray-500 mt-1 italic">
            Note: {{ project.validationNotes }}
          </p>
        </div>
        <div class="flex gap-2 shrink-0">
          <button
            @click="validate(project.id, false)"
            :disabled="processing === project.id"
            class="px-3 py-1.5 text-xs font-semibold rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition disabled:opacity-50"
          >
            {{ t.projectValidationQueue.reject }}
          </button>
          <button
            @click="validate(project.id, true)"
            :disabled="processing === project.id"
            class="px-3 py-1.5 text-xs font-semibold rounded-lg bg-gray-900 text-white hover:bg-gray-700 transition disabled:opacity-50"
          >
            <span v-if="processing === project.id">...</span>
            <span v-else>{{ t.projectValidationQueue.approve }}</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="error" class="mt-4 bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminProjectsAPI, superuserProjectsAPI } from '@/services/adminApi'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const loading = ref(true)
const error = ref(null)
const processing = ref(null)
const projects = ref([])

const load = async () => {
  loading.value = true
  try {
    const res = await adminProjectsAPI.getAll('PENDING_APPROVAL')
    projects.value = res.data.data || []
  } catch (e) {
    error.value = 'Failed to load projects'
  } finally {
    loading.value = false
  }
}

const validate = async (projectId, isValid) => {
  processing.value = projectId
  error.value = null
  try {
    await superuserProjectsAPI.validate(projectId, isValid, null)
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Validation failed'
  } finally {
    processing.value = null
  }
}

const formatCurrency = v => (v ? Number(v).toLocaleString('id-ID') : '-')
const formatDate = d =>
  d ? new Date(d).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' }) : '-'

onMounted(load)
</script>
