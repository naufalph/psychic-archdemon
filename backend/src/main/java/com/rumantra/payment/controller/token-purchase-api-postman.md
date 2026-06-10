# Token Purchase API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (architect)
- Requires `ROLE_ARCHITECT`
- Xendit test API key configured in environment variables

**Base URL:** `http://localhost:8080/rmtr/tokens`

---

## Token Purchase Flow

```
1. GET /rmtr/tokens/pricing      → Check current tier pricing
2. POST /rmtr/tokens             → Initiate purchase (returns Xendit payment link)
3. [User completes payment at Xendit checkout]
4. POST /rmtr/xendit/webhook/invoice  → Xendit sends callback (automatic)
5. Tokens added to architect's quota
6. GET /rmtr/tokens/{id}         → Verify purchase completed
```

**Token pricing (tier-based):**
- `FREE` tier: IDR 400,000 / token
- `BASIC` tier: IDR 250,000 / token

**Limits:** 1–50 tokens per purchase. Payment link expires in 24 hours.

---

## API Endpoints

---

### 1. Get Pricing

**GET** `/rmtr/tokens/pricing`

Returns current tier and pricing info before initiating a purchase.

```bash
curl --location 'http://localhost:8080/rmtr/tokens/pricing' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
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
  "timestamp": "2026-06-01T10:00:00"
}
```

---

### 2. Initiate Token Purchase

**POST** `/rmtr/tokens`

Creates a Xendit invoice and returns a payment link. Tokens are NOT added until payment is confirmed via webhook.

```bash
curl --location 'http://localhost:8080/rmtr/tokens' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "quantity": 5
}'
```

**Request Body:**
| Field | Type | Validation |
|-------|------|-----------|
| `quantity` | Integer | Required, min 1, max 50 |

**Expected Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "purchaseId": 42,
    "quantity": 5,
    "pricePerToken": 250000,
    "totalAmount": 1250000,
    "paymentLink": "https://checkout.xendit.co/v2/invoice/abc123",
    "expiresAt": "2026-06-02T10:00:00",
    "status": "PENDING"
  },
  "timestamp": "2026-06-01T10:00:00"
}
```

---

### 3. Get Purchase Details

**GET** `/rmtr/tokens/{{PURCHASE_ID}}`

Returns details of a specific purchase. Ownership is enforced — only the purchasing architect can access.

```bash
curl --location 'http://localhost:8080/rmtr/tokens/42' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 42,
    "quantity": 5,
    "pricePerToken": 250000,
    "totalAmount": 1250000,
    "tier": "BASIC",
    "status": "COMPLETED",
    "paymentMethod": "CREDIT_CARD",
    "paymentChannel": "VISA",
    "createdAt": "2026-06-01T10:00:00",
    "completedAt": "2026-06-01T10:05:00",
    "expiresAt": "2026-06-02T10:00:00",
    "failureReason": null
  },
  "timestamp": "2026-06-01T10:10:00"
}
```

**Purchase statuses:** `PENDING`, `COMPLETED`, `FAILED`, `EXPIRED`, `CANCELLED`

---

### 4. Get Purchase History

**GET** `/rmtr/tokens/history`

Returns paginated purchase history for the authenticated architect.

```bash
# Default (page 0, size 10)
curl --location 'http://localhost:8080/rmtr/tokens/history' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'

# With pagination
curl --location 'http://localhost:8080/rmtr/tokens/history?page=1&size=20' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Query Parameters:** standard Spring `Pageable` — `page`, `size`, `sort`

**Expected Response (200 OK):** `ApiResponse<Page<TokenPurchaseDetailResponse>>`

---

## Webhook — Xendit Invoice

**POST** `/rmtr/xendit/webhook/invoice`

Receives Xendit payment callbacks. **This endpoint is public (no JWT required)** and is secured by Xendit's `X-CALLBACK-TOKEN` header.

Configure this URL in your Xendit dashboard settings.

```bash
# Simulated Xendit webhook call (for testing)
curl --location 'http://localhost:8080/rmtr/xendit/webhook/invoice' \
--header 'X-CALLBACK-TOKEN: your-xendit-callback-token' \
--header 'Content-Type: application/json' \
--data '{
  "id": "payment_abc123",
  "external_id": "token_purchase_42",
  "status": "PAID",
  "amount": 1250000,
  "payment_method": "CREDIT_CARD",
  "payment_channel": "VISA"
}'
```

**Routing logic inside the handler:**
- If `external_id` starts with `token_purchase_` → handled as token purchase payment
- If `external_id` starts with `phase_` → handled as phase payment
- Tokens are allocated to architect's quota immediately on `PAID` status

**Response:** `200 OK` (no body) on success, `403 Forbidden` if callback token is invalid.

---

## DTO Reference

### TokenPurchasePricingResponse

| Field | Type |
|-------|------|
| `currentTier` | String (`FREE` or `BASIC`) |
| `pricePerToken` | BigDecimal |
| `currency` | String (`IDR`) |
| `minQuantity` | Integer |
| `maxQuantity` | Integer |
| `tierPricing` | Map\<String, BigDecimal\> |

### TokenPurchaseResponse (POST response)

| Field | Type |
|-------|------|
| `purchaseId` | Long |
| `quantity` | Integer |
| `pricePerToken` | BigDecimal |
| `totalAmount` | BigDecimal |
| `paymentLink` | String |
| `expiresAt` | LocalDateTime |
| `status` | String |

### TokenPurchaseDetailResponse (GET response)

| Field | Type |
|-------|------|
| `id` | Long |
| `quantity` | Integer |
| `pricePerToken` | BigDecimal |
| `totalAmount` | BigDecimal |
| `tier` | String |
| `status` | String |
| `paymentMethod` | String |
| `paymentChannel` | String |
| `createdAt` | LocalDateTime |
| `completedAt` | LocalDateTime |
| `expiresAt` | LocalDateTime |
| `failureReason` | String |

---

## Enums

| Enum | Values |
|------|--------|
| `PurchaseStatus` | `PENDING`, `COMPLETED`, `FAILED`, `EXPIRED`, `CANCELLED` |
| `SubscriptionTier` | `FREE`, `BASIC` |

---

## Testing Sequence

1. **Login as Architect** → Get JWT token
2. **Check Pricing** → `GET /rmtr/tokens/pricing`
3. **Initiate Purchase** → `POST /rmtr/tokens` with `quantity`
4. **Open Payment Link** → Copy `paymentLink` from response and complete payment in Xendit checkout
5. **Simulate Webhook** → `POST /rmtr/xendit/webhook/invoice` with `external_id: "token_purchase_{id}"`
6. **Verify Purchase** → `GET /rmtr/tokens/{id}` — status should be `COMPLETED`
7. **Check History** → `GET /rmtr/tokens/history`

---

## Error Responses

| Status | Reason |
|--------|--------|
| `401 Unauthorized` | Invalid or expired JWT token |
| `403 Forbidden` | Not your purchase (GET), or invalid callback token (webhook) |
| `404 Not Found` | Purchase not found |
| `400 Bad Request` | Invalid quantity |
