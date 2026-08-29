/**
 * Indonesian provinces and their major cities/regencies, for the architect profile
 * address form's cascading province → city selects.
 *
 * Place names don't translate between locales, so unlike projectTaxonomy.js this uses
 * a plain { value, label } shape rather than labelEn/labelId pairs.
 */

export const PROVINCES = [
  { value: 'ACEH', label: 'Aceh' },
  { value: 'SUMATERA_UTARA', label: 'Sumatera Utara' },
  { value: 'SUMATERA_BARAT', label: 'Sumatera Barat' },
  { value: 'RIAU', label: 'Riau' },
  { value: 'KEPULAUAN_RIAU', label: 'Kepulauan Riau' },
  { value: 'JAMBI', label: 'Jambi' },
  { value: 'SUMATERA_SELATAN', label: 'Sumatera Selatan' },
  { value: 'BANGKA_BELITUNG', label: 'Kepulauan Bangka Belitung' },
  { value: 'BENGKULU', label: 'Bengkulu' },
  { value: 'LAMPUNG', label: 'Lampung' },
  { value: 'DKI_JAKARTA', label: 'DKI Jakarta' },
  { value: 'JAWA_BARAT', label: 'Jawa Barat' },
  { value: 'JAWA_TENGAH', label: 'Jawa Tengah' },
  { value: 'DI_YOGYAKARTA', label: 'DI Yogyakarta' },
  { value: 'JAWA_TIMUR', label: 'Jawa Timur' },
  { value: 'BANTEN', label: 'Banten' },
  { value: 'BALI', label: 'Bali' },
  { value: 'NUSA_TENGGARA_BARAT', label: 'Nusa Tenggara Barat' },
  { value: 'NUSA_TENGGARA_TIMUR', label: 'Nusa Tenggara Timur' },
  { value: 'KALIMANTAN_BARAT', label: 'Kalimantan Barat' },
  { value: 'KALIMANTAN_TENGAH', label: 'Kalimantan Tengah' },
  { value: 'KALIMANTAN_SELATAN', label: 'Kalimantan Selatan' },
  { value: 'KALIMANTAN_TIMUR', label: 'Kalimantan Timur' },
  { value: 'KALIMANTAN_UTARA', label: 'Kalimantan Utara' },
  { value: 'SULAWESI_UTARA', label: 'Sulawesi Utara' },
  { value: 'SULAWESI_TENGAH', label: 'Sulawesi Tengah' },
  { value: 'SULAWESI_SELATAN', label: 'Sulawesi Selatan' },
  { value: 'SULAWESI_TENGGARA', label: 'Sulawesi Tenggara' },
  { value: 'GORONTALO', label: 'Gorontalo' },
  { value: 'SULAWESI_BARAT', label: 'Sulawesi Barat' },
  { value: 'MALUKU', label: 'Maluku' },
  { value: 'MALUKU_UTARA', label: 'Maluku Utara' },
  { value: 'PAPUA', label: 'Papua' },
  { value: 'PAPUA_BARAT', label: 'Papua Barat' }
]

export const CITIES_BY_PROVINCE = {
  ACEH: [
    'Banda Aceh',
    'Langsa',
    'Lhokseumawe',
    'Sabang',
    'Subulussalam',
    'Aceh Besar',
    'Aceh Utara'
  ],
  SUMATERA_UTARA: [
    'Medan',
    'Binjai',
    'Pematangsiantar',
    'Tebing Tinggi',
    'Sibolga',
    'Padang Sidempuan',
    'Deli Serdang'
  ],
  SUMATERA_BARAT: ['Padang', 'Bukittinggi', 'Padang Panjang', 'Payakumbuh', 'Sawahlunto', 'Solok'],
  RIAU: ['Pekanbaru', 'Dumai', 'Kampar', 'Bengkalis', 'Indragiri Hulu'],
  KEPULAUAN_RIAU: ['Batam', 'Tanjungpinang', 'Bintan', 'Karimun'],
  JAMBI: ['Jambi', 'Sungai Penuh', 'Muaro Jambi', 'Batanghari'],
  SUMATERA_SELATAN: ['Palembang', 'Lubuklinggau', 'Pagar Alam', 'Prabumulih', 'Ogan Komering Ilir'],
  BANGKA_BELITUNG: ['Pangkalpinang', 'Bangka', 'Belitung', 'Bangka Barat'],
  BENGKULU: ['Bengkulu', 'Rejang Lebong', 'Bengkulu Utara'],
  LAMPUNG: ['Bandar Lampung', 'Metro', 'Lampung Selatan', 'Lampung Tengah'],
  DKI_JAKARTA: [
    'Jakarta Pusat',
    'Jakarta Utara',
    'Jakarta Barat',
    'Jakarta Selatan',
    'Jakarta Timur',
    'Kepulauan Seribu'
  ],
  JAWA_BARAT: [
    'Bandung',
    'Bekasi',
    'Bogor',
    'Depok',
    'Cimahi',
    'Sukabumi',
    'Tasikmalaya',
    'Cirebon',
    'Banjar',
    'Karawang'
  ],
  JAWA_TENGAH: [
    'Semarang',
    'Surakarta',
    'Salatiga',
    'Magelang',
    'Pekalongan',
    'Tegal',
    'Kudus',
    'Klaten',
    'Purwokerto'
  ],
  DI_YOGYAKARTA: ['Yogyakarta', 'Sleman', 'Bantul', 'Kulon Progo', 'Gunungkidul'],
  JAWA_TIMUR: [
    'Surabaya',
    'Malang',
    'Kediri',
    'Blitar',
    'Madiun',
    'Mojokerto',
    'Pasuruan',
    'Probolinggo',
    'Batu',
    'Sidoarjo'
  ],
  BANTEN: ['Serang', 'Tangerang', 'Tangerang Selatan', 'Cilegon', 'Pandeglang'],
  BALI: ['Denpasar', 'Badung', 'Gianyar', 'Tabanan', 'Buleleng', 'Karangasem'],
  NUSA_TENGGARA_BARAT: ['Mataram', 'Bima', 'Lombok Barat', 'Lombok Timur', 'Sumbawa'],
  NUSA_TENGGARA_TIMUR: ['Kupang', 'Ende', 'Sikka', 'Manggarai'],
  KALIMANTAN_BARAT: ['Pontianak', 'Singkawang', 'Sambas', 'Kubu Raya'],
  KALIMANTAN_TENGAH: ['Palangka Raya', 'Kotawaringin Barat', 'Kotawaringin Timur'],
  KALIMANTAN_SELATAN: ['Banjarmasin', 'Banjarbaru', 'Banjar', 'Tanah Laut'],
  KALIMANTAN_TIMUR: ['Samarinda', 'Balikpapan', 'Bontang', 'Kutai Kartanegara'],
  KALIMANTAN_UTARA: ['Tarakan', 'Bulungan', 'Nunukan'],
  SULAWESI_UTARA: ['Manado', 'Bitung', 'Tomohon', 'Kotamobagu', 'Minahasa'],
  SULAWESI_TENGAH: ['Palu', 'Poso', 'Donggala', 'Banggai'],
  SULAWESI_SELATAN: ['Makassar', 'Parepare', 'Palopo', 'Gowa', 'Bone'],
  SULAWESI_TENGGARA: ['Kendari', 'Baubau', 'Konawe'],
  GORONTALO: ['Gorontalo', 'Boalemo', 'Bone Bolango'],
  SULAWESI_BARAT: ['Mamuju', 'Majene', 'Polewali Mandar'],
  MALUKU: ['Ambon', 'Tual', 'Maluku Tengah'],
  MALUKU_UTARA: ['Ternate', 'Tidore Kepulauan', 'Halmahera Barat'],
  PAPUA: ['Jayapura', 'Merauke', 'Biak Numfor', 'Nabire'],
  PAPUA_BARAT: ['Manokwari', 'Sorong', 'Fakfak']
}

export const citiesFor = province =>
  (CITIES_BY_PROVINCE[province] || []).map(city => ({ value: city, label: city }))

// Province is stored as a code (DKI_JAKARTA) but city as a display string, so anything
// user-facing or geocoded needs the label resolved back out.
export const provinceLabel = province => PROVINCES.find(p => p.value === province)?.label || ''

// Projects store the province as Google's Places API returns it ("Jawa Barat"), while the
// architect profile stores a code ("JAWA_BARAT"). Render either without the caller caring which.
export const displayProvince = value => provinceLabel(value) || value || ''
