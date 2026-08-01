<template>
  <div class="min-h-full bg-surface-alt">
    <div class="max-w-7xl mx-auto px-6 py-10">
      <div class="flex justify-between items-center mb-10">
        <div>
          <h1 class="text-4xl font-bold text-black">{{ t.architectDashboard.welcome }}, {{ architectName }}</h1>
          <p class="text-gray-500 mt-1">{{ t.architectDashboard.manageProjects }}</p>
        </div>
        <router-link :to="{ name: 'OpportunityList' }">
          <button
            class="bg-black text-white px-6 py-3 rounded-full font-bold tracking-widest text-sm hover:bg-brand-brown transition flex items-center gap-2"
          >
            <Search :size="16" />
            {{ t.architectDashboard.findProjects }}
          </button>
        </router-link>
      </div>

      <div class="grid grid-cols-3 gap-4 mb-10">
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">
            {{ t.architectDashboard.activeBids }}
          </p>
          <p class="text-4xl font-bold text-black">{{ pendingBids.length }}</p>
        </div>
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">
            {{ t.architectDashboard.accepted }}
          </p>
          <p class="text-4xl font-bold text-brand-brown">{{ acceptedBids.length }}</p>
        </div>
        <div class="bg-white rounded-2xl p-6 border border-gray-200 shadow-soft">
          <p class="text-xs text-gray-500 uppercase font-bold tracking-wider mb-2">
            {{ t.architectDashboard.bidTokens }}
          </p>
          <p class="text-4xl font-bold text-black">{{ bidsStore.quota.tokensRemaining }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-bold text-black tracking-wide">{{ t.architectDashboard.myBids }}</h2>
            <router-link
              :to="{ name: 'MyBids' }"
              class="text-xs font-bold text-gray-500 hover:text-black tracking-widest transition"
            >
              {{ t.architectDashboard.viewAll }}
            </router-link>
          </div>

          <div
            v-if="recentBids.length === 0"
            class="bg-white rounded-2xl border border-gray-200 p-12 text-center shadow-soft"
          >
            <ClipboardList :size="48" class="text-gray-300 mx-auto mb-3" />
            <p class="text-gray-500 text-sm">{{ t.architectDashboard.noBidsYet }}</p>
            <router-link :to="{ name: 'OpportunityList' }">
              <button
                class="mt-4 bg-brand-brown hover:bg-black text-white px-5 py-2 rounded-full text-sm font-medium transition"
              >
                {{ t.architectDashboard.browseProjects }}
              </button>
            </router-link>
          </div>

          <div v-else class="space-y-3">
            <router-link
              v-for="bid in recentBids"
              :key="bid.id"
              :to="{ name: 'MyBids' }"
              class="bg-white rounded-2xl border border-gray-200 p-5 shadow-soft flex items-center justify-between hover:shadow-md transition block"
            >
              <div class="flex items-center gap-4 min-w-0">
                <div class="w-10 h-10 rounded-full bg-brand-tan flex items-center justify-center flex-shrink-0">
                  <FileText :size="18" class="text-brand-brown" />
                </div>
                <div class="min-w-0">
                  <p class="font-semibold text-gray-900 truncate">{{ bid.projectTitle || 'Untitled Project' }}</p>
                  <p class="text-xs text-brand-brown font-medium mt-0.5">IDR {{ formatNumber(bid.bidAmount) }}</p>
                </div>
              </div>
              <span
                class="ml-4 flex-shrink-0 text-xs font-bold px-3 py-1 rounded-full"
                :class="statusClass(bid.status)"
              >
                {{ bid.status }}
              </span>
            </router-link>
          </div>
        </div>

        <div class="space-y-6">
          <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft">
            <h2 class="text-sm font-bold text-black tracking-widest uppercase mb-4">
              {{ t.architectDashboard.bidTokens }}
            </h2>
            <div class="flex items-end gap-1 mb-1">
              <span class="text-3xl font-bold text-black">{{ bidsStore.quota.tokensRemaining }}</span>
              <span class="text-gray-400 text-sm mb-1">{{ t.architectDashboard.tokensRemaining }}</span>
            </div>
            <p v-if="bidsStore.quota.tier" class="text-xs text-gray-500 mb-4">
              {{ t.architectDashboard.plan }}
              <span class="font-semibold text-gray-700">{{ bidsStore.quota.tier }}</span>
            </p>
            <button
              @click="tokenPurchaseStore.openModal()"
              class="w-full bg-black text-white px-4 py-2 rounded-full text-xs font-bold tracking-wider hover:bg-brand-brown transition"
            >
              {{ t.architectDashboard.buyTokens }}
            </button>
          </div>

          <div class="bg-brand-brown rounded-2xl p-6 text-white">
            <p class="text-xs font-bold tracking-widest uppercase mb-2 text-white/70">
              {{ t.architectDashboard.opportunities }}
            </p>
            <h3 class="text-lg font-bold mb-2">{{ t.architectDashboard.findNewProjects }}</h3>
            <p class="text-sm text-white/80 mb-4">{{ t.architectDashboard.browseProjectsDesc }}</p>
            <router-link :to="{ name: 'OpportunityList' }">
              <button
                class="bg-white text-brand-brown px-4 py-2 rounded-full text-xs font-bold tracking-wider hover:bg-brand-tan transition"
              >
                {{ t.architectDashboard.browseProjectsBtn }}
              </button>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>

  <TokenPurchaseModal />
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { Search, ClipboardList, FileText } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useBidsStore } from '@/stores/bids'
import { useTokenPurchaseStore } from '@/stores/tokenPurchase'
import { useI18n } from '@/composables/useI18n'
import TokenPurchaseModal from '@/components/architect/TokenPurchaseModal.vue'

const { t } = useI18n()
const authStore = useAuthStore()
const bidsStore = useBidsStore()
const tokenPurchaseStore = useTokenPurchaseStore()

const { myBids, pendingBids, acceptedBids } = storeToRefs(bidsStore)

const architectName = computed(() => {
  const user = authStore.user
  if (!user) return 'Architect'
  return user.name || user.email?.split('@')[0] || 'Architect'
})

const recentBids = computed(() => myBids.value.slice(0, 5))

const formatNumber = value => {
  if (!value) return '0'
  return new Intl.NumberFormat('id-ID').format(value)
}

const statusClass = status => {
  switch (status) {
    case 'ACCEPTED':
      return 'bg-green-100 text-green-700'
    case 'REJECTED':
    case 'WITHDRAWN':
      return 'bg-red-100 text-red-600'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

onMounted(async () => {
  authStore.updateLastLoginRole('ARCHITECT')
  await Promise.all([bidsStore.fetchMyBids(), bidsStore.fetchQuota()])
})
</script>
