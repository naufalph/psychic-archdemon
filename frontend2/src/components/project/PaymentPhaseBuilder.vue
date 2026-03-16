<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Left: Deliverable Pool -->
      <div>
        <h4 class="font-bold text-gray-900 mb-3">Available Deliverables</h4>
        <p class="text-xs text-gray-500 mb-4">Click a deliverable to assign it to the active phase, or drag it to a phase.</p>
        <div class="space-y-4">
          <div v-for="group in deliverableGroups" :key="group.category">
            <p class="text-xs font-bold text-gray-500 uppercase mb-2">{{ group.category }}</p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="item in group.items"
                :key="item.value"
                type="button"
                :disabled="isAssigned(item.value)"
                :draggable="!isAssigned(item.value)"
                @dragstart="onDragStart($event, item)"
                @click="assignToActivePhase(item)"
                :class="chipClasses(item.value)"
              >
                {{ item.label }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Phases -->
      <div>
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-gray-900">Payment Phases</h4>
          <button
            type="button"
            @click="addPhase"
            class="flex items-center gap-1 px-3 py-1.5 bg-[#7C4728] text-white rounded-full text-xs font-bold hover:bg-black transition"
          >
            + Add Phase
          </button>
        </div>

        <!-- Total Validation -->
        <div
          :class="totalMatchesBid ? 'bg-green-50 border-green-200 text-green-700' : 'bg-red-50 border-red-200 text-red-700'"
          class="border rounded-xl px-4 py-2 mb-4 text-xs font-bold flex items-center justify-between"
        >
          <span>Phases total</span>
          <span>{{ formatCurrency(phasesTotal) }} / {{ formatCurrency(bidAmount) }}</span>
        </div>

        <div class="space-y-4 max-h-[520px] overflow-y-auto pr-1">
          <div
            v-for="(phase, index) in phases"
            :key="phase.phaseNumber"
            :class="[
              'rounded-2xl border-2 p-4 transition-all cursor-pointer',
              activePhaseIndex === index ? 'border-[#7C4728] bg-[#FDF6EE]/50' : 'border-gray-200 bg-white hover:border-gray-300'
            ]"
            @click="activePhaseIndex = index"
            @dragover.prevent="onDragOver($event, index)"
            @drop="onDrop($event, index)"
          >
            <!-- Phase header -->
            <div class="flex items-center gap-2 mb-3">
              <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-[#7C4728] text-white whitespace-nowrap">
                Phase {{ phase.phaseNumber }}
              </span>
              <input
                v-model="phase.title"
                type="text"
                @click.stop
                :placeholder="`Phase ${phase.phaseNumber} title`"
                class="flex-1 text-sm font-bold bg-transparent border-b border-gray-200 focus:border-[#7C4728] outline-none pb-0.5"
              />
              <button
                v-if="phases.length > 1"
                type="button"
                @click.stop="removePhase(index)"
                class="text-gray-400 hover:text-red-500 transition text-xs"
              >
                ✕
              </button>
            </div>

            <!-- Amount + revisions -->
            <div class="grid grid-cols-2 gap-2 mb-3">
              <div>
                <label class="text-xs text-gray-500 font-bold">Amount (IDR)</label>
                <input
                  v-model.number="phase.amount"
                  type="number"
                  min="0"
                  @click.stop
                  placeholder="0"
                  class="w-full mt-1 px-2 py-1.5 border border-gray-200 rounded-lg text-sm focus:border-[#7C4728] outline-none"
                />
              </div>
              <div>
                <label class="text-xs text-gray-500 font-bold">Revision rounds</label>
                <input
                  v-model.number="phase.revisionRounds"
                  type="number"
                  min="0"
                  max="10"
                  @click.stop
                  placeholder="0"
                  class="w-full mt-1 px-2 py-1.5 border border-gray-200 rounded-lg text-sm focus:border-[#7C4728] outline-none"
                />
              </div>
            </div>

            <!-- Drop zone + assigned deliverables -->
            <div
              class="min-h-[48px] border border-dashed border-gray-300 rounded-xl p-2 flex flex-wrap gap-1.5 transition-colors"
              :class="dragOverPhaseIndex === index ? 'bg-[#F5E6D3]/50 border-[#C5A17A]' : ''"
            >
              <span
                v-if="phase.deliverables.length === 0"
                class="text-xs text-gray-400 self-center px-1"
              >
                Drop deliverables here or click above to assign
              </span>
              <span
                v-for="d in phase.deliverables"
                :key="d"
                class="inline-flex items-center gap-1 px-2 py-1 bg-[#F5E6D3] text-[#7C4728] rounded-full text-xs font-medium"
              >
                {{ getLabelForValue(d) }}
                <button
                  type="button"
                  @click.stop="removeDeliverable(index, d)"
                  class="text-[#C5A17A] hover:text-red-500 leading-none"
                >×</button>
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

const deliverableGroups = [
  {
    category: 'Site Analysis & Planning',
    items: [
      { value: 'SITE_ANALYSIS', label: 'Site Analysis' },
      { value: 'ZONING_STUDY', label: 'Zoning Study' }
    ]
  },
  {
    category: 'Design Phases',
    items: [
      { value: 'CONCEPT_DESIGN', label: 'Concept Design' },
      { value: 'SCHEMATIC_DESIGN', label: 'Schematic Design' },
      { value: 'DESIGN_DEVELOPMENT', label: 'Design Development' },
      { value: 'CONSTRUCTION_DOCS', label: 'Construction Documents' }
    ]
  },
  {
    category: 'Permits & Documentation',
    items: [
      { value: 'IMB_PERMIT', label: 'IMB (Building Permit)' },
      { value: 'SLF_CERT', label: 'SLF Certificate' },
      { value: 'ENVIRONMENTAL_PERMIT', label: 'Environmental Permit' }
    ]
  },
  {
    category: 'Specialized Services',
    items: [
      { value: 'INTERIOR_DESIGN', label: 'Interior Design' },
      { value: 'LANDSCAPE_DESIGN', label: 'Landscape Design' },
      { value: 'MEP_DESIGN', label: 'MEP Design' },
      { value: 'STRUCTURAL_DESIGN', label: 'Structural Design' }
    ]
  },
  {
    category: 'Construction Support',
    items: [
      { value: 'SUPERVISION', label: 'Construction Supervision' },
      { value: 'AS_BUILT', label: 'As-Built Drawings' }
    ]
  }
]

const allDeliverableMap = Object.fromEntries(
  deliverableGroups.flatMap(g => g.items.map(i => [i.value, i.label]))
)

const getLabelForValue = value => allDeliverableMap[value] || value

const newPhase = number => ({ phaseNumber: number, title: '', deliverables: [], amount: 0, revisionRounds: null })

const initPhases = incoming => {
  if (incoming && incoming.length > 0) {
    return incoming.map(p => ({ ...p, deliverables: p.deliverables || [] }))
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
    emit('update:modelValue', val.map(p => ({
      phaseNumber: p.phaseNumber,
      title: p.title || `Phase ${p.phaseNumber}`,
      deliverables: p.deliverables,
      amount: Number(p.amount) || 0,
      revisionRounds: p.revisionRounds ?? null
    })))
  },
  { deep: true }
)

const isAssigned = value => phases.value.some(p => p.deliverables.includes(value))

const assignToActivePhase = item => {
  if (isAssigned(item.value)) return
  phases.value[activePhaseIndex.value].deliverables.push(item.value)
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
  phases.value.forEach((p, i) => { p.phaseNumber = i + 1 })
  if (activePhaseIndex.value >= phases.value.length) {
    activePhaseIndex.value = phases.value.length - 1
  }
}

const phasesTotal = computed(() =>
  phases.value.reduce((sum, p) => sum + (Number(p.amount) || 0), 0)
)

const totalMatchesBid = computed(() => {
  if (!props.bidAmount) return false
  return Math.abs(phasesTotal.value - Number(props.bidAmount)) < 1
})

const formatCurrency = value => {
  if (!value && value !== 0) return 'N/A'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', notation: 'compact', compactDisplay: 'short' }).format(value)
}

const chipClasses = value => {
  const base = 'px-3 py-1 rounded-full text-xs font-medium border transition-all cursor-pointer'
  if (isAssigned(value)) return `${base} bg-gray-100 text-gray-400 border-gray-200 opacity-50 cursor-not-allowed`
  return `${base} bg-[#F5E6D3] text-[#7C4728] border-[#C5A17A] hover:bg-[#7C4728] hover:text-white`
}

const onDragStart = (event, item) => {
  draggedItem = item
  event.dataTransfer.effectAllowed = 'move'
}

const onDragOver = (event, index) => {
  dragOverPhaseIndex.value = index
}

const onDrop = (event, phaseIndex) => {
  dragOverPhaseIndex.value = null
  if (!draggedItem || isAssigned(draggedItem.value)) return
  phases.value[phaseIndex].deliverables.push(draggedItem.value)
  draggedItem = null
}
</script>
