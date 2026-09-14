<template>
  <span class="relative inline-flex">
    <button
      ref="triggerRef"
      type="button"
      class="w-4 h-4 rounded-full flex items-center justify-center text-gray-400 hover:text-brand-brown hover:bg-brand-tan/40 focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-brown/40 transition"
      :aria-label="label || t.common.moreInfo"
      :aria-expanded="open"
      :aria-describedby="open ? panelId : undefined"
      @click.stop.prevent="toggle"
      @mouseenter="show"
      @mouseleave="hide"
      @focus="show"
      @blur="hide"
      @keydown.escape.stop="hide"
    >
      <Info :size="14" />
    </button>

    <Teleport to="body">
      <div
        v-if="open"
        :id="panelId"
        ref="panelRef"
        role="tooltip"
        class="fixed z-[60] bg-white rounded-2xl shadow-xl ring-1 ring-black/5 px-4 py-3 text-xs leading-relaxed text-gray-700"
        :style="panelStyle"
        @mouseenter="show"
        @mouseleave="hide"
      >
        <p v-if="label" class="font-semibold text-brand-brown-900 mb-1">{{ label }}</p>
        <slot />
      </div>
    </Teleport>
  </span>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, nextTick } from 'vue'
import { Info } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'

defineProps({
  label: {
    type: String,
    default: ''
  }
})

const { t } = useI18n()

const panelId = `info-tooltip-${Math.random().toString(36).slice(2, 9)}`
const triggerRef = ref(null)
const panelRef = ref(null)
const open = ref(false)
const position = ref({ top: 0, left: 0, width: 280 })

/**
 * The panel is teleported and fixed rather than absolutely positioned inside the trigger:
 * the pickers that use it sit in scrollable, overflow-hidden panels that would otherwise
 * clip it, and clamping against the viewport is what keeps it on screen at phone width.
 */
const GUTTER = 12

const place = () => {
  const trigger = triggerRef.value
  if (!trigger) return
  const rect = trigger.getBoundingClientRect()
  const width = Math.min(280, window.innerWidth - GUTTER * 2)
  const left = Math.min(Math.max(GUTTER, rect.left + rect.width / 2 - width / 2), window.innerWidth - width - GUTTER)

  const panelHeight = panelRef.value?.offsetHeight ?? 0
  const below = rect.bottom + 8
  const fitsBelow = below + panelHeight + GUTTER <= window.innerHeight
  const top = fitsBelow ? below : Math.max(GUTTER, rect.top - panelHeight - 8)

  position.value = { top, left, width }
}

const panelStyle = computed(() => ({
  top: `${position.value.top}px`,
  left: `${position.value.left}px`,
  width: `${position.value.width}px`
}))

let hideTimer = null

const show = async () => {
  clearTimeout(hideTimer)
  if (!open.value) {
    open.value = true
    window.addEventListener('scroll', place, true)
    window.addEventListener('resize', place)
  }
  place()
  await nextTick()
  place()
}

// A tap or click also fires mouseenter and focus, which open the panel just before the click
// lands. Toggling on that click would close it again at once, so a click only closes a panel
// that an earlier click opened.
let pinned = false

const hide = () => {
  clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    pinned = false
    open.value = false
    window.removeEventListener('scroll', place, true)
    window.removeEventListener('resize', place)
  }, 80)
}

const toggle = () => {
  if (open.value && pinned) {
    hide()
  } else {
    pinned = true
    show()
  }
}

onBeforeUnmount(() => {
  clearTimeout(hideTimer)
  window.removeEventListener('scroll', place, true)
  window.removeEventListener('resize', place)
})
</script>
