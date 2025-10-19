<template>
  <div class="architect-profile">
    <!-- Loading State -->
    <div v-if="isLoading" class="min-h-screen flex items-center justify-center">
      <div class="spinner w-8 h-8"></div>
      <span class="ml-2 text-gray-600">Loading profile...</span>
    </div>

    <!-- Profile Content -->
    <div v-else-if="architect" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Profile Header -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-8 mb-8">
        <div class="flex flex-col lg:flex-row lg:items-start lg:space-x-8">
          <div class="flex-shrink-0 text-center lg:text-left">
            <div class="relative inline-block">
              <div class="w-32 h-32 bg-gray-200 rounded-full overflow-hidden">
                <img
                  v-if="architect.avatar"
                  :src="architect.avatar"
                  :alt="architect.name"
                  class="w-full h-full object-cover"
                />
                <div
                  v-else
                  class="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-100 to-primary-200"
                >
                  <svg class="w-16 h-16 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    />
                  </svg>
                </div>
              </div>
              <div v-if="architect.verified" class="absolute -top-2 -right-2">
                <div class="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path
                      fill-rule="evenodd"
                      d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <div class="flex-1 mt-6 lg:mt-0">
            <div class="text-center lg:text-left">
              <h1 class="text-3xl font-bold text-gray-900">{{ architect.name }}</h1>
              <p class="text-xl text-primary-600 font-medium mt-1">{{ architect.title }}</p>

              <!-- Rating -->
              <div class="flex items-center justify-center lg:justify-start mt-3">
                <div class="flex items-center">
                  <svg
                    v-for="i in 5"
                    :key="i"
                    class="w-5 h-5"
                    :class="i <= architect.rating ? 'text-yellow-400' : 'text-gray-300'"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                    />
                  </svg>
                </div>
                <span class="ml-2 text-sm text-gray-600"
                  >{{ architect.rating }}/5 ({{ architect.reviewCount }} reviews)</span
                >
              </div>

              <!-- Info Tags -->
              <div class="flex flex-wrap justify-center lg:justify-start gap-4 mt-4 text-sm text-gray-600">
                <div class="flex items-center">
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
                  {{ architect.location }}
                </div>
                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2-2v2m8 0V6a2 2 0 012 2v6a2 2 0 01-2 2H6a2 2 0 01-2-2V8a2 2 0 012-2V6"
                    />
                  </svg>
                  {{ architect.experience }} years experience
                </div>
                <div class="flex items-center">
                  <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                    />
                  </svg>
                  {{ architect.projectCount }} projects completed
                </div>
              </div>

              <!-- Specializations -->
              <div class="flex flex-wrap justify-center lg:justify-start gap-2 mt-6">
                <span v-for="spec in architect.specializations" :key="spec" class="badge badge-primary">
                  {{ spec }}
                </span>
              </div>
            </div>
          </div>

          <!-- Contact Actions -->
          <div class="flex-shrink-0 mt-8 lg:mt-0">
            <div class="space-y-3">
              <button class="btn btn-primary w-full lg:w-auto">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
                  />
                </svg>
                Contact
              </button>
              <button class="btn btn-outline w-full lg:w-auto">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                  />
                </svg>
                Save
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile Tabs -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200">
        <div class="border-b border-gray-200">
          <nav class="flex space-x-8 px-6">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              class="py-4 px-1 border-b-2 font-medium text-sm"
              :class="
                activeTab === tab.id
                  ? 'border-primary-500 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              "
            >
              {{ tab.name }}
            </button>
          </nav>
        </div>

        <div class="p-6">
          <!-- About Tab -->
          <div v-if="activeTab === 'about'">
            <div class="prose max-w-none">
              <h3>About</h3>
              <p>{{ architect.bio }}</p>

              <h3>Education</h3>
              <ul>
                <li v-for="edu in architect.education" :key="edu">{{ edu }}</li>
              </ul>

              <h3>Certifications</h3>
              <ul>
                <li v-for="cert in architect.certifications" :key="cert">{{ cert }}</li>
              </ul>
            </div>
          </div>

          <!-- Portfolio Tab -->
          <div v-if="activeTab === 'portfolio'">
            <div v-if="architect.portfolio.length === 0" class="text-center py-12">
              <svg class="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              <h3 class="text-lg font-medium text-gray-900 mb-2">No portfolio items</h3>
              <p class="text-gray-600">This architect hasn't uploaded their portfolio yet.</p>
            </div>

            <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <div
                v-for="item in architect.portfolio"
                :key="item.id"
                class="card hover:shadow-card-hover transition-shadow"
              >
                <div class="aspect-video bg-gray-200 rounded-t-lg">
                  <!-- Portfolio image would go here -->
                </div>
                <div class="card-body">
                  <h4 class="font-semibold text-gray-900">{{ item.title }}</h4>
                  <p class="text-sm text-gray-600 mt-1">{{ item.category }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Reviews Tab -->
          <div v-if="activeTab === 'reviews'">
            <div v-if="architect.reviews.length === 0" class="text-center py-12">
              <svg class="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
                />
              </svg>
              <h3 class="text-lg font-medium text-gray-900 mb-2">No reviews yet</h3>
              <p class="text-gray-600">This architect hasn't received any reviews.</p>
            </div>

            <div v-else class="space-y-6">
              <div v-for="review in architect.reviews" :key="review.id" class="border border-gray-200 rounded-lg p-6">
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <div class="flex items-center space-x-2">
                      <h4 class="font-semibold text-gray-900">{{ review.clientName }}</h4>
                      <div class="flex items-center">
                        <svg
                          v-for="i in 5"
                          :key="i"
                          class="w-4 h-4"
                          :class="i <= review.rating ? 'text-yellow-400' : 'text-gray-300'"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                        >
                          <path
                            d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                          />
                        </svg>
                      </div>
                    </div>
                    <p class="text-gray-600 mt-2">{{ review.comment }}</p>
                    <p class="text-sm text-gray-500 mt-2">{{ formatDate(review.createdAt) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Profile Not Found -->
    <div v-else class="min-h-screen flex items-center justify-center">
      <div class="text-center">
        <h2 class="text-2xl font-bold text-gray-900 mb-4">Architect not found</h2>
        <p class="text-gray-600 mb-6">The architect you're looking for doesn't exist or has been removed.</p>
        <router-link to="/architects" class="btn btn-primary"> Back to Architects </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ArchitectProfile',
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      isLoading: true,
      architect: null,
      activeTab: 'about',
      tabs: [
        { id: 'about', name: 'About' },
        { id: 'portfolio', name: 'Portfolio' },
        { id: 'reviews', name: 'Reviews' }
      ]
    }
  },
  async mounted() {
    await this.loadArchitect()
  },
  methods: {
    async loadArchitect() {
      this.isLoading = true
      try {
        // TODO: Replace with actual API call
        // Mock architect data
        this.architect = {
          id: this.id,
          name: 'Sarah Johnson',
          title: 'Senior Residential Architect',
          avatar: null,
          verified: true,
          rating: 5,
          reviewCount: 24,
          specializations: ['Residential', 'Sustainable Design', 'Modern Architecture'],
          location: 'San Francisco, CA',
          experience: 12,
          projectCount: 45,
          bio: "I am a passionate architect with over 12 years of experience in residential design and sustainable architecture. I believe in creating spaces that are not only beautiful but also environmentally responsible and perfectly suited to my clients' needs.",
          education: [
            'Master of Architecture, UC Berkeley (2012)',
            'Bachelor of Environmental Design, UC Davis (2010)'
          ],
          certifications: ['LEED Accredited Professional', 'California Registered Architect', 'NCARB Certificate'],
          portfolio: [],
          reviews: [
            {
              id: 1,
              clientName: 'John Smith',
              rating: 5,
              comment:
                'Sarah designed our dream home perfectly. Her attention to detail and sustainable design approach exceeded our expectations.',
              createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
            }
          ]
        }

        await new Promise(resolve => setTimeout(resolve, 1000)) // Simulate API call
      } catch (error) {
        console.error('Failed to load architect:', error)
      } finally {
        this.isLoading = false
      }
    },
    formatDate(date) {
      if (!date) return 'Unknown'
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
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
}

.badge-primary {
  background-color: rgb(219 234 254);
  color: rgb(30 64 175);
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

.prose ul {
  margin-bottom: 1rem;
  padding-left: 1.5rem;
}

.prose li {
  margin-bottom: 0.25rem;
  color: rgb(75 85 99);
}
</style>
