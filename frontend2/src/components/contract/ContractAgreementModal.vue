<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm px-4 py-6"
        @click.self="close"
      >
        <div class="bg-white rounded-2xl w-full max-w-3xl max-h-full shadow-2xl overflow-hidden flex flex-col">
          <!-- Header -->
          <div class="bg-brand-brown px-6 py-5 shrink-0">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center shrink-0">
                <FileText :size="20" class="text-white" aria-hidden="true" />
              </div>
              <div class="min-w-0">
                <p class="text-xs text-brand-tan font-semibold uppercase tracking-wide">
                  {{ readOnly || alreadySigned ? c.eyebrowSigned : c.eyebrow }}
                </p>
                <h3 class="text-lg font-bold text-white truncate">{{ c.title }}</h3>
              </div>
              <button
                type="button"
                class="ml-auto text-white/70 hover:text-white transition shrink-0"
                :aria-label="c.closeLabel"
                @click="close"
              >
                <X :size="20" />
              </button>
            </div>
          </div>

          <!-- Document body -->
          <div v-if="loading" class="flex-1 flex items-center justify-center py-16 text-gray-400 text-sm">
            <div class="w-8 h-8 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
          </div>
          <div v-else-if="loadError" class="flex-1 flex items-center justify-center py-16 px-6">
            <p class="text-sm text-red-600 text-center">{{ loadError }}</p>
          </div>
          <template v-else>
            <div
              ref="bodyPane"
              class="flex-1 overflow-y-auto px-6 py-5 min-h-0 contract-doc"
              @scroll="onScroll"
              v-html="renderedBody"
            />

            <!-- Signature footer -->
            <div class="border-t border-gray-100 px-6 py-5 shrink-0 space-y-4">
              <!-- Existing signatures -->
              <div class="grid grid-cols-2 gap-3">
                <div
                  v-for="block in signatureBlocks"
                  :key="block.party"
                  class="rounded-xl border border-gray-100 bg-brand-cream/40 px-3 py-2"
                >
                  <p class="text-[10px] font-bold uppercase tracking-wide text-gray-400">
                    {{ block.label }}
                  </p>
                  <template v-if="block.acceptance">
                    <p class="signature-preview text-base text-black leading-tight truncate">
                      {{ block.acceptance.signatureName }}
                    </p>
                    <p class="text-[10px] text-gray-400">
                      {{ formatSignedAt(block.acceptance.acceptedAt) }}
                    </p>
                  </template>
                  <p v-else class="text-xs text-gray-400 italic mt-1">{{ c.notSigned }}</p>
                </div>
              </div>

              <template v-if="!readOnly && !alreadySigned">
                <div v-if="!scrolledToEnd" class="flex items-center gap-2 text-xs text-gray-400">
                  <ArrowDown :size="14" />
                  {{ c.scrollHint }}
                </div>

                <div v-else class="space-y-2">
                  <label class="block text-xs font-bold uppercase tracking-wide text-gray-500">
                    {{ c.signatureLabel }}
                  </label>
                  <input
                    v-model="signatureName"
                    type="text"
                    :placeholder="expectedName || c.signaturePlaceholder"
                    class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-gold/40 focus:border-brand-gold transition"
                    autocomplete="off"
                    spellcheck="false"
                    @keyup.enter="submit"
                  />
                  <p class="text-xs" :class="signatureTouched && !signatureMatches ? 'text-red-600' : 'text-gray-400'">
                    {{
                      signatureTouched && !signatureMatches
                        ? c.nameMismatch?.replace('{name}', expectedName || '')
                        : c.signatureHint?.replace('{name}', expectedName || '')
                    }}
                  </p>
                  <p
                    v-if="signatureName.trim()"
                    class="signature-preview border-b border-gray-300 pb-1 text-xl text-black"
                  >
                    {{ signatureName }}
                  </p>
                </div>

                <p v-if="submitError" class="text-sm text-red-600">{{ submitError }}</p>

                <div class="flex gap-3">
                  <button
                    type="button"
                    :disabled="submitting"
                    class="flex-1 px-4 py-2.5 border border-gray-200 text-gray-600 text-sm font-semibold rounded-lg hover:bg-gray-50 disabled:opacity-50 transition"
                    @click="close"
                  >
                    {{ c.cancel }}
                  </button>
                  <button
                    type="button"
                    :disabled="!canSubmit"
                    class="flex-1 px-4 py-2.5 bg-brand-brown text-white text-sm font-bold rounded-lg hover:bg-brand-brown-dark disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center justify-center gap-2"
                    @click="submit"
                  >
                    <span v-if="submitting">{{ c.agreeing }}</span>
                    <template v-else>
                      <CheckCircle :size="15" aria-hidden="true" />
                      {{ c.agreeButton }}
                    </template>
                  </button>
                </div>
              </template>

              <div v-else class="flex justify-end">
                <button
                  type="button"
                  class="px-5 py-2.5 border border-gray-200 text-gray-600 text-sm font-semibold rounded-lg hover:bg-gray-50 transition"
                  @click="close"
                >
                  {{ c.closeLabel }}
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { FileText, CheckCircle, X, ArrowDown } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { contractAPI } from '@/services/api'

const props = defineProps({
  open: { type: Boolean, default: false },
  projectId: { type: [String, Number], required: true },
  readOnly: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'accepted'])

const { t, locale } = useI18n()
const c = computed(() => t.value.contractAgreement || {})

const doc = ref(null)
const loading = ref(false)
const loadError = ref(null)
const signatureName = ref('')
const signatureTouched = ref(false)
const submitting = ref(false)
const submitError = ref(null)
const scrolledToEnd = ref(false)
const bodyPane = ref(null)

/**
 * A signature is the signer's own name, so it is stored exactly as typed -- no auto-casing.
 * Matching ignores case, spacing, accents and punctuation, mirroring the backend check.
 */
const normalise = value =>
  (value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9 ]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

const expectedName = computed(() => {
  if (!doc.value) return ''
  return doc.value.myParty === 'CLIENT' ? doc.value.terms?.clientName || '' : doc.value.terms?.architectName || ''
})

const body = computed(() => {
  if (!doc.value) return ''
  return locale.value === 'id' ? doc.value.bodyId : doc.value.bodyEn
})

const renderedBody = computed(() => (body.value ? DOMPurify.sanitize(marked.parse(body.value)) : ''))

const acceptanceFor = party => (doc.value?.acceptances || []).find(a => a.party === party) || null

const alreadySigned = computed(() => !!acceptanceFor(doc.value?.myParty))

const signatureBlocks = computed(() => [
  { party: 'CLIENT', label: c.value.clientSignature, acceptance: acceptanceFor('CLIENT') },
  { party: 'ARCHITECT', label: c.value.architectSignature, acceptance: acceptanceFor('ARCHITECT') }
])

const signatureMatches = computed(() => {
  const given = normalise(signatureName.value)
  if (given.length < 3) return false
  const target = normalise(expectedName.value)
  return target ? given === target : true
})

const canSubmit = computed(() => !submitting.value && scrolledToEnd.value && signatureMatches.value)

const formatSignedAt = value =>
  value
    ? new Date(value).toLocaleDateString(locale.value === 'id' ? 'id-ID' : 'en-GB', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    : ''

const onScroll = e => {
  const el = e.target
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 8) scrolledToEnd.value = true
}

const load = async () => {
  loading.value = true
  loadError.value = null
  try {
    const res = await contractAPI.getDocument(props.projectId)
    doc.value = res.data.data || res.data || null
    await nextTick()
    // A contract short enough not to scroll has already been read in full.
    const el = bodyPane.value
    if (el && el.scrollHeight <= el.clientHeight + 8) scrolledToEnd.value = true
  } catch (err) {
    loadError.value = err.response?.data?.message || c.value.loadError
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!canSubmit.value) {
    signatureTouched.value = true
    return
  }
  submitting.value = true
  submitError.value = null
  try {
    const res = await contractAPI.accept(props.projectId, {
      contentHash: doc.value.contentHash,
      signatureName: signatureName.value.trim(),
      lang: locale.value === 'id' ? 'id' : 'en'
    })
    emit('accepted', res.data.data || res.data || null)
    emit('close')
  } catch (err) {
    submitError.value = err.response?.data?.message || c.value.acceptError
  } finally {
    submitting.value = false
  }
}

const close = () => {
  if (submitting.value) return
  emit('close')
}

watch(signatureName, () => {
  signatureTouched.value = true
})

watch(
  () => props.open,
  isOpen => {
    if (!isOpen) return
    doc.value = null
    signatureName.value = ''
    signatureTouched.value = false
    submitError.value = null
    scrolledToEnd.value = false
    load()
  },
  { immediate: true }
)
</script>

<style scoped>
.signature-preview {
  font-family: 'Brush Script MT', 'Segoe Script', 'Snell Roundhand', cursive;
}
.contract-doc :deep(h1) {
  font-size: 1.25rem;
  font-weight: 700;
  color: theme('colors.black');
  margin: 0 0 1rem;
}
.contract-doc :deep(h2) {
  font-size: 1rem;
  font-weight: 700;
  color: theme('colors.black');
  margin: 1.5rem 0 0.5rem;
}
.contract-doc :deep(h3) {
  font-size: 0.875rem;
  font-weight: 600;
  color: theme('colors.black');
  margin: 1.25rem 0 0.5rem;
}
.contract-doc :deep(p),
.contract-doc :deep(li) {
  font-size: 0.8125rem;
  line-height: 1.65;
  color: theme('colors.gray.700');
}
.contract-doc :deep(p) {
  margin: 0 0 0.875rem;
}
.contract-doc :deep(ul),
.contract-doc :deep(ol) {
  margin: 0 0 0.875rem;
  padding-left: 1.25rem;
  list-style: disc;
}
.contract-doc :deep(li) {
  margin-bottom: 0.25rem;
}
.contract-doc :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0 0 1rem;
  font-size: 0.75rem;
}
.contract-doc :deep(th),
.contract-doc :deep(td) {
  border: 1px solid theme('colors.border-gray');
  padding: 0.375rem 0.5rem;
  text-align: left;
}
.contract-doc :deep(th) {
  background: theme('colors.brand-cream');
  font-weight: 700;
}
.contract-doc :deep(strong) {
  color: theme('colors.black');
}
.contract-doc :deep(em) {
  color: theme('colors.gray.400');
}
</style>
