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
        <router-link to="/" class="text-caption font-medium text-ink-400 hover:text-ink-900 transition-colors">
          {{ t.nav.caraKerja }}
        </router-link>
        <router-link to="/" class="text-caption font-medium text-ink-400 hover:text-ink-900 transition-colors">
          {{ t.nav.inspirasi }}
        </router-link>
        <router-link to="/" class="text-caption font-medium text-ink-400 hover:text-ink-900 transition-colors">
          {{ t.nav.temukanArsitek }}
        </router-link>
        <span class="flex items-center gap-1.5">
          <router-link to="/" class="text-caption font-medium text-ink-400 hover:text-ink-900 transition-colors">
            {{ t.nav.riset }}
          </router-link>
          <span
            class="text-white text-micro-cap font-bold px-1.5 py-0.5 rounded-full uppercase tracking-wider bg-ink-900"
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
            class="text-caption-sm font-medium text-ink-400 hover:text-ink-900 transition-colors"
          >
            {{ t.nav.dasbor }}
          </router-link>
          <button
            class="px-5 py-2 bg-ink-900 text-white rounded-full text-caption font-semibold hover:opacity-90 transition-all hover:-translate-y-px whitespace-nowrap border-none cursor-pointer"
            style="box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08)"
            @click="handleLogout"
          >
            {{ t.nav.keluar }}
          </button>
        </template>
        <template v-else>
          <!-- Login dropdown -->
          <div ref="loginDropdownRef" class="relative">
            <button
              class="flex items-center gap-1 text-caption-sm font-medium text-ink-300 hover:text-ink-900 transition-colors"
              @click="isLoginDropdownOpen = !isLoginDropdownOpen"
            >
              {{ t.nav.masuk }}
              <ChevronDown
                class="w-3.5 h-3.5 transition-transform duration-200"
                :class="{ 'rotate-180': isLoginDropdownOpen }"
              />
            </button>

            <Transition
              enter-active-class="transition-all duration-150 ease-out"
              enter-from-class="opacity-0 translate-y-1"
              enter-to-class="opacity-100 translate-y-0"
              leave-active-class="transition-all duration-100 ease-in"
              leave-from-class="opacity-100 translate-y-0"
              leave-to-class="opacity-0 translate-y-1"
            >
              <div
                v-if="isLoginDropdownOpen"
                class="absolute right-0 top-full mt-3 w-56 bg-white rounded-2xl shadow-xl border border-hairline overflow-hidden z-50"
              >
                <router-link
                  to="/login"
                  class="flex items-center gap-3 px-4 py-3.5 hover:bg-surface-muted transition-colors"
                  @click="isLoginDropdownOpen = false"
                >
                  <div class="w-8 h-8 rounded-xl bg-surface-muted flex items-center justify-center flex-shrink-0">
                    <HardHat class="w-4 h-4 text-ink-900" />
                  </div>
                  <div>
                    <p class="text-caption-sm font-semibold text-ink-900">{{ t.nav.untukArsitek }}</p>
                    <p class="text-micro-sm text-ink-300">{{ t.nav.masuk }}</p>
                  </div>
                </router-link>
                <div class="h-px bg-[#F0F0F0]" />
                <router-link
                  to="/login"
                  class="flex items-center gap-3 px-4 py-3.5 hover:bg-surface-muted transition-colors"
                  @click="isLoginDropdownOpen = false"
                >
                  <div class="w-8 h-8 rounded-xl bg-surface-muted flex items-center justify-center flex-shrink-0">
                    <User class="w-4 h-4 text-ink-900" />
                  </div>
                  <div>
                    <p class="text-caption-sm font-semibold text-ink-900">{{ t.nav.untukKlien }}</p>
                    <p class="text-micro-sm text-ink-300">{{ t.nav.masuk }}</p>
                  </div>
                </router-link>
              </div>
            </Transition>
          </div>

          <router-link to="/signup">
            <button
              class="px-5 py-2 bg-ink-900 text-white rounded-full text-caption font-semibold hover:opacity-90 transition-all hover:-translate-y-px whitespace-nowrap border-none cursor-pointer"
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu, X, ChevronDown, HardHat, User } from 'lucide-vue-next'
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
const isLoginDropdownOpen = ref(false)
const loginDropdownRef = ref(null)

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

const handleClickOutside = e => {
  if (loginDropdownRef.value && !loginDropdownRef.value.contains(e.target)) {
    isLoginDropdownOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))

watch(
  () => route.path,
  () => {
    isMobileMenuOpen.value = false
    isLoginDropdownOpen.value = false
  }
)
</script>
