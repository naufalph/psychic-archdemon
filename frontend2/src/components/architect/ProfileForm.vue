<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <div class="bg-white rounded-3xl p-8 shadow-sm border border-black/5 space-y-6">
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
          {{ t.profile.form.city }}
        </label>
        <input
          v-model="formData.city"
          type="text"
          :placeholder="t.profile.form.cityPlaceholder"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all"
          :class="{ 'border-red-300': errors.city }"
        />
        <p v-if="errors.city" class="text-xs text-red-600">{{ errors.city }}</p>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-black/70 tracking-tight">
          {{ t.profile.form.experience }}
        </label>
        <select
          v-model="formData.experienceRange"
          class="w-full px-4 py-3 rounded-2xl border border-black/10 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition-all bg-white"
          :class="{ 'border-red-300': errors.experienceRange }"
        >
          <option value="" disabled>{{ t.profile.form.experiencePlaceholder }}</option>
          <option v-for="option in EXPERIENCE_OPTIONS" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
        <p v-if="errors.experienceRange" class="text-xs text-red-600">
          {{ errors.experienceRange }}
        </p>
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
          :class="{ 'border-red-300': errors.philosophy }"
        />
        <div class="flex justify-between items-center">
          <p class="text-xs text-black/40">{{ formData.philosophy.length }} characters</p>
        </div>
        <p v-if="errors.philosophy" class="text-xs text-red-600">{{ errors.philosophy }}</p>
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
            @click="toggleExpertise(tag)"
            :class="[
              'px-4 py-3 rounded-2xl font-medium text-sm transition-all',
              formData.expertise.includes(tag)
                ? 'bg-brand-brown text-white shadow-md scale-105'
                : 'bg-black/5 text-black/60 hover:bg-black/10'
            ]"
          >
            {{ tag }}
          </button>
        </div>
        <p v-if="formData.expertise.length > 0" class="text-xs text-black/60">
          Selected {{ formData.expertise.length }} {{ formData.expertise.length === 1 ? 'expertise' : 'expertises' }}
        </p>
        <p v-if="errors.expertise" class="text-xs text-red-600">{{ errors.expertise }}</p>
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
      Lengkapi KTP dan NPWP untuk dapat mengajukan penawaran pada proyek.
    </BaseAlert>

    <div class="flex justify-end items-center gap-4">
      <button
        type="button"
        @click="$emit.cancel"
        class="px-6 py-3 text-black/60 hover:text-black transition-colors font-semibold"
      >
        {{ t.profile.cancel }}
      </button>
      <button
        type="submit"
        :disabled="!isFormValid || isLoading"
        :class="[
          'px-8 py-3 rounded-full font-semibold transition-all flex items-center gap-2',
          isFormValid && !isLoading
            ? 'bg-brand-brown text-white hover:bg-brand-brown-dark hover:scale-105'
            : 'bg-black/10 text-black/30 cursor-not-allowed'
        ]"
      >
        <svg v-if="isLoading" class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        {{ isLoading ? 'Saving...' : t.profile.saveProfile }}
      </button>
    </div>
  </form>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useI18n } from '@/composables/useI18n'
import { EXPERTISE_TAGS, EXPERIENCE_OPTIONS } from '@/constants/onboarding'
import BaseAlert from '@/components/ui/BaseAlert.vue'

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({
      name: '',
      city: '',
      experienceRange: '',
      philosophy: '',
      expertise: [],
      ktpNum: '',
      npwp: ''
    })
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['submit', 'cancel'])

const { t } = useI18n()

const formData = ref({
  name: props.initialData.name || '',
  city: props.initialData.city || '',
  experienceRange: props.initialData.experienceRange || '',
  philosophy: props.initialData.philosophy || '',
  expertise: [...(props.initialData.expertise || [])],
  ktpNum: props.initialData.ktpNum || '',
  npwp: props.initialData.npwp || ''
})

const errors = ref({
  name: '',
  city: '',
  experienceRange: '',
  philosophy: '',
  expertise: '',
  ktpNum: '',
  npwp: ''
})

watch(
  () => props.initialData,
  newData => {
    if (newData) {
      formData.value = {
        name: newData.name || '',
        city: newData.city || '',
        experienceRange: newData.experienceRange || '',
        philosophy: newData.philosophy || '',
        expertise: [...(newData.expertise || [])],
        ktpNum: newData.ktpNum || '',
        npwp: newData.npwp || ''
      }
    }
  }
)

const validateForm = () => {
  errors.value = {
    name: '',
    city: '',
    experienceRange: '',
    philosophy: '',
    expertise: '',
    ktpNum: '',
    npwp: ''
  }

  let isValid = true

  if (!formData.value.name.trim()) {
    errors.value.name = t.profile.validation.nameRequired
    isValid = false
  } else if (formData.value.name.trim().length < 2) {
    errors.value.name = t.profile.validation.nameMinLength
    isValid = false
  }

  if (!formData.value.city.trim()) {
    errors.value.city = t.profile.validation.cityRequired
    isValid = false
  } else if (formData.value.city.trim().length < 2) {
    errors.value.city = t.profile.validation.cityMinLength
    isValid = false
  }

  if (!formData.value.experienceRange) {
    errors.value.experienceRange = t.profile.validation.experienceRequired
    isValid = false
  }

  if (!formData.value.philosophy.trim()) {
    errors.value.philosophy = t.profile.validation.philosophyRequired
    isValid = false
  } else if (formData.value.philosophy.trim().length < 50) {
    errors.value.philosophy = t.profile.validation.philosophyMinLength
    isValid = false
  }

  if (formData.value.expertise.length === 0) {
    errors.value.expertise = t.profile.validation.expertiseRequired
    isValid = false
  }

  if (!formData.value.ktpNum.trim()) {
    errors.value.ktpNum = t.profile.validation.ktpNumRequired
    isValid = false
  } else if (!/^[0-9]{16}$/.test(formData.value.ktpNum.trim())) {
    errors.value.ktpNum = t.profile.validation.ktpNumInvalid
    isValid = false
  }

  if (!formData.value.npwp.trim()) {
    errors.value.npwp = t.profile.validation.npwpRequired
    isValid = false
  } else if (!/^[0-9]{15,16}$/.test(formData.value.npwp.trim())) {
    errors.value.npwp = t.profile.validation.npwpInvalid
    isValid = false
  }

  return isValid
}

const hasDataChanged = computed(() => {
  const initial = props.initialData
  const current = formData.value

  if (initial.name !== current.name) return true
  if (initial.city !== current.city) return true
  if (initial.experienceRange !== current.experienceRange) return true
  if (initial.philosophy !== current.philosophy) return true
  if (initial.ktpNum !== current.ktpNum) return true
  if (initial.npwp !== current.npwp) return true

  if (initial.expertise.length !== current.expertise.length) return true
  const sortedInitial = [...initial.expertise].sort()
  const sortedCurrent = [...current.expertise].sort()
  for (let i = 0; i < sortedInitial.length; i++) {
    if (sortedInitial[i] !== sortedCurrent[i]) return true
  }

  return false
})

const meetsValidationRequirements = computed(() => {
  return (
    formData.value.name.trim().length >= 2 &&
    formData.value.city.trim().length >= 2 &&
    formData.value.experienceRange &&
    formData.value.expertise.length > 0
  )
})

const hasIncompleteIdentity = computed(() => {
  return !formData.value.ktpNum.trim() || !formData.value.npwp.trim()
})

const isFormValid = computed(() => {
  return meetsValidationRequirements.value && hasDataChanged.value
})

const toggleExpertise = tag => {
  const index = formData.value.expertise.indexOf(tag)
  if (index > -1) {
    formData.value.expertise.splice(index, 1)
  } else {
    formData.value.expertise.push(tag)
  }
  errors.value.expertise = ''
}

const handleSubmit = () => {
  if (validateForm()) {
    emit('submit', formData.value)
  }
}
</script>
