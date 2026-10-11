<template>
  <div v-if="authStore.isAuthenticated" class="flex items-center gap-2">
    <button
      v-if="showDashboard"
      type="button"
      class="hidden sm:inline-flex items-center gap-2 h-11 px-4 rounded-full text-caption font-semibold text-ink-900 hover:bg-surface-alt transition-colors"
      @click="$emit('dashboard')"
    >
      <LayoutDashboard :size="16" :stroke-width="2" /> Dashboard
    </button>

    <Menu as="div" class="relative">
      <MenuButton
        :aria-label="`Menu akun ${displayName}`"
        class="block w-10 h-10 rounded-full ring-offset-2 hover:ring-2 hover:ring-hairline-alt focus:outline-none focus-visible:ring-2 focus-visible:ring-ink-900 transition-shadow"
      >
        <img v-if="photoUrl" :src="photoUrl" alt="" class="w-10 h-10 rounded-full object-cover" />
        <span
          v-else
          class="w-10 h-10 rounded-full bg-ink-900 text-white flex items-center justify-center text-caption font-bold"
          aria-hidden="true"
        >
          {{ initials }}
        </span>
      </MenuButton>

      <transition
        enter-active-class="transition ease-out duration-150"
        enter-from-class="opacity-0 -translate-y-1"
        enter-to-class="opacity-100 translate-y-0"
        leave-active-class="transition ease-in duration-100"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <MenuItems
          class="absolute right-0 top-[48px] z-50 w-[260px] bg-white border border-hairline rounded-2xl shadow-[0_24px_48px_-24px_rgba(0,0,0,.25)] p-2 focus:outline-none"
        >
          <div class="px-3 pt-2 pb-3 mb-1 border-b border-hairline">
            <div class="text-caption-sm font-semibold uppercase tracking-[.06em] text-ink-400 mb-1">
              {{ roleLabel ? `Masuk sebagai ${roleLabel}` : 'Masuk sebagai' }}
            </div>
            <div class="text-[15px] font-bold text-ink-900 truncate">{{ displayName }}</div>
            <div class="text-caption text-ink-400 truncate">{{ authStore.user?.email }}</div>
          </div>
          <MenuItem v-if="showDashboard" v-slot="{ active }">
            <button type="button" :class="[itemCls, active ? 'bg-surface-alt' : '']" @click="$emit('dashboard')">
              <LayoutDashboard :size="16" :stroke-width="2" /> Dashboard
            </button>
          </MenuItem>
          <MenuItem v-if="profileTo" v-slot="{ active }">
            <router-link :to="profileTo" :class="[itemCls, active ? 'bg-surface-alt' : '']">
              <UserRound :size="16" :stroke-width="2" /> Profil
            </router-link>
          </MenuItem>
          <MenuItem v-slot="{ active }">
            <button type="button" :class="[itemCls, active ? 'bg-surface-alt' : '']" @click="$emit('logout')">
              <LogOut :size="16" :stroke-width="2" /> Keluar
            </button>
          </MenuItem>
        </MenuItems>
      </transition>
    </Menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Menu, MenuButton, MenuItems, MenuItem } from '@headlessui/vue'
import { LayoutDashboard, LogOut, UserRound } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  // Overrides the account's own name, e.g. the architect's public profile name
  name: { type: String, default: '' },
  photoUrl: { type: String, default: '' },
  roleLabel: { type: String, default: '' },
  profileTo: { type: String, default: '' },
  showDashboard: { type: Boolean, default: false }
})

defineEmits(['dashboard', 'logout'])

const authStore = useAuthStore()

const itemCls = 'w-full flex items-center gap-2.5 px-3 h-10 rounded-xl text-caption font-semibold text-ink-900'

const displayName = computed(() => {
  if (props.name) return props.name
  const user = authStore.user || {}
  return [user.firstName, user.lastName].filter(Boolean).join(' ').trim() || user.email || ''
})

const initials = computed(
  () =>
    displayName.value
      .split(/[\s@.]+/)
      .filter(Boolean)
      .slice(0, 2)
      .map(word => word[0].toUpperCase())
      .join('') || '?'
)
</script>
