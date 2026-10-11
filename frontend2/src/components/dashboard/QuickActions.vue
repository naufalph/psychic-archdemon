<template>
  <section
    v-if="actions.length"
    class="bg-white border border-hairline rounded-[20px] px-6 md:px-8 py-2"
    aria-labelledby="quick-actions-title"
  >
    <h2 id="quick-actions-title" class="text-[17px] font-bold tracking-[-.015em] text-ink-900 m-0 pt-5 pb-1">
      Aksi cepat
    </h2>

    <div
      v-for="(action, i) in actions"
      :key="action.key"
      class="flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-6 py-4"
      :class="i ? 'border-t border-hairline' : ''"
    >
      <span
        class="hidden sm:flex flex-none w-10 h-10 rounded-full bg-surface-alt text-ink-900 items-center justify-center"
        aria-hidden="true"
      >
        <component :is="action.icon" :size="18" :stroke-width="2" />
      </span>

      <div class="flex-1 min-w-0">
        <h3 class="text-[17px] font-bold tracking-[-.015em] leading-snug text-ink-900 m-0">{{ action.title }}</h3>
        <p class="text-caption text-ink-400 m-0 mt-0.5">
          {{ action.meta }}
          <template v-if="action.extra">
            ·
            <router-link :to="action.extra.to" class="font-semibold text-ink-900 hover:opacity-60 transition-opacity">
              {{ action.extra.label }}
            </router-link>
          </template>
        </p>
        <div
          v-if="action.percent != null"
          class="mt-2.5 h-1 max-w-[320px] rounded-full bg-hairline overflow-hidden"
          role="progressbar"
          :aria-valuenow="action.percent"
          aria-valuemin="0"
          aria-valuemax="100"
          :aria-label="action.title"
        >
          <div class="h-full bg-ink-900 rounded-full" :style="{ width: `${action.percent}%` }"></div>
        </div>
      </div>

      <div class="flex items-center gap-4 shrink-0">
        <router-link
          :to="action.cta.to"
          class="inline-flex items-center gap-1.5 h-10 px-4 rounded-full bg-ink-900 text-white text-caption font-bold whitespace-nowrap hover:bg-ink-500 transition-colors"
        >
          {{ action.cta.label }} <ArrowRight :size="16" :stroke-width="2.4" />
        </router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowRight, FileText, UserRound } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import { briefProgress } from '@/utils/briefProgress'
import { getRelativeTime } from '@/utils/notificationUtils'

/**
 * Things the client still has to do, most pressing first. Each entry is plain data, so a new
 * kind of action is one more block in `actions` below; the section hides when nothing is due.
 */
const props = defineProps({
  // DRAFT projects, newest first
  drafts: { type: Array, default: () => [] },
  // The client profile, or null while it is still loading
  profile: { type: Object, default: null }
})

const { t } = useI18n()

const draftAction = computed(() => {
  const draft = props.drafts[0]
  if (!draft) return null
  const progress = briefProgress(draft)
  const at = draft.updatedAt || draft.createdAt
  const others = props.drafts.length - 1
  return {
    key: `draft-${draft.id}`,
    icon: FileText,
    title: `Lanjutkan brief: ${draft.title || 'proyek Anda'}`,
    meta: [`${progress.done}/${progress.total} terisi`, at && `disimpan ${getRelativeTime(at, t.value).toLowerCase()}`]
      .filter(Boolean)
      .join(' · '),
    percent: progress.percent,
    cta: { label: 'Lanjutkan', to: { name: 'ProjectBrief', query: { draft: draft.id } } },
    extra: others > 0 ? { label: `${others} draft lainnya`, to: '/client/projects' } : null
  }
})

const profileAction = computed(() => {
  const profile = props.profile
  if (!profile) return null
  const missing = [!profile.firstName && 'Nama', !profile.phoneNumber && 'Nomor telepon'].filter(Boolean)
  if (!missing.length) return null
  return {
    key: 'profile',
    icon: UserRound,
    title: 'Lengkapi profil',
    meta: `${missing.join(' dan ')} belum diisi`,
    cta: { label: 'Lengkapi', to: '/client/profile' }
  }
})

const actions = computed(() => [draftAction.value, profileAction.value].filter(Boolean))
</script>
