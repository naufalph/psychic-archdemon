/**
 * Content for the architect onboarding (/architect/onboarding), ported from the Claude Design
 * file "Onboarding Arsitek rumantra". Indonesian only for now, like the design and like
 * projectSurvey.js.
 *
 * Architectural expertise reuses the IAI taxonomy in iaiTaxonomy.js. Interior expertise has no
 * backend vocabulary yet, so INTERIOR_TYPES stays on the client until the API accepts it.
 */

// Screens of the flow; 'welcome' sits outside the numbered progress
export const ONBOARDING_FLOW = ['welcome', 'profesi', 'jenis', 'dataDiri', 'keahlian']

// The flow ends at keahlian; documents and the rest of the profile continue in the dashboard
export const ONBOARDING_PROGRESS = [
  { key: 'profesi', label: '1 · Profesi' },
  { key: 'jenis', label: '2 · Jenis usaha' },
  { key: 'dataDiri', label: '3 · Data diri' },
  { key: 'keahlian', label: '4 · Keahlian' }
]
export const ONBOARDING_TOTAL_STEPS = ONBOARDING_PROGRESS.length

export const WELCOME_POINTS = [
  {
    icon: 'SearchCheck',
    title: 'Proyek datang ke Anda',
    desc: 'Kami yang mencari dan memvalidasi klien, lalu memastikan brief-nya lengkap sebelum sampai ke Anda.'
  },
  {
    icon: 'Lightbulb',
    title: 'Penawaran berbasis konsep',
    desc: 'Tawarkan konsep desain sejak awal, tanpa harus mengerjakan banyak hal gratis saat klien masih bertanya.'
  },
  {
    icon: 'Wallet',
    title: 'Langsung cair per tahap',
    desc: 'Dana cair setiap tahap disetujui, sesuai jadwal pembayaran yang Anda tentukan sendiri.'
  },
  {
    icon: 'LayoutDashboard',
    title: 'Kelola proyek di dashboard',
    desc: 'Diskusi, revisi, dan progres ada di satu tempat. Setiap proyek yang selesai membangun reputasi Anda.'
  }
]

export const PROFESI_OPTIONS = [
  {
    id: 'ARSITEK',
    icon: 'House',
    title: 'Arsitek',
    desc: 'Merancang bangunan: rumah, gedung komersial, renovasi, hingga bangunan publik.'
  },
  {
    id: 'INTERIOR',
    icon: 'Sofa',
    title: 'Desainer interior',
    desc: 'Merancang ruang dalam: tata letak, material, furnitur, dan pencahayaan.'
  }
]
export const PROFESI_SOON = {
  icon: 'HardHat',
  title: 'Kontraktor',
  desc: 'Membangun dan merenovasi sesuai gambar kerja. Belum bisa dipilih saat ini.',
  chip: 'Coming Soon'
}

// `category` is the value the architect profile already stores (rmtr_architect.category)
export const JENIS_USAHA_OPTIONS = [
  {
    id: 'SOLO',
    category: 'freelance',
    icon: 'User',
    title: 'Perseorangan',
    desc: 'Arsitek atau desainer interior independen yang menerima proyek atas nama pribadi.',
    needs: ['Nama lengkap', 'KTP', 'NPWP pribadi']
  },
  {
    id: 'STUDIO',
    category: 'studio',
    icon: 'Users',
    title: 'Studio',
    desc: 'Tim atau studio yang bekerja bersama, tanpa badan usaha resmi.',
    needs: ['Nama studio', 'KTP penanggung jawab', 'NPWP']
  },
  {
    id: 'COMPANY',
    category: 'company',
    icon: 'Building2',
    title: 'Perusahaan',
    desc: 'Badan usaha berbadan hukum, misalnya PT atau CV.',
    needs: ['Nama perusahaan', 'KTP penanggung jawab', 'NPWP perusahaan']
  }
]

// Copy on the data diri screen that depends on the business type
export const DATA_DIRI_COPY = {
  SOLO: {
    title: 'Data diri Anda',
    tahunLabel: 'Tahun mulai praktik arsitektur',
    tahunHint: 'Tahun pertama Anda menangani proyek sebagai arsitek.',
    publik: 'nama, kota, tahun praktik, website, dan media sosial'
  },
  STUDIO: {
    title: 'Data diri dan studio Anda',
    entitasLabel: 'Nama studio',
    entitasPh: 'Contoh: Studio Ruang Teduh',
    tahunLabel: 'Tahun pendirian studio',
    tahunHint: 'Tahun studio mulai menerima proyek.',
    publik: 'nama, nama studio, kota, tahun pendirian, website, dan media sosial'
  },
  COMPANY: {
    title: 'Data diri dan perusahaan Anda',
    entitasLabel: 'Nama perusahaan',
    entitasPh: 'Contoh: PT Ruang Teduh Arsitektur',
    tahunLabel: 'Tahun pendirian perusahaan',
    tahunHint: 'Sesuai akta pendirian.',
    publik: 'nama, nama perusahaan, kota, tahun pendirian, website, dan media sosial'
  }
}

export const INTERIOR_TYPES = [
  { value: 'INT_HUNIAN', label: 'Interior Hunian', examples: 'Rumah, apartemen, vila' },
  { value: 'INT_KANTOR', label: 'Interior Kantor', examples: 'Kantor, coworking, ruang rapat' },
  {
    value: 'INT_RETAIL_FNB',
    label: 'Interior Retail / F&B',
    examples: 'Toko, butik, kafe, restoran'
  },
  {
    value: 'INT_PERHOTELAN',
    label: 'Interior Perhotelan (Hotel / Resort)',
    examples: 'Hotel, resort, lobi, kamar tamu'
  },
  { value: 'INT_FURNITUR', label: 'Desain Furnitur', examples: 'Furnitur custom dan built-in' },
  {
    value: 'INT_PAMERAN',
    label: 'Desain Pameran & Set Exhibition',
    examples: 'Booth pameran, set acara, display produk'
  }
]

// IAI categories expanded when the keahlian screen first opens
export const IAI_OPEN_AT_START = ['K2', 'K3']

// Short category prefix on the selected-expertise chips, e.g. "Kat. 2 · Komersial"
export const IAI_CATEGORY_SHORT = {
  SOSIAL: 'Sosial',
  K1: 'Kat. 1',
  K2: 'Kat. 2',
  K3: 'Kat. 3',
  KHUSUS: 'Khusus'
}
