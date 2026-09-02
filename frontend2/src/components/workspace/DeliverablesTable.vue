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
        :class="{ 'bg-amber-50/60': isSelected(item) }"
        :style="gridStyle"
      >
        <div class="min-w-0">
          <p class="text-sm font-medium text-gray-900 truncate">{{ deliverableLabel(item.name, t) }}</p>
          <p class="text-xs text-gray-400 truncate">{{ subLine(item) }}</p>
          <!-- What the client asked for is only useful next to the row it applies to. -->
          <p v-if="openRevision(item)" class="text-xs text-amber-700 mt-1 flex items-start gap-1">
            <RotateCcw class="w-3 h-3 mt-0.5 shrink-0" />
            <span class="min-w-0">
              <span class="font-semibold">
                {{
                  (t.projectWorkspace?.revisionRoundLabel || 'Revision {n}').replace('{n}', openRevision(item).round)
                }}:
              </span>
              {{ openRevision(item).notes }}
            </span>
          </p>
        </div>

        <div class="flex items-center gap-1.5">
          <span class="w-1.5 h-1.5 rounded-full shrink-0" :class="styleFor(item).dot" />
          <span class="text-xs font-semibold" :class="styleFor(item).text">
            {{ statusLabel(item) }}
          </span>
        </div>

        <div class="flex flex-wrap items-center gap-1.5">
          <template v-if="isClient && effectiveStatus(item) === 'PENDING'">
            <button
              class="px-3.5 py-1.5 rounded-full bg-green-600 hover:bg-green-700 text-white text-xs font-semibold disabled:opacity-50"
              :disabled="busy || isSelected(item)"
              :aria-label="`${t.projectWorkspace?.approveBtn} ${t.projectWorkspace?.colDeliverable}: ${deliverableLabel(item.name, t)}`"
              @click="$emit('approve', item)"
            >
              {{ t.projectWorkspace?.approveBtn }}
            </button>
            <button
              v-if="canRevise"
              class="px-3.5 py-1.5 rounded-full text-xs font-semibold border"
              :class="
                isSelected(item)
                  ? 'bg-amber-500 border-amber-500 text-white hover:bg-amber-600'
                  : 'bg-white border-amber-300 text-amber-700 hover:bg-amber-50'
              "
              :aria-label="`${isSelected(item) ? t.projectWorkspace?.reviseUndoCta : t.projectWorkspace?.reviseCta} ${t.projectWorkspace?.colDeliverable}: ${deliverableLabel(item.name, t)}`"
              @click="toggleRevision(item)"
            >
              {{ isSelected(item) ? t.projectWorkspace?.reviseUndoCta : t.projectWorkspace?.reviseCta }}
            </button>
          </template>
          <button
            v-else-if="
              !isClient && canUpload && ['PENDING', 'MISSING', 'REVISION_REQUESTED'].includes(effectiveStatus(item))
            "
            class="px-3.5 py-1.5 rounded-full text-xs font-semibold"
            :class="
              ['MISSING', 'REVISION_REQUESTED'].includes(effectiveStatus(item))
                ? 'bg-ink-700 hover:bg-ink-500 text-white'
                : 'bg-white border border-gray-300 hover:border-gray-900'
            "
            :aria-label="`${t.projectWorkspace?.uploadCta} ${t.projectWorkspace?.colDeliverable}: ${deliverableLabel(item.name, t)}`"
            @click="$emit('upload', item)"
          >
            {{ effectiveStatus(item) === 'MISSING' ? t.projectWorkspace?.uploadCta : t.projectWorkspace?.reuploadCta }}
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

    <!--
      The revision basket only exists once something is in it: a revision is phase-level and
      costs a round, so it is composed deliberately from the rows rather than offered upfront.
    -->
    <div v-if="selected.length" class="mt-4 rounded-lg border border-amber-200 bg-amber-50/60 p-4">
      <div class="flex items-center gap-2">
        <RotateCcw class="w-4 h-4 text-amber-700 shrink-0" />
        <p class="text-sm font-semibold text-amber-800">
          {{ t.projectWorkspace?.revisionBasketTitle }}
        </p>
      </div>
      <p class="text-xs text-amber-700 mt-0.5">{{ t.projectWorkspace?.revisionBasketHint }}</p>

      <ul class="mt-3 flex flex-col gap-2">
        <li v-for="item in selected" :key="item.index" class="rounded-md bg-white border border-amber-200 p-3">
          <div class="flex items-center gap-2">
            <span class="text-sm font-medium text-gray-800 flex-1 min-w-0 truncate">
              {{ deliverableLabel(item.name, t) }}
            </span>
            <button
              class="text-xs font-semibold text-gray-400 hover:text-red-500 shrink-0"
              :aria-label="`${t.projectWorkspace?.reviseUndoCta}: ${deliverableLabel(item.name, t)}`"
              @click="toggleRevision(item)"
            >
              <X class="w-3.5 h-3.5" />
            </button>
          </div>
          <textarea
            v-model="comments[item.index]"
            rows="2"
            class="w-full mt-2 rounded-lg border border-amber-200 p-2.5 text-sm"
            :aria-label="`${t.projectWorkspace?.revisionCommentLabel}: ${deliverableLabel(item.name, t)}`"
            :placeholder="t.projectWorkspace?.revisionModalPlaceholder"
          />
        </li>
      </ul>

      <p v-if="showError" class="text-xs text-red-500 mt-2">
        {{ t.projectWorkspace?.revisionEachRequired }}
      </p>

      <div class="flex items-center gap-2 mt-3">
        <button
          class="px-4 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-white text-sm font-semibold disabled:opacity-50"
          :disabled="busy"
          @click="submit"
        >
          {{ t.projectWorkspace?.revisionSubmitCta }}
        </button>
        <button class="px-4 py-2.5 text-sm text-gray-500" @click="clearSelection">
          {{ t.projectWorkspace?.revisionModalCancel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { RotateCcw, X } from 'lucide-vue-next'
import { deliverableStyles, deliverableLabel } from './workspaceMaps'

const props = defineProps({
  items: { type: Array, default: () => [] },
  phaseStatusKey: { type: String, required: true },
  isClient: { type: Boolean, default: true },
  busy: { type: Boolean, default: false },
  revisionsLeft: { type: Number, default: 0 },
  t: { type: Object, required: true },
  formatDateTime: { type: Function, required: true }
})
const emit = defineEmits(['approve', 'upload', 'open-files', 'request-revision'])

const gridStyle = 'grid-template-columns: 2.4fr 1.2fr 1.3fr 1fr'

const approvedCount = computed(() => props.items.filter(i => i.status === 'APPROVED').length)
const canUpload = computed(() => ['IN_PROGRESS', 'DELIVERED'].includes(props.phaseStatusKey))

/** A revision is only meaningful against submitted work, and only while a round is left to spend. */
const canRevise = computed(() => props.isClient && props.phaseStatusKey === 'DELIVERED' && props.revisionsLeft > 0)

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
    REVISION_REQUESTED: props.t.projectWorkspace?.deliverableRevisionRequested,
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

const selectedIndexes = ref([])
const comments = ref({})
const showError = ref(false)

/**
 * The instruction still waiting on this deliverable: notes are kept for every past round, but only
 * the current one is an outstanding ask -- REVISION_REQUESTED is the backend saying so.
 */
const openRevision = item =>
  effectiveStatus(item) === 'REVISION_REQUESTED' && item.revisions?.length
    ? item.revisions[item.revisions.length - 1]
    : null

const selected = computed(() => props.items.filter(i => selectedIndexes.value.includes(i.index)))
const isSelected = item => selectedIndexes.value.includes(item.index)

const clearSelection = () => {
  selectedIndexes.value = []
  comments.value = {}
  showError.value = false
}

const toggleRevision = item => {
  selectedIndexes.value = isSelected(item)
    ? selectedIndexes.value.filter(i => i !== item.index)
    : [...selectedIndexes.value, item.index]
  if (!selectedIndexes.value.length) clearSelection()
}

// Leaving DELIVERED means the request went through (or the phase moved on): a basket built
// against the previous round must not survive into the next one.
watch(() => props.phaseStatusKey, clearSelection)

const submit = () => {
  const items = selected.value.map(item => ({ ...item, notes: (comments.value[item.index] || '').trim() }))
  if (items.some(i => !i.notes)) {
    showError.value = true
    return
  }
  emit('request-revision', { items })
}
</script>
