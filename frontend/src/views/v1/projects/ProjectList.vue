<template>
  <div class="project-list">
    <!-- Page Header -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="md:flex md:items-center md:justify-between">
          <div class="flex-1 min-w-0">
            <h1 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">Projects</h1>
            <p class="mt-1 text-sm text-gray-500">Discover architecture projects and opportunities</p>
          </div>
          <div class="mt-4 flex md:mt-0 md:ml-4">
            <router-link to="/projects/create" class="btn btn-primary">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              Post New Project
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Filters and Search -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <!-- Search -->
          <div class="md:col-span-2">
            <label class="form-label">Search projects</label>
            <div class="relative">
              <input
                v-model="filters.search"
                type="text"
                placeholder="Search by title, description, or location..."
                class="form-input pl-10"
              />
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
            </div>
          </div>

          <!-- Category Filter -->
          <div>
            <label class="form-label">Category</label>
            <select v-model="filters.category" class="form-input">
              <option value="">All Categories</option>
              <option value="residential">Residential</option>
              <option value="commercial">Commercial</option>
              <option value="industrial">Industrial</option>
              <option value="public">Public</option>
            </select>
          </div>

          <!-- Budget Range -->
          <div>
            <label class="form-label">Budget Range</label>
            <select v-model="filters.budget" class="form-input">
              <option value="">Any Budget</option>
              <option value="0-50000">$0 - $50K</option>
              <option value="50000-100000">$50K - $100K</option>
              <option value="100000-250000">$100K - $250K</option>
              <option value="250000+">$250K+</option>
            </select>
          </div>
        </div>

        <!-- Filter Actions -->
        <div class="flex justify-between items-center mt-6">
          <div class="flex space-x-2">
            <span class="text-sm text-gray-600">{{ filteredProjects.length }} projects found</span>
          </div>
          <div class="flex space-x-2">
            <button @click="clearFilters" class="btn btn-outline btn-sm">Clear Filters</button>
            <select v-model="sortBy" class="form-input text-sm">
              <option value="created">Newest First</option>
              <option value="budget">Budget: High to Low</option>
              <option value="deadline">Deadline: Soonest</option>
              <option value="bids">Most Bids</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex justify-center items-center py-12">
        <div class="spinner w-8 h-8"></div>
        <span class="ml-2 text-gray-600">Loading projects...</span>
      </div>

      <!-- Projects Grid -->
      <div v-else-if="filteredProjects.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="project in filteredProjects"
          :key="project.id"
          class="card hover:shadow-card-hover transition-shadow cursor-pointer group"
          @click="$router.push(`/projects/${project.id}`)"
        >
          <!-- Project Image -->
          <div class="aspect-video bg-gray-200 rounded-t-lg overflow-hidden">
            <img
              v-if="project.image"
              :src="project.image"
              :alt="project.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
            <div
              v-else
              class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200"
            >
              <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                />
              </svg>
            </div>
          </div>

          <!-- Project Content -->
          <div class="card-body">
            <div class="flex items-start justify-between mb-2">
              <h3
                class="text-lg font-semibold text-gray-900 group-hover:text-primary-600 transition-colors line-clamp-1"
              >
                {{ project.title }}
              </h3>
              <span class="badge" :class="getStatusBadgeClass(project.status)">
                {{ project.status }}
              </span>
            </div>

            <p class="text-gray-600 text-sm mb-4 line-clamp-2">
              {{ project.description }}
            </p>

            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center text-sm text-gray-500">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
                {{ project.location }}
              </div>
              <span class="badge badge-primary">{{ project.category }}</span>
            </div>

            <div class="flex items-center justify-between">
              <div>
                <span class="text-lg font-bold text-gray-900"> ${{ project.budget?.toLocaleString() }} </span>
                <span class="text-sm text-gray-500 ml-1">budget</span>
              </div>
              <div class="text-sm text-gray-500">{{ project.bidCount || 0 }} bids</div>
            </div>
          </div>

          <!-- Project Footer -->
          <div class="card-footer flex items-center justify-between">
            <div class="flex items-center text-sm text-gray-500">
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              {{ formatDate(project.deadline) }}
            </div>
            <span class="text-primary-600 font-medium text-sm group-hover:text-primary-700"> View Details → </span>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="1"
            d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2-2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
          />
        </svg>
        <h3 class="text-lg font-medium text-gray-900 mb-2">No projects found</h3>
        <p class="text-gray-600 mb-6">
          {{ hasFilters ? 'Try adjusting your filters or search terms.' : 'Be the first to post a project!' }}
        </p>
        <router-link v-if="!hasFilters" to="/projects/create" class="btn btn-primary">
          Post Your First Project
        </router-link>
        <button v-else @click="clearFilters" class="btn btn-outline">Clear All Filters</button>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="mt-8 flex justify-center">
        <nav class="flex items-center space-x-2">
          <button
            @click="currentPage > 1 && (currentPage -= 1)"
            :disabled="currentPage <= 1"
            class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Previous
          </button>

          <span class="px-4 py-2 text-sm text-gray-700"> Page {{ currentPage }} of {{ totalPages }} </span>

          <button
            @click="currentPage < totalPages && (currentPage += 1)"
            :disabled="currentPage >= totalPages"
            class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </nav>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProjectList',
  data() {
    return {
      isLoading: false,
      projects: [
        {
          id: 1,
          title: 'Modern Family Home Design',
          description:
            'Looking for an architect to design a contemporary 3-bedroom family home with sustainable features and open-plan living spaces.',
          category: 'residential',
          budget: 250000,
          location: 'Austin, TX',
          status: 'ACTIVE',
          bidCount: 8,
          deadline: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
          createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
          image: null
        },
        {
          id: 2,
          title: 'Office Complex Renovation',
          description:
            'Seeking architectural services to transform a 1980s office building into a modern, collaborative workspace with natural lighting and eco-friendly materials.',
          category: 'commercial',
          budget: 750000,
          location: 'Seattle, WA',
          status: 'ACTIVE',
          bidCount: 12,
          deadline: new Date(Date.now() + 45 * 24 * 60 * 60 * 1000),
          createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
          image: null
        },
        {
          id: 3,
          title: 'Community Center Design',
          description:
            'Design a multi-purpose community center that will serve as a hub for local activities, events, and educational programs.',
          category: 'public',
          budget: 1200000,
          location: 'Portland, OR',
          status: 'ACTIVE',
          bidCount: 6,
          deadline: new Date(Date.now() + 60 * 24 * 60 * 60 * 1000),
          createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
          image: null
        }
      ],
      filters: {
        search: '',
        category: '',
        budget: ''
      },
      sortBy: 'created',
      currentPage: 1,
      itemsPerPage: 9
    }
  },
  computed: {
    filteredProjects() {
      let filtered = [...this.projects]

      // Apply search filter
      if (this.filters.search) {
        const search = this.filters.search.toLowerCase()
        filtered = filtered.filter(
          project =>
            project.title.toLowerCase().includes(search) ||
            project.description.toLowerCase().includes(search) ||
            project.location.toLowerCase().includes(search)
        )
      }

      // Apply category filter
      if (this.filters.category) {
        filtered = filtered.filter(project => project.category === this.filters.category)
      }

      // Apply budget filter
      if (this.filters.budget) {
        const [min, max] = this.filters.budget.split('-').map(Number)
        if (max) {
          filtered = filtered.filter(project => project.budget >= min && project.budget <= max)
        } else {
          filtered = filtered.filter(project => project.budget >= min)
        }
      }

      // Apply sorting
      filtered.sort((a, b) => {
        switch (this.sortBy) {
          case 'created':
            return new Date(b.createdAt) - new Date(a.createdAt)
          case 'budget':
            return b.budget - a.budget
          case 'deadline':
            return new Date(a.deadline) - new Date(b.deadline)
          case 'bids':
            return (b.bidCount || 0) - (a.bidCount || 0)
          default:
            return 0
        }
      })

      return filtered
    },
    hasFilters() {
      return this.filters.search || this.filters.category || this.filters.budget
    },
    totalPages() {
      return Math.ceil(this.filteredProjects.length / this.itemsPerPage)
    }
  },
  mounted() {
    this.loadProjects()
  },
  methods: {
    async loadProjects() {
      this.isLoading = true
      try {
        // TODO: Replace with actual API call
        // const response = await projectAPI.getAll()
        // this.projects = response.data
        await new Promise(resolve => setTimeout(resolve, 1000)) // Simulate API call
      } catch (error) {
        console.error('Failed to load projects:', error)
      } finally {
        this.isLoading = false
      }
    },
    clearFilters() {
      this.filters = {
        search: '',
        category: '',
        budget: ''
      }
      this.currentPage = 1
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
      if (!date) return 'No deadline'
      const now = new Date()
      const target = new Date(date)
      const diffTime = target - now
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

      if (diffDays < 0) {
        return 'Overdue'
      } else if (diffDays === 0) {
        return 'Due today'
      } else if (diffDays === 1) {
        return 'Due tomorrow'
      } else if (diffDays <= 7) {
        return `Due in ${diffDays} days`
      } else {
        return target.toLocaleDateString()
      }
    }
  }
}
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.group:hover .group-hover\:scale-105 {
  transform: scale(1.05);
}

.group:hover .group-hover\:text-primary-600 {
  color: rgb(37 99 235);
}

.group:hover .group-hover\:text-primary-700 {
  color: rgb(29 78 216);
}

/* Custom scrollbar for filter section */
.overflow-x-auto::-webkit-scrollbar {
  height: 6px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  @apply bg-gray-100;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  @apply bg-gray-300 rounded-full;
}

/* Loading spinner */
.spinner {
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Card hover effects */
.card {
  transition: all 0.2s ease-in-out;
}

.card:hover {
  transform: translateY(-2px);
}

/* Badge styles */
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
  @apply bg-blue-100 text-blue-800;
}

.badge-success {
  @apply bg-green-100 text-green-800;
}

.badge-warning {
  @apply bg-yellow-100 text-yellow-800;
}

.badge-danger {
  @apply bg-red-100 text-red-800;
}

.badge-secondary {
  @apply bg-gray-100 text-gray-800;
}
</style>
