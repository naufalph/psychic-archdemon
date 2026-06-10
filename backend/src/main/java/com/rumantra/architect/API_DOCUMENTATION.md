# Architect API Documentation

## Overview
This document describes the API endpoints for the Architect module in the Rumantra platform.

**Base URL:** `http://localhost:8080/rmtr/architects`

---

## Endpoints

---

### 1. Register Architect

**POST** `/rmtr/architects/register`

Creates a new architect account. This endpoint requires an authenticated base user — call `POST /rmtr/users/register` and verify email first, then call this endpoint to activate the ARCHITECT role.

```bash
curl --location 'http://localhost:8080/rmtr/architects/register' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "userName": "pt_karya_desain",
  "email": "info@karyadesain.com",
  "password": "SecurePass123!",
  "companyName": "PT Karya Desain",
  "companySite": "https://karyadesain.com",
  "contactName": "Budi Santoso",
  "phoneNum": "08123456789012345",
  "category": "Residential",
  "ktpNum": "3273012345678901",
  "npwp": "123456789012345"
}'
```

**Request Body:**
| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| `userName` | String | Yes | 3–50 chars, alphanumeric + underscore |
| `email` | String | Yes | Valid email |
| `password` | String | Yes | Min 8 chars, must include digit, lowercase, uppercase, special char |
| `companyName` | String | Yes | Max 255 chars |
| `companySite` | String | No | Valid URL, max 255 chars |
| `contactName` | String | Yes | Max 255 chars |
| `phoneNum` | String | Yes | Exactly 16 digits |
| `category` | String | Yes | Architect specialization category |
| `ktpNum` | String | Yes | Exactly 16 digits |
| `npwp` | String | Yes | 15–16 digits |

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 42,
    "email": "info@karyadesain.com",
    "companyName": "PT Karya Desain",
    "companySite": "https://karyadesain.com",
    "category": "Residential",
    "phoneNumber": "08123456789012345",
    "contactName": "Budi Santoso",
    "ktpNum": "3273012345678901",
    "ktpVerified": false,
    "npwp": "123456789012345",
    "npwpVerified": false,
    "fullnameKtp": null,
    "phoneVerified": false,
    "bidLeft": 0,
    "successMatch": 0,
    "successProject": 0,
    "city": null,
    "experienceRange": null,
    "philosophy": null,
    "expertise": [],
    "needsOnboarding": true,
    "onboardingCompletedAt": null
  },
  "timestamp": "2026-06-01T10:00:00"
}
```

---

### 2. Get Architect Profile

**GET** `/rmtr/architects/profile`

Returns the authenticated architect's full profile.

```bash
curl --location 'http://localhost:8080/rmtr/architects/profile' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

Returns `ApiResponse<ArchitectDto>`.

---

### 3. Update Profile

**PUT** `/rmtr/architects/profile`

Updates the architect's editable profile fields.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/architects/profile' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "companyName": "PT Karya Desain Nusantara",
  "companySite": "https://karyadesain.co.id",
  "contactName": "Budi Santoso",
  "phoneNum": "08123456789012345",
  "category": "Commercial",
  "ktpNum": "3273012345678901",
  "npwp": "123456789012345",
  "fullnameKtp": "Budi Santoso Wijaya"
}'
```

**Request Body (all optional):**
| Field | Type | Validation |
|-------|------|-----------|
| `companyName` | String | Max 255 chars |
| `companySite` | String | Valid URL, max 255 chars |
| `contactName` | String | Max 255 chars |
| `phoneNum` | String | 8–16 digits |
| `category` | String | |
| `ktpNum` | String | Exactly 16 digits |
| `npwp` | String | 15–16 digits |
| `fullnameKtp` | String | Max 255 chars |

Returns `ApiResponse<ArchitectDto>`.

---

### 4. Update Onboarding Profile

**PUT** `/rmtr/architects/onboarding-profile`

Updates the extended profile shown on the architect's public page. Called after initial registration to complete onboarding.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/architects/onboarding-profile' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "companyName": "PT Karya Desain",
  "city": "Jakarta",
  "experienceRange": "5-10 years",
  "philosophy": "We believe architecture should enhance daily life through thoughtful space design.",
  "expertise": ["Residential", "Interior Design", "Sustainable Architecture"]
}'
```

**Request Body:**
| Field | Type | Notes |
|-------|------|-------|
| `companyName` | String | |
| `city` | String | City where the firm is based |
| `experienceRange` | String | e.g. `"5-10 years"` |
| `philosophy` | String | Design philosophy statement |
| `expertise` | List\<String\> | List of specialization areas |

Returns `ApiResponse<ArchitectDto>`.

---

### 5. Send Phone OTP

**POST** `/rmtr/architects/phone/send-otp`

Sends an OTP to the provided phone number for verification.

```bash
curl --location 'http://localhost:8080/rmtr/architects/phone/send-otp' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "phoneNumber": "+6281234567890"
}'
```

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| `phoneNumber` | String | Yes |

Returns `ApiResponse<Void>` on success.

---

### 6. Verify Phone OTP

**POST** `/rmtr/architects/phone/verify-otp`

Verifies the OTP entered by the architect. Sets `phoneVerified = true` on success.

```bash
curl --location 'http://localhost:8080/rmtr/architects/phone/verify-otp' \
--header 'Authorization: Bearer {{JWT_TOKEN}}' \
--header 'Content-Type: application/json' \
--data '{
  "phoneNumber": "+6281234567890",
  "code": "123456"
}'
```

**Request Body:**
| Field | Type | Validation |
|-------|------|-----------|
| `phoneNumber` | String | Required |
| `code` | String | Required, exactly 6 digits |

Returns `ApiResponse<ArchitectDto>` with `phoneVerified: true`.

---

## DTO Reference

### ArchitectDto

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | Architect record ID |
| `userId` | Long | Base user ID |
| `email` | String | |
| `companyName` | String | |
| `companySite` | String | |
| `category` | String | Specialization category |
| `phoneNumber` | String | |
| `contactName` | String | |
| `ktpNum` | String | |
| `ktpVerified` | boolean | Admin-verified KTP |
| `npwp` | String | |
| `npwpVerified` | boolean | Admin-verified NPWP |
| `fullnameKtp` | String | Full name as on KTP |
| `phoneVerified` | boolean | Phone OTP verified |
| `bidLeft` | int | Remaining bid tokens |
| `successMatch` | int | Number of accepted bids |
| `successProject` | int | Number of completed projects |
| `city` | String | |
| `experienceRange` | String | |
| `philosophy` | String | |
| `expertise` | List\<String\> | |
| `needsOnboarding` | Boolean | True until onboarding-profile is completed |
| `onboardingCompletedAt` | Timestamp | |

---

## Endpoint Summary

| Method | Path | Auth Required | Description |
|--------|------|--------------|-------------|
| POST | `/rmtr/architects/register` | Yes (authenticated user) | Create architect profile |
| GET | `/rmtr/architects/profile` | Yes (ARCHITECT) | Get own profile |
| PUT | `/rmtr/architects/profile` | Yes (ARCHITECT) | Update profile fields |
| PUT | `/rmtr/architects/onboarding-profile` | Yes (ARCHITECT) | Update extended onboarding fields |
| POST | `/rmtr/architects/phone/send-otp` | Yes (ARCHITECT) | Send phone verification OTP |
| POST | `/rmtr/architects/phone/verify-otp` | Yes (ARCHITECT) | Verify phone OTP |
