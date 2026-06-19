<template>
  <div class="min-h-full bg-[#F4F5F7]">
    <div class="max-w-7xl mx-auto px-6 py-12">
      <button
        @click="$router.push({ name: 'ArchitectDashboard' })"
        class="flex items-center gap-2 text-black/60 hover:text-black transition-colors mb-6"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        {{ t.common.backToDashboard }}
      </button>

      <div class="space-y-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-4xl font-black tracking-tighter text-black">
              {{ t.portfolio.title }}
            </h1>
            <p class="text-lg text-black/60 tracking-tight mt-2">Showcase your architectural projects</p>
          </div>
          <button
            @click="handleAddPortfolio"
            class="px-6 py-3 bg-[#7C4728] text-white rounded-full font-semibold hover:bg-[#6A3D22] transition-all hover:scale-105 flex items-center gap-2"
          >
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            {{ t.portfolio.addPortfolio }}
          </button>
        </div>

        <div
          v-if="store.isLoading && !store.hasPortfolios"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
        >
          <div v-for="i in 3" :key="i" class="bg-white rounded-2xl shadow-md overflow-hidden animate-pulse">
            <div class="aspect-square bg-gray-200"></div>
            <div class="p-4 space-y-3">
              <div class="h-4 bg-gray-200 rounded w-3/4"></div>
              <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              <div class="flex justify-between">
                <div class="h-6 bg-gray-200 rounded w-20"></div>
                <div class="h-4 bg-gray-200 rounded w-16"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="!store.hasPortfolios" class="flex flex-col items-center justify-center py-20 space-y-6">
          <div class="w-24 h-24 bg-amber-100 rounded-full flex items-center justify-center">
            <svg class="w-12 h-12 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          </div>
          <div class="text-center space-y-2">
            <h3 class="text-2xl font-bold text-black">{{ t.portfolio.emptyState.title }}</h3>
            <p class="text-black/60">{{ t.portfolio.emptyState.description }}</p>
          </div>
          <button
            @click="handleAddPortfolio"
            class="px-8 py-3 bg-[#7C4728] text-white rounded-full font-semibold hover:bg-[#6A3D22] transition-all hover:scale-105"
          >
            {{ t.portfolio.emptyState.cta }}
          </button>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <PortfolioCard
            v-for="portfolio in store.portfolios"
            :key="portfolio.id"
            :portfolio="portfolio"
            @view="handleViewPortfolio(portfolio)"
            @edit="handleEditPortfolio(portfolio)"
            @delete="handleDeletePortfolio(portfolio)"
          />
        </div>
      </div>
    </div>

    <PortfolioModal
      :is-open="store.isModalOpen"
      :portfolio="store.currentPortfolio"
      :is-loading="store.isLoading"
      :error="store.error"
      @close="store.closeModal"
      @save="handleSavePortfolio"
    />

    <PortfolioLightbox :portfolio="viewingPortfolio" @close="viewingPortfolio = null" />

    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div v-if="showDeleteConfirm" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="cancelDelete"></div>
        <div class="relative bg-white rounded-2xl shadow-xl p-6 max-w-md w-full space-y-4">
          <h3 class="text-xl font-bold text-black">{{ t.portfolio.deleteConfirm.title }}</h3>
          <p class="text-black/60">{{ t.portfolio.deleteConfirm.message }}</p>
          <div class="flex justify-end gap-3">
            <button
              @click="cancelDelete"
              class="px-6 py-2 text-black/60 hover:text-black transition-colors font-semibold"
            >
              {{ t.portfolio.deleteConfirm.cancel }}
            </button>
            <button
              @click="confirmDelete"
              :disabled="store.isLoading"
              class="px-6 py-2 bg-red-600 text-white rounded-full font-semibold hover:bg-red-700 transition-all disabled:opacity-50"
            >
              {{ t.portfolio.deleteConfirm.confirm }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="transform opacity-0 translate-y-2"
      enter-to-class="transform opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="transform opacity-100 translate-y-0"
      leave-to-class="transform opacity-0 translate-y-2"
    >
      <div
        v-if="showToast"
        class="fixed bottom-6 right-6 bg-white rounded-2xl shadow-xl border border-gray-200 p-4 flex items-center gap-3 max-w-md z-50"
      >
        <div :class="['w-2 h-2 rounded-full', toastType === 'success' ? 'bg-green-500' : 'bg-red-500']"></div>
        <p class="text-sm font-medium text-black">{{ toastMessage }}</p>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePortfoliosStore } from '@/stores/portfolios'
import { useI18n } from '@/composables/useI18n'
import PortfolioCard from '@/components/architect/PortfolioCard.vue'
import PortfolioModal from '@/components/architect/PortfolioModal.vue'
import PortfolioLightbox from '@/components/architect/PortfolioLightbox.vue'

const store = usePortfoliosStore()
const { t, getT } = useI18n()

const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')
const showDeleteConfirm = ref(false)
const portfolioToDelete = ref(null)
const viewingPortfolio = ref(null)

const displayToast = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 5000)
}

const handleViewPortfolio = portfolio => {
  viewingPortfolio.value = portfolio
}

const handleAddPortfolio = () => {
  store.openModal(null)
}

const handleEditPortfolio = portfolio => {
  store.openModal(portfolio)
}

const handleDeletePortfolio = portfolio => {
  portfolioToDelete.value = portfolio
  showDeleteConfirm.value = true
}

const confirmDelete = async () => {
  if (!portfolioToDelete.value) return

  try {
    await store.deletePortfolio(portfolioToDelete.value.id)
    displayToast(getT('portfolio.toast.deleteSuccess'), 'success')
  } catch (error) {
    displayToast(getT('portfolio.toast.deleteError'), 'error')
  } finally {
    showDeleteConfirm.value = false
    portfolioToDelete.value = null
  }
}

const cancelDelete = () => {
  showDeleteConfirm.value = false
  portfolioToDelete.value = null
}

const handleSavePortfolio = async ({ action, portfolioId, data, formData, newImages, files, imageId }) => {
  try {
    if (action === 'create') {
      await store.createPortfolio(formData)
      displayToast(getT('portfolio.toast.createSuccess'), 'success')
    } else if (action === 'update') {
      await store.updatePortfolio(portfolioId, data)

      if (newImages && newImages.length > 0) {
        await store.addImages(portfolioId, newImages)
      }

      displayToast(getT('portfolio.toast.updateSuccess'), 'success')
    } else if (action === 'uploadImages') {
      await store.addImages(portfolioId, files)
      displayToast(getT('portfolio.toast.imageUploadSuccess'), 'success')
    } else if (action === 'deleteImage') {
      await store.deleteImage(imageId)
      displayToast(getT('portfolio.toast.imageDeleteSuccess'), 'success')

      await store.fetchPortfolios()
    }
  } catch (error) {
    if (action === 'create') {
      displayToast(getT('portfolio.toast.createError'), 'error')
    } else if (action === 'update') {
      displayToast(getT('portfolio.toast.updateError'), 'error')
    } else if (action === 'uploadImages') {
      displayToast(getT('portfolio.toast.imageUploadError'), 'error')
    } else if (action === 'deleteImage') {
      displayToast(getT('portfolio.toast.imageDeleteError'), 'error')
    }
  }
}

onMounted(async () => {
  try {
    await store.fetchPortfolios()
  } catch (error) {
    displayToast('Failed to load portfolios', 'error')
  }
})
</script>
