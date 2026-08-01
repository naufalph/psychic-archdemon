<template>
  <div class="space-y-6">
    <div v-for="group in deliverableGroups" :key="group.category" class="bg-gray-50 rounded-2xl p-6">
      <div class="flex items-center justify-between mb-4">
        <h4 class="font-bold text-lg text-gray-900">{{ group.category }}</h4>
        <label
          :class="selectAllLabelClasses(group.category)"
          class="flex items-center gap-2 px-3 py-1.5 rounded-lg cursor-pointer transition-all text-sm"
        >
          <input
            type="checkbox"
            :checked="isAllSelected(group.category)"
            @change="toggleAllInGroup(group.category)"
            class="hidden"
          />
          <div :class="selectAllCheckboxClasses(group.category)">
            <Check v-if="isAllSelected(group.category)" :size="14" class="text-white" />
          </div>
          <span class="font-medium">Select All</span>
        </label>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <label v-for="item in group.items" :key="item.value" :class="labelClasses(item.value)">
          <input
            type="checkbox"
            :value="item.value"
            :checked="isSelected(item.value)"
            @change="toggleDeliverable(item.value)"
            class="hidden"
          />
          <div class="flex items-center gap-3">
            <div :class="checkboxClasses(item.value)">
              <Check v-if="isSelected(item.value)" :size="16" class="text-white" />
            </div>
            <div>
              <div class="font-medium text-gray-900">{{ item.label }}</div>
              <div v-if="item.description" class="text-xs text-gray-500 mt-1">
                {{ item.description }}
              </div>
            </div>
          </div>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Check } from 'lucide-vue-next'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const deliverableGroups = [
  {
    category: 'Site Analysis & Planning',
    items: [
      { value: 'SITE_ANALYSIS', label: 'Site Analysis', description: 'Land survey and environmental assessment' },
      { value: 'ZONING_STUDY', label: 'Zoning Study', description: 'Local regulations and building codes' }
    ]
  },
  {
    category: 'Design Phases',
    items: [
      { value: 'CONCEPT_DESIGN', label: 'Concept Design', description: 'Initial design concepts and sketches' },
      { value: 'SCHEMATIC_DESIGN', label: 'Schematic Design', description: 'Preliminary floor plans and elevations' },
      { value: 'DESIGN_DEVELOPMENT', label: 'Design Development', description: 'Detailed design drawings' },
      { value: 'CONSTRUCTION_DOCS', label: 'Construction Documents', description: 'Complete technical drawings' }
    ]
  },
  {
    category: 'Permits & Documentation',
    items: [
      { value: 'IMB_PERMIT', label: 'IMB (Building Permit)', description: 'Building construction permit' },
      { value: 'SLF_CERT', label: 'SLF Certificate', description: 'Building feasibility certificate' },
      { value: 'ENVIRONMENTAL_PERMIT', label: 'Environmental Permit', description: 'Environmental impact assessment' }
    ]
  },
  {
    category: 'Specialized Services',
    items: [
      { value: 'INTERIOR_DESIGN', label: 'Interior Design', description: 'Interior layout and finishes' },
      { value: 'LANDSCAPE_DESIGN', label: 'Landscape Design', description: 'Garden and outdoor spaces' },
      { value: 'MEP_DESIGN', label: 'MEP Design', description: 'Mechanical, electrical, and plumbing' },
      { value: 'STRUCTURAL_DESIGN', label: 'Structural Design', description: 'Structural engineering drawings' }
    ]
  },
  {
    category: 'Construction Support',
    items: [
      {
        value: 'SUPERVISION',
        label: 'Construction Supervision',
        description: 'On-site supervision during construction'
      },
      { value: 'AS_BUILT', label: 'As-Built Drawings', description: 'Final drawings reflecting construction changes' }
    ]
  }
]

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
  const group = deliverableGroups.find(g => g.category === category)
  if (!group) return false
  return group.items.every(item => isSelected(item.value))
}

const toggleAllInGroup = category => {
  const group = deliverableGroups.find(g => g.category === category)
  if (!group) return

  const allSelected = isAllSelected(category)
  const groupValues = group.items.map(item => item.value)

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
