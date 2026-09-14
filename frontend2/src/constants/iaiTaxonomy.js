/**
 * The official IAI (Ikatan Arsitek Indonesia) building classification, used by the
 * architect profile (expertise, multi-select) and portfolio (project type, single-select).
 *
 * The selectable unit is the (Kategori IAI, Tipe Bangunan) pair, not the Tipe alone:
 * "Hunian" appears under Kategori Sosial, 1, 2 and 3 and means something different each
 * time. `examples` is the third column of the IAI table — reference copy shown behind an
 * info tooltip, never a selectable value.
 *
 * Labels live beside their values rather than in locales/{en,id}.js for the same reason
 * projectTaxonomy.js gives: the two locale files must stay key-for-key identical, and 22
 * entries x 4 strings would be ~88 keys to keep in sync by hand.
 *
 * Keep in sync with backend/src/main/java/com/rumantra/shared/constants/IaiTaxonomy.java,
 * which enforces the same value set server-side. IaiTaxonomyTest pins the shape so drift
 * fails the build.
 */

export const IAI_CATEGORIES = [
  { value: 'SOSIAL', labelEn: 'Social Category', labelId: 'Kategori Sosial' },
  { value: 'K1', labelEn: 'Category 1', labelId: 'Kategori 1' },
  { value: 'K2', labelEn: 'Category 2', labelId: 'Kategori 2' },
  { value: 'K3', labelEn: 'Category 3', labelId: 'Kategori 3' },
  { value: 'KHUSUS', labelEn: 'Special Category', labelId: 'Kategori Khusus' }
]

export const IAI_TYPES = [
  {
    value: 'SOSIAL_PELAYANAN_MASYARAKAT',
    category: 'SOSIAL',
    labelEn: 'Social / Public Service',
    labelId: 'Sosial / Pelayanan Masyarakat',
    examplesEn: 'Public service buildings with a floor area of 250 m² or less',
    examplesId: 'Bangunan pelayanan masyarakat dengan luas ≤ 250 m²'
  },
  {
    value: 'SOSIAL_PERIBADATAN',
    category: 'SOSIAL',
    labelEn: 'Place of Worship',
    labelId: 'Peribadatan',
    examplesEn: 'Mosques, churches and other places of worship with a floor area of 250 m² or less',
    examplesId: 'Masjid, gereja, dan tempat ibadah lainnya dengan luas ≤ 250 m²'
  },
  {
    value: 'SOSIAL_SOSIAL',
    category: 'SOSIAL',
    labelEn: 'Social',
    labelId: 'Sosial',
    examplesEn: 'Orphanages and shelters',
    examplesId: 'Rumah penampungan yatim piatu'
  },
  {
    value: 'SOSIAL_HUNIAN',
    category: 'SOSIAL',
    labelEn: 'Residential',
    labelId: 'Hunian',
    examplesEn: 'Simple housing with a floor area of 36 m² or less',
    examplesId: 'Rumah tinggal sederhana dengan luas ≤ 36 m²'
  },
  {
    value: 'K1_HUNIAN',
    category: 'K1',
    labelEn: 'Residential',
    labelId: 'Hunian',
    examplesEn: 'Dormitories, hostels',
    examplesId: 'Asrama, hostel'
  },
  {
    value: 'K1_INDUSTRI',
    category: 'K1',
    labelEn: 'Industrial',
    labelId: 'Industri',
    examplesEn: 'Workshops, warehouses',
    examplesId: 'Bengkel, gudang'
  },
  {
    value: 'K1_KOMERSIAL',
    category: 'K1',
    labelEn: 'Commercial',
    labelId: 'Komersial',
    examplesEn: 'Single-storey commercial buildings, parking areas',
    examplesId: 'Bangunan komersial tidak bertingkat, tempat parkir'
  },
  {
    value: 'K2_HUNIAN',
    category: 'K2',
    labelEn: 'Residential',
    labelId: 'Hunian',
    examplesEn: 'Apartments, condominiums, housing complexes',
    examplesId: 'Apartemen, kondominium, kompleks perumahan'
  },
  {
    value: 'K2_INDUSTRI',
    category: 'K2',
    labelEn: 'Industrial',
    labelId: 'Industri',
    examplesEn: 'Power substations, cold storage, factories',
    examplesId: 'Gardu pembangkit listrik, gudang pendingin, pabrik'
  },
  {
    value: 'K2_KOMERSIAL',
    category: 'K2',
    labelEn: 'Commercial',
    labelId: 'Komersial',
    examplesEn:
      'Multi-storey car parks, cafeterias, restaurants, offices, office-houses, shophouses, shops, shopping centres, markets, hangars, stations, terminals, superblocks / mixed-use',
    examplesId:
      'Bangunan parkir bertingkat, kafetaria, restoran, kantor/perkantoran, rukan, ruko, toko, pusat perbelanjaan, pasar, hanggar, stasiun, terminal, superblok/fungsi campuran'
  },
  {
    value: 'K2_KOMUNITAS',
    category: 'K2',
    labelEn: 'Community',
    labelId: 'Komunitas',
    examplesEn:
      'Auditoriums, cinemas, exhibition halls, conference rooms, multipurpose halls, meeting rooms, libraries, prisons, public service offices',
    examplesId:
      'Auditorium, bioskop, ruang pameran, ruang konferensi, ruang serbaguna, ruang pertemuan, perpustakaan, penjara, kantor pelayanan umum'
  },
  {
    value: 'K2_MEDIS',
    category: 'K2',
    labelEn: 'Medical Services',
    labelId: 'Pelayanan Medis',
    examplesEn: 'Specialist clinics, general clinics, nursing homes',
    examplesId: 'Klinik spesialis, klinik umum, rumah jompo'
  },
  {
    value: 'K2_PENDIDIKAN',
    category: 'K2',
    labelEn: 'Education',
    labelId: 'Pendidikan',
    examplesEn: 'Schools, care facilities',
    examplesId: 'Sekolah, tempat perawatan'
  },
  {
    value: 'K2_REKREASI',
    category: 'K2',
    labelEn: 'Recreation',
    labelId: 'Rekreasi',
    examplesEn: 'Sports halls, gymnasiums, swimming pools, stadiums, public parks',
    examplesId: 'Gedung olahraga, gimnasium, kolam renang, stadion, taman umum'
  },
  {
    value: 'K3_HUNIAN',
    category: 'K3',
    labelEn: 'Residential',
    labelId: 'Hunian',
    examplesEn: 'Private houses',
    examplesId: 'Rumah tinggal privat'
  },
  {
    value: 'K3_KOMERSIAL',
    category: 'K3',
    labelEn: 'Commercial',
    labelId: 'Komersial',
    examplesEn: 'Airports, hotels',
    examplesId: 'Bandara, hotel'
  },
  {
    value: 'K3_KOMUNITAS',
    category: 'K3',
    labelEn: 'Community',
    labelId: 'Komunitas',
    examplesEn: 'Galleries, concert halls, museums, monuments, palaces',
    examplesId: 'Galeri, ruang konser, museum, monumen, istana'
  },
  {
    value: 'K3_MEDIS',
    category: 'K3',
    labelEn: 'Medical Services',
    labelId: 'Pelayanan Medis',
    examplesEn: 'Hospitals, sanatoriums',
    examplesId: 'Rumah sakit, sanatorium'
  },
  {
    value: 'K3_PENDIDIKAN',
    category: 'K3',
    labelEn: 'Education / Research',
    labelId: 'Pendidikan / Penelitian',
    examplesEn: 'Laboratories, campuses, research centres',
    examplesId: 'Laboratorium, kampus, pusat penelitian/riset'
  },
  {
    value: 'K3_PERIBADATAN',
    category: 'K3',
    labelEn: 'Place of Worship',
    labelId: 'Peribadatan',
    examplesEn:
      'Churches, temples, mosques and other places of worship with a floor area over 250 m²',
    examplesId: 'Gereja, klenteng, masjid, dan tempat ibadah lainnya dengan luas > 250 m²'
  },
  {
    value: 'K3_LAINNYA',
    category: 'K3',
    labelEn: 'Other',
    labelId: 'Lainnya',
    examplesEn:
      'Embassies, offices of high state institutions, restoration, renovation, buildings with special decoration',
    examplesId:
      'Kantor kedutaan, kantor lembaga tinggi negara, pemugaran, renovasi, bangunan dengan dekorasi khusus'
  },
  {
    value: 'KHUSUS_PEMERINTAH',
    category: 'KHUSUS',
    labelEn: 'Government Building',
    labelId: 'Bangunan Pemerintah',
    examplesEn:
      'Buildings and built environments owned, used and funded by the Government under the Technical Guidelines for the Construction of State Buildings',
    examplesId:
      'Bangunan dan lingkungan binaan yang dimiliki, digunakan, dan dibiayai Pemerintah sesuai Pedoman Teknis Pembangunan Bangunan Gedung Negara'
  }
]

const labelKey = locale => (locale === 'id' ? 'labelId' : 'labelEn')
const examplesKey = locale => (locale === 'id' ? 'examplesId' : 'examplesEn')

const TYPES_BY_VALUE = IAI_TYPES.reduce((map, type) => {
  map[type.value] = type
  return map
}, {})

export const typesForCategory = category => IAI_TYPES.filter(type => type.category === category)

export const isValidIaiType = value => Boolean(value) && Object.hasOwn(TYPES_BY_VALUE, value)

export const isValidIaiCategory = value => IAI_CATEGORIES.some(c => c.value === value)

export const iaiCategoryLabel = (value, locale) => {
  const match = IAI_CATEGORIES.find(c => c.value === value)
  return match ? match[labelKey(locale)] : value || ''
}

export const iaiTypeLabel = (value, locale) => {
  const match = TYPES_BY_VALUE[value]
  return match ? match[labelKey(locale)] : value || ''
}

export const iaiTypeExamples = (value, locale) => {
  const match = TYPES_BY_VALUE[value]
  return match ? match[examplesKey(locale)] : ''
}

/**
 * The one-line form used wherever a single value is shown out of context — a portfolio
 * card, a profile chip. The Kategori alone is meaningless and the Tipe alone is ambiguous,
 * so both are always shown together.
 */
export const iaiTypeFullLabel = (value, locale) => {
  const match = TYPES_BY_VALUE[value]
  if (!match) return value || ''
  return `${iaiCategoryLabel(match.category, locale)} · ${match[labelKey(locale)]}`
}
