<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <label for="firstName" class="block text-sm font-medium text-gray-700 mb-2">
          {{ t.clientProfile.form.firstName }}
          <span v-if="!ktpVerified" class="text-red-500">*</span>
          <span v-if="ktpVerified" class="inline-flex items-center gap-1 text-gray-500 text-xs ml-2">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path
                fill-rule="evenodd"
                d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z"
                clip-rule="evenodd"
              />
            </svg>
            {{ t.clientProfile.form.locked }}
          </span>
        </label>
        <input
          id="firstName"
          v-model="formData.firstName"
          type="text"
          autocomplete="given-name"
          :placeholder="t.clientProfile.form.firstNamePlaceholder"
          :disabled="ktpVerified"
          class="w-full px-4 py-3 rounded-lg border focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
          :class="{
            'bg-gray-100 cursor-not-allowed text-gray-500': ktpVerified,
            'border-red-300': errors.firstName,
            'border-gray-300': !errors.firstName
          }"
        />
        <p v-if="ktpVerified" class="mt-1 text-xs text-gray-500">
          {{ t.clientProfile.form.nameReadOnly }}
        </p>
        <p v-if="errors.firstName" class="mt-1 text-sm text-red-600">
          {{ errors.firstName }}
        </p>
      </div>

      <div>
        <label for="lastName" class="block text-sm font-medium text-gray-700 mb-2">
          {{ t.clientProfile.form.lastName }}
          <span v-if="!ktpVerified" class="text-red-500">*</span>
          <span v-if="ktpVerified" class="inline-flex items-center gap-1 text-gray-500 text-xs ml-2">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path
                fill-rule="evenodd"
                d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z"
                clip-rule="evenodd"
              />
            </svg>
            {{ t.clientProfile.form.locked }}
          </span>
        </label>
        <input
          id="lastName"
          v-model="formData.lastName"
          type="text"
          autocomplete="family-name"
          :placeholder="t.clientProfile.form.lastNamePlaceholder"
          :disabled="ktpVerified"
          class="w-full px-4 py-3 rounded-lg border focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
          :class="{
            'bg-gray-100 cursor-not-allowed text-gray-500': ktpVerified,
            'border-red-300': errors.lastName,
            'border-gray-300': !errors.lastName
          }"
        />
        <p v-if="ktpVerified" class="mt-1 text-xs text-gray-500">
          {{ t.clientProfile.form.nameReadOnly }}
        </p>
        <p v-if="errors.lastName" class="mt-1 text-sm text-red-600">
          {{ errors.lastName }}
        </p>
      </div>
    </div>

    <div>
      <label class="block text-sm font-medium text-gray-700 mb-2">
        {{ t.clientProfile.viewMode.email }}
      </label>
      <div class="px-4 py-3 bg-gray-50 rounded-lg border border-gray-200 text-gray-600">
        {{ profileEmail }}
      </div>
      <p class="mt-1 text-xs text-gray-500">{{ t.clientProfile.form.readOnly }}</p>
    </div>

    <div>
      <label for="phoneNumber" class="block text-sm font-medium text-gray-700 mb-2">
        {{ t.clientProfile.form.phoneNumber }} <span class="text-red-500">*</span>
      </label>
      <input
        id="phoneNumber"
        v-model="formData.phoneNumber"
        type="tel"
        autocomplete="tel"
        :placeholder="t.clientProfile.form.phoneNumberPlaceholder"
        class="w-full px-4 py-3 rounded-lg border focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
        :class="errors.phoneNumber ? 'border-red-300' : 'border-gray-300'"
      />
      <p v-if="errors.phoneNumber" class="mt-1 text-sm text-red-600">
        {{ errors.phoneNumber }}
      </p>
    </div>

    <div>
      <label for="ktpNumber" class="block text-sm font-medium text-gray-700 mb-2">
        {{ t.clientProfile.form.ktpNumber }}
        <span v-if="!ktpVerified" class="text-gray-400 font-normal">({{ t.common.optional }})</span>
        <span v-if="ktpVerified" class="inline-flex items-center gap-1 text-green-600 text-xs ml-2">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
              clip-rule="evenodd"
            />
          </svg>
          {{ t.clientProfile.form.verified }}
        </span>
      </label>
      <input
        id="ktpNumber"
        v-model="formData.ktpNum"
        type="text"
        maxlength="16"
        inputmode="numeric"
        autocomplete="off"
        :placeholder="t.clientProfile.form.ktpNumberPlaceholder"
        :disabled="ktpVerified"
        class="w-full px-4 py-3 rounded-lg border focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
        :class="{
          'bg-gray-100 cursor-not-allowed text-gray-500': ktpVerified,
          'border-red-300': errors.ktpNum,
          'border-gray-300': !errors.ktpNum
        }"
      />
      <p v-if="ktpVerified" class="mt-1 text-xs text-gray-500">
        {{ t.clientProfile.form.ktpReadOnly }}
      </p>
      <p v-if="errors.ktpNum" class="mt-1 text-sm text-red-600">
        {{ errors.ktpNum }}
      </p>
    </div>

    <div class="flex gap-3 pt-4">
      <button
        type="submit"
        :disabled="isLoading"
        class="flex-1 px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span v-if="!isLoading">{{ t.clientProfile.saveProfile }}</span>
        <span v-else class="flex items-center justify-center gap-2">
          <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none" />
            <path
              class="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          {{ t.common.saving }}
        </span>
      </button>

      <button
        type="button"
        @click="handleCancel"
        :disabled="isLoading"
        class="px-6 py-3 bg-white text-gray-700 font-semibold rounded-lg border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {{ t.clientProfile.cancel }}
      </button>
    </div>
  </form>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/composables/useI18n'

const props = defineProps({
  profileEmail: {
    type: String,
    default: ''
  },
  ktpNumber: {
    type: String,
    default: ''
  },
  ktpVerified: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: () => ({
      firstName: '',
      lastName: '',
      phoneNumber: '',
      ktpNum: ''
    })
  },
  isLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancel'])

const { t } = useI18n()

const formData = reactive({
  firstName: props.initialData.firstName || '',
  lastName: props.initialData.lastName || '',
  phoneNumber: props.initialData.phoneNumber || '',
  ktpNum: props.initialData.ktpNum || props.ktpNumber || ''
})

const errors = reactive({
  firstName: '',
  lastName: '',
  phoneNumber: '',
  ktpNum: ''
})

watch(
  () => props.initialData,
  newData => {
    formData.firstName = newData.firstName || ''
    formData.lastName = newData.lastName || ''
    formData.phoneNumber = newData.phoneNumber || ''
    formData.ktpNum = newData.ktpNum || ''
  },
  { deep: true }
)

const validateForm = () => {
  let isValid = true
  errors.firstName = ''
  errors.lastName = ''
  errors.phoneNumber = ''
  errors.ktpNum = ''

  if (!props.ktpVerified) {
    if (!formData.firstName.trim()) {
      errors.firstName = t.clientProfile.validation.firstNameRequired
      isValid = false
    }

    if (!formData.lastName.trim()) {
      errors.lastName = t.clientProfile.validation.lastNameRequired
      isValid = false
    }
  }

  if (!formData.phoneNumber.trim()) {
    errors.phoneNumber = t.clientProfile.validation.phoneRequired
    isValid = false
  } else if (!/^\+?[0-9\s-]{10,16}$/.test(formData.phoneNumber)) {
    errors.phoneNumber = t.clientProfile.validation.phoneInvalid
    isValid = false
  }

  if (!props.ktpVerified && formData.ktpNum.trim() && !/^\d{16}$/.test(formData.ktpNum)) {
    errors.ktpNum = t.clientProfile.validation.ktpInvalid
    isValid = false
  }

  return isValid
}

const handleSubmit = () => {
  if (validateForm()) {
    emit('submit', { ...formData })
  }
}

const handleCancel = () => {
  emit('cancel')
}
</script>
