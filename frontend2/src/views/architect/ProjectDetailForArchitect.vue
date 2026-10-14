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
        <AlertCircle :size="48" class="text-red-400" />
        <p class="text-body font-medium text-red-600 m-0">{{ error }}</p>
        <button class="text-caption font-semibold text-ink-900 hover:underline" @click="router.back()">
          {{ t.common.back }}
        </button>
      </div>
    </div>

    <DetailShell v-else-if="project" :crumb-label="crumb.label" :crumb-to="crumb.to" :title="brief.title">
      <ProjectBriefColumn
        :project="project"
        :brief="brief"
        :status="project.status"
        :needs-title="v2.needsArchitect"
        :refs-hint="v2.refsHintArchitect"
      />
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
          <ClosesIn v-if="project.status === 'OPEN' && project.biddingDeadline" :deadline="project.biddingDeadline" />
          <div v-if="bidRows.length" class="flex flex-col">
            <div
              v-for="row in bidRows"
              :key="row.k"
              class="flex justify-between gap-3 py-2.5 border-b border-[#F0F0F0] text-caption font-normal"
            >
              <span class="text-ink-400">{{ row.k }}</span>
              <span class="font-semibold text-ink-900">{{ row.v }}</span>
            </div>
          </div>
        </BriefRail>
      </template>
    </DetailShell>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AlertCircle } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import DetailShell from '@/components/project/brief/DetailShell.vue'
import ProjectBriefColumn from '@/components/project/brief/ProjectBriefColumn.vue'
import BriefRail from '@/components/project/brief/BriefRail.vue'
import ClosesIn from '@/components/project/brief/ClosesIn.vue'
import { briefView, formatBriefDate, formatShortFee } from '@/utils/projectBrief'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const project = ref(null)
const loading = ref(false)
const error = ref(null)
const existingBid = ref(null)

const v2 = computed(() => t.value.projectDetailV2)

const brief = computed(() =>
  briefView(project.value, {
    locale: locale.value,
    copy: v2.value,
    deliverableItems: t.value.bidCreate?.deliverableItems
  })
)

const crumb = computed(() =>
  existingBid.value
    ? { label: v2.value.crumbMyBids, to: { name: 'MyBids' } }
    : { label: v2.value.crumbFindProjects, to: { name: 'OpportunityList' } }
)

const railMeta = computed(() =>
  [
    { k: brief.value.feeLabel, v: brief.value.fee },
    { k: v2.value.startDesign, v: brief.value.start },
    brief.value.submittedAt && { k: v2.value.briefSubmitted, v: formatBriefDate(brief.value.submittedAt, locale.value) }
  ].filter(Boolean)
)

const bidRows = computed(() => {
  const bid = existingBid.value
  if (bid?.status !== 'PENDING') return []
  return [
    { k: v2.value.bidAmount, v: formatShortFee(bid.bidAmount, v2.value) },
    bid.proposedTimelineDays && {
      k: v2.value.bidDuration,
      v: v2.value.bidDays.replace('{n}', bid.proposedTimelineDays)
    },
    bid.submittedAt && { k: v2.value.bidSent, v: formatBriefDate(bid.submittedAt, locale.value) }
  ].filter(Boolean)
})

const goToBid = () => router.push({ name: 'BidCreate', params: { projectId: route.params.projectId } })

// Losing and withdrawn bidders can still open this page, so explain where the project ended up
const rail = computed(() => {
  const copy = v2.value
  const legacy = t.value.projectDetailArchitect
  const bid = existingBid.value
  const projectStatus = project.value?.status
  const base = { cta: '', ctaVariant: 'primary', footnote: copy.archFootnote, onCta: () => {} }

  if (bid?.status === 'PENDING') {
    return {
      ...base,
      title: copy.archSentTitle,
      body: copy.archSentBody,
      cta: copy.archSentCta,
      ctaVariant: 'outline',
      onCta: () => router.push({ name: 'ArchitectBidDetail', params: { bidId: bid.id } })
    }
  }
  if (bid?.status === 'DRAFT' && projectStatus === 'OPEN') {
    return { ...base, title: copy.archDraftTitle, body: copy.archDraftBody, cta: copy.archDraftCta, onCta: goToBid }
  }
  if (bid?.status === 'ACCEPTED') {
    const working = ['IN_PROGRESS', 'COMPLETED'].includes(projectStatus)
    return {
      ...base,
      title: copy.bidAcceptedTitle,
      body: working ? copy.bidAcceptedWorkingBody : copy.bidAcceptedBody,
      footnote: '',
      cta: working ? copy.openWorkspace : '',
      onCta: () => router.push({ name: 'ArchitectProjectWorkspace', params: { id: route.params.projectId } })
    }
  }
  if (bid?.status === 'REJECTED') {
    return { ...base, title: legacy.bidNotSelected, body: legacy.bidNotSelectedDesc, footnote: '' }
  }
  if (bid?.status === 'WITHDRAWN') {
    return { ...base, title: legacy.bidWithdrawn, body: legacy.bidWithdrawnDesc, footnote: '' }
  }
  if (projectStatus !== 'OPEN') {
    return { ...base, title: legacy.projectClosed, body: legacy.projectClosedDesc, footnote: '' }
  }
  return { ...base, title: copy.archOpenTitle, body: copy.archOpenBody, cta: copy.archOpenCta, onCta: goToBid }
})

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
    error.value = err.response?.data?.message || v2.value.loadError
    console.error('Failed to fetch project:', err)
  } finally {
    loading.value = false
  }
})
</script>
