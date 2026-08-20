export const CATEGORIES = [
  { value: 'semua', labelKey: 'filterAll' },
  { value: 'hunian', labelKey: 'filterHunian' },
  { value: 'komersil', labelKey: 'filterKomersil' },
  { value: 'industrial', labelKey: 'filterIndustrial' },
  { value: 'lainnya', labelKey: 'filterLainnya' }
]

const CATEGORY_MAP = {
  hunian: ['hunian', 'rumah', 'residential', 'villa', 'apartemen', 'apartment', 'townhouse'],
  komersil: [
    'komersil',
    'komersial',
    'commercial',
    'kantor',
    'office',
    'hotel',
    'mall',
    'retail',
    'kafe',
    'cafe',
    'restoran',
    'restaurant',
    'ruko'
  ],
  industrial: [
    'industri',
    'industrial',
    'pabrik',
    'factory',
    'gudang',
    'warehouse',
    'manufaktur',
    'logistic',
    'cold_storage',
    'packaging'
  ]
}

export function resolveCategory(project) {
  const haystack =
    `${project.buildingFunction || ''} ${project.subCategory || ''} ${project.projectCategory || ''}`.toLowerCase()
  for (const [cat, keywords] of Object.entries(CATEGORY_MAP)) {
    if (keywords.some(kw => haystack.includes(kw))) return cat
  }
  return 'lainnya'
}

export function filterProjects(projects, { search = '', category = 'semua' } = {}) {
  let result = projects
  if (category !== 'semua') {
    result = result.filter(p => resolveCategory(p) === category)
  }
  if (search.trim()) {
    const q = search.trim().toLowerCase()
    result = result.filter(
      p =>
        p.title?.toLowerCase().includes(q) ||
        p.location?.toLowerCase().includes(q) ||
        p.buildingFunction?.toLowerCase().includes(q) ||
        p.subCategory?.toLowerCase().includes(q)
    )
  }
  return result
}
