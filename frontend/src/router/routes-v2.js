/**
 * V2 Routes - New Figma Design
 */

// Auth views
const Signup = () => import('@/views/v2/auth/Signup.vue')
const Login = () => import('@/views/v2/auth/Login.vue')

export default [
  // Auth Routes
  {
    path: '/signup',
    name: 'Signup',
    component: Signup,
    meta: {
      title: 'Sign Up - Rumantra',
      description: 'Create your Rumantra account',
      requiresGuest: true,
      hideNavbar: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: 'Login - Rumantra',
      description: 'Login to your Rumantra account',
      requiresGuest: true,
      hideNavbar: true
    }
  },

  // Catch-all redirect to v1 for now (until v2 pages are ready)
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]
