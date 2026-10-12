<template>
  <div class="flex flex-col gap-6 animate-brief-rise">
    <div class="bg-white border border-hairline rounded-[20px] px-5 sm:px-7 py-2">
      <div
        v-for="(row, i) in rows"
        :key="row.k"
        class="grid grid-cols-[minmax(0,1fr)_auto] sm:grid-cols-[minmax(0,170px)_minmax(0,1fr)_auto] gap-x-4 gap-y-1 items-baseline py-[18px]"
        :class="i ? 'border-t border-hairline' : ''"
      >
        <span class="text-[15px] text-ink-400">{{ row.k }}</span>
        <span
          class="order-3 sm:order-none col-span-2 sm:col-span-1 text-[15px] font-semibold leading-normal text-ink-900 whitespace-pre-line"
        >
          {{ row.v || '—' }}
        </span>
        <button
          type="button"
          class="justify-self-end text-caption font-semibold text-ink-900 border-b border-hairline hover:border-ink-900"
          @click="survey.goTo(row.step)"
        >
          Ubah
        </button>
      </div>
    </div>

    <div v-if="needsPhone" class="max-w-[680px] bg-white border border-hairline rounded-[20px] p-6 md:p-8">
      <label :for="phoneId" class="block text-body font-medium text-ink-700 mb-1">Nomor telepon</label>
      <p class="text-caption leading-normal text-ink-400 mt-0 mb-3">
        Belum ada di profil Anda. Nomor ini tidak ditampilkan ke arsitek sebelum Anda menerima penawarannya.
      </p>
      <input
        :id="phoneId"
        v-model="survey.state.phone"
        type="tel"
        autocomplete="tel"
        placeholder="cth. +62 812 3456 7890"
        class="w-full box-border h-12 px-4 border rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors"
        :class="phoneError ? 'border-[#B42318]' : 'border-hairline'"
      />
      <p v-if="phoneError" class="text-caption text-[#B42318] mt-2 mb-0">{{ phoneError }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, useId } from 'vue'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { SURVEY_CATEGORY_GROUPS, START_OPTIONS } from '@/constants/projectSurvey'
import { subCategoryLabel } from '@/constants/projectTaxonomy'
import { provinceLabel } from '@/constants/regions'

defineProps({
  needsPhone: { type: Boolean, default: false },
  phoneError: { type: String, default: '' }
})

const survey = useProjectSurvey()
const phoneId = useId()

const rows = computed(() => {
  const s = survey.state
  const intOnly = survey.interiorOnly.value
  const group = SURVEY_CATEGORY_GROUPS.find(g => g.id === s.groupId)
  const sub = subCategoryLabel(s.category, s.subCategory, 'id')
  const property =
    group && sub ? [group.label, sub, survey.isApartment.value && s.aptType].filter(Boolean).join(' · ') : ''
  const location =
    s.province && s.city
      ? `${s.city}, ${provinceLabel(s.province)}${s.address.trim() ? `\n${s.address.trim()}` : ''}`
      : ''

  const size = intOnly
    ? [s.building && `Area ${s.building} m²`, survey.pieceMode.value && s.wall && `Panjang dinding ${s.wall} m`]
        .filter(Boolean)
        .join(' · ') || 'Belum diisi'
    : s.land
      ? `Tanah ${s.land} m² · Area proyek ${s.building} m²\n${s.floorsUnknown ? 'Jumlah lantai belum ditentukan' : `${s.floors} lantai`}`
      : ''

  const vision = s.vision.trim()
  const needs =
    (vision.length > 180 ? `${vision.slice(0, 180)}…` : vision) +
    (survey.refs.length ? `\n${survey.refs.length} gambar referensi` : '')

  const start =
    s.startType === 'SPECIFIC_DATE' && s.startDate
      ? new Date(`${s.startDate}T00:00:00`).toLocaleDateString('id-ID', {
          day: 'numeric',
          month: 'long',
          year: 'numeric'
        })
      : START_OPTIONS.find(o => o.value === s.startType)?.label || ''

  return [
    { k: 'Layanan', v: survey.services.value.map(service => service.label).join(', '), step: 'lineup' },
    { k: 'Lingkup proyek', v: survey.scopeLabels.value.join(', '), step: 'scope' },
    survey.interiorOn.value && { k: 'Area interior', v: survey.areaSummary.value.join('\n'), step: 'scope' },
    { k: 'Jenis properti', v: property, step: 'property' },
    { k: 'Lokasi', v: location, step: 'alamat' },
    { k: intOnly ? 'Ukuran' : 'Luas', v: size, step: 'luas' },
    { k: 'Kebutuhan', v: needs, step: 'kebutuhan' },
    { k: 'Dokumen', v: `${survey.selectedDocs.value.length} dokumen dipilih`, step: 'deliv' },
    { k: intOnly ? 'Budget fee desain' : 'Budget fee arsitek', v: s.designFee ? `Rp ${s.designFee}` : '', step: 'fee' },
    { k: 'Mulai desain', v: start, step: 'mulai' }
  ].filter(Boolean)
})
</script>
