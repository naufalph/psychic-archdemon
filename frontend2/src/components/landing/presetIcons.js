import {
  Home,
  Building,
  Building2,
  Palmtree,
  Briefcase,
  Hammer,
  Store,
  Hotel,
  Warehouse,
  Factory,
  School,
  Church,
  TreePine,
  Landmark
} from 'lucide-vue-next'

/**
 * Presets are admin-authored, so the stored icon name is resolved against this allowlist
 * rather than rendered straight from the database value.
 */
export const PRESET_ICONS = {
  Home,
  Building,
  Building2,
  Palmtree,
  Briefcase,
  Hammer,
  Store,
  Hotel,
  Warehouse,
  Factory,
  School,
  Church,
  TreePine,
  Landmark
}

export const PRESET_ICON_NAMES = Object.keys(PRESET_ICONS)

export function resolvePresetIcon(iconName) {
  return PRESET_ICONS[iconName] || Home
}
