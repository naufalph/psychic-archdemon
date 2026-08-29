const MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || ''

let loaderPromise = null

export const hasMapsKey = () => !!MAPS_API_KEY

export const mapsApiKey = () => MAPS_API_KEY

/**
 * Loads the Maps JavaScript API once per page and hands every later caller the same
 * promise. Only the interactive pin picker needs this metered API — read-only maps use
 * the free Embed API iframe instead, which needs no script at all.
 */
export const loadGoogleMaps = () => {
  // google.maps exists as a stub well before the API is usable, so readiness is keyed on a
  // real constructor rather than on the namespace merely being present.
  if (window.google?.maps?.Map) return Promise.resolve(window.google.maps)
  if (loaderPromise) return loaderPromise

  if (!MAPS_API_KEY) {
    return Promise.reject(new Error('VITE_GOOGLE_MAPS_API_KEY is not set'))
  }

  loaderPromise = new Promise((resolve, reject) => {
    // With loading=async the script's onload fires before the API finishes initialising, so
    // the documented callback is the only reliable ready signal.
    const callbackName = '__rumantraGoogleMapsReady'
    window[callbackName] = () => {
      delete window[callbackName]
      resolve(window.google.maps)
    }

    const script = document.createElement('script')
    script.src =
      `https://maps.googleapis.com/maps/api/js?key=${MAPS_API_KEY}` +
      `&libraries=marker,places&loading=async&callback=${callbackName}`
    script.async = true
    script.onerror = () => {
      // Let a later attempt retry rather than caching the failure forever
      loaderPromise = null
      delete window[callbackName]
      reject(new Error('Failed to load Google Maps'))
    }
    document.head.appendChild(script)
  })

  return loaderPromise
}

/**
 * Builds a free Maps Embed API URL. Coordinates win when present; otherwise the address
 * string is geocoded by the embed itself, at no extra API cost.
 */
export const embedMapUrl = ({ latitude, longitude, address, mapType = 'satellite', zoom = 18 }) => {
  if (!MAPS_API_KEY) return ''

  const query =
    latitude != null && longitude != null ? `${latitude},${longitude}` : (address || '').trim()
  if (!query) return ''

  const params = new URLSearchParams({
    key: MAPS_API_KEY,
    q: query,
    maptype: mapType,
    zoom: String(zoom)
  })
  return `https://www.google.com/maps/embed/v1/place?${params.toString()}`
}
