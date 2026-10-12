<template>
  <form novalidate @submit.prevent="next">
    <OnbHeading :title="copy.title" lead="Isi sesuai KTP. Data ini dipakai untuk kontrak dan pencairan dana." />

    <div class="flex flex-col gap-7 animate-brief-rise">
      <div class="flex items-center justify-between gap-4 px-5 py-[18px] rounded-2xl bg-surface-alt">
        <span class="text-[15px] text-ink-700">
          Jenis usaha: <strong class="text-ink-900 font-bold">{{ jenisLabel }}</strong>
        </span>
        <button
          type="button"
          class="text-[15px] font-bold text-ink-900 border-b border-hairline hover:border-ink-900"
          @click="$emit('edit-jenis')"
        >
          Ubah
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label for="onb-nama" :class="labelCls">Nama lengkap</label>
          <input
            id="onb-nama"
            v-model="state.nama"
            :class="[inputCls, errCls('nama')]"
            type="text"
            placeholder="Sesuai KTP"
            autocomplete="name"
          />
          <p v-if="shown.nama" :class="errText">{{ shown.nama }}</p>
        </div>
        <div v-if="!isSolo">
          <label for="onb-entitas" :class="labelCls">{{ copy.entitasLabel }}</label>
          <input
            id="onb-entitas"
            v-model="state.entitas"
            :class="[inputCls, errCls('entitas')]"
            type="text"
            :placeholder="copy.entitasPh"
            autocomplete="organization"
          />
          <p v-if="shown.entitas" :class="errText">{{ shown.entitas }}</p>
          <p v-else :class="hintText">Muncul di profil publik Anda.</p>
        </div>
        <div>
          <label for="onb-telp" :class="labelCls">Nomor telepon</label>
          <div :class="[prefixWrap, errCls('telp')]">
            <span :class="prefix">+62</span>
            <input
              id="onb-telp"
              v-model="state.telp"
              :class="prefixInput"
              type="tel"
              placeholder="812 3456 7890"
              autocomplete="tel-national"
            />
          </div>
          <p v-if="shown.telp" :class="errText">{{ shown.telp }}</p>
          <p v-else :class="hintText">Kami kirim kode verifikasi lewat WhatsApp atau SMS.</p>
        </div>
        <div>
          <label for="onb-tahun" :class="labelCls">{{ copy.tahunLabel }}</label>
          <input
            id="onb-tahun"
            v-model="state.tahun"
            :class="[inputCls, errCls('tahun')]"
            type="text"
            inputmode="numeric"
            maxlength="4"
            placeholder="cth. 2018"
            @input="state.tahun = state.tahun.replace(/\D/g, '')"
          />
          <p v-if="shown.tahun" :class="errText">{{ shown.tahun }}</p>
          <p v-else :class="hintText">{{ copy.tahunHint }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label for="onb-provinsi" :class="labelCls">Provinsi</label>
          <div class="relative">
            <select
              id="onb-provinsi"
              :class="[selectCls, errCls('lokasi'), state.province ? 'text-ink-900' : 'text-ink-400']"
              :value="state.province"
              @change="onProvince($event.target.value)"
            >
              <option value="">Pilih provinsi</option>
              <option v-for="p in PROVINCES" :key="p.value" :value="p.value">{{ p.label }}</option>
            </select>
            <ChevronDown
              :size="18"
              :stroke-width="2"
              class="absolute right-4 top-[15px] pointer-events-none text-ink-400"
            />
          </div>
        </div>
        <div>
          <label for="onb-kota" :class="labelCls">Kota / Kabupaten</label>
          <div class="relative">
            <select
              id="onb-kota"
              v-model="state.city"
              :class="[
                selectCls,
                errCls('lokasi'),
                state.city ? 'text-ink-900' : 'text-ink-400',
                'disabled:bg-surface-alt disabled:cursor-not-allowed'
              ]"
              :disabled="!state.province"
            >
              <option value="">{{ state.province ? 'Pilih kota' : 'Pilih provinsi dulu' }}</option>
              <option v-for="c in cities" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
            <ChevronDown
              :size="18"
              :stroke-width="2"
              class="absolute right-4 top-[15px] pointer-events-none text-ink-400"
            />
          </div>
        </div>
      </div>
      <p v-if="shown.lokasi" :class="[errText, '-mt-5']">{{ shown.lokasi }}</p>

      <div>
        <label for="onb-alamat" :class="labelCls">Alamat lengkap</label>
        <textarea
          id="onb-alamat"
          v-model="state.alamat"
          rows="3"
          :class="[
            'w-full box-border px-4 py-3.5 border border-hairline rounded-xl bg-white text-body leading-relaxed text-ink-900 placeholder-ink-300 outline-none resize-y focus:border-ink-900 transition-colors',
            errCls('alamat')
          ]"
          placeholder="cth. Jl. Kemang Raya No. 12, RT 03/RW 05, Bangka, Mampang Prapatan, 12730"
          autocomplete="street-address"
        ></textarea>
        <p v-if="shown.alamat" :class="errText">{{ shown.alamat }}</p>
      </div>

      <div class="flex gap-3.5 items-start px-5 py-[18px] rounded-2xl bg-surface-alt">
        <Lock :size="20" :stroke-width="2" class="flex-none mt-px text-ink-900" />
        <p class="text-[15px] leading-relaxed text-ink-700 m-0">
          <strong class="text-ink-900">Data Anda aman.</strong>
          Alamat dan nomor telepon hanya dipakai untuk kontrak dan pencairan dana, klien tidak bisa melihatnya. Yang
          tampil di profil publik: {{ copy.publik }}.
        </p>
      </div>

      <div class="pt-7 border-t border-hairline flex flex-col gap-7">
        <div>
          <div class="text-[20px] font-bold tracking-[-.02em] text-ink-900 mb-1">Website dan media sosial</div>
          <p class="text-caption leading-normal text-ink-400 m-0">
            Membantu klien mengenal karya Anda sebelum memilih penawaran.
          </p>
        </div>
        <div>
          <label for="onb-web" :class="labelCls"
            >Website <span class="font-normal text-ink-400">(opsional)</span></label
          >
          <div :class="prefixWrap">
            <span :class="prefix">https://</span>
            <input
              id="onb-web"
              v-model="state.website"
              :class="prefixInput"
              type="text"
              inputmode="url"
              placeholder="ruangteduh.id"
              autocomplete="url"
            />
          </div>
        </div>
        <div>
          <p class="text-caption leading-normal m-0 mb-3" :class="shown.sosmed ? 'text-[#B42318]' : 'text-ink-400'">
            Isi minimal salah satu: LinkedIn atau Instagram.
          </p>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label for="onb-linkedin" :class="labelCls">LinkedIn</label>
              <div :class="[prefixWrap, errCls('sosmed')]">
                <span :class="prefix">linkedin.com/</span>
                <input
                  id="onb-linkedin"
                  v-model="state.linkedin"
                  :class="prefixInput"
                  type="text"
                  placeholder="in/nama-anda"
                />
              </div>
            </div>
            <div>
              <label for="onb-instagram" :class="labelCls">Instagram</label>
              <div :class="[prefixWrap, errCls('sosmed')]">
                <span :class="prefix">@</span>
                <input
                  id="onb-instagram"
                  v-model="state.instagram"
                  :class="prefixInput"
                  type="text"
                  placeholder="namaakun"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <OnbFooter @back="$emit('back')" @next="next" />
  </form>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ChevronDown, Lock } from 'lucide-vue-next'
import OnbHeading from './OnbHeading.vue'
import OnbFooter from './OnbFooter.vue'
import { DATA_DIRI_COPY, JENIS_USAHA_OPTIONS } from '@/constants/architectOnboarding'
import { PROVINCES, citiesFor } from '@/constants/regions'
import { useArchitectOnboarding } from '@/composables/useArchitectOnboarding'

const emit = defineEmits(['back', 'next', 'edit-jenis'])

const { state, isSolo, dataDiriErrors } = useArchitectOnboarding()

const copy = computed(() => DATA_DIRI_COPY[state.jenis] || DATA_DIRI_COPY.SOLO)
const jenisLabel = computed(() => JENIS_USAHA_OPTIONS.find(o => o.id === state.jenis)?.title || '-')
const cities = computed(() => citiesFor(state.province))

// Errors appear only after the first "Lanjutkan", then update live as fields are fixed
const attempted = ref(false)
const shown = computed(() => (attempted.value ? dataDiriErrors() : {}))
const errCls = key => (shown.value[key] ? '!border-[#B42318]' : '')

const FIELD_FOR_ERROR = {
  nama: 'onb-nama',
  entitas: 'onb-entitas',
  telp: 'onb-telp',
  tahun: 'onb-tahun',
  lokasi: 'onb-provinsi',
  alamat: 'onb-alamat',
  sosmed: 'onb-linkedin'
}

const onProvince = value => {
  state.province = value
  if (!citiesFor(value).some(c => c.value === state.city)) state.city = ''
}

const next = () => {
  attempted.value = true
  const keys = Object.keys(dataDiriErrors())
  if (keys.length) {
    document.getElementById(FIELD_FOR_ERROR[keys[0]])?.focus()
    return
  }
  emit('next')
}

const labelCls = 'block text-body font-medium text-ink-700 mb-2.5'
const hintText = 'text-caption leading-normal text-ink-400 mt-2 mb-0'
const errText = 'text-caption leading-normal text-[#B42318] mt-2 mb-0'
const inputCls =
  'w-full box-border h-12 px-4 border border-hairline rounded-xl bg-white text-body text-ink-900 placeholder-ink-300 outline-none focus:border-ink-900 transition-colors'
const selectCls =
  'w-full box-border h-12 pl-4 pr-11 border border-hairline rounded-xl bg-white text-body font-semibold outline-none appearance-none cursor-pointer focus:border-ink-900 transition-colors bg-none'
const prefixWrap =
  'flex items-center h-12 box-border border border-hairline rounded-xl overflow-hidden bg-white focus-within:border-ink-900 transition-colors'
const prefix =
  'h-full px-3.5 flex items-center bg-surface-alt border-r border-hairline text-[15px] text-ink-400 whitespace-nowrap'
const prefixInput =
  'h-full w-full min-w-0 border-0 px-4 text-body text-ink-900 bg-white placeholder-ink-300 outline-none'
</script>
