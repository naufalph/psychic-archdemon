# Project API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (client or architect depending on endpoint)
- Replace `{{PROJECT_ID}}` with the project ID from response

**Base URL:** `http://localhost:8080/rmtr/projects`

---

## Project Lifecycle

```
PENDING_APPROVAL → [Superuser validates] → OPEN → [Bid accepted] → NEGOTIATION
                                       ↘ REJECTED
NEGOTIATION → [Both parties confirm] → IN_PROGRESS → COMPLETED
           → [Client rejects] → OPEN (back to bidding)
```

**All project statuses:** `PENDING_APPROVAL`, `REJECTED`, `OPEN`, `BIDDING_CLOSED`, `NEGOTIATION`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

---

## API Endpoints

---

### 1. Create Project (Client)

**POST** `/rmtr/projects`

Creates a new project. Requires `CLIENT` role. The `clientId` is derived from the authenticated user — do NOT pass it in the body.

**Content-Type:** `multipart/form-data`

```bash
curl --location 'http://localhost:8080/rmtr/projects' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'project={"title":"Modern Family Home","location":"Jakarta Selatan","designBudgetMin":50000000,"designBudgetMax":80000000,"projectCategory":"Residential","buildingFunction":"Single Family House","estimatedBuildArea":250,"numberOfFloors":2,"ownsLand":true,"hasLegalDocuments":true,"scopeOfWork":"Full architectural design from concept to construction documents","deliverables":["Floor plans","Elevations","3D renders","Construction drawings"],"designPreferences":"Modern minimalist with natural materials","contactPerson":"Budi Santoso","startDateType":"SPECIFIC_DATE","expectedStartDate":"2026-09-01","biddingDeadline":"2026-07-15"};type=application/json' \
--form 'files=@"/home/user/documents/site_plan.pdf"' \
--form 'files=@"/home/user/photos/site_photo.jpg"'
```

**Important:** The project data is sent as a JSON blob in the `project` form part with `type=application/json`. Files are sent in the `files` form part (optional).

**CreateProjectRequest fields:**
| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| `title` | String | Yes | max 255 chars |
| `location` | String | Yes | max 255 chars |
| `designBudgetMin` | Long | No | min 0 |
| `designBudgetMax` | Long | No | min 0 |
| `projectCategory` | String | No | max 255 chars |
| `buildingFunction` | String | No | max 255 chars |
| `estimatedBuildArea` | Integer | No | min 1 |
| `numberOfFloors` | Integer | No | min 1 |
| `ownsLand` | Boolean | No | |
| `hasLegalDocuments` | Boolean | No | |
| `scopeOfWork` | String | No | |
| `deliverables` | List\<String\> | No | |
| `designPreferences` | String | No | |
| `contactPerson` | String | No | max 255 chars |
| `startDateType` | StartDateType | No | `IMMEDIATELY` or `SPECIFIC_DATE` |
| `expectedStartDate` | LocalDate | No | |
| `biddingDeadline` | LocalDate | No | |

**Expected Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "clientId": 5,
    "title": "Modern Family Home",
    "location": "Jakarta Selatan",
    "designBudgetMin": 50000000,
    "designBudgetMax": 80000000,
    "projectCategory": "Residential",
    "buildingFunction": "Single Family House",
    "estimatedBuildArea": 250,
    "numberOfFloors": 2,
    "ownsLand": true,
    "hasLegalDocuments": true,
    "scopeOfWork": "Full architectural design from concept to construction documents",
    "deliverables": ["Floor plans", "Elevations", "3D renders", "Construction drawings"],
    "designPreferences": "Modern minimalist with natural materials",
    "contactPerson": "Budi Santoso",
    "startDateType": "SPECIFIC_DATE",
    "expectedStartDate": "2026-09-01",
    "status": "PENDING_APPROVAL",
    "isValid": null,
    "validationNotes": null,
    "biddingDeadline": "2026-07-15T00:00:00",
    "files": [
      {
        "id": 1,
        "fileName": "site_plan.pdf",
        "filePath": "https://storage.example.com/projects/1/site_plan.pdf",
        "fileType": "application/pdf",
        "fileSize": 524288,
        "uploadedAt": "2026-06-01T10:00:00"
      }
    ],
    "clientConfirmed": false,
    "architectConfirmed": false,
    "bidCount": 0,
    "createdAt": "2026-06-01T10:00:00",
    "updatedAt": "2026-06-01T10:00:00"
  },
  "timestamp": "2026-06-01T10:00:00"
}
```

---

### 2. Get Project by ID (Client)

**GET** `/rmtr/projects/{{PROJECT_ID}}`

Returns a project owned by the authenticated client.

```bash
curl --location 'http://localhost:8080/rmtr/projects/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

### 3. Get My Projects (Client)

**GET** `/rmtr/projects`

Returns all projects owned by the authenticated client.

```bash
curl --location 'http://localhost:8080/rmtr/projects' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

### 4. Delete Project (Client)

**DELETE** `/rmtr/projects/{{PROJECT_ID}}`

```bash
curl --location --request DELETE 'http://localhost:8080/rmtr/projects/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

### 5. Validate Project (Superuser)

**PUT** `/rmtr/projects/{{PROJECT_ID}}/validate`

Sets the project as valid (→ OPEN) or invalid (→ REJECTED). Requires `SUPERUSER` role.

```bash
# Approve
curl --location --request PUT 'http://localhost:8080/rmtr/projects/1/validate' \
--header 'Authorization: Bearer {{SUPERUSER_JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "isValid": true,
  "validationNotes": "Project looks well-scoped. Approved."
}'

# Reject
curl --location --request PUT 'http://localhost:8080/rmtr/projects/1/validate' \
--header 'Authorization: Bearer {{SUPERUSER_JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "isValid": false,
  "validationNotes": "Budget range is incomplete. Please revise and resubmit."
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `isValid` | Boolean | Yes |
| `validationNotes` | String | No |

---

### 6. Get All Projects (Superuser)

**GET** `/rmtr/projects/all`

Returns all projects regardless of status or owner. Requires `SUPERUSER` role.

```bash
curl --location 'http://localhost:8080/rmtr/projects/all' \
--header 'Authorization: Bearer {{SUPERUSER_JWT_TOKEN}}'
```

---

### 7. Get Open Projects (Architect)

**GET** `/rmtr/projects/open`

Returns all validated, open projects available for bidding.

```bash
# Default sort (newest first)
curl --location 'http://localhost:8080/rmtr/projects/open' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'

# Sort by design fee, exclude own projects (if user is also a client)
curl --location 'http://localhost:8080/rmtr/projects/open?sortBy=designBudgetMax&sortDirection=desc&excludeOwnProjects=true' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Query Parameters:**
| Param | Type | Default | Notes |
|-------|------|---------|-------|
| `sortBy` | String | `createdAt` | Field to sort by |
| `sortDirection` | String | `desc` | `asc` or `desc` |
| `excludeOwnProjects` | boolean | `false` | Exclude projects created by this user (for dual-role users) |

---

### 8. Get Project for Architect

**GET** `/rmtr/projects/{{PROJECT_ID}}/for-architect`

Returns project detail for an architect to view. Access rules:
- `OPEN` / `NEGOTIATION` → any architect can view (for bidding purposes)
- `IN_PROGRESS` / `COMPLETED` → only the architect with the accepted bid can view

```bash
curl --location 'http://localhost:8080/rmtr/projects/1/for-architect' \
--header 'Authorization: Bearer {{ARCHITECT_JWT_TOKEN}}'
```

---

### 9. Get Project Bids (Client)

**GET** `/rmtr/projects/{{PROJECT_ID}}/bids`

Returns all bids submitted on a project. Client must own the project.

```bash
curl --location 'http://localhost:8080/rmtr/projects/1/bids' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

### 10. Confirm Negotiation (Client)

**POST** `/rmtr/projects/{{PROJECT_ID}}/confirm-negotiation`

Client confirms the bid terms. Project moves to `IN_PROGRESS` only when BOTH client and architect have confirmed.

```bash
curl --location 'http://localhost:8080/rmtr/projects/1/confirm-negotiation' \
--header 'Authorization: Bearer {{CLIENT_JWT_TOKEN}}'
```

**Business Logic:**
- Sets `clientConfirmed = true` on the project
- If `architectConfirmed` is also true → project transitions to `IN_PROGRESS`
- Backend auto-creates `ProjectPhase` records from `BidPaymentPhase` data

---

### 11. Confirm Negotiation (Architect)

**POST** `/rmtr/projects/{{PROJECT_ID}}/architect-confirm-negotiation`

Architect confirms the bid terms.

```bash
curl --location 'http://localhost:8080/rmtr/projects/1/architect-confirm-negotiation' \
--header 'Authorization: Bearer {{ARCHITECT_JWT_TOKEN}}'
```

**Business Logic:**
- Sets `architectConfirmed = true` on the project
- If `clientConfirmed` is also true → project transitions to `IN_PROGRESS`
- Only callable by the architect with the accepted bid

---

### 12. Reject Negotiation (Client)

**POST** `/rmtr/projects/{{PROJECT_ID}}/reject-negotiation`

Client rejects the negotiation. Project reopens for bidding (status → `OPEN`).

```bash
curl --location 'http://localhost:8080/rmtr/projects/1/reject-negotiation' \
--header 'Authorization: Bearer {{CLIENT_JWT_TOKEN}}'
```

**Business Logic:**
- Project status returns to `OPEN`
- Resets confirmation flags
- The previously accepted bid is rejected

---

### 13. Initialize Phases (Recovery)

**POST** `/rmtr/projects/{{PROJECT_ID}}/initialize-phases`

Manually creates `ProjectPhase` records from `BidPaymentPhase` data. Use this as a recovery tool for existing `IN_PROGRESS` projects that are missing phase records.

```bash
curl --location 'http://localhost:8080/rmtr/projects/1/initialize-phases' \
--header 'Authorization: Bearer {{CLIENT_JWT_TOKEN}}'
```

---

## DTO Reference

### ProjectResponse

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | |
| `clientId` | Long | |
| `title` | String | |
| `location` | String | |
| `designBudgetMin` | Long | |
| `designBudgetMax` | Long | |
| `projectCategory` | String | |
| `buildingFunction` | String | |
| `estimatedBuildArea` | Integer | |
| `numberOfFloors` | Integer | |
| `ownsLand` | Boolean | |
| `hasLegalDocuments` | Boolean | |
| `scopeOfWork` | String | |
| `deliverables` | List\<String\> | |
| `designPreferences` | String | |
| `contactPerson` | String | |
| `startDateType` | StartDateType | `IMMEDIATELY` or `SPECIFIC_DATE` |
| `expectedStartDate` | LocalDate | |
| `status` | ProjectStatus | See lifecycle above |
| `isValid` | Boolean | null = pending superuser review |
| `validationNotes` | String | Set by superuser on validation |
| `biddingDeadline` | LocalDateTime | |
| `files` | List\<ProjectFileDto\> | |
| `clientConfirmed` | Boolean | For NEGOTIATION phase |
| `architectConfirmed` | Boolean | For NEGOTIATION phase |
| `bidCount` | Long | Number of bids submitted |
| `createdAt` | LocalDateTime | |
| `updatedAt` | LocalDateTime | |

### ProjectFileDto

| Field | Type |
|-------|------|
| `id` | Long |
| `fileName` | String |
| `filePath` | String |
| `fileType` | String |
| `fileSize` | Long |
| `uploadedAt` | LocalDateTime |

---

## Enums

| Enum | Values |
|------|--------|
| `ProjectStatus` | `PENDING_APPROVAL`, `REJECTED`, `OPEN`, `BIDDING_CLOSED`, `NEGOTIATION`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `StartDateType` | `IMMEDIATELY`, `SPECIFIC_DATE` |

---

## Testing Sequence

1. **Login as Client** → Get JWT token
2. **Create Project** → `POST /rmtr/projects`
3. **Login as Superuser** → Get superuser JWT token
4. **View All Projects** → `GET /rmtr/projects/all`
5. **Validate Project** → `PUT /rmtr/projects/{id}/validate` with `isValid: true`
6. **Login as Architect** → Get architect JWT token
7. **Browse Open Projects** → `GET /rmtr/projects/open`
8. **View Project Details** → `GET /rmtr/projects/{id}/for-architect`
9. *(Architect creates and submits a bid via bidding API)*
10. **Client Views Bids** → `GET /rmtr/projects/{id}/bids`
11. *(Client accepts bid via bidding API — project → NEGOTIATION)*
12. **Client Confirms** → `POST /rmtr/projects/{id}/confirm-negotiation`
13. **Architect Confirms** → `POST /rmtr/projects/{id}/architect-confirm-negotiation`
14. *(Project → IN_PROGRESS, phases initialized automatically)*

---

## Error Responses

```json
{
  "success": false,
  "message": "Project not found",
  "timestamp": "2026-06-01T12:00:00"
}
```

| Status | Reason |
|--------|--------|
| `401 Unauthorized` | Invalid or expired JWT token |
| `403 Forbidden` | Not your project, or wrong role |
| `404 Not Found` | Project not found |
| `400 Bad Request` | Validation error or invalid project state transition |
