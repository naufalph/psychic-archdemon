# Phase API — Postman / cURL Reference

**Variables:**
- `{{BASE_URL}}` = `http://localhost:8080`
- `{{JWT_TOKEN}}` = Client JWT (for billing/approval endpoints)
- `{{ARCHITECT_JWT}}` = Architect JWT (for deliverable/disbursement endpoints)
- `{{PROJECT_ID}}` = Project ID
- `{{PHASE_ID}}` = Phase ID

---

## Two-Phase System

The backend has two parallel phase entities — do NOT confuse them:

| Entity | Table | Purpose | Controller |
|--------|-------|---------|------------|
| `BidPaymentPhase` | `rmtr_bid_payment_phase` | Phase schedule proposed in bid | `PaymentController` (`/rmtr/payments/...`) |
| `ProjectPhase` | `rmtr_project_phase` | Actual execution phases | `PhaseController` (`/rmtr/projects/.../phases` and `/rmtr/phases/...`) |

When a project transitions to `IN_PROGRESS`, `ProjectPhase` records are auto-created from `BidPaymentPhase` records.

---

## ProjectPhase Status Machine

```
PENDING → BILLED → PAID → IN_PROGRESS → DELIVERED → APPROVED → DISBURSED
                                                   └→ DISPUTED (under review)
```

**Status meanings:**
| Status | Meaning |
|--------|---------|
| `PENDING` | Waiting for client to generate invoice |
| `BILLED` | Invoice created at Xendit, awaiting payment |
| `PAID` | Payment received, transitioning to work |
| `IN_PROGRESS` | Paid, architect is working |
| `DELIVERED` | Architect submitted deliverables |
| `APPROVED` | Client approved the work |
| `DISBURSED` | Architect received payout |
| `DISPUTED` | Client raised dispute, under review |

---

## ProjectPhase Endpoints

---

### 1. Create Phase (Manual)

**POST** `/rmtr/projects/{{PROJECT_ID}}/phases`

Manually create a phase for a project. Usually phases are auto-created from bid payment phases. Use this for custom phase management.

```bash
curl --location '{{BASE_URL}}/rmtr/projects/1/phases' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "title": "Schematic Design",
  "description": "Initial concept and schematic design phase",
  "amount": 15000000,
  "dueDate": "2026-08-01"
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `title` | String | Yes |
| `description` | String | No |
| `amount` | BigDecimal | Yes (positive) |
| `dueDate` | LocalDate | No |

---

### 2. List Phases for Project

**GET** `/rmtr/projects/{{PROJECT_ID}}/phases`

Returns all `ProjectPhase` records for a project. Accessible by both client and architect.

```bash
curl --location '{{BASE_URL}}/rmtr/projects/1/phases' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "projectId": 1,
      "phaseNumber": 1,
      "title": "Schematic Design",
      "description": "Initial concept phase",
      "amount": 15000000,
      "status": "IN_PROGRESS",
      "dueDate": "2026-08-01",
      "paymentStatus": "PAID",
      "paymentLink": null,
      "maxRevisions": 2,
      "revisionsUsed": 0,
      "disbursementStatus": null,
      "deliverables": [],
      "createdAt": "2026-06-01T10:00:00",
      "updatedAt": "2026-06-10T09:00:00"
    }
  ],
  "timestamp": "2026-06-10T12:00:00"
}
```

---

### 3. Get Phase by ID

**GET** `/rmtr/projects/{{PROJECT_ID}}/phases/{{PHASE_ID}}`

```bash
curl --location '{{BASE_URL}}/rmtr/projects/1/phases/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

### 4. Bill Phase (Client)

**POST** `/rmtr/phases/{{PHASE_ID}}/bill`

Client generates a Xendit invoice for a phase. Phase moves from `PENDING` → `BILLED`.

```bash
curl --location --request POST '{{BASE_URL}}/rmtr/phases/1/bill' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

Returns `PhaseResponse` with `paymentLink` populated.

---

### 5. Upload Deliverables (Architect — JSON metadata)

**POST** `/rmtr/phases/{{PHASE_ID}}/deliverables`

Upload deliverable metadata (file path already stored elsewhere). Use endpoint #6 for direct file upload.

```bash
curl --location '{{BASE_URL}}/rmtr/phases/1/deliverables' \
--header 'Authorization: Bearer {{ARCHITECT_JWT}}' \
--header 'Content-Type: application/json' \
--data '{
  "filePath": "https://storage.example.com/phases/1/floor_plans.pdf",
  "fileType": "application/pdf",
  "description": "Final floor plans — all levels"
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `filePath` | String | Yes |
| `fileType` | String | No |
| `description` | String | No |

**Expected Response (201 Created):** `ApiResponse<DeliverableResponse>`

---

### 6. Upload Deliverable File (Architect — multipart)

**POST** `/rmtr/phases/{{PHASE_ID}}/deliverables/upload`

Upload a file directly and attach it as a deliverable.

```bash
curl --location '{{BASE_URL}}/rmtr/phases/1/deliverables/upload' \
--header 'Authorization: Bearer {{ARCHITECT_JWT}}' \
--form 'file=@"/home/user/docs/floor_plans.pdf"' \
--form 'description=Final floor plans — all levels'
```

**Form fields:**
| Field | Type | Required |
|-------|------|----------|
| `file` | MultipartFile | Yes |
| `description` | String | No |

**Expected Response (201 Created):** `ApiResponse<DeliverableResponse>`

---

### 7. Submit for Review (Architect)

**POST** `/rmtr/phases/{{PHASE_ID}}/submit-for-review`

Architect marks deliverables as ready for client review. Phase moves to `DELIVERED`.

```bash
curl --location --request POST '{{BASE_URL}}/rmtr/phases/1/submit-for-review' \
--header 'Authorization: Bearer {{ARCHITECT_JWT}}'
```

Returns `ApiResponse<PhaseResponse>`.

---

### 8. Approve Deliverables (Client)

**POST** `/rmtr/phases/{{PHASE_ID}}/approve`

Client approves the delivered work. Phase moves from `DELIVERED` → `APPROVED`.

```bash
curl --location --request POST '{{BASE_URL}}/rmtr/phases/1/approve' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

### 9. Dispute Deliverables (Client)

**POST** `/rmtr/phases/{{PHASE_ID}}/dispute`

Client raises a dispute. Phase moves from `DELIVERED` → `DISPUTED`.

```bash
curl --location '{{BASE_URL}}/rmtr/phases/1/dispute' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "reason": "Floor plan dimensions do not match agreed specifications."
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `reason` | String | Yes |

---

### 10. Request Revision (Client)

**POST** `/rmtr/phases/{{PHASE_ID}}/request-revision`

Client requests a revision on delivered work. Revisions are tracked against `maxRevisions`.

```bash
curl --location '{{BASE_URL}}/rmtr/phases/1/request-revision' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "notes": "Please adjust the kitchen layout to accommodate the island counter."
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `notes` | String | No |

Returns `ApiResponse<PhaseResponse>` with updated `revisionsUsed`.

---

### 11. Request Payout (Architect)

**POST** `/rmtr/phases/{{PHASE_ID}}/disburse`

Architect requests payout after work is approved. Phase moves from `APPROVED` → `DISBURSED` (after Xendit confirms).

```bash
curl --location '{{BASE_URL}}/rmtr/phases/1/disburse' \
--header 'Authorization: Bearer {{ARCHITECT_JWT}}' \
--header 'Content-Type: application/json' \
--data '{
  "channelCode": "BCA",
  "accountNumber": "1234567890",
  "accountHolderName": "John Doe"
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `channelCode` | String | Yes | Bank code (e.g. `BCA`, `MANDIRI`, `BNI`) |
| `accountNumber` | String | Yes | |
| `accountHolderName` | String | Yes | |

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "status": "PENDING",
    "amount": 15000000,
    "failureCode": null
  },
  "timestamp": "2026-06-15T10:00:00"
}
```

**DisbursementStatus values:** `PENDING`, `ACCEPTED`, `SUCCEEDED`, `FAILED`, `REVERSED`

---

### 12. Get Phase Audit Log

**GET** `/rmtr/phases/{{PHASE_ID}}/logs`

Returns the full status transition history for a phase. Accessible by both parties.

```bash
curl --location '{{BASE_URL}}/rmtr/phases/1/logs' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "actorType": "CLIENT",
      "action": "BILL",
      "fromStatus": "PENDING",
      "toStatus": "BILLED",
      "createdAt": "2026-06-01T10:00:00"
    },
    {
      "actorType": "SYSTEM",
      "action": "PAYMENT_RECEIVED",
      "fromStatus": "BILLED",
      "toStatus": "IN_PROGRESS",
      "createdAt": "2026-06-01T10:05:00"
    }
  ],
  "timestamp": "2026-06-15T12:00:00"
}
```

---

## Xendit Webhook — Invoice (Phase Payment)

**POST** `/rmtr/xendit/webhook/invoice`

Receives Xendit invoice payment callbacks. **Public endpoint** secured by `X-CALLBACK-TOKEN` header.

On `PAID` status with `external_id` matching a phase invoice:
- Marks the `BidPaymentPhase` payment as `COMPLETED`
- Advances the matching `ProjectPhase` from `BILLED` → `IN_PROGRESS`

```bash
curl --location '{{BASE_URL}}/rmtr/xendit/webhook/invoice' \
--header 'X-CALLBACK-TOKEN: your-xendit-callback-token' \
--header 'Content-Type: application/json' \
--data '{
  "id": "invoice_abc123",
  "external_id": "phase_1",
  "status": "PAID",
  "amount": 15000000
}'
```

---

## Xendit Webhook — Payout (Disbursement)

**POST** `/rmtr/xendit/webhook/payout`

Receives Xendit payout/disbursement callbacks. **Public endpoint** secured by `X-CALLBACK-TOKEN` header.

On `SUCCEEDED` status: marks phase as `DISBURSED`.

```bash
curl --location '{{BASE_URL}}/rmtr/xendit/webhook/payout' \
--header 'X-CALLBACK-TOKEN: your-xendit-callback-token' \
--header 'Content-Type: application/json' \
--data '{
  "id": "disb_xyz789",
  "external_id": "phase_1_disburse",
  "status": "SUCCEEDED",
  "amount": 15000000
}'
```

---

## BidPaymentPhase Endpoints (Payment Tracking View)

These endpoints are on `PaymentController` (`/rmtr/payments/...`) and provide the payment-tracking summary used in `ActiveProjectDashboard`.

### Get Payment Summary

**GET** `/rmtr/payments/projects/{{PROJECT_ID}}`

```bash
curl --location '{{BASE_URL}}/rmtr/payments/projects/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

Returns `ApiResponse<List<PhasePaymentResponse>>`.

### Initiate BidPaymentPhase Invoice

**POST** `/rmtr/payments/phases/{{PHASE_ID}}`

Creates a Xendit invoice for a `BidPaymentPhase`.

```bash
curl --location --request POST '{{BASE_URL}}/rmtr/payments/phases/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

Returns `ApiResponse<PhasePaymentInitiateResponse>` with `paymentLink`.

---

## DTO Reference

### PhaseResponse

| Field | Type |
|-------|------|
| `id` | Long |
| `projectId` | Long |
| `phaseNumber` | Integer |
| `title` | String |
| `description` | String |
| `amount` | BigDecimal |
| `status` | PhaseStatus |
| `dueDate` | LocalDate |
| `paymentStatus` | String |
| `paymentLink` | String |
| `maxRevisions` | Integer |
| `revisionsUsed` | Integer |
| `disbursementStatus` | DisbursementStatus |
| `deliverables` | List\<DeliverableResponse\> |
| `createdAt` | LocalDateTime |
| `updatedAt` | LocalDateTime |

### DeliverableResponse

| Field | Type |
|-------|------|
| `id` | Long |
| `filePath` | String |
| `fileType` | String |
| `description` | String |
| `revisionRound` | Integer |
| `uploadedAt` | LocalDateTime |

### DisbursementResponse

| Field | Type |
|-------|------|
| `status` | String |
| `amount` | BigDecimal |
| `failureCode` | String |

### PhaseLogResponse

| Field | Type |
|-------|------|
| `actorType` | String |
| `action` | String |
| `fromStatus` | String |
| `toStatus` | String |
| `createdAt` | LocalDateTime |

### PhasePaymentResponse (BidPaymentPhase view)

| Field | Type |
|-------|------|
| `phaseId` | Long |
| `phaseNumber` | Integer |
| `title` | String |
| `deliverables` | List\<String\> |
| `amount` | BigDecimal |
| `paymentStatus` | String |
| `paymentLink` | String |
| `paidAt` | LocalDateTime |

### PhasePaymentInitiateResponse

| Field | Type |
|-------|------|
| `amount` | BigDecimal |
| `paymentLink` | String |
| `expiresAt` | LocalDateTime |
| `status` | String |

---

## Enums

| Enum | Values |
|------|--------|
| `PhaseStatus` | `PENDING`, `BILLED`, `PAID`, `IN_PROGRESS`, `DELIVERED`, `APPROVED`, `DISBURSED`, `DISPUTED` |
| `DisbursementStatus` | `PENDING`, `ACCEPTED`, `SUCCEEDED`, `FAILED`, `REVERSED` |

---

## Testing Sequence (Full Phase Flow)

1. *(Project is IN_PROGRESS, phases initialized)*
2. **List Phases** → `GET /rmtr/projects/{id}/phases`
3. **Bill Phase 1** → `POST /rmtr/phases/1/bill` (client)
4. **Get Payment Link** → from response `paymentLink`
5. **Simulate Payment Webhook** → `POST /rmtr/xendit/webhook/invoice`
6. **Phase now IN_PROGRESS** → architect begins work
7. **Upload Deliverable** → `POST /rmtr/phases/1/deliverables/upload` (architect)
8. **Submit for Review** → `POST /rmtr/phases/1/submit-for-review` (architect)
9. **Client Reviews and Approves** → `POST /rmtr/phases/1/approve`
10. **Request Payout** → `POST /rmtr/phases/1/disburse` (architect)
11. **Simulate Payout Webhook** → `POST /rmtr/xendit/webhook/payout`
12. **Phase DISBURSED** → `GET /rmtr/phases/1/logs` (audit trail)
13. Repeat for remaining phases until all DISBURSED → project auto-closes as COMPLETED
