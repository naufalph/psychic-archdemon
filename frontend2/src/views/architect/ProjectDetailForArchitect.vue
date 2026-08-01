<template>
  <div class="min-h-screen bg-surface-alt py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button @click="router.back()" class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition">
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
          <button @click="router.back()" class="text-brand-brown hover:underline font-medium">Back</button>
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
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">
                {{ t.projectDetailArchitect.designBudgetRange }}
              </p>
              <p class="text-2xl font-bold text-black">
                {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
              </p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.buildArea }}</p>
              <p class="text-2xl font-bold text-black">{{ project.estimatedBuildArea }} m²</p>
            </div>
            <div class="bg-gray-50 rounded-2xl p-6">
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.floors }}</p>
              <p class="text-2xl font-bold text-black">{{ project.numberOfFloors }}</p>
            </div>
          </div>

          <div class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.scopeOfWork }}</h2>
            <p class="text-gray-700 leading-relaxed">{{ project.scopeOfWork }}</p>
          </div>

          <div v-if="project.deliverables && project.deliverables.length > 0" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.deliverables }}</h2>
            <div class="space-y-4">
              <div v-for="group in groupedDeliverables" :key="group.categoryKey" class="bg-gray-50 rounded-2xl p-5">
                <h4 class="font-bold text-sm text-gray-700 uppercase mb-3">
                  {{ t.proposalCreate.deliverableCategories[group.categoryKey] }}
                </h4>
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="value in group.matched"
                    :key="value"
                    class="bg-white border border-brand-gold/30 px-3 py-1.5 rounded-full text-sm font-medium text-gray-800 flex items-center gap-1.5"
                  >
                    <Check :size="12" class="text-brand-brown" />
                    {{ t.proposalCreate.deliverableItems[value] || value.replace(/_/g, ' ') }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="mb-6 rounded-2xl p-5 border-2 border-amber-200 bg-amber-50 flex items-center gap-4">
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

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.category }}</p>
              <p class="text-base text-gray-900">{{ project.category }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">{{ t.projectDetailArchitect.landOwnership }}</p>
              <p class="text-base text-gray-900">
                {{ project.hasOwnedLand ? t.projectDetailArchitect.yes : t.projectDetailArchitect.no }}
              </p>
            </div>
            <div>
              <p class="text-xs text-gray-500 uppercase font-bold mb-2">
                {{ t.projectDetailArchitect.legalDocumentation }}
              </p>
              <p class="text-base text-gray-900">
                {{
                  project.hasLegalDocuments ? t.projectDetailArchitect.complete : t.projectDetailArchitect.incomplete
                }}
              </p>
            </div>
          </div>

          <div v-if="project.designPreferences" class="mb-8">
            <h2 class="text-lg font-bold text-black mb-3">{{ t.projectDetailArchitect.designPreferences }}</h2>
            <p class="text-gray-700 leading-relaxed">{{ project.designPreferences }}</p>
          </div>

          <div v-if="project.biddingDeadline" class="pt-6 border-t border-gray-100 mb-8">
            <BiddingCountdown :deadline="project.biddingDeadline" size="lg" />
          </div>

          <div v-if="existingBid" class="space-y-4">
            <div
              v-if="existingBid.status === 'ACCEPTED' && project.status === 'NEGOTIATION'"
              class="bg-amber-50 border border-amber-200 rounded-2xl p-6 flex items-start gap-4"
            >
              <div class="w-12 h-12 bg-amber-100 rounded-full flex items-center justify-center flex-shrink-0">
                <CheckCircle :size="24" class="text-amber-600" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-amber-900 mb-1">{{ t.projectDetailArchitect.proposalAccepted }}</p>
                <p class="text-sm text-amber-700">{{ t.projectDetailArchitect.proposalAcceptedDesc }}</p>
              </div>
            </div>

            <button
              v-if="existingBid.status === 'ACCEPTED' && project.status === 'NEGOTIATION'"
              @click="router.push({ name: 'ArchitectFinalizationView', params: { projectId: route.params.projectId } })"
              class="w-full bg-brand-brown text-white py-4 px-6 rounded-2xl hover:bg-black transition flex items-center justify-center gap-3 text-lg font-bold"
            >
              <CheckCircle :size="24" />
              {{ t.projectDetailArchitect.finalizeAgreement }}
            </button>

            <div
              class="bg-blue-50 border border-blue-200 rounded-2xl p-6 flex items-start gap-4"
              v-if="existingBid.status === 'PENDING'"
            >
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                <Send :size="24" class="text-blue-600" />
              </div>
              <div class="flex-1">
                <p class="font-bold text-blue-900 mb-1">{{ t.projectDetailArchitect.proposalSubmitted }}</p>
                <p class="text-sm text-blue-700">{{ t.projectDetailArchitect.proposalSubmittedDesc }}</p>
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
                <p class="font-bold text-yellow-900 mb-1">{{ t.projectDetailArchitect.draftSaved }}</p>
                <p class="text-sm text-yellow-700">{{ t.projectDetailArchitect.draftSavedDesc }}</p>
              </div>
            </div>

            <button
              v-if="existingBid.status === 'DRAFT'"
              @click="goToProposal"
              class="w-full bg-brand-brown text-white py-4 px-6 rounded-2xl hover:bg-brand-brown-hover transition flex items-center justify-center gap-3 text-lg font-bold"
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
              {{ t.projectDetailArchitect.proposalSubmitted }}
            </button>
          </div>

          <button
            v-else
            @click="goToProposal"
            :disabled="project.status !== 'OPEN'"
            class="w-full bg-brand-brown text-white py-4 px-6 rounded-2xl hover:bg-brand-brown-hover transition flex items-center justify-center gap-3 text-lg font-bold disabled:bg-gray-300 disabled:cursor-not-allowed disabled:text-gray-500"
          >
            <Send :size="24" />
            {{ t.projectDetailArchitect.noBidYet }}
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
              <div
                class="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition flex items-center justify-center"
              >
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
              <FileText :size="24" class="text-brand-brown" />
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
import { ArrowLeft, Send, FileText, AlertCircle, Check, CheckCircle, ExternalLink, CalendarDays } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const deliverableGroups = [
  {
    categoryKey: 'siteAnalysis',
    items: ['SITE_ANALYSIS', 'ZONING_STUDY']
  },
  {
    categoryKey: 'designPhases',
    items: ['CONCEPT_DESIGN', 'SCHEMATIC_DESIGN', 'DESIGN_DEVELOPMENT', 'CONSTRUCTION_DOCS']
  },
  {
    categoryKey: 'permits',
    items: ['IMB_PERMIT', 'SLF_CERT', 'ENVIRONMENTAL_PERMIT']
  },
  {
    categoryKey: 'specialized',
    items: ['INTERIOR_DESIGN', 'LANDSCAPE_DESIGN', 'MEP_DESIGN', 'STRUCTURAL_DESIGN']
  },
  {
    categoryKey: 'construction',
    items: ['SUPERVISION', 'AS_BUILT']
  }
]

const project = ref(null)
const loading = ref(false)
const error = ref(null)
const existingBid = ref(null)

const imageFiles = computed(() => (project.value?.files ?? []).filter(f => f.fileType?.startsWith('image/')))

const documentFiles = computed(() => (project.value?.files ?? []).filter(f => !f.fileType?.startsWith('image/')))

const groupedDeliverables = computed(() => {
  if (!project.value?.deliverables) return []
  return deliverableGroups
    .map(group => ({
      ...group,
      matched: group.items.filter(value => project.value.deliverables.includes(value))
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
