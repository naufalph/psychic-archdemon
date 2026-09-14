<template>
  <div class="space-y-6">
    <div v-for="group in DELIVERABLE_GROUPS" :key="group.categoryKey">
      <div class="flex items-center gap-3 mb-2.5">
        <span class="text-[11px] font-bold tracking-[0.16em] uppercase text-gray-400">
          {{ categoryLabel(group.categoryKey) }}
        </span>
        <span class="flex-1 h-px bg-gray-100" />
        <button
          type="button"
          :class="[
            'flex-none px-2.5 py-1 rounded-full border text-[10px] font-bold tracking-wider uppercase transition',
            isAllSelected(group.categoryKey)
              ? 'border-brand-brown bg-brand-brown text-white hover:bg-brand-brown-hover'
              : 'border-gray-200 bg-white text-gray-700 hover:border-brand-brown hover:text-brand-brown'
          ]"
          @click="toggleAllInGroup(group.categoryKey)"
        >
          {{ isAllSelected(group.categoryKey) ? t.bidCreate.deselectAll : t.bidCreate.selectAll }}
        </button>
      </div>

      <div class="flex flex-col gap-1.5">
        <label v-for="item in group.items" :key="item" :class="rowClasses(item)">
          <input
            type="checkbox"
            :value="item"
            :checked="isSelected(item)"
            class="sr-only"
            @change="toggleDeliverable(item)"
          />
          <span :class="checkboxClasses(item)">
            <Check v-if="isSelected(item)" :size="13" :stroke-width="3" class="text-white" />
          </span>
          <span class="text-sm font-medium leading-snug text-gray-700">{{ itemLabel(item) }}</span>
          <span v-if="itemDescription(item).length" class="flex-none ml-auto mt-0.5">
            <InfoTooltip :label="itemLabel(item)">
              <p class="text-[10px] font-bold tracking-[0.16em] uppercase text-gray-400 mb-1.5">
                {{ categoryLabel(group.categoryKey) }}
              </p>
              <p v-for="(paragraph, index) in itemDescription(item)" :key="index" class="mt-1.5 first-of-type:mt-0">
                {{ paragraph }}
              </p>
            </InfoTooltip>
          </span>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Check } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { DELIVERABLE_GROUPS } from '@/constants/projectDeliverables'
import InfoTooltip from '@/components/ui/InfoTooltip.vue'

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

const itemDescription = value => t.value.bidCreate?.deliverableDescriptions?.[value] || []

const isSelected = value => props.modelValue.includes(value)

const toggleDeliverable = value => {
  const updated = isSelected(value) ? props.modelValue.filter(item => item !== value) : [...props.modelValue, value]
  emit('update:modelValue', updated)
}

const rowClasses = value => {
  const base = 'flex items-start gap-3 px-3 py-2.5 rounded-lg border cursor-pointer transition'
  return isSelected(value)
    ? `${base} border-brand-brown bg-brand-tan/30`
    : `${base} border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50`
}

const checkboxClasses = value => {
  const base = 'flex-none mt-0.5 w-[18px] h-[18px] rounded flex items-center justify-center'
  return isSelected(value) ? `${base} bg-brand-brown` : `${base} border border-gray-300 bg-white`
}

const groupItems = category => DELIVERABLE_GROUPS.find(g => g.categoryKey === category)?.items || []

const isAllSelected = category => {
  const items = groupItems(category)
  return items.length > 0 && items.every(isSelected)
}

const toggleAllInGroup = category => {
  const items = groupItems(category)
  const updated = isAllSelected(category)
    ? props.modelValue.filter(item => !items.includes(item))
    : [...props.modelValue, ...items.filter(item => !props.modelValue.includes(item))]
  emit('update:modelValue', updated)
}
</script>
