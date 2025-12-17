<template>
  <div class="bg-white rounded-2xl p-4 shadow-md hover:shadow-xl transition-shadow duration-300 cursor-pointer">
    <div class="flex justify-between items-start mb-3">
      <div class="flex-1">
        <h3 class="text-[#2E2E2E] text-2xl font-bold mb-2 line-clamp-2">
          {{ project.buildingFunction || project.projectCategory || $t('projects.untitledProject') }}
        </h3>
        <div class="flex items-center gap-6 text-[#4A6073]">
          <span class="text-base font-medium">{{ entryCount }} {{ $t('projects.entries') }}</span>
          <span class="text-xl font-bold">{{ formatCurrency(project.budgetMax) }}</span>
        </div>
      </div>
    </div>

    <div class="flex flex-wrap gap-2 mb-3">
      <span v-if="isFeatured" class="bg-[#E07800] text-white px-3 py-1 rounded-xl text-sm font-semibold">
        {{ $t('projects.featured') }}
      </span>
      <span v-if="isGuaranteed" class="bg-[#17C20E] text-white px-3 py-1 rounded-xl text-sm font-semibold">
        {{ $t('projects.guaranteed') }}
      </span>
      <span v-if="isUrgent" class="bg-[#C20E0E] text-white px-3 py-1 rounded-xl text-sm font-semibold">
        {{ $t('projects.urgent') }}
      </span>
    </div>

    <p class="text-black text-base font-semibold mb-4 line-clamp-3">
      {{ project.scopeOfWork || project.designPreferences || $t('projects.noDescription') }}
    </p>

    <div class="flex justify-between items-center">
      <div class="flex items-center gap-6">
        <div class="flex items-center gap-2">
          <div class="flex items-center">
            <svg v-for="i in 5" :key="i" class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
              <path
                :class="i <= rating ? 'text-yellow-400' : 'text-gray-300'"
                d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
              />
            </svg>
          </div>
          <span class="text-black text-base font-semibold">{{ rating.toFixed(1) }}</span>
        </div>
        <div class="flex items-center gap-2">
          <svg class="w-8 h-8 text-black" fill="none" stroke="currentColor" viewBox="0 0 32 32">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M8 12h.01M12 12h.01M16 12h.01M21 16c0 4.418-4 8-9 8a9.863 9.863 0 01-4.255-.949L3 23l1.395-3.72C3.512 18.042 3 16.574 3 15c0-4.418 4-8 9-8s9 3.582 9 8z"
            />
          </svg>
          <span class="text-black text-base font-semibold">{{ commentCount }}</span>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <span class="text-black text-base">{{ timeAgo }}</span>
        <button class="p-2 hover:bg-gray-100 rounded-lg transition-colors" @click.stop="toggleSave">
          <svg
            class="w-8 h-8"
            :fill="isSaved ? 'currentColor' : 'none'"
            :stroke="isSaved ? 'none' : 'currentColor'"
            viewBox="0 0 32 32"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M5 5a2 2 0 012-2h14a2 2 0 012 2v20l-9-5-9 5V5z"
            />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps({
  project: {
    type: Object,
    required: true
  },
  entryCount: {
    type: Number,
    default: 0
  },
  isFeatured: {
    type: Boolean,
    default: false
  },
  isGuaranteed: {
    type: Boolean,
    default: false
  },
  isUrgent: {
    type: Boolean,
    default: false
  },
  rating: {
    type: Number,
    default: 0
  },
  commentCount: {
    type: Number,
    default: 0
  }
})

const isSaved = ref(false)

const timeAgo = computed(() => {
  if (!props.project.createdAt) return ''

  const now = new Date()
  const created = new Date(props.project.createdAt)
  const diffMs = now - created
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffHours < 1) {
    const diffMins = Math.floor(diffMs / (1000 * 60))
    return `${diffMins} ${t('projects.minutesAgo')}`
  } else if (diffHours < 24) {
    return `${diffHours} ${t('projects.hoursAgo')}`
  } else {
    return `${diffDays} ${t('projects.daysAgo')}`
  }
})

const formatCurrency = amount => {
  if (!amount) return 'Rp 0'
  return `Rp ${(amount / 100).toLocaleString('id-ID')}`
}

const toggleSave = () => {
  isSaved.value = !isSaved.value
}
</script>
