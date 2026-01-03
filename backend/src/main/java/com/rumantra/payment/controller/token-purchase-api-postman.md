# Token Purchase API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token from architect login
- Xendit test API key configured in environment variables
- Separate webhook endpoint configured in Xendit dashboard for Payment Requests

---

## Token Purchase Flow Overview

```
1. ARCHITECT NEEDS MORE BID TOKENS
   ↓
2. CHECK PRICING (GET /tokens/purchases/pricing)
   - FREE tier: IDR 400,000 per token
   - BASIC tier: IDR 250,000 per token (37.5% discount)
   ↓
3. INITIATE PURCHASE (POST /tokens/purchases)
   - Backend creates Xendit payment request (one-time)
   - Returns payment link (status: PENDING)
   - Payment link expires in 24 hours
   ↓
4. ARCHITECT REDIRECTED TO XENDIT CHECKOUT
   - User selects payment method (credit card, e-wallet, bank transfer, QRIS)
   - Xendit processes payment
   ↓
5. XENDIT SENDS WEBHOOK (POST /tokens/purchases/webhook)
   - Event: payment.succeeded
   - Backend marks purchase as COMPLETED
   - Backend allocates purchased tokens
   - Logs purchase in audit trail
   ↓
6. TOKENS ADDED TO ARCHITECT'S QUOTA
   - Tokens immediately available for bidding
   - No expiration date (tokens carry over)
```

---

## API Endpoints

---

### 1. Get Token Pricing

**GET** `/tokens/purchases/pricing`

Retrieves current token pricing based on architect's subscription tier.

```bash
curl --location 'http://localhost:8080/tokens/purchases/pricing' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Pricing information retrieved",
  "data": {
    "currentTier": "BASIC",
    "pricePerToken": 250000,
    "currency": "IDR",
    "minQuantity": 1,
    "maxQuantity": 50,
    "tierPricing": {
      "FREE": 400000,
      "BASIC": 250000
    }
  },
  "timestamp": "2026-01-01T14:30:00"
}
```

**Frontend Usage:**
- Display tier-based pricing dynamically
- Show savings for BASIC tier users
- Validate quantity input (1-50)

---

### 2. Initiate Token Purchase

**POST** `/tokens/purchases`

Creates a one-time payment request for purchasing bid tokens.

```bash
curl --location 'http://localhost:8080/tokens/purchases' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "quantity": 5
}'
```

**Request Body:**
```json
{
  "quantity": 5  // Min: 1, Max: 50
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Token purchase initiated",
  "data": {
    "purchaseId": 123,
    "quantity": 5,
    "pricePerToken": 250000,
    "totalAmount": 1250000,
    "paymentLink": "https://checkout.xendit.co/web/XXXXXXXXXXX",
    "expiresAt": "2026-01-02T14:30:00",
    "status": "PENDING"
  },
  "timestamp": "2026-01-01T14:30:00"
}
```

**Next Steps:**
1. Frontend redirects user to `paymentLink`
2. User completes payment on Xendit checkout page
3. Xendit sends webhook to backend
4. Backend allocates tokens

**Error Response (400 Bad Request) - Invalid Quantity:**
```json
{
  "timestamp": "2026-01-01T14:30:00",
  "status": 400,
  "errorCode": "INVALID_PURCHASE_QUANTITY",
  "message": "Quantity must be between 1 and 50"
}
```

---

### 3. Get Purchase Details

**GET** `/tokens/purchases/{id}`

Retrieves details of a specific token purchase.

```bash
curl --location 'http://localhost:8080/tokens/purchases/123' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Purchase details retrieved",
  "data": {
    "id": 123,
    "quantity": 5,
    "pricePerToken": 250000,
    "totalAmount": 1250000,
    "tier": "BASIC",
    "status": "COMPLETED",
    "paymentMethod": "EWALLET",
    "paymentChannel": "OVO",
    "createdAt": "2026-01-01T14:30:00",
    "completedAt": "2026-01-01T14:35:00",
    "expiresAt": "2026-01-02T14:30:00",
    "failureReason": null
  },
  "timestamp": "2026-01-01T15:00:00"
}
```

**Purchase Statuses:**
- `PENDING` - Awaiting payment
- `COMPLETED` - Payment successful, tokens allocated
- `FAILED` - Payment failed
- `EXPIRED` - Payment link expired (24 hours)
- `CANCELLED` - User cancelled before payment

**Error Response (403 Forbidden) - Not Owner:**
```json
{
  "timestamp": "2026-01-01T15:00:00",
  "status": 403,
  "errorCode": "UNAUTHORIZED_PURCHASE_ACCESS",
  "message": "You don't have access to this purchase"
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2026-01-01T15:00:00",
  "status": 404,
  "errorCode": "PURCHASE_NOT_FOUND",
  "message": "Purchase not found"
}
```

---

### 4. Get Purchase History

**GET** `/tokens/purchases/history`

Retrieves paginated purchase history for authenticated architect.

```bash
curl --location 'http://localhost:8080/tokens/purchases/history?page=0&size=10' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Query Parameters:**
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)
- `sort` - Sort field (default: createdAt,desc)

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Purchase history retrieved",
  "data": {
    "content": [
      {
        "id": 123,
        "quantity": 5,
        "pricePerToken": 250000,
        "totalAmount": 1250000,
        "tier": "BASIC",
        "status": "COMPLETED",
        "paymentMethod": "EWALLET",
        "paymentChannel": "OVO",
        "createdAt": "2026-01-01T14:30:00",
        "completedAt": "2026-01-01T14:35:00",
        "expiresAt": "2026-01-02T14:30:00",
        "failureReason": null
      },
      {
        "id": 122,
        "quantity": 10,
        "pricePerToken": 250000,
        "totalAmount": 2500000,
        "tier": "BASIC",
        "status": "COMPLETED",
        "paymentMethod": "CARD",
        "paymentChannel": "VISA",
        "createdAt": "2025-12-20T10:00:00",
        "completedAt": "2025-12-20T10:05:00",
        "expiresAt": "2025-12-21T10:00:00",
        "failureReason": null
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      }
    },
    "totalPages": 1,
    "totalElements": 2,
    "last": true,
    "first": true,
    "numberOfElements": 2,
    "size": 10,
    "number": 0,
    "empty": false
  },
  "timestamp": "2026-01-01T15:00:00"
}
```

**Frontend Usage:**
- Display purchase history table
- Show payment method icons
- Filter by status
- Export purchase records

---

### 5. Xendit Webhook Handler

**POST** `/tokens/purchases/webhook`

Receives webhook events from Xendit for payment requests. This endpoint is called by Xendit servers, not by frontend.

**Important:** Configure this webhook URL in Xendit dashboard:
- **Webhook URL**: `https://yourdomain.com/tokens/purchases/webhook`
- **Webhook Type**: Payment Requests
- **Events**: payment.succeeded, payment.failed, payment.expired

```bash
curl --location 'http://localhost:8080/tokens/purchases/webhook' \
--header 'x-callback-token: your_webhook_verification_token' \
--header 'Content-Type: application/json' \
--data '{
  "event": "payment.succeeded",
  "business_id": "business_123",
  "created": "2026-01-01T14:35:00.000Z",
  "data": {
    "id": "pr_abc123xyz",
    "reference_id": "token_purchase_arch_456_1735712345000",
    "type": "PAY",
    "country": "ID",
    "currency": "IDR",
    "amount": 1250000,
    "status": "SUCCEEDED",
    "description": "Purchase 5 bid token(s)",
    "metadata": {
      "architect_id": "456",
      "quantity": "5",
      "tier": "BASIC"
    },
    "payment_method": {
      "id": "pm_123",
      "type": "EWALLET",
      "ewallet": {
        "channel_code": "OVO",
        "account_details": "08123456789"
      }
    },
    "created": "2026-01-01T14:30:00.000Z",
    "updated": "2026-01-01T14:35:00.000Z"
  }
}'
```

**Expected Response (200 OK):**
```
HTTP 200 OK (empty body)
```

**Webhook Events Handled:**

| Event | Trigger | Backend Action |
|-------|---------|----------------|
| `payment.succeeded` | Payment completed | Mark purchase as COMPLETED, allocate tokens, log purchase |
| `payment.failed` | Payment failed | Mark purchase as FAILED, record failure reason |
| `payment.expired` | Payment link expired | Mark purchase as EXPIRED |

**Security:**
- Webhook signature verified via `x-callback-token` header
- Invalid signatures return HTTP 403 Forbidden
- Idempotency check using `xenditPaymentRequestId`

**Error Response (403 Forbidden) - Invalid Token:**
```
HTTP 403 Forbidden (empty body)
```

---

## Testing Sequence for Postman/Bruno

### Development Testing (with Xendit Sandbox)

1. **Login as Architect** → Get JWT token
2. **Check Current Tier** → `GET /api/subscriptions/status`
3. **Get Pricing Info** → `GET /tokens/purchases/pricing`
4. **Check Current Quota** → `GET /api/bids/quota` (e.g., 3 tokens remaining)
5. **Initiate Purchase** → `POST /tokens/purchases` with `{"quantity": 5}`
6. **Save Response** → Note `purchaseId` and `paymentLink`
7. **Open Payment Link** → Complete payment in Xendit sandbox
   - Use test card: `4000000000000002` (successful payment)
   - Or select e-wallet: GoPay/OVO/DANA (test credentials)
8. **Xendit Sends Webhook** → Automatically triggers `/tokens/purchases/webhook`
9. **Verify Purchase** → `GET /tokens/purchases/{id}` (status: COMPLETED)
10. **Check Updated Quota** → `GET /api/bids/quota` (should show 8 tokens: 3 + 5)
11. **View History** → `GET /tokens/purchases/history`

### Manual Webhook Testing (for Development)

If you want to manually trigger webhook without Xendit:

```bash
curl --location 'http://localhost:8080/tokens/purchases/webhook' \
--header 'x-callback-token: your_test_webhook_token' \
--header 'Content-Type: application/json' \
--data '{
  "event": "payment.succeeded",
  "business_id": "test_business",
  "created": "2026-01-01T14:35:00.000Z",
  "data": {
    "id": "pr_test_123",
    "reference_id": "token_purchase_arch_2_1735712345000",
    "currency": "IDR",
    "amount": 1250000,
    "status": "SUCCEEDED",
    "metadata": {
      "architect_id": "2",
      "quantity": "5",
      "tier": "BASIC"
    }
  }
}'
```

**Note:** Make sure `reference_id` matches a PENDING purchase in your database.

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

**How to Configure Xendit Webhooks:**
1. Login to Xendit Dashboard → https://dashboard.xendit.co
2. Toggle to "Test Mode" for development
3. Go to **Settings → Webhooks**
4. Click **"Set Webhook URL"**
5. Select **"Payment Requests"** from product dropdown
6. Enter webhook URL: `https://yourdomain.com/tokens/purchases/webhook`
7. Set verification token (same as `XENDIT_WEBHOOK_TOKEN`)
8. Save configuration

**Important:** You need **TWO separate webhooks**:
- `/api/subscriptions/webhook` - For Recurring Payments (subscriptions)
- `/tokens/purchases/webhook` - For Payment Requests (token purchases)

---

## Token Purchase Lifecycle

```
Purchase Created (PENDING)
   ↓
Payment Link Generated (expires in 24h)
   ↓
   ├─→ User Pays → COMPLETED → Tokens Allocated
   ├─→ Payment Fails → FAILED
   ├─→ Link Expires → EXPIRED
   └─→ User Cancels → CANCELLED
```

**Status Transitions:**

| From | To | Trigger |
|------|------|---------|
| PENDING | COMPLETED | payment.succeeded webhook |
| PENDING | FAILED | payment.failed webhook |
| PENDING | EXPIRED | 24 hours passed OR payment.expired webhook |
| PENDING | CANCELLED | User cancels before payment |

---

## Frontend Implementation Guidelines

### 1. Token Purchase Flow

**Step 1: Display Pricing**
```javascript
// Fetch pricing info
GET /tokens/purchases/pricing

// Display pricing card
FREE tier: IDR 400,000/token
BASIC tier: IDR 250,000/token (Save 37.5%!)
```

**Step 2: Purchase Form**
```javascript
// Quantity selector (1-50)
<input type="number" min="1" max="50" value="5" />

// Calculate total
totalAmount = quantity * pricePerToken

// Display breakdown
5 tokens × IDR 250,000 = IDR 1,250,000
```

**Step 3: Initiate Purchase**
```javascript
POST /tokens/purchases
{
  "quantity": 5
}

// Show loading indicator
// Receive payment link
```

**Step 4: Redirect to Payment**
```javascript
window.location.href = response.data.paymentLink
// User completes payment on Xendit page
```

**Step 5: Handle Return**
```javascript
// Xendit redirects back to your app
// Poll purchase status
const pollStatus = setInterval(async () => {
  const response = await GET(`/tokens/purchases/${purchaseId}`)

  if (response.data.status === 'COMPLETED') {
    clearInterval(pollStatus)
    showSuccessMessage('5 tokens added to your account!')
    refreshTokenBalance()
  } else if (response.data.status === 'FAILED') {
    clearInterval(pollStatus)
    showErrorMessage('Payment failed. Please try again.')
  }
}, 2000) // Poll every 2 seconds

// Timeout after 5 minutes
setTimeout(() => clearInterval(pollStatus), 300000)
```

**Step 6: Update UI**
```javascript
// Refresh token balance
GET /api/bids/quota

// Show purchase confirmation
"✅ Purchase successful! 5 tokens added to your account"

// Update token display in header
"8 tokens remaining"
```

### 2. Purchase History Page

**Layout:**
```
┌─────────────────────────────────────────────────┐
│ Token Purchase History                          │
├─────────────────────────────────────────────────┤
│ Date         │ Qty │ Amount      │ Status       │
│ 2026-01-01   │  5  │ 1,250,000   │ ✅ Completed │
│ 2025-12-20   │ 10  │ 2,500,000   │ ✅ Completed │
│ 2025-12-15   │  3  │ 1,200,000   │ ✅ Completed │
│ 2025-12-10   │  5  │ 2,000,000   │ ❌ Failed    │
└─────────────────────────────────────────────────┘
```

**Features:**
- Pagination controls
- Status badges (color-coded)
- Payment method icons (Credit Card, OVO, GoPay, etc.)
- Export to CSV/PDF
- Filter by status
- Date range picker

### 3. Token Balance Display

**Location:** Header/Dashboard

```
┌──────────────────────────┐
│  🎫 8 tokens remaining   │
│  [Buy More Tokens]       │
└──────────────────────────┘
```

**Tooltip:**
```
Tokens are used to place bids on projects
• 1 token = 1 bid
• Tokens never expire
• Buy more anytime
```

**Low Balance Warning:**
```javascript
if (tokensRemaining <= 2) {
  showWarning('Running low on tokens! Buy more to continue bidding.')
}
```

---

## Database Schema Quick Reference

- **rmtr_token_purchase**: Purchase records (quantity, price, status, payment details)
- **rmtr_bid_quota**: Token balance per architect (updated when purchase completes)
- **rmtr_bid_usage_log**: Audit trail with `TOKEN_PURCHASED` action

---

## Error Codes

| Error Code | HTTP Status | Description |
|-----------|-------------|-------------|
| `INVALID_PURCHASE_QUANTITY` | 400 | Quantity not between 1-50 |
| `PURCHASE_NOT_FOUND` | 404 | Purchase ID does not exist |
| `UNAUTHORIZED_PURCHASE_ACCESS` | 403 | User doesn't own this purchase |
| `XENDIT_PAYMENT_ERROR` | 502 | Xendit API communication failed |
| `PURCHASE_ALREADY_COMPLETED` | 409 | Purchase already processed |
| `UNAUTHORIZED_ARCHITECT_ACCESS` | 403 | User is not an architect |

---

## Xendit Test Cards

For testing in Xendit sandbox:

| Card Number | Result |
|-------------|--------|
| `4000000000000002` | Payment succeeds |
| `4000000000000010` | Payment fails |
| `4000000000000028` | Payment requires authentication (3DS) |

**E-Wallet Testing:**
- GoPay: Use test phone `081234567890`
- OVO: Use test phone `081234567890`
- DANA: Use test phone `081234567890`
- All e-wallets auto-approve in sandbox mode

**Other Payment Methods:**
- Bank Transfer: Auto-credited in sandbox
- QRIS: Generates test QR code
- Over-the-Counter: Test retail codes provided

---

## Pricing Comparison

| Tier | Price per Token | 5 Tokens | 10 Tokens | 50 Tokens |
|------|----------------|----------|-----------|-----------|
| FREE | IDR 400,000 | 2,000,000 | 4,000,000 | 20,000,000 |
| BASIC | IDR 250,000 | 1,250,000 | 2,500,000 | 12,500,000 |
| **Savings** | **37.5%** | **750,000** | **1,500,000** | **7,500,000** |

**Recommendation for Users:**
1. Upgrade to BASIC tier first (IDR 1,500,000/year + 10 tokens)
2. Then purchase additional tokens at discounted rate (IDR 250,000/token)
3. Total savings can offset subscription cost quickly

---

## Production Checklist

Before going live:

- [ ] Replace `xnd_development` API key with `xnd_production` key
- [ ] Configure production webhook URL for Payment Requests
- [ ] Test webhook with Xendit's webhook testing tool
- [ ] Verify webhook security (x-callback-token validation)
- [ ] Test actual payment with real credit card (small amount)
- [ ] Verify token allocation happens correctly
- [ ] Test payment expiration (24-hour timeout)
- [ ] Set up monitoring for failed payments
- [ ] Configure error alerting (Sentry, DataDog, etc.)
- [ ] Test all payment methods (cards, e-wallets, bank transfer, QRIS)
- [ ] Implement rate limiting on purchase endpoint
- [ ] Add payment receipt email notifications
- [ ] Test concurrent purchases (race conditions)
- [ ] Verify audit log completeness
- [ ] Load test webhook endpoint
- [ ] Set up dashboard for purchase analytics

---

## Integration Notes

**Xendit Payment Request API Docs:**
- Create Payment Request: https://docs.xendit.co/apidocs/create-payment-request
- Webhook Events: https://docs.xendit.co/webhook/payment-request-events
- Set Webhook URL: https://docs.xendit.co/apidocs/set-webhook-url

**Key Differences from Subscriptions:**
- One-time payment (not recurring)
- 24-hour payment link expiration
- Immediate token allocation on success
- No auto-renewal or billing cycles
- Different webhook events (payment.* vs recurring.*)

**Token Purchase vs Subscription:**

| Feature | Token Purchase | Subscription |
|---------|---------------|--------------|
| Payment Type | One-time | Recurring (yearly) |
| Tokens Allocated | Variable (1-50) | Fixed (1 or 10) |
| Payment Method | All methods | All methods |
| Price | Tier-based | Fixed annual |
| Expiration | None | 365 days |
| Auto-Renewal | No | Yes |
| Webhook URL | /tokens/purchases/webhook | /api/subscriptions/webhook |

---

## Common Issues & Troubleshooting

**Issue 1: Payment link expired**
- **Cause**: User took >24 hours to pay
- **Solution**: Create new purchase request

**Issue 2: Webhook not received**
- **Cause**: Webhook URL misconfigured
- **Solution**: Check Xendit dashboard webhook settings
- **Fallback**: Implement payment status polling

**Issue 3: Tokens not allocated**
- **Cause**: Webhook failed or duplicate prevented
- **Solution**: Check logs, manually trigger webhook test

**Issue 4: Different price charged**
- **Cause**: User tier changed between purchase creation and payment
- **Solution**: Price locked at purchase creation time (stored in DB)

**Issue 5: Purchase shows PENDING forever**
- **Cause**: User didn't complete payment
- **Solution**: Payment links auto-expire after 24 hours

---

## Support & Documentation

For questions or issues:
- Xendit Support: https://help.xendit.co
- Xendit API Status: https://status.xendit.co
- Rumantra Backend Team: backend@rumantra.com
