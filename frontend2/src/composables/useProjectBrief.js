const STORAGE_KEY = 'rumantra:project-brief-token:v2'

/**
 * The brief itself lives server-side; only its claim token is kept here. The token also
 * rides on the post-auth redirect URL, which is what lets a brief survive the email
 * verification round-trip onto a different browser.
 */
export function useProjectBrief() {
  const clearToken = () => {
    try {
      window.localStorage.removeItem(STORAGE_KEY)
    } catch {
      // Private mode / storage disabled — nothing to clean up
    }
  }

  const saveToken = token => {
    if (!token) return false
    try {
      window.localStorage.setItem(STORAGE_KEY, token)
      return true
    } catch {
      return false
    }
  }

  const storedToken = () => {
    try {
      return window.localStorage.getItem(STORAGE_KEY) || null
    } catch {
      return null
    }
  }

  // The URL wins: it is the copy that survived a device switch, so it is the fresher one.
  const tokenFromRoute = route => {
    const fromQuery = route?.query?.brief
    if (typeof fromQuery === 'string' && fromQuery.trim()) return fromQuery.trim()
    return storedToken()
  }

  /**
   * Where a just-authenticated user should land when a brief is still waiting for them.
   * Every post-auth entry point (password login and the OAuth callback alike) has to
   * consult this, or the brief is silently stranded and the user sees an empty form.
   */
  const pendingBriefPath = user => {
    const token = storedToken()
    if (!token) return null
    if (!user?.registeredRoles?.includes('CLIENT')) return null
    return `/client/projects/create?brief=${encodeURIComponent(token)}`
  }

  return { saveToken, storedToken, tokenFromRoute, pendingBriefPath, clearToken }
}
