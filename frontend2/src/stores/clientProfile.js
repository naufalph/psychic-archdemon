import { defineStore } from 'pinia'
import { clientAPI } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/composables/useI18n'

export const useClientProfileStore = defineStore('clientProfile', {
  state: () => ({
    profile: null,
    isLoading: false,
    error: null,
    isEditMode: false
  }),

  getters: {
    hasProfile: state => state.profile !== null,

    firstName: state => state.profile?.firstName || '',

    lastName: state => state.profile?.lastName || '',

    profileName: state => {
      const first = state.profile?.firstName || ''
      const last = state.profile?.lastName || ''
      return `${first} ${last}`.trim() || 'Client'
    },

    profileEmail: () => {
      const authStore = useAuthStore()
      return authStore.user?.email || ''
    },

    profilePhone: state => state.profile?.phoneNumber || '',

    ktpNumber: state => state.profile?.ktpNum || '',

    ktpVerified: state => state.profile?.ktpVerified || false,

    projectStats: state => ({
      matched: state.profile?.projectMatch || 0,
      finished: state.profile?.projectFinished || 0
    })
  },

  actions: {
    async fetchProfile() {
      this.isLoading = true
      this.error = null

      try {
        const response = await clientAPI.getProfile()

        if (response.data.success) {
          this.profile = response.data.data
        } else {
          throw new Error(response.data.message || 'Failed to load profile')
        }
      } catch (error) {
        console.error('Failed to fetch client profile:', error)
        this.error = error.response?.data?.message || error.message || 'Failed to load profile'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async updateProfile(profileData) {
      this.isLoading = true
      this.error = null

      try {
        const response = await clientAPI.updateProfile(profileData)

        if (response.data.success) {
          this.profile = response.data.data
          this.isEditMode = false
          return response.data
        } else {
          throw new Error(response.data.message || 'Failed to update profile')
        }
      } catch (error) {
        console.error('Failed to update client profile:', error)
        const { t } = useI18n()

        if (error.response?.data?.errors) {
          const fieldErrors = error.response.data.errors
          const translatedErrors = {}

          Object.keys(fieldErrors).forEach(field => {
            const errorCode = fieldErrors[field]
            translatedErrors[field] = t.errors?.[errorCode] || errorCode
          })

          this.error = Object.values(translatedErrors).join(', ')
        } else if (error.response?.data?.message) {
          const errorCode = error.response.data.message
          this.error = t.errors?.[errorCode] || errorCode
        } else {
          this.error = error.message || 'Failed to update profile'
        }
        throw error
      } finally {
        this.isLoading = false
      }
    },

    enableEditMode() {
      this.isEditMode = true
    },

    disableEditMode() {
      this.isEditMode = false
      this.error = null
    },

    clearError() {
      this.error = null
    }
  }
})
