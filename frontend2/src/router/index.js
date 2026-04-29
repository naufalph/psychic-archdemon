import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/landing/LandingPage.vue')
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

  // ── Client app shell (persistent sidebar) ──────────────────────────────────
  {
    path: '/client',
    component: () => import('@/layouts/ClientLayout.vue'),
    meta: { requiresAuth: true, requiresRole: 'CLIENT' },
    children: [
      {
        path: 'dashboard',
        name: 'ClientDashboard',
        component: () => import('@/views/client/ClientDashboard.vue')
      },
      {
        path: 'projects',
        name: 'ClientProjects',
        component: () => import('@/views/client/ClientProjectsPage.vue')
      },
      {
        path: 'messages',
        name: 'ClientMessages',
        component: () => import('@/views/client/ClientMessages.vue')
      },
      {
        path: 'payments',
        name: 'ClientPayments',
        component: () => import('@/views/client/ClientPaymentsPage.vue')
      },
      {
        path: 'settings',
        name: 'ClientSettings',
        component: () => import('@/views/client/ClientSettings.vue')
      },
      {
        path: 'profile',
        name: 'ClientProfile',
        component: () => import('@/views/client/ClientProfile.vue')
      },
      {
        path: 'projects/create',
        name: 'ProjectCreate',
        component: () => import('@/views/client/ProjectCreate.vue')
      },
      {
        path: 'projects/:projectId/finalization',
        name: 'PreProjectFinalization',
        component: () => import('@/views/client/PreProjectFinalization.vue')
      },
      {
        path: 'projects/:id/active',
        name: 'ActiveProjectDashboard',
        component: () => import('@/views/client/ActiveProjectDashboard.vue')
      },
      {
        path: 'projects/:projectId/payments',
        name: 'ProjectPhasePayments',
        component: () => import('@/views/client/ProjectPhasePayments.vue')
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/client/ProjectDetail.vue')
      },
      {
        path: 'projects/:projectId/bids/:bidId',
        name: 'BidDetail',
        component: () => import('@/views/client/BidDetail.vue')
      }
    ]
  },

  // ── Architect routes ────────────────────────────────────────────────────────
  {
    path: '/architect/onboarding',
    name: 'ArchitectOnboarding',
    component: () => import('@/views/architect/ArchitectOnboarding.vue'),
    meta: { requiresAuth: true, requiresOnboarding: true, role: 'ARCHITECT' }
  },
  // Full-screen flows (no sidebar)
  {
    path: '/architect/projects/:projectId/finalization',
    name: 'ArchitectFinalizationView',
    component: () => import('@/views/architect/PreProjectFinalizationForArchitect.vue'),
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
  // Architect app shell (persistent sidebar)
  {
    path: '/architect',
    component: () => import('@/layouts/ArchitectLayout.vue'),
    meta: { requiresAuth: true, requiresRole: 'ARCHITECT' },
    children: [
      {
        path: 'dashboard',
        name: 'ArchitectDashboard',
        component: () => import('@/views/architect/ArchitectDashboard.vue')
      },
      {
        path: 'opportunities',
        name: 'OpportunityList',
        component: () => import('@/views/architect/OpportunityList.vue')
      },
      {
        path: 'bids',
        name: 'MyBids',
        component: () => import('@/views/architect/MyBids.vue')
      },
      {
        path: 'portfolios',
        name: 'ArchitectPortfolios',
        component: () => import('@/views/architect/ArchitectPortfolios.vue')
      },
      {
        path: 'profile',
        name: 'ArchitectProfile',
        component: () => import('@/views/architect/ArchitectProfile.vue')
      },
      {
        path: 'settings',
        name: 'ArchitectSettings',
        component: () => import('@/views/architect/ArchitectSettings.vue')
      }
    ]
  },

  // ── Superuser ───────────────────────────────────────────────────────────────
  {
    path: '/superuser/support',
    name: 'SupportDashboard',
    component: () => import('@/views/superuser/SupportDashboard.vue'),
    meta: { requiresAuth: true, requiresRole: 'SUPERUSER' }
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
    const roles = authStore.user?.registeredRoles || []
    if (roles.includes('ARCHITECT')) {
      next({ name: 'ArchitectDashboard' })
    } else if (roles.includes('CLIENT')) {
      next({ name: 'ClientDashboard' })
    } else {
      next({ name: 'Landing' })
    }
    return
  }

  if (to.name === 'ArchitectDashboard' && authStore.user?.needsArchitectOnboarding === true) {
    next({ name: 'ArchitectOnboarding' })
    return
  }

  if (to.meta.requiresOnboarding) {
    const needsOnboarding =
      to.meta.role === 'ARCHITECT' ? authStore.user?.needsArchitectOnboarding : false
    if (!needsOnboarding) {
      const dashboardRoute = to.meta.role === 'ARCHITECT' ? 'ArchitectDashboard' : 'ClientDashboard'
      next({ name: dashboardRoute })
      return
    }
  }

  // For nested client routes, role check is on the parent; skip redundant check on children
  const effectiveRole =
    to.meta.requiresRole || to.matched.find(r => r.meta.requiresRole)?.meta.requiresRole
  if (effectiveRole && !authStore.hasRole(effectiveRole)) {
    const redirectRoute = effectiveRole === 'CLIENT' ? 'ClientDashboard' : 'ArchitectDashboard'
    next({ name: redirectRoute })
    return
  }

  next()
})

export default router
