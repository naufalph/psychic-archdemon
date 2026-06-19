export const TEST_USERS = {
  architect: {
    email: process.env.E2E_ARCHITECT_EMAIL || '',
    password: process.env.E2E_TEST_PASSWORD || '',
    role: 'ARCHITECT'
  },
  client: {
    email: process.env.E2E_CLIENT_EMAIL || '',
    password: process.env.E2E_TEST_PASSWORD || '',
    role: 'CLIENT'
  },
  superuser: {
    email: process.env.E2E_SUPERUSER_EMAIL || '',
    password: process.env.E2E_TEST_PASSWORD || '',
    role: 'SUPERUSER'
  }
}

export const API_BASE_URL = process.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const OAUTH_TEST_USERS = {
  google: {
    email: process.env.E2E_GOOGLE_EMAIL || '',
    password: process.env.E2E_GOOGLE_PASSWORD || ''
  },
  linkedin: {
    email: process.env.E2E_LINKEDIN_EMAIL || '',
    password: process.env.E2E_LINKEDIN_PASSWORD || ''
  }
}

export const ROUTES = {
  home: '/',
  login: '/login',
  signup: '/signup',
  authCallback: '/auth/callback',
  verifyEmail: '/verify-email',
  architectDashboard: '/architect/dashboard',
  architectPortfolios: '/architect/portfolios',
  architectOpportunities: '/architect/opportunities',
  architectBids: '/architect/bids',
  architectProfile: '/architect/profile',
  architectOnboarding: '/architect/onboarding',
  clientDashboard: '/client/dashboard',
  clientProjects: '/client/projects',
  clientProjectCreate: '/client/projects/create',
  clientMessages: '/client/messages',
  clientProfile: '/client/profile',
  superuserDashboard: '/superuser/dashboard',
  superuserProjectQueue: '/superuser/projects/queue'
}
