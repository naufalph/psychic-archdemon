import { reactive, computed, watch } from 'vue'
import { subCategoryLabel } from '@/constants/projectTaxonomy'
import {
  SURVEY_FLOW,
  SURVEY_SCOPES,
  SCOPE_INTERIOR,
  INTERIOR_AREAS,
  INTERIOR_WHOLE,
  INTERIOR_OTHER,
  INTERIOR_WET_AREAS,
  ARCHITECTURE_DOCUMENTS,
  INTERIOR_DOCUMENTS,
  APARTMENT_SUB_CATEGORY,
  SURVEY_SERVICES,
  SURVEY_CATEGORY_GROUPS
} from '@/constants/projectSurvey'
import { citiesFor, provinceLabel } from '@/constants/regions'
import { formatIDRDisplay } from '@/utils/currencyFormat'
import { LINE, parseRequirementsText } from '@/utils/projectBrief'

const STORAGE_KEY = 'rumantra:project-survey:v1'

const blankState = () => ({
  step: 0,
  presetSlug: null,
  presetTitle: '',
  draftId: null,
  services: { ARCHITECTURE: true },
  notified: {},
  scopes: {},
  scopeOtherOn: false,
  scopeOther: '',
  intAreas: {},
  intAreaOther: '',
  intScope: {},
  intScopeOther: {},
  groupId: '',
  category: '',
  subCategory: '',
  province: '',
  city: '',
  address: '',
  locationLabel: '',
  latitude: null,
  longitude: null,
  land: '',
  building: '',
  wall: '',
  floors: '',
  floorsUnknown: false,
  aptType: '',
  vision: '',
  docs: {},
  designFee: '',
  startType: '',
  startDate: '',
  phone: ''
})

const readStored = () => {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

// Only a province code and a city from that province's list count as an answer. Anything else
// (a draft saved before the dropdowns, or a project from the old Google form) is asked again.
const knownPlace = (province, city) => {
  const code = provinceLabel(province) ? province : ''
  return { province: code, city: code && citiesFor(code).some(c => c.value === city) ? city : '' }
}

let shared = null

/**
 * Shared state for the survey-style brief. Module-scoped so every step component reads the
 * same answers, and mirrored to localStorage so a visitor who leaves for Google sign-in or
 * the email-verification link comes back to the step they were on. Reference images are
 * File objects and cannot be stored, so they live only in memory.
 */
export function useProjectSurvey() {
  if (shared) return shared

  const state = reactive({ ...blankState(), ...(readStored() || {}) })
  Object.assign(state, knownPlace(state.province, state.city))
  const refs = reactive([])

  watch(
    state,
    value => {
      try {
        window.localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
      } catch {
        // Private mode / quota — the survey still works, it just will not survive a reload
      }
    },
    { deep: true }
  )

  const trim = value => String(value ?? '').trim()
  const digits = value => String(value ?? '').replace(/\D/g, '')

  const stepKey = computed(() => SURVEY_FLOW[state.step])

  const interiorOn = computed(() => !!state.scopes[SCOPE_INTERIOR])
  const scopeOtherText = computed(() => (state.scopeOtherOn ? trim(state.scopeOther) : ''))
  const chosenScopes = computed(() => SURVEY_SCOPES.filter(scope => state.scopes[scope.value]))
  const scopeLabels = computed(() => [
    ...chosenScopes.value.map(scope => scope.label),
    ...(scopeOtherText.value ? [scopeOtherText.value] : [])
  ])

  const interiorOnly = computed(
    () =>
      interiorOn.value &&
      !chosenScopes.value.some(scope => scope.value !== SCOPE_INTERIOR) &&
      !scopeOtherText.value
  )

  const selectedAreas = computed(() => INTERIOR_AREAS.filter(area => state.intAreas[area.id]))
  const areaOtherText = computed(() => trim(state.intAreaOther))

  const interiorComplete = computed(() => {
    if (!interiorOn.value) return true
    if (!selectedAreas.value.length) return false
    return selectedAreas.value.every(area => {
      if (!area.opts) return area.id !== INTERIOR_OTHER || !!areaOtherText.value
      const answer = state.intScope[area.id]
      return !!answer && (answer !== 'Lainnya' || !!trim(state.intScopeOther[area.id]))
    })
  })

  // A single furniture piece is sized by wall length rather than by floor area
  const pieceMode = computed(
    () =>
      interiorOnly.value &&
      selectedAreas.value.some(area => area.piece && state.intScope[area.id] === area.piece)
  )

  const areaLine = area => {
    if (area.id === INTERIOR_OTHER) return areaOtherText.value || INTERIOR_OTHER
    if (!area.opts) return area.id
    const answer = state.intScope[area.id]
    const shown =
      answer === 'Lainnya' ? trim(state.intScopeOther[area.id]) || 'Lainnya' : answer || '—'
    return `${area.id} · ${shown}`
  }
  const areaSummary = computed(() => selectedAreas.value.map(areaLine))

  const areaNames = computed(() =>
    selectedAreas.value
      .map(area => {
        if (area.id === INTERIOR_OTHER) return (areaOtherText.value || 'area lainnya').toLowerCase()
        if (area.id === INTERIOR_WHOLE) return 'interior'
        return area.area
      })
      .join(', ')
  )

  const documentGroups = computed(() => {
    if (!interiorOnly.value) return ARCHITECTURE_DOCUMENTS
    const needsWater = selectedAreas.value.some(area => INTERIOR_WET_AREAS.includes(area.id))
    return INTERIOR_DOCUMENTS.map(group => ({
      ...group,
      items: group.items.filter(item => needsWater || !item.water)
    }))
  })
  // Selections are kept per list, so switching between architectural and interior-only does
  // not carry ticks across lists whose items mean different things
  const docPrefix = computed(() => (interiorOnly.value ? 'i:' : 'a:'))
  const docKey = item => docPrefix.value + item.code
  const allDocItems = computed(() => documentGroups.value.flatMap(group => group.items))
  const selectedDocs = computed(() => allDocItems.value.filter(item => state.docs[docKey(item)]))

  const isApartment = computed(() => state.subCategory === APARTMENT_SUB_CATEGORY)

  const feeRange = computed(() => {
    const parts = String(state.designFee || '')
      .split(/[–-]/)
      .map(digits)
      .filter(Boolean)
      .map(Number)
    if (!parts.length) return { min: null, max: null }
    return { min: parts[0], max: parts[1] ?? parts[0] }
  })

  const services = computed(() =>
    SURVEY_SERVICES.filter(service => !service.soon && state.services[service.id])
  )

  const valid = computed(() => ({
    lineup: services.value.length > 0,
    scope: scopeLabels.value.length > 0 && interiorComplete.value,
    property: !!(state.category && state.subCategory),
    akun: true,
    alamat: !!(state.province && state.city),
    luas: interiorOnly.value
      ? !isApartment.value || !!state.aptType
      : !!(
          digits(state.land) &&
          digits(state.building) &&
          (digits(state.floors) || state.floorsUnknown) &&
          (!isApartment.value || state.aptType)
        ),
    kebutuhan: !!trim(state.vision),
    deliv: selectedDocs.value.length > 0,
    fee: feeRange.value.min != null && feeRange.value.max >= feeRange.value.min,
    mulai: !!state.startType && (state.startType !== 'SPECIFIC_DATE' || !!state.startDate),
    review: true,
    done: true
  }))

  // The backend keeps one scope per project; the first official scope picked leads, and a
  // free-text-only answer ("alih fungsi gudang") is closest to a renovation
  const primaryScope = computed(() => chosenScopes.value[0]?.value || 'RENOVATION')

  const projectTitle = () => {
    if (state.presetTitle) return state.presetTitle
    const subject = subCategoryLabel(state.category, state.subCategory, 'id') || 'Proyek'
    const prefix = interiorOnly.value ? 'Desain Interior ' : ''
    const where = trim(state.city) ? ` di ${trim(state.city)}` : ''
    return `${prefix}${subject}${where}`.slice(0, 255)
  }

  // Answers the project entity has no column for yet travel in the requirements text, so the
  // architect still reads them. The labelled lines are also what hydrateFromProject() reads
  // back, so a draft resumed on another device keeps them.
  const requirementsText = () => {
    const lines = [trim(state.vision), '']
    lines.push(`${LINE.services} ${services.value.map(service => service.label).join(', ')}`)
    lines.push(`${LINE.scopes} ${scopeLabels.value.join(', ')}`)
    if (interiorOn.value) lines.push(`${LINE.areas} ${areaSummary.value.join('; ')}`)
    if (isApartment.value && state.aptType) lines.push(`${LINE.apartment} ${state.aptType}`)
    if (pieceMode.value && trim(state.wall)) lines.push(`${LINE.wall} ${trim(state.wall)} m`)
    if (!interiorOnly.value && state.floorsUnknown) lines.push(LINE.floorsUnknown)
    return lines.join('\n').trim()
  }

  const toInt = value => (digits(value) ? Number(digits(value)) : null)

  const buildProjectData = () => ({
    title: projectTitle(),
    city: state.city || null,
    province: state.province || null,
    fullAddress: trim(state.address) || null,
    latitude: state.latitude,
    longitude: state.longitude,
    designBudgetMin: feeRange.value.min,
    designBudgetMax: feeRange.value.max,
    buildingFunction: state.category,
    projectScope: primaryScope.value,
    subCategory: state.subCategory || null,
    lotSize: interiorOnly.value ? null : toInt(state.land),
    estimatedBuildArea: toInt(state.building),
    numberOfFloors: interiorOnly.value || state.floorsUnknown ? null : toInt(state.floors),
    scopeOfWork: requirementsText(),
    deliverables: selectedDocs.value.map(item => item.code),
    startDateType: state.startType || 'IMMEDIATELY',
    expectedStartDate: state.startType === 'SPECIFIC_DATE' ? state.startDate : null
  })

  // The anonymous record of what a visitor answered before signing in; see LandingBriefService
  const briefSnapshot = () => ({
    presetSlug: state.presetSlug || null,
    projectScope: primaryScope.value,
    buildingFunction: state.category || null,
    subCategory: state.subCategory || null,
    title: projectTitle().slice(0, 160)
  })

  const go = index => {
    state.step = Math.min(Math.max(index, 0), SURVEY_FLOW.length - 1)
    window.scrollTo(0, 0)
  }
  const goTo = key => go(SURVEY_FLOW.indexOf(key))

  /**
   * Refills the survey from a project saved in the account, for a draft resumed from the
   * dashboard on a browser that has no local copy. Lands on the first step still missing an
   * answer, or on the review when nothing is.
   */
  const hydrateFromProject = project => {
    const fresh = blankState()
    const { vision, meta, floorsUnknown } = parseRequirementsText(project.scopeOfWork)

    if (meta.services) {
      fresh.services = Object.fromEntries(
        SURVEY_SERVICES.filter(service => meta.services.includes(service.label)).map(service => [
          service.id,
          true
        ])
      )
    }

    const scopeNames = meta.scopes ? meta.scopes.split(', ').filter(Boolean) : []
    scopeNames.forEach(name => {
      const scope = SURVEY_SCOPES.find(s => s.label === name)
      if (scope) fresh.scopes[scope.value] = true
      else {
        fresh.scopeOtherOn = true
        fresh.scopeOther = name
      }
    })
    if (!scopeNames.length && project.projectScope) fresh.scopes[project.projectScope] = true

    if (meta.areas) {
      meta.areas.split('; ').forEach(entry => {
        const [areaId, answer] = entry.split(' · ')
        const area = INTERIOR_AREAS.find(a => a.id === areaId)
        if (!area) {
          fresh.intAreas[INTERIOR_OTHER] = true
          fresh.intAreaOther = entry
          return
        }
        fresh.intAreas[area.id] = true
        if (area.opts && answer) {
          if (area.opts.includes(answer)) fresh.intScope[area.id] = answer
          else {
            fresh.intScope[area.id] = 'Lainnya'
            fresh.intScopeOther[area.id] = answer
          }
        }
      })
    }

    const group = SURVEY_CATEGORY_GROUPS.find(g => g.categories.includes(project.buildingFunction))
    fresh.groupId = group?.id || ''
    fresh.category = group ? project.buildingFunction : ''
    fresh.subCategory = group ? project.subCategory || '' : ''
    fresh.aptType = meta.apartment || ''

    Object.assign(fresh, knownPlace(project.province, project.city))
    fresh.address = project.fullAddress || ''
    fresh.latitude = project.latitude != null ? Number(project.latitude) : null
    fresh.longitude = project.longitude != null ? Number(project.longitude) : null

    fresh.land = project.lotSize != null ? String(project.lotSize) : ''
    fresh.building = project.estimatedBuildArea != null ? String(project.estimatedBuildArea) : ''
    fresh.floors = project.numberOfFloors != null ? String(project.numberOfFloors) : ''
    fresh.floorsUnknown = floorsUnknown
    fresh.wall = meta.wall ? meta.wall.replace(/\s*m$/, '') : ''
    fresh.vision = vision

    const min = project.designBudgetMin
    const max = project.designBudgetMax
    if (min != null) {
      fresh.designFee =
        max != null && max !== min
          ? `${formatIDRDisplay(min)} – ${formatIDRDisplay(max)}`
          : formatIDRDisplay(min)
    }
    if (project.startDateType) fresh.startType = project.startDateType
    if (project.expectedStartDate) fresh.startDate = String(project.expectedStartDate).slice(0, 10)

    fresh.draftId = project.id
    refs.splice(0)
    Object.assign(state, fresh)

    // Ticks are keyed per document list, which depends on the scopes just restored
    const prefix = docPrefix.value
    state.docs = Object.fromEntries((project.deliverables || []).map(code => [prefix + code, true]))

    const resumeAt = SURVEY_FLOW.findIndex(
      (key, i) => i > SURVEY_FLOW.indexOf('akun') && !valid.value[key]
    )
    state.step = resumeAt >= 0 ? resumeAt : SURVEY_FLOW.indexOf('review')
  }

  const reset = () => {
    Object.assign(state, blankState())
    refs.splice(0)
    try {
      window.localStorage.removeItem(STORAGE_KEY)
    } catch {
      // Nothing stored to clear
    }
  }

  shared = {
    state,
    refs,
    stepKey,
    interiorOn,
    interiorOnly,
    interiorComplete,
    pieceMode,
    chosenScopes,
    scopeLabels,
    selectedAreas,
    areaSummary,
    areaNames,
    documentGroups,
    docKey,
    allDocItems,
    selectedDocs,
    isApartment,
    feeRange,
    services,
    valid,
    buildProjectData,
    briefSnapshot,
    hydrateFromProject,
    go,
    goTo,
    reset
  }
  return shared
}

// Lets post-auth redirects send a returning visitor back to an unfinished survey
export const hasPendingSurvey = () => {
  const stored = readStored()
  return !!stored && stored.step > 0 && SURVEY_FLOW[stored.step] !== 'done'
}
