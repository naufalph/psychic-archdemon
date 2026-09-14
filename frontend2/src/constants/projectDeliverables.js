export const DELIVERABLE_GROUPS = [
  { categoryKey: 'preliminaryPlanning', items: ['SITE_ANALYSIS', 'ZONING_STUDY'] },
  {
    categoryKey: 'architecturalDesign',
    items: [
      'SITE_BLOCK_PLAN',
      'FLOOR_PLAN',
      'ELEVATION_SECTION',
      'DETAIL_DRAWINGS',
      'RENDER_3D_EXTERIOR',
      'RENDER_3D_INTERIOR',
      'RENDER_3D_VIDEO',
      'MATERIAL_FINISHING_SPEC'
    ]
  },
  {
    categoryKey: 'technicalDesign',
    items: [
      'STRUCTURAL_DRAWINGS',
      'MECHANICAL_DRAWINGS',
      'ELECTRICAL_DRAWINGS',
      'PLUMBING_DRAWINGS',
      'FIRE_PROTECTION_DRAWINGS',
      'STRUCTURAL_CALCULATION',
      'MEP_CALCULATION'
    ]
  },
  { categoryKey: 'interiorLandscape', items: ['INTERIOR_DESIGN', 'LANDSCAPE_DESIGN'] },
  { categoryKey: 'calculationEstimation', items: ['COST_ESTIMATION'] }
]

export const DELIVERABLE_VALUES = DELIVERABLE_GROUPS.flatMap(group => group.items)

/**
 * Codes split into finer items by the 2026 deliverable revision. Projects, bids and contracts
 * made before it still store them, so they keep their locale labels but are no longer offered.
 */
export const LEGACY_DELIVERABLES = [
  'ARCHITECTURAL_DRAWINGS',
  'DESIGN_VISUALIZATION_3D',
  'MEP_DRAWINGS'
]

export const OTHER_DELIVERABLES_KEY = 'other'

/**
 * Buckets a stored deliverable list into the display groups. Anything not in a current group
 * (legacy codes, free text) lands in a trailing "other" group instead of silently vanishing.
 */
export const groupDeliverables = values => {
  const selected = values || []
  const groups = DELIVERABLE_GROUPS.map(group => ({
    categoryKey: group.categoryKey,
    items: group.items.filter(item => selected.includes(item))
  }))
  const other = selected.filter(value => !DELIVERABLE_VALUES.includes(value))
  if (other.length > 0) groups.push({ categoryKey: OTHER_DELIVERABLES_KEY, items: other })
  return groups.filter(group => group.items.length > 0)
}
