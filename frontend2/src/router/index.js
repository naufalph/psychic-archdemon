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
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/client/dashboard',
    name: 'ClientDashboard',
    component: () => import('@/views/client/ClientDashboard.vue'),
    meta: { requiresAuth: true }
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

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next({ name: 'Landing' })
  } else if (to.meta.requiresRole) {
    const requiredRole = to.meta.requiresRole
    if (!authStore.hasRole(requiredRole)) {
      const redirectRoute = requiredRole === 'CLIENT' ? 'ClientDashboard' : 'ArchitectDashboard'
      next({ name: redirectRoute })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
