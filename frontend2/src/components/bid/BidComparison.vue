<template>
  <div class="space-y-6">
    <div class="grid grid-cols-2 gap-4">
      <div
        v-for="(bid, idx) in [bidA, bidB]"
        :key="bid.id"
        class="group relative bg-white rounded-2xl border border-gray-200 p-6 shadow-soft text-center transition-all duration-300 ease-out hover:-translate-y-0.5 hover:border-brand-brown/40 has-[button:focus-visible]:-translate-y-0.5 has-[button:focus-visible]:border-brand-brown/40 motion-reduce:transition-none motion-reduce:hover:translate-y-0 motion-reduce:has-[button:focus-visible]:translate-y-0"
      >
        <p class="text-xs text-gray-500 uppercase tracking-widest mb-1">
          {{ idx === 0 ? t.bidComparison.bidA : t.bidComparison.bidB }}
        </p>
        <h3 class="text-xl font-bold text-black mb-1">{{ bid.architectName || 'Architect' }}</h3>
        <p v-if="bid.architectCompany" class="text-sm text-gray-500 mb-4">{{ bid.architectCompany }}</p>

        <div v-if="bid.status === 'PENDING'" class="relative inline-block">
          <span
            aria-hidden="true"
            class="pointer-events-none absolute -inset-5 rounded-full opacity-0 blur-xl bg-[radial-gradient(circle,rgba(124,71,40,0.35)_0%,rgba(124,71,40,0)_70%)] transition-opacity duration-500 ease-out group-hover:opacity-100 group-has-[button:focus-visible]:opacity-100"
          />
          <button
            class="appoint-lead-btn relative inline-flex items-center gap-1.5 bg-brand-brown text-white px-5 py-2 rounded-full text-sm font-bold transition-all duration-300 ease-out hover:bg-brand-brown-dark hover:-translate-y-px hover:shadow-[0_0_0_4px_rgba(124,71,40,0.18),0_6px_20px_-4px_rgba(124,71,40,0.45)] focus-visible:bg-brand-brown-dark focus-visible:-translate-y-px focus-visible:shadow-[0_0_0_4px_rgba(124,71,40,0.18),0_6px_20px_-4px_rgba(124,71,40,0.45)] focus-visible:outline-none active:scale-[0.98] motion-reduce:transition-none motion-reduce:hover:translate-y-0 motion-reduce:focus-visible:translate-y-0 motion-reduce:active:scale-100"
            @click="pendingAppointBid = bid"
          >
            <Crown :size="14" aria-hidden="true" />
            {{ t.bidComparison.appointLead }}
          </button>
        </div>

        <span v-else-if="bid.status === 'ACCEPTED'" class="text-green-600 font-bold text-sm">{{
          t.bidComparison.appointed
        }}</span>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft">
      <div class="flex items-center justify-between mb-4">
        <div>
          <p class="text-xs font-bold text-gray-500 uppercase tracking-widest">
            {{ t.bidComparison.visualOverlay }}
          </p>
          <p class="text-xs text-gray-400 mt-0.5">{{ t.bidComparison.sideBySize }}</p>
        </div>
        <div class="flex gap-2">
          <button
            v-for="type in IMAGE_TYPES"
            :key="type"
            :class="[
              'px-4 py-1.5 rounded-full text-xs font-bold transition',
              activeImageType === type ? 'bg-brand-brown text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
            @click="switchType(type)"
          >
            {{ imageTypeLabel[type] }}
          </button>
        </div>
      </div>

      <div
        ref="sliderContainerRef"
        class="relative aspect-video rounded-2xl overflow-hidden select-none bg-gray-100"
        @mousemove="onMove"
        @mouseup="stopDrag"
        @mouseleave="stopDrag"
        @touchmove.prevent="onTouchMove"
        @touchend="stopDrag"
      >
        <div v-if="imageAUrl" class="absolute inset-0" :style="{ clipPath: `inset(0 ${100 - sliderPosition}% 0 0)` }">
          <img :src="imageAUrl" class="w-full h-full object-cover" :alt="bidA.architectName" />
        </div>

        <div v-if="imageBUrl" class="absolute inset-0" :style="{ clipPath: `inset(0 0 0 ${sliderPosition}%)` }">
          <img :src="imageBUrl" class="w-full h-full object-cover" :alt="bidB.architectName" />
        </div>

        <div
          class="absolute top-0 bottom-0 w-0.5 bg-white/80 pointer-events-none"
          :style="{ left: sliderPosition + '%' }"
        />

        <div
          class="absolute top-1/2 -translate-y-1/2 -translate-x-1/2 w-10 h-10 bg-white rounded-full shadow-lg flex items-center justify-center cursor-ew-resize z-10"
          :style="{ left: sliderPosition + '%' }"
          @mousedown.stop="startDrag"
          @touchstart.prevent.stop="startDrag"
        >
          <ChevronLeft :size="14" class="text-gray-600" />
          <ChevronRight :size="14" class="text-gray-600" />
        </div>

        <div
          class="absolute bottom-3 left-3 bg-black/50 backdrop-blur-sm text-white text-xs font-bold px-3 py-1.5 rounded-full uppercase tracking-wider"
        >
          {{ bidA.architectName || 'Bid A' }}
        </div>
        <div
          class="absolute bottom-3 right-3 bg-black/50 backdrop-blur-sm text-white text-xs font-bold px-3 py-1.5 rounded-full uppercase tracking-wider"
        >
          {{ bidB.architectName || 'Bid B' }}
        </div>

        <div
          class="absolute top-3 left-1/2 -translate-x-1/2 bg-black/40 backdrop-blur-sm text-white text-xs font-bold px-4 py-1.5 rounded-full flex items-center gap-2 whitespace-nowrap"
        >
          <span class="text-gray-300">{{ t.bidComparison.reviewing }}</span>
          <span>{{ imageTypeLabel[activeImageType] }}</span>
        </div>
      </div>

      <div v-if="descriptionA || descriptionB" class="grid grid-cols-2 gap-4 mt-4">
        <p class="text-xs text-gray-600 whitespace-pre-line">{{ descriptionA }}</p>
        <p class="text-xs text-gray-600 whitespace-pre-line">{{ descriptionB }}</p>
      </div>

      <div class="flex justify-between items-center mt-3 min-h-[28px]">
        <div v-if="imageAImages.length > 1" class="flex items-center gap-2">
          <button
            :disabled="imageIndexA === 0"
            :title="t.bidComparison.prevImage"
            class="w-6 h-6 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-30 transition"
            @click="imageIndexA = Math.max(0, imageIndexA - 1)"
          >
            <ChevronLeft :size="12" />
          </button>
          <span class="text-xs text-gray-400">
            {{
              t.bidComparison.imageOf.replace('{current}', imageIndexA + 1).replace('{total}', imageAImages.length)
            }}
          </span>
          <button
            :disabled="imageIndexA === imageAImages.length - 1"
            :title="t.bidComparison.nextImage"
            class="w-6 h-6 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-30 transition"
            @click="imageIndexA = Math.min(imageAImages.length - 1, imageIndexA + 1)"
          >
            <ChevronRight :size="12" />
          </button>
        </div>
        <div v-else />

        <p class="text-xs text-gray-400 uppercase tracking-widest">
          {{ t.bidComparison.adjustSlider }}
        </p>

        <div v-if="imageBImages.length > 1" class="flex items-center gap-2">
          <button
            :disabled="imageIndexB === 0"
            :title="t.bidComparison.prevImage"
            class="w-6 h-6 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-30 transition"
            @click="imageIndexB = Math.max(0, imageIndexB - 1)"
          >
            <ChevronLeft :size="12" />
          </button>
          <span class="text-xs text-gray-400">
            {{
              t.bidComparison.imageOf.replace('{current}', imageIndexB + 1).replace('{total}', imageBImages.length)
            }}
          </span>
          <button
            :disabled="imageIndexB === imageBImages.length - 1"
            :title="t.bidComparison.nextImage"
            class="w-6 h-6 flex items-center justify-center rounded-full bg-gray-100 hover:bg-gray-200 disabled:opacity-30 transition"
            @click="imageIndexB = Math.min(imageBImages.length - 1, imageIndexB + 1)"
          >
            <ChevronRight :size="12" />
          </button>
        </div>
        <div v-else />
      </div>
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div
        v-for="(bid, idx) in [bidA, bidB]"
        :key="'concepts-' + bid.id"
        class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft"
      >
        <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-4">
          {{ idx === 0 ? t.bidComparison.bidA : t.bidComparison.bidB }} —
          {{ t.bidComparison.conceptStatements }}
        </h2>
        <div v-if="bid.proposal" class="mb-4">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
            {{ t.bidComparison.studioOverview }}
          </p>
          <p class="text-sm text-gray-700 leading-relaxed whitespace-pre-line">{{ bid.proposal }}</p>
        </div>
        <div>
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">
            {{ t.bidComparison.conceptStatement }}
          </p>
          <p v-if="bid.details?.conceptStatement" class="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
            {{ bid.details.conceptStatement }}
          </p>
          <p v-else class="text-sm text-gray-400 italic">{{ t.bidComparison.noConceptStatement }}</p>
        </div>
      </div>
    </div>

    <div v-if="portfoliosA.length || portfoliosB.length" class="grid grid-cols-2 gap-4">
      <div
        v-for="(portfolios, idx) in [portfoliosA, portfoliosB]"
        :key="'portfolios-' + [bidA, bidB][idx].id"
        class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft"
      >
        <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-4">
          {{ idx === 0 ? t.bidComparison.bidA : t.bidComparison.bidB }} —
          {{ t.bidComparison.portfolios }}
        </h2>
        <div v-if="portfolios.length" class="grid grid-cols-2 sm:grid-cols-3 gap-3">
          <button
            v-for="portfolio in portfolios"
            :key="portfolio.id"
            type="button"
            class="text-left group"
            @click="viewingPortfolio = portfolio"
          >
            <div class="aspect-square bg-gray-100 rounded-xl overflow-hidden mb-1.5">
              <img
                v-if="portfolio.images?.[0]"
                :src="portfolio.images[0].mediumUrl || portfolio.images[0].originalUrl"
                :alt="portfolio.title"
                class="w-full h-full object-cover transition group-hover:scale-105"
              />
            </div>
            <p class="text-xs text-gray-600 truncate">{{ portfolio.title }}</p>
          </button>
        </div>
        <p v-else class="text-gray-400 text-sm">{{ t.bidComparison.noPortfolios }}</p>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-soft">
      <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest p-6 border-b border-gray-100">
        {{ t.bidComparison.summaryStats }}
      </h2>
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-center px-6 py-3 text-xs text-brand-brown font-bold uppercase tracking-wider">
              {{ t.bidComparison.bidA }}
            </th>
            <th class="text-center px-6 py-3 text-xs text-gray-500 font-bold uppercase tracking-wider">
              {{ t.bidComparison.metricHeader }}
            </th>
            <th class="text-center px-6 py-3 text-xs font-bold uppercase tracking-wider">
              {{ t.bidComparison.bidB }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-center font-bold text-brand-brown">{{ formatCurrency(bidA.bidAmount) }}</td>
            <td class="px-6 py-4 text-center text-gray-700 font-medium">{{ t.bidComparison.costEstimate }}</td>
            <td class="px-6 py-4 text-center font-bold">{{ formatCurrency(bidB.bidAmount) }}</td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-center">
              {{ bidA.proposedTimelineDays || '—' }} {{ t.bidComparison.days }}
            </td>
            <td class="px-6 py-4 text-center text-gray-700 font-medium">{{ t.bidComparison.timeline }}</td>
            <td class="px-6 py-4 text-center">
              {{ bidB.proposedTimelineDays || '—' }} {{ t.bidComparison.days }}
            </td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-center">{{ bidA.portfolioReferences?.length || 0 }}</td>
            <td class="px-6 py-4 text-center text-gray-700 font-medium">{{ t.bidComparison.portfolios }}</td>
            <td class="px-6 py-4 text-center">{{ bidB.portfolioReferences?.length || 0 }}</td>
          </tr>
          <tr class="border-t border-gray-100">
            <td class="px-6 py-4 text-center">{{ Math.round(scopeScore(bidA)) }}%</td>
            <td class="px-6 py-4 text-center text-gray-700 font-medium">{{ t.bidComparison.scopeAlignment }}</td>
            <td class="px-6 py-4 text-center">{{ Math.round(scopeScore(bidB)) }}%</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div
        v-for="(bid, idx) in [bidA, bidB]"
        :key="'phases-' + bid.id"
        class="bg-white rounded-2xl border border-gray-200 p-6 shadow-soft"
      >
        <h2 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-3">
          {{ idx === 0 ? t.bidComparison.bidA : t.bidComparison.bidB }} —
          {{ t.bidComparison.paymentScheduleTitle }}
        </h2>

        <div v-if="bid.details?.phases?.length" class="space-y-3">
          <div
            v-for="phase in bid.details.phases"
            :key="phase.phaseNumber"
            class="py-3 border-b border-gray-100 last:border-0"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-1.5">
                <span class="text-xs font-bold px-1.5 py-0 rounded-full bg-brand-brown text-white"
                  >P{{ phase.phaseNumber }}</span
                >
                <span class="text-sm font-medium text-gray-800">{{
                  phase.title || `${t.paymentPhaseBuilder.phase} ${phase.phaseNumber}`
                }}</span>
              </div>
              <div class="flex items-center gap-1.5">
                <span class="text-xs font-bold text-brand-brown">{{ formatCurrency(phase.amount) }}</span>
                <button
                  type="button"
                  class="w-4 h-4 rounded-full flex items-center justify-center text-gray-400 hover:text-brand-brown hover:bg-brand-tan/40 transition"
                  :title="t.bidComparison.paymentFlowInfo"
                  @click="
                    activePhaseInfo = {
                      phase,
                      bidLabel: idx === 0 ? t.bidComparison.bidA : t.bidComparison.bidB
                    }
                  "
                >
                  <Info :size="14" />
                </button>
              </div>
            </div>
            <div v-if="phase.deliverables?.length" class="flex flex-wrap gap-1 mb-1.5">
              <span
                v-for="d in phase.deliverables"
                :key="d"
                :class="[
                  'text-xs px-2 py-0.5 rounded-full border font-medium',
                  isMatchingDeliverable(d)
                    ? 'bg-green-100 text-green-700 border-green-200'
                    : 'bg-blue-100 text-blue-700 border-blue-200'
                ]"
                :title="
                  isMatchingDeliverable(d)
                    ? t.bidComparison.matchingDeliverable
                    : t.bidComparison.additionalDeliverable
                "
              >
                {{ t.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
              </span>
            </div>
            <div class="flex items-center gap-3 mt-0.5">
              <p v-if="phase.revisionRounds != null" class="text-xs text-gray-400">
                {{ phase.revisionRounds }}
                {{
                  phase.revisionRounds !== 1 ? t.clientFinalization.revisionRounds : t.clientFinalization.revisionRound
                }}
              </p>
              <p v-if="phase.estimatedDays" class="text-xs text-gray-400">
                {{ phase.estimatedDays }} {{ t.bidComparison.days }}
              </p>
            </div>
          </div>
        </div>
        <p v-else class="text-gray-400 text-sm">{{ t.bidComparison.noPhases }}</p>

        <div v-if="getMissingDeliverables(bid).length" class="mt-4 pt-4 border-t border-dashed border-gray-200">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2">
            {{ t.bidComparison.uncoveredTitle }}
          </p>
          <div class="flex flex-wrap gap-1">
            <span
              v-for="d in getMissingDeliverables(bid)"
              :key="d"
              class="text-xs px-2 py-0.5 rounded-full border bg-red-50 text-red-400 border-red-200 font-medium"
              :title="t.bidComparison.missingDeliverable"
            >
              {{ t.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ') }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="flex items-center justify-center gap-4 flex-wrap">
      <span
        class="inline-flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-green-100 text-green-700 border border-green-200 cursor-default"
        :title="t.bidComparison.matchingDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-green-500 inline-block" />
        {{ t.bidComparison.matchingDeliverable }}
      </span>
      <span
        class="inline-flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-blue-100 text-blue-700 border border-blue-200 cursor-default"
        :title="t.bidComparison.additionalDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-blue-500 inline-block" />
        {{ t.bidComparison.additionalDeliverable }}
      </span>
      <span
        class="inline-flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-red-50 text-red-400 border border-red-200 cursor-default"
        :title="t.bidComparison.missingDeliverable"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-red-300 inline-block" />
        {{ t.bidComparison.missingDeliverable }}
      </span>
    </div>

    <PortfolioDetailPopup :portfolio="viewingPortfolio" @close="viewingPortfolio = null" />

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
          v-if="activePhaseInfo"
          class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4"
          @click.self="activePhaseInfo = null"
        >
          <div class="w-full max-w-md bg-white rounded-3xl shadow-2xl p-6">
            <div class="flex items-start justify-between gap-4 mb-4">
              <div>
                <p class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">
                  {{ activePhaseInfo.bidLabel }}
                </p>
                <h3 class="text-lg font-bold text-black">
                  {{
                    activePhaseInfo.phase.title || `${t.paymentPhaseBuilder.phase} ${activePhaseInfo.phase.phaseNumber}`
                  }}
                  — {{ t.bidComparison.paymentFlowTitle }}
                </h3>
              </div>
              <button
                type="button"
                class="w-8 h-8 shrink-0 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition"
                @click="activePhaseInfo = null"
              >
                <X :size="16" />
              </button>
            </div>

            <p class="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
              {{ buildPaymentFlowText(activePhaseInfo.phase) }}
            </p>
          </div>
        </div>
      </Transition>
    </Teleport>

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
          v-if="pendingAppointBid"
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm px-4"
          @click.self="closeAppointModal"
        >
          <div class="bg-white rounded-2xl w-full max-w-md shadow-2xl overflow-hidden">
            <div class="bg-brand-brown px-6 py-5">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center shrink-0">
                  <Crown :size="20" class="text-white" aria-hidden="true" />
                </div>
                <div>
                  <p class="text-xs text-brand-tan font-semibold uppercase tracking-wide">
                    {{ t.bidComparison.appointModalEyebrow }}
                  </p>
                  <h3 class="text-lg font-bold text-white">{{ t.bidComparison.appointModalTitle }}</h3>
                </div>
              </div>
            </div>

            <div class="p-6 space-y-4">
              <div
                class="flex items-center justify-between gap-3 p-4 bg-brand-tan/30 border border-brand-gold/30 rounded-xl"
              >
                <div class="min-w-0">
                  <p class="font-bold text-gray-900 truncate">{{ pendingAppointBid.architectName || 'Architect' }}</p>
                  <p v-if="pendingAppointBid.architectCompany" class="text-sm text-gray-500 truncate">
                    {{ pendingAppointBid.architectCompany }}
                  </p>
                </div>
                <p class="text-sm font-bold text-brand-brown shrink-0">
                  {{ formatCurrency(pendingAppointBid.bidAmount) }}
                </p>
              </div>

              <div class="p-4 bg-amber-50 border border-amber-200 rounded-xl">
                <p class="text-sm font-bold text-amber-800 mb-2">
                  {{ t.bidComparison.appointModalWarningTitle }}
                </p>
                <ul class="text-xs text-amber-700 space-y-1.5 list-disc list-inside">
                  <li>{{ t.bidComparison.appointModalWarningItem1 }}</li>
                  <li>{{ t.bidComparison.appointModalWarningItem2 }}</li>
                  <li>{{ t.bidComparison.appointModalWarningItem3 }}</li>
                </ul>
              </div>

              <p v-if="appointError" class="text-sm text-red-600">{{ appointError }}</p>

              <div class="flex gap-3 pt-1">
                <button
                  type="button"
                  :disabled="appointLoading"
                  class="flex-1 px-4 py-2.5 border border-gray-200 text-gray-600 text-sm font-semibold rounded-lg hover:bg-gray-50 disabled:opacity-50 transition"
                  @click="closeAppointModal"
                >
                  {{ t.bidComparison.appointModalCancel }}
                </button>
                <button
                  type="button"
                  :disabled="appointLoading"
                  class="flex-1 px-4 py-2.5 bg-brand-brown text-white text-sm font-bold rounded-lg hover:bg-brand-brown-dark disabled:opacity-50 transition flex items-center justify-center gap-2"
                  @click="confirmAppoint"
                >
                  <span v-if="appointLoading">{{ t.bidComparison.appointModalLoading }}</span>
                  <template v-else>
                    <Crown :size="15" aria-hidden="true" />
                    {{ t.bidComparison.appointModalConfirm }}
                  </template>
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronLeft, ChevronRight, Crown, Info, X } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useBidsStore } from '@/stores/bids'
import PortfolioDetailPopup from '@/components/bid/PortfolioDetailPopup.vue'

const props = defineProps({
  bidA: { type: Object, required: true },
  bidB: { type: Object, required: true },
  project: { type: Object, required: true }
})

const { t } = useI18n()
const router = useRouter()
const bidsStore = useBidsStore()

const activeImageType = ref('FACADE')
const sliderPosition = ref(50)
const isDragging = ref(false)
const sliderContainerRef = ref(null)
const imageIndexA = ref(0)
const imageIndexB = ref(0)
const viewingPortfolio = ref(null)
const activePhaseInfo = ref(null)
const pendingAppointBid = ref(null)
const appointLoading = ref(false)
const appointError = ref(null)

const handlePhaseInfoKeydown = e => {
  if (e.key !== 'Escape') return
  activePhaseInfo.value = null
  if (!appointLoading.value) pendingAppointBid.value = null
}

onMounted(() => document.addEventListener('keydown', handlePhaseInfoKeydown))
onUnmounted(() => document.removeEventListener('keydown', handlePhaseInfoKeydown))

const portfoliosA = computed(() => props.bidA?.portfolioReferences || [])
const portfoliosB = computed(() => props.bidB?.portfolioReferences || [])

const IMAGE_TYPES = ['FACADE', 'INTERIOR', 'MASSING', 'ZONING']

const imageTypeLabel = computed(() => ({
  FACADE: t.value.bidComparison.facade,
  INTERIOR: t.value.bidComparison.interior,
  MASSING: t.value.bidComparison.massing,
  ZONING: t.value.bidComparison.zoning
}))

const IMAGE_TYPE_FIELD = {
  FACADE: 'facadeImages',
  INTERIOR: 'interiorImages',
  MASSING: 'massingImages',
  ZONING: 'zoningImages'
}

const IMAGE_TYPE_DESCRIPTION_FIELD = {
  FACADE: 'facadeDescription',
  INTERIOR: 'interiorDescription',
  MASSING: 'massingDescription',
  ZONING: 'zoningDescription'
}

const getImages = (bid, type) => bid?.[IMAGE_TYPE_FIELD[type]] ?? []
const getDescription = (bid, type) => bid?.details?.[IMAGE_TYPE_DESCRIPTION_FIELD[type]] ?? ''

const imageAImages = computed(() => getImages(props.bidA, activeImageType.value))
const imageBImages = computed(() => getImages(props.bidB, activeImageType.value))
const descriptionA = computed(() => getDescription(props.bidA, activeImageType.value))
const descriptionB = computed(() => getDescription(props.bidB, activeImageType.value))
const imageAUrl = computed(() => imageAImages.value[imageIndexA.value]?.imageUrl ?? null)
const imageBUrl = computed(() => imageBImages.value[imageIndexB.value]?.imageUrl ?? null)

const switchType = type => {
  activeImageType.value = type
  sliderPosition.value = 50
  imageIndexA.value = 0
  imageIndexB.value = 0
}

const startDrag = () => {
  isDragging.value = true
}
const stopDrag = () => {
  isDragging.value = false
}

const onMove = e => {
  if (!isDragging.value || !sliderContainerRef.value) return
  const rect = sliderContainerRef.value.getBoundingClientRect()
  sliderPosition.value = Math.max(0, Math.min(100, ((e.clientX - rect.left) / rect.width) * 100))
}

const onTouchMove = e => {
  if (!isDragging.value || !sliderContainerRef.value) return
  const rect = sliderContainerRef.value.getBoundingClientRect()
  sliderPosition.value = Math.max(0, Math.min(100, ((e.touches[0].clientX - rect.left) / rect.width) * 100))
}

const allPhasesDeliverables = bid => bid.details?.phases?.flatMap(p => p.deliverables || []) || []

const isMatchingDeliverable = d => (props.project?.deliverables || []).includes(d)

const getMissingDeliverables = bid => {
  const projectDeliverables = props.project?.deliverables || []
  const covered = allPhasesDeliverables(bid)
  return projectDeliverables.filter(d => !covered.includes(d))
}

const formatCurrency = value => {
  if (!value) return 'N/A'
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    notation: 'compact',
    compactDisplay: 'short'
  }).format(value)
}

const buildPaymentFlowText = phase => {
  const deliverableLabels = (phase.deliverables || []).map(
    d => t.value.bidCreate?.deliverableItems?.[d] || d.replace(/_/g, ' ')
  )

  const sentences = [t.value.bidComparison.paymentFlowUpfront.replace('{amount}', formatCurrency(phase.amount))]

  if (phase.estimatedDays) {
    const timelineKey = deliverableLabels.length
      ? t.value.bidComparison.paymentFlowTimelineWithDeliverables
          .replace('{days}', phase.estimatedDays)
          .replace('{deliverables}', deliverableLabels.join(', '))
      : t.value.bidComparison.paymentFlowTimeline.replace('{days}', phase.estimatedDays)
    sentences.push(timelineKey)
  }

  if (phase.revisionRounds != null) {
    sentences.push(t.value.bidComparison.paymentFlowRevisions.replace('{rounds}', phase.revisionRounds))
  }

  return sentences.join(' ')
}

const scopeScore = bid => {
  const projectDeliverables = props.project?.deliverables || []
  if (!projectDeliverables.length) return 0
  const bidDeliverables = allPhasesDeliverables(bid)
  const intersection = bidDeliverables.filter(d => projectDeliverables.includes(d))
  return (intersection.length / projectDeliverables.length) * 100
}

const closeAppointModal = () => {
  if (appointLoading.value) return
  pendingAppointBid.value = null
  appointError.value = null
}

const confirmAppoint = async () => {
  if (!pendingAppointBid.value) return
  appointLoading.value = true
  appointError.value = null
  try {
    await bidsStore.acceptBid(pendingAppointBid.value.id)
    router.push(`/client/projects/${props.project.id}/finalization`)
  } catch (err) {
    appointError.value = err.response?.data?.message || 'Failed to appoint architect'
  } finally {
    appointLoading.value = false
  }
}
</script>

<style scoped>
/* One-shot sheen sweep: replays only when hover/focus-visible re-enters, never loops. */
.appoint-lead-btn {
  overflow: hidden;
}

.appoint-lead-btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    115deg,
    transparent 25%,
    rgba(255, 255, 255, 0.55) 48%,
    rgba(255, 255, 255, 0.55) 52%,
    transparent 75%
  );
  transform: translateX(-130%);
  pointer-events: none;
}

.group:hover .appoint-lead-btn::after,
.group:has(button:focus-visible) .appoint-lead-btn::after {
  animation: appoint-sheen 0.9s ease-out;
}

@keyframes appoint-sheen {
  to {
    transform: translateX(130%);
  }
}

@media (prefers-reduced-motion: reduce) {
  .appoint-lead-btn::after {
    animation: none !important;
  }
}
</style>
