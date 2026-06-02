import { defineStore } from 'pinia'
import { architectAPI } from '@/services/api'

export const useArchitectProfileStore = defineStore('architectProfile', {
  state: () => ({
    profile: null,
    isLoading: false,
    error: null,
    isEditMode: false
  }),

  getters: {
    hasProfile: state => state.profile !== null,
    profileName: state => state.profile?.companyName || '',
    profileCity: state => state.profile?.city || '',
    profileExperience: state => state.profile?.experienceRange || '',
    profilePhilosophy: state => state.profile?.philosophy || '',
    profileExpertise: state => state.profile?.expertise || [],
    profileKtpNum: state => state.profile?.ktpNum || '',
    profileKtpVerified: state => state.profile?.ktpVerified || false,
    profileNpwp: state => state.profile?.npwp || '',
    profileNpwpVerified: state => state.profile?.npwpVerified || false,
    profileCompanySite: state => state.profile?.companySite || '',
    profileContactName: state => state.profile?.contactName || '',
    profilePhoneNumber: state => state.profile?.phoneNumber || '',
    profileFullnameKtp: state => state.profile?.fullnameKtp || '',
    profilePhoneVerified: state => state.profile?.phoneVerified || false
  },

  actions: {
    async fetchProfile() {
      try {
        this.isLoading = true
        this.error = null

        const response = await architectAPI.getProfile()
        this.profile = response.data.data
      } catch (error) {
        console.error('Fetch profile error:', error)
        this.error = error.response?.data?.message || 'Failed to load profile'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async updateProfile(profileData) {
      try {
        this.isLoading = true
        this.error = null

        const payload = {
          companyName: profileData.name,
          city: profileData.city,
          experienceRange: profileData.experienceRange,
          philosophy: profileData.philosophy,
          expertise: profileData.expertise,
          ktpNum: profileData.ktpNum,
          npwp: profileData.npwp,
          fullnameKtp: profileData.fullnameKtp,
          companySite: profileData.companySite,
          contactName: profileData.contactName,
          phoneNum: profileData.phoneNum
        }

        const response = await architectAPI.updateFullProfile(payload)
        this.profile = response.data.data
        this.isEditMode = false
      } catch (error) {
        console.error('Update profile error:', error)
        this.error = error.response?.data?.message || 'Failed to update profile'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    toggleEditMode() {
      this.isEditMode = !this.isEditMode
    },

    enableEditMode() {
      this.isEditMode = true
    },

    disableEditMode() {
      this.isEditMode = false
    },

    clearError() {
      this.error = null
    },

    async sendPhoneOtp(phoneNumber) {
      try {
        this.isLoading = true
        this.error = null
        await architectAPI.sendPhoneOtp({ phoneNumber })
      } catch (error) {
        this.error = error.response?.data?.message || 'Gagal mengirim OTP.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async verifyPhoneOtp(phoneNumber, code) {
      try {
        this.isLoading = true
        this.error = null
        const response = await architectAPI.verifyPhoneOtp({ phoneNumber, code })
        this.profile = response.data.data
      } catch (error) {
        this.error = error.response?.data?.message || 'OTP tidak valid atau sudah kadaluarsa.'
        throw error
      } finally {
        this.isLoading = false
      }
    }
  }
})
