import { defineStore } from 'pinia'
import { chatAPI } from '@/services/api'

export const useChatStore = defineStore('chat', {
  state: () => ({
    conversations: [],
    currentMessages: [],
    currentConversation: null,
    loading: false,
    sending: false,
    error: null,
    currentPage: 0,
    hasMoreMessages: true
  }),

  actions: {
    async fetchConversations() {
      this.loading = true
      this.error = null
      try {
        const response = await chatAPI.getMyConversations()
        this.conversations = response.data.data || []
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to load conversations'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchConversation(conversationId) {
      this.loading = true
      this.error = null
      try {
        const response = await chatAPI.getConversation(conversationId)
        this.currentConversation = response.data.data
        return this.currentConversation
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to load conversation'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchMessages(conversationId, page = 0) {
      this.loading = true
      this.error = null
      try {
        const response = await chatAPI.getMessages(conversationId, page)
        const data = response.data.data
        const messages = data.messages || []

        if (page === 0) {
          this.currentMessages = messages
        } else {
          this.currentMessages = [...messages, ...this.currentMessages]
        }

        this.currentPage = page
        this.hasMoreMessages = data.hasMore === true
        return messages
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to load messages'
        throw error
      } finally {
        this.loading = false
      }
    },

    async sendMessage(conversationId, content) {
      this.sending = true
      this.error = null
      try {
        const response = await chatAPI.sendMessage({ conversationId, content })
        const message = response.data.data
        this.currentMessages.push(message)
        return message
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to send message'
        throw error
      } finally {
        this.sending = false
      }
    },

    async markAllRead(conversationId) {
      try {
        await chatAPI.markAllRead(conversationId)
      } catch (error) {
        console.error('Failed to mark messages as read:', error)
      }
    },

    clearCurrentChat() {
      this.currentMessages = []
      this.currentConversation = null
      this.currentPage = 0
      this.hasMoreMessages = true
    }
  }
})
