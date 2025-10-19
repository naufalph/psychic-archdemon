# Porto API - cURL Commands for Postman Import

## Prerequisites
- Replace `{{JWT_TOKEN}}` with your actual JWT token from architect login
- Replace `{{ARCHITECT_ID}}` with your architect ID (e.g., 1)
- Replace `{{PORTO_ID}}` with the portfolio ID from create response
- Replace `{{IMAGE_ID}}` with the image ID from portfolio response
- Replace image file paths with actual image files on your system

---

## 1. Create Portfolio with Images

**POST** `/api/architects/{{ARCHITECT_ID}}/portos`

```bash
curl --location 'http://localhost:8080/api/architects/1/portos' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'title="Modern Villa Design"' \
--form 'description="A stunning modern villa with contemporary architecture and sustainable features"' \
--form 'location="Jakarta, Indonesia"' \
--form 'year="2024"' \
--form 'category="RESIDENTIAL"' \
--form 'images=@"/home/naufal-hadi/Downloads/nami.jpg"' \
--form 'isBuilt="true"' \
--form 'projectDate="2025-10-10"'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": 1,
      "architectId": 1,
      "title": "Modern Villa Design",
      "description": "A stunning modern villa with contemporary architecture and sustainable features",
      "projectDate": "2025-10-10",
      "location": "Jakarta, Indonesia",
      "projectType": null,
      "firstImage": {
        "id": 1,
        "originalUrl": "http://localhost:8080/uploads/1/1/89eb13e4-6073-49bf-902e-6b3481cf4d71_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/1/89eb13e4-6073-49bf-902e-6b3481cf4d71_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/1/89eb13e4-6073-49bf-902e-6b3481cf4d71_medium.jpg",
        "displayOrder": 0
      },
      "built": true
    }
  ],
  "timestamp": "2025-10-19T15:36:25.550021306"
}
```

---

## 2. Create Portfolio WITHOUT Images

**POST** `/api/architects/{{ARCHITECT_ID}}/portos`

```bash
curl --location 'http://localhost:8080/api/architects/1/portos' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'title="Office Building Design"' \
--form 'description="Modern commercial office space"' \
--form 'location="Surabaya, Indonesia"' \
--form 'year="2023"' \
--form 'category="COMMERCIAL"'
```

---

## 3. Get All Portfolios for an Architect

**GET** `/api/architects/{{ARCHITECT_ID}}/portos`

```bash
curl --location 'http://localhost:8080/api/architects/1/portos' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": 3,
      "architectId": 1,
      "title": "Modern Villa Design",
      "description": "A stunning modern villa with contemporary architecture and sustainable features",
      "projectDate": "2025-10-10",
      "location": "Jakarta, Indonesia",
      "projectType": null,
      "firstImage": {
        "id": 3,
        "originalUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_medium.jpg",
        "displayOrder": 0
      },
      "built": true
    }
  ],
  "timestamp": "2025-10-19T22:22:13.849299681"
}
```

---

## 4. Get Single Portfolio by ID

**GET** `/api/portos/{{PORTO_ID}}`

```bash
curl --location 'http://localhost:8080/api/portos/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": 3,
    "architectId": 1,
    "title": "Modern Villa Design",
    "description": "A stunning modern villa with contemporary architecture and sustainable features",
    "projectDate": "2025-10-10",
    "location": "Jakarta, Indonesia",
    "projectType": null,
    "images": [
      {
        "id": 3,
        "originalUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_medium.jpg",
        "displayOrder": 0
      },
      {
        "id": 4,
        "originalUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_medium.jpg",
        "displayOrder": 1
      }
    ],
    "built": true
  },
  "timestamp": "2025-10-19T22:24:35.118219194"
}
```

---

## 5. Update Portfolio Metadata

**PUT** `/api/portos/{{PORTO_ID}}`

```bash
curl --location --request PUT 'http://localhost:8080/api/portos/3' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "title": "Modern Villa Design - Updated",
  "description": "An award-winning modern villa with contemporary architecture, sustainable features, and eco-friendly materials",
  "location": "Bali, Indonesia CHANGE",
  "year": 2024,
  "category": "RESIDENTIAL"
}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": 3,
    "architectId": 1,
    "title": "Modern Villa Design - Updated",
    "description": "An award-winning modern villa with contemporary architecture, sustainable features, and eco-friendly materials",
    "projectDate": "2025-10-10",
    "location": "Bali, Indonesia CHANGE",
    "projectType": null,
    "images": [
      {
        "id": 3,
        "originalUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_medium.jpg",
        "displayOrder": 0
      },
      {
        "id": 4,
        "originalUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_medium.jpg",
        "displayOrder": 1
      }
    ],
    "built": true
  },
  "timestamp": "2025-10-19T22:27:47.084246996"
}
```

---

## 6. Add Images to Existing Portfolio

**POST** `/api/portos/{{PORTO_ID}}/images`

```bash
curl --location 'http://localhost:8080/api/portos/3/images' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--form 'images=@"/home/naufal-hadi/Desktop/safa/luffy.jpg"'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Images added successfully!",
  "data": {
    "id": 3,
    "architectId": 1,
    "title": "Modern Villa Design - Updated",
    "description": "An award-winning modern villa with contemporary architecture, sustainable features, and eco-friendly materials",
    "projectDate": "2025-10-10",
    "location": "Bali, Indonesia CHANGE",
    "projectType": null,
    "images": [
      {
        "id": 3,
        "originalUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/3/83ef7d70-8938-46a6-a1e3-0b78b34202a8_medium.jpg",
        "displayOrder": 0
      },
      {
        "id": 4,
        "originalUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/3/cac1208f-c5ad-44f9-98cd-aa0194f1b203_medium.jpg",
        "displayOrder": 1
      },
      {
        "id": 5,
        "originalUrl": "http://localhost:8080/uploads/1/3/335de1e2-3807-44fc-a76c-97a6f8adcc0b_original.jpg",
        "largeUrl": "http://localhost:8080/uploads/1/3/335de1e2-3807-44fc-a76c-97a6f8adcc0b_large.jpg",
        "mediumUrl": "http://localhost:8080/uploads/1/3/335de1e2-3807-44fc-a76c-97a6f8adcc0b_medium.jpg",
        "displayOrder": 2
      }
    ],
    "built": true
  },
  "timestamp": "2025-10-19T22:55:28.279344334"
}
```

---

## 7. Delete Specific Image

**DELETE** `/api/portos/images/{{IMAGE_ID}}`

```bash
curl --location --request DELETE 'http://localhost:8080/api/portos/images/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Image deleted successfully!",
  "data": null,
  "timestamp": "2025-10-19T22:57:01.813040026"
}
```

---

## 8. Delete Entire Portfolio

**DELETE** `/api/portos/{{PORTO_ID}}`

```bash
curl --location --request DELETE 'http://localhost:8080/api/portos/1' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Portfolio deleted successfully!",
  "data": null,
  "timestamp": "2025-10-19T22:58:31.982084524"
}
```
