<template>
  <div class="min-h-full bg-[#F4F5F7] px-6 py-8">
    <div class="max-w-3xl mx-auto">
      <div class="mb-8">
        <p class="text-xs text-gray-400 uppercase font-bold tracking-wide mb-1">{{ t.clientPaymentsPage?.title }}</p>
        <h1 class="text-2xl font-bold text-gray-900">{{ t.clientPaymentsPage?.subtitle }}</h1>
        <p class="text-sm text-gray-500 mt-1">{{ t.clientPaymentsPage?.selectProject }}</p>
      </div>

      <div v-if="loading" class="space-y-4">
        <div v-for="n in 2" :key="n" class="bg-white rounded-xl border border-gray-200 p-5 animate-pulse">
          <div class="flex gap-4">
            <div class="w-20 h-20 bg-gray-200 rounded-lg shrink-0" />
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-gray-200 rounded w-1/2" />
              <div class="h-3 bg-gray-200 rounded w-1/3" />
              <div class="h-3 bg-gray-200 rounded w-2/3" />
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeProjects.length === 0" class="bg-white rounded-xl border border-gray-200 py-16 text-center">
        <CreditCard :size="40" class="text-gray-300 mx-auto mb-3" />
        <p class="text-gray-500 font-medium">{{ t.clientPaymentsPage?.noProjects }}</p>
        <p class="text-sm text-gray-400 mt-1">{{ t.clientPaymentsPage?.noProjectsHint }}</p>
        <RouterLink
          to="/client/projects"
          class="inline-block mt-4 text-sm font-semibold text-[#7C4728] hover:underline"
        >
          {{ t.clientPaymentsPage?.viewProjects }}
        </RouterLink>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="item in activeProjects"
          :key="item.project.id"
          class="bg-white rounded-xl border border-gray-200 p-5 hover:border-gray-300 transition cursor-pointer"
          @click="openProject(item.project.id)"
        >
          <div class="flex items-center gap-4">
            <div class="w-20 h-20 rounded-lg overflow-hidden bg-gray-100 shrink-0">
              <img
                v-if="getCoverImage(item.project)"
                :src="getCoverImage(item.project)"
                :alt="item.project.title"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full flex items-center justify-center">
                <Building2 :size="24" class="text-gray-300" />
              </div>
            </div>

            <div class="flex-1 min-w-0">
              <h3 class="font-bold text-gray-900 truncate">{{ item.project.title }}</h3>
              <p class="text-sm text-gray-500 flex items-center gap-1 mt-0.5">
                <MapPin :size="12" /> {{ item.project.location }}
              </p>

              <div v-if="item.phases.length" class="mt-2 flex items-center gap-2">
                <span class="text-xs text-gray-500"
                  >{{ t.clientPaymentsPage?.phase }} {{ item.currentPhase }} {{ t.clientPaymentsPage?.of }}
                  {{ item.phases.length }}</span
                >
                <span class="text-gray-300">·</span>
                <span v-if="item.pendingAmount > 0" class="text-xs font-semibold text-amber-600">
                  {{ formatAmount(item.pendingAmount) }} {{ t.clientPaymentsPage?.pending }}
                </span>
                <span v-else class="text-xs font-semibold text-green-600">{{ t.clientPaymentsPage?.allPaid }}</span>
              </div>

              <div v-if="item.phases.length" class="mt-2 w-full bg-gray-100 rounded-full h-1">
                <div class="bg-green-500 h-1 rounded-full" :style="{ width: item.progressPercent + '%' }" />
              </div>
            </div>

            <ChevronRight :size="18" class="text-gray-400 shrink-0" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Building2, MapPin, CreditCard, ChevronRight } from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { paymentAPI } from '@/services/api'
import { useI18n } from '@/composables/useI18n'

const router = useRouter()
const projectsStore = useProjectsStore()
const { t } = useI18n()

const loading = ref(true)
const phaseData = ref({})

const inProgressProjects = computed(() =>
  projectsStore.projects.filter(p => p.status === 'IN_PROGRESS' || p.status === 'COMPLETED')
)

const activeProjects = computed(() =>
  inProgressProjects.value.map(project => {
    const phases = phaseData.value[project.id] || []
    const paidPhases = phases.filter(p => p.paymentStatus === 'COMPLETED')
    const pendingPhases = phases.filter(p => p.paymentStatus !== 'COMPLETED')
    const pendingAmount = pendingPhases.reduce((sum, p) => sum + Number(p.amount), 0)
    const totalAmount = phases.reduce((sum, p) => sum + Number(p.amount), 0)
    const currentPhase = paidPhases.length + 1
    const progressPercent =
      totalAmount > 0
        ? (phases.filter(p => p.paymentStatus === 'COMPLETED').reduce((s, p) => s + Number(p.amount), 0) /
            totalAmount) *
          100
        : 0

    return { project, phases, pendingAmount, currentPhase: Math.min(currentPhase, phases.length), progressPercent }
  })
)

const getCoverImage = project => {
  const files = project.files
  if (!files?.length) return null
  return files.find(f => f.fileType?.startsWith('image/'))?.filePath || null
}

const formatAmount = amount => {
  if (!amount) return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(amount)
}

const openProject = id => {
  router.push({ name: 'ActiveProjectDashboard', params: { id } })
}

onMounted(async () => {
  await projectsStore.fetchMyProjects()
  await Promise.all(
    inProgressProjects.value.map(async p => {
      try {
        const res = await paymentAPI.getProjectPhasePayments(p.id)
        phaseData.value[p.id] = res.data.data
      } catch {
        phaseData.value[p.id] = []
      }
    })
  )
  loading.value = false
})
</script>
