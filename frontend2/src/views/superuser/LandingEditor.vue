<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ t.landingEditor.title }}</h1>
    <p class="text-gray-500 text-sm mb-8">{{ t.landingEditor.subtitle }}</p>

    <div class="grid grid-cols-1 xl:grid-cols-[minmax(0,560px)_minmax(0,1fr)] gap-8 items-start">
      <!-- Live preview -->
      <div class="bg-white rounded-xl border border-gray-100 p-6 xl:sticky xl:top-8">
        <p class="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-4">
          {{ t.landingEditor.previewLabel }}
        </p>
        <ProposalCarousel :slides="previewSlides" :loading="loading" />
      </div>

      <!-- Editor panel -->
      <div class="space-y-4">
        <div v-if="loading" class="space-y-3">
          <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-5 animate-pulse border border-gray-100">
            <div class="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
            <div class="h-3 bg-gray-100 rounded w-1/2"></div>
          </div>
        </div>

        <template v-else>
          <!-- Slide list -->
          <div class="bg-white rounded-xl border border-gray-100 divide-y divide-gray-100">
            <div
              v-for="(slide, index) in slides"
              :key="slide.id"
              class="p-4 flex items-center gap-3"
              :class="{ 'bg-gray-50': editingId === slide.id }"
            >
              <div class="w-12 h-12 rounded-lg bg-gray-100 overflow-hidden shrink-0 flex items-center justify-center">
                <img
                  v-if="slide.imageUrl"
                  :src="slide.imageUrl"
                  :alt="slide.architectName"
                  class="w-full h-full object-cover"
                />
                <ImageIcon v-else class="w-5 h-5 text-gray-300" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-semibold text-gray-900 text-sm truncate">{{ slide.architectName }}</p>
                <p class="text-xs text-gray-400 truncate">
                  <span v-if="slide.rating">★ {{ Number(slide.rating).toFixed(1) }} · </span>
                  <span v-if="!slide.active" class="text-amber-600 font-medium">{{ t.landingEditor.hidden }} · </span>
                  {{ slide.reviewQuote || t.landingEditor.noQuote }}
                </p>
              </div>
              <div class="flex items-center gap-1 shrink-0">
                <button
                  type="button"
                  :disabled="index === 0 || savingOrder"
                  :title="t.landingEditor.moveUp"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-gray-900 hover:bg-gray-100 transition disabled:opacity-30 disabled:hover:bg-transparent"
                  @click="move(index, -1)"
                >
                  <ChevronUp :size="16" />
                </button>
                <button
                  type="button"
                  :disabled="index === slides.length - 1 || savingOrder"
                  :title="t.landingEditor.moveDown"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-gray-900 hover:bg-gray-100 transition disabled:opacity-30 disabled:hover:bg-transparent"
                  @click="move(index, 1)"
                >
                  <ChevronDown :size="16" />
                </button>
                <button
                  type="button"
                  :title="t.landingEditor.edit"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-gray-900 hover:bg-gray-100 transition"
                  @click="startEdit(slide)"
                >
                  <Pencil :size="16" />
                </button>
                <button
                  type="button"
                  :title="t.landingEditor.delete"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition"
                  @click="deleteTarget = slide"
                >
                  <Trash2 :size="16" />
                </button>
              </div>
            </div>

            <div v-if="slides.length === 0" class="p-10 text-center">
              <p class="text-gray-400 text-sm">{{ t.landingEditor.noSlides }}</p>
            </div>
          </div>

          <button
            v-if="!form"
            type="button"
            class="w-full py-3 rounded-xl border-2 border-dashed border-gray-200 text-sm font-semibold text-gray-500 hover:border-gray-400 hover:text-gray-900 transition flex items-center justify-center gap-2"
            @click="startCreate"
          >
            <Plus :size="16" /> {{ t.landingEditor.addSlide }}
          </button>

          <!-- Form -->
          <form v-if="form" class="bg-white rounded-xl border border-gray-100 p-6 space-y-5" @submit.prevent="save">
            <h2 class="font-bold text-gray-900">
              {{ editingId ? t.landingEditor.editSlide : t.landingEditor.newSlide }}
            </h2>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">{{ t.landingEditor.image }}</label>
              <div class="flex items-center gap-4">
                <div
                  class="w-24 h-24 rounded-xl bg-gray-100 overflow-hidden shrink-0 flex items-center justify-center border border-gray-200"
                >
                  <img v-if="formImageUrl" :src="formImageUrl" alt="" class="w-full h-full object-cover" />
                  <ImageIcon v-else class="w-6 h-6 text-gray-300" />
                </div>
                <div class="space-y-2">
                  <input
                    ref="fileInput"
                    type="file"
                    accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                    class="hidden"
                    @change="onFileChange"
                  />
                  <button
                    type="button"
                    class="px-3 py-1.5 text-xs font-semibold rounded-lg border border-gray-200 text-gray-600 hover:border-gray-400 transition"
                    @click="fileInput?.click()"
                  >
                    {{ t.landingEditor.chooseImage }}
                  </button>
                  <button
                    v-if="formImageUrl"
                    type="button"
                    class="px-3 py-1.5 text-xs font-semibold rounded-lg border border-red-200 text-red-600 hover:bg-red-50 transition ml-2"
                    @click="clearImage"
                  >
                    {{ t.landingEditor.removeImage }}
                  </button>
                  <p class="text-xs text-gray-400">{{ t.landingEditor.imageHint }}</p>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2"
                  >{{ t.landingEditor.architectName }} *</label
                >
                <input v-model="form.architectName" type="text" maxlength="120" required :class="inputClass" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">{{ t.landingEditor.rating }}</label>
                <input v-model="form.rating" type="number" min="0" max="5" step="0.1" :class="inputClass" />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">{{ t.landingEditor.quote }}</label>
              <textarea v-model="form.reviewQuote" rows="3" :class="inputClass" />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">{{ t.landingEditor.reviewerName }}</label>
              <input v-model="form.reviewerName" type="text" maxlength="120" :class="inputClass" />
            </div>

            <div class="flex items-center gap-6">
              <label class="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
                <input v-model="form.verified" type="checkbox" class="w-4 h-4 rounded accent-gray-900" />
                {{ t.landingEditor.verified }}
              </label>
              <label class="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
                <input v-model="form.active" type="checkbox" class="w-4 h-4 rounded accent-gray-900" />
                {{ t.landingEditor.visible }}
              </label>
            </div>

            <div class="flex gap-3 justify-end pt-2">
              <button
                type="button"
                class="px-4 py-2 text-sm font-medium text-gray-600 rounded-lg hover:bg-gray-100 transition"
                @click="cancelEdit"
              >
                {{ t.landingEditor.cancel }}
              </button>
              <button
                type="submit"
                :disabled="saving || !form.architectName.trim()"
                class="px-4 py-2 text-sm font-semibold text-white bg-gray-900 rounded-lg hover:bg-gray-700 transition disabled:opacity-50"
              >
                {{ saving ? t.landingEditor.saving : t.landingEditor.save }}
              </button>
            </div>
          </form>
        </template>

        <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 rounded-lg p-4 text-sm">{{ error }}</div>
      </div>
    </div>

    <!-- Delete confirmation -->
    <div v-if="deleteTarget" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="fixed inset-0 bg-black/50 backdrop-blur-sm" @click="deleteTarget = null"></div>
      <div class="relative min-h-screen flex items-center justify-center p-4">
        <div class="relative bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
          <h3 class="text-lg font-bold text-gray-900 mb-1">{{ t.landingEditor.deleteTitle }}</h3>
          <p class="text-sm text-gray-500 mb-5">
            {{ t.landingEditor.deleteHint }} <span class="font-semibold">{{ deleteTarget.architectName }}</span>
          </p>
          <div class="flex gap-3 justify-end">
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-gray-600 rounded-lg hover:bg-gray-100 transition"
              @click="deleteTarget = null"
            >
              {{ t.landingEditor.cancel }}
            </button>
            <button
              type="button"
              :disabled="saving"
              class="px-4 py-2 text-sm font-semibold text-white bg-red-600 rounded-lg hover:bg-red-700 transition disabled:opacity-50"
              @click="confirmDelete"
            >
              {{ t.landingEditor.delete }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ChevronUp, ChevronDown, Pencil, Trash2, Plus, Image as ImageIcon } from 'lucide-vue-next'
import ProposalCarousel from '@/components/landing/ProposalCarousel.vue'
import { adminLandingAPI } from '@/services/adminApi'
import { validateFile, getFilePreviewUrl, revokeFilePreviewUrl } from '@/services/uploadService'
import { useI18n } from '@/composables/useI18n'

const { t } = useI18n()

const inputClass =
  'w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-gray-400 outline-none text-sm'

const slides = ref([])
const loading = ref(true)
const saving = ref(false)
const savingOrder = ref(false)
const error = ref(null)

const editingId = ref(null)
const form = ref(null)
const imageFile = ref(null)
const localPreviewUrl = ref(null)
const fileInput = ref(null)
const deleteTarget = ref(null)

const formImageUrl = computed(() => localPreviewUrl.value || form.value?.imageUrl || null)

// The carousel renders saved slides with the in-progress draft merged in, so the
// superuser sees unsaved edits (including a not-yet-uploaded image) immediately.
const previewSlides = computed(() => {
  const draft = form.value
    ? {
        id: editingId.value ?? 'draft',
        imageUrl: formImageUrl.value,
        architectName: form.value.architectName || t.value.landingEditor.newSlide,
        avatarInitial: (form.value.architectName || '?').charAt(0).toUpperCase(),
        verified: form.value.verified,
        rating: form.value.rating,
        reviewQuote: form.value.reviewQuote,
        reviewerName: form.value.reviewerName,
        active: form.value.active
      }
    : null

  const visible = slides.value.filter(s => s.active && s.id !== editingId.value)
  if (!draft || !draft.active) return visible

  if (editingId.value) {
    const at = slides.value.findIndex(s => s.id === editingId.value)
    const merged = [...visible]
    merged.splice(Math.min(at, merged.length), 0, draft)
    return merged
  }
  return [...visible, draft]
})

const load = async () => {
  loading.value = true
  try {
    const res = await adminLandingAPI.getHeroSlides()
    slides.value = res.data.data || []
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to load hero slides'
  } finally {
    loading.value = false
  }
}

const blankForm = () => ({
  architectName: '',
  rating: '',
  reviewQuote: '',
  reviewerName: '',
  verified: true,
  active: true,
  imageUrl: null
})

const startCreate = () => {
  clearLocalPreview()
  editingId.value = null
  imageFile.value = null
  form.value = blankForm()
}

const startEdit = slide => {
  clearLocalPreview()
  editingId.value = slide.id
  imageFile.value = null
  form.value = {
    architectName: slide.architectName || '',
    rating: slide.rating ?? '',
    reviewQuote: slide.reviewQuote || '',
    reviewerName: slide.reviewerName || '',
    verified: slide.verified,
    active: slide.active,
    imageUrl: slide.imageUrl
  }
}

const cancelEdit = () => {
  clearLocalPreview()
  form.value = null
  editingId.value = null
  imageFile.value = null
  error.value = null
}

const clearLocalPreview = () => {
  revokeFilePreviewUrl(localPreviewUrl.value)
  localPreviewUrl.value = null
}

const onFileChange = event => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return

  try {
    validateFile(file)
  } catch (e) {
    error.value = e.message
    return
  }

  error.value = null
  clearLocalPreview()
  imageFile.value = file
  localPreviewUrl.value = getFilePreviewUrl(file)
}

const clearImage = () => {
  clearLocalPreview()
  imageFile.value = null
  form.value.imageUrl = null
}

const save = async () => {
  saving.value = true
  error.value = null

  const formData = new FormData()
  formData.append('architectName', form.value.architectName.trim())
  formData.append('verified', form.value.verified)
  formData.append('active', form.value.active)
  if (form.value.rating !== '' && form.value.rating !== null) formData.append('rating', form.value.rating)
  if (form.value.reviewQuote) formData.append('reviewQuote', form.value.reviewQuote)
  if (form.value.reviewerName) formData.append('reviewerName', form.value.reviewerName)
  if (imageFile.value) formData.append('image', imageFile.value)
  if (editingId.value && !imageFile.value && !form.value.imageUrl) formData.append('removeImage', 'true')

  try {
    if (editingId.value) {
      await adminLandingAPI.updateHeroSlide(editingId.value, formData)
    } else {
      await adminLandingAPI.createHeroSlide(formData)
    }
    cancelEdit()
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to save slide'
  } finally {
    saving.value = false
  }
}

const confirmDelete = async () => {
  saving.value = true
  error.value = null
  try {
    await adminLandingAPI.deleteHeroSlide(deleteTarget.value.id)
    if (editingId.value === deleteTarget.value.id) cancelEdit()
    deleteTarget.value = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to delete slide'
  } finally {
    saving.value = false
  }
}

const move = async (index, delta) => {
  const target = index + delta
  if (target < 0 || target >= slides.value.length) return

  const reordered = [...slides.value]
  ;[reordered[index], reordered[target]] = [reordered[target], reordered[index]]
  slides.value = reordered

  savingOrder.value = true
  error.value = null
  try {
    const res = await adminLandingAPI.reorderHeroSlides(reordered.map(s => s.id))
    slides.value = res.data.data || reordered
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to reorder slides'
    await load()
  } finally {
    savingOrder.value = false
  }
}

onMounted(load)
onUnmounted(clearLocalPreview)
</script>
