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
        <router-link to="/signup" class="text-accent-blue font-semibold">{{ tv.otherNeedsCta }}</router-link>
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

const { t } = useI18n()
const tv = computed(() => t.value.landing.v2.estimasi)

// Pedoman IAI 2007 (Pasal 50, 51, 61 + Lampiran 2.A)
// Kolom biaya dalam miliar rupiah; kolom kategori dalam persen.
const PCT_TABLE = [
  [0.2, 6.5, 7.0, 8.0],
  [2, 5.51, 5.9, 6.48],
  [4, 4.78, 5.13, 5.6],
  [20, 4.2, 4.52, 4.92],
  [40, 3.71, 4.01, 4.38],
  [60, 3.29, 3.58, 3.92],
  [80, 2.92, 3.2, 3.52],
  [100, 2.6, 2.88, 3.18],
  [120, 2.32, 2.59, 2.88],
  [140, 2.07, 2.34, 2.62],
  [160, 1.86, 2.12, 2.39],
  [180, 1.67, 1.98, 2.2],
  [200, 1.51, 1.76, 2.03],
  [220, 1.37, 1.62, 1.88],
  [240, 1.25, 1.51, 1.76],
  [260, 1.16, 1.41, 1.67],
  [280, 1.09, 1.34, 1.59],
  [300, 1.04, 1.29, 1.54],
  [500, 1.0, 1.25, 1.5]
]

const STAGES = [
  { id: 'konsep', w: 0.1 },
  { id: 'pra', w: 0.15 },
  { id: 'pengembangan', w: 0.3 },
  { id: 'gambar', w: 0.25 },
  { id: 'pengadaan', w: 0.1 },
  { id: 'pengawasan', w: 0.1 }
]
const SCOPE_GAMBAR = ['konsep', 'pra', 'pengembangan', 'gambar']

const WORKS = [
  { id: 'baru', f: 1.0 },
  { id: 'renovasi', f: 1.5 }
]

function feePercent(cat, B) {
  const m = B / 1e9
  const last = PCT_TABLE.length - 1
  if (m <= PCT_TABLE[0][0]) return PCT_TABLE[0][cat]
  if (m >= PCT_TABLE[last][0]) return PCT_TABLE[last][cat]
  for (let i = 0; i < last; i++) {
    const b1 = PCT_TABLE[i][0]
    const b2 = PCT_TABLE[i + 1][0]
    if (m >= b1 && m <= b2) {
      const p1 = PCT_TABLE[i][cat]
      const p2 = PCT_TABLE[i + 1][cat]
      return p1 + ((m - b1) / (b2 - b1)) * (p2 - p1)
    }
  }
  return PCT_TABLE[last][cat]
}

function scopeFactor(stageIds) {
  const S = STAGES.filter(s => stageIds.indexOf(s.id) >= 0).reduce((a, s) => a + s.w, 0)
  if (S >= 0.999) return 1
  return S + Math.min(0.5 * (1 - S), 0.2)
}

function estimateFee(B, cat, sf, wf) {
  const p = feePercent(cat, B)
  return { pct: p, fee: (B * p * sf * wf) / 100 }
}

const rp = n => `Rp ${  Math.round(n).toLocaleString('id-ID')}`
const num = v => {
  const n = Number(v)
  return v === '' || v === null || isNaN(n) ? null : n
}

const work = ref('baru')
const budgetMin = ref('1000000000')
const budgetMax = ref('')
const showCalc = ref(false)

// Kategori bangunan tetap di kategori 3 (mengikuti perilaku desain sumber, yang belum
// menampilkan pemilih jenis bangunan pada halaman ini).
const CATEGORY = 3

const result = computed(() => {
  const workDef = WORKS.find(w => w.id === work.value)
  const bMin = num(budgetMin.value)
  let bMax = num(budgetMax.value)

  if (bMin === null || bMin <= 0) return { notice: tv.value.fillBudgetNotice }
  if (bMax !== null && bMax > 0 && bMax < bMin) return { notice: tv.value.invalidRangeNotice }
  if (bMax !== null && bMax === bMin) bMax = null

  const sf = scopeFactor(SCOPE_GAMBAR)
  const wf = workDef.f
  const lo = estimateFee(bMin, CATEGORY, sf, wf)
  const hi = bMax !== null ? estimateFee(bMax, CATEGORY, sf, wf) : null

  return {
    bMin,
    bMax,
    workDef,
    feeText: hi ? `${rp(lo.fee)  } – ${  rp(hi.fee)}` : rp(lo.fee)
  }
})

const feeText = computed(() => result.value.feeText || '—')

const summary = computed(() => {
  const r = result.value
  if (!r.feeText) return ''
  const budget = r.bMax ? `${rp(r.bMin)  } – ${  rp(r.bMax)}` : rp(r.bMin)
  const label = r.workDef.id === 'renovasi' ? tv.value.workRenovasi : tv.value.workBaru
  return `${label  } · budget ${  budget}`
})

const calcRows = computed(() => {
  const r = result.value
  if (!r.feeText) return []
  const budget = r.bMax ? `${rp(r.bMin)  } – ${  rp(r.bMax)}` : rp(r.bMin)
  return [
    { k: tv.value.calcBudgetLabel, v: budget },
    { k: tv.value.calcScopeLabel, v: tv.value.calcScopeValue },
    {
      k: tv.value.calcCategoryLabel,
      v: `Kategori ${  CATEGORY  }${r.workDef.id === 'renovasi' ? ` · ${  tv.value.renovationAdjustment}` : ''}`
    },
    { k: tv.value.calcMethodLabel, v: tv.value.calcMethodValue }
  ]
})
</script>
