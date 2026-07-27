import { defineStore } from 'pinia'
import { legalAPI } from '@/services/api'

export const useLegalStore = defineStore('legal', {
  state: () => ({
    docs: {},
    isLoading: false,
    error: null
  }),

  actions: {
    async fetchCurrent(docType, lang) {
      const key = `${docType}:${lang}`
      if (this.docs[key]) {
        return this.docs[key]
      }

      try {
        this.isLoading = true
        this.error = null

        const response = await legalAPI.getCurrent(docType, lang)
        this.docs[key] = response.data.data
        return this.docs[key]
      } catch (error) {
        console.error('Fetch legal document error:', error)
        this.error = error.response?.data?.message || 'Failed to load legal document'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    invalidate(docType, lang) {
      delete this.docs[`${docType}:${lang}`]
    }
  }
})
