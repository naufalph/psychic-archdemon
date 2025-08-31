# Architect Authentication API Documentation

## Overview
This document describes the authentication APIs for the Architect module in the Rumantra platform.

## Base URL
```
http://localhost:8080/rmtr/architects
```

## Authentication
Protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

## Endpoints

### 1. Register (Create Architect Account)
**Endpoint:** `POST /rmtr/architects/register`  
**Authentication:** Not required  
**Description:** Creates a new architect account with user credentials and architect profile.

**Request Body:**
```json
{
  "userName": "john_architect",
  "email": "john@architecture.com",
  "password": "SecurePass123!",
  "companyName": "John Architecture Studio",
  "companySite": "https://johnarchitecture.com",
  "contactName": "John Doe",
  "ktpNum": "1234567890123456",
  "npwp": "123456789012345"
}
```

**Validation Rules:**
- `userName`: Required, 3-50 characters, alphanumeric and underscore only
- `email`: Required, valid email format
- `password`: Required, min 8 characters, must contain uppercase, lowercase, digit, and special character
- `companyName`: Required, max 255 characters
- `companySite`: Optional, valid URL format
- `contactName`: Required, max 255 characters
- `ktpNum`: Required, exactly 16 digits
- `npwp`: Required, 15-16 digits

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Architect registered successfully!",
  "data": {
    "id": 1,
    "userId": 1,
    "companyName": "John Architecture Studio",
    "companySite": "https://johnarchitecture.com",
    "contactName": "John Doe",
    "ktpNum": "1234567890123456",
    "ktpVerified": false,
    "npwp": "123456789012345",
    "npwpVerified": false,
    "bidLeft": 10,
    "successMatch": 0,
    "successProject": 0
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Username is already taken!"
}
```

### 3. Update Architect Profile
**Endpoint:** `PUT /rmtr/architects/profile`  
**Authentication:** Required  
**Description:** Updates architect profile information. Note that some fields like `ktpVerified`, `npwpVerified`, `bidLeft`, `successMatch`, and `successProject` cannot be updated by the user.

**Request Headers:**
```
Authorization: Bearer <token>
```

**Request Body (all fields optional):**
```json
{
  "companyName": "Updated Architecture Studio",
  "companySite": "https://newsite.com",
  "contactName": "John Updated",
  "ktpNum": "9876543210987654",
  "npwp": "987654321098765",
  "email": "newemail@architecture.com",
  "password": "NewSecurePass123!"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Profile updated successfully!",
  "data": {
    "id": 1,
    "userId": 1,
    "companyName": "Updated Architecture Studio",
    "companySite": "https://newsite.com",
    "contactName": "John Updated",
    "ktpNum": "9876543210987654",
    "ktpVerified": false,
    "npwp": "987654321098765",
    "npwpVerified": false,
    "bidLeft": 10,
    "successMatch": 0,
    "successProject": 0
  }
}
```

### 4. Get Profile
**Endpoint:** `GET /rmtr/architects/profile`  
**Authentication:** Required  
**Description:** Retrieves the authenticated architect's profile information.

**Request Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Profile retrieved successfully!",
  "data": {
    "id": 1,
    "userId": 1,
    "companyName": "John Architecture Studio",
    "companySite": "https://johnarchitecture.com",
    "contactName": "John Doe",
    "ktpNum": "1234567890123456",
    "ktpVerified": false,
    "npwp": "123456789012345",
    "npwpVerified": false,
    "bidLeft": 10,
    "successMatch": 0,
    "successProject": 0
  }
}
```

## Error Codes
- `400 Bad Request`: Invalid input data or business rule violation
- `401 Unauthorized`: Invalid credentials or missing/invalid token
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Notes
1. The JWT token expires after 24 hours (86400000 ms)
2. Fields that cannot be edited by users:
   - `ktpVerified`: KTP verification status (admin only)
   - `npwpVerified`: NPWP verification status (admin only)
   - `bidLeft`: Number of bids remaining (system managed)
   - `successMatch`: Number of successful matches (system managed)
   - `successProject`: Number of successful projects (system managed)
3. Username must be unique across the system
4. Email must be unique across the system
5. KTP number must be unique across all architects
6. NPWP must be unique across all architects
