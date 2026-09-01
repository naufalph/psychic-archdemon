/**
 * Status presentation for the project workspace, shared by both role views.
 * Previously duplicated verbatim in client/ and architect/ ProjectWorkspace.vue.
 */
export const statusStyles = {
  NOT_STARTED: { bg: 'bg-gray-100', text: 'text-gray-500', dot: 'bg-gray-400', icon: 'bg-gray-100 text-gray-400' },
  PENDING: { bg: 'bg-amber-50', text: 'text-amber-700', dot: 'bg-amber-500', icon: 'bg-amber-100 text-amber-700' },
  BILLED: { bg: 'bg-blue-50', text: 'text-blue-700', dot: 'bg-blue-500', icon: 'bg-blue-100 text-blue-700' },
  IN_PROGRESS: { bg: 'bg-sky-50', text: 'text-sky-700', dot: 'bg-sky-500', icon: 'bg-sky-100 text-sky-700' },
  DELIVERED: { bg: 'bg-purple-50', text: 'text-purple-700', dot: 'bg-purple-500', icon: 'bg-purple-100 text-purple-700' },
  APPROVED: { bg: 'bg-green-50', text: 'text-green-700', dot: 'bg-green-500', icon: 'bg-green-100 text-green-700' },
  DISBURSED: { bg: 'bg-gray-50', text: 'text-gray-500', dot: 'bg-gray-400', icon: 'bg-gray-100 text-gray-400' },
  DISPUTED: { bg: 'bg-red-50', text: 'text-red-700', dot: 'bg-red-500', icon: 'bg-red-100 text-red-700' }
}

/** Per-deliverable status, distinct from phase status. */
export const deliverableStyles = {
  APPROVED: { text: 'text-green-700', dot: 'bg-green-500' },
  PENDING: { text: 'text-purple-700', dot: 'bg-purple-500' },
  MISSING: { text: 'text-red-700', dot: 'bg-red-500' },
  LOCKED: { text: 'text-gray-400', dot: 'bg-gray-300' }
}

export const logIconClass = actorType =>
  ({
    CLIENT: 'bg-ink-700 text-white',
    ARCHITECT: 'bg-brand-tan text-brand-brown',
    SYSTEM: 'bg-gray-100 text-gray-500',
    XENDIT: 'bg-green-100 text-green-700',
    SUPERUSER: 'bg-gray-100 text-gray-500'
  })[actorType] || 'bg-gray-100 text-gray-500'

export const isImage = fileType => fileType?.startsWith('image/')
export const isPdf = fileType => fileType === 'application/pdf'
export const fileNameFromPath = path => path?.split('/').pop()?.split('?')[0] || path
export const fileExtension = path => {
  const name = fileNameFromPath(path) || ''
  const dot = name.lastIndexOf('.')
  return dot > -1 ? name.slice(dot + 1).toUpperCase().slice(0, 4) : 'FILE'
}
