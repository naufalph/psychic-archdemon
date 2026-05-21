<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">Dispute Management</h1>
    <p class="text-gray-500 text-sm mb-8">Resolve disputed phases between clients and architects</p>

    <div v-if="loading" class="space-y-3">
      <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-5 animate-pulse border border-gray-100">
        <div class="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
        <div class="h-3 bg-gray-100 rounded w-1/2"></div>
      </div>
    </div>

    <div v-else-if="phases.length === 0" class="bg-white rounded-xl border border-gray-100 p-12 text-center">
      <p class="text-gray-400 text-sm">No active disputes</p>
    </div>

    <div v-else class="space-y-4">
      <div v-for="phase in phases" :key="phase.id" class="bg-white rounded-xl border border-red-100 p-5">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <span class="text-xs font-bold text-red-600 bg-red-50 px-2 py-0.5 rounded-full">DISPUTED</span>
              <p class="font-semibold text-gray-900">Phase {{ phase.phaseNumber }}: {{ phase.title }}</p>
            </div>
            <p class="text-sm text-gray-500">
              Project #{{ phase.projectId }} · Amount IDR {{ formatCurrency(phase.amount) }}
            </p>
            <p class="text-xs text-gray-400 mt-1">
              Max revisions: {{ phase.maxRevisions }} · Used: {{ phase.revisionsUsed }}
            </p>
          </div>
        </div>

        <!-- Resolution form -->
        <div v-if="resolving === phase.id" class="mt-4 pt-4 border-t border-gray-100">
          <div class="mb-3">
            <label class="block text-xs font-semibold text-gray-600 mb-1">Resolution note (optional)</label>
            <textarea
              v-model="resolutionNote"
              rows="2"
              placeholder="Reason for your decision..."
              class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-gray-300 resize-none"
            ></textarea>
          </div>
          <div class="flex gap-2">
            <button
              @click="resolve(phase.id, 'APPROVE')"
              :disabled="processing === phase.id"
              class="flex-1 py-2 text-sm font-semibold rounded-lg bg-green-600 text-white hover:bg-green-700 transition disabled:opacity-50"
            >
              Approve Deliverable
            </button>
            <button
              @click="resolve(phase.id, 'REJECT')"
              :disabled="processing === phase.id"
              class="flex-1 py-2 text-sm font-semibold rounded-lg bg-orange-500 text-white hover:bg-orange-600 transition disabled:opacity-50"
            >
              Reject (Back to IN_PROGRESS)
            </button>
            <button
              @click="
                resolving = null
                resolutionNote = ''
              "
              class="px-4 py-2 text-sm font-semibold rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 transition"
            >
              Cancel
            </button>
          </div>
        </div>
        <div v-else class="mt-3">
          <button
            @click="
              resolving = phase.id
              resolutionNote = ''
            "
            class="px-4 py-1.5 text-xs font-semibold rounded-lg bg-gray-900 text-white hover:bg-gray-700 transition"
          >
            Resolve Dispute
          </button>
        </div>
      </div>
    </div>

    <div v-if="error" class="mt-4 bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminPhasesAPI } from '@/services/adminApi'

const loading = ref(true)
const error = ref(null)
const processing = ref(null)
const resolving = ref(null)
const resolutionNote = ref('')
const phases = ref([])

const load = async () => {
  loading.value = true
  try {
    const res = await adminPhasesAPI.getDisputed()
    phases.value = res.data.data || []
  } catch (e) {
    error.value = 'Failed to load disputes'
  } finally {
    loading.value = false
  }
}

const resolve = async (phaseId, decision) => {
  processing.value = phaseId
  error.value = null
  try {
    await adminPhasesAPI.resolveDispute(phaseId, decision, resolutionNote.value || null)
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

onMounted(load)
</script>
