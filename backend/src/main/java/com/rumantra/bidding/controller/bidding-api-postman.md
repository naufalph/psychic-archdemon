# Bidding System API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (architect or client)
- Replace `{{BID_ID}}` with the bid ID from response
- Replace `{{PROJECT_ID}}` with the project ID
- All endpoints require `ROLE_ARCHITECT` unless noted otherwise

**Base URL:** `http://localhost:8080/rmtr/bids`

---

## Bid Lifecycle

```
1. POST /rmtr/bids               → Create DRAFT bid (no tokens consumed yet)
2. PUT /rmtr/bids/{id}           → Update amount / proposal
3. PUT /rmtr/bids/{id}/details   → Add concept statement + payment phases
4. POST /rmtr/bids/{id}/images/{imageType}  → Upload images (FACADE/INTERIOR/MASSING/ZONING)
5. POST /rmtr/bids/{id}/portfolios → Link portfolio references (max 3)
6. POST /rmtr/bids/{id}/submit   → Submit bid (consumes 1 token → status: PENDING)
7. [Client accepts bid] → POST /rmtr/bids/{id}/accept (CLIENT role required)
```

**Bid statuses:** `DRAFT → PENDING → ACCEPTED | REJECTED | WITHDRAWN | REFUNDED`

---

## Image Types

| Type | Description |
|------|-------------|
| `FACADE` | Exterior facade renders |
| `INTERIOR` | Interior space renders |
| `MASSING` | Building massing studies |
| `ZONING` | Site zoning diagrams |

---

## API Endpoints

---

### 1. Create Draft Bid

**POST** `/rmtr/bids`

Creates a bid in DRAFT status. Tokens are NOT consumed until `submit`.

```bash
curl --location 'http://localhost:8080/rmtr/bids' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "projectId": 1,
  "bidAmount": 50000000,
  "proposal": "Our firm brings 10 years of residential experience to this project."
}'
```

**Request Body:**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `projectId` | Long | Yes | |
| `bidAmount` | BigDecimal | Yes | Must be positive |
| `proposal` | String | No | |

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "projectId": 1,
    "projectTitle": "Modern Family Home",
    "projectLocation": "Jakarta Selatan",
    "projectStatus": "OPEN",
    "architectId": 42,
    "architectName": "PT Karya Desain",
    "architectCompany": "PT Karya Desain",
    "bidAmount": 50000000,
    "proposedTimelineDays": null,
    "proposal": "Our firm brings 10 years of residential experience to this project.",
    "status": "DRAFT",
    "createdAt": "2026-06-01T10:00:00",
    "updatedAt": "2026-06-01T10:00:00",
    "submittedAt": null,
    "acceptedAt": null,
    "details": null,
    "facadeImages": [],
    "interiorImages": [],
    "massingImages": [],
    "zoningImages": [],
    "portfolioReferences": [],
    "conversationId": null
  },
  "timestamp": "2026-06-01T10:00:00"
}
```

---

### 2. Update Draft Bid

**PUT** `/rmtr/bids/{{BID_ID}}`

Update bid amount or proposal. Only works on DRAFT bids.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/bids/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "bidAmount": 55000000,
  "proposal": "Updated proposal with revised cost breakdown."
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `bidAmount` | BigDecimal | No (must be positive if provided) |
| `proposal` | String | No |

---

### 3. Update Bid Details (Concept + Payment Phases)

**PUT** `/rmtr/bids/{{BID_ID}}/details`

Add concept statement and define the payment phase schedule.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/bids/1/details' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "conceptStatement": "We propose a biophilic design approach integrating natural light and ventilation.",
  "phases": [
    {
      "phaseNumber": 1,
      "title": "Schematic Design",
      "deliverables": ["Site plan", "Floor plans", "Elevations"],
      "amount": 15000000,
      "revisionRounds": 2,
      "estimatedDays": 30
    },
    {
      "phaseNumber": 2,
      "title": "Design Development",
      "deliverables": ["Detailed drawings", "Material schedule", "3D renders"],
      "amount": 20000000,
      "revisionRounds": 2,
      "estimatedDays": 45
    },
    {
      "phaseNumber": 3,
      "title": "Construction Documents",
      "deliverables": ["Working drawings", "Bill of quantities", "Specifications"],
      "amount": 20000000,
      "revisionRounds": 1,
      "estimatedDays": 30
    }
  ]
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `conceptStatement` | String | No |
| `phases` | List\<BidPaymentPhaseRequest\> | No |

**BidPaymentPhaseRequest fields:**
| Field | Type |
|-------|------|
| `phaseNumber` | Integer |
| `title` | String |
| `deliverables` | List\<String\> |
| `amount` | BigDecimal |
| `revisionRounds` | Integer |
| `estimatedDays` | Integer |

---

### 4. Upload Bid Images

**POST** `/rmtr/bids/{{BID_ID}}/images/{imageType}`

Upload images for the bid. Call once per image type as needed.

`imageType` must be one of: `FACADE`, `INTERIOR`, `MASSING`, `ZONING`

```bash
# Upload facade renders
curl --location 'http://localhost:8080/rmtr/bids/1/images/FACADE' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'images=@"/home/user/renders/facade_front.jpg"' \
--form 'images=@"/home/user/renders/facade_rear.jpg"'

# Upload interior renders
curl --location 'http://localhost:8080/rmtr/bids/1/images/INTERIOR' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'images=@"/home/user/renders/living_room.jpg"'

# Upload massing study
curl --location 'http://localhost:8080/rmtr/bids/1/images/MASSING' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'images=@"/home/user/diagrams/massing_01.png"'

# Upload zoning diagram
curl --location 'http://localhost:8080/rmtr/bids/1/images/ZONING' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'images=@"/home/user/diagrams/zoning.png"'
```

**Expected Response (200 OK):** Returns `List<BidImageResponse>`
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "imageType": "FACADE",
      "imageUrl": "https://storage.example.com/bids/1/facade_01.jpg",
      "displayOrder": 1,
      "fileName": "facade_front.jpg",
      "fileSize": 2048000
    }
  ],
  "timestamp": "2026-06-01T10:30:00"
}
```

---

### 5. Delete Bid Image

**DELETE** `/rmtr/bids/images/{{IMAGE_ID}}`

```bash
curl --location --request DELETE 'http://localhost:8080/rmtr/bids/images/10' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

### 6. Link Portfolio References

**POST** `/rmtr/bids/{{BID_ID}}/portfolios`

Link up to 3 of your portfolios to the bid as references.

```bash
curl --location 'http://localhost:8080/rmtr/bids/1/portfolios' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "portfolioIds": [5, 12, 18]
}'
```

**Request Body:**
| Field | Type | Validation |
|-------|------|-----------|
| `portfolioIds` | List\<Long\> | Required, max 3 items |

---

### 7. Submit Bid

**POST** `/rmtr/bids/{{BID_ID}}/submit`

Submits the bid. Consumes 1 bid token. Bid moves from DRAFT → PENDING.

```bash
curl --location 'http://localhost:8080/rmtr/bids/1/submit' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Business Rules:**
- Requires at least 1 token remaining in quota
- Deducts 1 token from `rmtr_bid_quota`
- Bid becomes visible to the project's client
- Cannot be un-submitted (only withdrawn)

---

### 8. Withdraw Bid

**PUT** `/rmtr/bids/{{BID_ID}}/withdraw`

Withdraws a submitted (PENDING) bid. Moves status to WITHDRAWN. Token is NOT refunded on withdrawal.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/bids/1/withdraw' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Business Rules:**
- Only works on PENDING bids
- Cannot withdraw ACCEPTED bids

---

### 9. Get My Bids

**GET** `/rmtr/bids/my-bids`

Returns all bids created by the authenticated architect.

```bash
curl --location 'http://localhost:8080/rmtr/bids/my-bids' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):** `ApiResponse<List<BidResponse>>`

---

### 10. Get Bid by ID

**GET** `/rmtr/bids/{{BID_ID}}`

Returns full bid details including images, phases, and portfolio references.

```bash
curl --location 'http://localhost:8080/rmtr/bids/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "projectId": 1,
    "projectTitle": "Modern Family Home",
    "projectLocation": "Jakarta Selatan",
    "projectStatus": "OPEN",
    "projectCoverImagePath": "https://storage.example.com/projects/1/cover.jpg",
    "architectId": 42,
    "architectName": "John Doe",
    "architectCompany": "PT Karya Desain",
    "bidAmount": 55000000,
    "proposedTimelineDays": 105,
    "proposal": "Our firm brings 10 years of residential experience.",
    "status": "PENDING",
    "createdAt": "2026-06-01T10:00:00",
    "updatedAt": "2026-06-01T11:00:00",
    "submittedAt": "2026-06-01T11:00:00",
    "acceptedAt": null,
    "details": {
      "id": 5,
      "conceptStatement": "We propose a biophilic design approach.",
      "phases": [
        {
          "id": 1,
          "phaseNumber": 1,
          "title": "Schematic Design",
          "deliverables": ["Site plan", "Floor plans", "Elevations"],
          "amount": 15000000,
          "revisionRounds": 2,
          "estimatedDays": 30,
          "displayOrder": 1
        }
      ]
    },
    "facadeImages": [
      {
        "id": 10,
        "imageType": "FACADE",
        "imageUrl": "https://storage.example.com/bids/1/facade_01.jpg",
        "displayOrder": 1,
        "fileName": "facade_front.jpg",
        "fileSize": 2048000
      }
    ],
    "interiorImages": [],
    "massingImages": [],
    "zoningImages": [],
    "portfolioReferences": [],
    "conversationId": null
  },
  "timestamp": "2026-06-01T12:00:00"
}
```

---

### 11. Accept Bid (Client Role)

**POST** `/rmtr/bids/{{BID_ID}}/accept`

Client accepts an architect's bid. Project moves to NEGOTIATION status. A conversation is automatically created between architect and client.

**Required Role:** `CLIENT`

```bash
curl --location 'http://localhost:8080/rmtr/bids/1/accept' \
--header 'Authorization: Bearer {{CLIENT_JWT_TOKEN}}'
```

**Expected Response (200 OK):** `ApiResponse<BidResponse>` — the accepted bid with `conversationId` populated.

**Business Logic:**
- Project status changes to `NEGOTIATION`
- `BidAcceptedEvent` published → creates a chat conversation between the two parties
- The bid's `acceptedAt` timestamp is set
- Other bids on the project are rejected automatically

---

### 12. Get Bid Quota

**GET** `/rmtr/bids/quota`

Returns the current token quota for the authenticated architect.

```bash
curl --location 'http://localhost:8080/rmtr/bids/quota' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "tier": "BASIC",
    "tokensRemaining": 7,
    "tokensAllocated": 10
  },
  "timestamp": "2026-06-01T12:00:00"
}
```

**BidQuotaResponse fields:**
| Field | Type | Description |
|-------|------|-------------|
| `tier` | SubscriptionTier | `FREE` or `BASIC` |
| `tokensRemaining` | Integer | Tokens available to spend |
| `tokensAllocated` | Integer | Total tokens allocated in current period |

---

## DTO Reference

### BidResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | |
| `projectId` | Long | |
| `projectTitle` | String | |
| `projectLocation` | String | |
| `projectStatus` | String | |
| `projectCoverImagePath` | String | |
| `architectId` | Long | |
| `architectName` | String | |
| `architectCompany` | String | |
| `bidAmount` | BigDecimal | |
| `proposedTimelineDays` | Integer | Derived from sum of phase `estimatedDays` |
| `proposal` | String | |
| `status` | BidStatus | |
| `createdAt` | LocalDateTime | |
| `updatedAt` | LocalDateTime | |
| `submittedAt` | LocalDateTime | |
| `acceptedAt` | LocalDateTime | null until accepted |
| `details` | BidDetailResponse | null until PUT /details called |
| `facadeImages` | List\<BidImageResponse\> | |
| `interiorImages` | List\<BidImageResponse\> | |
| `massingImages` | List\<BidImageResponse\> | |
| `zoningImages` | List\<BidImageResponse\> | |
| `portfolioReferences` | List\<PortoListResponse\> | |
| `conversationId` | Long | null until bid is accepted |

### BidDetailResponse

| Field | Type |
|-------|------|
| `id` | Long |
| `conceptStatement` | String |
| `phases` | List\<BidPaymentPhaseResponse\> |

### BidPaymentPhaseResponse

| Field | Type |
|-------|------|
| `id` | Long |
| `phaseNumber` | Integer |
| `title` | String |
| `deliverables` | List\<String\> |
| `amount` | BigDecimal |
| `revisionRounds` | Integer |
| `estimatedDays` | Integer |
| `displayOrder` | Integer |

### BidImageResponse

| Field | Type |
|-------|------|
| `id` | Long |
| `imageType` | BidImageType (`FACADE`, `INTERIOR`, `MASSING`, `ZONING`) |
| `imageUrl` | String |
| `displayOrder` | Integer |
| `fileName` | String |
| `fileSize` | Long |

---

## Enums

| Enum | Values |
|------|--------|
| `BidStatus` | `DRAFT`, `PENDING`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`, `REFUNDED` |
| `BidImageType` | `FACADE`, `INTERIOR`, `MASSING`, `ZONING` |
| `SubscriptionTier` | `FREE`, `BASIC` |

---

## Testing Sequence

1. **Login as Architect** → Get JWT token
2. **Check Quota** → `GET /rmtr/bids/quota`
3. **Create Draft Bid** → `POST /rmtr/bids`
4. **Update Draft** → `PUT /rmtr/bids/{id}`
5. **Add Concept + Phases** → `PUT /rmtr/bids/{id}/details`
6. **Upload Images** → `POST /rmtr/bids/{id}/images/FACADE` (repeat for INTERIOR, MASSING, ZONING as needed)
7. **Link Portfolios** → `POST /rmtr/bids/{id}/portfolios`
8. **Submit Bid** → `POST /rmtr/bids/{id}/submit` (consumes 1 token)
9. **View My Bids** → `GET /rmtr/bids/my-bids`
10. **Login as Client** → Get client JWT token
11. **Accept Bid** → `POST /rmtr/bids/{id}/accept` (creates conversation, project → NEGOTIATION)

---

## Error Responses

```json
{
  "success": false,
  "message": "Insufficient bid tokens",
  "timestamp": "2026-06-01T12:00:00"
}
```

| Status | Reason |
|--------|--------|
| `401 Unauthorized` | Invalid or expired JWT token |
| `403 Forbidden` | Not your bid, or wrong role |
| `404 Not Found` | Bid or project not found |
| `400 Bad Request` | Validation error, insufficient tokens, invalid bid state |
