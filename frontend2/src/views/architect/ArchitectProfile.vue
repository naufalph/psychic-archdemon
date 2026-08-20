<template>
  <div class="min-h-full bg-surface-alt">
    <div class="max-w-2xl mx-auto px-6 py-12">
      <button
        class="flex items-center gap-2 text-black/60 hover:text-black transition-colors mb-6"
        @click="$router.push({ name: 'ArchitectDashboard' })"
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
            class="px-6 py-3 bg-brand-brown text-white rounded-full font-semibold hover:bg-brand-brown-dark transition-all hover:scale-105"
            @click="store.enableEditMode"
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
              </div>

              <div>
                <label class="block text-sm font-semibold text-black/50 mb-2">
                  {{ t.profile.viewMode.npwp }}
                </label>
                <p class="text-lg font-semibold text-black">{{ store.profileNpwp || '-' }}</p>
              </div>
            </div>

            <div v-if="store.profileFullnameKtp">
              <label class="block text-sm font-semibold text-black/50 mb-2">
                {{ t.identityDocs?.fullnameKtp || 'Full Name (as in KTP)' }}
              </label>
              <p class="text-lg font-semibold text-black">{{ store.profileFullnameKtp }}</p>
            </div>

            <div v-if="store.profilePhoneNumber">
              <label class="block text-sm font-semibold text-black/50 mb-2">
                {{ t.identityDocs?.phoneNum || 'Mobile Phone Number' }}
              </label>
              <p class="text-lg font-semibold text-black">{{ store.profilePhoneNumber }}</p>
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
            fullnameKtp: store.profileFullnameKtp,
            ktpNum: store.profileKtpNum,
            npwp: store.profileNpwp,
            phoneNum: store.profilePhoneNumber
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

onMounted(async () => {
  try {
    await store.fetchProfile()
  } catch (error) {
    displayToast(t.value.profile?.toast?.loadError || 'Failed to load profile', 'error')
  }
})
</script>
