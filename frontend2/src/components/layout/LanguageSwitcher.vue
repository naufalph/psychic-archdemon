<template>
  <button @click="toggleLocale" class="px-3 py-1.5 text-sm font-bold text-gray-700 hover:text-brand-brown transition-colors flex items-center gap-2 border border-gray-200 rounded-lg hover:border-gray-300">
    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
    </svg>
    {{ locale.toUpperCase() }}
  </button>
</template>

<script setup>
import { useI18n } from '@/composables/useI18n'

const { locale, setLocale } = useI18n()

const toggleLocale = () => {
  setLocale(locale.value === 'id' ? 'en' : 'id')
}
</script>
