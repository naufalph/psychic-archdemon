<template>
  <div
    class="group flex items-center gap-1.5 px-3 py-1.5 border border-gray-200 rounded-lg hover:border-gray-300 transition-all cursor-default select-none"
  >
    <Globe class="w-4 h-4 text-gray-500 flex-shrink-0" />
    <span class="text-sm font-bold text-ink-900">{{ locale.toUpperCase() }}</span>
    <span class="hidden group-hover:inline text-gray-400 text-sm font-medium transition-all"> | </span>
    <button
      class="hidden group-hover:inline text-sm font-medium text-ink-300 hover:text-ink-900 transition-colors"
      @click="switchLocale"
    >
      {{ otherLocale }}
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Globe } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'

const { locale, setLocale } = useI18n()

const otherLocale = computed(() => (locale.value === 'id' ? 'EN' : 'ID'))

const switchLocale = () => {
  setLocale(locale.value === 'id' ? 'en' : 'id')
}
</script>
