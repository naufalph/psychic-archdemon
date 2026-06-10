# User API Documentation

## Overview
This document describes the authentication and user management API endpoints for the Rumantra platform.

**Base URL:** `http://localhost:8080/rmtr/users`

**Public endpoints** (no JWT required): `/register`, `/login`, `/verify-email`, `/resend-verification`, `/oauth2/google`, `/oauth2/callback/google`, `/oauth2/linkedin`, `/oauth2/callback/linkedin`

---

## Endpoints

---

### 1. Register

**POST** `/rmtr/users/register`

Creates a new user account and sends a verification email. The user must verify their email before logging in.

```bash
curl --location 'http://localhost:8080/rmtr/users/register' \
--header 'Content-Type: application/json' \
--data '{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "Budi",
  "lastName": "Santoso",
  "role": "ARCHITECT"
}'
```

**Request Body:**
| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| `email` | String | Yes | Valid email |
| `password` | String | Yes | Min 8 chars, must include digit, lowercase, uppercase, special char |
| `firstName` | String | No | |
| `lastName` | String | No | |
| `role` | String | Yes | Role hint for initial profile creation |

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": "Verification email sent to: user@example.com",
  "timestamp": "2026-06-01T10:00:00"
}
```

The response body is a plain string message — not a user object.

---

### 2. Verify Email

**GET** `/rmtr/users/verify-email?token={token}`

Verifies the email address using the token sent in the registration email.

```bash
curl --location 'http://localhost:8080/rmtr/users/verify-email?token=abc123token'
```

**Query Parameters:**
| Param | Type | Required |
|-------|------|----------|
| `token` | String | Yes |

**Expected Response (200 OK):** `ApiResponse<UserAuthResponseDto>` — returns JWT token on successful verification, allowing immediate login.

---

### 3. Resend Verification Email

**POST** `/rmtr/users/resend-verification?email={email}`

Resends the email verification link if the original expired.

```bash
curl --location --request POST 'http://localhost:8080/rmtr/users/resend-verification?email=user@example.com'
```

**Query Parameters:**
| Param | Type | Required |
|-------|------|----------|
| `email` | String | Yes |

**Expected Response (200 OK):** `ApiResponse<String>` — confirmation message.

---

### 4. Login

**POST** `/rmtr/users/login`

Authenticates the user and returns a JWT token.

```bash
curl --location 'http://localhost:8080/rmtr/users/login' \
--header 'Content-Type: application/json' \
--data '{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "role": "ARCHITECT"
}'
```

**Request Body:**
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email` | String | Yes | User's email address |
| `password` | String | Yes | |
| `role` | String | No | Default: `"ARCHITECT"` |

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 42,
    "email": "user@example.com",
    "firstName": "Budi",
    "lastName": "Santoso",
    "registeredRoles": ["ARCHITECT"],
    "needsArchitectOnboarding": true,
    "needsClientOnboarding": false,
    "lastLoginRole": "ARCHITECT"
  },
  "timestamp": "2026-06-01T10:00:00"
}
```

**Notes:**
- `registeredRoles` is empty if user has not activated any role yet
- `needsArchitectOnboarding: true` means the architect hasn't completed `PUT /rmtr/architects/onboarding-profile`
- Use the `token` value as `Bearer {{JWT_TOKEN}}` in all subsequent requests

---

### 5. Google OAuth2 Login

**GET** `/rmtr/users/oauth2/google`

Redirects to Google OAuth2 consent screen.

```bash
# Open in browser or redirect from frontend
curl --location 'http://localhost:8080/rmtr/users/oauth2/google?role=CLIENT'
```

**Query Parameters:**
| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `role` | String | No | Role to auto-activate after OAuth login |

---

### 6. Google OAuth2 Callback

**GET** `/rmtr/users/oauth2/callback/google`

Handles Google's OAuth2 callback. Redirects to frontend with JWT token.

**Query Parameters:** `code` (required), `state` (optional) — handled automatically by OAuth flow.

---

### 7. LinkedIn OAuth2 Login

**GET** `/rmtr/users/oauth2/linkedin`

Redirects to LinkedIn OAuth2 consent screen.

```bash
curl --location 'http://localhost:8080/rmtr/users/oauth2/linkedin?role=ARCHITECT'
```

**Query Parameters:**
| Param | Type | Required |
|-------|------|----------|
| `role` | String | No |

---

### 8. LinkedIn OAuth2 Callback

**GET** `/rmtr/users/oauth2/callback/linkedin`

Handles LinkedIn's OAuth2 callback. Redirects to frontend with JWT token.

**Query Parameters:** `code`, `state`, `error`, `error_description` — handled automatically by OAuth flow.

---

### 9. Get Current User

**GET** `/rmtr/users/me`

Returns the authenticated user's profile and registered roles.

```bash
curl --location 'http://localhost:8080/rmtr/users/me' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 42,
    "userName": "budi_santoso",
    "email": "user@example.com",
    "firstName": "Budi",
    "lastName": "Santoso",
    "isEmailVerified": true,
    "isActive": true,
    "registeredRoles": ["ARCHITECT"],
    "needsArchitectOnboarding": false,
    "needsClientOnboarding": null,
    "lastLoginRole": "ARCHITECT"
  },
  "timestamp": "2026-06-01T10:00:00"
}
```

---

### 10. Activate Role

**POST** `/rmtr/users/me/activate-role?role={ARCHITECT|CLIENT}`

Activates a role for the current user. Creates the corresponding architect or client profile record. Users can hold both roles simultaneously.

```bash
# Activate ARCHITECT role
curl --location --request POST 'http://localhost:8080/rmtr/users/me/activate-role?role=ARCHITECT' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'

# Activate CLIENT role
curl --location --request POST 'http://localhost:8080/rmtr/users/me/activate-role?role=CLIENT' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Query Parameters:**
| Param | Type | Required | Values |
|-------|------|----------|--------|
| `role` | String | Yes | `ARCHITECT` or `CLIENT` |

**Expected Response (200 OK):** `ApiResponse<UserDto>` with updated `registeredRoles`.

**Business Logic:**
- Creates the `Architect` or `Client` record linked to the user
- The new role is immediately active on the next request (JWT is re-evaluated)
- Safe to call multiple times — idempotent

---

### 11. Update Last Login Role

**PUT** `/rmtr/users/me/last-login-role?role={ARCHITECT|CLIENT}`

Stores the user's last active role. Used to restore the correct dashboard on next login.

```bash
curl --location --request PUT 'http://localhost:8080/rmtr/users/me/last-login-role?role=CLIENT' \
--header 'Authorization: Bearer {{JWT_TOKEN}}'
```

**Query Parameters:**
| Param | Type | Required | Values |
|-------|------|----------|--------|
| `role` | String | Yes | `ARCHITECT` or `CLIENT` |

**Expected Response (200 OK):** `ApiResponse<String>` — confirmation message.

---

## DTO Reference

### UserLoginRequestDto

| Field | Type | Notes |
|-------|------|-------|
| `email` | String | User's email address |
| `password` | String | |
| `role` | String | Default: `"ARCHITECT"` |

### UserSignupRequestDto

| Field | Type |
|-------|------|
| `email` | String |
| `password` | String |
| `firstName` | String |
| `lastName` | String |
| `role` | String |

### UserAuthResponseDto (login / email-verify response)

| Field | Type |
|-------|------|
| `token` | String |
| `type` | String (`"Bearer"`) |
| `id` | Long |
| `email` | String |
| `firstName` | String |
| `lastName` | String |
| `registeredRoles` | List\<String\> |
| `needsArchitectOnboarding` | Boolean |
| `needsClientOnboarding` | Boolean |
| `lastLoginRole` | String |

### UserDto (GET /me, activate-role response)

| Field | Type |
|-------|------|
| `id` | Long |
| `userName` | String |
| `email` | String |
| `firstName` | String |
| `lastName` | String |
| `isEmailVerified` | boolean |
| `isActive` | boolean |
| `registeredRoles` | List\<String\> |
| `needsArchitectOnboarding` | Boolean |
| `needsClientOnboarding` | Boolean |
| `lastLoginRole` | String |

---

## Endpoint Summary

| Method | Path | Public | Description |
|--------|------|--------|-------------|
| POST | `/rmtr/users/register` | Yes | Register new user |
| GET | `/rmtr/users/verify-email` | Yes | Verify email token |
| POST | `/rmtr/users/resend-verification` | Yes | Resend verification email |
| POST | `/rmtr/users/login` | Yes | Login, get JWT |
| GET | `/rmtr/users/oauth2/google` | Yes | Google OAuth login |
| GET | `/rmtr/users/oauth2/callback/google` | Yes | Google OAuth callback |
| GET | `/rmtr/users/oauth2/linkedin` | Yes | LinkedIn OAuth login |
| GET | `/rmtr/users/oauth2/callback/linkedin` | Yes | LinkedIn OAuth callback |
| GET | `/rmtr/users/me` | No | Get current user |
| POST | `/rmtr/users/me/activate-role` | No | Activate ARCHITECT or CLIENT role |
| PUT | `/rmtr/users/me/last-login-role` | No | Update last active role |

---

## Testing Sequence

1. **Register** → `POST /rmtr/users/register`
2. **Verify Email** → `GET /rmtr/users/verify-email?token={token}` (or click link in email)
3. **Login** → `POST /rmtr/users/login` → save `token`
4. **Get Profile** → `GET /rmtr/users/me`
5. **Activate Role** → `POST /rmtr/users/me/activate-role?role=ARCHITECT`
6. **Set Last Role** → `PUT /rmtr/users/me/last-login-role?role=ARCHITECT`
