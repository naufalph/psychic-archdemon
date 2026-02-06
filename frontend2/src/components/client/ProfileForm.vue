<template>
  <form @submit.prevent="handleSubmit" class="profile-form">
    <!-- Name Fields -->
    <div class="form-section">
      <div class="field-row">
        <div class="form-field" :class="{ 'field-disabled': ktpVerified }" :style="{ animationDelay: '0.05s' }">
          <label :for="`firstName-${uniqueId}`" class="field-label">
            {{ t.value.clientProfile.form.firstName }}
            <span v-if="!ktpVerified" class="required-mark">*</span>
            <span v-if="ktpVerified" class="lock-badge">
              <svg viewBox="0 0 20 20" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z"
                  clip-rule="evenodd"
                />
              </svg>
              {{ t.value.clientProfile.form.locked }}
            </span>
          </label>
          <div class="input-wrapper">
            <input
              :id="`firstName-${uniqueId}`"
              v-model="formData.firstName"
              type="text"
              autocomplete="given-name"
              :placeholder="t.value.clientProfile.form.firstNamePlaceholder"
              :disabled="ktpVerified"
              class="field-input"
              :class="{ 'has-error': errors.firstName }"
              @focus="onFieldFocus('firstName')"
              @blur="onFieldBlur('firstName')"
            />
            <div v-if="focusedField === 'firstName' && !ktpVerified" class="input-highlight"></div>
          </div>
          <Transition name="error-slide">
            <p v-if="errors.firstName" class="field-error">
              <svg viewBox="0 0 20 20" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                  clip-rule="evenodd"
                />
              </svg>
              {{ errors.firstName }}
            </p>
          </Transition>
          <p v-if="ktpVerified" class="field-hint">{{ t.value.clientProfile.form.nameReadOnly }}</p>
        </div>

        <div class="form-field" :class="{ 'field-disabled': ktpVerified }" :style="{ animationDelay: '0.1s' }">
          <label :for="`lastName-${uniqueId}`" class="field-label">
            {{ t.value.clientProfile.form.lastName }}
            <span v-if="!ktpVerified" class="required-mark">*</span>
            <span v-if="ktpVerified" class="lock-badge">
              <svg viewBox="0 0 20 20" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z"
                  clip-rule="evenodd"
                />
              </svg>
              {{ t.value.clientProfile.form.locked }}
            </span>
          </label>
          <div class="input-wrapper">
            <input
              :id="`lastName-${uniqueId}`"
              v-model="formData.lastName"
              type="text"
              autocomplete="family-name"
              :placeholder="t.value.clientProfile.form.lastNamePlaceholder"
              :disabled="ktpVerified"
              class="field-input"
              :class="{ 'has-error': errors.lastName }"
              @focus="onFieldFocus('lastName')"
              @blur="onFieldBlur('lastName')"
            />
            <div v-if="focusedField === 'lastName' && !ktpVerified" class="input-highlight"></div>
          </div>
          <Transition name="error-slide">
            <p v-if="errors.lastName" class="field-error">
              <svg viewBox="0 0 20 20" fill="currentColor">
                <path
                  fill-rule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                  clip-rule="evenodd"
                />
              </svg>
              {{ errors.lastName }}
            </p>
          </Transition>
          <p v-if="ktpVerified" class="field-hint">{{ t.value.clientProfile.form.nameReadOnly }}</p>
        </div>
      </div>
    </div>

    <!-- Email Field (Read-only) -->
    <div class="form-field field-readonly" :style="{ animationDelay: '0.15s' }">
      <label class="field-label">{{ t.value.clientProfile.viewMode.email }}</label>
      <div class="readonly-display">
        <svg class="readonly-icon" viewBox="0 0 20 20" fill="currentColor">
          <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
          <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
        </svg>
        <span>{{ profileEmail }}</span>
      </div>
      <p class="field-hint">{{ t.value.clientProfile.form.readOnly }}</p>
    </div>

    <!-- Phone Field -->
    <div class="form-field" :style="{ animationDelay: '0.2s' }">
      <label :for="`phoneNumber-${uniqueId}`" class="field-label">
        {{ t.value.clientProfile.form.phoneNumber }}
        <span class="required-mark">*</span>
      </label>
      <div class="input-wrapper">
        <div class="input-icon">
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path
              d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"
            />
          </svg>
        </div>
        <input
          :id="`phoneNumber-${uniqueId}`"
          v-model="formData.phoneNumber"
          type="tel"
          autocomplete="tel"
          :placeholder="t.value.clientProfile.form.phoneNumberPlaceholder"
          class="field-input has-icon"
          :class="{ 'has-error': errors.phoneNumber }"
          @focus="onFieldFocus('phoneNumber')"
          @blur="onFieldBlur('phoneNumber')"
        />
        <div v-if="focusedField === 'phoneNumber'" class="input-highlight"></div>
      </div>
      <Transition name="error-slide">
        <p v-if="errors.phoneNumber" class="field-error">
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path
              fill-rule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
              clip-rule="evenodd"
            />
          </svg>
          {{ errors.phoneNumber }}
        </p>
      </Transition>
    </div>

    <!-- KTP Field -->
    <div class="form-field" :class="{ 'field-disabled': ktpVerified }" :style="{ animationDelay: '0.25s' }">
      <label :for="`ktpNumber-${uniqueId}`" class="field-label">
        {{ t.value.clientProfile.form.ktpNumber }}
        <span v-if="!ktpVerified" class="required-mark">*</span>
        <span v-if="ktpVerified" class="verified-badge">
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path
              fill-rule="evenodd"
              d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
              clip-rule="evenodd"
            />
          </svg>
          {{ t.value.clientProfile.form.verified }}
        </span>
      </label>
      <div class="input-wrapper">
        <div class="input-icon">
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path
              fill-rule="evenodd"
              d="M10 2a1 1 0 00-1 1v1a1 1 0 002 0V3a1 1 0 00-1-1zM4 4h3a3 3 0 006 0h3a2 2 0 012 2v9a2 2 0 01-2 2H4a2 2 0 01-2-2V6a2 2 0 012-2zm2.5 7a1.5 1.5 0 100-3 1.5 1.5 0 000 3zm2.45 4a2.5 2.5 0 10-4.9 0h4.9zM12 9a1 1 0 100 2h3a1 1 0 100-2h-3zm-1 4a1 1 0 011-1h2a1 1 0 110 2h-2a1 1 0 01-1-1z"
              clip-rule="evenodd"
            />
          </svg>
        </div>
        <input
          :id="`ktpNumber-${uniqueId}`"
          v-model="formData.ktpNum"
          type="text"
          maxlength="16"
          inputmode="numeric"
          autocomplete="off"
          :placeholder="t.value.clientProfile.form.ktpNumberPlaceholder"
          :disabled="ktpVerified"
          class="field-input has-icon"
          :class="{ 'has-error': errors.ktpNum }"
          @focus="onFieldFocus('ktpNum')"
          @blur="onFieldBlur('ktpNum')"
        />
        <div v-if="focusedField === 'ktpNum' && !ktpVerified" class="input-highlight"></div>
      </div>
      <Transition name="error-slide">
        <p v-if="errors.ktpNum" class="field-error">
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path
              fill-rule="evenodd"
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
              clip-rule="evenodd"
            />
          </svg>
          {{ errors.ktpNum }}
        </p>
      </Transition>
      <p v-if="ktpVerified" class="field-hint">{{ t.value.clientProfile.form.ktpReadOnly }}</p>
    </div>

    <!-- Action Buttons -->
    <div class="form-actions">
      <button type="button" @click="handleCancel" :disabled="isLoading" class="btn btn-secondary">
        <svg viewBox="0 0 20 20" fill="currentColor">
          <path
            fill-rule="evenodd"
            d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
            clip-rule="evenodd"
          />
        </svg>
        <span>{{ t.value.clientProfile.cancel }}</span>
      </button>
      <button type="submit" :disabled="isLoading" class="btn btn-primary">
        <Transition name="btn-content" mode="out-in">
          <span v-if="!isLoading" key="save" class="btn-content">
            <svg viewBox="0 0 20 20" fill="currentColor">
              <path
                d="M7.707 10.293a1 1 0 10-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 00-1.414-1.414L11 11.586V6h5a2 2 0 012 2v7a2 2 0 01-2 2H4a2 2 0 01-2-2V8a2 2 0 012-2h5v5.586l-1.293-1.293zM9 4a1 1 0 012 0v2H9V4z"
              />
            </svg>
            <span>{{ t.value.clientProfile.saveProfile }}</span>
          </span>
          <span v-else key="loading" class="btn-content">
            <svg class="loading-spinner" viewBox="0 0 24 24">
              <circle class="spinner-track" cx="12" cy="12" r="10" stroke-width="4" fill="none" />
              <circle class="spinner-head" cx="12" cy="12" r="10" stroke-width="4" fill="none" />
            </svg>
            <span>{{ t.value.common.saving }}</span>
          </span>
        </Transition>
      </button>
    </div>
  </form>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
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

const t = useI18n()
const uniqueId = Math.random().toString(36).substring(2, 9)
const focusedField = ref(null)

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

const onFieldFocus = fieldName => {
  focusedField.value = fieldName
}

const onFieldBlur = fieldName => {
  focusedField.value = null
}

const validateForm = () => {
  let isValid = true
  errors.firstName = ''
  errors.lastName = ''
  errors.phoneNumber = ''
  errors.ktpNum = ''

  if (!props.ktpVerified) {
    if (!formData.firstName.trim()) {
      errors.firstName = t.value.clientProfile.validation.firstNameRequired
      isValid = false
    }

    if (!formData.lastName.trim()) {
      errors.lastName = t.value.clientProfile.validation.lastNameRequired
      isValid = false
    }
  }

  if (!formData.phoneNumber.trim()) {
    errors.phoneNumber = t.value.clientProfile.validation.phoneRequired
    isValid = false
  } else if (!/^\+?[0-9\s-]{10,16}$/.test(formData.phoneNumber)) {
    errors.phoneNumber = t.value.clientProfile.validation.phoneInvalid
    isValid = false
  }

  if (!props.ktpVerified) {
    if (!formData.ktpNum.trim()) {
      errors.ktpNum = t.value.clientProfile.validation.ktpRequired
      isValid = false
    } else if (!/^\d{16}$/.test(formData.ktpNum)) {
      errors.ktpNum = t.value.clientProfile.validation.ktpInvalid
      isValid = false
    }
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

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700&family=DM+Sans:wght@400;500;600&display=swap');

.profile-form {
  font-family:
    'DM Sans',
    -apple-system,
    BlinkMacSystemFont,
    sans-serif;
}

.form-section {
  margin-bottom: 1.5rem;
}

.field-row {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
}

.form-field {
  animation: fadeSlideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) backwards;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.625rem;
}

.required-mark {
  color: #ef4444;
  font-size: 1rem;
}

.lock-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: #64748b;
  background: #f1f5f9;
  padding: 0.25rem 0.5rem;
  border-radius: 0.375rem;
}

.lock-badge svg {
  width: 0.875rem;
  height: 0.875rem;
}

.verified-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: #92400e;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  padding: 0.25rem 0.5rem;
  border-radius: 0.375rem;
  border: 1px solid #fcd34d;
}

.verified-badge svg {
  width: 0.875rem;
  height: 0.875rem;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  width: 1.25rem;
  height: 1.25rem;
  pointer-events: none;
  z-index: 1;
  transition: color 0.2s;
}

.field-input {
  width: 100%;
  padding: 0.875rem 1rem;
  font-size: 0.9375rem;
  font-weight: 500;
  color: #1e293b;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 0.875rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  font-family: 'DM Sans', sans-serif;
}

.field-input.has-icon {
  padding-left: 3rem;
}

.field-input:hover:not(:disabled) {
  border-color: #cbd5e1;
}

.field-input:focus:not(:disabled) {
  border-color: #4f46e5;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
}

.field-input:focus:not(:disabled) ~ .input-icon {
  color: #4f46e5;
}

.field-input:disabled {
  background: #f8fafc;
  color: #94a3b8;
  cursor: not-allowed;
  border-color: #e2e8f0;
}

.field-input.has-error {
  border-color: #ef4444;
}

.field-input.has-error:focus {
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1);
}

.input-highlight {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #4f46e5, #6366f1);
  border-radius: 0 0 0.875rem 0.875rem;
  animation: expandWidth 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.field-readonly {
  opacity: 0.8;
}

.readonly-display {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 0.875rem;
  color: #64748b;
  font-size: 0.9375rem;
  font-weight: 500;
}

.readonly-icon {
  width: 1.25rem;
  height: 1.25rem;
  color: #94a3b8;
  flex-shrink: 0;
}

.field-hint {
  margin: 0.5rem 0 0 0;
  font-size: 0.8125rem;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.field-error {
  margin: 0.5rem 0 0 0;
  font-size: 0.8125rem;
  color: #ef4444;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.field-error svg {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
}

.field-disabled {
  opacity: 0.65;
}

/* Action Buttons */
.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2.5rem;
  animation: fadeSlideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) 0.3s backwards;
}

.btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.625rem;
  padding: 0.875rem 1.75rem;
  font-size: 0.9375rem;
  font-weight: 600;
  border-radius: 0.875rem;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  font-family: 'DM Sans', sans-serif;
}

.btn svg {
  width: 1.125rem;
  height: 1.125rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.3);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-secondary {
  background: white;
  color: #64748b;
  border: 2px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.btn-secondary:hover:not(:disabled) {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #1e293b;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.625rem;
}

.loading-spinner {
  width: 1.125rem;
  height: 1.125rem;
  animation: spin 1s linear infinite;
}

.spinner-track {
  stroke: rgba(255, 255, 255, 0.3);
}

.spinner-head {
  stroke: white;
  stroke-linecap: round;
  stroke-dasharray: 50;
  stroke-dashoffset: 30;
}

/* Animations */
@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes expandWidth {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Transitions */
.error-slide-enter-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.error-slide-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 1, 1);
}

.error-slide-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.error-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.btn-content-enter-active,
.btn-content-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-content-enter-from {
  opacity: 0;
  transform: scale(0.9);
}

.btn-content-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

/* Responsive */
@media (max-width: 768px) {
  .field-row {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
  }
}
</style>
