<template>
  <div class="flex flex-col h-screen overflow-hidden bg-surface-alt">
    <DashboardTopbar>
      <AccountMenu
        :name="userName"
        :photo-url="userPhotoUrl || ''"
        role-label="arsitek"
        profile-to="/architect/profile"
        @logout="handleLogout"
      />
    </DashboardTopbar>

    <div class="flex flex-1 min-h-0">
      <aside class="w-56 shrink-0 flex flex-col bg-white border-r border-hairline">
        <nav class="flex-1 px-3 pt-4 space-y-1">
          <RouterLink
            v-for="item in navItems"
            :key="item.name"
            :to="item.to"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all"
            :class="
              isActive(item)
                ? 'bg-surface-alt text-ink-900 font-semibold'
                : 'text-ink-400 hover:text-ink-900 hover:bg-surface-alt'
            "
          >
            <component :is="item.icon" :size="18" />
            <span>{{ item.label }}</span>
          </RouterLink>
        </nav>

        <div class="px-3 py-4 border-t border-hairline space-y-1">
          <NotificationDropdown variant="architect" />
          <RouterLink
            to="/architect/settings"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all"
            :class="
              route.path.startsWith('/architect/settings')
                ? 'bg-surface-alt text-ink-900 font-semibold'
                : 'text-ink-400 hover:text-ink-900 hover:bg-surface-alt'
            "
          >
            <Settings :size="18" />
            <span>Settings</span>
          </RouterLink>
        </div>
      </aside>

      <main class="flex-1 overflow-y-auto relative">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LayoutDashboard, Search, ClipboardList, Layout, Settings } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useArchitectProfileStore } from '@/stores/architectProfile'
import NotificationDropdown from '@/components/notifications/NotificationDropdown.vue'
import DashboardTopbar from '@/components/layout/DashboardTopbar.vue'
import AccountMenu from '@/components/layout/AccountMenu.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const architectProfileStore = useArchitectProfileStore()

onMounted(async () => {
  if (!architectProfileStore.hasProfile) {
    try {
      await architectProfileStore.fetchProfile()
    } catch {
      // silently ignore — the account menu falls back to the auth name
    }
  }
})

const userName = computed(() => architectProfileStore.profileName || authStore.userName || 'Architect')
const userPhotoUrl = computed(() => architectProfileStore.profilePhotoUrl)
const navItems = [
  { name: 'dashboard', label: 'Dashboard', to: '/architect/dashboard', icon: LayoutDashboard },
  { name: 'opportunities', label: 'Find Projects', to: '/architect/opportunities', icon: Search },
  { name: 'bids', label: 'My Bids', to: '/architect/bids', icon: ClipboardList },
  { name: 'portfolios', label: 'Portfolio', to: '/architect/portfolios', icon: Layout }
]

const isActive = item => {
  if (item.name === 'dashboard') return route.path === '/architect/dashboard'
  return route.path.startsWith(item.to)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>
