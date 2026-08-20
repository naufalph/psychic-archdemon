<template>
  <div class="space-y-6">
    <div class="grid grid-cols-2 gap-4">
      <div
        v-for="(bid, idx) in [bidA, bidB]"
        :key="bid.id"
        class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft text-center"
      >
        <p class="text-xs text-gray-500 uppercase tracking-widest mb-1">
          {{ idx === 0 ? t.proposalComparison.proposalA : t.proposalComparison.proposalB }}
        </p>
        <h3 class="text-xl font-bold text-black mb-1">{{ bid.architectName || 'Architect' }}</h3>
        <p v-if="bid.architectCompany" class="text-sm text-gray-500 mb-4">{{ bid.architectCompany }}</p>
        <button
          v-if="bid.status === 'PENDING'"
          class="bg-brand-brown text-white px-5 py-2 rounded-full text-sm font-bold hover:bg-black transition"
          @click="handleAppoint(bid.id)"
        >
          {{ t.proposalComparison.appointLead }}
        </button>
        <span v-else-if="bid.status === 'ACCEPTED'" class="text-green-600 font-bold text-sm">{{
          t.proposalComparison.appointed
        }}</span>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft">
      <div class="flex items-center justify-between mb-4">
        <div>
          <p class="text-xs font-bold text-gray-500 uppercase tracking-widest">
            {{ t.proposalComparison.visualOverlay }}
          </p>
          <p class="text-xs text-gray-400 mt-0.5">{{ t.proposalComparison.sideBySize }}</p>
        </div>
        <div class="flex gap-2">
          <button
            v-for="type in IMAGE_TYPES"
            :key="type"
            :class="[
              'px-4 py-1.5 rounded-full text-xs font-bold transition',
              activeImageType === type ? 'bg-brand-brown text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
            @click="switchType(type)"
          >
            {{ imageTypeLabel[type] }}
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
        <div v-if="imageAUrl" class="absolute inset-0" :style="{ clipPath: `inset(0 ${100 - sliderPosition}% 0 0)` }">
          <img :src="imageAUrl" class="w-full h-full object-cover" :alt="bidA.architectName" />
        </div>

        <div v-if="imageBUrl" class="absolute inset-0" :style="{ clipPath: `inset(0 0 0 ${sliderPosition}%)` }">
          <img :src="imageBUrl" class="w-full h-full object-cover" :alt="bidB.architectName" />
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
          <span class="text-gray-300">{{ t.proposalComparison.reviewing }}</span>
          <span>{{ imageTypeLabel[activeImageType] }}</span>
        </div>
      </div>

      <div class="flex justify-between items-center mt-3 min-h-[28px]">
        <div v-if="imageAImages.length > 1" class="flex items-center gap-2">
          <button
            :disabled="imageIndexA === 0"
            :title="t.proposalComparison.prevImage"
            class="w-6 h-6 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-30 transition"
            @click="imageIndexA = Math.max(0, imageIndexA - 1)"
          >
            <ChevronLeft :size="12" />
          </button>
          <span class="text-xs text-gray-400">
            {{
              t.proposalComparison.imageOf.replace('{current}', imageIndexA + 1).replace('{total}', imageAImages.length)
            }}
          </span>
          <button
            :disabled="imageIndexA === imageAImages.length - 1"
            :title="t.proposalComparison.nextImage"
            class="w-6 h-6 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-30 transition"
            @click="imageIndexA = Math.min(imageAImages.length - 1, imageIndexA + 1)"
          >
            <ChevronRight :size="12" />
          </button>
        </div>
        <div v-else />

        <p class="text-xs text-gray-400 uppercase tracking-widest">
          {{ t.proposalComparison.adjustSlider }}
        </p>

        <div v-if="imageBImages.length > 1" class="flex items-center gap-2">
          <button
            :disabled="imageIndexB === 0"
            :title="t.proposalComparison.prevImage"
            class="w-6 h-6 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-30 transition"
            @click="imageIndexB = Math.max(0, imageIndexB - 1)"
          >
            <ChevronLeft :size="12" />
          </button>
          <span class="text-xs text-gray-400">
            {{
              t.proposalComparison.imageOf.replace('{current}', imageIndexB + 1).replace('{total}', imageBImages.length)
            }}
          </span>
          <button
            :disabled="imageIndexB === imageBImages.length - 1"
            :title="t.proposalComparison.nextImage"
            class="w-6 h-6 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-30 transition"
            @click="imageIndexB = Math.min(imageBImages.length - 1, imageIndexB + 1)"
          >
            <ChevronRight :size="12" />
          </button>
        </div>
        <div v-else />
      </div>
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div
        v-for="(bid, idx) in [bidA, bidB]"
        :key="'concepts-' + bid.id"
        class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft"
      >
        <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-4">
          {{ idx === 0 ? t.proposalComparison.proposalA : t.proposalComparison.proposalB }} —
          {{ t.proposalComparison.conceptStatements }}
        </h2>
        <div v-if="bid.proposal" class="mb-4">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
            {{ t.proposalComparison.studioOverview }}
          </p>
          <p class="text-sm text-gray-700 leading-relaxed whitespace-pre-line">{{ bid.proposal }}</p>
        </div>
        <div>
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
            {{ t.proposalComparison.conceptStatement }}
          </p>
          <p v-if="bid.details?.conceptStatement" class="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
            {{ bid.details.conceptStatement }}
          </p>
          <p v-else class="text-sm text-gray-400 italic">{{ t.proposalComparison.noConceptStatement }}</p>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-soft">
      <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest p-6 border-b border-gray-100">
        {{ t.proposalComparison.summaryStats }}
      </h2>
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider">
              {{ t.proposalComparison.metricHeader }}
            </th>
            <th class="text-center px-6 py-3 text-xs text-brand-brown font-bold uppercase tracking-wider">
              {{ t.proposalComparison.proposalA }}
            </th>
            <th class="text-center px-6 py-3 text-xs font-bold uppercase tracking-wider">
              {{ t.proposalComparison.proposalB }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-gray-700 font-medium">{{ t.proposalComparison.costEstimate }}</td>
            <td class="px-6 py-4 text-center font-bold text-brand-brown">{{ formatCurrency(bidA.bidAmount) }}</td>
            <td class="px-6 py-4 text-center font-bold">{{ formatCurrency(bidB.bidAmount) }}</td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-gray-700 font-medium">{{ t.proposalComparison.timeline }}</td>
            <td class="px-6 py-4 text-center">
              {{ bidA.proposedTimelineDays || '—' }} {{ t.proposalComparison.days }}
            </td>
            <td class="px-6 py-4 text-center">
              {{ bidB.proposedTimelineDays || '—' }} {{ t.proposalComparison.days }}
            </td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-gray-700 font-medium">{{ t.proposalComparison.portfolios }}</td>
            <td class="px-6 py-4 text-center">{{ bidA.portfolios?.length || 0 }}</td>
            <td class="px-6 py-4 text-center">{{ bidB.portfolios?.length || 0 }}</td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-gray-700 font-medium">{{ t.proposalComparison.scopeAlignment }}</td>
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
        <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-3">
          {{ idx === 0 ? t.proposalComparison.proposalA : t.proposalComparison.proposalB }} —
          {{ t.proposalComparison.paymentScheduleTitle }}
        </h2>

        <div v-if="bid.details?.phases?.length" class="space-y-3">
          <div
            v-for="phase in bid.details.phases"
            :key="phase.phaseNumber"
            class="py-3 border-b border-gray-100 last:border-0"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-1.5">
                <span class="text-xs font-bold px-1.5 py-0 rounded-full bg-brand-brown text-white"
                  >P{{ phase.phaseNumber }}</span
                >
                <span class="text-sm font-medium text-gray-800">{{
                  phase.title || `${t.paymentPhaseBuilder.phase} ${phase.phaseNumber}`
                }}</span>
              </div>
              <span class="text-xs font-bold text-brand-brown">{{ formatCurrency(phase.amount) }}</span>
            </div>
            <div v-if="phase.deliverables?.length" class="flex flex-wrap gap-1 mb-1.5">
              <span
                v-for="d in phase.deliverables"
                :key="d"
                :class="[
                  'text-xs px-2 py-0.5 rounded-full border font-medium',
                  isMatchingDeliverable(d)
                    ? 'bg-green-100 text-green-700 border-green-200'
                    : 'bg-blue-100 text-blue-700 border-blue-200'
                ]"
                :title="
                  isMatchingDeliverable(d)
                    ? t.proposalComparison.matchingDeliverable
                    : t.proposalComparison.additionalDeliverable
                "
              >
                {{ t.proposalCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
              </span>
            </div>
            <div class="flex items-center gap-3 mt-0.5">
              <p v-if="phase.revisionRounds != null" class="text-xs text-gray-400">
                {{ phase.revisionRounds }}
                {{
                  phase.revisionRounds !== 1 ? t.clientFinalization.revisionRounds : t.clientFinalization.revisionRound
                }}
              </p>
              <p v-if="phase.estimatedDays" class="text-xs text-gray-400">
                {{ phase.estimatedDays }} {{ t.proposalComparison.days }}
              </p>
            </div>
          </div>
        </div>
        <p v-else class="text-gray-400 text-sm">{{ t.proposalComparison.noPhases }}</p>

        <div v-if="getMissingDeliverables(bid).length" class="mt-4 pt-4 border-t border-dashed border-gray-200">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">
            {{ t.proposalComparison.uncoveredTitle }}
          </p>
          <div class="flex flex-wrap gap-1">
            <span
              v-for="d in getMissingDeliverables(bid)"
              :key="d"
              class="text-xs px-2 py-0.5 rounded-full border bg-red-50 text-red-400 border-red-200 font-medium"
              :title="t.proposalComparison.missingDeliverable"
            >
              {{ t.proposalCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="flex items-center justify-center gap-4 flex-wrap">
      <span
        class="inline-flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-green-100 text-green-700 border border-green-200 cursor-default"
        :title="t.proposalComparison.matchingDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-green-500 inline-block" />
        {{ t.proposalComparison.matchingDeliverable }}
      </span>
      <span
        class="inline-flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-blue-100 text-blue-700 border border-blue-200 cursor-default"
        :title="t.proposalComparison.additionalDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-blue-500 inline-block" />
        {{ t.proposalComparison.additionalDeliverable }}
      </span>
      <span
        class="inline-flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-red-50 text-red-400 border border-red-200 cursor-default"
        :title="t.proposalComparison.missingDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-red-300 inline-block" />
        {{ t.proposalComparison.missingDeliverable }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useBidsStore } from '@/stores/bids'

const props = defineProps({
  bidA: { type: Object, required: true },
  bidB: { type: Object, required: true },
  project: { type: Object, required: true }
})

const { t } = useI18n()
const router = useRouter()
const bidsStore = useBidsStore()

const activeImageType = ref('FACADE')
const sliderPosition = ref(50)
const isDragging = ref(false)
const sliderContainerRef = ref(null)
const imageIndexA = ref(0)
const imageIndexB = ref(0)

const IMAGE_TYPES = ['FACADE', 'INTERIOR', 'MASSING', 'ZONING']

const imageTypeLabel = computed(() => ({
  FACADE: t.value.proposalComparison.facade,
  INTERIOR: t.value.proposalComparison.interior,
  MASSING: t.value.proposalComparison.massing,
  ZONING: t.value.proposalComparison.zoning
}))

const IMAGE_TYPE_FIELD = {
  FACADE: 'facadeImages',
  INTERIOR: 'interiorImages',
  MASSING: 'massingImages',
  ZONING: 'zoningImages'
}

const getImages = (bid, type) => bid?.[IMAGE_TYPE_FIELD[type]] ?? []

const imageAImages = computed(() => getImages(props.bidA, activeImageType.value))
const imageBImages = computed(() => getImages(props.bidB, activeImageType.value))
const imageAUrl = computed(() => imageAImages.value[imageIndexA.value]?.imageUrl ?? null)
const imageBUrl = computed(() => imageBImages.value[imageIndexB.value]?.imageUrl ?? null)

const switchType = type => {
  activeImageType.value = type
  sliderPosition.value = 50
  imageIndexA.value = 0
  imageIndexB.value = 0
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

const isMatchingDeliverable = d => (props.project?.deliverables || []).includes(d)

const getMissingDeliverables = bid => {
  const projectDeliverables = props.project?.deliverables || []
  const covered = allPhasesDeliverables(bid)
  return projectDeliverables.filter(d => !covered.includes(d))
}

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

const handleAppoint = async bidId => {
  if (!confirm(t.value.proposalComparison.appointConfirm)) return
  try {
    await bidsStore.acceptBid(bidId)
    router.push(`/client/projects/${props.project.id}/finalization`)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to appoint architect')
  }
}
</script>
