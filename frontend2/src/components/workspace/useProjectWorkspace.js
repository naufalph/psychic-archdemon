import { ref, reactive, computed, nextTick } from 'vue'
import { phaseAPI, projectAPI, bidAPI, chatAPI } from '@/services/api'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import { useI18n } from '@/composables/useI18n'

const SCROLL_OFFSET = 90

/**
 * Everything both workspace views need. The two role views differ only in how the project and
 * the counterparty are loaded, and in which actions they may take -- not in the derived state,
 * which is why this lives in one place rather than being copied between them.
 */
export function useProjectWorkspace(projectId, role) {
  const projectsStore = useProjectsStore()
  const bidsStore = useBidsStore()
  const { t, locale } = useI18n()
  const isClient = role === 'client'

  const tab = ref('summary')
  const chatOpen = ref(true)
  const phases = ref([])
  const contract = ref(null)
  const loading = ref(true)
  const error = ref(null)
  const openPhases = reactive({})
  const openLogs = reactive({})
  const phaseLogs = reactive({})
  const logsLoading = reactive({})
  const actionLoading = ref(null)
  const uploadLoading = ref(null)
  const toast = ref('')
  const architectConversationId = ref(null)
  const myBid = ref(null)

  let toastTimer = null
  const showToast = message => {
    toast.value = message
    clearTimeout(toastTimer)
    toastTimer = setTimeout(() => (toast.value = ''), 2600)
  }

  const project = computed(() => projectsStore.currentProject)
  const acceptedBid = computed(() =>
    isClient ? bidsStore.projectBids.find(b => b.status === 'ACCEPTED') || null : myBid.value
  )
  const conversationId = computed(() =>
    isClient ? (acceptedBid.value?.conversationId ?? null) : architectConversationId.value
  )
  const architectName = computed(
    () => acceptedBid.value?.architectName || t.value.projectWorkspace?.architectFallback
  )
  const architectInitials = computed(() =>
    (architectName.value || '?')
      .split(' ')
      .map(w => w[0])
      .join('')
      .slice(0, 2)
      .toUpperCase()
  )
  const coverImage = computed(
    () => project.value?.files?.find(f => f.fileType?.startsWith('image/'))?.filePath || null
  )

  const sortedPhases = computed(() => [...phases.value].sort((a, b) => a.phaseNumber - b.phaseNumber))
  const disbursedCount = computed(() => phases.value.filter(p => p.status === 'DISBURSED').length)
  const totalAmount = computed(() => phases.value.reduce((sum, p) => sum + Number(p.amount || 0), 0))
  const paidAmount = computed(() =>
    phases.value
      .filter(p => ['APPROVED', 'DISBURSED'].includes(p.status))
      .reduce((sum, p) => sum + Number(p.amount || 0), 0)
  )
  const remainingAmount = computed(() => totalAmount.value - paidAmount.value)
  const progressPercent = computed(() =>
    totalAmount.value > 0 ? (paidAmount.value / totalAmount.value) * 100 : 0
  )

  /**
   * NOT_STARTED is derived, never stored: a PENDING phase is not started while any earlier
   * phase has yet to be approved.
   */
  const isNotStarted = (phase, index) =>
    phase.status === 'PENDING' &&
    sortedPhases.value.slice(0, index).some(p => !['APPROVED', 'DISBURSED'].includes(p.status))

  const statusKey = (phase, index) => (isNotStarted(phase, index) ? 'NOT_STARTED' : phase.status)

  const revisionsLeft = phase =>
    phase.maxRevisions != null ? phase.maxRevisions - (phase.revisionsUsed || 0) : Infinity
  const showRevisionBadge = phase =>
    ['IN_PROGRESS', 'DELIVERED'].includes(phase.status) && phase.maxRevisions != null

  const daysLeft = phase => {
    if (!phase.dueDate) return null
    const due = new Date(phase.dueDate)
    const today = new Date()
    due.setHours(0, 0, 0, 0)
    today.setHours(0, 0, 0, 0)
    return Math.round((due - today) / 86400000)
  }

  const deadlineLabel = phase => {
    const w = t.value.projectWorkspace || {}
    const n = daysLeft(phase)
    if (n === null) return w.closedLabel || 'Closed'
    if (n < 0) return (w.overdueDays || '{d} days overdue').replace('{d}', Math.abs(n))
    if (n === 0) return w.dueToday || 'Due today!'
    if (n === 1) return w.oneDayLeft || '1 day left'
    return (w.daysLeftCount || '{d} days left').replace('{d}', n)
  }

  /** Rows for the Summary tab's "needs your action" card. Derived, never hand-authored. */
  const needsAction = computed(() => {
    const w = t.value.projectWorkspace || {}
    const rows = []
    sortedPhases.value.forEach((phase, index) => {
      if (isNotStarted(phase, index)) return
      const push = (title, cta) => rows.push({ phase, title, cta })
      if (isClient) {
        if (phase.status === 'DELIVERED') push(w.workSubmittedTitle, w.approveBtn)
        else if (phase.status === 'PENDING') push(w.paymentRequiredTitle, w.createInvoice)
        else if (phase.status === 'BILLED') push(w.invoiceSentTitle, w.payNow)
      } else {
        if (phase.status === 'IN_PROGRESS') push(w.workPhaseActiveTitle, w.uploadBtn)
        else if (phase.status === 'APPROVED') push(w.workApprovedExclaim, w.requestPayout)
      }
    })
    return rows
  })

  const deliverableItems = phase => phase.deliverableItems || []
  const approvedCount = phase => deliverableItems(phase).filter(d => d.status === 'APPROVED').length

  /** Files of one deliverable, grouped by revision round for the files modal. */
  const filesByRound = item => {
    const map = {}
    for (const file of item?.files || []) {
      const round = file.revisionRound ?? 0
      ;(map[round] ||= []).push(file)
    }
    return Object.keys(map)
      .map(round => ({ round: Number(round), files: map[round] }))
      .sort((a, b) => a.round - b.round)
  }

  const formatAmount = amount =>
    new Intl.NumberFormat(locale.value === 'id' ? 'id-ID' : 'en-US', {
      style: 'currency',
      currency: 'IDR',
      maximumFractionDigits: 0
    }).format(Number(amount || 0))

  const formatDate = value =>
    value
      ? new Date(value).toLocaleDateString(locale.value === 'id' ? 'id-ID' : 'en-US', {
          day: 'numeric',
          month: 'short',
          year: 'numeric'
        })
      : '-'

  const formatDateTime = value =>
    value
      ? new Date(value).toLocaleString(locale.value === 'id' ? 'id-ID' : 'en-US', {
          day: 'numeric',
          month: 'short',
          hour: '2-digit',
          minute: '2-digit'
        })
      : '-'

  const formatLogAction = action => {
    const map = t.value.projectWorkspace?.logActions || {}
    if (map[action]) return map[action]
    return String(action || '')
      .toLowerCase()
      .split('_')
      .map(w => w.charAt(0).toUpperCase() + w.slice(1))
      .join(' ')
  }

  const phaseFallbackTitle = phase =>
    phase.title ||
    (t.value.projectWorkspace?.phaseFallback || 'Phase {n}').replace('{n}', phase.phaseNumber)

  const fetchLogs = async phaseId => {
    if (phaseLogs[phaseId] !== undefined) return
    logsLoading[phaseId] = true
    try {
      const res = await phaseAPI.getLogs(phaseId)
      phaseLogs[phaseId] = res.data.data || res.data || []
    } catch {
      phaseLogs[phaseId] = []
    } finally {
      logsLoading[phaseId] = false
    }
  }

  const refreshPhases = async () => {
    const res = await phaseAPI.getPhases(projectId)
    phases.value = res.data.data || res.data || []
    Object.keys(phaseLogs).forEach(k => delete phaseLogs[k])
  }

  const refreshContract = async () => {
    try {
      const res = await phaseAPI.getContract(projectId)
      contract.value = res.data.data || res.data || null
    } catch {
      contract.value = null
    }
  }

  const loadAll = async () => {
    loading.value = true
    error.value = null
    try {
      if (isClient) {
        await Promise.all([
          projectsStore.fetchProjectById(projectId),
          bidsStore.fetchProjectBids(projectId)
        ])
        phases.value = (await phaseAPI.getPhases(projectId)).data.data || []
      } else {
        await projectsStore.fetchProjectForArchitect(projectId)
        const [phasesRes, convsRes, bidsRes] = await Promise.all([
          phaseAPI.getPhases(projectId),
          chatAPI.getMyConversations(),
          bidAPI.getMyBids().catch(() => ({ data: { data: [] } }))
        ])
        phases.value = phasesRes.data.data || phasesRes.data || []
        const conv = (convsRes.data.data || convsRes.data || []).find(
          c => String(c.projectId) === String(projectId)
        )
        if (conv) architectConversationId.value = conv.id
        const bids = bidsRes.data.data || bidsRes.data || []
        myBid.value =
          bids.find(b => String(b.projectId) === String(projectId) && b.status === 'ACCEPTED') || null
      }

      await refreshContract()

      const active = sortedPhases.value.find(p => p.status !== 'DISBURSED')
      if (active) {
        openPhases[active.id] = true
        fetchLogs(active.id)
      }
    } catch (err) {
      error.value = err.response?.data?.message || t.value.projectWorkspace?.loadError
    } finally {
      loading.value = false
    }
  }

  const togglePhase = phase => {
    openPhases[phase.id] = !openPhases[phase.id]
    if (openPhases[phase.id]) fetchLogs(phase.id)
  }

  const scrollTo = elementId => {
    const el = document.getElementById(elementId)
    if (!el) return
    const top = el.getBoundingClientRect().top + window.scrollY - SCROLL_OFFSET
    window.scrollTo({ top, behavior: 'smooth' })
  }

  /** Summary rows are navigation: switch tab, force the phase open, then scroll to it. */
  const goToPhase = async phaseId => {
    tab.value = 'phases'
    openPhases[phaseId] = true
    fetchLogs(phaseId)
    await nextTick()
    requestAnimationFrame(() => scrollTo(`phase-${phaseId}`))
  }

  const goToContract = async () => {
    tab.value = 'contract'
    await nextTick()
    requestAnimationFrame(() => scrollTo('payment-schedule'))
  }

  const afterAction = async phaseId => {
    await refreshPhases()
    await refreshContract()
    delete phaseLogs[phaseId]
    fetchLogs(phaseId)
  }

  const run = async (phaseId, fn, errorKey) => {
    actionLoading.value = phaseId
    try {
      const result = await fn()
      await afterAction(phaseId)
      return result
    } catch (err) {
      showToast(err.response?.data?.message || t.value.projectWorkspace?.[errorKey] || 'Error')
      return null
    } finally {
      actionLoading.value = null
    }
  }

  return {
    t,
    locale,
    isClient,
    tab,
    chatOpen,
    phases,
    sortedPhases,
    contract,
    loading,
    error,
    openPhases,
    openLogs,
    phaseLogs,
    logsLoading,
    actionLoading,
    uploadLoading,
    toast,
    showToast,
    project,
    acceptedBid,
    conversationId,
    architectName,
    architectInitials,
    coverImage,
    disbursedCount,
    totalAmount,
    paidAmount,
    remainingAmount,
    progressPercent,
    isNotStarted,
    statusKey,
    revisionsLeft,
    showRevisionBadge,
    deadlineLabel,
    needsAction,
    deliverableItems,
    approvedCount,
    filesByRound,
    formatAmount,
    formatDate,
    formatDateTime,
    formatLogAction,
    phaseFallbackTitle,
    fetchLogs,
    refreshPhases,
    refreshContract,
    loadAll,
    togglePhase,
    goToPhase,
    goToContract,
    run,
    projectAPI
  }
}
