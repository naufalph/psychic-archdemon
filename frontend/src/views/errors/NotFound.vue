<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="text-center">
        <!-- 404 Illustration -->
        <div class="mx-auto w-32 h-32 mb-8">
          <svg class="w-full h-full text-primary-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1"
              d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
        </div>

        <!-- Error Code -->
        <div class="mb-8">
          <h1 class="text-9xl font-bold text-primary-600 tracking-tight">404</h1>
        </div>

        <!-- Error Message -->
        <div class="mb-8">
          <h2 class="text-3xl font-bold text-gray-900 mb-4">Page not found</h2>
          <p class="text-lg text-gray-600 mb-6 max-w-md mx-auto">
            Sorry, we couldn't find the page you're looking for. The page might have been moved, deleted, or never
            existed.
          </p>
        </div>

        <!-- Action Buttons -->
        <div class="flex flex-col sm:flex-row gap-4 justify-center mb-8">
          <router-link to="/" class="btn btn-primary px-8 py-3">
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
              />
            </svg>
            Go Home
          </router-link>
          <button @click="goBack" class="btn btn-outline px-8 py-3">
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Go Back
          </button>
        </div>

        <!-- Helpful Links -->
        <div class="border-t border-gray-200 pt-8">
          <p class="text-sm text-gray-500 mb-4">Looking for something specific?</p>
          <div class="flex flex-col sm:flex-row gap-6 justify-center text-sm">
            <router-link to="/projects" class="text-primary-600 hover:text-primary-500 font-medium">
              Browse Projects
            </router-link>
            <router-link to="/architects" class="text-primary-600 hover:text-primary-500 font-medium">
              Find Architects
            </router-link>
            <router-link to="/about" class="text-primary-600 hover:text-primary-500 font-medium">
              About Us
            </router-link>
            <a href="mailto:support@rumantra.com" class="text-primary-600 hover:text-primary-500 font-medium">
              Contact Support
            </a>
          </div>
        </div>

        <!-- Search Box -->
        <div class="mt-8 max-w-md mx-auto">
          <div class="relative">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search for projects, architects..."
              class="form-input pl-10 pr-4 py-3 w-full"
              @keyup.enter="handleSearch"
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
            <button @click="handleSearch" class="absolute inset-y-0 right-0 pr-3 flex items-center">
              <span class="sr-only">Search</span>
              <svg
                class="h-5 w-5 text-gray-400 hover:text-gray-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5-5 5M6 7l5 5-5 5" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer with additional help -->
    <div class="mt-16 text-center">
      <div class="max-w-md mx-auto px-4">
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div class="flex items-center justify-center mb-4">
            <svg class="w-8 h-8 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mb-2">Need Help?</h3>
          <p class="text-sm text-gray-600 mb-4">
            If you believe this is an error or need assistance, our support team is here to help.
          </p>
          <div class="flex flex-col gap-2">
            <a
              href="mailto:support@rumantra.com"
              class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium text-primary-600 bg-primary-50 rounded-md hover:bg-primary-100 transition-colors"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                />
              </svg>
              Email Support
            </a>
            <p class="text-xs text-gray-500">We typically respond within 24 hours</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  data() {
    return {
      searchQuery: ''
    }
  },
  mounted() {
    // Set the document title
    document.title = '404 - Page Not Found | Rumantra'

    // Log the 404 for analytics (if enabled)
    if (import.meta.env.VITE_ENABLE_ANALYTICS === 'true') {
      this.logPageNotFound()
    }
  },
  methods: {
    goBack() {
      // Check if there's history to go back to
      if (window.history.length > 1) {
        this.$router.go(-1)
      } else {
        // If no history, go to home page
        this.$router.push('/')
      }
    },

    handleSearch() {
      if (this.searchQuery.trim()) {
        // Navigate to search results page with query
        this.$router
          .push({
            name: 'Search',
            query: { q: this.searchQuery.trim() }
          })
          .catch(() => {
            // If search route doesn't exist, go to projects with search
            this.$router.push({
              name: 'Projects',
              query: { search: this.searchQuery.trim() }
            })
          })
      }
    },

    logPageNotFound() {
      // Log 404 errors for analytics
      const errorData = {
        path: this.$route.fullPath,
        referrer: document.referrer,
        userAgent: navigator.userAgent,
        timestamp: new Date().toISOString()
      }

      // In a real application, you would send this to your analytics service
      console.log('404 Error logged:', errorData)

      // Example: Send to Google Analytics
      if (typeof gtag !== 'undefined') {
        gtag('event', 'page_not_found', {
          page_path: errorData.path,
          page_referrer: errorData.referrer
        })
      }

      // Example: Send to custom analytics endpoint
      if (import.meta.env.VITE_API_BASE_URL) {
        fetch(`${import.meta.env.VITE_API_BASE_URL}/analytics/404`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(errorData)
        }).catch(error => {
          console.warn('Failed to log 404 error:', error)
        })
      }
    }
  }
}
</script>

<style scoped>
/* Animation for the 404 number */
@keyframes bounce {
  0%,
  20%,
  53%,
  80%,
  100% {
    animation-timing-function: cubic-bezier(0.215, 0.61, 0.355, 1);
    transform: translate3d(0, 0, 0);
  }
  40%,
  43% {
    animation-timing-function: cubic-bezier(0.755, 0.05, 0.855, 0.06);
    transform: translate3d(0, -10px, 0);
  }
  70% {
    animation-timing-function: cubic-bezier(0.755, 0.05, 0.855, 0.06);
    transform: translate3d(0, -5px, 0);
  }
  90% {
    transform: translate3d(0, -2px, 0);
  }
}

.animate-bounce-slow {
  animation: bounce 2s infinite;
}

/* Hover effects for buttons */
.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

/* Focus styles for accessibility */
.form-input:focus {
  ring: 2px;
  ring-color: #3b82f6;
  ring-offset: 2px;
  outline: none;
  border-color: #3b82f6;
}

/* Custom styles for links */
a:hover {
  transition: color 0.2s ease;
}

/* Responsive adjustments */
@media (max-width: 640px) {
  h1 {
    font-size: 6rem;
  }

  h2 {
    font-size: 1.875rem;
  }

  .btn {
    width: 100%;
  }
}

/* Loading animation for the SVG */
@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.animate-fade-in-scale {
  animation: fadeInScale 0.6s ease-out;
}

/* Subtle animations on page load */
.animate-slide-up {
  animation: slideUp 0.8s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Style for helpful links section */
.helpful-links a {
  position: relative;
  transition: all 0.2s ease;
}

.helpful-links a:hover {
  transform: translateY(-1px);
}

.helpful-links a::after {
  content: '';
  position: absolute;
  width: 0;
  height: 2px;
  bottom: -2px;
  left: 50%;
  background-color: currentColor;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.helpful-links a:hover::after {
  width: 100%;
}

/* Search box enhancements */
.search-container {
  position: relative;
}

.search-input:focus + .search-icon {
  color: #3b82f6;
}

/* Support box styling */
.support-box {
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.95);
}

/* Error illustration animation */
.error-illustration {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
  100% {
    transform: translateY(0px);
  }
}
</style>
