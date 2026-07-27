import { ref, computed, onMounted, watch } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { useLegalStore } from '@/stores/legal'

export function useLegalDocs(locale) {
  const legalStore = useLegalStore()

  const activeDocType = ref(null)

  const isReady = computed(
    () =>
      !!legalStore.docs[`ACCOUNT_TC:${locale.value}`] &&
      !!legalStore.docs[`PRIVACY_POLICY:${locale.value}`]
  )

  const activeDoc = computed(() =>
    activeDocType.value ? legalStore.docs[`${activeDocType.value}:${locale.value}`] : null
  )

  const renderedActiveDoc = computed(() =>
    activeDoc.value ? DOMPurify.sanitize(marked.parse(activeDoc.value.contentMd)) : ''
  )

  const acceptances = computed(() => {
    const accountTc = legalStore.docs[`ACCOUNT_TC:${locale.value}`]
    const privacyPolicy = legalStore.docs[`PRIVACY_POLICY:${locale.value}`]
    if (!accountTc || !privacyPolicy) return []

    return [accountTc, privacyPolicy].map(doc => ({
      docType: doc.docType,
      version: doc.version,
      contentHash: doc.contentHash,
      lang: doc.lang
    }))
  })

  const fetchDocs = async () => {
    await Promise.all([
      legalStore.fetchCurrent('ACCOUNT_TC', locale.value),
      legalStore.fetchCurrent('PRIVACY_POLICY', locale.value)
    ])
  }

  onMounted(fetchDocs)
  watch(locale, fetchDocs)

  const openModal = async docType => {
    activeDocType.value = docType
    if (!legalStore.docs[`${docType}:${locale.value}`]) {
      await legalStore.fetchCurrent(docType, locale.value)
    }
  }

  const closeModal = () => {
    activeDocType.value = null
  }

  return { isReady, acceptances, activeDocType, activeDoc, renderedActiveDoc, openModal, closeModal }
}
