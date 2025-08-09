<template>
  <div class="architect-list">
    <!-- Page Header -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="md:flex md:items-center md:justify-between">
          <div class="flex-1 min-w-0">
            <h1 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
              Find Architects
            </h1>
            <p class="mt-1 text-sm text-gray-500">
              Discover talented architects for your next project
            </p>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Search and Filters -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <!-- Search -->
          <div class="md:col-span-2">
            <label class="form-label">Search architects</label>
            <div class="relative">
              <input
                v-model="filters.search"
                type="text"
                placeholder="Search by name, specialization, or location..."
                class="form-input pl-10"
              />
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                </svg>
              </div>
            </div>
          </div>

          <!-- Specialization Filter -->
          <div>
            <label class="form-label">Specialization</label>
            <select v-model="filters.specialization" class="form-input">
              <option value="">All Specializations</option>
              <option value="residential">Residential</option>
              <option value="commercial">Commercial</option>
              <option value="industrial">Industrial</option>
              <option value="sustainable">Sustainable Design</option>
            </select>
          </div>

          <!-- Experience Level -->
          <div>
            <label class="form-label">Experience</label>
            <select v-model="filters.experience" class="form-input">
              <option value="">Any Experience</option>
              <option value="0-5">0-5 years</option>
              <option value="5-10">5-10 years</option>
              <option value="10-20">10-20 years</option>
              <option value="20+">20+ years</option>
            </select>
          </div>
        </div>

        <!-- Filter Actions -->
        <div class="flex justify-between items-center mt-6">
          <div class="flex space-x-2">
            <span class="text-sm text-gray-600">{{ filteredArchitects.length }} architects found</span>
          </div>
          <div class="flex space-x-2">
            <button @click="clearFilters" class="btn btn-outline btn-sm">
              Clear Filters
            </button>
            <select v-model="sortBy" class="form-input text-sm">
              <option value="rating">Highest Rated</option>
              <option value="experience">Most Experienced</option>
              <option value="projects">Most Projects</option>
              <option value="recent">Recently Active</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex justify-center items-center py-12">
        <div class="spinner w-8 h-8"></div>
        <span class="ml-2 text-gray-600">Loading architects...</span>
      </div>

      <!-- Architects Grid -->
      <div v-else-if="filteredArchitects.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="architect in filteredArchitects"
          :key="architect.id"
          class="card hover:shadow-card-hover transition-shadow cursor-pointer group"
          @click="$router.push(`/architects/${architect.id}`)"
        >
          <!-- Architect Avatar -->
          <div class="card-body text-center">
            <div class="relative mb-4">
              <div class="w-20 h-20 bg-gray-200 rounded-full mx-auto overflow-hidden">
                <img
                  v-if="architect.avatar"
                  :src="architect.avatar"
                  :alt="architect.name"
                  class="w-full h-full object-cover"
                >
                <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-100 to-primary-200">
                  <svg class="w-10 h-10 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                  </svg>
                </div>
              </div>
              <div v-if="architect.verified" class="absolute -top-1 -right-1">
                <div class="w-6 h-6 bg-green-500 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                  </svg>
                </div>
              </div>
            </div>

            <h3 class="text-lg font-semibold text-gray-900 group-hover:text-primary-600 transition-colors mb-1">
              {{ architect.name }}
            </h3>
            <p class="text-sm text-primary-600 font-medium mb-3">{{ architect.title }}</p>

            <!-- Rating -->
            <div class="flex items-center justify-center mb-3">
              <div class="flex items-center">
                <svg
                  v-for="i in 5"
                  :key="i"
                  class="w-4 h-4"
                  :class="i <= architect.rating ? 'text-yellow-400' : 'text-gray-300'"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                </svg>
              </div>
              <span class="ml-1 text-sm text-gray-600">({{ architect.reviewCount }})</span>
            </div>

            <!-- Specializations -->
            <div class="flex flex-wrap justify-center gap-1 mb-4">
              <span
                v-for="spec in architect.specializations.slice(0, 2)"
                :key="spec"
                class="badge badge-primary text-xs"
              >
                {{ spec }}
              </span>
              <span
                v-if="architect.specializations.length > 2"
                class="badge bg-gray-100 text-gray-600 text-xs"
              >
                +{{ architect.specializations.length - 2 }}
              </span>
            </div>

            <!-- Location and Experience -->
            <div class="space-y-2 text-sm text-gray-600 mb-4">
              <div class="flex items-center justify-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
                </svg>
                {{ architect.location }}
              </div>
              <div class="flex items-center justify-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2-2v2m8 0V6a2 2 0 012 2v6a2 2 0 01-2 2H6a2 2 0 01-2-2V8a2 2 0 012-2V6"/>
                </svg>
                {{ architect.experience }} years experience
              </div>
            </div>

            <!-- Projects Count -->
            <div class="text-center border-t border-gray-200 pt-4">
              <div class="text-lg font-semibold text-gray-900">{{ architect.projectCount }}</div>
              <div class="text-xs text-gray-500">Completed Projects</div>
            </div>
          </div>

          <!-- Hover Action -->
          <div class="card-footer">
            <span class="text-primary-600 font-medium text-sm group-hover:text-primary-700 w-full text-center">
              View Profile →
            </span>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <svg class="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
        </svg>
        <h3 class="text-lg font-medium text-gray-900 mb-2">No architects found</h3>
        <p class="text-gray-600 mb-6">
          {{ hasFilters ? 'Try adjusting your search criteria.' : 'No architects are currently available.' }}
        </p>
        <button
          v-if="hasFilters"
          @click="clearFilters"
          class="btn btn-outline"
        >
          Clear All Filters
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ArchitectList',
  data() {
    return {
      isLoading: false,
      architects: [
        {
          id: 1,
          name: 'Sarah Johnson',
          title: 'Senior Residential Architect',
          avatar: null,
          verified: true,
          rating: 5,
          reviewCount: 24,
          specializations: ['Residential', 'Sustainable Design'],
          location: 'San Francisco, CA',
          experience: 12,
          projectCount: 45
        },
        {
          id: 2,
          name: 'Michael Chen',
          title: 'Commercial Architecture Lead',
          avatar: null,
          verified: true,
          rating: 4,
          reviewCount: 18,
          specializations: ['Commercial', 'Industrial', 'Urban Planning'],
          location: 'New York, NY',
          experience: 15,
          projectCount: 32
        },
        {
          id: 3,
          name: 'Emily Rodriguez',
          title: 'Sustainable Design Specialist',
          avatar: null,
          verified: false,
          rating: 5,
          reviewCount: 12,
          specializations: ['Sustainable Design', 'Residential'],
          location: 'Austin, TX',
          experience: 8,
          projectCount: 28
        }
      ],
      filters: {
        search: '',
        specialization: '',
        experience: ''
      },
      sortBy: 'rating'
    }
  },
  computed: {
    filteredArchitects() {
      let filtered = [...this.architects]

      // Apply search filter
      if (this.filters.search) {
        const search = this.filters.search.toLowerCase()
        filtered = filtered.filter(architect =>
          architect.name.toLowerCase().includes(search) ||
          architect.title.toLowerCase().includes(search) ||
          architect.location.toLowerCase().includes(search) ||
          architect.specializations.some(spec => spec.toLowerCase().includes(search))
        )
      }

      // Apply specialization filter
      if (this.filters.specialization) {
        filtered = filtered.filter(architect =>
          architect.specializations.some(spec =>
            spec.toLowerCase().includes(this.filters.specialization.toLowerCase())
          )
        )
      }

      // Apply experience filter
      if (this.filters.experience) {
        const [min, max] = this.filters.experience.includes('+')
          ? [20, Infinity]
          : this.filters.experience.split('-').map(Number)
        filtered = filtered.filter(architect => {
          return architect.experience >= min && (max === undefined || architect.experience <= max)
        })
      }

      // Apply sorting
      filtered.sort((a, b) => {
        switch (this.sortBy) {
          case 'rating':
            return b.rating - a.rating || b.reviewCount - a.reviewCount
          case 'experience':
            return b.experience - a.experience
          case 'projects':
            return b.projectCount - a.projectCount
          case 'recent':
            return Math.random() - 0.5 // Random for demo
          default:
            return 0
        }
      })

      return filtered
    },
    hasFilters() {
      return this.filters.search || this.filters.specialization || this.filters.experience
    }
  },
  mounted() {
    this.loadArchitects()
  },
  methods: {
    async loadArchitects() {
      this.isLoading = true
      try {
        // TODO: Replace with actual API call
        await new Promise(resolve => setTimeout(resolve, 1000))
      } catch (error) {
        console.error('Failed to load architects:', error)
      } finally {
        this.isLoading = false
      }
    },
    clearFilters() {
      this.filters = {
        search: '',
        specialization: '',
        experience: ''
      }
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
  padding: 0.125rem 0.375rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
}

.badge-primary {
  background-color: rgb(219 234 254);
  color: rgb(30 64 175);
}

.card:hover {
  transform: translateY(-2px);
}

.group:hover .group-hover\:text-primary-600 {
  color: rgb(37 99 235);
}

.group:hover .group-hover\:text-primary-700 {
  color: rgb(29 78 216);
}
</style>
