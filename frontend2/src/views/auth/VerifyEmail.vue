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

        <div v-if="isLoading" class="py-12">
          <div
            class="w-16 h-16 border-4 border-[#C5A17A] border-t-transparent rounded-full animate-spin mx-auto mb-6"
          ></div>
          <h2 class="text-2xl font-bold text-black mb-3">
            {{ t.auth?.verifyEmail?.verifying || 'Verifying your email...' }}
          </h2>
          <p class="text-gray-500">
            {{ t.auth?.verifyEmail?.pleaseWait || 'Please wait a moment' }}
          </p>
        </div>

        <div
          v-else-if="verificationSuccess"
          v-motion
          :initial="{ opacity: 0, scale: 0.9 }"
          :enter="{ opacity: 1, scale: 1 }"
        >
          <ConfettiExplosion
            v-if="showConfetti"
            :particleCount="80"
            :force="0.3"
            :duration="2500"
            :colors="['#C5A17A', '#7C4728', '#10B981', '#FBBF24']"
          />

          <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <CheckCircle2 class="w-10 h-10 text-green-600" />
          </div>

          <h2 class="text-3xl font-bold text-black mb-4">
            {{ t.auth?.verifyEmail?.success || 'Email Verified!' }}
          </h2>
          <p class="text-gray-600 mb-8 leading-relaxed">
            {{
              t.auth?.verifyEmail?.successMessage ||
              'Your email has been verified successfully. You can now sign in to your account.'
            }}
          </p>

          <router-link to="/login">
            <BaseButton :fullWidth="true" class="bg-[#C5A17A] hover:bg-[#B39069] text-white border-none">
              {{ t.auth?.verifyEmail?.continueToLogin || 'Continue to Sign In' }}
            </BaseButton>
          </router-link>
        </div>

        <div v-else v-motion :initial="{ opacity: 0, scale: 0.9 }" :enter="{ opacity: 1, scale: 1 }">
          <div class="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <XCircle class="w-10 h-10 text-red-600" />
          </div>

          <h2 class="text-3xl font-bold text-black mb-4">
            {{ t.auth?.verifyEmail?.failed || 'Verification Failed' }}
          </h2>
          <p class="text-gray-600 mb-2 leading-relaxed">
            {{
              errorMessage || t.auth?.verifyEmail?.failedMessage || 'The verification link is invalid or has expired.'
            }}
          </p>

          <div class="mt-8 space-y-4">
            <div v-if="!resendLoading && !resendSuccess" class="flex flex-col gap-4">
              <BaseInput
                v-model="resendEmail"
                :label="t.auth?.verifyEmail?.emailLabel || 'Email'"
                type="email"
                placeholder="your@email.com"
              />
              <BaseButton
                @click="handleResendVerification"
                :fullWidth="true"
                :isLoading="resendLoading"
                class="bg-[#7C4728] hover:bg-black text-white border-none"
              >
                {{ t.auth?.verifyEmail?.resend || 'Resend Verification Email' }}
              </BaseButton>
            </div>

            <div v-else-if="resendSuccess" class="bg-green-50 border border-green-200 rounded-2xl p-4">
              <p class="text-green-800 text-sm">
                {{ t.auth?.verifyEmail?.resendSuccess || 'Verification email sent! Please check your inbox.' }}
              </p>
            </div>

            <router-link to="/login">
              <button
                class="w-full px-10 py-3.5 text-base font-bold tracking-tight rounded-full border-2 border-gray-200 hover:bg-gray-50 hover:border-gray-300 transition-all"
              >
                {{ t.auth?.verifyEmail?.backToLogin || 'Back to Sign In' }}
              </button>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import Logo from '@/components/ui/Logo.vue'
import ConfettiExplosion from 'vue-confetti-explosion'
import { CheckCircle2, XCircle } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()

const isLoading = ref(true)
const verificationSuccess = ref(false)
const showConfetti = ref(false)
const errorMessage = ref('')
const resendEmail = ref('')
const resendLoading = ref(false)
const resendSuccess = ref(false)

onMounted(async () => {
  const token = route.query.token

  if (!token) {
    isLoading.value = false
    errorMessage.value = 'No verification token provided'
    return
  }

  try {
    const response = await authStore.verifyEmail(token)
    console.log('Verification response:', response)

    verificationSuccess.value = true
    showConfetti.value = true

    const pendingEmail = localStorage.getItem('pending_user_email')

    localStorage.removeItem('pending_user_role')
    localStorage.removeItem('pending_user_email')

    setTimeout(() => {
      router.push({
        name: 'Login',
        query: {
          verified: 'true',
          email: pendingEmail || ''
        }
      })
    }, 3000)
  } catch (error) {
    console.error('Email verification failed:', error)
    console.error('Error response:', error.response)
    verificationSuccess.value = false
    errorMessage.value = error.response?.data?.message || 'Verification failed'
  } finally {
    isLoading.value = false
  }
})

const handleResendVerification = async () => {
  if (!resendEmail.value || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(resendEmail.value)) {
    errorMessage.value = 'Please enter a valid email address'
    return
  }

  resendLoading.value = true

  try {
    await authStore.resendVerification(resendEmail.value)
    resendSuccess.value = true
  } catch (error) {
    console.error('Resend verification failed:', error)
    errorMessage.value = error.response?.data?.message || 'Failed to resend verification email'
  } finally {
    resendLoading.value = false
  }
}
</script>
