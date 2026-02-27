<template>
  <div class="min-h-screen bg-[#F4F5F7] py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button
        @click="router.push({ name: 'OpportunityList' })"
        class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition"
      >
        <ArrowLeft :size="20" />
        Back to Opportunities
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
          <button @click="router.push({ name: 'OpportunityList' })" class="text-[#7C4728] hover:underline font-medium">
            Back to Opportunities
          </button>
        </div>
      </div>

      <div v-else-if="project" class="space-y-6">
        <div class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-black mb-2">{{ project.title }}</h1>
              <p class="text-gray-500">{{ project.location }} • {{ project.buildingType }}</p>
            </div>
            <ProjectStatusBadge :status="project.status" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Design Budget Range</p>
              <p class="text-2xl font-bold text-black">
                {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
              </p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Build Area</p>
              <p class="text-2xl font-bold text-black">{{ project.estimatedBuildArea }} m²</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Floors</p>
              <p class="text-2xl font-bold text-black">{{ project.numberOfFloors }}</p>
            </div>
          </div>

          <div class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">Scope of Work</h2>
            <p class="text-gray-700 leading-relaxed">{{ project.scopeOfWork }}</p>
          </div>

          <div v-if="project.deliverables && project.deliverables.length > 0" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">Deliverables</h2>
            <div class="space-y-4">
              <div v-for="group in groupedDeliverables" :key="group.category" class="bg-gray-50 rounded-2xl p-5">
                <h4 class="font-bold text-sm text-gray-700 uppercase mb-3">{{ group.category }}</h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <div
                    v-for="item in group.matched"
                    :key="item.value"
                    class="flex items-start gap-3 p-3 bg-white rounded-xl border border-[#C5A17A]/30"
                  >
                    <div class="w-5 h-5 rounded bg-[#7C4728] flex items-center justify-center flex-shrink-0 mt-0.5">
                      <Check :size="12" class="text-white" />
                    </div>
                    <div>
                      <div class="font-medium text-gray-900 text-sm">{{ item.label }}</div>
                      <div class="text-xs text-gray-500 mt-0.5">{{ item.description }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Category</p>
              <p class="text-base text-gray-900">{{ project.category }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Expected Start Date</p>
              <p class="text-base text-gray-900">{{ formatDate(project.expectedStartDate) }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Land Ownership</p>
              <p class="text-base text-gray-900">{{ project.hasOwnedLand ? 'Yes' : 'No' }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">Legal Documentation</p>
              <p class="text-base text-gray-900">{{ project.hasLegalDocuments ? 'Complete' : 'Incomplete' }}</p>
            </div>
          </div>

          <div v-if="project.designPreferences" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">Design Preferences</h2>
            <p class="text-gray-700 leading-relaxed">{{ project.designPreferences }}</p>
          </div>

          <div v-if="project.biddingDeadline" class="pt-6 border-t border-gray-100 mb-8">
            <BiddingCountdown :deadline="project.biddingDeadline" size="lg" />
          </div>

          <div v-if="existingBid" class="space-y-4">
            <div
              class="bg-blue-50 border border-blue-200 rounded-2xl p-6 flex items-start gap-4"
              v-if="existingBid.status === 'PENDING'"
            >
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                <Send :size="24" class="text-blue-600" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-blue-900 mb-1">Proposal Submitted</p>
                <p class="text-sm text-blue-700">Your bid is pending review by the client.</p>
              </div>
            </div>

            <div
              class="bg-yellow-50 border border-yellow-200 rounded-2xl p-6 flex items-start gap-4"
              v-if="existingBid.status === 'DRAFT'"
            >
              <div class="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center flex-shrink-0">
                <FileText :size="24" class="text-yellow-600" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-yellow-900 mb-1">Draft Saved</p>
                <p class="text-sm text-yellow-700">You have an unfinished proposal for this project.</p>
              </div>
            </div>

            <button
              v-if="existingBid.status === 'DRAFT'"
              @click="goToProposal"
              class="w-full bg-[#7C4728] text-white py-4 px-6 rounded-2xl hover:bg-[#5a3419] transition flex items-center justify-center gap-3 text-lg font-bold"
            >
              <Send :size="24" />
              Continue Draft
            </button>

            <button
              v-if="existingBid.status === 'PENDING'"
              disabled
              class="w-full bg-gray-300 text-gray-500 py-4 px-6 rounded-2xl cursor-not-allowed flex items-center justify-center gap-3 text-lg font-bold"
            >
              <Send :size="24" />
              Proposal Submitted
            </button>
          </div>

          <button
            v-else
            @click="goToProposal"
            :disabled="project.status !== 'OPEN'"
            class="w-full bg-[#7C4728] text-white py-4 px-6 rounded-2xl hover:bg-[#5a3419] transition flex items-center justify-center gap-3 text-lg font-bold disabled:bg-gray-300 disabled:cursor-not-allowed disabled:text-gray-500"
          >
            <Send :size="24" />
            Submit Proposal
          </button>
        </div>

        <div v-if="imageFiles.length > 0" class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <h2 class="text-2xl font-bold text-black mb-6">Visual References</h2>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
            <div
              v-for="file in imageFiles"
              :key="file.id"
              class="relative aspect-square rounded-2xl overflow-hidden cursor-pointer group"
              @click="window.open(file.filePath, '_blank')"
            >
              <img
                :src="file.filePath"
                :alt="file.fileName"
                class="w-full h-full object-cover transition group-hover:scale-105"
              />
              <div class="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition flex items-center justify-center">
                <ExternalLink :size="24" class="text-white opacity-0 group-hover:opacity-100 transition" />
              </div>
            </div>
          </div>
        </div>

        <div v-if="documentFiles.length > 0" class="bg-white rounded-3xl border border-gray-200 p-8 shadow-soft">
          <h2 class="text-2xl font-bold text-black mb-6">Project Documents</h2>
          <div class="space-y-3">
            <a
              v-for="file in documentFiles"
              :key="file.id"
              :href="file.filePath"
              target="_blank"
              class="flex items-center gap-3 p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition border border-gray-200"
            >
              <FileText :size="24" class="text-[#7C4728]" />
              <div class="flex-1">
                <p class="font-medium text-gray-900">{{ file.fileName }}</p>
                <p class="text-sm text-gray-500">{{ file.fileType }}</p>
              </div>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Send, FileText, AlertCircle, Check, ExternalLink } from 'lucide-vue-next'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'

const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const deliverableGroups = [
  {
    category: 'Site Analysis & Planning',
    items: [
      { value: 'SITE_ANALYSIS', label: 'Site Analysis', description: 'Land survey and environmental assessment' },
      { value: 'ZONING_STUDY', label: 'Zoning Study', description: 'Local regulations and building codes' }
    ]
  },
  {
    category: 'Design Phases',
    items: [
      { value: 'CONCEPT_DESIGN', label: 'Concept Design', description: 'Initial design concepts and sketches' },
      { value: 'SCHEMATIC_DESIGN', label: 'Schematic Design', description: 'Preliminary floor plans and elevations' },
      { value: 'DESIGN_DEVELOPMENT', label: 'Design Development', description: 'Detailed design drawings' },
      { value: 'CONSTRUCTION_DOCS', label: 'Construction Documents', description: 'Complete technical drawings' }
    ]
  },
  {
    category: 'Permits & Documentation',
    items: [
      { value: 'IMB_PERMIT', label: 'IMB (Building Permit)', description: 'Building construction permit' },
      { value: 'SLF_CERT', label: 'SLF Certificate', description: 'Building feasibility certificate' },
      { value: 'ENVIRONMENTAL_PERMIT', label: 'Environmental Permit', description: 'Environmental impact assessment' }
    ]
  },
  {
    category: 'Specialized Services',
    items: [
      { value: 'INTERIOR_DESIGN', label: 'Interior Design', description: 'Interior layout and finishes' },
      { value: 'LANDSCAPE_DESIGN', label: 'Landscape Design', description: 'Garden and outdoor spaces' },
      { value: 'MEP_DESIGN', label: 'MEP Design', description: 'Mechanical, electrical, and plumbing' },
      { value: 'STRUCTURAL_DESIGN', label: 'Structural Design', description: 'Structural engineering drawings' }
    ]
  },
  {
    category: 'Construction Support',
    items: [
      { value: 'SUPERVISION', label: 'Construction Supervision', description: 'On-site supervision during construction' },
      { value: 'AS_BUILT', label: 'As-Built Drawings', description: 'Final drawings reflecting construction changes' }
    ]
  }
]

const project = ref(null)
const loading = ref(false)
const error = ref(null)
const existingBid = ref(null)

const imageFiles = computed(() =>
  (project.value?.files ?? []).filter(f => f.fileType?.startsWith('image/'))
)

const documentFiles = computed(() =>
  (project.value?.files ?? []).filter(f => !f.fileType?.startsWith('image/'))
)

const groupedDeliverables = computed(() => {
  if (!project.value?.deliverables) return []
  return deliverableGroups
    .map(group => ({
      ...group,
      matched: group.items.filter(item => project.value.deliverables.includes(item.value))
    }))
    .filter(group => group.matched.length > 0)
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

const goToProposal = () => {
  router.push({
    name: 'ProposalCreate',
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
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load project details'
    console.error('Failed to fetch project:', err)
  } finally {
    loading.value = false
  }
})
</script>
