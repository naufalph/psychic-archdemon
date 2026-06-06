<template>
  <div class="min-h-full bg-[#F4F5F7]">
    <div class="max-w-7xl mx-auto px-6 py-10">
      <div class="flex justify-between items-center mb-10">
        <div>
          <h1 class="text-4xl font-bold text-black">{{ t.clientDashboard.welcome }}, {{ clientName }}</h1>
          <p class="text-gray-500 mt-1">{{ t.clientDashboard.manageProjects }}</p>
        </div>
        <router-link :to="{ name: 'ProjectCreate' }">
          <button
            class="bg-black text-white px-6 py-3 rounded-full font-bold tracking-widest text-sm hover:bg-[#7C4728] transition flex items-center gap-2"
          >
            <Plus :size="16" />
            {{ t.clientDashboard.newProjectBtn }}
          </button>
        </router-link>
      </div>

      <div class="grid grid-cols-3 gap-4 mb-10">
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">{{ t.clientDashboard.activeProjects }}</p>
          <p class="text-4xl font-bold text-black">{{ activeProjectCount }}</p>
        </div>
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">{{ t.clientDashboard.totalProposals }}</p>
          <p class="text-4xl font-bold text-[#7C4728]">{{ totalProposalCount }}</p>
        </div>
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">{{ t.clientDashboard.budgetUsed }}</p>
          <p class="text-2xl font-bold text-black">{{ formatCurrency(budgetUsed) }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-bold text-black tracking-wide">{{ t.clientDashboard.activeVentures }}</h2>
            <router-link
              :to="{ name: 'ProjectCreate' }"
              class="text-xs font-bold text-gray-500 hover:text-black tracking-widest transition"
            >
              {{ t.clientDashboard.viewAll }}
            </router-link>
          </div>
          <ProjectList />
        </div>

        <div class="space-y-6">
          <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft">
            <h2 class="text-sm font-bold text-black tracking-widest uppercase mb-4">{{ t.clientDashboard.recentActivity }}</h2>
            <div v-if="recentBids.length === 0" class="text-center py-6">
              <p class="text-gray-400 text-sm">{{ t.clientDashboard.noProposalsYet }}</p>
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="bid in recentBids"
                :key="bid.id"
                class="flex items-start gap-3 pb-3 border-b border-gray-100 last:border-0 last:pb-0"
              >
                <div class="w-8 h-8 rounded-full bg-[#F5E6D3] flex items-center justify-center flex-shrink-0 mt-0.5">
                  <FileText :size="14" class="text-[#7C4728]" />
                </div>
                <div class="min-w-0">
                  <p class="text-sm font-medium text-gray-900 truncate">
                    {{ bid.architectName || 'An architect' }}
                  </p>
                  <p class="text-xs text-gray-500">{{ t.clientDashboard.submittedProposal }}</p>
                  <p class="text-xs text-[#7C4728] font-medium mt-0.5">{{ formatCurrency(bid.bidAmount) }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-[#7C4728] rounded-2xl p-6 text-white">
            <p class="text-xs font-bold tracking-widest uppercase mb-2 text-white/70">{{ t.clientDashboard.curatedSelection }}</p>
            <h3 class="text-lg font-bold mb-2">{{ t.clientDashboard.findArchitect }}</h3>
            <p class="text-sm text-white/80 mb-4">{{ t.clientDashboard.browseStudiosDesc }}</p>
            <button
              class="bg-white text-[#7C4728] px-4 py-2 rounded-full text-xs font-bold tracking-wider hover:bg-[#F5E6D3] transition"
            >
              {{ t.clientDashboard.browseStudios }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { Plus, FileText } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import { useI18n } from '@/composables/useI18n'
import ProjectList from './ProjectList.vue'

const { t } = useI18n()
const authStore = useAuthStore()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const { projects } = storeToRefs(projectsStore)
const { projectBids } = storeToRefs(bidsStore)

const clientName = computed(() => {
  const user = authStore.user
  if (!user) return 'Client'
  return user.name || user.email?.split('@')[0] || 'Client'
})

const activeProjectCount = computed(
  () => projects.value.filter(p => p.status === 'OPEN' || p.status === 'IN_PROGRESS').length
)

const totalProposalCount = computed(() => projectBids.value?.length || 0)

const budgetUsed = computed(() => {
  return projects.value
    .filter(p => p.status === 'AWARDED' || p.status === 'IN_PROGRESS')
    .reduce((sum, p) => sum + (p.designBudgetMax || 0), 0)
})

const recentBids = computed(() => {
  return (projectBids.value || []).slice(0, 5)
})

const formatCurrency = value => {
  if (!value) return 'Rp 0'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

onMounted(async () => {
  authStore.updateLastLoginRole('CLIENT')
  await projectsStore.fetchMyProjects()
})
</script>
