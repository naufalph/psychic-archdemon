<template>
  <TransitionRoot :show="isOpen" as="template">
    <Dialog @close="$emit('close')" class="relative z-50">
      <TransitionChild
        as="template"
        enter="ease-out duration-300"
        enter-from="opacity-0"
        enter-to="opacity-100"
        leave="ease-in duration-200"
        leave-from="opacity-100"
        leave-to="opacity-0"
      >
        <div class="fixed inset-0 bg-black/70" />
      </TransitionChild>

      <div class="fixed inset-0 overflow-y-auto">
        <div class="flex min-h-full items-center justify-center p-4">
          <TransitionChild
            as="template"
            enter="ease-out duration-300"
            enter-from="opacity-0 scale-95"
            enter-to="opacity-100 scale-100"
            leave="ease-in duration-200"
            leave-from="opacity-100 scale-100"
            leave-to="opacity-0 scale-95"
          >
            <DialogPanel class="w-full max-w-5xl bg-white rounded-3xl shadow-2xl overflow-hidden">
              <div class="bg-brand-brown text-white p-6 flex justify-between items-center">
                <div>
                  <h2 class="text-2xl font-bold">{{ proposal.architectName }}'s Proposal</h2>
                  <p v-if="proposal.architectCompany" class="text-white/80">
                    {{ proposal.architectCompany }}
                  </p>
                </div>
                <button
                  @click="$emit('close')"
                  class="w-10 h-10 bg-white/20 hover:bg-white/30 rounded-full flex items-center justify-center transition"
                >
                  <X :size="24" />
                </button>
              </div>

              <div class="bg-gray-50 p-6" style="min-height: 600px">
                <div v-if="pdfUrl" class="bg-white rounded-2xl p-4 shadow-inner">
                  <iframe :src="pdfUrl" class="w-full rounded-lg" style="height: 700px" frameborder="0" />
                </div>
                <div v-else class="flex items-center justify-center h-96">
                  <p class="text-gray-500">No PDF attachment available</p>
                </div>

                <div v-if="pdfUrl" class="mt-4 flex justify-center">
                  <a
                    :href="pdfUrl"
                    :download="fileName"
                    class="px-6 py-3 bg-brand-brown text-white rounded-full font-medium hover:bg-black transition flex items-center gap-2"
                  >
                    <Download :size="18" />
                    Download PDF
                  </a>
                </div>
              </div>
            </DialogPanel>
          </TransitionChild>
        </div>
      </div>
    </Dialog>
  </TransitionRoot>
</template>

<script setup>
import { computed } from 'vue'
import { Dialog, DialogPanel, TransitionRoot, TransitionChild } from '@headlessui/vue'
import { X, Download } from 'lucide-vue-next'

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  proposal: {
    type: Object,
    required: true
  }
})

defineEmits(['close'])

const pdfUrl = computed(() => {
  if (props.proposal.attachments && props.proposal.attachments.length > 0) {
    return props.proposal.attachments[0].fileUrl
  }
  return null
})

const fileName = computed(() => {
  if (props.proposal.attachments && props.proposal.attachments.length > 0) {
    return props.proposal.attachments[0].fileName
  }
  return 'proposal.pdf'
})
</script>
