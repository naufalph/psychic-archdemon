<template>
  <nav class="sticky top-0 z-50 bg-white border-b-2 border-amber-500">
    <div class="max-w-7xl mx-auto px-6 py-4 relative">
      <div class="flex items-center justify-between">
        <router-link to="/" class="flex items-center">
          <Logo class="h-8" />
        </router-link>

        <div class="flex items-center gap-4">
          <div class="px-3 py-1 text-xs font-semibold rounded-full bg-amber-100 text-amber-800 border border-amber-300">
            ARCHITECT MODE
          </div>

          <NotificationDropdown variant="architect" />

          <div class="relative" ref="dropdownRef">
            <button
              @click="toggleDropdown"
              class="flex items-center gap-2 px-4 py-2 rounded-lg hover:bg-amber-50 transition-colors border border-transparent hover:border-amber-200"
            >
              <div
                class="w-8 h-8 rounded-full bg-gradient-to-br from-amber-500 to-amber-700 flex items-center justify-center text-white font-semibold text-sm shadow-sm"
              >
                {{ userInitials }}
              </div>
              <ChevronDownIcon
                :class="['w-5 h-5 text-gray-600 transition-transform', isDropdownOpen && 'rotate-180']"
              />
            </button>

            <Transition
              enter-active-class="transition ease-out duration-100"
              enter-from-class="transform opacity-0 scale-95"
              enter-to-class="transform opacity-100 scale-100"
              leave-active-class="transition ease-in duration-75"
              leave-from-class="transform opacity-100 scale-100"
              leave-to-class="transform opacity-0 scale-95"
            >
              <div
                v-if="isDropdownOpen"
                class="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-lg border border-gray-200 py-2"
                style="z-index: 9999"
              >
                <div class="px-4 py-3 border-b border-gray-100">
                  <p class="text-sm font-semibold text-gray-900">{{ userName }}</p>
                  <p class="text-xs text-gray-500">{{ userEmail }}</p>
                </div>

                <div class="border-b border-gray-100 py-2">
                  <router-link
                    to="/architect/profile"
                    @click="closeDropdown"
                    class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-amber-50 transition-colors flex items-center gap-2"
                  >
                    <UserCircleIcon class="w-4 h-4" />
                    {{ t.navbar.profile }}
                  </router-link>
                  <router-link
                    to="/architect/portfolios"
                    @click="closeDropdown"
                    class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-amber-50 transition-colors flex items-center gap-2"
                  >
                    <PhotoIcon class="w-4 h-4" />
                    {{ t.navbar.portfolios }}
                  </router-link>
                </div>

                <div class="py-2">
                  <div class="px-4 py-2">
                    <p class="text-xs font-semibold text-gray-500 uppercase mb-2">
                      {{ t.navbar.switchRole }}
                    </p>
                    <div class="space-y-1">
                      <button
                        class="w-full text-left px-3 py-2 rounded-lg text-sm bg-amber-50 text-amber-900 font-medium border border-amber-200 flex items-center gap-2"
                      >
                        <BriefcaseIcon class="w-4 h-4" />
                        {{ t.navbar.architectMode }}
                      </button>

                      <button
                        v-if="hasClientRole"
                        @click="switchToClient"
                        class="w-full text-left px-3 py-2 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition-colors flex items-center gap-2"
                      >
                        <UserIcon class="w-4 h-4" />
                        {{ t.navbar.clientMode }}
                      </button>
                      <button
                        v-else
                        @click="activateClientRole"
                        class="w-full text-left px-3 py-2 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors flex items-center gap-2"
                      >
                        <PlusCircleIcon class="w-4 h-4" />
                        {{ t.navbar.activateClient }}
                      </button>
                    </div>
                  </div>

                  <div class="border-t border-gray-100 mt-2 pt-2 px-4 py-2">
                    <p class="text-xs font-semibold text-gray-500 uppercase mb-2">
                      {{ t.navbar.language }}
                    </p>
                    <div class="flex gap-2">
                      <button
                        @click="setLanguage('en')"
                        :class="[
                          'flex-1 px-3 py-2 rounded-lg text-sm transition-colors',
                          locale === 'en' ? 'bg-amber-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        ]"
                      >
                        English
                      </button>
                      <button
                        @click="setLanguage('id')"
                        :class="[
                          'flex-1 px-3 py-2 rounded-lg text-sm transition-colors',
                          locale === 'id' ? 'bg-amber-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        ]"
                      >
                        Indonesia
                      </button>
                    </div>
                  </div>

                  <div class="border-t border-gray-100 mt-2 pt-2">
                    <button
                      @click="handleLogout"
                      class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors flex items-center gap-2"
                    >
                      <ArrowRightOnRectangleIcon class="w-4 h-4" />
                      {{ t.navbar.logout }}
                    </button>
                  </div>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'
import Logo from '@/components/ui/Logo.vue'
import NotificationDropdown from '@/components/notifications/NotificationDropdown.vue'
import {
  ChevronDownIcon,
  BriefcaseIcon,
  UserIcon,
  PlusCircleIcon,
  ArrowRightOnRectangleIcon,
  UserCircleIcon,
  PhotoIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const authStore = useAuthStore()
const { t, locale, setLocale } = useI18n()

const isDropdownOpen = ref(false)
const dropdownRef = ref(null)

const userName = computed(() => authStore.userName || 'User')
const userEmail = computed(() => authStore.user?.email || '')
const userInitials = computed(() => {
  const name = userName.value
  return name
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})

const hasClientRole = computed(() => authStore.isClient)

const toggleDropdown = event => {
  event.stopPropagation()
  isDropdownOpen.value = !isDropdownOpen.value
}

const closeDropdown = () => {
  isDropdownOpen.value = false
}

const switchToClient = () => {
  closeDropdown()
  router.push('/client/dashboard')
}

const activateClientRole = async () => {
  if (hasClientRole.value) {
    closeDropdown()
    router.push('/client/dashboard')
    return
  }

  try {
    await authStore.activateRole('CLIENT')
    closeDropdown()
    router.push('/client/dashboard')
  } catch (error) {
    console.error('Failed to activate client role:', error)
    const errorMessage = error.response?.data?.message || 'Failed to activate client role. Please try again.'
    alert(errorMessage)
  }
}

const setLanguage = lang => {
  setLocale(lang)
}

const handleLogout = async () => {
  closeDropdown()
  await authStore.logout()
  router.push('/login')
}

const handleClickOutside = event => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
