/**
 * Test fixtures for E2E tests
 */

export const TEST_USERS = {
  architect: {
    email: 'test.architect1@rumantra.com',
    password: 'password123',
    role: 'ARCHITECT'
  },
  client: {
    email: 'test.client1@rumantra.com',
    password: 'password123',
    role: 'CLIENT'
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
  architectDashboard: '/architect/dashboard',
  clientDashboard: '/client/dashboard'
}
