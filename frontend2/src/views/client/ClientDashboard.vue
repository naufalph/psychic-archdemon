<template>
  <div class="min-h-screen bg-[#F4F5F7]">
    <ClientNavbar />

    <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: linear-gradient(#000 1px, transparent 1px), linear-gradient(90deg, #000 1px, transparent 1px); background-size: 50px 50px"></div>

    <div class="relative z-10 max-w-7xl mx-auto px-6 py-12">
      <div class="mb-12">
        <h1 class="text-4xl font-bold mb-2 text-black">Client Dashboard</h1>
        <p class="text-lg text-gray-500">Welcome to your workspace</p>
      </div>

      <ProjectList />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import ClientNavbar from '@/components/layout/ClientNavbar.vue'
import ProjectList from './ProjectList.vue'

onMounted(() => {
  localStorage.setItem('last_dashboard_role', 'CLIENT')
})
</script>
