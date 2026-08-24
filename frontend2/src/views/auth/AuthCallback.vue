<template>
  <div class="min-h-screen bg-surface-alt flex items-center justify-center p-6">
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
          <div class="w-16 h-16 bg-brand-gold/10 rounded-full flex items-center justify-center mx-auto mb-6">
            <Loader2 class="w-8 h-8 text-brand-gold animate-spin" />
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
          <div class="flex items-center justify-center gap-2 text-brand-gold">
            <div class="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin"></div>
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
            :full-width="true"
            class="bg-brand-gold hover:bg-brand-gold-light text-white border-none"
            @click="goToLogin"
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
import { useProjectBrief } from '@/composables/useProjectBrief'
import { useI18n } from '@/composables/useI18n'
import BaseButton from '@/components/ui/BaseButton.vue'
import Logo from '@/components/ui/Logo.vue'
import { Loader2, CheckCircle2, XCircle } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { pendingBriefPath } = useProjectBrief()
const { t } = useI18n()

const isProcessing = ref(true)
const isSuccess = ref(false)
const errorMessage = ref('')

const getRedirectPath = user => {
  const roles = user?.registeredRoles || []

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
      needsClientOnboarding: needsClientOnboarding === 'true' ? true : needsClientOnboarding === 'false' ? false : null,
      lastLoginRole: lastLoginRole || null
    }
    authStore.token = token

    isSuccess.value = true
    isProcessing.value = false

    // OAuth cannot carry ?redirect= through the provider round-trip, so a brief left in
    // this browser is the only signal that the user came from the landing mini-form.
    const redirectPath = pendingBriefPath(authStore.user) || getRedirectPath(authStore.user)
    setTimeout(() => {
      router.push(redirectPath)
    }, 1500)
  } else if (success === 'false' || error) {
    const decodedError = error ? decodeURIComponent(error) : ''

    if (decodedError === 'STALE_TERMS') {
      router.replace('/signup?termsUpdated=true')
      return
    }

    errorMessage.value = t.value.errors?.[decodedError] || decodedError
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
