<template>
  <div class="min-h-screen bg-surface-alt py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition" @click="router.back()">
        <ArrowLeft :size="20" />
        {{ t.bidDetail.backToProject }}
      </button>

      <div v-if="loading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded mb-6" />
      </div>

      <div v-else-if="error" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600 mb-4">{{ error }}</p>
        <button class="text-brand-brown hover:underline" @click="fetchBid">Try again</button>
      </div>

      <div v-else-if="currentBid" class="lg:grid lg:grid-cols-3 gap-6">
        <!-- Left sidebar: Project Summary -->
        <div class="lg:col-span-1 mb-6 lg:mb-0">
          <div class="lg:sticky lg:top-6 bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
            <h2 class="text-xl font-bold text-black mb-4">
              {{ t.bidCreate?.projectSummary || 'Project Summary' }}
            </h2>
            <div class="space-y-4">
              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.bidCreate?.project || 'Project' }}
                </p>
                <p class="font-bold text-gray-900">{{ currentBid.projectTitle || currentProject?.title }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.bidCreate?.location || 'Location' }}
                </p>
                <p class="text-gray-900">{{ currentBid.projectLocation || currentProject?.location }}</p>
              </div>

              <template v-if="currentProject">
                <div>
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                    {{ t.bidCreate?.designBudget || 'Design Budget' }}
                  </p>
                  <p class="text-gray-900 font-medium">{{ formatCurrency(currentProject.designBudget) }}</p>
                </div>

                <div v-if="currentProject.estimatedBuildArea">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                    {{ t.bidCreate?.buildArea || 'Lot Size' }}
                  </p>
                  <p class="text-gray-900">{{ currentProject.estimatedBuildArea }} m²</p>
                </div>

                <div>
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                    {{ t.bidCreate?.buildingType || 'Building Type' }}
                  </p>
                  <p class="text-gray-900">{{ projectTypeLabel(currentProject, locale) }}</p>
                </div>

                <div v-if="groupedProjectDeliverables.length > 0">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-2">
                    {{ t.bidCreate?.deliverables || 'Deliverables' }}
                  </p>
                  <div class="space-y-2">
                    <div v-for="group in groupedProjectDeliverables" :key="group.categoryKey">
                      <p class="text-xs text-gray-400 font-semibold mb-1">
                        {{ t.bidCreate?.deliverableCategories?.[group.categoryKey] }}
                      </p>
                      <div class="flex flex-wrap gap-1">
                        <span
                          v-for="d in group.items"
                          :key="d"
                          class="bg-brand-tan text-brand-brown px-2 py-0.5 rounded-full text-xs font-medium"
                        >
                          {{ t.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- Right: Bid content -->
        <div class="lg:col-span-2 space-y-6">
          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <div class="flex justify-between items-start mb-6">
              <div>
                <h1 class="text-3xl font-bold text-black mb-2">
                  {{ currentBid.architectName || 'Architect' }}
                </h1>
                <p v-if="currentBid.architectCompany" class="text-gray-500">
                  {{ currentBid.architectCompany }}
                </p>
                <p class="text-sm text-gray-400 mt-2">
                  {{ t.bidDetail.submittedOn.replace('{date}', formatDate(currentBid.submittedAt)) }}
                </p>
              </div>
              <BidStatusBadge :status="currentBid.status" />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="bg-gray-50 rounded-2xl p-6">
                <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.bidDetail.proposedCost }}</p>
                <p class="text-3xl font-bold text-brand-brown">{{ formatCurrency(currentBid.bidAmount) }}</p>
              </div>
              <div class="bg-gray-50 rounded-2xl p-6">
                <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.bidDetail.timeline }}</p>
                <p class="text-3xl font-bold text-black">
                  {{ currentBid.proposedTimelineDays }} {{ t.bidDetail.days }}
                </p>
              </div>
            </div>
          </div>

          <div v-if="currentBid.proposal" class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <h2 class="text-2xl font-bold text-black mb-4">{{ t.bidDetail.studioOverview }}</h2>
            <p class="text-gray-700 leading-relaxed whitespace-pre-line">{{ currentBid.proposal }}</p>
          </div>

          <div
            v-if="currentBid.details?.conceptStatement"
            class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
          >
            <h2 class="text-2xl font-bold text-black mb-4">{{ t.bidDetail.designConcept }}</h2>
            <p class="text-gray-700 leading-relaxed whitespace-pre-line">
              {{ currentBid.details.conceptStatement }}
            </p>
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <BidImageGallery
              :images="currentBid.facadeImages"
              :title="t.bidDetail.facade"
              :description="currentBid.details?.facadeDescription"
              :empty-message="t.bidDetail.noFacade"
            />
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <BidImageGallery
              :images="currentBid.interiorImages"
              :title="t.bidDetail.interior"
              :description="currentBid.details?.interiorDescription"
              :empty-message="t.bidDetail.noInterior"
            />
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <BidImageGallery
              :images="currentBid.massingImages"
              :title="t.bidDetail.massing"
              :description="currentBid.details?.massingDescription"
              :empty-message="t.bidDetail.noMassing"
            />
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <BidImageGallery
              :images="currentBid.zoningImages"
              :title="t.bidDetail.zoning"
              :description="currentBid.details?.zoningDescription"
              :empty-message="t.bidDetail.noZoning"
            />
          </div>

          <div
            v-if="currentBid.details?.phases?.length"
            class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
          >
            <h2 class="text-2xl font-bold text-black mb-4">{{ t.bidDetail.paymentSchedule }}</h2>

            <div class="flex items-center gap-3 mb-5 flex-wrap">
              <span
                class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-green-100 text-green-700 border border-green-200 cursor-default"
                :title="t.bidDetail.matchingDeliverable"
              >
                <span class="w-1.5 h-1.5 rounded-full bg-green-500 inline-block" />
                {{ t.bidDetail.matchingDeliverable }}
              </span>
              <span
                class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-blue-100 text-blue-700 border border-blue-200 cursor-default"
                :title="t.bidDetail.additionalDeliverable"
              >
                <span class="w-1.5 h-1.5 rounded-full bg-blue-500 inline-block" />
                {{ t.bidDetail.additionalDeliverable }}
              </span>
              <span
                class="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-red-50 text-red-400 border border-red-200 cursor-default"
                :title="t.bidDetail.missingDeliverable"
              >
                <span class="w-1.5 h-1.5 rounded-full bg-red-300 inline-block" />
                {{ t.bidDetail.missingDeliverable }}
              </span>
            </div>

            <div class="space-y-3">
              <div
                v-for="phase in currentBid.details.phases"
                :key="phase.phaseNumber"
                class="rounded-2xl border border-gray-100 p-4 bg-brand-cream"
              >
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center gap-2">
                    <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-brand-brown text-white">
                      {{ t.clientFinalization.phase }} {{ phase.phaseNumber }}
                    </span>
                    <span class="text-sm font-bold text-black">{{
                      phase.title || `${t.clientFinalization.phase} ${phase.phaseNumber}`
                    }}</span>
                  </div>
                  <span class="text-sm font-bold text-brand-brown">{{ formatCurrency(phase.amount) }}</span>
                </div>
                <div v-if="phase.deliverables?.length" class="flex flex-wrap gap-1 mb-2">
                  <span
                    v-for="d in phase.deliverables"
                    :key="d"
                    :class="[
                      'text-xs px-2 py-0.5 rounded-full border font-medium',
                      isMatchingDeliverable(d)
                        ? 'bg-green-100 text-green-700 border-green-200'
                        : 'bg-blue-100 text-blue-700 border-blue-200'
                    ]"
                    :title="
                      isMatchingDeliverable(d) ? t.bidDetail.matchingDeliverable : t.bidDetail.additionalDeliverable
                    "
                  >
                    {{ t.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
                  </span>
                </div>
                <div class="flex items-center gap-3 mt-1">
                  <p v-if="phase.revisionRounds != null" class="text-xs text-gray-500">
                    {{ phase.revisionRounds }}
                    {{
                      phase.revisionRounds !== 1
                        ? t.clientFinalization.revisionRounds
                        : t.clientFinalization.revisionRound
                    }}
                  </p>
                  <p v-if="phase.estimatedDays" class="text-xs text-gray-500">
                    {{ phase.estimatedDays }} {{ t.bidDetail.days }}
                  </p>
                </div>
              </div>
            </div>

            <div v-if="missingDeliverables.length" class="mt-4 pt-4 border-t border-dashed border-gray-200">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">
                {{ t.bidDetail.uncoveredTitle }}
              </p>
              <div class="flex flex-wrap gap-1">
                <span
                  v-for="d in missingDeliverables"
                  :key="d"
                  class="text-xs px-2 py-0.5 rounded-full border bg-red-50 text-red-400 border-red-200 font-medium"
                  :title="t.bidDetail.missingDeliverable"
                >
                  {{ t.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
                </span>
              </div>
            </div>

            <div class="mt-3 pt-3 border-t border-gray-100 flex justify-between text-sm">
              <span class="text-gray-500 font-bold">{{ t.clientFinalization.total }}</span>
              <span class="font-bold text-brand-brown">{{ formatCurrency(currentBid.bidAmount) }}</span>
            </div>
          </div>

          <div
            v-if="currentBid.portfolioReferences && currentBid.portfolioReferences.length > 0"
            class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
          >
            <h2 class="text-2xl font-bold text-black mb-4">{{ t.bidDetail.portfolioProjects }}</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <button
                v-for="portfolio in currentBid.portfolioReferences"
                :key="portfolio.id"
                type="button"
                class="text-left bg-gray-50 rounded-2xl p-4 border border-gray-200 hover:border-brand-gold transition"
                @click="viewingPortfolio = portfolio"
              >
                <div v-if="portfolio.images?.[0]" class="aspect-video bg-gray-200 rounded-xl mb-3 overflow-hidden">
                  <img
                    :src="portfolio.images[0].mediumUrl || portfolio.images[0].originalUrl"
                    :alt="portfolio.title"
                    class="w-full h-full object-cover"
                  />
                </div>
                <p class="font-medium text-gray-900">{{ portfolio.title }}</p>
              </button>
            </div>
          </div>

          <PortfolioDetailPopup :portfolio="viewingPortfolio" @close="viewingPortfolio = null" />

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <div class="flex gap-4">
              <button
                v-if="currentBid.status === 'PENDING'"
                class="flex-1 px-6 py-4 bg-brand-brown text-white rounded-full font-bold hover:bg-black transition shadow-md hover:shadow-lg flex items-center justify-center gap-2"
                @click="handleAcceptBid"
              >
                <Check :size="20" />
                {{ t.bidDetail.acceptBid }}
              </button>
              <button
                v-else-if="currentBid.status === 'ACCEPTED'"
                disabled
                class="flex-1 px-6 py-4 bg-green-600 text-white rounded-full font-bold flex items-center justify-center gap-2 cursor-not-allowed"
              >
                <Trophy :size="20" />
                {{ t.bidDetail.accepted }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { projectTypeLabel } from '@/constants/projectTaxonomy'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, Check, Trophy } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useBidsStore } from '@/stores/bids'
import { useProjectsStore } from '@/stores/projects'
import BidImageGallery from '@/components/bid/BidImageGallery.vue'
import BidStatusBadge from '@/components/project/BidStatusBadge.vue'
import PortfolioDetailPopup from '@/components/bid/PortfolioDetailPopup.vue'
import { DELIVERABLE_GROUPS } from '@/constants/projectDeliverables'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()
const projectsStore = useProjectsStore()

const { currentBid, loading, error } = storeToRefs(bidsStore)
const { currentProject } = storeToRefs(projectsStore)

const viewingPortfolio = ref(null)

const groupedProjectDeliverables = computed(() => {
  const deliverables = currentProject.value?.deliverables || []
  return DELIVERABLE_GROUPS.map(g => ({
    categoryKey: g.categoryKey,
    items: g.items.filter(d => deliverables.includes(d))
  })).filter(g => g.items.length > 0)
})

const isMatchingDeliverable = d => (currentProject.value?.deliverables || []).includes(d)

const missingDeliverables = computed(() => {
  const projectDeliverables = currentProject.value?.deliverables || []
  const covered = currentBid.value?.details?.phases?.flatMap(p => p.deliverables || []) || []
  return projectDeliverables.filter(d => !covered.includes(d))
})

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const formatDate = dateString => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('id-ID', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const fetchBid = async () => {
  try {
    await bidsStore.fetchBidById(route.params.bidId)
    await projectsStore.fetchProjectById(route.params.projectId)
  } catch (err) {
    console.error('Failed to fetch bid details:', err)
  }
}

const handleAcceptBid = async () => {
  if (!confirm('Are you sure you want to accept this bid?')) return

  try {
    await bidsStore.acceptBid(route.params.bidId)
    router.push(`/client/projects/${route.params.projectId}/finalization`)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept bid')
  }
}

onMounted(() => {
  fetchBid()
})
</script>
