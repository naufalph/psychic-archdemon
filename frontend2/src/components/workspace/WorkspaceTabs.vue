<template>
  <div class="border-b border-border-gray">
    <div class="max-w-7xl mx-auto px-6 flex gap-1 items-end">
      <button
        v-for="item in tabs"
        :key="item.key"
        class="px-4 py-2.5 text-sm transition-colors -mb-px border-b-2"
        :class="
          modelValue === item.key
            ? 'font-bold text-gray-900 border-ink-700'
            : 'font-medium text-gray-500 border-transparent hover:text-gray-900'
        "
        @click="$emit('update:modelValue', item.key)"
      >
        {{ item.label }}
      </button>

      <button
        class="ml-auto mb-2 px-4 py-2 rounded-full text-sm font-semibold flex items-center gap-2 transition-colors"
        :class="
          chatOpen
            ? 'bg-white border border-border-gray hover:border-brand-gold'
            : 'bg-ink-700 text-white hover:bg-ink-500'
        "
        @click="$emit('toggle-chat')"
      >
        <component :is="chatOpen ? PanelRightClose : PanelRightOpen" class="w-[18px] h-[18px]" />
        {{ chatOpen ? t.projectWorkspace?.hideChatBtn : t.projectWorkspace?.projectChatBtn }}
        <span v-if="!chatOpen" class="w-[7px] h-[7px] rounded-full bg-green-500" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { PanelRightClose, PanelRightOpen } from 'lucide-vue-next'

const props = defineProps({
  modelValue: { type: String, required: true },
  chatOpen: { type: Boolean, default: true },
  t: { type: Object, required: true }
})
defineEmits(['update:modelValue', 'toggle-chat'])

const tabs = computed(() => [
  { key: 'summary', label: props.t.projectWorkspace?.tabSummary },
  { key: 'phases', label: props.t.projectWorkspace?.tabPhases },
  { key: 'contract', label: props.t.projectWorkspace?.tabContract }
])
</script>
