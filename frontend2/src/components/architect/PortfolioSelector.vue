<template>
  <div class="space-y-4">
    <div v-if="loading" class="text-center py-8">
      <div class="animate-spin h-8 w-8 border-4 border-brand-brown border-t-transparent rounded-full mx-auto"></div>
      <p class="text-gray-500 mt-2">Loading portfolios...</p>
    </div>

    <div v-else-if="portfolios.length === 0" class="text-center py-8 bg-gray-50 rounded-2xl">
      <p class="text-gray-500">No portfolios available. Create portfolios first to attach them to proposals.</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div
        v-for="portfolio in portfolios"
        :key="portfolio.id"
        class="cursor-pointer border-2 rounded-2xl p-4 transition-all hover:shadow-md"
        :class="isSelected(portfolio.id) ? 'border-brand-brown bg-brand-tan' : 'border-gray-200 bg-white'"
        @click="toggleSelection(portfolio.id)"
      >
        <div class="flex gap-4">
          <div v-if="portfolio.images && portfolio.images.length > 0" class="flex-shrink-0">
            <img
              :src="portfolio.images[0].thumbnailUrl || portfolio.images[0].url"
              :alt="portfolio.title"
              class="w-20 h-20 object-cover rounded-lg"
            />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-start justify-between gap-2">
              <h3 class="font-bold text-gray-900 truncate">{{ portfolio.title }}</h3>
              <input
                type="checkbox"
                :checked="isSelected(portfolio.id)"
                class="mt-1 h-5 w-5 text-brand-brown border-gray-300 rounded focus:ring-brand-brown"
                @click.stop="toggleSelection(portfolio.id)"
              />
            </div>
            <p class="text-xs text-gray-500 mt-1">{{ portfolio.buildingType }} • {{ portfolio.location }}</p>
            <p v-if="portfolio.description" class="text-sm text-gray-600 mt-2 line-clamp-2">
              {{ portfolio.description }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="selectedCount > 0" class="text-sm text-gray-600 font-medium">
      {{ selectedCount }} portfolio{{ selectedCount > 1 ? 's' : '' }} selected
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePortfoliosStore } from '@/stores/portfolios'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const portfoliosStore = usePortfoliosStore()
const portfolios = computed(() => portfoliosStore.portfolios)
const loading = ref(false)

const selectedCount = computed(() => props.modelValue.length)

const isSelected = portfolioId => {
  return props.modelValue.includes(portfolioId)
}

const toggleSelection = portfolioId => {
  const newValue = [...props.modelValue]
  const index = newValue.indexOf(portfolioId)

  if (index > -1) {
    newValue.splice(index, 1)
  } else {
    newValue.push(portfolioId)
  }

  emit('update:modelValue', newValue)
}

onMounted(async () => {
  loading.value = true
  try {
    await portfoliosStore.fetchPortfolios()
  } catch (error) {
    console.error('Failed to load portfolios:', error)
  } finally {
    loading.value = false
  }
})
</script>
