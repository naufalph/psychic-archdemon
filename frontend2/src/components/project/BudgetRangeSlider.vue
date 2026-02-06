<template>
  <div class="space-y-4">
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">
        {{ label }}<span v-if="required" class="text-red-500">*</span>
      </label>
      <p v-if="description" class="text-xs text-gray-500 mb-2">{{ description }}</p>

      <div class="relative">
        <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 font-medium">IDR</span>
        <input
          type="text"
          v-model="totalDisplay"
          @input="handleTotalInput"
          @blur="formatTotalDisplay"
          :required="required"
          placeholder="e.g., 2.000.000.000"
          class="w-full pl-16 pr-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none text-right font-medium"
        />
      </div>
    </div>

    <div v-if="localTotal > 0" class="space-y-4 bg-gray-50 rounded-2xl p-6">
      <div class="flex items-center justify-between">
        <span class="text-sm font-medium text-gray-700">Flexible Range</span>
        <div class="text-sm text-gray-600 font-medium">
          {{ formatCurrency(localMin) }} - {{ formatCurrency(localMax) }}
        </div>
      </div>

      <div class="relative pt-6 pb-4">
        <div class="relative h-2 bg-gray-200 rounded-full">
          <div
            class="absolute h-2 bg-[#7C4728] rounded-full"
            :style="{
              left: minPercent + '%',
              width: maxPercent - minPercent + '%'
            }"
          />

          <div
            class="absolute w-1 h-4 bg-gray-400 rounded-full -top-1"
            :style="{ left: totalPercent + '%', transform: 'translateX(-50%)' }"
            :title="`Total: ${formatCurrency(localTotal)}`"
          />
        </div>

        <input
          type="range"
          :min="sliderMin"
          :max="sliderMax"
          :step="step"
          v-model.number="localMin"
          @input="handleMinChange"
          class="absolute w-full h-2 bg-transparent appearance-none top-6"
          style="z-index: 3; pointer-events: auto"
        />

        <input
          type="range"
          :min="sliderMin"
          :max="sliderMax"
          :step="step"
          v-model.number="localMax"
          @input="handleMaxChange"
          class="absolute w-full h-2 bg-transparent appearance-none top-6"
          style="z-index: 4; pointer-events: auto"
        />
      </div>

      <div class="flex items-center justify-between text-xs text-gray-500">
        <span>{{ formatCurrency(sliderMin) }}</span>
        <span class="text-gray-700 font-medium">Total: {{ formatCurrency(localTotal) }}</span>
        <span>{{ formatCurrency(sliderMax) }}</span>
      </div>
    </div>

    <div v-if="hint" class="bg-[#F5E6D3]/30 p-4 rounded-xl border border-[#C5A17A]/20 flex gap-3">
      <Info :size="20" class="text-[#7C4728] flex-shrink-0" />
      <p class="text-xs text-gray-700 leading-relaxed" v-html="hint" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Info } from 'lucide-vue-next'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ total: 0, min: 0, max: 0 })
  },
  label: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  step: {
    type: Number,
    default: 10000000
  },
  required: {
    type: Boolean,
    default: false
  },
  hint: {
    type: String,
    default: ''
  },
  rangePercent: {
    type: Number,
    default: 30
  }
})

const emit = defineEmits(['update:modelValue'])

const localTotal = ref(props.modelValue.total || 0)
const localMin = ref(props.modelValue.min || 0)
const localMax = ref(props.modelValue.max || 0)
const totalDisplay = ref(formatNumberWithCommas(props.modelValue.total || 0))

watch(
  () => props.modelValue,
  newValue => {
    if (newValue) {
      localTotal.value = newValue.total || 0
      localMin.value = newValue.min || 0
      localMax.value = newValue.max || 0
      totalDisplay.value = formatNumberWithCommas(newValue.total || 0)
    }
  },
  { deep: true }
)

const sliderMin = computed(() => {
  if (localTotal.value === 0) return 0
  return Math.max(0, Math.floor((localTotal.value * (1 - props.rangePercent / 100)) / props.step) * props.step)
})

const sliderMax = computed(() => {
  if (localTotal.value === 0) return 0
  return Math.ceil((localTotal.value * (1 + props.rangePercent / 100)) / props.step) * props.step
})

const minPercent = computed(() => {
  if (sliderMax.value === sliderMin.value) return 0
  return ((localMin.value - sliderMin.value) / (sliderMax.value - sliderMin.value)) * 100
})

const maxPercent = computed(() => {
  if (sliderMax.value === sliderMin.value) return 100
  return ((localMax.value - sliderMin.value) / (sliderMax.value - sliderMin.value)) * 100
})

const totalPercent = computed(() => {
  if (sliderMax.value === sliderMin.value) return 50
  return ((localTotal.value - sliderMin.value) / (sliderMax.value - sliderMin.value)) * 100
})

function formatNumberWithCommas(num) {
  if (!num || num === 0) return ''
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

function formatCurrencyWithCommas(value) {
  if (!value && value !== 0) return 'IDR 0'
  return 'IDR ' + value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

function parseNumberFromDisplay(str) {
  return parseInt(str.replace(/\./g, ''), 10) || 0
}

function handleTotalInput(event) {
  const value = event.target.value.replace(/[^0-9]/g, '')
  const numValue = parseInt(value, 10) || 0

  localTotal.value = numValue
  totalDisplay.value = formatNumberWithCommas(numValue)

  if (numValue > 0) {
    localMin.value = sliderMin.value
    localMax.value = sliderMax.value
  } else {
    localMin.value = 0
    localMax.value = 0
  }

  emitValue()
}

function formatTotalDisplay() {
  totalDisplay.value = formatNumberWithCommas(localTotal.value)
}

const handleMinChange = () => {
  if (localMin.value > localMax.value) {
    localMin.value = localMax.value
  }
  emitValue()
}

const handleMaxChange = () => {
  if (localMax.value < localMin.value) {
    localMax.value = localMin.value
  }
  emitValue()
}

function emitValue() {
  emit('update:modelValue', {
    total: localTotal.value,
    min: localMin.value,
    max: localMax.value
  })
}

const formatCurrency = value => {
  if (!value && value !== 0) return 'IDR 0'
  return 'IDR ' + value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}
</script>

<style scoped>
input[type='range']::-webkit-slider-thumb {
  appearance: none;
  pointer-events: all;
  width: 20px;
  height: 20px;
  background-color: #7c4728;
  border-radius: 50%;
  cursor: pointer;
  border: 3px solid white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

input[type='range']::-moz-range-thumb {
  appearance: none;
  pointer-events: all;
  width: 20px;
  height: 20px;
  background-color: #7c4728;
  border-radius: 50%;
  cursor: pointer;
  border: 3px solid white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

input[type='range']::-webkit-slider-thumb:hover {
  background-color: #5a3319;
}

input[type='range']::-moz-range-thumb:hover {
  background-color: #5a3319;
}
</style>
