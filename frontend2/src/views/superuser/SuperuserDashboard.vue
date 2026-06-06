<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ t.superuserDashboard.title }}</h1>
    <p class="text-gray-500 text-sm mb-8">{{ t.superuserDashboard.subtitle }}</p>

    <div v-if="loading" class="grid grid-cols-4 gap-4">
      <div v-for="i in 4" :key="i" class="bg-white rounded-xl p-6 animate-pulse">
        <div class="h-4 bg-gray-200 rounded w-3/4 mb-3"></div>
        <div class="h-8 bg-gray-200 rounded w-1/2"></div>
      </div>
    </div>

    <div v-else class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <RouterLink
        to="/superuser/projects/queue"
        class="bg-white rounded-xl p-6 border border-gray-100 hover:shadow-md transition-shadow cursor-pointer"
      >
        <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">{{ t.superuserDashboard.pendingApproval }}</p>
        <p class="text-3xl font-bold text-amber-500">{{ stats.pendingApproval }}</p>
        <p class="text-xs text-gray-400 mt-1">{{ t.superuserDashboard.pendingApprovalDesc }}</p>
      </RouterLink>

      <RouterLink
        to="/superuser/projects"
        class="bg-white rounded-xl p-6 border border-gray-100 hover:shadow-md transition-shadow cursor-pointer"
      >
        <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">{{ t.superuserDashboard.negotiation }}</p>
        <p class="text-3xl font-bold text-blue-500">{{ stats.negotiation }}</p>
        <p class="text-xs text-gray-400 mt-1">{{ t.superuserDashboard.negotiationDesc }}</p>
      </RouterLink>

      <RouterLink
        to="/superuser/disputes"
        class="bg-white rounded-xl p-6 border border-gray-100 hover:shadow-md transition-shadow cursor-pointer"
      >
        <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">{{ t.superuserDashboard.activeDisputes }}</p>
        <p class="text-3xl font-bold text-red-500">{{ stats.disputes }}</p>
        <p class="text-xs text-gray-400 mt-1">{{ t.superuserDashboard.activeDisputesDesc }}</p>
      </RouterLink>

      <RouterLink
        to="/superuser/projects"
        class="bg-white rounded-xl p-6 border border-gray-100 hover:shadow-md transition-shadow cursor-pointer"
      >
        <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">{{ t.superuserDashboard.inProgress }}</p>
        <p class="text-3xl font-bold text-green-500">{{ stats.inProgress }}</p>
        <p class="text-xs text-gray-400 mt-1">{{ t.superuserDashboard.inProgressDesc }}</p>
      </RouterLink>
    </div>

    <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminProjectsAPI, adminPhasesAPI } from '@/services/adminApi'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const loading = ref(true)
const error = ref(null)
const stats = ref({ pendingApproval: 0, negotiation: 0, disputes: 0, inProgress: 0 })

onMounted(async () => {
  try {
    const [projectsRes, disputesRes] = await Promise.all([adminProjectsAPI.getAll(), adminPhasesAPI.getDisputed()])
    const projects = projectsRes.data.data || []
    stats.value = {
      pendingApproval: projects.filter(p => p.status === 'PENDING_APPROVAL').length,
      negotiation: projects.filter(p => p.status === 'NEGOTIATION').length,
      inProgress: projects.filter(p => p.status === 'IN_PROGRESS').length,
      disputes: (disputesRes.data.data || []).length
    }
  } catch (e) {
    error.value = 'Failed to load dashboard data'
  } finally {
    loading.value = false
  }
})
</script>
