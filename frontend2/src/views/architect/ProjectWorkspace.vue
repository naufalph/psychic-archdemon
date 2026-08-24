<template>
  <div class="min-h-screen bg-surface-alt">
    <div v-if="loading" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <div class="w-10 h-10 border-2 border-brand-gold border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p class="text-gray-500">Memuat workspace...</p>
      </div>
    </div>

    <div v-else-if="error" class="flex items-center justify-center h-screen">
      <div class="text-center">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <button class="text-brand-brown hover:underline" @click="loadAll">Coba lagi</button>
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
              <p class="text-xs text-gray-400 uppercase font-bold tracking-wide">Project Workspace</p>
              <h1 class="text-lg font-bold text-black">{{ project?.title || 'Active Project' }}</h1>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span class="text-sm text-gray-500 flex items-center gap-1.5">
              <Layers :size="14" />
              {{ disbursedCount }} / {{ phases.length }} selesai
            </span>
            <span
              v-if="activePhase?.dueDate"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap"
              :class="daysLeft(activePhase.dueDate) < 3 ? 'bg-red-100 text-red-700' : 'bg-amber-50 text-amber-700'"
            >
              <Clock :size="12" />
              {{ t.projectWorkspace?.deadline }}: {{ countdownText(activePhase.dueDate) }}
            </span>
            <span
              v-if="isCompleted"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold bg-green-100 text-green-700 whitespace-nowrap"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-green-600" />
              {{ t.projectWorkspace?.completed }}
            </span>
            <span
              v-else
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold bg-brand-tan text-brand-brown whitespace-nowrap"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-brand-brown" />
              {{ t.projectWorkspace?.active }}
            </span>
            <button
              v-if="isCompleted && !project?.archivedPortoId"
              :disabled="archiving"
              class="px-4 py-1.5 rounded-full text-xs font-bold bg-black text-white hover:bg-black/80 transition disabled:opacity-50"
              @click="archiveToPortfolio"
            >
              {{ archiving ? t.projectWorkspace?.archiving : t.projectWorkspace?.archiveToPortfolio }}
            </button>
            <button
              v-else-if="isCompleted"
              class="px-4 py-1.5 rounded-full text-xs font-bold bg-white border border-gray-300 text-black hover:border-black transition"
              @click="router.push({ name: 'ArchitectPortfolios' })"
            >
              {{ t.projectWorkspace?.viewInPortfolio }}
            </button>
          </div>
        </div>
        <p v-if="archiveError" class="max-w-7xl mx-auto text-xs text-red-600 mt-2">{{ archiveError }}</p>
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
                      <span class="text-xs text-brand-brown font-medium group-hover:underline">Lihat Detail ↗</span>
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
                    <p class="text-xs text-gray-400">Total Nilai Proyek</p>
                    <p class="font-bold text-gray-900">{{ formatAmount(totalAmount) }}</p>
                    <p class="text-xs text-gray-400 mt-0.5">{{ formatAmount(disbursedAmount) }} dicairkan</p>
                  </div>
                </div>
                <div class="space-y-1">
                  <div class="flex justify-between text-xs text-gray-500">
                    <span>{{ Math.round(progressPercent) }}% selesai</span>
                    <span>{{ disbursedCount }}/{{ phases.length }} fase</span>
                  </div>
                  <div class="w-full bg-gray-100 rounded-full h-1.5">
                    <div
                      class="bg-brand-brown h-1.5 rounded-full transition-all duration-500"
                      :style="{ width: progressPercent + '%' }"
                    />
                  </div>
                </div>
              </div>
            </div>
          </button>

          <!-- Phase list -->
          <div class="space-y-3">
            <p class="text-xs font-bold text-gray-400 uppercase tracking-wide px-1">Fase Pembayaran · Payment Phases</p>

            <div v-if="phases.length === 0" class="bg-white rounded-xl border border-gray-200 py-14 text-center">
              <Layers :size="32" class="text-gray-300 mx-auto mb-3" />
              <p class="text-gray-500 font-medium">Belum ada fase</p>
              <p class="text-sm text-gray-400 mt-1">Detail fase akan muncul setelah klien menyiapkannya.</p>
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
                        {{ phase.title || `Fase ${phase.phaseNumber}` }}
                      </p>
                      <div class="flex items-center gap-2 text-xs text-gray-500">
                        <span>{{ formatAmount(phase.amount) }}</span>
                        <span v-if="phase.dueDate">· Tenggat {{ formatDate(phase.dueDate) }}</span>
                        <span
                          v-if="phase.status === 'IN_PROGRESS' && phase.dueDate"
                          class="font-semibold"
                          :class="daysLeft(phase.dueDate) < 3 ? 'text-red-500' : 'text-amber-600'"
                        >
                          · {{ countdownText(phase.dueDate) }}
                        </span>
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
                      Deskripsi · Description
                    </p>
                    <p class="text-sm text-gray-600 leading-relaxed">{{ phase.description }}</p>
                  </div>

                  <!-- Revision info (when applicable) -->
                  <div v-if="showRevisionBadge(phase)" class="px-5 py-3 bg-purple-50 border-b border-purple-100">
                    <div class="flex items-center gap-2">
                      <RotateCcw :size="14" class="text-purple-600" />
                      <span class="text-xs font-semibold text-purple-700">
                        Sisa Revisi: {{ phase.maxRevisions - phase.revisionsUsed }} dari {{ phase.maxRevisions }}
                        <span class="font-normal text-purple-500 ml-1">(Revisions Remaining)</span>
                      </span>
                    </div>
                  </div>

                  <!-- Architect actions -->
                  <div class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">Tindakan · Actions</p>

                    <!-- NOT STARTED -->
                    <div v-if="isNotStarted(phase, index)" class="p-3 bg-gray-50 border border-gray-200 rounded-lg">
                      <p class="text-sm font-semibold text-gray-500">Belum Dimulai · Not Started</p>
                      <p class="text-xs text-gray-400 mt-0.5">
                        Fase ini akan dimulai setelah fase sebelumnya selesai sepenuhnya.
                      </p>
                    </div>

                    <!-- PENDING: waiting for client to bill -->
                    <div
                      v-else-if="phase.status === 'PENDING'"
                      class="p-3 bg-amber-50 border border-amber-200 rounded-lg"
                    >
                      <p class="text-sm font-semibold text-amber-800">
                        Menunggu Pembayaran Klien · Awaiting Client Payment
                      </p>
                      <p class="text-xs text-amber-600 mt-0.5">
                        Klien perlu membuat invoice dan membayar fase ini sebelum Anda dapat memulai pekerjaan.
                      </p>
                    </div>

                    <!-- BILLED: invoice sent, waiting for payment -->
                    <div v-else-if="phase.status === 'BILLED'" class="p-3 bg-blue-50 border border-blue-200 rounded-lg">
                      <p class="text-sm font-semibold text-blue-800">Invoice Terkirim · Invoice Sent</p>
                      <p class="text-xs text-blue-600 mt-0.5">
                        Invoice sudah dikirim ke klien. Anda akan diberitahu setelah pembayaran dikonfirmasi.
                      </p>
                    </div>

                    <!-- IN_PROGRESS: upload deliverable -->
                    <div v-else-if="phase.status === 'IN_PROGRESS'">
                      <div class="p-3 bg-sky-50 border border-sky-200 rounded-lg mb-3">
                        <div class="flex items-center justify-between">
                          <div>
                            <p class="text-sm font-semibold text-sky-800">Fase Aktif · Work Phase Active</p>
                            <p class="text-xs text-sky-600 mt-0.5">
                              Pembayaran dikonfirmasi. Unggah deliverable Anda ketika siap untuk direview klien.
                            </p>
                          </div>
                          <div v-if="phase.dueDate" class="shrink-0 ml-3 text-right">
                            <p
                              class="text-xs font-bold"
                              :class="daysLeft(phase.dueDate) < 3 ? 'text-red-600' : 'text-amber-600'"
                            >
                              {{ countdownText(phase.dueDate) }}
                            </p>
                            <p class="text-xs text-gray-400">{{ t.projectWorkspace?.remaining }}</p>
                          </div>
                        </div>
                      </div>

                      <!-- File upload -->
                      <div class="space-y-3 p-4 bg-gray-50 rounded-xl border border-gray-200">
                        <p class="text-xs font-bold text-gray-500 uppercase tracking-wide">Unggah File · Upload File</p>

                        <div class="flex items-center gap-3">
                          <label
                            class="flex-1 flex items-center gap-2 px-4 py-2.5 border-2 border-dashed border-gray-300 rounded-lg cursor-pointer hover:border-brand-gold hover:bg-brand-tan/20 transition"
                          >
                            <Paperclip :size="16" class="text-gray-400 shrink-0" />
                            <span class="text-sm text-gray-500 truncate">
                              {{ selectedFile[phase.id]?.name || 'Pilih file / Choose file' }}
                            </span>
                            <input
                              type="file"
                              class="hidden"
                              accept="image/*,application/pdf,.dwg,.dxf,.zip,.rar,.doc,.docx,.xls,.xlsx,.ppt,.pptx"
                              @change="e => onFileSelect(phase.id, e)"
                            />
                          </label>
                          <button
                            v-if="selectedFile[phase.id]"
                            class="text-gray-400 hover:text-gray-600 transition"
                            @click="clearFile(phase.id)"
                          >
                            <X :size="16" />
                          </button>
                        </div>

                        <div>
                          <input
                            v-model="deliverableDesc[phase.id]"
                            type="text"
                            placeholder="Deskripsi file (opsional) · File description (optional)"
                            class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-300"
                          />
                        </div>

                        <div class="flex gap-2 pt-1">
                          <button
                            :disabled="!selectedFile[phase.id] || uploadLoading === phase.id"
                            class="px-4 py-2 bg-ink-700 text-white text-sm font-semibold rounded-lg hover:bg-ink-500 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2"
                            @click="uploadFile(phase)"
                          >
                            <span v-if="uploadLoading === phase.id" class="flex items-center gap-2">
                              <div
                                class="w-3.5 h-3.5 border border-white border-t-transparent rounded-full animate-spin"
                              />
                              Mengunggah...
                            </span>
                            <template v-else>
                              <Upload :size="14" />
                              Unggah
                            </template>
                          </button>
                        </div>
                      </div>

                      <!-- Submit for review (if deliverables exist) -->
                      <div
                        v-if="phase.deliverables && phase.deliverables.length > 0"
                        class="mt-3 p-4 bg-amber-50 border border-amber-200 rounded-xl"
                      >
                        <p class="text-xs font-bold text-amber-700 uppercase tracking-wide mb-2">
                          Tandai Selesai · Mark Complete
                        </p>
                        <p class="text-xs text-amber-600 mb-3">
                          Setelah semua file diunggah, kirim fase ini untuk direview oleh klien. Klien akan diberi
                          notifikasi.
                          <span class="block mt-0.5 text-amber-500"
                            >Once all files are uploaded, submit this phase for client review.</span
                          >
                        </p>
                        <button
                          :disabled="actionLoading === phase.id"
                          class="flex items-center gap-2 px-4 py-2.5 bg-amber-600 text-white text-sm font-semibold rounded-lg hover:bg-amber-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                          @click="submitForReview(phase)"
                        >
                          <span v-if="actionLoading === phase.id">Mengirimkan...</span>
                          <template v-else>
                            <Send :size="14" />
                            Kirim untuk Review
                          </template>
                        </button>
                      </div>
                    </div>

                    <!-- DELIVERED: waiting for client review -->
                    <div
                      v-else-if="phase.status === 'DELIVERED'"
                      class="p-3 bg-purple-50 border border-purple-200 rounded-lg"
                    >
                      <p class="text-sm font-semibold text-purple-800">Sedang Direview · Under Client Review</p>
                      <p class="text-xs text-purple-600 mt-0.5">
                        Deliverable Anda sudah dikirim. Menunggu persetujuan atau revisi dari klien.
                      </p>
                    </div>

                    <!-- APPROVED: request payout -->
                    <div v-else-if="phase.status === 'APPROVED'">
                      <div class="p-3 bg-green-50 border border-green-200 rounded-lg mb-3">
                        <p class="text-sm font-semibold text-green-800">Pekerjaan Disetujui! · Work Approved!</p>
                        <p class="text-xs text-green-600 mt-0.5">
                          Klien menyetujui pekerjaan Anda. Ajukan pencairan dana di bawah.
                        </p>
                      </div>

                      <!-- Case 1: Disbursement webhook pending (ACCEPTED/PENDING) -->
                      <div
                        v-if="['ACCEPTED', 'PENDING'].includes(phase.disbursementStatus)"
                        class="p-3 bg-amber-50 border border-amber-200 rounded-lg flex items-center gap-2"
                      >
                        <div
                          class="w-4 h-4 border-2 border-amber-500 border-t-transparent rounded-full animate-spin shrink-0"
                        />
                        <div>
                          <p class="text-sm font-semibold text-amber-800">{{ t.projectWorkspace?.payoutProcessing }}</p>
                          <p class="text-xs text-amber-600 mt-0.5">{{ t.projectWorkspace?.payoutProcessingHint }}</p>
                        </div>
                      </div>

                      <!-- Case 2: Disbursement failed — show banner then button/form -->
                      <template v-else-if="['FAILED', 'REVERSED'].includes(phase.disbursementStatus)">
                        <div class="p-3 bg-red-50 border border-red-200 rounded-lg mb-3">
                          <p class="text-sm font-semibold text-red-800">{{ t.projectWorkspace?.payoutFailed }}</p>
                          <p class="text-xs text-red-600 mt-0.5">{{ t.projectWorkspace?.payoutFailedHint }}</p>
                        </div>
                        <div v-if="!showDisbursementForm[phase.id]">
                          <button
                            class="flex items-center gap-2 px-4 py-2.5 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 transition"
                            @click="showDisbursementForm[phase.id] = true"
                          >
                            <Banknote :size="15" />
                            {{ t.projectWorkspace?.retryPayout }}
                          </button>
                        </div>
                        <div v-else class="space-y-3 p-4 bg-gray-50 rounded-xl border border-gray-200">
                          <p class="text-xs font-bold text-gray-500 uppercase tracking-wide">
                            Detail Pencairan · Payout Details
                          </p>
                          <div>
                            <label class="text-xs font-medium text-gray-600 mb-1 block">Bank / Channel</label>
                            <select
                              v-model="disbursementForm[phase.id].channelCode"
                              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-gray-300"
                            >
                              <option value="">Pilih bank...</option>
                              <option value="ID_BCA">BCA</option>
                              <option value="ID_MANDIRI">Mandiri</option>
                              <option value="ID_BNI">BNI</option>
                              <option value="ID_BRI">BRI</option>
                              <option value="ID_PERMATA">Permata</option>
                              <option value="ID_CIMB">CIMB Niaga</option>
                              <option value="ID_DANAMON">Danamon</option>
                            </select>
                          </div>
                          <div>
                            <label class="text-xs font-medium text-gray-600 mb-1 block"
                              >Nomor Rekening · Account Number</label
                            >
                            <input
                              v-model="disbursementForm[phase.id].accountNumber"
                              type="text"
                              placeholder="cth. 1234567890"
                              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-300"
                            />
                          </div>
                          <div>
                            <label class="text-xs font-medium text-gray-600 mb-1 block"
                              >Nama Pemilik Rekening · Account Holder Name</label
                            >
                            <input
                              v-model="disbursementForm[phase.id].accountHolderName"
                              type="text"
                              placeholder="Nama sesuai buku tabungan"
                              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-300"
                            />
                          </div>
                          <div class="pt-1 p-3 bg-amber-50 border border-amber-200 rounded-lg">
                            <p class="text-xs text-amber-700 font-medium">
                              Jumlah pencairan: {{ formatAmount(phase.amount) }}
                            </p>
                            <p class="text-xs text-amber-600 mt-0.5">
                              Transfer akan diproses melalui Xendit. Harap periksa detail rekening dengan cermat.
                            </p>
                          </div>
                          <div class="flex gap-2">
                            <button
                              :disabled="!isDisbursementFormValid(phase.id) || actionLoading === phase.id"
                              class="px-4 py-2 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2"
                              @click="pendingDisbursement = phase"
                            >
                              <span v-if="actionLoading === phase.id">Memproses...</span>
                              <template v-else><Banknote :size="14" />Konfirmasi Pencairan</template>
                            </button>
                            <button
                              class="px-4 py-2 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-200 transition"
                              @click="showDisbursementForm[phase.id] = false"
                            >
                              Batal
                            </button>
                          </div>
                        </div>
                      </template>

                      <!-- Case 3: No disbursement yet — show button or form -->
                      <template v-else>
                        <div v-if="!showDisbursementForm[phase.id]">
                          <button
                            class="flex items-center gap-2 px-4 py-2.5 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 transition"
                            @click="showDisbursementForm[phase.id] = true"
                          >
                            <Banknote :size="15" />
                            {{ t.projectWorkspace?.requestPayout }}
                          </button>
                        </div>
                        <div v-else class="space-y-3 p-4 bg-gray-50 rounded-xl border border-gray-200">
                          <p class="text-xs font-bold text-gray-500 uppercase tracking-wide">
                            Detail Pencairan · Payout Details
                          </p>
                          <div>
                            <label class="text-xs font-medium text-gray-600 mb-1 block">Bank / Channel</label>
                            <select
                              v-model="disbursementForm[phase.id].channelCode"
                              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-gray-300"
                            >
                              <option value="">Pilih bank...</option>
                              <option value="ID_BCA">BCA</option>
                              <option value="ID_MANDIRI">Mandiri</option>
                              <option value="ID_BNI">BNI</option>
                              <option value="ID_BRI">BRI</option>
                              <option value="ID_PERMATA">Permata</option>
                              <option value="ID_CIMB">CIMB Niaga</option>
                              <option value="ID_DANAMON">Danamon</option>
                            </select>
                          </div>
                          <div>
                            <label class="text-xs font-medium text-gray-600 mb-1 block"
                              >Nomor Rekening · Account Number</label
                            >
                            <input
                              v-model="disbursementForm[phase.id].accountNumber"
                              type="text"
                              placeholder="cth. 1234567890"
                              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-300"
                            />
                          </div>
                          <div>
                            <label class="text-xs font-medium text-gray-600 mb-1 block"
                              >Nama Pemilik Rekening · Account Holder Name</label
                            >
                            <input
                              v-model="disbursementForm[phase.id].accountHolderName"
                              type="text"
                              placeholder="Nama sesuai buku tabungan"
                              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-300"
                            />
                          </div>
                          <div class="pt-1 p-3 bg-amber-50 border border-amber-200 rounded-lg">
                            <p class="text-xs text-amber-700 font-medium">
                              Jumlah pencairan: {{ formatAmount(phase.amount) }}
                            </p>
                            <p class="text-xs text-amber-600 mt-0.5">
                              Transfer akan diproses melalui Xendit. Harap periksa detail rekening dengan cermat.
                            </p>
                          </div>
                          <div class="flex gap-2">
                            <button
                              :disabled="!isDisbursementFormValid(phase.id) || actionLoading === phase.id"
                              class="px-4 py-2 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2"
                              @click="pendingDisbursement = phase"
                            >
                              <span v-if="actionLoading === phase.id">Memproses...</span>
                              <template v-else>
                                <Banknote :size="14" />
                                Konfirmasi Pencairan
                              </template>
                            </button>
                            <button
                              class="px-4 py-2 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-200 transition"
                              @click="showDisbursementForm[phase.id] = false"
                            >
                              Batal
                            </button>
                          </div>
                        </div>
                      </template>
                    </div>

                    <!-- DISBURSED: complete -->
                    <div
                      v-else-if="phase.status === 'DISBURSED'"
                      class="flex items-center gap-2 p-3 bg-gray-50 border border-gray-200 rounded-lg"
                    >
                      <CheckCircle :size="16" class="text-green-500 shrink-0" />
                      <p class="text-sm text-gray-600 font-medium">
                        Dana berhasil dicairkan · Payout disbursed successfully.
                      </p>
                    </div>

                    <!-- DISPUTED -->
                    <div v-else-if="phase.status === 'DISPUTED'" class="p-3 bg-red-50 border border-red-200 rounded-lg">
                      <p class="text-sm font-semibold text-red-800">
                        Klien Mengajukan Sengketa · Client Raised a Dispute
                      </p>
                      <p class="text-xs text-red-600 mt-0.5">
                        Klien mengajukan sengketa untuk deliverable ini. Tim dukungan kami akan menghubungi kedua pihak.
                      </p>
                    </div>
                  </div>

                  <!-- Deliverables -->
                  <div class="px-5 py-4">
                    <div class="flex items-center justify-between mb-3">
                      <p class="text-xs font-bold text-gray-400 uppercase tracking-wide">Deliverable</p>
                      <span class="text-xs text-gray-400">{{ phase.deliverables?.length || 0 }} file</span>
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
                          <a
                            v-for="d in group.files"
                            :key="d.id"
                            :href="d.filePath"
                            target="_blank"
                            class="group border border-gray-200 rounded-lg overflow-hidden hover:border-brand-gold transition block"
                          >
                            <template v-if="isImage(d.fileType)">
                              <div class="aspect-video bg-gray-100 overflow-hidden">
                                <img
                                  :src="d.filePath"
                                  :alt="d.description || 'Deliverable'"
                                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
                                />
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
                          </a>
                        </div>
                      </div>
                    </div>
                    <div v-else class="py-8 text-center border-2 border-dashed border-gray-200 rounded-xl">
                      <FileX :size="24" class="text-gray-300 mx-auto mb-2" />
                      <p class="text-xs text-gray-400">Belum ada deliverable yang diunggah</p>
                    </div>
                  </div>

                  <!-- Audit log -->
                  <div class="px-5 py-4">
                    <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">
                      Log Aktivitas · Activity Log
                    </p>
                    <div v-if="logsLoading[phase.id]" class="flex items-center gap-2 py-4">
                      <div class="w-4 h-4 border border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <p class="text-xs text-gray-400">Memuat aktivitas...</p>
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
                          <p
                            v-if="logReasonText(log)"
                            class="text-xs text-gray-700 mt-1 p-2 bg-gray-50 rounded-lg border border-gray-100"
                          >
                            "{{ logReasonText(log) }}"
                          </p>
                          <p class="text-xs text-gray-400 mt-0.5">{{ formatDateTime(log.createdAt) }}</p>
                        </div>
                      </div>
                    </div>
                    <div v-else class="py-4 text-center">
                      <p class="text-xs text-gray-400">Belum ada aktivitas tercatat</p>
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
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2.5">Chat Proyek · Project Chat</p>
              <div class="flex items-center gap-2.5">
                <div
                  class="w-8 h-8 rounded-full bg-brand-tan text-brand-brown flex items-center justify-center text-xs font-bold shrink-0"
                >
                  {{ clientInitials }}
                </div>
                <div>
                  <p class="text-sm font-semibold text-gray-900">{{ clientName }}</p>
                  <span class="inline-flex items-center gap-1 text-xs text-green-600">
                    <span class="w-1.5 h-1.5 rounded-full bg-green-500" />
                    Aktif di proyek
                  </span>
                </div>
              </div>
            </div>
            <div class="flex-1 min-h-0 flex flex-col">
              <ChatPanel v-if="conversationId" :conversation-id="conversationId" class="flex-1 min-h-0" />
              <div v-else class="flex-1 flex items-center justify-center text-center px-4">
                <p class="text-sm text-gray-400">Chat tersedia setelah kedua pihak mengkonfirmasi proyek.</p>
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
          <!-- Modal header -->
          <div class="bg-ink-700 px-6 py-5 flex items-start justify-between">
            <div>
              <p class="text-xs text-brand-gold uppercase font-bold tracking-widest mb-1">
                Detail Proyek · Project Details
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

          <!-- Modal body -->
          <div class="p-6 space-y-6 max-h-[75vh] overflow-y-auto">
            <!-- Project cover image -->
            <div v-if="coverImage" class="w-full h-48 rounded-xl overflow-hidden bg-gray-100">
              <img :src="coverImage" :alt="project?.title" class="w-full h-full object-cover" />
            </div>

            <!-- Project info grid -->
            <div>
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">
                Informasi Proyek · Project Information
              </p>
              <div class="grid grid-cols-2 gap-3">
                <div v-if="project?.location" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">Lokasi · Location</p>
                  <p class="text-sm font-semibold text-gray-800">{{ project.location }}</p>
                </div>
                <div v-if="project?.projectCategory" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">Kategori · Category</p>
                  <p class="text-sm font-semibold text-gray-800">{{ project.projectCategory }}</p>
                </div>
                <div v-if="project?.buildingFunction" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">Fungsi Bangunan · Building Function</p>
                  <p class="text-sm font-semibold text-gray-800">{{ projectTypeLabel(project, locale) }}</p>
                </div>
                <div v-if="project?.estimatedBuildArea" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">Luas Lahan · Lot Size</p>
                  <p class="text-sm font-semibold text-gray-800">{{ project.estimatedBuildArea }} m²</p>
                </div>
                <div v-if="project?.numberOfFloors" class="bg-gray-50 rounded-lg px-4 py-3">
                  <p class="text-xs text-gray-400 mb-0.5">Jumlah Lantai · Floors</p>
                  <p class="text-sm font-semibold text-gray-800">{{ project.numberOfFloors }}</p>
                </div>
                <div
                  v-if="project?.designBudgetMin || project?.designBudgetMax"
                  class="bg-gray-50 rounded-lg px-4 py-3"
                >
                  <p class="text-xs text-gray-400 mb-0.5">Anggaran Desain · Design Budget</p>
                  <p class="text-sm font-semibold text-gray-800">
                    {{ formatAmount(project.designBudgetMin) }} – {{ formatAmount(project.designBudgetMax) }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Scope of Work -->
            <div v-if="project?.scopeOfWork">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">
                Lingkup Pekerjaan · Scope of Work
              </p>
              <p class="text-sm text-gray-700 leading-relaxed bg-gray-50 rounded-lg p-4">{{ project.scopeOfWork }}</p>
            </div>

            <!-- Deliverables list from project -->
            <div v-if="project?.deliverables && project.deliverables.length > 0">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">
                Deliverable Proyek · Project Deliverables
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

            <!-- Divider -->
            <div class="border-t border-gray-100" />

            <!-- Winning bid section -->
            <div v-if="myBid">
              <p class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-3">Penawaran Menang · Winning Bid</p>
              <div class="bg-brand-tan/40 border border-brand-gold/30 rounded-xl p-4 space-y-3">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <div
                      class="w-10 h-10 rounded-full bg-brand-brown text-white flex items-center justify-center text-sm font-bold shrink-0"
                    >
                      {{
                        (myBid.architectName || 'A')
                          .split(' ')
                          .map(w => w[0])
                          .join('')
                          .slice(0, 2)
                          .toUpperCase()
                      }}
                    </div>
                    <div>
                      <p class="font-semibold text-gray-900 text-sm">{{ myBid.architectName }}</p>
                      <p v-if="myBid.architectCompany" class="text-xs text-gray-500">{{ myBid.architectCompany }}</p>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-xs text-gray-400">Nilai Penawaran · Bid Amount</p>
                    <p class="font-bold text-brand-brown">{{ formatAmount(myBid.bidAmount) }}</p>
                  </div>
                </div>

                <div v-if="myBid.proposedTimelineDays" class="flex items-center gap-2 text-xs text-gray-600">
                  <Clock :size="13" />
                  <span>{{ myBid.proposedTimelineDays }} hari pengerjaan · days of work</span>
                </div>

                <div v-if="myBid.proposal" class="pt-2 border-t border-brand-gold/20">
                  <p class="text-xs font-semibold text-gray-500 mb-1">Proposal</p>
                  <p class="text-sm text-gray-700 leading-relaxed line-clamp-4">{{ myBid.proposal }}</p>
                </div>

                <!-- Bid phases -->
                <div
                  v-if="myBid.details?.phases && myBid.details.phases.length > 0"
                  class="pt-2 border-t border-brand-gold/20"
                >
                  <p class="text-xs font-semibold text-gray-500 mb-2">Rencana Fase · Phase Plan</p>
                  <div class="space-y-2">
                    <div
                      v-for="(bp, i) in myBid.details.phases"
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
                            {{ bp.estimatedDays }} hari · days
                          </p>
                          <p v-if="bp.revisionRounds" class="text-xs text-gray-400 mt-0.5">
                            {{ bp.revisionRounds }}x revisi
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
              Tutup · Close
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Disbursement Confirmation Modal -->
    <Teleport to="body">
      <div
        v-if="pendingDisbursement"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm px-4"
        @click.self="pendingDisbursement = null"
      >
        <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl overflow-hidden">
          <div class="bg-green-600 px-6 py-5">
            <p class="text-xs text-green-100 uppercase font-bold tracking-widest mb-1">
              Konfirmasi Pencairan · Confirm Payout
            </p>
            <h2 class="text-lg font-bold text-white leading-tight">
              {{ pendingDisbursement.title || `Fase ${pendingDisbursement.phaseNumber}` }}
            </h2>
          </div>
          <div class="p-6 space-y-3">
            <div class="p-3 bg-amber-50 border border-amber-200 rounded-lg">
              <p class="text-xs text-amber-700 font-medium">
                Periksa kembali detail berikut sebelum dana dicairkan melalui Xendit. Tindakan ini tidak dapat
                dibatalkan.
              </p>
            </div>
            <div class="divide-y divide-gray-100 border border-gray-200 rounded-lg overflow-hidden">
              <div class="flex justify-between px-4 py-2.5 text-sm">
                <span class="text-gray-500">Jumlah · Amount</span>
                <span class="font-semibold text-gray-900">{{ formatAmount(pendingDisbursement.amount) }}</span>
              </div>
              <div class="flex justify-between px-4 py-2.5 text-sm">
                <span class="text-gray-500">Bank</span>
                <span class="font-semibold text-gray-900">{{
                  bankLabel(disbursementForm[pendingDisbursement.id]?.channelCode)
                }}</span>
              </div>
              <div class="flex justify-between px-4 py-2.5 text-sm">
                <span class="text-gray-500">Nomor Rekening · Account No.</span>
                <span class="font-semibold text-gray-900">{{
                  disbursementForm[pendingDisbursement.id]?.accountNumber
                }}</span>
              </div>
              <div class="flex justify-between px-4 py-2.5 text-sm">
                <span class="text-gray-500">Nama Pemilik · Holder Name</span>
                <span class="font-semibold text-gray-900">{{
                  disbursementForm[pendingDisbursement.id]?.accountHolderName
                }}</span>
              </div>
            </div>
          </div>
          <div class="px-6 py-4 border-t border-gray-100 flex justify-end gap-2">
            <button
              class="px-4 py-2 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-100 transition"
              @click="pendingDisbursement = null"
            >
              Batal
            </button>
            <button
              :disabled="actionLoading === pendingDisbursement.id"
              class="px-4 py-2 bg-green-600 text-white text-sm font-semibold rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2"
              @click="confirmDisbursement"
            >
              <span v-if="actionLoading === pendingDisbursement.id">Memproses...</span>
              <template v-else><Banknote :size="14" />Ya, Cairkan Dana Sekarang</template>
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive } from 'vue'
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
  FileText,
  File,
  FileX,
  Upload,
  Banknote,
  X,
  Paperclip,
  Send,
  RotateCcw,
  Clock
} from 'lucide-vue-next'
import { useProjectsStore } from '@/stores/projects'
import { usePortfoliosStore } from '@/stores/portfolios'
import { phaseAPI, chatAPI, bidAPI } from '@/services/api'
import ChatPanel from '@/components/chat/ChatPanel.vue'
import { useI18n } from '@/composables/useI18n'

const route = useRoute()
const router = useRouter()
const projectsStore = useProjectsStore()
const portfoliosStore = usePortfoliosStore()
const { t, getT, locale } = useI18n()

const projectId = route.params.id

const phases = ref([])
const loading = ref(true)
const error = ref(null)
const expandedPhaseId = ref(null)
const phaseLogs = reactive({})
const logsLoading = reactive({})
const actionLoading = ref(null)
const uploadLoading = ref(null)
const showDisbursementForm = reactive({})
const disbursementForm = reactive({})
const selectedFile = reactive({})
const deliverableDesc = reactive({})
const conversationId = ref(null)
const showProjectModal = ref(false)
const myBid = ref(null)
const archiving = ref(false)
const archiveError = ref(null)
let countdownTimer = null

const project = computed(() => projectsStore.currentProject)
const isCompleted = computed(() => project.value?.status === 'COMPLETED')

const archiveToPortfolio = async () => {
  archiving.value = true
  archiveError.value = null
  try {
    const newPortfolio = await portfoliosStore.createPortfolioFromProject(projectId)
    if (projectsStore.currentProject) {
      projectsStore.currentProject.archivedPortoId = newPortfolio.id
    }
  } catch (err) {
    archiveError.value = err.response?.data?.message || getT('projectWorkspace.archiveError')
  } finally {
    archiving.value = false
  }
}

const coverImage = computed(() => {
  const files = project.value?.files
  if (!files?.length) return null
  return files.find(f => f.fileType?.startsWith('image/'))?.filePath || null
})

const clientName = computed(() => project.value?.clientName || 'Klien')
const clientInitials = computed(() =>
  clientName.value
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
)

const sortedPhases = computed(() => [...phases.value].sort((a, b) => a.phaseNumber - b.phaseNumber))
const disbursedCount = computed(() => phases.value.filter(p => p.status === 'DISBURSED').length)
const activePhase = computed(() => sortedPhases.value.find(p => p.status === 'IN_PROGRESS') || null)
const totalAmount = computed(() => phases.value.reduce((sum, p) => sum + Number(p.amount || 0), 0))
const disbursedAmount = computed(() =>
  phases.value.filter(p => p.status === 'DISBURSED').reduce((sum, p) => sum + Number(p.amount || 0), 0)
)
const progressPercent = computed(() => (totalAmount.value > 0 ? (disbursedAmount.value / totalAmount.value) * 100 : 0))

const isNotStarted = (phase, index) => {
  if (phase.status !== 'PENDING') return false
  return sortedPhases.value.slice(0, index).some(p => !['APPROVED', 'DISBURSED'].includes(p.status))
}

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

const statusConfig = {
  NOT_STARTED: {
    label: 'Belum Dimulai',
    bg: 'bg-gray-100',
    text: 'text-gray-500',
    dot: 'bg-gray-400',
    icon: 'bg-gray-100 text-gray-400'
  },
  PENDING: {
    label: 'Menunggu',
    bg: 'bg-amber-50',
    text: 'text-amber-700',
    dot: 'bg-amber-500',
    icon: 'bg-amber-100 text-amber-700'
  },
  BILLED: {
    label: 'Ditagihkan',
    bg: 'bg-blue-50',
    text: 'text-blue-700',
    dot: 'bg-blue-500',
    icon: 'bg-blue-100 text-blue-700'
  },
  IN_PROGRESS: {
    label: 'Sedang Berjalan',
    bg: 'bg-sky-50',
    text: 'text-sky-700',
    dot: 'bg-sky-500',
    icon: 'bg-sky-100 text-sky-700'
  },
  DELIVERED: {
    label: 'Dikirimkan',
    bg: 'bg-purple-50',
    text: 'text-purple-700',
    dot: 'bg-purple-500',
    icon: 'bg-purple-100 text-purple-700'
  },
  APPROVED: {
    label: 'Disetujui',
    bg: 'bg-green-50',
    text: 'text-green-700',
    dot: 'bg-green-500',
    icon: 'bg-green-100 text-green-700'
  },
  DISBURSED: {
    label: 'Dicairkan',
    bg: 'bg-gray-50',
    text: 'text-gray-500',
    dot: 'bg-gray-400',
    icon: 'bg-green-100 text-green-600'
  },
  DISPUTED: {
    label: 'Disengketakan',
    bg: 'bg-red-50',
    text: 'text-red-700',
    dot: 'bg-red-500',
    icon: 'bg-red-100 text-red-700'
  }
}

const phaseStatusConfig = (phase, index) => {
  if (isNotStarted(phase, index)) return statusConfig.NOT_STARTED
  return (
    statusConfig[phase.status] || {
      label: phase.status,
      bg: 'bg-gray-50',
      text: 'text-gray-500',
      dot: 'bg-gray-400',
      icon: 'bg-gray-100 text-gray-500'
    }
  )
}

const phaseIconClass = (status, index) => {
  const phase = sortedPhases.value[index]
  if (phase && isNotStarted(phase, index)) return 'bg-gray-100 text-gray-400'
  if (status === 'DISBURSED') return 'bg-green-100 text-green-600'
  return statusConfig[status]?.icon || 'bg-gray-100 text-gray-500'
}

const logIconClass = actorType =>
  ({
    CLIENT: 'bg-blue-100 text-blue-700',
    ARCHITECT: 'bg-purple-100 text-purple-700',
    SYSTEM: 'bg-gray-100 text-gray-500',
    XENDIT: 'bg-green-100 text-green-700'
  })[actorType] || 'bg-gray-100 text-gray-500'

const logReasonText = log => log.metadata?.reason || log.metadata?.notes || null

const formatLogAction = action => {
  const labels = {
    PHASE_CREATED: 'Fase dibuat',
    PHASE_BILLED: 'Invoice dibuat',
    PAYMENT_RECEIVED: 'Pembayaran diterima',
    DELIVERABLE_UPLOADED: 'File diunggah',
    PHASE_SUBMITTED_FOR_REVIEW: 'Fase dikirim untuk review',
    DELIVERABLE_APPROVED: 'Deliverable disetujui klien',
    REVISION_REQUESTED: 'Revisi diminta klien',
    DELIVERABLE_DISPUTED: 'Sengketa diajukan klien',
    PAYOUT_INITIATED: 'Pencairan diajukan',
    PAYOUT_COMPLETED: 'Pencairan berhasil',
    PAYOUT_FAILED: 'Pencairan gagal'
  }
  return (
    labels[action] ||
    action
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase())
  )
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
  return new Date(dateStr).toLocaleString('id-ID', {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const daysLeft = dueDate => {
  if (!dueDate) return Infinity
  const diff = new Date(dueDate) - Date.now()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

const countdownText = dueDate => {
  const d = daysLeft(dueDate)
  if (d < 0) return `${Math.abs(d)} hari terlambat`
  if (d === 0) return 'Tenggat hari ini!'
  if (d === 1) return '1 hari tersisa'
  return `${d} hari tersisa`
}

const isDisbursementFormValid = phaseId => {
  const f = disbursementForm[phaseId]
  return f?.channelCode && f?.accountNumber && f?.accountHolderName
}

const initDisbursementForm = phaseId => {
  if (!disbursementForm[phaseId]) {
    disbursementForm[phaseId] = { channelCode: '', accountNumber: '', accountHolderName: '' }
  }
}

const onFileSelect = (phaseId, event) => {
  selectedFile[phaseId] = event.target.files?.[0] || null
}

const clearFile = phaseId => {
  selectedFile[phaseId] = null
}

const loadAll = async () => {
  loading.value = true
  error.value = null
  try {
    await projectsStore.fetchProjectForArchitect(projectId)
    const [phasesRes, convsRes, bidsRes] = await Promise.all([
      phaseAPI.getPhases(projectId),
      chatAPI.getMyConversations(),
      bidAPI.getMyBids().catch(() => ({ data: { data: [] } }))
    ])
    phases.value = phasesRes.data.data || phasesRes.data || []
    const conversations = convsRes.data.data || convsRes.data || []
    const conv = conversations.find(c => String(c.projectId) === String(projectId))
    if (conv) conversationId.value = conv.id

    const bids = bidsRes.data.data || bidsRes.data || []
    myBid.value = bids.find(b => String(b.projectId) === String(projectId) && b.status === 'ACCEPTED') || null

    const active = sortedPhases.value.find(p => p.status !== 'DISBURSED')
    if (active) {
      expandedPhaseId.value = active.id
      fetchLogs(active.id)
      initDisbursementForm(active.id)
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Gagal memuat workspace'
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
  initDisbursementForm(phase.id)
  fetchLogs(phase.id)
}

const refreshPhases = async () => {
  const res = await phaseAPI.getPhases(projectId)
  phases.value = res.data.data || res.data || []
  Object.keys(phaseLogs).forEach(k => delete phaseLogs[k])
}

const uploadFile = async phase => {
  const file = selectedFile[phase.id]
  if (!file) return
  uploadLoading.value = phase.id
  try {
    const formData = new FormData()
    formData.append('file', file)
    if (deliverableDesc[phase.id]) formData.append('description', deliverableDesc[phase.id])
    await phaseAPI.uploadDeliverableFile(phase.id, formData)
    selectedFile[phase.id] = null
    deliverableDesc[phase.id] = ''
    await refreshPhases()
    delete phaseLogs[phase.id]
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || 'Gagal mengunggah file')
  } finally {
    uploadLoading.value = null
  }
}

const submitForReview = async phase => {
  actionLoading.value = phase.id
  try {
    await phaseAPI.submitForReview(phase.id)
    await refreshPhases()
    delete phaseLogs[phase.id]
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || 'Gagal mengirim untuk review')
  } finally {
    actionLoading.value = null
  }
}

const requestPayout = async phase => {
  const form = disbursementForm[phase.id]
  if (!isDisbursementFormValid(phase.id)) return
  actionLoading.value = phase.id
  try {
    await phaseAPI.disburse(phase.id, {
      channelCode: form.channelCode,
      accountNumber: form.accountNumber,
      accountHolderName: form.accountHolderName
    })
    showDisbursementForm[phase.id] = false
    disbursementForm[phase.id] = { channelCode: '', accountNumber: '', accountHolderName: '' }
    await refreshPhases()
    delete phaseLogs[phase.id]
    fetchLogs(phase.id)
  } catch (err) {
    alert(err.response?.data?.message || 'Gagal mengajukan pencairan')
  } finally {
    actionLoading.value = null
  }
}

const BANK_LABELS = {
  ID_BCA: 'BCA',
  ID_MANDIRI: 'Mandiri',
  ID_BNI: 'BNI',
  ID_BRI: 'BRI',
  ID_PERMATA: 'Permata',
  ID_CIMB: 'CIMB Niaga',
  ID_DANAMON: 'Danamon'
}
const bankLabel = code => BANK_LABELS[code] || code || '-'

const pendingDisbursement = ref(null)
const confirmDisbursement = async () => {
  if (!pendingDisbursement.value) return
  const phase = pendingDisbursement.value
  await requestPayout(phase)
  pendingDisbursement.value = null
}

onMounted(() => {
  loadAll()
  countdownTimer = setInterval(() => {}, 60000)
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>
