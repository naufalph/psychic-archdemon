<template>
  <div class="bg-white rounded-3xl border border-gray-200 shadow-soft overflow-hidden">
    <div class="p-8 pb-0">
      <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-6">
        {{ t.clientDashboard.bidRegistry }} ({{ pendingBids.length }})
      </h2>
    </div>

    <div v-if="bids.some(b => b.status === 'PENDING')" class="px-8 pb-6 flex flex-wrap items-end gap-4">
      <div>
        <label class="block text-xs font-medium text-gray-500 mb-1.5">{{ t.bidTable.filterByCity }}</label>
        <select
          v-model="cityFilter"
          class="px-4 py-2 border-2 border-gray-200 rounded-full text-sm focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
        >
          <option value="">{{ t.bidTable.allCities }}</option>
          <option v-for="city in availableCities" :key="city" :value="city">{{ city }}</option>
        </select>
      </div>

      <div>
        <label class="block text-xs font-medium text-gray-500 mb-1.5">{{ t.bidTable.priceMin }}</label>
        <div class="relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 text-xs text-gray-400 font-medium">IDR</span>
          <input
            v-model="priceMinDisplay"
            type="text"
            :placeholder="t.bidTable.priceMin"
            class="w-40 pl-10 pr-3 py-2 border-2 border-gray-200 rounded-full text-sm focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
            @input="onPriceMinInput"
          />
        </div>
      </div>

      <div>
        <label class="block text-xs font-medium text-gray-500 mb-1.5">{{ t.bidTable.priceMax }}</label>
        <div class="relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 text-xs text-gray-400 font-medium">IDR</span>
          <input
            v-model="priceMaxDisplay"
            type="text"
            :placeholder="t.bidTable.priceMax"
            class="w-40 pl-10 pr-3 py-2 border-2 border-gray-200 rounded-full text-sm focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
            @input="onPriceMaxInput"
          />
        </div>
      </div>

      <button
        v-if="cityFilter || priceMinDisplay || priceMaxDisplay"
        class="px-4 py-2 text-sm font-medium text-gray-500 hover:text-gray-700 transition"
        @click="clearFilters"
      >
        {{ t.bidTable.clearFilters }}
      </button>
    </div>

    <div v-if="sortedBids.length > 0" class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th
              class="text-left px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider cursor-pointer select-none whitespace-nowrap"
              @click="toggleSort('architectCompany')"
            >
              <span class="inline-flex items-center gap-1">
                {{ t.bidTable.studio }}
                <ChevronUp v-if="sortKey === 'architectCompany' && sortDir === 'asc'" :size="12" />
                <ChevronDown v-else-if="sortKey === 'architectCompany' && sortDir === 'desc'" :size="12" />
              </span>
            </th>
            <th class="text-left px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider whitespace-nowrap">
              {{ t.bidTable.rating }}
            </th>
            <th
              class="text-left px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider cursor-pointer select-none whitespace-nowrap"
              @click="toggleSort('architectCity')"
            >
              <span class="inline-flex items-center gap-1">
                {{ t.bidTable.location }}
                <ChevronUp v-if="sortKey === 'architectCity' && sortDir === 'asc'" :size="12" />
                <ChevronDown v-else-if="sortKey === 'architectCity' && sortDir === 'desc'" :size="12" />
              </span>
            </th>
            <th class="text-left px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider whitespace-nowrap">
              {{ t.bidTable.education }}
            </th>
            <th
              class="text-right px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider cursor-pointer select-none whitespace-nowrap"
              @click="toggleSort('bidAmount')"
            >
              <span class="inline-flex items-center gap-1 justify-end">
                {{ t.bidTable.totalPrice }}
                <ChevronUp v-if="sortKey === 'bidAmount' && sortDir === 'asc'" :size="12" />
                <ChevronDown v-else-if="sortKey === 'bidAmount' && sortDir === 'desc'" :size="12" />
              </span>
            </th>
            <th
              class="text-right px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider cursor-pointer select-none whitespace-nowrap"
              @click="toggleSort('proposedTimelineDays')"
            >
              <span class="inline-flex items-center gap-1 justify-end">
                {{ t.bidTable.totalDuration }}
                <ChevronUp v-if="sortKey === 'proposedTimelineDays' && sortDir === 'asc'" :size="12" />
                <ChevronDown v-else-if="sortKey === 'proposedTimelineDays' && sortDir === 'desc'" :size="12" />
              </span>
            </th>
            <th class="px-6 py-3" />
          </tr>
        </thead>
        <tbody>
          <tr v-for="bid in sortedBids" :key="bid.id" class="border-t border-gray-100 hover:bg-gray-50/50 transition">
            <td class="px-6 py-4">
              <p class="font-bold text-gray-900">{{ bid.architectCompany || bid.architectName || 'Architect' }}</p>
              <p v-if="bid.architectCompany && bid.architectName" class="text-xs text-gray-400 mt-0.5">
                {{ bid.architectName }}
              </p>
            </td>
            <td class="px-6 py-4">
              <span class="inline-flex items-center gap-1 text-gray-300" :title="t.bidTable.ratingComingSoon">
                <Star :size="14" />
                <span class="text-xs">—</span>
              </span>
            </td>
            <td class="px-6 py-4 text-gray-600">
              {{ bid.architectCity || '—' }}
            </td>
            <td class="px-6 py-4">
              <span
                v-if="highestEducation(bid)"
                :title="allEducationTitle(bid)"
                class="inline-flex items-center gap-1.5 px-3 py-1 bg-amber-100 text-amber-900 rounded-full text-xs font-medium border border-amber-200 whitespace-nowrap"
              >
                <GraduationCap :size="12" />
                {{ highestEducation(bid).level }} · {{ highestEducation(bid).universityName }}
              </span>
              <span v-else class="text-gray-400">{{ t.bidTable.noEducation }}</span>
            </td>
            <td class="px-6 py-4 text-right font-bold text-brand-brown whitespace-nowrap">
              {{ formatCurrency(bid.bidAmount) }}
            </td>
            <td class="px-6 py-4 text-right text-gray-700 whitespace-nowrap">
              {{ bid.proposedTimelineDays || '—' }} {{ t.bidComparison.days }}
            </td>
            <td class="px-6 py-4">
              <div class="flex items-center justify-end gap-2">
                <button
                  class="px-3 py-1.5 bg-white border-2 border-brand-gold text-brand-brown rounded-full text-xs font-medium hover:bg-brand-gold hover:text-white transition flex items-center gap-1.5 whitespace-nowrap"
                  @click="$emit('view-details', bid.id)"
                >
                  <Eye :size="14" />
                  {{ t.bidTable.detail }}
                </button>
                <button
                  :class="[
                    'px-3 py-1.5 rounded-full text-xs font-medium flex items-center gap-1.5 transition whitespace-nowrap',
                    isSelected(bid.id) ? 'bg-brand-brown text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  ]"
                  @click="$emit('toggle-compare', bid.id)"
                >
                  <Check v-if="isSelected(bid.id)" :size="14" />
                  <Plus v-else :size="14" />
                  {{ isSelected(bid.id) ? t.bidCard.selectedForCompare : t.bidCard.addToCompare }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="px-8 py-12 text-center">
      <FileText :size="48" class="text-gray-300 mx-auto mb-3" />
      <p class="text-gray-500 font-medium">
        {{ pendingBids.length === 0 ? t.bidTable.noPendingBids : t.bidTable.noBidsMatchFilters }}
      </p>
      <button
        v-if="pendingBids.length > 0 && (cityFilter || priceMinFilter != null || priceMaxFilter != null)"
        class="mt-3 text-sm text-brand-brown font-medium hover:underline"
        @click="clearFilters"
      >
        {{ t.bidTable.clearFilters }}
      </button>
    </div>

    <div v-if="sortedBids.length > 0" class="px-8 py-6 border-t border-gray-100 bg-gray-50/50">
      <div class="grid grid-cols-2 md:grid-cols-6 gap-3">
        <div class="bg-white rounded-2xl p-4 border border-gray-100">
          <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.bidTable.minPrice }}</p>
          <p class="text-sm font-bold text-black">{{ formatCurrency(priceStats.min) }}</p>
        </div>
        <div class="bg-white rounded-2xl p-4 border border-gray-100">
          <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.bidTable.avgPrice }}</p>
          <p class="text-sm font-bold text-black">{{ formatCurrency(priceStats.avg) }}</p>
        </div>
        <div class="bg-white rounded-2xl p-4 border border-gray-100">
          <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.bidTable.maxPrice }}</p>
          <p class="text-sm font-bold text-black">{{ formatCurrency(priceStats.max) }}</p>
        </div>
        <div class="bg-white rounded-2xl p-4 border border-gray-100">
          <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.bidTable.minDuration }}</p>
          <p class="text-sm font-bold text-black">{{ durationStats.min }} {{ t.bidComparison.days }}</p>
        </div>
        <div class="bg-white rounded-2xl p-4 border border-gray-100">
          <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.bidTable.avgDuration }}</p>
          <p class="text-sm font-bold text-black">{{ durationStats.avg }} {{ t.bidComparison.days }}</p>
        </div>
        <div class="bg-white rounded-2xl p-4 border border-gray-100">
          <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.bidTable.maxDuration }}</p>
          <p class="text-sm font-bold text-black">{{ durationStats.max }} {{ t.bidComparison.days }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Eye, Plus, Check, Star, ChevronUp, ChevronDown, FileText, GraduationCap } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { formatIDRDisplay, parseIDRInput } from '@/utils/currencyFormat'

const props = defineProps({
  bids: { type: Array, default: () => [] },
  compareIds: { type: Array, default: () => [] }
})

defineEmits(['toggle-compare', 'view-details'])

const { t } = useI18n()

const cityFilter = ref('')
const priceMinFilter = ref(null)
const priceMaxFilter = ref(null)
const priceMinDisplay = ref('')
const priceMaxDisplay = ref('')
const sortKey = ref('bidAmount')
const sortDir = ref('asc')

const pendingBids = computed(() => props.bids.filter(b => b.status === 'PENDING'))

const availableCities = computed(() => {
  const cities = pendingBids.value.map(b => b.architectCity).filter(Boolean)
  return [...new Set(cities)].sort()
})

const filteredBids = computed(() =>
  pendingBids.value.filter(bid => {
    if (cityFilter.value && bid.architectCity !== cityFilter.value) return false
    if (priceMinFilter.value != null && (bid.bidAmount ?? 0) < priceMinFilter.value) return false
    if (priceMaxFilter.value != null && (bid.bidAmount ?? 0) > priceMaxFilter.value) return false
    return true
  })
)

const sortedBids = computed(() => {
  const list = [...filteredBids.value]
  const dir = sortDir.value === 'asc' ? 1 : -1
  list.sort((a, b) => {
    const key = sortKey.value
    const av = a[key]
    const bv = b[key]
    if (av == null && bv == null) return 0
    if (av == null) return 1
    if (bv == null) return -1
    if (typeof av === 'string') return av.localeCompare(bv) * dir
    return (av - bv) * dir
  })
  return list
})

const isSelected = bidId => props.compareIds.includes(bidId)

const toggleSort = key => {
  if (sortKey.value === key) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortDir.value = 'asc'
  }
}

const onPriceMinInput = event => {
  priceMinDisplay.value = formatIDRDisplay(event.target.value)
  priceMinFilter.value = parseIDRInput(event.target.value)
}

const onPriceMaxInput = event => {
  priceMaxDisplay.value = formatIDRDisplay(event.target.value)
  priceMaxFilter.value = parseIDRInput(event.target.value)
}

const clearFilters = () => {
  cityFilter.value = ''
  priceMinDisplay.value = ''
  priceMaxDisplay.value = ''
  priceMinFilter.value = null
  priceMaxFilter.value = null
}

const average = values => (values.length ? Math.round(values.reduce((sum, v) => sum + v, 0) / values.length) : 0)

const priceStats = computed(() => {
  const values = filteredBids.value.map(b => b.bidAmount).filter(v => v != null)
  if (!values.length) return { min: 0, avg: 0, max: 0 }
  return { min: Math.min(...values), avg: average(values), max: Math.max(...values) }
})

const durationStats = computed(() => {
  const values = filteredBids.value.map(b => b.proposedTimelineDays).filter(v => v != null)
  if (!values.length) return { min: 0, avg: 0, max: 0 }
  return { min: Math.min(...values), avg: average(values), max: Math.max(...values) }
})

const DEGREE_RANK = { S3: 3, S2: 2, S1: 1 }

const highestEducation = bid => {
  const list = bid.architectEducation || []
  if (!list.length) return null
  return [...list].sort((a, b) => (DEGREE_RANK[b.level] || 0) - (DEGREE_RANK[a.level] || 0))[0]
}

const allEducationTitle = bid =>
  (bid.architectEducation || [])
    .map(e => `${e.level} — ${e.universityName}${e.fieldOfStudy ? ` (${e.fieldOfStudy})` : ''}`)
    .join('\n')

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}
</script>
