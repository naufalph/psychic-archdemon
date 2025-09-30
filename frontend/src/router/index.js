import { createRouter, createWebHistory } from 'vue-router'

// Import views (lazy-loaded for better performance)
const Home = () => import('@/views/Home.vue')
const Projects = () => import('@/views/projects/ProjectList.vue')
const ProjectDetail = () => import('@/views/projects/ProjectDetail.vue')
const CreateProject = () => import('@/views/projects/CreateProject.vue')
const Architects = () => import('@/views/architects/ArchitectList.vue')
const ArchitectProfile = () => import('@/views/architects/ArchitectProfile.vue')
const Dashboard = () => import('@/views/clients/ClientDashboard.vue')
const ClientLandingPage = () => import('@/views/clients/ClientLandingPage.vue')
const ArchitectLandingPage = () => import('@/views/architects/ArchitectLandingPage.vue')
const Profile = () => import('@/views/user/Profile.vue')
const NotFound = () => import('@/views/errors/NotFound.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: 'Home - Rumantra',
      description: 'Connect with talented architects for your next project'
    }
  },

  // Project Routes
  {
    path: '/projects',
    name: 'Projects',
    component: Projects,
    meta: {
      title: 'Projects - Rumantra',
      description: 'Browse available architecture projects'
    }
  },
  {
    path: '/projects/create',
    name: 'CreateProject',
    component: CreateProject,
    meta: {
      title: 'Create Project - Rumantra',
      requiresAuth: true,
      roles: ['CLIENT', 'ADMIN']
    }
  },
  {
    path: '/projects/:id',
    name: 'ProjectDetail',
    component: ProjectDetail,
    props: true,
    meta: {
      title: 'Project Details - Rumantra'
    }
  },

  // Architect Routes
  {
    path: '/architects',
    name: 'Architects',
    component: Architects,
    meta: {
      title: 'Architects - Rumantra',
      description: 'Discover talented architects'
    }
  },
  {
    path: '/architects/:id',
    name: 'ArchitectProfile',
    component: ArchitectProfile,
    props: true,
    meta: {
      title: 'Architect Profile - Rumantra'
    }
  },

  // User Dashboard Routes
  {
    path: '/dashboard',
    name: 'ClientDashboard',
    component: Dashboard,
    meta: {
      title: 'Client Dashboard - Rumantra',
      description: 'Manage your architecture projects and find architects'
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: {
      title: 'My Profile - Rumantra',
      requiresAuth: true
    }
  },

  // Temporary route for reviewing ClientLandingPage
  {
    path: '/client-landing',
    name: 'ClientLanding',
    component: ClientLandingPage,
    meta: {
      title: 'Client Landing - Rumantra',
      description: 'Landing page for clients to find architects'
    }
  },

  // Architect Landing Page
  {
    path: '/architect-landing',
    name: 'ArchitectLanding',
    component: ArchitectLandingPage,
    meta: {
      title: 'Architect Landing - Rumantra',
      description: 'Landing page for architects to join the platform'
    }
  },

  // Error Routes
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: 'Page Not Found - Rumantra'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // Return saved position if available (browser back/forward)
    if (savedPosition) {
      return savedPosition
    }

    // Scroll to anchor if present
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    }

    // Scroll to top for new pages
    return { top: 0 }
  }
})

// Navigation Guards
router.beforeEach(async (to, from, next) => {
  // Update document title
  if (to.meta.title) {
    document.title = to.meta.title
  }

  // Update meta description
  if (to.meta.description) {
    const metaDescription = document.querySelector('meta[name="description"]')
    if (metaDescription) {
      metaDescription.setAttribute('content', to.meta.description)
    }
  }

  // Only access store for routes that require authentication or guest checks
  if (to.meta.requiresAuth || to.meta.requiresGuest) {
    try {
      // Import the auth store inside the guard to avoid Pinia initialization issues
      const { useAuthStore } = await import('@/stores/auth')
      const authStore = useAuthStore()

      // Check if route requires authentication
      if (to.meta.requiresAuth) {
        // Initialize auth store if not already done
        if (!authStore.isInitialized) {
          await authStore.checkAuth()
        }

        if (!authStore.isAuthenticated) {
          // Redirect to login with return URL
          next({
            name: 'Login',
            query: { redirect: to.fullPath }
          })
          return
        }

        // Check role permissions
        if (to.meta.roles && to.meta.roles.length > 0) {
          const userRole = authStore.user?.role
          if (!userRole || !to.meta.roles.includes(userRole)) {
            // Redirect to dashboard or show unauthorized
            next({
              name: 'Dashboard',
              query: { error: 'unauthorized' }
            })
            return
          }
        }
      }

      // Check if route requires guest (not authenticated)
      if (to.meta.requiresGuest && authStore.isAuthenticated) {
        // Redirect authenticated users away from guest-only pages
        const redirectPath = to.query.redirect || '/dashboard'
        next(redirectPath)
        return
      }
    } catch (error) {
      console.error('Router guard error:', error)
      // If there's an error with the store, continue navigation
    }
  }

  // Continue with navigation
  next()
})

// Global after hook for analytics, loading states, etc.
router.afterEach((to, from) => {
  // You can add analytics tracking here
  // Example: gtag('config', 'GA_TRACKING_ID', { page_path: to.path })

  // Log route changes in development
  if (import.meta.env.DEV) {
    console.log(`Navigated from ${from.path} to ${to.path}`)
  }
})

// Handle navigation errors
router.onError((error) => {
  console.error('Router error:', error)

  // You could redirect to an error page or show a notification
  if (error.message.includes('Loading chunk')) {
    // Handle lazy loading errors (chunk loading failed)
    window.location.reload()
  }
})

export default router
