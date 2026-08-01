<template>
  <div>
    <p class="text-center text-xs leading-relaxed text-gray-500">
      {{ legalText.noticePrefix }}
      <button
        type="button"
        class="font-medium text-gray-700 underline underline-offset-2 hover:text-black"
        @click="openModal('ACCOUNT_TC')"
      >
        {{ legalText.termsLabel }}
      </button>
      {{ legalText.and }}
      <button
        type="button"
        class="font-medium text-gray-700 underline underline-offset-2 hover:text-black"
        @click="openModal('PRIVACY_POLICY')"
      >
        {{ legalText.privacyLabel }}
      </button>
      {{ legalText.noticeSuffix }}
    </p>

    <Teleport to="body">
      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="activeDocType" class="fixed inset-0 z-50 overflow-y-auto" @click.self="closeModal">
          <div class="flex min-h-screen items-center justify-center p-4">
            <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="closeModal"></div>

            <Transition
              enter-active-class="transition ease-out duration-200"
              enter-from-class="opacity-0 scale-95"
              enter-to-class="opacity-100 scale-100"
              leave-active-class="transition ease-in duration-150"
              leave-from-class="opacity-100 scale-100"
              leave-to-class="opacity-0 scale-95"
            >
              <div
                v-if="activeDocType"
                class="relative z-10 w-full max-w-2xl transform rounded-2xl bg-white shadow-2xl transition-all"
              >
                <div
                  class="sticky top-0 z-10 flex items-center justify-between rounded-t-2xl border-b border-gray-200 bg-white px-6 py-4"
                >
                  <h2 class="text-lg font-bold text-black">
                    {{ activeDocType === 'ACCOUNT_TC' ? legalText.termsLabel : legalText.privacyLabel }}
                  </h2>
                  <button
                    @click="closeModal"
                    class="rounded-lg p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
                  >
                    <X :size="20" />
                  </button>
                </div>

                <div class="max-h-[calc(100vh-200px)] overflow-y-auto px-6 py-6">
                  <div v-if="!activeDoc" class="flex items-center justify-center py-16">
                    <div class="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-black"></div>
                  </div>
                  <div v-else class="legal-doc-content" v-html="renderedActiveDoc"></div>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { X } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useLegalDocs } from '@/composables/useLegalDocs'

const { t, locale } = useI18n()

const legalText = computed(() => t.value.auth.signup.legal)

const { isReady, acceptances, activeDocType, activeDoc, renderedActiveDoc, openModal, closeModal } =
  useLegalDocs(locale)

defineExpose({ acceptances, isReady })
</script>

<style scoped>
.legal-doc-content {
  font-size: 0.875rem;
  line-height: 1.7;
  color: theme('colors.gray.700');
}
.legal-doc-content :deep(h1) {
  font-size: 1.375rem;
  font-weight: 700;
  color: theme('colors.black');
  margin: 0 0 1rem;
}
.legal-doc-content :deep(h2) {
  font-size: 1.125rem;
  font-weight: 700;
  color: theme('colors.black');
  margin: 1.75rem 0 0.75rem;
}
.legal-doc-content :deep(h3) {
  font-size: 1rem;
  font-weight: 600;
  color: theme('colors.black');
  margin: 1.25rem 0 0.5rem;
}
.legal-doc-content :deep(p) {
  margin: 0 0 0.875rem;
}
.legal-doc-content :deep(ul),
.legal-doc-content :deep(ol) {
  margin: 0 0 0.875rem;
  padding-left: 1.25rem;
}
.legal-doc-content :deep(li) {
  margin-bottom: 0.25rem;
}
.legal-doc-content :deep(blockquote) {
  border-left: 3px solid theme('colors.border-gray');
  padding-left: 1rem;
  margin: 0 0 0.875rem;
  color: theme('colors.gray.500');
  font-style: italic;
}
.legal-doc-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0 0 0.875rem;
}
.legal-doc-content :deep(th),
.legal-doc-content :deep(td) {
  border: 1px solid theme('colors.border-gray');
  padding: 0.5rem 0.75rem;
  text-align: left;
}
.legal-doc-content :deep(hr) {
  border: none;
  border-top: 1px solid theme('colors.border-gray');
  margin: 1.5rem 0;
}
.legal-doc-content :deep(strong) {
  color: theme('colors.black');
}
</style>
