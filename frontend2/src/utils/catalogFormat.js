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
  industrial: ['industri', 'industrial', 'pabrik', 'factory', 'gudang', 'warehouse', 'manufaktur']
}

export function resolveCategory(project) {
  const haystack =
    `${project.buildingFunction || ''} ${project.projectCategory || ''}`.toLowerCase()
  for (const [cat, keywords] of Object.entries(CATEGORY_MAP)) {
    if (keywords.some(kw => haystack.includes(kw))) return cat
  }
  return 'lainnya'
}

export function formatBudget(amount) {
  if (!amount) return null
  if (amount >= 1_000_000_000)
    return `Rp ${(amount / 1_000_000_000).toFixed(1).replace('.0', '')} M`
  if (amount >= 1_000_000) return `Rp ${(amount / 1_000_000).toFixed(0)} jt`
  return `Rp ${amount.toLocaleString('id-ID')}`
}

export function formatStatus(status) {
  const map = { OPEN: 'Open', IN_PROGRESS: 'Berjalan', COMPLETED: 'Selesai' }
  return map[status] || status
}

export function statusBadgeClass(status) {
  if (status === 'OPEN') return 'bg-green-100 text-green-800'
  if (status === 'IN_PROGRESS') return 'bg-blue-100 text-blue-800'
  if (status === 'COMPLETED') return 'bg-gray-100 text-gray-600'
  return 'bg-gray-100 text-gray-600'
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
        p.buildingFunction?.toLowerCase().includes(q)
    )
  }
  return result
}
