import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { tokenPurchaseAPI } from '@/services/api'

export const useTokenPurchaseStore = defineStore('tokenPurchase', () => {
  const isModalOpen = ref(false)
  const loading = ref(false)
  const error = ref(null)
  const pricing = ref(null)
  const selectedQuantity = ref(1)
  const showCheckout = ref(false)
  const checkoutUrl = ref(null)
  const currentPurchaseId = ref(null)

  const calculatedTotal = computed(() => {
    if (!pricing.value) return 0
    return selectedQuantity.value * pricing.value.pricePerToken
  })

  const openModal = async () => {
    isModalOpen.value = true
    error.value = null
    await fetchPricing()
  }

  const closeModal = () => {
    isModalOpen.value = false
    loading.value = false
    error.value = null
    pricing.value = null
    selectedQuantity.value = 1
    resetCheckout()
  }

  const resetCheckout = () => {
    showCheckout.value = false
    checkoutUrl.value = null
    currentPurchaseId.value = null
  }

  const fetchPricing = async () => {
    try {
      loading.value = true
      error.value = null
      const response = await tokenPurchaseAPI.getPricing()
      pricing.value = response.data.data
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to fetch pricing'
      console.error('Failed to fetch pricing:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const setQuantity = qty => {
    const quantity = parseInt(qty)
    if (quantity >= 1 && quantity <= 50) {
      selectedQuantity.value = quantity
    }
  }

  const initiatePurchase = async () => {
    try {
      loading.value = true
      error.value = null

      const response = await tokenPurchaseAPI.initiatePurchase(selectedQuantity.value)
      const purchaseData = response.data.data

      if (purchaseData.paymentLink) {
        checkoutUrl.value = purchaseData.paymentLink
        currentPurchaseId.value = purchaseData.purchaseId
        showCheckout.value = true
        return purchaseData
      } else {
        throw new Error('No payment link received')
      }
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to initiate purchase'
      console.error('Failed to initiate purchase:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    isModalOpen,
    loading,
    error,
    pricing,
    selectedQuantity,
    calculatedTotal,
    showCheckout,
    checkoutUrl,
    currentPurchaseId,
    openModal,
    closeModal,
    fetchPricing,
    setQuantity,
    initiatePurchase,
    resetCheckout
  }
})
