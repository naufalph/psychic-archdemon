<template>
  <div class="animate-brief-rise">
    <div
      v-if="survey.interiorOnly.value"
      class="flex gap-3 items-start px-[18px] py-4 rounded-[14px] bg-surface-alt mb-5"
    >
      <Info :size="18" :stroke-width="2" class="flex-none mt-0.5 text-ink-900" />
      <p class="text-caption leading-relaxed text-ink-700 m-0">
        Daftar ini disesuaikan untuk desain interior {{ survey.areaNames.value }}. Dokumen seperti analisis tapak dan
        desain struktur tidak diperlukan.
      </p>
    </div>

    <div class="flex items-center justify-between gap-3 flex-wrap mb-4">
      <div class="flex items-center gap-4 flex-wrap">
        <span class="text-[15px] font-semibold text-ink-900">
          {{ survey.selectedDocs.value.length }} dari {{ survey.allDocItems.value.length }} dokumen dipilih
        </span>
        <button
          type="button"
          class="h-9 px-4 border border-ink-900 rounded-full text-caption-sm font-semibold transition-all"
          :class="allOn ? 'bg-ink-900 text-white' : 'bg-white text-ink-900'"
          @click="setAll(!allOn)"
        >
          {{ allOn ? 'Batalkan semua' : 'Pilih semua' }}
        </button>
      </div>
      <button
        type="button"
        class="h-9 px-4 border rounded-full text-caption-sm font-semibold text-ink-900 transition-all hover:border-ink-900"
        :class="advanced ? 'border-ink-900 bg-surface-alt' : 'border-hairline bg-white'"
        @click="toggleAdvanced"
      >
        {{ advanced ? 'Tutup pengaturan lanjutan' : 'Pengaturan lanjutan' }}
      </button>
    </div>

    <div class="flex flex-col gap-3">
      <div
        v-for="(group, gi) in survey.documentGroups.value"
        :key="group.title"
        class="border rounded-2xl overflow-hidden transition-all"
        :class="groupAllOn(group) ? 'bg-surface-alt border-ink-900' : 'bg-white border-hairline'"
      >
        <div
          role="checkbox"
          tabindex="0"
          :aria-checked="groupAllOn(group)"
          class="flex items-center gap-4 px-5 py-[18px] cursor-pointer"
          @click="setGroup(group, !groupAllOn(group))"
          @keydown.enter.prevent="setGroup(group, !groupAllOn(group))"
          @keydown.space.prevent="setGroup(group, !groupAllOn(group))"
        >
          <SurveyCheck :on="groupAllOn(group)" size="sm" />
          <div class="flex-1 min-w-0 flex flex-col gap-1">
            <span class="text-[15px] font-bold tracking-[-.015em] text-ink-900">{{ group.title }}</span>
            <span class="text-caption leading-normal text-ink-400">{{ preview(group) }}</span>
          </div>
          <span class="flex-none text-caption-sm font-semibold text-ink-400 whitespace-nowrap">
            {{ groupCount(group) }} dari {{ group.items.length }} dokumen
          </span>
          <button
            v-if="advanced"
            type="button"
            :title="collapsed[gi] ? 'Tampilkan' : 'Sembunyikan'"
            :aria-label="collapsed[gi] ? 'Tampilkan' : 'Sembunyikan'"
            class="flex-none w-8 h-8 rounded-full border border-hairline bg-white flex items-center justify-center hover:border-ink-900"
            @click.stop="collapsed[gi] = !collapsed[gi]"
          >
            <ChevronDown
              :size="16"
              :stroke-width="2.2"
              class="text-ink-900 transition-transform"
              :class="collapsed[gi] ? '-rotate-90' : ''"
            />
          </button>
        </div>

        <div v-if="advanced && !collapsed[gi]" class="border-t border-hairline px-5 py-4 flex flex-col gap-2">
          <div
            v-for="item in group.items"
            :key="item.code"
            role="checkbox"
            tabindex="0"
            :aria-checked="isOn(item)"
            class="flex items-start gap-3 px-3.5 py-3 border rounded-xl cursor-pointer transition-all hover:border-ink-900"
            :class="isOn(item) ? 'border-ink-900 bg-surface-alt' : 'border-hairline bg-white'"
            @click="toggle(item)"
            @keydown.enter.prevent="toggle(item)"
            @keydown.space.prevent="toggle(item)"
          >
            <SurveyCheck :on="isOn(item)" size="sm" class="mt-px" />
            <span class="text-[15px] font-medium leading-normal text-ink-900">{{ item.label }}</span>
            <button
              type="button"
              :aria-label="`Penjelasan ${item.label}`"
              class="flex-none mt-px ml-auto w-5 h-5 rounded-full border border-hairline text-ink-400 flex items-center justify-center text-micro font-bold italic font-serif hover:border-ink-900 hover:text-ink-900"
              @mouseenter="openTip($event, group, item)"
              @mouseleave="tip = null"
              @focus="openTip($event, group, item)"
              @blur="tip = null"
              @click.stop="toggleTip($event, group, item)"
            >
              i
            </button>
          </div>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div
        v-if="tip"
        role="tooltip"
        class="fixed z-[60] w-[340px] max-w-[calc(100vw-32px)] bg-white border border-hairline rounded-2xl shadow-[0_24px_60px_-30px_rgba(0,0,0,.4)] p-5 pointer-events-none"
        :style="{ top: tip.top + 'px', left: tip.left + 'px' }"
      >
        <div class="text-caption-sm font-semibold uppercase tracking-[.06em] text-accent-blue mb-2">
          {{ tip.group }}
        </div>
        <div class="text-[17px] font-bold tracking-[-.02em] leading-[1.3] text-ink-900 mb-2.5">{{ tip.title }}</div>
        <div class="flex flex-col gap-2.5">
          <p v-for="para in tip.paras" :key="para" class="m-0 text-caption leading-relaxed text-ink-400">{{ para }}</p>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ChevronDown, Info } from 'lucide-vue-next'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import SurveyCheck from './SurveyCheck.vue'

const survey = useProjectSurvey()
const state = survey.state

const advanced = ref(false)
const collapsed = reactive({})
const tip = ref(null)

const isOn = item => !!state.docs[survey.docKey(item)]
const groupCount = group => group.items.filter(isOn).length
const groupAllOn = group => groupCount(group) === group.items.length
const allOn = computed(() => survey.selectedDocs.value.length === survey.allDocItems.value.length)

const preview = group =>
  group.items
    .slice(0, 3)
    .map(item => item.label)
    .join(', ') + (group.items.length > 3 ? ', dll.' : '')

const toggle = item => {
  const key = survey.docKey(item)
  state.docs = { ...state.docs, [key]: !state.docs[key] }
}

const setItems = (items, on) => {
  const next = { ...state.docs }
  items.forEach(item => {
    next[survey.docKey(item)] = on
  })
  state.docs = next
}
const setGroup = (group, on) => setItems(group.items, on)
const setAll = on => setItems(survey.allDocItems.value, on)

const toggleAdvanced = () => {
  advanced.value = !advanced.value
  tip.value = null
}

const TIP_WIDTH = 340
const MARGIN = 16

const openTip = (event, group, item) => {
  const rect = event.currentTarget.getBoundingClientRect()
  let left = rect.right + 14
  if (left + TIP_WIDTH + MARGIN > window.innerWidth) left = rect.left - TIP_WIDTH - 14
  if (left < MARGIN) left = MARGIN
  let top = rect.top - 12
  if (top + 240 > window.innerHeight) top = Math.max(MARGIN, window.innerHeight - 260)
  tip.value = { group: group.title, title: item.label, paras: item.def, top: Math.round(top), left: Math.round(left) }
}

// Touch screens have no hover, so a tap on the "i" toggles the same tooltip; with a mouse the
// hover already opened it and the click must not close it again
const toggleTip = (event, group, item) => {
  if (window.matchMedia('(hover: hover)').matches) return
  if (tip.value?.title === item.label) tip.value = null
  else openTip(event, group, item)
}
</script>
