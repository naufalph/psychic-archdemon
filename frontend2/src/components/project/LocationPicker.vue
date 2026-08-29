<template>
  <div class="space-y-2">
    <label class="block text-sm font-medium text-gray-700">{{ t.projectCreate.pinLocation }}</label>
    <p class="text-xs text-gray-500">{{ t.projectCreate.pinLocationHint }}</p>

    <div v-if="!available" class="text-sm text-gray-400 bg-gray-50 border border-gray-200 rounded-2xl px-4 py-6">
      {{ t.projectCreate.pinLocationUnavailable }}
    </div>

    <template v-else>
      <div ref="mapEl" class="w-full h-[360px] rounded-2xl overflow-hidden border-2 border-gray-200 bg-gray-100" />

      <div class="flex flex-wrap items-center justify-between gap-3">
        <p v-if="hasPin" class="text-xs text-gray-500 font-mono">
          {{ Number(latitude).toFixed(6) }}, {{ Number(longitude).toFixed(6) }}
        </p>
        <p v-else class="text-xs text-gray-400">{{ t.projectCreate.pinLocationNotSet }}</p>

        <button
          v-if="hasPin"
          type="button"
          class="text-xs font-medium text-gray-500 hover:text-brand-brown transition"
          @click="clearPin"
        >
          {{ t.projectCreate.pinLocationClear }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from '@/composables/useI18n'
import { loadGoogleMaps, hasMapsKey } from '@/composables/useGoogleMaps'

const props = defineProps({
  latitude: { type: [Number, String], default: null },
  longitude: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:latitude', 'update:longitude'])

const { t } = useI18n()

// Roughly Indonesia's centre — only used until an address is picked or a pin dropped
const FALLBACK_CENTER = { lat: -2.5, lng: 118 }

const mapEl = ref(null)
const available = ref(hasMapsKey())
const hasPin = computed(() => props.latitude != null && props.longitude != null)

let map = null
let marker = null

const setPin = ({ lat, lng }) => {
  emit('update:latitude', Number(lat.toFixed(7)))
  emit('update:longitude', Number(lng.toFixed(7)))
}

const clearPin = () => {
  emit('update:latitude', null)
  emit('update:longitude', null)
  if (marker) marker.setMap(null)
  marker = null
}

const placeMarker = position => {
  if (!map) return
  if (marker) {
    marker.setPosition(position)
    return
  }
  marker = new window.google.maps.Marker({ map, position, draggable: true })
  marker.addListener('dragend', event => setPin({ lat: event.latLng.lat(), lng: event.latLng.lng() }))
}

onMounted(async () => {
  if (!available.value) return

  try {
    await loadGoogleMaps()
  } catch {
    available.value = false
    return
  }

  const gmaps = window.google.maps
  const center = hasPin.value ? { lat: Number(props.latitude), lng: Number(props.longitude) } : FALLBACK_CENTER

  map = new gmaps.Map(mapEl.value, {
    center,
    zoom: hasPin.value ? 18 : 5,
    mapTypeId: 'satellite',
    mapTypeControl: true,
    streetViewControl: false,
    fullscreenControl: true
  })
  if (hasPin.value) placeMarker(center)

  map.addListener('click', event => {
    const position = { lat: event.latLng.lat(), lng: event.latLng.lng() }
    setPin(position)
    placeMarker(position)
  })
})

// An address chosen in the autocomplete arrives here as new coordinates; recentre so the
// client sees the site rather than having to hunt for the moved pin.
watch(
  () => [props.latitude, props.longitude],
  ([lat, lng]) => {
    if (lat == null || lng == null) return
    const position = { lat: Number(lat), lng: Number(lng) }
    placeMarker(position)
    if (map) {
      map.setCenter(position)
      if (map.getZoom() < 17) map.setZoom(18)
    }
  }
)
</script>
