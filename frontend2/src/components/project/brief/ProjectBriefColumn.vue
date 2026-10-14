<template>
  <div class="flex flex-col gap-6 min-w-0">
    <section class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-7">
      <div class="flex flex-wrap-reverse justify-between items-start gap-4">
        <div class="flex flex-col gap-2 min-w-0">
          <h1
            class="m-0 text-2xl md:text-[32px] break-words font-bold tracking-[-0.03em] leading-[1.15] text-ink-900 text-pretty"
          >
            {{ brief.title }}
          </h1>
          <p class="m-0 text-body-sm text-ink-400">{{ brief.metaLine }}</p>
        </div>
        <ProjectStatusBadge :status="status" class="shrink-0" />
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-6 border-t border-hairline pt-6">
        <div v-for="fact in brief.facts" :key="fact.k" class="flex flex-col gap-1.5 min-w-0">
          <span class="text-caption-sm font-normal text-ink-400">{{ fact.k }}</span>
          <span class="text-[22px] font-semibold tracking-[-0.025em] leading-[1.2] text-ink-900">{{ fact.v }}</span>
          <span v-if="fact.sub" class="text-caption-sm font-normal text-ink-300">{{ fact.sub }}</span>
        </div>
      </div>
    </section>

    <section v-if="brief.vision" class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-4">
      <h2 class="m-0 text-xl font-bold tracking-[-0.025em] text-ink-900">{{ needsTitle }}</h2>
      <p class="m-0 text-body leading-[1.65] text-ink-500 whitespace-pre-line text-pretty">{{ brief.vision }}</p>
    </section>

    <section
      v-if="brief.scopes.length || brief.areas.length"
      class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-6"
    >
      <h2 class="m-0 text-xl font-bold tracking-[-0.025em] text-ink-900">{{ copy.scopeTitle }}</h2>
      <div v-if="brief.scopes.length" class="flex flex-wrap gap-2">
        <span
          v-for="scope in brief.scopes"
          :key="scope"
          class="inline-flex items-center h-9 px-4 rounded-full bg-surface-alt border border-hairline text-caption text-ink-900"
        >
          {{ scope }}
        </span>
      </div>
      <div v-if="brief.areas.length" class="flex flex-col">
        <p class="m-0 mb-1 text-micro font-bold tracking-[0.08em] uppercase text-ink-300">
          {{ copy.interiorAreas }}
        </p>
        <div
          v-for="row in brief.areas"
          :key="row.area + row.scope"
          class="grid grid-cols-[minmax(0,140px)_minmax(0,1fr)] md:grid-cols-[200px_minmax(0,1fr)] gap-4 py-3.5 border-b border-[#F0F0F0] text-body-sm"
        >
          <span class="text-ink-400">{{ row.area }}</span>
          <span class="font-semibold text-ink-900">{{ row.scope }}</span>
        </div>
      </div>
    </section>

    <section
      v-if="brief.docs.length"
      class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-5"
    >
      <div class="flex justify-between items-baseline gap-4">
        <h2 class="m-0 text-xl font-bold tracking-[-0.025em] text-ink-900">{{ copy.documentsTitle }}</h2>
        <span class="text-caption font-normal text-ink-400">
          {{ copy.documentsCount.replace('{n}', brief.docCount) }}
        </span>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-4">
        <div v-for="group in brief.docs" :key="group.title" class="flex flex-col gap-2">
          <p class="m-0 text-micro font-bold tracking-[0.08em] uppercase text-ink-300">{{ group.title }}</p>
          <div v-for="item in group.items" :key="item" class="flex items-start gap-2.5 text-body-sm leading-[1.45]">
            <Check :size="16" :stroke-width="2.4" class="mt-0.5 shrink-0 text-ink-900" />
            <span class="text-ink-900">{{ item }}</span>
          </div>
        </div>
      </div>
    </section>

    <section
      v-if="brief.images.length || brief.files.length"
      class="bg-white border border-hairline rounded-card p-6 md:p-8 flex flex-col gap-5"
    >
      <div class="flex flex-col gap-1">
        <h2 class="m-0 text-xl font-bold tracking-[-0.025em] text-ink-900">{{ copy.refsTitle }}</h2>
        <p class="m-0 text-caption font-normal text-ink-400">{{ refsHint }}</p>
      </div>
      <div v-if="brief.images.length" class="grid grid-cols-2 sm:grid-cols-3 gap-3">
        <a
          v-for="image in brief.images"
          :key="image.id"
          :href="image.filePath"
          target="_blank"
          rel="noopener noreferrer"
          class="block aspect-[4/3] rounded-[14px] overflow-hidden bg-surface-alt"
        >
          <img :src="image.filePath" :alt="image.fileName" class="w-full h-full object-cover block" loading="lazy" />
        </a>
      </div>
      <div v-if="brief.files.length" class="flex flex-col gap-2">
        <a
          v-for="file in brief.files"
          :key="file.id"
          :href="file.filePath"
          target="_blank"
          rel="noopener noreferrer"
          class="flex items-center gap-3 px-4 py-3 border border-hairline rounded-xl hover:border-ink-900 transition"
        >
          <span class="w-9 h-9 rounded-full bg-surface-alt flex items-center justify-center shrink-0">
            <FileText :size="16" class="text-ink-900" />
          </span>
          <span class="flex-1 min-w-0 flex flex-col">
            <span class="text-caption font-semibold text-ink-900 truncate">{{ file.fileName }}</span>
            <span class="text-caption-sm font-normal text-ink-300">{{ fileTypeLabel(file) }}</span>
          </span>
          <Download :size="16" class="text-ink-400 shrink-0" />
        </a>
      </div>
    </section>

    <section v-if="hasLocation" class="bg-white border border-hairline rounded-card p-6 md:p-8">
      <SiteLocationMap
        compact
        :full-address="project.fullAddress"
        :city="project.city"
        :province="project.province"
        :latitude="project.latitude"
        :longitude="project.longitude"
      />
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Check, Download, FileText } from 'lucide-vue-next'
import { useI18n } from '@/composables/useI18n'
import ProjectStatusBadge from '@/components/project/ProjectStatusBadge.vue'
import SiteLocationMap from '@/components/project/SiteLocationMap.vue'

const props = defineProps({
  project: { type: Object, required: true },
  brief: { type: Object, required: true },
  status: { type: String, required: true },
  needsTitle: { type: String, required: true },
  refsHint: { type: String, required: true }
})

const { t } = useI18n()
const copy = computed(() => t.value.projectDetailV2)

const hasLocation = computed(() => !!(props.project.fullAddress || props.project.city))

const fileTypeLabel = file => (file.fileName?.split('.').pop() || file.fileType?.split('/').pop() || '').toUpperCase()
</script>
