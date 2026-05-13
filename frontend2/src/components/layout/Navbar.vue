<template>
  <nav
    class="sticky top-0 z-50 flex items-center"
    style="height: 72px; background: rgba(255, 255, 255, 0.8); backdrop-filter: blur(10px)"
  >
    <div class="flex justify-between items-center w-full px-10 max-w-[1440px] mx-auto">
      <!-- Logo (flex-1, left) -->
      <div class="flex-1 flex justify-start">
        <Logo />
      </div>

      <!-- Center nav links -->
      <div class="hidden md:flex items-center gap-8 flex-none">
        <router-link to="/" class="text-[14px] font-medium text-[#666666] hover:text-[#0A0A0A] transition-colors">
          {{ t.nav.caraKerja }}
        </router-link>
        <router-link to="/" class="text-[14px] font-medium text-[#666666] hover:text-[#0A0A0A] transition-colors">
          {{ t.nav.inspirasi }}
        </router-link>
        <router-link to="/" class="text-[14px] font-medium text-[#666666] hover:text-[#0A0A0A] transition-colors">
          {{ t.nav.temukanArsitek }}
        </router-link>
        <span class="flex items-center gap-1.5">
          <router-link to="/" class="text-[14px] font-medium text-[#666666] hover:text-[#0A0A0A] transition-colors">
            {{ t.nav.riset }}
          </router-link>
          <span
            class="text-white text-[9px] font-bold px-1.5 py-0.5 rounded-full uppercase tracking-wider"
            style="background: #0a0a0a"
            >NEW</span
          >
        </span>
      </div>

      <!-- Right actions -->
      <div class="hidden md:flex items-center gap-4 flex-1 justify-end">
        <LanguageSwitcher />
        <template v-if="isAuthenticated">
          <router-link
            :to="dashboardPath"
            class="text-[13px] font-medium text-[#666666] hover:text-[#0A0A0A] transition-colors"
          >
            {{ t.nav.dasbor }}
          </router-link>
          <button
            class="px-5 py-2 bg-[#0A0A0A] text-white rounded-full text-[14px] font-semibold hover:opacity-90 transition-all hover:-translate-y-px whitespace-nowrap border-none cursor-pointer"
            style="box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08)"
            @click="handleLogout"
          >
            {{ t.nav.keluar }}
          </button>
        </template>
        <template v-else>
          <router-link
            to="/signup?role=ARCHITECT"
            class="text-[13px] font-medium text-[#666666] hover:text-[#0A0A0A] transition-colors"
          >
            {{ t.nav.untukArsitek }}
          </router-link>
          <router-link
            to="/login"
            class="text-[13px] font-medium text-[#888888] hover:text-[#0A0A0A] transition-colors"
          >
            {{ t.nav.masuk }}
          </router-link>
          <router-link to="/signup">
            <button
              class="px-5 py-2 bg-[#0A0A0A] text-white rounded-full text-[14px] font-semibold hover:opacity-90 transition-all hover:-translate-y-px whitespace-nowrap border-none cursor-pointer"
              style="box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08)"
            >
              {{ t.nav.mulaiProyek }}
            </button>
          </router-link>
        </template>
      </div>

      <!-- Mobile menu toggle -->
      <button class="md:hidden text-black" @click="isMobileMenuOpen = !isMobileMenuOpen">
        <component :is="isMobileMenuOpen ? X : Menu" :size="24" />
      </button>
    </div>

    <!-- Mobile menu -->
    <div
      v-if="isMobileMenuOpen"
      class="absolute top-full left-0 w-full bg-white shadow-xl p-8 flex flex-col gap-6 md:hidden border-b border-gray-100"
    >
      <router-link to="/" class="text-lg font-bold">{{ t.nav.caraKerja }}</router-link>
      <router-link to="/" class="text-lg font-bold">{{ t.nav.inspirasi }}</router-link>
      <router-link to="/" class="text-lg font-bold">{{ t.nav.temukanArsitek }}</router-link>
      <router-link to="/signup?role=ARCHITECT" class="text-lg font-bold">{{ t.nav.untukArsitek }}</router-link>
      <hr class="border-gray-100" />
      <div class="flex flex-col gap-4">
        <LanguageSwitcher />
        <template v-if="isAuthenticated">
          <router-link :to="dashboardPath">
            <BaseButton variant="ghost" :fullWidth="true" class="font-bold">{{ t.nav.dasbor }}</BaseButton>
          </router-link>
          <BaseButton variant="primary" :fullWidth="true" class="font-bold" @click="handleLogout">
            {{ t.nav.keluar }}
          </BaseButton>
        </template>
        <template v-else>
          <router-link to="/login">
            <BaseButton variant="ghost" :fullWidth="true" class="font-bold">{{ t.nav.masuk }}</BaseButton>
          </router-link>
          <router-link to="/signup">
            <BaseButton variant="primary" :fullWidth="true" class="font-bold">{{ t.nav.mulaiProyek }}</BaseButton>
          </router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu, X } from 'lucide-vue-next'
import BaseButton from '@/components/ui/BaseButton.vue'
import LanguageSwitcher from './LanguageSwitcher.vue'
import Logo from '@/components/ui/Logo.vue'
import { useI18n } from '@/composables/useI18n'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const isMobileMenuOpen = ref(false)

const isAuthenticated = computed(() => authStore.isAuthenticated)

const dashboardPath = computed(() => {
  if (authStore.isArchitect) return '/architect/dashboard'
  if (authStore.isClient) return '/client/dashboard'
  return '/'
})

const handleLogout = async () => {
  await authStore.logout()
  router.push('/')
}

watch(
  () => route.path,
  () => {
    isMobileMenuOpen.value = false
  }
)
</script>
