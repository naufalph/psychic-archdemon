<template>
  <div class="min-h-screen bg-surface-alt">
    <!-- Header -->
    <div class="bg-white border-b border-gray-200 px-6 py-4">
      <div class="max-w-7xl mx-auto flex items-center gap-4">
        <button class="text-gray-500 hover:text-black transition" @click="router.back()">
          <ArrowLeft :size="20" />
        </button>
        <div>
          <h1 class="text-lg font-bold text-black">{{ t.support.dashboard.title }}</h1>
          <p class="text-xs text-gray-500">{{ t.support.dashboard.subtitle }}</p>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-6 py-6">
      <div class="grid grid-cols-1 lg:grid-cols-5 gap-6" style="height: calc(100vh - 140px)">
        <!-- LEFT: Conversation list -->
        <div class="lg:col-span-2 overflow-y-auto space-y-2 pr-1">
          <div v-if="loading" class="text-center py-12 text-gray-400 text-sm">{{ t.support.dashboard.loading }}</div>

          <div v-else-if="conversations.length === 0" class="text-center py-12 text-gray-400 text-sm">
            {{ t.support.dashboard.empty }}
          </div>

          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="bg-white rounded-2xl border p-4 cursor-pointer transition hover:shadow-md"
            :class="selectedConversation?.id === conv.id ? 'border-brand-gold shadow-md' : 'border-gray-200'"
            @click="selectConversation(conv)"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="min-w-0">
                <p class="font-bold text-black text-sm truncate">
                  {{ conv.architectName }} &amp; {{ conv.clientName }}
                </p>
                <p class="text-xs text-gray-500 mt-0.5">
                  {{ t.support.dashboard.projectRef }} #{{ conv.projectId }}
                  <span v-if="conv.bidId">· {{ t.support.dashboard.bidRef }} #{{ conv.bidId }}</span>
                </p>
              </div>
              <span
                v-if="conv.unreadCount > 0"
                class="flex-shrink-0 bg-brand-gold text-white text-xs font-bold rounded-full px-2 py-0.5"
              >
                {{ conv.unreadCount }}
              </span>
            </div>
            <p v-if="conv.lastMessage" class="text-xs text-gray-400 mt-2 truncate">
              {{ conv.lastMessage.content }}
            </p>
            <p class="text-xs text-gray-300 mt-1">
              {{ conv.lastMessageAt ? formatTime(conv.lastMessageAt) : formatTime(conv.createdAt) }}
            </p>
          </div>
        </div>

        <!-- RIGHT: Chat panel -->
        <div class="lg:col-span-3 flex flex-col">
          <div
            v-if="selectedConversation"
            class="flex-1 bg-white rounded-3xl border border-gray-200 shadow-soft overflow-hidden flex flex-col"
          >
            <div class="px-5 py-4 border-b border-gray-100">
              <h3 class="font-bold text-black">
                {{ selectedConversation.architectName }} &amp; {{ selectedConversation.clientName }}
              </h3>
              <p class="text-xs text-gray-500 mt-0.5">
                {{ t.support.dashboard.projectRef }} #{{ selectedConversation.projectId }}
                <span v-if="selectedConversation.bidId"
                  >· {{ t.support.dashboard.bidRef }} #{{ selectedConversation.bidId }}</span
                >
              </p>
            </div>
            <div class="flex-1 min-h-0">
              <ChatPanel :conversation-id="selectedConversation.id" class="h-full" />
            </div>
          </div>

          <div
            v-else
            class="flex-1 bg-white rounded-3xl border border-gray-200 flex items-center justify-center text-gray-400 text-sm"
          >
            {{ t.support.dashboard.selectPrompt }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { supportAPI } from '@/services/api'
import ChatPanel from '@/components/chat/ChatPanel.vue'

const { t } = useI18n()
const router = useRouter()
const conversations = ref([])
const loading = ref(false)
const selectedConversation = ref(null)

const formatTime = dateString => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / 86400000)
  if (days === 0) {
    return date.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })
  }
  if (days === 1) return 'Yesterday'
  return date.toLocaleDateString('id-ID', { day: 'numeric', month: 'short' })
}

const selectConversation = conv => {
  selectedConversation.value = conv
}

const loadConversations = async () => {
  loading.value = true
  try {
    const response = await supportAPI.getSupportConversations()
    conversations.value = response.data.data
  } catch (err) {
    console.error('Failed to load support conversations:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConversations()
})
</script>
