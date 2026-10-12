/**
 * Content for the survey-style project brief (/brief-proyek), ported from the Claude Design
 * file "Project Onboarding - Survey Style". The copy is Indonesian only for now, like the
 * design; it lives here rather than in locales/ because most of it is long-form definitions
 * that have no English source yet.
 *
 * Values sent to the backend reuse the existing vocabularies: scopes and categories map onto
 * projectTaxonomy.js, architectural documents onto projectDeliverables.js codes.
 */
import { subCategoriesFor } from './projectTaxonomy'

export const SURVEY_FLOW = [
  'lineup',
  'scope',
  'property',
  'akun',
  'alamat',
  'luas',
  'kebutuhan',
  'deliv',
  'fee',
  'mulai',
  'review',
  'done'
]

export const SURVEY_PAGES = {
  lineup: { title: 'Layanan apa yang Anda butuhkan?', sub: 'Pilih satu atau beberapa layanan.' },
  scope: { title: 'Apa tujuan dari proyek Anda?', sub: 'Boleh pilih lebih dari satu.' },
  property: {
    title: 'Apa jenis properti Anda?',
    sub: 'Pilih kategori, lalu sub-kategori yang paling mendekati.'
  },
  akun: {
    title: 'Simpan brief awal Anda',
    sub: 'Masuk atau buat akun agar brief tersimpan dan bisa dilanjutkan kapan saja.'
  },
  alamat: {
    title: 'Di mana lokasi proyek Anda?',
    sub: 'Pilih provinsi dan kota. Alamat lengkap dan titik lokasi boleh dilengkapi nanti.'
  },
  luas: {
    title: 'Berapa luas tanah dan area proyek?',
    sub: 'Isi sesuai data yang Anda miliki saat ini. Angkanya bisa disesuaikan lagi bersama arsitek.'
  },
  kebutuhan: {
    title: 'Ceritakan kebutuhan Anda',
    sub: 'Jumlah ruangan, gaya yang disukai, kebiasaan keluarga, atau hal yang perlu dihindari.'
  },
  deliv: {
    title: 'Dokumen apa yang Anda butuhkan?',
    sub: 'Pilih paket yang paling mendekati kebutuhan Anda. Detailnya bisa dibahas bersama arsitek yang Anda pilih.'
  },
  fee: {
    title: 'Berapa budget fee arsitek Anda?',
    sub: 'Biaya jasa desain, di luar biaya pembangunan.'
  },
  mulai: { title: 'Kapan Anda ingin mulai desain?', sub: '' },
  review: {
    title: 'Periksa brief Anda',
    sub: 'Pastikan semua sudah sesuai sebelum dikirim untuk verifikasi.'
  },
  done: { title: '', sub: '' }
}

// Interior-only briefs reword the steps that would otherwise talk about land and buildings
export const SURVEY_INTERIOR_PAGES = {
  luas: {
    title: 'Berapa ukuran area yang ingin didesain?',
    sub: 'Isi jika Anda tahu. Perkiraan sudah cukup, ukuran pasti akan dicek bersama desainer.'
  },
  kebutuhan: {
    title: 'Ceritakan kebutuhan Anda',
    sub: 'Gaya yang disukai, cara pemakaian sehari-hari, dan hal yang perlu dihindari.'
  },
  deliv: {
    title: 'Dokumen apa yang Anda butuhkan?',
    sub: 'Pilih dokumen desain interior yang Anda perlukan.'
  },
  fee: {
    title: 'Berapa budget fee desain Anda?',
    sub: 'Biaya jasa desain interior, di luar biaya pembuatan dan pemasangan.'
  }
}

export const SURVEY_NEED_HINTS = {
  lineup: 'Pilih minimal satu layanan',
  scope: 'Pilih minimal satu lingkup',
  scopeInterior: 'Lengkapi pilihan area interior',
  property: 'Pilih kategori dan sub-kategori',
  alamat: 'Pilih provinsi dan isi kota',
  luas: 'Lengkapi semua isian',
  kebutuhan: 'Ceritakan kebutuhan Anda',
  deliv: 'Pilih minimal satu dokumen',
  fee: 'Isi budget fee arsitek',
  mulai: 'Pilih perkiraan mulai',
  review: 'Isi nomor telepon Anda'
}

export const SURVEY_SERVICES = [
  {
    id: 'ARCHITECTURE',
    label: 'Desain arsitektur',
    desc: 'Konsep bangunan, tata ruang, gambar kerja, dan visualisasi desain.'
  },
  {
    id: 'CONSTRUCTION',
    label: 'Pembangunan',
    soon: true,
    desc: 'Persiapan konstruksi, pelaksanaan pekerjaan, dan penyelesaian bangunan.'
  },
  {
    id: 'TRADES',
    label: 'Tukang dan Teknisi',
    soon: true,
    desc: 'Untuk perbaikan atau pekerjaan berskala kecil.'
  }
]

export const SCOPE_INTERIOR = 'INTERIOR_FIT_OUT'

export const SURVEY_SCOPES = [
  {
    value: 'NEW_BUILD',
    label: 'Bangun baru',
    desc: 'Bangunan baru di lahan kosong atau sebagai pengganti bangunan lama.'
  },
  {
    value: 'RENOVATION',
    label: 'Renovasi atau perluasan',
    desc: 'Perubahan tata ruang, pembaruan, atau penambahan luas bangunan yang sudah ada.'
  },
  {
    value: SCOPE_INTERIOR,
    label: 'Desain interior',
    desc: 'Penataan ruang dalam, termasuk tata letak, material, furnitur, dan pencahayaan.'
  },
  {
    value: 'RESTORATION',
    label: 'Restorasi atau konservasi',
    desc: 'Pemulihan atau pelestarian bangunan lama dengan tetap menjaga karakter aslinya.'
  }
]

export const INTERIOR_WHOLE = 'Seluruh interior'
export const INTERIOR_OTHER = 'Lainnya'

// `piece` is the answer that narrows the job to a single furniture piece, which is when the
// size step asks for a wall length instead of a floor area
export const INTERIOR_AREAS = [
  { id: INTERIOR_WHOLE },
  {
    id: 'Dapur',
    area: 'dapur',
    opts: ['Kitchen set saja', 'Seluruh area dapur', 'Lainnya'],
    piece: 'Kitchen set saja'
  },
  {
    id: 'Kamar tidur',
    area: 'kamar tidur',
    opts: ['Lemari pakaian saja', 'Seluruh kamar tidur', 'Lainnya'],
    piece: 'Lemari pakaian saja'
  },
  {
    id: 'Ruang keluarga',
    area: 'ruang keluarga',
    opts: ['Backdrop TV dan kabinet saja', 'Seluruh ruang keluarga', 'Lainnya'],
    piece: 'Backdrop TV dan kabinet saja'
  },
  {
    id: 'Kamar mandi',
    area: 'kamar mandi',
    opts: ['Kabinet wastafel saja', 'Seluruh kamar mandi', 'Lainnya'],
    piece: 'Kabinet wastafel saja'
  },
  { id: INTERIOR_OTHER }
]

// Areas whose design usually moves water points, so the interior document list offers them
export const INTERIOR_WET_AREAS = [INTERIOR_WHOLE, 'Dapur', 'Kamar mandi', INTERIOR_OTHER]

const toOptions = category =>
  subCategoriesFor(category).map(sub => ({ category, subCategory: sub.value, label: sub.labelId }))

// The design shows four categories; "Lainnya" gathers the remaining official categories so the
// value sent is still a valid (category, sub-category) pair from projectTaxonomy.js
export const SURVEY_CATEGORY_GROUPS = [
  { id: 'hunian', label: 'Hunian', categories: ['RESIDENTIAL'] },
  { id: 'komersil', label: 'Komersil', categories: ['COMMERCIAL'] },
  { id: 'industrial', label: 'Industrial', categories: ['INDUSTRIAL'] },
  {
    id: 'lainnya',
    label: 'Lainnya',
    categories: [
      'INSTITUTIONAL',
      'MIXED_USE',
      'LANDSCAPE',
      'RECREATIONAL',
      'AGRICULTURAL',
      'INFRASTRUCTURE'
    ]
  }
].map(group => ({ ...group, options: group.categories.flatMap(toOptions) }))

export const groupForCategory = category =>
  SURVEY_CATEGORY_GROUPS.find(group => group.categories.includes(category)) || null

export const APARTMENT_SUB_CATEGORY = 'APARTMENT_UNIT'

export const APARTMENT_TYPES = [
  'Studio',
  '1BR',
  '1BR + Study',
  '2BR',
  '2BR + Study',
  '3BR',
  '3BR + Study',
  '4BR',
  '5BR atau lebih',
  'Loft',
  'Duplex / Maisonette',
  'Penthouse',
  'SOHO'
]

export const START_OPTIONS = [
  { value: 'IMMEDIATELY', label: 'Secepatnya' },
  { value: 'SPECIFIC_DATE', label: 'Pada tanggal tertentu' }
]

/**
 * Architectural documents. `code` is the projectDeliverables.js value stored on the project.
 */
export const ARCHITECTURE_DOCUMENTS = [
  {
    title: 'Perencanaan Awal',
    items: [
      {
        code: 'SITE_ANALYSIS',
        label: 'Analisis Tapak',
        def: [
          'Arsitek mempelajari lahan Anda sebelum merancang apa pun, misalnya arah matahari dan angin, jalur akses, dan pepohonan yang ada. Dengan tahapan ini, bangunan Anda dirancang menyesuaikan kondisi lahan yang sebenarnya.'
        ]
      },
      {
        code: 'ZONING_STUDY',
        label: 'Analisis Zonasi & Regulasi',
        def: [
          'Pengecekan terhadap aturan tata ruang pemerintah daerah, seperti apakah lahan Anda boleh dibangun, jumlah lantai maksimal yang diizinkan, dan teknis lain seperti jarak minimum antara muka bangunan dan jalan, batas lahan terbangun, batas total luas lantai, dan minimum lahan hijau pada lahan Anda. Tahap ini melindungi Anda dari risiko izin ditolak atau bangunan yang harus dibongkar di kemudian hari.'
        ]
      }
    ]
  },
  {
    title: 'Desain Arsitektur',
    items: [
      {
        code: 'SITE_BLOCK_PLAN',
        label: 'Site Plan & Block Plan',
        def: [
          'Site Plan menunjukkan posisi bangunan Anda di dalam lahan, misalnya letak carport, taman, dan pagar.',
          'Block Plan menunjukkan posisi lahan Anda dalam suatu kawasan, umumnya digunakan untuk proyek townhouse.'
        ]
      },
      {
        code: 'FLOOR_PLAN',
        label: 'Denah',
        def: [
          'Gambar tata letak dan dimensi seluruh ruangan dalam bangunan yang dilihat dari atas.'
        ]
      },
      {
        code: 'ELEVATION_SECTION',
        label: 'Tampak & Potongan',
        def: [
          'Tampak menunjukkan wujud bangunan dari empat arah beserta materialnya.',
          'Potongan menunjukkan bangunan yang “diiris” secara vertikal untuk memperlihatkan detail seperti tinggi plafon, ketebalan lantai, dan kemiringan atap.'
        ]
      },
      {
        code: 'DETAIL_DRAWINGS',
        label: 'Rencana & Detail (Atap, Plafon, Lantai, Kamar Mandi, Kusen, Pintu & Jendela, dll.)',
        def: [
          'Gambar rinci mengenai dimensi komponen bangunan Anda. Berguna untuk tahap konstruksi dan menjadi referensi ketika Anda membeli atau mendesain komponen seperti pintu, jendela, kusen, dan lain-lain.'
        ]
      },
      {
        code: 'RENDER_3D_EXTERIOR',
        label: 'Render Gambar 3D Eksterior',
        def: [
          'Visualisasi tampilan luar bangunan dalam bentuk gambar realistis sebelum bangunan benar-benar berdiri.'
        ]
      },
      {
        code: 'RENDER_3D_INTERIOR',
        label: 'Render Gambar 3D Interior',
        def: [
          'Visualisasi suasana ruang dalam bangunan, seperti pencahayaan, ukuran ruangan, warna, dan penataan furnitur.'
        ]
      },
      {
        code: 'RENDER_3D_VIDEO',
        label: 'Render Video 3D Walkthrough/Animation',
        def: ['Simulasi video yang membawa Anda menyusuri rancangan bangunan.']
      },
      {
        code: 'MATERIAL_FINISHING_SPEC',
        label: 'Spesifikasi Material dan Finishing',
        def: [
          'Daftar rinci mengenai jenis, merek, dan kualitas material yang digunakan pada setiap bagian bangunan. Dokumen ini umumnya menjadi acuan tertulis untuk memastikan kesesuaian kualitas material yang terpasang.'
        ]
      }
    ]
  },
  {
    title: 'Desain Teknis',
    items: [
      {
        code: 'STRUCTURAL_DRAWINGS',
        label: 'Gambar Struktur',
        def: [
          'Gambar teknis untuk komponen seperti pondasi, kolom, dan balok, beserta ukuran dan kekuatannya. Tahap ini krusial untuk Anda melanjutkan desain ke tahap konstruksi.'
        ]
      },
      {
        code: 'MECHANICAL_DRAWINGS',
        label: 'Gambar Mechanical',
        def: [
          'Gambar teknis untuk mengatur sistem tata udara, seperti AC dan ventilasi, di seluruh bagian bangunan.'
        ]
      },
      {
        code: 'ELECTRICAL_DRAWINGS',
        label: 'Gambar Electrical',
        def: [
          'Gambar teknis untuk mengatur jalur kelistrikan, termasuk titik lampu, saklar, dan stop kontak di setiap ruangan dalam bangunan.'
        ]
      },
      {
        code: 'PLUMBING_DRAWINGS',
        label: 'Gambar Plumbing',
        def: ['Gambar teknis untuk mengatur jalur perpipaan air bersih, air kotor, dan air hujan.']
      },
      {
        code: 'FIRE_PROTECTION_DRAWINGS',
        label: 'Gambar Proteksi Kebakaran',
        def: [
          'Gambar teknis untuk mengatur sistem deteksi dan pemadam kebakaran, seperti sprinkler dan alarm. Umumnya diperlukan untuk bangunan komersial atau bertingkat.'
        ]
      },
      {
        code: 'STRUCTURAL_CALCULATION',
        label: 'Perhitungan Struktur',
        def: [
          'Perhitungan teknis yang membuktikan bahwa kekuatan struktur bangunan telah sesuai standar keamanan. Dokumen ini diperlukan untuk pengurusan izin bangunan (PBG) dan menjadi jaminan bahwa struktur dirancang dengan perhitungan yang tepat, bukan perkiraan.'
        ]
      },
      {
        code: 'MEP_CALCULATION',
        label: 'Perhitungan Mekanikal, Elektrikal & Plumbing (MEP)',
        def: [
          'Perhitungan kebutuhan daya listrik, kapasitas pompa air, dan sistem pendukung lainnya sesuai kebutuhan bangunan.'
        ]
      }
    ]
  },
  {
    title: 'Interior dan Landscape',
    items: [
      {
        code: 'INTERIOR_DESIGN',
        label: 'Desain Interior',
        def: [
          'Perancangan lanjutan setelah denah, yang menentukan pemilihan warna, furnitur, dan pencahayaan di dalam ruangan. Tahap ini menghadirkan karakter dan kenyamanan pada rumah Anda, tidak sekadar ruang kosong yang telah selesai dibangun.'
        ]
      },
      {
        code: 'LANDSCAPE_DESIGN',
        label: 'Desain Landscape',
        def: [
          'Perancangan mendetail area terbuka seperti taman dan jalur pejalan kaki yang meliputi jumlah, tipe, dan ukuran komponen seperti pohon, bebatuan, dan pencahayaan.'
        ]
      }
    ]
  },
  {
    title: 'Perhitungan & Estimasi',
    items: [
      {
        code: 'COST_ESTIMATION',
        label: 'Estimasi Biaya',
        def: ['Perkiraan total biaya yang dibutuhkan untuk membangun desain bangunan Anda.']
      }
    ]
  }
]

/**
 * Interior documents. These have no deliverable code yet, so the label itself is stored; the
 * project views list unknown values under "other" rather than dropping them.
 */
export const INTERIOR_DOCUMENTS = [
  {
    title: 'Desain Interior',
    items: [
      {
        code: 'Konsep dan moodboard',
        label: 'Konsep dan moodboard',
        def: [
          'Arah gaya, palet warna, dan contoh material sebagai acuan sebelum desain detail dibuat.'
        ]
      },
      {
        code: 'Layout / denah interior',
        label: 'Layout / denah interior',
        def: ['Tata letak furnitur dan alur gerak di area yang didesain, dilihat dari atas.']
      },
      {
        code: 'RENDER_3D_INTERIOR',
        label: 'Render Gambar 3D Interior',
        def: [
          'Visualisasi suasana ruang, seperti pencahayaan, warna, material, dan penataan furnitur.'
        ]
      },
      {
        code: 'Gambar kerja furnitur custom',
        label: 'Gambar kerja furnitur custom',
        def: [
          'Gambar tampak, potongan, dan ukuran detail furnitur seperti kitchen set atau lemari. Dokumen ini dipakai workshop atau tukang untuk produksi.'
        ]
      },
      {
        code: 'MATERIAL_FINISHING_SPEC',
        label: 'Spesifikasi Material dan Finishing',
        def: [
          'Daftar jenis, merek, dan kualitas material serta finishing yang digunakan, sebagai acuan saat produksi dan pemasangan.'
        ]
      }
    ]
  },
  {
    title: 'Instalasi Pendukung',
    items: [
      {
        code: 'Rencana titik listrik dan pencahayaan',
        label: 'Rencana titik listrik dan pencahayaan',
        def: ['Posisi stop kontak, saklar, dan lampu yang menyesuaikan tata letak interior.']
      },
      {
        code: 'Rencana titik air dan saluran',
        label: 'Rencana titik air dan saluran',
        water: true,
        def: [
          'Posisi keran, sink, dan saluran pembuangan. Umumnya dibutuhkan untuk dapur dan kamar mandi.'
        ]
      }
    ]
  },
  {
    title: 'Perhitungan & Estimasi',
    items: [
      {
        code: 'COST_ESTIMATION',
        label: 'Estimasi Biaya',
        def: [
          'Perkiraan biaya pembuatan dan pemasangan sesuai desain, sebagai acuan saat meminta penawaran workshop atau kontraktor.'
        ]
      }
    ]
  }
]

export const LOGIN_PERKS = [
  'Simpan brief dan lanjutkan kapan saja',
  'Terima penawaran dari arsitek terverifikasi',
  'Kelola kontrak, progres, dan pembayaran di satu tempat'
]

export const MAX_REFERENCE_IMAGES = 10
