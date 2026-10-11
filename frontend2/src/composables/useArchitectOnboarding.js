import { reactive, computed, watch } from 'vue'
import { IAI_TYPES } from '@/constants/iaiTaxonomy'
import { EXPERIENCE_OPTIONS } from '@/constants/architectProfileOptions'
import { JENIS_USAHA_OPTIONS, INTERIOR_TYPES } from '@/constants/architectOnboarding'

const STORAGE_KEY = 'rumantra:architect-onboarding:v1'

const blank = () => ({
  profesi: [],
  jenis: '',
  nama: '',
  entitas: '',
  telp: '',
  tahun: '',
  alamat: '',
  province: '',
  city: '',
  website: '',
  linkedin: '',
  instagram: '',
  keahlian: []
})

const load = () => {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
    return saved ? { ...blank(), ...saved } : blank()
  } catch {
    return blank()
  }
}

// One draft shared by every step, kept in localStorage so a refresh or a detour to the
// login page does not throw away what the architect already typed.
const state = reactive(load())
watch(state, value => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
  } catch {
    // Storage can be unavailable (private mode); the draft then just lives in memory
  }
})

const digits = v => String(v || '').replace(/\D/g, '')
const iaiValues = new Set(IAI_TYPES.map(t => t.value))
const interiorValues = new Set(INTERIOR_TYPES.map(t => t.value))

const experienceFromYear = year => {
  const years = new Date().getFullYear() - Number(year)
  if (years <= 3) return EXPERIENCE_OPTIONS[0]
  if (years <= 7) return EXPERIENCE_OPTIONS[1]
  if (years <= 15) return EXPERIENCE_OPTIONS[2]
  return EXPERIENCE_OPTIONS[3]
}

export function useArchitectOnboarding() {
  const isSolo = computed(() => state.jenis === 'SOLO')
  const showArsitek = computed(() => state.profesi.includes('ARSITEK'))
  const showInterior = computed(() => state.profesi.includes('INTERIOR'))

  // Expertise only counts while its profession is still selected
  const activeKeahlian = computed(() =>
    state.keahlian.filter(
      v => (showArsitek.value && iaiValues.has(v)) || (showInterior.value && interiorValues.has(v))
    )
  )

  const toggle = (list, value) => {
    const i = state[list].indexOf(value)
    if (i >= 0) state[list].splice(i, 1)
    else state[list].push(value)
  }

  const dataDiriErrors = () => {
    const e = {}
    const year = Number(state.tahun)
    if (!state.nama.trim()) e.nama = 'Isi nama lengkap sesuai KTP.'
    if (!isSolo.value && !state.entitas.trim())
      e.entitas = 'Isi nama ini agar tampil di profil publik Anda.'
    const tel = digits(state.telp)
    if (tel.length < 8 || tel.length > 13) e.telp = 'Masukkan nomor telepon yang valid.'
    if (!/^\d{4}$/.test(state.tahun) || year < 1950 || year > new Date().getFullYear())
      e.tahun = 'Masukkan tahun 4 digit, misalnya 2018.'
    if (!state.alamat.trim()) e.alamat = 'Isi alamat lengkap.'
    if (!state.province || !state.city) e.lokasi = 'Pilih provinsi dan kota.'
    if (!state.linkedin.trim() && !state.instagram.trim())
      e.sosmed = 'Isi minimal salah satu: LinkedIn atau Instagram.'
    return e
  }

  // Only fields the architect profile API already stores. Profession, exact year, social
  // links and interior expertise stay in the local draft until the backend has columns for them.
  const profilePayload = () => {
    const jenis = JENIS_USAHA_OPTIONS.find(o => o.id === state.jenis)
    const tel = digits(state.telp).replace(/^(62|0)/, '')
    const site = state.website.trim().replace(/^https?:\/\//, '')
    return {
      fullnameKtp: state.nama.trim(),
      contactName: state.nama.trim(),
      companyName: isSolo.value ? null : state.entitas.trim(),
      category: jenis?.category || null,
      // Stored in the local 08… form the profile page already uses
      phoneNum: tel ? `0${tel}` : null,
      fullAddress: state.alamat.trim(),
      province: state.province,
      city: state.city,
      companySite: site ? `https://${site}` : null,
      experienceRange: state.tahun ? experienceFromYear(state.tahun) : null,
      expertise: activeKeahlian.value.filter(v => iaiValues.has(v))
    }
  }

  const reset = () => Object.assign(state, blank())

  return {
    state,
    isSolo,
    showArsitek,
    showInterior,
    activeKeahlian,
    toggle,
    dataDiriErrors,
    profilePayload,
    reset
  }
}
