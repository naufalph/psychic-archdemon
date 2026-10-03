<template>
  <Listbox v-slot="{ open }" as="div" :model-value="modelValue" :disabled="disabled" @update:model-value="onSelect">
    <ListboxLabel class="block text-body font-medium text-ink-700 mb-2.5">{{ label }}</ListboxLabel>
    <div class="relative">
      <ListboxButton
        class="w-full box-border h-12 pl-4 pr-[42px] border rounded-xl text-left text-body font-semibold outline-none transition-colors"
        :class="[
          disabled ? 'bg-surface-alt cursor-not-allowed border-hairline' : 'bg-white cursor-pointer',
          !disabled &&
            (error ? 'border-red-400' : open ? 'border-ink-900' : 'border-hairline focus-visible:border-ink-900'),
          selected ? 'text-ink-900' : 'text-ink-400'
        ]"
      >
        <span class="block truncate">{{ selected ? selected.label : placeholder }}</span>
        <ChevronDown
          :size="18"
          class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-ink-400 transition-transform"
          :class="{ 'rotate-180': open }"
        />
      </ListboxButton>

      <transition
        leave-active-class="transition duration-100 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <ListboxOptions
          class="absolute z-30 mt-2 w-full max-h-72 overflow-auto p-1.5 bg-white border border-hairline rounded-xl shadow-[0_16px_40px_-20px_rgba(0,0,0,.25)] outline-none"
        >
          <ListboxOption
            v-for="option in options"
            :key="option.value"
            v-slot="{ active, selected: isSelected }"
            :value="option.value"
            as="template"
          >
            <li
              class="flex items-center justify-between gap-3 px-3 py-2.5 rounded-lg text-body cursor-pointer select-none"
              :class="[active ? 'bg-surface-alt' : '', isSelected ? 'font-semibold text-ink-900' : 'text-ink-700']"
            >
              <span class="truncate">{{ option.label }}</span>
              <Check v-if="isSelected" :size="16" :stroke-width="2.6" class="flex-none text-ink-900" />
            </li>
          </ListboxOption>
        </ListboxOptions>
      </transition>
    </div>
  </Listbox>
</template>

<script setup>
import { computed } from 'vue'
import { Listbox, ListboxLabel, ListboxButton, ListboxOptions, ListboxOption } from '@headlessui/vue'
import { Check, ChevronDown } from 'lucide-vue-next'

// A styled replacement for <select>: the native dropdown list is painted by the OS (system
// font, accent colour), which the public brief page should not inherit.
const props = defineProps({
  modelValue: { type: String, default: '' },
  options: { type: Array, required: true },
  label: { type: String, required: true },
  placeholder: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  error: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const selected = computed(() => props.options.find(o => o.value === props.modelValue) || null)

const onSelect = value => emit('update:modelValue', value)
</script>
