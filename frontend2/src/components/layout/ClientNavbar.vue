<template>
  <nav class="sticky top-0 z-50 bg-white border-b-2 border-blue-500">
    <div class="max-w-7xl mx-auto px-6 py-4 relative">
      <div class="flex items-center justify-between">
        <router-link to="/" class="flex items-center">
          <Logo class="h-8" />
        </router-link>

        <div class="flex items-center gap-4">
          <div class="px-3 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800 border border-blue-300">
            CLIENT MODE
          </div>

          <div class="relative" ref="dropdownRef">
            <button
              @click="toggleDropdown"
              class="flex items-center gap-2 px-4 py-2 rounded-lg hover:bg-blue-50 transition-colors border border-transparent hover:border-blue-200"
            >
              <div
                class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center text-white font-semibold text-sm shadow-sm"
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
                    to="/client/profile"
                    @click="closeDropdown"
                    class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-blue-50 transition-colors flex items-center gap-2"
                  >
                    <UserCircleIcon class="w-4 h-4" />
                    {{ t.navbar.profile }}
                  </router-link>
                </div>

                <div class="py-2">
                  <div class="px-4 py-2">
                    <p class="text-xs font-semibold text-gray-500 uppercase mb-2">
                      {{ t.navbar.switchRole }}
                    </p>
                    <div class="space-y-1">
                      <button
                        v-if="hasArchitectRole"
                        @click="switchToArchitect"
                        class="w-full text-left px-3 py-2 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition-colors flex items-center gap-2"
                      >
                        <BriefcaseIcon class="w-4 h-4" />
                        {{ t.navbar.architectMode }}
                      </button>
                      <button
                        v-else
                        @click="activateArchitectRole"
                        class="w-full text-left px-3 py-2 rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors flex items-center gap-2"
                      >
                        <PlusCircleIcon class="w-4 h-4" />
                        {{ t.navbar.activateArchitect }}
                      </button>

                      <button
                        class="w-full text-left px-3 py-2 rounded-lg text-sm bg-blue-50 text-blue-900 font-medium border border-blue-200 flex items-center gap-2"
                      >
                        <UserIcon class="w-4 h-4" />
                        {{ t.navbar.clientMode }}
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
                          locale === 'en' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        ]"
                      >
                        English
                      </button>
                      <button
                        @click="setLanguage('id')"
                        :class="[
                          'flex-1 px-3 py-2 rounded-lg text-sm transition-colors',
                          locale === 'id' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
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
import {
  ChevronDownIcon,
  BriefcaseIcon,
  UserIcon,
  PlusCircleIcon,
  ArrowRightOnRectangleIcon,
  UserCircleIcon
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

const hasArchitectRole = computed(() => authStore.isArchitect)

const toggleDropdown = event => {
  event.stopPropagation()
  isDropdownOpen.value = !isDropdownOpen.value
}

const closeDropdown = () => {
  isDropdownOpen.value = false
}

const switchToArchitect = () => {
  closeDropdown()
  router.push('/architect/dashboard')
}

const activateArchitectRole = async () => {
  if (hasArchitectRole.value) {
    console.log('Architect role already exists, switching instead of activating')
    closeDropdown()
    router.push('/architect/dashboard')
    return
  }

  try {
    const result = await authStore.activateRole('ARCHITECT')
    if (result.alreadyActivated) {
      console.log('Role was already activated, switching to architect dashboard')
    }
    closeDropdown()
    router.push('/architect/dashboard')
  } catch (error) {
    console.error('Failed to activate architect role:', error)
    const errorMessage = error.response?.data?.message || 'Failed to activate architect role. Please try again.'
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
  console.log('ClientNavbar mounted', {
    hasArchitectRole: hasArchitectRole.value,
    registeredRoles: authStore.user?.registeredRoles
  })
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
