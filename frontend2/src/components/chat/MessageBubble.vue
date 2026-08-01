<template>
  <div :class="['flex gap-3 mb-3', isOwn ? 'flex-row-reverse' : 'flex-row']">
    <div
      class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-white"
      :class="avatarClass"
    >
      {{ initials }}
    </div>
    <div :class="['max-w-[70%]', isOwn ? 'items-end' : 'items-start', 'flex flex-col']">
      <p class="text-xs text-gray-500 mb-1 px-1">{{ message.senderName }}</p>
      <div :class="['rounded-2xl px-4 py-3 text-sm leading-relaxed', bubbleClass]">
        {{ message.content }}
      </div>
      <p class="text-xs text-gray-400 mt-1 px-1">{{ formatTime(message.createdAt) }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: {
    type: Object,
    required: true
  },
  currentUserId: {
    type: Number,
    required: true
  }
})

const isOwn = computed(() => Number(props.message.senderUserId) === Number(props.currentUserId))

const avatarClass = computed(() => {
  if (isOwn.value) return 'bg-brand-brown'
  if (props.message.senderType === 'SUPERUSER') return 'bg-blue-500'
  if (props.message.senderType === 'CLIENT') return 'bg-amber-500'
  return 'bg-gray-500'
})

const bubbleClass = computed(() => {
  if (isOwn.value) return 'bg-brand-brown text-white rounded-tr-sm'
  if (props.message.senderType === 'SUPERUSER') return 'bg-blue-50 border border-blue-200 text-blue-900 rounded-tl-sm'
  if (props.message.senderType === 'CLIENT') return 'bg-amber-50 border border-amber-200 text-amber-900 rounded-tl-sm'
  return 'bg-white border border-gray-200 text-gray-800 rounded-tl-sm'
})

const initials = props.message.senderName
  ? props.message.senderName
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2)
  : '?'

const formatTime = dateString => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleTimeString('id-ID', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>
