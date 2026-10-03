const DAY = 86400000
const DONE_STATUSES = ['APPROVED', 'DISBURSED']

/**
 * Calendar day of a backend value. A bare LocalDate ("2026-10-31") is read as local midnight --
 * `new Date()` would read it as UTC and put it on the previous day west of Greenwich.
 */
export const toDay = value => {
  if (!value) return null
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
    const [y, m, d] = value.split('-').map(Number)
    return new Date(y, m - 1, d)
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return null
  date.setHours(0, 0, 0, 0)
  return date
}

export const daysBetween = (a, b) => Math.round((b - a) / DAY)
export const addDays = (date, n) => new Date(date.getTime() + n * DAY)
const latest = (...dates) => new Date(Math.max(...dates.filter(Boolean).map(d => d.getTime())))

/** The bid's duration for a phase; legacy phases without one fall back to their funded window. */
export const estimatedDaysOf = phase => {
  if (phase.estimatedDays != null) return phase.estimatedDays
  const start = toDay(phase.startedAt)
  const due = toDay(phase.dueDate)
  return start && due ? Math.max(0, daysBetween(start, due)) : 0
}

/**
 * Lays the bid's promise next to what happened. The target lane strings the bid's durations end to
 * end from contract start; the actual lane runs from each phase's start to its sign-off, and any
 * phase still open is forecast forward from today -- so a late phase pushes every later one.
 */
export function buildSchedule(sortedPhases, contractStart, today = toDay(new Date())) {
  const start = toDay(contractStart) || today
  let targetCursor = start
  let actualCursor = start
  let workDelta = 0
  let submittedCount = 0

  const rows = sortedPhases.map(phase => {
    const est = estimatedDaysOf(phase)
    const startedAt = toDay(phase.startedAt)
    const deliveredAt = toDay(phase.deliveredAt)
    const isDone = DONE_STATUSES.includes(phase.status)

    const targetStart = targetCursor
    const targetEnd = addDays(targetStart, est)
    targetCursor = targetEnd

    const actualStart = startedAt || latest(actualCursor, today)
    const plannedEnd = toDay(phase.dueDate) || addDays(actualStart, est)
    const signedOffAt = toDay(phase.approvedAt) || (isDone ? deliveredAt || plannedEnd : null)
    const actualEnd =
      signedOffAt ||
      (deliveredAt
        ? latest(deliveredAt, today)
        : startedAt
          ? latest(plannedEnd, today)
          : plannedEnd)
    actualCursor = actualEnd

    // Speed is judged on the architect's working time only -- the client's review days are theirs.
    const workDays = startedAt && deliveredAt ? daysBetween(startedAt, deliveredAt) : null
    const delta = workDays != null && est > 0 ? workDays - est : null
    if (delta != null) {
      workDelta += delta
      submittedCount++
    }

    let chip = null
    if (delta != null)
      chip = {
        kind: delta < 0 ? 'faster' : delta === 0 ? 'onSchedule' : 'slower',
        days: Math.abs(delta)
      }
    else if (startedAt && !deliveredAt && !signedOffAt && est > 0)
      chip = { kind: 'dayOf', day: daysBetween(startedAt, today) + 1, of: est }

    const state = signedOffAt
      ? 'done'
      : deliveredAt
        ? 'delivered'
        : startedAt
          ? 'active'
          : 'upcoming'

    return {
      phaseId: phase.id,
      est,
      state,
      targetStart,
      targetEnd,
      actualStart,
      actualEnd,
      deliveredAt,
      workDays,
      chip
    }
  })

  return {
    start,
    today,
    rows,
    byPhaseId: Object.fromEntries(rows.map(r => [r.phaseId, r])),
    targetFinish: targetCursor,
    forecastFinish: actualCursor,
    // Once every phase is signed off the last end date is history, not a forecast.
    finished: rows.length > 0 && rows.every(r => r.state === 'done'),
    workDelta,
    submittedCount
  }
}

export const chipClasses = {
  faster: 'bg-green-50 text-green-700',
  onSchedule: 'bg-gray-100 text-gray-600',
  slower: 'bg-amber-50 text-amber-700',
  dayOf: 'bg-sky-50 text-sky-700'
}

export const chipText = (chip, w) => {
  if (!chip) return ''
  if (chip.kind === 'faster') return (w.chipFaster || '{d}d faster').replace('{d}', chip.days)
  if (chip.kind === 'slower') return (w.chipSlower || '{d}d slower').replace('{d}', chip.days)
  if (chip.kind === 'onSchedule') return w.chipOnSchedule || 'On schedule'
  return (w.chipDayOf || 'Day {n} of {total}').replace('{n}', chip.day).replace('{total}', chip.of)
}

/** Day and short month, e.g. "3 Oct" -- the timeline never needs the year. */
export const formatDayMonth = (date, locale) =>
  date.toLocaleDateString(locale === 'id' ? 'id-ID' : 'en-US', { day: 'numeric', month: 'short' })

export const formatRange = (a, b, locale) =>
  `${formatDayMonth(a, locale)} – ${formatDayMonth(b, locale)}`
