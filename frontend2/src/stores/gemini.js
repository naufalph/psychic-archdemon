import { defineStore } from 'pinia'
import { analyzeProposals, chatWithData } from '@/services/geminiService'

export const useGeminiStore = defineStore('gemini', {
  state: () => ({
    analysis: null,
    chatHistory: [],
    loading: false,
    error: null
  }),

  getters: {
    hasAnalysis: state => state.analysis !== null,
    sortedProposals: state => {
      if (!state.analysis?.comparison) return []
      return [...state.analysis.comparison].sort((a, b) => b.overallScore - a.overallScore)
    }
  },

  actions: {
    async analyzeProposals(project, proposals) {
      this.loading = true
      this.error = null
      try {
        const result = await analyzeProposals(project, proposals)
        this.analysis = result
        return result
      } catch (error) {
        this.error = error.message || 'Failed to analyze proposals'
        console.error('Failed to analyze proposals:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async sendChatMessage(message, project, proposals) {
      this.loading = true
      this.error = null
      try {
        this.chatHistory.push({
          role: 'user',
          text: message
        })

        const response = await chatWithData(this.chatHistory, project, proposals, message)

        this.chatHistory.push({
          role: 'model',
          text: response
        })

        return response
      } catch (error) {
        this.error = error.message || 'Failed to send chat message'
        this.chatHistory.pop()
        console.error('Failed to send chat message:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    clearAnalysis() {
      this.analysis = null
      this.chatHistory = []
      this.error = null
    },

    clearChatHistory() {
      this.chatHistory = []
    },

    clearError() {
      this.error = null
    }
  }
})
