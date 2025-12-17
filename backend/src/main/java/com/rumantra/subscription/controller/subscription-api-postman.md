# Subscription Payment API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token from architect login
- Xendit test API key configured in environment variables
- Webhook endpoint configured in Xendit dashboard

---

## Subscription Flow Overview

```
1. ARCHITECT ON FREE TIER
   ↓
2. ARCHITECT INITIATES UPGRADE (POST /api/subscriptions/upgrade)
   - Backend creates Xendit recurring payment plan
   - Returns payment link (status: PENDING)
   ↓
3. ARCHITECT REDIRECTED TO XENDIT CHECKOUT
   - User enters payment method (credit card, e-wallet)
   - Xendit processes first payment
   ↓
4. XENDIT SENDS WEBHOOK (POST /api/subscriptions/webhook)
   - Event: recurring.payment.succeeded
   - Backend activates subscription (PENDING → ACTIVE)
   - Backend allocates 10 bid tokens
   ↓
5. ARCHITECT NOW ON BASIC TIER
   - 10 bid tokens available
   - Subscription valid for 365 days
   ↓
6. AUTO-RENEWAL AFTER 365 DAYS
   - Xendit auto-charges payment method
   - Webhook triggers token allocation (+10 tokens)
   - Subscription extended for another year
```

---

## API Endpoints

---

### 1. Initiate Subscription Upgrade

**POST** `/api/subscriptions/upgrade`

Initiates upgrade from FREE to BASIC tier. Creates Xendit recurring payment plan and returns payment link.

```bash
curl --location 'http://localhost:8080/api/subscriptions/upgrade' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Subscription upgrade initiated",
  "data": {
    "subscriptionId": 42,
    "paymentLink": "https://checkout.xendit.co/web/XXXXXXXXXXX",
    "mobilePaymentLink": "https://checkout.xendit.co/mobile/XXXXXXXXXXX",
    "status": "PENDING"
  },
  "timestamp": "2025-12-13T14:30:00"
}
```

**Next Steps:**
1. Frontend redirects user to `paymentLink`
2. User completes payment on Xendit checkout page
3. Xendit sends webhook to backend
4. Backend activates subscription

**Error Response (400 Bad Request) - Already on BASIC:**
```json
{
  "timestamp": "2025-12-13T14:30:00",
  "status": 400,
  "errorCode": "ALREADY_BASIC_TIER",
  "message": "Already on BASIC tier"
}
```

---

### 2. Get Subscription Status

**GET** `/api/subscriptions/status`

Retrieves current subscription details for authenticated architect.

```bash
curl --location 'http://localhost:8080/api/subscriptions/status' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK) - FREE Tier:**
```json
{
  "success": true,
  "message": "Subscription status retrieved",
  "data": {
    "id": 1,
    "tier": "FREE",
    "status": "ACTIVE",
    "startDate": "2025-01-01",
    "endDate": null,
    "yearlyPrice": 0.00,
    "xenditPlanId": null,
    "nextBillingDate": null,
    "isActive": true
  },
  "timestamp": "2025-12-13T14:35:00"
}
```

**Expected Response (200 OK) - BASIC Tier:**
```json
{
  "success": true,
  "message": "Subscription status retrieved",
  "data": {
    "id": 42,
    "tier": "BASIC",
    "status": "ACTIVE",
    "startDate": "2025-06-15",
    "endDate": "2026-06-15",
    "yearlyPrice": 1500000.00,
    "xenditPlanId": "rp_abc123xyz",
    "nextBillingDate": "2026-06-15",
    "lastPaymentDate": "2025-06-15T10:30:00",
    "isActive": true
  },
  "timestamp": "2025-12-13T14:35:00"
}
```

**Frontend Usage:**
- Display current tier badge (FREE/BASIC)
- Show next billing date for BASIC users
- Show "Upgrade" button for FREE users
- Show "Cancel" button for BASIC users

---

### 3. Cancel Subscription

**POST** `/api/subscriptions/cancel`

Cancels BASIC subscription. User keeps benefits until current subscription period ends.

```bash
curl --location --request POST 'http://localhost:8080/api/subscriptions/cancel' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Subscription cancelled successfully",
  "timestamp": "2025-12-13T15:00:00"
}
```

**Side Effects:**
- Xendit recurring payment stopped (no more auto-renewal)
- Subscription status: ACTIVE → CANCELLED
- User keeps BASIC benefits until `endDate`
- Remaining bid tokens stay usable until `endDate`
- After `endDate`, user automatically downgraded to FREE tier

**Error Response (400 Bad Request) - Already on FREE:**
```json
{
  "timestamp": "2025-12-13T15:00:00",
  "status": 400,
  "errorCode": "NO_ACTIVE_SUBSCRIPTION",
  "message": "No active subscription found"
}
```

**Frontend Implementation Tip:**
- Show cancellation confirmation dialog
- Explain: "Your BASIC benefits will continue until [endDate]. No refund available."
- After cancellation, update UI to show expiration date

---

### 4. Xendit Webhook Handler

**POST** `/api/subscriptions/webhook`

Receives webhook events from Xendit. This endpoint is called by Xendit servers, not by frontend.

**Important:** Configure this webhook URL in Xendit dashboard:
- Webhook URL: `https://yourdomain.com/api/subscriptions/webhook`
- Webhook Events: All recurring payment events

```bash
curl --location 'http://localhost:8080/api/subscriptions/webhook' \
--header 'x-callback-token: your_webhook_verification_token' \
--header 'Content-Type: application/json' \
--data '{
  "id": "rp_abc123xyz",
  "reference_id": "rumantra_arch_456_1702123456789",
  "customer_id": "arch_456",
  "recurring_action": "PAYMENT",
  "status": "ACTIVE",
  "currency": "IDR",
  "amount": "1500000",
  "recurring_cycle_id": "cycle_xyz789",
  "created": "2025-06-15T10:30:00Z",
  "updated": "2025-06-15T10:35:00Z"
}'
```

**Expected Response (200 OK):**
```
HTTP 200 OK (empty body)
```

**Webhook Events Handled:**

| Event Status | Trigger | Backend Action |
|--------------|---------|----------------|
| `ACTIVE` | First payment succeeded OR renewal succeeded | Activate subscription, allocate 10 tokens |
| `FAILED` | Payment failed | Mark subscription as EXPIRED |
| `STOPPED` | Recurring payment stopped | Mark as CANCELLED, downgrade to FREE |

**Security:**
- Webhook signature verified via `x-callback-token` header
- Invalid signatures return HTTP 403 Forbidden
- Idempotency check using `recurring_cycle_id`

**Error Response (403 Forbidden) - Invalid Token:**
```
HTTP 403 Forbidden (empty body)
```

---

## Testing Sequence for Postman/Bruno

### Development Testing (with Xendit Sandbox)

1. **Login as Architect** → Get JWT token
2. **Check Current Subscription** → `GET /api/subscriptions/status`
3. **Check Bid Quota** → `GET /api/bids/quota` (should show 1 token for FREE)
4. **Initiate Upgrade** → `POST /api/subscriptions/upgrade` → Save payment link
5. **Open Payment Link** → Complete payment in Xendit sandbox
   - Use test card: `4000000000000002` (successful payment)
   - Or test card: `4000000000000010` (failed payment)
6. **Xendit Sends Webhook** → Automatically triggers `/api/subscriptions/webhook`
7. **Verify Upgrade** → `GET /api/subscriptions/status` (should show BASIC tier)
8. **Check Updated Quota** → `GET /api/bids/quota` (should show 11 tokens total)
9. **Cancel Subscription** → `POST /api/subscriptions/cancel`
10. **Verify Cancellation** → `GET /api/subscriptions/status` (status: CANCELLED)

### Manual Webhook Testing (for Development)

If you want to manually trigger webhook without Xendit:

```bash
curl --location 'http://localhost:8080/api/subscriptions/webhook' \
--header 'x-callback-token: your_test_webhook_token' \
--header 'Content-Type: application/json' \
--data '{
  "id": "rp_test_123",
  "reference_id": "rumantra_arch_2_1702123456789",
  "status": "ACTIVE",
  "recurring_cycle_id": "cycle_test_456"
}'
```

**Note:** Make sure `reference_id` matches a PENDING subscription in your database.

---

## Environment Variables Required

```bash
# Xendit Sandbox (for testing)
XENDIT_API_KEY=xnd_development_your_test_key_here
XENDIT_WEBHOOK_TOKEN=your_webhook_verification_token

# Xendit Production (for live)
XENDIT_API_KEY=xnd_production_your_live_key_here
XENDIT_WEBHOOK_TOKEN=your_production_webhook_token
```

**How to Get Xendit Credentials:**
1. Create account at https://dashboard.xendit.co/register
2. Toggle to "Test Mode" in dashboard
3. Go to Settings → API Keys → Generate new key
4. Go to Settings → Webhooks → Set verification token
5. Add webhook URL: `https://yourdomain.com/api/subscriptions/webhook`

---

## Subscription Lifecycle States

| Status | Meaning | User Actions Available |
|--------|---------|------------------------|
| `PENDING` | Waiting for first payment | Complete payment via link |
| `ACTIVE` | Subscription paid and working | Cancel subscription |
| `EXPIRED` | Payment failed or period ended | Upgrade again |
| `CANCELLED` | User cancelled, waiting for period end | None (auto-downgrade on endDate) |

---

## Frontend Implementation Guidelines

### 1. Subscription Upgrade Flow

**Step 1: Display Pricing**
- Show FREE vs BASIC comparison
- Highlight: "1 token/year" vs "10 tokens/year"
- Button: "Upgrade to BASIC - IDR 1,500,000/year"

**Step 2: Initiate Upgrade**
- Call `POST /api/subscriptions/upgrade`
- Show loading indicator
- Receive payment link

**Step 3: Redirect to Payment**
- `window.location.href = response.data.paymentLink`
- User completes payment on Xendit page

**Step 4: Handle Return**
- Xendit redirects to `success_return_url` or `failure_return_url`
- Poll `GET /api/subscriptions/status` every 2 seconds
- When status becomes ACTIVE, show success message

**Step 5: Update UI**
- Show BASIC tier badge
- Display next billing date
- Update token balance

### 2. Subscription Management Page

**Layout:**
- Current Tier Badge (FREE or BASIC)
- Token Balance Display
- Billing Information (for BASIC users)
- Action Buttons (Upgrade or Cancel)

**For FREE Users:**
- Show "Upgrade to BASIC" card
- Display benefits comparison

**For BASIC Users:**
- Show next billing date
- Show "Cancel Subscription" button
- Display payment history (optional)

### 3. Token Display Component

**Location:** Header/Dashboard
**Display:**
- "10 tokens remaining" (icon + count)
- Tooltip: "Tokens are used to place bids"
- Link to "Upgrade" if on FREE tier

---

## Database Schema Quick Reference

- **rmtr_subscription**: Subscription records (tier, status, dates, Xendit IDs)
- **rmtr_bid_quota**: Token balance per architect (tokens_remaining, tokens_allocated)
- **rmtr_bid_usage_log**: Audit trail for all token changes (TOKEN_ALLOCATED, BID_PLACED, BID_REFUNDED)

---

## Error Codes

| Error Code | HTTP Status | Description |
|-----------|-------------|-------------|
| `ALREADY_BASIC_TIER` | 400 | User already on BASIC tier |
| `NO_ACTIVE_SUBSCRIPTION` | 400 | No subscription found for user |
| `ARCHITECT_NOT_FOUND` | 404 | Architect profile not found |
| `SUBSCRIPTION_NOT_FOUND` | 404 | Subscription does not exist |
| `INVALID_WEBHOOK_SIGNATURE` | 403 | Webhook verification failed |

---

## Xendit Test Cards

For testing in Xendit sandbox:

| Card Number | Result |
|-------------|--------|
| `4000000000000002` | Payment succeeds |
| `4000000000000010` | Payment fails |
| `4000000000000028` | Payment requires authentication (3DS) |

**E-Wallet Testing:**
- Select GoPay/OVO/DANA in Xendit checkout
- Use test credentials provided by Xendit sandbox

---

## Production Checklist

Before going live:

- [ ] Replace `xnd_development` API key with `xnd_production` key
- [ ] Update webhook URL to production domain
- [ ] Test webhook with Xendit's webhook testing tool
- [ ] Configure success/failure return URLs to production frontend
- [ ] Set up monitoring for failed webhooks
- [ ] Test actual payment with real credit card (small amount)
- [ ] Verify token allocation happens correctly
- [ ] Test auto-renewal after 365 days (or trigger manually via Xendit dashboard)
