<template>
  <div class="bg-white border border-border-gray rounded-xl p-5">
    <div class="flex items-baseline justify-between gap-3 mb-5">
      <div class="min-w-0">
        <p class="text-xs font-bold uppercase tracking-wider text-gray-400">
          {{ t.projectWorkspace?.progressByPhaseTitle }}
        </p>
        <p v-if="targetFinish" class="text-sm text-gray-600 mt-0.5">
          {{ (t.projectWorkspace?.targetFinishLabel || 'Target finish {date}').replace('{date}', targetFinish) }}
        </p>
      </div>
      <div class="text-right shrink-0">
        <p class="text-xs font-semibold text-gray-900">{{ formatAmount(totalAmount) }}</p>
        <p class="text-xs text-gray-500">
          {{ disbursedCount }} {{ t.projectWorkspace?.of }} {{ sortedPhases.length }}
          {{ t.projectWorkspace?.phasesDone }} &middot; {{ Math.round(progressPercent) }}%
        </p>
      </div>
    </div>

    <div v-if="bars.length">
      <!-- Phase names sit above the line so the space beneath it belongs to the dates. -->
      <div class="flex">
        <div v-for="bar in bars" :key="`label-${bar.id}`" class="flex-1 min-w-0 pr-2">
          <p class="text-sm font-semibold text-gray-900 truncate">{{ bar.title }}</p>
          <p class="text-xs text-gray-500 truncate">{{ formatAmount(bar.amount) }}</p>
          <p class="text-xs truncate" :class="bar.stateText">{{ bar.statusLabel }}</p>
        </div>
      </div>

      <!-- One track: each node opens its phase, the segment after it carries that phase's colour
           to the next node, and every date hangs off the same line. -->
      <div ref="railEl" class="relative mt-2.5" :style="{ height: `${marksHeight}px` }">
        <div class="flex">
          <div v-for="bar in bars" :key="bar.id" class="flex-1 min-w-0 relative pr-1">
            <div class="absolute left-2 right-0 top-[7px] h-[3px] rounded-full" :class="bar.segment" />
            <span
              class="relative block w-4 h-4 rounded-full border-2 border-white ring-1"
              :class="[bar.node, bar.ring]"
            />
          </div>
        </div>

        <!-- Each mark points back at the spot on the line its date falls on, inside its own
             phase's span, so it can never drift under a phase it does not belong to. -->
        <div
          v-for="mark in packedMarks"
          :key="mark.key"
          class="absolute top-4 w-28 -translate-x-1/2 flex flex-col items-center"
          :style="{ left: `${mark.offset}%` }"
        >
          <span class="w-0 h-0 border-x-[5px] border-x-transparent border-b-[6px]" :class="mark.caretClass" />
          <span class="w-px" :class="mark.tick" :style="{ height: `${mark.stem}px` }" />
          <p class="text-[11px] font-semibold mt-1" :class="mark.dateClass">{{ mark.dateText }}</p>
          <p
            v-for="(line, i) in mark.labels"
            :key="i"
            class="text-xs text-center leading-tight"
            :class="line.overdue ? 'text-red-600' : 'text-gray-500'"
          >
            {{ line.text }}
          </p>
        </div>
      </div>
    </div>

    <div v-else class="border-2 border-dashed border-border-gray rounded-lg py-6 text-center text-sm text-gray-400">
      {{ t.projectWorkspace?.noPhasesYet }}
    </div>

    <p v-if="bars.length && !packedMarks.length" class="text-sm text-gray-400 mt-2">
      {{ t.projectWorkspace?.noKeyDatesYet }}
    </p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  t: { type: Object, required: true },
  isClient: { type: Boolean, default: true },
  sortedPhases: { type: Array, default: () => [] },
  totalAmount: { type: Number, default: 0 },
  progressPercent: { type: Number, default: 0 },
  disbursedCount: { type: Number, default: 0 },
  statusKey: { type: Function, required: true },
  formatAmount: { type: Function, required: true },
  formatDate: { type: Function, required: true }
})

// Which milestone gives a shared mark its colour when several land on one day.
const KIND_RANK = { due: 0, started: 1, delivered: 2, approved: 3 }

const PAID_STATUSES = ['IN_PROGRESS', 'DELIVERED', 'DISPUTED', 'APPROVED', 'DISBURSED']

/**
 * How far a payout has travelled is the architect's business, not the client's -- the same rule
 * PaymentProgress applies to the aggregate bar, applied here per phase so the split cannot leak
 * back to the client through the chart.
 */
const moneyState = phase => {
  if (!PAID_STATUSES.includes(phase.status)) return 'unfunded'
  if (props.isClient) return 'paid'
  return phase.status === 'DISBURSED' ? 'disbursed' : 'escrow'
}

const stateStyles = {
  disbursed: { node: 'bg-green-500', ring: 'ring-green-500', segment: 'bg-green-500', text: 'text-green-600' },
  paid: { node: 'bg-green-500', ring: 'ring-green-500', segment: 'bg-green-500', text: 'text-green-600' },
  escrow: { node: 'bg-brand-gold', ring: 'ring-brand-gold', segment: 'bg-brand-gold', text: 'text-brand-brown' },
  unfunded: { node: 'bg-gray-200', ring: 'ring-gray-200', segment: 'bg-gray-100', text: 'text-gray-400' }
}

const bars = computed(() =>
  props.sortedPhases.map((phase, index) => {
    const style = stateStyles[moneyState(phase)]
    return {
      id: phase.id,
      title: (props.t.projectWorkspace?.phaseFallback || 'Phase {n}').replace('{n}', phase.phaseNumber),
      amount: Number(phase.amount || 0),
      node: style.node,
      ring: style.ring,
      segment: style.segment,
      stateText: style.text,
      // Where the work stands, not where the money is -- an approved phase reads as approved even
      // though its payout has not left escrow yet. The colour still carries the money.
      statusLabel: (props.t.projectWorkspace?.statusLabels || {})[props.statusKey(phase, index)]
    }
  })
)

// The project is done when its last phase is, so that phase's deadline is the project's.
const targetFinish = computed(() => {
  const last = [...props.sortedPhases].reverse().find(p => p.dueDate)
  return last ? props.formatDate(last.dueDate) : null
})

const startOfToday = () => {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  return d
}

/**
 * Where a date sits on the rail. The phases are evenly spaced, so a date is placed inside its own
 * phase's slot, proportionally between that phase's start and its deadline -- a mark can never
 * drift under a phase it did not happen in. With no span to interpolate across, a deadline pins to
 * the right edge of the slot and everything else to the left.
 */
const offsetFor = (phase, index, at) => {
  const slot = 100 / Math.max(1, props.sortedPhases.length)
  const start = phase.startedAt ? new Date(phase.startedAt).getTime() : null
  const due = phase.dueDate ? new Date(phase.dueDate).getTime() : null
  let fraction = 0
  if (start != null && due != null && due > start) {
    fraction = Math.min(1, Math.max(0, (at.getTime() - start) / (due - start)))
  } else if (due != null && at.getTime() === due) {
    fraction = 1
  }
  return (index + fraction) * slot
}

const marks = computed(() => {
  const w = props.t.projectWorkspace || {}
  const rows = []

  props.sortedPhases.forEach((phase, index) => {
    const push = (suffix, at, kind, overdue) =>
      rows.push({
        phaseId: phase.id,
        at,
        offset: offsetFor(phase, index, at),
        label: (w[`eventPhase${suffix}`] || '{n}').replace('{n}', phase.phaseNumber),
        kind,
        overdue
      })

    if (phase.startedAt) push('Started', new Date(phase.startedAt), 'started')
    if (phase.deliveredAt) push('Delivered', new Date(phase.deliveredAt), 'delivered')
    if (phase.approvedAt) push('Approved', new Date(phase.approvedAt), 'approved')
    if (phase.dueDate) {
      const at = new Date(phase.dueDate)
      push('Estimated', at, 'due', !phase.deliveredAt && at < startOfToday())
    }
  })

  rows.sort((a, b) => a.at - b.at)
  if (!rows.length) return []

  // Milestones of one phase on one day share a mark: nudging them apart would put them at dates
  // that never happened, and stacking their labels keeps the rail honest.
  const groups = []
  rows.forEach(row => {
    const id = `${row.phaseId}-${row.at.toDateString()}`
    const group = groups.find(g => g.id === id)
    if (group) {
      group.labels.push({ text: row.label, overdue: row.overdue })
      group.overdue = group.overdue || row.overdue
      // A mark showing several things takes the colour of the most conclusive one.
      if (KIND_RANK[row.kind] > KIND_RANK[group.kind]) group.kind = row.kind
    } else {
      groups.push({
        id,
        at: row.at,
        offset: row.offset,
        kind: row.kind,
        overdue: row.overdue,
        labels: [{ text: row.label, overdue: row.overdue }]
      })
    }
  })

  return groups.map(group => ({
    ...group,
    key: group.id,
    // Keep the end marks' labels inside the card rather than half-clipped by its padding.
    offset: Math.min(94, Math.max(6, group.offset)),
    caretClass: group.overdue
      ? 'border-b-red-400'
      : group.kind === 'due'
        ? 'border-b-gray-300'
        : group.kind === 'approved'
          ? 'border-b-green-600'
          : group.kind === 'delivered'
            ? 'border-b-green-500'
            : 'border-b-sky-500',
    tick: group.overdue ? 'bg-red-300' : 'bg-border-gray',
    dateClass: group.overdue ? 'text-red-600' : 'text-gray-900',
    dateText: props.formatDate(group.at)
  }))
})

/**
 * A label wraps inside its fixed-width column, so its height is estimated from wrapped lines.
 */
const CHARS_PER_LINE = 17
const LINE_HEIGHT = 15
const DATE_HEIGHT = 20
const ROW_GAP = 10
const STEM_BASE = 8
const LABEL_WIDTH_PX = 112

const labelLines = mark => mark.labels.reduce((sum, l) => sum + Math.ceil(l.text.length / CHARS_PER_LINE), 0)
const blockHeight = mark => DATE_HEIGHT + labelLines(mark) * LINE_HEIGHT

// The rail's pixel width is what decides whether two marks collide, and it changes with the
// viewport, so it is measured rather than assumed.
const railEl = ref(null)
const railWidth = ref(0)
let observer = null
onMounted(() => {
  if (!railEl.value || typeof ResizeObserver === 'undefined') return
  observer = new ResizeObserver(([entry]) => (railWidth.value = entry.contentRect.width))
  observer.observe(railEl.value)
})
onBeforeUnmount(() => observer?.disconnect())

/**
 * Marks are packed into rows: each one drops to the first row where its label will not touch the
 * label already sitting there. Two milestones three days apart in a fortnight-long phase are ~2%
 * apart on the rail, which no fixed stagger can clear -- so the rows are computed, not alternated.
 */
const packedMarks = computed(() => {
  const widthPct = railWidth.value > 0 ? (LABEL_WIDTH_PX / railWidth.value) * 100 : 15
  const lastInRow = []
  const rowHeights = []

  const placed = marks.value.map(mark => {
    let row = 0
    while (lastInRow[row] != null && mark.offset - lastInRow[row] < widthPct) row++
    lastInRow[row] = mark.offset
    rowHeights[row] = Math.max(rowHeights[row] || 0, blockHeight(mark))
    return { ...mark, row }
  })

  const rowTop = row => rowHeights.slice(0, row).reduce((sum, h) => sum + h + ROW_GAP, STEM_BASE)
  return placed.map(mark => ({ ...mark, stem: rowTop(mark.row), rowHeight: rowHeights[mark.row] }))
})

const marksHeight = computed(() => packedMarks.value.reduce((max, m) => Math.max(max, m.stem + m.rowHeight + 24), 24))
</script>
