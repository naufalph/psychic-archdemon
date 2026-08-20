<template>
  <div
    v-motion
    :initial="{ opacity: 0, y: 20 }"
    :enter="{ opacity: 1, y: 0, transition: { duration: 600 } }"
    class="max-w-2xl mx-auto py-12"
  >
    <div class="space-y-8">
      <div class="space-y-3">
        <h2 class="text-5xl font-black tracking-tighter text-black">
          {{ t.identityDocs?.title || 'Identity Verification' }}
        </h2>
        <p class="text-lg text-black/60 tracking-tight">
          {{ t.identityDocs?.subtitle || 'Required before submitting bids. You can skip this for now.' }}
        </p>
      </div>

      <div class="bg-amber-50 border border-amber-200 rounded-2xl p-5 flex gap-3">
        <svg class="w-5 h-5 text-amber-500 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <p class="text-sm text-amber-800 leading-relaxed">
          {{
            t.identityDocs?.notice ||
            'KTP, NPWP, and full name will be verified manually by the Rumantra team. Phone number requires WhatsApp OTP verification, which can be done from your Profile page.'
          }}
        </p>
      </div>

      <div class="space-y-6 bg-white rounded-3xl p-8 shadow-sm border border-black/5">
        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">
            {{ t.identityDocs?.fullnameKtp || 'Full Name (as in KTP)' }}
          </label>
          <input
            v-model="formData.fullnameKtp"
            type="text"
            :placeholder="t.identityDocs?.fullnameKtpPlaceholder || 'e.g., Budi Santoso'"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
          />
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">
            {{ t.identityDocs?.ktpNum || 'KTP Number' }}
          </label>
          <input
            v-model="formData.ktpNum"
            type="text"
            maxlength="16"
            :placeholder="t.identityDocs?.ktpNumPlaceholder || '16-digit KTP number'"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
            @input="formData.ktpNum = formData.ktpNum.replace(/\D/g, '')"
          />
          <p v-if="formData.ktpNum && !/^[0-9]{16}$/.test(formData.ktpNum)" class="text-xs text-red-500">
            {{ t.identityDocs?.ktpNumError || 'KTP number must be exactly 16 digits' }}
          </p>
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">
            {{ t.identityDocs?.npwp || 'NPWP' }}
          </label>
          <input
            v-model="formData.npwp"
            type="text"
            maxlength="16"
            :placeholder="t.identityDocs?.npwpPlaceholder || '15–16 digit NPWP number'"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
            @input="formData.npwp = formData.npwp.replace(/\D/g, '')"
          />
          <p v-if="formData.npwp && !/^[0-9]{15,16}$/.test(formData.npwp)" class="text-xs text-red-500">
            {{ t.identityDocs?.npwpError || 'NPWP must be 15 or 16 digits' }}
          </p>
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">
            {{ t.identityDocs?.phoneNum || 'Mobile Phone Number' }}
          </label>
          <input
            v-model="formData.phoneNum"
            type="tel"
            :placeholder="t.identityDocs?.phoneNumPlaceholder || 'e.g., 08123456789'"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
          />
          <p class="text-xs text-black/40">
            {{ t.identityDocs?.phoneNumHint || 'OTP verification via WhatsApp is done from your Profile page.' }}
          </p>
        </div>
      </div>

      <p v-if="store.error" class="text-sm text-red-600 px-1">{{ store.error }}</p>

      <div class="flex justify-between items-center pt-4">
        <button class="px-6 py-3 text-black/60 hover:text-black transition-colors" @click="emit('back')">
          {{ t.identityDocs?.back || 'Back' }}
        </button>
        <div class="flex gap-3">
          <button
            class="px-6 py-3 border border-black/10 rounded-full text-black/60 hover:bg-black/5 transition-all text-sm font-medium"
            @click="handleSkip"
          >
            {{ t.identityDocs?.skip || 'Skip for now' }}
          </button>
          <button
            :disabled="!hasAnyField || !isFormValid || store.isLoading"
            :class="[
              'px-8 py-3 rounded-full font-semibold transition-all text-sm',
              hasAnyField && isFormValid && !store.isLoading
                ? 'bg-brand-brown text-white hover:bg-brand-brown-dark hover:scale-105'
                : 'bg-black/10 text-black/30 cursor-not-allowed'
            ]"
            @click="handleSave"
          >
            {{ store.isLoading ? t.identityDocs?.saving || 'Saving...' : t.identityDocs?.save || 'Save & Continue' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useOnboardingStore } from '@/stores/onboarding'
import { useI18n } from '@/composables/useI18n'

const emit = defineEmits(['next', 'back'])
const store = useOnboardingStore()
const { t } = useI18n()

const formData = ref({
  fullnameKtp: store.identityDocs.fullnameKtp || '',
  ktpNum: store.identityDocs.ktpNum || '',
  npwp: store.identityDocs.npwp || '',
  phoneNum: store.identityDocs.phoneNum || ''
})

const hasAnyField = computed(
  () => formData.value.fullnameKtp || formData.value.ktpNum || formData.value.npwp || formData.value.phoneNum
)

const isFormValid = computed(() => {
  const { ktpNum, npwp } = formData.value
  if (ktpNum && !/^[0-9]{16}$/.test(ktpNum)) return false
  if (npwp && !/^[0-9]{15,16}$/.test(npwp)) return false
  return true
})

const handleSkip = () => {
  store.error = null
  emit('next')
}

const handleSave = async () => {
  if (!hasAnyField.value || !isFormValid.value) return
  store.updateIdentityDocs(formData.value)
  try {
    await store.saveIdentityDocsToBackend()
    emit('next')
  } catch {
    // store.error already set by the action
  }
}
</script>
