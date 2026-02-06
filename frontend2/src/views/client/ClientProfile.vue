<template>
  <div class="profile-page">
    <!-- Success Animation Overlay -->
    <Transition name="celebration">
      <div v-if="showSuccessAnimation" class="celebration-overlay">
        <div class="celebration-content">
          <div class="success-icon">
            <svg viewBox="0 0 52 52" class="checkmark">
              <circle class="checkmark-circle" cx="26" cy="26" r="25" fill="none" />
              <path class="checkmark-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8" />
            </svg>
          </div>
          <h3 class="success-title">{{ t.value.clientProfile.toast.updateSuccess }}</h3>
        </div>
        <div class="confetti-container">
          <div v-for="i in 50" :key="i" class="confetti" :style="getConfettiStyle(i)"></div>
        </div>
      </div>
    </Transition>

    <div class="profile-container">
      <!-- Header -->
      <header class="profile-header">
        <button @click="goToDashboard" class="back-button">
          <ArrowLeftIcon class="icon" />
          <span>{{ t.value.common.backToDashboard }}</span>
        </button>
        <div class="header-content">
          <h1 class="page-title">{{ t.value.clientProfile.title }}</h1>
          <p class="page-subtitle">{{ t.value.clientProfile.subtitle }}</p>
        </div>
      </header>

      <!-- Error Message -->
      <Transition name="slide-down">
        <div v-if="error" class="error-banner">
          <svg class="icon" fill="currentColor" viewBox="0 0 20 20">
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 9.586 8.707 8.293z"
              clip-rule="evenodd"
            />
          </svg>
          <p>{{ error }}</p>
          <button @click="error = null" class="close-button">
            <svg fill="currentColor" viewBox="0 0 20 20">
              <path
                fill-rule="evenodd"
                d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                clip-rule="evenodd"
              />
            </svg>
          </button>
        </div>
      </Transition>

      <!-- Loading Skeleton -->
      <div v-if="isLoading && !hasProfile" class="skeleton-container">
        <div class="skeleton-card" v-for="i in 4" :key="i" :style="{ animationDelay: `${i * 0.1}s` }">
          <div class="skeleton-line w-1/4"></div>
          <div class="skeleton-line w-3/4"></div>
        </div>
      </div>

      <!-- View Mode -->
      <Transition name="fade-slide" mode="out-in">
        <div v-if="!isLoading && !isEditMode" key="view" class="view-mode">
          <div class="view-header">
            <h2 class="section-title">{{ t.value.clientProfile.viewMode.title }}</h2>
            <button @click="enableEdit" class="edit-button">
              <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                />
              </svg>
              <span>{{ t.value.clientProfile.editProfile }}</span>
            </button>
          </div>

          <!-- Info Cards Grid -->
          <div class="info-grid">
            <div
              class="info-card"
              v-for="(item, index) in viewModeFields"
              :key="item.key"
              :style="{ animationDelay: `${index * 0.05}s` }"
            >
              <div class="info-label">{{ item.label }}</div>
              <div class="info-value">{{ item.value || t.value.common.notSet }}</div>
              <component :is="item.badge" v-if="item.badge" />
            </div>
          </div>

          <!-- Stats Section -->
          <div class="stats-section">
            <h3 class="stats-title">{{ t.value.clientProfile.viewMode.projectStats }}</h3>
            <div class="stats-grid">
              <div class="stat-card stat-matched">
                <div class="stat-icon">
                  <svg fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z" />
                    <path
                      fill-rule="evenodd"
                      d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </div>
                <div class="stat-number">{{ projectStats.matched }}</div>
                <div class="stat-label">{{ t.value.clientProfile.viewMode.projectsMatched }}</div>
              </div>
              <div class="stat-card stat-finished">
                <div class="stat-icon">
                  <svg fill="currentColor" viewBox="0 0 20 20">
                    <path
                      fill-rule="evenodd"
                      d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </div>
                <div class="stat-number">{{ projectStats.finished }}</div>
                <div class="stat-label">{{ t.value.clientProfile.viewMode.projectsCompleted }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Edit Mode -->
        <div v-else-if="!isLoading && isEditMode" key="edit" class="edit-mode">
          <div class="edit-header">
            <h2 class="section-title">{{ t.value.clientProfile.editProfile }}</h2>
          </div>

          <ProfileForm
            :profile-email="profileEmail"
            :ktp-number="ktpNumber"
            :ktp-verified="ktpVerified"
            :initial-data="{
              firstName: firstName,
              lastName: lastName,
              phoneNumber: profilePhone,
              ktpNum: ktpNumber
            }"
            :is-loading="isLoading"
            @submit="handleSaveProfile"
            @cancel="handleCancel"
          />
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useClientProfileStore } from '@/stores/clientProfile'
import { useI18n } from '@/composables/useI18n'
import ProfileForm from '@/components/client/ProfileForm.vue'
import { ArrowLeftIcon } from '@heroicons/vue/24/outline'

const router = useRouter()
const t = useI18n()
const clientProfileStore = useClientProfileStore()

const error = ref(null)
const showSuccessAnimation = ref(false)

const isLoading = computed(() => clientProfileStore.isLoading)
const hasProfile = computed(() => clientProfileStore.hasProfile)
const isEditMode = computed(() => clientProfileStore.isEditMode)
const firstName = computed(() => clientProfileStore.firstName)
const lastName = computed(() => clientProfileStore.lastName)
const profileEmail = computed(() => clientProfileStore.profileEmail)
const profilePhone = computed(() => clientProfileStore.profilePhone)
const ktpNumber = computed(() => clientProfileStore.ktpNumber)
const ktpVerified = computed(() => clientProfileStore.ktpVerified)
const projectStats = computed(() => clientProfileStore.projectStats)

// Verified Badge Component
const VerifiedBadge = {
  template: `
    <div class="verified-badge">
      <svg fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
      </svg>
      <span>${t.value.clientProfile.viewMode.verified}</span>
    </div>
  `
}

const viewModeFields = computed(() => [
  {
    key: 'firstName',
    label: t.value.clientProfile.viewMode.firstName,
    value: firstName.value
  },
  {
    key: 'lastName',
    label: t.value.clientProfile.viewMode.lastName,
    value: lastName.value
  },
  {
    key: 'email',
    label: t.value.clientProfile.viewMode.email,
    value: profileEmail.value
  },
  {
    key: 'phone',
    label: t.value.clientProfile.viewMode.phoneNumber,
    value: profilePhone.value
  },
  {
    key: 'ktp',
    label: t.value.clientProfile.viewMode.ktpNumber,
    value: ktpNumber.value,
    badge: ktpVerified.value ? VerifiedBadge : null
  }
])

const getConfettiStyle = index => {
  const colors = ['#4F46E5', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899']
  const angle = (index / 50) * 360
  const velocity = 3 + Math.random() * 4
  const delay = Math.random() * 0.3

  return {
    left: '50%',
    top: '50%',
    backgroundColor: colors[Math.floor(Math.random() * colors.length)],
    '--angle': `${angle}deg`,
    '--velocity': velocity,
    animationDelay: `${delay}s`
  }
}

onMounted(async () => {
  try {
    await clientProfileStore.fetchProfile()
  } catch (err) {
    error.value = t.value.clientProfile.toast.loadError
  }
})

const goToDashboard = () => {
  router.push('/client/dashboard')
}

const enableEdit = () => {
  clientProfileStore.enableEditMode()
  error.value = null
}

const handleSaveProfile = async profileData => {
  error.value = null

  try {
    await clientProfileStore.updateProfile(profileData)
    triggerSuccessAnimation()
  } catch (err) {
    error.value = clientProfileStore.error || t.value.clientProfile.toast.updateError
  }
}

const handleCancel = () => {
  clientProfileStore.disableEditMode()
  error.value = null
}

const triggerSuccessAnimation = () => {
  showSuccessAnimation.value = true
  setTimeout(() => {
    showSuccessAnimation.value = false
  }, 2500)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700&family=DM+Sans:wght@400;500;600&display=swap');

* {
  box-sizing: border-box;
}

.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fd 0%, #f1f3f9 100%);
  font-family:
    'DM Sans',
    -apple-system,
    BlinkMacSystemFont,
    sans-serif;
  padding: 2rem 1rem;
  position: relative;
}

.profile-container {
  max-width: 900px;
  margin: 0 auto;
}

/* Header */
.profile-header {
  margin-bottom: 3rem;
  animation: slideDown 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #64748b;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
  padding: 0.5rem 1rem;
  border-radius: 0.75rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  background: transparent;
  cursor: pointer;
}

.back-button:hover {
  background: white;
  color: #4f46e5;
  transform: translateX(-4px);
}

.back-button .icon {
  width: 1.25rem;
  height: 1.25rem;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-button:hover .icon {
  transform: translateX(-2px);
}

.header-content {
  background: white;
  padding: 2rem 2.5rem;
  border-radius: 1.5rem;
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.05),
    0 10px 40px rgba(79, 70, 229, 0.08);
}

.page-title {
  font-family: 'Outfit', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 1rem;
  color: #64748b;
  margin: 0;
  font-weight: 400;
}

/* Error Banner */
.error-banner {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 1rem 1.5rem;
  border-radius: 1rem;
  margin-bottom: 2rem;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.1);
}

.error-banner .icon {
  width: 1.25rem;
  height: 1.25rem;
  flex-shrink: 0;
}

.error-banner p {
  flex: 1;
  margin: 0;
  font-size: 0.875rem;
  font-weight: 500;
}

.error-banner .close-button {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: #991b1b;
  cursor: pointer;
  border-radius: 0.375rem;
  transition: background 0.2s;
}

.error-banner .close-button:hover {
  background: rgba(153, 27, 27, 0.1);
}

/* Skeleton Loading */
.skeleton-container {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.skeleton-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  animation: skeletonPulse 1.5s ease-in-out infinite;
}

.skeleton-line {
  height: 0.875rem;
  background: linear-gradient(90deg, #e2e8f0 0%, #cbd5e1 50%, #e2e8f0 100%);
  background-size: 200% 100%;
  border-radius: 0.5rem;
  margin-bottom: 0.75rem;
  animation: shimmer 1.5s ease-in-out infinite;
}

.skeleton-line:last-child {
  margin-bottom: 0;
}

.skeleton-line.w-1/4 {
  width: 25%;
}
.skeleton-line.w-3/4 {
  width: 75%;
}

/* View Mode */
.view-mode {
  animation: fadeSlideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.section-title {
  font-family: 'Outfit', sans-serif;
  font-size: 1.75rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  letter-spacing: -0.01em;
}

.edit-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  color: white;
  font-weight: 600;
  font-size: 0.9375rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.875rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}

.edit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.3);
}

.edit-button:active {
  transform: translateY(0);
}

.edit-button .icon {
  width: 1.125rem;
  height: 1.125rem;
}

/* Info Grid */
.info-grid {
  display: grid;
  gap: 1.25rem;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  margin-bottom: 2.5rem;
}

.info-card {
  background: white;
  padding: 1.75rem;
  border-radius: 1.25rem;
  border: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeSlideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  position: relative;
  overflow: hidden;
}

.info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #4f46e5, #6366f1);
  transform: scaleX(0);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.info-card:hover {
  border-color: rgba(79, 70, 229, 0.2);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.12);
  transform: translateY(-4px);
}

.info-card:hover::before {
  transform: scaleX(1);
}

.info-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.5rem;
}

.info-value {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.75rem;
  word-break: break-word;
}

.verified-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.375rem 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid #fcd34d;
}

.verified-badge svg {
  width: 1rem;
  height: 1rem;
}

/* Stats Section */
.stats-section {
  background: white;
  padding: 2rem;
  border-radius: 1.5rem;
  border: 1px solid rgba(226, 232, 240, 0.8);
  animation: fadeSlideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.2s backwards;
}

.stats-title {
  font-family: 'Outfit', sans-serif;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 1.5rem 0;
}

.stats-grid {
  display: grid;
  gap: 1.25rem;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
}

.stat-card {
  padding: 1.75rem;
  border-radius: 1.25rem;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0));
  opacity: 0;
  transition: opacity 0.3s;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-matched {
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  border: 1px solid #c7d2fe;
}

.stat-finished {
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
  border: 1px solid #a7f3d0;
}

.stat-icon {
  width: 3rem;
  height: 3rem;
  margin: 0 auto 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-matched .stat-icon {
  color: #4f46e5;
}

.stat-finished .stat-icon {
  color: #10b981;
}

.stat-icon svg {
  width: 1.5rem;
  height: 1.5rem;
}

.stat-number {
  font-family: 'Outfit', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 0.5rem;
}

.stat-matched .stat-number {
  color: #4f46e5;
}

.stat-finished .stat-number {
  color: #10b981;
}

.stat-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #64748b;
}

/* Edit Mode */
.edit-mode {
  animation: fadeSlideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.edit-header {
  margin-bottom: 2rem;
}

/* Success Animation */
.celebration-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.celebration-content {
  position: relative;
  z-index: 2;
  text-align: center;
}

.success-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 1.5rem;
}

.checkmark {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: block;
  stroke-width: 3;
  stroke: #10b981;
  stroke-miterlimit: 10;
  animation: scaleIn 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.checkmark-circle {
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  stroke-width: 3;
  stroke: #10b981;
  fill: white;
  animation: strokeCircle 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}

.checkmark-check {
  transform-origin: 50% 50%;
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  stroke-width: 3;
  stroke: #10b981;
  animation: strokeCheck 0.3s cubic-bezier(0.65, 0, 0.45, 1) 0.6s forwards;
}

.success-title {
  font-family: 'Outfit', sans-serif;
  font-size: 1.75rem;
  font-weight: 700;
  color: white;
  margin: 0;
  animation: fadeSlideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) 0.4s backwards;
}

.confetti-container {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.confetti {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 2px;
  animation: confettiFall 2s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  transform-origin: center;
}

/* Animations */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

@keyframes skeletonPulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes strokeCircle {
  100% {
    stroke-dashoffset: 0;
  }
}

@keyframes strokeCheck {
  100% {
    stroke-dashoffset: 0;
  }
}

@keyframes confettiFall {
  0% {
    transform: translate(0, 0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translate(
        calc(cos(var(--angle)) * var(--velocity) * 100px),
        calc(sin(var(--angle)) * var(--velocity) * 100px + 100vh)
      )
      rotate(720deg);
    opacity: 0;
  }
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.slide-down-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-down-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.6, 1);
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.celebration-enter-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.celebration-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 1, 1);
}

.celebration-enter-from,
.celebration-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .profile-page {
    padding: 1rem 0.75rem;
  }

  .header-content {
    padding: 1.5rem 1.25rem;
  }

  .page-title {
    font-size: 2rem;
  }

  .info-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .view-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .edit-button {
    width: 100%;
    justify-content: center;
  }
}
</style>
