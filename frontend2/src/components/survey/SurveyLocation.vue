<template>
  <div class="flex flex-col gap-7 animate-brief-rise">
    <div class="grid grid-cols-[repeat(auto-fit,minmax(240px,1fr))] gap-6">
      <div>
        <label :for="ids.province" :class="labelCls">Provinsi</label>
        <div class="relative">
          <select
            :id="ids.province"
            :value="state.province"
            :class="[selectCls, state.province ? 'text-ink-900' : 'text-ink-400']"
            @change="onProvince($event.target.value)"
          >
            <option value="">Pilih provinsi</option>
            <option v-for="p in PROVINCES" :key="p.value" :value="p.value">{{ p.label }}</option>
          </select>
          <ChevronDown
            :size="18"
            :stroke-width="2"
            class="absolute right-4 top-[15px] pointer-events-none text-ink-400"
          />
        </div>
      </div>
      <div>
        <label :for="ids.city" :class="labelCls">Kota / Kabupaten</label>
        <div class="relative">
          <select
            :id="ids.city"
            v-model="state.city"
            :class="[
              selectCls,
              state.city ? 'text-ink-900' : 'text-ink-400',
              'disabled:bg-surface-alt disabled:cursor-not-allowed'
            ]"
            :disabled="!state.province"
          >
            <option value="">{{ state.province ? 'Pilih kota / kabupaten' : 'Pilih provinsi dulu' }}</option>
            <option v-for="c in cities" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
          <ChevronDown
            :size="18"
            :stroke-width="2"
            class="absolute right-4 top-[15px] pointer-events-none text-ink-400"
          />
        </div>
      </div>
    </div>

    <div class="flex gap-3.5 items-start px-5 py-[18px] rounded-2xl bg-surface-alt">
      <MapPin :size="20" :stroke-width="2" class="flex-none mt-px text-ink-900" />
      <p class="text-[15px] leading-relaxed text-ink-700 m-0">
        <strong class="text-ink-900">Semakin detail alamatnya, semakin tepat desainnya.</strong>
        Dengan alamat dan titik lokasi, arsitek atau desainer dapat melihat bangunan di sekitarnya, arah matahari, akses
        jalan, dan kondisi lapangan sebelum mulai merancang.
      </p>
    </div>

    <div>
      <label :for="ids.address" :class="labelCls">
        Alamat lengkap <span class="font-normal text-ink-400">(opsional)</span>
      </label>
      <textarea
        :id="ids.address"
        v-model="state.address"
        rows="3"
        placeholder="cth. Jl. Kemang Raya No. 12, RT 03/RW 05, Bangka, Mampang Prapatan, Jakarta Selatan 12730"
        class="w-full box-border px-4 py-3.5 border border-hairline rounded-xl bg-white text-body leading-relaxed text-ink-900 placeholder-ink-300 outline-none resize-y focus:border-ink-900 transition-colors"
      ></textarea>
    </div>

    <div>
      <p class="text-body font-medium text-ink-700 mt-0 mb-1">
        Titik lokasi <span class="font-normal text-ink-400">(opsional)</span>
      </p>
      <p class="text-caption text-ink-400 mt-0 mb-3">Cari alamat, lalu klik peta untuk menandai titik lokasi proyek.</p>
      <p
        v-if="!available"
        class="text-caption text-ink-400 m-0 px-4 py-6 rounded-2xl border border-hairline bg-surface-alt"
      >
        Peta tidak tersedia saat ini. Isi alamat lengkap di atas agar arsitek tetap dapat menemukan lokasinya.
      </p>
      <template v-else>
        <div ref="searchHost" class="survey-place-autocomplete"></div>
        <div
          ref="mapEl"
          class="mt-3 h-[340px] border border-hairline rounded-2xl overflow-hidden bg-surface-alt relative z-0"
        ></div>
      </template>
      <div v-if="state.locationLabel" class="flex gap-2.5 items-start mt-3 text-caption leading-normal text-ink-900">
        <MapPin :size="18" :stroke-width="2" class="flex-none mt-px" />
        <span>{{ state.locationLabel }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, useId } from 'vue'
import { ChevronDown, MapPin } from 'lucide-vue-next'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { loadGoogleMaps, hasMapsKey } from '@/composables/useGoogleMaps'
import { PROVINCES, citiesFor } from '@/constants/regions'

const { state } = useProjectSurvey()

const ids = { province: useId(), city: useId(), address: useId() }
const labelCls = 'block text-body font-medium text-ink-700 mb-2.5'
const selectCls =
  'w-full box-border h-12 pl-4 pr-11 border border-hairline rounded-xl bg-white text-body font-semibold outline-none appearance-none cursor-pointer focus:border-ink-900 transition-colors bg-none'

const cities = computed(() => citiesFor(state.province))

// A city only belongs to its own province, so it is dropped when the province moves
const onProvince = value => {
  state.province = value
  if (!citiesFor(value).some(c => c.value === state.city)) state.city = ''
}

const JAKARTA = { lat: -6.2, lng: 106.82 }

const mapEl = ref(null)
const searchHost = ref(null)
const available = ref(hasMapsKey())
let map = null
let marker = null
let autocomplete = null

const coordinateLabel = ({ lat, lng }) => `${lat.toFixed(5)}, ${lng.toFixed(5)}`

const placePin = position => {
  if (marker) marker.setPosition(position)
  else {
    marker = new window.google.maps.Marker({ map, position, draggable: true })
    marker.addListener('dragend', event => {
      const moved = { lat: event.latLng.lat(), lng: event.latLng.lng() }
      placePin(moved)
      state.locationLabel = coordinateLabel(moved)
    })
  }
  state.latitude = Number(position.lat.toFixed(7))
  state.longitude = Number(position.lng.toFixed(7))
}

const onSelect = async event => {
  try {
    const place = event.placePrediction.toPlace()
    // Essentials-tier fields only. A Pro field such as displayName would reprice every
    // autocomplete request in the session at the Enterprise rate, not just this one call.
    await place.fetchFields({ fields: ['formattedAddress', 'location'] })
    if (!place.location) return
    const position = { lat: place.location.lat(), lng: place.location.lng() }
    placePin(position)
    map.setCenter(position)
    if (map.getZoom() < 17) map.setZoom(18)
    state.locationLabel = place.formattedAddress || coordinateLabel(position)
    if (!state.address.trim()) state.address = place.formattedAddress || ''
  } catch (err) {
    console.error('Failed to resolve selected place:', err)
  }
}

onMounted(async () => {
  if (!available.value) return
  let maps
  try {
    maps = await loadGoogleMaps()
  } catch {
    available.value = false
    return
  }
  if (!mapEl.value) return

  const hasPin = state.latitude != null && state.longitude != null
  const center = hasPin ? { lat: Number(state.latitude), lng: Number(state.longitude) } : JAKARTA
  map = new maps.Map(mapEl.value, {
    center,
    zoom: hasPin ? 18 : 11,
    mapTypeId: 'satellite',
    mapTypeControl: true,
    streetViewControl: false,
    fullscreenControl: true
  })
  if (hasPin) placePin(center)
  map.addListener('click', event => {
    const position = { lat: event.latLng.lat(), lng: event.latLng.lng() }
    placePin(position)
    state.locationLabel = coordinateLabel(position)
  })

  try {
    const places = maps.importLibrary ? await maps.importLibrary('places') : maps.places
    const { PlaceAutocompleteElement } = places || {}
    if (!PlaceAutocompleteElement) throw new Error('PlaceAutocompleteElement unavailable')
    autocomplete = new PlaceAutocompleteElement({
      includedRegionCodes: ['id'],
      requestedLanguage: 'id',
      requestedRegion: 'id'
    })
    autocomplete.addEventListener('gmp-select', onSelect)
    searchHost.value?.appendChild(autocomplete)
  } catch (err) {
    // The map still takes a pin by click, so a missing search box is not fatal
    console.error('Places Autocomplete unavailable:', err)
  }
})

onBeforeUnmount(() => {
  autocomplete?.removeEventListener('gmp-select', onSelect)
  autocomplete?.remove()
  autocomplete = null
  marker?.setMap(null)
  marker = null
  map = null
})
</script>

<style scoped>
/* The element is a web component; these custom properties are its supported styling surface. */
.survey-place-autocomplete :deep(gmp-place-autocomplete) {
  width: 100%;
  --gmp-place-autocomplete-input-border-radius: 0.75rem;
  --gmp-place-autocomplete-input-border: 1px solid #e8e8e8;
  font-family: inherit;
  color-scheme: light;
  background-color: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 0.75rem;
  min-height: 3rem;
}

.survey-place-autocomplete :deep(gmp-place-autocomplete:focus-within) {
  border-color: #0a0a0a;
}
</style>
