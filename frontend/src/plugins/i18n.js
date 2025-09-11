import { createI18n } from 'vue-i18n'
import en from '@/locales/en.json'
import id from '@/locales/id.json'

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('language') || 'id', // default to Indonesian
  fallbackLocale: 'en',
  messages: {
    en,
    id
  }
})

export default i18n
