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
          <p class="text-xs text-gray-400 mt-1">
            {{ t.projectValidationQueue.submittedOn }} {{ formatDate(project.createdAt) }}
          </p>
          <p v-if="project.validationNotes" class="text-xs text-gray-500 mt-1 italic">
            Note: {{ project.validationNotes }}
          </p>
        </div>
        <div class="flex gap-2 shrink-0">
          <router-link
            :to="{ name: 'SuperuserProjectDetail', params: { id: project.id } }"
            class="px-3 py-1.5 text-xs font-semibold rounded-lg border border-gray-200 text-gray-600 hover:border-gray-400 transition"
          >
            {{ t.projectValidationQueue.viewDetails }}
          </router-link>
          <button
            :disabled="processing === project.id"
            class="px-3 py-1.5 text-xs font-semibold rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition disabled:opacity-50"
            @click="openRejectModal(project.id)"
          >
            {{ t.projectValidationQueue.reject }}
          </button>
          <button
            :disabled="processing === project.id"
            class="px-3 py-1.5 text-xs font-semibold rounded-lg bg-gray-900 text-white hover:bg-gray-700 transition disabled:opacity-50"
            @click="validate(project.id, true, null)"
          >
            <span v-if="processing === project.id">...</span>
            <span v-else>{{ t.projectValidationQueue.approve }}</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="error" class="mt-4 bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm">{{ error }}</div>

    <div v-if="rejectModalProjectId !== null" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeRejectModal"></div>
      <div class="relative min-h-screen flex items-center justify-center p-4">
        <div class="relative bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
          <h3 class="text-lg font-bold text-gray-900 mb-1">{{ t.projectValidationQueue.rejectModalTitle }}</h3>
          <p class="text-sm text-gray-500 mb-4">{{ t.projectValidationQueue.rejectModalHint }}</p>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            {{ t.projectValidationQueue.rejectReasonLabel }}
          </label>
          <textarea
            v-model="rejectReason"
            rows="4"
            :placeholder="t.projectValidationQueue.rejectReasonPlaceholder"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-red-400 focus:border-red-400 outline-none text-sm"
          />
          <div class="flex gap-3 mt-5 justify-end">
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-gray-600 rounded-lg hover:bg-gray-100 transition"
              @click="closeRejectModal"
            >
              {{ t.projectValidationQueue.rejectModalCancel }}
            </button>
            <button
              type="button"
              :disabled="!rejectReason.trim() || processing === rejectModalProjectId"
              class="px-4 py-2 text-sm font-semibold text-white bg-red-600 rounded-lg hover:bg-red-700 transition disabled:opacity-50"
              @click="confirmReject"
            >
              {{ t.projectValidationQueue.rejectModalConfirm }}
            </button>
          </div>
        </div>
      </div>
    </div>
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
const rejectModalProjectId = ref(null)
const rejectReason = ref('')

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

const openRejectModal = projectId => {
  rejectModalProjectId.value = projectId
  rejectReason.value = ''
}

const closeRejectModal = () => {
  rejectModalProjectId.value = null
  rejectReason.value = ''
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) return
  await validate(rejectModalProjectId.value, false, rejectReason.value.trim())
  closeRejectModal()
}

const validate = async (projectId, isValid, validationNotes) => {
  processing.value = projectId
  error.value = null
  try {
    await superuserProjectsAPI.validate(projectId, isValid, validationNotes)
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
