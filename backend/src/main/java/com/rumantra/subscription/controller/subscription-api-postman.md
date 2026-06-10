# Subscription Payment API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (architect)
- Requires `ROLE_ARCHITECT`
- Xendit test API key configured in environment variables

**Base URL:** `http://localhost:8080/rmtr/subscriptions`

---

## Subscription Flow

```
1. GET /rmtr/subscriptions/status       → Check current subscription
2. POST /rmtr/subscriptions/upgrade     → Initiate upgrade to BASIC (returns Xendit link)
3. [User completes payment at Xendit checkout]
4. POST /rmtr/subscriptions/webhook     → Xendit sends recurring.payment.succeeded callback
5. Backend allocates 10 bid tokens, activates BASIC subscription
6. [After 365 days]: Xendit auto-charges, webhook fires again → 10 more tokens
```

**Tiers:**
- `FREE`: IDR 0/year — 1 bid token per year
- `BASIC`: IDR 1,500,000/year — 10 bid tokens per year (tokens accumulate, unused tokens carry over)

---

## API Endpoints

---

### 1. Upgrade to BASIC

**POST** `/rmtr/subscriptions/upgrade`

Initiates upgrade from FREE to BASIC. Creates a Xendit recurring payment plan and returns a checkout link.

```bash
curl --location 'http://localhost:8080/rmtr/subscriptions/upgrade' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**No request body required.**

**Expected Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "paymentLink": "https://checkout.xendit.co/v2/plan/xyz123",
    "mobilePaymentLink": "https://checkout.xendit.co/mobile/plan/xyz123",
    "status": "PENDING"
  },
  "timestamp": "2026-06-01T10:00:00"
}
```

**SubscriptionUpgradeResponse fields:**
| Field | Type |
|-------|------|
| `paymentLink` | String |
| `mobilePaymentLink` | String |
| `status` | String |

---

### 2. Get Subscription Status

**GET** `/rmtr/subscriptions/status`

Returns the current subscription entity for the authenticated architect.

```bash
curl --location 'http://localhost:8080/rmtr/subscriptions/status' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "tier": "BASIC",
    "status": "ACTIVE",
    "startDate": "2026-06-01",
    "endDate": "2027-06-01",
    "yearlyPrice": 1500000,
    "xenditPlanId": "plan_abc123",
    "xenditReferenceId": "ref_xyz789",
    "xenditCycleId": "cycle_001",
    "paymentLink": "https://checkout.xendit.co/v2/plan/xyz123",
    "lastPaymentDate": "2026-06-01T10:05:00",
    "nextBillingDate": "2027-06-01",
    "isActive": true,
    "createdAt": "2026-06-01T10:00:00",
    "updatedAt": "2026-06-01T10:05:00"
  },
  "timestamp": "2026-06-01T10:10:00"
}
```

**Subscription status values:** `PENDING`, `ACTIVE`, `CANCELLED`, `EXPIRED`, `FAILED`

---

### 3. Cancel Subscription

**POST** `/rmtr/subscriptions/cancel`

Cancels the active subscription. Benefits continue until the `endDate` (no immediate loss of tokens).

```bash
curl --location 'http://localhost:8080/rmtr/subscriptions/cancel' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "timestamp": "2026-06-01T10:00:00"
}
```

**Business Logic:**
- Subscription status changes to `CANCELLED`
- Subscription benefits remain active until `endDate`
- No auto-renewal after cancellation

---

## Webhook — Xendit Recurring Payment

**POST** `/rmtr/subscriptions/webhook`

Receives Xendit recurring payment callbacks. **Public endpoint** secured by `x-callback-token` header.

Configure this URL in your Xendit dashboard under recurring payment settings.

```bash
# Simulate ACTIVE (payment succeeded)
curl --location 'http://localhost:8080/rmtr/subscriptions/webhook' \
--header 'x-callback-token: your-xendit-callback-token' \
--header 'Content-Type: application/json' \
--data '{
  "event": "recurring.payment.succeeded",
  "data": {
    "id": "plan_abc123",
    "status": "ACTIVE",
    "reference_id": "ref_xyz789"
  }
}'

# Simulate FAILED
curl --location 'http://localhost:8080/rmtr/subscriptions/webhook' \
--header 'x-callback-token: your-xendit-callback-token' \
--header 'Content-Type: application/json' \
--data '{
  "event": "recurring.payment.failed",
  "data": {
    "id": "plan_abc123",
    "status": "FAILED"
  }
}'

# Simulate STOPPED (cancelled)
curl --location 'http://localhost:8080/rmtr/subscriptions/webhook' \
--header 'x-callback-token: your-xendit-callback-token' \
--header 'Content-Type: application/json' \
--data '{
  "event": "recurring.plan.stopped",
  "data": {
    "id": "plan_abc123",
    "status": "STOPPED"
  }
}'
```

**Handled event statuses:**
| Status | Action |
|--------|--------|
| `ACTIVE` | Activate subscription, allocate 10 bid tokens |
| `FAILED` | Mark subscription as FAILED |
| `STOPPED` | Mark subscription as CANCELLED |

**Response:** `200 OK` (no body) on success, `403 Forbidden` if callback token is invalid.

---

## DTO Reference

### SubscriptionUpgradeResponse

| Field | Type |
|-------|------|
| `paymentLink` | String |
| `mobilePaymentLink` | String |
| `status` | String |

### Subscription (domain entity, returned by GET /status)

| Field | Type |
|-------|------|
| `id` | Long |
| `tier` | SubscriptionTier |
| `status` | SubscriptionStatus |
| `startDate` | LocalDate |
| `endDate` | LocalDate |
| `yearlyPrice` | BigDecimal |
| `xenditPlanId` | String |
| `xenditReferenceId` | String |
| `xenditCycleId` | String |
| `paymentLink` | String |
| `lastPaymentDate` | LocalDateTime |
| `nextBillingDate` | LocalDate |
| `isActive` | Boolean |
| `createdAt` | LocalDateTime |
| `updatedAt` | LocalDateTime |

---

## Enums

| Enum | Values |
|------|--------|
| `SubscriptionTier` | `FREE`, `BASIC` |
| `SubscriptionStatus` | `PENDING`, `ACTIVE`, `CANCELLED`, `EXPIRED`, `FAILED` |

---

## Testing Sequence

1. **Login as Architect** → Get JWT token
2. **Check Status** → `GET /rmtr/subscriptions/status`
3. **Initiate Upgrade** → `POST /rmtr/subscriptions/upgrade`
4. **Open Payment Link** → Copy `paymentLink` from response
5. **Simulate Webhook** → `POST /rmtr/subscriptions/webhook` with status `ACTIVE`
6. **Verify Status** → `GET /rmtr/subscriptions/status` — tier should be `BASIC`, isActive = `true`
7. **Cancel** → `POST /rmtr/subscriptions/cancel`
8. **Verify Cancelled** → `GET /rmtr/subscriptions/status` — status should be `CANCELLED`

---

## Error Responses

| Status | Reason |
|--------|--------|
| `401 Unauthorized` | Invalid or expired JWT token |
| `403 Forbidden` | Wrong role, or invalid callback token (webhook) |
| `400 Bad Request` | Already on BASIC tier, or subscription in invalid state |
