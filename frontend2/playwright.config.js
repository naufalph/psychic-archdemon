import { defineConfig, devices } from '@playwright/test'
import { readFileSync, existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const envFile = resolve(__dirname, 'e2e/.env.test.local')
if (existsSync(envFile)) {
  for (const line of readFileSync(envFile, 'utf8').split('\n')) {
    const [key, ...vals] = line.split('=')
    if (key?.trim() && !key.startsWith('#')) {
      process.env[key.trim()] ??= vals.join('=').trim()
    }
  }
}

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: 'html',

  use: {
    baseURL: 'http://localhost:3001',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 15000,
    viewport: null,
    launchOptions: {
      args: ['--start-maximized']
    }
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'], viewport: null, deviceScaleFactor: undefined }
    }
  ],

  webServer: [
    {
      command: 'npm run dev',
      url: 'http://localhost:3001',
      reuseExistingServer: !process.env.CI,
      timeout: 120 * 1000
    }
  ],

  expect: {
    timeout: 15000
  },

  timeout: 90000
})
