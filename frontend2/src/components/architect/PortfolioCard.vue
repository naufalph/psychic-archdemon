<template>
  <div
    class="group relative bg-white rounded-2xl shadow-md hover:shadow-xl transition-all duration-300 overflow-hidden border border-gray-100 hover:border-amber-200"
  >
    <div class="aspect-square overflow-hidden bg-gray-100 relative">
      <img
        v-if="coverImage"
        :src="coverImage"
        :alt="portfolio.title"
        class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
      />
      <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-amber-100 to-amber-200">
        <svg class="w-16 h-16 text-amber-600/30" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
          />
        </svg>
      </div>

      <div v-if="portfolio.madeWithRumantra" class="absolute top-3 left-3">
        <span
          class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full font-medium text-xs shadow-sm"
        >
          {{ t.portfolio.card.madeWithRumantra }}
        </span>
      </div>

      <div
        class="absolute inset-0 bg-gradient-to-t from-black/60 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
      >
        <div class="absolute bottom-4 left-4 right-4">
          <h3 class="text-white font-bold text-lg line-clamp-2">{{ portfolio.title }}</h3>
        </div>
      </div>

      <div class="absolute top-3 right-3 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
        <button
          @click.stop="$emit('view')"
          class="w-9 h-9 bg-white rounded-full flex items-center justify-center shadow-lg hover:bg-blue-50 transition-colors"
          title="View"
        >
          <Eye class="w-4 h-4 text-blue-600" />
        </button>
        <button
          @click.stop="$emit('edit')"
          class="w-9 h-9 bg-white rounded-full flex items-center justify-center shadow-lg hover:bg-amber-50 transition-colors"
          :title="t.portfolio.card.edit"
        >
          <svg class="w-4 h-4 text-amber-800" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
            />
          </svg>
        </button>
        <button
          @click.stop="$emit('delete')"
          class="w-9 h-9 bg-white rounded-full flex items-center justify-center shadow-lg hover:bg-red-50 transition-colors"
          :title="t.portfolio.card.delete"
        >
          <svg class="w-4 h-4 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
            />
          </svg>
        </button>
      </div>
    </div>

    <div class="p-4 space-y-3">
      <div>
        <h3 class="font-bold text-black text-base line-clamp-1">{{ portfolio.title }}</h3>
        <p v-if="portfolio.location" class="text-sm text-gray-600 line-clamp-1">
          {{ portfolio.location }}
        </p>
      </div>

      <div class="flex items-center justify-between text-xs">
        <div class="flex items-center gap-3">
          <span class="px-3 py-1 bg-amber-100 text-amber-800 rounded-full font-medium">
            {{ portfolio.projectType }}
          </span>
          <span v-if="projectYear" class="text-gray-600">{{ projectYear }}</span>
        </div>
        <div class="flex items-center gap-1 text-gray-600">
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
          </svg>
          <span>{{ imageCount }} {{ t.portfolio.card.photos }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Eye } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'

const props = defineProps({
  portfolio: {
    type: Object,
    required: true
  }
})

defineEmits(['view', 'edit', 'delete'])

const { t } = useI18n()

const coverImage = computed(() => {
  if (props.portfolio.images && props.portfolio.images.length > 0) {
    return (
      props.portfolio.images[0].mediumUrl || props.portfolio.images[0].largeUrl || props.portfolio.images[0].originalUrl
    )
  }
  return null
})

const imageCount = computed(() => {
  return props.portfolio.images?.length || 0
})

const projectYear = computed(() => {
  if (props.portfolio.projectDate) {
    const date = new Date(props.portfolio.projectDate)
    return date.getFullYear()
  }
  return null
})
</script>
