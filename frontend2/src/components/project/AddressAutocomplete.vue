<template>
  <div class="space-y-2">
    <label class="block text-sm font-medium text-gray-700">
      {{ t.projectCreate.addressSearch }}<span class="text-red-500">*</span>
    </label>
    <p class="text-xs text-gray-500">{{ t.projectCreate.addressSearchHint }}</p>

    <div v-if="autocompleteReady" ref="hostEl" class="rumantra-place-autocomplete" />

    <input
      v-else
      :value="fullAddress"
      required
      type="text"
      :placeholder="t.projectCreate.addressSearchPlaceholder"
      class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none transition"
      @input="$emit('update:fullAddress', $event.target.value)"
    />

    <p v-if="!autocompleteReady && loadFailed" class="text-xs text-gray-400">
      {{ t.projectCreate.addressSearchUnavailable }}
    </p>

    <div
      v-if="resolvedRegion"
      class="flex items-start gap-2 text-sm text-gray-600 bg-gray-50 border border-gray-200 rounded-xl px-4 py-3"
    >
      <MapPin :size="16" class="text-brand-brown flex-shrink-0 mt-0.5" />
      <span>
        <span class="text-gray-400">{{ t.projectCreate.addressResolved }}</span>
        {{ resolvedRegion }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { MapPin } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { loadGoogleMaps, hasMapsKey } from '@/composables/useGoogleMaps'

const props = defineProps({
  fullAddress: { type: String, default: '' },
  city: { type: String, default: '' },
  province: { type: String, default: '' }
})

const emit = defineEmits([
  'update:fullAddress',
  'update:city',
  'update:province',
  'update:latitude',
  'update:longitude'
])

const { t } = useI18n()

const hostEl = ref(null)
const autocompleteReady = ref(false)
const loadFailed = ref(false)

let element = null

const resolvedRegion = computed(() => [props.city, props.province].filter(Boolean).join(', '))

const componentValue = (components, type) => components?.find(c => c.types?.includes(type))?.longText || ''

const applyPlace = place => {
  const components = place.addressComponents

  emit('update:fullAddress', place.formattedAddress || '')
  emit('update:province', componentValue(components, 'administrative_area_level_1'))

  // Regency-based provinces report the city at level 2; Jakarta's kota values arrive as locality.
  emit(
    'update:city',
    componentValue(components, 'administrative_area_level_2') || componentValue(components, 'locality')
  )

  const location = place.location
  if (location) {
    emit('update:latitude', Number(location.lat().toFixed(7)))
    emit('update:longitude', Number(location.lng().toFixed(7)))
  }
}

const onSelect = async event => {
  try {
    const place = event.placePrediction.toPlace()
    // Essentials-tier fields only. Adding a Pro field such as displayName would reprice every
    // autocomplete request in the session at the Enterprise rate, not just this one call.
    await place.fetchFields({ fields: ['formattedAddress', 'addressComponents', 'location'] })
    applyPlace(place)
  } catch (err) {
    console.error('Failed to resolve selected place:', err)
  }
}

onMounted(async () => {
  if (!hasMapsKey()) {
    loadFailed.value = true
    return
  }

  try {
    const maps = await loadGoogleMaps()

    // importLibrary is the documented path, but the places library is already loaded via the
    // loader's libraries= param, so fall back to the namespace if it isn't exposed.
    const places = maps.importLibrary ? await maps.importLibrary('places') : maps.places
    const { PlaceAutocompleteElement } = places || {}
    if (!PlaceAutocompleteElement) throw new Error('PlaceAutocompleteElement unavailable')

    element = new PlaceAutocompleteElement({
      includedRegionCodes: ['id'],
      requestedLanguage: 'id',
      requestedRegion: 'id'
    })
    element.addEventListener('gmp-select', onSelect)

    autocompleteReady.value = true
    // The host div only exists once autocompleteReady flips, so wait for that render.
    await nextTick()
    hostEl.value?.appendChild(element)
  } catch (err) {
    console.error('Places Autocomplete unavailable:', err)
    autocompleteReady.value = false
    loadFailed.value = true
  }
})

onBeforeUnmount(() => {
  element?.removeEventListener('gmp-select', onSelect)
  element?.remove()
  element = null
})
</script>

<style scoped>
/* The element is a web component; these custom properties are its supported styling surface. */
.rumantra-place-autocomplete :deep(gmp-place-autocomplete) {
  width: 100%;
  --gmp-place-autocomplete-input-border-radius: 1rem;
  --gmp-place-autocomplete-input-border: 2px solid rgb(229 231 235);
  --gmp-place-autocomplete-input-padding: 0.75rem 1rem;
  font-family: inherit;
}
</style>
