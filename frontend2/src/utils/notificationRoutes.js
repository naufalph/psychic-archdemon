import {
  AlertCircle,
  Bell,
  CheckCircle2,
  CircleDollarSign,
  FileText,
  PencilLine
} from 'lucide-vue-next'

const WORKSPACE_STATUSES = ['IN_PROGRESS', 'COMPLETED']

// Rejections are sent as PROJECT_VALIDATED (there is no separate type), so the project's
// current status is what tells them apart
export const isRejectedProject = notification =>
  notification.type === 'PROJECT_VALIDATED' && notification.projectStatus === 'REJECTED'

const ICONS = {
  PROJECT_VALIDATED: CheckCircle2,
  BID_ACCEPTED: CheckCircle2,
  BID_RECEIVED: FileText,
  PROJECT_UPDATED: PencilLine,
  PROJECT_REJECTED: PencilLine,
  REVISION_REQUESTED: PencilLine,
  BID_REJECTED: AlertCircle
}

export const getNotificationIcon = notification => {
  if (isRejectedProject(notification)) return PencilLine
  if (notification.type?.startsWith('PAYMENT_')) return CircleDollarSign
  return ICONS[notification.type] || Bell
}

const getClientRoute = (projectId, status) => {
  if (WORKSPACE_STATUSES.includes(status))
    return { name: 'ClientProjectWorkspace', params: { id: projectId } }
  if (status === 'NEGOTIATION') return { name: 'PreProjectFinalization', params: { projectId } }
  return { name: 'ProjectDetail', params: { id: projectId } }
}

const getArchitectRoute = (notification, projectId, status) => {
  // A rejected bidder still gets the project page, which shows their bid; the workspace and
  // finalization belong to the winning architect only
  if (notification.type === 'BID_REJECTED')
    return { name: 'ProjectDetailForArchitect', params: { projectId } }
  if (WORKSPACE_STATUSES.includes(status))
    return { name: 'ArchitectProjectWorkspace', params: { id: projectId } }
  if (status === 'NEGOTIATION') return { name: 'ArchitectFinalizationView', params: { projectId } }
  return { name: 'ProjectDetailForArchitect', params: { projectId } }
}

export const getNotificationRoute = (notification, role) => {
  const { projectId, projectStatus } = notification
  if (!projectId || !projectStatus || projectStatus === 'DELETED') return null
  return role === 'architect'
    ? getArchitectRoute(notification, projectId, projectStatus)
    : getClientRoute(projectId, projectStatus)
}

export const getNotificationCtaKey = (notification, route) => {
  if (!route) return null
  if (isRejectedProject(notification)) return 'completeBrief'
  if (['ClientProjectWorkspace', 'ArchitectProjectWorkspace'].includes(route.name))
    return 'openWorkspace'
  if (['PreProjectFinalization', 'ArchitectFinalizationView'].includes(route.name))
    return 'continueFinalization'
  if (notification.type === 'PROJECT_VALIDATED') return 'openProject'
  return 'viewBids'
}

export const notificationsRouteName = role =>
  role === 'architect' ? 'ArchitectNotifications' : 'ClientNotifications'
