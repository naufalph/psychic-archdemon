<template>
  <div class="min-h-screen bg-surface-alt">
    <div v-if="loading" class="max-w-[1280px] mx-auto px-4 md:px-6 pt-10">
      <div class="bg-white rounded-card border border-hairline p-12 animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/2 mb-4" />
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-8" />
        <div class="h-32 bg-gray-200 rounded" />
      </div>
    </div>

    <div v-else-if="error" class="max-w-[1280px] mx-auto px-4 md:px-6 pt-10">
      <div class="bg-white rounded-card border border-hairline p-12 flex flex-col items-center gap-4 text-center">
        <p class="text-body font-medium text-red-600 m-0">{{ error }}</p>
        <button class="text-caption font-semibold text-ink-900 hover:underline" @click="load">{{ v2.retry }}</button>
      </div>
    </div>

    <DetailShell v-else-if="bid" :crumb-label="v2.crumbMyBids" :crumb-to="{ name: 'MyBids' }" :title="bid.projectTitle">
      <div class="flex flex-col gap-6 min-w-0">
        <section class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-7">
          <div class="flex flex-wrap-reverse justify-between items-start gap-4">
            <div class="flex flex-col gap-2 min-w-0">
              <h1
                class="m-0 text-2xl md:text-[32px] break-words font-bold tracking-[-0.03em] leading-[1.15] text-ink-900 text-pretty"
              >
                {{ bid.projectTitle }}
              </h1>
              <p class="m-0 text-body-sm text-ink-400">{{ metaLine }}</p>
            </div>
            <span
              class="shrink-0 inline-flex items-center gap-2 whitespace-nowrap h-[30px] px-3 rounded-full bg-surface-alt text-caption-sm font-semibold text-ink-900"
            >
              <span class="w-2 h-2 rounded-full" :class="BID_DOT[bid.status] || 'bg-ink-300'" />
              {{ t.bidStatus?.[bid.status] || bid.status }}
            </span>
          </div>
          <div class="grid grid-cols-2 md:grid-cols-3 gap-6 border-t border-hairline pt-6">
            <div v-for="fact in facts" :key="fact.k" class="flex flex-col gap-1.5 min-w-0">
              <span class="text-caption-sm font-normal text-ink-400">{{ fact.k }}</span>
              <span class="text-[22px] font-semibold tracking-[-0.025em] leading-[1.2] text-ink-900">{{ fact.v }}</span>
              <span v-if="fact.sub" class="text-caption-sm font-normal text-ink-300">{{ fact.sub }}</span>
            </div>
          </div>
        </section>

        <section
          v-if="bid.proposal"
          class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-4"
        >
          <h2 class="m-0 text-xl font-bold tracking-[-0.025em] text-ink-900">{{ v2.bidOverview }}</h2>
          <p class="m-0 text-body leading-[1.65] text-ink-500 whitespace-pre-line text-pretty">{{ bid.proposal }}</p>
        </section>

        <section
          v-if="bid.details?.conceptStatement"
          class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-4"
        >
          <h2 class="m-0 text-xl font-bold tracking-[-0.025em] text-ink-900">{{ v2.bidConcept }}</h2>
          <p class="m-0 text-body leading-[1.65] text-ink-500 whitespace-pre-line text-pretty">
            {{ bid.details.conceptStatement }}
          </p>
        </section>

        <section
          v-for="gallery in galleries"
          :key="gallery.title"
          class="bg-white border border-hairline rounded-card p-6 md:p-8"
        >
          <BidImageGallery :images="gallery.images" :title="gallery.title" :description="gallery.description" />
        </section>

        <section v-if="bid.details?.phases?.length" class="bg-white border border-hairline rounded-card p-6 md:p-8">
          <BidPaymentSchedule
            :phases="bid.details.phases"
            :project-deliverables="project?.deliverables || []"
            :total="bid.bidAmount"
          />
        </section>

        <section
          v-if="bid.portfolioReferences?.length"
          class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-5"
        >
          <h2 class="m-0 text-xl font-bold tracking-[-0.025em] text-ink-900">{{ t.bidDetail.portfolioProjects }}</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            <button
              v-for="portfolio in bid.portfolioReferences"
              :key="portfolio.id"
              type="button"
              class="text-left rounded-2xl p-3 border border-hairline hover:border-ink-900 transition"
              @click="viewingPortfolio = portfolio"
            >
              <div v-if="portfolio.images?.[0]" class="aspect-[4/3] bg-surface-alt rounded-xl mb-3 overflow-hidden">
                <img
                  :src="portfolio.images[0].mediumUrl || portfolio.images[0].originalUrl"
                  :alt="portfolio.title"
                  class="w-full h-full object-cover"
                />
              </div>
              <p class="m-0 text-caption font-semibold text-ink-900">{{ portfolio.title }}</p>
            </button>
          </div>
          <PortfolioDetailPopup :portfolio="viewingPortfolio" @close="viewingPortfolio = null" />
        </section>
      </div>

      <template #rail>
        <BriefRail
          :title="rail.title"
          :body="rail.body"
          :cta-label="rail.cta"
          :cta-variant="rail.ctaVariant"
          :footnote="rail.footnote"
          :meta="railMeta"
          @cta="rail.onCta"
        >
          <template v-if="rail.showBriefLink" #after>
            <BaseButton
              variant="outline"
              size="md"
              full-width
              @click="router.push({ name: 'ProjectDetailForArchitect', params: { projectId: bid.projectId } })"
            >
              {{ v2.bidViewBrief }}
            </BaseButton>
          </template>
        </BriefRail>
      </template>
    </DetailShell>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from '@/composables/useI18n'
import { useBidsStore } from '@/stores/bids'
import { useProjectsStore } from '@/stores/projects'
import { placeLabel } from '@/constants/regions'
import BaseButton from '@/components/ui/BaseButton.vue'
import BidImageGallery from '@/components/bid/BidImageGallery.vue'
import BidPaymentSchedule from '@/components/bid/BidPaymentSchedule.vue'
import PortfolioDetailPopup from '@/components/bid/PortfolioDetailPopup.vue'
import DetailShell from '@/components/project/brief/DetailShell.vue'
import BriefRail from '@/components/project/brief/BriefRail.vue'
import { briefView, formatBriefDate, formatShortFee } from '@/utils/projectBrief'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const bidsStore = useBidsStore()
const projectsStore = useProjectsStore()

const bid = ref(null)
const project = ref(null)
const loading = ref(false)
const error = ref(null)
const viewingPortfolio = ref(null)

const v2 = computed(() => t.value.projectDetailV2)

const BID_DOT = {
  DRAFT: 'bg-amber-400',
  PENDING: 'bg-accent-blue',
  ACCEPTED: 'bg-brand-green',
  REJECTED: 'bg-red-500',
  WITHDRAWN: 'bg-ink-300'
}

// The project read is a bonus (fee, deliverable matching); a closed project can refuse it
const brief = computed(() =>
  project.value
    ? briefView(project.value, {
        locale: locale.value,
        copy: v2.value,
        deliverableItems: t.value.bidCreate?.deliverableItems
      })
    : null
)

const metaLine = computed(() =>
  [
    placeLabel({ city: bid.value.projectCity, province: bid.value.projectProvince }),
    bid.value.submittedAt && `${v2.value.bidSent} ${formatBriefDate(bid.value.submittedAt, locale.value)}`
  ]
    .filter(Boolean)
    .join(' · ')
)

const facts = computed(() =>
  [
    { k: v2.value.bidAmount, v: formatShortFee(bid.value.bidAmount, v2.value) },
    {
      k: v2.value.bidDuration,
      v: bid.value.proposedTimelineDays ? v2.value.bidDays.replace('{n}', bid.value.proposedTimelineDays) : '—'
    },
    brief.value && { k: brief.value.feeLabel, v: brief.value.facts[0].v, sub: brief.value.facts[0].sub }
  ].filter(Boolean)
)

const galleries = computed(() =>
  [
    {
      title: t.value.bidDetail.facade,
      images: bid.value.facadeImages,
      description: bid.value.details?.facadeDescription
    },
    {
      title: t.value.bidDetail.interior,
      images: bid.value.interiorImages,
      description: bid.value.details?.interiorDescription
    },
    {
      title: t.value.bidDetail.massing,
      images: bid.value.massingImages,
      description: bid.value.details?.massingDescription
    },
    {
      title: t.value.bidDetail.zoning,
      images: bid.value.zoningImages,
      description: bid.value.details?.zoningDescription
    }
  ].filter(gallery => gallery.images?.length)
)

const railMeta = computed(() =>
  [
    bid.value.submittedAt && { k: v2.value.bidSent, v: formatBriefDate(bid.value.submittedAt, locale.value) },
    brief.value && { k: brief.value.feeLabel, v: brief.value.fee },
    brief.value && { k: v2.value.startDesign, v: brief.value.start }
  ].filter(Boolean)
)

const rail = computed(() => {
  const copy = v2.value
  const legacy = t.value.projectDetailArchitect
  const status = bid.value.status
  const projectStatus = bid.value.projectStatus
  const briefLink = {
    cta: copy.bidViewBrief,
    ctaVariant: 'outline',
    showBriefLink: false,
    onCta: () => router.push({ name: 'ProjectDetailForArchitect', params: { projectId: bid.value.projectId } })
  }
  const withAction = (cta, onCta) => ({ cta, ctaVariant: 'primary', showBriefLink: true, onCta })

  if (status === 'DRAFT') {
    return {
      title: copy.archDraftTitle,
      body: copy.archDraftBody,
      footnote: copy.archFootnote,
      ...(projectStatus === 'OPEN'
        ? withAction(copy.bidEdit, () => router.push({ name: 'BidCreate', params: { projectId: bid.value.projectId } }))
        : briefLink)
    }
  }
  if (status === 'ACCEPTED') {
    if (projectStatus === 'NEGOTIATION') {
      return {
        title: copy.bidAcceptedTitle,
        body: copy.bidAcceptedBody,
        footnote: '',
        ...withAction(legacy.finalizeAgreement || copy.bidAcceptedTitle, () =>
          router.push({ name: 'ArchitectFinalizationView', params: { projectId: bid.value.projectId } })
        )
      }
    }
    const working = ['IN_PROGRESS', 'COMPLETED'].includes(projectStatus)
    return {
      title: copy.bidAcceptedTitle,
      body: working ? copy.bidAcceptedWorkingBody : copy.bidAcceptedBody,
      footnote: '',
      ...(working
        ? withAction(copy.openWorkspace, () =>
            router.push({ name: 'ArchitectProjectWorkspace', params: { id: bid.value.projectId } })
          )
        : briefLink)
    }
  }
  if (status === 'REJECTED') {
    return { title: legacy.bidNotSelected, body: legacy.bidNotSelectedDesc, footnote: '', ...briefLink }
  }
  if (status === 'WITHDRAWN') {
    return { title: legacy.bidWithdrawn, body: legacy.bidWithdrawnDesc, footnote: '', ...briefLink }
  }
  return { title: copy.archSentTitle, body: copy.archSentBody, footnote: copy.archFootnote, ...briefLink }
})

const load = async () => {
  loading.value = true
  error.value = null
  try {
    bid.value = await bidsStore.fetchBidById(route.params.bidId)
    try {
      project.value = await projectsStore.fetchProjectForArchitect(bid.value.projectId)
    } catch {
      project.value = null
    }
  } catch (err) {
    error.value = err.response?.data?.message || v2.value.loadError
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
