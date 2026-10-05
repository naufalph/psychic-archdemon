<template>
  <div class="max-w-[880px] mx-auto px-12 pt-12 pb-16 flex flex-col gap-6">
    <div class="flex items-end justify-between gap-6">
      <div>
        <h1 class="text-[32px] leading-[1.15] font-bold tracking-[-0.03em] text-ink-900">
          {{ t.notifications.title }}
        </h1>
        <p class="mt-2 text-[15px] leading-[1.6] text-ink-400">{{ subtitle }}</p>
      </div>
      <button
        v-if="notificationsStore.hasUnread"
        class="shrink-0 flex items-center gap-2 h-10 px-[18px] rounded-full border border-hairline bg-white text-sm font-semibold text-ink-900 hover:border-ink-900 transition-colors"
        @click="handleMarkAllAsRead"
      >
        <CheckCheck :size="16" />
        {{ t.notifications.markAllRead }}
      </button>
    </div>

    <div class="flex gap-2">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="flex items-center gap-2 h-9 px-4 rounded-full border text-sm font-semibold whitespace-nowrap shrink-0 transition-colors"
        :class="filter === tab.key ? 'bg-ink-900 text-white border-ink-900' : 'bg-white border-hairline text-ink-900'"
        :aria-pressed="filter === tab.key"
        @click="setFilter(tab.key)"
      >
        {{ tab.label }}
        <span
          v-if="tab.count > 0"
          class="min-w-[20px] h-5 px-1.5 rounded-full bg-brand-gold text-ink-900 text-[11px] font-bold flex items-center justify-center"
        >
          {{ notificationsStore.formattedUnreadCount }}
        </span>
      </button>
    </div>

    <div
      v-if="isInitialLoading"
      class="bg-white border border-hairline rounded-[20px] overflow-hidden"
      :aria-label="t.notifications.loading"
    >
      <div
        v-for="i in 3"
        :key="i"
        class="flex gap-4 items-start px-6 py-[18px] animate-pulse"
        :class="{ 'border-t border-hairline': i > 1 }"
      >
        <div class="w-10 h-10 shrink-0 rounded-full bg-surface-alt"></div>
        <div class="flex-1 space-y-2 pt-1">
          <div class="h-4 w-1/3 rounded bg-surface-alt"></div>
          <div class="h-3.5 w-5/6 rounded bg-surface-alt"></div>
          <div class="h-3 w-1/4 rounded bg-surface-alt"></div>
        </div>
      </div>
    </div>

    <div
      v-else-if="notificationsStore.pageError && !visibleItems.length"
      class="bg-white border border-hairline rounded-[20px] px-6 py-10 flex flex-col items-center text-center gap-4"
    >
      <AlertCircle :size="26" class="text-ink-300" />
      <p class="text-sm font-medium text-ink-900">{{ t.notifications.loadError }}</p>
      <button
        class="h-10 px-5 rounded-full border border-hairline bg-white text-sm font-semibold text-ink-900 hover:border-ink-900 transition-colors"
        @click="loadFirstPage"
      >
        {{ t.notifications.retry }}
      </button>
    </div>

    <div
      v-else-if="!visibleItems.length"
      class="bg-white border border-hairline rounded-[20px] py-[72px] px-8 flex flex-col items-center text-center gap-4"
    >
      <span class="w-16 h-16 rounded-full bg-brand-cream text-brand-brown flex items-center justify-center">
        <Bell :size="26" />
      </span>
      <div>
        <p class="text-lg font-semibold tracking-[-0.01em] text-ink-900">{{ emptyCopy.title }}</p>
        <p class="mt-1.5 max-w-[380px] text-sm leading-[1.6] text-ink-400">{{ emptyCopy.message }}</p>
      </div>
    </div>

    <template v-else>
      <section v-for="group in groups" :key="group.key" class="flex flex-col gap-3">
        <p class="px-1 text-xs font-semibold uppercase tracking-[0.08em] text-ink-300">{{ group.label }}</p>
        <div class="bg-white border border-hairline rounded-[20px] overflow-hidden">
          <component
            :is="row.route ? RouterLink : 'div'"
            v-for="row in group.rows"
            :key="row.notification.id"
            :to="row.route || undefined"
            class="flex gap-4 items-start px-6 py-[18px] border-t border-hairline first:border-t-0 transition-colors"
            :class="[
              row.notification.isRead ? 'bg-white' : 'bg-brand-cream',
              row.route && row.notification.isRead ? 'hover:bg-brand-cream/50' : ''
            ]"
            @click="handleRowClick(row.notification)"
          >
            <span
              class="w-10 h-10 shrink-0 rounded-full bg-white border border-hairline text-ink-900 flex items-center justify-center"
            >
              <component :is="getNotificationIcon(row.notification)" :size="18" />
            </span>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <p class="text-[15px] font-semibold text-ink-900">{{ row.display.title }}</p>
                <span v-if="!row.notification.isRead" class="w-2 h-2 shrink-0 rounded-full bg-brand-gold"></span>
              </div>
              <p class="mt-1 text-sm leading-[1.55] text-ink-500">{{ row.display.message }}</p>
              <p class="mt-1.5 text-xs text-ink-300">{{ row.meta }}</p>
            </div>
            <span
              v-if="row.cta"
              class="shrink-0 self-center flex items-center gap-0.5 text-[13px] font-semibold text-ink-900 whitespace-nowrap"
            >
              {{ row.cta }}
              <ChevronRight :size="16" />
            </span>
          </component>
        </div>
      </section>

      <div v-if="notificationsStore.nextCursor" class="flex justify-center pt-2">
        <button
          class="h-10 px-5 rounded-full border border-hairline bg-white text-sm font-semibold text-ink-900 hover:border-ink-900 transition-colors disabled:opacity-50"
          :disabled="notificationsStore.pageLoading"
          @click="loadMore"
        >
          {{ t.notifications.loadMore }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { AlertCircle, Bell, CheckCheck, ChevronRight } from 'lucide-vue-next'
import { useNotificationsStore } from '@/stores/notifications'
import { useI18n } from '@/composables/useI18n'
import { composeNotificationMessage, getRelativeTime } from '@/utils/notificationUtils'
import { getNotificationCtaKey, getNotificationIcon, getNotificationRoute } from '@/utils/notificationRoutes'

const props = defineProps({
  role: {
    type: String,
    required: true,
    validator: value => ['architect', 'client'].includes(value)
  }
})

const DAY_MS = 86400000

const notificationsStore = useNotificationsStore()
const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const hasLoaded = ref(false)

const filter = computed(() => (route.query.filter === 'unread' ? 'unread' : 'all'))

const tabs = computed(() => [
  { key: 'all', label: t.value.notifications.filterAll, count: 0 },
  { key: 'unread', label: t.value.notifications.filterUnread, count: notificationsStore.unreadCount }
])

const visibleItems = computed(() =>
  filter.value === 'unread' ? notificationsStore.pageItems.filter(n => !n.isRead) : notificationsStore.pageItems
)

const isInitialLoading = computed(
  () => !hasLoaded.value || (notificationsStore.pageLoading && !visibleItems.value.length)
)

const subtitle = computed(() => {
  const copy = t.value.notifications
  if (notificationsStore.totalCount === 0) return copy.subtitleEmpty
  if (notificationsStore.unreadCount > 0) {
    return copy.subtitleUnread
      .replace('{n}', notificationsStore.unreadCount)
      .replace('{total}', notificationsStore.totalCount)
  }
  return copy.subtitleAllRead
})

const emptyCopy = computed(() => {
  const copy = t.value.notifications
  if (filter.value === 'unread' && notificationsStore.totalCount > 0) {
    return { title: copy.allReadTitle, message: copy.allReadMessage }
  }
  return { title: copy.emptyTitle, message: copy.emptyMessage }
})

const formatTime = createdAt => {
  const time = new Date(createdAt)
  if (Date.now() - time.getTime() < 7 * DAY_MS) return getRelativeTime(createdAt, t.value)
  return time.toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' })
}

const toRow = notification => {
  const target = getNotificationRoute(notification, props.role)
  const ctaKey = getNotificationCtaKey(notification, target)
  const time = formatTime(notification.createdAt)
  return {
    notification,
    route: target,
    cta: ctaKey ? t.value.notifications.cta[ctaKey] : null,
    display: composeNotificationMessage(notification, t.value),
    meta: notification.projectName ? `${notification.projectName} · ${time}` : time
  }
}

const groups = computed(() => {
  const startOfToday = new Date()
  startOfToday.setHours(0, 0, 0, 0)
  const startOfWeek = startOfToday.getTime() - 7 * DAY_MS

  const buckets = { today: [], thisWeek: [], older: [] }
  visibleItems.value.forEach(notification => {
    const created = new Date(notification.createdAt).getTime()
    const key = created >= startOfToday.getTime() ? 'today' : created >= startOfWeek ? 'thisWeek' : 'older'
    buckets[key].push(toRow(notification))
  })

  return Object.entries(buckets)
    .filter(([, rows]) => rows.length)
    .map(([key, rows]) => ({ key, label: t.value.notifications.groups[key], rows }))
})

const loadFirstPage = async () => {
  await notificationsStore.fetchPage({ unreadOnly: filter.value === 'unread' })
  hasLoaded.value = true
}

const loadMore = () =>
  notificationsStore.fetchPage({ cursor: notificationsStore.nextCursor, unreadOnly: filter.value === 'unread' })

const setFilter = key => {
  if (key === filter.value) return
  router.replace({ query: key === 'unread' ? { ...route.query, filter: 'unread' } : {} })
}

const handleRowClick = async notification => {
  try {
    await notificationsStore.markAsRead(notification.id)
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

watch(filter, loadFirstPage, { immediate: true })
</script>
