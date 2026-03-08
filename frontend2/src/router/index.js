import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/marketing/LandingPage.vue')
  },
  {
    path: '/signup',
    name: 'Signup',
    component: () => import('@/views/auth/Signup.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/verify-email',
    name: 'VerifyEmail',
    component: () => import('@/views/auth/VerifyEmail.vue')
  },
  {
    path: '/auth/callback',
    name: 'AuthCallback',
    component: () => import('@/views/auth/AuthCallback.vue')
  },
  {
    path: '/client/dashboard',
    name: 'ClientDashboard',
    component: () => import('@/views/client/ClientDashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/projects/:projectId/bids/:bidId',
    name: 'BidDetail',
    component: () => import('@/views/client/BidDetail.vue'),
    meta: { requiresAuth: true, requiresRole: 'CLIENT' }
  },
  {
    path: '/architect/onboarding',
    name: 'ArchitectOnboarding',
    component: () => import('@/views/architect/ArchitectOnboarding.vue'),
    meta: { requiresAuth: true, requiresOnboarding: true, role: 'ARCHITECT' }
  },
  {
    path: '/architect/dashboard',
    name: 'ArchitectDashboard',
    component: () => import('@/views/architect/ArchitectDashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/projects/create',
    name: 'ProjectCreate',
    component: () => import('@/views/client/ProjectCreate.vue'),
    meta: { requiresAuth: true, requiresRole: 'CLIENT' }
  },
  {
    path: '/client/projects/:projectId/finalization',
    name: 'PreProjectFinalization',
    component: () => import('@/views/client/PreProjectFinalization.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/architect/projects/:projectId/finalization',
    name: 'ArchitectFinalizationView',
    component: () => import('@/views/architect/PreProjectFinalizationForArchitect.vue'),
    meta: { requiresAuth: true, requiresRole: 'ARCHITECT' }
  },
  {
    path: '/client/projects/:id',
    name: 'ProjectDetail',
    component: () => import('@/views/client/ProjectDetail.vue'),
    meta: { requiresAuth: true, requiresRole: 'CLIENT' }
  },
  {
    path: '/architect/opportunities',
    name: 'OpportunityList',
    component: () => import('@/views/architect/OpportunityList.vue'),
    meta: { requiresAuth: true, requiresRole: 'ARCHITECT' }
  },
  {
    path: '/architect/opportunities/:projectId',
    name: 'ProjectDetailForArchitect',
    component: () => import('@/views/architect/ProjectDetailForArchitect.vue'),
    meta: { requiresAuth: true, requiresRole: 'ARCHITECT' }
  },
  {
    path: '/architect/opportunities/:projectId/propose',
    name: 'ProposalCreate',
    component: () => import('@/views/architect/ProposalCreate.vue'),
    meta: { requiresAuth: true, requiresRole: 'ARCHITECT' }
  },
  {
    path: '/architect/bids',
    name: 'MyBids',
    component: () => import('@/views/architect/MyBids.vue'),
    meta: { requiresAuth: true, requiresRole: 'ARCHITECT' }
  },
  {
    path: '/architect/profile',
    name: 'ArchitectProfile',
    component: () => import('@/views/architect/ArchitectProfile.vue'),
    meta: { requiresAuth: true, requiresRole: 'ARCHITECT' }
  },
  {
    path: '/architect/portfolios',
    name: 'ArchitectPortfolios',
    component: () => import('@/views/architect/ArchitectPortfolios.vue'),
    meta: { requiresAuth: true, requiresRole: 'ARCHITECT' }
  },
  {
    path: '/client/profile',
    name: 'ClientProfile',
    component: () => import('@/views/client/ClientProfile.vue'),
    meta: { requiresAuth: true, requiresRole: 'CLIENT' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // Initialize auth from token if not already done
  if (authStore.token && !authStore.user) {
    try {
      await authStore.fetchUserData()
    } catch (error) {
      console.error('Failed to fetch user data:', error)
      authStore.clearAuth()
      if (to.meta.requiresAuth) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
    }
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next({ name: 'Landing' })
    return
  }

  // Check if architect needs onboarding before accessing architect routes
  if (to.name === 'ArchitectDashboard' && authStore.user?.needsArchitectOnboarding === true) {
    next({ name: 'ArchitectOnboarding' })
    return
  }

  if (to.meta.requiresOnboarding) {
    // Check backend data, not localStorage
    const needsOnboarding =
      to.meta.role === 'ARCHITECT' ? authStore.user?.needsArchitectOnboarding : false

    if (!needsOnboarding) {
      // Already completed onboarding, redirect to dashboard
      const dashboardRoute = to.meta.role === 'ARCHITECT' ? 'ArchitectDashboard' : 'ClientDashboard'
      next({ name: dashboardRoute })
      return
    }
  }

  if (to.meta.requiresRole) {
    const requiredRole = to.meta.requiresRole
    if (!authStore.hasRole(requiredRole)) {
      const redirectRoute = requiredRole === 'CLIENT' ? 'ClientDashboard' : 'ArchitectDashboard'
      next({ name: redirectRoute })
      return
    }
  }

  next()
})

export default router
