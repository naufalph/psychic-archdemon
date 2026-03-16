<template>
  <div class="space-y-6">
    <div class="grid grid-cols-2 gap-4">
      <div
        v-for="(bid, idx) in [bidA, bidB]"
        :key="bid.id"
        class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft text-center"
      >
        <p class="text-xs text-gray-500 uppercase tracking-widest mb-1">Proposal {{ idx === 0 ? 'A' : 'B' }}</p>
        <h3 class="text-xl font-bold text-black mb-1">{{ bid.architectName || 'Architect' }}</h3>
        <p v-if="bid.architectCompany" class="text-sm text-gray-500 mb-4">{{ bid.architectCompany }}</p>
        <button
          v-if="bid.status === 'PENDING'"
          @click="handleAppoint(bid.id)"
          class="bg-[#7C4728] text-white px-5 py-2 rounded-full text-sm font-bold hover:bg-black transition"
        >
          Appoint Lead Architect
        </button>
        <span v-else-if="bid.status === 'ACCEPTED'" class="text-green-600 font-bold text-sm">✓ Appointed</span>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft">
      <div class="flex items-center justify-between mb-4">
        <div>
          <p class="text-xs font-bold text-gray-500 uppercase tracking-widest">Visual Overlay</p>
          <p class="text-xs text-gray-400 mt-0.5">Design Review Side-by-Side</p>
        </div>
        <div class="flex gap-2">
          <button
            v-for="type in IMAGE_TYPES"
            :key="type"
            @click="switchType(type)"
            :class="[
              'px-4 py-1.5 rounded-full text-xs font-bold transition',
              activeImageType === type ? 'bg-[#7C4728] text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            {{ type }}
          </button>
        </div>
      </div>

      <div
        ref="sliderContainerRef"
        class="relative aspect-video rounded-2xl overflow-hidden select-none bg-gray-100"
        @mousemove="onMove"
        @mouseup="stopDrag"
        @mouseleave="stopDrag"
        @touchmove.prevent="onTouchMove"
        @touchend="stopDrag"
      >
        <img
          v-if="imageBUrl"
          :src="imageBUrl"
          class="absolute inset-0 w-full h-full object-cover"
          :alt="bidB.architectName"
        />
        <div v-else class="absolute inset-0 flex items-center justify-center text-gray-400 text-sm">
          No {{ activeImageType.toLowerCase() }} images
        </div>

        <div v-if="imageAUrl" class="absolute inset-0" :style="{ clipPath: `inset(0 ${100 - sliderPosition}% 0 0)` }">
          <img :src="imageAUrl" class="w-full h-full object-cover" :alt="bidA.architectName" />
        </div>

        <div
          class="absolute top-0 bottom-0 w-0.5 bg-white/80 pointer-events-none"
          :style="{ left: sliderPosition + '%' }"
        />

        <div
          class="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 w-10 h-10 bg-white rounded-full shadow-lg flex items-center justify-center cursor-ew-resize z-10"
          :style="{ left: sliderPosition + '%' }"
          @mousedown.stop="startDrag"
          @touchstart.prevent.stop="startDrag"
        >
          <ChevronLeft :size="14" class="text-gray-600" />
          <ChevronRight :size="14" class="text-gray-600" />
        </div>

        <div
          class="absolute bottom-3 left-3 bg-black/50 backdrop-blur-sm text-white text-xs font-bold px-3 py-1.5 rounded-full uppercase tracking-wider"
        >
          {{ bidA.architectName || 'Proposal A' }}
        </div>
        <div
          class="absolute bottom-3 right-3 bg-black/50 backdrop-blur-sm text-white text-xs font-bold px-3 py-1.5 rounded-full uppercase tracking-wider"
        >
          {{ bidB.architectName || 'Proposal B' }}
        </div>

        <div
          class="absolute top-3 left-1/2 -translate-x-1/2 bg-black/40 backdrop-blur-sm text-white text-xs font-bold px-4 py-1.5 rounded-full flex items-center gap-2 whitespace-nowrap"
        >
          <span class="text-gray-300">Reviewing</span>
          <span>{{ activeImageType }}</span>
        </div>
      </div>

      <p class="text-center text-xs text-gray-400 uppercase tracking-widest mt-3">Adjust slider for relative review</p>
    </div>

    <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft">
      <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-6">Competitive Performance Audit</h2>
      <div class="space-y-5">
        <div v-for="metric in performanceMetrics" :key="metric.label">
          <div class="flex justify-between items-center mb-2">
            <span class="text-sm font-medium text-gray-700">{{ metric.label }}</span>
            <div class="flex gap-6 text-xs text-gray-500">
              <span class="text-[#7C4728] font-bold">A: {{ Math.round(metric.a) }}%</span>
              <span class="text-black font-bold">B: {{ Math.round(metric.b) }}%</span>
            </div>
          </div>
          <div class="space-y-1.5">
            <div class="h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                class="h-full bg-[#7C4728] rounded-full transition-all duration-700"
                :style="{ width: metric.a + '%' }"
              />
            </div>
            <div class="h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                class="h-full bg-black rounded-full transition-all duration-700"
                :style="{ width: metric.b + '%' }"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-soft">
      <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest p-6 border-b border-gray-100">
        Summary Stats
      </h2>
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider">Metric</th>
            <th class="text-center px-6 py-3 text-xs text-[#7C4728] font-bold uppercase tracking-wider">Proposal A</th>
            <th class="text-center px-6 py-3 text-xs font-bold uppercase tracking-wider">Proposal B</th>
          </tr>
        </thead>
        <tbody>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-gray-700 font-medium">Cost Estimate</td>
            <td class="px-6 py-4 text-center font-bold text-[#7C4728]">{{ formatCurrency(bidA.bidAmount) }}</td>
            <td class="px-6 py-4 text-center font-bold">{{ formatCurrency(bidB.bidAmount) }}</td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-gray-700 font-medium">Timeline</td>
            <td class="px-6 py-4 text-center">{{ bidA.proposedTimelineDays || '—' }} days</td>
            <td class="px-6 py-4 text-center">{{ bidB.proposedTimelineDays || '—' }} days</td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-gray-700 font-medium">Portfolios</td>
            <td class="px-6 py-4 text-center">{{ bidA.portfolios?.length || 0 }}</td>
            <td class="px-6 py-4 text-center">{{ bidB.portfolios?.length || 0 }}</td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-gray-700 font-medium">Scope Alignment</td>
            <td class="px-6 py-4 text-center">{{ Math.round(scopeScore(bidA)) }}%</td>
            <td class="px-6 py-4 text-center">{{ Math.round(scopeScore(bidB)) }}%</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div
        v-for="(bid, idx) in [bidA, bidB]"
        :key="'phases-' + bid.id"
        class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft"
      >
        <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-4">
          Proposal {{ idx === 0 ? 'A' : 'B' }} — Payment Schedule
        </h2>
        <div v-if="bid.details?.phases?.length" class="space-y-2">
          <div
            v-for="phase in bid.details.phases"
            :key="phase.phaseNumber"
            class="flex items-start justify-between text-sm py-2 border-b border-gray-100 last:border-0"
          >
            <div>
              <div class="flex items-center gap-1.5 mb-0.5">
                <span class="text-xs font-bold px-1.5 py-0 rounded-full bg-[#7C4728] text-white">P{{ phase.phaseNumber }}</span>
                <span class="font-medium text-gray-800">{{ phase.title || `Phase ${phase.phaseNumber}` }}</span>
              </div>
              <p v-if="phase.deliverables?.length" class="text-xs text-gray-400">{{ phase.deliverables.length }} deliverable{{ phase.deliverables.length !== 1 ? 's' : '' }}</p>
            </div>
            <span class="text-xs font-bold text-[#7C4728]">{{ formatCurrency(phase.amount) }}</span>
          </div>
        </div>
        <p v-else class="text-gray-400 text-sm">No payment phases specified</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'

const props = defineProps({
  bidA: { type: Object, required: true },
  bidB: { type: Object, required: true },
  project: { type: Object, required: true }
})

const router = useRouter()
const bidsStore = useBidsStore()

const activeImageType = ref('FACADE')
const sliderPosition = ref(50)
const isDragging = ref(false)
const sliderContainerRef = ref(null)

const IMAGE_TYPES = ['FACADE', 'INTERIOR', 'MASSING', 'ZONING']

const IMAGE_TYPE_FIELD = {
  FACADE: 'facadeImages',
  INTERIOR: 'interiorImages',
  MASSING: 'massingImages',
  ZONING: 'zoningImages'
}

const getImages = (bid, type) => bid?.[IMAGE_TYPE_FIELD[type]] ?? []

const imageAUrl = computed(() => getImages(props.bidA, activeImageType.value)[0]?.imageUrl ?? null)
const imageBUrl = computed(() => getImages(props.bidB, activeImageType.value)[0]?.imageUrl ?? null)

const switchType = type => {
  activeImageType.value = type
  sliderPosition.value = 50
}

const startDrag = () => {
  isDragging.value = true
}
const stopDrag = () => {
  isDragging.value = false
}

const onMove = e => {
  if (!isDragging.value || !sliderContainerRef.value) return
  const rect = sliderContainerRef.value.getBoundingClientRect()
  sliderPosition.value = Math.max(0, Math.min(100, ((e.clientX - rect.left) / rect.width) * 100))
}

const onTouchMove = e => {
  if (!isDragging.value || !sliderContainerRef.value) return
  const rect = sliderContainerRef.value.getBoundingClientRect()
  sliderPosition.value = Math.max(0, Math.min(100, ((e.touches[0].clientX - rect.left) / rect.width) * 100))
}

const allPhasesDeliverables = bid => bid.details?.phases?.flatMap(p => p.deliverables || []) || []

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const scopeScore = bid => {
  const projectDeliverables = props.project?.deliverables || []
  if (!projectDeliverables.length) return 0
  const bidDeliverables = allPhasesDeliverables(bid)
  const intersection = bidDeliverables.filter(d => projectDeliverables.includes(d))
  return (intersection.length / projectDeliverables.length) * 100
}

const costScore = bid => {
  const budgetMax = props.project?.designBudgetMax || props.project?.designBudget
  if (!budgetMax || !bid.bidAmount) return 50
  return Math.max(0, Math.min(100, ((budgetMax - bid.bidAmount) / budgetMax) * 100))
}

const timelineScore = (bid, otherBid) => {
  const a = bid.proposedTimelineDays || 0
  const b = otherBid.proposedTimelineDays || 0
  const maxDays = Math.max(a, b)
  if (!maxDays) return 50
  return (1 - a / maxDays) * 100
}

const practiceScore = bid => Math.min(100, (bid.portfolios?.length || 0) * 20)

const performanceMetrics = computed(() => [
  { label: 'Cost Efficiency', a: costScore(props.bidA), b: costScore(props.bidB) },
  { label: 'Timeline Speed', a: timelineScore(props.bidA, props.bidB), b: timelineScore(props.bidB, props.bidA) },
  { label: 'Practice Depth', a: practiceScore(props.bidA), b: practiceScore(props.bidB) },
  { label: 'Scope Alignment', a: scopeScore(props.bidA), b: scopeScore(props.bidB) }
])

const handleAppoint = async bidId => {
  if (!confirm('Appoint this architect as the lead for this project?')) return
  try {
    await bidsStore.acceptBid(bidId)
    router.push(`/client/projects/${props.project.id}/finalization`)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to appoint architect')
  }
}
</script>
