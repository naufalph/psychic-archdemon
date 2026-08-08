<template>
  <div
    v-if="variant === 'client'"
    class="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-soft hover:shadow-glow transition-all cursor-pointer group"
    @click="handleClick"
  >
    <div class="relative aspect-[16/10] overflow-hidden bg-gradient-to-br from-brand-brown to-brand-brown-900">
      <img
        v-if="coverImage"
        :src="coverImage"
        :alt="project.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
      />
      <span
        class="absolute top-3 right-3 bg-black/70 text-white text-xs font-bold px-2.5 py-1 rounded-full tracking-wider uppercase"
      >
        {{ buildingLabel }}
      </span>
      <span
        class="absolute bottom-3 left-3 px-2.5 py-1 rounded-full text-xs font-bold tracking-wider"
        :class="statusBadgeClass"
      >
        {{ project.status }}
      </span>
    </div>

    <div class="p-5">
      <h3 class="text-base font-bold text-black mb-1 line-clamp-1">{{ project.title }}</h3>
      <p v-if="project.description" class="text-gray-500 text-xs mb-3 line-clamp-2 leading-relaxed">
        {{ project.description }}
      </p>

      <div class="flex items-center gap-3 text-xs text-gray-500 mb-4">
        <span v-if="project.location" class="flex items-center gap-1">
          <MapPin :size="11" /> {{ project.location }}
        </span>
        <span v-if="project.designBudget" class="flex items-center gap-1">
          <DollarSign :size="11" /> {{ formatCurrency(project.designBudget) }}
        </span>
        <span v-if="project.createdAt" class="flex items-center gap-1 ml-auto">
          <Clock :size="11" /> {{ timeAgo(project.createdAt) }}
        </span>
      </div>

      <div class="flex items-center justify-between pt-3 border-t border-gray-100">
        <span class="text-xs text-gray-500 font-medium">
          {{ proposalCount }}
          {{ proposalCount === 1 ? t.clientDashboard.proposalSingular : t.clientDashboard.proposalPlural }}
        </span>
        <button
          class="bg-black text-white text-xs font-bold px-4 py-2 rounded-full tracking-wider hover:bg-brand-brown transition"
        >
          {{ project.status === 'DRAFT' ? t.projectCard.continueDraft : t.projectCard.manage }}
        </button>
      </div>
    </div>
  </div>

  <div
    v-else
    class="bg-white rounded-3xl border border-gray-200 overflow-hidden shadow-soft hover:shadow-glow transition-all cursor-pointer group"
    @click="handleClick"
  >
    <div class="relative aspect-[16/10] overflow-hidden bg-gray-100">
      <img
        v-if="coverImage"
        :src="coverImage"
        :alt="project.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
      />
      <div v-else class="w-full h-full bg-gray-200" />
    </div>

    <div class="p-8">
      <div class="flex justify-between items-start mb-4">
        <h3 class="text-xl font-bold text-black line-clamp-2">{{ project.title }}</h3>
        <div class="flex gap-2">
          <ProjectStatusBadge :status="project.status" />
          <BidStatusBadge v-if="bidStatus" :status="bidStatus" />
        </div>
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
          class="bg-brand-brown text-white px-3 py-1.5 rounded-full text-xs font-bold"
        >
          {{ proposalCount }}
          {{ proposalCount === 1 ? t.clientDashboard.proposalSingular : t.clientDashboard.proposalPlural }}
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { MapPin, DollarSign, Clock } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import ProjectStatusBadge from './ProjectStatusBadge.vue'
import BidStatusBadge from './BidStatusBadge.vue'
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
  },
  bidStatus: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['submit-proposal'])
const { t } = useI18n()
const router = useRouter()

const proposalCount = computed(() => props.project.bids?.length || props.project.proposalCount || 0)

const coverImage = computed(() => {
  const files = props.project.files
  if (!files || files.length === 0) return null
  const img = files.find(f => f.fileType?.startsWith('image/'))
  return img?.filePath || null
})

const buildingLabel = computed(() => {
  const map = {
    RESIDENTIAL: 'NEW BUILD',
    VILLA: 'VILLA',
    COMMERCIAL: 'COMMERCIAL',
    STUDENT_HOUSING: 'STUDENT HOUSING',
    RENOVATION: 'RENOVATION'
  }
  return map[props.project.buildingFunction || props.project.buildingType] || 'PROJECT'
})

const statusBadgeClass = computed(() => {
  switch (props.project.status) {
    case 'OPEN':
      return 'bg-green-500 text-white'
    case 'NEGOTIATION':
      return 'bg-amber-400 text-white'
    case 'IN_PROGRESS':
      return 'bg-blue-500 text-white'
    case 'AWARDED':
      return 'bg-blue-600 text-white'
    case 'CLOSED':
      return 'bg-gray-500 text-white'
    case 'REJECTED':
      return 'bg-red-500 text-white'
    case 'DRAFT':
      return 'bg-amber-500 text-white'
    default:
      return 'bg-gray-400 text-white'
  }
})

const formatCurrency = value => {
  if (!value) return '0'
  return new Intl.NumberFormat('id-ID', {
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const timeAgo = dateString => {
  if (!dateString) return ''
  const diff = Date.now() - new Date(dateString).getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} day${days === 1 ? '' : 's'} ago`
  const months = Math.floor(days / 30)
  return `${months} month${months === 1 ? '' : 's'} ago`
}

const handleClick = () => {
  if (props.variant === 'client') {
    if (props.project.status === 'DRAFT') {
      router.push({ name: 'ProjectCreate' })
    } else if (props.project.status === 'IN_PROGRESS') {
      router.push({ name: 'ClientProjectWorkspace', params: { id: props.project.id } })
    } else {
      router.push({ name: 'ProjectDetail', params: { id: props.project.id } })
    }
  } else if (props.variant === 'architect') {
    router.push({ name: 'ProjectDetailForArchitect', params: { projectId: props.project.id } })
  }
}
</script>
