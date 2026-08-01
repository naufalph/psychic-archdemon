<template>
  <div
    class="group rounded-card overflow-hidden border border-hairline bg-white cursor-pointer transition-all duration-300 hover:-translate-y-1.5 hover:shadow-[0_20px_30px_-10px_rgba(0,0,0,0.12)] hover:border-hairline-alt"
  >
    <div class="relative overflow-hidden bg-surface-muted" style="aspect-ratio: 4/3">
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
      <span
        v-if="project.buildingFunction || project.projectCategory"
        class="absolute top-3 left-3 text-nano font-semibold px-2.5 py-1 rounded-full uppercase tracking-wide text-ink-900"
        style="background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(4px)"
      >
        {{ project.buildingFunction || project.projectCategory }}
      </span>
      <span
        v-if="statusLabel"
        class="absolute top-3 right-3 text-nano font-semibold px-2.5 py-1 rounded-full uppercase tracking-wide"
        :class="statusClass"
      >
        {{ statusLabel }}
      </span>
    </div>
    <div class="p-4">
      <h3 class="text-body-sm font-semibold text-ink-900 mb-1 line-clamp-1 tracking-[-0.01em]">
        {{ project.title }}
      </h3>
      <p v-if="project.location" class="text-micro text-ink-300 leading-snug line-clamp-1">📍 {{ project.location }}</p>
      <p v-if="budgetLabel" class="text-micro font-semibold text-ink-900 mt-2">
        {{ budgetLabel }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatBudget, formatStatus, statusBadgeClass } from '@/utils/catalogFormat'

const props = defineProps({
  project: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const budgetLabel = computed(() => (props.project.budgetDisplay ? formatBudget(props.project.budgetDisplay) : null))
const statusLabel = computed(() => (props.project.status ? formatStatus(props.project.status) : null))
const statusClass = computed(() => statusBadgeClass(props.project.status))
</script>
