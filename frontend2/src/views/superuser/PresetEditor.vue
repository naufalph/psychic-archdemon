<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ t.presetEditor.title }}</h1>
    <p class="text-gray-500 text-sm mb-8">{{ t.presetEditor.subtitle }}</p>

    <div class="grid grid-cols-1 xl:grid-cols-[minmax(0,420px)_minmax(0,1fr)] gap-8 items-start">
      <!-- Live preview -->
      <div class="bg-white rounded-xl border border-gray-100 p-6 xl:sticky xl:top-8">
        <p class="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-4">
          {{ t.presetEditor.previewLabel }}
        </p>
        <div class="grid grid-cols-2 gap-4">
          <StarterCard
            v-for="preset in previewPresets"
            :key="preset.slug"
            :label="preset.labelEn || t.presetEditor.newPreset"
            :eyebrow="preset.eyebrowEn || '—'"
            :icon="resolvePresetIcon(preset.iconName)"
          />
        </div>
        <p v-if="previewPresets.length === 0" class="text-sm text-gray-400 py-8 text-center">
          {{ t.presetEditor.noPresets }}
        </p>
      </div>

      <!-- Editor panel -->
      <div class="space-y-4">
        <BaseAlert v-if="error" variant="error">{{ error }}</BaseAlert>

        <div v-if="loading" class="space-y-3">
          <div v-for="i in 3" :key="i" class="bg-white rounded-xl p-5 animate-pulse border border-gray-100">
            <div class="h-4 bg-gray-200 rounded w-1/3 mb-2"></div>
            <div class="h-3 bg-gray-100 rounded w-1/2"></div>
          </div>
        </div>

        <template v-else>
          <div class="bg-white rounded-xl border border-gray-100 divide-y divide-gray-100">
            <div
              v-for="(preset, index) in presets"
              :key="preset.id"
              class="p-4 flex items-center gap-3"
              :class="{ 'bg-gray-50': editingId === preset.id }"
            >
              <div class="w-10 h-10 rounded-lg bg-gray-100 shrink-0 flex items-center justify-center">
                <component :is="resolvePresetIcon(preset.iconName)" class="w-5 h-5 text-gray-500" />
              </div>
              <div class="min-w-0 flex-1">
                <p class="font-bold text-gray-900 text-sm truncate">
                  {{ preset.labelEn }}
                  <span v-if="!preset.active" class="text-xs font-normal text-gray-400">
                    · {{ t.presetEditor.hidden }}
                  </span>
                </p>
                <p class="text-xs text-gray-500 truncate">{{ preset.slug }} · {{ presetTypeLabel(preset) }}</p>
              </div>
              <div class="flex items-center gap-1 shrink-0">
                <button
                  type="button"
                  :disabled="index === 0 || savingOrder"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-gray-900 hover:bg-gray-100 disabled:opacity-30"
                  :title="t.presetEditor.moveUp"
                  @click="move(index, -1)"
                >
                  <ChevronUp :size="16" />
                </button>
                <button
                  type="button"
                  :disabled="index === presets.length - 1 || savingOrder"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-gray-900 hover:bg-gray-100 disabled:opacity-30"
                  :title="t.presetEditor.moveDown"
                  @click="move(index, 1)"
                >
                  <ChevronDown :size="16" />
                </button>
                <button
                  type="button"
                  class="px-3 py-1.5 text-xs font-semibold rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50"
                  @click="startEdit(preset)"
                >
                  {{ t.presetEditor.edit }}
                </button>
                <button
                  type="button"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50"
                  :title="t.presetEditor.delete"
                  @click="deleteTarget = preset"
                >
                  <Trash2 :size="16" />
                </button>
              </div>
            </div>

            <p v-if="presets.length === 0" class="p-8 text-center text-sm text-gray-400">
              {{ t.presetEditor.noPresets }}
            </p>
          </div>

          <button
            v-if="!form"
            type="button"
            class="w-full py-3 rounded-xl border-2 border-dashed border-gray-200 text-sm font-semibold text-gray-500 hover:border-gray-400 hover:text-gray-900 transition"
            @click="startCreate"
          >
            + {{ t.presetEditor.addPreset }}
          </button>

          <!-- Edit / create form -->
          <div v-if="form" class="bg-white rounded-xl border border-gray-100 p-6 space-y-4">
            <h2 class="font-bold text-gray-900">
              {{ editingId ? t.presetEditor.editPreset : t.presetEditor.newPreset }}
            </h2>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-semibold text-gray-500 mb-1">
                  {{ t.presetEditor.slug }}
                </label>
                <input v-model="form.slug" :class="inputClass" placeholder="apartment" />
                <p class="text-xs text-gray-400 mt-1">{{ t.presetEditor.slugHint }}</p>
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-500 mb-1">
                  {{ t.presetEditor.projectScope }}
                </label>
                <select v-model="form.projectScope" :class="inputClass">
                  <option v-for="scope in PROJECT_SCOPES" :key="scope.value" :value="scope.value">
                    {{ localeLabel(scope) }}
                  </option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-500 mb-1">
                  {{ t.presetEditor.buildingFunction }}
                </label>
                <select v-model="form.buildingFunction" :class="inputClass" @change="onCategoryChange">
                  <option v-for="cat in PROJECT_CATEGORIES" :key="cat.value" :value="cat.value">
                    {{ localeLabel(cat) }}
                  </option>
                </select>
              </div>
              <div v-if="formSubCategories.length > 0">
                <label class="block text-xs font-semibold text-gray-500 mb-1">
                  {{ t.presetEditor.subCategory }}
                </label>
                <select v-model="form.subCategory" :class="inputClass">
                  <option :value="null">{{ t.presetEditor.subCategoryNone }}</option>
                  <option v-for="sub in formSubCategories" :key="sub.value" :value="sub.value">
                    {{ localeLabel(sub) }}
                  </option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-500 mb-1">
                  {{ t.presetEditor.labelEn }}
                </label>
                <input v-model="form.labelEn" :class="inputClass" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-500 mb-1">
                  {{ t.presetEditor.labelId }}
                </label>
                <input v-model="form.labelId" :class="inputClass" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-500 mb-1">
                  {{ t.presetEditor.eyebrowEn }}
                </label>
                <input v-model="form.eyebrowEn" :class="inputClass" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-gray-500 mb-1">
                  {{ t.presetEditor.eyebrowId }}
                </label>
                <input v-model="form.eyebrowId" :class="inputClass" />
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold text-gray-500 mb-2">
                {{ t.presetEditor.icon }}
              </label>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="name in PRESET_ICON_NAMES"
                  :key="name"
                  type="button"
                  class="w-10 h-10 rounded-lg border-2 flex items-center justify-center transition"
                  :class="
                    form.iconName === name
                      ? 'border-gray-900 bg-gray-900 text-white'
                      : 'border-gray-200 text-gray-500 hover:border-gray-400'
                  "
                  :title="name"
                  @click="form.iconName = name"
                >
                  <component :is="resolvePresetIcon(name)" class="w-5 h-5" />
                </button>
              </div>
            </div>

            <div class="pt-2 border-t border-gray-100">
              <p class="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-3">
                {{ t.presetEditor.defaultsLabel }}
              </p>
              <p class="text-xs text-gray-400 mb-4">{{ t.presetEditor.defaultsHint }}</p>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label class="block text-xs font-semibold text-gray-500 mb-1">
                    {{ t.presetEditor.defaultTitleEn }}
                  </label>
                  <input v-model="form.defaultTitleEn" :class="inputClass" />
                </div>
                <div>
                  <label class="block text-xs font-semibold text-gray-500 mb-1">
                    {{ t.presetEditor.defaultTitleId }}
                  </label>
                  <input v-model="form.defaultTitleId" :class="inputClass" />
                </div>
                <div>
                  <label class="block text-xs font-semibold text-gray-500 mb-1">
                    {{ t.presetEditor.defaultLotSize }}
                  </label>
                  <input v-model="form.defaultLotSize" type="number" min="1" :class="inputClass" />
                </div>
                <div>
                  <label class="block text-xs font-semibold text-gray-500 mb-1">
                    {{ t.presetEditor.defaultDesignBudget }}
                  </label>
                  <input v-model="form.defaultDesignBudget" type="number" min="0" :class="inputClass" />
                </div>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-4">
                <div>
                  <label class="block text-xs font-semibold text-gray-500 mb-1">
                    {{ t.presetEditor.defaultDescriptionEn }}
                  </label>
                  <textarea v-model="form.defaultDescriptionEn" rows="3" :class="inputClass" />
                </div>
                <div>
                  <label class="block text-xs font-semibold text-gray-500 mb-1">
                    {{ t.presetEditor.defaultDescriptionId }}
                  </label>
                  <textarea v-model="form.defaultDescriptionId" rows="3" :class="inputClass" />
                </div>
              </div>
            </div>

            <label class="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
              <input v-model="form.active" type="checkbox" class="w-4 h-4 rounded border-gray-300" />
              {{ t.presetEditor.visible }}
            </label>

            <div class="flex gap-3 pt-2">
              <button
                type="button"
                class="px-4 py-2 text-sm font-semibold rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50"
                @click="cancelEdit"
              >
                {{ t.presetEditor.cancel }}
              </button>
              <button
                type="button"
                :disabled="saving"
                class="px-4 py-2 text-sm font-semibold rounded-lg bg-gray-900 text-white hover:bg-gray-700 disabled:opacity-50"
                @click="save"
              >
                {{ saving ? t.presetEditor.saving : t.presetEditor.save }}
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Delete confirmation -->
    <div
      v-if="deleteTarget"
      class="fixed inset-0 bg-black/40 flex items-center justify-center p-6 z-50"
      @click.self="deleteTarget = null"
    >
      <div class="bg-white rounded-xl p-6 max-w-sm w-full">
        <h3 class="font-bold text-gray-900 mb-2">{{ t.presetEditor.deleteTitle }}</h3>
        <p class="text-sm text-gray-500 mb-6">{{ t.presetEditor.deleteHint }} "{{ deleteTarget.labelEn }}".</p>
        <div class="flex gap-3 justify-end">
          <button
            type="button"
            class="px-4 py-2 text-sm font-semibold rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50"
            @click="deleteTarget = null"
          >
            {{ t.presetEditor.cancel }}
          </button>
          <button
            type="button"
            class="px-4 py-2 text-sm font-semibold rounded-lg bg-red-600 text-white hover:bg-red-700"
            @click="confirmDelete"
          >
            {{ t.presetEditor.delete }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ChevronUp, ChevronDown, Trash2 } from 'lucide-vue-next'
import StarterCard from '@/components/landing/StarterCard.vue'
import BaseAlert from '@/components/ui/BaseAlert.vue'
import { resolvePresetIcon, PRESET_ICON_NAMES } from '@/components/landing/presetIcons'
import { adminLandingAPI } from '@/services/adminApi'
import { useI18n } from '@/composables/useI18n'
import {
  PROJECT_SCOPES,
  PROJECT_CATEGORIES,
  subCategoriesFor,
  isValidSubCategory,
  categoryLabel,
  subCategoryLabel
} from '@/constants/projectTaxonomy'

const { t, locale } = useI18n()

const inputClass =
  'w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-gray-400 outline-none text-sm'

const localeLabel = entry => (locale.value === 'id' ? entry.labelId : entry.labelEn)

const formSubCategories = computed(() => subCategoriesFor(form.value?.buildingFunction))

const onCategoryChange = () => {
  if (!isValidSubCategory(form.value.buildingFunction, form.value.subCategory)) {
    form.value.subCategory = null
  }
}

const presetTypeLabel = preset =>
  preset.subCategory
    ? subCategoryLabel(preset.buildingFunction, preset.subCategory, locale.value)
    : categoryLabel(preset.buildingFunction, locale.value)

const presets = ref([])
const loading = ref(true)
const saving = ref(false)
const savingOrder = ref(false)
const error = ref(null)
const editingId = ref(null)
const form = ref(null)
const deleteTarget = ref(null)

const previewPresets = computed(() => {
  const visible = presets.value.filter(p => p.active && p.id !== editingId.value)
  if (!form.value) return visible
  const draft = { ...form.value, slug: form.value.slug || 'draft' }
  if (!draft.active) return visible
  if (editingId.value) {
    const at = presets.value.findIndex(p => p.id === editingId.value)
    const merged = [...visible]
    merged.splice(Math.max(at, 0), 0, draft)
    return merged
  }
  return [...visible, draft]
})

const load = async () => {
  loading.value = true
  try {
    const res = await adminLandingAPI.getPresets()
    presets.value = res.data?.data || []
    error.value = null
  } catch (err) {
    error.value = err.response?.data?.message || t.value.presetEditor.loadError
  } finally {
    loading.value = false
  }
}

const blankForm = () => ({
  slug: '',
  labelEn: '',
  labelId: '',
  eyebrowEn: '',
  eyebrowId: '',
  iconName: 'Home',
  projectScope: 'NEW_BUILD',
  buildingFunction: 'RESIDENTIAL',
  subCategory: null,
  defaultTitleEn: '',
  defaultTitleId: '',
  defaultLotSize: '',
  defaultDesignBudget: '',
  defaultDescriptionEn: '',
  defaultDescriptionId: '',
  active: true
})

const startCreate = () => {
  editingId.value = null
  form.value = blankForm()
}

const startEdit = preset => {
  editingId.value = preset.id
  form.value = {
    slug: preset.slug,
    labelEn: preset.labelEn,
    labelId: preset.labelId,
    eyebrowEn: preset.eyebrowEn || '',
    eyebrowId: preset.eyebrowId || '',
    iconName: preset.iconName,
    projectScope: preset.projectScope || 'NEW_BUILD',
    buildingFunction: preset.buildingFunction,
    subCategory: preset.subCategory || null,
    defaultTitleEn: preset.defaultTitleEn || '',
    defaultTitleId: preset.defaultTitleId || '',
    defaultLotSize: preset.defaultLotSize ?? '',
    defaultDesignBudget: preset.defaultDesignBudget ?? '',
    defaultDescriptionEn: preset.defaultDescriptionEn || '',
    defaultDescriptionId: preset.defaultDescriptionId || '',
    active: preset.active
  }
}

const cancelEdit = () => {
  editingId.value = null
  form.value = null
  error.value = null
}

const toPayload = () => ({
  ...form.value,
  defaultLotSize: form.value.defaultLotSize === '' ? null : Number(form.value.defaultLotSize),
  defaultDesignBudget: form.value.defaultDesignBudget === '' ? null : Number(form.value.defaultDesignBudget)
})

const save = async () => {
  saving.value = true
  error.value = null
  try {
    if (editingId.value) {
      await adminLandingAPI.updatePreset(editingId.value, toPayload())
    } else {
      await adminLandingAPI.createPreset(toPayload())
    }
    cancelEdit()
    await load()
  } catch (err) {
    error.value = err.response?.data?.message || t.value.presetEditor.saveError
  } finally {
    saving.value = false
  }
}

const confirmDelete = async () => {
  const target = deleteTarget.value
  deleteTarget.value = null
  try {
    await adminLandingAPI.deletePreset(target.id)
    if (editingId.value === target.id) cancelEdit()
    await load()
  } catch (err) {
    error.value = err.response?.data?.message || t.value.presetEditor.deleteError
  }
}

const move = async (index, delta) => {
  const next = [...presets.value]
  const target = index + delta
  if (target < 0 || target >= next.length) return
  ;[next[index], next[target]] = [next[target], next[index]]
  presets.value = next

  savingOrder.value = true
  try {
    await adminLandingAPI.reorderPresets(next.map(p => p.id))
  } catch (err) {
    error.value = err.response?.data?.message || t.value.presetEditor.reorderError
    await load()
  } finally {
    savingOrder.value = false
  }
}

onMounted(load)
</script>
