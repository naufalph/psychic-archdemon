<template>
  <div class="relative">
    <button
      @click.stop="toggleDropdown"
      class="relative p-2 rounded-full transition-colors"
      :class="themeClasses.button"
      aria-label="Notifications"
    >
      <BellIcon class="h-6 w-6" />
      <span
        v-if="notificationsStore.hasUnread"
        class="absolute top-0 right-0 inline-flex items-center justify-center px-1.5 py-0.5 text-xs font-bold leading-none transform translate-x-1/4 -translate-y-1/4 rounded-full"
        :class="themeClasses.badge"
      >
        {{ notificationsStore.formattedUnreadCount }}
      </span>
    </button>

    <Transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div
        v-if="isOpen"
        ref="dropdownRef"
        class="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-lg ring-1 ring-black ring-opacity-5 z-50"
      >
        <div class="p-4 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900">{{ t.notifications.title }}</h3>
            <button
              v-if="notificationsStore.hasUnread"
              @click="handleMarkAllAsRead"
              class="text-sm font-medium transition-colors"
              :class="themeClasses.markAllButton"
            >
              {{ t.notifications.markAllRead }}
            </button>
          </div>
        </div>

        <div class="max-h-96 overflow-y-auto">
          <div v-if="notificationsStore.loading" class="p-4 text-center text-gray-500">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 mx-auto mb-2" :class="themeClasses.spinner"></div>
            {{ t.notifications.loading }}
          </div>

          <div v-else-if="notificationsStore.recentNotifications.length === 0" class="p-8 text-center">
            <BellIcon class="h-12 w-12 mx-auto mb-3 text-gray-400" />
            <p class="text-sm font-medium text-gray-900">{{ t.notifications.emptyTitle }}</p>
            <p class="text-sm text-gray-500 mt-1">{{ t.notifications.emptyMessage }}</p>
          </div>

          <div v-else class="divide-y divide-gray-100">
            <button
              v-for="notification in notificationsStore.recentNotifications"
              :key="notification.id"
              @click="handleNotificationClick(notification)"
              class="w-full px-4 py-3 hover:bg-gray-50 transition-colors text-left flex gap-3"
              :class="{ 'bg-blue-50/50': !notification.isRead }"
            >
              <div class="flex-shrink-0 mt-1">
                <component :is="getNotificationIcon(notification.type)" class="h-6 w-6" :class="themeClasses.icon" />
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-2">
                  <p class="text-sm font-medium text-gray-900 truncate">
                    {{ getNotificationDisplay(notification).title }}
                  </p>
                  <span
                    v-if="!notification.isRead"
                    class="flex-shrink-0 w-2 h-2 rounded-full mt-1.5"
                    :class="themeClasses.unreadDot"
                  ></span>
                </div>
                <p class="text-xs text-gray-600 mt-0.5 line-clamp-2">
                  {{ getNotificationDisplay(notification).message }}
                </p>
                <p class="text-xs text-gray-400 mt-1">
                  {{ getRelativeTime(notification.createdAt) }}
                </p>
              </div>
            </button>
          </div>
        </div>

        <div v-if="notificationsStore.notifications.length > 10" class="p-3 border-t border-gray-200 text-center">
          <button class="text-sm font-medium transition-colors" :class="themeClasses.viewAllButton">
            {{ t.notifications.viewAll }}
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationsStore } from '@/stores/notifications'
import { useI18n } from '@/composables/useI18n'
import { composeNotificationMessage, getRelativeTime as getRelativeTimeUtil } from '@/utils/notificationUtils'
import {
  BellIcon,
  CheckCircleIcon,
  ExclamationCircleIcon,
  CurrencyDollarIcon,
  DocumentTextIcon,
  PencilSquareIcon
} from '@heroicons/vue/24/outline'

const props = defineProps({
  variant: {
    type: String,
    required: true,
    validator: value => ['architect', 'client'].includes(value)
  }
})

const notificationsStore = useNotificationsStore()
const router = useRouter()
const { t } = useI18n()

const isOpen = ref(false)
const dropdownRef = ref(null)
let pollingInterval = null

const themeClasses = computed(() => {
  if (props.variant === 'architect') {
    return {
      button: 'hover:bg-amber-50 text-amber-700',
      badge: 'bg-amber-500 text-white',
      spinner: 'border-amber-500',
      icon: 'text-amber-600',
      unreadDot: 'bg-amber-500',
      markAllButton: 'text-amber-600 hover:text-amber-700',
      viewAllButton: 'text-amber-600 hover:text-amber-700'
    }
  } else {
    return {
      button: 'hover:bg-blue-50 text-blue-700',
      badge: 'bg-blue-500 text-white',
      spinner: 'border-blue-500',
      icon: 'text-blue-600',
      unreadDot: 'bg-blue-500',
      markAllButton: 'text-blue-600 hover:text-blue-700',
      viewAllButton: 'text-blue-600 hover:text-blue-700'
    }
  }
})

const CACHE_TTL_MS = 5 * 60 * 1000

const isStale = () => {
  if (!notificationsStore.lastFetchedAt) return true
  return Date.now() - notificationsStore.lastFetchedAt > CACHE_TTL_MS
}

const toggleDropdown = async () => {
  isOpen.value = !isOpen.value

  if (isOpen.value && !notificationsStore.loading) {
    if (notificationsStore.notifications.length === 0 || isStale()) {
      await notificationsStore.fetchNotifications()
    }
  }
}

const handleClickOutside = event => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

const getNotificationIcon = type => {
  const iconMap = {
    PROJECT_VALIDATED: CheckCircleIcon,
    PROJECT_UPDATED: PencilSquareIcon,
    BID_RECEIVED: DocumentTextIcon,
    BID_ACCEPTED: CheckCircleIcon,
    PAYMENT_RECEIVED: CurrencyDollarIcon
  }
  return iconMap[type] || BellIcon
}

const getNotificationDisplay = notification => {
  return composeNotificationMessage(notification, t)
}

const getRelativeTime = timestamp => {
  return getRelativeTimeUtil(timestamp, t)
}

const getNotificationRoute = notification => {
  const typeRouteMap = {
    PROJECT_VALIDATED: { name: 'ClientProjects' },
    PROJECT_UPDATED: { name: 'ClientProjects' },
    BID_RECEIVED: { name: 'ClientProjects', params: { id: notification.referenceId } },
    BID_ACCEPTED: { name: 'ArchitectMyBids' },
    PAYMENT_RECEIVED: { name: 'ArchitectDashboard' }
  }

  return (
    typeRouteMap[notification.type] || {
      name: props.variant === 'architect' ? 'ArchitectDashboard' : 'ClientDashboard'
    }
  )
}

const handleNotificationClick = async notification => {
  try {
    if (!notification.isRead) {
      await notificationsStore.markAsRead(notification.id)
    }

    isOpen.value = false

    const route = getNotificationRoute(notification)
    if (route) {
      router.push(route)
    }
  } catch (error) {
    console.error('Failed to handle notification click:', error)
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

  notificationsStore.fetchUnreadCount()

  pollingInterval = setInterval(() => {
    notificationsStore.fetchUnreadCount()
  }, 60000)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)

  if (pollingInterval) {
    clearInterval(pollingInterval)
  }
})
</script>
