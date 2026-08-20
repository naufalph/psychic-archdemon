<template>
  <div
    class="group rounded-card overflow-hidden border border-hairline bg-white cursor-pointer transition-all duration-300 hover:-translate-y-1.5 hover:shadow-[0_20px_30px_-10px_rgba(0,0,0,0.12)] hover:border-hairline-alt"
  >
    <div class="relative overflow-hidden bg-surface-muted" style="aspect-ratio: 1/1">
      <img
        v-if="project.firstImageUrl"
        :src="project.firstImageUrl"
        :alt="project.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
      />
      <div v-else class="w-full h-full flex items-center justify-center">
        <svg class="w-12 h-12 text-hairline-alt" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="1.5"
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
          />
        </svg>
      </div>
    </div>
    <div class="p-3">
      <span class="text-nano font-extrabold uppercase tracking-wide text-ink-400 block mb-1">
        {{ projectTypeLabel(project, locale) || project.projectCategory }}
      </span>
      <h3 class="text-caption font-bold text-ink-900 line-clamp-1 leading-snug">
        {{ project.title }}
      </h3>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from '@/composables/useI18n'
import { projectTypeLabel } from '@/constants/projectTaxonomy'

const { locale } = useI18n()

defineProps({
  project: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])
</script>
