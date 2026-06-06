<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ t.userManagement.title }}</h1>
    <p class="text-gray-500 text-sm mb-8">{{ t.userManagement.subtitle }}</p>

    <div v-if="loading" class="space-y-3">
      <div v-for="i in 5" :key="i" class="bg-white rounded-xl p-4 animate-pulse border border-gray-100">
        <div class="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
        <div class="h-3 bg-gray-100 rounded w-1/2"></div>
      </div>
    </div>

    <div v-else>
      <div class="bg-white rounded-xl border border-gray-100 overflow-hidden mb-4">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">{{ t.userManagement.colUser }}</th>
              <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">{{ t.userManagement.colRoles }}</th>
              <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">{{ t.userManagement.colStatus }}</th>
              <th class="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">{{ t.userManagement.colJoined }}</th>
              <th class="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50/50">
              <td class="px-4 py-3">
                <p class="font-medium text-gray-900">{{ user.firstName }} {{ user.lastName }}</p>
                <p class="text-xs text-gray-400">{{ user.email }}</p>
              </td>
              <td class="px-4 py-3">
                <div class="flex gap-1 flex-wrap">
                  <span
                    v-for="role in user.roles"
                    :key="role"
                    class="text-xs px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full font-medium"
                    >{{ role }}</span
                  >
                  <span v-if="!user.roles?.length" class="text-xs text-gray-300">—</span>
                </div>
              </td>
              <td class="px-4 py-3">
                <span
                  class="text-xs font-semibold px-2 py-0.5 rounded-full"
                  :class="user.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'"
                >
                  {{ user.active ? t.userManagement.statusActive : t.userManagement.statusDeactivated }}
                </span>
              </td>
              <td class="px-4 py-3 text-xs text-gray-400">{{ formatDate(user.createdAt) }}</td>
              <td class="px-4 py-3 text-right">
                <button
                  v-if="user.active && !user.superuser"
                  @click="deactivate(user.id)"
                  :disabled="processing === user.id"
                  class="text-xs px-3 py-1 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition disabled:opacity-50"
                >
                  {{ t.userManagement.deactivate }}
                </button>
                <button
                  v-else-if="!user.active"
                  @click="reactivate(user.id)"
                  :disabled="processing === user.id"
                  class="text-xs px-3 py-1 rounded-lg border border-green-200 text-green-600 hover:bg-green-50 transition disabled:opacity-50"
                >
                  {{ t.userManagement.reactivate }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-between">
        <p class="text-xs text-gray-400">{{ t.userManagement.page }} {{ page + 1 }} {{ t.userManagement.of }} {{ totalPages }}</p>
        <div class="flex gap-2">
          <button
            @click="prev"
            :disabled="page === 0"
            class="px-3 py-1.5 text-xs rounded-lg border border-gray-200 disabled:opacity-40 hover:bg-gray-50"
          >
            {{ t.userManagement.prev }}
          </button>
          <button
            @click="next"
            :disabled="page >= totalPages - 1"
            class="px-3 py-1.5 text-xs rounded-lg border border-gray-200 disabled:opacity-40 hover:bg-gray-50"
          >
            {{ t.userManagement.next }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="error" class="mt-4 bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminUsersAPI } from '@/services/adminApi'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const loading = ref(true)
const error = ref(null)
const processing = ref(null)
const users = ref([])
const page = ref(0)
const totalPages = ref(1)

const load = async () => {
  loading.value = true
  error.value = null
  try {
    const res = await adminUsersAPI.getAll(page.value, 20)
    const data = res.data.data
    users.value = data.content || []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    error.value = 'Failed to load users'
  } finally {
    loading.value = false
  }
}

const deactivate = async id => {
  if (!confirm(t.value.userManagement.deactivateConfirm)) return
  processing.value = id
  try {
    await adminUsersAPI.deactivate(id)
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to deactivate user'
  } finally {
    processing.value = null
  }
}

const reactivate = async id => {
  processing.value = id
  try {
    await adminUsersAPI.reactivate(id)
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to reactivate user'
  } finally {
    processing.value = null
  }
}

const prev = () => {
  if (page.value > 0) {
    page.value--
    load()
  }
}
const next = () => {
  if (page.value < totalPages.value - 1) {
    page.value++
    load()
  }
}
const formatDate = d =>
  d ? new Date(d).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' }) : '-'

onMounted(load)
</script>
