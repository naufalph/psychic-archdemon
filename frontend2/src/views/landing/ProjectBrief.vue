<template>
  <div class="min-h-screen bg-white text-ink-900">
    <header
      class="min-h-[72px] flex flex-wrap gap-4 items-center justify-between box-border px-4 md:px-6 py-3 border-b border-hairline"
    >
      <router-link to="/" class="flex items-baseline gap-0.5">
        <span class="text-[22px] font-extrabold tracking-[-.03em] text-ink-900">rumantra</span>
        <span class="text-[22px] font-extrabold text-brand-gold">.</span>
      </router-link>
      <nav class="flex items-center flex-wrap gap-2">
        <router-link
          v-for="link in navLinks"
          :key="link.to"
          :to="link.to"
          class="hidden md:flex h-11 items-center px-3 text-body font-medium text-ink-400 hover:text-ink-900 transition-colors"
        >
          {{ link.label }}
        </router-link>
        <router-link
          v-if="!authStore.isAuthenticated"
          to="/login"
          class="h-11 flex items-center px-3 text-body font-medium text-ink-400 hover:text-ink-900 transition-colors"
        >
          {{ tv.nav.login }}
        </router-link>
        <button
          type="button"
          class="inline-flex items-center h-11 px-[22px] rounded-full bg-ink-900 text-body font-bold text-white hover:opacity-85 transition-opacity"
          @click="restart"
        >
          {{ tv.nav.cta }}
        </button>
      </nav>
    </header>

    <main class="box-border px-6 pt-[clamp(40px,5vw,72px)] pb-[clamp(56px,7vw,96px)] max-w-[1328px] mx-auto w-full">
      <div class="mb-8">
        <h1 class="font-bold tracking-[-.035em] leading-[1.08] m-0 mb-3.5" style="font-size: clamp(32px, 4vw, 48px)">
          {{ page.title }}
        </h1>
        <p v-if="page.subtitle" class="text-[17px] leading-relaxed text-ink-400 m-0">{{ page.subtitle }}</p>
        <div v-if="step === 2" class="flex items-center gap-3 flex-wrap mt-4">
          <span class="inline-flex items-center gap-2 text-[15px] text-ink-900">
            <Lock :size="18" class="flex-none" />{{ tv.privacy.badge }}
          </span>
          <button
            type="button"
            class="text-[15px] font-semibold text-ink-900 border-b border-hairline hover:border-ink-900 transition-colors"
            @click="privacyOpen = true"
          >
            {{ tv.privacy.learnMore }}
          </button>
        </div>
      </div>

      <!-- Step 1: services -->
      <div v-if="step === 1" class="bg-white border border-hairline rounded-[20px] p-6 md:p-8 animate-brief-rise">
        <div class="grid grid-cols-[repeat(auto-fit,minmax(200px,1fr))] gap-4">
          <button
            v-for="svc in services"
            :key="svc.id"
            type="button"
            :disabled="svc.soon"
            :aria-pressed="isSelected(svc)"
            class="relative min-w-0 text-left border rounded-2xl p-5 flex flex-col gap-2.5 min-h-[168px] transition-all duration-200"
            :class="serviceClass(svc)"
            @click="toggleService(svc)"
          >
            <div class="flex items-center flex-wrap gap-2 min-h-[22px]">
              <span
                v-if="svc.soon"
                class="inline-flex items-center h-[22px] px-2.5 rounded-full border border-hairline text-[11px] font-bold tracking-[.05em] uppercase text-ink-400 whitespace-nowrap"
              >
                {{ tv.lineup.comingSoon }}
              </span>
            </div>
            <span
              class="text-[17px] font-bold tracking-[-.02em] leading-tight"
              :class="isSelected(svc) ? 'text-white' : 'text-ink-900'"
            >
              {{ svc.label }}
            </span>
            <span class="text-caption leading-normal" :class="isSelected(svc) ? 'text-white/80' : 'text-ink-400'">
              {{ svc.desc }}
            </span>
            <span
              v-if="isSelected(svc)"
              class="absolute bottom-4 right-4 w-5 h-5 rounded-full bg-white text-ink-900 flex items-center justify-center"
            >
              <Check :size="13" :stroke-width="3.4" />
            </span>
          </button>
        </div>
      </div>

      <!-- Step 2: quick brief -->
      <div v-else class="bg-white border border-hairline rounded-[20px] p-6 md:p-8 animate-brief-rise">
        <div class="mb-7">
          <label :for="ids.title" :class="labelCls">{{ tf.title }}</label>
          <input
            :id="ids.title"
            v-model="form.title"
            type="text"
            maxlength="160"
            :placeholder="tf.titlePlaceholder"
            :class="inputCls"
          />
        </div>

        <div class="mb-7">
          <span :class="labelCls" class="!mb-3">{{ tf.scope }}</span>
          <div class="flex gap-2 flex-wrap">
            <div v-for="scope in scopeOptions" :key="scope.value" class="relative">
              <button
                type="button"
                class="h-11 inline-flex items-center gap-2 pl-5 pr-3.5 rounded-full text-[15px] font-semibold border transition-colors"
                :class="pillClass(form.projectScope === scope.value)"
                @click="form.projectScope = scope.value"
              >
                {{ scope.label }}
                <span
                  aria-hidden="true"
                  class="flex-none w-[18px] h-[18px] box-border rounded-full border-[1.5px] border-current inline-flex items-center justify-center text-[11px] font-bold italic font-serif opacity-70 cursor-help"
                  @mouseenter="scopeTip = scope.value"
                  @mouseleave="scopeTip = null"
                  >i</span
                >
              </button>
              <div
                v-if="scopeTip === scope.value"
                class="absolute left-0 bottom-[calc(100%+8px)] z-50 w-[260px] px-3.5 py-3 rounded-xl bg-ink-900 text-white text-caption leading-normal font-medium shadow-[0_16px_32px_-16px_rgba(0,0,0,.4)] pointer-events-none"
              >
                {{ tf.scopeTips[scope.value] }}
              </div>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-[repeat(auto-fit,minmax(230px,1fr))] gap-6 mb-7">
          <div>
            <label :for="ids.category" :class="labelCls">{{ tf.category }}</label>
            <div class="relative">
              <select
                :id="ids.category"
                v-model="form.category"
                :class="[selectCls, form.category ? 'text-ink-900' : 'text-ink-400']"
              >
                <option value="">{{ tf.categoryPlaceholder }}</option>
                <option v-for="c in categoryOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
              </select>
              <ChevronDown :size="18" class="absolute right-4 top-[15px] pointer-events-none text-ink-400" />
            </div>
          </div>
          <div>
            <label :for="ids.subCategory" :class="labelCls">{{ tf.subCategory }}</label>
            <div class="relative">
              <select
                :id="ids.subCategory"
                v-model="form.subCategory"
                :disabled="!subOptions.length"
                :class="[
                  selectCls,
                  subOptions.length ? 'bg-white cursor-pointer' : 'bg-surface-alt cursor-not-allowed',
                  form.subCategory ? 'text-ink-900' : 'text-ink-400'
                ]"
              >
                <option value="">{{ subOptions.length ? tf.subCategoryPlaceholder : tf.subCategoryDisabled }}</option>
                <option v-for="s in subOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
              </select>
              <ChevronDown :size="18" class="absolute right-4 top-[15px] pointer-events-none text-ink-400" />
            </div>
          </div>
        </div>

        <div class="py-7 border-t border-hairline space-y-3">
          <div>
            <label :for="ids.location" :class="labelCls" class="!mb-1">{{ tf.location }}</label>
            <p class="text-caption text-ink-400 m-0">{{ tf.locationHint }}</p>
          </div>
          <input
            :id="ids.location"
            v-model="form.fullAddress"
            type="text"
            maxlength="255"
            autocomplete="street-address"
            :placeholder="tf.locationPlaceholder"
            :class="inputCls"
          />
          <!-- Map placeholder: the pin picker is deliberately not wired here yet, so this page makes
               no metered Google Maps calls. Hooking up a map should also fill city, province,
               latitude and longitude, which the brief API already accepts. -->
          <div
            class="h-[320px] rounded-2xl border border-dashed border-hairline-alt bg-surface-alt flex flex-col items-center justify-center gap-2 text-center px-6"
          >
            <MapPin :size="28" class="text-ink-300" />
            <p class="text-body font-semibold text-ink-500 m-0">{{ tf.mapPlaceholderTitle }}</p>
            <p class="text-caption text-ink-400 m-0 max-w-[40ch]">{{ tf.mapPlaceholderDesc }}</p>
          </div>
        </div>

        <div class="pt-7 border-t border-hairline grid grid-cols-[repeat(auto-fit,minmax(230px,1fr))] gap-6">
          <div>
            <label :for="ids.lot" :class="labelCls">{{ tf.lotSize }}</label>
            <input
              :id="ids.lot"
              v-model="form.lotSize"
              type="number"
              min="1"
              inputmode="numeric"
              :placeholder="tf.lotSizePlaceholder"
              :class="inputCls"
            />
          </div>
          <div>
            <label :for="ids.build" :class="labelCls">{{ tf.buildArea }}</label>
            <input
              :id="ids.build"
              v-model="form.buildArea"
              type="number"
              min="1"
              inputmode="numeric"
              :placeholder="tf.buildAreaPlaceholder"
              :class="inputCls"
            />
          </div>

          <div class="col-span-full">
            <label :for="ids.fee" :class="labelCls">{{ tf.designFee }}</label>
            <input
              :id="ids.fee"
              :value="form.designFee"
              type="text"
              inputmode="numeric"
              :placeholder="tf.designFeePlaceholder"
              :class="inputCls"
              @input="onFeeInput"
            />
            <div class="flex items-center justify-between gap-3 flex-wrap mt-2.5">
              <p class="text-caption leading-normal text-ink-400 m-0">{{ tf.designFeeHint }}</p>
              <button
                type="button"
                class="flex-none inline-flex items-center gap-2 h-10 px-4 rounded-full border border-ink-900 text-caption font-bold text-ink-900 hover:bg-black/5 transition-colors"
                @click="calculatorOpen = true"
              >
                <Calculator :size="16" />{{ tf.openCalculator }}
              </button>
            </div>
          </div>

          <div class="col-span-full">
            <label :for="ids.vision" :class="labelCls">{{ tf.vision }}</label>
            <textarea
              :id="ids.vision"
              v-model="form.description"
              rows="4"
              maxlength="2000"
              :placeholder="tf.visionPlaceholder"
              class="w-full box-border px-4 py-3.5 border border-hairline rounded-xl bg-white text-body leading-relaxed text-ink-900 placeholder-ink-300 outline-none resize-y focus:border-ink-900 transition-colors"
            ></textarea>
          </div>

          <div class="col-span-full">
            <label :for="ids.phone" :class="labelCls">{{ tf.phone }}</label>
            <input
              :id="ids.phone"
              v-model="form.phoneNumber"
              type="tel"
              autocomplete="tel"
              :placeholder="tf.phonePlaceholder"
              :class="[inputCls, phoneInvalid && '!border-red-400']"
            />
            <p v-if="phoneInvalid" class="text-caption text-[#B42318] mt-1.5 mb-0">{{ tf.phoneInvalid }}</p>
          </div>
        </div>

        <div class="mt-7 pt-7 border-t border-hairline">
          <span :class="labelCls" class="!mb-3">{{ tf.start }}</span>
          <div class="grid grid-cols-[repeat(auto-fit,minmax(220px,1fr))] gap-3">
            <button
              v-for="opt in startOptions"
              :key="opt.value"
              type="button"
              role="radio"
              :aria-checked="form.startDateType === opt.value"
              class="flex items-center gap-3 h-14 px-5 border rounded-2xl text-left transition-colors hover:border-ink-900"
              :class="form.startDateType === opt.value ? 'border-ink-900 bg-surface-alt' : 'border-hairline bg-white'"
              @click="form.startDateType = opt.value"
            >
              <span
                class="flex-none w-[18px] h-[18px] box-border rounded-full bg-white"
                :class="form.startDateType === opt.value ? 'border-[5px] border-ink-900' : 'border border-hairline'"
              ></span>
              <span class="text-[15px] font-semibold text-ink-900">{{ opt.label }}</span>
            </button>
          </div>
          <input
            v-if="form.startDateType === 'SPECIFIC_DATE'"
            v-model="form.expectedStartDate"
            type="date"
            :min="today"
            :class="inputCls"
            class="max-w-[340px] mt-3"
          />
        </div>
      </div>

      <div class="flex items-center justify-end gap-4 flex-wrap mt-6">
        <button
          v-if="step > 1"
          type="button"
          class="h-[52px] px-7 rounded-full border border-ink-900 text-[17px] font-bold text-ink-900 hover:bg-black/5 transition-colors"
          @click="goBack"
        >
          {{ ta.back }}
        </button>
        <span v-if="!canContinue" class="text-caption text-ink-700">
          {{ step === 1 ? ta.needService : `${ta.needFields}: ${missingFields.join(', ')}` }}
        </span>
        <button
          type="button"
          :disabled="!canContinue || submitting"
          class="h-[52px] px-8 rounded-full bg-ink-900 text-white text-[17px] font-bold transition-opacity disabled:opacity-35 disabled:cursor-not-allowed"
          @click="goNext"
        >
          {{ submitting ? ta.submitting : step === 1 ? ta.saveAndContinue : ta.continue }}
        </button>
      </div>
      <p v-if="submitError" class="text-right text-caption text-[#B42318] mt-3 mb-0">{{ submitError }}</p>

      <div
        v-if="step === 1"
        class="flex justify-end items-baseline gap-1.5 mt-4 text-[15px] leading-normal text-ink-400"
      >
        <span>{{ tv.lineup.providerPrompt }}</span>
        <router-link
          to="/signup?role=ARCHITECT"
          class="font-semibold text-ink-900 border-b border-hairline hover:border-ink-900 transition-colors"
        >
          {{ tv.lineup.providerCta }}
        </router-link>
      </div>
    </main>

    <BriefPrivacyModal :open="privacyOpen" @close="privacyOpen = false" />
    <BriefFeeCalculatorModal :open="calculatorOpen" @close="calculatorOpen = false" @use="applyCalculatedFee" />
    <BriefAuthModal :open="authOpen" @close="authOpen = false" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, useId } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Check, ChevronDown, Lock, Calculator, MapPin } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { useAuthStore } from '@/stores/auth'
import { useProjectBrief } from '@/composables/useProjectBrief'
import { landingAPI } from '@/services/api'
import { PROJECT_SCOPES, PROJECT_CATEGORIES, subCategoriesFor, isValidCategory } from '@/constants/projectTaxonomy'
import BriefPrivacyModal from '@/components/brief/BriefPrivacyModal.vue'
import BriefFeeCalculatorModal from '@/components/brief/BriefFeeCalculatorModal.vue'
import BriefAuthModal from '@/components/brief/BriefAuthModal.vue'

const { t, locale } = useI18n()
const tv = computed(() => t.value.brief)
const tf = computed(() => t.value.brief.form)
const ta = computed(() => t.value.brief.actions)
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { saveToken } = useProjectBrief()

const PHONE_PATTERN = /^\+?[0-9\s-]{10,16}$/

const ids = {
  title: useId(),
  location: useId(),
  category: useId(),
  subCategory: useId(),
  lot: useId(),
  build: useId(),
  fee: useId(),
  vision: useId(),
  phone: useId()
}
const labelCls = 'block text-body font-medium text-ink-700 mb-2.5'
const inputCls =
  'w-full box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors'
const selectCls =
  'w-full box-border h-12 pl-4 pr-[42px] border border-hairline rounded-xl text-body font-semibold appearance-none outline-none focus:border-ink-900 transition-colors'
const pillClass = on => (on ? 'bg-ink-900 text-white border-ink-900' : 'bg-transparent text-ink-700 border-hairline')

const step = ref(1)
const privacyOpen = ref(false)
const calculatorOpen = ref(false)
const authOpen = ref(false)
const scopeTip = ref(null)
const submitting = ref(false)
const submitError = ref('')
const selectedServices = ref(['design'])

const form = reactive({
  title: '',
  projectScope: '',
  category: '',
  subCategory: '',
  fullAddress: '',
  city: '',
  province: '',
  latitude: null,
  longitude: null,
  lotSize: '',
  buildArea: '',
  designFee: '',
  description: '',
  phoneNumber: '',
  startDateType: '',
  expectedStartDate: ''
})

const today = new Date().toISOString().slice(0, 10)

const page = computed(() => (step.value === 1 ? tv.value.steps.lineup : tv.value.steps.quick))

const navLinks = computed(() => [
  { to: '/#ruang-proyek', label: tv.value.nav.howItWorks },
  { to: '/#mitra', label: tv.value.nav.findArchitect },
  { to: '/#estimasi', label: tv.value.nav.estimateCost }
])

const services = computed(() => [
  { id: 'design', ...tv.value.lineup.design, soon: false },
  { id: 'build', ...tv.value.lineup.build, soon: true },
  { id: 'craftsman', ...tv.value.lineup.craftsman, soon: true }
])

const isSelected = svc => !svc.soon && selectedServices.value.includes(svc.id)

const serviceClass = svc => {
  if (svc.soon) return 'border-hairline bg-white opacity-55 cursor-not-allowed'
  return isSelected(svc)
    ? 'border-ink-900 bg-ink-900 cursor-pointer hover:-translate-y-1 hover:shadow-[0_24px_48px_-36px_rgba(0,0,0,.4)]'
    : 'border-hairline bg-white cursor-pointer hover:-translate-y-1 hover:shadow-[0_24px_48px_-36px_rgba(0,0,0,.4)]'
}

const toggleService = svc => {
  if (svc.soon) return
  selectedServices.value = isSelected(svc)
    ? selectedServices.value.filter(id => id !== svc.id)
    : [...selectedServices.value, svc.id]
}

const localized = item => (locale.value === 'en' ? item.labelEn : item.labelId)

const scopeOptions = computed(() => PROJECT_SCOPES.map(s => ({ value: s.value, label: localized(s) })))
const categoryOptions = computed(() => PROJECT_CATEGORIES.map(c => ({ value: c.value, label: localized(c) })))
const subOptions = computed(() => subCategoriesFor(form.category).map(s => ({ value: s.value, label: localized(s) })))

watch(
  () => form.category,
  () => {
    if (!subOptions.value.some(s => s.value === form.subCategory)) form.subCategory = ''
  }
)

const startOptions = computed(() => [
  { value: 'IMMEDIATELY', label: tf.value.startImmediately },
  { value: 'SPECIFIC_DATE', label: tf.value.startSpecific }
])

const digitsOf = value => {
  const n = Number(String(value || '').replace(/\D/g, ''))
  return n > 0 ? n : null
}

// The fee field holds either one amount or a "min – max" pair from the calculator
const parsedFee = computed(() => {
  const parts = String(form.designFee || '')
    .split(/[–-]/)
    .map(digitsOf)
    .filter(Boolean)
  if (!parts.length) return null
  const [min, max = min] = parts
  return { min: Math.min(min, max), max: Math.max(min, max) }
})

const onFeeInput = e => {
  const raw = e.target.value
  form.designFee = /[–-]/.test(raw) ? raw : (digitsOf(raw)?.toLocaleString('id-ID') ?? '')
  e.target.value = form.designFee
}

const applyCalculatedFee = ({ min, max }) => {
  form.designFee = max ? `${min.toLocaleString('id-ID')} – ${max.toLocaleString('id-ID')}` : min.toLocaleString('id-ID')
  calculatorOpen.value = false
}

const phoneInvalid = computed(() => !!form.phoneNumber.trim() && !PHONE_PATTERN.test(form.phoneNumber.trim()))

// Each entry is [label, filled]; the labels double as the hint telling the user what is left.
const briefChecks = computed(() => [
  [tf.value.title, !!form.title.trim()],
  [tf.value.scope, !!form.projectScope],
  [tf.value.category, isValidCategory(form.category)],
  [tf.value.subCategory, !subOptions.value.length || !!form.subCategory],
  [tf.value.location, !!form.fullAddress.trim()],
  [tf.value.lotSize, !!digitsOf(form.lotSize)],
  [tf.value.buildArea, !!digitsOf(form.buildArea)],
  [tf.value.designFee, !!parsedFee.value],
  [tf.value.vision, !!form.description.trim()],
  [tf.value.phone, !!form.phoneNumber.trim() && !phoneInvalid.value],
  [tf.value.start, !!form.startDateType && (form.startDateType !== 'SPECIFIC_DATE' || !!form.expectedStartDate)]
])

const missingFields = computed(() => briefChecks.value.filter(([, filled]) => !filled).map(([label]) => label))

const briefComplete = computed(() => missingFields.value.length === 0)

const canContinue = computed(() => (step.value === 1 ? selectedServices.value.length > 0 : briefComplete.value))

const scrollTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

const goBack = () => {
  step.value = 1
  submitError.value = ''
  scrollTop()
}

const restart = () => {
  step.value = 1
  scrollTop()
}

const buildPayload = () => ({
  buildingFunction: form.category,
  projectScope: form.projectScope,
  subCategory: form.subCategory || null,
  title: form.title.trim(),
  location: form.fullAddress.trim().slice(0, 255),
  city: form.city || null,
  province: form.province || null,
  latitude: form.latitude,
  longitude: form.longitude,
  lotSize: digitsOf(form.lotSize),
  buildArea: digitsOf(form.buildArea),
  description: form.description.trim(),
  phoneNumber: form.phoneNumber.trim(),
  designBudgetTotal: parsedFee.value.min === parsedFee.value.max ? parsedFee.value.min : null,
  designBudgetMin: parsedFee.value.min,
  designBudgetMax: parsedFee.value.max,
  startDateType: form.startDateType,
  expectedStartDate: form.startDateType === 'SPECIFIC_DATE' ? form.expectedStartDate : null
})

const submitBrief = async () => {
  submitError.value = ''
  submitting.value = true
  try {
    const res = await landingAPI.createBrief(buildPayload())
    const claimToken = res.data?.data?.claimToken
    if (!claimToken) throw new Error('Missing claim token')
    saveToken(claimToken)

    if (authStore.isAuthenticated && authStore.isClient) {
      router.push(`/client/projects/create?brief=${encodeURIComponent(claimToken)}`)
    } else {
      authOpen.value = true
    }
  } catch (err) {
    submitError.value = err.response?.data?.message || ta.value.submitError
  } finally {
    submitting.value = false
  }
}

const goNext = () => {
  if (!canContinue.value) return
  if (step.value === 1) {
    step.value = 2
    scrollTop()
    return
  }
  submitBrief()
}

onMounted(() => {
  const category = String(route.query.kategori || '').toUpperCase()
  if (isValidCategory(category)) form.category = category
})
</script>
