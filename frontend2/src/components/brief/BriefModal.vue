<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="open"
        class="fixed inset-0 z-[2000] bg-[rgba(17,17,17,.55)] flex items-start justify-center px-4 py-[clamp(16px,6vh,72px)] box-border overflow-y-auto"
        @click.self="$emit('close')"
      >
        <div
          role="dialog"
          aria-modal="true"
          class="relative w-full bg-white rounded-[24px] box-border animate-brief-rise"
          :class="panelClass"
        >
          <slot />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  panelClass: { type: String, default: 'max-w-[560px] p-8' }
})

const emit = defineEmits(['close'])

const onKey = e => {
  if (e.key === 'Escape') emit('close')
}

watch(
  () => props.open,
  isOpen => {
    document.body.style.overflow = isOpen ? 'hidden' : ''
    if (isOpen) window.addEventListener('keydown', onKey)
    else window.removeEventListener('keydown', onKey)
  }
)

onBeforeUnmount(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKey)
})
</script>
