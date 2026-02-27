<template>
  <div class="flex flex-col h-full">
    <div v-if="loading && !messages.length" class="flex-1 flex items-center justify-center">
      <div class="text-center text-gray-400">
        <div class="w-8 h-8 border-2 border-[#C5A17A] border-t-transparent rounded-full animate-spin mx-auto mb-2" />
        <p class="text-sm">Loading messages...</p>
      </div>
    </div>

    <div v-else-if="error" class="flex-1 flex items-center justify-center">
      <div class="text-center text-red-500">
        <p class="text-sm mb-2">{{ error }}</p>
        <button @click="loadMessages" class="text-xs text-[#7C4728] hover:underline">Retry</button>
      </div>
    </div>

    <div v-else class="flex flex-col flex-1 min-h-0">
      <div
        ref="scrollContainer"
        class="flex-1 overflow-y-auto p-4 space-y-1 min-h-0"
      >
        <div v-if="messages.length === 0" class="text-center text-gray-400 py-8">
          <p class="text-sm">No messages yet. Start the conversation!</p>
        </div>

        <MessageBubble
          v-for="message in messages"
          :key="message.id"
          :message="message"
          :current-user-id="currentUserId"
        />
      </div>

      <MessageInput
        :disabled="sending"
        @send="handleSend"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import MessageBubble from './MessageBubble.vue'
import MessageInput from './MessageInput.vue'

const props = defineProps({
  conversationId: {
    type: Number,
    required: true
  }
})

const chatStore = useChatStore()
const authStore = useAuthStore()

const { currentMessages, loading, sending, error } = storeToRefs(chatStore)
const messages = currentMessages
const scrollContainer = ref(null)

const currentUserId = computed(() => authStore.user?.id)

let pollInterval = null

const scrollToBottom = async () => {
  await nextTick()
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
  }
}

const loadMessages = async () => {
  await chatStore.fetchMessages(props.conversationId, 0)
  await chatStore.markAllRead(props.conversationId)
  scrollToBottom()
}

const handleSend = async text => {
  await chatStore.sendMessage(props.conversationId, text)
  scrollToBottom()
}

watch(
  () => messages.value.length,
  () => scrollToBottom()
)

onMounted(async () => {
  chatStore.clearCurrentChat()
  await loadMessages()

  pollInterval = setInterval(async () => {
    try {
      await chatStore.fetchMessages(props.conversationId, 0)
    } catch {
      // silently fail on poll errors
    }
  }, 5000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
  chatStore.clearCurrentChat()
})
</script>
