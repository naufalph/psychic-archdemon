<template>
  <!-- Pinned while the brief scrolls; capped to the visible area (below the 72px top bar, 24px
       margin each side) so its lower half is never out of reach on short screens -->
  <aside class="lg:sticky lg:top-6 lg:max-h-[calc(100dvh-120px)] lg:overflow-y-auto lg:overscroll-contain rounded-card">
    <div
      class="bg-white border border-hairline rounded-card p-6 flex flex-col gap-5 shadow-[0_24px_60px_-48px_rgba(0,0,0,0.35)]"
    >
      <div v-if="meta.length" class="flex flex-col gap-3 pb-5 border-b border-hairline">
        <div class="flex flex-col gap-1">
          <span class="text-caption-sm font-normal text-ink-400">{{ meta[0].k }}</span>
          <span class="text-[22px] font-semibold tracking-[-0.025em] leading-[1.2] text-ink-900">{{ meta[0].v }}</span>
        </div>
        <div v-for="row in meta.slice(1)" :key="row.k" class="flex justify-between gap-3 text-caption font-normal">
          <span class="text-ink-400">{{ row.k }}</span>
          <span class="font-semibold text-right text-ink-900">{{ row.v }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-1.5">
        <h3 class="m-0 text-xl font-bold tracking-[-0.025em] leading-[1.25] text-ink-900">{{ title }}</h3>
        <p class="m-0 text-caption font-normal leading-[1.55] text-ink-500 text-pretty">{{ body }}</p>
      </div>

      <div v-if="notes" class="bg-surface-alt rounded-xl p-4 flex flex-col gap-1.5">
        <span class="text-micro font-bold tracking-[0.08em] uppercase text-ink-400">{{ notesTitle }}</span>
        <span class="text-caption font-normal leading-[1.55] text-ink-900 whitespace-pre-line">{{ notes }}</span>
      </div>

      <slot />

      <BaseButton
        v-if="ctaLabel"
        :variant="ctaVariant"
        size="md"
        full-width
        class="shadow-none hover:shadow-none"
        @click="emit('cta')"
      >
        {{ ctaLabel }}
      </BaseButton>

      <slot name="after" />

      <p v-if="footnote" class="m-0 text-caption-sm font-normal leading-normal text-ink-400">{{ footnote }}</p>
    </div>
  </aside>
</template>

<script setup>
import BaseButton from '@/components/ui/BaseButton.vue'

defineProps({
  title: { type: String, required: true },
  body: { type: String, default: '' },
  notes: { type: String, default: '' },
  notesTitle: { type: String, default: '' },
  ctaLabel: { type: String, default: '' },
  ctaVariant: { type: String, default: 'primary' },
  footnote: { type: String, default: '' },
  meta: { type: Array, default: () => [] }
})

const emit = defineEmits(['cta'])
</script>
