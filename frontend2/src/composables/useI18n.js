import { ref, computed } from 'vue'
import id from '@/locales/id'
import en from '@/locales/en'

const locale = ref(localStorage.getItem('locale') || 'id')

const messages = {
  id,
  en
}

export function useI18n() {
  const t = computed(() => {
    return messages[locale.value] || {}
  })

  const getT = key => {
    const localeMessages = messages[locale.value] || {}
    return key.split('.').reduce((obj, k) => (obj && obj[k] ? obj[k] : null), localeMessages) || key
  }

  const setLocale = newLocale => {
    if (messages[newLocale]) {
      locale.value = newLocale
      localStorage.setItem('locale', newLocale)
    }
  }

  return {
    t,
    locale,
    setLocale,
    getT
  }
}
