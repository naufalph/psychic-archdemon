<template>
  <div class="min-h-screen bg-[#F4F5F7] py-12">
    <div class="max-w-4xl mx-auto px-6">
      <button
        @click="router.push({ name: 'OpportunityList' })"
        class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition"
      >
        <ArrowLeft :size="20" />
        Back to Opportunities
      </button>

      <div class="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden">
        <div class="bg-[#7C4728] p-8 text-white">
          <h1 class="text-3xl font-bold flex items-center gap-3">
            <FileText :size="32" />
            Submit Proposal
          </h1>
          <p class="text-white/80 mt-2">Showcase your expertise and win the project</p>
        </div>

        <form @submit.prevent="handleSubmit" class="p-8 space-y-8">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2"
                >Bid Amount (IDR)<span class="text-red-500">*</span></label
              >
              <input
                v-model.number="formData.bidAmount"
                required
                type="number"
                placeholder="e.g., 50000000"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Proposed Timeline (days)</label>
              <input
                v-model.number="formData.proposedTimelineDays"
                type="number"
                placeholder="e.g., 60"
                class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Proposal</label>
            <textarea
              v-model="formData.proposal"
              rows="6"
              placeholder="Describe your design concept, approach, and key features..."
              class="w-full px-4 py-3 border-2 border-gray-200 rounded-2xl focus:ring-2 focus:ring-[#7C4728] focus:border-[#7C4728] outline-none"
            />
          </div>

          <div>
            <ImageUploader v-model="coverImage" label="Cover Image" required />
          </div>

          <div>
            <MultiImageUploader v-model="conceptSketches" label="Concept Sketches" :max-files="5" />
          </div>

          <div>
            <MultiImageUploader v-model="moodBoards" label="Mood Boards & Inspiration" :max-files="5" />
          </div>

          <div v-if="uploadProgress > 0" class="bg-gray-50 rounded-2xl p-6">
            <UploadProgress :progress="uploadProgress" label="Uploading files..." />
          </div>

          <div v-if="error" class="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
            {{ error }}
          </div>

          <div class="flex gap-4 pt-6 border-t border-gray-100">
            <button
              type="button"
              @click="router.push({ name: 'OpportunityList' })"
              class="px-6 py-3 text-gray-700 bg-white border-2 border-gray-300 rounded-full hover:bg-gray-50 transition font-medium"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="loading || uploadProgress > 0"
              class="flex-1 px-6 py-3 text-white bg-[#7C4728] rounded-full hover:bg-black shadow-md hover:shadow-lg transition-all font-bold flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Loader v-if="loading" :size="20" class="animate-spin" />
              <Send v-else :size="20" />
              {{ loading ? 'Submitting...' : 'Submit Proposal' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ArrowLeft, FileText, Loader, Send } from 'lucide-vue-next'
import { useBidsStore } from '@/stores/bids'
import ImageUploader from '@/components/upload/ImageUploader.vue'
import MultiImageUploader from '@/components/upload/MultiImageUploader.vue'
import UploadProgress from '@/components/upload/UploadProgress.vue'

const route = useRouter()
const router = useRouter()
const bidsStore = useBidsStore()

const { loading, uploadProgress } = storeToRefs(bidsStore)

const formData = ref({
  bidAmount: null,
  proposedTimelineDays: null,
  proposal: ''
})

const coverImage = ref(null)
const conceptSketches = ref([])
const moodBoards = ref([])
const error = ref(null)

const handleSubmit = async () => {
  error.value = null

  try {
    const bidData = {
      projectId: route.params.projectId,
      bidAmount: formData.value.bidAmount,
      proposedTimelineDays: formData.value.proposedTimelineDays,
      proposal: formData.value.proposal
    }

    const bid = await bidsStore.createDraftBid(bidData)

    if (conceptSketches.value.length > 0) {
      await bidsStore.uploadConceptSketches(bid.id, conceptSketches.value)
    }

    if (moodBoards.value.length > 0) {
      await bidsStore.uploadMoodBoards(bid.id, moodBoards.value)
    }

    await bidsStore.submitBid(bid.id)

    router.push({ name: 'MyBids' })
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to submit proposal. Please try again.'
    console.error('Failed to submit proposal:', err)
  }
}
</script>
