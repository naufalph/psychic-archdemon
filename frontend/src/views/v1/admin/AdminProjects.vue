<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Page Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Project Validation</h1>
        <p class="mt-2 text-sm text-gray-600">Review and validate client-submitted projects</p>

        <!-- Stats Summary -->
        <div class="mt-4 grid grid-cols-1 gap-5 sm:grid-cols-4">
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
              <dt class="text-sm font-medium text-gray-500 truncate">Total Projects</dt>
              <dd class="mt-1 text-3xl font-semibold text-gray-900">{{ stats.total }}</dd>
            </div>
          </div>
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
              <dt class="text-sm font-medium text-gray-500 truncate">Pending Approval</dt>
              <dd class="mt-1 text-3xl font-semibold text-amber-600">{{ stats.pending }}</dd>
            </div>
          </div>
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
              <dt class="text-sm font-medium text-gray-500 truncate">Approved</dt>
              <dd class="mt-1 text-3xl font-semibold text-green-600">{{ stats.approved }}</dd>
            </div>
          </div>
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
              <dt class="text-sm font-medium text-gray-500 truncate">Rejected</dt>
              <dd class="mt-1 text-3xl font-semibold text-red-600">{{ stats.rejected }}</dd>
            </div>
          </div>
        </div>
      </div>

      <!-- Filter Section -->
      <div class="bg-white shadow rounded-lg mb-6">
        <div class="px-4 py-5 sm:p-6">
          <!-- Status Filter Tabs -->
          <div class="flex flex-wrap gap-2 mb-4">
            <button
              v-for="filter in statusFilters"
              :key="filter.value"
              @click="filters.status = filter.value"
              :class="[
                'px-4 py-2 rounded-md text-sm font-medium transition',
                filters.status === filter.value
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              {{ filter.label }}
            </button>
          </div>

          <!-- Search and Sort -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Search</label>
              <input
                v-model="filters.search"
                type="text"
                placeholder="Search by title, location, or building function..."
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Sort By</label>
              <select
                v-model="sortBy"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="newest">Newest First</option>
                <option value="oldest">Oldest First</option>
                <option value="budget_low">Budget (Low to High)</option>
                <option value="budget_high">Budget (High to Low)</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        <p class="mt-4 text-gray-600">Loading projects...</p>
      </div>

      <!-- Projects Grid -->
      <div v-else-if="filteredProjects.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="project in filteredProjects"
          :key="project.id"
          class="bg-white rounded-lg shadow hover:shadow-lg transition"
        >
          <div class="p-6">
            <!-- Category Badge -->
            <div class="flex items-center justify-between mb-3">
              <span
                class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
              >
                {{ project.category || 'N/A' }}
              </span>
              <span :class="getStatusBadgeClass(project.status)">
                {{ formatStatus(project.status) }}
              </span>
            </div>

            <!-- Project Details -->
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              {{ project.buildingFunction || `Project ${project.id}` }}
            </h3>

            <div class="space-y-2 text-sm text-gray-600 mb-4">
              <div class="flex items-center">
                <svg
                  class="h-4 w-4 mr-2 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                <span>
                  {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
                </span>
              </div>

              <div v-if="project.estimatedBuildArea" class="flex items-center">
                <svg
                  class="h-4 w-4 mr-2 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                  />
                </svg>
                <span>{{ project.estimatedBuildArea }} m²</span>
              </div>

              <div v-if="project.expectedStartDate" class="flex items-center">
                <svg
                  class="h-4 w-4 mr-2 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
                <span>Start: {{ formatDate(project.expectedStartDate) }}</span>
              </div>
            </div>

            <!-- Validation Notes (if any) -->
            <div v-if="project.validationNotes" class="mb-4 p-3 bg-gray-50 rounded-md">
              <p class="text-xs font-medium text-gray-700 mb-1">Validation Notes:</p>
              <p class="text-xs text-gray-600">{{ project.validationNotes }}</p>
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-2">
              <button
                @click="$router.push(`/admin/projects/${project.id}`)"
                class="flex-1 px-3 py-2 text-sm font-medium text-blue-700 bg-blue-50 hover:bg-blue-100 rounded-md transition"
              >
                View Details
              </button>
              <button
                v-if="project.status === 'PENDING_APPROVAL'"
                @click="openValidationModal(project, 'approve')"
                class="flex-1 px-3 py-2 text-sm font-medium text-white bg-green-600 hover:bg-green-700 rounded-md transition"
              >
                Approve
              </button>
              <button
                v-if="project.status === 'PENDING_APPROVAL'"
                @click="openValidationModal(project, 'reject')"
                class="flex-1 px-3 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-md transition"
              >
                Reject
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12 bg-white rounded-lg shadow">
        <svg
          class="mx-auto h-12 w-12 text-gray-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">No projects found</h3>
        <p class="mt-1 text-sm text-gray-500">
          {{ filters.status || filters.search ? 'Try adjusting your filters' : 'No projects to review' }}
        </p>
      </div>
    </div>

    <!-- Validation Modal -->
    <ValidationModal
      :show="validationModal.show"
      :project-title="validationModal.projectTitle"
      :action="validationModal.action"
      :notes="validationModal.notes"
      @update:notes="validationModal.notes = $event"
      @confirm="confirmValidation"
      @cancel="closeValidationModal"
    />
  </div>
</template>

<script>
import { adminProjectAPI } from '@/services/api'
import ValidationModal from '@/components/admin/ValidationModal.vue'

export default {
  name: 'AdminProjects',
  components: {
    ValidationModal
  },
  data() {
    return {
      projects: [],
      isLoading: false,
      filters: {
        status: '',
        search: ''
      },
      sortBy: 'newest',
      statusFilters: [
        { label: 'All Projects', value: '' },
        { label: 'Pending Approval', value: 'PENDING_APPROVAL' },
        { label: 'Approved', value: 'OPEN' },
        { label: 'Rejected', value: 'REJECTED' }
      ],
      validationModal: {
        show: false,
        projectId: null,
        projectTitle: '',
        action: '',
        notes: ''
      }
    }
  },
  computed: {
    filteredProjects() {
      let filtered = [...this.projects]

      if (this.filters.status) {
        filtered = filtered.filter(p => p.status === this.filters.status)
      }

      if (this.filters.search) {
        const searchLower = this.filters.search.toLowerCase()
        filtered = filtered.filter(
          p =>
            p.buildingFunction?.toLowerCase().includes(searchLower) ||
            p.category?.toLowerCase().includes(searchLower) ||
            p.location?.toLowerCase().includes(searchLower)
        )
      }

      filtered.sort((a, b) => {
        switch (this.sortBy) {
          case 'newest':
            return new Date(b.createdAt) - new Date(a.createdAt)
          case 'oldest':
            return new Date(a.createdAt) - new Date(b.createdAt)
          case 'budget_low':
            return (a.designBudgetMin || 0) - (b.designBudgetMin || 0)
          case 'budget_high':
            return (b.designBudgetMax || 0) - (a.designBudgetMax || 0)
          default:
            return 0
        }
      })

      return filtered
    },
    stats() {
      return {
        total: this.projects.length,
        pending: this.projects.filter(p => p.status === 'PENDING_APPROVAL').length,
        approved: this.projects.filter(p => p.status === 'OPEN').length,
        rejected: this.projects.filter(p => p.status === 'REJECTED').length
      }
    }
  },
  methods: {
    async loadProjects() {
      this.isLoading = true
      try {
        const response = await adminProjectAPI.getAllProjects()
        this.projects = response.data.data || []
      } catch (error) {
        console.error('Failed to load projects:', error)
        this.$notify({
          type: 'error',
          title: 'Error',
          message: 'Failed to load projects. Please try again.'
        })
      } finally {
        this.isLoading = false
      }
    },
    openValidationModal(project, action) {
      this.validationModal = {
        show: true,
        projectId: project.id,
        projectTitle: project.buildingFunction || `Project ${project.id}`,
        action: action,
        notes: project.validationNotes || ''
      }
    },
    closeValidationModal() {
      this.validationModal = {
        show: false,
        projectId: null,
        projectTitle: '',
        action: '',
        notes: ''
      }
    },
    async confirmValidation() {
      if (
        this.validationModal.action === 'reject' &&
        (!this.validationModal.notes || this.validationModal.notes.trim().length < 10)
      ) {
        alert('Rejection notes are required (minimum 10 characters)')
        return
      }

      try {
        const validationData = {
          isValid: this.validationModal.action === 'approve',
          validationNotes: this.validationModal.notes.trim()
        }

        await adminProjectAPI.updateValidation(this.validationModal.projectId, validationData)

        await this.loadProjects()

        this.closeValidationModal()

        this.$notify({
          type: 'success',
          title: 'Success',
          message: `Project ${this.validationModal.action === 'approve' ? 'approved' : 'rejected'} successfully`
        })
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
        month: 'short',
        day: 'numeric'
      })
    }
  },
  mounted() {
    this.loadProjects()
  }
}
</script>
