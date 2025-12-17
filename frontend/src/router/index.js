import { createRouter, createWebHistory } from 'vue-router'
import { APP_VERSION } from '@/config/app-version'
import routesV1 from './routes-v1'
import routesV2 from './routes-v2'

// Version-based route selection
const routeMap = {
  v1: routesV1,
  v2: routesV2
}

const routes = routeMap[APP_VERSION] || routesV1

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
          const userRoles = authStore.user?.registeredRoles || []
          const hasRequiredRole = to.meta.roles.some(role => userRoles.includes(role))

          if (!hasRequiredRole) {
            // Redirect to home or show unauthorized
            next({
              path: '/',
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
router.onError(error => {
  console.error('Router error:', error)

  // You could redirect to an error page or show a notification
  if (error.message.includes('Loading chunk')) {
    // Handle lazy loading errors (chunk loading failed)
    window.location.reload()
  }
})

export default router
