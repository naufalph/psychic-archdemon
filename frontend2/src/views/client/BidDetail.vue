<template>
  <div class="min-h-screen bg-[#F4F5F7] py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button @click="router.back()" class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition">
        <ArrowLeft :size="20" />
        Back to Project
      </button>

      <div v-if="loading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded mb-6" />
      </div>

      <div v-else-if="error" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600 mb-4">{{ error }}</p>
        <button @click="fetchBid" class="text-[#7C4728] hover:underline">Try again</button>
      </div>

      <div v-else-if="currentBid" class="lg:grid lg:grid-cols-3 gap-6">
        <!-- Left sidebar: Project Summary -->
        <div class="lg:col-span-1 mb-6 lg:mb-0">
          <div class="lg:sticky lg:top-6 bg-white rounded-3xl border border-gray-200 p-6 shadow-soft">
            <h2 class="text-xl font-bold text-black mb-4">{{ t.proposalCreate?.projectSummary || 'Project Summary' }}</h2>
            <div class="space-y-4">
              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.proposalCreate?.project || 'Project' }}</p>
                <p class="font-bold text-gray-900">{{ currentBid.projectTitle || currentProject?.title }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.proposalCreate?.location || 'Location' }}</p>
                <p class="text-gray-900">{{ currentBid.projectLocation || currentProject?.location }}</p>
              </div>

              <template v-if="currentProject">
                <div>
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.proposalCreate?.designBudget || 'Design Budget' }}</p>
                  <p class="text-gray-900 font-medium">{{ formatCurrency(currentProject.designBudget) }}</p>
                </div>

                <div v-if="currentProject.estimatedBuildArea">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.proposalCreate?.buildArea || 'Build Area' }}</p>
                  <p class="text-gray-900">{{ currentProject.estimatedBuildArea }} m²</p>
                </div>

                <div>
                  <p class="text-xs text-gray-500 uppercase font-bold mb-1">{{ t.proposalCreate?.buildingType || 'Building Type' }}</p>
                  <p class="text-gray-900">{{ currentProject.buildingType }}</p>
                </div>

                <div v-if="groupedProjectDeliverables.length > 0">
                  <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.proposalCreate?.deliverables || 'Deliverables' }}</p>
                  <div class="space-y-2">
                    <div v-for="group in groupedProjectDeliverables" :key="group.categoryKey">
                      <p class="text-xs text-gray-400 font-semibold mb-1">
                        {{ t.proposalCreate?.deliverableCategories?.[group.categoryKey] }}
                      </p>
                      <div class="flex flex-wrap gap-1">
                        <span
                          v-for="d in group.items"
                          :key="d"
                          class="bg-[#F5E6D3] text-[#7C4728] px-2 py-0.5 rounded-full text-xs font-medium"
                        >
                          {{ t.proposalCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
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
                <p class="text-sm text-gray-400 mt-2">Submitted on {{ formatDate(currentBid.submittedAt) }}</p>
              </div>
              <BidStatusBadge :status="currentBid.status" />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="bg-gray-50 rounded-2xl p-6">
                <p class="text-xs text-gray-500 uppercase font-bold mb-2">Proposed Cost</p>
                <p class="text-3xl font-bold text-[#7C4728]">{{ formatCurrency(currentBid.bidAmount) }}</p>
              </div>
              <div class="bg-gray-50 rounded-2xl p-6">
                <p class="text-xs text-gray-500 uppercase font-bold mb-2">Timeline</p>
                <p class="text-3xl font-bold text-black">{{ currentBid.proposedTimelineDays }} days</p>
              </div>
            </div>
          </div>

          <div
            v-if="currentBid.details?.conceptStatement"
            class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
          >
            <h2 class="text-2xl font-bold text-black mb-4">Design Concept</h2>
            <p class="text-gray-700 leading-relaxed whitespace-pre-line">
              {{ currentBid.details.conceptStatement }}
            </p>
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <BidImageGallery
              :images="currentBid.facadeImages"
              title="Facade"
              empty-message="No facade images available"
            />
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <BidImageGallery
              :images="currentBid.interiorImages"
              title="Interior"
              empty-message="No interior images available"
            />
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <BidImageGallery
              :images="currentBid.massingImages"
              title="Massing"
              empty-message="No massing images available"
            />
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <BidImageGallery
              :images="currentBid.zoningImages"
              title="Zoning"
              empty-message="No zoning images available"
            />
          </div>

          <div
            v-if="currentBid.details?.phases?.length"
            class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
          >
            <h2 class="text-2xl font-bold text-black mb-4">Payment Schedule</h2>
            <div class="space-y-3">
              <div
                v-for="phase in currentBid.details.phases"
                :key="phase.phaseNumber"
                class="rounded-2xl border border-gray-100 p-4 bg-[#FDF6EE]"
              >
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center gap-2">
                    <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-[#7C4728] text-white">
                      Phase {{ phase.phaseNumber }}
                    </span>
                    <span class="text-sm font-bold text-black">{{ phase.title || `Phase ${phase.phaseNumber}` }}</span>
                  </div>
                  <span class="text-sm font-bold text-[#7C4728]">{{ formatCurrency(phase.amount) }}</span>
                </div>
                <div v-if="phase.deliverables?.length" class="flex flex-wrap gap-1 mb-2">
                  <span
                    v-for="d in phase.deliverables"
                    :key="d"
                    class="text-xs px-2 py-0.5 bg-white border border-gray-200 rounded-full text-gray-600"
                  >{{ t.proposalCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}</span>
                </div>
                <p v-if="phase.revisionRounds != null" class="text-xs text-gray-500">
                  {{ phase.revisionRounds }} revision round{{ phase.revisionRounds !== 1 ? 's' : '' }}
                </p>
              </div>
            </div>
            <div class="mt-3 pt-3 border-t border-gray-100 flex justify-between text-sm">
              <span class="text-gray-500 font-bold">Total</span>
              <span class="font-bold text-[#7C4728]">{{ formatCurrency(currentBid.bidAmount) }}</span>
            </div>
          </div>

          <div
            v-if="currentBid.portfolioReferences && currentBid.portfolioReferences.length > 0"
            class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft"
          >
            <h2 class="text-2xl font-bold text-black mb-4">Related Portfolio Projects</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div
                v-for="portfolio in currentBid.portfolioReferences"
                :key="portfolio.id"
                class="bg-gray-50 rounded-2xl p-4 border border-gray-200 hover:border-[#C5A17A] transition"
              >
                <div v-if="portfolio.coverImage" class="aspect-video bg-gray-200 rounded-xl mb-3 overflow-hidden">
                  <img :src="portfolio.coverImage" :alt="portfolio.projectName" class="w-full h-full object-cover" />
                </div>
                <p class="font-medium text-gray-900">{{ portfolio.projectName }}</p>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
            <div class="flex gap-4">
              <button
                v-if="currentBid.status === 'PENDING'"
                @click="handleAcceptBid"
                class="flex-1 px-6 py-4 bg-[#7C4728] text-white rounded-full font-bold hover:bg-black transition shadow-md hover:shadow-lg flex items-center justify-center gap-2"
              >
                <Check :size="20" />
                Accept This Proposal
              </button>
              <button
                v-else-if="currentBid.status === 'ACCEPTED'"
                disabled
                class="flex-1 px-6 py-4 bg-green-600 text-white rounded-full font-bold flex items-center justify-center gap-2 cursor-not-allowed"
              >
                <Trophy :size="20" />
                Accepted
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, Check, Trophy } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useBidsStore } from '@/stores/bids'
import { useProjectsStore } from '@/stores/projects'
import BidImageGallery from '@/components/bid/BidImageGallery.vue'
import BidStatusBadge from '@/components/project/BidStatusBadge.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()
const projectsStore = useProjectsStore()

const { currentBid, loading, error } = storeToRefs(bidsStore)
const { currentProject } = storeToRefs(projectsStore)

const DELIVERABLE_CATEGORIES = [
  { categoryKey: 'siteAnalysis', items: ['SITE_ANALYSIS', 'ZONING_STUDY'] },
  { categoryKey: 'designPhases', items: ['CONCEPT_DESIGN', 'SCHEMATIC_DESIGN', 'DESIGN_DEVELOPMENT', 'CONSTRUCTION_DOCS'] },
  { categoryKey: 'permits', items: ['IMB_PERMIT', 'SLF_CERT', 'ENVIRONMENTAL_PERMIT'] },
  { categoryKey: 'specialized', items: ['INTERIOR_DESIGN', 'LANDSCAPE_DESIGN', 'MEP_DESIGN', 'STRUCTURAL_DESIGN'] },
  { categoryKey: 'construction', items: ['SUPERVISION', 'AS_BUILT'] }
]

const groupedProjectDeliverables = computed(() => {
  const deliverables = currentProject.value?.deliverables || []
  return DELIVERABLE_CATEGORIES
    .map(g => ({ categoryKey: g.categoryKey, items: g.items.filter(d => deliverables.includes(d)) }))
    .filter(g => g.items.length > 0)
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
  if (!confirm('Are you sure you want to accept this proposal?')) return

  try {
    await bidsStore.acceptBid(route.params.bidId)
    router.push(`/client/projects/${route.params.projectId}/finalization`)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept proposal')
  }
}

onMounted(() => {
  fetchBid()
})
</script>
