<template>
  <div>
    <div class="flex items-center justify-between mb-3">
      <span class="text-xs font-bold uppercase tracking-wider text-gray-400">
        {{ t.projectWorkspace?.deliverablesLabel }}
      </span>
      <span v-if="items.length" class="text-xs text-gray-400">
        {{
          (t.projectWorkspace?.approvedOf || '{approved} of {total} approved')
            .replace('{approved}', approvedCount)
            .replace('{total}', items.length)
        }}
      </span>
    </div>

    <div v-if="items.length" class="text-sm">
      <div
        class="grid gap-3 pb-2 border-b border-border-gray text-[10px] font-bold uppercase tracking-[0.08em] text-gray-400"
        :style="gridStyle"
      >
        <span>{{ t.projectWorkspace?.colDeliverable }}</span>
        <span>{{ t.projectWorkspace?.colStatus }}</span>
        <span>{{ t.projectWorkspace?.colAction }}</span>
        <span class="text-right">{{ t.projectWorkspace?.colFiles }}</span>
      </div>

      <div
        v-for="item in items"
        :key="item.index"
        class="grid gap-3 py-3 border-b border-gray-50 items-center"
        :style="gridStyle"
      >
        <div class="min-w-0">
          <p class="text-sm font-medium text-gray-900 truncate">{{ deliverableLabel(item.name, t) }}</p>
          <p class="text-xs text-gray-400 truncate">{{ subLine(item) }}</p>
        </div>

        <div class="flex items-center gap-1.5">
          <span class="w-1.5 h-1.5 rounded-full shrink-0" :class="styleFor(item).dot" />
          <span class="text-xs font-semibold" :class="styleFor(item).text">
            {{ statusLabel(item) }}
          </span>
        </div>

        <div>
          <button
            v-if="isClient && effectiveStatus(item) === 'PENDING'"
            class="px-3.5 py-1.5 rounded-full bg-green-600 hover:bg-green-700 text-white text-xs font-semibold disabled:opacity-50"
            :disabled="busy"
            :aria-label="`${t.projectWorkspace?.approveBtn} ${t.projectWorkspace?.colDeliverable}: ${deliverableLabel(item.name, t)}`"
            @click="$emit('approve', item)"
          >
            {{ t.projectWorkspace?.approveBtn }}
          </button>
          <button
            v-else-if="!isClient && canUpload && ['PENDING', 'MISSING'].includes(effectiveStatus(item))"
            class="px-3.5 py-1.5 rounded-full bg-white border border-gray-300 hover:border-gray-900 text-xs font-semibold"
            :aria-label="`${t.projectWorkspace?.uploadCta} ${t.projectWorkspace?.colDeliverable}: ${deliverableLabel(item.name, t)}`"
            @click="$emit('upload', item)"
          >
            {{ t.projectWorkspace?.uploadCta }}
          </button>
          <span v-else class="text-gray-300">&mdash;</span>
        </div>

        <div class="text-right">
          <button
            v-if="item.files?.length"
            class="px-3 py-1.5 rounded-full bg-gray-50 border border-border-gray hover:border-brand-gold hover:bg-white text-xs font-semibold text-gray-700"
            :aria-label="`${t.projectWorkspace?.colFiles}: ${deliverableLabel(item.name, t)}`"
            @click="$emit('open-files', item)"
          >
            {{ (t.projectWorkspace?.filesCountLabel || '{n} files').replace('{n}', item.files.length) }}
          </button>
          <span v-else class="text-xs text-gray-300">{{ t.projectWorkspace?.noFilesLabel }}</span>
        </div>
      </div>
    </div>

    <div v-else class="border-2 border-dashed border-border-gray rounded-lg py-6 text-center text-sm text-gray-400">
      {{ t.projectWorkspace?.noDeliverablesUploaded }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { deliverableStyles, deliverableLabel } from './workspaceMaps'

const props = defineProps({
  items: { type: Array, default: () => [] },
  phaseStatusKey: { type: String, required: true },
  isClient: { type: Boolean, default: true },
  busy: { type: Boolean, default: false },
  t: { type: Object, required: true },
  formatDateTime: { type: Function, required: true }
})
defineEmits(['approve', 'upload', 'open-files'])

const gridStyle = 'grid-template-columns: 2.4fr 1.2fr 1.3fr 1fr'

const approvedCount = computed(() => props.items.filter(i => i.status === 'APPROVED').length)
const canUpload = computed(() => ['IN_PROGRESS', 'DELIVERED'].includes(props.phaseStatusKey))

/**
 * LOCKED is derived, never stored: until the phase is actually being worked on, every
 * deliverable that is not already approved reads as locked rather than as missing.
 */
const effectiveStatus = item => {
  if (item.status === 'APPROVED') return 'APPROVED'
  if (['NOT_STARTED', 'PENDING', 'BILLED'].includes(props.phaseStatusKey)) return 'LOCKED'
  return item.status || 'MISSING'
}

const styleFor = item => deliverableStyles[effectiveStatus(item)] || deliverableStyles.MISSING

const statusLabel = item =>
  ({
    APPROVED: props.t.projectWorkspace?.deliverableApproved,
    PENDING: props.t.projectWorkspace?.deliverableAwaiting,
    MISSING: props.t.projectWorkspace?.deliverableMissing,
    LOCKED: props.t.projectWorkspace?.deliverableLocked
  })[effectiveStatus(item)]

const subLine = item => {
  const w = props.t.projectWorkspace || {}
  if (effectiveStatus(item) === 'LOCKED') return w.unlocksWithPhase
  if (!item.files?.length) return w.awaitingUpload
  const latest = item.files.reduce((a, f) => (!a || new Date(f.uploadedAt) > new Date(a) ? f.uploadedAt : a), null)
  return (w.filesUpdated || '{n} files · updated {at}')
    .replace('{n}', item.files.length)
    .replace('{at}', props.formatDateTime(latest))
}
</script>
