<template>
  <div
    class="max-w-[680px] bg-white border border-hairline rounded-[20px] p-6 md:p-8 flex flex-col gap-6 animate-brief-rise"
  >
    <div class="flex flex-col">
      <div
        v-for="row in savedRows"
        :key="row.k"
        class="grid grid-cols-[110px_minmax(0,1fr)] sm:grid-cols-[140px_minmax(0,1fr)] gap-4 py-3.5 border-b border-hairline text-[15px] leading-normal"
      >
        <span class="text-ink-400">{{ row.k }}</span>
        <span class="text-ink-900 font-semibold">{{ row.v }}</span>
      </div>
    </div>

    <button
      v-if="!authStore.isAuthenticated"
      type="button"
      class="self-start h-[52px] px-8 rounded-full bg-ink-900 text-white text-[17px] font-bold hover:opacity-85 transition-opacity"
      @click="$emit('login')"
    >
      Masuk / Daftar
    </button>

    <div v-else-if="authStore.isClient" class="flex items-center gap-3 text-[15px] text-ink-900">
      <span class="flex-none w-7 h-7 rounded-full bg-ink-900 text-white flex items-center justify-center">
        <Check :size="14" :stroke-width="3" />
      </span>
      <span>
        Tersimpan di akun <strong>{{ authStore.user?.email }}</strong>
      </span>
    </div>

    <div v-else class="flex flex-col gap-3 items-start">
      <p class="text-[15px] leading-normal text-ink-700 m-0">
        Akun <strong class="text-ink-900">{{ authStore.user?.email }}</strong> belum aktif sebagai klien. Aktifkan peran
        klien untuk menyimpan brief dan menerima penawaran.
      </p>
      <p v-if="activateError" class="text-caption text-[#B42318] m-0">{{ activateError }}</p>
      <button
        type="button"
        class="h-12 px-6 rounded-full bg-ink-900 text-white text-[15px] font-bold hover:opacity-85 transition-opacity disabled:opacity-50"
        :disabled="activating"
        @click="activateClient"
      >
        Aktifkan peran klien
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Check } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { SURVEY_CATEGORY_GROUPS } from '@/constants/projectSurvey'
import { subCategoryLabel } from '@/constants/projectTaxonomy'

defineEmits(['login'])

const authStore = useAuthStore()
const survey = useProjectSurvey()

const activating = ref(false)
const activateError = ref('')

const savedRows = computed(() => {
  const group = SURVEY_CATEGORY_GROUPS.find(g => g.id === survey.state.groupId)
  const sub = subCategoryLabel(survey.state.category, survey.state.subCategory, 'id')
  return [
    { k: 'Layanan', v: survey.services.value.map(s => s.label).join(', ') || '—' },
    { k: 'Lingkup proyek', v: survey.scopeLabels.value.join(', ') || '—' },
    { k: 'Jenis properti', v: group && sub ? `${group.label} · ${sub}` : '—' }
  ]
})

const activateClient = async () => {
  activating.value = true
  activateError.value = ''
  try {
    await authStore.activateRole('CLIENT')
  } catch (err) {
    activateError.value = err.response?.data?.message || 'Gagal mengaktifkan peran klien. Coba lagi.'
  } finally {
    activating.value = false
  }
}
</script>
