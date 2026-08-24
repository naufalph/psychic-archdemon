<template>
  <div class="space-y-6">
    <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5 space-y-6">
      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.photo }}
        </label>
        <div class="flex items-center gap-4">
          <img
            v-if="photoPreview"
            :src="photoPreview"
            alt=""
            class="w-20 h-20 rounded-full object-cover border border-black/10"
          />
          <div
            v-else
            class="w-20 h-20 rounded-full bg-brand-brown/10 text-brand-brown flex items-center justify-center text-xl font-bold"
          >
            {{ initials }}
          </div>
          <div class="flex-1">
            <input ref="photoInput" type="file" accept="image/*" class="hidden" @change="handlePhotoSelect" />
            <button
              type="button"
              class="px-4 py-2 rounded-full text-sm font-semibold bg-black/5 text-black/70 hover:bg-black/10 transition-all"
              :disabled="isUploadingPhoto"
              @click="$refs.photoInput.click()"
            >
              {{ isUploadingPhoto ? 'Uploading...' : t.profile.form.photo }}
            </button>
            <p v-if="photoError" class="text-xs text-red-600 mt-1">{{ photoError }}</p>
          </div>
        </div>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.practiceName }}
        </label>
        <input
          v-model="formData.name"
          type="text"
          :placeholder="t.profile.form.practiceNamePlaceholder"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
          :class="{ 'border-red-300': errors.name }"
        />
        <p v-if="errors.name" class="text-xs text-red-600">{{ errors.name }}</p>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.category }}
        </label>
        <div class="flex gap-3">
          <button
            type="button"
            :class="[
              'flex-1 px-4 py-3 rounded-2xl font-medium text-sm transition-all',
              formData.category === 'freelance'
                ? 'bg-brand-brown text-white shadow-md'
                : 'bg-black/5 text-black/60 hover:bg-black/10'
            ]"
            @click="formData.category = 'freelance'"
          >
            {{ t.profile.form.categoryFreelance }}
          </button>
          <button
            type="button"
            :class="[
              'flex-1 px-4 py-3 rounded-2xl font-medium text-sm transition-all',
              formData.category === 'company'
                ? 'bg-brand-brown text-white shadow-md'
                : 'bg-black/5 text-black/60 hover:bg-black/10'
            ]"
            @click="formData.category = 'company'"
          >
            {{ t.profile.form.categoryCompany }}
          </button>
        </div>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.fullAddress }}
        </label>
        <input
          v-model="formData.fullAddress"
          type="text"
          :placeholder="t.profile.form.fullAddressPlaceholder"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
        />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">
            {{ t.profile.form.province }}
          </label>
          <select
            v-model="formData.province"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all bg-white"
            @change="onProvinceChange"
          >
            <option value="" disabled>{{ t.profile.form.provincePlaceholder }}</option>
            <option v-for="p in PROVINCES" :key="p.value" :value="p.value">{{ p.label }}</option>
          </select>
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-semibold text-black/70 tracking-tight">
            {{ t.profile.form.city }}
          </label>
          <select
            v-model="formData.city"
            class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all bg-white"
            :disabled="availableCities.length === 0"
            :class="{ 'border-red-300': errors.city }"
          >
            <option value="" disabled>{{ t.profile.form.cityDropdownPlaceholder }}</option>
            <option v-for="c in availableCities" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
          <p v-if="errors.city" class="text-xs text-red-600">{{ errors.city }}</p>
        </div>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.experience }}
        </label>
        <select
          v-model="formData.experienceRange"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all bg-white"
        >
          <option value="" disabled>{{ t.profile.form.experiencePlaceholder }}</option>
          <option v-for="option in EXPERIENCE_OPTIONS" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.philosophy }}
        </label>
        <textarea
          v-model="formData.philosophy"
          rows="6"
          :placeholder="t.profile.form.philosophyPlaceholder"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all resize-none"
        />
        <div class="flex justify-between items-center">
          <p class="text-xs text-black/40">{{ formData.philosophy.length }} characters</p>
        </div>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.expertise }}
        </label>
        <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
          <button
            v-for="tag in EXPERTISE_TAGS"
            :key="tag"
            type="button"
            :class="[
              'px-4 py-3 rounded-2xl font-medium text-sm transition-all',
              formData.expertise.includes(tag)
                ? 'bg-brand-brown text-white shadow-md scale-105'
                : 'bg-black/5 text-black/60 hover:bg-black/10'
            ]"
            @click="toggleExpertise(tag)"
          >
            {{ t.expertiseTagLabels?.[tag] || tag }}
          </button>
        </div>
        <p v-if="formData.expertise.length > 0" class="text-xs text-black/60">
          Selected {{ formData.expertise.length }} {{ formData.expertise.length === 1 ? 'expertise' : 'expertises' }}
        </p>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.fullnameKtp }}
        </label>
        <input
          v-model="formData.fullnameKtp"
          type="text"
          :placeholder="t.profile.form.fullnameKtpPlaceholder"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
        />
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.phoneNum }}
        </label>
        <input
          v-model="formData.phoneNum"
          type="tel"
          maxlength="16"
          inputmode="numeric"
          :placeholder="t.profile.form.phoneNumPlaceholder"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
          :class="{ 'border-red-300': errors.phoneNum }"
          @input="formData.phoneNum = formData.phoneNum.replace(/\D/g, '')"
        />
        <p class="text-xs text-black/40">8-16 digits required</p>
        <p v-if="errors.phoneNum" class="text-xs text-red-600">{{ errors.phoneNum }}</p>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.ktpNum }}
        </label>
        <input
          v-model="formData.ktpNum"
          type="text"
          maxlength="16"
          inputmode="numeric"
          :placeholder="t.profile.form.ktpNumPlaceholder"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
          :class="{ 'border-red-300': errors.ktpNum }"
          @input="formData.ktpNum = formData.ktpNum.replace(/\D/g, '')"
        />
        <p class="text-xs text-black/40">16 digits required</p>
        <p v-if="errors.ktpNum" class="text-xs text-red-600">{{ errors.ktpNum }}</p>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.npwp }}
        </label>
        <input
          v-model="formData.npwp"
          type="text"
          maxlength="16"
          inputmode="numeric"
          :placeholder="t.profile.form.npwpPlaceholder"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
          :class="{ 'border-red-300': errors.npwp }"
          @input="formData.npwp = formData.npwp.replace(/\D/g, '')"
        />
        <p class="text-xs text-black/40">15-16 digits required</p>
        <p v-if="errors.npwp" class="text-xs text-red-600">{{ errors.npwp }}</p>
      </div>
    </div>

    <BaseAlert v-if="error" variant="error">{{ error }}</BaseAlert>

    <BaseAlert v-if="hasIncompleteIdentity" variant="warning">
      Lengkapi Nama Lengkap sesuai KTP, KTP, dan NPWP untuk dapat mengajukan penawaran pada proyek.
    </BaseAlert>

    <div class="flex justify-end items-center gap-4">
      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition ease-in duration-300"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <p v-if="saveStatus === 'saving'" class="text-sm text-black/40 flex items-center gap-1.5">
          <svg class="w-3.5 h-3.5 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path
              class="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          {{ t.profile.autosave.saving }}
        </p>
      </Transition>

      <button
        type="button"
        class="px-8 py-3 rounded-full font-semibold transition-all bg-brand-brown text-white hover:bg-brand-brown-dark hover:scale-105"
        @click="handleDone"
      >
        {{ t.profile.done }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { useI18n } from '@/composables/useI18n'
import { EXPERTISE_TAGS, EXPERIENCE_OPTIONS } from '@/constants/architectProfileOptions'
import { PROVINCES, citiesFor } from '@/constants/regions'
import { useArchitectProfileStore } from '@/stores/architectProfile'
import BaseAlert from '@/components/ui/BaseAlert.vue'

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({
      name: '',
      category: '',
      city: '',
      province: '',
      fullAddress: '',
      experienceRange: '',
      philosophy: '',
      expertise: [],
      fullnameKtp: '',
      ktpNum: '',
      npwp: '',
      phoneNum: ''
    })
  },
  error: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['cancel', 'saved', 'save-error'])

const { t } = useI18n()
const architectProfileStore = useArchitectProfileStore()

const formData = ref({
  name: props.initialData.name || '',
  category: props.initialData.category || '',
  city: props.initialData.city || '',
  province: props.initialData.province || '',
  fullAddress: props.initialData.fullAddress || '',
  experienceRange: props.initialData.experienceRange || '',
  philosophy: props.initialData.philosophy || '',
  expertise: [...(props.initialData.expertise || [])],
  fullnameKtp: props.initialData.fullnameKtp || '',
  ktpNum: props.initialData.ktpNum || '',
  npwp: props.initialData.npwp || '',
  phoneNum: props.initialData.phoneNum || ''
})

const errors = ref({
  name: '',
  city: '',
  ktpNum: '',
  npwp: '',
  phoneNum: ''
})

const availableCities = computed(() => citiesFor(formData.value.province))

const onProvinceChange = () => {
  if (!availableCities.value.some(c => c.value === formData.value.city)) {
    formData.value.city = ''
  }
}

// Live format feedback only — a field with a formatting problem never blocks the rest
// of the form from autosaving; the store simply omits that one field until it's fixed.
const validateForm = () => {
  errors.value = {
    name: '',
    city: '',
    ktpNum: '',
    npwp: '',
    phoneNum: ''
  }

  if (formData.value.name.trim() && formData.value.name.trim().length < 2) {
    errors.value.name = t.value.profile.validation.nameMinLength
  }

  if (formData.value.ktpNum.trim() && !/^[0-9]{16}$/.test(formData.value.ktpNum.trim())) {
    errors.value.ktpNum = t.value.profile.validation.ktpNumInvalid
  }

  if (formData.value.npwp.trim() && !/^[0-9]{15,16}$/.test(formData.value.npwp.trim())) {
    errors.value.npwp = t.value.profile.validation.npwpInvalid
  }

  if (formData.value.phoneNum.trim() && !/^[0-9]{8,16}$/.test(formData.value.phoneNum.trim())) {
    errors.value.phoneNum = t.value.profile.validation.phoneNumInvalid
  }
}

const hasIncompleteIdentity = computed(() => {
  return (
    !formData.value.fullnameKtp.trim() ||
    !formData.value.ktpNum.trim() ||
    !formData.value.npwp.trim() ||
    !formData.value.phoneNum.trim()
  )
})

const toggleExpertise = tag => {
  const index = formData.value.expertise.indexOf(tag)
  if (index > -1) {
    formData.value.expertise.splice(index, 1)
  } else {
    formData.value.expertise.push(tag)
  }
}

// Autosave: debounced 1s after the last edit. The confirmation itself is emitted up to
// the parent, which surfaces it as a fixed-position toast — this form can be long enough
// to scroll past an inline indicator, so a scroll-independent confirmation is the only
// one guaranteed to actually be seen.
const AUTOSAVE_DELAY_MS = 1000
let debounceTimer = null
let lastSnapshot = JSON.stringify(formData.value)
let saveAgainAfterCurrent = false
const isSaving = ref(false)
const saveStatus = ref('idle') // idle | saving | error

const runAutosave = async () => {
  if (isSaving.value) {
    saveAgainAfterCurrent = true
    return
  }

  const snapshot = JSON.stringify(formData.value)
  if (snapshot === lastSnapshot) return

  isSaving.value = true
  saveStatus.value = 'saving'
  try {
    await architectProfileStore.updateProfile(formData.value)
    lastSnapshot = snapshot
    saveStatus.value = 'idle'
    emit('saved')
  } catch (err) {
    console.error('Autosave error:', err)
    saveStatus.value = 'error'
    emit('save-error')
  } finally {
    isSaving.value = false
    if (saveAgainAfterCurrent) {
      saveAgainAfterCurrent = false
      runAutosave()
    }
  }
}

const scheduleAutosave = () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(runAutosave, AUTOSAVE_DELAY_MS)
}

watch(
  formData,
  () => {
    validateForm()
    scheduleAutosave()
  },
  { deep: true }
)

const handleDone = async () => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
    debounceTimer = null
    await runAutosave()
  }
  emit('cancel')
}

onBeforeUnmount(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
})

// Profile photo — uploaded immediately on selection, independent of the autosave cycle.
const photoInput = ref(null)
const isUploadingPhoto = ref(false)
const photoError = ref('')

const photoPreview = computed(() => architectProfileStore.profilePhotoUrl)

const initials = computed(() =>
  (props.initialData.name || 'Architect')
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
)

const handlePhotoSelect = async e => {
  const file = e.target.files?.[0]
  if (!file) return

  photoError.value = ''
  isUploadingPhoto.value = true
  try {
    await architectProfileStore.uploadPhoto(file)
  } catch (err) {
    console.error('Photo upload error:', err)
    photoError.value = t.value.profile.toast.photoUpdateError
  } finally {
    isUploadingPhoto.value = false
    if (photoInput.value) {
      photoInput.value.value = ''
    }
  }
}
</script>
