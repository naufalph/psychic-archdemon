<template>
  <div class="project-detail">
    <!-- Loading State -->
    <div v-if="isLoading" class="min-h-screen flex items-center justify-center">
      <div class="spinner w-8 h-8"></div>
      <span class="ml-2 text-gray-600">Loading project...</span>
    </div>

    <!-- Project Content -->
    <div v-else-if="project" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Project Header -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-8">
        <div class="flex flex-col lg:flex-row lg:items-start lg:justify-between">
          <div class="flex-1">
            <div class="flex items-center space-x-3 mb-4">
              <h1 class="text-3xl font-bold text-gray-900">{{ project.title }}</h1>
              <span
                class="badge"
                :class="getStatusBadgeClass(project.status)"
              >
                {{ project.status }}
              </span>
            </div>

            <div class="flex flex-wrap items-center gap-4 text-sm text-gray-600 mb-4">
              <div class="flex items-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
                </svg>
                {{ project.location }}
              </div>

              <div class="flex items-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                Deadline: {{ formatDate(project.deadline) }}
              </div>

              <div class="flex items-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
                </svg>
                {{ project.category }}
              </div>
            </div>

            <p class="text-gray-700 leading-relaxed">{{ project.description }}</p>
          </div>

          <div class="mt-6 lg:mt-0 lg:ml-8 lg:flex-shrink-0">
            <div class="bg-gray-50 rounded-lg p-6 min-w-[280px]">
              <div class="text-center mb-6">
                <div class="text-3xl font-bold text-gray-900 mb-1">
                  ${{ project.budget?.toLocaleString() }}
                </div>
                <div class="text-sm text-gray-500">Project Budget</div>
              </div>

              <div class="space-y-4">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Total Bids:</span>
                  <span class="font-medium">{{ project.bidCount || 0 }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600">Posted:</span>
                  <span class="font-medium">{{ formatDate(project.createdAt) }}</span>
                </div>
              </div>

              <div class="mt-6 space-y-3">
                <button class="btn btn-primary w-full">
                  Submit Proposal
                </button>
                <button class="btn btn-outline w-full">
                  Save Project
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Project Details Tabs -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
        <div class="border-b border-gray-200">
          <nav class="flex space-x-8 px-6">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              class="py-4 px-1 border-b-2 font-medium text-sm"
              :class="activeTab === tab.id
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
            >
              {{ tab.name }}
            </button>
          </nav>
        </div>

        <div class="p-6">
          <!-- Overview Tab -->
          <div v-if="activeTab === 'overview'">
            <div class="prose max-w-none">
              <h3>Project Requirements</h3>
              <p>{{ project.requirements || 'No specific requirements provided.' }}</p>

              <h3>Timeline</h3>
              <p>Expected completion: {{ formatDate(project.expectedCompletion) }}</p>

              <h3>Additional Information</h3>
              <p>{{ project.additionalInfo || 'No additional information provided.' }}</p>
            </div>
          </div>

          <!-- Proposals Tab -->
          <div v-if="activeTab === 'proposals'">
            <div v-if="proposals.length === 0" class="text-center py-12">
              <svg class="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
              </svg>
              <h3 class="text-lg font-medium text-gray-900 mb-2">No proposals yet</h3>
              <p class="text-gray-600">Be the first architect to submit a proposal for this project.</p>
            </div>

            <div v-else class="space-y-6">
              <div
                v-for="proposal in proposals"
                :key="proposal.id"
                class="border border-gray-200 rounded-lg p-6"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <h4 class="text-lg font-semibold text-gray-900">{{ proposal.architect.name }}</h4>
                    <p class="text-gray-600 mb-4">{{ proposal.message }}</p>
                    <div class="flex items-center space-x-4 text-sm text-gray-500">
                      <span>Timeline: {{ proposal.timeline }}</span>
                      <span>Experience: {{ proposal.architect.experience }} years</span>
                    </div>
                  </div>
                  <div class="ml-6 text-right">
                    <div class="text-2xl font-bold text-gray-900">${{ proposal.amount?.toLocaleString() }}</div>
                    <div class="text-sm text-gray-500">Proposal amount</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Files Tab -->
          <div v-if="activeTab === 'files'">
            <div v-if="files.length === 0" class="text-center py-12">
              <svg class="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"/>
              </svg>
              <h3 class="text-lg font-medium text-gray-900 mb-2">No files uploaded</h3>
              <p class="text-gray-600">The project owner hasn't uploaded any files yet.</p>
            </div>
          </div>

          <!-- Activity Tab -->
          <div v-if="activeTab === 'activity'">
            <div class="space-y-4">
              <div
                v-for="activity in activities"
                :key="activity.id"
                class="flex items-start space-x-3"
              >
                <div class="w-2 h-2 bg-primary-500 rounded-full mt-2"></div>
                <div class="flex-1">
                  <p class="text-sm text-gray-900">{{ activity.message }}</p>
                  <p class="text-xs text-gray-500">{{ formatDate(activity.createdAt) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Project Not Found -->
    <div v-else class="min-h-screen flex items-center justify-center">
      <div class="text-center">
        <h2 class="text-2xl font-bold text-gray-900 mb-4">Project not found</h2>
        <p class="text-gray-600 mb-6">The project you're looking for doesn't exist or has been removed.</p>
        <router-link to="/projects" class="btn btn-primary">
          Back to Projects
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProjectDetail',
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      isLoading: true,
      project: null,
      activeTab: 'overview',
      tabs: [
        { id: 'overview', name: 'Overview' },
        { id: 'proposals', name: 'Proposals' },
        { id: 'files', name: 'Files' },
        { id: 'activity', name: 'Activity' }
      ],
      proposals: [],
      files: [],
      activities: [
        {
          id: 1,
          message: 'Project was posted',
          createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
        },
        {
          id: 2,
          message: 'New proposal received from John Smith',
          createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000)
        }
      ]
    }
  },
  async mounted() {
    await this.loadProject()
  },
  methods: {
    async loadProject() {
      this.isLoading = true
      try {
        // TODO: Replace with actual API call
        // const response = await projectAPI.getById(this.id)
        // this.project = response.data

        // Mock project data
        this.project = {
          id: this.id,
          title: 'Modern Family Home Design',
          description: 'Looking for an architect to design a contemporary 3-bedroom family home with sustainable features and open-plan living spaces. The design should incorporate modern materials and energy-efficient systems.',
          category: 'Residential',
          budget: 250000,
          location: 'Austin, TX',
          status: 'ACTIVE',
          bidCount: 8,
          deadline: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
          createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
          expectedCompletion: new Date(Date.now() + 120 * 24 * 60 * 60 * 1000),
          requirements: 'The home should be approximately 2,500 square feet with 3 bedrooms, 2.5 bathrooms, an open kitchen and living area, and a 2-car garage. Sustainable materials and energy-efficient systems are preferred.',
          additionalInfo: 'We have a sloped lot with great views to the south. Looking for an architect who specializes in modern residential design and has experience with sustainable building practices.'
        }

        await new Promise(resolve => setTimeout(resolve, 1000)) // Simulate API call
      } catch (error) {
        console.error('Failed to load project:', error)
      } finally {
        this.isLoading = false
      }
    },
    getStatusBadgeClass(status) {
      const classes = {
        ACTIVE: 'badge-primary',
        IN_PROGRESS: 'badge-warning',
        COMPLETED: 'badge-success',
        CANCELLED: 'badge-danger',
        DRAFT: 'bg-gray-100 text-gray-800'
      }
      return classes[status] || 'badge-secondary'
    },
    formatDate(date) {
      if (!date) return 'Not specified'
      return new Date(date).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }
  }
}
</script>

<style scoped>
.spinner {
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.badge-primary {
  background-color: rgb(219 234 254);
  color: rgb(30 64 175);
}

.badge-success {
  background-color: rgb(220 252 231);
  color: rgb(22 101 52);
}

.badge-warning {
  background-color: rgb(254 249 195);
  color: rgb(133 77 14);
}

.badge-danger {
  background-color: rgb(254 226 226);
  color: rgb(153 27 27);
}

.badge-secondary {
  background-color: rgb(243 244 246);
  color: rgb(55 65 81);
}

.prose h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: rgb(17 24 39);
  margin-top: 1.5rem;
  margin-bottom: 0.5rem;
}

.prose h3:first-child {
  margin-top: 0;
}

.prose p {
  margin-bottom: 1rem;
  line-height: 1.625;
  color: rgb(75 85 99);
}
</style>
