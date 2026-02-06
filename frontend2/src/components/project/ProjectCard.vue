<template>
  <div
    class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft hover:shadow-glow transition-all cursor-pointer"
    @click="handleClick"
  >
    <div class="flex justify-between items-start mb-4">
      <h3 class="text-xl font-bold text-black line-clamp-2">{{ project.title }}</h3>
      <ProjectStatusBadge :status="project.status" />
    </div>

    <p class="text-gray-500 text-sm mb-4">{{ project.location }} • {{ project.buildingType }}</p>

    <p v-if="project.description" class="text-gray-600 text-sm mb-6 line-clamp-3">
      {{ project.description }}
    </p>

    <div class="flex flex-wrap gap-2 mb-6">
      <span class="bg-gray-100 px-3 py-1.5 rounded-full text-xs font-medium border border-gray-200">
        {{ project.lotSize }} m²
      </span>
      <span
        v-if="project.totalBudget"
        class="bg-gray-100 px-3 py-1.5 rounded-full text-xs font-medium border border-gray-200"
      >
        IDR {{ formatCurrency(project.totalBudget) }}
      </span>
      <span
        v-if="showProposalCount && proposalCount > 0"
        class="bg-[#7C4728] text-white px-3 py-1.5 rounded-full text-xs font-bold"
      >
        {{ proposalCount }} {{ proposalCount === 1 ? 'Proposal' : 'Proposals' }}
      </span>
    </div>

    <div v-if="project.biddingDeadline && project.status === 'OPEN'" class="pt-4 border-t border-gray-100">
      <BiddingCountdown :deadline="project.biddingDeadline" size="sm" />
    </div>

    <div v-if="variant === 'architect'" class="pt-4 border-t border-gray-100 mt-4">
      <div class="flex items-center justify-between">
        <div class="text-xs text-gray-500">
          <span class="font-semibold text-gray-700">Design Budget:</span>
          IDR {{ formatCurrency(project.designBudget) }}
        </div>
        <button
          @click.stop="$emit('submit-proposal', project.id)"
          class="bg-[#7C4728] hover:bg-black text-white px-6 py-2 rounded-full text-sm font-medium transition"
        >
          Submit Proposal
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import ProjectStatusBadge from './ProjectStatusBadge.vue'
import BiddingCountdown from '../bidding/BiddingCountdown.vue'

const props = defineProps({
  project: {
    type: Object,
    required: true
  },
  variant: {
    type: String,
    default: 'client',
    validator: value => ['client', 'architect'].includes(value)
  },
  showProposalCount: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['submit-proposal'])
const router = useRouter()

const proposalCount = computed(() => props.project.bids?.length || props.project.proposalCount || 0)

const formatCurrency = value => {
  if (!value) return '0'
  return new Intl.NumberFormat('id-ID', {
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const handleClick = () => {
  if (props.variant === 'client') {
    router.push({ name: 'ProjectDetail', params: { id: props.project.id } })
  }
}
</script>
