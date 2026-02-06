import { ref, computed } from 'vue'
import id from '@/locales/id'
import en from '@/locales/en'

const locale = ref(localStorage.getItem('locale') || 'id')

const messages = {
  id,
  en
}

export function useI18n() {
  const t = computed(() => messages[locale.value])

  const setLocale = newLocale => {
    if (messages[newLocale]) {
      locale.value = newLocale
      localStorage.setItem('locale', newLocale)
    }
  }

  return {
    t,
    locale,
    setLocale
  }
}
