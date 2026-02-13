/**
 * V1 Routes - Original Design
 */

// Import views (lazy-loaded for better performance)
const Home = () => import('@/views/v1/Home.vue')
const Projects = () => import('@/views/v1/projects/ProjectList.vue')
const ProjectDetail = () => import('@/views/v1/projects/ProjectDetail.vue')
const CreateProject = () => import('@/views/v1/projects/CreateProject.vue')
const Architects = () => import('@/views/v1/architects/ArchitectList.vue')
const ArchitectProfile = () => import('@/views/v1/architects/ArchitectProfile.vue')
const Dashboard = () => import('@/views/v1/clients/ClientDashboard.vue')
const ClientLandingPage = () => import('@/views/v1/clients/ClientLandingPage.vue')
const ArchitectLandingPage = () => import('@/views/v1/architects/ArchitectLandingPage.vue')
const Profile = () => import('@/views/v1/user/Profile.vue')
const VerifyEmail = () => import('@/views/v1/auth/VerifyEmail.vue')
const NotFound = () => import('@/views/v1/errors/NotFound.vue')

export default [
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

  // Admin Routes (Superuser Only)
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/v1/admin/AdminDashboard.vue'),
    meta: {
      title: 'Admin Dashboard - Rumantra',
      requiresAuth: true,
      roles: ['SUPERUSER']
    }
  },
  {
    path: '/admin/projects',
    name: 'AdminProjects',
    component: () => import('@/views/v1/admin/AdminProjects.vue'),
    meta: {
      title: 'Project Validation - Rumantra',
      requiresAuth: true,
      roles: ['SUPERUSER']
    }
  },
  {
    path: '/admin/projects/:id',
    name: 'AdminProjectDetail',
    component: () => import('@/views/v1/admin/AdminProjectDetail.vue'),
    props: true,
    meta: {
      title: 'Project Details - Admin - Rumantra',
      requiresAuth: true,
      roles: ['SUPERUSER']
    }
  },

  // Email Verification Route
  {
    path: '/verify-email',
    name: 'VerifyEmail',
    component: VerifyEmail,
    meta: {
      title: 'Verify Email - Rumantra',
      description: 'Verify your email address'
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
