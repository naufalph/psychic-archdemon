<template>
  <div class="flex flex-col gap-7 animate-brief-rise">
    <div class="grid grid-cols-[repeat(auto-fit,minmax(240px,1fr))] gap-6">
      <div>
        <label :for="ids.province" :class="labelCls">Provinsi</label>
        <div class="relative">
          <select
            :id="ids.province"
            v-model="state.province"
            class="w-full box-border h-12 pl-4 pr-11 border border-hairline rounded-xl bg-white text-body font-semibold outline-none appearance-none cursor-pointer focus:border-ink-900 transition-colors bg-none"
            :class="state.province ? 'text-ink-900' : 'text-ink-400'"
          >
            <option value="">Pilih provinsi</option>
            <option v-for="province in SURVEY_PROVINCES" :key="province" :value="province">{{ province }}</option>
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
        <input :id="ids.city" v-model="state.city" type="text" placeholder="cth. Jakarta Selatan" :class="inputCls" />
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
      <label :for="ids.search" class="block text-body font-medium text-ink-700 mb-1">
        Titik lokasi <span class="font-normal text-ink-400">(opsional)</span>
      </label>
      <p class="text-caption text-ink-400 mt-0 mb-3">Cari area, lalu klik peta untuk menandai titik lokasi proyek.</p>
      <div class="relative">
        <div class="flex gap-2">
          <input
            :id="ids.search"
            v-model="query"
            type="text"
            placeholder="cth. Jakarta Selatan"
            :class="[inputCls, 'flex-1 min-w-0']"
            @keydown.enter.prevent="search"
          />
          <button
            type="button"
            class="flex-none h-12 px-5 rounded-full border border-ink-900 text-ink-900 text-[15px] font-bold hover:bg-black/5 transition-colors disabled:opacity-50"
            :disabled="searching"
            @click="search"
          >
            {{ searching ? 'Mencari…' : 'Cari' }}
          </button>
        </div>
        <div
          v-if="results.length"
          class="absolute inset-x-0 top-14 z-[1000] bg-white border border-hairline rounded-xl shadow-[0_24px_48px_-24px_rgba(0,0,0,.25)] overflow-hidden"
        >
          <button
            v-for="result in results"
            :key="result.place_id"
            type="button"
            class="block w-full text-left px-4 py-3 text-caption leading-snug text-ink-900 border-b border-hairline last:border-b-0 hover:bg-surface-alt"
            @click="pickResult(result)"
          >
            {{ result.display_name }}
          </button>
        </div>
        <p v-if="searchError" class="text-caption text-[#B42318] mt-2 mb-0">{{ searchError }}</p>
      </div>
      <div
        ref="mapEl"
        class="mt-3 h-[340px] border border-hairline rounded-2xl overflow-hidden bg-surface-alt relative z-0"
      ></div>
      <div v-if="state.locationLabel" class="flex gap-2.5 items-start mt-3 text-caption leading-normal text-ink-900">
        <MapPin :size="18" :stroke-width="2" class="flex-none mt-px" />
        <span>{{ state.locationLabel }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, useId } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { ChevronDown, MapPin } from 'lucide-vue-next'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { SURVEY_PROVINCES } from '@/constants/projectSurvey'

const { state } = useProjectSurvey()

const ids = { province: useId(), city: useId(), address: useId(), search: useId() }
const labelCls = 'block text-body font-medium text-ink-700 mb-2.5'
const inputCls =
  'w-full box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors'

// OpenStreetMap's free Nominatim service: no key and no billing, but its usage policy asks for
// light, user-initiated traffic, so lookups only run on an explicit search or map click
const NOMINATIM = 'https://nominatim.openstreetmap.org'
const JAKARTA = [-6.2, 106.82]

const mapEl = ref(null)
const query = ref(state.locationLabel || '')
const results = ref([])
const searching = ref(false)
const searchError = ref('')
let map = null
let marker = null

const pinIcon = L.divIcon({
  className: '',
  html: '<div style="width:22px;height:22px;border-radius:999px;background:#0A0A0A;border:4px solid #fff;box-shadow:0 2px 10px rgba(0,0,0,.35)"></div>',
  iconSize: [22, 22],
  iconAnchor: [11, 11]
})

const placePin = (lat, lng) => {
  if (marker) marker.setLatLng([lat, lng])
  else marker = L.marker([lat, lng], { icon: pinIcon }).addTo(map)
  state.latitude = Number(lat.toFixed(7))
  state.longitude = Number(lng.toFixed(7))
}

const reverseLookup = async (lat, lng) => {
  const fallback = `${lat.toFixed(5)}, ${lng.toFixed(5)}`
  try {
    const res = await fetch(`${NOMINATIM}/reverse?format=json&accept-language=id&lat=${lat}&lon=${lng}`)
    const data = await res.json()
    state.locationLabel = data?.display_name || fallback
  } catch {
    state.locationLabel = fallback
  }
  query.value = state.locationLabel
}

const search = async () => {
  const q = query.value.trim()
  if (!q) return
  searching.value = true
  searchError.value = ''
  try {
    const res = await fetch(
      `${NOMINATIM}/search?format=json&accept-language=id&countrycodes=id&limit=5&q=${encodeURIComponent(q)}`
    )
    const list = await res.json()
    results.value = Array.isArray(list) ? list : []
    if (!results.value.length) searchError.value = 'Lokasi tidak ditemukan. Coba kata kunci lain.'
  } catch {
    results.value = []
    searchError.value = 'Pencarian gagal. Anda tetap bisa menandai lokasi langsung di peta.'
  } finally {
    searching.value = false
  }
}

const pickResult = result => {
  const lat = parseFloat(result.lat)
  const lng = parseFloat(result.lon)
  results.value = []
  query.value = result.display_name
  state.locationLabel = result.display_name
  map.setView([lat, lng], 16)
  placePin(lat, lng)
}

onMounted(() => {
  const hasPin = state.latitude != null && state.longitude != null
  map = L.map(mapEl.value, { zoomControl: true, attributionControl: true }).setView(
    hasPin ? [state.latitude, state.longitude] : JAKARTA,
    hasPin ? 16 : 11
  )
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap'
  }).addTo(map)
  if (hasPin) placePin(state.latitude, state.longitude)
  map.on('click', event => {
    placePin(event.latlng.lat, event.latlng.lng)
    reverseLookup(event.latlng.lat, event.latlng.lng)
  })
  // The step animates in, so Leaflet measures the container before it has its final size
  setTimeout(() => map?.invalidateSize(), 250)
})

onBeforeUnmount(() => {
  map?.remove()
  map = null
  marker = null
})
</script>
