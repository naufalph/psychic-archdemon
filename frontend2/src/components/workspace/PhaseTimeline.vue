<template>
  <div id="phase-timeline" class="bg-white border border-border-gray rounded-xl p-5 flex flex-col gap-4 scroll-mt-24">
    <div class="flex items-baseline justify-between gap-3">
      <div class="min-w-0">
        <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
          {{ w.progressByPhaseTitle }}
        </p>
        <p v-if="rows.length" class="text-sm text-gray-600 mt-0.5">
          {{
            (schedule.finished
              ? w.timelineFinishedLine || 'Target finish {target} · Finished {forecast}'
              : w.timelineFinishLine || 'Target finish {target} · Forecast {forecast}'
            )
              .replace('{target}', formatDate(schedule.targetFinish))
              .replace('{forecast}', formatDate(schedule.forecastFinish))
          }}
        </p>
      </div>
      <div class="text-right shrink-0">
        <p class="text-xs font-semibold text-gray-900">{{ formatAmount(totalAmount) }}</p>
        <p class="text-xs text-gray-500">
          {{ disbursedCount }} {{ w.of }} {{ sortedPhases.length }} {{ w.phasesDone }} &middot;
          {{ Math.round(progressPercent) }}%
        </p>
      </div>
    </div>

    <template v-if="rows.length">
      <div class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg" :class="ack.bg">
        <span class="w-7 h-7 rounded-full bg-white flex items-center justify-center shrink-0" :class="ack.text">
          <Clock class="w-4 h-4" />
        </span>
        <div class="min-w-0">
          <p class="text-sm font-semibold" :class="ack.text">{{ ack.title }}</p>
          <p class="text-xs text-gray-600">{{ ack.sub }}</p>
        </div>
      </div>

      <div class="grid grid-cols-[56px_minmax(0,1fr)] gap-x-3">
        <div class="relative h-[172px]">
          <p class="absolute top-[53px] text-[11px] font-bold tracking-[0.08em] text-gray-400">
            {{ w.timelineTargetLane || 'TARGET' }}
          </p>
          <p class="absolute top-[105px] text-[11px] font-bold tracking-[0.08em] text-gray-900">
            {{ w.timelineActualLane || 'ACTUAL' }}
          </p>
        </div>

        <div ref="laneEl" class="relative h-[172px]" @mouseleave="hover = null">
          <template v-for="r in rows" :key="`band-${r.id}`">
            <span
              class="absolute -top-1.5 h-[78px] rounded-[10px] bg-gray-50 border border-[#E8E8E8] transition-opacity duration-200 ease-in-out"
              :class="hover === r.index ? 'opacity-100' : 'opacity-0'"
              :style="{ left: r.tBandLeft, width: r.tBandWidth }"
            />
            <span
              class="absolute top-[94px] h-14 rounded-[10px] bg-gray-50 border border-[#E8E8E8] transition-opacity duration-200 ease-in-out"
              :class="hover === r.index ? 'opacity-100' : 'opacity-0'"
              :style="{ left: r.aBandLeft, width: r.aBandWidth }"
            />
          </template>

          <template v-for="m in months" :key="`month-${m.key}`">
            <span class="absolute top-11 bottom-6 w-px bg-gray-100" :style="{ left: m.left }" />
            <p class="absolute bottom-0 pl-1 text-[11px] font-semibold text-gray-300" :style="{ left: m.left }">
              {{ m.label }}
            </p>
          </template>

          <span class="absolute top-11 bottom-6 w-px bg-[#0A0A0A] opacity-30" :style="{ left: todayLeft }" />
          <p
            class="absolute bottom-0 -translate-x-1/2 px-1 bg-white text-[11px] font-bold text-[#0A0A0A]"
            :style="{ left: todayLeft }"
          >
            {{ w.timelineToday || 'Today' }}
          </p>

          <template v-for="r in rows" :key="`row-${r.id}`">
            <div
              class="absolute top-0 pl-1.5 pr-2 min-w-0 transition-opacity duration-200 ease-in-out"
              :style="{ left: r.tLeft, width: r.tWidth, opacity: r.dim }"
            >
              <p class="text-[13px] leading-[18px] font-semibold text-gray-900 truncate">
                {{ r.heading }}
              </p>
              <p class="text-[11px] leading-4 text-gray-400 truncate">{{ r.targetShort }}</p>
            </div>
            <span
              class="absolute top-[58px] h-[3px] rounded-full bg-gray-200"
              :style="{ left: r.tLeft, width: r.tWidth, opacity: r.dim }"
            />
            <span
              class="absolute top-[53px] w-3 h-3 -ml-1.5 rounded-full bg-white border-2 border-gray-300"
              :style="{ left: r.tLeft, opacity: r.dim }"
            />
            <span
              class="absolute top-[66px] h-5 w-0 border-l border-dashed border-gray-300"
              :style="{ left: r.link.x1, opacity: r.dim }"
            />
            <span
              class="absolute top-[86px] h-0 border-t border-dashed border-gray-300"
              :style="{ left: r.link.lo, width: r.link.w, opacity: r.dim }"
            />
            <span
              class="absolute top-[86px] h-4 w-0 border-l border-dashed border-gray-300"
              :style="{ left: r.link.x2, opacity: r.dim }"
            />
            <span
              v-for="(s, si) in r.segs"
              :key="si"
              class="absolute top-[108px] h-1 rounded-full"
              :class="s.cls"
              :style="{ left: s.left, width: s.width, opacity: r.dim, background: s.background }"
            />
            <span
              class="absolute top-[102px] w-4 h-4 -ml-2 rounded-full border-2 border-white"
              :class="r.node"
              :style="{ left: r.aLeft, opacity: r.dim }"
            />
            <span
              v-if="r.chipText"
              class="absolute top-[124px] -translate-x-1/2 px-2 py-0.5 rounded-full text-[11px] font-bold whitespace-nowrap"
              :class="r.chipClass"
              :style="{ left: r.aMid, opacity: r.dim }"
            >
              {{ r.chipText }}
            </span>
            <button
              type="button"
              class="absolute -top-1.5 h-[78px] rounded-[10px] bg-transparent cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-gold"
              :aria-label="r.ariaLabel"
              :title="r.heading"
              :style="{ left: r.tBandLeft, width: r.tBandWidth }"
              @mouseenter="hover = r.index"
              @focus="hover = r.index"
              @click="open(r.id)"
            />
            <button
              type="button"
              tabindex="-1"
              class="absolute top-[94px] h-14 rounded-[10px] bg-transparent cursor-pointer"
              :aria-label="r.ariaLabel"
              :style="{ left: r.aBandLeft, width: r.aBandWidth }"
              @mouseenter="hover = r.index"
              @click="open(r.id)"
            />
          </template>

          <span
            class="absolute top-[53px] w-3 h-3 -ml-1.5 rounded-full bg-white border-2 border-gray-300"
            :style="{ left: finish.tLeft }"
          />
          <span
            class="absolute top-[66px] h-5 w-0 border-l border-dashed border-gray-300"
            :style="{ left: finish.x1 }"
          />
          <span
            class="absolute top-[86px] h-0 border-t border-dashed border-gray-300"
            :style="{ left: finish.lo, width: finish.w }"
          />
          <span
            class="absolute top-[86px] h-4 w-0 border-l border-dashed border-gray-300"
            :style="{ left: finish.x2 }"
          />
          <span
            class="absolute top-[102px] w-4 h-4 -ml-2 rounded-full bg-white border-2"
            :class="schedule.finished ? 'border-green-500' : 'border-dashed border-gray-400'"
            :style="{ left: finish.aLeft }"
          />
          <p
            class="absolute top-[102px] ml-3.5 text-[11px] leading-4 font-semibold text-gray-500 whitespace-nowrap"
            :style="{ left: finish.aLeft }"
          >
            {{ finish.label }}
          </p>
        </div>
      </div>

      <div class="flex flex-wrap items-center gap-4 text-xs text-gray-500 border-t border-gray-100 pt-3">
        <span class="flex items-center gap-1.5">
          <span class="w-2.5 h-2.5 rounded-full border-2 border-gray-300" />{{ w.legendTarget || 'Target from bid' }}
        </span>
        <span class="flex items-center gap-1.5">
          <span class="w-4 h-1 rounded-full bg-green-500" />{{ w.legendCompleted || 'Completed' }}
        </span>
        <span class="flex items-center gap-1.5">
          <span class="w-4 h-1 rounded-full bg-sky-500" />{{ w.legendInProgress || 'In progress' }}
        </span>
        <span class="flex items-center gap-1.5">
          <span class="w-4 h-1 rounded-full" :style="{ background: FORECAST }" />{{ w.legendForecast || 'Forecast' }}
        </span>
        <button
          type="button"
          class="ml-auto text-xs font-semibold text-brand-brown hover:text-[#0A0A0A]"
          @click="hover != null ? open(rows[hover].id) : $emit('go-phases')"
        >
          {{ footerCta }} &rarr;
        </button>
      </div>
    </template>

    <div v-else class="border-2 border-dashed border-border-gray rounded-lg py-6 text-center text-sm text-gray-400">
      {{ w.noPhasesYet }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Clock } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { daysBetween, chipText, chipClasses, formatDayMonth, formatRange } from './phaseSchedule'

const props = defineProps({
  t: { type: Object, required: true },
  isClient: { type: Boolean, default: true },
  sortedPhases: { type: Array, default: () => [] },
  schedule: { type: Object, required: true },
  totalAmount: { type: Number, default: 0 },
  progressPercent: { type: Number, default: 0 },
  disbursedCount: { type: Number, default: 0 },
  phaseFallbackTitle: { type: Function, required: true },
  formatAmount: { type: Function, required: true },
  formatDate: { type: Function, required: true }
})
const emit = defineEmits(['go-phase', 'go-phases'])

const { locale } = useI18n()
const w = computed(() => props.t.projectWorkspace || {})
const hover = ref(null)

const FORECAST = 'repeating-linear-gradient(90deg,#D1D5DB 0 4px,transparent 4px 7px)'
// Months closer than this to today give up their label so it does not collide with "Today".
const TODAY_LABEL_CLEARANCE_DAYS = 7
// Room for a chip under its phase. Phases that start and finish within days of each other would
// otherwise collapse onto one point, so each is drawn at least this wide on the actual lane.
const MIN_ACTUAL_PX = 120
const CHIP_PX = 104
const FINISH_LABEL_PX = 110

// Whether two things collide is decided in pixels, and the lane's width follows the viewport.
const laneEl = ref(null)
const laneWidth = ref(0)
let observer = null
onMounted(() => {
  if (!laneEl.value || typeof ResizeObserver === 'undefined') return
  observer = new ResizeObserver(([entry]) => (laneWidth.value = entry.contentRect.width))
  observer.observe(laneEl.value)
})
onBeforeUnmount(() => observer?.disconnect())
const pxToPct = px => (laneWidth.value > 0 ? (px / laneWidth.value) * 100 : 0)

/** The axis spans whole months: from the one work began in to the one after everything ends. */
const axis = computed(() => {
  const { start, today, targetFinish, forecastFinish, rows } = props.schedule
  const first = new Date(Math.min(start, today, ...rows.map(r => r.actualStart)))
  const last = new Date(Math.max(targetFinish, forecastFinish, today))
  const from = new Date(first.getFullYear(), first.getMonth(), 1)
  const to = new Date(last.getFullYear(), last.getMonth() + 1, 1)
  return { from, to, span: Math.max(1, daysBetween(from, to)) }
})

// The right edge is kept clear for the finish label, so the dates are scaled into what is left.
const usablePct = computed(() => 100 - Math.min(40, pxToPct(FINISH_LABEL_PX + 14)))
const pn = date => (daysBetween(axis.value.from, date) / axis.value.span) * usablePct.value
const pct = date => `${pn(date)}%`
const widthPct = (a, b) => `${Math.max(0, pn(b) - pn(a))}%`
const connector = (x1, x2) => ({ x1: `${x1}%`, x2: `${x2}%`, lo: `${Math.min(x1, x2)}%`, w: `${Math.abs(x2 - x1)}%` })

const months = computed(() => {
  const out = []
  const { from, to } = axis.value
  const today = props.schedule.today
  for (let d = new Date(from); d < to; d = new Date(d.getFullYear(), d.getMonth() + 1, 1)) {
    const nearToday = Math.abs(daysBetween(d, today)) < TODAY_LABEL_CLEARANCE_DAYS
    out.push({
      key: d.getTime(),
      left: pct(d),
      label: nearToday ? '' : d.toLocaleDateString(locale.value === 'id' ? 'id-ID' : 'en-US', { month: 'short' })
    })
  }
  return out
})

const todayLeft = computed(() => pct(props.schedule.today))

/**
 * Where each phase is drawn on the actual lane. Dates stay calendar-true until two phases would
 * crowd each other; then the later one is pushed right just far enough to be readable. A pushed
 * phase keeps its length, and the dates in its labels are untouched.
 */
const actualLayout = computed(() => {
  const rows = props.schedule.rows
  const usable = usablePct.value
  const minPct = Math.min(pxToPct(MIN_ACTUAL_PX), usable / (rows.length + 1))
  let cursor = -Infinity
  const placed = rows.map(r => {
    const realStart = pn(r.actualStart)
    const realEnd = pn(r.actualEnd)
    const start = Math.max(realStart, cursor)
    const end = Math.max(realEnd + (start - realStart), start + minPct)
    cursor = end
    return { realStart, realEnd, start, end }
  })
  // Pushing phases right can run them past the space kept for the finish label; pull them back in.
  const scale = cursor > usable ? usable / cursor : 1
  return placed.map(({ realStart, realEnd, start: s0, end: e0 }) => {
    const start = s0 * scale
    const end = e0 * scale
    // A date inside the phase, mapped onto the (possibly stretched) drawn span.
    const at = date => {
      if (realEnd <= realStart) return start + (end - start) / 2
      const f = Math.min(1, Math.max(0, (pn(date) - realStart) / (realEnd - realStart)))
      return start + f * (end - start)
    }
    return { start, end, at }
  })
})

const span = (from, to, extra) => ({ left: `${from}%`, width: `${Math.max(0, to - from)}%`, ...extra })

const segmentsFor = (r, { start, end, at }) => {
  if (r.state === 'done') return [span(start, end, { cls: 'bg-green-500' })]
  if (r.state === 'delivered') {
    const split = at(r.deliveredAt)
    return [span(start, split, { cls: 'bg-sky-500' }), span(split, end, { cls: 'bg-purple-400' })]
  }
  if (r.state === 'active') {
    const split = at(props.schedule.today)
    return [span(start, split, { cls: 'bg-sky-500' }), span(split, end, { background: FORECAST })]
  }
  return [span(start, end, { background: FORECAST })]
}

const nodeClasses = {
  done: 'bg-green-500 ring-1 ring-green-500',
  delivered: 'bg-sky-500 ring-1 ring-sky-500',
  active: 'bg-sky-500 ring-1 ring-sky-500',
  upcoming: 'bg-white ring-1 ring-gray-300'
}

const rows = computed(() => {
  let lastChipAt = -Infinity
  return props.sortedPhases.map((phase, index) => {
    const r = props.schedule.byPhaseId[phase.id]
    const a = actualLayout.value[index]
    const phaseLabel = (w.value.phaseFallback || 'Phase {n}').replace('{n}', phase.phaseNumber)
    const title = props.phaseFallbackTitle(phase)
    const isDimmed = hover.value != null && hover.value !== index
    const mid = (a.start + a.end) / 2
    // Squeezed into a narrow lane, a chip that would overlap the previous one is dropped.
    const showChip = !!r.chip && mid - lastChipAt >= pxToPct(CHIP_PX)
    if (showChip) lastChipAt = mid
    return {
      id: phase.id,
      index,
      phaseLabel,
      title,
      // Bids without phase names fall back to "Phase n", which would read "Phase 1 · Phase 1".
      heading: title === phaseLabel ? phaseLabel : `${phaseLabel} · ${title}`,
      targetShort: formatRange(r.targetStart, r.targetEnd, locale.value),
      tLeft: pct(r.targetStart),
      tWidth: widthPct(r.targetStart, r.targetEnd),
      aLeft: `${a.start}%`,
      aMid: `${mid}%`,
      link: connector(pn(r.targetStart), a.start),
      segs: segmentsFor(r, a),
      node: nodeClasses[r.state],
      chipText: showChip ? chipText(r.chip, w.value) : '',
      chipClass: r.chip ? chipClasses[r.chip.kind] : '',
      // Hover bands overhang the bars a little so a short phase is still a comfortable target.
      tBandLeft: `${pn(r.targetStart) - 1}%`,
      tBandWidth: `${pn(r.targetEnd) - pn(r.targetStart) + 2}%`,
      aBandLeft: `${a.start - 1.5}%`,
      aBandWidth: `${a.end - a.start + 3}%`,
      dim: isDimmed ? 0.3 : 1,
      ariaLabel: (w.value.timelineOpenAria || 'Open {phase} · {title} in Phases & Deliverables')
        .replace('{phase}', phaseLabel)
        .replace('{title}', title)
    }
  })
})

const finish = computed(() => {
  const { targetFinish, forecastFinish } = props.schedule
  const layout = actualLayout.value
  const aLeft = Math.max(pn(forecastFinish), layout.length ? layout[layout.length - 1].end : -Infinity)
  return {
    tLeft: pct(targetFinish),
    aLeft: `${aLeft}%`,
    ...connector(pn(targetFinish), aLeft),
    label: (w.value.timelineFinishShort || '{date} · finish').replace(
      '{date}',
      formatDayMonth(forecastFinish, locale.value)
    )
  }
})

const ack = computed(() => {
  const { workDelta, submittedCount } = props.schedule
  const role = props.isClient ? 'Client' : 'Architect'
  const tpl = (key, fallback) => w.value[`${key}${role}`] || fallback
  const days = n => (n === 1 ? w.value.dayCountOne || '{n} day' : w.value.dayCountMany || '{n} days').replace('{n}', n)
  const across = (
    submittedCount === 1
      ? w.value.ackAcrossOne || 'Across {n} submitted phase, measured from start to submission.'
      : w.value.ackAcrossMany || 'Across {n} submitted phases, measured from start to submission.'
  ).replace('{n}', submittedCount)
  const neutral = { bg: 'bg-gray-50', text: 'text-gray-900' }

  if (!submittedCount)
    return {
      ...neutral,
      title: tpl('ackOnTrack', 'Working to the bid timeline'),
      sub: w.value.ackOnTrackSub || 'Speed vs bid appears once the first phase is submitted.'
    }
  if (workDelta < 0)
    return {
      bg: 'bg-green-50',
      text: 'text-green-700',
      title: tpl('ackFaster', 'Delivered {days} faster than the bid').replace('{days}', days(-workDelta)),
      sub: across
    }
  if (workDelta === 0) return { ...neutral, title: tpl('ackOnTime', 'Right on the bid timeline'), sub: across }
  return {
    bg: 'bg-amber-50',
    text: 'text-amber-700',
    title: tpl('ackSlower', 'Delivered {days} slower than the bid').replace('{days}', days(workDelta)),
    sub: across
  }
})

const footerCta = computed(() =>
  hover.value != null
    ? (w.value.timelineOpenPhase || 'Click to open Phase {n} deliverables').replace(
        '{n}',
        props.sortedPhases[hover.value]?.phaseNumber
      )
    : w.value.timelineOpenAll || 'Phases & Deliverables'
)

const open = phaseId => {
  hover.value = null
  emit('go-phase', phaseId)
}
</script>
