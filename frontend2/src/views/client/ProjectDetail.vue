<template>
  <div class="min-h-screen bg-surface-alt py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button
        class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition"
        @click="router.push({ name: 'ClientDashboard' })"
      >
        <ArrowLeft :size="20" />
        {{ t.clientDashboard.backToProjects }}
      </button>

      <div v-if="loading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded mb-6" />
      </div>

      <div v-else-if="error" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600 mb-4">{{ error }}</p>
        <button class="text-brand-brown hover:underline" @click="fetchProject">Try again</button>
      </div>

      <div v-else-if="currentProject" class="space-y-6">
        <!-- Project Header -->
        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-black mb-2">{{ currentProject.title }}</h1>
              <p class="text-gray-500">
                {{ currentProject.location }} • {{ projectTypeLabel(currentProject, locale) }}
              </p>
            </div>
            <ProjectStatusBadge :status="currentProject.status" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.clientDashboard.lotSize }}</p>
              <p class="text-2xl font-bold text-black">{{ currentProject.lotSize }} m²</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.clientDashboard.designBudget }}</p>
              <p class="text-2xl font-bold text-black">{{ formatCurrency(currentProject.designBudget) }}</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.clientDashboard.bidPlural }}</p>
              <p class="text-2xl font-bold text-brand-brown">{{ bidCount }}</p>
            </div>
          </div>

          <div v-if="hasLocation" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.siteLocation }}</h2>
            <SiteLocationMap
              :full-address="currentProject.fullAddress"
              :city="currentProject.city"
              :province="currentProject.province"
              :latitude="currentProject.latitude"
              :longitude="currentProject.longitude"
              :legacy-location="currentProject.location"
            />
          </div>

          <div v-if="galleryImages.length > 0" class="mb-8">
            <BidImageGallery
              :images="galleryImages"
              :title="t.projectDetailArchitect.visualReferences"
              :description="t.projectDetailArchitect.visualReferencesHint"
            />
          </div>

          <div class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.clientDashboard.description }}</h2>
            <p class="text-gray-700 leading-relaxed">{{ currentProject.description }}</p>
          </div>

          <div v-if="groupedDeliverables.length > 0">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.clientDashboard.deliverables }}</h2>
            <div class="space-y-3">
              <div v-for="group in groupedDeliverables" :key="group.categoryKey">
                <p class="text-xs text-gray-400 font-semibold uppercase tracking-wide mb-1.5">
                  {{ t.bidCreate?.deliverableCategories?.[group.categoryKey] }}
                </p>
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="d in group.items"
                    :key="d"
                    class="bg-brand-tan text-brand-brown px-4 py-2 rounded-full text-sm font-medium border border-brand-gold/20"
                  >
                    {{ t.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-6 pt-6 border-t border-gray-100">
            <p class="text-xs text-gray-500 uppercase font-bold mb-2">
              {{ t.projectDetailArchitect.expectedStartDate }}
            </p>
            <p class="text-base text-gray-900 font-medium">
              <span v-if="!currentProject.startDateType || currentProject.startDateType === 'IMMEDIATELY'">
                Immediately
              </span>
              <span v-else>{{ formatDate(currentProject.expectedStartDate) }}</span>
            </p>
          </div>

          <div
            v-if="currentProject.biddingDeadline && currentProject.status === 'OPEN'"
            class="mt-8 pt-6 border-t border-gray-100"
          >
            <BiddingCountdown :deadline="currentProject.biddingDeadline" size="md" />
          </div>

          <div v-if="currentProject.status === 'DRAFT'" class="mt-8 pt-6 border-t border-gray-100">
            <div class="bg-amber-50 rounded-2xl p-5 flex items-center justify-between">
              <div>
                <p class="font-bold text-amber-800">{{ t.clientDashboard.draftBannerTitle }}</p>
                <p class="text-sm text-amber-600 mt-0.5">
                  {{ t.clientDashboard.draftBannerDesc }}
                </p>
              </div>
              <button
                class="flex-shrink-0 px-5 py-2.5 bg-brand-brown text-white rounded-full font-bold text-sm hover:bg-black transition ml-4"
                @click="router.push({ name: 'ProjectCreate' })"
              >
                {{ t.clientDashboard.continueEditing }}
              </button>
            </div>
          </div>

          <div v-if="currentProject.status === 'NEGOTIATION'" class="mt-8 pt-6 border-t border-gray-100">
            <div class="bg-amber-50 rounded-2xl p-5 flex items-center justify-between">
              <div>
                <p class="font-bold text-amber-800">{{ t.clientDashboard.bidAcceptedTitle }}</p>
                <p class="text-sm text-amber-600 mt-0.5">
                  {{ t.clientDashboard.bidAcceptedDesc }}
                </p>
              </div>
              <button
                class="flex-shrink-0 px-5 py-2.5 bg-brand-brown text-white rounded-full font-bold text-sm hover:bg-black transition ml-4"
                @click="router.push(`/client/projects/${currentProject.id}/finalization`)"
              >
                {{ t.clientDashboard.continueToFinalization }}
              </button>
            </div>
          </div>

          <div v-if="currentProject.status === 'REJECTED'" class="mt-8 pt-6 border-t border-gray-100">
            <div class="bg-red-50 rounded-2xl p-5">
              <p class="font-bold text-red-800">{{ t.clientDashboard.projectRejectedTitle }}</p>
              <p class="text-sm text-red-700 mt-1 leading-relaxed">
                {{ currentProject.validationNotes || t.clientDashboard.projectRejectedFallback }}
              </p>
            </div>
          </div>
        </div>

        <!-- Bid Registry -->
        <BidComparisonTable
          v-if="bidCount > 0"
          :bids="projectBids"
          :compare-ids="compareIds"
          @toggle-compare="toggleCompare"
          @view-details="handleViewDetails"
        />

        <div v-else class="bg-white rounded-3xl border border-gray-200 p-12 text-center shadow-soft">
          <FileText :size="64" class="text-gray-300 mx-auto mb-4" />
          <h3 class="text-xl font-bold text-gray-900 mb-2">{{ t.clientDashboard.noBidsYet }}</h3>
          <p class="text-gray-500">{{ t.clientDashboard.noBidsMessage }}</p>
        </div>

        <!-- Comparative Analysis Zone (always visible) -->
        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-6">
            {{ t.clientDashboard.comparativeAnalysis }}
          </h2>

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div
              v-for="(slot, idx) in [{ bid: bidA }, { bid: bidB }]"
              :key="idx"
              class="rounded-2xl border-2 border-dashed p-6 transition"
              :class="slot.bid ? 'border-brand-brown bg-brand-tan/20' : 'border-gray-200 bg-gray-50'"
            >
              <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-3">
                {{ idx === 0 ? t.clientDashboard.subjectA : t.clientDashboard.subjectB }}
              </p>

              <div v-if="slot.bid" class="flex items-start justify-between">
                <div>
                  <p class="font-bold text-black">{{ slot.bid.architectName || 'Architect' }}</p>
                  <p v-if="slot.bid.architectCompany" class="text-sm text-gray-500 mt-0.5">
                    {{ slot.bid.architectCompany }}
                  </p>
                  <p class="text-sm font-medium text-brand-brown mt-2">{{ formatCurrency(slot.bid.bidAmount) }}</p>
                  <p class="text-xs text-gray-500 mt-0.5">{{ slot.bid.proposedTimelineDays || '—' }} days</p>
                </div>
                <button
                  class="text-gray-400 hover:text-red-500 transition p-1"
                  title="Remove"
                  @click="toggleCompare(slot.bid.id)"
                >
                  <X :size="18" />
                </button>
              </div>

              <div v-else class="flex flex-col items-center justify-center py-4 text-center">
                <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center mb-3">
                  <Plus :size="20" class="text-gray-400" />
                </div>
                <p class="text-xs text-gray-400">{{ t.clientDashboard.emptyCompareSlot }}</p>
              </div>
            </div>
          </div>

          <p v-if="!bidA && !bidB" class="text-center text-sm text-gray-400 py-2">
            {{ t.clientDashboard.compareHint }}
          </p>

          <div v-if="bidA && bidB" class="mt-6 pt-6 border-t border-gray-100">
            <BidComparison :bid-a="bidA" :bid-b="bidB" :project="currentProject" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { projectTypeLabel } from '@/constants/projectTaxonomy'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/composables/useI18n'
import { ArrowLeft, FileText, Plus, X } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'
import BidImageGallery from '@/components/bid/BidImageGallery.vue'
import SiteLocationMap from '@/components/project/SiteLocationMap.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'
import BidComparisonTable from '@/components/bid/BidComparisonTable.vue'
import BidComparison from '@/components/bid/BidComparison.vue'
import { DELIVERABLE_GROUPS } from '@/constants/projectDeliverables'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const { currentProject, loading, error } = storeToRefs(projectsStore)
const { projectBids } = storeToRefs(bidsStore)

const compareIds = ref([])

const imageFiles = computed(() => (currentProject.value?.files ?? []).filter(f => f.fileType?.startsWith('image/')))
const galleryImages = computed(() =>
  imageFiles.value.map(file => ({ imageUrl: file.filePath, fileName: file.fileName }))
)

const hasLocation = computed(
  () => !!(currentProject.value?.fullAddress || currentProject.value?.city || currentProject.value?.location)
)

const groupedDeliverables = computed(() => {
  const deliverables = currentProject.value?.deliverables || []
  return DELIVERABLE_GROUPS.map(g => ({
    categoryKey: g.categoryKey,
    items: g.items.filter(d => deliverables.includes(d))
  })).filter(g => g.items.length > 0)
})

const bidCount = computed(() => projectBids.value?.length || 0)
const bidA = computed(() => projectBids.value?.find(b => b.id === compareIds.value[0]) ?? null)
const bidB = computed(() => projectBids.value?.find(b => b.id === compareIds.value[1]) ?? null)

const toggleCompare = bidId => {
  const idx = compareIds.value.indexOf(bidId)
  if (idx !== -1) {
    compareIds.value.splice(idx, 1)
  } else if (compareIds.value.length < 2) {
    compareIds.value.push(bidId)
  }
}

const formatDate = dateString => {
  if (!dateString) return '—'
  return new Date(dateString).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
}

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const fetchProject = async () => {
  try {
    await projectsStore.fetchProjectById(route.params.id)
    await bidsStore.fetchProjectBids(route.params.id)
  } catch (err) {
    console.error('Failed to fetch project:', err)
  }
}

const handleViewDetails = bidId => {
  router.push({
    name: 'BidDetail',
    params: { projectId: route.params.id, bidId }
  })
}

onMounted(() => {
  fetchProject()
})
</script>
