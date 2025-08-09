<template>
  <div class="user-profile">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Profile Header -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-8">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
          <div class="flex items-center space-x-4">
            <div class="relative">
              <div class="w-20 h-20 bg-gray-200 rounded-full overflow-hidden">
                <img
                  v-if="user.avatar"
                  :src="user.avatar"
                  :alt="user.name"
                  class="w-full h-full object-cover"
                >
                <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-primary-100 to-primary-200">
                  <svg class="w-10 h-10 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                  </svg>
                </div>
              </div>
              <button
                @click="showAvatarUpload = true"
                class="absolute -bottom-1 -right-1 bg-primary-600 text-white p-1.5 rounded-full hover:bg-primary-700 transition-colors"
              >
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/>
                </svg>
              </button>
            </div>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">{{ userName }}</h1>
              <p class="text-sm text-gray-600">{{ user.email }}</p>
              <div class="flex items-center mt-1">
                <span
                  class="badge"
                  :class="getRoleBadgeClass(user.role)"
                >
                  {{ user.role }}
                </span>
                <span v-if="user.verified" class="ml-2 text-green-600 text-sm font-medium">
                  ✓ Verified
                </span>
              </div>
            </div>
          </div>
          <div class="mt-4 sm:mt-0">
            <button
              @click="activeTab = 'edit'"
              class="btn btn-primary"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
              </svg>
              Edit Profile
            </button>
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
              :class="activeTab === tab.id
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
            >
              {{ tab.name }}
            </button>
          </nav>
        </div>

        <div class="p-6">
          <!-- Overview Tab -->
          <div v-if="activeTab === 'overview'">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Account Stats -->
              <div class="space-y-6">
                <div>
                  <h3 class="text-lg font-semibold text-gray-900 mb-4">Account Statistics</h3>
                  <div class="space-y-4">
                    <div class="flex justify-between items-center py-2 border-b border-gray-100">
                      <span class="text-gray-600">Member since</span>
                      <span class="font-medium">{{ formatDate(user.createdAt) }}</span>
                    </div>
                    <div v-if="isClient" class="flex justify-between items-center py-2 border-b border-gray-100">
                      <span class="text-gray-600">Projects posted</span>
                      <span class="font-medium">{{ stats.projectsPosted || 0 }}</span>
                    </div>
                    <div v-if="isArchitect" class="flex justify-between items-center py-2 border-b border-gray-100">
                      <span class="text-gray-600">Proposals submitted</span>
                      <span class="font-medium">{{ stats.proposalsSubmitted || 0 }}</span>
                    </div>
                    <div class="flex justify-between items-center py-2 border-b border-gray-100">
                      <span class="text-gray-600">Profile views</span>
                      <span class="font-medium">{{ stats.profileViews || 0 }}</span>
                    </div>
                  </div>
                </div>

                <!-- Recent Activity -->
                <div>
                  <h3 class="text-lg font-semibold text-gray-900 mb-4">Recent Activity</h3>
                  <div v-if="recentActivity.length === 0" class="text-gray-500 text-sm">
                    No recent activity
                  </div>
                  <div v-else class="space-y-3">
                    <div
                      v-for="activity in recentActivity.slice(0, 5)"
                      :key="activity.id"
                      class="flex items-start space-x-3 p-3 bg-gray-50 rounded-lg"
                    >
                      <div class="w-2 h-2 bg-primary-500 rounded-full mt-2 flex-shrink-0"></div>
                      <div class="flex-1">
                        <p class="text-sm text-gray-900">{{ activity.message }}</p>
                        <p class="text-xs text-gray-500 mt-1">{{ formatDate(activity.createdAt) }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Personal Information -->
              <div>
                <h3 class="text-lg font-semibold text-gray-900 mb-4">Personal Information</h3>
                <div class="space-y-4">
                  <div>
                    <label class="text-sm font-medium text-gray-700">Full Name</label>
                    <p class="mt-1 text-gray-900">{{ userName }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-gray-700">Email</label>
                    <p class="mt-1 text-gray-900">{{ user.email }}</p>
                  </div>
                  <div v-if="user.phone">
                    <label class="text-sm font-medium text-gray-700">Phone</label>
                    <p class="mt-1 text-gray-900">{{ user.phone }}</p>
                  </div>
                  <div v-if="user.location">
                    <label class="text-sm font-medium text-gray-700">Location</label>
                    <p class="mt-1 text-gray-900">{{ user.location }}</p>
                  </div>
                  <div v-if="user.bio">
                    <label class="text-sm font-medium text-gray-700">Bio</label>
                    <p class="mt-1 text-gray-900">{{ user.bio }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Edit Profile Tab -->
          <div v-if="activeTab === 'edit'">
            <form @submit.prevent="handleSaveProfile" class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- Personal Information -->
                <div class="space-y-4">
                  <h3 class="text-lg font-semibold text-gray-900">Personal Information</h3>

                  <div class="grid grid-cols-2 gap-4">
                    <div>
                      <label for="firstName" class="form-label">First Name</label>
                      <input
                        id="firstName"
                        v-model="editForm.firstName"
                        type="text"
                        class="form-input"
                      />
                    </div>
                    <div>
                      <label for="lastName" class="form-label">Last Name</label>
                      <input
                        id="lastName"
                        v-model="editForm.lastName"
                        type="text"
                        class="form-input"
                      />
                    </div>
                  </div>

                  <div>
                    <label for="email" class="form-label">Email</label>
                    <input
                      id="email"
                      v-model="editForm.email"
                      type="email"
                      class="form-input"
                    />
                  </div>

                  <div>
                    <label for="phone" class="form-label">Phone</label>
                    <input
                      id="phone"
                      v-model="editForm.phone"
                      type="tel"
                      class="form-input"
                    />
                  </div>

                  <div>
                    <label for="location" class="form-label">Location</label>
                    <input
                      id="location"
                      v-model="editForm.location"
                      type="text"
                      placeholder="City, State"
                      class="form-input"
                    />
                  </div>
                </div>

                <!-- Professional Information -->
                <div class="space-y-4">
                  <h3 class="text-lg font-semibold text-gray-900">Professional Information</h3>

                  <div>
                    <label for="bio" class="form-label">Bio</label>
                    <textarea
                      id="bio"
                      v-model="editForm.bio"
                      rows="4"
                      placeholder="Tell us about yourself..."
                      class="form-input"
                    ></textarea>
                  </div>

                  <div v-if="isArchitect">
                    <label for="specializations" class="form-label">Specializations</label>
                    <input
                      id="specializations"
                      v-model="editForm.specializations"
                      type="text"
                      placeholder="e.g., Residential, Commercial, Sustainable Design"
                      class="form-input"
                    />
                    <p class="text-sm text-gray-500 mt-1">Separate multiple specializations with commas</p>
                  </div>

                  <div v-if="isArchitect">
                    <label for="experience" class="form-label">Years of Experience</label>
                    <input
                      id="experience"
                      v-model="editForm.experience"
                      type="number"
                      min="0"
                      class="form-input"
                    />
                  </div>
                </div>
              </div>

              <!-- Form Actions -->
              <div class="flex justify-end space-x-4 pt-6">
                <button
                  type="button"
                  @click="activeTab = 'overview'"
                  class="btn btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  :disabled="isLoading"
                  class="btn btn-primary"
                >
                  <span v-if="isLoading">Saving...</span>
                  <span v-else>Save Changes</span>
                </button>
              </div>
            </form>
          </div>

          <!-- Security Tab -->
          <div v-if="activeTab === 'security'">
            <div class="max-w-2xl">
              <!-- Change Password -->
              <div class="mb-8">
                <h3 class="text-lg font-semibold text-gray-900 mb-4">Change Password</h3>
                <form @submit.prevent="handleChangePassword" class="space-y-4">
                  <div>
                    <label for="currentPassword" class="form-label">Current Password</label>
                    <input
                      id="currentPassword"
                      v-model="passwordForm.currentPassword"
                      type="password"
                      class="form-input"
                    />
                  </div>
                  <div>
                    <label for="newPassword" class="form-label">New Password</label>
                    <input
                      id="newPassword"
                      v-model="passwordForm.newPassword"
                      type="password"
                      class="form-input"
                    />
                  </div>
                  <div>
                    <label for="confirmPassword" class="form-label">Confirm New Password</label>
                    <input
                      id="confirmPassword"
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      class="form-input"
                    />
                  </div>
                  <button
                    type="submit"
                    :disabled="isLoading"
                    class="btn btn-primary"
                  >
                    <span v-if="isLoading">Updating...</span>
                    <span v-else>Update Password</span>
                  </button>
                </form>
              </div>

              <!-- Account Settings -->
              <div class="border-t border-gray-200 pt-8">
                <h3 class="text-lg font-semibold text-gray-900 mb-4">Account Settings</h3>
                <div class="space-y-4">
                  <div class="flex items-center justify-between">
                    <div>
                      <h4 class="text-sm font-medium text-gray-900">Email Notifications</h4>
                      <p class="text-sm text-gray-500">Receive notifications about your projects and proposals</p>
                    </div>
                    <label class="relative inline-flex items-center cursor-pointer">
                      <input
                        v-model="settings.emailNotifications"
                        type="checkbox"
                        class="sr-only peer"
                      />
                      <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
                    </label>
                  </div>

                  <div class="flex items-center justify-between">
                    <div>
                      <h4 class="text-sm font-medium text-gray-900">Profile Visibility</h4>
                      <p class="text-sm text-gray-500">Allow others to find and view your profile</p>
                    </div>
                    <label class="relative inline-flex items-center cursor-pointer">
                      <input
                        v-model="settings.profileVisible"
                        type="checkbox"
                        class="sr-only peer"
                      />
                      <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
                    </label>
                  </div>
                </div>
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
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'Profile',
  data() {
    return {
      activeTab: 'overview',
      isLoading: false,
      showAvatarUpload: false,
      tabs: [
        { id: 'overview', name: 'Overview' },
        { id: 'edit', name: 'Edit Profile' },
        { id: 'security', name: 'Security' }
      ],
      editForm: {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        location: '',
        bio: '',
        specializations: '',
        experience: ''
      },
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      settings: {
        emailNotifications: true,
        profileVisible: true
      },
      stats: {
        projectsPosted: 5,
        proposalsSubmitted: 12,
        profileViews: 148
      },
      recentActivity: [
        {
          id: 1,
          message: 'Updated profile information',
          createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000)
        },
        {
          id: 2,
          message: 'Submitted proposal for Modern Kitchen Design project',
          createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000)
        }
      ]
    }
  },
  computed: {
    ...mapState(useAuthStore, ['user', 'userName', 'isClient', 'isArchitect'])
  },
  mounted() {
    this.initializeForm()
  },
  methods: {
    ...mapActions(useAuthStore, ['updateProfile', 'changePassword']),

    initializeForm() {
      if (this.user) {
        this.editForm = {
          firstName: this.user.firstName || '',
          lastName: this.user.lastName || '',
          email: this.user.email || '',
          phone: this.user.phone || '',
          location: this.user.location || '',
          bio: this.user.bio || '',
          specializations: this.user.specializations?.join(', ') || '',
          experience: this.user.experience || ''
        }
      }
    },

    async handleSaveProfile() {
      this.isLoading = true
      try {
        const profileData = { ...this.editForm }
        if (profileData.specializations) {
          profileData.specializations = profileData.specializations.split(',').map(s => s.trim())
        }

        await this.updateProfile(profileData)
        this.activeTab = 'overview'
        alert('Profile updated successfully!')
      } catch (error) {
        console.error('Failed to update profile:', error)
        alert('Failed to update profile. Please try again.')
      } finally {
        this.isLoading = false
      }
    },

    async handleChangePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        alert('New passwords do not match')
        return
      }

      this.isLoading = true
      try {
        await this.changePassword({
          currentPassword: this.passwordForm.currentPassword,
          newPassword: this.passwordForm.newPassword
        })

        this.passwordForm = {
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        }

        alert('Password updated successfully!')
      } catch (error) {
        console.error('Failed to change password:', error)
        alert('Failed to change password. Please try again.')
      } finally {
        this.isLoading = false
      }
    },

    getRoleBadgeClass(role) {
      const classes = {
        CLIENT: 'badge-primary',
        ARCHITECT: 'badge-success',
        ADMIN: 'badge-danger'
      }
      return classes[role] || 'badge-secondary'
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

.badge-danger {
  background-color: rgb(254 226 226);
  color: rgb(153 27 27);
}

.badge-secondary {
  background-color: rgb(243 244 246);
  color: rgb(55 65 81);
}

/* Toggle switch styling */
.peer:checked ~ .peer-checked\:bg-primary-600 {
  background-color: rgb(37 99 235);
}

.peer:focus ~ .peer-focus\:ring-primary-300 {
  box-shadow: 0 0 0 4px rgba(147, 197, 253, 0.5);
}
</style>
