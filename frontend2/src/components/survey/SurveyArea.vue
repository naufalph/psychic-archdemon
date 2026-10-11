<template>
  <div class="flex flex-col gap-7 max-w-[680px] animate-brief-rise">
    <div class="grid grid-cols-[repeat(auto-fit,minmax(240px,1fr))] gap-6">
      <div v-if="!survey.interiorOnly.value">
        <label :for="ids.land" :class="labelCls">Luas tanah</label>
        <div class="relative">
          <input
            :id="ids.land"
            v-model="state.land"
            type="number"
            min="0"
            placeholder="cth. 200"
            :class="bigInputCls"
          />
          <span :class="unitCls">m²</span>
        </div>
      </div>

      <div>
        <label :for="ids.building" :class="labelCls">
          <template v-if="survey.interiorOnly.value">
            Luas area yang didesain <span class="font-normal text-ink-400">(opsional)</span>
          </template>
          <template v-else>Luas area proyek</template>
        </label>
        <div class="relative">
          <input
            :id="ids.building"
            v-model="state.building"
            type="number"
            min="0"
            placeholder="cth. 320"
            :class="bigInputCls"
          />
          <span :class="unitCls">m²</span>
        </div>
        <p class="text-caption leading-normal text-ink-400 mt-2 mb-0">
          {{
            survey.interiorOnly.value
              ? 'Perkiraan luas ruangan yang akan didesain.'
              : 'Total luas lantai yang akan didesain, termasuk semua lantai.'
          }}
        </p>
      </div>

      <div v-if="survey.pieceMode.value">
        <label :for="ids.wall" :class="labelCls">
          Panjang dinding <span class="font-normal text-ink-400">(opsional)</span>
        </label>
        <div class="relative">
          <input
            :id="ids.wall"
            v-model="state.wall"
            type="number"
            min="0"
            step="0.1"
            placeholder="cth. 3,2"
            :class="bigInputCls"
          />
          <span :class="unitCls">m</span>
        </div>
        <p class="text-caption leading-normal text-ink-400 mt-2 mb-0">
          Dinding tempat furnitur akan dipasang. Perkiraan sudah cukup.
        </p>
      </div>
    </div>

    <div v-if="survey.isApartment.value">
      <div :class="labelCls">Tipe unit apartemen</div>
      <div class="grid grid-cols-[repeat(auto-fill,minmax(150px,1fr))] gap-2.5">
        <SurveyChip
          v-for="type in APARTMENT_TYPES"
          :key="type"
          :on="state.aptType === type"
          class="!min-h-[48px] !rounded-[14px]"
          @click="state.aptType = type"
        >
          {{ type }}
        </SurveyChip>
      </div>
    </div>

    <div v-if="!survey.interiorOnly.value">
      <label :for="ids.floors" :class="labelCls">Jumlah lantai</label>
      <div class="flex items-center gap-4 flex-wrap">
        <div class="relative w-[200px]">
          <input
            :id="ids.floors"
            v-model="state.floors"
            type="number"
            min="1"
            placeholder="cth. 2"
            :disabled="state.floorsUnknown"
            :class="[bigInputCls, '!pr-[72px] disabled:bg-surface-alt disabled:cursor-not-allowed']"
          />
          <span :class="unitCls">lantai</span>
        </div>
        <label class="flex items-center gap-2.5 text-[15px] font-medium text-ink-900 cursor-pointer select-none">
          <input v-model="state.floorsUnknown" type="checkbox" class="sr-only" @change="state.floors = ''" />
          <SurveyCheck :on="state.floorsUnknown" size="sm" />
          Belum tahu
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useId } from 'vue'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { APARTMENT_TYPES } from '@/constants/projectSurvey'
import SurveyChip from './SurveyChip.vue'
import SurveyCheck from './SurveyCheck.vue'

const survey = useProjectSurvey()
const state = survey.state

const ids = { land: useId(), building: useId(), wall: useId(), floors: useId() }
const labelCls = 'block text-body font-medium text-ink-700 mb-2.5'
const bigInputCls =
  'w-full box-border h-14 pl-4 pr-14 border border-hairline rounded-xl bg-white text-[18px] font-semibold text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors'
const unitCls = 'absolute right-4 top-[17px] text-body text-ink-400 pointer-events-none'
</script>
