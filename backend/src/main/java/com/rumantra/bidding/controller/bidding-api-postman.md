# Bidding System API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token from architect login
- Replace `{{BID_ID}}` with the bid ID from create response
- Replace `{{PROJECT_ID}}` with the project ID you want to bid on
- Replace `{{IMAGE_ID}}` with the image ID from upload response
- Replace image file paths with actual image files on your system

---

## General Bidding Flow for Frontend Implementation

### Complete Bid Submission Flow

```
1. ARCHITECT BROWSES OPEN PROJECTS
   ↓
2. ARCHITECT CREATES DRAFT BID (POST /api/bids)
   - Status: DRAFT
   - No quota deducted yet
   - Returns bidId
   ↓
3. ARCHITECT FILLS BID DETAILS (PUT /api/bids/{bidId}/details)
   - Concept statement (max 200 words, REQUIRED)
   - Project risks (optional)
   ↓
4. ARCHITECT UPLOADS CONCEPT SKETCHES (POST /api/bids/{bidId}/concept-sketches)
   - Minimum 1 required, maximum 3
   - Drag-and-drop file upload
   ↓
5. ARCHITECT UPLOADS MOOD BOARDS (POST /api/bids/{bidId}/mood-boards) [OPTIONAL]
   - No maximum limit
   - Visual inspiration/material palette
   ↓
6. ARCHITECT LINKS PORTFOLIOS (POST /api/bids/{bidId}/portfolios) [OPTIONAL]
   - Select from architect's existing portfolios
   - Maximum 3 portfolios
   ↓
7. ARCHITECT REVIEWS BID (GET /api/bids/{bidId})
   - Preview all details before submission
   - Check quota availability (GET /api/bids/quota)
   ↓
8. ARCHITECT SUBMITS BID (POST /api/bids/{bidId}/submit)
   - Validates completeness
   - Deducts quota
   - Status changes: DRAFT → PENDING
   - Bid becomes immutable
   ↓
9. CLIENT REVIEWS BID
   - Client sees PENDING bids
   ↓
10. BID OUTCOME
    ├─→ ACCEPTED (client accepts this bid)
    │   └─→ Can withdraw if negotiation fails
    ├─→ REJECTED (client accepts another bid)
    │   └─→ Cannot withdraw, no refund
    └─→ REFUNDED (project cancelled by client/superuser)
        └─→ Quota refunded automatically
```

### Frontend State Management Tips

1. **Draft Auto-Save**: Save draft bid details to backend on every change
2. **Progress Indicator**: Show completion percentage (details + images + portfolios)
3. **Validation Before Submit**: Disable submit button until minimum requirements met
4. **Quota Display**: Show remaining bids prominently before submission
5. **Image Preview**: Show uploaded concept sketches and mood boards with delete option
6. **Portfolio Cards**: Display linked portfolios with thumbnail preview

---

## API Endpoints

---

### 1. Create Draft Bid

**POST** `/api/bids`

Creates a new bid in DRAFT status. No quota deducted yet.

```bash
curl --location 'http://localhost:8080/api/bids' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "projectId": 1,
  "bidAmount": 50000000,
  "proposedTimelineDays": 180,
  "proposal": "I am excited to submit my proposal for this residential project. With over 10 years of experience in sustainable architecture, I believe I can deliver a design that perfectly balances modern aesthetics with environmental consciousness."
}'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Draft bid created successfully",
  "data": {
    "id": 1,
    "projectId": 1,
    "architectId": 2,
    "architectName": "John Doe",
    "architectCompany": "ABC Architecture Studio",
    "bidAmount": 50000000,
    "proposedTimelineDays": 180,
    "proposal": "I am excited to submit my proposal for this residential project...",
    "status": "DRAFT",
    "createdAt": "2025-12-03T10:00:00",
    "updatedAt": null,
    "submittedAt": null,
    "acceptedAt": null,
    "rejectedAt": null,
    "details": null,
    "conceptSketches": [],
    "moodBoards": [],
    "portfolioReferences": []
  },
  "timestamp": "2025-12-03T10:00:00"
}
```

---

### 2. Update Draft Bid (Basic Info)

**PUT** `/api/bids/{{BID_ID}}`

Updates bid amount, timeline, and proposal. Only works for DRAFT status.

```bash
curl --location --request PUT 'http://localhost:8080/api/bids/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "bidAmount": 55000000,
  "proposedTimelineDays": 200,
  "proposal": "Updated: I am excited to submit my proposal for this residential project. With over 10 years of experience in sustainable architecture and award-winning designs, I can deliver exceptional results."
}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Draft bid updated successfully",
  "data": {
    "id": 1,
    "projectId": 1,
    "architectId": 2,
    "architectName": "John Doe",
    "architectCompany": "ABC Architecture Studio",
    "bidAmount": 55000000,
    "proposedTimelineDays": 200,
    "proposal": "Updated: I am excited to submit my proposal...",
    "status": "DRAFT",
    "createdAt": "2025-12-03T10:00:00",
    "updatedAt": "2025-12-03T10:05:00",
    "submittedAt": null,
    "acceptedAt": null,
    "rejectedAt": null,
    "details": null,
    "conceptSketches": [],
    "moodBoards": [],
    "portfolioReferences": []
  },
  "timestamp": "2025-12-03T10:05:00"
}
```

---

### 3. Update Bid Details (Concept Statement & Risks)

**PUT** `/api/bids/{{BID_ID}}/details`

Updates or creates bid details. Only works for DRAFT status.

```bash
curl --location --request PUT 'http://localhost:8080/api/bids/1/details' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "conceptStatement": "This design integrates modern minimalism with traditional Indonesian architecture. The open-plan layout maximizes natural light and cross-ventilation while maintaining privacy through strategic placement of courtyards and green spaces. Key features include: sustainable local materials, rainwater harvesting system, solar panel integration, and biophilic design elements. The facade uses recycled teak wood combined with contemporary glass panels, creating a striking balance between heritage and innovation. Interior spaces flow seamlessly to outdoor gardens, emphasizing connection with nature. The design prioritizes energy efficiency with proper building orientation, natural cooling strategies, and smart home automation. Total of 145 words.",
  "projectRisks": "Potential challenges include: (1) Sourcing sustainable local materials within budget constraints - mitigation: established relationships with eco-certified suppliers. (2) Managing construction timeline during monsoon season - mitigation: detailed weather-contingent schedule with buffer periods. (3) Integrating traditional craftsmanship with modern techniques - mitigation: collaboration with experienced local artisans and regular quality checks."
}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Bid details updated successfully",
  "data": {
    "id": 1,
    "projectId": 1,
    "architectId": 2,
    "architectName": "John Doe",
    "architectCompany": "ABC Architecture Studio",
    "bidAmount": 55000000,
    "proposedTimelineDays": 200,
    "proposal": "Updated: I am excited to submit my proposal...",
    "status": "DRAFT",
    "createdAt": "2025-12-03T10:00:00",
    "updatedAt": "2025-12-03T10:10:00",
    "submittedAt": null,
    "acceptedAt": null,
    "rejectedAt": null,
    "details": {
      "id": 1,
      "conceptStatement": "This design integrates modern minimalism with traditional Indonesian architecture...",
      "projectRisks": "Potential challenges include: (1) Sourcing sustainable local materials..."
    },
    "conceptSketches": [],
    "moodBoards": [],
    "portfolioReferences": []
  },
  "timestamp": "2025-12-03T10:10:00"
}
```

**Validation:**
- Concept statement: Maximum 200 words (word count validation on backend)
- Concept statement is REQUIRED before bid submission

---

### 4. Upload Concept Sketches

**POST** `/api/bids/{{BID_ID}}/concept-sketches`

Uploads concept sketch images (max 3 total). Only works for DRAFT status.

```bash
curl --location 'http://localhost:8080/api/bids/1/concept-sketches' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'images=@"/home/naufal-hadi/Downloads/sketch1_front_elevation.jpg"' \
--form 'images=@"/home/naufal-hadi/Downloads/sketch2_floor_plan.png"' \
--form 'images=@"/home/naufal-hadi/Downloads/sketch3_section_view.jpg"'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Concept sketches uploaded successfully",
  "data": [
    {
      "id": 1,
      "imageType": "CONCEPT_SKETCH",
      "imageUrl": "http://localhost:8080/uploads/bids/1/concept_sketch/45a3c8e1-6073-49bf-902e-6b3481cf4d71_sketch1_front_elevation.jpg",
      "displayOrder": 1,
      "fileName": "sketch1_front_elevation.jpg",
      "fileSize": 2048576
    },
    {
      "id": 2,
      "imageType": "CONCEPT_SKETCH",
      "imageUrl": "http://localhost:8080/uploads/bids/1/concept_sketch/78b2d5f3-8938-46a6-a1e3-0b78b34202a8_sketch2_floor_plan.png",
      "displayOrder": 2,
      "fileName": "sketch2_floor_plan.png",
      "fileSize": 1536000
    },
    {
      "id": 3,
      "imageType": "CONCEPT_SKETCH",
      "imageUrl": "http://localhost:8080/uploads/bids/1/concept_sketch/92c4e7g5-3807-44fc-a76c-97a6f8adcc0b_sketch3_section_view.jpg",
      "displayOrder": 3,
      "fileName": "sketch3_section_view.jpg",
      "fileSize": 1843200
    }
  ],
  "timestamp": "2025-12-03T10:15:00"
}
```

**Validation:**
- Maximum 3 concept sketches per bid (checked against existing images)
- At least 1 concept sketch REQUIRED before bid submission
- Only allowed for DRAFT status

**Error Response (400 Bad Request) - Bid Not Draft:**
```json
{
  "timestamp": "2025-12-08T10:15:00",
  "status": 400,
  "errorCode": "BID_NOT_DRAFT",
  "path": "uri=/api/bids/1/concept-sketches"
}
```

**Frontend Implementation Tip:**
- Show error if trying to upload when already have 3 images
- Display current count: "2/3 concept sketches uploaded"
- Allow drag-and-drop upload
- Disable upload button if bid status is not DRAFT
- Translate errorCode "BID_NOT_DRAFT" to user-friendly message in appropriate language

---

### 5. Upload Mood Boards

**POST** `/api/bids/{{BID_ID}}/mood-boards`

Uploads mood board/inspiration images. No maximum limit. Only works for DRAFT status.

```bash
curl --location 'http://localhost:8080/api/bids/1/mood-boards' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'images=@"/home/naufal-hadi/Downloads/mood_material_palette.jpg"' \
--form 'images=@"/home/naufal-hadi/Downloads/mood_color_scheme.png"' \
--form 'images=@"/home/naufal-hadi/Downloads/mood_furniture_inspiration.jpg"'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Mood boards uploaded successfully",
  "data": [
    {
      "id": 4,
      "imageType": "MOOD_BOARD",
      "imageUrl": "http://localhost:8080/uploads/bids/1/mood_board/12d3e4f5-9876-54cd-b123-456789abcdef_mood_material_palette.jpg",
      "displayOrder": 1,
      "fileName": "mood_material_palette.jpg",
      "fileSize": 1024000
    },
    {
      "id": 5,
      "imageType": "MOOD_BOARD",
      "imageUrl": "http://localhost:8080/uploads/bids/1/mood_board/34e5f6a7-8765-43cd-a987-654321fedcba_mood_color_scheme.png",
      "displayOrder": 2,
      "fileName": "mood_color_scheme.png",
      "fileSize": 896000
    },
    {
      "id": 6,
      "imageType": "MOOD_BOARD",
      "imageUrl": "http://localhost:8080/uploads/bids/1/mood_board/56f7g8h9-7654-32cd-8765-432109876543_mood_furniture_inspiration.jpg",
      "displayOrder": 3,
      "fileName": "mood_furniture_inspiration.jpg",
      "fileSize": 1152000
    }
  ],
  "timestamp": "2025-12-03T10:20:00"
}
```

**Validation:**
- No maximum limit
- Optional for submission
- Only allowed for DRAFT status

**Error Response (400 Bad Request) - Bid Not Draft:**
```json
{
  "timestamp": "2025-12-08T10:20:00",
  "status": 400,
  "errorCode": "BID_NOT_DRAFT",
  "path": "uri=/api/bids/1/mood-boards"
}
```

---

### 6. Link Portfolios to Bid

**POST** `/api/bids/{{BID_ID}}/portfolios`

Links architect's existing portfolios to the bid (max 3). Only works for DRAFT status.

```bash
curl --location 'http://localhost:8080/api/bids/1/portfolios' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "portfolioIds": [5, 12, 18]
}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Portfolios linked successfully",
  "data": {
    "id": 1,
    "projectId": 1,
    "architectId": 2,
    "architectName": "John Doe",
    "architectCompany": "ABC Architecture Studio",
    "bidAmount": 55000000,
    "proposedTimelineDays": 200,
    "proposal": "Updated: I am excited to submit my proposal...",
    "status": "DRAFT",
    "createdAt": "2025-12-03T10:00:00",
    "updatedAt": "2025-12-03T10:25:00",
    "submittedAt": null,
    "acceptedAt": null,
    "rejectedAt": null,
    "details": {
      "id": 1,
      "conceptStatement": "This design integrates modern minimalism...",
      "projectRisks": "Potential challenges include..."
    },
    "conceptSketches": [
      {
        "id": 1,
        "imageType": "CONCEPT_SKETCH",
        "imageUrl": "http://localhost:8080/uploads/bids/1/concept_sketch/45a3c8e1-6073-49bf-902e-6b3481cf4d71_sketch1_front_elevation.jpg",
        "displayOrder": 1,
        "fileName": "sketch1_front_elevation.jpg",
        "fileSize": 2048576
      }
    ],
    "moodBoards": [
      {
        "id": 4,
        "imageType": "MOOD_BOARD",
        "imageUrl": "http://localhost:8080/uploads/bids/1/mood_board/12d3e4f5-9876-54cd-b123-456789abcdef_mood_material_palette.jpg",
        "displayOrder": 1,
        "fileName": "mood_material_palette.jpg",
        "fileSize": 1024000
      }
    ],
    "portfolioReferences": [
      {
        "id": 5,
        "architectId": 2,
        "title": "Modern Villa in Ubud",
        "description": "A contemporary take on traditional Balinese architecture",
        "projectDate": "2023-06-15",
        "location": "Ubud, Bali",
        "projectType": "Residential",
        "isBuilt": true,
        "firstImage": {
          "id": 23,
          "originalUrl": "http://localhost:8080/uploads/2/5/portfolio_img1_original.jpg",
          "largeUrl": "http://localhost:8080/uploads/2/5/portfolio_img1_large.jpg",
          "mediumUrl": "http://localhost:8080/uploads/2/5/portfolio_img1_medium.jpg",
          "displayOrder": 0
        }
      },
      {
        "id": 12,
        "architectId": 2,
        "title": "Sustainable Office Complex Jakarta",
        "description": "Green building certified commercial space",
        "projectDate": "2024-01-20",
        "location": "Jakarta",
        "projectType": "Commercial",
        "isBuilt": true,
        "firstImage": {
          "id": 45,
          "originalUrl": "http://localhost:8080/uploads/2/12/portfolio_img1_original.jpg",
          "largeUrl": "http://localhost:8080/uploads/2/12/portfolio_img1_large.jpg",
          "mediumUrl": "http://localhost:8080/uploads/2/12/portfolio_img1_medium.jpg",
          "displayOrder": 0
        }
      },
      {
        "id": 18,
        "architectId": 2,
        "title": "Minimalist Beach House",
        "description": "Coastal contemporary residential design",
        "projectDate": "2023-09-10",
        "location": "Canggu, Bali",
        "projectType": "Residential",
        "isBuilt": false,
        "firstImage": {
          "id": 67,
          "originalUrl": "http://localhost:8080/uploads/2/18/portfolio_img1_original.jpg",
          "largeUrl": "http://localhost:8080/uploads/2/18/portfolio_img1_large.jpg",
          "mediumUrl": "http://localhost:8080/uploads/2/18/portfolio_img1_medium.jpg",
          "displayOrder": 0
        }
      }
    ]
  },
  "timestamp": "2025-12-03T10:25:00"
}
```

**Validation:**
- Maximum 3 portfolios
- Portfolios must belong to the architect (ownership check)
- Optional for submission
- Only allowed for DRAFT status
- Replaces previous portfolio links (not additive)

**Frontend Implementation Tip:**
- Show portfolio selection modal with architect's portfolios
- Display selected portfolios with preview cards
- Show "3/3 portfolios selected" counter

---

### 7. Delete Image (Concept Sketch or Mood Board)

**DELETE** `/api/bids/images/{{IMAGE_ID}}`

Deletes a specific image from the bid.

```bash
curl --location --request DELETE 'http://localhost:8080/api/bids/images/4' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Image deleted successfully",
  "timestamp": "2025-12-03T10:30:00"
}
```

**Validation:**
- Only allowed for DRAFT status bids
- Image must belong to architect's bid (ownership check)
- File deleted from storage

**Error Response (400 Bad Request) - Bid Not Draft:**
```json
{
  "timestamp": "2025-12-08T10:30:00",
  "status": 400,
  "errorCode": "BID_NOT_DRAFT",
  "path": "uri=/api/bids/images/4"
}
```

**Error Response (404 Not Found) - Image Not Found:**
```json
{
  "timestamp": "2025-12-08T10:30:00",
  "status": 404,
  "errorCode": "BID_IMAGE_NOT_FOUND",
  "path": "uri=/api/bids/images/999"
}
```

---

### 8. Check Bid Quota Before Submission

**GET** `/api/bids/quota`

Retrieves current bid quota for the authenticated architect.

```bash
curl --location 'http://localhost:8080/api/bids/quota' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Quota retrieved successfully",
  "data": {
    "id": 1,
    "architectId": 2,
    "subscriptionTier": "FREE",
    "bidsRemaining": 2,
    "totalBids": 3,
    "resetInterval": "BI_WEEKLY",
    "nextResetDate": "2025-12-14"
  },
  "timestamp": "2025-12-03T10:32:00"
}
```

**Frontend Implementation Tip:**
- Display quota prominently: "2 bids remaining"
- Show next reset date
- Warn if bidsRemaining = 0
- Show upgrade to premium option

---

### 9. Submit Bid (Final Step)

**POST** `/api/bids/{{BID_ID}}/submit`

Submits the draft bid. Validates completeness and deducts quota.

```bash
curl --location 'http://localhost:8080/api/bids/1/submit' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Bid submitted successfully",
  "data": {
    "id": 1,
    "projectId": 1,
    "architectId": 2,
    "architectName": "John Doe",
    "architectCompany": "ABC Architecture Studio",
    "bidAmount": 55000000,
    "proposedTimelineDays": 200,
    "proposal": "Updated: I am excited to submit my proposal...",
    "status": "PENDING",
    "createdAt": "2025-12-03T10:00:00",
    "updatedAt": "2025-12-03T10:35:00",
    "submittedAt": "2025-12-03T10:35:00",
    "acceptedAt": null,
    "rejectedAt": null,
    "details": {
      "id": 1,
      "conceptStatement": "This design integrates modern minimalism...",
      "projectRisks": "Potential challenges include..."
    },
    "conceptSketches": [
      {
        "id": 1,
        "imageType": "CONCEPT_SKETCH",
        "imageUrl": "http://localhost:8080/uploads/bids/1/concept_sketch/45a3c8e1-6073-49bf-902e-6b3481cf4d71_sketch1_front_elevation.jpg",
        "displayOrder": 1,
        "fileName": "sketch1_front_elevation.jpg",
        "fileSize": 2048576
      },
      {
        "id": 2,
        "imageType": "CONCEPT_SKETCH",
        "imageUrl": "http://localhost:8080/uploads/bids/1/concept_sketch/78b2d5f3-8938-46a6-a1e3-0b78b34202a8_sketch2_floor_plan.png",
        "displayOrder": 2,
        "fileName": "sketch2_floor_plan.png",
        "fileSize": 1536000
      }
    ],
    "moodBoards": [
      {
        "id": 4,
        "imageType": "MOOD_BOARD",
        "imageUrl": "http://localhost:8080/uploads/bids/1/mood_board/12d3e4f5-9876-54cd-b123-456789abcdef_mood_material_palette.jpg",
        "displayOrder": 1,
        "fileName": "mood_material_palette.jpg",
        "fileSize": 1024000
      }
    ],
    "portfolioReferences": [
      {
        "id": 5,
        "architectId": 2,
        "title": "Modern Villa in Ubud",
        "description": "A contemporary take on traditional Balinese architecture",
        "projectDate": "2023-06-15",
        "location": "Ubud, Bali",
        "projectType": "Residential",
        "isBuilt": true,
        "firstImage": {
          "id": 23,
          "originalUrl": "http://localhost:8080/uploads/2/5/portfolio_img1_original.jpg",
          "largeUrl": "http://localhost:8080/uploads/2/5/portfolio_img1_large.jpg",
          "mediumUrl": "http://localhost:8080/uploads/2/5/portfolio_img1_medium.jpg",
          "displayOrder": 0
        }
      }
    ]
  },
  "timestamp": "2025-12-03T10:35:00"
}
```

**Validation Before Submission:**
1. Concept statement must exist and not be empty
2. At least 1 concept sketch required
3. Architect must have bidsRemaining > 0
4. Bid must be in DRAFT status

**Side Effects:**
- `status` changes: DRAFT → PENDING
- `submittedAt` timestamp set
- Quota deducted (bidsRemaining - 1)
- Usage logged in `rmtr_bid_usage_log`
- Bid becomes immutable (cannot edit/delete)

**Error Response (400 Bad Request) - No Quota:**
```json
{
  "success": false,
  "message": "No bids remaining. Upgrade to premium or wait for reset on 2025-12-14",
  "timestamp": "2025-12-03T10:35:00"
}
```

**Error Response (400 Bad Request) - Missing Concept Statement:**
```json
{
  "success": false,
  "message": "Concept statement is required before submission",
  "timestamp": "2025-12-03T10:35:00"
}
```

**Error Response (400 Bad Request) - Missing Concept Sketch:**
```json
{
  "success": false,
  "message": "At least 1 concept sketch is required before submission",
  "timestamp": "2025-12-03T10:35:00"
}
```

---

### 10. Get Bid by ID (Preview/Review)

**GET** `/api/bids/{{BID_ID}}`

Retrieves a specific bid with all nested data (details, images, portfolios).

```bash
curl --location 'http://localhost:8080/api/bids/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Bid retrieved successfully",
  "data": {
    "id": 1,
    "projectId": 1,
    "architectId": 2,
    "architectName": "John Doe",
    "architectCompany": "ABC Architecture Studio",
    "bidAmount": 55000000,
    "proposedTimelineDays": 200,
    "proposal": "Updated: I am excited to submit my proposal...",
    "status": "PENDING",
    "createdAt": "2025-12-03T10:00:00",
    "updatedAt": "2025-12-03T10:35:00",
    "submittedAt": "2025-12-03T10:35:00",
    "acceptedAt": null,
    "rejectedAt": null,
    "details": {
      "id": 1,
      "conceptStatement": "This design integrates modern minimalism...",
      "projectRisks": "Potential challenges include..."
    },
    "conceptSketches": [...],
    "moodBoards": [...],
    "portfolioReferences": [...]
  },
  "timestamp": "2025-12-03T10:40:00"
}
```

**Authorization:**
- Architect can only view their own bids

**Frontend Implementation Tip:**
- Use for preview before submission
- Show completion checklist:
  - ✅ Basic info (amount, timeline, proposal)
  - ✅ Concept statement
  - ✅ At least 1 concept sketch (1/3)
  - ✅ Mood boards (optional, 3 uploaded)
  - ✅ Portfolios linked (optional, 3/3)

---

### 11. Get All My Bids

**GET** `/api/bids/my-bids`

Retrieves all bids for the authenticated architect.

```bash
curl --location 'http://localhost:8080/api/bids/my-bids' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Bids retrieved successfully",
  "data": [
    {
      "id": 1,
      "projectId": 1,
      "architectId": 2,
      "architectName": "John Doe",
      "architectCompany": "ABC Architecture Studio",
      "bidAmount": 55000000,
      "proposedTimelineDays": 200,
      "proposal": "Updated: I am excited to submit my proposal...",
      "status": "PENDING",
      "createdAt": "2025-12-03T10:00:00",
      "updatedAt": "2025-12-03T10:35:00",
      "submittedAt": "2025-12-03T10:35:00",
      "acceptedAt": null,
      "rejectedAt": null,
      "details": {...},
      "conceptSketches": [...],
      "moodBoards": [...],
      "portfolioReferences": [...]
    },
    {
      "id": 2,
      "projectId": 3,
      "architectId": 2,
      "architectName": "John Doe",
      "architectCompany": "ABC Architecture Studio",
      "bidAmount": 30000000,
      "proposedTimelineDays": 120,
      "proposal": "Draft proposal for office renovation project",
      "status": "DRAFT",
      "createdAt": "2025-12-02T14:00:00",
      "updatedAt": "2025-12-02T15:30:00",
      "submittedAt": null,
      "acceptedAt": null,
      "rejectedAt": null,
      "details": null,
      "conceptSketches": [],
      "moodBoards": [],
      "portfolioReferences": []
    }
  ],
  "timestamp": "2025-12-03T10:45:00"
}
```

**Frontend Implementation Tip:**
- Separate tabs/filters for different statuses:
  - DRAFT (editable, show "Continue editing" button)
  - PENDING (under review, show status badge)
  - ACCEPTED (show client contact info)
  - REJECTED (archived, show reason if available)
  - WITHDRAWN (archived)
  - REFUNDED (quota refunded, show refund badge)

---

### 12. Withdraw Bid

**PUT** `/api/bids/{{BID_ID}}/withdraw`

Withdraws a bid. Only allowed for DRAFT or ACCEPTED status.

```bash
curl --location --request PUT 'http://localhost:8080/api/bids/1/withdraw' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Bid withdrawn successfully",
  "timestamp": "2025-12-03T10:50:00"
}
```

**Business Rules:**
- **DRAFT withdrawal**: Allowed (no quota impact since not submitted)
- **ACCEPTED withdrawal**: Allowed (negotiation failed scenario, no quota refund)
- **PENDING withdrawal**: NOT allowed (under client review)
- **REJECTED withdrawal**: NOT allowed (already closed)

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Can only withdraw draft or accepted bids. Current status: PENDING",
  "timestamp": "2025-12-03T10:50:00"
}
```

**Frontend Implementation Tip:**
- Show "Withdraw" button only for DRAFT and ACCEPTED bids
- Confirmation dialog: "Are you sure you want to withdraw this bid? This action cannot be undone."
- For ACCEPTED: Explain no quota refund

---

## Error Responses

All error responses with error codes use the following format for internationalization support:

```json
{
  "timestamp": "2025-12-08T11:00:00",
  "status": 400,
  "errorCode": "BID_NOT_DRAFT",
  "path": "uri=/api/bids/1/details"
}
```

### Common Error Codes

| Error Code | HTTP Status | Description |
|-----------|-------------|-------------|
| `BID_NOT_DRAFT` | 400 | Operation only allowed for draft bids |
| `BID_NOT_FOUND` | 404 | Bid does not exist |
| `BID_DETAIL_NOT_FOUND` | 404 | Bid detail does not exist |
| `BID_IMAGE_NOT_FOUND` | 404 | Image does not exist |
| `UNAUTHORIZED_BID_ACCESS` | 403 | Not authorized to access this bid |
| `ARCHITECT_NOT_FOUND` | 404 | Architect profile not found |
| `PROJECT_NOT_FOUND` | 404 | Project does not exist |
| `INVALID_BID_STATUS` | 400 | Invalid bid status for operation |

### Frontend Error Handling

Frontend should:
1. Check for `errorCode` field in error response
2. Translate error code to user-friendly message based on language
3. Display appropriate error message to user

Example translations:
- `BID_NOT_DRAFT` (EN): "This bid has already been submitted and cannot be modified"
- `BID_NOT_DRAFT` (ID): "Bid ini sudah disubmit dan tidak dapat diubah"
- `BID_NOT_FOUND` (EN): "Bid not found"
- `BID_NOT_FOUND` (ID): "Bid tidak ditemukan"

---

## Frontend Implementation Guidelines

### 1. Bid Creation Page (Multi-Step Form)

**Step 1: Basic Information**
- Project selection (dropdown of OPEN projects)
- Bid amount (number input with currency format)
- Proposed timeline (number input in days)
- Initial proposal (textarea)
- Action: "Save Draft" → Creates DRAFT bid

**Step 2: Concept Statement**
- Concept statement (rich text editor, max 200 words)
- Real-time word counter: "145/200 words"
- Project risks (textarea, optional)
- Action: "Save & Continue" → Updates bid details

**Step 3: Concept Sketches**
- Drag-and-drop file upload
- Image preview gallery
- Delete button for each image
- Counter: "2/3 sketches uploaded"
- Validation: At least 1 required
- Action: "Save & Continue"

**Step 4: Mood Boards (Optional)**
- Drag-and-drop file upload
- Image preview gallery
- Delete button for each image
- No maximum limit
- Action: "Save & Continue"

**Step 5: Portfolio References (Optional)**
- Portfolio selection grid (architect's portfolios)
- Multi-select (max 3)
- Preview cards with thumbnail
- Counter: "3/3 portfolios selected"
- Action: "Save & Continue"

**Step 6: Review & Submit**
- Summary of all bid information
- Completion checklist
- Quota display: "2 bids remaining"
- Warning if quota = 0
- Action: "Submit Bid" → POST /api/bids/{bidId}/submit

### 2. My Bids Page

**Layout:**
- Tabs: All | Draft | Pending | Accepted | Rejected
- Card-based list view
- Each card shows:
  - Project title/thumbnail
  - Bid amount
  - Status badge
  - Submitted date (if applicable)
  - Action buttons (Edit/View/Withdraw)

**Draft Bids:**
- "Continue Editing" button
- "Delete Draft" option
- Progress indicator: "60% complete"

**Pending Bids:**
- "Under Review" badge
- View-only mode
- No edit/delete options

**Accepted Bids:**
- "Accepted" badge (green)
- Client contact information
- "Withdraw" option (with warning)

**Rejected Bids:**
- "Rejected" badge (red)
- View-only mode
- Archive option

### 3. Quota Display Component

**Location:** Header/Dashboard
**Display:**
- "2 bids remaining"
- Progress bar: 1/3 used
- Next reset: "Resets in 11 days (Dec 14)"
- "Upgrade to Premium" button if FREE tier

### 4. Validation & Error Handling

**Client-Side Validation:**
- Bid amount > 0
- Timeline > 0
- Proposal not empty
- Concept statement word count ≤ 200
- At least 1 concept sketch before submit
- Max 3 concept sketches
- Max 3 portfolios

**Server-Side Error Display:**
- Toast notifications for errors
- Inline validation messages
- Quota warning modal

### 5. Real-Time Features (Optional)

**Auto-Save Draft:**
- Debounced auto-save every 30 seconds
- "Saving..." indicator
- "Draft saved at 10:35 AM"

**Bid Status Updates:**
- WebSocket/polling for status changes
- Notification when bid accepted/rejected

---

## Testing Sequence for Postman/Bruno

1. **Login as Architect** → Get JWT token
2. **Check Quota** → `GET /api/bids/quota`
3. **Browse Projects** → `GET /api/v1/projects` (with status=OPEN filter)
4. **Create Draft Bid** → `POST /api/bids` → Save bidId
5. **Update Basic Info** → `PUT /api/bids/{bidId}`
6. **Add Concept Statement** → `PUT /api/bids/{bidId}/details`
7. **Upload Concept Sketches** → `POST /api/bids/{bidId}/concept-sketches`
8. **Upload Mood Boards** → `POST /api/bids/{bidId}/mood-boards`
9. **Link Portfolios** → `POST /api/bids/{bidId}/portfolios`
10. **Preview Bid** → `GET /api/bids/{bidId}`
11. **Submit Bid** → `POST /api/bids/{bidId}/submit`
12. **Verify Submission** → `GET /api/bids/{bidId}` (check status=PENDING)
13. **Check Updated Quota** → `GET /api/bids/quota` (verify deduction)
14. **View All Bids** → `GET /api/bids/my-bids`

---

## Database Schema Quick Reference

- **rmtr_bid**: Main bid table (status, amount, timeline)
- **rmtr_bid_detail**: Concept statement & project risks
- **rmtr_bid_image**: Images (CONCEPT_SKETCH, MOOD_BOARD)
- **rmtr_bid_portfolio_ref**: Portfolio links (max 3)
- **rmtr_bid_quota**: Quota tracking per architect
- **rmtr_bid_usage_log**: Usage history for analytics
