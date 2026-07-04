import { defineStore } from 'pinia'
import { portfolioAPI } from '@/services/api'

export const usePortfoliosStore = defineStore('portfolios', {
  state: () => ({
    portfolios: [],
    currentPortfolio: null,
    isModalOpen: false,
    isLoading: false,
    uploadProgress: 0,
    error: null
  }),

  getters: {
    hasPortfolios: state => state.portfolios.length > 0,
    portfolioCount: state => state.portfolios.length,
    isCreateMode: state => state.currentPortfolio === null,
    isEditMode: state => state.currentPortfolio !== null
  },

  actions: {
    async fetchPortfolios() {
      try {
        this.isLoading = true
        this.error = null

        const response = await portfolioAPI.getAll()
        this.portfolios = response.data.data || []
      } catch (error) {
        console.error('Fetch portfolios error:', error)
        this.error = error.response?.data?.message || 'Failed to load portfolios'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async createPortfolio(formData) {
      try {
        this.isLoading = true
        this.error = null

        const response = await portfolioAPI.create(formData)
        const newPortfolio = response.data.data

        this.portfolios.unshift(newPortfolio)
        this.closeModal()
        return newPortfolio
      } catch (error) {
        console.error('Create portfolio error:', error)
        this.error = error.response?.data?.message || 'Failed to create portfolio'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async updatePortfolio(id, data) {
      try {
        this.isLoading = true
        this.error = null

        const response = await portfolioAPI.update(id, data)
        const updatedPortfolio = response.data.data

        const index = this.portfolios.findIndex(p => p.id === id)
        if (index !== -1) {
          this.portfolios[index] = updatedPortfolio
        }

        this.closeModal()
        return updatedPortfolio
      } catch (error) {
        console.error('Update portfolio error:', error)
        this.error = error.response?.data?.message || 'Failed to update portfolio'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async createPortfolioFromProject(projectId) {
      try {
        this.isLoading = true
        this.error = null

        const response = await portfolioAPI.createFromProject(projectId)
        const newPortfolio = response.data.data

        this.portfolios.unshift(newPortfolio)
        return newPortfolio
      } catch (error) {
        console.error('Archive project to portfolio error:', error)
        this.error = error.response?.data?.message || 'Failed to archive project to portfolio'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async deletePortfolio(id) {
      try {
        this.isLoading = true
        this.error = null

        await portfolioAPI.delete(id)

        const index = this.portfolios.findIndex(p => p.id === id)
        if (index !== -1) {
          this.portfolios.splice(index, 1)
        }
      } catch (error) {
        console.error('Delete portfolio error:', error)
        this.error = error.response?.data?.message || 'Failed to delete portfolio'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async addImages(portfolioId, files) {
      try {
        this.isLoading = true
        this.uploadProgress = 0
        this.error = null

        const response = await portfolioAPI.addImages(portfolioId, files)
        const updatedImages = response.data.data

        const portfolio = this.portfolios.find(p => p.id === portfolioId)
        if (portfolio) {
          portfolio.images = updatedImages
        }

        if (this.currentPortfolio && this.currentPortfolio.id === portfolioId) {
          this.currentPortfolio.images = updatedImages
        }

        return updatedImages
      } catch (error) {
        console.error('Add images error:', error)
        this.error = error.response?.data?.message || 'Failed to upload images'
        throw error
      } finally {
        this.isLoading = false
        this.uploadProgress = 0
      }
    },

    async deleteImage(imageId) {
      try {
        this.error = null

        await portfolioAPI.deleteImage(imageId)

        this.portfolios.forEach(portfolio => {
          if (portfolio.images) {
            portfolio.images = portfolio.images.filter(img => img.id !== imageId)
          }
        })

        if (this.currentPortfolio && this.currentPortfolio.images) {
          this.currentPortfolio.images = this.currentPortfolio.images.filter(
            img => img.id !== imageId
          )
        }
      } catch (error) {
        console.error('Delete image error:', error)
        this.error = error.response?.data?.message || 'Failed to delete image'
        throw error
      }
    },

    openModal(portfolio = null) {
      this.currentPortfolio = portfolio
      this.isModalOpen = true
      this.error = null
    },

    closeModal() {
      this.isModalOpen = false
      this.currentPortfolio = null
      this.error = null
      this.uploadProgress = 0
    },

    clearError() {
      this.error = null
    }
  }
})
