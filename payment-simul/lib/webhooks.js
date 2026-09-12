import { config } from './config.js'

const INVOICE_PATH = '/rmtr/xendit/webhook/invoice'
const PAYOUT_PATH = '/rmtr/xendit/webhook/payout'

/**
 * paid_at is mandatory even on EXPIRED: TokenPurchaseService parses it with
 * ZonedDateTime.parse() and no null guard, so omitting it turns a simulated
 * webhook into a 500 that looks like a backend bug.
 */
const invoicePayload = (item, outcome) => ({
  id: `simul_inv_${Date.now()}`,
  external_id: item.xendit_reference_id,
  user_id: 'payment-simul',
  status: outcome === 'expire' ? 'EXPIRED' : 'PAID',
  paid_amount: Number(item.amount ?? item.total_amount ?? 0),
  paid_at: new Date().toISOString(),
  payment_channel: 'BCA',
  payment_method: 'BANK_TRANSFER',
  currency: 'IDR',
  description: `Simulated by payment-simul for ${item.xendit_reference_id}`
})

const PAYOUT_EVENTS = {
  succeed: 'payout.succeeded',
  fail: 'payout.failed',
  reverse: 'payout.reversed'
}

/**
 * handlePayoutCallback looks up by xendit_payout_id first and only then falls
 * back to reference_id, so the stored payout id is passed through when the row
 * has one; a synthetic id would otherwise match nothing and the real reference
 * would never be tried.
 */
const payoutPayload = (item, outcome) => ({
  event: PAYOUT_EVENTS[outcome],
  id: item.xendit_payout_id || `simul_payout_${Date.now()}`,
  reference_id: item.xendit_reference_id,
  status: outcome === 'succeed' ? 'SUCCEEDED' : 'FAILED',
  channel_code: 'ID_BCA',
  failure_code: outcome === 'succeed' ? null : 'INSUFFICIENT_BALANCE',
  amount: Number(item.amount ?? 0),
  updated: new Date().toISOString()
})

export const buildPayload = (flow, item, outcome) =>
  flow === 'disbursement' ? payoutPayload(item, outcome) : invoicePayload(item, outcome)

export const pathFor = flow => (flow === 'disbursement' ? PAYOUT_PATH : INVOICE_PATH)

export const fire = async (flow, item, outcome) => {
  const payload = buildPayload(flow, item, outcome)
  const url = `${config.backendUrl}${pathFor(flow)}`

  let response
  try {
    response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-CALLBACK-TOKEN': config.webhookToken
      },
      body: JSON.stringify(payload)
    })
  } catch (err) {
    throw new Error(`Could not reach the backend at ${url}. Is it running? ${err.message}`)
  }

  const body = await response.text()
  return { ok: response.ok, httpStatus: response.status, url, payloadSent: payload, body }
}
