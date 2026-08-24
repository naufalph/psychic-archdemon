export const formatIDRDisplay = raw => {
  const digits = String(raw ?? '').replace(/[^0-9]/g, '')
  return digits ? parseInt(digits, 10).toLocaleString('id-ID') : ''
}

export const parseIDRInput = display => {
  const digits = String(display ?? '').replace(/[^0-9]/g, '')
  return digits ? parseInt(digits, 10) : null
}
