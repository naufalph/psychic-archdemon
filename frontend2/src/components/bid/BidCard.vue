<template>
  <div
    class="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-soft hover:shadow-glow transition-all cursor-pointer group"
    @click="navigate"
  >
    <div class="relative aspect-[16/10] overflow-hidden bg-gradient-to-br from-brand-brown to-brand-brown-900">
      <img
        v-if="coverImage"
        :src="coverImage"
        :alt="bid.projectTitle"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
      />
      <BidStatusBadge :status="bid.status" class="absolute top-3 right-3" />
      <ProjectStatusBadge :status="bid.projectStatus" class="absolute bottom-3 left-3" />
    </div>

    <div class="p-5">
      <h3 class="text-base font-bold text-black mb-1 line-clamp-1">{{ bid.projectTitle || 'Project' }}</h3>
      <p v-if="bid.projectLocation" class="text-gray-500 text-xs mb-3 flex items-center gap-1">
        <MapPin :size="11" /> {{ bid.projectLocation }}
      </p>

      <div class="flex flex-wrap gap-2 mb-4">
        <span class="bg-gray-100 px-3 py-1.5 rounded-full text-xs font-medium border border-gray-200">
          IDR {{ formatNumber(bid.bidAmount) }}
        </span>
        <span class="bg-gray-100 px-3 py-1.5 rounded-full text-xs font-medium border border-gray-200">
          {{ bid.proposedTimelineDays }} {{ t.clientDashboard.days }}
        </span>
      </div>

      <div class="flex items-center justify-between pt-3 border-t border-gray-100">
        <span class="text-xs text-gray-500">
          {{ formatDate(bid.submittedAt) }}
        </span>
        <button
          class="bg-black text-white text-xs font-bold px-4 py-2 rounded-full tracking-wider hover:bg-brand-brown transition"
          @click.stop="navigate"
        >
          {{ ctaLabel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { MapPin } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import BidStatusBadge from '@/components/project/BidStatusBadge.vue'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'

const props = defineProps({
  bid: { type: Object, required: true }
})

const { t } = useI18n()
const router = useRouter()

const coverImage = computed(() => props.bid.projectCoverImagePath || props.bid.facadeImages?.[0]?.imageUrl || null)

const ctaLabel = computed(() => {
  if (props.bid.status === 'ACCEPTED' && props.bid.projectStatus === 'NEGOTIATION') return 'Finalize →'
  if (props.bid.status === 'ACCEPTED' && props.bid.projectStatus === 'IN_PROGRESS') return 'View Project →'
  return 'View →'
})

const navigate = () => {
  const projectId = props.bid.projectId
  if (!projectId) return
  if (props.bid.status === 'ACCEPTED' && props.bid.projectStatus === 'NEGOTIATION') {
    router.push({ name: 'ArchitectFinalizationView', params: { projectId } })
  } else if (props.bid.status === 'ACCEPTED' && props.bid.projectStatus === 'IN_PROGRESS') {
    router.push({ name: 'ArchitectProjectWorkspace', params: { id: projectId } })
  } else {
    router.push({ name: 'ProjectDetailForArchitect', params: { projectId } })
  }
}

const formatNumber = num => new Intl.NumberFormat('id-ID').format(num)

const formatDate = dateString => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('id-ID')
}
</script>
