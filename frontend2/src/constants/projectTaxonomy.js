/**
 * The single source of truth for the three-level project taxonomy.
 *
 * Labels live beside their values rather than in locales/{en,id}.js on purpose: the two
 * locale files must stay key-for-key identical, and ~70 taxonomy entries would mean ~140
 * keys to keep in sync by hand. Here, adding a sub-category is one line that cannot drift.
 *
 * Keep in sync with backend/src/main/java/com/rumantra/shared/constants/ProjectTaxonomy.java,
 * which enforces the same value set server-side.
 */

export const PROJECT_SCOPES = [
  { value: 'NEW_BUILD', labelEn: 'New Build', labelId: 'Bangun Baru' },
  {
    value: 'RENOVATION',
    labelEn: 'Renovation, Remodel, Expansion',
    labelId: 'Renovasi, Remodel, Perluasan'
  },
  { value: 'INTERIOR_FIT_OUT', labelEn: 'Interior Fit-Out', labelId: 'Fit-Out Interior' },
  {
    value: 'RESTORATION',
    labelEn: 'Restoration / Conservation',
    labelId: 'Restorasi / Konservasi'
  }
]

export const PROJECT_CATEGORIES = [
  {
    value: 'RESIDENTIAL',
    labelEn: 'Residential',
    labelId: 'Hunian',
    descriptionEn: 'Any living space, from houses to apartments',
    descriptionId: 'Segala ruang tinggal, dari rumah hingga apartemen'
  },
  {
    value: 'COMMERCIAL',
    labelEn: 'Commercial',
    labelId: 'Komersial',
    descriptionEn: 'Offices, retail, restaurants, hotels, etc.',
    descriptionId: 'Kantor, retail, restoran, hotel, dll.'
  },
  {
    value: 'INDUSTRIAL',
    labelEn: 'Industrial',
    labelId: 'Industri',
    descriptionEn: 'Factories, warehouses, plants',
    descriptionId: 'Pabrik, gudang, fasilitas produksi'
  },
  {
    value: 'INSTITUTIONAL',
    labelEn: 'Institutional',
    labelId: 'Institusi',
    descriptionEn: 'Schools, hospitals, religious buildings, gov. facilities',
    descriptionId: 'Sekolah, rumah sakit, tempat ibadah, fasilitas pemerintah'
  },
  {
    value: 'INTERIOR_ONLY',
    labelEn: 'Interior Only',
    labelId: 'Interior Saja',
    descriptionEn: 'Projects that involve interior design or renovation only',
    descriptionId: 'Proyek yang hanya mencakup desain atau renovasi interior'
  },
  {
    value: 'LANDSCAPE',
    labelEn: 'Landscape',
    labelId: 'Lansekap',
    descriptionEn: 'Gardens, parks, exterior areas (hardscape/softscape)',
    descriptionId: 'Taman, ruang terbuka, area eksterior (hardscape/softscape)'
  },
  {
    value: 'INFRASTRUCTURE',
    labelEn: 'Infrastructure / Utility',
    labelId: 'Infrastruktur / Utilitas',
    descriptionEn: 'Drainage, water tank, towers, bridges, etc.',
    descriptionId: 'Drainase, tandon air, menara, jembatan, dll.'
  },
  {
    value: 'MIXED_USE',
    labelEn: 'Other / Mixed-Use',
    labelId: 'Lainnya / Multi-Fungsi',
    descriptionEn:
      "Projects combining multiple functions or that don't fit other categories (e.g. live-work space, ruko, TOD)",
    descriptionId:
      'Proyek yang menggabungkan beberapa fungsi atau tidak masuk kategori lain (mis. hunian-kerja, ruko, TOD)'
  },
  {
    value: 'RECREATIONAL',
    labelEn: 'Recreational / Sports',
    labelId: 'Rekreasi / Olahraga',
    descriptionEn: 'Sports fields, pools, sports halls, playgrounds',
    descriptionId: 'Lapangan olahraga, kolam renang, gedung olahraga, taman bermain'
  },
  {
    value: 'AGRICULTURAL',
    labelEn: 'Agricultural',
    labelId: 'Pertanian',
    descriptionEn: 'Farms, greenhouses, barns, agro-processing facilities',
    descriptionId: 'Lahan pertanian, rumah kaca, kandang, fasilitas pengolahan hasil tani'
  }
]

/**
 * Sub-categories keyed by category. A category absent from this map (or mapped to an empty
 * list) simply has no third level — the picker is not shown and no value is submitted.
 */
export const SUB_CATEGORIES = {
  RESIDENTIAL: [
    { value: 'HOUSE', labelEn: 'House / Home', labelId: 'Rumah Tinggal' },
    { value: 'VILLA', labelEn: 'Villa', labelId: 'Villa' },
    { value: 'APARTMENT_UNIT', labelEn: 'Apartment Unit', labelId: 'Unit Apartemen' },
    { value: 'BOARDING_HOUSE', labelEn: 'Boarding House (Kost)', labelId: 'Kost' },
    { value: 'TOWNHOUSE', labelEn: 'Townhouse', labelId: 'Townhouse' },
    { value: 'DORMITORY', labelEn: 'Dormitory', labelId: 'Asrama' },
    { value: 'SHOPHOUSE', labelEn: 'Shophouse (Ruko)', labelId: 'Ruko' }
  ],
  COMMERCIAL: [
    { value: 'RETAIL_STORE', labelEn: 'Retail Store / Boutique', labelId: 'Toko Retail / Butik' },
    {
      value: 'OFFICE',
      labelEn: 'Office / Coworking Space / Workshop',
      labelId: 'Kantor / Coworking Space / Workshop'
    },
    { value: 'SHOPHOUSE', labelEn: 'Shophouse (Ruko)', labelId: 'Ruko' },
    { value: 'SHOWROOM', labelEn: 'Showroom', labelId: 'Showroom' },
    { value: 'SALON', labelEn: 'Beauty Salon / Barbershop', labelId: 'Salon / Barbershop' },
    { value: 'CLINIC', labelEn: 'Clinic / Health Services', labelId: 'Klinik / Layanan Kesehatan' },
    { value: 'WAREHOUSE', labelEn: 'Warehouse / Storage', labelId: 'Gudang / Penyimpanan' },
    {
      value: 'MINIMARKET',
      labelEn: 'Convenience Store / Minimarket',
      labelId: 'Toko Kelontong / Minimarket'
    },
    { value: 'MALL_BOOTH', labelEn: 'Mall Booth', labelId: 'Booth Mall' },
    { value: 'CAFE', labelEn: 'Café', labelId: 'Kafe' },
    { value: 'RESTAURANT', labelEn: 'Restaurant', labelId: 'Restoran' },
    { value: 'FOOD_KIOSK', labelEn: 'Food Court Stall / Kiosk', labelId: 'Stan Food Court / Kios' },
    { value: 'BAR_LOUNGE', labelEn: 'Bar / Lounge', labelId: 'Bar / Lounge' },
    { value: 'BAKERY', labelEn: 'Bakery / Dessert Shop', labelId: 'Toko Roti / Dessert' },
    { value: 'HOTEL', labelEn: 'Hotel', labelId: 'Hotel' },
    {
      value: 'GUESTHOUSE',
      labelEn: 'Guesthouse / Homestay / Hostel',
      labelId: 'Guesthouse / Homestay / Hostel'
    },
    { value: 'AIRBNB_UNIT', labelEn: 'Airbnb Unit', labelId: 'Unit Airbnb' },
    { value: 'RESORT', labelEn: 'Resort / Villa for Rent', labelId: 'Resort / Villa Sewa' },
    { value: 'GYM', labelEn: 'Gym / Fitness Center', labelId: 'Gym / Pusat Kebugaran' },
    { value: 'SPA', labelEn: 'Spa / Wellness Retreat', labelId: 'Spa / Wellness Retreat' },
    { value: 'PHARMACY', labelEn: 'Pharmacy', labelId: 'Apotek' },
    { value: 'LABORATORY', labelEn: 'Laboratory', labelId: 'Laboratorium' },
    { value: 'BANK_BRANCH', labelEn: 'Bank Branch', labelId: 'Kantor Cabang Bank' },
    { value: 'GAS_STATION', labelEn: 'Gas Station (SPBU)', labelId: 'SPBU' },
    { value: 'CAR_WASH', labelEn: 'Car Wash', labelId: 'Cuci Mobil' },
    { value: 'EVENT_HALL', labelEn: 'Event Hall / Ballroom', labelId: 'Gedung Acara / Ballroom' },
    { value: 'CINEMA', labelEn: 'Cinema', labelId: 'Bioskop' },
    { value: 'LAUNDRY', labelEn: 'Laundry', labelId: 'Laundry' }
  ],
  INDUSTRIAL: [
    {
      value: 'FACTORY',
      labelEn: 'Factory / Production Facility',
      labelId: 'Pabrik / Fasilitas Produksi'
    },
    {
      value: 'FOOD_PROCESSING',
      labelEn: 'Food Processing Plant',
      labelId: 'Pabrik Pengolahan Makanan'
    },
    { value: 'WORKSHOP', labelEn: 'Workshop (Industrial)', labelId: 'Bengkel Industri' },
    { value: 'PACKAGING_PLANT', labelEn: 'Packaging Plant', labelId: 'Pabrik Pengemasan' },
    { value: 'COLD_STORAGE', labelEn: 'Cold Storage', labelId: 'Cold Storage' },
    {
      value: 'UTILITY_BUILDING',
      labelEn: 'Utility Building (Genset, Water Treatment)',
      labelId: 'Bangunan Utilitas (Genset, Pengolahan Air)'
    },
    {
      value: 'WASTE_MANAGEMENT',
      labelEn: 'Waste Management Facility',
      labelId: 'Fasilitas Pengelolaan Limbah'
    },
    { value: 'INDUSTRIAL_WAREHOUSE', labelEn: 'Industrial Warehouse', labelId: 'Gudang Industri' },
    { value: 'LOGISTIC_HUB', labelEn: 'Logistic Hub', labelId: 'Pusat Logistik' }
  ],
  INSTITUTIONAL: [
    { value: 'KINDERGARTEN', labelEn: 'Kindergarten / PAUD', labelId: 'TK / PAUD' },
    { value: 'SCHOOL', labelEn: 'School', labelId: 'Sekolah' },
    { value: 'LEARNING_CENTER', labelEn: 'Learning Center', labelId: 'Pusat Belajar' },
    { value: 'LIBRARY', labelEn: 'Library', labelId: 'Perpustakaan' },
    { value: 'RELIGIOUS_FACILITY', labelEn: 'Religious Facility', labelId: 'Tempat Ibadah' },
    { value: 'COMMUNITY_CENTER', labelEn: 'Community Center', labelId: 'Balai Warga' },
    { value: 'GALLERY_MUSEUM', labelEn: 'Gallery / Museum', labelId: 'Galeri / Museum' },
    {
      value: 'GOVERNMENT_FACILITY',
      labelEn: 'Government Facility',
      labelId: 'Fasilitas Pemerintah'
    },
    { value: 'HOSPITAL', labelEn: 'Hospital', labelId: 'Rumah Sakit' },
    { value: 'UNIVERSITY', labelEn: 'University / Campus', labelId: 'Universitas / Kampus' },
    { value: 'NURSING_HOME', labelEn: 'Nursing Home (Panti Jompo)', labelId: 'Panti Jompo' },
    { value: 'ORPHANAGE', labelEn: 'Orphanage (Panti Asuhan)', labelId: 'Panti Asuhan' },
    {
      value: 'FIRE_POLICE_STATION',
      labelEn: 'Fire Station / Police Station',
      labelId: 'Pos Pemadam Kebakaran / Kantor Polisi'
    },
    { value: 'CEMETERY', labelEn: 'Cemetery (TPU)', labelId: 'Pemakaman (TPU)' }
  ],
  INTERIOR_ONLY: [
    { value: 'RESIDENTIAL_INTERIOR', labelEn: 'Residential Interior', labelId: 'Interior Hunian' },
    { value: 'OFFICE_INTERIOR', labelEn: 'Office Interior', labelId: 'Interior Kantor' },
    {
      value: 'RETAIL_FNB_INTERIOR',
      labelEn: 'Retail / F&B Interior',
      labelId: 'Interior Retail / F&B'
    },
    {
      value: 'HOSPITALITY_INTERIOR',
      labelEn: 'Hospitality Interior (Hotel / Resort)',
      labelId: 'Interior Perhotelan (Hotel / Resort)'
    }
  ],
  LANDSCAPE: [
    { value: 'PRIVATE_GARDEN', labelEn: 'Private Garden', labelId: 'Taman Pribadi' },
    { value: 'PUBLIC_PARK', labelEn: 'Public Park', labelId: 'Taman Publik' },
    { value: 'ROOFTOP_GARDEN', labelEn: 'Rooftop Garden', labelId: 'Taman Atap' },
    {
      value: 'STREETSCAPE',
      labelEn: 'Streetscape / Urban Design',
      labelId: 'Lansekap Jalan / Desain Perkotaan'
    }
  ],
  INFRASTRUCTURE: [
    { value: 'DRAINAGE', labelEn: 'Drainage', labelId: 'Drainase' },
    { value: 'ROAD', labelEn: 'Road', labelId: 'Jalan' },
    { value: 'BRIDGE', labelEn: 'Bridge', labelId: 'Jembatan' },
    {
      value: 'TELECOM_TOWER',
      labelEn: 'Telecommunication Tower',
      labelId: 'Menara Telekomunikasi'
    },
    { value: 'PARKING_STRUCTURE', labelEn: 'Parking Structure', labelId: 'Gedung Parkir' },
    { value: 'SOLAR_PANEL', labelEn: 'Solar Panel Installation', labelId: 'Instalasi Panel Surya' }
  ],
  MIXED_USE: [
    { value: 'LIVE_WORK', labelEn: 'Live-Work Space', labelId: 'Ruang Hunian-Kerja' },
    {
      value: 'TOD',
      labelEn: 'Transit-Oriented Development (TOD)',
      labelId: 'Kawasan Berorientasi Transit (TOD)'
    },
    {
      value: 'MIXED_USE_TOWER',
      labelEn: 'Mixed-Use Tower (Residential + Commercial)',
      labelId: 'Gedung Multi-Fungsi (Hunian + Komersial)'
    }
  ],
  RECREATIONAL: [
    {
      value: 'SPORTS_FIELD',
      labelEn: 'Sports Field / Futsal Court',
      labelId: 'Lapangan Olahraga / Futsal'
    },
    { value: 'SWIMMING_POOL', labelEn: 'Public Swimming Pool', labelId: 'Kolam Renang Umum' },
    { value: 'SPORTS_HALL', labelEn: 'GOR / Sports Hall', labelId: 'GOR / Gedung Olahraga' },
    { value: 'GOLF_COURSE', labelEn: 'Golf Course', labelId: 'Lapangan Golf' },
    { value: 'PLAYGROUND', labelEn: 'Playground', labelId: 'Taman Bermain' }
  ],
  AGRICULTURAL: [
    { value: 'GREENHOUSE', labelEn: 'Greenhouse', labelId: 'Rumah Kaca' },
    { value: 'BARN', labelEn: 'Barn / Livestock Shed', labelId: 'Lumbung / Kandang Ternak' },
    { value: 'FARM_STORAGE', labelEn: 'Farm Storage / Silo', labelId: 'Gudang Hasil Tani / Silo' },
    { value: 'AGROTOURISM', labelEn: 'Agrotourism Facility', labelId: 'Fasilitas Agrowisata' }
  ]
}

/**
 * Sub-categories retired by the 2026 taxonomy revision. Projects created before it still carry
 * them, so they keep a readable label but are never offered in a picker or accepted on submit.
 */
const LEGACY_SUB_CATEGORIES = {
  INFRASTRUCTURE: [
    { value: 'PARK', labelEn: 'Park', labelId: 'Taman' },
    { value: 'OTHER_INFRASTRUCTURE', labelEn: 'Other', labelId: 'Lainnya' }
  ]
}

const labelKey = locale => (locale === 'id' ? 'labelId' : 'labelEn')

const findLabel = (list, value, locale) => {
  if (!value) return ''
  const match = list.find(entry => entry.value === value)
  return match ? match[labelKey(locale)] : value
}

export const subCategoriesFor = category => SUB_CATEGORIES[category] || []

export const requiresSubCategory = category => subCategoriesFor(category).length > 0

export const isValidCategory = value => PROJECT_CATEGORIES.some(c => c.value === value)

export const isValidSubCategory = (category, value) =>
  subCategoriesFor(category).some(s => s.value === value)

export const scopeLabel = (value, locale) => findLabel(PROJECT_SCOPES, value, locale)

export const categoryLabel = (value, locale) => findLabel(PROJECT_CATEGORIES, value, locale)

export const subCategoryLabel = (category, value, locale) =>
  findLabel(
    [...subCategoriesFor(category), ...(LEGACY_SUB_CATEGORIES[category] || [])],
    value,
    locale
  )

/**
 * What to show when a project is summarised in one line. The sub-category is the most
 * specific thing we know, so it wins; the category is the fallback for drafts and older
 * projects saved without one.
 */
export const projectTypeLabel = (project, locale) => {
  if (!project) return ''
  const category = project.buildingFunction || project.buildingType
  const sub = project.subCategory
  if (sub) return subCategoryLabel(category, sub, locale)
  return categoryLabel(category, locale)
}

/**
 * Old single-field values, mapped onto the new pair. Keeps existing ?type= links and the
 * seeded landing presets meaningful instead of silently resolving to nothing.
 */
export const LEGACY_TYPE_MAP = {
  RESIDENTIAL: { projectScope: 'NEW_BUILD', category: 'RESIDENTIAL', subCategory: 'HOUSE' },
  STUDENT_HOUSING: {
    projectScope: 'NEW_BUILD',
    category: 'RESIDENTIAL',
    subCategory: 'BOARDING_HOUSE'
  },
  VILLA: { projectScope: 'NEW_BUILD', category: 'RESIDENTIAL', subCategory: 'VILLA' },
  COMMERCIAL: { projectScope: 'NEW_BUILD', category: 'COMMERCIAL', subCategory: 'OFFICE' },
  RENOVATION: { projectScope: 'RENOVATION', category: 'RESIDENTIAL', subCategory: 'HOUSE' }
}
