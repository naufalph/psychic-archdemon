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
    profileEducation: state => state.profile?.education || [],
    profileKtpNum: state => state.profile?.ktpNum || '',
    profileKtpVerified: state => state.profile?.ktpVerified || false,
    profileNpwp: state => state.profile?.npwp || '',
    profileNpwpVerified: state => state.profile?.npwpVerified || false,
    profileCompanySite: state => state.profile?.companySite || '',
    profileContactName: state => state.profile?.contactName || '',
    profilePhoneNumber: state => state.profile?.phoneNumber || '',
    profileFullnameKtp: state => state.profile?.fullnameKtp || '',
    profilePhoneVerified: state => state.profile?.phoneVerified || false,
    profileCategory: state => state.profile?.category || '',
    profileFullAddress: state => state.profile?.fullAddress || '',
    profileProvince: state => state.profile?.province || '',
    profilePhotoUrl: state => state.profile?.photoUrl || '',
    profileCompletionPercent: state => state.profile?.profileCompletion?.percent ?? 0,
    profileCompletionChecklist: state => state.profile?.profileCompletion || null
  },

  actions: {
    async fetchProfile() {
      try {
        this.isLoading = true
        this.error = null

        const response = await architectAPI.getProfile()
        this.profile = response.data.data

        // A freshly-created architect has an all-null profile — nudge straight into
        // edit mode instead of showing a wall of blank view-mode labels.
        if (this.profile?.profileCompletion?.percent === 0) {
          this.isEditMode = true
        }
      } catch (error) {
        console.error('Fetch profile error:', error)
        this.error = error.response?.data?.message || 'Failed to load profile'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // Called on every autosaved change, so blank/half-typed fields must never be sent as
    // literal empty strings — that would either fail backend format validation (KTP/NPWP/
    // phone) or silently overwrite a previously-saved valid value with "".
    async updateProfile(profileData) {
      try {
        this.isLoading = true
        this.error = null

        const emptyToNull = v => (v && String(v).trim() ? String(v).trim() : null)
        const validOrNull = (v, pattern) => {
          const trimmed = v ? String(v).trim() : ''
          return pattern.test(trimmed) ? trimmed : null
        }

        const payload = {
          companyName: emptyToNull(profileData.name),
          category: emptyToNull(profileData.category),
          city: emptyToNull(profileData.city),
          province: emptyToNull(profileData.province),
          fullAddress: emptyToNull(profileData.fullAddress),
          experienceRange: emptyToNull(profileData.experienceRange),
          philosophy: emptyToNull(profileData.philosophy),
          expertise: profileData.expertise,
          education: profileData.education,
          ktpNum: validOrNull(profileData.ktpNum, /^[0-9]{16}$/),
          npwp: validOrNull(profileData.npwp, /^[0-9]{15,16}$/),
          fullnameKtp: emptyToNull(profileData.fullnameKtp),
          phoneNum: validOrNull(profileData.phoneNum, /^[0-9]{8,16}$/)
        }

        const response = await architectAPI.updateFullProfile(payload)
        this.profile = response.data.data
      } catch (error) {
        console.error('Update profile error:', error)
        this.error = error.response?.data?.message || 'Failed to update profile'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async uploadPhoto(file) {
      const response = await architectAPI.uploadPhoto(file)
      this.profile = response.data.data
      return this.profile
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
    }
  }
})
