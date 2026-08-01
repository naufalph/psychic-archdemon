<template>
  <div class="min-h-full bg-surface-alt">
    <div class="max-w-2xl mx-auto px-6 py-12">
      <button
        @click="$router.push({ name: 'ArchitectDashboard' })"
        class="flex items-center gap-2 text-black/60 hover:text-black transition-colors mb-6"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        {{ t.common.backToDashboard }}
      </button>

      <div class="space-y-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-4xl font-black tracking-tighter text-black">
              {{ t.profile.title }}
            </h1>
            <p class="text-lg text-black/60 tracking-tight mt-2">Manage your architect profile information</p>
          </div>
          <button
            v-if="!store.isEditMode && store.hasProfile"
            @click="store.enableEditMode"
            class="px-6 py-3 bg-brand-brown text-white rounded-full font-semibold hover:bg-brand-brown-dark transition-all hover:scale-105"
          >
            {{ t.profile.editProfile }}
          </button>
        </div>

        <div v-if="store.isLoading && !store.hasProfile" class="space-y-6">
          <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5 animate-pulse">
            <div class="space-y-6">
              <div>
                <div class="h-4 bg-gray-200 rounded w-24 mb-2"></div>
                <div class="h-6 bg-gray-200 rounded w-48"></div>
              </div>
              <div>
                <div class="h-4 bg-gray-200 rounded w-24 mb-2"></div>
                <div class="h-6 bg-gray-200 rounded w-32"></div>
              </div>
              <div>
                <div class="h-4 bg-gray-200 rounded w-32 mb-2"></div>
                <div class="h-6 bg-gray-200 rounded w-40"></div>
              </div>
              <div>
                <div class="h-4 bg-gray-200 rounded w-32 mb-2"></div>
                <div class="h-24 bg-gray-200 rounded w-full"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="store.hasProfile && !store.isEditMode" class="space-y-6">
          <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5 space-y-6">
            <div>
              <label class="block text-sm font-semibold text-black/50 mb-2">
                {{ t.profile.viewMode.practiceName }}
              </label>
              <p class="text-lg font-semibold text-black">{{ store.profileName }}</p>
            </div>

            <div class="grid grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-semibold text-black/50 mb-2">
                  {{ t.profile.viewMode.city }}
                </label>
                <p class="text-lg font-semibold text-black">{{ store.profileCity }}</p>
              </div>

              <div>
                <label class="block text-sm font-semibold text-black/50 mb-2">
                  {{ t.profile.viewMode.experience }}
                </label>
                <p class="text-lg font-semibold text-black">{{ store.profileExperience }}</p>
              </div>
            </div>

            <div>
              <label class="block text-sm font-semibold text-black/50 mb-2">
                {{ t.profile.viewMode.philosophy }}
              </label>
              <p class="text-base text-black/80 leading-relaxed whitespace-pre-wrap">
                {{ store.profilePhilosophy }}
              </p>
            </div>

            <div>
              <label class="block text-sm font-semibold text-black/50 mb-3">
                {{ t.profile.viewMode.expertise }}
              </label>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="tag in store.profileExpertise"
                  :key="tag"
                  class="px-4 py-2 bg-amber-100 text-amber-900 rounded-full text-sm font-medium border border-amber-200"
                >
                  {{ tag }}
                </span>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-semibold text-black/50 mb-2">
                  {{ t.profile.viewMode.ktpNum }}
                </label>
                <p class="text-lg font-semibold text-black">{{ store.profileKtpNum || '-' }}</p>
                <div class="flex items-center gap-2 mt-1">
                  <span
                    :class="[
                      'px-3 py-1 rounded-full text-xs font-medium',
                      store.profileKtpVerified
                        ? 'bg-green-100 text-green-800 border border-green-200'
                        : 'bg-gray-100 text-gray-600 border border-gray-200'
                    ]"
                  >
                    {{ store.profileKtpVerified ? t.profile.viewMode.verified : t.profile.viewMode.notVerified }}
                  </span>
                </div>
              </div>

              <div>
                <label class="block text-sm font-semibold text-black/50 mb-2">
                  {{ t.profile.viewMode.npwp }}
                </label>
                <p class="text-lg font-semibold text-black">{{ store.profileNpwp || '-' }}</p>
                <div class="flex items-center gap-2 mt-1">
                  <span
                    :class="[
                      'px-3 py-1 rounded-full text-xs font-medium',
                      store.profileNpwpVerified
                        ? 'bg-green-100 text-green-800 border border-green-200'
                        : 'bg-gray-100 text-gray-600 border border-gray-200'
                    ]"
                  >
                    {{ store.profileNpwpVerified ? t.profile.viewMode.verified : t.profile.viewMode.notVerified }}
                  </span>
                </div>
              </div>
            </div>

            <div v-if="store.profileFullnameKtp">
              <label class="block text-sm font-semibold text-black/50 mb-2">
                {{ t.identityDocs?.fullnameKtp || 'Full Name (as in KTP)' }}
              </label>
              <p class="text-lg font-semibold text-black">{{ store.profileFullnameKtp }}</p>
            </div>
          </div>

          <!-- Phone Verification Card -->
          <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5 space-y-5">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-lg font-bold text-black">
                  {{ t.identityDocs?.phoneNum || 'Mobile Phone Number' }}
                </h3>
                <p v-if="store.profilePhoneNumber" class="text-base text-black/70 mt-1">
                  {{ store.profilePhoneNumber }}
                </p>
              </div>
              <span
                :class="[
                  'px-3 py-1 rounded-full text-xs font-medium',
                  store.profilePhoneVerified
                    ? 'bg-green-100 text-green-800 border border-green-200'
                    : 'bg-amber-100 text-amber-800 border border-amber-200'
                ]"
              >
                {{
                  store.profilePhoneVerified
                    ? t.identityDocs?.verified || 'Verified'
                    : t.identityDocs?.notVerified || 'Not verified'
                }}
              </span>
            </div>

            <div v-if="!store.profilePhoneVerified" class="space-y-4">
              <div class="flex gap-3">
                <input
                  v-model="otpPhone"
                  type="tel"
                  :placeholder="t.identityDocs?.phoneNumPlaceholder || 'e.g., 08123456789'"
                  class="flex-1 px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all text-sm"
                />
                <button
                  @click="handleSendOtp"
                  :disabled="!otpPhone || otpSending"
                  class="px-5 py-3 bg-brand-brown text-white rounded-2xl text-sm font-semibold hover:bg-brand-brown-dark transition-all disabled:opacity-40 disabled:cursor-not-allowed whitespace-nowrap"
                >
                  {{ otpSending ? '...' : t.identityDocs?.sendOtp || 'Send OTP' }}
                </button>
              </div>

              <div v-if="otpSent" class="space-y-3">
                <p class="text-sm text-green-700 font-medium">
                  {{ t.identityDocs?.otpSent || 'OTP sent to your WhatsApp' }}
                </p>
                <div class="flex gap-3">
                  <input
                    v-model="otpCode"
                    type="text"
                    maxlength="6"
                    :placeholder="t.identityDocs?.otpCodePlaceholder || '6-digit OTP'"
                    class="flex-1 px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all text-sm tracking-widest"
                    @input="otpCode = otpCode.replace(/\D/g, '')"
                  />
                  <button
                    @click="handleVerifyOtp"
                    :disabled="otpCode.length !== 6 || otpVerifying"
                    class="px-5 py-3 bg-brand-brown text-white rounded-2xl text-sm font-semibold hover:bg-brand-brown-dark transition-all disabled:opacity-40 disabled:cursor-not-allowed"
                  >
                    {{
                      otpVerifying ? t.identityDocs?.verifying || 'Verifying...' : t.identityDocs?.verify || 'Verify'
                    }}
                  </button>
                </div>
                <button @click="handleSendOtp" class="text-xs text-brand-brown hover:underline">
                  {{ t.identityDocs?.resendOtp || 'Resend OTP' }}
                </button>
              </div>

              <p v-if="otpError" class="text-sm text-red-600">{{ otpError }}</p>
            </div>
          </div>
        </div>

        <ProfileForm
          v-else-if="store.isEditMode"
          :initial-data="{
            name: store.profileName,
            city: store.profileCity,
            experienceRange: store.profileExperience,
            philosophy: store.profilePhilosophy,
            expertise: store.profileExpertise,
            ktpNum: store.profileKtpNum,
            npwp: store.profileNpwp
          }"
          :is-loading="store.isLoading"
          :error="store.error"
          @submit="handleSaveProfile"
          @cancel="handleCancel"
        />

        <Transition
          enter-active-class="transition ease-out duration-300"
          enter-from-class="transform opacity-0 translate-y-2"
          enter-to-class="transform opacity-100 translate-y-0"
          leave-active-class="transition ease-in duration-200"
          leave-from-class="transform opacity-100 translate-y-0"
          leave-to-class="transform opacity-0 translate-y-2"
        >
          <div
            v-if="showToast"
            class="fixed bottom-6 right-6 bg-white rounded-2xl shadow-xl border border-gray-200 p-4 flex items-center gap-3 max-w-md z-50"
          >
            <div :class="['w-2 h-2 rounded-full', toastType === 'success' ? 'bg-green-500' : 'bg-red-500']"></div>
            <p class="text-sm font-medium text-black">{{ toastMessage }}</p>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useArchitectProfileStore } from '@/stores/architectProfile'
import { useI18n } from '@/composables/useI18n'
import ProfileForm from '@/components/architect/ProfileForm.vue'

const store = useArchitectProfileStore()
const { t } = useI18n()

const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

const otpPhone = ref('')
const otpCode = ref('')
const otpSent = ref(false)
const otpSending = ref(false)
const otpVerifying = ref(false)
const otpError = ref('')

const displayToast = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 5000)
}

const handleSaveProfile = async profileData => {
  try {
    await store.updateProfile(profileData)
    displayToast(t('profile.toast.updateSuccess'), 'success')
  } catch (error) {
    displayToast(t('profile.toast.updateError'), 'error')
  }
}

const handleCancel = () => {
  store.disableEditMode()
  store.clearError()
}

const handleSendOtp = async () => {
  otpError.value = ''
  otpSending.value = true
  try {
    await store.sendPhoneOtp(otpPhone.value)
    otpSent.value = true
    otpCode.value = ''
  } catch {
    otpError.value = store.error || t.value.identityDocs?.otpError || 'Gagal mengirim OTP.'
  } finally {
    otpSending.value = false
  }
}

const handleVerifyOtp = async () => {
  otpError.value = ''
  otpVerifying.value = true
  try {
    await store.verifyPhoneOtp(otpPhone.value, otpCode.value)
    otpSent.value = false
    displayToast(t.value.identityDocs?.phoneVerifiedSuccess || 'Phone number successfully verified.', 'success')
  } catch {
    otpError.value = store.error || t.value.identityDocs?.otpError || 'OTP tidak valid.'
  } finally {
    otpVerifying.value = false
  }
}

onMounted(async () => {
  try {
    await store.fetchProfile()
    if (store.profilePhoneNumber) {
      otpPhone.value = store.profilePhoneNumber
    }
  } catch (error) {
    displayToast(t.value.profile?.toast?.loadError || 'Failed to load profile', 'error')
  }
})
</script>
