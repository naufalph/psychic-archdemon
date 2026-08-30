<template>
  <div class="min-h-screen bg-surface-alt">
    <div v-if="loading" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <div class="w-10 h-10 border-2 border-brand-gold border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p class="text-gray-500">{{ t.projectWorkspace?.loading }}</p>
      </div>
    </div>

    <div v-else-if="error" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button class="text-brand-brown hover:underline" @click="loadAll">{{ t.projectWorkspace?.tryAgain }}</button>
      </div>
    </div>

    <div v-else>
      <!-- Header -->
      <div class="bg-white border-b border-gray-200 px-6 py-4 sticky top-0 z-10">
        <div class="max-w-7xl mx-auto flex items-center justify-between">
          <div class="flex items-center gap-4">
            <button class="text-gray-500 hover:text-black transition" @click="router.back()">
              <ArrowLeft :size="20" />
            </button>
            <div>
              <p class="text-xs text-gray-400 uppercase font-bold tracking-wide">{{ t.projectWorkspace?.eyebrow }}</p>
              <h1 class="text-lg font-bold text-black">{{ project?.title || t.projectWorkspace?.titleFallback }}</h1>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span class="text-sm text-gray-500 flex items-center gap-1.5">
              <Layers :size="14" />
              {{ disbursedCount }} / {{ phases.length }} {{ t.projectWorkspace?.phasesDone }}
            </span>
            <span
              v-if="isCompleted"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold bg-green-100 text-green-700 whitespace-nowrap"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-green-600" />
              {{ t.projectWorkspace?.completedBadge }}
            </span>
            <span
              v-else
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold bg-blue-50 text-blue-700 whitespace-nowrap"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-blue-500" />
              {{ t.projectWorkspace?.inProgressBadge }}
            </span>
          </div>
        </div>
      </div>

      <!-- Body -->
      <div class="max-w-7xl mx-auto px-6 py-6 grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        <!-- LEFT: overview + phases -->
        <div class="lg:col-span-2 space-y-4">
          <!-- Project overview card (clickable) -->
          <button
            class="w-full bg-white rounded-xl border border-gray-200 p-5 text-left hover:border-brand-gold hover:shadow-sm transition group"
            @click="showProjectModal = true"
          >
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
                    <div class="flex items-center gap-2">
                      <h2 class="font-bold text-gray-900">{{ project?.title }}</h2>
                      <span class="text-xs text-brand-brown font-medium group-hover:underline">{{
                        t.projectWorkspace?.viewDetail
                      }}</span>
                    </div>
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
                    <p class="text-xs text-gray-400">{{ t.projectWorkspace?.totalValue }}</p>
                    <p class="font-bold text-gray-900">{{ formatAmount(totalAmount) }}</p>
                  </div>
                </div>
                <div class="space-y-1">
                  <div class="flex justify-between text-xs text-gray-500">
                    <span>{{ formatAmount(paidAmount) }} {{ t.projectWorkspace?.paidLabel }}</span>
                    <span>{{ Math.round(progressPercent) }}%</span>
                  </div>
                  <div class="w-full bg-gray-100 rounded-full h-1.5">
                    <div
                      class="bg-green-500 h-1.5 rounded-full transition-all duration-500"
                      :style="{ width: progressPercent + '%' }"
                    />
                  </div>
                  <div class="flex justify-between text-xs text-gray-400">
                    <span
                      >{{ disbursedCount }} {{ t.projectWorkspace?.of }} {{ phases.length }}
                      {{ t.projectWorkspace?.phasesDone }}</span
                    >
                    <span>{{ formatAmount(remainingAmount) }} {{ t.projectWorkspace?.remaining }}</span>
                  </div>
                </div>
              </div>
            </div>
          </button>

          <!-- Phase list -->
          <div class="space-y-3">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wide px-1">
              {{ t.projectWorkspace?.paymentPhasesTitle }}
            </p>

            <div v-if="phases.length === 0" class="bg-white rounded-xl border border-gray-200 py-14 text-center">
              <Layers :size="32" class="text-gray-300 mx-auto mb-3" />
              <p class="text-gray-500 font-medium">{{ t.projectWorkspace?.noPhasesTitle }}</p>
              <p class="text-sm text-gray-400 mt-1 mb-4">{{ t.projectWorkspace?.noPhasesDesc }}</p>
              <button
                :disabled="initializingPhases"
                class="px-4 py-2 bg-brand-brown text-white text-sm font-semibold rounded-lg hover:bg-black disabled:opacity-50 transition"
                @click="initPhases"
              >
                {{ initializingPhases ? t.projectWorkspace?.initializing : t.projectWorkspace?.initPhasesBtn }}
              </button>
            </div>

            <div v-for="(phase, index) in sortedPhases" :key="phase.id">
              <div
                class="bg-white rounded-xl border overflow-hidden transition-all"
                :class="expandedPhaseId === phase.id ? 'border-brand-gold shadow-sm' : 'border-gray-200'"
              >
                <!-- Phase header row -->
                <button
                  class="w-full flex items-center justify-between px-5 py-4 text-left hover:bg-gray-50 transition"
                  @click="togglePhase(phase)"
                >
                  <div class="flex items-center gap-3">
                    <div
                      class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-sm font-bold"
                      :class="phaseIconClass(phase.status, index)"
                    >
                      <CheckCircle v-if="phase.status === 'DISBURSED'" :size="16" />
                      <Lock v-else-if="isNotStarted(phase, index)" :size="14" />
                      <span v-else>{{ index + 1 }}</span>
                    </div>
                    <div>
                      <p class="font-semibold text-gray-900 text-sm">
                        {{ phase.title || phaseFallbackTitle(phase) }}
                      </p>
                      <div class="flex items-center gap-2 text-xs text-gray-500">
                        <span>{{ formatAmount(phase.amount) }}</span>
                        <span v-if="phase.dueDate"
                          >· {{ t.projectWorkspace?.deadline }} {{ formatDate(phase.dueDate) }}</span
                        >
                      </div>
                    </div>
                  </div>
                  <div class="flex items-center gap-3">
                    <span
                      class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold whitespace-nowrap"
                      :class="[phaseStatusConfig(phase, index)?.bg, phaseStatusConfig(phase, index)?.text]"
                    >
                      <span class="w-1.5 h-1.5 rounded-full" :class="phaseStatusConfig(phase, index)?.dot" />
                      {{ phaseStatusConfig(phase, index)?.label }}
                    </span>
                    <ChevronDown
                      :size="16"
                      class="text-gray-400 transition-transform duration-200 shrink-0"
                      :class="expandedPhaseId === phase.id ? 'rotate-180' : ''"
                    />
                  </div>
                </button>

                <!-- Expanded detail -->
                <div v-if="expandedPhaseId === phase.id" class="border-t border-gray-100 divide-y divide-gray-100">
                  <!-- Description -->
                  <div v-if="phase.description" class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-1.5">
                      {{ t.projectWorkspace?.descriptionLabel }}
                    </p>
                    <p class="text-sm text-gray-600 leading-relaxed">{{ phase.description }}</p>
                  </div>

                  <!-- Revision info banner -->
                  <div
                    v-if="showRevisionBadge(phase)"
                    class="px-5 py-3"
                    :class="
                      revisionsLeft(phase) === 0
                        ? 'bg-red-50 border-b border-red-100'
                        : 'bg-purple-50 border-b border-purple-100'
                    "
                  >
                    <div class="flex items-center gap-2">
                      <RotateCcw :size="14" :class="revisionsLeft(phase) === 0 ? 'text-red-500' : 'text-purple-600'" />
                      <span
                        class="text-xs font-semibold"
                        :class="revisionsLeft(phase) === 0 ? 'text-red-700' : 'text-purple-700'"
                      >
                        {{ t.projectWorkspace?.revisionsLeftLabel }}: {{ revisionsLeft(phase) }}
                        {{ t.projectWorkspace?.of }} {{ phase.maxRevisions }}
                      </span>
                      <span v-if="revisionsLeft(phase) === 0" class="text-xs text-red-600 font-medium ml-1"
                        >· {{ t.projectWorkspace?.revisionsExhausted }}</span
                      >
                    </div>
                  </div>

                  <!-- Client actions -->
                  <div class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">
                      {{ t.projectWorkspace?.actionsLabel }}
                    </p>

                    <!-- NOT STARTED -->
                    <div
                      v-if="isNotStarted(phase, index)"
                      class="p-3 bg-gray-50 border border-gray-200 rounded-lg flex items-center gap-3"
                    >
                      <Lock :size="16" class="text-gray-400 shrink-0" />
                      <div>
                        <p class="text-sm font-semibold text-gray-500">{{ t.projectWorkspace?.notStartedTitle }}</p>
                        <p class="text-xs text-gray-400 mt-0.5">
                          {{ t.projectWorkspace?.notStartedDesc }}
                        </p>
                      </div>
                    </div>

                    <!-- PENDING: bill the phase (current phase only) -->
                    <div v-else-if="phase.status === 'PENDING'" class="flex items-start gap-3">
                      <div class="flex-1 p-3 bg-amber-50 border border-amber-200 rounded-lg">
                        <p class="text-sm font-semibold text-amber-800">
                          {{ t.projectWorkspace?.paymentRequiredTitle }}
                        </p>
                        <p class="text-xs text-amber-600 mt-0.5">
                          {{ t.projectWorkspace?.paymentRequiredDesc }}
                        </p>
                      </div>
                      <button
                        :disabled="actionLoading === phase.id"
                        class="px-4 py-2.5 bg-ink-700 text-white text-sm font-semibold rounded-lg hover:bg-ink-500 disabled:opacity-50 disabled:cursor-not-allowed transition whitespace-nowrap shrink-0"
                        @click="billPhase(phase)"
                      >
                        <span v-if="actionLoading === phase.id">{{ t.projectWorkspace?.creating }}</span>
                        <span v-else>{{ t.projectWorkspace?.createInvoice }}</span>
                      </button>
                    </div>

                    <!-- BILLED: awaiting payment -->
                    <div
                      v-else-if="phase.status === 'BILLED'"
                      class="flex items-center justify-between p-3 bg-blue-50 border border-blue-200 rounded-lg"
                    >
                      <div>
                        <p class="text-sm font-semibold text-blue-800">{{ t.projectWorkspace?.invoiceSentTitle }}</p>
                        <p class="text-xs text-blue-600 mt-0.5">
                          {{ t.projectWorkspace?.invoiceSentDesc }}
                        </p>
                      </div>
                      <a
                        v-if="phase.paymentLink"
                        :href="phase.paymentLink"
                        target="_blank"
                        class="px-3 py-1.5 bg-blue-700 text-white text-xs font-semibold rounded-lg hover:bg-blue-800 transition whitespace-nowrap"
                      >
                        {{ t.projectWorkspace?.payNow }}
                      </a>
                      <button
                        v-else
                        :disabled="actionLoading === phase.id"
                        class="px-3 py-1.5 bg-blue-700 text-white text-xs font-semibold rounded-lg hover:bg-blue-800 disabled:opacity-50 transition whitespace-nowrap"
                        @click="billPhase(phase)"
                      >
                        {{ t.projectWorkspace?.getLink }}
                      </button>
                    </div>

                    <!-- IN_PROGRESS: work underway -->
                    <div
                      v-else-if="phase.status === 'IN_PROGRESS'"
                      class="p-3 bg-sky-50 border border-sky-200 rounded-lg"
                    >
                      <p class="text-sm font-semibold text-sky-800">{{ t.projectWorkspace?.workInProgressTitle }}</p>
                      <p class="text-xs text-sky-600 mt-0.5">
                        {{ t.projectWorkspace?.workInProgressDesc }}
                      </p>
                    </div>

                    <!-- DELIVERED: approve, request revision, or dispute -->
                    <div v-else-if="phase.status === 'DELIVERED'">
                      <div class="p-3 bg-purple-50 border border-purple-200 rounded-lg mb-3">
                        <p class="text-sm font-semibold text-purple-800">
                          {{ t.projectWorkspace?.workSubmittedTitle }}
                        </p>
                        <p class="text-xs text-purple-600 mt-0.5">
                          {{ t.projectWorkspace?.workSubmittedDesc }}
                        </p>
                      </div>

                      <div v-if="!showDisputeForm[phase.id]">
                        <!-- Primary actions row -->
                        <div class="flex flex-wrap gap-2">
                          <!-- Approve button -->
                          <button
                            :disabled="actionLoading === phase.id"
                            class="flex-1 min-w-[140px] px-4 py-2.5 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 disabled:opacity-50 transition flex items-center justify-center gap-2"
                            @click="showApproveConfirm = phase.id"
                          >
                            <ThumbsUp :size="15" />
                            {{ t.projectWorkspace?.approveBtn }}
                          </button>

                          <!-- Request revision button (if revisions remain) -->
                          <button
                            v-if="revisionsLeft(phase) > 0"
                            :disabled="actionLoading === phase.id"
                            class="flex-1 min-w-[140px] px-4 py-2.5 border-2 border-amber-300 text-amber-700 text-sm font-semibold rounded-lg hover:bg-amber-50 disabled:opacity-50 transition flex items-center justify-center gap-2"
                            @click="doRequestRevision(phase)"
                          >
                            <RotateCcw :size="15" />
                            {{ t.projectWorkspace?.requestRevisionBtn }}
                          </button>
                        </div>

                        <!-- Dispute trigger — de-emphasized secondary action -->
                        <button
                          class="w-full mt-2 px-3 py-1.5 text-xs border border-red-200 text-red-500 rounded-lg hover:bg-red-50 transition flex items-center justify-center gap-1.5"
                          @click="showDisputeForm[phase.id] = true"
                        >
                          <AlertTriangle :size="12" />
                          {{ t.projectWorkspace?.disputeBtn }}
                        </button>
                      </div>

                      <!-- Dispute form -->
                      <div v-else class="space-y-3">
                        <p class="text-xs font-bold text-gray-500 uppercase tracking-wide">
                          {{ t.projectWorkspace?.disputeReasonLabel }}
                        </p>
                        <textarea
                          v-model="disputeReason[phase.id]"
                          :placeholder="t.projectWorkspace?.disputeReasonPlaceholder"
                          rows="3"
                          class="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-red-200 focus:border-red-300"
                        />
                        <div class="flex gap-2">
                          <button
                            :disabled="!disputeReason[phase.id] || actionLoading === phase.id"
                            class="px-4 py-2 bg-red-600 text-white text-sm font-semibold rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                            @click="submitDispute(phase)"
                          >
                            <span v-if="actionLoading === phase.id">{{ t.projectWorkspace?.submitting }}</span>
                            <span v-else>{{ t.projectWorkspace?.submitDispute }}</span>
                          </button>
                          <button
                            class="px-4 py-2 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-100 transition"
                            @click="showDisputeForm[phase.id] = false"
                          >
                            {{ t.projectWorkspace?.cancel }}
                          </button>
                        </div>
                      </div>
                    </div>

                    <!-- APPROVED: payout initiated by architect -->
                    <div
                      v-else-if="phase.status === 'APPROVED'"
                      class="p-3 bg-green-50 border border-green-200 rounded-lg"
                    >
                      <p class="text-sm font-semibold text-green-800">{{ t.projectWorkspace?.workApprovedTitle }}</p>
                      <p class="text-xs text-green-600 mt-0.5">
                        {{ t.projectWorkspace?.workApprovedDesc }}
                      </p>
                    </div>

                    <!-- DISPUTED -->
                    <div v-else-if="phase.status === 'DISPUTED'" class="p-3 bg-red-50 border border-red-200 rounded-lg">
                      <p class="text-sm font-semibold text-red-800">{{ t.projectWorkspace?.underDisputeTitle }}</p>
                      <p class="text-xs text-red-600 mt-0.5">
                        {{ t.projectWorkspace?.underDisputeDesc }}
                      </p>
                    </div>

                    <!-- DISBURSED: complete -->
                    <div
                      v-else-if="phase.status === 'DISBURSED'"
                      class="flex items-center gap-2 p-3 bg-gray-50 border border-gray-200 rounded-lg"
                    >
                      <CheckCircle :size="16" class="text-green-500 shrink-0" />
                      <p class="text-sm text-gray-600 font-medium">
                        {{ t.projectWorkspace?.phaseCompleteDisbursed }}
                      </p>
                    </div>
                  </div>

                  <!-- Deliverables -->
                  <div class="px-5 py-4">
                    <div class="flex items-center justify-between mb-3">
                      <p class="text-xs font-bold text-gray-400 uppercase tracking-wide">
                        {{ t.projectWorkspace?.deliverablesLabel }}
                      </p>
                      <span class="text-xs text-gray-400"
                        >{{ phase.deliverables?.length || 0 }} {{ t.projectWorkspace?.fileCountSuffix }}</span
                      >
                    </div>
                    <div v-if="phase.deliverables && phase.deliverables.length > 0" class="space-y-4">
                      <div v-for="group in groupedDeliverables(phase)" :key="group.round">
                        <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">
                          {{
                            group.round === 0
                              ? t.projectWorkspace?.initialDelivery
                              : t.projectWorkspace?.revisionRound + ' ' + group.round
                          }}
                        </p>
                        <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
                          <button
                            v-for="d in group.files"
                            :key="d.id"
                            class="group border border-gray-200 rounded-lg overflow-hidden hover:border-brand-gold transition text-left"
                            @click="openPreview(phase, phase.deliverables.indexOf(d))"
                          >
                            <template v-if="isImage(d.fileType)">
                              <div class="aspect-video bg-gray-100 overflow-hidden relative">
                                <img
                                  :src="d.filePath"
                                  :alt="d.description || 'Deliverable'"
                                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
                                />
                                <div
                                  class="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition flex items-center justify-center"
                                >
                                  <Eye :size="20" class="text-white opacity-0 group-hover:opacity-100 transition" />
                                </div>
                              </div>
                              <div class="p-2.5">
                                <p class="text-xs font-medium text-gray-700 truncate">
                                  {{ d.description || fileNameFromPath(d.filePath) }}
                                </p>
                                <p class="text-xs text-gray-400 mt-0.5">{{ formatDateTime(d.uploadedAt) }}</p>
                              </div>
                            </template>
                            <template v-else-if="isPdf(d.fileType)">
                              <div class="p-3 flex items-start gap-2.5">
                                <div class="w-9 h-9 bg-red-50 rounded-lg flex items-center justify-center shrink-0">
                                  <FileText :size="18" class="text-red-500" />
                                </div>
                                <div class="flex-1 min-w-0">
                                  <p class="text-xs font-medium text-gray-700 truncate">
                                    {{ d.description || fileNameFromPath(d.filePath) }}
                                  </p>
                                  <p class="text-xs text-gray-400 mt-0.5">PDF · {{ formatDateTime(d.uploadedAt) }}</p>
                                </div>
                              </div>
                            </template>
                            <template v-else>
                              <div class="p-3 flex items-start gap-2.5">
                                <div class="w-9 h-9 bg-gray-50 rounded-lg flex items-center justify-center shrink-0">
                                  <File :size="18" class="text-gray-400" />
                                </div>
                                <div class="flex-1 min-w-0">
                                  <p class="text-xs font-medium text-gray-700 truncate">
                                    {{ d.description || fileNameFromPath(d.filePath) }}
                                  </p>
                                  <p class="text-xs text-gray-400 mt-0.5">
                                    {{ d.fileType || 'File' }} · {{ formatDateTime(d.uploadedAt) }}
                                  </p>
                                </div>
                              </div>
                            </template>
                          </button>
                        </div>
                      </div>
                    </div>
                    <div v-else class="py-8 text-center border-2 border-dashed border-gray-200 rounded-xl">
                      <FileX :size="24" class="text-gray-300 mx-auto mb-2" />
                      <p class="text-xs text-gray-400">{{ t.projectWorkspace?.noDeliverablesUploaded }}</p>
                    </div>
                  </div>

                  <!-- Audit log -->
                  <div class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">
                      {{ t.projectWorkspace?.activityLogLabel }}
                    </p>
                    <div v-if="logsLoading[phase.id]" class="flex items-center gap-2 py-4">
                      <div class="w-4 h-4 border border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <p class="text-xs text-gray-400">{{ t.projectWorkspace?.loadingActivity }}</p>
                    </div>
                    <div v-else-if="phaseLogs[phase.id] && phaseLogs[phase.id].length > 0" class="space-y-3">
                      <div v-for="(log, i) in phaseLogs[phase.id]" :key="i" class="flex items-start gap-3">
                        <div
                          class="w-6 h-6 rounded-full flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold"
                          :class="logIconClass(log.actorType)"
                        >
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
                      <p class="text-xs text-gray-400">{{ t.projectWorkspace?.noActivityRecorded }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- RIGHT: chat sidebar -->
        <div class="lg:col-span-1 lg:sticky lg:top-24">
          <div
            class="bg-white rounded-xl border border-gray-200 flex flex-col overflow-hidden"
            style="height: calc(100vh - 7.5rem)"
          >
            <div class="px-4 py-4 border-b border-gray-100 shrink-0">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2.5">
                {{ t.projectWorkspace?.chatTitle }}
              </p>
              <div class="flex items-center gap-2.5">
                <div
                  class="w-8 h-8 rounded-full bg-ink-700 text-white flex items-center justify-center text-xs font-bold shrink-0"
                >
                  {{ architectInitials }}
                </div>
                <div>
                  <p class="text-sm font-semibold text-gray-900">{{ architectName }}</p>
                  <span class="inline-flex items-center gap-1 text-xs text-green-600">
                    <span class="w-1.5 h-1.5 rounded-full bg-green-500" />
                    {{ t.projectWorkspace?.activeOnProject }}
                  </span>
                </div>
              </div>
            </div>
            <div class="flex-1 min-h-0 flex flex-col">
              <ChatPanel v-if="conversationId" :conversation-id="conversationId" class="flex-1 min-h-0" />
              <div v-else class="flex-1 flex items-center justify-center text-center px-4">
                <p class="text-sm text-gray-400">{{ t.projectWorkspace?.chatUnavailableHint }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Project Detail Modal -->
    <Teleport to="body">
      <div
        v-if="showProjectModal"
        class="fixed inset-0 z-50 flex items-start justify-center bg-black/60 backdrop-blur-sm overflow-y-auto py-8 px-4"
        @click.self="showProjectModal = false"
      >
        <div class="bg-white rounded-2xl w-full max-w-3xl shadow-2xl overflow-hidden my-auto">
          <div class="bg-ink-700 px-6 py-5 flex items-start justify-between">
            <div>
              <p class="text-xs text-brand-gold uppercase font-bold tracking-widest mb-1">
                {{ t.projectWorkspace?.projectDetailsLabel }}
              </p>
              <h2 class="text-xl font-bold text-white leading-tight">{{ project?.title }}</h2>
            </div>
            <button
              class="text-gray-400 hover:text-white transition ml-4 shrink-0 mt-1"
              @click="showProjectModal = false"
            >
              <X :size="22" />
            </button>
          </div>

          <div class="p-6 space-y-6 max-h-[75vh] overflow-y-auto">
            <div v-if="coverImage" class="w-full h-48 rounded-xl overflow-hidden bg-gray-100">
              <img :src="coverImage" :alt="project?.title" class="w-full h-full object-cover" />
            </div>

            <div>
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">
                {{ t.projectWorkspace?.projectInfoLabel }}
              </p>
              <div class="grid grid-cols-2 gap-3">
                <div v-if="project?.location" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">{{ t.projectWorkspace?.locationLabel }}</p>
                  <p class="text-sm font-semibold text-gray-800">{{ project.location }}</p>
                </div>
                <div v-if="project?.projectCategory" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">{{ t.projectWorkspace?.categoryLabel }}</p>
                  <p class="text-sm font-semibold text-gray-800">{{ project.projectCategory }}</p>
                </div>
                <div v-if="project?.buildingFunction" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">{{ t.projectWorkspace?.buildingFunctionLabel }}</p>
                  <p class="text-sm font-semibold text-gray-800">{{ projectTypeLabel(project, locale) }}</p>
                </div>
                <div v-if="project?.estimatedBuildArea" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">{{ t.projectWorkspace?.lotSizeLabel }}</p>
                  <p class="text-sm font-semibold text-gray-800">{{ project.estimatedBuildArea }} m²</p>
                </div>
                <div v-if="project?.numberOfFloors" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">{{ t.projectWorkspace?.floorsLabel }}</p>
                  <p class="text-sm font-semibold text-gray-800">{{ project.numberOfFloors }}</p>
                </div>
                <div
                  v-if="project?.designBudgetMin || project?.designBudgetMax"
                  class="bg-gray-50 rounded-lg px-4 py-3"
                >
                  <p class="text-xs text-gray-400 mb-0.5">{{ t.projectWorkspace?.designBudgetLabel }}</p>
                  <p class="text-sm font-semibold text-gray-800">
                    {{ formatAmount(project.designBudgetMin) }} – {{ formatAmount(project.designBudgetMax) }}
                  </p>
                </div>
              </div>
            </div>

            <div v-if="project?.scopeOfWork">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">
                {{ t.projectWorkspace?.scopeOfWorkLabel }}
              </p>
              <p class="text-sm text-gray-700 leading-relaxed bg-gray-50 rounded-lg p-4">{{ project.scopeOfWork }}</p>
            </div>

            <div v-if="project?.deliverables && project.deliverables.length > 0">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">
                {{ t.projectWorkspace?.projectDeliverablesLabel }}
              </p>
              <ul class="space-y-1">
                <li
                  v-for="(item, i) in project.deliverables"
                  :key="i"
                  class="flex items-center gap-2 text-sm text-gray-700"
                >
                  <span class="w-1.5 h-1.5 rounded-full bg-brand-gold shrink-0" />
                  {{ item }}
                </li>
              </ul>
            </div>

            <div class="border-t border-gray-100" />

            <div v-if="acceptedBid">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">
                {{ t.projectWorkspace?.winningBidLabel }}
              </p>
              <div class="bg-brand-tan/40 border border-brand-gold/30 rounded-xl p-4 space-y-3">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <div
                      class="w-10 h-10 rounded-full bg-brand-brown text-white flex items-center justify-center text-sm font-bold shrink-0"
                    >
                      {{
                        (acceptedBid.architectName || 'A')
                          .split(' ')
                          .map(w => w[0])
                          .join('')
                          .slice(0, 2)
                          .toUpperCase()
                      }}
                    </div>
                    <div>
                      <p class="font-semibold text-gray-900 text-sm">{{ acceptedBid.architectName }}</p>
                      <p v-if="acceptedBid.architectCompany" class="text-xs text-gray-500">
                        {{ acceptedBid.architectCompany }}
                      </p>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-xs text-gray-400">{{ t.projectWorkspace?.bidAmountLabel }}</p>
                    <p class="font-bold text-brand-brown">{{ formatAmount(acceptedBid.bidAmount) }}</p>
                  </div>
                </div>

                <div v-if="acceptedBid.proposedTimelineDays" class="flex items-center gap-2 text-xs text-gray-600">
                  <Clock :size="13" />
                  <span>{{ acceptedBid.proposedTimelineDays }} {{ t.projectWorkspace?.workDaysSuffix }}</span>
                </div>

                <div v-if="acceptedBid.proposal" class="pt-2 border-t border-brand-gold/20">
                  <p class="text-xs font-semibold text-gray-500 mb-1">Bid</p>
                  <p class="text-sm text-gray-700 leading-relaxed line-clamp-4">{{ acceptedBid.proposal }}</p>
                </div>

                <div
                  v-if="acceptedBid.details?.phases && acceptedBid.details.phases.length > 0"
                  class="pt-2 border-t border-brand-gold/20"
                >
                  <p class="text-xs font-semibold text-gray-500 mb-2">{{ t.projectWorkspace?.phasePlanLabel }}</p>
                  <div class="space-y-2">
                    <div
                      v-for="(bp, i) in acceptedBid.details.phases"
                      :key="i"
                      class="flex items-start justify-between gap-3 py-2 border-b border-brand-gold/10 last:border-0"
                    >
                      <div class="flex items-start gap-2 flex-1">
                        <span
                          class="w-5 h-5 rounded-full bg-brand-brown text-white flex items-center justify-center text-xs font-bold shrink-0 mt-0.5"
                        >
                          {{ bp.phaseNumber }}
                        </span>
                        <div>
                          <p class="text-xs font-semibold text-gray-800">{{ bp.title }}</p>
                          <p v-if="bp.estimatedDays" class="text-xs text-gray-500 mt-0.5">
                            {{ bp.estimatedDays }} {{ t.projectWorkspace?.days }}
                          </p>
                          <p v-if="bp.revisionRounds" class="text-xs text-gray-400 mt-0.5">
                            {{ bp.revisionRounds }}x {{ t.projectWorkspace?.revisionsWord }}
                          </p>
                        </div>
                      </div>
                      <span class="text-xs font-bold text-gray-700 shrink-0">{{ formatAmount(bp.amount) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="px-6 py-4 border-t border-gray-100 flex justify-end">
            <button
              class="px-5 py-2 bg-ink-700 text-white text-sm font-semibold rounded-lg hover:bg-ink-500 transition"
              @click="showProjectModal = false"
            >
              {{ t.projectWorkspace?.closeLabel }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Hard Approve Confirmation Modal -->
    <Teleport to="body">
      <div
        v-if="showApproveConfirm"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm px-4"
        @click.self="showApproveConfirm = null"
      >
        <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl overflow-hidden">
          <div class="bg-green-600 px-6 py-5">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center shrink-0">
                <ShieldCheck :size="22" class="text-white" />
              </div>
              <div>
                <p class="text-xs text-green-100 font-semibold uppercase tracking-wide">
                  {{ t.projectWorkspace?.confirmApprovalEyebrow }}
                </p>
                <h3 class="text-lg font-bold text-white">{{ t.projectWorkspace?.approveModalTitle }}</h3>
              </div>
            </div>
          </div>

          <div class="p-6 space-y-4">
            <div class="p-4 bg-amber-50 border border-amber-200 rounded-xl">
              <p class="text-sm font-bold text-amber-800 mb-2">{{ t.projectWorkspace?.approveModalWarningTitle }}</p>
              <ul class="text-xs text-amber-700 space-y-1.5 list-disc list-inside">
                <li v-html="t.projectWorkspace?.approveModalItem1" />
                <li v-html="t.projectWorkspace?.approveModalItem2" />
                <li>{{ t.projectWorkspace?.approveModalItem3 }}</li>
              </ul>
            </div>

            <div class="p-4 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-600">
              <p class="font-semibold text-gray-700 mb-1">{{ t.projectWorkspace?.approveModalConfirmLabel }}</p>
              <p>
                {{ t.projectWorkspace?.approveModalConfirmDesc }}
              </p>
            </div>

            <div class="flex gap-3 pt-2">
              <button
                class="flex-1 px-4 py-2.5 border border-gray-200 text-gray-600 text-sm font-semibold rounded-lg hover:bg-gray-50 transition"
                @click="showApproveConfirm = null"
              >
                {{ t.projectWorkspace?.cancel }}
              </button>
              <button
                :disabled="actionLoading === showApproveConfirm"
                class="flex-1 px-4 py-2.5 bg-green-600 text-white text-sm font-bold rounded-lg hover:bg-green-700 disabled:opacity-50 transition flex items-center justify-center gap-2"
                @click="approvePhase(showApproveConfirm)"
              >
                <span v-if="actionLoading === showApproveConfirm">{{ t.projectWorkspace?.approving }}</span>
                <template v-else>
                  <ThumbsUp :size="15" />
                  {{ t.projectWorkspace?.confirmApproveBtn }}
                </template>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Fullscreen Deliverable Preview -->
    <Teleport to="body">
      <div
        v-if="previewState.open"
        ref="previewOverlay"
        class="fixed inset-0 z-[60] bg-black/95 flex flex-col"
        tabindex="0"
        @keydown.esc="closePreview"
      >
        <!-- Preview toolbar -->
        <div class="flex items-center justify-between px-6 py-4 bg-black/80 border-b border-white/10 shrink-0">
          <div class="flex items-center gap-3">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wide">
              {{ previewState.index + 1 }} / {{ previewState.items.length }}
            </span>
            <span class="text-sm font-semibold text-white truncate max-w-xs">
              {{
                previewState.items[previewState.index]?.description ||
                fileNameFromPath(previewState.items[previewState.index]?.filePath)
              }}
            </span>
          </div>
          <div class="flex items-center gap-3">
            <a
              :href="previewState.items[previewState.index]?.filePath"
              target="_blank"
              download
              class="flex items-center gap-2 px-3 py-1.5 border border-white/20 text-white text-xs font-semibold rounded-lg hover:bg-white/10 transition"
            >
              <Download :size="14" />
              {{ t.projectWorkspace?.downloadLabel }}
            </a>
            <button
              class="p-2 text-gray-400 hover:text-white transition rounded-lg hover:bg-white/10"
              @click="closePreview"
            >
              <X :size="20" />
            </button>
          </div>
        </div>

        <!-- Preview content -->
        <div class="flex-1 min-h-0 relative flex items-center justify-center overflow-hidden">
          <!-- Left nav -->
          <button
            v-if="previewState.items.length > 1"
            class="absolute left-4 z-10 p-3 bg-black/50 hover:bg-black/80 text-white rounded-full transition"
            @click="prevPreview"
          >
            <ChevronLeft :size="22" />
          </button>

          <!-- Image preview -->
          <template v-if="isImage(previewState.items[previewState.index]?.fileType)">
            <img
              :src="previewState.items[previewState.index]?.filePath"
              :alt="previewState.items[previewState.index]?.description"
              class="max-w-full max-h-full object-contain select-none"
              draggable="false"
            />
          </template>

          <!-- PDF preview -->
          <template v-else-if="isPdf(previewState.items[previewState.index]?.fileType)">
            <iframe
              :src="previewState.items[previewState.index]?.filePath"
              class="w-full h-full border-0"
              title="PDF Preview"
            />
          </template>

          <!-- Other file types -->
          <template v-else>
            <div class="text-center">
              <div class="w-20 h-20 bg-white/10 rounded-2xl flex items-center justify-center mx-auto mb-4">
                <File :size="36" class="text-white/60" />
              </div>
              <p class="text-white font-semibold mb-1">
                {{ fileNameFromPath(previewState.items[previewState.index]?.filePath) }}
              </p>
              <p class="text-gray-400 text-sm mb-4">{{ t.projectWorkspace?.previewUnavailable }}</p>
              <a
                :href="previewState.items[previewState.index]?.filePath"
                target="_blank"
                class="inline-flex items-center gap-2 px-5 py-2.5 bg-white text-gray-900 text-sm font-semibold rounded-lg hover:bg-gray-100 transition"
              >
                <Download :size="16" />
                {{ t.projectWorkspace?.downloadFileLabel }}
              </a>
            </div>
          </template>

          <!-- Right nav -->
          <button
            v-if="previewState.items.length > 1"
            class="absolute right-4 z-10 p-3 bg-black/50 hover:bg-black/80 text-white rounded-full transition"
            @click="nextPreview"
          >
            <ChevronRight :size="22" />
          </button>
        </div>

        <!-- File list strip -->
        <div v-if="previewState.items.length > 1" class="px-6 py-3 bg-black/80 border-t border-white/10 shrink-0">
          <div class="flex gap-2 overflow-x-auto">
            <button
              v-for="(d, di) in previewState.items"
              :key="di"
              class="shrink-0 w-14 h-14 rounded-lg overflow-hidden border-2 transition"
              :class="di === previewState.index ? 'border-brand-gold' : 'border-white/20 hover:border-white/40'"
              @click="previewState.index = di"
            >
              <img v-if="isImage(d.fileType)" :src="d.filePath" class="w-full h-full object-cover" />
              <div v-else class="w-full h-full bg-white/10 flex items-center justify-center">
                <FileText v-if="isPdf(d.fileType)" :size="20" class="text-red-400" />
                <File v-else :size="20" class="text-gray-400" />
              </div>
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Revision Notes Modal -->
    <Teleport to="body">
      <div
        v-if="showRevisionModal !== null"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60"
        @click.self="showRevisionModal = null"
      >
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
          <h3 class="text-base font-bold text-gray-900 mb-1">{{ t.projectWorkspace?.revisionModalTitle }}</h3>
          <p class="text-sm text-gray-500 mb-4">{{ t.projectWorkspace?.revisionModalHint }}</p>
          <textarea
            v-model="revisionNotes[showRevisionModal]"
            :placeholder="t.projectWorkspace?.revisionModalPlaceholder"
            rows="4"
            class="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-amber-200 focus:border-amber-300"
          />
          <p
            v-if="revisionNotes[showRevisionModal] !== undefined && !revisionNotes[showRevisionModal]?.trim()"
            class="text-xs text-red-500 mt-1"
          >
            {{ t.projectWorkspace?.revisionModalRequired }}
          </p>
          <div class="flex gap-2 mt-4">
            <button
              :disabled="!revisionNotes[showRevisionModal]?.trim() || actionLoading === showRevisionModal"
              class="flex-1 px-4 py-2.5 bg-amber-500 text-white text-sm font-semibold rounded-lg hover:bg-amber-600 disabled:opacity-50 disabled:cursor-not-allowed transition"
              @click="submitRevision(sortedPhases.find(p => p.id === showRevisionModal))"
            >
              <span v-if="actionLoading === showRevisionModal">...</span>
              <span v-else>{{ t.projectWorkspace?.revisionModalSubmit }}</span>
            </button>
            <button
              class="px-4 py-2.5 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-100 transition"
              @click="showRevisionModal = null"
            >
              {{ t.projectWorkspace?.revisionModalCancel }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, nextTick } from 'vue'
import { projectTypeLabel } from '@/constants/projectTaxonomy'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  CheckCircle,
  Building2,
  MapPin,
  Tag,
  Layers,
  Lock,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  ThumbsUp,
  AlertTriangle,
  FileText,
  File,
  FileX,
  X,
  Eye,
  Download,
  RotateCcw,
  ShieldCheck,
  Clock
} from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { useBidsStore } from '@/stores/bids'
import { phaseAPI, projectAPI } from '@/services/api'
import ChatPanel from '@/components/chat/ChatPanel.vue'
import { useI18n } from '@/composables/useI18n'

const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const bidsStore = useBidsStore()
const { t, locale } = useI18n()

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
const showRevisionModal = ref(null)
const revisionNotes = reactive({})
const initializingPhases = ref(false)
const showProjectModal = ref(false)
const showApproveConfirm = ref(null)
const previewOverlay = ref(null)

const previewState = reactive({
  open: false,
  items: [],
  index: 0
})

const project = computed(() => projectsStore.currentProject)
const isCompleted = computed(() => project.value?.status === 'COMPLETED')

const coverImage = computed(() => {
  const files = project.value?.files
  if (!files?.length) return null
  return files.find(f => f.fileType?.startsWith('image/'))?.filePath || null
})

const acceptedBid = computed(() => bidsStore.projectBids.find(b => b.status === 'ACCEPTED') || null)
const architectName = computed(() => acceptedBid.value?.architectName || t.value.projectWorkspace?.architectFallback)
const architectInitials = computed(() =>
  architectName.value
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
)

const conversationId = computed(() => acceptedBid.value?.conversationId ?? null)

const sortedPhases = computed(() => [...phases.value].sort((a, b) => a.phaseNumber - b.phaseNumber))
const disbursedCount = computed(() => phases.value.filter(p => p.status === 'DISBURSED').length)
const totalAmount = computed(() => phases.value.reduce((sum, p) => sum + Number(p.amount || 0), 0))
const paidAmount = computed(() =>
  phases.value
    .filter(p => ['APPROVED', 'DISBURSED'].includes(p.status))
    .reduce((sum, p) => sum + Number(p.amount || 0), 0)
)
const remainingAmount = computed(() => totalAmount.value - paidAmount.value)
const progressPercent = computed(() => (totalAmount.value > 0 ? (paidAmount.value / totalAmount.value) * 100 : 0))

const isNotStarted = (phase, index) => {
  if (phase.status !== 'PENDING') return false
  return sortedPhases.value.slice(0, index).some(p => !['APPROVED', 'DISBURSED'].includes(p.status))
}

const revisionsLeft = phase => (phase.maxRevisions != null ? phase.maxRevisions - (phase.revisionsUsed || 0) : Infinity)

const showRevisionBadge = phase => ['IN_PROGRESS', 'DELIVERED'].includes(phase.status) && phase.maxRevisions != null

const groupedDeliverables = phase => {
  if (!phase.deliverables?.length) return []
  const map = {}
  for (const d of phase.deliverables) {
    const r = d.revisionRound ?? 0
    if (!map[r]) map[r] = []
    map[r].push(d)
  }
  return Object.keys(map)
    .map(r => ({ round: Number(r), files: map[r] }))
    .sort((a, b) => a.round - b.round)
}

const statusStyles = {
  NOT_STARTED: {
    bg: 'bg-gray-100',
    text: 'text-gray-500',
    dot: 'bg-gray-400',
    icon: 'bg-gray-100 text-gray-400'
  },
  PENDING: {
    bg: 'bg-amber-50',
    text: 'text-amber-700',
    dot: 'bg-amber-500',
    icon: 'bg-amber-100 text-amber-700'
  },
  BILLED: {
    bg: 'bg-blue-50',
    text: 'text-blue-700',
    dot: 'bg-blue-500',
    icon: 'bg-blue-100 text-blue-700'
  },
  IN_PROGRESS: {
    bg: 'bg-sky-50',
    text: 'text-sky-700',
    dot: 'bg-sky-500',
    icon: 'bg-sky-100 text-sky-700'
  },
  DELIVERED: {
    bg: 'bg-purple-50',
    text: 'text-purple-700',
    dot: 'bg-purple-500',
    icon: 'bg-purple-100 text-purple-700'
  },
  APPROVED: {
    bg: 'bg-green-50',
    text: 'text-green-700',
    dot: 'bg-green-500',
    icon: 'bg-green-100 text-green-700'
  },
  DISBURSED: {
    bg: 'bg-gray-50',
    text: 'text-gray-500',
    dot: 'bg-gray-400',
    icon: 'bg-green-100 text-green-600'
  },
  DISPUTED: {
    bg: 'bg-red-50',
    text: 'text-red-700',
    dot: 'bg-red-500',
    icon: 'bg-red-100 text-red-700'
  }
}

const statusLabels = computed(() => t.value.projectWorkspace?.statusLabels || {})

const phaseStatusConfig = (phase, index) => {
  const key = isNotStarted(phase, index) ? 'NOT_STARTED' : phase.status
  const style = statusStyles[key] || {
    bg: 'bg-gray-50',
    text: 'text-gray-500',
    dot: 'bg-gray-400',
    icon: 'bg-gray-100 text-gray-500'
  }
  return { ...style, label: statusLabels.value[key] || phase.status }
}

const phaseIconClass = (status, index) => {
  const phase = sortedPhases.value[index]
  if (phase && isNotStarted(phase, index)) return 'bg-gray-100 text-gray-400'
  if (status === 'DISBURSED') return 'bg-green-100 text-green-600'
  return statusStyles[status]?.icon || 'bg-gray-100 text-gray-500'
}

const logIconClass = actorType =>
  ({
    CLIENT: 'bg-blue-100 text-blue-700',
    ARCHITECT: 'bg-purple-100 text-purple-700',
    SYSTEM: 'bg-gray-100 text-gray-500',
    XENDIT: 'bg-green-100 text-green-700'
  })[actorType] || 'bg-gray-100 text-gray-500'

const formatLogAction = action => {
  const labels = t.value.projectWorkspace?.logActions || {}
  return (
    labels[action] ||
    action
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase())
  )
}

const phaseFallbackTitle = phase =>
  (t.value.projectWorkspace?.phaseFallback || 'Phase {n}').replace('{n}', phase.phaseNumber)

const isImage = fileType => fileType?.startsWith('image/')
const isPdf = fileType => fileType === 'application/pdf'
const fileNameFromPath = path => path?.split('/').pop() || path

const formatAmount = amount => {
  if (amount == null) return '-'
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(amount)
}

const formatDate = dateStr => {
  if (!dateStr) return ''
  const intlLocale = locale.value === 'en' ? 'en-US' : 'id-ID'
  return new Date(dateStr).toLocaleDateString(intlLocale, { day: 'numeric', month: 'short', year: 'numeric' })
}

const formatDateTime = dateStr => {
  if (!dateStr) return ''
  const intlLocale = locale.value === 'en' ? 'en-US' : 'id-ID'
  return new Date(dateStr).toLocaleString(intlLocale, {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const openPreview = async (phase, startIndex) => {
  previewState.items = phase.deliverables || []
  previewState.index = startIndex
  previewState.open = true
  await nextTick()
  previewOverlay.value?.focus()
}

const closePreview = () => {
  previewState.open = false
}

const nextPreview = () => {
  previewState.index = (previewState.index + 1) % previewState.items.length
}

const prevPreview = () => {
  previewState.index = (previewState.index - 1 + previewState.items.length) % previewState.items.length
}

const loadAll = async () => {
  loading.value = true
  error.value = null
  try {
    await Promise.all([projectsStore.fetchProjectById(projectId), bidsStore.fetchProjectBids(projectId)])
    const res = await phaseAPI.getPhases(projectId)
    phases.value = res.data.data || res.data || []
    const active = sortedPhases.value.find(p => p.status !== 'DISBURSED')
    if (active) {
      expandedPhaseId.value = active.id
      fetchLogs(active.id)
    }
  } catch (err) {
    error.value = err.response?.data?.message || t.value.projectWorkspace?.loadError
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
  Object.keys(phaseLogs).forEach(k => delete phaseLogs[k])
}

const billPhase = async phase => {
  actionLoading.value = phase.id
  try {
    const res = await phaseAPI.billPhase(phase.id)
    const link = res.data.data?.paymentLink || res.data?.paymentLink
    if (link) window.open(link, '_blank')
    await refreshPhases()
    delete phaseLogs[phase.id]
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || t.value.projectWorkspace?.billError)
  } finally {
    actionLoading.value = null
  }
}

const approvePhase = async phaseId => {
  actionLoading.value = phaseId
  try {
    await phaseAPI.approveDeliverable(phaseId)
    showApproveConfirm.value = null
    await refreshPhases()
    delete phaseLogs[phaseId]
    fetchLogs(phaseId)
  } catch (err) {
    alert(err.response?.data?.message || t.value.projectWorkspace?.approveError)
  } finally {
    actionLoading.value = null
  }
}

const doRequestRevision = phase => {
  revisionNotes[phase.id] = ''
  showRevisionModal.value = phase.id
}

const submitRevision = async phase => {
  if (!revisionNotes[phase.id]?.trim()) return
  actionLoading.value = phase.id
  try {
    await phaseAPI.requestRevision(phase.id, { notes: revisionNotes[phase.id] })
    showRevisionModal.value = null
    revisionNotes[phase.id] = ''
    await refreshPhases()
    delete phaseLogs[phase.id]
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || t.value.projectWorkspace?.revisionError)
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
    delete phaseLogs[phase.id]
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || t.value.projectWorkspace?.disputeError)
  } finally {
    actionLoading.value = null
  }
}

const initPhases = async () => {
  initializingPhases.value = true
  try {
    await projectAPI.initializePhases(projectId)
    await refreshPhases()
  } catch (err) {
    alert(err.response?.data?.message || t.value.projectWorkspace?.initError)
  } finally {
    initializingPhases.value = false
  }
}

onMounted(loadAll)
</script>
