import { test, expect } from '@playwright/test'
import { loginAsClient, loginAsArchitect } from './helpers/auth.js'
import { API_BASE_URL, TEST_USERS } from './helpers/fixtures.js'
import { querySql } from './helpers/db.js'

const SHOTS = process.env.E2E_SHOT_DIR

const expectedPath = (role, n) => {
  if (!n.projectId || !n.projectStatus || n.projectStatus === 'DELETED') return null
  const workspace = ['IN_PROGRESS', 'COMPLETED'].includes(n.projectStatus)
  if (role === 'client') {
    if (workspace) return `/client/projects/${n.projectId}/workspace`
    if (n.projectStatus === 'NEGOTIATION') return `/client/projects/${n.projectId}/finalization`
    return `/client/projects/${n.projectId}`
  }
  if (n.type === 'BID_REJECTED') return `/architect/opportunities/${n.projectId}`
  if (workspace) return `/architect/projects/${n.projectId}/workspace`
  if (n.projectStatus === 'NEGOTIATION') return `/architect/projects/${n.projectId}/finalization`
  return `/architect/opportunities/${n.projectId}`
}

for (const [role, login] of [
  ['client', loginAsClient],
  ['architect', loginAsArchitect]
]) {
  test.describe(`sidebar notifications (${role})`, () => {
    test('trigger lives in the sidebar and the panel opens beside it', async ({ page }) => {
      await page.setViewportSize({ width: 1280, height: 800 })
      await login(page)
      await page.goto(`/${role}/dashboard`)

      const sidebar = page.locator('aside')
      const trigger = sidebar.getByRole('button', { name: /Notifikasi|Notifications/ })
      await expect(trigger).toBeVisible()
      if (SHOTS) await page.screenshot({ path: `${SHOTS}/${role}-closed.png` })

      await trigger.click()
      const panel = page.getByRole('dialog')
      await expect(panel).toBeVisible()
      await expect(page.getByText(/Memuat notifikasi|Loading notifications/)).toBeHidden()
      if (SHOTS) await page.screenshot({ path: `${SHOTS}/${role}-open.png` })

      const sidebarBox = await sidebar.boundingBox()
      const panelBox = await panel.boundingBox()
      expect(panelBox.x).toBeGreaterThanOrEqual(sidebarBox.x + sidebarBox.width)
      expect(panelBox.y + panelBox.height).toBeLessThanOrEqual(800)

      await page.keyboard.press('Escape')
      await expect(panel).toBeHidden()
    })

    test('project notifications link to the right project page', async ({ page }) => {
      const token = await login(page)
      const response = await page.request.get(`${API_BASE_URL}/rmtr/notifications`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      const body = await response.json()
      const recent = (body.data ?? []).slice(0, 10)
      test.skip(recent.length === 0, `no notifications for the ${role} test account`)

      await page.goto(`/${role}/dashboard`)
      await page
        .locator('aside')
        .getByRole('button', { name: /Notifikasi|Notifications/ })
        .click()
      const panel = page.getByRole('dialog')
      await expect(page.getByText(/Memuat notifikasi|Loading notifications/)).toBeHidden()

      const rows = panel.locator('.overflow-y-auto > *')
      await expect(rows).toHaveCount(recent.length)
      for (let i = 0; i < recent.length; i++) {
        const path = expectedPath(role, recent[i])
        const row = rows.nth(i)
        if (path) {
          await expect(row).toHaveAttribute('href', path)
        } else {
          expect(await row.evaluate(el => el.tagName)).toBe('DIV')
        }
      }

      const firstLinked = recent.findIndex(n => expectedPath(role, n))
      test.skip(firstLinked < 0, 'no project-linked notifications to click')
      await rows.nth(firstLinked).click()
      await expect(page).toHaveURL(new RegExp(`${expectedPath(role, recent[firstLinked])}$`))
      await expect(panel).toBeHidden()
    })

    test('"Lihat semua notifikasi" opens the full page', async ({ page }) => {
      await login(page)
      await page.goto(`/${role}/dashboard`)
      const trigger = page
        .locator('aside')
        .getByRole('button', { name: /Notifikasi|Notifications/ })
      await trigger.click()
      await page
        .getByRole('link', { name: /Lihat semua notifikasi|View all notifications/ })
        .click()

      await expect(page).toHaveURL(new RegExp(`/${role}/notifications$`))
      await expect(page.getByRole('dialog')).toBeHidden()
      await expect(page.getByRole('heading', { level: 1 })).toHaveText(/Notifikasi|Notifications/)
      await expect(trigger).toHaveClass(/bg-white\/10/)

      await trigger.click()
      await expect(page.getByRole('dialog')).toBeHidden()
    })

    test('page: unread filter survives reload and row click lowers the badge', async ({ page }) => {
      const email = TEST_USERS[role].email
      querySql(
        `UPDATE rmtr_dashboard_notif SET is_read = false, read_at = NULL WHERE id = ` +
          `(SELECT n.id FROM rmtr_dashboard_notif n JOIN rmtr_user u ON u.id = n.user_id ` +
          `WHERE u.email = '${email}' ORDER BY n.created_at DESC, n.id DESC LIMIT 1)`
      )
      await login(page)
      await page.goto(`/${role}/notifications`)

      const unreadPill = page.getByRole('button', { name: /Belum dibaca|Unread/ })
      await unreadPill.click()
      await expect(page).toHaveURL(/filter=unread/)
      await page.reload()
      await expect(unreadPill).toHaveAttribute('aria-pressed', 'true')

      const badge = page
        .locator('aside')
        .getByRole('button', { name: /Notifikasi|Notifications/ })
        .locator('span.rounded-full')
      const before = Number(await badge.textContent())
      if (SHOTS) await page.screenshot({ path: `${SHOTS}/${role}-page-unread.png`, fullPage: true })

      const firstRow = page.locator('section .rounded-\\[20px\\] > *').first()
      await firstRow.click({ modifiers: ['ControlOrMeta'] })
      if (before > 1) {
        await expect(badge).toHaveText(String(before - 1))
      } else {
        await expect(badge).toBeHidden()
      }
    })

    test('page: load more appends the next page without duplicates', async ({ page }) => {
      const token = await login(page)
      const first = await page.request.get(`${API_BASE_URL}/rmtr/notifications?limit=20`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      const firstPage = (await first.json()).data
      test.skip(!firstPage.nextCursor, `the ${role} test account has 20 or fewer notifications`)

      await page.goto(`/${role}/notifications`)
      const rows = page.locator('section .rounded-\\[20px\\] > *')
      await expect(rows).toHaveCount(20)
      if (SHOTS) await page.screenshot({ path: `${SHOTS}/${role}-page.png` })

      await page.getByRole('button', { name: /Muat lebih banyak|Load more/ }).click()
      const expected = Math.min(40, firstPage.totalCount)
      await expect(rows).toHaveCount(expected)

      const second = await page.request.get(
        `${API_BASE_URL}/rmtr/notifications?limit=20&cursor=${firstPage.nextCursor}`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      const ids = [...firstPage.items, ...(await second.json()).data.items].map(n => n.id)
      expect(new Set(ids).size).toBe(ids.length)
    })
  })
}
