# User Controller API Testing Documentation

## Overview
This document outlines the testing procedures for all endpoints in the User Controller, which handles user authentication and registration for the Rumantra platform.

## Base URL
```
http://localhost:8080/rmtr/users
```

## Authentication
Protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

## Endpoints

### 1. Login
**Endpoint:** `POST /rmtr/users/login`  
**Authentication:** Not required  
**Description:** Authenticates a user using username/email and password.

**Test Request Body:**
```json
{
  "usernameOrEmail": "test_user",
  "password": "TestPass123!"
}
```

**Expected Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful!",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "id": 1,
    "userName": "test_user",
    "email": "test@example.com"
  }
}
```

**Expected Error Responses:**
- **401 Unauthorized:** Invalid credentials
```json
{
  "success": false,
  "message": "Invalid password!"
}
```

- **500 Internal Server Error:** Server-side issues
```json
{
  "success": false,
  "message": "An error occurred during login"
}
```

### 2. Register
**Endpoint:** `POST /rmtr/users/register`  
**Authentication:** Not required  
**Description:** Creates a new user account with basic information.

**Test Request Body:**
```json
{
  "userName": "test_user",
  "email": "test@example.com",
  "password": "TestPass123!"
}
```

**Validation Rules:**
- `userName`: Required, 3-50 characters, alphanumeric and underscore only
- `email`: Required, valid email format
- `password`: Required, min 8 characters, must contain uppercase, lowercase, digit, and special character

**Expected Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Architect registered successfully!",
  "data": {
    "id": 1,
    "userId": 1,
    "userName": "test_user",
    "email": "test@example.com"
  }
}
```

**Expected Error Responses:**
- **400 Bad Request:** Invalid input data
```json
{
  "success": false,
  "message": "Username is already taken!"
}
```

- **500 Internal Server Error:** Server-side issues
```json
{
  "success": false,
  "message": "An error occurred during registration"
}