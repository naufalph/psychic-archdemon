<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <!-- Navigation Header -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo and Brand -->
          <div class="flex items-center">
            <router-link to="/" class="flex-shrink-0 flex items-center">
              <svg class="w-8 h-8 text-primary-600" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L2 7L12 12L22 7L12 2Z"/>
                <path d="M2 17L12 22L22 17"/>
                <path d="M2 12L12 17L22 12"/>
              </svg>
              <span class="ml-2 text-xl font-bold text-gray-900">ArchMatch</span>
            </router-link>
          </div>

          <!-- Navigation Links -->
          <div class="hidden md:block">
            <div class="ml-10 flex items-baseline space-x-4">
              <router-link
                to="/"
                class="nav-link"
                :class="{ 'active': $route.name === 'Home' }"
              >
                Home
              </router-link>
              <router-link
                to="/projects"
                class="nav-link"
                :class="{ 'active': $route.name === 'Projects' }"
              >
                Projects
              </router-link>
              <router-link
                to="/architects"
                class="nav-link"
                :class="{ 'active': $route.name === 'Architects' }"
              >
                Architects
              </router-link>
              <router-link
                to="/about"
                class="nav-link"
                :class="{ 'active': $route.name === 'About' }"
              >
                About
              </router-link>
            </div>
          </div>

          <!-- User Menu -->
          <div class="flex items-center space-x-4">
            <!-- Notifications -->
            <button
              type="button"
              class="p-1 rounded-full text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              @click="showNotifications = !showNotifications"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-5-5V9.5a6.5 6.5 0 1 0-13 0V12l-5 5h5m0 0v1a3 3 0 1 0 6 0v-1m-6 0h6"/>
              </svg>
            </button>

            <!-- User Authentication -->
            <div v-if="!isAuthenticated" class="flex items-center space-x-2">
              <router-link to="/login" class="btn btn-outline btn-sm">
                Sign In
              </router-link>
              <router-link to="/register" class="btn btn-primary btn-sm">
                Sign Up
              </router-link>
            </div>

            <!-- User Profile Dropdown -->
            <div v-else class="relative">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                <img
                  class="w-8 h-8 rounded-full"
                  :src="user.avatar || '/default-avatar.jpg'"
                  :alt="user.name"
                >
              </button>

              <!-- Dropdown menu -->
              <div
                v-if="showUserMenu"
                class="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 focus:outline-none"
                role="menu"
              >
                <div class="py-1" role="none">
                  <router-link
                    to="/profile"
                    class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    @click="showUserMenu = false"
                  >
                    Your Profile
                  </router-link>
                  <router-link
                    to="/dashboard"
                    class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    @click="showUserMenu = false"
                  >
                    Dashboard
                  </router-link>
                  <button
                    @click="handleLogout"
                    class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  >
                    Sign Out
                  </button>
                </div>
              </div>
            </div>

            <!-- Mobile menu button -->
            <button
              @click="showMobileMenu = !showMobileMenu"
              class="md:hidden inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Mobile Navigation Menu -->
        <div v-if="showMobileMenu" class="md:hidden">
          <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3 border-t border-gray-200">
            <router-link
              to="/"
              class="nav-link block"
              :class="{ 'active': $route.name === 'Home' }"
              @click="showMobileMenu = false"
            >
              Home
            </router-link>
            <router-link
              to="/projects"
              class="nav-link block"
              :class="{ 'active': $route.name === 'Projects' }"
              @click="showMobileMenu = false"
            >
              Projects
            </router-link>
            <router-link
              to="/architects"
              class="nav-link block"
              :class="{ 'active': $route.name === 'Architects' }"
              @click="showMobileMenu = false"
            >
              Architects
            </router-link>
            <router-link
              to="/about"
              class="nav-link block"
              :class="{ 'active': $route.name === 'About' }"
              @click="showMobileMenu = false"
            >
              About
            </router-link>
          </div>
        </div>
      </div>
    </nav>

    <!-- Loading Indicator -->
    <div v-if="isLoading" class="fixed top-0 left-0 w-full h-1 bg-primary-200 z-50">
      <div class="h-full bg-primary-600 animate-pulse"></div>
    </div>

    <!-- Main Content -->
    <main class="flex-1">
      <router-view v-slot="{ Component }">
        <transition
          name="fade"
          mode="out-in"
          enter-active-class="animate-fade-in"
          leave-active-class="animate-fade-in"
        >
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200">
      <div class="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div class="col-span-1 md:col-span-2">
            <div class="flex items-center">
              <svg class="w-8 h-8 text-primary-600" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L2 7L12 12L22 7L12 2Z"/>
                <path d="M2 17L12 22L22 17"/>
                <path d="M2 12L12 17L22 12"/>
              </svg>
              <span class="ml-2 text-xl font-bold text-gray-900">ArchMatch</span>
            </div>
            <p class="mt-4 text-gray-600 text-sm max-w-md">
              Connecting talented architects with clients to create amazing spaces and buildings.
            </p>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-gray-900 tracking-wider uppercase">Company</h3>
            <ul class="mt-4 space-y-4">
              <li><a href="#" class="text-sm text-gray-600 hover:text-gray-900">About</a></li>
              <li><a href="#" class="text-sm text-gray-600 hover:text-gray-900">Careers</a></li>
              <li><a href="#" class="text-sm text-gray-600 hover:text-gray-900">Contact</a></li>
            </ul>
          </div>

          <div>
            <h3 class="text-sm font-semibold text-gray-900 tracking-wider uppercase">Support</h3>
            <ul class="mt-4 space-y-4">
              <li><a href="#" class="text-sm text-gray-600 hover:text-gray-900">Help Center</a></li>
              <li><a href="#" class="text-sm text-gray-600 hover:text-gray-900">Terms of Service</a></li>
              <li><a href="#" class="text-sm text-gray-600 hover:text-gray-900">Privacy Policy</a></li>
            </ul>
          </div>
        </div>

        <div class="mt-8 border-t border-gray-200 pt-8">
          <p class="text-sm text-gray-600 text-center">
            © {{ new Date().getFullYear() }} ArchMatch. All rights reserved.
          </p>
        </div>
      </div>
    </footer>

    <!-- Global Notifications -->
    <div
      v-if="showNotifications"
      class="fixed inset-0 z-50 overflow-hidden"
      @click.self="showNotifications = false"
    >
      <div class="absolute inset-0 bg-black bg-opacity-25" />
      <div class="fixed inset-y-0 right-0 pl-10 max-w-full flex">
        <div class="w-screen max-w-md">
          <div class="h-full flex flex-col bg-white shadow-xl">
            <div class="flex-1 py-6 overflow-y-auto px-4 sm:px-6">
              <div class="flex items-start justify-between">
                <h2 class="text-lg font-medium text-gray-900">Notifications</h2>
                <button
                  @click="showNotifications = false"
                  class="ml-3 h-7 w-7 flex items-center justify-center text-gray-400 hover:text-gray-500"
                >
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                  </svg>
                </button>
              </div>

              <div class="mt-8">
                <p class="text-sm text-gray-500">No new notifications</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useAuthStore } from './stores/auth'

export default {
  name: 'App',
  data() {
    return {
      showMobileMenu: false,
      showUserMenu: false,
      showNotifications: false,
      isLoading: false
    }
  },
  computed: {
    ...mapState(useAuthStore, ['isAuthenticated', 'user'])
  },
  methods: {
    ...mapActions(useAuthStore, ['logout']),

    async handleLogout() {
      try {
        this.isLoading = true
        await this.logout()
        this.showUserMenu = false
        this.$router.push('/')
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.isLoading = false
      }
    },

    // Close dropdowns when clicking outside
    handleClickOutside(event) {
      const userMenu = this.$el.querySelector('.relative')
      if (userMenu && !userMenu.contains(event.target)) {
        this.showUserMenu = false
      }
    }
  },
  mounted() {
    // Close mobile menu on route change
    this.$router.afterEach(() => {
      this.showMobileMenu = false
    })

    // Add click outside listener
    document.addEventListener('click', this.handleClickOutside)

    // Listen to route changes for loading indicator
    this.$router.beforeEach((to, from, next) => {
      this.isLoading = true
      next()
    })

    this.$router.afterEach(() => {
      this.isLoading = false
    })
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside)
  }
}
</script>

<style scoped>
/* Transition styles */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* Custom animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}
</style>
