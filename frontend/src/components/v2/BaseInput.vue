<!--
  BaseInput Component - V2 Design System

  A reusable input field with floating label design matching Figma specs.

  @example
  <BaseInput
    v-model="email"
    label="Email"
    type="email"
    placeholder="Input here"
  />

  @example Password field with toggle
  <BaseInput
    v-model="password"
    label="Password"
    type="password"
    placeholder="Input password here"
  />

  @example Disabled state
  <BaseInput
    v-model="username"
    label="Username"
    :disabled="true"
  />
-->

<template>
  <div class="relative w-full">
    <!-- Floating Label (positioned above border) -->
    <label
      :for="id"
      class="absolute -top-[9px] left-[18px] bg-white px-[5px] z-10"
    >
      <span
        class="text-sm font-normal leading-[18px] font-poppins"
        :class="hasError ? 'text-red-500' : 'text-black'"
      >
        {{ label }}
      </span>
    </label>

    <!-- Input Container -->
    <div class="relative">
      <!-- Input Field -->
      <input
        :id="id"
        :type="inputType"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :autocomplete="type === 'password' ? 'new-password' : 'off'"
        :name="id"
        class="w-full h-[40px] px-[18px] border rounded-lg text-xs font-normal leading-5 outline-none bg-white placeholder:text-slate-500 font-poppins disabled:text-gray-400 disabled:bg-gray-50 transition-colors autofill-white"
        :class="[
          disabled ? 'border-gray-300' : 'border-slate-700',
          hasError ? 'border-red-500' : ''
        ]"
        @input="handleInput"
        @blur="$emit('blur', $event)"
        @focus="$emit('focus', $event)"
      />

      <!-- Password Toggle Eye Icon -->
      <button
        v-if="type === 'password'"
        type="button"
        class="absolute right-[13px] top-1/2 -translate-y-1/2 hover:opacity-70 transition-opacity"
        tabindex="-1"
        @click="togglePassword"
      >
        <img
          src="@/assets/images/sign/eye-icon.svg"
          alt="Toggle password visibility"
          class="w-[14px] h-[14px]"
        />
      </button>
    </div>

    <!-- Error Message -->
    <p v-if="errorMessage" class="mt-1 text-xs text-red-500 font-poppins">
      {{ errorMessage }}
    </p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

/**
 * Props
 * @property {String} id - Unique identifier for the input (auto-generated if not provided)
 * @property {String} modelValue - v-model binding value
 * @property {String} label - Floating label text (required)
 * @property {String} type - Input type: 'text', 'email', 'password', etc. Default: 'text'
 * @property {String} placeholder - Placeholder text shown inside input. Default: 'Input here'
 * @property {Boolean} disabled - Whether input is disabled. Default: false
 * @property {String} errorMessage - Error message to display below input
 */
const props = defineProps({
  id: {
    type: String,
    default: () => `input-${Math.random().toString(36).substr(2, 9)}`
  },
  modelValue: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'text',
    validator: (value) => ['text', 'email', 'password', 'tel', 'number', 'url'].includes(value)
  },
  placeholder: {
    type: String,
    default: 'Input here'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  errorMessage: {
    type: String,
    default: ''
  }
})

/**
 * Emits
 * @event update:modelValue - Emitted when input value changes
 * @event blur - Emitted when input loses focus
 * @event focus - Emitted when input gains focus
 */
const emit = defineEmits(['update:modelValue', 'blur', 'focus'])

// Password visibility toggle
const showPassword = ref(false)

// Computed input type (handles password toggle)
const inputType = computed(() => {
  if (props.type === 'password') {
    return showPassword.value ? 'text' : 'password'
  }
  return props.type
})

// Check if input has error
const hasError = computed(() => props.errorMessage && props.errorMessage.length > 0)

/**
 * Toggles password visibility
 */
const togglePassword = () => {
  showPassword.value = !showPassword.value
}

/**
 * Handles input event and emits update
 */
const handleInput = (event) => {
  emit('update:modelValue', event.target.value)
}
</script>

<style scoped>
/* Ensure Poppins font is loaded */
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.font-poppins {
  font-family: 'Poppins', sans-serif;
}

/* Override browser autofill styling - keep background white */
.autofill-white:-webkit-autofill,
.autofill-white:-webkit-autofill:hover,
.autofill-white:-webkit-autofill:focus,
.autofill-white:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 30px white inset !important;
  box-shadow: 0 0 0 30px white inset !important;
  -webkit-text-fill-color: #000000 !important;
}
</style>
