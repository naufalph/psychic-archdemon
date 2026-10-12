<template>
  <div class="min-h-full bg-surface-alt">
    <div class="max-w-7xl mx-auto px-6 py-10">
      <section
        class="rounded-[20px] border border-hairline p-6 md:p-8 mb-6 flex flex-col md:flex-row md:items-center gap-6 md:gap-10 bg-cover bg-no-repeat"
        :style="{ backgroundImage: `url(${welcomeBg})`, backgroundPosition: 'left top' }"
      >
        <div class="flex-1 min-w-0">
          <h1 class="text-[clamp(28px,3vw,40px)] font-bold tracking-[-.03em] leading-[1.1] text-ink-900 m-0">
            {{ t.clientDashboard.welcome }}, {{ clientName }}
          </h1>
          <p class="text-body-lg text-ink-700 mt-2 mb-0">{{ t.clientDashboard.manageProjects }}</p>
        </div>
        <router-link
          :to="{ name: 'ProjectBrief', query: { baru: '1' } }"
          class="shrink-0 self-start md:self-auto inline-flex items-center gap-2 h-[52px] px-7 rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity"
        >
          <Plus :size="18" :stroke-width="2.4" /> {{ t.clientDashboard.newProject }}
        </router-link>
      </section>

      <QuickActions :drafts="drafts" :profile="clientProfileStore.profile" class="mb-10" />

      <div class="grid grid-cols-3 gap-4 mb-10">
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">
            {{ t.clientDashboard.activeProjects }}
          </p>
          <p class="text-4xl font-bold text-black">{{ activeProjectCount }}</p>
        </div>
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">
            {{ t.clientDashboard.totalBids }}
          </p>
          <p class="text-4xl font-bold text-ink-900">{{ totalBidCount }}</p>
        </div>
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">
            {{ t.clientDashboard.budgetUsed }}
          </p>
          <p class="text-2xl font-bold text-black">{{ formatCurrency(budgetUsed) }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2">
          <div class="flex justify-between items-baseline gap-4 mb-5">
            <h2 class="text-[22px] font-bold tracking-[-.02em] text-ink-900 m-0">{{ t.clientDashboard.myProjects }}</h2>
            <router-link
              to="/client/projects"
              class="inline-flex items-center gap-1.5 text-caption font-semibold text-ink-900 hover:opacity-60 transition-opacity"
            >
              {{ t.clientDashboard.seeAll }} <ChevronRight :size="16" :stroke-width="2.4" />
            </router-link>
          </div>
          <ProjectList embedded :limit="4" />
        </div>

        <div class="space-y-6">
          <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft">
            <h2 class="text-sm font-bold text-black tracking-widest uppercase mb-4">
              {{ t.clientDashboard.recentActivity }}
            </h2>
            <div v-if="recentActivity.length === 0" class="text-center py-6">
              <p class="text-gray-400 text-sm">{{ t.clientDashboard.noActivityYet }}</p>
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="notification in recentActivity"
                :key="notification.id"
                class="flex items-start gap-3 pb-3 border-b border-gray-100 last:border-0 last:pb-0"
              >
                <div class="w-8 h-8 rounded-full bg-surface-alt flex items-center justify-center flex-shrink-0 mt-0.5">
                  <component :is="getNotificationIcon(notification.type)" class="w-3.5 h-3.5 text-ink-900" />
                </div>
                <div class="min-w-0">
                  <p class="text-sm font-medium text-gray-900 truncate">
                    {{ composeNotificationMessage(notification, t).title }}
                  </p>
                  <p class="text-xs text-gray-500 line-clamp-2">
                    {{ composeNotificationMessage(notification, t).message }}
                  </p>
                  <p class="text-xs text-gray-400 mt-0.5">{{ getRelativeTime(notification.createdAt, t) }}</p>
                </div>
              </div>
            </div>
          </div>

          <div
            class="rounded-2xl border border-hairline p-6 text-ink-900 bg-cover bg-no-repeat"
            :style="{ backgroundImage: `url(${curatedBg})`, backgroundPosition: 'left top' }"
          >
            <p class="text-xs font-bold tracking-widest uppercase mb-2 text-ink-400">
              {{ t.clientDashboard.curatedSelection }}
            </p>
            <h3 class="text-lg font-bold mb-2">{{ t.clientDashboard.findArchitect }}</h3>
            <p class="text-sm text-ink-500 mb-4">{{ t.clientDashboard.browseStudiosDesc }}</p>
            <button
              class="bg-ink-900 text-white px-4 py-2 rounded-full text-xs font-bold tracking-wider hover:bg-ink-500 transition"
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
import { Plus, ChevronRight } from 'lucide-vue-next'
import {
  CheckCircleIcon,
  PencilSquareIcon,
  DocumentTextIcon,
  CurrencyDollarIcon,
  BellIcon
} from '@heroicons/vue/24/outline'
import { useAuthStore } from '@/stores/auth'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import { useNotificationsStore } from '@/stores/notifications'
import { useI18n } from '@/composables/useI18n'
import { composeNotificationMessage, getRelativeTime } from '@/utils/notificationUtils'
import ProjectList from './ProjectList.vue'
import QuickActions from '@/components/dashboard/QuickActions.vue'
import { useClientProfileStore } from '@/stores/clientProfile'
import welcomeBg from '@/assets/images/landing/dashboard-welcome-bg.jpg'
import curatedBg from '@/assets/images/landing/dashboard-curated-bg.jpg'

const { t } = useI18n()
const authStore = useAuthStore()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()
const notificationsStore = useNotificationsStore()
const clientProfileStore = useClientProfileStore()

const { projects } = storeToRefs(projectsStore)
const { projectBids } = storeToRefs(bidsStore)
const { notifications } = storeToRefs(notificationsStore)

const clientName = computed(() => {
  const user = authStore.user
  if (!user) return 'Client'
  return user.firstName || user.name || user.email?.split('@')[0] || 'Client'
})

const activeProjectCount = computed(
  () => projects.value.filter(p => p.status === 'OPEN' || p.status === 'IN_PROGRESS').length
)

// Unsent briefs feed the quick actions at the top of the dashboard
const drafts = computed(() =>
  projects.value
    .filter(p => p.status === 'DRAFT')
    .sort((a, b) => new Date(b.updatedAt || b.createdAt || 0) - new Date(a.updatedAt || a.createdAt || 0))
)
const totalBidCount = computed(() => projectBids.value?.length || 0)

const budgetUsed = computed(() => {
  return projects.value
    .filter(p => p.status === 'AWARDED' || p.status === 'IN_PROGRESS')
    .reduce((sum, p) => sum + (p.designBudgetMax || 0), 0)
})

const recentActivity = computed(() => notifications.value.slice(0, 5))

const notificationIconMap = {
  PROJECT_VALIDATED: CheckCircleIcon,
  PROJECT_UPDATED: PencilSquareIcon,
  BID_RECEIVED: DocumentTextIcon,
  BID_ACCEPTED: CheckCircleIcon,
  PAYMENT_RECEIVED: CurrencyDollarIcon
}

const getNotificationIcon = type => notificationIconMap[type] || BellIcon

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
  clientProfileStore.fetchProfile().catch(() => {})
  await projectsStore.fetchMyProjects()
  try {
    await notificationsStore.fetchNotifications()
  } catch (err) {
    console.error('Failed to fetch recent activity:', err)
  }
})
</script>
