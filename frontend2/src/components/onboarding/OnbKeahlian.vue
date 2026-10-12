<template>
  <div>
    <OnbHeading :title="title" :lead="lead" />

    <div class="flex flex-col gap-8 animate-brief-rise">
      <div v-if="chosen.length" class="flex flex-wrap gap-2" aria-live="polite">
        <span
          v-for="c in chosen"
          :key="c.value"
          class="inline-flex items-center gap-1.5 min-h-[40px] pl-4 pr-2 rounded-xl bg-ink-900 text-white text-[15px] font-semibold"
        >
          {{ c.label }}
          <button
            type="button"
            class="w-6 h-6 rounded-full hover:bg-white/15 flex items-center justify-center"
            :aria-label="`Hapus ${c.label}`"
            @click="toggle('keahlian', c.value)"
          >
            <X :size="14" :stroke-width="2.6" />
          </button>
        </span>
      </div>

      <section v-if="showArsitek" aria-label="Keahlian arsitektur">
        <div v-if="both" class="mb-3">
          <div class="text-body font-medium text-ink-700">Arsitektur</div>
          <p class="text-caption leading-normal text-ink-400 m-0 mt-1">Tipe bangunan menurut klasifikasi IAI.</p>
        </div>
        <div class="flex flex-col gap-3">
          <details
            v-for="cat in cats"
            :key="cat.value"
            class="group bg-white border border-hairline rounded-[20px] overflow-hidden"
            :open="cat.open"
          >
            <summary
              class="list-none [&::-webkit-details-marker]:hidden flex items-center gap-4 px-6 md:px-8 py-5 cursor-pointer hover:bg-surface-alt transition-colors"
            >
              <span class="flex-1 min-w-0 flex flex-col gap-1">
                <span class="text-[18px] font-bold tracking-[-.02em] text-ink-900">{{ cat.label }}</span>
                <span class="text-caption text-ink-400">{{ cat.summary }}</span>
              </span>
              <span
                v-if="cat.count"
                class="inline-flex items-center h-[22px] px-2.5 rounded-full bg-ink-900 text-white text-micro-sm uppercase tracking-[.05em] whitespace-nowrap"
              >
                {{ cat.count }} dipilih
              </span>
              <ChevronDown
                :size="20"
                :stroke-width="2"
                class="flex-none text-ink-400 transition-transform duration-300 group-open:rotate-180"
              />
            </summary>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-2.5 px-6 md:px-8 pt-1 pb-6 md:pb-8">
              <button
                v-for="t in cat.types"
                :key="t.value"
                type="button"
                role="checkbox"
                :aria-checked="on(t.value)"
                :class="[typeCls, on(t.value) ? 'border-ink-900' : 'border-hairline hover:border-ink-900']"
                @click="toggle('keahlian', t.value)"
              >
                <SurveyCheck :on="on(t.value)" size="sm" class="mt-0.5" />
                <span class="flex flex-col gap-1 min-w-0">
                  <span class="text-[15px] font-semibold text-ink-900">{{ t.labelId }}</span>
                  <span class="text-caption leading-normal text-ink-400">{{ t.examplesId }}</span>
                </span>
              </button>
            </div>
          </details>
        </div>
      </section>

      <section v-if="showInterior" aria-label="Keahlian interior">
        <div v-if="both" class="mb-3">
          <div class="text-body font-medium text-ink-700">Interior</div>
          <p class="text-caption leading-normal text-ink-400 m-0 mt-1">Jenis proyek interior yang Anda kerjakan.</p>
        </div>
        <div class="bg-white border border-hairline rounded-[20px] p-6 md:p-8">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2.5">
            <button
              v-for="t in INTERIOR_TYPES"
              :key="t.value"
              type="button"
              role="checkbox"
              :aria-checked="on(t.value)"
              :class="[typeCls, on(t.value) ? 'border-ink-900' : 'border-hairline hover:border-ink-900']"
              @click="toggle('keahlian', t.value)"
            >
              <SurveyCheck :on="on(t.value)" size="sm" class="mt-0.5" />
              <span class="flex flex-col gap-1 min-w-0">
                <span class="text-[15px] font-semibold text-ink-900">{{ t.label }}</span>
                <span class="text-caption leading-normal text-ink-400">{{ t.examples }}</span>
              </span>
            </button>
          </div>
        </div>
      </section>

      <p v-if="error" class="text-caption text-[#B42318] m-0">{{ error }}</p>
    </div>

    <OnbFooter
      next-label="Kirim"
      hint="Pilih minimal satu keahlian"
      :disabled="!chosen.length"
      :loading="saving"
      @back="$emit('back')"
      @next="$emit('next')"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ChevronDown, X } from 'lucide-vue-next'
import OnbHeading from './OnbHeading.vue'
import OnbFooter from './OnbFooter.vue'
import SurveyCheck from '@/components/survey/SurveyCheck.vue'
import { IAI_CATEGORIES, IAI_TYPES, typesForCategory } from '@/constants/iaiTaxonomy'
import { INTERIOR_TYPES, IAI_OPEN_AT_START, IAI_CATEGORY_SHORT } from '@/constants/architectOnboarding'
import { useArchitectOnboarding } from '@/composables/useArchitectOnboarding'

defineProps({
  saving: { type: Boolean, default: false },
  error: { type: String, default: '' }
})
defineEmits(['back', 'next'])

const { state, showArsitek, showInterior, activeKeahlian, toggle } = useArchitectOnboarding()
const on = v => state.keahlian.includes(v)
const both = computed(() => showArsitek.value && showInterior.value)

const cats = computed(() =>
  IAI_CATEGORIES.map(cat => {
    const types = typesForCategory(cat.value)
    return {
      value: cat.value,
      label: cat.labelId,
      types,
      open: IAI_OPEN_AT_START.includes(cat.value),
      summary: types.map(t => t.labelId).join(' · '),
      count: types.filter(t => on(t.value)).length
    }
  })
)

const chosen = computed(() =>
  activeKeahlian.value.map(v => {
    const iai = IAI_TYPES.find(t => t.value === v)
    if (iai) return { value: v, label: `${IAI_CATEGORY_SHORT[iai.category]} · ${iai.labelId}` }
    return { value: v, label: INTERIOR_TYPES.find(t => t.value === v)?.label || v }
  })
)

const title = computed(() =>
  both.value
    ? 'Apa saja keahlian Anda?'
    : showArsitek.value
      ? 'Bangunan apa yang biasa Anda rancang?'
      : 'Proyek interior apa yang biasa Anda kerjakan?'
)
const lead = computed(() =>
  both.value
    ? 'Pilih keahlian arsitektur dan interior Anda. Anda boleh memilih lebih dari satu. Kami pakai pilihan ini untuk mencocokkan Anda dengan brief proyek.'
    : showArsitek.value
      ? 'Pilih sesuai klasifikasi IAI. Anda boleh memilih lebih dari satu. Kami pakai pilihan ini untuk mencocokkan Anda dengan brief proyek.'
      : 'Anda boleh memilih lebih dari satu. Kami pakai pilihan ini untuk mencocokkan Anda dengan brief proyek.'
)

const typeCls =
  'flex gap-3 items-start p-4 rounded-[14px] border bg-white text-left cursor-pointer transition-colors box-border'
</script>
