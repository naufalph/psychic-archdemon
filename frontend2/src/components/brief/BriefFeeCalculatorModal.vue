<template>
  <BriefModal :open="open" panel-class="max-w-[1040px] p-6 md:p-8 !bg-surface-alt" @close="$emit('close')">
    <div class="flex items-start justify-between gap-4 mb-7">
      <div>
        <h2 class="text-[28px] font-bold tracking-[-.025em] m-0 mb-1.5 text-ink-900">{{ tv.title }}</h2>
        <p class="text-[15px] text-ink-400 m-0">{{ tv.subtitle }}</p>
      </div>
      <button
        type="button"
        :aria-label="t.brief.privacy.close"
        class="flex-none w-11 h-11 rounded-full border border-hairline bg-white text-ink-900 flex items-center justify-center hover:border-ink-900 transition-colors"
        @click="$emit('close')"
      >
        <X :size="18" :stroke-width="2.2" />
      </button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 items-stretch">
      <div class="bg-white border border-hairline rounded-[20px] p-6 md:p-8">
        <label class="block text-body font-medium text-ink-700 mb-3">{{ tv.workLabel }}</label>
        <div class="flex gap-2 flex-wrap mb-7">
          <button
            v-for="w in works"
            :key="w.id"
            type="button"
            class="h-11 px-5 rounded-full text-[15px] font-semibold border transition-colors"
            :class="
              work === w.id
                ? 'bg-ink-900 text-white border-ink-900'
                : 'bg-white text-ink-900 border-hairline hover:border-ink-500'
            "
            @click="work = w.id"
          >
            {{ w.label }}
          </button>
        </div>

        <label class="block text-body font-medium text-ink-700 mb-1">
          {{ work === 'renovasi' ? tv.budgetLabelRenovasi : tv.budgetLabelBaru }}
        </label>
        <p class="text-caption text-ink-400 mb-3">{{ tv.budgetHint }}</p>
        <div class="flex items-center gap-3 flex-wrap">
          <input
            v-model="budgetMin"
            type="number"
            min="0"
            step="100000000"
            :placeholder="tv.minPlaceholder"
            class="w-[200px] box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body font-semibold text-ink-900 focus:outline-none focus:border-ink-900"
          />
          <span class="text-[15px] text-ink-400">{{ tv.until }}</span>
          <input
            v-model="budgetMax"
            type="number"
            min="0"
            step="100000000"
            :placeholder="tv.maxPlaceholder"
            class="w-[200px] box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body font-semibold text-ink-900 focus:outline-none focus:border-ink-900"
          />
        </div>
      </div>

      <div
        class="rounded-[20px] p-6 md:p-8 flex flex-col text-white"
        style="background: linear-gradient(135deg, #2f7dc0, #185c93)"
      >
        <span class="text-caption-sm font-semibold uppercase tracking-[.06em] text-white/70">{{ tv.eyebrow }}</span>

        <template v-if="result.feeText">
          <button
            type="button"
            :title="tv.useTitle"
            class="flex items-center justify-between gap-3 w-full text-left mt-4 mb-3 px-4 py-3.5 rounded-[14px] border border-white/40 bg-white/[.08] text-white hover:bg-white/[.18] hover:border-white transition-colors"
            @click="useFee"
          >
            <span class="text-[30px] font-bold tracking-[-.025em] leading-[1.15]">{{ result.feeText }}</span>
            <span
              class="flex-none inline-flex items-center gap-1.5 h-8 px-3 rounded-full bg-white text-ink-900 text-caption-sm font-bold"
            >
              {{ tv.use }} <Check :size="14" :stroke-width="2.6" />
            </span>
          </button>
          <p class="text-[15px] font-semibold m-0 mb-1">{{ tv.subtitle1 }}</p>
          <p class="text-[15px] font-semibold m-0 mb-4">{{ tv.subtitle2 }}</p>
          <p class="text-[15px] text-white/80 leading-relaxed m-0 mb-6">{{ summary }}</p>

          <button
            type="button"
            class="w-full flex justify-between items-center gap-3 border-y border-white/[.18] py-4 mb-6 text-caption font-semibold text-white text-left"
            @click="showCalc = !showCalc"
          >
            {{ tv.calcToggle }}
            <span class="text-[18px] font-normal text-white/70">{{ showCalc ? '−' : '+' }}</span>
          </button>

          <div v-if="showCalc" class="flex flex-col gap-2.5 -mt-2 mb-6">
            <div v-for="row in calcRows" :key="row.k" class="flex justify-between items-baseline gap-4 text-caption">
              <span class="text-white/70">{{ row.k }}</span>
              <span class="font-semibold text-right">{{ row.v }}</span>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="text-[34px] font-bold tracking-[-.025em] mt-4 mb-3 leading-[1.15]">—</div>
          <p class="text-[15px] text-white/85 leading-relaxed m-0 mb-6">{{ result.notice }}</p>
        </template>

        <p class="text-caption-sm text-white/70 leading-relaxed mt-auto mb-0">{{ tv.disclaimer }}</p>
      </div>
    </div>
  </BriefModal>
</template>

<script setup>
import { ref, computed } from 'vue'
import { X, Check } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { estimateDesignFee, FEE_CATEGORY } from '@/utils/architectFee'
import BriefModal from './BriefModal.vue'

defineProps({ open: { type: Boolean, default: false } })
const emit = defineEmits(['close', 'use'])

const { t } = useI18n()
const tv = computed(() => t.value.brief.calculator)

const works = computed(() => [
  { id: 'baru', label: tv.value.workBaru },
  { id: 'renovasi', label: tv.value.workRenovasi }
])

const work = ref('baru')
const budgetMin = ref('1000000000')
const budgetMax = ref('')
const showCalc = ref(false)

const result = computed(() => {
  const r = estimateDesignFee(work.value, budgetMin.value, budgetMax.value)
  if (r.notice) return { notice: tv.value[r.notice] }
  return r
})

const summary = computed(() => {
  const r = result.value
  if (!r.feeText) return ''
  return `${r.work.id === 'renovasi' ? tv.value.workRenovasi : tv.value.workBaru} · budget ${r.budgetText}`
})

const calcRows = computed(() => {
  const r = result.value
  if (!r.feeText) return []
  return [
    { k: tv.value.calcBudget, v: r.budgetText },
    { k: tv.value.calcScope, v: tv.value.calcScopeValue },
    {
      k: tv.value.calcCategory,
      v: `Kategori ${FEE_CATEGORY}${r.work.id === 'renovasi' ? ` · ${tv.value.renovationAdjustment}` : ''}`
    },
    { k: tv.value.calcMethod, v: tv.value.calcMethodValue }
  ]
})

const useFee = () => {
  const r = result.value
  if (!r.feeText) return
  emit('use', { min: Math.round(r.lo), max: r.hi ? Math.round(r.hi) : null })
}
</script>
