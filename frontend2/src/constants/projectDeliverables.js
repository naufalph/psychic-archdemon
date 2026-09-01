export const DELIVERABLE_GROUPS = [
  { categoryKey: 'preliminaryPlanning', items: ['SITE_ANALYSIS', 'ZONING_STUDY'] },
  {
    categoryKey: 'architecturalDesign',
    items: ['ARCHITECTURAL_DRAWINGS', 'DESIGN_VISUALIZATION_3D', 'MATERIAL_FINISHING_SPEC']
  },
  {
    categoryKey: 'technicalDesign',
    items: ['STRUCTURAL_DRAWINGS', 'MEP_DRAWINGS', 'FIRE_PROTECTION_DRAWINGS']
  },
  { categoryKey: 'interiorLandscape', items: ['INTERIOR_DESIGN', 'LANDSCAPE_DESIGN'] },
  {
    categoryKey: 'calculationEstimation',
    items: ['STRUCTURAL_CALCULATION', 'MEP_CALCULATION', 'COST_ESTIMATION']
  }
]

export const DELIVERABLE_VALUES = DELIVERABLE_GROUPS.flatMap(group => group.items)
