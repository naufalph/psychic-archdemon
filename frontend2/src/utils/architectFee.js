// Pedoman IAI 2007 (Pasal 50, 51, 61 + Lampiran 2.A)
// Kolom biaya dalam miliar rupiah; kolom kategori dalam persen.
const PCT_TABLE = [
  [0.2, 6.5, 7.0, 8.0],
  [2, 5.51, 5.9, 6.48],
  [4, 4.78, 5.13, 5.6],
  [20, 4.2, 4.52, 4.92],
  [40, 3.71, 4.01, 4.38],
  [60, 3.29, 3.58, 3.92],
  [80, 2.92, 3.2, 3.52],
  [100, 2.6, 2.88, 3.18],
  [120, 2.32, 2.59, 2.88],
  [140, 2.07, 2.34, 2.62],
  [160, 1.86, 2.12, 2.39],
  [180, 1.67, 1.98, 2.2],
  [200, 1.51, 1.76, 2.03],
  [220, 1.37, 1.62, 1.88],
  [240, 1.25, 1.51, 1.76],
  [260, 1.16, 1.41, 1.67],
  [280, 1.09, 1.34, 1.59],
  [300, 1.04, 1.29, 1.54],
  [500, 1.0, 1.25, 1.5]
]

const STAGES = [
  { id: 'konsep', w: 0.1 },
  { id: 'pra', w: 0.15 },
  { id: 'pengembangan', w: 0.3 },
  { id: 'gambar', w: 0.25 },
  { id: 'pengadaan', w: 0.1 },
  { id: 'pengawasan', w: 0.1 }
]
const SCOPE_GAMBAR = ['konsep', 'pra', 'pengembangan', 'gambar']

export const FEE_WORKS = [
  { id: 'baru', f: 1.0 },
  { id: 'renovasi', f: 1.5 }
]

// Kategori bangunan tetap di kategori 3 (mengikuti desain sumber, yang belum menampilkan
// pemilih jenis bangunan pada kalkulator).
export const FEE_CATEGORY = 3

function feePercent(cat, B) {
  const m = B / 1e9
  const last = PCT_TABLE.length - 1
  if (m <= PCT_TABLE[0][0]) return PCT_TABLE[0][cat]
  if (m >= PCT_TABLE[last][0]) return PCT_TABLE[last][cat]
  for (let i = 0; i < last; i++) {
    const b1 = PCT_TABLE[i][0]
    const b2 = PCT_TABLE[i + 1][0]
    if (m >= b1 && m <= b2) {
      const p1 = PCT_TABLE[i][cat]
      const p2 = PCT_TABLE[i + 1][cat]
      return p1 + ((m - b1) / (b2 - b1)) * (p2 - p1)
    }
  }
  return PCT_TABLE[last][cat]
}

function scopeFactor(stageIds) {
  const S = STAGES.filter(s => stageIds.indexOf(s.id) >= 0).reduce((a, s) => a + s.w, 0)
  if (S >= 0.999) return 1
  return S + Math.min(0.5 * (1 - S), 0.2)
}

const feeFor = (B, wf) => (B * feePercent(FEE_CATEGORY, B) * scopeFactor(SCOPE_GAMBAR) * wf) / 100

export const formatRupiah = n => `Rp ${Math.round(n).toLocaleString('id-ID')}`

const toNumber = v => {
  const n = Number(v)
  return v === '' || v === null || v === undefined || isNaN(n) ? null : n
}

/**
 * Design fee (concept through working drawings) for a construction budget range.
 * Returns `{ notice: 'fillBudget' | 'invalidRange' }` when the inputs cannot produce an estimate.
 */
export function estimateDesignFee(workId, budgetMin, budgetMax) {
  const work = FEE_WORKS.find(w => w.id === workId) || FEE_WORKS[0]
  const bMin = toNumber(budgetMin)
  let bMax = toNumber(budgetMax)

  if (bMin === null || bMin <= 0) return { notice: 'fillBudget' }
  if (bMax !== null && bMax > 0 && bMax < bMin) return { notice: 'invalidRange' }
  if (bMax !== null && (bMax === bMin || bMax <= 0)) bMax = null

  const lo = feeFor(bMin, work.f)
  const hi = bMax !== null ? feeFor(bMax, work.f) : null

  return {
    work,
    bMin,
    bMax,
    lo,
    hi,
    feeText: hi ? `${formatRupiah(lo)} – ${formatRupiah(hi)}` : formatRupiah(lo),
    budgetText: bMax ? `${formatRupiah(bMin)} – ${formatRupiah(bMax)}` : formatRupiah(bMin)
  }
}
