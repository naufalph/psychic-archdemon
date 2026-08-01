import { defineStore } from 'pinia'

export const useCatalogStore = defineStore('catalog', {
  state: () => ({
    search: '',
    category: 'semua'
  }),

  actions: {
    setSearch(value) {
      this.search = value
    },
    setCategory(value) {
      this.category = value
    }
  }
})
