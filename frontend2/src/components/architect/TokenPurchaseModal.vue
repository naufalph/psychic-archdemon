<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="isModalOpen" class="fixed inset-0 z-50 overflow-y-auto" @click.self="handleClose">
        <div class="flex min-h-screen items-center justify-center p-4">
          <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="handleClose"></div>

          <Transition
            enter-active-class="transition ease-out duration-200"
            enter-from-class="opacity-0 scale-95"
            enter-to-class="opacity-100 scale-100"
            leave-active-class="transition ease-in duration-150"
            leave-from-class="opacity-100 scale-100"
            leave-to-class="opacity-0 scale-95"
          >
            <div
              v-if="isModalOpen"
              class="relative z-10 w-full max-w-md transform rounded-2xl bg-white shadow-2xl transition-all"
            >
              <!-- Header -->
              <div
                class="sticky top-0 z-10 flex items-center justify-between border-b border-gray-200 bg-white px-6 py-4 rounded-t-2xl"
              >
                <h2 class="text-xl font-bold text-gray-900">
                  {{ getT('tokenPurchase.modal.title') }}
                </h2>
                <button
                  @click="handleClose"
                  :disabled="loading"
                  class="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <X :size="20" />
                </button>
              </div>

              <!-- Content -->
              <div class="max-h-[calc(100vh-200px)] overflow-y-auto px-6 py-6">
                <!-- Quantity Selection View -->
                <div>
                  <!-- Current Quota Display -->
                  <div class="mb-6 rounded-xl bg-gradient-to-br from-brand-brown to-brand-brown-hover p-4 text-white">
                    <div class="text-sm opacity-90">
                      {{ getT('tokenPurchase.modal.currentQuota') }}
                    </div>
                    <div class="mt-1 text-3xl font-bold">
                      {{ quota?.tokensRemaining || 0 }}
                    </div>
                    <div class="mt-1 text-xs opacity-75">tokens</div>
                  </div>

                  <!-- Pricing Info -->
                  <div v-if="pricing" class="space-y-4">
                    <!-- Tier Info -->
                    <div class="flex items-center justify-between rounded-lg bg-gray-50 p-4">
                      <span class="text-sm text-gray-600">
                        {{ getT('tokenPurchase.modal.tier') }}
                      </span>
                      <span class="font-semibold text-gray-900">
                        {{ pricing.currentTier }}
                      </span>
                    </div>

                    <!-- Price Per Token -->
                    <div class="flex items-center justify-between rounded-lg bg-gray-50 p-4">
                      <span class="text-sm text-gray-600">
                        {{ getT('tokenPurchase.modal.pricePerToken') }}
                      </span>
                      <span class="font-semibold text-gray-900">
                        {{ formatCurrency(pricing.pricePerToken) }}
                      </span>
                    </div>

                    <!-- Quantity Selector -->
                    <div class="space-y-2">
                      <label class="block text-sm font-medium text-gray-700">
                        {{ getT('tokenPurchase.modal.quantity') }}
                      </label>
                      <div class="flex items-center gap-3">
                        <button
                          @click="decrementQuantity"
                          :disabled="selectedQuantity <= 1 || loading"
                          class="flex h-10 w-10 items-center justify-center rounded-lg border-2 border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                          <span class="text-xl font-semibold">−</span>
                        </button>
                        <input
                          v-model.number="selectedQuantity"
                          type="number"
                          min="1"
                          max="50"
                          :disabled="loading"
                          @input="handleQuantityInput"
                          class="flex-1 rounded-lg border-2 border-gray-300 px-4 py-2 text-center text-lg font-semibold focus:border-brand-brown focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
                        />
                        <button
                          @click="incrementQuantity"
                          :disabled="selectedQuantity >= 50 || loading"
                          class="flex h-10 w-10 items-center justify-center rounded-lg border-2 border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                          <span class="text-xl font-semibold">+</span>
                        </button>
                      </div>
                      <p class="text-xs text-gray-500">
                        {{ getT('tokenPurchase.modal.quantityRange') }}
                      </p>
                    </div>

                    <!-- Total Calculation -->
                    <div class="rounded-xl bg-gray-900 p-6 text-white">
                      <div class="text-sm opacity-75">
                        {{ getT('tokenPurchase.modal.total') }}
                      </div>
                      <div class="mt-2 text-3xl font-bold">
                        {{ formatCurrency(calculatedTotal) }}
                      </div>
                    </div>

                    <!-- Info Text -->
                    <div class="rounded-lg bg-blue-50 p-4 text-sm text-blue-800">
                      <p>{{ getT('tokenPurchase.modal.redirectInfo') }}</p>
                    </div>

                    <!-- Waiting for Payment Message -->
                    <Transition
                      enter-active-class="transition ease-out duration-200"
                      enter-from-class="opacity-0 scale-95"
                      enter-to-class="opacity-100 scale-100"
                    >
                      <div v-if="showWaitingMessage" class="rounded-lg bg-blue-50 border border-blue-200 p-4">
                        <div class="flex items-center gap-3">
                          <Loader class="animate-spin text-blue-600" :size="20" />
                          <div class="text-sm text-blue-800">
                            <p class="font-medium">Waiting for payment...</p>
                            <p class="mt-1">
                              Complete your payment in the opened Xendit tab. This window will automatically update when
                              payment is received.
                            </p>
                          </div>
                        </div>
                      </div>
                    </Transition>

                    <!-- Success Message -->
                    <Transition
                      enter-active-class="transition ease-out duration-200"
                      enter-from-class="opacity-0 scale-95"
                      enter-to-class="opacity-100 scale-100"
                    >
                      <div
                        v-if="successMessage"
                        class="flex items-center gap-3 rounded-lg bg-green-50 border border-green-200 p-4"
                      >
                        <CheckCircle class="text-green-500" :size="24" />
                        <p class="text-sm font-medium text-green-800">{{ successMessage }}</p>
                      </div>
                    </Transition>
                  </div>

                  <!-- Loading State -->
                  <div v-else-if="loading && !pricing" class="flex items-center justify-center py-12">
                    <div class="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-brand-brown"></div>
                  </div>

                  <!-- Error State -->
                  <div v-if="error" class="mt-4 rounded-lg bg-red-50 border border-red-200 p-4 text-sm text-red-800">
                    {{ error }}
                  </div>
                </div>
              </div>

              <!-- Footer -->
              <div
                class="sticky bottom-0 flex items-center justify-end gap-3 border-t border-gray-200 bg-white px-6 py-4 rounded-b-2xl"
              >
                <button
                  @click="handleClose"
                  :disabled="loading"
                  class="rounded-lg px-6 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {{ getT('tokenPurchase.modal.cancel') }}
                </button>
                <button
                  @click="handleConfirm"
                  :disabled="loading || !pricing"
                  class="relative rounded-lg bg-brand-brown px-6 py-2.5 text-sm font-medium text-white hover:bg-black transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <span v-if="!loading">{{ getT('tokenPurchase.modal.confirm') }}</span>
                  <span v-else class="flex items-center gap-2">
                    <div class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
                    Processing...
                  </span>
                </button>
              </div>

              <!-- Full Loading Overlay -->
              <div
                v-if="loading"
                class="absolute inset-0 flex items-center justify-center rounded-2xl bg-white/80 backdrop-blur-sm"
              >
                <div class="flex flex-col items-center gap-3">
                  <div class="h-12 w-12 animate-spin rounded-full border-4 border-gray-200 border-t-brand-brown"></div>
                  <p class="text-sm font-medium text-gray-600">Processing payment...</p>
                </div>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useTokenPurchaseStore } from '@/stores/tokenPurchase'
import { useBidsStore } from '@/stores/bids'
import { useI18n } from '@/composables/useI18n'
import { tokenPurchaseAPI } from '@/services/api'
import { X, Loader, CheckCircle } from 'lucide-vue-next'

const tokenStore = useTokenPurchaseStore()
const bidsStore = useBidsStore()
const { isModalOpen, loading, error, pricing, selectedQuantity, calculatedTotal } = storeToRefs(tokenStore)
const { quota } = storeToRefs(bidsStore)
const { getT } = useI18n()

const showWaitingMessage = ref(false)
const successMessage = ref(null)
const pollingIntervalId = ref(null)

const formatCurrency = amount => {
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    minimumFractionDigits: 0
  }).format(amount)
}

const handleClose = () => {
  if (!loading.value) {
    if (showWaitingMessage.value && !successMessage.value) {
      const confirmed = confirm(
        'Payment is in progress. Are you sure you want to close? Your payment may still be processing.'
      )
      if (!confirmed) return
    }

    if (pollingIntervalId.value) {
      clearInterval(pollingIntervalId.value)
      pollingIntervalId.value = null
    }

    tokenStore.closeModal()
    showWaitingMessage.value = false
    successMessage.value = null
  }
}

const handleConfirm = async () => {
  try {
    const purchaseData = await tokenStore.initiatePurchase()

    if (purchaseData && purchaseData.paymentLink) {
      window.open(purchaseData.paymentLink, '_blank', 'noopener,noreferrer')

      startPollingPaymentStatus(purchaseData.purchaseId)

      showWaitingMessage.value = true
    }
  } catch (err) {
    // Error already stored in tokenStore.error
  }
}

const startPollingPaymentStatus = purchaseId => {
  const maxAttempts = 60
  let attempts = 0

  const intervalId = setInterval(async () => {
    attempts++

    if (attempts >= maxAttempts) {
      clearInterval(intervalId)
      showWaitingMessage.value = false
      tokenStore.error = 'Payment verification timeout. Please check your purchase history.'
      return
    }

    try {
      const response = await tokenPurchaseAPI.getPurchaseById(purchaseId)
      const status = response.data.data.status

      if (status === 'COMPLETED') {
        clearInterval(intervalId)
        await handlePaymentSuccess()
      } else if (status === 'FAILED' || status === 'EXPIRED') {
        clearInterval(intervalId)
        showWaitingMessage.value = false
        tokenStore.error = `Payment ${status.toLowerCase()}. Please try again.`
      }
    } catch (err) {
      console.error('Error polling payment status:', err)
    }
  }, 5000)

  pollingIntervalId.value = intervalId
}

const handlePaymentSuccess = async () => {
  await bidsStore.fetchQuota()

  showWaitingMessage.value = false
  successMessage.value = `Successfully purchased ${selectedQuantity.value} token(s)!`

  setTimeout(() => {
    tokenStore.closeModal()
    tokenStore.resetCheckout()
    successMessage.value = null
  }, 2000)
}

onUnmounted(() => {
  if (pollingIntervalId.value) {
    clearInterval(pollingIntervalId.value)
  }
})

const incrementQuantity = () => {
  if (selectedQuantity.value < 50) {
    tokenStore.setQuantity(selectedQuantity.value + 1)
  }
}

const decrementQuantity = () => {
  if (selectedQuantity.value > 1) {
    tokenStore.setQuantity(selectedQuantity.value - 1)
  }
}

const handleQuantityInput = event => {
  const value = parseInt(event.target.value)
  if (!isNaN(value)) {
    tokenStore.setQuantity(value)
  }
}
</script>
