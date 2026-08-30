import { defineStore } from 'pinia'
import { projectAPI } from '@/services/api'

const transformProjectData = backendProject => ({
  ...backendProject,
  buildingType: backendProject.buildingFunction,
  description: backendProject.scopeOfWork,
  lotSize: backendProject.lotSize,
  designBudget: backendProject.designBudgetMax,
  buildingFunction: backendProject.buildingFunction,
  scopeOfWork: backendProject.scopeOfWork,
  estimatedBuildArea: backendProject.estimatedBuildArea,
  bidCount: backendProject.bidCount ?? 0
})

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
    myClosedProjects: state => state.projects.filter(p => p.status === 'CLOSED')
  },

  actions: {
    async fetchMyProjects() {
      this.loading = true
      this.error = null
      try {
        const response = await projectAPI.getAll()
        const projects = response.data.data || []
        this.projects = projects.map(transformProjectData)
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
        const projects = response.data.data || []
        this.openProjects = projects.map(transformProjectData)
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
        this.currentProject = transformProjectData(response.data.data)
        return this.currentProject
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch project'
        console.error('Failed to fetch project:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProjectForArchitect(id) {
      this.loading = true
      this.error = null
      try {
        const response = await projectAPI.getProjectForArchitect(id)
        this.currentProject = transformProjectData(response.data.data)
        return this.currentProject
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch project details'
        console.error('Failed to fetch project for architect:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async createDraftProject(projectData) {
      this.loading = true
      this.error = null
      try {
        const response = await projectAPI.createDraft(projectData)
        const draft = response.data.data
        this.currentProject = transformProjectData(draft)
        return draft
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to save draft project'
        console.error('Failed to create draft project:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateDraftProject(id, projectData) {
      this.loading = true
      this.error = null
      try {
        const response = await projectAPI.update(id, projectData)
        const draft = response.data.data
        this.currentProject = transformProjectData(draft)
        return draft
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to save draft project'
        console.error('Failed to update draft project:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async submitProject(id, files) {
      this.loading = true
      this.error = null
      try {
        const formData = new FormData()
        if (files && files.length > 0) {
          files.forEach(file => {
            formData.append('files', file)
          })
        }

        const response = await projectAPI.submit(id, formData)
        const newProject = response.data.data
        this.projects.unshift(newProject)
        return newProject
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to submit project'
        console.error('Failed to submit project:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async uploadProjectFiles(id, files) {
      this.error = null
      try {
        const response = await projectAPI.uploadFiles(id, files)
        return response.data.data
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to upload images'
        console.error('Failed to upload project files:', error)
        throw error
      }
    },

    async deleteProjectFile(id, fileId) {
      this.error = null
      try {
        await projectAPI.deleteFile(id, fileId)
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to delete image'
        console.error('Failed to delete project file:', error)
        throw error
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
