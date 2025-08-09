<template>
  <div class="admin-dashboard">
    <!-- Dashboard Header -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
            <p class="mt-1 text-sm text-gray-500">
              Manage platform users, projects, and system settings
            </p>
          </div>
          <div class="flex space-x-4">
            <button class="btn btn-outline">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3M3 17V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
              </svg>
              Export Report
            </button>
            <button class="btn btn-primary">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
              System Settings
            </button>
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
        <!-- Quick Stats -->
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
                    <dd class="flex items-baseline">
                      <div class="text-2xl font-semibold text-gray-900">{{ stat.value }}</div>
                      <div
                        v-if="stat.change"
                        class="ml-2 flex items-baseline text-sm"
                        :class="stat.changePositive ? 'text-green-600' : 'text-red-600'"
                      >
                        <svg
                          class="w-3 h-3 flex-shrink-0"
                          :class="stat.changePositive ? 'text-green-500' : 'text-red-500'"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                        >
                          <path
                            v-if="stat.changePositive"
                            fill-rule="evenodd"
                            d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L11 7.414V15a1 1 0 11-2 0V7.414L6.707 9.707a1 1 0 01-1.414 0z"
                            clip-rule="evenodd"
                          />
                          <path
                            v-else
                            fill-rule="evenodd"
                            d="M14.707 10.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L9 12.586V5a1 1 0 012 0v7.586l2.293-2.293a1 1 0 011.414 0z"
                            clip-rule="evenodd"
                          />
                        </svg>
                        {{ stat.change }}%
                      </div>
                    </dd>
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
                <div class="flex items-center justify-between">
                  <h3 class="text-lg leading-6 font-medium text-gray-900">Recent Activity</h3>
                  <button class="text-sm text-primary-600 hover:text-primary-500">View all</button>
                </div>
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
                        <div class="flex items-center mt-1 space-x-2 text-xs text-gray-500">
                          <span>{{ formatDate(activity.createdAt) }}</span>
                          <span v-if="activity.user">by {{ activity.user }}</span>
                        </div>
                      </div>
                      <div v-if="activity.actionRequired" class="flex-shrink-0">
                        <button class="btn btn-outline btn-sm">Review</button>
                      </div>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- System Status & Quick Actions -->
          <div class="space-y-6">
            <!-- System Status -->
            <div class="card">
              <div class="card-header">
                <h3 class="text-lg leading-6 font-medium text-gray-900">System Status</h3>
              </div>
              <div class="card-body space-y-4">
                <div
                  v-for="status in systemStatus"
                  :key="status.service"
                  class="flex items-center justify-between"
                >
                  <span class="text-sm font-medium text-gray-900">{{ status.service }}</span>
                  <span
                    class="badge"
                    :class="getStatusBadgeClass(status.status)"
                  >
                    {{ status.status }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Quick Actions -->
            <div class="card">
              <div class="card-header">
                <h3 class="text-lg leading-6 font-medium text-gray-900">Quick Actions</h3>
              </div>
              <div class="card-body space-y-3">
                <button
                  v-for="action in quickActions"
                  :key="action.label"
                  @click="handleQuickAction(action.id)"
                  class="flex items-center w-full p-3 text-sm font-medium rounded-md text-gray-700 hover:text-gray-900 hover:bg-gray-50 transition-colors"
                >
                  <component :is="action.icon" class="w-5 h-5 mr-3 text-gray-400" />
                  {{ action.label }}
                </button>
              </div>
            </div>

            <!-- Platform Health -->
            <div class="card">
              <div class="card-header">
                <h3 class="text-lg leading-6 font-medium text-gray-900">Platform Health</h3>
              </div>
              <div class="card-body">
                <div class="space-y-3">
                  <div class="flex justify-between items-center">
                    <span class="text-sm text-gray-600">Server Uptime</span>
                    <span class="text-sm font-medium">99.9%</span>
                  </div>
                  <div class="flex justify-between items-center">
                    <span class="text-sm text-gray-600">Response Time</span>
                    <span class="text-sm font-medium">234ms</span>
                  </div>
                  <div class="flex justify-between items-center">
                    <span class="text-sm text-gray-600">Active Sessions</span>
                    <span class="text-sm font-medium">{{ stats.find(s => s.label === 'Active Users')?.value || '0' }}</span>
                  </div>
                  <div class="flex justify-between items-center">
                    <span class="text-sm text-gray-600">Database Health</span>
                    <span class="badge badge-success text-xs">Healthy</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Data Tables Section -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- Recent Users -->
          <div class="card">
            <div class="card-header">
              <div class="flex items-center justify-between">
                <h3 class="text-lg leading-6 font-medium text-gray-900">Recent Users</h3>
                <router-link to="/admin/users" class="text-sm text-primary-600 hover:text-primary-500">
                  View all
                </router-link>
              </div>
            </div>
            <div class="card-body p-0">
              <div class="overflow-hidden">
                <table class="min-w-full divide-y divide-gray-200">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        User
                      </th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Role
                      </th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="user in recentUsers" :key="user.id" class="hover:bg-gray-50">
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div class="flex items-center">
                          <div class="flex-shrink-0 h-8 w-8">
                            <div class="h-8 w-8 rounded-full bg-gray-200 flex items-center justify-center">
                              <span class="text-sm font-medium text-gray-700">
                                {{ user.name.split(' ').map(n => n[0]).join('') }}
                              </span>
                            </div>
                          </div>
                          <div class="ml-4">
                            <div class="text-sm font-medium text-gray-900">{{ user.name }}</div>
                            <div class="text-sm text-gray-500">{{ user.email }}</div>
                          </div>
                        </div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <span class="badge" :class="getRoleBadgeClass(user.role)">
                          {{ user.role }}
                        </span>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <span class="badge" :class="getStatusBadgeClass(user.status)">
                          {{ user.status }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Recent Projects -->
          <div class="card">
            <div class="card-header">
              <div class="flex items-center justify-between">
                <h3 class="text-lg leading-6 font-medium text-gray-900">Recent Projects</h3>
                <router-link to="/admin/projects" class="text-sm text-primary-600 hover:text-primary-500">
                  View all
                </router-link>
              </div>
            </div>
            <div class="card-body p-0">
              <div class="overflow-hidden">
                <table class="min-w-full divide-y divide-gray-200">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Project
                      </th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Budget
                      </th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="project in recentProjects" :key="project.id" class="hover:bg-gray-50">
                      <td class="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div class="text-sm font-medium text-gray-900">{{ project.title }}</div>
                          <div class="text-sm text-gray-500">{{ project.category }}</div>
                        </div>
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        ${{ project.budget?.toLocaleString() }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap">
                        <span class="badge" :class="getProjectStatusBadgeClass(project.status)">
                          {{ project.status }}
                        </span>
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
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  data() {
    return {
      isLoading: false,
      stats: [
        {
          label: 'Total Users',
          value: '1,234',
          change: '12',
          changePositive: true,
          icon: 'UsersIcon',
          iconBg: 'bg-blue-100',
          iconColor: 'text-blue-600'
        },
        {
          label: 'Active Projects',
          value: '89',
          change: '8',
          changePositive: true,
          icon: 'FolderIcon',
          iconBg: 'bg-green-100',
          iconColor: 'text-green-600'
        },
        {
          label: 'Total Revenue',
          value: '$45,678',
          change: '23',
          changePositive: true,
          icon: 'CashIcon',
          iconBg: 'bg-yellow-100',
          iconColor: 'text-yellow-600'
        },
        {
          label: 'Support Tickets',
          value: '12',
          change: '5',
          changePositive: false,
          icon: 'SupportIcon',
          iconBg: 'bg-red-100',
          iconColor: 'text-red-600'
        }
      ],
      systemStatus: [
        { service: 'API Gateway', status: 'Healthy' },
        { service: 'Database', status: 'Healthy' },
        { service: 'File Storage', status: 'Warning' },
        { service: 'Email Service', status: 'Healthy' },
        { service: 'Payment Gateway', status: 'Healthy' }
      ],
      quickActions: [
        { id: 'users', label: 'Manage Users', icon: 'UsersIcon' },
        { id: 'projects', label: 'Review Projects', icon: 'FolderIcon' },
        { id: 'reports', label: 'Generate Reports', icon: 'ChartBarIcon' },
        { id: 'settings', label: 'System Settings', icon: 'CogIcon' },
        { id: 'backup', label: 'Database Backup', icon: 'DatabaseIcon' }
      ],
      recentActivity: [
        {
          id: 1,
          type: 'user',
          message: 'New architect registration: Sarah Johnson',
          user: 'System',
          createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000),
          actionRequired: false
        },
        {
          id: 2,
          type: 'project',
          message: 'Project "Modern Office Design" requires review',
          user: 'John Smith',
          createdAt: new Date(Date.now() - 4 * 60 * 60 * 1000),
          actionRequired: true
        },
        {
          id: 3,
          type: 'payment',
          message: 'Payment processed: $2,500 for Project #123',
          user: 'Payment System',
          createdAt: new Date(Date.now() - 6 * 60 * 60 * 1000),
          actionRequired: false
        }
      ],
      recentUsers: [
        {
          id: 1,
          name: 'Sarah Johnson',
          email: 'sarah@example.com',
          role: 'ARCHITECT',
          status: 'Active'
        },
        {
          id: 2,
          name: 'Mike Chen',
          email: 'mike@example.com',
          role: 'CLIENT',
          status: 'Active'
        },
        {
          id: 3,
          name: 'Emily Davis',
          email: 'emily@example.com',
          role: 'ARCHITECT',
          status: 'Pending'
        }
      ],
      recentProjects: [
        {
          id: 1,
          title: 'Modern Family Home',
          category: 'Residential',
          budget: 250000,
          status: 'ACTIVE'
        },
        {
          id: 2,
          title: 'Office Renovation',
          category: 'Commercial',
          budget: 450000,
          status: 'IN_PROGRESS'
        },
        {
          id: 3,
          title: 'Community Center',
          category: 'Public',
          budget: 750000,
          status: 'COMPLETED'
        }
      ]
    }
  },
  mounted() {
    this.loadDashboardData()
  },
  methods: {
    async loadDashboardData() {
      this.isLoading = true
      try {
        // TODO: Replace with actual API calls
        await new Promise(resolve => setTimeout(resolve, 1000))
      } catch (error) {
        console.error('Failed to load dashboard data:', error)
      } finally {
        this.isLoading = false
      }
    },

    handleQuickAction(actionId) {
      switch (actionId) {
        case 'users':
          this.$router.push('/admin/users')
          break
        case 'projects':
          this.$router.push('/admin/projects')
          break
        case 'reports':
          alert('Generate Reports functionality (placeholder)')
          break
        case 'settings':
          alert('System Settings functionality (placeholder)')
          break
        case 'backup':
          alert('Database Backup functionality (placeholder)')
          break
        default:
          console.log('Unknown action:', actionId)
      }
    },

    getActivityIcon(type) {
      const icons = {
        user: 'UserPlusIcon',
        project: 'FolderPlusIcon',
        payment: 'CashIcon',
        system: 'CogIcon',
        default: 'InformationCircleIcon'
      }
      return icons[type] || icons.default
    },

    getActivityIconBg(type) {
      const backgrounds = {
        user: 'bg-blue-100',
        project: 'bg-green-100',
        payment: 'bg-yellow-100',
        system: 'bg-gray-100',
        default: 'bg-gray-100'
      }
      return backgrounds[type] || backgrounds.default
    },

    getActivityIconColor(type) {
      const colors = {
        user: 'text-blue-600',
        project: 'text-green-600',
        payment: 'text-yellow-600',
        system: 'text-gray-600',
        default: 'text-gray-600'
      }
      return colors[type] || colors.default
    },

    getStatusBadgeClass(status) {
      const classes = {
        Healthy: 'badge-success',
        Warning: 'badge-warning',
        Error: 'badge-danger',
        Active: 'badge-success',
        Pending: 'badge-warning',
        Inactive: 'badge-danger'
      }
      return classes[status] || 'badge-secondary'
    },

    getRoleBadgeClass(role) {
      const classes = {
        ADMIN: 'badge-danger',
        ARCHITECT: 'badge-success',
        CLIENT: 'badge-primary'
      }
      return classes[role] || 'badge-secondary'
    },

    getProjectStatusBadgeClass(status) {
      const classes = {
        ACTIVE: 'badge-primary',
        IN_PROGRESS: 'badge-warning',
        COMPLETED: 'badge-success',
        CANCELLED: 'badge-danger'
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
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.badge-primary {
  background-color: rgb(219 234 254);
  color: rgb(30 64 175);
}

.badge-success {
  background-color: rgb(220 252 231);
  color: rgb(22 101 52);
}

.badge-warning {
  background-color: rgb(254 249 195);
  color: rgb(133 77 14);
}

.badge-danger {
  background-color: rgb(254 226 226);
  color: rgb(153 27 27);
}

.badge-secondary {
  background-color: rgb(243 244 246);
  color: rgb(55 65 81);
}

/* Card hover effects */
.card {
  transition: all 0.2s ease-in-out;
}

.card:hover {
  transform: translateY(-1px);
}

/* Table hover effects */
tbody tr:hover {
  background-color: #f9fafb;
  transition: background-color 0.2s ease;
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
</style>
