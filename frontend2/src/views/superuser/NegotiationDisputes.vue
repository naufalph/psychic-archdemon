<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ t.negotiationDisputes.title }}</h1>
    <p class="text-gray-500 text-sm mb-8">{{ t.negotiationDisputes.subtitle }}</p>

    <div v-if="loading" class="space-y-3">
      <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-5 animate-pulse border border-gray-100">
        <div class="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
        <div class="h-3 bg-gray-100 rounded w-1/2"></div>
      </div>
    </div>

    <div v-else-if="disputes.length === 0" class="bg-white rounded-xl border border-gray-100 p-12 text-center">
      <p class="text-gray-400 text-sm">{{ t.negotiationDisputes.noDisputes }}</p>
    </div>

    <div v-else class="space-y-4">
      <div v-for="dispute in disputes" :key="dispute.projectId" class="bg-white rounded-xl border border-red-100 p-5">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <span class="text-xs font-bold text-red-600 bg-red-50 px-2 py-0.5 rounded-full">{{
                t.negotiationDisputes.expired
              }}</span>
              <p class="font-semibold text-gray-900">{{ dispute.projectTitle }}</p>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-1 mt-2 text-sm text-gray-600">
              <p>
                <span class="text-gray-400">{{ t.negotiationDisputes.client }}:</span>
                {{ dispute.clientName }} ({{ dispute.clientEmail }})
              </p>
              <p>
                <span class="text-gray-400">{{ t.negotiationDisputes.architect }}:</span>
                {{ dispute.architectName || '—' }}
                <span v-if="dispute.architectCompany">· {{ dispute.architectCompany }}</span>
              </p>
              <p>
                <span class="text-gray-400">{{ t.negotiationDisputes.bidAmount }}:</span>
                IDR {{ formatCurrency(dispute.bidAmount) }}
              </p>
              <p>
                <span class="text-gray-400">{{ t.negotiationDisputes.acceptedOn }}:</span>
                {{ formatDate(dispute.acceptedAt) }}
              </p>
              <p>
                <span class="text-gray-400">{{ t.negotiationDisputes.expiredOn }}:</span>
                {{ formatDate(dispute.expiredAt) }}
              </p>
            </div>
          </div>
        </div>

        <!-- Resolution form -->
        <div v-if="resolving === dispute.projectId" class="mt-4 pt-4 border-t border-gray-100">
          <div class="mb-3">
            <label class="block text-xs font-semibold text-gray-600 mb-1">{{
              t.negotiationDisputes.resolutionNote
            }}</label>
            <textarea
              v-model="resolutionNote"
              rows="2"
              :placeholder="t.negotiationDisputes.resolutionPlaceholder"
              class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-gray-300 resize-none"
            ></textarea>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
            <div>
              <button
                :disabled="processing === dispute.projectId"
                class="w-full py-2 text-sm font-semibold rounded-lg bg-orange-500 text-white hover:bg-orange-600 transition disabled:opacity-50"
                @click="resolve(dispute.projectId, 'CLIENT_ABANDONED')"
              >
                {{ t.negotiationDisputes.clientAbandoned }}
              </button>
              <p class="text-xs text-gray-400 mt-1">{{ t.negotiationDisputes.clientAbandonedHint }}</p>
            </div>
            <div>
              <button
                :disabled="processing === dispute.projectId"
                class="w-full py-2 text-sm font-semibold rounded-lg bg-red-600 text-white hover:bg-red-700 transition disabled:opacity-50"
                @click="resolve(dispute.projectId, 'ARCHITECT_ABANDONED')"
              >
                {{ t.negotiationDisputes.architectAbandoned }}
              </button>
              <p class="text-xs text-gray-400 mt-1">{{ t.negotiationDisputes.architectAbandonedHint }}</p>
            </div>
          </div>
          <button
            class="mt-3 px-4 py-2 text-sm font-semibold rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 transition"
            @click="cancelResolve()"
          >
            {{ t.negotiationDisputes.cancel }}
          </button>
        </div>
        <div v-else class="mt-3">
          <button
            class="px-4 py-1.5 text-xs font-semibold rounded-lg bg-gray-900 text-white hover:bg-gray-700 transition"
            @click="startResolve(dispute.projectId)"
          >
            {{ t.negotiationDisputes.resolve }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="error" class="mt-4 bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminProjectsAPI } from '@/services/adminApi'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const loading = ref(true)
const error = ref(null)
const processing = ref(null)
const resolving = ref(null)
const resolutionNote = ref('')
const disputes = ref([])

const load = async () => {
  loading.value = true
  try {
    const res = await adminProjectsAPI.getNegotiationDisputes()
    disputes.value = res.data.data || []
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to load negotiation disputes'
  } finally {
    loading.value = false
  }
}

const startResolve = projectId => {
  resolving.value = projectId
  resolutionNote.value = ''
}

const cancelResolve = () => {
  resolving.value = null
  resolutionNote.value = ''
}

const resolve = async (projectId, decision) => {
  processing.value = projectId
  error.value = null
  try {
    await adminProjectsAPI.resolveNegotiationDispute(projectId, decision, resolutionNote.value || null)
    resolving.value = null
    resolutionNote.value = ''
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Resolution failed'
  } finally {
    processing.value = null
  }
}

const formatCurrency = v => (v ? Number(v).toLocaleString('id-ID') : '-')

const formatDate = dateString => {
  if (!dateString) return '—'
  return new Date(dateString).toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

onMounted(load)
</script>
