<template>
  <div class="min-h-full bg-surface-alt">
    <div
      class="absolute inset-0 opacity-[0.03] pointer-events-none"
      style="
        background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px);
        background-size: 50px 50px;
      "
    ></div>

    <div class="relative z-10 max-w-7xl mx-auto px-6 py-12">
      <button
        @click="router.push({ name: 'ArchitectDashboard' })"
        class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition"
      >
        <ArrowLeft :size="20" />
        {{ t.common.backToDashboard }}
      </button>

      <div class="mb-8">
        <h1 class="text-4xl font-bold text-black mb-2">{{ t.myBids.title }}</h1>
        <p class="text-gray-500">{{ t.myBids.subtitle }}</p>
      </div>

      <div v-if="loading" class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        <div v-for="n in 6" :key="n" class="bg-white rounded-2xl border border-gray-200 overflow-hidden animate-pulse">
          <div class="aspect-[16/10] bg-gray-200" />
          <div class="p-5">
            <div class="h-4 bg-gray-200 rounded w-3/4 mb-2" />
            <div class="h-3 bg-gray-200 rounded w-1/2 mb-4" />
            <div class="flex gap-2 mb-4">
              <div class="h-6 bg-gray-200 rounded-full w-24" />
              <div class="h-6 bg-gray-200 rounded-full w-20" />
            </div>
            <div class="h-px bg-gray-100 mb-3" />
            <div class="flex justify-between">
              <div class="h-3 bg-gray-200 rounded w-16" />
              <div class="h-7 bg-gray-200 rounded-full w-20" />
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="myBids.length === 0" class="text-center py-20 bg-white rounded-3xl border border-gray-200">
        <FileText :size="64" class="text-gray-300 mx-auto mb-4" />
        <h3 class="text-xl font-bold text-gray-900 mb-2">{{ t.myBids.empty.title }}</h3>
        <p class="text-gray-500 mb-6">{{ t.myBids.empty.message }}</p>
        <router-link :to="{ name: 'OpportunityList' }">
          <button class="bg-brand-brown hover:bg-black text-white px-6 py-3 rounded-full font-medium transition">
            {{ t.myBids.empty.cta }}
          </button>
        </router-link>
      </div>

      <div v-else class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        <BidCard v-for="bid in myBids" :key="bid.id" :bid="bid" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, FileText } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useBidsStore } from '@/stores/bids'
import BidCard from '@/components/bid/BidCard.vue'

const { t } = useI18n()
const router = useRouter()
const bidsStore = useBidsStore()

const { myBids, loading } = storeToRefs(bidsStore)

onMounted(async () => {
  try {
    await bidsStore.fetchMyBids()
  } catch (err) {
    console.error('Failed to fetch bids:', err)
  }
})
</script>
