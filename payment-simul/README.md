# payment-simul

A local simulator for the Xendit webhooks that drive Rumantra's money flows.

## Why this exists

Every payment in Rumantra only completes when **Xendit calls back into the backend**.
In local dev that callback never arrives — Xendit cannot reach `localhost:8080`. So you can
click "pay", get a real checkout page, pay it, and watch the app not move.

The usual workaround is patching the row in Postgres. Don't. Flipping a `status` column
skips everything the webhook handler actually does:

- the ledger transition through `StatusTransitionService` — and per `CLAUDE.md`, **the log
  is the source of truth and the `status` column is only a projection of it**, so a patched
  row is internally inconsistent
- bid token allocation and the `rmtr_bid_usage_log` audit row
- `PhaseProcessLog` rows, phase `dueDate`, phase auto-advance, project auto-close

This tool posts the real webhook to the real endpoint, so the real handler runs.

**No mocking is involved.** `XenditService.verifyWebhookToken` is a plain string comparison
against `xendit.webhook-token` — no HMAC, no signature, no call back out to Xendit. A
webhook carrying the right token is indistinguishable from a genuine one.

## Prerequisites

- Dev database up: `docker compose -f docker/dev-database.yml up -d`
- Backend running on `:8080`
- `backend/.env` present with `XENDIT_WEBHOOK_TOKEN` — read directly from there, so the
  simulator and the backend can never disagree about the token

## Run

```bash
cd payment-simul
npm install
npm start          # → http://localhost:3100
```

Overridable via env: `PORT`, `BACKEND_URL`, `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`,
`PGDATABASE`. Defaults match `docker/dev-database.yml`.

## The important caveat

**The simulator fires the callback — it does not create the payment.**

`XenditClient` hardcodes `https://api.xendit.co` with no sandbox toggle, so a row only
exists after the app has successfully called the real Xendit API. The workflow is always:

1. Do the real action in the app (buy tokens / bill a phase / request payout)
2. Refresh the simulator — the row appears
3. Click the outcome you want to test

If nothing shows up, the Xendit API call failed — check `backend.log`.

Seeding synthetic rows to skip step 1 is deliberately **not supported**: it would recreate
the exact "patched state isn't real state" problem this tool exists to solve.

## Flows covered

| Section | Where to trigger it | Reference id | Outcomes |
|---|---|---|---|
| Bid Token Purchases | Architect → buy tokens | `token_purchase_arch_{id}_{ms}` | Pay, Expire |
| Phase Payments — Workspace | `ProjectWorkspace.vue` → "Buat Invoice" | `proj_phase_{phaseId}_{ms}` | Pay, Expire |
| Phase Payments — Contract tab | `ProjectWorkspace.vue` → Contract & Payment → "Bayar Sekarang" | `phase_payment_proj_{p}_phase_{ph}_{ms}` | Pay, Expire |
| Disbursements | Architect → request payout on an APPROVED phase | `phase_payout_{phaseId}_{ms}` | Succeed, Fail, Reverse |

The two phase-payment rows are **separate backend paths** that share `rmtr_phase_payment`
and are told apart only by `project_phase_id IS NOT NULL`. They are listed separately
because paying one does not advance the other.

Subscription (`POST /rmtr/subscriptions/webhook`) is **out of scope** — not covered here.

## How it works

```
public/index.html  one page, no build step
server.js          GET /api/items · POST /api/simulate
lib/config.js      env + XENDIT_WEBHOOK_TOKEN read from backend/.env
lib/db.js          read-only discovery queries
lib/webhooks.js    payload builders + POST to the backend
```

Reference ids embed `System.currentTimeMillis()`, so they cannot be reconstructed from a
project or phase id — they can only be read back from the row. That is why this needs
database access and cannot be a static page or a fixed curl collection.

`lib/db.js` is **read-only by design.** Advancing state is the webhook handler's job.

Every response echoes the exact payload that was sent, so the tool doubles as live
documentation of the webhook contract.

### Buttons that are greyed out

The handlers are idempotent — firing `Pay` at an already-`COMPLETED` purchase logs
"already completed, skipping" and changes nothing. Rather than let that look like a broken
button, settled rows are disabled with a tooltip explaining why.

## Notes on the payloads

- `paid_at` is always sent, including on `EXPIRED`: `TokenPurchaseService` parses it with
  `ZonedDateTime.parse()` and no null guard, so omitting it produces a 500.
- The payout handler branches on `event` (`payout.succeeded` / `payout.failed` /
  `payout.reversed`) and **never reads `status`**.
- Payout lookup tries `xendit_payout_id` first, then `reference_id`, so the stored payout
  id is passed through when the row has one.
