<template>
  <div class="min-h-screen bg-surface-alt py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition" @click="router.back()">
        <ArrowLeft :size="20" />
        Back
      </button>

      <div v-if="loading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded mb-6" />
      </div>

      <div v-else-if="error" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <div class="flex flex-col items-center gap-4">
          <AlertCircle :size="64" class="text-red-400" />
          <p class="text-red-600 mb-2 text-lg font-medium">{{ error }}</p>
          <button class="text-brand-brown hover:underline font-medium" @click="router.back()">Back</button>
        </div>
      </div>

      <div v-else-if="project" class="space-y-6">
        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-black mb-2">{{ project.title }}</h1>
              <p class="text-gray-500">{{ regionLine }} • {{ projectTypeLabel(project, locale) }}</p>
            </div>
            <ProjectStatusBadge :status="project.status" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">
                {{ t.projectDetailArchitect.designBudgetRange }}
              </p>
              <p class="text-2xl font-bold text-black">
                {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
              </p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.floors }}</p>
              <p class="text-2xl font-bold text-black">{{ project.numberOfFloors }}</p>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.lotSize }}</p>
              <p class="text-2xl font-bold text-black">{{ project.lotSize ? `${project.lotSize} m²` : '—' }}</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.buildArea }}</p>
              <p class="text-2xl font-bold text-black">
                {{ project.estimatedBuildArea ? `${project.estimatedBuildArea} m²` : '—' }}
              </p>
              <p v-if="!project.estimatedBuildArea" class="text-xs text-gray-400 mt-1">
                {{ t.projectDetailArchitect.buildAreaNotSpecified }}
              </p>
            </div>
          </div>

          <div class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.scopeOfWork }}</h2>
            <p class="text-gray-700 leading-relaxed">{{ project.scopeOfWork }}</p>
          </div>

          <div v-if="groupedDeliverables.length > 0" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.deliverables }}</h2>
            <div class="flex flex-wrap gap-3">
              <div
                v-for="group in groupedDeliverables"
                :key="group.categoryKey"
                class="bg-gray-50 rounded-2xl px-4 py-3"
              >
                <h4 class="font-bold text-xs text-gray-700 uppercase mb-2">
                  {{ t.bidCreate.deliverableCategories[group.categoryKey] }}
                </h4>
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="value in group.matched"
                    :key="value"
                    class="bg-white border border-brand-gold/30 px-3 py-1.5 rounded-full text-sm font-medium text-gray-800 flex items-center gap-1.5"
                  >
                    <Check :size="12" class="text-brand-brown" />
                    {{ t.bidCreate.deliverableItems[value] || value.replace(/_/g, ' ') }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="project.designPreferences" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.designPreferences }}</h2>
            <p class="text-gray-700 leading-relaxed">{{ project.designPreferences }}</p>
          </div>

          <div class="rounded-2xl p-5 border-2 border-amber-200 bg-amber-50 flex items-center gap-4">
            <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center flex-shrink-0">
              <CalendarDays :size="20" class="text-amber-600" />
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-0.5">
                {{ t.projectDetailArchitect.expectedStartDate }}
              </p>
              <p class="text-base font-bold text-gray-900">
                <span v-if="!project.startDateType || project.startDateType === 'IMMEDIATELY'">
                  {{ t.projectDetailArchitect.immediately }}
                </span>
                <span v-else>{{ formatDate(project.expectedStartDate) }}</span>
              </p>
            </div>
          </div>
        </div>

        <div v-if="hasLocation" class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <h2 class="text-2xl font-bold text-black mb-6">{{ t.projectDetailArchitect.siteLocation }}</h2>
          <SiteLocationMap
            :full-address="project.fullAddress"
            :city="project.city"
            :province="project.province"
            :latitude="project.latitude"
            :longitude="project.longitude"
            :legacy-location="project.location"
          />
        </div>

        <div v-if="galleryImages.length > 0" class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <BidImageGallery
            :images="galleryImages"
            :title="t.projectDetailArchitect.visualReferences"
            :description="t.projectDetailArchitect.visualReferencesHint"
          />
        </div>

        <div v-if="documentFiles.length > 0" class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <h2 class="text-2xl font-bold text-black mb-6">{{ t.projectDetailArchitect.projectDocuments }}</h2>
          <div class="space-y-3">
            <a
              v-for="file in documentFiles"
              :key="file.id"
              :href="file.filePath"
              target="_blank"
              rel="noopener noreferrer"
              class="flex items-center gap-3 p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition border border-gray-200"
            >
              <FileText :size="24" class="text-brand-brown" />
              <div class="flex-1">
                <p class="font-medium text-gray-900">{{ file.fileName }}</p>
                <p class="text-sm text-gray-500">{{ file.fileType }}</p>
              </div>
            </a>
          </div>
        </div>

        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div v-if="project.biddingDeadline" class="mb-6">
            <BiddingCountdown :deadline="project.biddingDeadline" size="lg" />
          </div>

          <div v-if="existingBid" class="space-y-4">
            <div
              v-if="existingBid.status === 'PENDING'"
              class="bg-blue-50 border border-blue-200 rounded-2xl p-6 flex items-start gap-4"
            >
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                <Send :size="24" class="text-blue-600" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-blue-900 mb-1">{{ t.projectDetailArchitect.bidSubmitted }}</p>
                <p class="text-sm text-blue-700">{{ t.projectDetailArchitect.bidSubmittedDesc }}</p>
              </div>
            </div>

            <div
              v-if="existingBid.status === 'DRAFT'"
              class="bg-yellow-50 border border-yellow-200 rounded-2xl p-6 flex items-start gap-4"
            >
              <div class="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center flex-shrink-0">
                <FileText :size="24" class="text-yellow-600" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-yellow-900 mb-1">{{ t.projectDetailArchitect.draftSaved }}</p>
                <p class="text-sm text-yellow-700">{{ t.projectDetailArchitect.draftSavedDesc }}</p>
              </div>
            </div>

            <button
              v-if="existingBid.status === 'DRAFT'"
              class="w-full bg-brand-brown text-white py-4 px-6 rounded-2xl hover:bg-brand-brown-hover transition flex items-center justify-center gap-3 text-lg font-bold"
              @click="goToBid"
            >
              <Send :size="24" />
              {{ t.projectDetailArchitect.draftSaved }}
            </button>

            <button
              v-if="existingBid.status === 'PENDING'"
              disabled
              class="w-full bg-gray-300 text-gray-500 py-4 px-6 rounded-2xl cursor-not-allowed flex items-center justify-center gap-3 text-lg font-bold"
            >
              <Send :size="24" />
              {{ t.projectDetailArchitect.bidSubmitted }}
            </button>

            <div v-if="bidOutcome" class="bg-gray-50 border border-gray-200 rounded-2xl p-6 flex items-start gap-4">
              <div class="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center flex-shrink-0">
                <component :is="bidOutcome.icon" :size="24" class="text-gray-500" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-gray-800 mb-1">{{ bidOutcome.title }}</p>
                <p class="text-sm text-gray-600">{{ bidOutcome.description }}</p>
              </div>
            </div>
          </div>

          <button
            v-else
            :disabled="project.status !== 'OPEN'"
            class="w-full bg-brand-brown text-white py-4 px-6 rounded-2xl hover:bg-brand-brown-hover transition flex items-center justify-center gap-3 text-lg font-bold disabled:bg-gray-300 disabled:cursor-not-allowed disabled:text-gray-500"
            @click="goToBid"
          >
            <Send :size="24" />
            {{ t.projectDetailArchitect.noBidYet }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { projectTypeLabel } from '@/constants/projectTaxonomy'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Send, FileText, AlertCircle, Check, CalendarDays, XCircle, Archive } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'
import BidImageGallery from '@/components/bid/BidImageGallery.vue'
import SiteLocationMap from '@/components/project/SiteLocationMap.vue'
import { displayProvince } from '@/constants/regions'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import { DELIVERABLE_GROUPS } from '@/constants/projectDeliverables'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const project = ref(null)
const loading = ref(false)
const error = ref(null)
const existingBid = ref(null)

const imageFiles = computed(() => (project.value?.files ?? []).filter(f => f.fileType?.startsWith('image/')))

const galleryImages = computed(() =>
  imageFiles.value.map(file => ({ imageUrl: file.filePath, fileName: file.fileName }))
)

const regionLine = computed(() => {
  const structured = [project.value?.city, displayProvince(project.value?.province)].filter(Boolean).join(', ')
  return structured || project.value?.location || ''
})

const hasLocation = computed(() => !!(project.value?.fullAddress || project.value?.city || project.value?.location))

const documentFiles = computed(() => (project.value?.files ?? []).filter(f => !f.fileType?.startsWith('image/')))

// Losing and withdrawn bidders can still open this page, so explain where the project ended up
const bidOutcome = computed(() => {
  const status = existingBid.value?.status
  const projectStatus = project.value?.status
  const copy = t.value.projectDetailArchitect

  if (status === 'REJECTED') {
    return { icon: XCircle, title: copy.bidNotSelected, description: copy.bidNotSelectedDesc }
  }
  if (status === 'WITHDRAWN') {
    return { icon: XCircle, title: copy.bidWithdrawn, description: copy.bidWithdrawnDesc }
  }
  if (['CANCELLED', 'BIDDING_CLOSED', 'NEGOTIATION_EXPIRED'].includes(projectStatus)) {
    return { icon: Archive, title: copy.projectClosed, description: copy.projectClosedDesc }
  }
  return null
})

const groupedDeliverables = computed(() => {
  if (!project.value?.deliverables) return []
  return DELIVERABLE_GROUPS.map(group => ({
    ...group,
    matched: group.items.filter(value => project.value.deliverables.includes(value))
  })).filter(group => group.matched.length > 0)
})

const formatCurrency = value => {
  if (!value) return 'N/A'
  const millions = value / 1000000
  if (millions >= 1) {
    return `Rp ${millions.toFixed(0)}M`
  }
  const thousands = value / 1000
  return `Rp ${thousands.toFixed(0)}K`
}

const formatDate = dateString => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
}

const goToBid = () => {
  router.push({
    name: 'BidCreate',
    params: { projectId: route.params.projectId }
  })
}

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    project.value = await projectsStore.fetchProjectForArchitect(route.params.projectId)

    await bidsStore.fetchMyBids()
    existingBid.value = bidsStore.myBids.find(bid => bid.projectId === parseInt(route.params.projectId))

    // The winning architect negotiates on the finalization page, not here. Redirecting
    // in the view (rather than only in BidCard) also covers direct URLs and the back button.
    if (existingBid.value?.status === 'ACCEPTED' && project.value.status === 'NEGOTIATION') {
      router.replace({ name: 'ArchitectFinalizationView', params: { projectId: route.params.projectId } })
      return
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load project details'
    console.error('Failed to fetch project:', err)
  } finally {
    loading.value = false
  }
})
</script>
