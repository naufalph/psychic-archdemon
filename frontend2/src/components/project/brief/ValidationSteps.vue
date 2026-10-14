<template>
  <ol class="m-0 p-0 list-none flex flex-col border-t border-hairline pt-5">
    <li v-for="(step, i) in steps" :key="step.label" class="grid grid-cols-[24px_minmax(0,1fr)] gap-3">
      <div class="flex flex-col items-center">
        <span
          class="w-6 h-6 rounded-full border flex items-center justify-center text-micro font-bold"
          :class="DOT[step.state]"
        >
          {{ glyph(step, i) }}
        </span>
        <span
          class="flex-1 w-px min-h-3"
          :class="i === steps.length - 1 ? 'bg-transparent' : step.state === 'done' ? 'bg-ink-900' : 'bg-hairline'"
        />
      </div>
      <div class="flex flex-col gap-0.5 pb-4">
        <span class="text-caption font-semibold" :class="TITLE[step.state]">{{ step.label }}</span>
        <span v-if="step.meta" class="text-caption-sm font-normal leading-[1.45] text-ink-400">{{ step.meta }}</span>
      </div>
    </li>
  </ol>
</template>

<script setup>
defineProps({
  // [{ state: 'done' | 'active' | 'error' | 'todo', label, meta }]
  steps: { type: Array, required: true }
})

const DOT = {
  done: 'bg-ink-900 border-ink-900 text-white',
  active: 'bg-accent-blue border-accent-blue text-white',
  error: 'bg-red-500 border-red-500 text-white',
  todo: 'bg-white border-hairline text-ink-300'
}

const TITLE = {
  done: 'text-ink-900',
  active: 'text-accent-blue',
  error: 'text-red-700',
  todo: 'text-ink-300'
}

const glyph = (step, i) => ({ done: '✓', active: '•', error: '!' })[step.state] || String(i + 1)
</script>
