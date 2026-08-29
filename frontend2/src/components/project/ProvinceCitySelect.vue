<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
    <div class="space-y-2">
      <label class="block text-sm font-medium text-gray-700">
        {{ t.profile.form.province }}<span v-if="required" class="text-red-500">*</span>
      </label>
      <select
        :value="province"
        :required="required"
        :class="selectClass"
        @change="onProvinceChange($event.target.value)"
      >
        <option value="" disabled>{{ t.profile.form.provincePlaceholder }}</option>
        <option v-for="p in PROVINCES" :key="p.value" :value="p.value">{{ p.label }}</option>
      </select>
    </div>

    <div class="space-y-2">
      <label class="block text-sm font-medium text-gray-700">
        {{ t.profile.form.city }}<span v-if="required" class="text-red-500">*</span>
      </label>
      <select
        :value="city"
        :required="required"
        :disabled="availableCities.length === 0"
        :class="[selectClass, error ? 'border-red-300' : '']"
        @change="$emit('update:city', $event.target.value)"
      >
        <option value="" disabled>{{ t.profile.form.cityDropdownPlaceholder }}</option>
        <option v-for="c in availableCities" :key="c.value" :value="c.value">{{ c.label }}</option>
      </select>
      <p v-if="error" class="text-xs text-red-600">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '@/composables/useI18n'
import { PROVINCES, citiesFor } from '@/constants/regions'

const props = defineProps({
  province: { type: String, default: '' },
  city: { type: String, default: '' },
  required: { type: Boolean, default: false },
  error: { type: String, default: '' }
})

const emit = defineEmits(['update:province', 'update:city'])

const { t } = useI18n()

const selectClass =
  'w-full px-4 py-3 rounded-2xl border-2 border-gray-200 focus:border-brand-brown focus:ring-2 focus:ring-brand-brown/20 outline-none transition bg-white disabled:bg-gray-50 disabled:text-gray-400'

const availableCities = computed(() => citiesFor(props.province))

// A city only means something under its own province, so drop it when the province moves
// rather than keeping a value that no longer belongs.
const onProvinceChange = value => {
  emit('update:province', value)
  if (!citiesFor(value).some(c => c.value === props.city)) {
    emit('update:city', '')
  }
}
</script>
