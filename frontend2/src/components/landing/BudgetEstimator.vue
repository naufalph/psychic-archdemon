<template>
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 items-stretch">
    <div class="bg-white border border-hairline rounded-[20px] p-8">
      <label class="block text-body-sm font-semibold mb-3">{{ tv.jenisLabel }}</label>
      <div class="flex gap-2 flex-wrap mb-7">
        <button
          v-for="w in WORKS"
          :key="w.id"
          class="h-11 px-5 rounded-full text-body-sm font-semibold border transition-colors"
          :class="
            work === w.id
              ? 'bg-ink-900 text-white border-ink-900'
              : 'bg-white text-ink-900 border-hairline hover:border-ink-500'
          "
          @click="work = w.id"
        >
          {{ w.id === 'baru' ? tv.workBaru : tv.workRenovasi }}
        </button>
      </div>

      <div>
        <label class="block text-body-sm font-semibold mb-1">
          {{ work === 'renovasi' ? tv.budgetLabelRenovasi : tv.budgetLabelBaru }}
        </label>
        <p class="text-caption text-ink-500 mb-3">{{ tv.budgetHint }}</p>
        <div class="flex items-center gap-3 flex-wrap mb-4">
          <input
            v-model="budgetMin"
            type="number"
            min="0"
            step="100000000"
            :placeholder="tv.minPlaceholder"
            class="w-[200px] box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body font-semibold text-ink-900 focus:outline-none focus:ring-2 focus:ring-ink-200"
          />
          <span class="text-body-sm text-ink-500">{{ tv.sampai }}</span>
          <input
            v-model="budgetMax"
            type="number"
            min="0"
            step="100000000"
            :placeholder="tv.maxPlaceholder"
            class="w-[200px] box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body font-semibold text-ink-900 focus:outline-none focus:ring-2 focus:ring-ink-200"
          />
        </div>
      </div>

      <p class="text-body-sm text-ink-500 mt-7 pt-6 border-t border-hairline leading-relaxed">
        {{ tv.otherNeeds }}
        <router-link to="/brief-proyek" class="text-accent-blue font-semibold">{{ tv.otherNeedsCta }}</router-link>
      </p>
    </div>

    <div
      class="rounded-[20px] p-8 flex flex-col text-white"
      style="background: linear-gradient(135deg, #2f7dc0, #185c93)"
    >
      <span class="text-caption-sm font-semibold uppercase tracking-[.06em] text-white/70">{{ tv.feeEyebrow }}</span>
      <div class="text-[34px] font-bold tracking-[-.025em] leading-[1.15] mt-4 mb-3">{{ feeText }}</div>

      <template v-if="result.feeText">
        <p class="text-body-sm text-white font-semibold mb-1">{{ tv.feeSubtitle1 }}</p>
        <p class="text-body-sm text-white font-semibold mb-4">{{ tv.feeSubtitle2 }}</p>
        <p class="text-body-sm text-white/80 mb-6 leading-relaxed">{{ summary }}</p>

        <button
          class="w-full flex justify-between items-center gap-3 bg-transparent border-t border-b border-white/[.18] py-4 mb-6 text-caption font-semibold text-white text-left"
          @click="showCalc = !showCalc"
        >
          {{ tv.calcToggle }}
          <span class="text-[18px] font-normal text-white/70">{{ showCalc ? '−' : '+' }}</span>
        </button>

        <div v-if="showCalc" class="flex flex-col gap-2.5 -mt-2 mb-6">
          <div v-for="row in calcRows" :key="row.k" class="flex justify-between items-baseline gap-4 text-caption">
            <span class="text-white/70">{{ row.k }}</span>
            <span class="text-white font-semibold text-right">{{ row.v }}</span>
          </div>
        </div>
      </template>

      <p v-else class="text-body-sm text-white/85 leading-relaxed mb-6">{{ result.notice }}</p>

      <p class="text-caption-sm text-white/70 leading-relaxed mt-auto mb-1">{{ tv.estimateNote }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from '@/composables/useI18n'
import { estimateDesignFee, FEE_CATEGORY } from '@/utils/architectFee'

const { t } = useI18n()
const tv = computed(() => t.value.landing.v2.estimasi)

const WORKS = [{ id: 'baru' }, { id: 'renovasi' }]

const work = ref('baru')
const budgetMin = ref('1000000000')
const budgetMax = ref('')
const showCalc = ref(false)

const result = computed(() => {
  const r = estimateDesignFee(work.value, budgetMin.value, budgetMax.value)
  if (r.notice === 'fillBudget') return { notice: tv.value.fillBudgetNotice }
  if (r.notice === 'invalidRange') return { notice: tv.value.invalidRangeNotice }
  return r
})

const feeText = computed(() => result.value.feeText || '—')

const summary = computed(() => {
  const r = result.value
  if (!r.feeText) return ''
  const label = r.work.id === 'renovasi' ? tv.value.workRenovasi : tv.value.workBaru
  return `${label} · budget ${r.budgetText}`
})

const calcRows = computed(() => {
  const r = result.value
  if (!r.feeText) return []
  return [
    { k: tv.value.calcBudgetLabel, v: r.budgetText },
    { k: tv.value.calcScopeLabel, v: tv.value.calcScopeValue },
    {
      k: tv.value.calcCategoryLabel,
      v: `Kategori ${FEE_CATEGORY}${r.work.id === 'renovasi' ? ` · ${tv.value.renovationAdjustment}` : ''}`
    },
    { k: tv.value.calcMethodLabel, v: tv.value.calcMethodValue }
  ]
})
</script>
