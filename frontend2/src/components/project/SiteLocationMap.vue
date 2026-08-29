<template>
  <div v-if="hasAnyLocation" class="space-y-4">
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-full bg-brand-tan flex items-center justify-center flex-shrink-0">
          <MapPin :size="20" class="text-brand-brown" />
        </div>
        <div>
          <p class="text-base font-semibold text-gray-900">{{ primaryLine }}</p>
          <p v-if="secondaryLine" class="text-sm text-gray-500 mt-0.5">{{ secondaryLine }}</p>
        </div>
      </div>

      <div v-if="mapUrl" class="flex rounded-full bg-gray-100 p-1">
        <button
          v-for="option in mapTypes"
          :key="option.value"
          type="button"
          :class="[
            'px-4 py-1.5 rounded-full text-sm font-medium transition',
            mapType === option.value ? 'bg-white text-brand-brown shadow-sm' : 'text-gray-500 hover:text-gray-700'
          ]"
          @click="selectMapType(option.value)"
        >
          {{ option.label }}
        </button>
      </div>
    </div>

    <div v-if="mapUrl" class="relative h-[420px] rounded-2xl overflow-hidden border border-gray-200 bg-gray-100">
      <!-- Each map type keeps its own frame once opened. Switching maptype changes the iframe
           src, which forces a full reload and a white flash, so the loaded frames are kept
           alive and cross-faded instead — the Embed API is unmetered, so this costs nothing. -->
      <iframe
        v-for="type in mountedTypes"
        :key="type"
        :src="urlFor(type)"
        class="absolute inset-0 w-full h-full border-0 transition-opacity duration-300"
        :class="type === mapType && isLoaded(type) ? 'opacity-100' : 'opacity-0 pointer-events-none'"
        loading="lazy"
        referrerpolicy="no-referrer-when-downgrade"
        allowfullscreen
        :title="t.projectDetailArchitect.siteLocation"
        @load="markLoaded(type)"
      />

      <div
        v-if="!isLoaded(mapType)"
        class="absolute inset-0 flex flex-col items-center justify-center gap-3 bg-gray-100"
      >
        <div class="w-10 h-10 rounded-full bg-brand-tan flex items-center justify-center animate-pulse">
          <MapPin :size="20" class="text-brand-brown" />
        </div>
        <p class="text-sm text-gray-400">{{ t.projectDetailArchitect.mapLoading }}</p>
      </div>
    </div>

    <p v-else class="text-sm text-gray-400 bg-gray-50 border border-gray-200 rounded-2xl px-4 py-6 text-center">
      {{ t.projectDetailArchitect.mapUnavailable }}
    </p>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { MapPin } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { displayProvince } from '@/constants/regions'
import { embedMapUrl } from '@/composables/useGoogleMaps'

const props = defineProps({
  fullAddress: { type: String, default: '' },
  city: { type: String, default: '' },
  province: { type: String, default: '' },
  latitude: { type: [Number, String], default: null },
  longitude: { type: [Number, String], default: null },
  // Projects created before structured addresses existed only have this free-text string
  legacyLocation: { type: String, default: '' }
})

const { t } = useI18n()

const mapType = ref('satellite')
const mapTypes = computed(() => [
  { value: 'satellite', label: t.value.projectDetailArchitect.viewSatellite },
  { value: 'roadmap', label: t.value.projectDetailArchitect.viewRoadmap }
])

const regionLine = computed(() => [props.city, displayProvince(props.province)].filter(Boolean).join(', '))

const primaryLine = computed(() => props.fullAddress || regionLine.value || props.legacyLocation)

const secondaryLine = computed(() => (props.fullAddress ? regionLine.value : ''))

const hasAnyLocation = computed(() => !!primaryLine.value)

const urlFor = type =>
  embedMapUrl({
    latitude: props.latitude,
    longitude: props.longitude,
    address: [props.fullAddress, regionLine.value || props.legacyLocation].filter(Boolean).join(', '),
    mapType: type
  })

const mapUrl = computed(() => urlFor(mapType.value))

// Only the map type actually opened gets a frame, so the second one costs nothing unless asked for.
const mountedTypes = ref([mapType.value])
const loadedTypes = ref([])

const isLoaded = type => loadedTypes.value.includes(type)

let revealTimer = null

// The frame is invisible until its load event fires, so a load event that never arrives would
// hide a perfectly good map behind the placeholder forever. Reveal it regardless after a beat.
const scheduleReveal = () => {
  clearTimeout(revealTimer)
  revealTimer = setTimeout(() => markLoaded(mapType.value), 8000)
}

const markLoaded = type => {
  if (!isLoaded(type)) loadedTypes.value = [...loadedTypes.value, type]
}

const selectMapType = type => {
  if (!mountedTypes.value.includes(type)) mountedTypes.value = [...mountedTypes.value, type]
  mapType.value = type
  if (!isLoaded(type)) scheduleReveal()
}

// A new address swaps every frame's src, so they all have to load again.
watch(
  () => [props.latitude, props.longitude, props.fullAddress],
  () => {
    loadedTypes.value = []
    scheduleReveal()
  }
)

onMounted(scheduleReveal)
onBeforeUnmount(() => clearTimeout(revealTimer))
</script>
