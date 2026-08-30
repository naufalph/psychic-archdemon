<template>
  <div class="min-h-screen bg-surface-alt py-12">
    <div class="max-w-7xl mx-auto px-6">
      <button class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition" @click="saveDraftAndLeave">
        <ArrowLeft :size="20" />
        {{ t.bidCreate?.backToOpportunities || 'Back to Opportunities' }}
      </button>

      <div v-if="projectLoading" class="bg-white rounded-3xl border border-gray-200 p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
      </div>

      <div v-else-if="projectError" class="bg-white rounded-3xl border border-gray-200 p-12 text-center">
        <p class="text-red-600 mb-4">{{ projectError }}</p>
        <button class="text-brand-brown hover:underline" @click="router.push({ name: 'OpportunityList' })">
          {{ t.bidCreate?.backToOpportunities || 'Back to Opportunities' }}
        </button>
      </div>

      <div v-else-if="project" class="lg:grid lg:grid-cols-3 gap-6">
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
                <p class="font-bold text-gray-900">{{ project.title }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.bidCreate?.location || 'Location' }}
                </p>
                <p class="text-gray-900">{{ project.location }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.bidCreate?.designBudget || 'Design Budget' }}
                </p>
                <p class="text-gray-900 font-medium">
                  {{ formatCurrency(project.designBudgetMin) }} - {{ formatCurrency(project.designBudgetMax) }}
                </p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.projectDetailArchitect.lotSize }}
                </p>
                <p class="text-gray-900">{{ project.lotSize ? `${project.lotSize} m²` : '—' }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.bidCreate?.buildArea || 'Build Area' }}
                </p>
                <p class="text-gray-900">{{ project.estimatedBuildArea ? `${project.estimatedBuildArea} m²` : '—' }}</p>
              </div>

              <div>
                <p class="text-xs text-gray-500 uppercase font-bold mb-1">
                  {{ t.bidCreate?.buildingType || 'Building Type' }}
                </p>
                <p class="text-gray-900">{{ projectTypeLabel(project, locale) }}</p>
              </div>

              <div v-if="project.deliverables && project.deliverables.length > 0">
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

              <div v-if="sidebarImages.length > 0" class="pt-4 border-t border-gray-100">
                <p class="text-xs text-gray-500 uppercase font-bold mb-2">
                  {{ t.bidCreate?.referenceImages || 'Client Reference Images' }}
                </p>
                <div class="grid grid-cols-3 gap-2">
                  <button
                    v-for="(img, index) in sidebarImages"
                    :key="img.id"
                    type="button"
                    class="aspect-square rounded-lg overflow-hidden border border-gray-200 hover:border-brand-brown transition"
                    @click="openLightbox(index)"
                  >
                    <img :src="img.url" :alt="img.name" class="w-full h-full object-cover" />
                  </button>
                </div>
              </div>

              <div v-if="project.biddingDeadline" class="pt-4 border-t border-gray-100">
                <BiddingCountdown :deadline="project.biddingDeadline" size="sm" />
              </div>
            </div>
          </div>
        </div>

        <div class="lg:col-span-2">
          <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden">
            <div class="bg-brand-brown p-8 text-white">
              <h1 class="text-3xl font-bold flex items-center gap-3">
                <FileText :size="32" />
                {{
                  existingBidId
                    ? t.bidCreate?.updateTitle || 'Update Bid'
                    : t.bidCreate?.title || 'Submit Bid'
                }}
              </h1>
              <p class="text-white/80 mt-2">
                {{
                  existingBidId
                    ? t.bidCreate?.updateSubtitle || 'Continue editing your draft bid'
                    : t.bidCreate?.subtitle || 'Showcase your expertise and win the project'
                }}
              </p>
            </div>

            <div
              v-if="profileStore.profile && !isIdentityComplete"
              class="mx-8 mt-6 p-5 bg-amber-50 border border-amber-200 rounded-2xl flex gap-4 items-start"
            >
              <svg
                class="w-5 h-5 text-amber-500 flex-shrink-0 mt-0.5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <div class="flex-1">
                <p class="text-sm font-semibold text-amber-900">
                  {{ t.bidCreate?.identityIncompleteTitle || 'Identity verification required to submit' }}
                </p>
                <p class="text-sm text-amber-800 mt-1">
                  {{
                    t.bidCreate?.identityIncompleteDesc ||
                    'Complete your KTP, NPWP, full name, and phone number before submitting a bid. You can still save a draft.'
                  }}
                </p>
                <button
                  type="button"
                  :disabled="isSavingDraft"
                  class="inline-block mt-2 text-sm font-semibold text-amber-900 underline hover:text-brand-brown disabled:opacity-60"
                  @click="goToProfile"
                >
                  {{
                    isSavingDraft
                      ? t.bidCreate?.savingDraft || 'Saving draft...'
                      : (t.bidCreate?.identityIncompleteAction || 'Complete Profile') + ' →'
                  }}
                </button>
              </div>
            </div>

            <form class="p-8 space-y-8" @submit.prevent="handleSubmit">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">About your studio</label>
                <p class="text-xs text-gray-500 mb-3">
                  {{
                    t.bidCreate?.studioPhilosophyHelp ||
                    "Prefilled from your profile's Design Philosophy — feel free to tailor it for this project."
                  }}
                </p>
                <textarea
                  v-model="formData.proposal"
                  rows="6"
                  placeholder="Tell the client about your studio's expertise, experience, and approach..."
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Concept Statement<span class="text-red-500">*</span>
                </label>
                <p class="text-xs text-gray-500 mb-3">
                  Describe your design plan, concept, and approach for this specific project (max 200 words)
                </p>
                <textarea
                  v-model="formData.conceptStatement"
                  rows="6"
                  required
                  placeholder="Explain your design concept, key features, and how you'll address the client's needs..."
                  class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                  @input="validateWordCount"
                />
                <p class="text-xs text-gray-500 mt-1">{{ wordCount }}/200 words</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Facade Images <span class="text-gray-400 font-normal">(exterior views, max 3)</span>
                </label>
                <MultiImageUploader
                  v-model="facadeImages"
                  :max-files="3"
                  :existing-images="existingFacade"
                  @delete-existing="id => deleteExistingImage(id, 'facade')"
                />
                <p class="text-xs text-gray-400 mt-2">
                  {{
                    t.bidCreate?.aspectRatioHint ||
                    'Recommended aspect ratio: 16:9 for the best display in bid comparisons'
                  }}
                </p>
                <textarea
                  v-model="formData.facadeDescription"
                  rows="2"
                  :placeholder="
                    t.bidCreate?.facadeDescriptionPlaceholder ||
                    'Add context about these facade images (materials, style, site conditions)...'
                  "
                  class="w-full mt-3 px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none text-sm"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Interior Images <span class="text-gray-400 font-normal">(interior spaces, max 3)</span>
                </label>
                <MultiImageUploader
                  v-model="interiorImages"
                  :max-files="3"
                  :existing-images="existingInterior"
                  @delete-existing="id => deleteExistingImage(id, 'interior')"
                />
                <p class="text-xs text-gray-400 mt-2">
                  {{
                    t.bidCreate?.aspectRatioHint ||
                    'Recommended aspect ratio: 16:9 for the best display in bid comparisons'
                  }}
                </p>
                <textarea
                  v-model="formData.interiorDescription"
                  rows="2"
                  :placeholder="
                    t.bidCreate?.interiorDescriptionPlaceholder || 'Add context about these interior images...'
                  "
                  class="w-full mt-3 px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none text-sm"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Massing Images <span class="text-gray-400 font-normal">(3D form studies, max 3)</span>
                </label>
                <MultiImageUploader
                  v-model="massingImages"
                  :max-files="3"
                  :existing-images="existingMassing"
                  @delete-existing="id => deleteExistingImage(id, 'massing')"
                />
                <p class="text-xs text-gray-400 mt-2">
                  {{
                    t.bidCreate?.aspectRatioHint ||
                    'Recommended aspect ratio: 16:9 for the best display in bid comparisons'
                  }}
                </p>
                <textarea
                  v-model="formData.massingDescription"
                  rows="2"
                  :placeholder="
                    t.bidCreate?.massingDescriptionPlaceholder || 'Add context about these massing images...'
                  "
                  class="w-full mt-3 px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none text-sm"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Zoning Images <span class="text-gray-400 font-normal">(site plan diagrams, max 3)</span>
                </label>
                <MultiImageUploader
                  v-model="zoningImages"
                  :max-files="3"
                  :existing-images="existingZoning"
                  @delete-existing="id => deleteExistingImage(id, 'zoning')"
                />
                <p class="text-xs text-gray-400 mt-2">
                  {{
                    t.bidCreate?.aspectRatioHint ||
                    'Recommended aspect ratio: 16:9 for the best display in bid comparisons'
                  }}
                </p>
                <textarea
                  v-model="formData.zoningDescription"
                  rows="2"
                  :placeholder="
                    t.bidCreate?.zoningDescriptionPlaceholder || 'Add context about these zoning images...'
                  "
                  class="w-full mt-3 px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none text-sm"
                />
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Attach Relevant Portfolios</label>
                <p class="text-xs text-gray-500 mb-3">Select portfolios that demonstrate your relevant experience</p>
                <PortfolioSelector v-model="formData.portfolioIds" />
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2"
                    >{{ t.bidCreate?.bidAmount || 'Bid Amount (IDR)' }}<span class="text-red-500">*</span></label
                  >
                  <input
                    v-model="bidAmountDisplay"
                    required
                    type="text"
                    inputmode="numeric"
                    placeholder="e.g., 50.000.000"
                    class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-brand-brown focus:border-brand-brown outline-none"
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">{{
                  t.bidCreate?.paymentPhases || 'Payment Phases'
                }}</label>
                <p class="text-xs text-gray-500 mb-3">
                  {{
                    t.bidCreate?.paymentPhasesHelp ||
                    'Define payment phases. Phase 0 is free (pre-project). Phase 1+ must total your bid amount.'
                  }}
                </p>
                <PaymentPhaseBuilder
                  v-model="formData.phases"
                  :bid-amount="formData.bidAmount"
                  :required-deliverables="project?.deliverables || []"
                />
              </div>

              <div v-if="uploadProgress > 0" class="bg-gray-50 rounded-2xl p-6">
                <UploadProgress :progress="uploadProgress" label="Uploading files..." />
              </div>

              <div v-if="error" ref="errorRef" class="p-4 bg-red-50 border border-red-200 rounded-xl space-y-2">
                <p class="text-sm font-semibold text-red-700">{{ error }}</p>
                <ul v-if="identityMissing.length" class="text-xs text-red-600 list-disc list-inside space-y-0.5">
                  <li v-for="item in identityMissing" :key="item">{{ item }}</li>
                </ul>
                <button
                  v-if="!isIdentityComplete"
                  type="button"
                  :disabled="isSavingDraft"
                  class="inline-block text-sm font-semibold text-brand-brown underline disabled:opacity-60"
                  @click="goToProfile"
                >
                  {{
                    isSavingDraft
                      ? t.bidCreate?.savingDraft || 'Saving draft...'
                      : t.bidCreate?.completeProfileLink || 'Complete your profile →'
                  }}
                </button>
              </div>

              <div class="flex gap-4 pt-6 border-t border-gray-100">
                <button
                  type="button"
                  class="px-6 py-3 text-gray-700 bg-white border-2 border-gray-300 rounded-full hover:bg-gray-50 transition font-medium"
                  @click="saveDraftAndLeave"
                >
                  {{ t.bidCreate?.cancelBtn || 'Cancel' }}
                </button>
                <button
                  type="submit"
                  :disabled="(loading && !isSavingDraft) || uploadProgress > 0"
                  class="flex-1 px-6 py-3 text-white bg-brand-brown rounded-full hover:bg-black shadow-md hover:shadow-lg transition-all font-bold flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  <Loader v-if="loading && !isSavingDraft" :size="20" class="animate-spin" />
                  <Send v-else :size="20" />
                  {{
                    loading && !isSavingDraft
                      ? t.bidCreate?.submitting || 'Submitting...'
                      : t.bidCreate?.submitBtn || 'Submit Bid'
                  }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="showIdentityModal"
          class="fixed inset-0 z-50 overflow-y-auto"
          @click.self="showIdentityModal = false"
        >
          <div class="flex min-h-screen items-center justify-center p-4">
            <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="showIdentityModal = false"></div>

            <Transition
              enter-active-class="transition ease-out duration-200"
              enter-from-class="opacity-0 scale-95"
              enter-to-class="opacity-100 scale-100"
              leave-active-class="transition ease-in duration-150"
              leave-from-class="opacity-100 scale-100"
              leave-to-class="opacity-0 scale-95"
            >
              <div
                v-if="showIdentityModal"
                class="relative z-10 w-full max-w-md transform rounded-2xl bg-white shadow-2xl transition-all"
              >
                <div class="flex items-start justify-between gap-4 px-6 pt-6">
                  <div class="w-12 h-12 rounded-full bg-amber-100 flex items-center justify-center flex-shrink-0">
                    <AlertTriangle :size="24" class="text-amber-600" />
                  </div>
                  <button
                    class="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
                    @click="showIdentityModal = false"
                  >
                    <X :size="20" />
                  </button>
                </div>

                <div class="px-6 pb-6 pt-4">
                  <h2 class="text-lg font-bold text-gray-900">
                    {{ t.bidCreate?.identityIncompleteTitle || 'Identity verification required to submit' }}
                  </h2>
                  <p class="text-sm text-gray-600 mt-2 leading-relaxed">
                    {{
                      t.bidCreate?.identityIncompleteDesc ||
                      'Complete your KTP, NPWP, full name, and phone number before submitting a bid. You can still save a draft.'
                    }}
                  </p>

                  <div class="mt-4 bg-amber-50 border border-amber-200 rounded-xl p-4">
                    <p class="text-xs font-bold text-amber-800 uppercase tracking-wide mb-2">
                      {{ t.bidCreate?.identityIncompleteMissingLabel || 'Missing:' }}
                    </p>
                    <ul class="space-y-1">
                      <li
                        v-for="item in identityMissing"
                        :key="item"
                        class="text-sm text-amber-900 flex items-center gap-2"
                      >
                        <span class="w-1.5 h-1.5 rounded-full bg-amber-500 flex-shrink-0"></span>
                        {{ item }}
                      </li>
                    </ul>
                  </div>
                </div>

                <div
                  class="flex items-center justify-end gap-3 border-t border-gray-200 px-6 py-4 rounded-b-2xl bg-gray-50"
                >
                  <button
                    class="px-5 py-2.5 text-sm font-semibold text-gray-600 hover:text-gray-900 transition-colors"
                    @click="showIdentityModal = false"
                  >
                    {{ t.bidCreate?.identityIncompleteDismiss || 'Later' }}
                  </button>
                  <button
                    :disabled="isSavingDraft"
                    class="px-6 py-2.5 bg-brand-brown text-white rounded-full font-semibold hover:bg-brand-brown-dark transition-all disabled:opacity-60 flex items-center gap-2"
                    @click="goToProfile"
                  >
                    <Loader v-if="isSavingDraft" :size="16" class="animate-spin" />
                    {{
                      isSavingDraft
                        ? t.bidCreate?.savingDraft || 'Saving draft...'
                        : t.bidCreate?.identityIncompleteAction || 'Complete Profile'
                    }}
                  </button>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="lightboxIndex !== null" class="fixed inset-0 z-50 overflow-y-auto" @click.self="closeLightbox">
          <div class="flex min-h-screen items-center justify-center p-4">
            <div class="fixed inset-0 bg-black/80 backdrop-blur-sm" @click="closeLightbox"></div>

            <div class="relative z-10 w-full max-w-3xl">
              <button class="absolute -top-10 right-0 text-white hover:text-gray-300 transition" @click="closeLightbox">
                <X :size="28" />
              </button>

              <img
                v-if="sidebarImages[lightboxIndex]"
                :src="sidebarImages[lightboxIndex].url"
                :alt="sidebarImages[lightboxIndex].name"
                class="w-full h-auto rounded-2xl"
              />

              <div v-if="sidebarImages.length > 1" class="flex justify-between mt-4">
                <button
                  class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-full transition"
                  @click="prevLightboxImage"
                >
                  ← Previous
                </button>
                <p class="text-white text-sm self-center">{{ lightboxIndex + 1 }} / {{ sidebarImages.length }}</p>
                <button
                  class="px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-full transition"
                  @click="nextLightboxImage"
                >
                  Next →
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { projectTypeLabel } from '@/constants/projectTaxonomy'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, FileText, Loader, Send, AlertTriangle, X } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import { useProjectsStore } from '@/stores/projects'
import { useArchitectProfileStore } from '@/stores/architectProfile'
import { useI18n } from '@/composables/useI18n'
import { formatIDRDisplay, parseIDRInput } from '@/utils/currencyFormat'
import MultiImageUploader from '@/components/upload/MultiImageUploader.vue'
import UploadProgress from '@/components/upload/UploadProgress.vue'
import BiddingCountdown from '@/components/bidding/BiddingCountdown.vue'
import PaymentPhaseBuilder from '@/components/project/PaymentPhaseBuilder.vue'
import PortfolioSelector from '@/components/architect/PortfolioSelector.vue'

const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()
const projectsStore = useProjectsStore()
const profileStore = useArchitectProfileStore()
const { t, locale, getT } = useI18n()

const identityMissing = computed(() => {
  const p = profileStore.profile
  if (!p) return ['profile']
  const missing = []
  if (!p.ktpNum || !p.ktpNum.trim()) missing.push('KTP')
  if (!p.npwp || !p.npwp.trim()) missing.push('NPWP')
  if (!p.fullnameKtp || !p.fullnameKtp.trim()) missing.push('Nama Lengkap sesuai KTP')
  if (!p.phoneNumber || !p.phoneNumber.trim()) missing.push('Nomor HP')
  return missing
})

const isIdentityComplete = computed(() => identityMissing.value.length === 0)

const showIdentityModal = ref(false)

const DELIVERABLE_CATEGORIES = [
  { categoryKey: 'siteAnalysis', items: ['SITE_ANALYSIS', 'ZONING_STUDY'] },
  {
    categoryKey: 'designPhases',
    items: ['CONCEPT_DESIGN', 'SCHEMATIC_DESIGN', 'DESIGN_DEVELOPMENT', 'CONSTRUCTION_DOCS']
  },
  { categoryKey: 'permits', items: ['IMB_PERMIT', 'SLF_CERT', 'ENVIRONMENTAL_PERMIT'] },
  { categoryKey: 'specialized', items: ['INTERIOR_DESIGN', 'LANDSCAPE_DESIGN', 'MEP_DESIGN', 'STRUCTURAL_DESIGN'] },
  { categoryKey: 'construction', items: ['SUPERVISION', 'AS_BUILT'] }
]

const { loading, uploadProgress } = storeToRefs(bidsStore)

const formData = ref({
  bidAmount: null,
  proposal: '',
  conceptStatement: '',
  phases: [],
  portfolioIds: [],
  facadeDescription: '',
  interiorDescription: '',
  massingDescription: '',
  zoningDescription: ''
})

const bidAmountDisplay = computed({
  get: () => formatIDRDisplay(formData.value.bidAmount),
  set: val => {
    formData.value.bidAmount = parseIDRInput(val)
  }
})

const facadeImages = ref([])
const interiorImages = ref([])
const massingImages = ref([])
const zoningImages = ref([])
const existingFacade = ref([])
const existingInterior = ref([])
const existingMassing = ref([])
const existingZoning = ref([])
const error = ref(null)
const existingBidId = ref(null)

const project = ref(null)
const projectLoading = ref(false)
const projectError = ref(null)

const sidebarImages = computed(() =>
  (project.value?.files || [])
    .filter(f => f.fileType?.startsWith('image/'))
    .map(f => ({ id: f.id, url: f.filePath, name: f.fileName }))
)

const lightboxIndex = ref(null)
const openLightbox = index => {
  lightboxIndex.value = index
}
const closeLightbox = () => {
  lightboxIndex.value = null
}
const nextLightboxImage = () => {
  if (lightboxIndex.value === null || sidebarImages.value.length === 0) return
  lightboxIndex.value = (lightboxIndex.value + 1) % sidebarImages.value.length
}
const prevLightboxImage = () => {
  if (lightboxIndex.value === null || sidebarImages.value.length === 0) return
  lightboxIndex.value = (lightboxIndex.value - 1 + sidebarImages.value.length) % sidebarImages.value.length
}
const onLightboxKeydown = e => {
  if (lightboxIndex.value === null) return
  if (e.key === 'Escape') closeLightbox()
  else if (e.key === 'ArrowRight') nextLightboxImage()
  else if (e.key === 'ArrowLeft') prevLightboxImage()
}
onMounted(() => document.addEventListener('keydown', onLightboxKeydown))
onUnmounted(() => document.removeEventListener('keydown', onLightboxKeydown))

const formatCurrency = value => {
  if (!value) return 'N/A'
  const millions = value / 1000000
  if (millions >= 1) {
    return `Rp ${millions.toFixed(0)}M`
  }
  const thousands = value / 1000
  return `Rp ${thousands.toFixed(0)}K`
}

const groupedProjectDeliverables = computed(() => {
  const deliverables = project.value?.deliverables || []
  return DELIVERABLE_CATEGORIES.map(group => ({
    categoryKey: group.categoryKey,
    items: group.items.filter(d => deliverables.includes(d))
  })).filter(group => group.items.length > 0)
})

const wordCount = computed(() => {
  const text = formData.value.conceptStatement || ''
  return text
    .trim()
    .split(/\s+/)
    .filter(word => word.length > 0).length
})

const validateWordCount = () => {
  if (wordCount.value > 200) {
    error.value = 'Concept statement must not exceed 200 words'
  } else if (error.value === 'Concept statement must not exceed 200 words') {
    error.value = null
  }
}

const deleteExistingImage = async (imageId, type) => {
  try {
    await bidsStore.deleteBidImage(imageId)
    if (type === 'facade') {
      existingFacade.value = existingFacade.value.filter(img => img.id !== imageId)
    } else if (type === 'interior') {
      existingInterior.value = existingInterior.value.filter(img => img.id !== imageId)
    } else if (type === 'massing') {
      existingMassing.value = existingMassing.value.filter(img => img.id !== imageId)
    } else if (type === 'zoning') {
      existingZoning.value = existingZoning.value.filter(img => img.id !== imageId)
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to delete image'
    console.error('Failed to delete image:', err)
  }
}

const isSavingDraft = ref(false)

const persistDraft = async () => {
  if (!formData.value.bidAmount) return

  let bid
  const bidData = {
    projectId: route.params.projectId,
    bidAmount: formData.value.bidAmount,
    proposal: formData.value.proposal
  }
  if (existingBidId.value) {
    await bidsStore.updateDraftBid(existingBidId.value, bidData)
    bid = { id: existingBidId.value }
  } else {
    bid = await bidsStore.createDraftBid(bidData)
    existingBidId.value = bid.id
  }
  await bidsStore.updateBidDetails(bid.id, {
    conceptStatement: formData.value.conceptStatement,
    phases: formData.value.phases,
    facadeDescription: formData.value.facadeDescription,
    interiorDescription: formData.value.interiorDescription,
    massingDescription: formData.value.massingDescription,
    zoningDescription: formData.value.zoningDescription
  })

  if (formData.value.portfolioIds.length > 0) {
    await bidsStore.linkPortfolios(bid.id, formData.value.portfolioIds)
  }

  const uploadIfAny = async (type, newFiles, existingRef, fileRef) => {
    const fresh = newFiles.filter(f => f instanceof File)
    if (!fresh.length) return
    try {
      const uploaded = await bidsStore.uploadBidImages(bid.id, type, fresh)
      existingRef.value = [
        ...existingRef.value,
        ...uploaded.map(img => ({ id: img.id, url: img.imageUrl, name: img.fileName }))
      ]
      fileRef.value = []
    } catch {
      // per-type failure doesn't abort the others
    }
  }

  await uploadIfAny('FACADE', facadeImages.value, existingFacade, facadeImages)
  await uploadIfAny('INTERIOR', interiorImages.value, existingInterior, interiorImages)
  await uploadIfAny('MASSING', massingImages.value, existingMassing, massingImages)
  await uploadIfAny('ZONING', zoningImages.value, existingZoning, zoningImages)
}

const saveDraftAndLeave = async () => {
  isSavingDraft.value = true
  try {
    await persistDraft()
  } catch {
    // Silent — draft save is best-effort, always navigate away
  } finally {
    isSavingDraft.value = false
  }
  router.push({ name: 'OpportunityList' })
}

const goToProfile = async () => {
  isSavingDraft.value = true
  try {
    await persistDraft()
  } catch {
    // Silent — draft save is best-effort, still send the architect to fix their profile
  } finally {
    isSavingDraft.value = false
  }
  router.push({ name: 'ArchitectProfile' })
}

const errorRef = ref(null)

const scrollToError = async () => {
  await nextTick()
  errorRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const handleSubmit = async () => {
  error.value = null

  if (!isIdentityComplete.value) {
    await profileStore.fetchProfile()
  }
  if (!isIdentityComplete.value) {
    error.value =
      t.value.bidCreate?.identityIncompleteError ||
      'Harap lengkapi data identitas berikut di halaman Profil sebelum mengirim penawaran:'
    showIdentityModal.value = true
    await scrollToError()
    return
  }

  if (wordCount.value > 200) {
    error.value = getT('bid.wordCountExceeded')
    return
  }

  const phasesTotal = formData.value.phases.reduce((sum, p) => sum + (Number(p.amount) || 0), 0)
  if (formData.value.phases.length > 0 && Math.abs(phasesTotal - Number(formData.value.bidAmount)) >= 1) {
    error.value = getT('bid.phasesTotalMismatch')
    return
  }

  try {
    let bid

    const bidData = {
      projectId: route.params.projectId,
      bidAmount: formData.value.bidAmount,
      proposal: formData.value.proposal,
      deliverables: formData.value.deliverables
    }

    if (existingBidId.value) {
      await bidsStore.updateDraftBid(existingBidId.value, bidData)
      bid = { id: existingBidId.value }
    } else {
      bid = await bidsStore.createDraftBid(bidData)
      existingBidId.value = bid.id
    }

    const bidDetailData = {
      conceptStatement: formData.value.conceptStatement,
      phases: formData.value.phases,
      facadeDescription: formData.value.facadeDescription,
      interiorDescription: formData.value.interiorDescription,
      massingDescription: formData.value.massingDescription,
      zoningDescription: formData.value.zoningDescription
    }
    await bidsStore.updateBidDetails(bid.id, bidDetailData)

    if (formData.value.portfolioIds.length > 0) {
      await bidsStore.linkPortfolios(bid.id, formData.value.portfolioIds)
    }

    const newFacade = facadeImages.value.filter(f => f instanceof File)
    if (newFacade.length > 0) {
      await bidsStore.uploadBidImages(bid.id, 'FACADE', newFacade)
    }

    const newInterior = interiorImages.value.filter(f => f instanceof File)
    if (newInterior.length > 0) {
      await bidsStore.uploadBidImages(bid.id, 'INTERIOR', newInterior)
    }

    const newMassing = massingImages.value.filter(f => f instanceof File)
    if (newMassing.length > 0) {
      await bidsStore.uploadBidImages(bid.id, 'MASSING', newMassing)
    }

    const newZoning = zoningImages.value.filter(f => f instanceof File)
    if (newZoning.length > 0) {
      await bidsStore.uploadBidImages(bid.id, 'ZONING', newZoning)
    }

    await bidsStore.submitBid(bid.id)

    router.push({ name: 'MyBids' })
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to submit bid. Please try again.'
    console.error('Failed to submit bid:', err)
  }
}

onMounted(async () => {
  projectLoading.value = true
  projectError.value = null
  try {
    await profileStore.fetchProfile()
    project.value = await projectsStore.fetchProjectForArchitect(route.params.projectId)

    await bidsStore.fetchMyBids()
    const existingDraft = bidsStore.myBids.find(
      bid => bid.projectId === parseInt(route.params.projectId) && bid.status === 'DRAFT'
    )

    if (existingDraft) {
      existingBidId.value = existingDraft.id
      formData.value.bidAmount = existingDraft.bidAmount
      formData.value.proposal = existingDraft.proposal || ''
      formData.value.conceptStatement = existingDraft.details?.conceptStatement || ''
      formData.value.phases = existingDraft.details?.phases || []
      formData.value.facadeDescription = existingDraft.details?.facadeDescription || ''
      formData.value.interiorDescription = existingDraft.details?.interiorDescription || ''
      formData.value.massingDescription = existingDraft.details?.massingDescription || ''
      formData.value.zoningDescription = existingDraft.details?.zoningDescription || ''

      try {
        const fullDraft = await bidsStore.fetchBidById(existingDraft.id)
        formData.value.portfolioIds = fullDraft.portfolioReferences?.map(p => p.id) || []
      } catch {
        formData.value.portfolioIds = []
      }

      existingFacade.value = (existingDraft.facadeImages || []).map(img => ({
        id: img.id,
        url: img.imageUrl,
        name: img.fileName || 'Facade'
      }))
      existingInterior.value = (existingDraft.interiorImages || []).map(img => ({
        id: img.id,
        url: img.imageUrl,
        name: img.fileName || 'Interior'
      }))
      existingMassing.value = (existingDraft.massingImages || []).map(img => ({
        id: img.id,
        url: img.imageUrl,
        name: img.fileName || 'Massing'
      }))
      existingZoning.value = (existingDraft.zoningImages || []).map(img => ({
        id: img.id,
        url: img.imageUrl,
        name: img.fileName || 'Zoning'
      }))
    } else {
      formData.value.proposal = profileStore.profilePhilosophy
    }
  } catch (err) {
    projectError.value = err.response?.data?.message || 'Failed to load project details'
    console.error('Failed to fetch project:', err)
  } finally {
    projectLoading.value = false
  }
})
</script>
