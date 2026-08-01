<template>
  <div class="min-h-screen bg-surface-alt">
    <div v-if="loading" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <div class="w-10 h-10 border-2 border-brand-gold border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p class="text-gray-500">{{ t.phasePayments.loading }}</p>
      </div>
    </div>

    <div v-else-if="error" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button @click="loadPhases" class="text-brand-brown hover:underline">{{ t.phasePayments.tryAgain }}</button>
      </div>
    </div>

    <div v-else>
      <!-- Header -->
      <div class="bg-white border-b border-gray-200 px-6 py-4">
        <div class="max-w-4xl mx-auto flex items-center gap-4">
          <button
            @click="router.push(`/client/projects/${projectId}`)"
            class="text-gray-500 hover:text-black transition"
          >
            <ArrowLeft :size="20" />
          </button>
          <div>
            <p class="text-xs text-gray-400 uppercase font-bold tracking-wide">{{ t.phasePayments.title }}</p>
            <h1 class="text-lg font-bold text-black">{{ t.phasePayments.subtitle }}</h1>
          </div>
        </div>
      </div>

      <!-- Success banner after finalization -->
      <div class="bg-green-50 border-b border-green-200 px-6 py-3">
        <div class="max-w-4xl mx-auto flex items-center gap-2 text-green-700 text-sm font-medium">
          <CheckCircle :size="16" />
          {{ t.phasePayments.inProgressBanner }}
        </div>
      </div>

      <!-- Phase List -->
      <div class="max-w-4xl mx-auto px-6 py-6 space-y-4">
        <div v-if="phases.length === 0" class="text-center text-gray-400 py-16">
          {{ t.phasePayments.noPhases }}
        </div>

        <div
          v-for="(phase, index) in phases"
          :key="phase.phaseId"
          class="bg-white rounded-xl border border-gray-200 overflow-hidden"
        >
          <!-- Phase Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <div class="flex items-center gap-3">
              <div
                class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
                :class="phaseIconClass(phase.paymentStatus)"
              >
                {{ index + 1 }}
              </div>
              <div>
                <p class="font-semibold text-gray-900">
                  {{ phase.title || `${t.phasePayments.phase} ${phase.phaseNumber}` }}
                </p>
                <p class="text-sm text-gray-500">{{ formatAmount(phase.amount) }}</p>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <span
                class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold"
                :class="statusBadgeClass(phase.paymentStatus)"
              >
                <span class="w-1.5 h-1.5 rounded-full" :class="statusDotClass(phase.paymentStatus)" />
                {{ statusLabel(phase.paymentStatus) }}
              </span>
              <button
                v-if="phase.paymentStatus === 'PENDING' || phase.paymentStatus === 'EXPIRED'"
                :disabled="payingPhaseId === phase.phaseId"
                @click="payPhase(phase)"
                class="px-4 py-2 bg-ink-700 text-white text-sm font-semibold rounded-lg hover:bg-ink-500 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                <span v-if="payingPhaseId === phase.phaseId">{{ t.phasePayments.processing }}</span>
                <span v-else>{{ t.phasePayments.payNow }}</span>
              </button>
              <div v-else-if="phase.paymentStatus === 'COMPLETED'" class="text-xs text-gray-400">
                {{ t.phasePayments.paid }} {{ phase.paidAt ? formatDate(phase.paidAt) : '' }}
              </div>
            </div>
          </div>

          <!-- Deliverables -->
          <div v-if="phase.deliverables && phase.deliverables.length" class="px-6 py-4">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">
              {{ t.phasePayments.deliverables }}
            </p>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="deliverable in phase.deliverables"
                :key="deliverable"
                class="px-2.5 py-1 bg-gray-50 border border-gray-200 rounded-md text-xs text-gray-600"
              >
                {{ deliverable }}
              </span>
            </div>
          </div>
        </div>

        <!-- Total Summary -->
        <div class="bg-white rounded-xl border border-gray-200 px-6 py-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500">{{ t.phasePayments.totalValue }}</p>
              <p class="text-xl font-bold text-gray-900">{{ formatAmount(totalAmount) }}</p>
            </div>
            <div class="text-right">
              <p class="text-sm text-gray-500">{{ t.phasePayments.paid }}</p>
              <p class="text-xl font-bold text-green-600">{{ formatAmount(paidAmount) }}</p>
            </div>
            <div class="text-right">
              <p class="text-sm text-gray-500">{{ t.phasePayments.remaining }}</p>
              <p class="text-xl font-bold text-amber-600">{{ formatAmount(remainingAmount) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, CheckCircle } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { paymentAPI } from '@/services/api'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const projectId = route.params.projectId
const phases = ref([])
const loading = ref(true)
const error = ref(null)
const payingPhaseId = ref(null)

const totalAmount = computed(() => phases.value.reduce((sum, p) => sum + Number(p.amount), 0))
const paidAmount = computed(() =>
  phases.value.filter(p => p.paymentStatus === 'COMPLETED').reduce((sum, p) => sum + Number(p.amount), 0)
)
const remainingAmount = computed(() => totalAmount.value - paidAmount.value)

const loadPhases = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await paymentAPI.getProjectPhasePayments(projectId)
    phases.value = res.data.data
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load payment schedule'
  } finally {
    loading.value = false
  }
}

const payPhase = async phase => {
  payingPhaseId.value = phase.phaseId
  try {
    const res = await paymentAPI.initiatePhasePayment(phase.phaseId)
    const paymentLink = res.data.data.paymentLink
    window.open(paymentLink, '_blank')
    await loadPhases()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to initiate payment')
  } finally {
    payingPhaseId.value = null
  }
}

const formatAmount = amount => {
  if (!amount && amount !== 0) return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(amount)
}

const formatDate = dateStr => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' })
}

const phaseIconClass = status => ({
  'bg-green-100 text-green-700': status === 'COMPLETED',
  'bg-amber-100 text-amber-700': status === 'PENDING' || status === 'EXPIRED',
  'bg-blue-100 text-blue-700': status === 'PROCESSING'
})

const statusBadgeClass = status => ({
  'bg-green-50 text-green-700': status === 'COMPLETED',
  'bg-amber-50 text-amber-700': status === 'PENDING',
  'bg-red-50 text-red-700': status === 'EXPIRED',
  'bg-blue-50 text-blue-700': status === 'PROCESSING'
})

const statusDotClass = status => ({
  'bg-green-500': status === 'COMPLETED',
  'bg-amber-500': status === 'PENDING',
  'bg-red-500': status === 'EXPIRED',
  'bg-blue-500': status === 'PROCESSING'
})

const statusLabel = status => {
  const labels = {
    PENDING: t.value.phasePayments.statusPending,
    COMPLETED: t.value.phasePayments.statusPaid,
    EXPIRED: t.value.phasePayments.statusExpired,
    PROCESSING: t.value.phasePayments.statusProcessing
  }
  return labels[status] || status
}

onMounted(loadPhases)
</script>
