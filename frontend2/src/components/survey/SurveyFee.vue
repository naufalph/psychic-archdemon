<template>
  <div class="max-w-[680px] flex flex-col gap-5 animate-brief-rise">
    <div class="relative">
      <span class="absolute left-[18px] top-[19px] text-[18px] font-semibold text-ink-400 pointer-events-none">Rp</span>
      <input
        :value="state.designFee"
        type="text"
        inputmode="numeric"
        placeholder="cth. 150.000.000"
        aria-label="Budget fee desain"
        class="w-full box-border h-16 pl-[54px] pr-[18px] border border-hairline rounded-[14px] bg-white text-[20px] font-semibold text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors"
        @input="onInput"
      />
    </div>
    <p v-if="rangeError" class="text-caption text-[#B42318] -mt-2 mb-0">
      Angka kedua sebaiknya lebih besar dari angka pertama.
    </p>

    <div class="flex items-center justify-between gap-4 flex-wrap p-5 rounded-2xl bg-surface-alt">
      <p class="flex-[1_1_280px] text-[15px] leading-normal text-ink-700 m-0">
        Belum tahu angka yang wajar? Hitung perkiraannya dari budget pembangunan dengan panduan IAI.
      </p>
      <button
        type="button"
        class="flex-none inline-flex items-center gap-2 h-11 px-[18px] rounded-full border border-ink-900 bg-white text-ink-900 text-[15px] font-bold hover:bg-surface-alt transition-colors"
        @click="calcOpen = true"
      >
        <Calculator :size="16" :stroke-width="2" /> Buka kalkulator
      </button>
    </div>

    <BriefFeeCalculatorModal :open="calcOpen" @close="calcOpen = false" @use="useFee" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Calculator } from 'lucide-vue-next'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { formatIDRDisplay } from '@/utils/currencyFormat'
import BriefFeeCalculatorModal from '@/components/brief/BriefFeeCalculatorModal.vue'

const survey = useProjectSurvey()
const state = survey.state

const calcOpen = ref(false)

const rangeError = computed(() => {
  const { min, max } = survey.feeRange.value
  return min != null && max != null && max < min
})

// A single amount or a "min – max" range (what the calculator fills in), each part grouped
// with thousand separators as it is typed
const onInput = event => {
  const formatted = event.target.value
    .split(/[–-]/)
    .slice(0, 2)
    .map(part => formatIDRDisplay(part))
    .join(event.target.value.match(/[–-]/) ? ' – ' : '')
  state.designFee = formatted
  event.target.value = formatted
}

const useFee = ({ min, max }) => {
  state.designFee = max ? `${formatIDRDisplay(min)} – ${formatIDRDisplay(max)}` : formatIDRDisplay(min)
  calcOpen.value = false
}
</script>
