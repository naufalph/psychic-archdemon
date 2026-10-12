<template>
  <div class="flex flex-col h-screen overflow-hidden bg-surface-alt">
    <DashboardTopbar>
      <AccountMenu role-label="klien" @logout="handleLogout" />
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
            <span
              v-if="item.badge && item.badge > 0"
              class="ml-auto text-xs bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center font-bold"
            >
              {{ item.badge > 9 ? '9+' : item.badge }}
            </span>
          </RouterLink>
        </nav>

        <div class="px-3 py-4 border-t border-hairline space-y-1">
          <NotificationDropdown variant="client" />
          <RouterLink
            to="/client/settings"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all"
            :class="
              route.path.startsWith('/client/settings')
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LayoutDashboard, FolderOpen, MessageSquare, Settings } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'
import NotificationDropdown from '@/components/notifications/NotificationDropdown.vue'
import DashboardTopbar from '@/components/layout/DashboardTopbar.vue'
import AccountMenu from '@/components/layout/AccountMenu.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()

const navItems = computed(() => [
  {
    name: 'dashboard',
    label: t.value?.clientNav?.dashboard || 'Dashboard',
    to: '/client/dashboard',
    icon: LayoutDashboard
  },
  { name: 'projects', label: t.value?.clientNav?.projects || 'Projects', to: '/client/projects', icon: FolderOpen },
  { name: 'messages', label: t.value?.clientNav?.messages || 'Messages', to: '/client/messages', icon: MessageSquare }
])

const isActive = item => {
  if (item.name === 'dashboard') return route.path === '/client/dashboard'
  return route.path.startsWith(item.to)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>
