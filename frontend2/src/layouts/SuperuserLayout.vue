<template>
  <div class="flex h-screen overflow-hidden bg-surface-alt">
    <!-- Sidebar -->
    <aside class="w-56 shrink-0 flex flex-col bg-ink-700 h-screen">
      <div class="px-5 py-6 cursor-pointer" @mouseenter="logoHovered = true" @mouseleave="logoHovered = false">
        <span class="text-lg font-black tracking-tighter logo" :class="logoHovered ? 'logo-hover' : 'logo-default'"
          >rumantra.</span
        >
        <p class="text-white/30 text-xs mt-0.5 font-medium tracking-wide">BACKOFFICE</p>
      </div>

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
          <span
            v-if="item.badge && item.badge > 0"
            class="ml-auto text-xs bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center font-bold"
          >
            {{ item.badge > 9 ? '9+' : item.badge }}
          </span>
        </RouterLink>
      </nav>

      <div class="px-3 py-4 border-t border-white/10 space-y-1">
        <button
          class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all text-white/60 hover:text-white hover:bg-white/5"
          @click="handleLogout"
        >
          <LogOut :size="18" />
          <span>Sign Out</span>
        </button>
        <div class="flex items-center gap-3 px-3 py-2 mt-2">
          <div
            class="w-8 h-8 rounded-full bg-red-800 text-white flex items-center justify-center text-xs font-bold shrink-0"
          >
            {{ userInitials }}
          </div>
          <div class="min-w-0">
            <p class="text-white text-xs font-semibold truncate">{{ userName }}</p>
            <p class="text-white/40 text-xs truncate">Superuser</p>
          </div>
        </div>
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  LayoutDashboard,
  ClipboardCheck,
  FolderOpen,
  AlertTriangle,
  Timer,
  MessageSquare,
  Users,
  Image,
  LayoutGrid,
  LogOut
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const logoHovered = ref(false)
const userName = computed(() => authStore.userName || 'Superuser')
const userInitials = computed(() =>
  userName.value
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
)

const navItems = [
  { name: 'dashboard', label: 'Dashboard', to: '/superuser/dashboard', icon: LayoutDashboard },
  { name: 'queue', label: 'Project Queue', to: '/superuser/projects/queue', icon: ClipboardCheck },
  { name: 'projects', label: 'All Projects', to: '/superuser/projects', icon: FolderOpen },
  { name: 'disputes', label: 'Disputes', to: '/superuser/disputes', icon: AlertTriangle },
  {
    name: 'negotiation-disputes',
    label: 'Negotiation Disputes',
    to: '/superuser/negotiation-disputes',
    icon: Timer
  },
  { name: 'support', label: 'Support', to: '/superuser/support', icon: MessageSquare },
  { name: 'users', label: 'Users', to: '/superuser/users', icon: Users },
  { name: 'landing', label: 'Landing Page', to: '/superuser/landing', icon: Image },
  { name: 'presets', label: 'Form Presets', to: '/superuser/landing/presets', icon: LayoutGrid }
]

const isActive = item => {
  if (item.name === 'dashboard') return route.path === '/superuser/dashboard'
  if (item.name === 'projects') return route.path === '/superuser/projects'
  if (item.name === 'queue') return route.path === '/superuser/projects/queue'
  if (item.name === 'landing') return route.path === '/superuser/landing'
  return route.path.startsWith(item.to)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logo-default {
  background: linear-gradient(
    to right,
    theme('colors.white'),
    theme('colors.brand-gold'),
    theme('colors.white'),
    theme('colors.white')
  );
  background-size: 200% 100%;
  background-position: 0% 50%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: background-position 0.8s ease-in-out;
}
.logo-hover {
  background: linear-gradient(
    to right,
    theme('colors.white'),
    theme('colors.brand-gold'),
    theme('colors.brand-brown'),
    theme('colors.brand-gold'),
    theme('colors.white')
  );
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
