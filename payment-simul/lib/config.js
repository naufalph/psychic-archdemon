import { readFileSync, existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))

const BACKEND_ENV = resolve(__dirname, '../../backend/.env')

/**
 * The webhook token is read from backend/.env rather than duplicated into a
 * config of our own: the backend compares against whatever it booted with, so a
 * second copy would silently drift and every webhook would 403.
 */
const readWebhookToken = () => {
  if (!existsSync(BACKEND_ENV)) {
    throw new Error(
      `Cannot find ${BACKEND_ENV}. payment-simul reads XENDIT_WEBHOOK_TOKEN from the ` +
        `backend's own .env so the two can never disagree. Create it (see backend/.env.example) first.`
    )
  }
  for (const line of readFileSync(BACKEND_ENV, 'utf8').split('\n')) {
    if (line.trim().startsWith('#')) continue
    const [key, ...vals] = line.split('=')
    if (key?.trim() === 'XENDIT_WEBHOOK_TOKEN') {
      const token = vals.join('=').trim()
      if (token) return token
    }
  }
  throw new Error(`XENDIT_WEBHOOK_TOKEN is not set in ${BACKEND_ENV}`)
}

export const config = {
  port: Number(process.env.PORT || 3100),
  backendUrl: (process.env.BACKEND_URL || 'http://localhost:8080').replace(/\/$/, ''),
  webhookToken: readWebhookToken(),
  db: {
    host: process.env.PGHOST || 'localhost',
    port: Number(process.env.PGPORT || 5432),
    user: process.env.PGUSER || 'postgres',
    password: process.env.PGPASSWORD || 'password',
    database: process.env.PGDATABASE || 'rumantra-db'
  }
}
