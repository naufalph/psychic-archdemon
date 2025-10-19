<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <div class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <svg class="w-8 h-8 text-primary-600" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" />
                <path d="M2 17L12 22L22 17" />
                <path d="M2 12L12 17L22 12" />
              </svg>
            </div>
            <div class="ml-4">
              <h1 class="text-2xl font-bold text-gray-900">Client Dashboard</h1>
              <p class="text-sm text-gray-500">Kelola proyek arsitektur Anda</p>
            </div>
          </div>

          <!-- User Actions -->
          <div class="flex items-center space-x-4">
            <button v-if="!isAuthenticated" @click="openSignInPopup('signin')" class="btn btn-outline btn-sm">
              Masuk
            </button>
            <button v-if="!isAuthenticated" @click="openSignInPopup('signup')" class="btn btn-primary btn-sm">
              Daftar
            </button>
            <div v-else class="flex items-center space-x-3">
              <span class="text-sm text-gray-700">Halo, {{ userName }}!</span>
              <button @click="handleLogout" class="btn btn-outline btn-sm">Keluar</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Welcome Section -->
      <div class="bg-white rounded-lg shadow-sm p-6 mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-xl font-semibold text-gray-900 mb-2">Selamat Datang di Rumantra</h2>
            <p class="text-gray-600">Platform terbaik untuk menemukan arsitek profesional untuk proyek impian Anda</p>
          </div>
          <div class="hidden md:block">
            <svg class="w-24 h-24 text-primary-200" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" />
              <path d="M2 17L12 22L22 17" />
              <path d="M2 12L12 17L22 12" />
            </svg>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- Create Project -->
        <div class="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                <svg class="w-6 h-6 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                  />
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <h3 class="text-lg font-medium text-gray-900">Buat Proyek</h3>
              <p class="text-sm text-gray-500">Mulai proyek baru</p>
            </div>
          </div>
          <div class="mt-4">
            <button @click="handleCreateProject" class="w-full btn btn-primary btn-sm">Buat Proyek Baru</button>
          </div>
        </div>

        <!-- Find Architects -->
        <div class="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <h3 class="text-lg font-medium text-gray-900">Cari Arsitek</h3>
              <p class="text-sm text-gray-500">Temukan arsitek terbaik</p>
            </div>
          </div>
          <div class="mt-4">
            <button @click="handleFindArchitects" class="w-full btn btn-outline btn-sm">Jelajahi Arsitek</button>
          </div>
        </div>

        <!-- My Projects -->
        <div class="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                  />
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <h3 class="text-lg font-medium text-gray-900">Proyek Saya</h3>
              <p class="text-sm text-gray-500">{{ myProjectsCount }} proyek aktif</p>
            </div>
          </div>
          <div class="mt-4">
            <button @click="handleViewProjects" class="w-full btn btn-outline btn-sm">Lihat Proyek</button>
          </div>
        </div>

        <!-- Messages -->
        <div class="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <div class="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
                  />
                </svg>
              </div>
            </div>
            <div class="ml-4">
              <h3 class="text-lg font-medium text-gray-900">Pesan</h3>
              <p class="text-sm text-gray-500">{{ unreadMessagesCount }} pesan baru</p>
            </div>
          </div>
          <div class="mt-4">
            <button @click="handleViewMessages" class="w-full btn btn-outline btn-sm">Buka Pesan</button>
          </div>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="bg-white rounded-lg shadow-sm">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">Aktivitas Terbaru</h3>
        </div>
        <div class="p-6">
          <div v-if="!isAuthenticated" class="text-center py-12">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
              />
            </svg>
            <h3 class="mt-2 text-sm font-medium text-gray-900">Masuk untuk melihat aktivitas</h3>
            <p class="mt-1 text-sm text-gray-500">Daftar atau masuk untuk melihat proyek dan aktivitas terbaru Anda</p>
            <div class="mt-6 flex justify-center space-x-3">
              <button @click="openSignInPopup('signin')" class="btn btn-outline">Masuk</button>
              <button @click="openSignInPopup('signup')" class="btn btn-primary">Daftar Sekarang</button>
            </div>
          </div>

          <div v-else-if="recentActivities.length === 0" class="text-center py-12">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
              />
            </svg>
            <h3 class="mt-2 text-sm font-medium text-gray-900">Belum ada aktivitas</h3>
            <p class="mt-1 text-sm text-gray-500">Mulai dengan membuat proyek pertama Anda</p>
            <div class="mt-6">
              <button @click="handleCreateProject" class="btn btn-primary">Buat Proyek Pertama</button>
            </div>
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="activity in recentActivities"
              :key="activity.id"
              class="flex items-start space-x-3 p-4 bg-gray-50 rounded-lg"
            >
              <div class="flex-shrink-0">
                <div class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-primary-600" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2L2 7L12 12L22 7L12 2Z" />
                  </svg>
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900">{{ activity.title }}</p>
                <p class="text-sm text-gray-500">{{ activity.description }}</p>
                <p class="text-xs text-gray-400 mt-1">{{ activity.time }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Sign In Popup -->
    <SignInPopup
      :visible="showSignInPopup"
      :initial-view="signInView"
      @close="closeSignInPopup"
      @success="handleAuthSuccess"
    />
  </div>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import SignInPopup from '@/components/auth/SignInPopup.vue'

export default {
  name: 'ClientDashboard',
  components: {
    SignInPopup
  },
  data() {
    return {
      showSignInPopup: false,
      signInView: 'signup', // 'signup', 'signin', 'emailSignup'
      myProjectsCount: 0,
      unreadMessagesCount: 0,
      recentActivities: []
    }
  },
  computed: {
    isAuthenticated() {
      try {
        const authStore = useAuthStore()
        return authStore.isAuthenticated
      } catch (error) {
        console.warn('Auth store not available:', error)
        return false
      }
    },
    user() {
      try {
        const authStore = useAuthStore()
        return authStore.user
      } catch (error) {
        console.warn('Auth store not available:', error)
        return null
      }
    },
    userName() {
      try {
        const authStore = useAuthStore()
        return authStore.userName
      } catch (error) {
        console.warn('Auth store not available:', error)
        return ''
      }
    }
  },
  methods: {
    // Sign In Popup Methods
    openSignInPopup(view = 'signup') {
      this.signInView = view
      this.showSignInPopup = true
    },

    closeSignInPopup() {
      this.showSignInPopup = false
    },

    handleAuthSuccess(data) {
      console.log('Auth success:', data)

      // Refresh dashboard data after successful authentication
      this.loadDashboardData()

      // Show welcome message
      if (data.type === 'register') {
        this.$toast?.success('Selamat datang! Akun Anda berhasil dibuat.')
      } else {
        this.$toast?.success('Selamat datang kembali!')
      }
    },

    // Dashboard Action Methods
    handleCreateProject() {
      if (!this.isAuthenticated) {
        this.openSignInPopup('signup')
        return
      }

      // Navigate to create project page
      this.$router.push('/projects/create')
    },

    handleFindArchitects() {
      this.$router.push('/architects')
    },

    handleViewProjects() {
      if (!this.isAuthenticated) {
        this.openSignInPopup('signin')
        return
      }

      this.$router.push('/projects?filter=my-projects')
    },

    handleViewMessages() {
      if (!this.isAuthenticated) {
        this.openSignInPopup('signin')
        return
      }

      // Navigate to messages page (would need to be created)
      this.$router.push('/messages')
    },

    async handleLogout() {
      try {
        const authStore = useAuthStore()
        await authStore.logout()
        this.$router.push('/')
        this.$toast?.success('Anda telah keluar dari akun')
      } catch (error) {
        console.error('Logout error:', error)
        this.$toast?.error('Gagal keluar dari akun')
      }
    },

    // Data Loading Methods
    async loadDashboardData() {
      if (!this.isAuthenticated) {
        this.myProjectsCount = 0
        this.unreadMessagesCount = 0
        this.recentActivities = []
        return
      }

      try {
        // In a real app, these would be API calls
        // For demo purposes, we'll simulate the data
        await this.loadProjectsCount()
        await this.loadMessagesCount()
        await this.loadRecentActivities()
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      }
    },

    async loadProjectsCount() {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500))
      this.myProjectsCount = Math.floor(Math.random() * 5) + 1
    },

    async loadMessagesCount() {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 300))
      this.unreadMessagesCount = Math.floor(Math.random() * 10)
    },

    async loadRecentActivities() {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 400))

      const sampleActivities = [
        {
          id: 1,
          title: 'Proyek Rumah Minimalis',
          description: 'Mendapat 3 penawaran baru dari arsitek',
          time: '2 jam yang lalu'
        },
        {
          id: 2,
          title: 'Konsultasi dengan Arsitek',
          description: 'Sesi konsultasi dengan Arsitek John Doe selesai',
          time: '1 hari yang lalu'
        },
        {
          id: 3,
          title: 'Pembayaran Berhasil',
          description: 'Pembayaran untuk proyek villa berhasil diproses',
          time: '3 hari yang lalu'
        }
      ]

      this.recentActivities = sampleActivities.slice(0, Math.floor(Math.random() * 4))
    }
  },

  async mounted() {
    // Load dashboard data when component mounts
    await this.loadDashboardData()
  },

  watch: {
    // Watch for authentication changes and reload data
    isAuthenticated(newVal) {
      if (newVal) {
        this.loadDashboardData()
      } else {
        this.myProjectsCount = 0
        this.unreadMessagesCount = 0
        this.recentActivities = []
      }
    }
  }
}
</script>

<style scoped>
/* Additional component-specific styles */
.btn {
  transition: all 0.2s ease-in-out;
}

.btn:hover {
  transform: translateY(-1px);
}

/* Card hover effects */
.hover\:shadow-md:hover {
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

/* Loading animation for buttons */
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .grid-cols-1.md\:grid-cols-2.lg\:grid-cols-4 {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .grid-cols-1.md\:grid-cols-2.lg\:grid-cols-4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
