<template>
  <div class="min-h-screen bg-[#F4F5F7]">
    <ArchitectNavbar />

    <div
      class="absolute inset-0 opacity-[0.03] pointer-events-none"
      style="
        background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px);
        background-size: 50px 50px;
      "
    ></div>

    <div class="relative z-10 max-w-7xl mx-auto px-6 py-12">
      <div class="mb-12">
        <h1 class="text-4xl font-bold mb-2 text-black">Architect Dashboard</h1>
        <p class="text-lg text-gray-500">Welcome to your workspace</p>
      </div>

      <div class="grid md:grid-cols-3 gap-6">
        <router-link
          :to="{ name: 'OpportunityList' }"
          class="bg-white rounded-3xl p-8 border border-gray-200 hover:shadow-glow transition cursor-pointer"
        >
          <div class="w-16 h-16 bg-[#F5E6D3] rounded-full flex items-center justify-center mb-4">
            <Search :size="32" class="text-[#7C4728]" />
          </div>
          <h3 class="font-bold text-xl mb-2 text-black">Find Projects</h3>
          <p class="text-sm text-gray-600">Browse available opportunities</p>
        </router-link>

        <router-link
          :to="{ name: 'MyBids' }"
          class="bg-white rounded-3xl p-8 border border-gray-200 hover:shadow-glow transition cursor-pointer"
        >
          <div class="w-16 h-16 bg-[#F5E6D3] rounded-full flex items-center justify-center mb-4">
            <ClipboardList :size="32" class="text-[#7C4728]" />
          </div>
          <h3 class="font-bold text-xl mb-2 text-black">My Bids</h3>
          <p class="text-sm text-gray-600">Track your proposals</p>
        </router-link>

        <router-link
          :to="{ name: 'ArchitectPortfolios' }"
          class="bg-white rounded-3xl p-8 border border-gray-200 hover:shadow-glow transition cursor-pointer"
        >
          <div class="w-16 h-16 bg-[#F5E6D3] rounded-full flex items-center justify-center mb-4">
            <Layout :size="32" class="text-[#7C4728]" />
          </div>
          <h3 class="font-bold text-xl mb-2 text-black">My Portfolio</h3>
          <p class="text-sm text-gray-600">Showcase your work</p>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import ArchitectNavbar from '@/components/layout/ArchitectNavbar.vue'
import { Layout, Search, ClipboardList } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  authStore.updateLastLoginRole('ARCHITECT')
})
</script>
