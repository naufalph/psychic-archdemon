import { defineStore } from 'pinia'
import { projectAPI } from '@/services/api'

export const useProjectsStore = defineStore('projects', {
  state: () => ({
    projects: [],
    openProjects: [],
    currentProject: null,
    loading: false,
    error: null
  }),

  getters: {
    myOpenProjects: state => state.projects.filter(p => p.status === 'OPEN'),
    myClosedProjects: state => state.projects.filter(p => p.status === 'CLOSED'),
    myAwardedProjects: state => state.projects.filter(p => p.status === 'AWARDED')
  },

  actions: {
    async fetchMyProjects() {
      this.loading = true
      this.error = null
      try {
        const response = await projectAPI.getAll()
        this.projects = response.data.data || []
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch projects'
        console.error('Failed to fetch projects:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchOpenProjects() {
      this.loading = true
      this.error = null
      try {
        const response = await projectAPI.getOpenProjects()
        this.openProjects = response.data.data || []
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch open projects'
        console.error('Failed to fetch open projects:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProjectById(id) {
      this.loading = true
      this.error = null
      try {
        const response = await projectAPI.getById(id)
        this.currentProject = response.data.data
        return this.currentProject
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch project'
        console.error('Failed to fetch project:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async createProject(projectData, files) {
      this.loading = true
      this.error = null
      try {
        console.log('=== Creating Project ===')
        console.log('Project Data:', projectData)
        console.log('JSON stringified:', JSON.stringify(projectData))

        const formData = new FormData()
        const jsonBlob = new Blob([JSON.stringify(projectData)], { type: 'application/json' })
        console.log('Blob created:', jsonBlob)
        console.log('Blob size:', jsonBlob.size)

        formData.append('project', jsonBlob)

        if (files && files.length > 0) {
          files.forEach(file => {
            formData.append('files', file)
          })
        }

        console.log('FormData entries:')
        for (let pair of formData.entries()) {
          console.log(pair[0], pair[1])
        }

        const response = await projectAPI.create(formData)
        const newProject = response.data.data
        this.projects.unshift(newProject)
        return newProject
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to create project'
        console.error('Failed to create project:', error)
        console.error('Error response:', error.response?.data)
        throw error
      } finally {
        this.loading = false
      }
    },

    async deleteProject(id) {
      this.loading = true
      this.error = null
      try {
        await projectAPI.delete(id)
        this.projects = this.projects.filter(p => p.id !== id)
        if (this.currentProject?.id === id) {
          this.currentProject = null
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to delete project'
        console.error('Failed to delete project:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    clearCurrentProject() {
      this.currentProject = null
    },

    clearError() {
      this.error = null
    }
  }
})
