<template>
  <div class="flex flex-col gap-3 max-w-[680px] animate-brief-rise">
    <div
      v-for="scope in SURVEY_SCOPES"
      :key="scope.value"
      class="rounded-2xl border transition-all hover:border-ink-900"
      :class="state.scopes[scope.value] ? 'bg-surface-alt border-ink-900' : 'bg-white border-hairline'"
    >
      <div
        role="checkbox"
        tabindex="0"
        :aria-checked="!!state.scopes[scope.value]"
        class="flex gap-4 items-start px-[22px] py-5 cursor-pointer"
        @click="toggleScope(scope.value)"
        @keydown.enter.prevent="toggleScope(scope.value)"
        @keydown.space.prevent="toggleScope(scope.value)"
      >
        <SurveyCheck :on="!!state.scopes[scope.value]" class="mt-0.5" />
        <div class="flex flex-col gap-1 min-w-0">
          <span class="text-[17px] font-bold tracking-[-.02em] text-ink-900">{{ scope.label }}</span>
          <span class="text-[15px] leading-normal text-ink-400">{{ scope.desc }}</span>
        </div>
      </div>

      <div
        v-if="scope.value === SCOPE_INTERIOR && state.scopes[SCOPE_INTERIOR]"
        class="pt-1 pb-[22px] pr-[22px] pl-[22px] sm:pl-[60px] flex flex-col gap-6"
      >
        <div>
          <div class="text-body font-semibold text-ink-900 mb-3">Bagian mana yang ingin Anda desain?</div>
          <div class="flex flex-wrap gap-2">
            <SurveyChip
              v-for="area in INTERIOR_AREAS"
              :key="area.id"
              :on="!!state.intAreas[area.id]"
              @click="toggleArea(area.id)"
            >
              {{ area.id }}
            </SurveyChip>
          </div>
          <input
            v-if="state.intAreas[INTERIOR_OTHER]"
            v-model="state.intAreaOther"
            type="text"
            placeholder="cth. Ruang kerja"
            aria-label="Area lainnya"
            :class="[inputCls, 'mt-2.5']"
          />
        </div>

        <div v-for="area in followUps" :key="area.id" class="animate-brief-rise">
          <div class="text-body font-semibold text-ink-900 mb-3">
            Apa yang ingin Anda desain di area {{ area.area }}?
          </div>
          <div class="flex flex-wrap gap-2">
            <SurveyChip
              v-for="opt in area.opts"
              :key="opt"
              :on="state.intScope[area.id] === opt"
              @click="state.intScope[area.id] = opt"
            >
              {{ opt }}
            </SurveyChip>
          </div>
          <input
            v-if="state.intScope[area.id] === 'Lainnya'"
            v-model="state.intScopeOther[area.id]"
            type="text"
            placeholder="cth. Meja makan dan rak display"
            :aria-label="`Desain lainnya di area ${area.area}`"
            :class="[inputCls, 'mt-2.5']"
          />
        </div>

        <div
          v-if="survey.selectedAreas.value.length"
          class="flex gap-3 items-start px-4 py-3.5 rounded-xl bg-white border border-hairline"
        >
          <Info :size="18" :stroke-width="2" class="flex-none mt-0.5 text-ink-900" />
          <p class="text-caption leading-relaxed text-ink-700 m-0">
            Permintaan ini untuk jasa desain. Pembuatan dan pemasangan, misalnya produksi kitchen set atau lemari, belum
            tersedia di rumantra.
          </p>
        </div>
      </div>
    </div>

    <div
      class="rounded-2xl border transition-all hover:border-ink-900"
      :class="state.scopeOtherOn ? 'bg-surface-alt border-ink-900' : 'bg-white border-hairline'"
    >
      <div
        role="checkbox"
        tabindex="0"
        :aria-checked="state.scopeOtherOn"
        class="flex gap-4 items-start px-[22px] py-5 cursor-pointer"
        @click="state.scopeOtherOn = !state.scopeOtherOn"
        @keydown.enter.prevent="state.scopeOtherOn = !state.scopeOtherOn"
        @keydown.space.prevent="state.scopeOtherOn = !state.scopeOtherOn"
      >
        <SurveyCheck :on="state.scopeOtherOn" class="mt-0.5" />
        <span class="text-[17px] font-bold tracking-[-.02em] text-ink-900">Lainnya</span>
      </div>
      <div v-if="state.scopeOtherOn" class="pb-5 pr-[22px] pl-[22px] sm:pl-[60px]">
        <input
          v-model="state.scopeOther"
          type="text"
          placeholder="cth. Alih fungsi gudang menjadi kafe"
          aria-label="Tujuan proyek lainnya"
          :class="inputCls"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Info } from 'lucide-vue-next'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import {
  SURVEY_SCOPES,
  SCOPE_INTERIOR,
  INTERIOR_AREAS,
  INTERIOR_WHOLE,
  INTERIOR_OTHER
} from '@/constants/projectSurvey'
import SurveyCheck from './SurveyCheck.vue'
import SurveyChip from './SurveyChip.vue'

const survey = useProjectSurvey()
const state = survey.state

const inputCls =
  'w-full box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors'

const followUps = computed(() => survey.selectedAreas.value.filter(area => area.opts))

const toggleScope = value => {
  state.scopes[value] = !state.scopes[value]
}

// "Seluruh interior" covers every room, so it excludes the individual areas and vice versa
const toggleArea = id => {
  const nowOn = !state.intAreas[id]
  if (id === INTERIOR_WHOLE) {
    state.intAreas = nowOn ? { [INTERIOR_WHOLE]: true } : {}
    return
  }
  state.intAreas = { ...state.intAreas, [INTERIOR_WHOLE]: false, [id]: nowOn }
}
</script>
