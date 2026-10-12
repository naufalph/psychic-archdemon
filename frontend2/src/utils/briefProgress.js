/**
 * How far a DRAFT project has come through the brief, for the dashboard quick actions.
 * Also reads drafts saved by the retired ProjectCreate wizard, which lack the survey's extra lines.
 */

// The survey appends labelled lines after the client's own words; see useProjectSurvey
const SURVEY_META_START = 'Layanan:'

// The client's own description, without the survey's labelled lines
export const briefVision = text => {
  const value = String(text || '')
  if (value.startsWith(SURVEY_META_START)) return ''
  const at = value.indexOf(`\n\n${SURVEY_META_START}`)
  return (at >= 0 ? value.slice(0, at) : value).trim()
}

const SECTIONS = [
  { label: 'Jenis properti', done: p => !!(p.buildingFunction && p.subCategory) },
  { label: 'Lokasi proyek', done: p => !!(p.city && p.province) },
  {
    label: 'Luas tanah dan area',
    done: p => p.projectScope === 'INTERIOR_FIT_OUT' || !!(p.lotSize && p.estimatedBuildArea)
  },
  { label: 'Kebutuhan proyek', done: p => !!briefVision(p.scopeOfWork) },
  { label: 'Dokumen yang dibutuhkan', done: p => (p.deliverables || []).length > 0 },
  { label: 'Budget fee', done: p => p.designBudgetMin != null }
]

export const briefProgress = project => {
  const done = SECTIONS.filter(section => section.done(project)).length
  return {
    done,
    total: SECTIONS.length,
    percent: Math.round((done / SECTIONS.length) * 100),
    next: SECTIONS.find(section => !section.done(project))?.label || null
  }
}
