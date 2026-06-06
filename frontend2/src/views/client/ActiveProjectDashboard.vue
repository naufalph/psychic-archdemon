<template>
  <div class="min-h-screen bg-[#F4F5F7]">
    <div v-if="loading" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <div class="w-10 h-10 border-2 border-[#C5A17A] border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p class="text-gray-500">{{ t.activeProjectDashboard.loading }}</p>
      </div>
    </div>

    <div v-else-if="error" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button @click="loadAll" class="text-[#7C4728] hover:underline">{{ t.activeProjectDashboard.tryAgain }}</button>
      </div>
    </div>

    <div v-else>
      <!-- Header -->
      <div class="bg-white border-b border-gray-200 px-6 py-4">
        <div class="max-w-6xl mx-auto flex items-center justify-between">
          <div class="flex items-center gap-4">
            <button @click="router.push('/client/dashboard')" class="text-gray-500 hover:text-black transition">
              <ArrowLeft :size="20" />
            </button>
            <div>
              <p class="text-xs text-gray-400 uppercase font-bold tracking-wide">{{ t.activeProjectDashboard.projectDashboard }}</p>
              <h1 class="text-lg font-bold text-black">{{ project?.title || 'Active Project' }}</h1>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-blue-500" />
              {{ t.activeProjectDashboard.inProgress }}
            </span>
            <span class="text-sm text-gray-500">{{ t.activeProjectDashboard.phase }} {{ currentPhaseNumber }} {{ t.activeProjectDashboard.of }} {{ phases.length }}</span>
            <button
              @click="router.push(`/client/projects/${projectId}/workspace`)"
              class="flex items-center gap-1.5 px-4 py-1.5 bg-[#1C1C1C] text-white text-xs font-semibold rounded-full hover:bg-[#333] transition"
            >
              <Layers :size="13" />
              {{ t.activeProjectDashboard.openWorkspace }}
            </button>
          </div>
        </div>
      </div>

      <!-- Pending payment warning -->
      <div v-if="hasPendingPayment" class="bg-amber-50 border-b border-amber-200 px-6 py-3">
        <div class="max-w-6xl mx-auto flex items-center gap-2 text-amber-800 text-sm font-medium">
          <AlertCircle :size="16" class="shrink-0" />
          Payment pending for <strong>Phase {{ currentPhaseNumber }}</strong> — work cannot proceed until payment is
          completed.
        </div>
      </div>

      <!-- Main body -->
      <div class="max-w-6xl mx-auto px-6 py-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- LEFT SIDEBAR -->
        <div class="lg:col-span-1 space-y-4">
          <!-- Cover image -->
          <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <div class="aspect-video bg-gray-100 overflow-hidden">
              <img v-if="coverImage" :src="coverImage" :alt="project?.title" class="w-full h-full object-cover" />
              <div v-else class="w-full h-full flex items-center justify-center">
                <Building2 :size="40" class="text-gray-300" />
              </div>
            </div>
            <div class="px-4 py-4 space-y-1">
              <h2 class="font-bold text-gray-900 text-base">{{ project?.title }}</h2>
              <p class="text-sm text-gray-500 flex items-center gap-1">
                <MapPin :size="13" />
                {{ project?.location }}
              </p>
              <p v-if="project?.projectCategory" class="text-xs text-gray-400 uppercase tracking-wide">
                {{ project.projectCategory }}
              </p>
            </div>
          </div>

          <!-- Budget summary -->
          <div class="bg-white rounded-xl border border-gray-200 px-4 py-4 space-y-3">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wide">{{ t.activeProjectDashboard.budget }}</p>
            <div class="space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">{{ t.activeProjectDashboard.totalValue }}</span>
                <span class="font-semibold text-gray-900">{{ formatAmount(totalAmount) }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">{{ t.activeProjectDashboard.paid }}</span>
                <span class="font-semibold text-green-600">{{ formatAmount(paidAmount) }}</span>
              </div>
              <div class="w-full bg-gray-100 rounded-full h-1.5 mt-1">
                <div class="bg-green-500 h-1.5 rounded-full transition-all" :style="{ width: progressPercent + '%' }" />
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">{{ t.activeProjectDashboard.remaining }}</span>
                <span class="font-semibold text-amber-600">{{ formatAmount(remainingAmount) }}</span>
              </div>
            </div>
          </div>

          <!-- Architect card -->
          <div v-if="acceptedBid" class="bg-white rounded-xl border border-gray-200 px-4 py-4">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">{{ t.activeProjectDashboard.architect }}</p>
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 rounded-full bg-[#1C1C1C] text-white flex items-center justify-center text-sm font-bold shrink-0"
              >
                {{ architectInitials }}
              </div>
              <div>
                <p class="font-semibold text-gray-900 text-sm">{{ acceptedBid.architectName }}</p>
                <p v-if="acceptedBid.architectCompany" class="text-xs text-gray-500">
                  {{ acceptedBid.architectCompany }}
                </p>
              </div>
            </div>
          </div>

          <!-- Scope / description -->
          <div v-if="project?.scopeOfWork" class="bg-white rounded-xl border border-gray-200 px-4 py-4">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">{{ t.activeProjectDashboard.scopeOfWork }}</p>
            <p class="text-sm text-gray-600 leading-relaxed line-clamp-4">{{ project.scopeOfWork }}</p>
          </div>
        </div>

        <!-- RIGHT: Phase payment list -->
        <div class="lg:col-span-2 space-y-3">
          <!-- Progress header -->
          <div class="flex items-center justify-between mb-1">
            <p class="text-sm font-semibold text-gray-700">
              {{ paidCount }} of {{ phases.length }} phase{{ phases.length !== 1 ? 's' : '' }} paid
            </p>
            <p class="text-xs text-gray-400">{{ Math.round(progressPercent) }}% complete</p>
          </div>

          <!-- Phase cards -->
          <div v-for="(phase, index) in phases" :key="phase.phaseId">
            <!-- COMPLETED phase -->
            <div
              v-if="phase.paymentStatus === 'COMPLETED'"
              class="bg-white rounded-xl border border-gray-200 px-5 py-4 flex items-center justify-between"
            >
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center shrink-0">
                  <CheckCircle :size="16" class="text-green-600" />
                </div>
                <div>
                  <p class="font-semibold text-gray-900 text-sm">{{ phase.title || `Phase ${phase.phaseNumber}` }}</p>
                  <p class="text-xs text-gray-500">{{ formatAmount(phase.amount) }}</p>
                </div>
              </div>
              <div class="text-right">
                <span
                  class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-green-50 text-green-700"
                >
                  <span class="w-1.5 h-1.5 rounded-full bg-green-500" /> {{ t.activeProjectDashboard.paid }}
                </span>
                <p v-if="phase.paidAt" class="text-xs text-gray-400 mt-1">{{ formatDate(phase.paidAt) }}</p>
              </div>
            </div>

            <!-- CURRENT (first non-paid) phase -->
            <div
              v-else-if="index === currentPhaseIndex"
              class="bg-white rounded-xl border-2 border-amber-300 overflow-hidden"
            >
              <div class="px-5 py-4 flex items-center justify-between border-b border-amber-100">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center shrink-0">
                    <span class="text-sm font-bold text-amber-700">{{ index + 1 }}</span>
                  </div>
                  <div>
                    <p class="font-semibold text-gray-900">{{ phase.title || `Phase ${phase.phaseNumber}` }}</p>
                    <p class="text-sm text-gray-500">{{ formatAmount(phase.amount) }}</p>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <span
                    class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700"
                  >
                    <span class="w-1.5 h-1.5 rounded-full bg-amber-500" />
                    {{ phase.paymentStatus === 'EXPIRED' ? t.activeProjectDashboard.expired : t.activeProjectDashboard.pending }}
                  </span>
                  <div class="flex items-center gap-2">
                    <button
                      v-if="pendingPaymentLink"
                      @click="checkPaymentStatus"
                      class="px-3 py-2 text-xs font-semibold text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50 transition"
                    >
                      {{ t.activeProjectDashboard?.paidRefresh }}
                    </button>
                    <button
                      :disabled="payingPhaseId === phase.phaseId"
                      @click="payPhase(phase)"
                      class="px-4 py-2 bg-[#1C1C1C] text-white text-sm font-semibold rounded-lg hover:bg-[#333] disabled:opacity-50 disabled:cursor-not-allowed transition"
                    >
                      <span v-if="payingPhaseId === phase.phaseId">{{ t.activeProjectDashboard?.processing }}</span>
                      <span v-else>{{
                        pendingPaymentLink ? t.activeProjectDashboard?.payNowAgain : t.activeProjectDashboard?.payNow
                      }}</span>
                    </button>
                  </div>
                </div>
              </div>
              <div v-if="phase.deliverables && phase.deliverables.length" class="px-5 py-3 bg-amber-50">
                <p class="text-xs font-bold text-amber-700 uppercase tracking-wide mb-2">
                  {{ t.activeProjectDashboard?.deliverables }}
                </p>
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="d in phase.deliverables"
                    :key="d"
                    class="px-2.5 py-1 bg-white border border-amber-200 rounded-md text-xs text-amber-800"
                  >
                    {{ d.replace(/_/g, ' ') }}
                  </span>
                </div>
              </div>
            </div>

            <!-- LOCKED future phase -->
            <div
              v-else
              class="bg-white rounded-xl border border-gray-200 px-5 py-4 flex items-center justify-between opacity-50"
            >
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center shrink-0">
                  <Lock :size="14" class="text-gray-400" />
                </div>
                <div>
                  <p class="font-semibold text-gray-500 text-sm">{{ phase.title || `Phase ${phase.phaseNumber}` }}</p>
                  <p class="text-xs text-gray-400">{{ formatAmount(phase.amount) }}</p>
                </div>
              </div>
              <p class="text-xs text-gray-400">{{ t.activeProjectDashboard.phase }} {{ currentPhaseNumber }} {{ t.activeProjectDashboard.payFirst }}</p>
            </div>
          </div>

          <!-- Empty state -->
          <div
            v-if="phases.length === 0"
            class="bg-white rounded-xl border border-gray-200 py-16 text-center text-gray-400"
          >
            {{ t.activeProjectDashboard.noPhases }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, CheckCircle, AlertCircle, Building2, MapPin, Lock, Layers } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import { paymentAPI } from '@/services/api'
import { useI18n } from '@/composables/useI18n'

const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()
const { t } = useI18n()

const projectId = route.params.id
const phases = ref([])
const loading = ref(true)
const error = ref(null)
const payingPhaseId = ref(null)
const pendingPaymentLink = ref(null)

const project = computed(() => projectsStore.currentProject)
const projectBids = computed(() => bidsStore.projectBids)

const coverImage = computed(() => {
  const files = project.value?.files
  if (!files?.length) return null
  return files.find(f => f.fileType?.startsWith('image/'))?.filePath || null
})

const acceptedBid = computed(() => projectBids.value.find(b => b.status === 'ACCEPTED') || null)

const architectInitials = computed(() => {
  const name = acceptedBid.value?.architectName || ''
  return name
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
})

const currentPhaseIndex = computed(() => {
  const idx = phases.value.findIndex(p => p.paymentStatus !== 'COMPLETED')
  return idx === -1 ? phases.value.length : idx
})

const currentPhaseNumber = computed(() => currentPhaseIndex.value + 1)

const paidCount = computed(() => phases.value.filter(p => p.paymentStatus === 'COMPLETED').length)

const totalAmount = computed(() => phases.value.reduce((sum, p) => sum + Number(p.amount), 0))
const paidAmount = computed(() =>
  phases.value.filter(p => p.paymentStatus === 'COMPLETED').reduce((sum, p) => sum + Number(p.amount), 0)
)
const remainingAmount = computed(() => totalAmount.value - paidAmount.value)
const progressPercent = computed(() => (totalAmount.value > 0 ? (paidAmount.value / totalAmount.value) * 100 : 0))

const hasPendingPayment = computed(() =>
  phases.value.some(p => p.paymentStatus === 'PENDING' || p.paymentStatus === 'EXPIRED')
)

const loadAll = async () => {
  loading.value = true
  error.value = null
  try {
    await Promise.all([projectsStore.fetchProjectById(projectId), bidsStore.fetchProjectBids(projectId)])
    const res = await paymentAPI.getProjectPhasePayments(projectId)
    phases.value = res.data.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load project'
  } finally {
    loading.value = false
  }
}

const payPhase = async phase => {
  payingPhaseId.value = phase.phaseId
  try {
    const res = await paymentAPI.initiatePhasePayment(phase.phaseId)
    const link = res.data.data.paymentLink
    pendingPaymentLink.value = link
    window.open(link, '_blank')
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to initiate payment')
  } finally {
    payingPhaseId.value = null
  }
}

const checkPaymentStatus = async () => {
  try {
    const updated = await paymentAPI.getProjectPhasePayments(projectId)
    const updatedPhases = updated.data.data
    const wasUnpaid = phases.value.filter(p => p.paymentStatus === 'COMPLETED').length
    const nowPaid = updatedPhases.filter(p => p.paymentStatus === 'COMPLETED').length
    phases.value = updatedPhases
    pendingPaymentLink.value = null
    if (nowPaid > wasUnpaid) {
      router.push(`/client/projects/${projectId}/workspace`)
    }
  } catch (err) {
    console.error('Failed to refresh payment status', err)
  }
}

const formatAmount = amount => {
  if (!amount && amount !== 0) return '-'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(amount)
}

const formatDate = dateStr => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('id-ID', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  })
}

onMounted(loadAll)
</script>
