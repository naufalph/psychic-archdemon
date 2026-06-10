# Porto (Portfolio) API - cURL Commands for Postman/Bruno

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token (architect)
- Replace `{{PORTO_ID}}` with the portfolio ID from response
- Replace `{{IMAGE_ID}}` with the image ID from response
- All endpoints require `ROLE_ARCHITECT`

**Base URL:** `http://localhost:8080/rmtr/porto`

**Important:** The architect ID is derived from the authenticated user's JWT — do NOT pass it as a path or body parameter.

---

## API Endpoints

---

### 1. Create Portfolio

**POST** `/rmtr/porto`

**Content-Type:** `multipart/form-data`

```bash
# With images
curl --location 'http://localhost:8080/rmtr/porto' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'title="Modern Residential House"' \
--form 'description="A 250sqm family home in South Jakarta featuring biophilic design principles."' \
--form 'projectDate="2025-03-15"' \
--form 'location="Jakarta Selatan"' \
--form 'projectType="Residential"' \
--form 'isBuilt="true"' \
--form 'images=@"/home/user/photos/exterior.jpg"' \
--form 'images=@"/home/user/photos/interior.jpg"'

# Without images
curl --location 'http://localhost:8080/rmtr/porto' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'title="Office Tower Concept"' \
--form 'projectDate="2024-08-01"' \
--form 'isBuilt="false"'
```

**Form Fields:**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `title` | String | Yes | |
| `description` | String | No | |
| `projectDate` | LocalDate (`YYYY-MM-DD`) | Yes | |
| `location` | String | No | |
| `projectType` | String | No | e.g. "Residential", "Commercial" |
| `isBuilt` | Boolean | Yes | `true` = built project, `false` = concept/unbuilt |
| `images` | MultipartFile[] | No | Multiple files accepted |

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "architectId": 42,
    "title": "Modern Residential House",
    "description": "A 250sqm family home in South Jakarta.",
    "projectDate": "2025-03-15",
    "location": "Jakarta Selatan",
    "projectType": "Residential",
    "isBuilt": true,
    "images": [
      {
        "id": 10,
        "originalUrl": "https://storage.example.com/porto/1/exterior_orig.jpg",
        "largeUrl": "https://storage.example.com/porto/1/exterior_large.jpg",
        "mediumUrl": "https://storage.example.com/porto/1/exterior_medium.jpg",
        "displayOrder": 1
      }
    ]
  },
  "timestamp": "2026-06-01T10:00:00"
}
```

---

### 2. Get My Portfolios

**GET** `/rmtr/porto`

Returns all portfolios for the authenticated architect.

```bash
curl --location 'http://localhost:8080/rmtr/porto' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):** `ApiResponse<List<PortoListResponse>>`

Each item in the list:
```json
{
  "id": 1,
  "architectId": 42,
  "title": "Modern Residential House",
  "description": "A 250sqm family home in South Jakarta.",
  "projectDate": "2025-03-15",
  "location": "Jakarta Selatan",
  "projectType": "Residential",
  "isBuilt": true,
  "images": [
    {
      "id": 10,
      "originalUrl": "https://storage.example.com/porto/1/img_orig.jpg",
      "largeUrl": "https://storage.example.com/porto/1/img_large.jpg",
      "mediumUrl": "https://storage.example.com/porto/1/img_medium.jpg",
      "displayOrder": 1
    }
  ]
}
```

---

### 3. Get Portfolio by ID

**GET** `/rmtr/porto/{{PORTO_ID}}`

```bash
curl --location 'http://localhost:8080/rmtr/porto/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

Returns `ApiResponse<PortoResponse>` (same structure as create response).

**Authorization:** Only the portfolio owner can access this endpoint.

---

### 4. Update Portfolio

**PUT** `/rmtr/porto/{{PORTO_ID}}`

All fields are optional — only provided fields are updated.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/porto/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "title": "Modern Residential House - Revised",
  "description": "Updated description with award recognition.",
  "projectDate": "2025-06-01",
  "location": "Jakarta Selatan, DKI Jakarta",
  "projectType": "Residential",
  "isBuilt": true
}'
```

**Request Body (all optional):**
| Field | Type |
|-------|------|
| `title` | String |
| `description` | String |
| `projectDate` | LocalDate |
| `location` | String |
| `projectType` | String |
| `isBuilt` | Boolean |

Returns `ApiResponse<PortoResponse>`.

---

### 5. Delete Portfolio

**DELETE** `/rmtr/porto/{{PORTO_ID}}`

```bash
curl --location --request DELETE 'http://localhost:8080/rmtr/porto/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

### 6. Add Images to Portfolio

**POST** `/rmtr/porto/{{PORTO_ID}}/images`

Upload additional images to an existing portfolio.

```bash
curl --location 'http://localhost:8080/rmtr/porto/1/images' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'images=@"/home/user/photos/new_angle.jpg"' \
--form 'images=@"/home/user/photos/detail.jpg"'
```

Returns `ApiResponse<PortoResponse>` with the full updated portfolio including all images.

---

### 7. Delete Portfolio Image

**DELETE** `/rmtr/porto/images/{{IMAGE_ID}}`

```bash
curl --location --request DELETE 'http://localhost:8080/rmtr/porto/images/10' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

---

## DTO Reference

### PortoResponse / PortoListResponse

Both have identical fields:

| Field | Type |
|-------|------|
| `id` | Long |
| `architectId` | Long |
| `title` | String |
| `description` | String |
| `projectDate` | LocalDate |
| `location` | String |
| `projectType` | String |
| `isBuilt` | boolean |
| `images` | List\<PortoDetailResponse\> |

### PortoDetailResponse (image item)

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | |
| `originalUrl` | String | Full-resolution image |
| `largeUrl` | String | Resized to 1920px width |
| `mediumUrl` | String | Resized to 800px width |
| `displayOrder` | int | 1-based ordering |

---

## Testing Sequence

1. **Login as Architect** → Get JWT token
2. **Create Portfolio** → `POST /rmtr/porto` with images
3. **View My Portfolios** → `GET /rmtr/porto`
4. **View Single Portfolio** → `GET /rmtr/porto/{id}`
5. **Update Portfolio** → `PUT /rmtr/porto/{id}`
6. **Add More Images** → `POST /rmtr/porto/{id}/images`
7. **Delete an Image** → `DELETE /rmtr/porto/images/{imageId}`
8. **Delete Portfolio** → `DELETE /rmtr/porto/{id}`

---

## Error Responses

| Status | Reason |
|--------|--------|
| `401 Unauthorized` | Invalid or expired JWT token |
| `403 Forbidden` | Not your portfolio |
| `404 Not Found` | Portfolio or image not found |
| `400 Bad Request` | Validation error (missing required fields) |
