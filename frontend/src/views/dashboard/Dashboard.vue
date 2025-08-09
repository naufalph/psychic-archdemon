<template>
  <div class="dashboard">
    <!-- Dashboard Header -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-6">
          <div class="md:flex md:items-center md:justify-between">
            <div class="flex-1 min-w-0">
              <h1 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
                Welcome back, {{ userName }}!
              </h1>
              <p class="mt-1 text-sm text-gray-500">
                Here's what's happening with your projects today.
              </p>
            </div>
            <div class="mt-4 flex md:mt-0 md:ml-4">
              <router-link
                v-if="isClient"
                to="/projects/create"
                class="btn btn-primary"
              >
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                </svg>
                Post New Project
              </router-link>
              <button
                v-else-if="isArchitect"
                @click="showBrowseProjects"
                class="btn btn-primary"
              >
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                </svg>
                Browse Projects
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Loading State -->
      <div v-if="isLoading" class="flex justify-center items-center py-12">
        <div class="spinner w-8 h-8"></div>
        <span class="ml-2 text-gray-600">Loading dashboard...</span>
      </div>

      <!-- Dashboard Content -->
      <div v-else class="space-y-8">
        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div
            v-for="stat in stats"
            :key="stat.label"
            class="card hover:shadow-card-hover transition-shadow"
          >
            <div class="card-body">
              <div class="flex items-center">
                <div class="flex-shrink-0">
                  <div
                    class="w-8 h-8 rounded-md flex items-center justify-center"
                    :class="stat.iconBg"
                  >
                    <component :is="stat.icon" class="w-5 h-5" :class="stat.iconColor" />
                  </div>
                </div>
                <div class="ml-5 w-0 flex-1">
                  <dl>
                    <dt class="text-sm font-medium text-gray-500 truncate">{{ stat.label }}</dt>
                    <dd class="text-lg font-semibold text-gray-900">{{ stat.value }}</dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content Grid -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <!-- Recent Activity -->
          <div class="lg:col-span-2">
            <div class="card">
              <div class="card-header">
                <h3 class="text-lg leading-6 font-medium text-gray-900">Recent Activity</h3>
              </div>
              <div class="card-body p-0">
                <div v-if="recentActivity.length === 0" class="p-6 text-center text-gray-500">
                  No recent activity to display.
                </div>
                <ul v-else class="divide-y divide-gray-200">
                  <li
                    v-for="activity in recentActivity"
                    :key="activity.id"
                    class="px-6 py-4 hover:bg-gray-50 transition-colors"
                  >
                    <div class="flex items-center space-x-3">
                      <div class="flex-shrink-0">
                        <div
                          class="w-8 h-8 rounded-full flex items-center justify-center"
                          :class="getActivityIconBg(activity.type)"
                        >
                          <component
                            :is="getActivityIcon(activity.type)"
                            class="w-4 h-4"
                            :class="getActivityIconColor(activity.type)"
                          />
                        </div>
                      </div>
                      <div class="flex-1 min-w-0">
                        <p class="text-sm text-gray-900">{{ activity.message }}</p>
                        <p class="text-xs text-gray-500">{{ formatDate(activity.createdAt) }}</p>
                      </div>
                      <div v-if="activity.actionUrl" class="flex-shrink-0">
                        <router-link
                          :to="activity.actionUrl"
                          class="text-sm font-medium text-primary-600 hover:text-primary-500"
                        >
                          View
                        </router-link>
                      </div>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Quick Actions & Notifications -->
          <div class="space-y-6">
            <!-- Quick Actions -->
            <div class="card">
              <div class="card-header">
                <h3 class="text-lg leading-6 font-medium text-gray-900">Quick Actions</h3>
              </div>
              <div class="card-body space-y-3">
                <router-link
                  v-for="action in quickActions"
                  :key="action.label"
                  :to="action.to"
                  class="flex items-center p-3 text-sm font-medium rounded-md text-gray-700 hover:text-gray-900 hover:bg-gray-50 transition-colors"
                >
                  <component :is="action.icon" class="w-5 h-5 mr-3 text-gray-400" />
                  {{ action.label }}
                </router-link>
              </div>
            </div>

            <!-- Notifications -->
            <div class="card">
              <div class="card-header">
                <div class="flex items-center justify-between">
                  <h3 class="text-lg leading-6 font-medium text-gray-900">Notifications</h3>
                  <span v-if="notifications.length > 0" class="badge badge-primary">
                    {{ notifications.length }}
                  </span>
                </div>
              </div>
              <div class="card-body p-0">
                <div v-if="notifications.length === 0" class="p-6 text-center text-gray-500">
                  No new notifications.
                </div>
                <ul v-else class="divide-y divide-gray-200">
                  <li
                    v-for="notification in notifications.slice(0, 5)"
                    :key="notification.id"
                    class="px-6 py-4 hover:bg-gray-50 transition-colors"
                  >
                    <div class="flex items-start space-x-3">
                      <div class="flex-shrink-0">
                        <div
                          class="w-2 h-2 mt-2 rounded-full"
                          :class="notification.read ? 'bg-gray-300' : 'bg-primary-500'"
                        ></div>
                      </div>
                      <div class="flex-1 min-w-0">
                        <p class="text-sm text-gray-900" :class="{ 'font-semibold': !notification.read }">
                          {{ notification.message }}
                        </p>
                        <p class="text-xs text-gray-500 mt-1">{{ formatDate(notification.createdAt) }}</p>
                      </div>
                    </div>
                  </li>
                </ul>
                <div v-if="notifications.length > 5" class="px-6 py-3 bg-gray-50 border-t border-gray-200">
                  <router-link
                    to="/notifications"
                    class="text-sm font-medium text-primary-600 hover:text-primary-500"
                  >
                    View all notifications
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Projects Overview -->
        <div class="card">
          <div class="card-header">
            <div class="flex items-center justify-between">
              <h3 class="text-lg leading-6 font-medium text-gray-900">
                {{ isClient ? 'My Projects' : 'Recent Bids' }}
              </h3>
              <router-link
                :to="isClient ? '/projects' : '/bids'"
                class="text-sm font-medium text-primary-600 hover:text-primary-500"
              >
                View all
              </router-link>
            </div>
          </div>
          <div class="card-body p-0">
            <div v-if="projects.length === 0" class="p-6 text-center text-gray-500">
              {{ isClient ? 'No projects yet. Create your first project!' : 'No recent bids to display.' }}
            </div>
            <div v-else class="overflow-hidden">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      {{ isClient ? 'Project' : 'Project & Bid' }}
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      {{ isClient ? 'Bids' : 'Amount' }}
                    </th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Updated
                    </th>
                    <th class="relative px-6 py-3"><span class="sr-only">Actions</span></th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="project in projects.slice(0, 5)" :key="project.id" class="hover:bg-gray-50">
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div class="text-sm font-medium text-gray-900">{{ project.title }}</div>
                        <div class="text-sm text-gray-500">{{ project.category }}</div>
                      </div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        class="badge"
                        :class="getStatusBadgeClass(project.status)"
                      >
                        {{ project.status }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {{ isClient ? `${project.bidCount || 0} bids` : `$${project.bidAmount?.toLocaleString()}` }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {{ formatDate(project.updatedAt) }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <router-link
                        :to="`/projects/${project.id}`"
                        class="text-primary-600 hover:text-primary-900"
                      >
                        View
                      </router-link>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { projectAPI, userAPI } from '@/services/api'

export default {
  name: 'Dashboard',
  data() {
    return {
      isLoading: true,
      stats: [],
      recentActivity: [],
      notifications: [],
      projects: [],
      quickActions: []
    }
  },
  computed: {
    ...mapState(useAuthStore, ['user', 'isClient', 'isArchitect', 'userName'])
  },
  async mounted() {
    await this.loadDashboardData()
  },
  methods: {
    async loadDashboardData() {
      this.isLoading = true
      try {
        await Promise.all([
          this.loadStats(),
          this.loadRecentActivity(),
          this.loadNotifications(),
          this.loadProjects(),
          this.setupQuickActions()
        ])
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      } finally {
        this.isLoading = false
      }
    },

    async loadStats() {
      try {
        // This would normally come from an API
        if (this.isClient) {
          this.stats = [
            {
              label: 'Active Projects',
              value: '3',
              icon: 'FolderIcon',
              iconBg: 'bg-blue-100',
              iconColor: 'text-blue-600'
            },
            {
              label: 'Total Bids',
              value: '24',
              icon: 'CursorClickIcon',
              iconBg: 'bg-green-100',
              iconColor: 'text-green-600'
            },
            {
              label: 'Completed',
              value: '12',
              icon: 'CheckIcon',
              iconBg: 'bg-purple-100',
              iconColor: 'text-purple-600'
            },
            {
              label: 'Total Spent',
              value: '$45,000',
              icon: 'CashIcon',
              iconBg: 'bg-yellow-100',
              iconColor: 'text-yellow-600'
            }
          ]
        } else if (this.isArchitect) {
          this.stats = [
            {
              label: 'Active Bids',
              value: '8',
              icon: 'PresentationChartLineIcon',
              iconBg: 'bg-blue-100',
              iconColor: 'text-blue-600'
            },
            {
              label: 'Won Projects',
              value: '15',
              icon: 'TrophyIcon',
              iconBg: 'bg-green-100',
              iconColor: 'text-green-600'
            },
            {
              label: 'Success Rate',
              value: '68%',
              icon: 'TrendingUpIcon',
              iconBg: 'bg-purple-100',
              iconColor: 'text-purple-600'
            },
            {
              label: 'Total Earned',
              value: '$125,000',
              icon: 'CashIcon',
              iconBg: 'bg-yellow-100',
              iconColor: 'text-yellow-600'
            }
          ]
        }
      } catch (error) {
        console.error('Failed to load stats:', error)
      }
    },

    async loadRecentActivity() {
      try {
        // Mock data - replace with API call
        this.recentActivity = [
          {
            id: 1,
            type: 'bid_received',
            message: 'New bid received on "Modern Family Home" project',
            createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000),
            actionUrl: '/projects/1'
          },
          {
            id: 2,
            type: 'project_update',
            message: 'Project "Office Renovation" status updated to In Progress',
            createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000),
            actionUrl: '/projects/2'
          },
          {
            id: 3,
            type: 'message',
            message: 'New message from architect John Smith',
            createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000),
            actionUrl: '/messages'
          }
        ]
      } catch (error) {
        console.error('Failed to load recent activity:', error)
      }
    },

    async loadNotifications() {
      try {
        const response = await userAPI.getNotifications()
        this.notifications = response.data || []
      } catch (error) {
        console.error('Failed to load notifications:', error)
        // Mock data for demo
        this.notifications = [
          {
            id: 1,
            message: 'Your project "Modern Kitchen Design" received a new bid',
            read: false,
            createdAt: new Date(Date.now() - 30 * 60 * 1000)
          },
          {
            id: 2,
            message: 'Profile verification completed successfully',
            read: true,
            createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000)
          }
        ]
      }
    },

    async loadProjects() {
      try {
        const response = await projectAPI.getAll({
          limit: 5,
          sortBy: 'updatedAt',
          sortOrder: 'desc'
        })
        this.projects = response.data.content || []
      } catch (error) {
        console.error('Failed to load projects:', error)
        // Mock data for demo
        this.projects = [
          {
            id: 1,
            title: 'Modern Family Home',
            category: 'Residential',
            status: 'ACTIVE',
            bidCount: 5,
            bidAmount: 45000,
            updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000)
          },
          {
            id: 2,
            title: 'Office Complex Renovation',
            category: 'Commercial',
            status: 'IN_PROGRESS',
            bidCount: 0,
            bidAmount: 125000,
            updatedAt: new Date(Date.now() - 24 * 60 * 60 * 1000)
          }
        ]
      }
    },

    setupQuickActions() {
      if (this.isClient) {
        this.quickActions = [
          { label: 'Post New Project', to: '/projects/create', icon: 'PlusIcon' },
          { label: 'Browse Architects', to: '/architects', icon: 'UsersIcon' },
          { label: 'My Projects', to: '/projects', icon: 'FolderIcon' },
          { label: 'Messages', to: '/messages', icon: 'ChatIcon' },
          { label: 'Account Settings', to: '/profile', icon: 'CogIcon' }
        ]
      } else if (this.isArchitect) {
        this.quickActions = [
          { label: 'Browse Projects', to: '/projects', icon: 'SearchIcon' },
          { label: 'My Bids', to: '/bids', icon: 'ClipboardListIcon' },
          { label: 'Portfolio', to: '/portfolio', icon: 'PhotographIcon' },
          { label: 'Messages', to: '/messages', icon: 'ChatIcon' },
          { label: 'Profile Settings', to: '/profile', icon: 'CogIcon' }
        ]
      }
    },

    showBrowseProjects() {
      this.$router.push('/projects')
    },

    getActivityIcon(type) {
      const icons = {
        bid_received: 'CursorClickIcon',
        project_update: 'RefreshIcon',
        message: 'ChatIcon',
        payment: 'CashIcon',
        default: 'InformationCircleIcon'
      }
      return icons[type] || icons.default
    },

    getActivityIconBg(type) {
      const backgrounds = {
        bid_received: 'bg-green-100',
        project_update: 'bg-blue-100',
        message: 'bg-yellow-100',
        payment: 'bg-purple-100',
        default: 'bg-gray-100'
      }
      return backgrounds[type] || backgrounds.default
    },

    getActivityIconColor(type) {
      const colors = {
        bid_received: 'text-green-600',
        project_update: 'text-blue-600',
        message: 'text-yellow-600',
        payment: 'text-purple-600',
        default: 'text-gray-600'
      }
      return colors[type] || colors.default
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
      if (!date) return 'N/A'
      const now = new Date()
      const diff = now - new Date(date)
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)

      if (minutes < 1) return 'Just now'
      if (minutes < 60) return `${minutes}m ago`
      if (hours < 24) return `${hours}h ago`
      if (days < 7) return `${days}d ago`
      return new Date(date).toLocaleDateString()
    }
  }
}
</script>

<style scoped>
/* Custom scrollbar for activity lists */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  @apply bg-gray-100;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  @apply bg-gray-300 rounded-full;
}

/* Hover effects for interactive elements */
.hover\:shadow-card-hover:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.2s ease-in-out;
}

/* Animation for stats cards */
.card {
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Table hover effects */
tbody tr:hover {
  background-color: #f9fafb;
  transition: background-color 0.2s ease;
}

/* Loading spinner */
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

/* Badge styling */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.75rem;
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
