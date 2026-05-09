# Phase Payment API — Postman / cURL Reference

Variables used throughout:
- `{{BASE_URL}}` — `http://localhost:8080`
- `{{JWT_TOKEN}}` — Client JWT
- `{{ARCH_JWT_TOKEN}}` — Architect JWT
- `{{PROJECT_ID}}` — Long project ID
- `{{PHASE_ID}}` — Long phase ID
- `{{WEBHOOK_TOKEN}}` — Value of `xendit.webhook-token` in config

---

## 1. Create Phase
`POST /rmtr/projects/{projectId}/phases` — **CLIENT role**

```bash
curl -X POST "{{BASE_URL}}/rmtr/projects/{{PROJECT_ID}}/phases" \
  -H "Authorization: Bearer {{JWT_TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Schematic Design",
    "description": "Initial concept and layout drawings",
    "amount": 5000000,
    "dueDate": "2026-06-01"
  }'
```

**Success 201:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "projectId": 42,
    "phaseNumber": 1,
    "title": "Schematic Design",
    "description": "Initial concept and layout drawings",
    "amount": 5000000.00,
    "status": "PENDING",
    "dueDate": "2026-06-01",
    "paymentStatus": null,
    "paymentLink": null,
    "deliverables": [],
    "createdAt": "2026-05-01T10:00:00",
    "updatedAt": null
  },
  "timestamp": "2026-05-01T10:00:00"
}
```

**Error — project not IN_PROGRESS (400):**
```json
{ "success": false, "message": "PROJECT_NOT_IN_PROGRESS" }
```

---

## 2. List Phases
`GET /rmtr/projects/{projectId}/phases` — **CLIENT or ARCHITECT**

```bash
curl -X GET "{{BASE_URL}}/rmtr/projects/{{PROJECT_ID}}/phases" \
  -H "Authorization: Bearer {{JWT_TOKEN}}"
```

**Success 200:** returns array of phase objects (same shape as above).

---

## 3. Get Single Phase
`GET /rmtr/projects/{projectId}/phases/{phaseId}` — **CLIENT or ARCHITECT**

```bash
curl -X GET "{{BASE_URL}}/rmtr/projects/{{PROJECT_ID}}/phases/{{PHASE_ID}}" \
  -H "Authorization: Bearer {{JWT_TOKEN}}"
```

---

## 4. Bill Phase (Create Invoice)
`POST /rmtr/phases/{phaseId}/bill` — **CLIENT role**

Triggers a Xendit invoice. Phase must be in `PENDING` status.

```bash
curl -X POST "{{BASE_URL}}/rmtr/phases/{{PHASE_ID}}/bill" \
  -H "Authorization: Bearer {{JWT_TOKEN}}"
```

**Success 200:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "BILLED",
    "paymentLink": "https://checkout.xendit.co/web/inv_abc",
    ...
  }
}
```

**Error — phase not PENDING (409):**
```json
{ "success": false, "message": "PHASE_WRONG_STATUS" }
```

---

## 5. Upload Deliverable
`POST /rmtr/phases/{phaseId}/deliverables` — **ARCHITECT role**

Phase must be in `IN_PROGRESS` status.

```bash
curl -X POST "{{BASE_URL}}/rmtr/phases/{{PHASE_ID}}/deliverables" \
  -H "Authorization: Bearer {{ARCH_JWT_TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "projects/42/phase1/schematic_v1.pdf",
    "fileType": "application/pdf",
    "description": "Schematic design v1 — floor plan and elevations"
  }'
```

**Success 201:**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "filePath": "projects/42/phase1/schematic_v1.pdf",
    "fileType": "application/pdf",
    "description": "Schematic design v1 — floor plan and elevations",
    "uploadedAt": "2026-05-15T14:30:00"
  }
}
```

**Error — phase not IN_PROGRESS (409):**
```json
{ "success": false, "message": "PHASE_WRONG_STATUS" }
```

---

## 6. Approve Deliverable
`POST /rmtr/phases/{phaseId}/approve` — **CLIENT role**

Phase must be in `DELIVERED` status.

```bash
curl -X POST "{{BASE_URL}}/rmtr/phases/{{PHASE_ID}}/approve" \
  -H "Authorization: Bearer {{JWT_TOKEN}}"
```

**Success 200:** returns updated phase with `status: "APPROVED"`.

---

## 7. Dispute Deliverable
`POST /rmtr/phases/{phaseId}/dispute` — **CLIENT role**

Phase must be in `DELIVERED` status.

```bash
curl -X POST "{{BASE_URL}}/rmtr/phases/{{PHASE_ID}}/dispute" \
  -H "Authorization: Bearer {{JWT_TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Floor plan dimensions do not match the agreed spec"
  }'
```

**Success 200:** returns updated phase with `status: "DISPUTED"`.

---

## 8. Initiate Disbursement (Payout)
`POST /rmtr/phases/{phaseId}/disburse` — **ARCHITECT role**

Phase must be in `APPROVED` status. Triggers Xendit Payouts v2.

```bash
curl -X POST "{{BASE_URL}}/rmtr/phases/{{PHASE_ID}}/disburse" \
  -H "Authorization: Bearer {{ARCH_JWT_TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "channelCode": "ID_BCA",
    "accountNumber": "1234567890",
    "accountHolderName": "Budi Santoso"
  }'
```

**Success 200:**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "phaseId": 1,
    "xenditPayoutId": "payout_abc123",
    "xenditReferenceId": "phase_payout_1_1746100000000",
    "channelCode": "ID_BCA",
    "accountNumber": "1234567890",
    "accountHolderName": "Budi Santoso",
    "amount": 5000000.00,
    "status": "ACCEPTED",
    "failureCode": null,
    "initiatedAt": "2026-05-20T09:00:00",
    "completedAt": null
  }
}
```

**Error — phase not APPROVED (409):**
```json
{ "success": false, "message": "PHASE_WRONG_STATUS" }
```

**Error — Xendit payout creation failed (502):**
```json
{ "success": false, "message": "PAYOUT_FAILED" }
```

---

## 9. Get Audit Log
`GET /rmtr/phases/{phaseId}/logs` — **CLIENT or ARCHITECT**

```bash
curl -X GET "{{BASE_URL}}/rmtr/phases/{{PHASE_ID}}/logs" \
  -H "Authorization: Bearer {{JWT_TOKEN}}"
```

**Success 200:**
```json
{
  "success": true,
  "data": [
    { "actorType": "SYSTEM", "action": "PHASE_CREATED", "fromStatus": null, "toStatus": "PENDING", "createdAt": "2026-05-01T10:00:00" },
    { "actorType": "CLIENT", "action": "PHASE_BILLED", "fromStatus": "PENDING", "toStatus": "BILLED", "createdAt": "2026-05-01T10:05:00" },
    { "actorType": "XENDIT", "action": "PAYMENT_RECEIVED", "fromStatus": "BILLED", "toStatus": "IN_PROGRESS", "createdAt": "2026-05-01T10:30:00" },
    { "actorType": "ARCHITECT", "action": "DELIVERABLE_UPLOADED", "fromStatus": "IN_PROGRESS", "toStatus": "DELIVERED", "createdAt": "2026-05-15T14:30:00" },
    { "actorType": "CLIENT", "action": "DELIVERABLE_APPROVED", "fromStatus": "DELIVERED", "toStatus": "APPROVED", "createdAt": "2026-05-16T09:00:00" },
    { "actorType": "ARCHITECT", "action": "PAYOUT_INITIATED", "fromStatus": "APPROVED", "toStatus": "APPROVED", "createdAt": "2026-05-20T09:00:00" },
    { "actorType": "XENDIT", "action": "PAYOUT_COMPLETED", "fromStatus": "APPROVED", "toStatus": "DISBURSED", "createdAt": "2026-05-20T09:05:00" }
  ]
}
```

---

## Webhooks

### Invoice Paid (routes through existing unified handler)
`POST /rmtr/payments/webhook/invoice` — public, token-verified

The `external_id` prefix `proj_phase_` routes to PhasePaymentService.

```bash
curl -X POST "{{BASE_URL}}/rmtr/payments/webhook/invoice" \
  -H "X-CALLBACK-TOKEN: {{WEBHOOK_TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "inv_abc123",
    "external_id": "proj_phase_1_1746100000000",
    "status": "PAID",
    "payment_method": "BANK_TRANSFER",
    "payment_channel": "BCA",
    "paid_at": "2026-05-01T10:30:00+07:00"
  }'
```

**Success 200:** empty body. Phase transitions from `BILLED` → `IN_PROGRESS`.

---

### Payout Callback (new endpoint)
`POST /rmtr/phases/webhook/payout` — public, token-verified

```bash
# payout.succeeded
curl -X POST "{{BASE_URL}}/rmtr/phases/webhook/payout" \
  -H "X-CALLBACK-TOKEN: {{WEBHOOK_TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "event": "payout.succeeded",
    "id": "payout_abc123",
    "reference_id": "phase_payout_1_1746200000000",
    "status": "SUCCEEDED",
    "channel_code": "ID_BCA",
    "amount": 5000000
  }'
```

**Success 200:** empty body. Phase transitions from `APPROVED` → `DISBURSED`. If last phase, project → `COMPLETED`.

```bash
# payout.failed
curl -X POST "{{BASE_URL}}/rmtr/phases/webhook/payout" \
  -H "X-CALLBACK-TOKEN: {{WEBHOOK_TOKEN}}" \
  -H "Content-Type: application/json" \
  -d '{
    "event": "payout.failed",
    "id": "payout_abc123",
    "reference_id": "phase_payout_1_1746200000000",
    "status": "FAILED",
    "failure_code": "INVALID_DESTINATION",
    "amount": 5000000
  }'
```

**Success 200:** empty body. Disbursement status → `FAILED`, phase remains `APPROVED`.

**Error — bad token (403):** empty body.

---

## Phase Status Lifecycle

```
PENDING → BILLED → (payment webhook) → IN_PROGRESS → DELIVERED → APPROVED → DISBURSED
                                                    ↘ DISPUTED
```
