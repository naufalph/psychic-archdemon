<template>
  <div class="flex h-full bg-[#F4F5F7]" style="height: calc(100vh)">
    <!-- Conversation list -->
    <div class="w-72 shrink-0 bg-white border-r border-gray-200 flex flex-col">
      <div class="px-5 py-5 border-b border-gray-100">
        <h1 class="text-lg font-bold text-gray-900">Messages</h1>
      </div>

      <div v-if="chatStore.loading && !chatStore.conversations.length" class="flex-1 flex items-center justify-center">
        <div class="w-6 h-6 border-2 border-[#C5A17A] border-t-transparent rounded-full animate-spin" />
      </div>

      <div
        v-else-if="chatStore.conversations.length === 0"
        class="flex-1 flex items-center justify-center px-5 text-center"
      >
        <div>
          <MessageSquare :size="32" class="text-gray-300 mx-auto mb-2" />
          <p class="text-sm text-gray-400">No conversations yet</p>
        </div>
      </div>

      <div v-else class="flex-1 overflow-y-auto">
        <button
          v-for="conv in chatStore.conversations"
          :key="conv.id"
          class="w-full text-left px-5 py-4 border-b border-gray-50 hover:bg-gray-50 transition"
          :class="selectedConvId === conv.id ? 'bg-[#F5E6D3]' : ''"
          @click="selectConversation(conv)"
        >
          <div class="flex items-start gap-3">
            <div
              class="w-9 h-9 rounded-full bg-[#1C1C1C] text-white flex items-center justify-center text-xs font-bold shrink-0"
            >
              {{ getConvInitials(conv) }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <p class="text-sm font-semibold text-gray-900 truncate">{{ conv.projectTitle || 'Project Chat' }}</p>
                <span
                  v-if="conv.unreadCount > 0"
                  class="ml-2 w-5 h-5 bg-[#7C4728] text-white text-xs rounded-full flex items-center justify-center font-bold shrink-0"
                >
                  {{ conv.unreadCount > 9 ? '9+' : conv.unreadCount }}
                </span>
              </div>
              <p class="text-xs text-gray-500 truncate mt-0.5">{{ conv.lastMessage || 'No messages yet' }}</p>
            </div>
          </div>
        </button>
      </div>
    </div>

    <!-- Chat panel -->
    <div class="flex-1 flex flex-col">
      <!-- Empty state -->
      <div v-if="!selectedConvId" class="flex-1 flex items-center justify-center text-center px-8">
        <div>
          <MessageSquare :size="48" class="text-gray-300 mx-auto mb-3" />
          <p class="text-gray-500 font-medium">Select a conversation</p>
          <p class="text-sm text-gray-400 mt-1">Choose a project chat from the list to start messaging.</p>
        </div>
      </div>

      <template v-else>
        <!-- Chat header -->
        <div class="bg-white border-b border-gray-200 px-6 py-4 flex items-center gap-3 shrink-0">
          <div class="w-9 h-9 rounded-full bg-[#1C1C1C] text-white flex items-center justify-center text-xs font-bold">
            {{ getConvInitials(selectedConv) }}
          </div>
          <div>
            <p class="font-semibold text-gray-900 text-sm">{{ selectedConv?.projectTitle || 'Project Chat' }}</p>
            <p class="text-xs text-gray-400">{{ selectedConv?.architectName || '' }}</p>
          </div>
        </div>

        <!-- Messages -->
        <div ref="messagesEl" class="flex-1 overflow-y-auto px-6 py-4 space-y-3">
          <div v-if="chatStore.loading" class="flex justify-center py-8">
            <div class="w-6 h-6 border-2 border-[#C5A17A] border-t-transparent rounded-full animate-spin" />
          </div>
          <template v-else>
            <div
              v-for="msg in chatStore.currentMessages"
              :key="msg.id"
              class="flex"
              :class="isMine(msg) ? 'justify-end' : 'justify-start'"
            >
              <div
                class="max-w-xs lg:max-w-md px-4 py-2.5 rounded-2xl text-sm"
                :class="
                  isMine(msg)
                    ? 'bg-[#1C1C1C] text-white rounded-br-sm'
                    : 'bg-white border border-gray-200 text-gray-800 rounded-bl-sm'
                "
              >
                <p class="leading-relaxed">{{ msg.content }}</p>
                <p class="text-xs mt-1 opacity-60" :class="isMine(msg) ? 'text-right' : ''">
                  {{ formatTime(msg.createdAt) }}
                </p>
              </div>
            </div>
          </template>
        </div>

        <!-- Input -->
        <div class="bg-white border-t border-gray-200 px-5 py-4 shrink-0">
          <form @submit.prevent="sendMsg" class="flex items-center gap-3">
            <input
              v-model="newMessage"
              type="text"
              placeholder="Type a message..."
              class="flex-1 bg-gray-100 rounded-full px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[#7C4728]/30"
              :disabled="chatStore.sending"
            />
            <button
              type="submit"
              :disabled="!newMessage.trim() || chatStore.sending"
              class="w-10 h-10 bg-[#1C1C1C] text-white rounded-full flex items-center justify-center hover:bg-[#7C4728] disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              <Send :size="16" />
            </button>
          </form>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { MessageSquare, Send } from 'lucide-vue-next'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'

const chatStore = useChatStore()
const authStore = useAuthStore()

const selectedConvId = ref(null)
const newMessage = ref('')
const messagesEl = ref(null)

const selectedConv = computed(() => chatStore.conversations.find(c => c.id === selectedConvId.value) || null)

const isMine = msg => msg.senderUserId === authStore.user?.id

const getConvInitials = conv => {
  if (!conv) return '?'
  const name = conv.architectName || conv.projectTitle || '?'
  return name
    .split(' ')
    .map(w => w[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
}

const selectConversation = async conv => {
  selectedConvId.value = conv.id
  chatStore.clearCurrentChat()
  await chatStore.fetchMessages(conv.id)
  await chatStore.markAllRead(conv.id)
  scrollToBottom()
}

const sendMsg = async () => {
  const content = newMessage.value.trim()
  if (!content || !selectedConvId.value) return
  newMessage.value = ''
  const msg = await chatStore.sendMessage(selectedConvId.value, content)
  if (msg) chatStore.appendMessage(msg)
  scrollToBottom()
}

const formatTime = dateStr => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  await chatStore.fetchConversations()
  if (chatStore.conversations.length === 1) {
    selectConversation(chatStore.conversations[0])
  }
})
</script>
