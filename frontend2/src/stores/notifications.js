import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationAPI } from '@/services/api'

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const error = ref(null)
  const lastFetchedAt = ref(null)

  // The notifications page pages through the server separately from the panel's list
  const pageItems = ref([])
  const nextCursor = ref(null)
  const totalCount = ref(0)
  const pageLoading = ref(false)
  const pageError = ref(null)

  const copiesOf = id => [...notifications.value, ...pageItems.value].filter(n => n.id === id)

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
      lastFetchedAt.value = Date.now()
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

  async function fetchPage({ cursor = null, unreadOnly = false } = {}) {
    pageLoading.value = true
    pageError.value = null

    try {
      const response = await notificationAPI.getPage({ cursor, unreadOnly })
      const page = response.data.data
      if (cursor) {
        const loadedIds = new Set(pageItems.value.map(n => n.id))
        pageItems.value = [...pageItems.value, ...page.items.filter(n => !loadedIds.has(n.id))]
      } else {
        pageItems.value = page.items
      }
      nextCursor.value = page.nextCursor
      totalCount.value = page.totalCount
      unreadCount.value = page.unreadCount
    } catch (err) {
      pageError.value = err.response?.data?.message || 'Failed to fetch notifications'
      console.error('Failed to fetch notification page:', err)
    } finally {
      pageLoading.value = false
    }
  }

  async function markAsRead(notificationId) {
    const copies = copiesOf(notificationId)
    const wasUnread = copies.some(n => !n.isRead)
    if (!wasUnread) return

    const readAt = new Date().toISOString()
    copies.forEach(n => {
      n.isRead = true
      n.readAt = readAt
    })
    if (unreadCount.value > 0) {
      unreadCount.value--
    }

    try {
      await notificationAPI.markAsRead(notificationId)
    } catch (err) {
      unreadCount.value++
      copies.forEach(n => {
        n.isRead = false
        n.readAt = null
      })

      error.value = err.response?.data?.message || 'Failed to mark notification as read'
      console.error('Failed to mark notification as read:', err)
      throw err
    }
  }

  async function markAllAsRead() {
    const changed = [...notifications.value, ...pageItems.value].filter(n => !n.isRead)
    const previousCount = unreadCount.value

    const readAt = new Date().toISOString()
    changed.forEach(n => {
      n.isRead = true
      n.readAt = readAt
    })
    unreadCount.value = 0

    try {
      await notificationAPI.markAllAsRead()
    } catch (err) {
      changed.forEach(n => {
        n.isRead = false
        n.readAt = null
      })
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
    lastFetchedAt,
    pageItems,
    nextCursor,
    totalCount,
    pageLoading,
    pageError,
    unreadNotifications,
    hasUnread,
    recentNotifications,
    formattedUnreadCount,
    fetchNotifications,
    fetchUnreadNotifications,
    fetchUnreadCount,
    fetchPage,
    markAsRead,
    markAllAsRead
  }
})
