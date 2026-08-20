<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Left: Deliverable Pool -->
      <div>
        <h4 class="font-bold text-gray-900 mb-3">{{ t.paymentPhaseBuilder.availableDeliverables }}</h4>
        <p class="text-xs text-gray-500 mb-4">
          {{ t.paymentPhaseBuilder.availableDeliverablesHelp }}
        </p>
        <div class="space-y-4">
          <div v-for="group in deliverableGroups" :key="group.categoryKey">
            <p class="text-xs font-bold text-gray-500 uppercase mb-2">
              {{ t.proposalCreate.deliverableCategories[group.categoryKey] }}
            </p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="value in group.items"
                :key="value"
                type="button"
                :disabled="isAssigned(value)"
                :draggable="!isAssigned(value)"
                :class="chipClasses(value)"
                @dragstart="onDragStart($event, value)"
                @click="assignToActivePhase(value)"
              >
                {{ getLabelForValue(value) }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Phases -->
      <div>
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-gray-900">{{ t.paymentPhaseBuilder.paymentPhases }}</h4>
          <button
            type="button"
            class="flex items-center gap-1 px-3 py-1.5 bg-brand-brown text-white rounded-full text-xs font-bold hover:bg-black transition"
            @click="addPhase"
          >
            {{ t.paymentPhaseBuilder.addPhase }}
          </button>
        </div>

        <!-- Total Validation -->
        <div
          :class="
            totalMatchesBid ? 'bg-green-50 border-green-200 text-green-700' : 'bg-red-50 border-red-200 text-red-700'
          "
          class="border rounded-xl px-4 py-2 mb-2 text-xs font-bold flex items-center justify-between"
        >
          <span>{{ t.paymentPhaseBuilder.phasesTotal }}</span>
          <span>{{ formatCurrency(phasesTotal) }} / {{ formatCurrency(bidAmount) }}</span>
        </div>
        <div
          class="border border-gray-200 rounded-xl px-4 py-2 mb-4 text-xs font-bold flex items-center justify-between text-gray-600 bg-gray-50"
        >
          <span>{{ t.paymentPhaseBuilder.daysTotal }}</span>
          <span>{{ daysTotal }} {{ t.bidDetail?.days || 'days' }}</span>
        </div>

        <div class="space-y-4 max-h-[520px] overflow-y-auto pr-1">
          <div
            v-for="(phase, index) in phases"
            :key="phase.phaseNumber"
            :class="[
              'rounded-2xl border-2 p-4 transition-all cursor-pointer',
              activePhaseIndex === index
                ? 'border-brand-brown bg-brand-cream/50'
                : 'border-gray-200 bg-white hover:border-gray-300'
            ]"
            @click="activePhaseIndex = index"
            @dragover.prevent="onDragOver($event, index)"
            @drop="onDrop($event, index)"
          >
            <!-- Phase header -->
            <div class="flex items-center gap-2 mb-3">
              <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-brand-brown text-white whitespace-nowrap">
                {{ t.paymentPhaseBuilder.phase }} {{ phase.phaseNumber }}
              </span>
              <input
                v-model="phase.title"
                type="text"
                :placeholder="`${t.paymentPhaseBuilder.phase} ${phase.phaseNumber} ${t.paymentPhaseBuilder.phaseTitlePlaceholder}`"
                class="flex-1 text-sm font-bold bg-transparent border-b border-gray-200 focus:border-brand-brown outline-none pb-0.5"
                @click.stop
              />
              <button
                v-if="phases.length > 1"
                type="button"
                class="text-gray-400 hover:text-red-500 transition text-xs"
                @click.stop="removePhase(index)"
              >
                ✕
              </button>
            </div>

            <!-- Amount + revisions + days -->
            <div class="grid grid-cols-3 gap-2 mb-3">
              <div>
                <label class="text-xs text-gray-500 font-bold">{{ t.paymentPhaseBuilder.amount }}</label>
                <input
                  v-model.number="phase.amount"
                  type="number"
                  min="0"
                  placeholder="0"
                  class="w-full mt-1 px-2 py-1.5 border border-gray-200 rounded-lg text-sm focus:border-brand-brown outline-none"
                  @click.stop
                />
              </div>
              <div>
                <label class="text-xs text-gray-500 font-bold">{{ t.paymentPhaseBuilder.revisionRounds }}</label>
                <input
                  v-model.number="phase.revisionRounds"
                  type="number"
                  min="0"
                  max="10"
                  placeholder="0"
                  class="w-full mt-1 px-2 py-1.5 border border-gray-200 rounded-lg text-sm focus:border-brand-brown outline-none"
                  @click.stop
                />
              </div>
              <div>
                <label class="text-xs text-gray-500 font-bold">{{ t.paymentPhaseBuilder.estimatedDays }}</label>
                <input
                  v-model.number="phase.estimatedDays"
                  type="number"
                  min="1"
                  placeholder="0"
                  class="w-full mt-1 px-2 py-1.5 border border-gray-200 rounded-lg text-sm focus:border-brand-brown outline-none"
                  @click.stop
                />
              </div>
            </div>

            <!-- Drop zone + assigned deliverables -->
            <div
              class="min-h-[48px] border border-dashed border-gray-300 rounded-xl p-2 flex flex-wrap gap-1.5 transition-colors"
              :class="dragOverPhaseIndex === index ? 'bg-brand-tan/50 border-brand-gold' : ''"
            >
              <span v-if="phase.deliverables.length === 0" class="text-xs text-gray-400 self-center px-1">
                {{ t.paymentPhaseBuilder.dropZoneHint }}
              </span>
              <span
                v-for="d in phase.deliverables"
                :key="d"
                class="inline-flex items-center gap-1 px-2 py-1 bg-brand-tan text-brand-brown rounded-full text-xs font-medium"
              >
                {{ getLabelForValue(d) }}
                <button
                  type="button"
                  class="text-brand-gold hover:text-red-500 leading-none"
                  @click.stop="removeDeliverable(index, d)"
                >
                  ×
                </button>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useI18n } from '@/composables/useI18n'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  bidAmount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

const deliverableGroups = [
  {
    categoryKey: 'siteAnalysis',
    items: ['SITE_ANALYSIS', 'ZONING_STUDY']
  },
  {
    categoryKey: 'designPhases',
    items: ['CONCEPT_DESIGN', 'SCHEMATIC_DESIGN', 'DESIGN_DEVELOPMENT', 'CONSTRUCTION_DOCS']
  },
  {
    categoryKey: 'permits',
    items: ['IMB_PERMIT', 'SLF_CERT', 'ENVIRONMENTAL_PERMIT']
  },
  {
    categoryKey: 'specialized',
    items: ['INTERIOR_DESIGN', 'LANDSCAPE_DESIGN', 'MEP_DESIGN', 'STRUCTURAL_DESIGN']
  },
  {
    categoryKey: 'construction',
    items: ['SUPERVISION', 'AS_BUILT']
  }
]

const getLabelForValue = value => t.value.proposalCreate?.deliverableItems?.[value] || value.replace(/_/g, ' ')

const newPhase = number => ({
  phaseNumber: number,
  title: '',
  deliverables: [],
  amount: 0,
  revisionRounds: null,
  estimatedDays: null
})

const initPhases = incoming => {
  if (incoming && incoming.length > 0) {
    return incoming.map(p => ({ ...p, deliverables: p.deliverables || [], estimatedDays: p.estimatedDays ?? null }))
  }
  return [newPhase(1)]
}

const phases = ref(initPhases(props.modelValue))
const activePhaseIndex = ref(0)
const dragOverPhaseIndex = ref(null)
let draggedItem = null

watch(
  phases,
  val => {
    emit(
      'update:modelValue',
      val.map(p => ({
        phaseNumber: p.phaseNumber,
        title: p.title || `${t.value.paymentPhaseBuilder.phase} ${p.phaseNumber}`,
        deliverables: p.deliverables,
        amount: Number(p.amount) || 0,
        revisionRounds: p.revisionRounds ?? null,
        estimatedDays: Number(p.estimatedDays) || null
      }))
    )
  },
  { deep: true }
)

const isAssigned = value => phases.value.some(p => p.deliverables.includes(value))

const assignToActivePhase = value => {
  if (isAssigned(value)) return
  phases.value[activePhaseIndex.value].deliverables.push(value)
}

const removeDeliverable = (phaseIndex, value) => {
  phases.value[phaseIndex].deliverables = phases.value[phaseIndex].deliverables.filter(d => d !== value)
}

const addPhase = () => {
  phases.value.push(newPhase(phases.value.length + 1))
  activePhaseIndex.value = phases.value.length - 1
}

const removePhase = index => {
  phases.value.splice(index, 1)
  phases.value.forEach((p, i) => {
    p.phaseNumber = i + 1
  })
  if (activePhaseIndex.value >= phases.value.length) {
    activePhaseIndex.value = phases.value.length - 1
  }
}

const phasesTotal = computed(() => phases.value.reduce((sum, p) => sum + (Number(p.amount) || 0), 0))
const daysTotal = computed(() => phases.value.reduce((sum, p) => sum + (Number(p.estimatedDays) || 0), 0))

const totalMatchesBid = computed(() => {
  if (!props.bidAmount) return false
  return Math.abs(phasesTotal.value - Number(props.bidAmount)) < 1
})

const formatCurrency = value => {
  if (!value && value !== 0) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const chipClasses = value => {
  const base = 'px-3 py-1 rounded-full text-xs font-medium border transition-all cursor-pointer'
  if (isAssigned(value)) return `${base} bg-gray-100 text-gray-400 border-gray-200 opacity-50 cursor-not-allowed`
  return `${base} bg-brand-tan text-brand-brown border-brand-gold hover:bg-brand-brown hover:text-white`
}

const onDragStart = (event, value) => {
  draggedItem = value
  event.dataTransfer.effectAllowed = 'move'
}

const onDragOver = (event, index) => {
  dragOverPhaseIndex.value = index
}

const onDrop = (event, phaseIndex) => {
  dragOverPhaseIndex.value = null
  if (!draggedItem || isAssigned(draggedItem)) return
  phases.value[phaseIndex].deliverables.push(draggedItem)
  draggedItem = null
}
</script>
