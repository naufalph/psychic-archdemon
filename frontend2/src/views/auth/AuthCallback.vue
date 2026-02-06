<template>
  <div class="min-h-screen bg-[#F4F5F7] flex items-center justify-center p-6">
    <div
      class="absolute inset-0 opacity-[0.03] pointer-events-none"
      style="
        background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px);
        background-size: 50px 50px;
      "
    ></div>

    <div v-motion :initial="{ opacity: 0, y: 20 }" :enter="{ opacity: 1, y: 0 }" class="w-full max-w-md relative z-10">
      <div class="bg-white rounded-3xl shadow-2xl p-12 border border-gray-100 text-center">
        <router-link to="/" class="inline-block mb-8">
          <Logo class="h-10" />
        </router-link>

        <div v-if="isProcessing">
          <div class="w-16 h-16 bg-[#C5A17A]/10 rounded-full flex items-center justify-center mx-auto mb-6">
            <Loader2 class="w-8 h-8 text-[#C5A17A] animate-spin" />
          </div>
          <h1 class="text-2xl font-bold mb-3 text-black">{{ t.auth.callback.processing }}</h1>
          <p class="text-gray-500">{{ t.auth.callback.pleaseWait }}</p>
        </div>

        <div v-else-if="isSuccess">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <CheckCircle2 class="w-8 h-8 text-green-600" />
          </div>
          <h1 class="text-2xl font-bold mb-3 text-black">{{ t.auth.callback.success }}</h1>
          <p class="text-gray-500 mb-6">{{ t.auth.callback.redirecting }}</p>
          <div class="flex items-center justify-center gap-2 text-[#C5A17A]">
            <div class="w-4 h-4 border-2 border-[#C5A17A] border-t-transparent rounded-full animate-spin"></div>
            <span class="text-sm">{{ t.auth.callback.takingYou }}</span>
          </div>
        </div>

        <div v-else>
          <div class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <XCircle class="w-8 h-8 text-red-600" />
          </div>
          <h1 class="text-2xl font-bold mb-3 text-black">{{ t.auth.callback.error }}</h1>
          <p class="text-gray-500 mb-6">{{ errorMessage || t.auth.callback.genericError }}</p>
          <BaseButton
            @click="goToLogin"
            :fullWidth="true"
            class="bg-[#C5A17A] hover:bg-[#B39069] text-white border-none"
          >
            {{ t.auth.callback.returnToLogin }}
          </BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'
import BaseButton from '@/components/ui/BaseButton.vue'
import Logo from '@/components/ui/Logo.vue'
import { Loader2, CheckCircle2, XCircle } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { t } = useI18n()

const isProcessing = ref(true)
const isSuccess = ref(false)
const errorMessage = ref('')

const getRedirectPath = user => {
  const roles = user?.registeredRoles || []

  // Priority 1: Check onboarding status
  if (user?.needsArchitectOnboarding === true) {
    return '/architect/onboarding'
  }

  // Priority 2: Use lastLoginRole from database
  if (user?.lastLoginRole && roles.includes(user.lastLoginRole)) {
    return user.lastLoginRole === 'ARCHITECT' ? '/architect/dashboard' : '/client/dashboard'
  }

  // Priority 3: Single role
  if (roles.length === 1) {
    return roles[0] === 'ARCHITECT' ? '/architect/dashboard' : '/client/dashboard'
  }

  // Priority 4: Default to CLIENT if user has both roles
  if (roles.includes('CLIENT')) {
    return '/client/dashboard'
  }

  if (roles.includes('ARCHITECT')) {
    return '/architect/dashboard'
  }

  // Priority 5: No roles
  return '/'
}

const processCallback = () => {
  const success = route.query.success
  const token = route.query.token
  const email = route.query.email
  const id = route.query.id
  const rolesParam = route.query.roles
  const needsArchitectOnboarding = route.query.needsArchitectOnboarding
  const needsClientOnboarding = route.query.needsClientOnboarding
  const lastLoginRole = route.query.lastLoginRole
  const error = route.query.error

  if (success === 'true' && token && email && id) {
    const registeredRoles = rolesParam ? rolesParam.split(',').filter(r => r) : []

    localStorage.setItem('auth_token', token)
    authStore.user = {
      id,
      email,
      registeredRoles,
      needsArchitectOnboarding:
        needsArchitectOnboarding === 'true' ? true : needsArchitectOnboarding === 'false' ? false : null,
      needsClientOnboarding: needsClientOnboarding === 'true' ? true : needsClientOnboarding === 'false' ? false : null,
      lastLoginRole: lastLoginRole || null
    }
    authStore.token = token

    isSuccess.value = true
    isProcessing.value = false

    const redirectPath = getRedirectPath(authStore.user)
    setTimeout(() => {
      router.push(redirectPath)
    }, 1500)
  } else if (success === 'false' || error) {
    errorMessage.value = error ? decodeURIComponent(error) : ''
    isSuccess.value = false
    isProcessing.value = false
  } else {
    errorMessage.value = ''
    isSuccess.value = false
    isProcessing.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  processCallback()
})
</script>
