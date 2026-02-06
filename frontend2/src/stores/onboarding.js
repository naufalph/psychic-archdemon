import { defineStore } from 'pinia'
import { useAuthStore } from './auth'
import { portfolioAPI, architectAPI } from '@/services/api'
import { polishPhilosophy, getArchitecturalAdvice } from '@/services/geminiService'

const STEPS = [
  'WELCOME',
  'IDENTITY',
  'PHILOSOPHY',
  'EXPERTISE',
  'PROFILE_CONFIRM',
  'PORTFOLIO_INTRO',
  'PORTFOLIO_PROJECT',
  'REVIEW',
  'ACTIVATED'
]

export const useOnboardingStore = defineStore('onboarding', {
  state: () => ({
    currentStep: 'WELCOME',
    profile: {
      name: '',
      city: '',
      experienceRange: '',
      philosophy: '',
      expertise: []
    },
    portfolio: [],
    currentProject: null,
    isLoading: false,
    error: null,
    aiTip: 'Welcome. I am your design guide.'
  }),

  getters: {
    currentStepIndex: state => STEPS.indexOf(state.currentStep),
    canGoBack: state => state.currentStep !== 'WELCOME' && state.currentStep !== 'ACTIVATED',
    canGoNext: state => {
      switch (state.currentStep) {
        case 'IDENTITY':
          return state.profile.name && state.profile.city && state.profile.experienceRange
        case 'PHILOSOPHY':
          return state.profile.philosophy.trim().length > 0
        case 'EXPERTISE':
          return state.profile.expertise.length > 0
        case 'PORTFOLIO_PROJECT':
          return (
            state.currentProject &&
            state.currentProject.name &&
            state.currentProject.type &&
            state.currentProject.year &&
            state.currentProject.images.length > 0
          )
        default:
          return true
      }
    },
    totalPortfolios: state => state.portfolio.length,
    isProfileComplete: state => {
      return (
        state.profile.name &&
        state.profile.city &&
        state.profile.experienceRange &&
        state.profile.philosophy &&
        state.profile.expertise.length > 0
      )
    }
  },

  actions: {
    nextStep() {
      const currentIndex = STEPS.indexOf(this.currentStep)
      if (currentIndex < STEPS.length - 1) {
        this.currentStep = STEPS[currentIndex + 1]
        this.saveToLocalStorage()
      }
    },

    previousStep() {
      const currentIndex = STEPS.indexOf(this.currentStep)
      if (currentIndex > 0) {
        this.currentStep = STEPS[currentIndex - 1]
        this.saveToLocalStorage()
      }
    },

    goToStep(step) {
      if (STEPS.includes(step)) {
        this.currentStep = step
        this.saveToLocalStorage()
      }
    },

    updateProfile(data) {
      this.profile = { ...this.profile, ...data }
      this.saveToLocalStorage()
    },

    updateExpertise(tags) {
      this.profile.expertise = tags
      this.saveToLocalStorage()
    },

    async enhancePhilosophy() {
      if (!this.profile.philosophy) {
        this.error = 'Please enter your philosophy first'
        return
      }

      try {
        this.isLoading = true
        this.error = null
        const enhanced = await polishPhilosophy(this.profile.philosophy)
        this.profile.philosophy = enhanced
        this.saveToLocalStorage()
      } catch (error) {
        console.error('Philosophy enhancement error:', error)
        this.error = 'Failed to enhance philosophy. Please try again.'
      } finally {
        this.isLoading = false
      }
    },

    async saveProfileToBackend() {
      try {
        this.isLoading = true
        this.error = null

        const profileData = {
          companyName: this.profile.name,
          city: this.profile.city,
          experienceRange: this.profile.experienceRange,
          philosophy: this.profile.philosophy,
          expertise: this.profile.expertise
        }

        await architectAPI.updateOnboardingProfile(profileData)
      } catch (error) {
        console.error('Save profile error:', error)
        this.error = error.response?.data?.message || 'Failed to save profile. Please try again.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    createProject() {
      this.currentProject = {
        name: '',
        type: '',
        year: new Date().getFullYear(),
        story: {
          problem: '',
          decision: ''
        },
        images: []
      }
    },

    updateProject(data) {
      if (this.currentProject) {
        this.currentProject = { ...this.currentProject, ...data }
        this.saveToLocalStorage()
      }
    },

    async saveProject() {
      if (!this.currentProject) {
        this.error = 'No project to save'
        return
      }

      try {
        this.isLoading = true
        this.error = null

        const authStore = useAuthStore()
        if (!authStore.user?.registeredRoles?.includes('ARCHITECT')) {
          await authStore.activateRole('ARCHITECT')
        }

        const portfolioId = await this.createPortfolioAPI(this.currentProject)

        this.portfolio.push({ ...this.currentProject, id: portfolioId })
        this.currentProject = null

        this.saveToLocalStorage()
      } catch (error) {
        console.error('Save project error:', error)
        this.error = error.response?.data?.message || 'Failed to save portfolio. Please try again.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async createPortfolioAPI(project) {
      const formData = new FormData()
      formData.append('title', project.name)

      const description =
        project.story.problem && project.story.decision
          ? `${project.story.problem}\n\n${project.story.decision}`
          : project.story.problem || project.story.decision || ''

      formData.append('description', description)
      formData.append('projectDate', new Date(project.year, 0, 1).toISOString().split('T')[0])
      formData.append('projectType', project.type)
      formData.append('isBuilt', 'true')

      project.images.forEach(file => {
        formData.append('images', file)
      })

      const response = await portfolioAPI.create(formData)
      return response.data.data.id
    },

    deleteProject(index) {
      if (index >= 0 && index < this.portfolio.length) {
        this.portfolio.splice(index, 1)
        this.saveToLocalStorage()
      }
    },

    async getContextualAdvice(context) {
      try {
        const advice = await getArchitecturalAdvice(context)
        this.aiTip = advice
      } catch (error) {
        console.error('AI advice error:', error)
      }
    },

    saveToLocalStorage() {
      const data = {
        currentStep: this.currentStep,
        profile: this.profile,
        portfolio: this.portfolio.map(p => ({
          ...p,
          images: []
        }))
      }
      localStorage.setItem('onboarding_progress', JSON.stringify(data))
    },

    loadFromLocalStorage() {
      const saved = localStorage.getItem('onboarding_progress')
      if (saved) {
        try {
          const data = JSON.parse(saved)
          this.currentStep = data.currentStep || 'WELCOME'
          this.profile = data.profile || this.profile
          this.portfolio = data.portfolio || []
        } catch (error) {
          console.error('Failed to load onboarding progress:', error)
        }
      }
    },

    clearOnboardingData() {
      this.$reset()
      localStorage.removeItem('onboarding_progress')
      localStorage.removeItem('pending_user_role')
      localStorage.removeItem('pending_user_email')
    }
  }
})
