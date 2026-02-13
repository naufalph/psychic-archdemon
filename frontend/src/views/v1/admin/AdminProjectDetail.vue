<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Back Button -->
      <button
        @click="$router.push('/admin/projects')"
        class="mb-6 flex items-center text-sm font-medium text-gray-600 hover:text-gray-900"
      >
        <svg class="h-5 w-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        Back to Projects
      </button>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <p class="mt-4 text-gray-600">Loading project details...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6">
        <h3 class="text-lg font-medium text-red-800">Error Loading Project</h3>
        <p class="mt-2 text-sm text-red-600">{{ error }}</p>
        <button
          @click="loadProject"
          class="mt-4 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
        >
          Try Again
        </button>
      </div>

      <!-- Project Details -->
      <div v-else-if="project">
        <!-- Header -->
        <div class="bg-white shadow rounded-lg mb-6 p-6">
          <div class="flex items-start justify-between">
            <div>
              <h1 class="text-2xl font-bold text-gray-900">
                {{ project.buildingFunction || `Project #${project.id}` }}
              </h1>
              <p v-if="project.category" class="mt-1 text-sm text-gray-500">{{ project.category }}</p>
            </div>
            <span :class="getStatusBadgeClass(project.status)">
              {{ formatStatus(project.status) }}
            </span>
          </div>
        </div>

        <!-- Project Information Card -->
        <div class="bg-white shadow rounded-lg mb-6 p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Project Information</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- Budget -->
            <div>
              <dt class="text-sm font-medium text-gray-500">Budget Range</dt>
              <dd class="mt-1 text-sm text-gray-900">
                {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
              </dd>
            </div>

            <!-- Total Budget -->
            <div v-if="project.budgetTotal">
              <dt class="text-sm font-medium text-gray-500">Total Budget</dt>
              <dd class="mt-1 text-sm text-gray-900">{{ formatCurrency(project.budgetTotal) }}</dd>
            </div>

            <!-- Category -->
            <div v-if="project.category">
              <dt class="text-sm font-medium text-gray-500">Category</dt>
              <dd class="mt-1 text-sm text-gray-900">{{ project.category }}</dd>
            </div>

            <!-- Building Function -->
            <div v-if="project.buildingFunction">
              <dt class="text-sm font-medium text-gray-500">Building Function</dt>
              <dd class="mt-1 text-sm text-gray-900">{{ project.buildingFunction }}</dd>
            </div>

            <!-- Build Area -->
            <div v-if="project.estimatedBuildArea">
              <dt class="text-sm font-medium text-gray-500">Estimated Build Area</dt>
              <dd class="mt-1 text-sm text-gray-900">{{ project.estimatedBuildArea }} m²</dd>
            </div>

            <!-- Number of Floors -->
            <div v-if="project.numberOfFloors">
              <dt class="text-sm font-medium text-gray-500">Number of Floors</dt>
              <dd class="mt-1 text-sm text-gray-900">{{ project.numberOfFloors }}</dd>
            </div>

            <!-- Land Ownership -->
            <div>
              <dt class="text-sm font-medium text-gray-500">Land Ownership</dt>
              <dd class="mt-1 text-sm text-gray-900">
                {{ project.ownsLand ? 'Owns Land' : 'Does Not Own Land' }}
              </dd>
            </div>

            <!-- Legal Documents -->
            <div>
              <dt class="text-sm font-medium text-gray-500">Legal Documents</dt>
              <dd class="mt-1 text-sm text-gray-900">
                {{ project.hasLegalDocuments ? 'Available' : 'Not Available' }}
              </dd>
            </div>

            <!-- Expected Start Date -->
            <div v-if="project.expectedStartDate">
              <dt class="text-sm font-medium text-gray-500">Expected Start Date</dt>
              <dd class="mt-1 text-sm text-gray-900">{{ formatDate(project.expectedStartDate) }}</dd>
            </div>

            <!-- Bidding Deadline -->
            <div v-if="project.biddingDeadline">
              <dt class="text-sm font-medium text-gray-500">Bidding Deadline</dt>
              <dd class="mt-1 text-sm text-gray-900">{{ formatDate(project.biddingDeadline) }}</dd>
            </div>
          </div>

          <!-- Scope of Work -->
          <div v-if="project.scopeOfWork" class="mt-6">
            <dt class="text-sm font-medium text-gray-500 mb-2">Scope of Work</dt>
            <dd class="text-sm text-gray-900 whitespace-pre-wrap">{{ project.scopeOfWork }}</dd>
          </div>

          <!-- Deliverables -->
          <div v-if="project.deliverables && project.deliverables.length > 0" class="mt-6">
            <dt class="text-sm font-medium text-gray-500 mb-2">Deliverables</dt>
            <ul class="list-disc list-inside text-sm text-gray-900 space-y-1">
              <li v-for="(item, index) in project.deliverables" :key="index">{{ item }}</li>
            </ul>
          </div>

          <!-- Design Preferences -->
          <div v-if="project.designPreferences" class="mt-6">
            <dt class="text-sm font-medium text-gray-500 mb-2">Design Preferences</dt>
            <dd class="text-sm text-gray-900 whitespace-pre-wrap">{{ project.designPreferences }}</dd>
          </div>

          <!-- Attached Files -->
          <div v-if="project.attachedFiles && project.attachedFiles.length > 0" class="mt-6">
            <dt class="text-sm font-medium text-gray-500 mb-2">Attached Files</dt>
            <div class="space-y-2">
              <a
                v-for="(file, index) in project.attachedFiles"
                :key="index"
                :href="file.url"
                target="_blank"
                class="flex items-center text-sm text-blue-600 hover:text-blue-800"
              >
                <svg class="h-4 w-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                  />
                </svg>
                {{ file.name || `Document ${index + 1}` }}
              </a>
            </div>
          </div>
        </div>

        <!-- Validation Section Card -->
        <div class="bg-white shadow rounded-lg p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Validation</h2>

          <!-- Current Status Display -->
          <div class="mb-4 p-4 bg-gray-50 rounded-md">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-gray-700">Current Status:</span>
              <span :class="getStatusBadgeClass(project.status)">
                {{ formatStatus(project.status) }}
              </span>
            </div>
          </div>

          <!-- Existing Validation Notes -->
          <div v-if="project.validationNotes" class="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-md">
            <p class="text-sm font-medium text-blue-800 mb-2">Current Validation Notes:</p>
            <p class="text-sm text-blue-900">{{ project.validationNotes }}</p>
          </div>

          <!-- Validation Form -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">Validation Notes</label>
            <textarea
              v-model="validationNotes"
              rows="4"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Add notes about your decision..."
            ></textarea>
          </div>

          <!-- Action Buttons -->
          <div class="flex gap-4">
            <button
              @click="openValidationModal('approve')"
              :disabled="project.status === 'OPEN'"
              class="flex-1 px-6 py-3 text-sm font-medium text-white bg-green-600 hover:bg-green-700 rounded-md transition disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              {{ project.status === 'OPEN' ? 'Already Approved' : 'Approve Project' }}
            </button>
            <button
              @click="openValidationModal('reject')"
              :disabled="project.status === 'REJECTED'"
              class="flex-1 px-6 py-3 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-md transition disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              {{ project.status === 'REJECTED' ? 'Already Rejected' : 'Reject Project' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Validation Modal -->
    <ValidationModal
      :show="validationModal.show"
      :project-title="validationModal.projectTitle"
      :action="validationModal.action"
      :notes="validationNotes"
      @update:notes="validationNotes = $event"
      @confirm="confirmValidation"
      @cancel="closeValidationModal"
    />
  </div>
</template>

<script>
import { adminProjectAPI } from '@/services/api'
import ValidationModal from '@/components/admin/ValidationModal.vue'

export default {
  name: 'AdminProjectDetail',
  components: {
    ValidationModal
  },
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      project: null,
      isLoading: false,
      error: null,
      validationNotes: '',
      validationModal: {
        show: false,
        projectTitle: '',
        action: ''
      }
    }
  },
  methods: {
    async loadProject() {
      this.isLoading = true
      this.error = null
      try {
        const response = await adminProjectAPI.getProjectById(this.id)
        this.project = response.data.data
        this.validationNotes = this.project.validationNotes || ''
      } catch (error) {
        console.error('Failed to load project:', error)
        this.error = error.response?.data?.message || 'Failed to load project details'
      } finally {
        this.isLoading = false
      }
    },
    openValidationModal(action) {
      if (action === 'reject' && (!this.validationNotes || this.validationNotes.trim().length < 10)) {
        alert('Please provide rejection notes (minimum 10 characters) before rejecting')
        return
      }

      this.validationModal = {
        show: true,
        projectTitle: this.project.buildingFunction || `Project #${this.project.id}`,
        action: action
      }
    },
    closeValidationModal() {
      this.validationModal = {
        show: false,
        projectTitle: '',
        action: ''
      }
    },
    async confirmValidation() {
      try {
        const validationData = {
          isValid: this.validationModal.action === 'approve',
          validationNotes: this.validationNotes.trim()
        }

        await adminProjectAPI.updateValidation(this.id, validationData)

        await this.loadProject()

        this.closeValidationModal()

        this.$notify({
          type: 'success',
          title: 'Success',
          message: `Project ${this.validationModal.action === 'approve' ? 'approved' : 'rejected'} successfully`
        })

        setTimeout(() => {
          this.$router.push('/admin/projects')
        }, 1500)
      } catch (error) {
        console.error('Validation failed:', error)
        this.$notify({
          type: 'error',
          title: 'Error',
          message: error.response?.data?.message || 'Failed to update project validation'
        })
      }
    },
    getStatusBadgeClass(status) {
      const classes = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium'
      switch (status) {
        case 'PENDING_APPROVAL':
          return `${classes} bg-amber-100 text-amber-800`
        case 'OPEN':
          return `${classes} bg-green-100 text-green-800`
        case 'REJECTED':
          return `${classes} bg-red-100 text-red-800`
        case 'BIDDING_CLOSED':
          return `${classes} bg-gray-100 text-gray-800`
        case 'IN_PROGRESS':
          return `${classes} bg-blue-100 text-blue-800`
        case 'COMPLETED':
          return `${classes} bg-emerald-100 text-emerald-800`
        case 'CANCELLED':
          return `${classes} bg-gray-100 text-gray-800`
        default:
          return `${classes} bg-gray-100 text-gray-800`
      }
    },
    formatStatus(status) {
      return status ? status.replace(/_/g, ' ') : 'Unknown'
    },
    formatCurrency(value) {
      if (!value) return 'N/A'
      return new Intl.NumberFormat('id-ID', {
        style: 'currency',
        currency: 'IDR',
        minimumFractionDigits: 0
      }).format(value)
    },
    formatDate(dateString) {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString('id-ID', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }
  },
  mounted() {
    this.loadProject()
  }
}
</script>
