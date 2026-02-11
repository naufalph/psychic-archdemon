/**
 * Compose notification title and message from messageCode and messageData
 * Falls back to notification.title and notification.message if messageCode is not found
 *
 * @param {Object} notification - Notification object from backend
 * @param {Object} t - i18n translation object
 * @returns {Object} { title: string, message: string }
 */
export function composeNotificationMessage(notification, t) {
  if (!notification.messageCode || !t.notifications?.messages?.[notification.messageCode]) {
    return {
      title: notification.title || 'Notification',
      message: notification.message || ''
    }
  }

  const template = t.notifications.messages[notification.messageCode]
  let messageData = {}

  if (notification.messageData) {
    try {
      messageData = JSON.parse(notification.messageData)
    } catch (error) {
      console.error('Failed to parse notification messageData:', error)
      return {
        title: notification.title || template.title,
        message: notification.message || template.message
      }
    }
  }

  const composedTitle = replacePlaceholders(template.title, messageData)
  const composedMessage = replacePlaceholders(template.message, messageData)

  return {
    title: composedTitle,
    message: composedMessage
  }
}

/**
 * Replace {placeholder} patterns in string with values from data object
 *
 * @param {string} template - String with {placeholder} patterns
 * @param {Object} data - Data object with values
 * @returns {string} String with placeholders replaced
 */
function replacePlaceholders(template, data) {
  if (!template) return ''

  return template.replace(/\{(\w+)\}/g, (match, key) => {
    return data[key] !== undefined ? data[key] : match
  })
}

/**
 * Format currency amount (Indonesian Rupiah)
 *
 * @param {number|string} amount - Amount to format
 * @returns {string} Formatted amount (e.g., "50.000.000")
 */
export function formatCurrency(amount) {
  if (!amount) return '0'
  const num = typeof amount === 'string' ? parseInt(amount) : amount
  return num.toLocaleString('id-ID')
}

/**
 * Get relative time string for notification
 *
 * @param {string} timestamp - ISO timestamp
 * @param {Object} t - i18n translation object
 * @returns {string} Relative time string
 */
export function getRelativeTime(timestamp, t) {
  const now = new Date()
  const time = new Date(timestamp)
  const diffMs = now - time
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) {
    return t.notifications.justNow
  } else if (diffMins < 60) {
    return t.notifications.minutesAgo.replace('{n}', diffMins)
  } else if (diffHours < 24) {
    return t.notifications.hoursAgo.replace('{n}', diffHours)
  } else {
    return t.notifications.daysAgo.replace('{n}', diffDays)
  }
}
