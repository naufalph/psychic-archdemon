<template>
  <div class="min-h-screen bg-[#F4F5F7] py-12">
    <div class="max-w-5xl mx-auto px-6">
      <button class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition" @click="router.back()">
        <ArrowLeft :size="20" />
        Back
      </button>

      <div v-if="loading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded mb-6" />
      </div>

      <div v-else-if="error" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600">{{ error }}</p>
      </div>

      <div v-else-if="detail" class="space-y-6">
        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-black mb-2">{{ detail.project.title }}</h1>
              <p class="text-gray-500">{{ detail.project.location }} • {{ detail.project.projectCategory }}</p>
            </div>
            <ProjectStatusBadge :status="detail.project.status" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">
                {{ t.projectDetailArchitect.designBudgetRange }}
              </p>
              <p class="text-2xl font-bold text-black">
                {{ formatCurrency(detail.project.designBudgetMin) }} -
                {{ formatCurrency(detail.project.designBudgetMax) }}
              </p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.buildArea }}</p>
              <p class="text-2xl font-bold text-black">{{ detail.project.estimatedBuildArea }} m²</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.floors }}</p>
              <p class="text-2xl font-bold text-black">{{ detail.project.numberOfFloors }}</p>
            </div>
          </div>

          <div v-if="imageFiles.length > 0" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.visualReferences }}</h2>
            <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
              <div
                v-for="file in imageFiles"
                :key="file.id"
                class="relative aspect-square rounded-2xl overflow-hidden cursor-pointer group"
                @click="window.open(file.filePath, '_blank')"
              >
                <img
                  :src="file.filePath"
                  :alt="file.fileName"
                  class="w-full h-full object-cover transition group-hover:scale-105"
                />
              </div>
            </div>
          </div>

          <div class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.scopeOfWork }}</h2>
            <p class="text-gray-700 leading-relaxed">{{ detail.project.scopeOfWork }}</p>
          </div>

          <div v-if="detail.project.validationNotes" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">Validation Notes</h2>
            <p class="text-gray-700 leading-relaxed">{{ detail.project.validationNotes }}</p>
          </div>
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <h2 class="text-lg font-bold text-black mb-1">{{ t.superuserProjectDetail.clientDetails }}</h2>
          <p class="text-sm text-gray-500 mb-6">{{ t.superuserProjectDetail.clientDetailsHint }}</p>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.superuserProjectDetail.name }}</p>
              <p class="text-base font-medium text-gray-900">
                {{ detail.clientName || t.superuserProjectDetail.notProvided }}
              </p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.superuserProjectDetail.email }}</p>
              <a :href="`mailto:${detail.clientEmail}`" class="text-base font-medium text-[#7C4728] hover:underline">
                {{ detail.clientEmail || t.superuserProjectDetail.notProvided }}
              </a>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.superuserProjectDetail.phone }}</p>
              <div class="flex items-center gap-2">
                <a
                  v-if="detail.clientPhone"
                  :href="`tel:${detail.clientPhone}`"
                  class="text-base font-medium text-[#7C4728] hover:underline flex items-center gap-1.5"
                >
                  <Phone :size="16" />
                  {{ detail.clientPhone }}
                </a>
                <p v-else class="text-base font-medium text-gray-900">{{ t.superuserProjectDetail.notProvided }}</p>
                <span
                  v-if="detail.clientPhone"
                  class="text-xs px-2 py-0.5 rounded-full font-semibold whitespace-nowrap"
                  :class="detail.clientPhoneVerified ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'"
                >
                  {{
                    detail.clientPhoneVerified
                      ? t.superuserProjectDetail.phoneVerified
                      : t.superuserProjectDetail.phoneUnverified
                  }}
                </span>
              </div>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.superuserProjectDetail.ktp }}</p>
              <div class="flex items-center gap-2">
                <p class="text-base font-medium text-gray-900">
                  {{ detail.clientKtpNum || t.superuserProjectDetail.notProvided }}
                </p>
                <span
                  v-if="detail.clientKtpNum"
                  class="text-xs px-2 py-0.5 rounded-full font-semibold whitespace-nowrap"
                  :class="detail.clientKtpVerified ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'"
                >
                  {{
                    detail.clientKtpVerified
                      ? t.superuserProjectDetail.ktpVerified
                      : t.superuserProjectDetail.ktpUnverified
                  }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="detail.project.status === 'PENDING_APPROVAL'" class="flex gap-3 justify-end">
          <button
            :disabled="processing"
            class="px-5 py-2.5 text-sm font-semibold rounded-full border border-red-200 text-red-600 hover:bg-red-50 transition disabled:opacity-50"
            @click="openRejectModal"
          >
            {{ t.superuserProjectDetail.reject }}
          </button>
          <button
            :disabled="processing"
            class="px-5 py-2.5 text-sm font-semibold rounded-full bg-gray-900 text-white hover:bg-gray-700 transition disabled:opacity-50"
            @click="validate(true, null)"
          >
            {{ t.superuserProjectDetail.approve }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showRejectModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeRejectModal"></div>
      <div class="relative min-h-screen flex items-center justify-center p-4">
        <div class="relative bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
          <h3 class="text-lg font-bold text-gray-900 mb-1">{{ t.superuserProjectDetail.rejectModalTitle }}</h3>
          <p class="text-sm text-gray-500 mb-4">{{ t.superuserProjectDetail.rejectModalHint }}</p>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            {{ t.superuserProjectDetail.rejectReasonLabel }}
          </label>
          <textarea
            v-model="rejectReason"
            rows="4"
            :placeholder="t.superuserProjectDetail.rejectReasonPlaceholder"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-red-400 focus:border-red-400 outline-none text-sm"
          />
          <div class="flex gap-3 mt-5 justify-end">
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-gray-600 rounded-lg hover:bg-gray-100 transition"
              @click="closeRejectModal"
            >
              {{ t.superuserProjectDetail.rejectModalCancel }}
            </button>
            <button
              type="button"
              :disabled="!rejectReason.trim() || processing"
              class="px-4 py-2 text-sm font-semibold text-white bg-red-600 rounded-lg hover:bg-red-700 transition disabled:opacity-50"
              @click="confirmReject"
            >
              {{ t.superuserProjectDetail.rejectModalConfirm }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Phone } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'
import { adminProjectsAPI, superuserProjectsAPI } from '@/services/adminApi'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const detail = ref(null)
const loading = ref(true)
const error = ref(null)
const processing = ref(false)
const showRejectModal = ref(false)
const rejectReason = ref('')

const imageFiles = computed(() => (detail.value?.project?.files ?? []).filter(f => f.fileType?.startsWith('image/')))

const formatCurrency = value => {
  if (!value) return 'N/A'
  const millions = value / 1000000
  if (millions >= 1) return `Rp ${millions.toFixed(0)}M`
  const thousands = value / 1000
  return `Rp ${thousands.toFixed(0)}K`
}

const load = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await adminProjectsAPI.getDetail(route.params.id)
    detail.value = res.data.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to load project details'
  } finally {
    loading.value = false
  }
}

const openRejectModal = () => {
  rejectReason.value = ''
  showRejectModal.value = true
}

const closeRejectModal = () => {
  showRejectModal.value = false
  rejectReason.value = ''
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) return
  await validate(false, rejectReason.value.trim())
}

const validate = async (isValid, validationNotes) => {
  processing.value = true
  try {
    await superuserProjectsAPI.validate(detail.value.project.id, isValid, validationNotes)
    router.push({ name: 'ProjectValidationQueue' })
  } catch (e) {
    error.value = e.response?.data?.message || 'Validation failed'
    showRejectModal.value = false
  } finally {
    processing.value = false
  }
}

onMounted(load)
</script>
