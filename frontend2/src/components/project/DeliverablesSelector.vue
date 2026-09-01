<template>
  <div class="space-y-6">
    <div v-for="group in DELIVERABLE_GROUPS" :key="group.categoryKey" class="bg-gray-50 rounded-2xl p-6">
      <div class="flex items-center justify-between mb-4">
        <h4 class="font-bold text-lg text-gray-900">{{ categoryLabel(group.categoryKey) }}</h4>
        <label
          :class="selectAllLabelClasses(group.categoryKey)"
          class="flex items-center gap-2 px-3 py-1.5 rounded-lg cursor-pointer transition-all text-sm"
        >
          <input
            type="checkbox"
            :checked="isAllSelected(group.categoryKey)"
            class="hidden"
            @change="toggleAllInGroup(group.categoryKey)"
          />
          <div :class="selectAllCheckboxClasses(group.categoryKey)">
            <Check v-if="isAllSelected(group.categoryKey)" :size="14" class="text-white" />
          </div>
          <span class="font-medium">{{ t.bidCreate.selectAll }}</span>
        </label>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <label v-for="item in group.items" :key="item" :class="labelClasses(item)">
          <input
            type="checkbox"
            :value="item"
            :checked="isSelected(item)"
            class="hidden"
            @change="toggleDeliverable(item)"
          />
          <div class="flex items-center gap-3">
            <div :class="checkboxClasses(item)">
              <Check v-if="isSelected(item)" :size="16" class="text-white" />
            </div>
            <div>
              <div class="font-medium text-gray-900">{{ itemLabel(item) }}</div>
              <div v-if="itemDescription(item)" class="text-xs text-gray-500 mt-1">
                {{ itemDescription(item) }}
              </div>
            </div>
          </div>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Check } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { DELIVERABLE_GROUPS } from '@/constants/projectDeliverables'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

const categoryLabel = key => t.value.bidCreate?.deliverableCategories?.[key] || key

const itemLabel = value => t.value.bidCreate?.deliverableItems?.[value] || value.replace(/_/g, ' ')

const itemDescription = value => t.value.bidCreate?.deliverableDescriptions?.[value] || ''

const isSelected = value => {
  return props.modelValue.includes(value)
}

const toggleDeliverable = value => {
  let updated
  if (isSelected(value)) {
    updated = props.modelValue.filter(item => item !== value)
  } else {
    updated = [...props.modelValue, value]
  }
  emit('update:modelValue', updated)
}

const labelClasses = value => {
  const baseClasses = 'block p-4 rounded-xl border-2 cursor-pointer transition-all'
  return isSelected(value)
    ? `${baseClasses} border-brand-brown bg-brand-tan/30`
    : `${baseClasses} border-gray-200 hover:border-brand-gold bg-white`
}

const checkboxClasses = value => {
  const baseClasses = 'w-5 h-5 rounded flex items-center justify-center flex-shrink-0'
  return isSelected(value) ? `${baseClasses} bg-brand-brown` : `${baseClasses} border-2 border-gray-300`
}

const isAllSelected = category => {
  const group = DELIVERABLE_GROUPS.find(g => g.categoryKey === category)
  if (!group) return false
  return group.items.every(item => isSelected(item))
}

const toggleAllInGroup = category => {
  const group = DELIVERABLE_GROUPS.find(g => g.categoryKey === category)
  if (!group) return

  const allSelected = isAllSelected(category)
  const groupValues = group.items

  let updated
  if (allSelected) {
    updated = props.modelValue.filter(item => !groupValues.includes(item))
  } else {
    const newItems = groupValues.filter(value => !props.modelValue.includes(value))
    updated = [...props.modelValue, ...newItems]
  }

  emit('update:modelValue', updated)
}

const selectAllLabelClasses = category => {
  const baseClasses = 'border-2'
  return isAllSelected(category)
    ? `${baseClasses} border-brand-brown bg-brand-tan/50`
    : `${baseClasses} border-gray-300 hover:border-brand-gold bg-white`
}

const selectAllCheckboxClasses = category => {
  const baseClasses = 'w-4 h-4 rounded flex items-center justify-center flex-shrink-0'
  return isAllSelected(category) ? `${baseClasses} bg-brand-brown` : `${baseClasses} border-2 border-gray-400`
}
</script>
