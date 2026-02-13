<template>
  <div class="bg-white rounded-3xl shadow-soft border border-gray-200 overflow-hidden hover:shadow-lg transition-all">
    <div class="relative aspect-video bg-gradient-to-br from-amber-100 to-amber-200">
      <img v-if="heroImage" :src="heroImage" :alt="proposal.architectName" class="w-full h-full object-cover" />
      <div v-else class="w-full h-full flex items-center justify-center">
        <Building :size="64" class="text-amber-600/30" />
      </div>

      <div
        v-if="proposal.status === 'ACCEPTED'"
        class="absolute top-4 right-4 bg-[#7C4728] text-white px-4 py-2 rounded-full text-sm font-bold flex items-center gap-2 shadow-lg"
      >
        <Trophy :size="16" />
        {{ t.clientDashboard.winner }}
      </div>
    </div>

    <div class="p-6 space-y-4">
      <div>
        <h3 class="text-xl font-bold text-black">{{ proposal.architectName || 'Architect' }}</h3>
        <p v-if="proposal.architectCompany" class="text-sm text-gray-500">
          {{ proposal.architectCompany }}
        </p>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div class="bg-gray-50 rounded-2xl p-4">
          <p class="text-xs text-gray-500 uppercase font-bold mb-1">
            {{ t.clientDashboard.proposedCost }}
          </p>
          <p class="text-lg font-bold text-[#7C4728]">{{ formatCurrency(proposal.bidAmount) }}</p>
        </div>
        <div class="bg-gray-50 rounded-2xl p-4">
          <p class="text-xs text-gray-500 uppercase font-bold mb-1">
            {{ t.clientDashboard.duration }}
          </p>
          <p class="text-lg font-bold text-black">{{ proposal.proposedTimelineDays }} {{ t.clientDashboard.days }}</p>
        </div>
      </div>

      <div v-if="proposal.details?.conceptStatement">
        <p class="text-xs text-gray-500 uppercase font-bold mb-2">Concept</p>
        <p class="text-sm text-gray-700 line-clamp-3">
          {{ proposal.details.conceptStatement }}
        </p>
      </div>

      <div class="space-y-2 pt-2">
        <button
          disabled
          class="w-full px-4 py-2 bg-gray-100 text-gray-400 rounded-full text-sm font-medium flex items-center justify-center gap-2 cursor-not-allowed"
          title="Coming Soon"
        >
          <Sparkles :size="16" />
          Run analysis for scores
        </button>

        <button
          @click="$emit('view-details', proposal.id)"
          class="w-full px-4 py-2 bg-white border-2 border-[#C5A17A] text-[#7C4728] rounded-full text-sm font-medium hover:bg-[#C5A17A] hover:text-white transition flex items-center justify-center gap-2"
        >
          <Eye :size="16" />
          View Full Details
        </button>

        <button
          v-if="hasPdfAttachment"
          @click="$emit('view-pdf', proposal)"
          class="w-full px-4 py-2 bg-white border-2 border-[#7C4728] text-[#7C4728] rounded-full text-sm font-medium hover:bg-[#7C4728] hover:text-white transition flex items-center justify-center gap-2"
        >
          <FileText :size="16" />
          View PDF
        </button>

        <button
          v-if="showAcceptButton"
          @click="$emit('accept', proposal.id)"
          class="w-full px-4 py-3 bg-[#7C4728] text-white rounded-full font-bold hover:bg-black transition shadow-md hover:shadow-lg flex items-center justify-center gap-2"
        >
          <Check :size="18" />
          {{ t.clientDashboard.acceptProposal }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '@/composables/useI18n'
import { Building, Trophy, Sparkles, FileText, Check, Eye } from 'lucide-vue-next'

const props = defineProps({
  proposal: {
    type: Object,
    required: true
  },
  projectStatus: {
    type: String,
    default: 'OPEN'
  }
})

defineEmits(['accept', 'view-pdf', 'view-details'])

const { t } = useI18n()

const heroImage = computed(() => {
  if (props.proposal.conceptSketches && props.proposal.conceptSketches.length > 0) {
    return props.proposal.conceptSketches[0].imageUrl
  }
  return null
})

const hasPdfAttachment = computed(() => {
  return props.proposal.attachments && props.proposal.attachments.length > 0
})

const showAcceptButton = computed(() => {
  return props.proposal.status === 'PENDING' && props.projectStatus === 'OPEN'
})

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}
</script>
