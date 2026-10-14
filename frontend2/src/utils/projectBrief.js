import { categoryLabel, scopeLabel, subCategoryLabel } from '@/constants/projectTaxonomy'
import {
  SURVEY_SCOPES,
  SCOPE_INTERIOR,
  INTERIOR_WHOLE,
  ARCHITECTURE_DOCUMENTS,
  INTERIOR_DOCUMENTS,
  APARTMENT_SUB_CATEGORY,
  START_OPTIONS
} from '@/constants/projectSurvey'
import { placeLabel } from '@/constants/regions'

// Labels of the lines the survey appends to a project's requirements text. The survey writes
// them (useProjectSurvey.requirementsText) and both the survey and the detail pages read them
// back, because the project entity has no columns for these answers yet.
export const LINE = {
  services: 'Layanan:',
  scopes: 'Lingkup proyek:',
  areas: 'Area interior:',
  apartment: 'Tipe unit apartemen:',
  wall: 'Panjang dinding:',
  floorsUnknown: 'Jumlah lantai: belum ditentukan'
}

/** Splits a survey project's `scopeOfWork` into the client's own words and the labelled lines. */
export const parseRequirementsText = scopeOfWork => {
  const text = String(scopeOfWork || '')
  // A draft saved before the needs step has no vision yet, so its text opens with the labels
  const markerAt = text.startsWith(LINE.services) ? 0 : text.indexOf(`\n\n${LINE.services}`)
  const vision = markerAt >= 0 ? text.slice(0, markerAt) : text
  const meta = {}
  if (markerAt >= 0) {
    text
      .slice(markerAt)
      .split('\n')
      .forEach(line => {
        const hit = Object.entries(LINE).find(([, label]) => line.startsWith(label))
        if (hit) meta[hit[0]] = line.slice(hit[1].length).trim()
      })
  }
  return { vision: vision.trim(), meta, floorsUnknown: text.includes(LINE.floorsUnknown) }
}

const interiorLabel = SURVEY_SCOPES.find(scope => scope.value === SCOPE_INTERIOR).label

const formatShortIDR = (value, units) => {
  if (value >= 1_000_000) {
    return {
      n: (value / 1_000_000).toLocaleString('id-ID', { maximumFractionDigits: 1 }),
      unit: units.million
    }
  }
  return {
    n: (value / 1_000).toLocaleString('id-ID', { maximumFractionDigits: 0 }),
    unit: units.thousand
  }
}

const feeText = (min, max, units, compact) => {
  if (min == null) return '—'
  const lo = formatShortIDR(min, units)
  if (max == null || max === min) return `Rp${lo.n} ${lo.unit}`
  const hi = formatShortIDR(max, units)
  if (compact && lo.unit === hi.unit) return `Rp${lo.n}–${hi.n} ${hi.unit}`
  return `Rp${lo.n} ${lo.unit} – Rp${hi.n} ${hi.unit}`
}

/** One amount in the brief's short form, e.g. "Rp52 Jt". `copy` is the `projectDetailV2` block. */
export const formatShortFee = (value, copy) =>
  value == null
    ? '—'
    : feeText(
        Number(value),
        null,
        { million: copy.millionShort, thousand: copy.thousandShort },
        true
      )

export const formatBriefDate = (value, locale, withTime = false) => {
  if (!value) return ''
  const date = new Date(/^\d{4}-\d{2}-\d{2}$/.test(value) ? `${value}T00:00:00` : value)
  const tag = locale === 'en' ? 'en-GB' : 'id-ID'
  const day = date.toLocaleDateString(tag, {
    day: 'numeric',
    month: withTime ? 'short' : 'long',
    year: 'numeric'
  })
  if (!withTime) return day
  return `${day} · ${date.toLocaleTimeString(tag, { hour: '2-digit', minute: '2-digit' })}`
}

const documentGroups = (codes, interiorFirst, deliverableItems, otherTitle) => {
  const lists = interiorFirst
    ? [INTERIOR_DOCUMENTS, ARCHITECTURE_DOCUMENTS]
    : [ARCHITECTURE_DOCUMENTS, INTERIOR_DOCUMENTS]
  const groups = new Map()
  const other = []
  codes.forEach(code => {
    const group = lists.flat().find(g => g.items.some(item => item.code === code))
    if (!group) {
      other.push(deliverableItems?.[code] || code.replace(/_/g, ' '))
      return
    }
    if (!groups.has(group.title)) groups.set(group.title, [])
    groups.get(group.title).push(group.items.find(item => item.code === code).label)
  })
  const ordered = lists
    .flat()
    .filter(g => groups.has(g.title))
    .map(g => ({ title: g.title, items: groups.get(g.title) }))
    .filter((g, i, all) => all.findIndex(x => x.title === g.title) === i)
  return other.length ? [...ordered, { title: otherTitle, items: other }] : ordered
}

/**
 * Everything the v2 detail pages show about a brief, derived from a ProjectResponse.
 * `copy` is the `projectDetailV2` locale block; `deliverableItems` is `bidCreate.deliverableItems`.
 */
export const briefView = (project, { locale, copy, deliverableItems }) => {
  const { vision, meta, floorsUnknown } = parseRequirementsText(project.scopeOfWork)

  const scopes = meta.scopes
    ? meta.scopes.split(', ').filter(Boolean)
    : [
        SURVEY_SCOPES.find(s => s.value === project.projectScope)?.label ||
          scopeLabel(project.projectScope, locale)
      ].filter(Boolean)
  const interiorOnly = scopes.length > 0 && scopes.every(scope => scope === interiorLabel)
  const isApartment = project.subCategory === APARTMENT_SUB_CATEGORY

  const areas = meta.areas
    ? meta.areas.split('; ').map(entry => {
        const [area, scope] = entry.split(' · ')
        return area === INTERIOR_WHOLE
          ? { area: copy.interiorArea, scope: area }
          : { area, scope: scope || '—' }
      })
    : []

  const units = { million: copy.millionShort, thousand: copy.thousandShort }
  const min = project.designBudgetMin
  const max = project.designBudgetMax
  const feeLabel = interiorOnly ? copy.feeDesign : copy.feeArchitect
  const fee = feeText(min, max, units, false)
  const m2 = value => (value != null ? `${value} m²` : '—')

  const facts = [
    {
      k: feeLabel,
      v: feeText(min, max, units, true),
      sub: interiorOnly ? copy.feeDesignSub : copy.feeArchitectSub
    }
  ]
  if (interiorOnly) {
    if (isApartment && meta.apartment)
      facts.push({ k: copy.unitType, v: meta.apartment, sub: copy.unitTypeSub })
    facts.push({ k: copy.area, v: m2(project.estimatedBuildArea), sub: copy.areaSub })
    if (meta.wall)
      facts.push({
        k: copy.wallLength,
        v: meta.wall.replace(/\s*m$/, ' m'),
        sub: copy.wallLengthSub
      })
  } else {
    facts.push(
      { k: copy.lotSize, v: m2(project.lotSize), sub: copy.lotSizeSub },
      { k: copy.projectArea, v: m2(project.estimatedBuildArea), sub: copy.projectAreaSub },
      {
        k: copy.floors,
        v:
          project.numberOfFloors != null
            ? copy.floorsValue.replace('{n}', project.numberOfFloors)
            : '—',
        sub: floorsUnknown ? copy.floorsUnknown : ''
      }
    )
  }

  const property = [
    categoryLabel(project.buildingFunction, locale),
    subCategoryLabel(project.buildingFunction, project.subCategory, locale),
    isApartment && meta.apartment
  ]
    .filter(Boolean)
    .join(' · ')
  const region = placeLabel(project)

  const start =
    project.startDateType === 'SPECIFIC_DATE' && project.expectedStartDate
      ? formatBriefDate(project.expectedStartDate, locale)
      : locale === 'en'
        ? copy.startImmediately
        : START_OPTIONS[0].label

  const docs = documentGroups(
    project.deliverables || [],
    interiorOnly,
    deliverableItems,
    copy.otherDocuments
  )
  const files = project.files || []

  return {
    title: project.title,
    vision,
    scopes,
    areas,
    property,
    region,
    address: project.fullAddress || '',
    metaLine: [property, region].filter(Boolean).join(' · '),
    facts,
    feeLabel,
    fee,
    start,
    docs,
    docCount: docs.reduce((n, g) => n + g.items.length, 0),
    images: files.filter(f => f.fileType?.startsWith('image/')),
    files: files.filter(f => !f.fileType?.startsWith('image/')),
    submittedAt: project.submittedAt || null,
    reviewedAt: project.reviewedAt || null
  }
}
