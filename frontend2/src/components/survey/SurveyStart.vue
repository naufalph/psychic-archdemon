<template>
  <div
    class="max-w-[680px] flex flex-col gap-3 animate-brief-rise"
    role="radiogroup"
    aria-label="Perkiraan mulai desain"
  >
    <div
      v-for="opt in START_OPTIONS"
      :key="opt.value"
      role="radio"
      tabindex="0"
      :aria-checked="state.startType === opt.value"
      class="flex items-center gap-3.5 min-h-[64px] px-[22px] border rounded-2xl cursor-pointer transition-all hover:border-ink-900"
      :class="state.startType === opt.value ? 'bg-surface-alt border-ink-900' : 'bg-white border-hairline'"
      @click="state.startType = opt.value"
      @keydown.enter.prevent="state.startType = opt.value"
      @keydown.space.prevent="state.startType = opt.value"
    >
      <span
        class="flex-none w-5 h-5 box-border rounded-full bg-white"
        :class="state.startType === opt.value ? 'border-[6px] border-ink-900' : 'border border-hairline'"
      ></span>
      <span class="text-[17px] font-semibold text-ink-900">{{ opt.label }}</span>
    </div>
    <input
      v-if="state.startType === 'SPECIFIC_DATE'"
      v-model="state.startDate"
      type="date"
      :min="today"
      aria-label="Tanggal mulai desain"
      class="max-w-[340px] box-border h-[52px] px-4 mt-1 border border-hairline rounded-xl bg-white text-body text-ink-900 outline-none focus:border-ink-900"
    />
  </div>
</template>

<script setup>
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { START_OPTIONS } from '@/constants/projectSurvey'

const { state } = useProjectSurvey()

const today = new Date().toISOString().slice(0, 10)
</script>
