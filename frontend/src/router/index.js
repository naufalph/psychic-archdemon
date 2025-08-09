import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Import views (lazy-loaded for better performance)
const Home = () => import('@/views/Home.vue')
const About = () => import('@/views/About.vue')
const Login = () => import('@/views/auth/Login.vue')
const Register = () => import('@/views/auth/Register.vue')
const Projects = () => import('@/views/projects/ProjectList.vue')
const ProjectDetail = () => import('@/views/projects/ProjectDetail.vue')
const CreateProject = () => import('@/views/projects/CreateProject.vue')
const Architects = () => import('@/views/architects/ArchitectList.vue')
const ArchitectProfile = () => import('@/views/architects/ArchitectProfile.vue')
const Dashboard = () => import('@/views/dashboard/Dashboard.vue')
const Profile = () => import('@/views/user/Profile.vue')
const AdminDashboard = () => import('@/views/admin/AdminDashboard.vue')
const NotFound = () => import('@/views/errors/NotFound.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: 'Home - ArchMatch',
      description: 'Connect with talented architects for your next project'
    }
  },
  {
    path: '/about',
    name: 'About',
    component: About,
    meta: {
      title: 'About - ArchMatch',
      description: 'Learn more about our platform and mission'
    }
  },

  // Authentication Routes
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: 'Sign In - ArchMatch',
      requiresGuest: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: 'Sign Up - ArchMatch',
      requiresGuest: true
    }
  },

  // Project Routes
  {
    path: '/projects',
    name: 'Projects',
    component: Projects,
    meta: {
      title: 'Projects - ArchMatch',
      description: 'Browse available architecture projects'
    }
  },
  {
    path: '/projects/create',
    name: 'CreateProject',
    component: CreateProject,
    meta: {
      title: 'Create Project - ArchMatch',
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
      title: 'Project Details - ArchMatch'
    }
  },

  // Architect Routes
  {
    path: '/architects',
    name: 'Architects',
    component: Architects,
    meta: {
      title: 'Architects - ArchMatch',
      description: 'Discover talented architects'
    }
  },
  {
    path: '/architects/:id',
    name: 'ArchitectProfile',
    component: ArchitectProfile,
    props: true,
    meta: {
      title: 'Architect Profile - ArchMatch'
    }
  },

  // User Dashboard Routes
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: {
      title: 'Dashboard - ArchMatch',
      requiresAuth: true
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: {
      title: 'My Profile - ArchMatch',
      requiresAuth: true
    }
  },

  // Admin Routes
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: {
      title: 'Admin Dashboard - ArchMatch',
      requiresAuth: true,
      roles: ['ADMIN']
    }
  },

  // Error Routes
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: 'Page Not Found - ArchMatch'
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
  const authStore = useAuthStore()

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
