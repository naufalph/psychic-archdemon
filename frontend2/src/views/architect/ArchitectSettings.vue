<template>
  <div class="min-h-full bg-surface-alt px-6 py-8">
    <div class="max-w-lg mx-auto space-y-4">
      <div class="mb-6">
        <p class="text-xs text-gray-400 uppercase font-bold tracking-wide mb-1">Settings</p>
        <h1 class="text-2xl font-bold text-gray-900">Account Settings</h1>
      </div>

      <!-- Account -->
      <div class="bg-white rounded-xl border border-gray-200 divide-y divide-gray-100">
        <div class="px-5 py-4">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">Account</p>
          <RouterLink to="/architect/profile" class="flex items-center justify-between group">
            <div class="flex items-center gap-3">
              <div
                class="w-9 h-9 rounded-full bg-ink-700 text-white flex items-center justify-center text-xs font-bold"
              >
                {{ userInitials }}
              </div>
              <div>
                <p class="font-semibold text-gray-900 text-sm">{{ userName }}</p>
                <p class="text-xs text-gray-500">{{ userEmail }}</p>
              </div>
            </div>
            <ChevronRight :size="16" class="text-gray-400 group-hover:text-gray-600 transition" />
          </RouterLink>
        </div>

        <button
          class="w-full flex items-center gap-3 px-5 py-4 text-left hover:bg-gray-50 transition"
          @click="handleLogout"
        >
          <LogOut :size="16" class="text-red-500" />
          <span class="text-sm font-medium text-red-500">Sign Out</span>
        </button>
      </div>

      <!-- Change Password (email/password accounts only) -->
      <div v-if="authStore.user?.socialType === 'EMAIL'" class="bg-white rounded-xl border border-gray-200 px-5 py-4">
        <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">
          {{ t.settings.changePassword.title }}
        </p>
        <form class="space-y-3" @submit.prevent="handleChangePassword">
          <div>
            <input
              v-model="passwordForm.currentPassword"
              type="password"
              :placeholder="t.settings.changePassword.currentPasswordPlaceholder"
              class="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm focus:border-ink-700 focus:ring-1 focus:ring-ink-700 outline-none"
            />
          </div>
          <div>
            <input
              v-model="passwordForm.newPassword"
              type="password"
              :placeholder="t.settings.changePassword.newPasswordPlaceholder"
              class="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm focus:border-ink-700 focus:ring-1 focus:ring-ink-700 outline-none"
            />
          </div>
          <div>
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              :placeholder="t.settings.changePassword.confirmPasswordPlaceholder"
              class="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm focus:border-ink-700 focus:ring-1 focus:ring-ink-700 outline-none"
            />
          </div>
          <p class="text-xs text-gray-400">{{ t.settings.changePassword.policyHint }}</p>
          <p v-if="passwordError" class="text-xs text-red-500">{{ passwordError }}</p>
          <p v-if="passwordSuccess" class="text-xs text-green-600">{{ t.settings.changePassword.success }}</p>
          <button
            type="submit"
            :disabled="isChangingPassword"
            class="w-full bg-ink-700 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-black transition disabled:opacity-50"
          >
            {{ t.settings.changePassword.submit }}
          </button>
        </form>
      </div>

      <!-- Language -->
      <div class="bg-white rounded-xl border border-gray-200 px-5 py-4">
        <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">Language</p>
        <div class="flex gap-2">
          <button
            class="flex-1 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="locale === 'en' ? 'bg-ink-700 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
            @click="setLocale('en')"
          >
            English
          </button>
          <button
            class="flex-1 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
            :class="locale === 'id' ? 'bg-ink-700 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
            @click="setLocale('id')"
          >
            Indonesia
          </button>
        </div>
      </div>

      <!-- Mode switching -->
      <div class="bg-white rounded-xl border border-gray-200 px-5 py-4">
        <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">Mode</p>
        <div class="flex gap-2">
          <div class="flex-1 flex items-center gap-2 px-3 py-2 rounded-lg bg-ink-700 text-white text-sm font-medium">
            <HardHat :size="15" />
            <span>Architect</span>
          </div>
          <button
            v-if="canSwitchToClient"
            class="flex-1 flex items-center gap-2 px-3 py-2 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200 transition text-sm font-medium"
            @click="switchToClient"
          >
            <UserCog :size="15" />
            <span>Client</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronRight, LogOut, UserCog, HardHat } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'
import { authAPI } from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()
const { t, locale, setLocale } = useI18n()

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const isChangingPassword = ref(false)
const passwordError = ref('')
const passwordSuccess = ref(false)

const PASSWORD_POLICY = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_-]).*$/

const handleChangePassword = async () => {
  passwordError.value = ''
  passwordSuccess.value = false

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = t.value.settings.changePassword.mismatchError
    return
  }

  if (passwordForm.newPassword.length < 8 || !PASSWORD_POLICY.test(passwordForm.newPassword)) {
    passwordError.value = t.value.settings.changePassword.policyHint
    return
  }

  isChangingPassword.value = true
  try {
    await authAPI.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    passwordSuccess.value = true
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    passwordError.value = error.response?.data?.message || t.value.settings.changePassword.error
  } finally {
    isChangingPassword.value = false
  }
}

const userName = computed(() => authStore.userName || 'Architect')
const userEmail = computed(() => authStore.user?.email || '')
const userInitials = computed(() =>
  userName.value
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
)
const canSwitchToClient = computed(() => authStore.hasRole('CLIENT'))

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}

const switchToClient = () => {
  authStore.updateLastLoginRole('CLIENT')
  router.push('/client/dashboard')
}
</script>
