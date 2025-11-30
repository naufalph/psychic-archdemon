<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="bg-gradient-to-br from-primary-600 to-primary-800 text-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <h1 class="text-4xl md:text-6xl font-bold mb-6 text-balance">
              Connect with Talented
              <span class="text-primary-200">Architects</span>
            </h1>
            <p class="text-xl text-primary-100 mb-8 leading-relaxed">
              Find the perfect architect for your project or discover exciting opportunities as an architect. Our
              platform connects clients with skilled professionals to create amazing spaces.
            </p>
            <div class="flex flex-col sm:flex-row gap-4">
              <router-link
                to="/projects"
                class="btn btn-primary bg-white text-primary-600 hover:bg-primary-50 font-semibold px-8 py-3 text-lg"
              >
                Browse Projects
              </router-link>
              <router-link
                to="/architects"
                class="btn btn-outline border-white text-white hover:bg-white hover:text-primary-600 font-semibold px-8 py-3 text-lg"
              >
                Find Architects
              </router-link>
            </div>
          </div>
          <div class="hidden lg:block">
            <div class="relative">
              <div
                class="absolute inset-0 bg-primary-400 rounded-full blur-3xl opacity-20 transform rotate-6 scale-110"
              ></div>
              <svg class="w-full h-96 relative z-10" viewBox="0 0 400 300" fill="none">
                <!-- Building illustration -->
                <rect x="50" y="150" width="80" height="120" fill="currentColor" class="text-white" opacity="0.9" />
                <rect
                  x="140"
                  y="120"
                  width="60"
                  height="150"
                  fill="currentColor"
                  class="text-primary-200"
                  opacity="0.8"
                />
                <rect x="210" y="100" width="90" height="170" fill="currentColor" class="text-white" opacity="0.9" />
                <rect
                  x="310"
                  y="140"
                  width="50"
                  height="130"
                  fill="currentColor"
                  class="text-primary-200"
                  opacity="0.8"
                />

                <!-- Windows -->
                <rect x="60" y="160" width="15" height="15" fill="currentColor" class="text-primary-600" />
                <rect x="85" y="160" width="15" height="15" fill="currentColor" class="text-primary-600" />
                <rect x="60" y="185" width="15" height="15" fill="currentColor" class="text-primary-600" />
                <rect x="85" y="185" width="15" height="15" fill="currentColor" class="text-primary-600" />

                <rect x="150" y="135" width="12" height="12" fill="currentColor" class="text-primary-600" />
                <rect x="170" y="135" width="12" height="12" fill="currentColor" class="text-primary-600" />
                <rect x="150" y="155" width="12" height="12" fill="currentColor" class="text-primary-600" />
                <rect x="170" y="155" width="12" height="12" fill="currentColor" class="text-primary-600" />

                <rect x="225" y="115" width="18" height="18" fill="currentColor" class="text-primary-600" />
                <rect x="255" y="115" width="18" height="18" fill="currentColor" class="text-primary-600" />
                <rect x="225" y="145" width="18" height="18" fill="currentColor" class="text-primary-600" />
                <rect x="255" y="145" width="18" height="18" fill="currentColor" class="text-primary-600" />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">How Rumantra Works</h2>
          <p class="text-xl text-gray-600 max-w-3xl mx-auto">
            Our platform makes it easy to connect clients with architects through a streamlined process
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-12">
          <!-- For Clients -->
          <div class="text-center">
            <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg class="w-8 h-8 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 mb-4">Post Your Project</h3>
            <p class="text-gray-600 leading-relaxed">
              Describe your project, set your budget, and specify your requirements. Our platform helps you create
              detailed project briefs.
            </p>
          </div>

          <!-- For Matching -->
          <div class="text-center">
            <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg class="w-8 h-8 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 mb-4">Receive Proposals</h3>
            <p class="text-gray-600 leading-relaxed">
              Qualified architects submit proposals with their portfolios, timelines, and pricing. Review and compare
              multiple options.
            </p>
          </div>

          <!-- For Architects -->
          <div class="text-center">
            <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg class="w-8 h-8 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
                />
              </svg>
            </div>
            <h3 class="text-xl font-semibold text-gray-900 mb-4">Start Creating</h3>
            <p class="text-gray-600 leading-relaxed">
              Collaborate with your chosen architect to bring your vision to life. Track progress and communicate
              through our platform.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="py-16 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8 text-center">
          <div v-for="stat in stats" :key="stat.label" class="animate-fade-in">
            <div class="text-3xl md:text-4xl font-bold text-primary-600 mb-2">
              {{ stat.value }}
            </div>
            <div class="text-gray-600 font-medium">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-24 bg-primary-600 text-white">
      <div class="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl md:text-4xl font-bold mb-6">Ready to Start Your Next Project?</h2>
        <p class="text-xl text-primary-100 mb-8 leading-relaxed">
          Join thousands of clients and architects who have found success through our platform
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link
            v-if="!isAuthenticated"
            to="/register"
            class="btn bg-white text-primary-600 hover:bg-primary-50 font-semibold px-8 py-3 text-lg"
          >
            Get Started Today
          </router-link>
          <router-link
            v-else
            to="/dashboard"
            class="btn bg-white text-primary-600 hover:bg-primary-50 font-semibold px-8 py-3 text-lg"
          >
            Go to Dashboard
          </router-link>
          <router-link
            to="/about"
            class="btn btn-outline border-white text-white hover:bg-white hover:text-primary-600 font-semibold px-8 py-3 text-lg"
          >
            Learn More
          </router-link>
        </div>
      </div>
    </section>

    <!-- Recent Projects Preview -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-gray-900 mb-4">Recent Projects</h2>
          <p class="text-xl text-gray-600">Discover amazing architectural projects from our community</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div
            v-for="project in featuredProjects"
            :key="project.id"
            class="card hover:shadow-card-hover transition-shadow cursor-pointer group"
            @click="$router.push(`/projects/${project.id}`)"
          >
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
            <div class="card-body">
              <h3 class="text-lg font-semibold text-gray-900 mb-2 group-hover:text-primary-600 transition-colors">
                {{ project.title }}
              </h3>
              <p class="text-gray-600 text-sm mb-4 line-clamp-2">
                {{ project.description }}
              </p>
              <div class="flex items-center justify-between">
                <span class="badge badge-primary">{{ project.category }}</span>
                <span class="text-sm font-medium text-gray-900">${{ project.budget?.toLocaleString() }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="text-center mt-12">
          <router-link to="/projects" class="btn btn-primary btn-lg"> View All Projects </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { mapState } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { projectAPI } from '@/services/api'

export default {
  name: 'Home',
  data() {
    return {
      stats: [
        { label: 'Active Projects', value: '150+' },
        { label: 'Architects', value: '300+' },
        { label: 'Completed Projects', value: '500+' },
        { label: 'Happy Clients', value: '1000+' }
      ],
      featuredProjects: [],
      isLoading: false
    }
  },
  computed: {
    ...mapState(useAuthStore, ['isAuthenticated'])
  },
  async mounted() {
    await this.loadFeaturedProjects()
  },
  methods: {
    async loadFeaturedProjects() {
      this.isLoading = true
      try {
        const response = await projectAPI.getAll({
          featured: true,
          limit: 3,
          status: 'ACTIVE'
        })
        this.featuredProjects = response.data.content || []
      } catch (error) {
        console.error('Failed to load featured projects:', error)
        // Fallback to mock data for demo purposes
        this.featuredProjects = [
          {
            id: 1,
            title: 'Modern Family Home',
            description:
              'A contemporary 3-bedroom family home with sustainable design principles and open-plan living spaces.',
            category: 'Residential',
            budget: 250000,
            image: null
          },
          {
            id: 2,
            title: 'Office Complex Renovation',
            description:
              'Transform an outdated office building into a modern workspace with collaborative areas and natural lighting.',
            category: 'Commercial',
            budget: 500000,
            image: null
          },
          {
            id: 3,
            title: 'Community Center Design',
            description:
              'Design a multi-purpose community center that serves as a hub for local activities and events.',
            category: 'Public',
            budget: 750000,
            image: null
          }
        ]
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
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

/* Animation for stats on scroll */
@media (prefers-reduced-motion: no-preference) {
  .animate-fade-in {
    opacity: 0;
    animation: fadeInUp 0.6s ease-out forwards;
  }

  .animate-fade-in:nth-child(1) {
    animation-delay: 0.1s;
  }
  .animate-fade-in:nth-child(2) {
    animation-delay: 0.2s;
  }
  .animate-fade-in:nth-child(3) {
    animation-delay: 0.3s;
  }
  .animate-fade-in:nth-child(4) {
    animation-delay: 0.4s;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
