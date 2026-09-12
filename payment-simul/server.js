import { createServer } from 'node:http'
import { readFile } from 'node:fs/promises'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

import { config } from './lib/config.js'
import {
  listTokenPurchases,
  listWorkspacePhasePayments,
  listDashboardPhasePayments,
  listDisbursements
} from './lib/db.js'
import { fire } from './lib/webhooks.js'

const __dirname = dirname(fileURLToPath(import.meta.url))

const FLOWS = {
  tokenPurchase: { list: listTokenPurchases, outcomes: ['pay', 'expire'] },
  workspacePhase: { list: listWorkspacePhasePayments, outcomes: ['pay', 'expire'] },
  dashboardPhase: { list: listDashboardPhasePayments, outcomes: ['pay', 'expire'] },
  disbursement: { list: listDisbursements, outcomes: ['succeed', 'fail', 'reverse'] }
}

const json = (res, status, body) => {
  const payload = JSON.stringify(body)
  res.writeHead(status, {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(payload)
  })
  res.end(payload)
}

const readBody = req =>
  new Promise((resolvePromise, reject) => {
    const chunks = []
    req.on('data', c => chunks.push(c))
    req.on('end', () => {
      try {
        resolvePromise(JSON.parse(Buffer.concat(chunks).toString() || '{}'))
      } catch (err) {
        reject(new Error(`Malformed JSON body: ${err.message}`))
      }
    })
    req.on('error', reject)
  })

const handleItems = async res => {
  const entries = await Promise.all(
    Object.entries(FLOWS).map(async ([name, flow]) => [name, await flow.list()])
  )
  json(res, 200, {
    meta: {
      backendUrl: config.backendUrl,
      database: `${config.db.host}:${config.db.port}/${config.db.database}`,
      tokenLoaded: Boolean(config.webhookToken)
    },
    items: Object.fromEntries(entries)
  })
}

const handleSimulate = async (req, res) => {
  const { flow, referenceId, outcome } = await readBody(req)

  const definition = FLOWS[flow]
  if (!definition) return json(res, 400, { error: `Unknown flow: ${flow}` })
  if (!definition.outcomes.includes(outcome)) {
    return json(res, 400, {
      error: `Outcome "${outcome}" is not valid for ${flow} (expected ${definition.outcomes.join(', ')})`
    })
  }

  const item = (await definition.list()).find(r => r.xendit_reference_id === referenceId)
  if (!item) {
    return json(res, 404, {
      error: `No ${flow} row found with reference id ${referenceId}. It may have been removed — refresh the list.`
    })
  }

  json(res, 200, await fire(flow, item, outcome))
}

const server = createServer(async (req, res) => {
  try {
    if (req.method === 'GET' && req.url === '/api/items') return await handleItems(res)
    if (req.method === 'POST' && req.url === '/api/simulate') return await handleSimulate(req, res)
    if (req.method === 'GET' && (req.url === '/' || req.url === '/index.html')) {
      const html = await readFile(resolve(__dirname, 'public/index.html'))
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' })
      return res.end(html)
    }
    json(res, 404, { error: 'Not found' })
  } catch (err) {
    json(res, 500, { error: err.message })
  }
})

server.listen(config.port, () => {
  console.log(`payment-simul  →  http://localhost:${config.port}`)
  console.log(`  backend      →  ${config.backendUrl}`)
  console.log(`  database     →  ${config.db.host}:${config.db.port}/${config.db.database}`)
  console.log(`  webhook token loaded from backend/.env`)
})
