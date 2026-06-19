import { readFileSync, existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'
import { getPhasePaymentReferenceId } from './db.js'

const __dirname = dirname(fileURLToPath(import.meta.url))

const readWebhookTokenFromBackendEnv = () => {
  const envFile = resolve(__dirname, '../../../backend/.env')
  if (!existsSync(envFile)) {
    throw new Error(`Cannot find backend/.env at ${envFile} to read XENDIT_WEBHOOK_TOKEN`)
  }
  for (const line of readFileSync(envFile, 'utf8').split('\n')) {
    const [key, ...vals] = line.split('=')
    if (key?.trim() === 'XENDIT_WEBHOOK_TOKEN') {
      return vals.join('=').trim()
    }
  }
  throw new Error('XENDIT_WEBHOOK_TOKEN not found in backend/.env')
}

const WEBHOOK_TOKEN = readWebhookTokenFromBackendEnv()

/**
 * XenditService.verifyWebhookToken() is a plain string comparison against this
 * locally-configured token (no real signature, no outbound call to Xendit), so
 * a synthetic webhook is enough to simulate a paid invoice in dev — no real
 * Xendit checkout needed.
 */
export const simulatePhasePaymentWebhook = async (request, apiBaseUrl, projectId, phaseNumber, amount) => {
  const externalId = getPhasePaymentReferenceId(projectId, phaseNumber)

  const response = await request.post(`${apiBaseUrl}/rmtr/xendit/webhook/invoice`, {
    headers: { 'X-CALLBACK-TOKEN': WEBHOOK_TOKEN },
    data: {
      id: `e2e_inv_${Date.now()}`,
      external_id: externalId,
      user_id: 'e2e_test',
      status: 'PAID',
      paid_amount: amount,
      paid_at: new Date().toISOString(),
      payment_channel: 'BCA',
      payment_method: 'BANK_TRANSFER',
      currency: 'IDR',
      description: `E2E simulated payment for project ${projectId} phase ${phaseNumber}`
    }
  })

  if (!response.ok()) {
    throw new Error(`Webhook simulation failed: ${response.status()} ${await response.text()}`)
  }
}
