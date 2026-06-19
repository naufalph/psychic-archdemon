import { defineStore } from 'pinia'
import { bidAPI } from '@/services/api'

export const useBidsStore = defineStore('bids', {
  state: () => ({
    myBids: [],
    projectBids: [],
    currentBid: null,
    quota: {
      tokensRemaining: 0,
      tokensUsedThisMonth: 0,
      tier: null
    },
    loading: false,
    uploadProgress: 0,
    error: null
  }),

  getters: {
    pendingBids: state => state.myBids.filter(b => b.status === 'PENDING'),
    acceptedBids: state => state.myBids.filter(b => b.status === 'ACCEPTED')
  },

  actions: {
    async fetchMyBids() {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.getMyBids()
        this.myBids = response.data.data || []
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch bids'
        console.error('Failed to fetch bids:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProjectBids(projectId) {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.getProjectBids(projectId)
        const bids = response.data.data || []
        this.projectBids = bids
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch project bids'
        console.error('Failed to fetch project bids:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchBidById(bidId) {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.getBid(bidId)
        this.currentBid = response.data.data
        return this.currentBid
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch bid details'
        console.error('Failed to fetch bid:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchQuota() {
      try {
        const response = await bidAPI.getQuota()
        this.quota = response.data.data || {
          tokensRemaining: 0,
          tokensUsedThisMonth: 0,
          tier: null
        }
      } catch (error) {
        console.error('Failed to fetch quota:', error)
        throw error
      }
    },

    async createDraftBid(bidData) {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.createDraftBid(bidData)
        this.currentBid = response.data.data
        return this.currentBid
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to create bid'
        console.error('Failed to create bid:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateDraftBid(bidId, bidData) {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.updateDraftBid(bidId, bidData)
        this.currentBid = response.data.data
        return this.currentBid
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to update bid'
        console.error('Failed to update bid:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateBidDetails(bidId, detailsData) {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.updateBidDetails(bidId, detailsData)
        this.currentBid = response.data.data
        return this.currentBid
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to update bid'
        console.error('Failed to update bid:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async uploadBidImages(bidId, imageType, files) {
      this.uploadProgress = 0
      this.error = null
      try {
        const onProgress = progress => {
          this.uploadProgress = progress
        }
        const response = await bidAPI.uploadBidImages(bidId, imageType, files, onProgress)
        return response.data.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to upload images'
        console.error('Failed to upload bid images:', error)
        throw error
      } finally {
        this.uploadProgress = 0
      }
    },

    async linkPortfolios(bidId, portfolioIds) {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.linkPortfolios(bidId, portfolioIds)
        this.currentBid = response.data.data
        return this.currentBid
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to link portfolios'
        console.error('Failed to link portfolios:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async submitBid(bidId) {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.submitBid(bidId)
        const submittedBid = response.data.data
        this.myBids.unshift(submittedBid)
        this.currentBid = null
        await this.fetchQuota()
        return submittedBid
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to submit bid'
        console.error('Failed to submit bid:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async withdrawBid(bidId) {
      this.loading = true
      this.error = null
      try {
        await bidAPI.withdrawBid(bidId)
        this.myBids = this.myBids.filter(b => b.id !== bidId)
        if (this.currentBid?.id === bidId) {
          this.currentBid = null
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to withdraw bid'
        console.error('Failed to withdraw bid:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async acceptBid(bidId) {
      this.loading = true
      this.error = null
      try {
        const response = await bidAPI.acceptBid(bidId)
        const acceptedBid = response.data.data
        const bidIndex = this.projectBids.findIndex(b => b.id === bidId)
        if (bidIndex !== -1) {
          this.projectBids[bidIndex] = acceptedBid
        }
        return acceptedBid
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to accept bid'
        console.error('Failed to accept bid:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async deleteBidImage(imageId) {
      this.loading = true
      this.error = null
      try {
        await bidAPI.deleteImage(imageId)
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to delete image'
        console.error('Failed to delete bid image:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    clearCurrentBid() {
      this.currentBid = null
    },

    clearError() {
      this.error = null
    }
  }
})
