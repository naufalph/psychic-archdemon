<template>
  <div class="flex h-screen overflow-hidden bg-[#F4F5F7]">
    <!-- Sidebar -->
    <aside class="w-56 shrink-0 flex flex-col bg-[#1C1C1C] h-screen">
      <!-- Logo -->
      <div class="px-5 py-6 cursor-pointer" @mouseenter="logoHovered = true" @mouseleave="logoHovered = false">
        <span class="text-lg font-black tracking-tighter logo" :class="logoHovered ? 'logo-hover' : 'logo-default'"
          >rumantra.</span
        >
      </div>

      <!-- Nav items -->
      <nav class="flex-1 px-3 space-y-1">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          :to="item.to"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all"
          :class="isActive(item) ? 'bg-white/10 text-white' : 'text-white/60 hover:text-white hover:bg-white/5'"
        >
          <component :is="item.icon" :size="18" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <!-- Bottom: settings + sign out + user -->
      <div class="px-3 py-4 border-t border-white/10 space-y-1">
        <RouterLink
          to="/architect/settings"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all text-white/60 hover:text-white hover:bg-white/5"
        >
          <Settings :size="18" />
          <span>Settings</span>
        </RouterLink>
        <button
          @click="handleLogout"
          class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all text-white/60 hover:text-white hover:bg-white/5"
        >
          <LogOut :size="18" />
          <span>Sign Out</span>
        </button>
        <div class="flex items-center gap-3 px-3 py-2 mt-2">
          <div
            class="w-8 h-8 rounded-full bg-[#7C4728] text-white flex items-center justify-center text-xs font-bold shrink-0"
          >
            {{ userInitials }}
          </div>
          <div class="min-w-0">
            <p class="text-white text-xs font-semibold truncate">{{ userName }}</p>
            <p class="text-white/40 text-xs truncate">Architect</p>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <main class="flex-1 overflow-y-auto">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LayoutDashboard, Search, ClipboardList, Layout, Settings, LogOut } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const logoHovered = ref(false)
const userName = computed(() => authStore.userName || 'Architect')
const userInitials = computed(() => {
  const name = userName.value
  return name
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
})

const navItems = [
  { name: 'dashboard', label: 'Dashboard', to: '/architect/dashboard', icon: LayoutDashboard },
  { name: 'opportunities', label: 'Find Projects', to: '/architect/opportunities', icon: Search },
  { name: 'bids', label: 'My Bids', to: '/architect/bids', icon: ClipboardList },
  { name: 'portfolios', label: 'Portfolio', to: '/architect/portfolios', icon: Layout },
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

<style scoped>
.logo-default {
  background: linear-gradient(to right, #ffffff, #c5a17a, #ffffff, #ffffff);
  background-size: 200% 100%;
  background-position: 0% 50%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: background-position 0.8s ease-in-out;
}

.logo-hover {
  background: linear-gradient(to right, #ffffff, #c5a17a, #7c4728, #c5a17a, #ffffff);
  background-size: 200% 100%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientShift 3s ease-in-out infinite;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
</style>
