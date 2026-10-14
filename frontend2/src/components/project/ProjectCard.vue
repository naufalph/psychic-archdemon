<template>
  <div
    v-if="variant === 'client'"
    class="bg-white rounded-[20px] border border-hairline overflow-hidden flex flex-col cursor-pointer group transition-all hover:-translate-y-0.5 hover:shadow-[0_24px_48px_-36px_rgba(0,0,0,.35)]"
    @click="handleClick"
  >
    <div class="relative aspect-[16/9] overflow-hidden bg-surface-alt">
      <img
        v-if="coverImage"
        :src="coverImage"
        :alt="project.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
      />
      <div v-else class="w-full h-full flex items-center justify-center text-ink-200">
        <Building2 :size="40" :stroke-width="1.5" />
      </div>
      <span
        class="absolute top-3 right-3 max-w-[60%] truncate bg-white/90 text-ink-900 text-micro font-semibold px-2.5 py-1 rounded-full"
      >
        {{ projectTypeText || 'Proyek' }}
      </span>
      <ProjectStatusBadge :status="project.status" size="sm" on-image class="absolute bottom-3 left-3" />
    </div>

    <div class="p-5 flex flex-col flex-1">
      <h3 class="text-base font-bold leading-snug text-ink-900 m-0 mb-1.5 line-clamp-2 min-h-[2.75em]">
        {{ project.title }}
      </h3>
      <p v-if="summary" class="text-caption leading-relaxed text-ink-400 m-0 mb-3 line-clamp-2">
        {{ summary }}
      </p>

      <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-caption-sm text-ink-400 mt-auto mb-4">
        <span v-if="place" class="inline-flex items-center gap-1 min-w-0 max-w-full">
          <MapPin :size="12" class="shrink-0" /> <span class="truncate">{{ place }}</span>
        </span>
        <span v-if="project.designBudget" class="whitespace-nowrap">Rp {{ formatCurrency(project.designBudget) }}</span>
        <span v-if="project.createdAt" class="inline-flex items-center gap-1 whitespace-nowrap">
          <Clock :size="12" /> {{ timeAgo(project.createdAt) }}
        </span>
      </div>

      <div class="flex items-center justify-between gap-3 pt-3 border-t border-hairline">
        <span class="text-caption-sm text-ink-400 font-medium whitespace-nowrap">
          {{ bidCount }}
          {{ bidCount === 1 ? t.clientDashboard.bidSingular : t.clientDashboard.bidPlural }}
        </span>
        <span
          class="inline-flex items-center gap-1 bg-ink-900 text-white text-caption-sm font-semibold px-4 h-9 rounded-full whitespace-nowrap group-hover:bg-ink-500 transition-colors"
        >
          {{ project.status === 'DRAFT' ? t.projectCard.continueDraft : t.projectCard.manage }}
        </span>
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

      <p class="text-gray-500 text-sm mb-4">{{ place }} • {{ projectTypeText }}</p>

      <p v-if="project.description" class="text-gray-600 text-sm mb-6 line-clamp-3">
        {{ project.description }}
      </p>

      <div class="flex flex-wrap gap-2 mb-6">
        <span
          v-if="project.lotSize"
          class="bg-gray-100 px-3 py-1.5 rounded-full text-xs font-medium border border-gray-200"
        >
          {{ project.lotSize }} m²
        </span>
        <span
          v-if="showBidCount && bidCount > 0"
          class="bg-ink-900 text-white px-3 py-1.5 rounded-full text-xs font-bold"
        >
          {{ bidCount }}
          {{ bidCount === 1 ? t.clientDashboard.bidSingular : t.clientDashboard.bidPlural }}
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
import { MapPin, Clock, Building2 } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { projectTypeLabel } from '@/constants/projectTaxonomy'
import { placeLabel } from '@/constants/regions'
import ProjectStatusBadge from './ProjectStatusBadge.vue'
import BidStatusBadge from './BidStatusBadge.vue'
import BiddingCountdown from '../bidding/BiddingCountdown.vue'
import { briefVision } from '@/utils/briefProgress'

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
  showBidCount: {
    type: Boolean,
    default: true
  },
  bidStatus: {
    type: String,
    default: null
  }
})

defineEmits(['submit-bid'])
const { t, locale } = useI18n()
const router = useRouter()

const bidCount = computed(() => props.project.bids?.length || props.project.bidCount || 0)

const coverImage = computed(() => {
  const files = props.project.files
  if (!files || files.length === 0) return null
  const img = files.find(f => f.fileType?.startsWith('image/'))
  return img?.filePath || null
})

const projectTypeText = computed(() => projectTypeLabel(props.project, locale.value))
const place = computed(() => placeLabel(props.project))

// Status reads as a coloured dot on a neutral pill, so the badge stays calm over any photo
const summary = computed(() => briefVision(props.project.description))

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
  const label = (key, n) => t.value.projectCard[key].replace('{n}', n)
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return label('minutesAgo', minutes)
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return label('hoursAgo', hours)
  const days = Math.floor(hours / 24)
  if (days < 30) return label('daysAgo', days)
  return label('monthsAgo', Math.floor(days / 30))
}

const handleClick = () => {
  if (props.variant === 'client') {
    if (props.project.status === 'DRAFT') {
      router.push({ name: 'ProjectBrief', query: { draft: props.project.id } })
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
