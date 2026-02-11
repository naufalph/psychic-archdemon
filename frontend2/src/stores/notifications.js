import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationAPI } from '@/services/api'

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const error = ref(null)

  const unreadNotifications = computed(() => {
    return notifications.value.filter(n => !n.isRead)
  })

  const hasUnread = computed(() => {
    return unreadCount.value > 0
  })

  const recentNotifications = computed(() => {
    return notifications.value.slice(0, 10)
  })

  const formattedUnreadCount = computed(() => {
    return unreadCount.value > 9 ? '9+' : unreadCount.value.toString()
  })

  async function fetchNotifications() {
    loading.value = true
    error.value = null

    try {
      const response = await notificationAPI.getAll()
      notifications.value = response.data.data || []
      unreadCount.value = notifications.value.filter(n => !n.isRead).length
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to fetch notifications'
      console.error('Failed to fetch notifications:', err)
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadNotifications() {
    loading.value = true
    error.value = null

    try {
      const response = await notificationAPI.getUnread()
      const unreadItems = response.data.data || []

      // Merge with existing notifications, avoiding duplicates
      const existingIds = new Set(notifications.value.map(n => n.id))
      const newNotifications = unreadItems.filter(n => !existingIds.has(n.id))
      notifications.value = [...newNotifications, ...notifications.value]

      unreadCount.value = unreadItems.length
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to fetch unread notifications'
      console.error('Failed to fetch unread notifications:', err)
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    try {
      const response = await notificationAPI.getUnreadCount()
      unreadCount.value = response.data.data?.unreadCount || 0
    } catch (err) {
      console.error('Failed to fetch unread count:', err)
    }
  }

  async function markAsRead(notificationId) {
    const notification = notifications.value.find(n => n.id === notificationId)
    if (!notification) return

    const wasUnread = !notification.isRead
    notification.isRead = true
    notification.readAt = new Date().toISOString()

    if (wasUnread && unreadCount.value > 0) {
      unreadCount.value--
    }

    try {
      await notificationAPI.markAsRead(notificationId)
    } catch (err) {
      if (wasUnread) {
        unreadCount.value++
      }
      notification.isRead = false
      notification.readAt = null

      error.value = err.response?.data?.message || 'Failed to mark notification as read'
      console.error('Failed to mark notification as read:', err)
      throw err
    }
  }

  async function markAllAsRead() {
    const previousNotifications = [...notifications.value]
    const previousCount = unreadCount.value

    notifications.value.forEach(n => {
      if (!n.isRead) {
        n.isRead = true
        n.readAt = new Date().toISOString()
      }
    })
    unreadCount.value = 0

    try {
      await notificationAPI.markAllAsRead()
    } catch (err) {
      notifications.value = previousNotifications
      unreadCount.value = previousCount

      error.value = err.response?.data?.message || 'Failed to mark all notifications as read'
      console.error('Failed to mark all notifications as read:', err)
      throw err
    }
  }

  return {
    notifications,
    unreadCount,
    loading,
    error,
    unreadNotifications,
    hasUnread,
    recentNotifications,
    formattedUnreadCount,
    fetchNotifications,
    fetchUnreadNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead
  }
})
