<template>
  <div class="min-h-full bg-[#F4F5F7]">
    <div class="max-w-4xl mx-auto px-6 py-12">
      <div class="bg-white rounded-2xl shadow-sm overflow-hidden border border-gray-200">
        <div class="bg-[#1C1C1C] px-8 py-6">
          <button
            @click="goToDashboard"
            class="flex items-center gap-2 text-white hover:text-white/70 transition-colors mb-4 group"
          >
            <ArrowLeftIcon class="w-5 h-5 group-hover:-translate-x-1 transition-transform" />
            <span class="font-medium">{{ t.common.backToDashboard }}</span>
          </button>
          <h1 class="text-3xl font-bold text-white">{{ t.clientProfile.title }}</h1>
          <p class="text-white/60 mt-2">{{ t.clientProfile.subtitle }}</p>
        </div>

        <div class="p-8">
          <div v-if="error" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
            <p class="text-sm text-red-600">{{ error }}</p>
          </div>

          <div v-if="isLoading && !hasProfile" class="space-y-4">
            <div class="animate-pulse">
              <div class="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
              <div class="h-10 bg-gray-200 rounded mb-4"></div>
              <div class="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
              <div class="h-10 bg-gray-200 rounded mb-4"></div>
            </div>
          </div>

          <div v-else-if="!isEditMode">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-xl font-semibold text-gray-900">{{ t.clientProfile.viewMode.title }}</h2>
              <button
                @click="enableEdit"
                class="px-4 py-2 bg-[#1C1C1C] text-white font-semibold rounded-lg hover:bg-[#7C4728] focus:outline-none focus:ring-2 focus:ring-[#7C4728] focus:ring-offset-2 transition-colors"
              >
                {{ t.clientProfile.editProfile }}
              </button>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
              <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
                <p class="text-sm font-medium text-gray-500 mb-1">{{ t.clientProfile.viewMode.firstName }}</p>
                <p class="text-base font-semibold text-gray-900">{{ firstName }}</p>
              </div>

              <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
                <p class="text-sm font-medium text-gray-500 mb-1">{{ t.clientProfile.viewMode.lastName }}</p>
                <p class="text-base font-semibold text-gray-900">{{ lastName }}</p>
              </div>

              <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
                <p class="text-sm font-medium text-gray-500 mb-1">{{ t.clientProfile.viewMode.email }}</p>
                <p class="text-base font-semibold text-gray-900">{{ profileEmail }}</p>
              </div>

              <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
                <p class="text-sm font-medium text-gray-500 mb-1">{{ t.clientProfile.viewMode.phoneNumber }}</p>
                <p class="text-base font-semibold text-gray-900">{{ profilePhone || t.common.notSet }}</p>
              </div>

              <div class="bg-gray-50 rounded-lg p-4 border border-gray-200 md:col-span-2">
                <div class="flex items-center justify-between">
                  <div class="flex-1">
                    <p class="text-sm font-medium text-gray-500 mb-1">{{ t.clientProfile.viewMode.ktpNumber }}</p>
                    <p class="text-base font-semibold text-gray-900">{{ ktpNumber || t.common.notSet }}</p>
                  </div>
                  <div
                    v-if="ktpVerified"
                    class="flex items-center gap-2 px-3 py-1 bg-green-100 text-green-700 rounded-full"
                  >
                    <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fill-rule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                        clip-rule="evenodd"
                      />
                    </svg>
                    <span class="text-sm font-medium">{{ t.clientProfile.viewMode.verified }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 rounded-lg p-6 border border-gray-200">
              <h3 class="text-lg font-semibold text-gray-900 mb-4">
                {{ t.clientProfile.viewMode.projectStats }}
              </h3>
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-white rounded-lg p-4 text-center">
                  <p class="text-3xl font-bold text-[#1C1C1C]">{{ projectStats.matched }}</p>
                  <p class="text-sm text-gray-600 mt-1">{{ t.clientProfile.viewMode.projectsMatched }}</p>
                </div>
                <div class="bg-white rounded-lg p-4 text-center">
                  <p class="text-3xl font-bold text-green-600">{{ projectStats.finished }}</p>
                  <p class="text-sm text-gray-600 mt-1">{{ t.clientProfile.viewMode.projectsCompleted }}</p>
                </div>
              </div>
            </div>
          </div>

          <div v-else>
            <div class="mb-6">
              <h2 class="text-xl font-semibold text-gray-900">{{ t.clientProfile.editProfile }}</h2>
            </div>

            <ProfileForm
              :profile-email="profileEmail"
              :ktp-number="ktpNumber"
              :ktp-verified="ktpVerified"
              :initial-data="{
                firstName: firstName,
                lastName: lastName,
                phoneNumber: profilePhone,
                ktpNum: ktpNumber
              }"
              :is-loading="isLoading"
              @submit="handleSaveProfile"
              @cancel="handleCancel"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useClientProfileStore } from '@/stores/clientProfile'
import { useI18n } from '@/composables/useI18n'
import ProfileForm from '@/components/client/ProfileForm.vue'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'

const router = useRouter()
const { t, getT } = useI18n()
const clientProfileStore = useClientProfileStore()

const error = ref(null)

const isLoading = computed(() => clientProfileStore.isLoading)
const hasProfile = computed(() => clientProfileStore.hasProfile)
const isEditMode = computed(() => clientProfileStore.isEditMode)
const firstName = computed(() => clientProfileStore.firstName)
const lastName = computed(() => clientProfileStore.lastName)
const profileEmail = computed(() => clientProfileStore.profileEmail)
const profilePhone = computed(() => clientProfileStore.profilePhone)
const ktpNumber = computed(() => clientProfileStore.ktpNumber)
const ktpVerified = computed(() => clientProfileStore.ktpVerified)
const projectStats = computed(() => clientProfileStore.projectStats)

onMounted(async () => {
  try {
    await clientProfileStore.fetchProfile()
  } catch (err) {
    error.value = t.clientProfile.toast.loadError
  }
})

const goToDashboard = () => {
  router.push('/client/dashboard')
}

const enableEdit = () => {
  clientProfileStore.enableEditMode()
  error.value = null
}

const handleSaveProfile = async profileData => {
  error.value = null

  try {
    await clientProfileStore.updateProfile(profileData)
    showSuccessToast()
  } catch (err) {
    error.value = clientProfileStore.error || getT('clientProfile.toast.updateError')
  }
}

const handleCancel = () => {
  clientProfileStore.disableEditMode()
  error.value = null
}

const showSuccessToast = () => {
  const toast = document.createElement('div')
  toast.className = 'fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg z-50 animate-fade-in'
  toast.textContent = getT('clientProfile.toast.updateSuccess')
  document.body.appendChild(toast)

  setTimeout(() => {
    toast.remove()
  }, 3000)
}
</script>
