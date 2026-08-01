<template>
  <div class="border-t border-gray-100 p-4 bg-white">
    <div class="flex gap-3 items-end">
      <textarea
        ref="textareaRef"
        v-model="content"
        :placeholder="placeholder"
        :disabled="disabled"
        @keydown.enter.exact.prevent="handleSend"
        rows="1"
        class="flex-1 resize-none rounded-2xl border border-gray-200 px-4 py-3 text-sm text-gray-800 placeholder-gray-400 focus:outline-none focus:border-brand-gold transition overflow-hidden"
        style="min-height: 44px; max-height: 120px"
        @input="autoResize"
      />
      <button
        @click="handleSend"
        :disabled="!content.trim() || disabled"
        class="flex-shrink-0 w-11 h-11 rounded-full bg-brand-brown text-white flex items-center justify-center hover:bg-black transition disabled:opacity-40 disabled:cursor-not-allowed"
      >
        <Send :size="16" />
      </button>
    </div>
    <p class="text-xs text-gray-400 mt-2 px-1">Press Enter to send</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Send } from 'lucide-vue-next'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: 'Type a message...'
  }
})

const emit = defineEmits(['send'])

const content = ref('')
const textareaRef = ref(null)

const autoResize = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

const handleSend = () => {
  const text = content.value.trim()
  if (!text || props.disabled) return
  emit('send', text)
  content.value = ''
  if (textareaRef.value) {
    textareaRef.value.style.height = '44px'
  }
}
</script>
