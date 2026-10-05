<template>
  <div ref="rootRef">
    <button
      class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all"
      :class="isOpen || isOnPage ? 'bg-white/10 text-white' : 'text-white/60 hover:text-white hover:bg-white/5'"
      :aria-expanded="isOpen"
      aria-haspopup="dialog"
      @click.stop="toggleDropdown"
    >
      <Bell :size="18" />
      <span>{{ t.notifications.title }}</span>
      <span
        v-if="notificationsStore.hasUnread"
        class="ml-auto min-w-[20px] h-5 px-1.5 rounded-full bg-brand-gold text-ink-900 text-[11px] font-bold flex items-center justify-center"
      >
        {{ notificationsStore.formattedUnreadCount }}
      </span>
    </button>

    <Transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <div
        v-if="isOpen"
        class="fixed left-[calc(14rem+12px)] bottom-4 w-96 z-50 bg-white border border-hairline rounded-[20px] shadow-popover overflow-hidden"
        role="dialog"
        :aria-label="t.notifications.title"
      >
        <div class="px-5 pt-[18px] pb-3.5 flex items-center justify-between">
          <h3 class="text-base font-semibold text-ink-900 tracking-[-0.01em]">{{ t.notifications.title }}</h3>
          <button
            v-if="notificationsStore.hasUnread"
            class="text-[13px] font-medium text-accent-blue hover:opacity-65 transition-opacity"
            @click="handleMarkAllAsRead"
          >
            {{ t.notifications.markAllRead }}
          </button>
        </div>

        <div class="max-h-[min(560px,calc(100vh-120px))] overflow-y-auto">
          <div
            v-if="notificationsStore.loading"
            class="px-5 py-8 border-t border-hairline text-center text-sm text-ink-400"
          >
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-ink-900 mx-auto mb-2"></div>
            {{ t.notifications.loading }}
          </div>

          <div v-else-if="notificationsStore.error" class="px-5 py-8 border-t border-hairline text-center">
            <AlertCircle :size="40" class="mx-auto mb-3 text-ink-300" />
            <p class="text-sm font-medium text-ink-900">{{ t.notifications.loadError }}</p>
            <button
              class="mt-2 text-[13px] font-medium text-accent-blue hover:opacity-65 transition-opacity"
              @click="notificationsStore.fetchNotifications()"
            >
              {{ t.notifications.retry }}
            </button>
          </div>

          <div
            v-else-if="notificationsStore.recentNotifications.length === 0"
            class="px-5 py-8 border-t border-hairline text-center"
          >
            <Bell :size="40" class="mx-auto mb-3 text-ink-300" />
            <p class="text-sm font-medium text-ink-900">{{ t.notifications.emptyTitle }}</p>
            <p class="text-[13px] text-ink-400 mt-1">{{ t.notifications.emptyMessage }}</p>
          </div>

          <template v-else>
            <component
              :is="getNotificationRoute(notification) ? RouterLink : 'div'"
              v-for="notification in notificationsStore.recentNotifications"
              :key="notification.id"
              :to="getNotificationRoute(notification) || undefined"
              class="flex gap-3 px-5 py-3.5 border-t border-hairline text-left w-full"
              :class="[
                notification.isRead ? '' : 'bg-brand-cream',
                getNotificationRoute(notification) ? 'hover:bg-surface-alt transition-colors' : ''
              ]"
              @click="handleNotificationClick(notification)"
            >
              <div
                class="w-9 h-9 shrink-0 rounded-full bg-white border border-hairline text-ink-900 flex items-center justify-center"
              >
                <component :is="getNotificationIcon(notification)" :size="16" />
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-2">
                  <p class="text-sm font-semibold text-ink-900">
                    {{ getNotificationDisplay(notification).title }}
                  </p>
                  <span v-if="!notification.isRead" class="shrink-0 w-2 h-2 rounded-full bg-brand-gold mt-1.5"></span>
                </div>
                <p class="text-[13px] leading-normal text-ink-400 mt-0.5 line-clamp-2">
                  {{ getNotificationDisplay(notification).message }}
                </p>
                <p class="text-xs text-ink-300 mt-1">
                  {{ getRelativeTime(notification.createdAt) }}
                </p>
              </div>
            </component>
          </template>
        </div>

        <RouterLink
          :to="{ name: notificationsRouteName(props.variant) }"
          class="border-t border-hairline py-3.5 flex items-center justify-center gap-1 text-[13px] font-semibold text-ink-900 hover:opacity-65 transition-opacity"
          @click="isOpen = false"
        >
          {{ t.notifications.viewAll }}
          <ChevronRight :size="16" />
        </RouterLink>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useNotificationsStore } from '@/stores/notifications'
import { useI18n } from '@/composables/useI18n'
import { composeNotificationMessage, getRelativeTime as getRelativeTimeUtil } from '@/utils/notificationUtils'
import {
  getNotificationIcon,
  getNotificationRoute as resolveRoute,
  notificationsRouteName
} from '@/utils/notificationRoutes'
import { AlertCircle, Bell, ChevronRight } from 'lucide-vue-next'

const props = defineProps({
  variant: {
    type: String,
    required: true,
    validator: value => ['architect', 'client'].includes(value)
  }
})

const notificationsStore = useNotificationsStore()
const { t } = useI18n()

const route = useRoute()

const isOpen = ref(false)
const isOnPage = computed(() => route.name === notificationsRouteName(props.variant))
const rootRef = ref(null)
let pollingInterval = null

const CACHE_TTL_MS = 5 * 60 * 1000

const isStale = () => {
  if (!notificationsStore.lastFetchedAt) return true
  return Date.now() - notificationsStore.lastFetchedAt > CACHE_TTL_MS
}

const toggleDropdown = async () => {
  if (isOnPage.value) return
  isOpen.value = !isOpen.value

  if (isOpen.value) {
    if (notificationsStore.notifications.length === 0 || isStale()) {
      await notificationsStore.fetchNotifications()
    }
    if (notificationsStore.hasUnread) {
      await notificationsStore.markAllAsRead()
    }
  }
}

const handleClickOutside = event => {
  if (rootRef.value && !rootRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

const handleKeydown = event => {
  if (event.key === 'Escape') {
    isOpen.value = false
  }
}

const getNotificationDisplay = notification => {
  return composeNotificationMessage(notification, t.value)
}

const getRelativeTime = timestamp => {
  return getRelativeTimeUtil(timestamp, t.value)
}

const getNotificationRoute = notification => resolveRoute(notification, props.variant)

const handleNotificationClick = async notification => {
  isOpen.value = false
  try {
    if (!notification.isRead) {
      await notificationsStore.markAsRead(notification.id)
    }
  } catch (error) {
    console.error('Failed to mark notification as read:', error)
  }
}

const handleMarkAllAsRead = async () => {
  try {
    await notificationsStore.markAllAsRead()
  } catch (error) {
    console.error('Failed to mark all as read:', error)
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleKeydown)

  notificationsStore.fetchUnreadCount()

  pollingInterval = setInterval(() => {
    notificationsStore.fetchUnreadCount()
  }, 60000)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleKeydown)

  if (pollingInterval) {
    clearInterval(pollingInterval)
  }
})
</script>
