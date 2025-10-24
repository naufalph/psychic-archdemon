# Project API - cURL Commands for Postman Import

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token from client login
- Replace `{{CLIENT_ID}}` with your client ID (e.g., 1)
- Replace `{{PROJECT_ID}}` with the project ID from create response
- Replace file paths with actual document files on your system (PNG, JPG, or PDF)
- Budget values are in the smallest currency unit (e.g., cents for USD, sen for IDR)

---

## 1. Create Project with Files

**POST** `/api/v1/projects`

```bash
curl --location 'http://localhost:8080/api/v1/projects' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'clientId="1"' \
--form 'budgetMin="50000000"' \
--form 'budgetMax="100000000"' \
--form 'projectCategory="Residential"' \
--form 'buildingFunction="Single Family Home"' \
--form 'estimatedBuildArea="250"' \
--form 'numberOfFloors="2"' \
--form 'ownsLand="true"' \
--form 'hasLegalDocuments="true"' \
--form 'scopeOfWork="Complete architectural design including structural, MEP, and interior design"' \
--form 'deliverables=["Architectural Drawings","3D Renderings","Construction Documents","BOQ (Bill of Quantities)"]' \
--form 'designPreferences="Modern minimalist style with open floor plan and large windows for natural lighting"' \
--form 'contactPerson="John Doe - +628123456789"' \
--form 'expectedStartDate="2025-12-01"' \
--form 'files=@"/home/user/documents/site-plan.pdf"' \
--form 'files=@"/home/user/documents/land-certificate.jpg"'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Project created successfully!",
  "data": {
    "id": 1,
    "clientId": 1,
    "budgetMin": 50000000,
    "budgetMax": 100000000,
    "projectCategory": "Residential",
    "buildingFunction": "Single Family Home",
    "estimatedBuildArea": 250,
    "numberOfFloors": 2,
    "ownsLand": true,
    "hasLegalDocuments": true,
    "scopeOfWork": "Complete architectural design including structural, MEP, and interior design",
    "deliverables": [
      "Architectural Drawings",
      "3D Renderings",
      "Construction Documents",
      "BOQ (Bill of Quantities)"
    ],
    "designPreferences": "Modern minimalist style with open floor plan and large windows for natural lighting",
    "contactPerson": "John Doe - +628123456789",
    "expectedStartDate": "2025-12-01",
    "files": [
      {
        "id": 1,
        "fileName": "site-plan.pdf",
        "filePath": "uploads/projects/a1b2c3d4-e5f6-7890-1234-56789abcdef0.pdf",
        "fileType": "application/pdf",
        "fileSize": 245678,
        "uploadedAt": "2025-10-19T23:40:15.123456"
      },
      {
        "id": 2,
        "fileName": "land-certificate.jpg",
        "filePath": "uploads/projects/b2c3d4e5-f6a7-8901-2345-6789abcdef01.jpg",
        "fileType": "image/jpeg",
        "fileSize": 152340,
        "uploadedAt": "2025-10-19T23:40:15.234567"
      }
    ],
    "createdAt": "2025-10-19T23:40:15.012345",
    "updatedAt": null
  },
  "timestamp": "2025-10-19T23:40:15.345678"
}
```

---

## 2. Create Project WITHOUT Files

**POST** `/api/v1/projects`

```bash
curl --location 'http://localhost:8080/api/v1/projects' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'clientId="1"' \
--form 'budgetMin="30000000"' \
--form 'budgetMax="75000000"' \
--form 'projectCategory="Commercial"' \
--form 'buildingFunction="Office Building"' \
--form 'estimatedBuildArea="500"' \
--form 'numberOfFloors="3"' \
--form 'ownsLand="false"' \
--form 'hasLegalDocuments="false"' \
--form 'scopeOfWork="Architectural design and permit processing"' \
--form 'deliverables=["Conceptual Design","Permit Documents"]' \
--form 'designPreferences="Contemporary office design with efficient space utilization"' \
--form 'contactPerson="Jane Smith - +628234567890"' \
--form 'expectedStartDate="2026-01-15"'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Project created successfully!",
  "data": {
    "id": 2,
    "clientId": 1,
    "budgetMin": 30000000,
    "budgetMax": 75000000,
    "projectCategory": "Commercial",
    "buildingFunction": "Office Building",
    "estimatedBuildArea": 500,
    "numberOfFloors": 3,
    "ownsLand": false,
    "hasLegalDocuments": false,
    "scopeOfWork": "Architectural design and permit processing",
    "deliverables": [
      "Conceptual Design",
      "Permit Documents"
    ],
    "designPreferences": "Contemporary office design with efficient space utilization",
    "contactPerson": "Jane Smith - +628234567890",
    "expectedStartDate": "2026-01-15",
    "files": [],
    "createdAt": "2025-10-19T23:45:22.123456",
    "updatedAt": null
  },
  "timestamp": "2025-10-19T23:45:22.234567"
}
```

---

## 3. Get Single Project by ID

**GET** `/api/v1/projects/{{PROJECT_ID}}`

```bash
curl --location 'http://localhost:8080/api/v1/projects/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Project retrieved successfully",
  "data": {
    "id": 1,
    "clientId": 1,
    "budgetMin": 50000000,
    "budgetMax": 100000000,
    "projectCategory": "Residential",
    "buildingFunction": "Single Family Home",
    "estimatedBuildArea": 250,
    "numberOfFloors": 2,
    "ownsLand": true,
    "hasLegalDocuments": true,
    "scopeOfWork": "Complete architectural design including structural, MEP, and interior design",
    "deliverables": [
      "Architectural Drawings",
      "3D Renderings",
      "Construction Documents",
      "BOQ (Bill of Quantities)"
    ],
    "designPreferences": "Modern minimalist style with open floor plan and large windows for natural lighting",
    "contactPerson": "John Doe - +628123456789",
    "expectedStartDate": "2025-12-01",
    "files": [
      {
        "id": 1,
        "fileName": "site-plan.pdf",
        "filePath": "uploads/projects/a1b2c3d4-e5f6-7890-1234-56789abcdef0.pdf",
        "fileType": "application/pdf",
        "fileSize": 245678,
        "uploadedAt": "2025-10-19T23:40:15.123456"
      },
      {
        "id": 2,
        "fileName": "land-certificate.jpg",
        "filePath": "uploads/projects/b2c3d4e5-f6a7-8901-2345-6789abcdef01.jpg",
        "fileType": "image/jpeg",
        "fileSize": 152340,
        "uploadedAt": "2025-10-19T23:40:15.234567"
      }
    ],
    "createdAt": "2025-10-19T23:40:15.012345",
    "updatedAt": null
  },
  "timestamp": "2025-10-19T23:50:10.123456"
}
```

---

## 4. Get All Projects for a Client

**GET** `/api/v1/projects/client/{{CLIENT_ID}}`

```bash
curl --location 'http://localhost:8080/api/v1/projects/client/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Projects retrieved successfully",
  "data": [
    {
      "id": 2,
      "clientId": 1,
      "budgetMin": 30000000,
      "budgetMax": 75000000,
      "projectCategory": "Commercial",
      "buildingFunction": "Office Building",
      "estimatedBuildArea": 500,
      "numberOfFloors": 3,
      "ownsLand": false,
      "hasLegalDocuments": false,
      "scopeOfWork": "Architectural design and permit processing",
      "deliverables": [
        "Conceptual Design",
        "Permit Documents"
      ],
      "designPreferences": "Contemporary office design with efficient space utilization",
      "contactPerson": "Jane Smith - +628234567890",
      "expectedStartDate": "2026-01-15",
      "files": [],
      "createdAt": "2025-10-19T23:45:22.123456",
      "updatedAt": null
    },
    {
      "id": 1,
      "clientId": 1,
      "budgetMin": 50000000,
      "budgetMax": 100000000,
      "projectCategory": "Residential",
      "buildingFunction": "Single Family Home",
      "estimatedBuildArea": 250,
      "numberOfFloors": 2,
      "ownsLand": true,
      "hasLegalDocuments": true,
      "scopeOfWork": "Complete architectural design including structural, MEP, and interior design",
      "deliverables": [
        "Architectural Drawings",
        "3D Renderings",
        "Construction Documents",
        "BOQ (Bill of Quantities)"
      ],
      "designPreferences": "Modern minimalist style with open floor plan and large windows for natural lighting",
      "contactPerson": "John Doe - +628123456789",
      "expectedStartDate": "2025-12-01",
      "files": [
        {
          "id": 1,
          "fileName": "site-plan.pdf",
          "filePath": "uploads/projects/a1b2c3d4-e5f6-7890-1234-56789abcdef0.pdf",
          "fileType": "application/pdf",
          "fileSize": 245678,
          "uploadedAt": "2025-10-19T23:40:15.123456"
        },
        {
          "id": 2,
          "fileName": "land-certificate.jpg",
          "filePath": "uploads/projects/b2c3d4e5-f6a7-8901-2345-6789abcdef01.jpg",
          "fileType": "image/jpeg",
          "fileSize": 152340,
          "uploadedAt": "2025-10-19T23:40:15.234567"
        }
      ],
      "createdAt": "2025-10-19T23:40:15.012345",
      "updatedAt": null
    }
  ],
  "timestamp": "2025-10-19T23:52:30.123456"
}
```

---

## 5. Delete Project

**DELETE** `/api/v1/projects/{{PROJECT_ID}}`

```bash
curl --location --request DELETE 'http://localhost:8080/api/v1/projects/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Project deleted successfully",
  "data": null,
  "timestamp": "2025-10-19T23:55:45.123456"
}
```

---

## Notes

### Budget Values
- Budget is stored in the smallest currency unit (e.g., cents)
- Example: IDR 50,000,000 = 50000000 cents
- Frontend should convert display values accordingly

### Deliverables Format
- Deliverables are sent as a JSON array string
- Example: `'deliverables=["Item 1","Item 2","Item 3"]'`
- Backend stores this as JSONB in PostgreSQL

### File Upload Constraints
- Accepted file types: PNG, JPG, JPEG, PDF
- Files are validated server-side
- Invalid file types will be skipped with a warning log
- Files are stored in `uploads/projects/` directory
- File names are UUID-based to prevent conflicts

### Expected Start Date Format
- Format: `YYYY-MM-DD`
- Example: `2025-12-01`

### Error Responses

**Validation Error (400 Bad Request):**
```json
{
  "success": false,
  "message": "Maximum budget must be greater than minimum budget",
  "data": null,
  "timestamp": "2025-10-19T23:58:00.123456"
}
```

**Not Found (404 Not Found):**
```json
{
  "success": false,
  "message": "Project not found with id: 999",
  "data": null,
  "timestamp": "2025-10-19T23:59:00.123456"
}
```

**Server Error (500 Internal Server Error):**
```json
{
  "success": false,
  "message": "An error occurred while creating project",
  "data": null,
  "timestamp": "2025-10-20T00:00:00.123456"
}
```

---

## Testing Tips

1. **Create a client first** before creating projects
2. **Use valid file paths** on your local system
3. **Check file permissions** if upload fails
4. **Verify budget range** (budgetMax >= budgetMin)
5. **Test with and without files** to ensure both scenarios work
6. **Use proper JWT token** from client authentication

## Import to Postman

1. Copy the cURL commands above
2. In Postman, click "Import" → "Raw text"
3. Paste the cURL command
4. Postman will automatically create the request
5. Set up environment variables for `{{JWT_TOKEN}}`, `{{CLIENT_ID}}`, and `{{PROJECT_ID}}`
