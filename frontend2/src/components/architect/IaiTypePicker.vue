<template>
  <div class="space-y-2">
    <div
      v-for="category in IAI_CATEGORIES"
      :key="category.value"
      class="border rounded-xl overflow-hidden transition"
      :class="error ? 'border-red-300' : 'border-black/10'"
    >
      <button
        type="button"
        class="w-full flex items-center justify-between gap-3 px-4 py-3 text-left bg-brand-cream/60 hover:bg-brand-tan/40 focus:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-brand-brown/40 transition"
        :aria-expanded="isExpanded(category.value)"
        :aria-controls="panelIdFor(category.value)"
        @click="toggleGroup(category.value)"
      >
        <span class="flex items-center gap-2 min-w-0">
          <ChevronRight
            :size="16"
            class="shrink-0 text-brand-brown transition-transform"
            :class="{ 'rotate-90': isExpanded(category.value) }"
          />
          <span class="text-sm font-semibold text-brand-brown-900 truncate">
            {{ iaiCategoryLabel(category.value, locale) }}
          </span>
        </span>
        <span
          v-if="countFor(category.value) > 0"
          class="shrink-0 text-xs font-bold text-white bg-brand-brown rounded-full px-2 py-0.5"
        >
          {{ countFor(category.value) }}
        </span>
      </button>

      <fieldset
        v-show="isExpanded(category.value)"
        :id="panelIdFor(category.value)"
        class="px-4 py-3 space-y-1 border-t border-black/10"
        :disabled="disabled"
      >
        <legend class="sr-only">{{ iaiCategoryLabel(category.value, locale) }}</legend>
        <div v-for="type in typesForCategory(category.value)" :key="type.value" class="flex items-center gap-2 py-1">
          <label class="flex items-center gap-2.5 flex-1 min-w-0 cursor-pointer group">
            <input
              :type="multiple ? 'checkbox' : 'radio'"
              :name="multiple ? undefined : groupName"
              :value="type.value"
              :checked="isSelected(type.value)"
              :disabled="disabled"
              class="shrink-0 w-4 h-4 accent-brand-brown cursor-pointer disabled:cursor-not-allowed"
              @change="onToggle(type.value)"
            />
            <span
              class="text-sm text-black/80 group-hover:text-brand-brown transition truncate"
              :class="{ 'font-semibold text-brand-brown': isSelected(type.value) }"
            >
              {{ iaiTypeLabel(type.value, locale) }}
            </span>
          </label>
          <InfoTooltip :label="t.iaiPicker.examplesLabel">
            {{ iaiTypeExamples(type.value, locale) }}
          </InfoTooltip>
        </div>
      </fieldset>
    </div>

    <p v-if="legacyValues.length > 0" class="text-xs text-black/50 pt-1">
      {{ t.iaiPicker.legacyNote }}
    </p>
    <div v-if="legacyValues.length > 0" class="flex flex-wrap gap-2">
      <span
        v-for="value in legacyValues"
        :key="value"
        class="px-3 py-1 rounded-full text-xs bg-gray-100 text-gray-500 border border-gray-200"
      >
        {{ t.expertiseTagLabels?.[value] || value }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import InfoTooltip from '@/components/ui/InfoTooltip.vue'
import {
  IAI_CATEGORIES,
  typesForCategory,
  isValidIaiType,
  iaiCategoryLabel,
  iaiTypeLabel,
  iaiTypeExamples
} from '@/constants/iaiTaxonomy'

const props = defineProps({
  modelValue: {
    type: [Array, String],
    default: null
  },
  multiple: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  error: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const { t, locale } = useI18n()

const groupName = `iai-type-${Math.random().toString(36).slice(2, 9)}`
const panelIdFor = category => `${groupName}-${category}`

const selected = computed(() => {
  if (props.multiple) return Array.isArray(props.modelValue) ? props.modelValue : []
  return props.modelValue ? [props.modelValue] : []
})

/**
 * Values stored before this taxonomy existed that no mapping could place. They are shown
 * as inert chips rather than dropped, and are carried back out on save untouched.
 */
const legacyValues = computed(() => selected.value.filter(value => !isValidIaiType(value)))

const isSelected = value => selected.value.includes(value)

const countFor = category => typesForCategory(category).filter(type => isSelected(type.value)).length

/**
 * Collapsed by default, except groups holding a current selection — otherwise an architect
 * editing a saved profile is shown five empty rows and no sign of what they already chose.
 *
 * Derived on every read rather than seeded once, because the profile form mounts before its
 * data arrives: a set built at setup time would be computed against an empty selection and
 * would never reopen. A click records an explicit override, so a group the user deliberately
 * opened or closed stays that way even as the selection changes underneath it.
 */
const overrides = ref({})

const isExpanded = category => (category in overrides.value ? overrides.value[category] : countFor(category) > 0)

const toggleGroup = category => {
  overrides.value = { ...overrides.value, [category]: !isExpanded(category) }
}

const onToggle = value => {
  if (props.disabled) return
  if (!props.multiple) {
    emit('update:modelValue', value)
    return
  }
  const current = selected.value
  emit('update:modelValue', current.includes(value) ? current.filter(v => v !== value) : [...current, value])
}
</script>
