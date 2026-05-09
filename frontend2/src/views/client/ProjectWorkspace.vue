<template>
  <div class="min-h-screen bg-[#F4F5F7]">
    <div v-if="loading" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <div class="w-10 h-10 border-2 border-[#C5A17A] border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p class="text-gray-500">Loading workspace...</p>
      </div>
    </div>

    <div v-else-if="error" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button @click="loadAll" class="text-[#7C4728] hover:underline">Try again</button>
      </div>
    </div>

    <div v-else>
      <!-- Header -->
      <div class="bg-white border-b border-gray-200 px-6 py-4 sticky top-0 z-10">
        <div class="max-w-7xl mx-auto flex items-center justify-between">
          <div class="flex items-center gap-4">
            <button @click="router.back()" class="text-gray-500 hover:text-black transition">
              <ArrowLeft :size="20" />
            </button>
            <div>
              <p class="text-xs text-gray-400 uppercase font-bold tracking-wide">Project Workspace</p>
              <h1 class="text-lg font-bold text-black">{{ project?.title || 'Active Project' }}</h1>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span class="text-sm text-gray-500 flex items-center gap-1.5">
              <Layers :size="14" />
              {{ disbursedCount }} / {{ phases.length }} disbursed
            </span>
            <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700">
              <span class="w-1.5 h-1.5 rounded-full bg-blue-500" />
              In Progress
            </span>
          </div>
        </div>
      </div>

      <!-- Body -->
      <div class="max-w-7xl mx-auto px-6 py-6 grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">

        <!-- LEFT: overview + phases -->
        <div class="lg:col-span-2 space-y-4">

          <!-- Project overview card -->
          <div class="bg-white rounded-xl border border-gray-200 p-5">
            <div class="flex items-start gap-4">
              <div class="w-20 h-20 rounded-lg bg-gray-100 overflow-hidden shrink-0">
                <img v-if="coverImage" :src="coverImage" :alt="project?.title" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center">
                  <Building2 :size="28" class="text-gray-300" />
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-2 mb-3">
                  <div>
                    <h2 class="font-bold text-gray-900">{{ project?.title }}</h2>
                    <div class="flex flex-wrap items-center gap-3 mt-1 text-sm text-gray-500">
                      <span v-if="project?.location" class="flex items-center gap-1">
                        <MapPin :size="13" /> {{ project.location }}
                      </span>
                      <span v-if="project?.projectCategory" class="flex items-center gap-1">
                        <Tag :size="13" /> {{ project.projectCategory }}
                      </span>
                    </div>
                  </div>
                  <div class="text-right shrink-0">
                    <p class="text-xs text-gray-400">Total Value</p>
                    <p class="font-bold text-gray-900">{{ formatAmount(totalAmount) }}</p>
                  </div>
                </div>
                <div class="space-y-1">
                  <div class="flex justify-between text-xs text-gray-500">
                    <span>{{ formatAmount(paidAmount) }} paid</span>
                    <span>{{ Math.round(progressPercent) }}%</span>
                  </div>
                  <div class="w-full bg-gray-100 rounded-full h-1.5">
                    <div class="bg-green-500 h-1.5 rounded-full transition-all duration-500" :style="{ width: progressPercent + '%' }" />
                  </div>
                  <div class="flex justify-between text-xs text-gray-400">
                    <span>{{ disbursedCount }} of {{ phases.length }} phases complete</span>
                    <span>{{ formatAmount(remainingAmount) }} remaining</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="project?.scopeOfWork" class="mt-4 pt-4 border-t border-gray-100">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-1">Scope of Work</p>
              <p class="text-sm text-gray-600 leading-relaxed line-clamp-3">{{ project.scopeOfWork }}</p>
            </div>
          </div>

          <!-- Phase list -->
          <div class="space-y-3">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wide px-1">Payment Phases</p>

            <div v-if="phases.length === 0" class="bg-white rounded-xl border border-gray-200 py-14 text-center">
              <Layers :size="32" class="text-gray-300 mx-auto mb-3" />
              <p class="text-gray-500 font-medium">No phases yet</p>
              <p class="text-sm text-gray-400 mt-1">Phase details will appear once your architect sets them up.</p>
            </div>

            <div v-for="(phase, index) in sortedPhases" :key="phase.id">
              <div class="bg-white rounded-xl border overflow-hidden transition-all"
                :class="expandedPhaseId === phase.id ? 'border-[#C5A17A] shadow-sm' : 'border-gray-200'">

                <!-- Phase header row -->
                <button
                  @click="togglePhase(phase)"
                  class="w-full flex items-center justify-between px-5 py-4 text-left hover:bg-gray-50 transition"
                >
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-sm font-bold"
                      :class="phaseIconClass(phase.status)">
                      <CheckCircle v-if="phase.status === 'DISBURSED'" :size="16" />
                      <span v-else>{{ index + 1 }}</span>
                    </div>
                    <div>
                      <p class="font-semibold text-gray-900 text-sm">{{ phase.title || `Phase ${phase.phaseNumber}` }}</p>
                      <div class="flex items-center gap-2 text-xs text-gray-500">
                        <span>{{ formatAmount(phase.amount) }}</span>
                        <span v-if="phase.dueDate">· Due {{ formatDate(phase.dueDate) }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="flex items-center gap-3">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold"
                      :class="[statusConfig[phase.status]?.bg, statusConfig[phase.status]?.text]">
                      <span class="w-1.5 h-1.5 rounded-full" :class="statusConfig[phase.status]?.dot" />
                      {{ statusConfig[phase.status]?.label || phase.status }}
                    </span>
                    <ChevronDown :size="16" class="text-gray-400 transition-transform duration-200 shrink-0"
                      :class="expandedPhaseId === phase.id ? 'rotate-180' : ''" />
                  </div>
                </button>

                <!-- Expanded detail -->
                <div v-if="expandedPhaseId === phase.id" class="border-t border-gray-100 divide-y divide-gray-100">

                  <!-- Description -->
                  <div v-if="phase.description" class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-1.5">Description</p>
                    <p class="text-sm text-gray-600 leading-relaxed">{{ phase.description }}</p>
                  </div>

                  <!-- Client actions -->
                  <div class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">Actions</p>

                    <!-- PENDING: bill the phase -->
                    <div v-if="phase.status === 'PENDING'" class="flex items-start gap-3">
                      <div class="flex-1 p-3 bg-amber-50 border border-amber-200 rounded-lg">
                        <p class="text-sm font-semibold text-amber-800">Payment Required</p>
                        <p class="text-xs text-amber-600 mt-0.5">Generate an invoice to start this phase. The architect can begin work once payment is confirmed.</p>
                      </div>
                      <button
                        @click="billPhase(phase)"
                        :disabled="actionLoading === phase.id"
                        class="px-4 py-2.5 bg-[#1C1C1C] text-white text-sm font-semibold rounded-lg hover:bg-[#333] disabled:opacity-50 disabled:cursor-not-allowed transition whitespace-nowrap shrink-0"
                      >
                        <span v-if="actionLoading === phase.id">Generating...</span>
                        <span v-else>Bill Phase</span>
                      </button>
                    </div>

                    <!-- BILLED: awaiting payment -->
                    <div v-else-if="phase.status === 'BILLED'" class="flex items-center justify-between p-3 bg-blue-50 border border-blue-200 rounded-lg">
                      <div>
                        <p class="text-sm font-semibold text-blue-800">Invoice Sent</p>
                        <p class="text-xs text-blue-600 mt-0.5">Awaiting payment confirmation.</p>
                      </div>
                      <a v-if="phase.paymentLink" :href="phase.paymentLink" target="_blank"
                        class="px-3 py-1.5 bg-blue-700 text-white text-xs font-semibold rounded-lg hover:bg-blue-800 transition whitespace-nowrap">
                        Complete Payment
                      </a>
                    </div>

                    <!-- IN_PROGRESS: work underway -->
                    <div v-else-if="phase.status === 'IN_PROGRESS'" class="p-3 bg-sky-50 border border-sky-200 rounded-lg">
                      <p class="text-sm font-semibold text-sky-800">Work In Progress</p>
                      <p class="text-xs text-sky-600 mt-0.5">The architect is working on this phase. Deliverables will appear below when submitted.</p>
                    </div>

                    <!-- DELIVERED: approve or dispute -->
                    <div v-else-if="phase.status === 'DELIVERED'">
                      <div class="p-3 bg-purple-50 border border-purple-200 rounded-lg mb-3">
                        <p class="text-sm font-semibold text-purple-800">Work Submitted</p>
                        <p class="text-xs text-purple-600 mt-0.5">Review the deliverables below, then approve or raise a dispute.</p>
                      </div>
                      <div v-if="!showDisputeForm[phase.id]" class="flex gap-3">
                        <button
                          @click="approvePhase(phase)"
                          :disabled="actionLoading === phase.id"
                          class="flex-1 px-4 py-2.5 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 disabled:opacity-50 transition flex items-center justify-center gap-2"
                        >
                          <ThumbsUp :size="15" />
                          <span v-if="actionLoading === phase.id">Approving...</span>
                          <span v-else>Approve Work</span>
                        </button>
                        <button
                          @click="showDisputeForm[phase.id] = true"
                          class="flex-1 px-4 py-2.5 border-2 border-red-200 text-red-600 text-sm font-semibold rounded-lg hover:bg-red-50 transition flex items-center justify-center gap-2"
                        >
                          <AlertTriangle :size="15" />
                          Dispute
                        </button>
                      </div>
                      <div v-else class="space-y-3">
                        <textarea
                          v-model="disputeReason[phase.id]"
                          placeholder="Describe what doesn't match the agreed specification..."
                          rows="3"
                          class="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-red-200 focus:border-red-300"
                        />
                        <div class="flex gap-2">
                          <button
                            @click="submitDispute(phase)"
                            :disabled="!disputeReason[phase.id] || actionLoading === phase.id"
                            class="px-4 py-2 bg-red-600 text-white text-sm font-semibold rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                          >
                            <span v-if="actionLoading === phase.id">Submitting...</span>
                            <span v-else>Submit Dispute</span>
                          </button>
                          <button
                            @click="showDisputeForm[phase.id] = false"
                            class="px-4 py-2 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-100 transition"
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    </div>

                    <!-- APPROVED: payout initiated by architect -->
                    <div v-else-if="phase.status === 'APPROVED'" class="p-3 bg-green-50 border border-green-200 rounded-lg">
                      <p class="text-sm font-semibold text-green-800">Work Approved</p>
                      <p class="text-xs text-green-600 mt-0.5">You approved this phase. The architect is processing their payout.</p>
                    </div>

                    <!-- DISPUTED -->
                    <div v-else-if="phase.status === 'DISPUTED'" class="p-3 bg-red-50 border border-red-200 rounded-lg">
                      <p class="text-sm font-semibold text-red-800">Under Dispute</p>
                      <p class="text-xs text-red-600 mt-0.5">This phase is under review. Our support team will reach out to both parties.</p>
                    </div>

                    <!-- DISBURSED: complete -->
                    <div v-else-if="phase.status === 'DISBURSED'" class="flex items-center gap-2 p-3 bg-gray-50 border border-gray-200 rounded-lg">
                      <CheckCircle :size="16" class="text-green-500 shrink-0" />
                      <p class="text-sm text-gray-600 font-medium">Phase complete. Payout disbursed to architect.</p>
                    </div>
                  </div>

                  <!-- Deliverables -->
                  <div class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">Deliverables</p>
                    <div v-if="phase.deliverables && phase.deliverables.length > 0">
                      <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
                        <a
                          v-for="d in phase.deliverables" :key="d.id"
                          :href="d.filePath" target="_blank"
                          class="group border border-gray-200 rounded-lg overflow-hidden hover:border-[#C5A17A] transition block"
                        >
                          <!-- Image -->
                          <template v-if="isImage(d.fileType)">
                            <div class="aspect-video bg-gray-100 overflow-hidden">
                              <img :src="d.filePath" :alt="d.description || 'Deliverable'"
                                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200" />
                            </div>
                            <div class="p-2.5">
                              <p class="text-xs font-medium text-gray-700 truncate">{{ d.description || fileNameFromPath(d.filePath) }}</p>
                              <p class="text-xs text-gray-400 mt-0.5">{{ formatDateTime(d.uploadedAt) }}</p>
                            </div>
                          </template>
                          <!-- PDF -->
                          <template v-else-if="isPdf(d.fileType)">
                            <div class="p-3 flex items-start gap-2.5">
                              <div class="w-9 h-9 bg-red-50 rounded-lg flex items-center justify-center shrink-0">
                                <FileText :size="18" class="text-red-500" />
                              </div>
                              <div class="flex-1 min-w-0">
                                <p class="text-xs font-medium text-gray-700 truncate">{{ d.description || fileNameFromPath(d.filePath) }}</p>
                                <p class="text-xs text-gray-400 mt-0.5">PDF · {{ formatDateTime(d.uploadedAt) }}</p>
                              </div>
                            </div>
                          </template>
                          <!-- Other -->
                          <template v-else>
                            <div class="p-3 flex items-start gap-2.5">
                              <div class="w-9 h-9 bg-gray-50 rounded-lg flex items-center justify-center shrink-0">
                                <File :size="18" class="text-gray-400" />
                              </div>
                              <div class="flex-1 min-w-0">
                                <p class="text-xs font-medium text-gray-700 truncate">{{ d.description || fileNameFromPath(d.filePath) }}</p>
                                <p class="text-xs text-gray-400 mt-0.5">{{ d.fileType || 'File' }} · {{ formatDateTime(d.uploadedAt) }}</p>
                              </div>
                            </div>
                          </template>
                        </a>
                      </div>
                    </div>
                    <div v-else class="py-8 text-center border-2 border-dashed border-gray-200 rounded-xl">
                      <FileX :size="24" class="text-gray-300 mx-auto mb-2" />
                      <p class="text-xs text-gray-400">No deliverables uploaded yet</p>
                    </div>
                  </div>

                  <!-- Audit log -->
                  <div class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">Activity Log</p>
                    <div v-if="logsLoading[phase.id]" class="flex items-center gap-2 py-4">
                      <div class="w-4 h-4 border border-[#C5A17A] border-t-transparent rounded-full animate-spin" />
                      <p class="text-xs text-gray-400">Loading activity...</p>
                    </div>
                    <div v-else-if="phaseLogs[phase.id] && phaseLogs[phase.id].length > 0" class="space-y-3">
                      <div v-for="(log, i) in phaseLogs[phase.id]" :key="i" class="flex items-start gap-3">
                        <div class="w-6 h-6 rounded-full flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold"
                          :class="logIconClass(log.actorType)">
                          {{ log.actorType[0] }}
                        </div>
                        <div class="flex-1 pb-3 border-b border-gray-50 last:border-0 last:pb-0">
                          <p class="text-xs font-semibold text-gray-700">{{ formatLogAction(log.action) }}</p>
                          <p v-if="log.fromStatus && log.toStatus" class="text-xs text-gray-400 mt-0.5">
                            {{ log.fromStatus }} → {{ log.toStatus }}
                          </p>
                          <p class="text-xs text-gray-400 mt-0.5">{{ formatDateTime(log.createdAt) }}</p>
                        </div>
                      </div>
                    </div>
                    <div v-else class="py-4 text-center">
                      <p class="text-xs text-gray-400">No activity recorded yet</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- RIGHT: chat sidebar -->
        <div class="lg:col-span-1 lg:sticky lg:top-24">
          <div class="bg-white rounded-xl border border-gray-200 flex flex-col overflow-hidden" style="height: calc(100vh - 7.5rem)">

            <!-- Chat header -->
            <div class="px-4 py-4 border-b border-gray-100 shrink-0">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2.5">Project Chat</p>
              <div class="flex items-center gap-2.5">
                <div class="w-8 h-8 rounded-full bg-[#1C1C1C] text-white flex items-center justify-center text-xs font-bold shrink-0">
                  {{ architectInitials }}
                </div>
                <div>
                  <p class="text-sm font-semibold text-gray-900">{{ architectName }}</p>
                  <span class="inline-flex items-center gap-1 text-xs text-green-600">
                    <span class="w-1.5 h-1.5 rounded-full bg-green-500" />
                    Active on project
                  </span>
                </div>
              </div>
            </div>

            <!-- Message thread -->
            <div class="flex-1 overflow-y-auto px-4 py-4 space-y-4">
              <div class="text-center mb-2">
                <span class="text-xs text-gray-400 bg-gray-50 px-3 py-1 rounded-full">Today</span>
              </div>
              <div v-for="msg in mockMessages" :key="msg.id"
                class="flex" :class="msg.from === 'client' ? 'justify-end' : 'justify-start'">
                <div class="max-w-[82%]">
                  <div class="px-3 py-2 rounded-2xl text-sm leading-relaxed"
                    :class="msg.from === 'client'
                      ? 'bg-[#1C1C1C] text-white rounded-br-md'
                      : 'bg-gray-100 text-gray-800 rounded-bl-md'">
                    {{ msg.text }}
                  </div>
                  <p class="text-xs text-gray-400 mt-1 px-1"
                    :class="msg.from === 'client' ? 'text-right' : ''">
                    {{ msg.time }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Chat input -->
            <div class="px-4 py-3 border-t border-gray-100 shrink-0">
              <div class="flex items-end gap-2">
                <textarea
                  v-model="chatInput"
                  placeholder="Type a message..."
                  rows="2"
                  class="flex-1 px-3 py-2 border border-gray-200 rounded-xl text-sm resize-none focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-gray-300"
                  @keydown.enter.exact.prevent="sendChat"
                />
                <button
                  @click="sendChat"
                  class="w-9 h-9 rounded-full bg-[#1C1C1C] text-white flex items-center justify-center hover:bg-[#333] transition shrink-0 mb-0.5"
                >
                  <Send :size="15" />
                </button>
              </div>
              <p class="text-xs text-gray-300 mt-1.5">Press Enter to send · Shift+Enter for new line</p>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, CheckCircle, Building2, MapPin, Tag, Layers,
  ChevronDown, ThumbsUp, AlertTriangle, FileText, File, FileX,
  Send
} from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import { phaseAPI } from '@/services/api'

const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()

const projectId = route.params.id

const phases = ref([])
const loading = ref(true)
const error = ref(null)
const expandedPhaseId = ref(null)
const phaseLogs = reactive({})
const logsLoading = reactive({})
const actionLoading = ref(null)
const showDisputeForm = reactive({})
const disputeReason = reactive({})
const chatInput = ref('')

const mockMessages = [
  { id: 1, from: 'architect', text: 'Hi! I\'ve started reviewing the project scope. I\'ll have the first schematic ready by end of week.', time: '09:14' },
  { id: 2, from: 'client', text: 'Great, please keep the open-plan living area as the focal point.', time: '09:32' },
  { id: 3, from: 'architect', text: 'Understood. I\'m also factoring in cross-ventilation for the tropical climate — it\'ll reduce the HVAC load significantly.', time: '09:45' },
  { id: 4, from: 'client', text: 'That was on my wishlist! Looking forward to the first draft.', time: '10:02' },
]

const project = computed(() => projectsStore.currentProject)

const coverImage = computed(() => {
  const files = project.value?.files
  if (!files?.length) return null
  return files.find(f => f.fileType?.startsWith('image/'))?.filePath || null
})

const acceptedBid = computed(() => bidsStore.projectBids.find(b => b.status === 'ACCEPTED') || null)
const architectName = computed(() => acceptedBid.value?.architectName || 'Architect')
const architectInitials = computed(() =>
  architectName.value.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
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

const statusConfig = {
  PENDING:     { label: 'Pending',     bg: 'bg-amber-50',  text: 'text-amber-700',  dot: 'bg-amber-500',  icon: 'bg-amber-100 text-amber-700' },
  BILLED:      { label: 'Billed',      bg: 'bg-blue-50',   text: 'text-blue-700',   dot: 'bg-blue-500',   icon: 'bg-blue-100 text-blue-700' },
  IN_PROGRESS: { label: 'In Progress', bg: 'bg-sky-50',    text: 'text-sky-700',    dot: 'bg-sky-500',    icon: 'bg-sky-100 text-sky-700' },
  DELIVERED:   { label: 'Delivered',   bg: 'bg-purple-50', text: 'text-purple-700', dot: 'bg-purple-500', icon: 'bg-purple-100 text-purple-700' },
  APPROVED:    { label: 'Approved',    bg: 'bg-green-50',  text: 'text-green-700',  dot: 'bg-green-500',  icon: 'bg-green-100 text-green-700' },
  DISBURSED:   { label: 'Disbursed',   bg: 'bg-gray-50',   text: 'text-gray-500',   dot: 'bg-gray-400',   icon: 'bg-green-100 text-green-600' },
  DISPUTED:    { label: 'Disputed',    bg: 'bg-red-50',    text: 'text-red-700',    dot: 'bg-red-500',    icon: 'bg-red-100 text-red-700' },
}

const phaseIconClass = status => {
  if (status === 'DISBURSED') return 'bg-green-100 text-green-600'
  return statusConfig[status]?.icon || 'bg-gray-100 text-gray-500'
}

const logIconClass = actorType => ({
  CLIENT:   'bg-blue-100 text-blue-700',
  ARCHITECT: 'bg-purple-100 text-purple-700',
  SYSTEM:   'bg-gray-100 text-gray-500',
  XENDIT:   'bg-green-100 text-green-700',
}[actorType] || 'bg-gray-100 text-gray-500')

const formatLogAction = action => {
  const labels = {
    PHASE_CREATED: 'Phase created',
    PHASE_BILLED: 'Phase billed — invoice generated',
    PAYMENT_RECEIVED: 'Payment received',
    DELIVERABLE_UPLOADED: 'Deliverable uploaded',
    DELIVERABLE_APPROVED: 'Deliverable approved by client',
    DELIVERABLE_DISPUTED: 'Deliverable disputed by client',
    PAYOUT_INITIATED: 'Payout requested by architect',
    PAYOUT_COMPLETED: 'Payout completed',
    PAYOUT_FAILED: 'Payout failed',
  }
  return labels[action] || action.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase())
}

const isImage = fileType => fileType?.startsWith('image/')
const isPdf = fileType => fileType === 'application/pdf'
const fileNameFromPath = path => path?.split('/').pop() || path

const formatAmount = amount => {
  if (amount == null) return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(amount)
}

const formatDate = dateStr => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' })
}

const formatDateTime = dateStr => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('id-ID', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
}

const loadAll = async () => {
  loading.value = true
  error.value = null
  try {
    await Promise.all([
      projectsStore.fetchProjectById(projectId),
      bidsStore.fetchProjectBids(projectId),
    ])
    const res = await phaseAPI.getPhases(projectId)
    phases.value = res.data.data || res.data || []
    const active = sortedPhases.value.find(p => p.status !== 'DISBURSED')
    if (active) {
      expandedPhaseId.value = active.id
      fetchLogs(active.id)
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load workspace'
  } finally {
    loading.value = false
  }
}

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

const togglePhase = async phase => {
  if (expandedPhaseId.value === phase.id) {
    expandedPhaseId.value = null
    return
  }
  expandedPhaseId.value = phase.id
  fetchLogs(phase.id)
}

const refreshPhases = async () => {
  const res = await phaseAPI.getPhases(projectId)
  phases.value = res.data.data || res.data || []
  // clear log cache so they reload on next expand
  Object.keys(phaseLogs).forEach(k => delete phaseLogs[k])
}

const billPhase = async phase => {
  actionLoading.value = phase.id
  try {
    const res = await phaseAPI.billPhase(phase.id)
    const link = res.data.data?.paymentLink || res.data?.paymentLink
    if (link) window.open(link, '_blank')
    await refreshPhases()
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to bill phase')
  } finally {
    actionLoading.value = null
  }
}

const approvePhase = async phase => {
  actionLoading.value = phase.id
  try {
    await phaseAPI.approveDeliverable(phase.id)
    await refreshPhases()
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to approve phase')
  } finally {
    actionLoading.value = null
  }
}

const submitDispute = async phase => {
  const reason = disputeReason[phase.id]
  if (!reason?.trim()) return
  actionLoading.value = phase.id
  try {
    await phaseAPI.disputeDeliverable(phase.id, { reason })
    showDisputeForm[phase.id] = false
    disputeReason[phase.id] = ''
    await refreshPhases()
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to submit dispute')
  } finally {
    actionLoading.value = null
  }
}

const sendChat = () => {
  chatInput.value = ''
}

onMounted(loadAll)
</script>
