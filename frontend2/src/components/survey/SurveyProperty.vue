<template>
  <div class="flex flex-col gap-8 animate-brief-rise">
    <div>
      <div :class="labelCls">Kategori</div>
      <div class="grid grid-cols-[repeat(auto-fit,minmax(160px,1fr))] gap-2.5">
        <button
          v-for="group in SURVEY_CATEGORY_GROUPS"
          :key="group.id"
          type="button"
          :aria-pressed="state.groupId === group.id"
          class="min-h-[56px] px-[18px] py-3 rounded-[14px] border text-[15px] font-semibold text-left leading-[1.3] transition-all"
          :class="
            state.groupId === group.id
              ? 'bg-ink-900 border-ink-900 text-white'
              : 'bg-white border-hairline text-ink-900 hover:border-ink-900'
          "
          @click="pickGroup(group.id)"
        >
          {{ group.label }}
        </button>
      </div>
    </div>

    <div v-if="activeGroup" class="animate-brief-rise">
      <div :class="labelCls">Sub-kategori</div>
      <div class="flex flex-wrap gap-2">
        <SurveyChip
          v-for="opt in activeGroup.options"
          :key="opt.category + opt.subCategory"
          :on="state.category === opt.category && state.subCategory === opt.subCategory"
          class="!min-h-[48px] !px-[18px] !rounded-[14px]"
          @click="pickSub(opt)"
        >
          {{ opt.label }}
        </SurveyChip>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { SURVEY_CATEGORY_GROUPS } from '@/constants/projectSurvey'
import SurveyChip from './SurveyChip.vue'

const { state } = useProjectSurvey()

const labelCls = 'text-body font-medium text-ink-700 mb-3'

const activeGroup = computed(() => SURVEY_CATEGORY_GROUPS.find(group => group.id === state.groupId) || null)

const pickGroup = id => {
  if (state.groupId === id) return
  state.groupId = id
  state.category = ''
  state.subCategory = ''
  state.aptType = ''
}

const pickSub = opt => {
  state.category = opt.category
  state.subCategory = opt.subCategory
}
</script>
