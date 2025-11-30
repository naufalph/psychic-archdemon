/**
 * Application Version Configuration
 *
 * Controls which version of the UI is loaded.
 * Options: 'v1', 'v2'
 *
 * Can be controlled via:
 * 1. Environment variable: VITE_APP_VERSION
 * 2. LocalStorage: 'app-version'
 * 3. URL parameter: ?version=v2
 */

// Check URL parameter first
const urlParams = new URLSearchParams(window.location.search)
const urlVersion = urlParams.get('version')

// Check localStorage
const storedVersion = localStorage.getItem('app-version')

// Check environment variable, fallback to v1
const envVersion = import.meta.env.VITE_APP_VERSION

// Priority: URL > localStorage > env > default
export const APP_VERSION = urlVersion || storedVersion || envVersion || 'v1'

// Save to localStorage if set via URL
if (urlVersion && urlVersion !== storedVersion) {
  localStorage.setItem('app-version', urlVersion)
}

// Helper function to switch versions at runtime
export function setAppVersion(version) {
  if (!['v1', 'v2'].includes(version)) {
    console.warn(`Invalid version: ${version}. Must be 'v1' or 'v2'`)
    return false
  }

  localStorage.setItem('app-version', version)
  window.location.reload() // Reload to apply new version
  return true
}

// Helper to get versioned view path
export function getVersionedPath(viewPath) {
  return `@/views/${APP_VERSION}/${viewPath}`
}

console.log(`🎨 Running app version: ${APP_VERSION}`)
