<template>
  <div class="flex flex-col gap-8 animate-brief-rise">
    <textarea
      v-model="state.vision"
      rows="7"
      :placeholder="placeholder"
      aria-label="Kebutuhan proyek"
      class="w-full box-border px-[18px] py-4 border border-hairline rounded-2xl bg-white text-[17px] leading-relaxed text-ink-900 placeholder-ink-300 outline-none resize-y focus:border-ink-900 transition-colors"
    ></textarea>

    <div>
      <div class="text-body font-medium text-ink-700 mb-1">
        {{ survey.interiorOnly.value ? 'Foto kondisi saat ini dan referensi' : 'Gambar referensi' }}
      </div>
      <p class="text-caption leading-normal text-ink-400 mt-0 mb-3">{{ description }}</p>
      <label
        class="flex flex-col items-center justify-center gap-2 min-h-[140px] p-6 box-border border border-dashed border-hairline rounded-2xl bg-surface-alt cursor-pointer text-center hover:border-ink-900 transition-colors"
        :class="full ? 'opacity-50 pointer-events-none' : ''"
      >
        <input type="file" accept="image/jpeg,image/png" multiple class="hidden" :disabled="full" @change="addRefs" />
        <Upload :size="24" :stroke-width="2" class="text-ink-900" />
        <span class="text-[15px] font-bold text-ink-900">Klik untuk unggah gambar</span>
        <span class="text-caption text-ink-400">
          {{
            survey.interiorOnly.value
              ? 'Foto kondisi saat ini, inspirasi desain, atau sketsa'
              : 'Foto lahan, inspirasi desain, atau sketsa'
          }}
        </span>
      </label>
      <div v-if="previews.length" class="grid grid-cols-[repeat(auto-fill,minmax(120px,1fr))] gap-2.5 mt-3">
        <div
          v-for="(preview, i) in previews"
          :key="preview.url"
          class="relative aspect-square rounded-xl overflow-hidden bg-surface-alt"
        >
          <img :src="preview.url" :alt="preview.name" class="w-full h-full object-cover" />
          <button
            type="button"
            :aria-label="`Hapus ${preview.name}`"
            class="absolute top-1.5 right-1.5 w-7 h-7 rounded-full bg-[rgba(17,17,17,.75)] text-white flex items-center justify-center"
            @click="removeRef(i)"
          >
            <X :size="12" :stroke-width="3" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount } from 'vue'
import { Upload, X } from 'lucide-vue-next'
import { useProjectSurvey } from '@/composables/useProjectSurvey'
import { MAX_REFERENCE_IMAGES } from '@/constants/projectSurvey'

const survey = useProjectSurvey()
const state = survey.state

const full = computed(() => survey.refs.length >= MAX_REFERENCE_IMAGES)

const placeholder = computed(() => {
  if (!survey.interiorOnly.value) {
    return 'cth. Rumah 2 lantai untuk keluarga dengan 2 anak. 4 kamar tidur, ruang kerja di lantai atas, dapur terbuka ke taman. Gaya tropis modern, banyak cahaya alami.'
  }
  if (state.intAreas.Dapur) {
    return 'cth. Kitchen set bentuk L di dinding sekitar 3 m. Kabinet atas sampai plafon, warna kayu terang, ada tempat oven tanam dan rak bumbu.'
  }
  return 'cth. Kamar tidur utama untuk pasangan. Ingin suasana hangat dengan lemari built-in, meja rias, dan lampu yang bisa diredupkan.'
})

const description = computed(() =>
  survey.interiorOnly.value
    ? `Opsional. Foto ${survey.areaNames.value || 'area'} saat ini membantu desainer memahami ukuran ruang, posisi jendela, serta titik listrik dan air yang sudah ada.`
    : `Opsional. JPG atau PNG, maksimal ${MAX_REFERENCE_IMAGES} gambar.`
)

// One object URL per file, revoked when the file is removed or the step unmounts
const urlCache = new Map()
const previews = computed(() =>
  survey.refs.map(file => {
    if (!urlCache.has(file)) urlCache.set(file, URL.createObjectURL(file))
    return { name: file.name, url: urlCache.get(file) }
  })
)

const addRefs = event => {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  survey.refs.push(...files.slice(0, MAX_REFERENCE_IMAGES - survey.refs.length))
}

const removeRef = index => {
  const [file] = survey.refs.splice(index, 1)
  if (file && urlCache.has(file)) {
    URL.revokeObjectURL(urlCache.get(file))
    urlCache.delete(file)
  }
}

onBeforeUnmount(() => {
  urlCache.forEach(url => URL.revokeObjectURL(url))
  urlCache.clear()
})
</script>
