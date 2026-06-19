<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ t.allProjectsAdmin.title }}</h1>
    <p class="text-gray-500 text-sm mb-6">{{ t.allProjectsAdmin.subtitle }}</p>

    <!-- Filters -->
    <div class="flex gap-2 mb-6 flex-wrap">
      <button
        v-for="s in statusFilters"
        :key="s.value"
        @click="
          selectedStatus = s.value
          load()
        "
        class="px-3 py-1.5 text-xs font-semibold rounded-lg transition"
        :class="
          selectedStatus === s.value
            ? 'bg-gray-900 text-white'
            : 'bg-white border border-gray-200 text-gray-600 hover:border-gray-400'
        "
      >
        {{ s.label }}
      </button>
    </div>

    <div v-if="loading" class="space-y-3">
      <div v-for="i in 5" :key="i" class="bg-white rounded-xl p-5 animate-pulse border border-gray-100">
        <div class="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
        <div class="h-3 bg-gray-100 rounded w-1/2"></div>
      </div>
    </div>

    <div v-else-if="projects.length === 0" class="bg-white rounded-xl border border-gray-100 p-12 text-center">
      <p class="text-gray-400 text-sm">{{ t.allProjectsAdmin.noProjects }}</p>
    </div>

    <div v-else class="space-y-3">
      <div v-for="project in projects" :key="project.id" class="bg-white rounded-xl border border-gray-100 p-5">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <p class="font-semibold text-gray-900 truncate">{{ project.title }}</p>
              <span class="text-xs px-2 py-0.5 rounded-full font-semibold" :class="statusColor(project.status)">
                {{ project.status }}
              </span>
            </div>
            <p class="text-sm text-gray-500">{{ project.location }} · IDR {{ formatCurrency(project.budgetTotal) }}</p>
            <p class="text-xs text-gray-400 mt-1">
              {{ project.bidCount || 0 }} bid(s) · Created {{ formatDate(project.createdAt) }}
            </p>
          </div>
          <div class="flex gap-2 shrink-0">
            <button
              v-if="project.status === 'NEGOTIATION'"
              @click="overrideNegotiation(project.id)"
              :disabled="processing === project.id"
              class="px-3 py-1.5 text-xs font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition disabled:opacity-50"
            >
              {{ t.allProjectsAdmin.forceInProgress }}
            </button>
            <button
              v-if="!['CANCELLED', 'COMPLETED'].includes(project.status)"
              @click="forceCancel(project.id)"
              :disabled="processing === project.id"
              class="px-3 py-1.5 text-xs font-semibold rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition disabled:opacity-50"
            >
              {{ t.allProjectsAdmin.forceCancel }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="error" class="mt-4">
      <BaseAlert variant="error">{{ error }}</BaseAlert>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminProjectsAPI } from '@/services/adminApi'
import { useI18n } from '@/composables/useI18n'
import BaseAlert from '@/components/ui/BaseAlert.vue'

const { t } = useI18n()

const loading = ref(true)
const error = ref(null)
const processing = ref(null)
const projects = ref([])
const selectedStatus = ref(null)

const statusFilters = computed(() => [
  { label: t.value.allProjectsAdmin.filterAll, value: null },
  { label: t.value.allProjectsAdmin.filterPendingApproval, value: 'PENDING_APPROVAL' },
  { label: t.value.allProjectsAdmin.filterOpen, value: 'OPEN' },
  { label: t.value.allProjectsAdmin.filterNegotiation, value: 'NEGOTIATION' },
  { label: t.value.allProjectsAdmin.filterInProgress, value: 'IN_PROGRESS' },
  { label: t.value.allProjectsAdmin.filterCompleted, value: 'COMPLETED' },
  { label: t.value.allProjectsAdmin.filterCancelled, value: 'CANCELLED' }
])

const load = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await adminProjectsAPI.getAll(selectedStatus.value)
    projects.value = res.data.data || []
  } catch (e) {
    error.value = 'Failed to load projects'
  } finally {
    loading.value = false
  }
}

const forceCancel = async id => {
  if (!confirm(t.value.allProjectsAdmin.forceCancelConfirm)) return
  processing.value = id
  try {
    await adminProjectsAPI.forceCancel(id)
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to cancel project'
  } finally {
    processing.value = null
  }
}

const overrideNegotiation = async id => {
  if (!confirm(t.value.allProjectsAdmin.forceProgressConfirm)) return
  processing.value = id
  try {
    await adminProjectsAPI.overrideNegotiation(id)
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Override failed'
  } finally {
    processing.value = null
  }
}

const statusColor = s =>
  ({
    PENDING_APPROVAL: 'bg-amber-100 text-amber-700',
    OPEN: 'bg-green-100 text-green-700',
    NEGOTIATION: 'bg-blue-100 text-blue-700',
    IN_PROGRESS: 'bg-purple-100 text-purple-700',
    COMPLETED: 'bg-gray-100 text-gray-600',
    CANCELLED: 'bg-red-100 text-red-600',
    REJECTED: 'bg-red-100 text-red-600',
    BIDDING_CLOSED: 'bg-orange-100 text-orange-700'
  })[s] || 'bg-gray-100 text-gray-600'

const formatCurrency = v => (v ? Number(v).toLocaleString('id-ID') : '-')
const formatDate = d =>
  d ? new Date(d).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' }) : '-'

onMounted(load)
</script>
