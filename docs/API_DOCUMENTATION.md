# LandGo Backend - API Documentation

> **Version:** 1.1.0  
> **Base URL:** `http://localhost:8080`  
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`  
> **Last Updated:** 14 February 2026

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Prerequisites](#3-prerequisites)
4. [Authentication](#4-authentication)
5. [API Endpoints](#5-api-endpoints)
   - [5.1 Auth APIs](#51-auth-apis)
   - [5.2 Land APIs](#52-land-apis)
   - [5.3 Vendor APIs](#53-vendor-apis)
   - [5.4 Subscription APIs](#54-subscription-apis)
   - [5.5 Actuator APIs](#55-actuator-apis)
6. [Enums & Constants](#6-enums--constants)
7. [Error Handling](#7-error-handling)
8. [Postman Testing Guide](#8-postman-testing-guide)

---

## 1. Overview

**LandGo** is a land listing platform backend API that enables:

- **Users** to browse land listings, search/filter, and subscribe for premium access.
- **Vendors** to list, manage, and sell land properties.
- **Admins** to manage the platform (future scope).

**Tech Stack:**
| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.2.2 |
| Language | Java 21 |
| Database | PostgreSQL 16 |
| ORM | Hibernate 6.4 / Spring Data JPA |
| Auth | JWT (jjwt 0.12.5) |
| Mapping | MapStruct 1.5.5 |
| Docs | SpringDoc OpenAPI 2.3.0 |
| Migration | Flyway 10.8.1 (disabled by default) |

---

## 2. Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      CLIENT (Postman / App)                  │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP Request
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                   Security Filter Chain                      │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  JwtAuthenticationFilter                                │ │
│  │  • Extracts JWT from Authorization header               │ │
│  │  • Validates token                                      │ │
│  │  • Sets SecurityContext                                 │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Authorization Rules                                    │ │
│  │  • /api/v1/auth/** → Public                            │ │
│  │  • /api/v1/lands (GET) → Public                        │ │
│  │  • /api/v1/vendor/** → VENDOR role                     │ │
│  │  • /api/v1/admin/** → ADMIN role                       │ │
│  │  • Everything else → Authenticated                     │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                      Controllers                             │
│  AuthController │ LandController │ VendorController │ Sub... │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                       Services                               │
│  AuthService │ LandService │ VendorService │ SubscriptionSvc│
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│              Repositories (Spring Data JPA)                  │
│  UserRepository │ LandRepository │ VendorRepository │ Sub.. │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                        │
│  users │ vendor_profiles │ lands │ subscriptions │ saved..   │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Prerequisites

### Local Development Setup

1. **Java 21** installed
2. **PostgreSQL 16** running on `localhost:5432`
3. **Database created:**
   ```sql
   CREATE DATABASE landgo;
   ```
4. **Default credentials:** `postgres` / `postgres`

### Using Docker Compose (alternative)

```bash
docker-compose up postgres pgadmin
```

This starts:
- PostgreSQL on port `5432`
- pgAdmin on port `5050` (admin@landgo.com / admin)

### Running the App

```bash
./mvnw clean compile spring-boot:run
```

The app starts on `http://localhost:8080`.

---

## 4. Authentication

### JWT Token Format

All protected endpoints require the `Authorization` header:

```
Authorization: Bearer <access_token>
```

### Token Details

| Token Type | Expiration | Description |
|-----------|-----------|-------------|
| Access Token | 1 hour (3600000ms) | Used for API authorization |
| Refresh Token | 7 days (604800000ms) | Used to get new access token |

### Token Payload (decoded)

```json
{
  "sub": "user-uuid",
  "role": "USER",
  "iat": 1739400000,
  "exp": 1739403600
}
```

### Role Hierarchy

| Role | Access |
|------|--------|
| `USER` | Browse lands, subscribe, view vendors (with subscription) |
| `VENDOR` | All USER permissions + create/manage land listings |
| `ADMIN` | Full platform access (future scope) |

---

## 5. API Endpoints

### Standard Response Envelope

All APIs return this structure:

```json
{
  "success": true,
  "message": "Operation message",
  "data": { ... },
  "errors": null,
  "timestamp": "2026-02-13T12:00:00"
}
```

---

### 5.1 Auth APIs

**Base Path:** `/api/v1/auth`

---

#### 5.1.1 Register User

```
POST /api/v1/auth/register
```

**Auth Required:** No

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "1234567890"
}
```

**Validation Rules:**
| Field | Rule |
|-------|------|
| firstName | Required, 2-50 chars |
| lastName | Required, 2-50 chars |
| email | Required, valid email format |
| password | Min 8 chars |
| phone | Optional |

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phone": "1234567890",
      "profileImageUrl": null,
      "authProvider": "EMAIL",
      "role": "USER",
      "emailVerified": false,
      "isVendor": false,
      "createdAt": "2026-02-13T12:00:00"
    }
  },
  "timestamp": "2026-02-13T12:00:00"
}
```

**Error Responses:**
| Status | Condition |
|--------|-----------|
| 400 | Email already registered |
| 400 | Validation failed |

---

#### 5.1.2 Login

```
POST /api/v1/auth/login
```

**Auth Required:** No

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Validation Rules:**
| Field | Rule |
|-------|------|
| email | Required, valid email |
| password | Required |

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": { ... }
  },
  "timestamp": "2026-02-13T12:00:00"
}
```

**Error Responses:**
| Status | Condition |
|--------|-----------|
| 401 | Invalid email or password |

---

#### 5.1.3 OAuth2 Login (Google / Apple)

```
POST /api/v1/auth/oauth2
```

**Auth Required:** No

**Request Body:**
```json
{
  "token": "google-or-apple-id-token",
  "authProvider": "GOOGLE"
}
```

| Field | Rule |
|-------|------|
| token | Required, ID token from provider |
| authProvider | Required, `GOOGLE` or `APPLE` |

**Success Response (200 OK):** Same as Login response.

**Flow:**
1. Validates ID token with Google/Apple servers
2. Extracts user info (email, name, profile picture)
3. Creates new user if not exists, or logs in existing user
4. Returns JWT tokens

---

#### 5.1.4 Get Current User

```
GET /api/v1/auth/me
```

**Auth Required:** Yes (Bearer token)

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "profileImageUrl": null,
    "authProvider": "EMAIL",
    "role": "USER",
    "emailVerified": false,
    "isVendor": false,
    "createdAt": "2026-02-13T12:00:00"
  },
  "timestamp": "2026-02-13T12:00:00"
}
```

---

#### 5.1.5 Forgot Password

```
POST /api/v1/auth/forgot-password
```

**Auth Required:** No

**Request Body:**
```json
{
  "email": "elena@example.com"
}
```

**Validation Rules:**
| Field | Rule |
|-------|------|
| email | Required, valid email format |

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Password reset link has been sent to your email",
  "timestamp": "2026-02-14T12:00:00"
}
```

**Flow:**
1. Validates the email exists and belongs to an `EMAIL` auth provider (not Google/Apple)
2. Invalidates any existing unused reset tokens for the user
3. Generates a UUID token with 30-minute expiry
4. Stores the token in the `password_reset_tokens` table
5. Sends a branded HTML email with the reset link (async)

> **Security Note:** This endpoint always returns a 200 OK regardless of whether the email exists. This prevents email enumeration attacks.

---

#### 5.1.6 Validate Reset Token

```
GET /api/v1/auth/reset-password/validate?token={token}
```

**Auth Required:** No

**Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| token | String (required) | The UUID reset token from the email link |

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Token is valid",
  "timestamp": "2026-02-14T12:00:00"
}
```

**Error Responses:**
| Status | Condition |
|--------|-----------|
| 400 | Token is invalid, expired, or already used |

---

#### 5.1.7 Reset Password

```
POST /api/v1/auth/reset-password
```

**Auth Required:** No

**Request Body:**
```json
{
  "token": "baa879d6-533b-4cae-b87e-03a72839e6b3",
  "newPassword": "NewPass@123"
}
```

**Validation Rules:**
| Field | Rule |
|-------|------|
| token | Required |
| newPassword | Required, minimum 8 characters |

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Password has been reset successfully",
  "timestamp": "2026-02-14T12:00:00"
}
```

**Error Responses:**
| Status | Condition |
|--------|-----------|
| 400 | Token is invalid, expired, or already used |
| 400 | Password validation failed (too short) |

**Flow:**
1. Validates the token (exists, not expired, not used)
2. Updates the user's password (BCrypt encoded)
3. Marks the token as `used = true`
4. Invalidates all other active reset tokens for the user
5. User can now login with the new password

---

### 5.2 Land APIs

**Base Path:** `/api/v1/lands` (public) and `/api/v1/vendor/lands` (vendor)

---

#### 5.2.1 Get All Active Lands (Public)

```
GET /api/v1/lands?page=0&size=20&sort=createdAt,desc
```

**Auth Required:** No

**Query Parameters:**
| Param | Default | Description |
|-------|---------|-------------|
| page | 0 | Page number (0-indexed) |
| size | 20 | Page size |
| sort | - | Sort field and direction |

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "...",
        "title": "Beautiful Residential Plot",
        "description": "A prime plot...",
        "landType": "RESIDENTIAL",
        "status": "ACTIVE",
        "address": "456 Oak Ave",
        "city": "Austin",
        "state": "Texas",
        "zipCode": "73301",
        "country": "USA",
        "latitude": 30.26720000,
        "longitude": -97.74310000,
        "price": 150000.00,
        "areaSqFt": 5000.00,
        "frontage": null,
        "depth": null,
        "hasWaterAccess": true,
        "hasElectricity": true,
        "hasRoadAccess": true,
        "hasSewage": false,
        "zoningInfo": null,
        "topography": null,
        "soilType": null,
        "imageUrls": ["https://example.com/img1.jpg"],
        "videoUrl": null,
        "virtualTourUrl": null,
        "viewCount": 5,
        "inquiryCount": 0,
        "vendorId": "...",
        "vendorCompanyName": "LandCorp",
        "vendorVerified": false,
        "createdAt": "2026-02-13T12:00:00",
        "updatedAt": "2026-02-13T12:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-02-13T12:00:00"
}
```

> **Note:** Only lands with `status = ACTIVE` are returned. New listings start as `PENDING_APPROVAL`.

---

#### 5.2.2 Get Land by ID (Public)

```
GET /api/v1/lands/{id}
```

**Auth Required:** No  
**Side Effect:** Increments `viewCount` by 1

---

#### 5.2.3 Search Lands (Public)

```
GET /api/v1/lands/search?query=Austin&page=0&size=20
```

**Auth Required:** No  
**Searches in:** `title`, `city`, `state` (case-insensitive)

---

#### 5.2.4 Filter Lands (Public)

```
GET /api/v1/lands/filter?city=Austin&type=RESIDENTIAL&minPrice=100000&maxPrice=500000&page=0&size=20
```

**Auth Required:** No

**Query Parameters (all optional):**
| Param | Type | Description |
|-------|------|-------------|
| city | String | Filter by city |
| type | LandType enum | RESIDENTIAL, COMMERCIAL, etc. |
| minPrice | BigDecimal | Minimum price |
| maxPrice | BigDecimal | Maximum price |

---

#### 5.2.5 Recent Listings (Public)

```
GET /api/v1/lands/recent?limit=10
```

**Auth Required:** No  
Returns most recently created active listings.

---

#### 5.2.6 Popular Listings (Public)

```
GET /api/v1/lands/popular?limit=10
```

**Auth Required:** No  
Returns listings sorted by `viewCount` descending.

---

#### 5.2.7 Create Land Listing (Vendor)

```
POST /api/v1/vendor/lands
```

**Auth Required:** Yes (VENDOR role)

**Request Body:**
```json
{
  "title": "Beautiful Residential Plot in Austin",
  "description": "A 5000 sq ft plot with road access and electricity",
  "landType": "RESIDENTIAL",
  "address": "456 Oak Ave",
  "city": "Austin",
  "state": "Texas",
  "zipCode": "73301",
  "country": "USA",
  "latitude": 30.2672,
  "longitude": -97.7431,
  "price": 150000.00,
  "areaSqFt": 5000.00,
  "frontage": 50.00,
  "depth": 100.00,
  "hasWaterAccess": true,
  "hasElectricity": true,
  "hasRoadAccess": true,
  "hasSewage": false,
  "zoningInfo": "R-1 Residential",
  "topography": "Flat",
  "soilType": "Loamy",
  "imageUrls": ["https://example.com/img1.jpg", "https://example.com/img2.jpg"],
  "videoUrl": "https://example.com/video.mp4",
  "virtualTourUrl": "https://example.com/tour"
}
```

**Validation Rules:**
| Field | Rule |
|-------|------|
| title | Required, 5-200 chars |
| landType | Required (enum) |
| address | Required |
| city | Required |
| state | Required |
| zipCode | Required |
| country | Required |
| price | Required, > 0 |
| areaSqFt | Required, positive |

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Land listing created successfully",
  "data": { ... land response ... },
  "timestamp": "2026-02-13T12:00:00"
}
```

> **Note:** New listings are created with `status = PENDING_APPROVAL`.

---

#### 5.2.8 Get My Lands (Vendor)

```
GET /api/v1/vendor/lands?page=0&size=20
```

**Auth Required:** Yes (VENDOR role)

---

#### 5.2.9 Update Land Listing (Vendor)

```
PUT /api/v1/vendor/lands/{id}
```

**Auth Required:** Yes (VENDOR role, must be owner)

**Request Body:** Same as Create Land Listing.

**Error Responses:**
| Status | Condition |
|--------|-----------|
| 403 | Not the owner of this listing |
| 404 | Land not found |

---

#### 5.2.10 Delete Land Listing (Vendor)

```
DELETE /api/v1/vendor/lands/{id}
```

**Auth Required:** Yes (VENDOR role, must be owner)  
**Behavior:** Soft-delete (sets `deleted = true`)

---

### 5.3 Vendor APIs

**Base Path:** `/api/v1/vendor` and `/api/v1/vendors`

---

#### 5.3.1 Register as Vendor

```
POST /api/v1/vendor/register
```

**Auth Required:** Yes (any authenticated user)

> **Important:** This changes the user's role from `USER` to `VENDOR`. You must re-login to get a new token with the updated role.

**Request Body:**
```json
{
  "companyName": "LandCorp",
  "companyDescription": "Premium land deals",
  "companyLogo": "https://example.com/logo.png",
  "businessLicense": "LIC-12345",
  "businessAddress": "123 Main St",
  "businessCity": "Austin",
  "businessState": "Texas",
  "businessZipCode": "73301",
  "businessCountry": "USA",
  "website": "https://landcorp.com"
}
```

**Validation Rules:**
| Field | Rule |
|-------|------|
| companyName | Required, 2-100 chars |
| businessAddress | Required |
| businessCity | Required |
| businessState | Required |
| businessZipCode | Required |
| businessCountry | Required |

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Vendor registration successful",
  "data": {
    "id": "...",
    "userId": "...",
    "companyName": "LandCorp",
    "companyDescription": "Premium land deals",
    "companyLogo": "https://example.com/logo.png",
    "businessAddress": "123 Main St",
    "businessCity": "Austin",
    "businessState": "Texas",
    "businessZipCode": "73301",
    "businessCountry": "USA",
    "website": "https://landcorp.com",
    "verified": false,
    "rating": null,
    "totalReviews": 0,
    "totalLandsListed": 0,
    "totalLandsSold": 0,
    "ownerName": "John Doe",
    "ownerEmail": "john@example.com",
    "createdAt": "2026-02-13T12:00:00"
  },
  "timestamp": "2026-02-13T12:00:00"
}
```

---

#### 5.3.2 Get All Vendors (Public)

```
GET /api/v1/vendors?page=0&size=20
```

**Auth Required:** No  
**Returns:** Only verified vendors (`verified = true`).

---

#### 5.3.3 Get Vendor by ID

```
GET /api/v1/vendors/{id}
```

**Auth Required:** Yes (requires a paid subscription — not FREE plan)

**Error Responses:**
| Status | Condition |
|--------|-----------|
| 403 | No active paid subscription |
| 404 | Vendor not found |

---

#### 5.3.4 Search Vendors (Public)

```
GET /api/v1/vendors/search?query=LandCorp&page=0&size=20
```

**Auth Required:** No  
**Searches in:** `companyName`, `businessCity` (case-insensitive)

---

#### 5.3.5 Get My Vendor Profile

```
GET /api/v1/vendor/profile
```

**Auth Required:** Yes (VENDOR role)

---

#### 5.3.6 Update Vendor Profile

```
PUT /api/v1/vendor/profile
```

**Auth Required:** Yes (VENDOR role)

**Request Body:** Same as Vendor Register.

---

### 5.4 Subscription APIs

**Base Path:** `/api/v1/subscriptions`

---

#### 5.4.1 Subscribe to a Plan

```
POST /api/v1/subscriptions
```

**Auth Required:** Yes

**Request Body:**
```json
{
  "plan": "PREMIUM",
  "paymentMethod": "credit_card",
  "paymentToken": "tok_visa_123",
  "autoRenew": true
}
```

| Field | Rule |
|-------|------|
| plan | Required (FREE, BASIC, PREMIUM, ENTERPRISE) |
| paymentMethod | Optional |
| autoRenew | Optional, default false |

**Subscription Plans:**

| Plan | Price | Duration | Vendor Views/mo | Saved Lands | Premium Access | Direct Contact |
|------|-------|----------|----------------|-------------|---------------|----------------|
| FREE | $0.00 | Unlimited | 5 | 10 | ❌ | ❌ |
| BASIC | $9.99 | 30 days | 20 | 50 | ❌ | ✅ |
| PREMIUM | $29.99 | 30 days | 100 | 200 | ✅ | ✅ |
| ENTERPRISE | $99.99 | 365 days | Unlimited | Unlimited | ✅ | ✅ |

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Subscription successful",
  "data": {
    "id": "...",
    "plan": "PREMIUM",
    "status": "ACTIVE",
    "startDate": "2026-02-13T12:00:00",
    "endDate": "2026-03-15T12:00:00",
    "amount": 29.99,
    "paymentMethod": "credit_card",
    "autoRenew": true,
    "isActive": true,
    "maxVendorViewsPerMonth": 100,
    "maxSavedLands": 200,
    "canAccessPremiumListings": true,
    "canContactVendorDirectly": true
  },
  "timestamp": "2026-02-13T12:00:00"
}
```

**Error Responses:**
| Status | Condition |
|--------|-----------|
| 400 | Already has an active subscription |

---

#### 5.4.2 Get Current Subscription

```
GET /api/v1/subscriptions/current
```

**Auth Required:** Yes

**Error Responses:**
| Status | Condition |
|--------|-----------|
| 404 | No active subscription found |

---

#### 5.4.3 Cancel Subscription

```
POST /api/v1/subscriptions/cancel?reason=Too expensive
```

**Auth Required:** Yes

| Param | Type | Description |
|-------|------|-------------|
| reason | String (optional) | Cancellation reason |

---

### 5.5 Actuator APIs

| Endpoint | Auth | Description |
|----------|------|-------------|
| GET `/actuator/health` | No | App health status |
| GET `/actuator/info` | No | App info |

---

## 6. Enums & Constants

### AuthProvider
| Value | Description |
|-------|-------------|
| `EMAIL` | Email/password registration |
| `GOOGLE` | Google OAuth2 |
| `APPLE` | Apple OAuth2 |

### Role
| Value | Description |
|-------|-------------|
| `USER` | Regular user |
| `VENDOR` | Land listing vendor |
| `ADMIN` | Platform administrator |

### LandType
| Value | Description |
|-------|-------------|
| `RESIDENTIAL` | Residential property |
| `COMMERCIAL` | Commercial property |
| `AGRICULTURAL` | Agricultural land |
| `INDUSTRIAL` | Industrial zone |
| `MIXED_USE` | Mixed-use property |

### LandStatus
| Value | Description |
|-------|-------------|
| `DRAFT` | Draft listing |
| `PENDING_APPROVAL` | Waiting for admin approval |
| `ACTIVE` | Live and visible |
| `SOLD` | Sold |
| `INACTIVE` | Deactivated |

### SubscriptionPlan
| Value | Description |
|-------|-------------|
| `FREE` | Free tier |
| `BASIC` | Basic plan |
| `PREMIUM` | Premium plan |
| `ENTERPRISE` | Enterprise plan |

### SubscriptionStatus
| Value | Description |
|-------|-------------|
| `ACTIVE` | Currently active |
| `EXPIRED` | Expired |
| `CANCELLED` | Cancelled by user |
| `PENDING` | Payment pending |

---

## 7. Error Handling

### Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "errors": {
    "fieldName": "Validation error message"
  },
  "timestamp": "2026-02-13T12:00:00"
}
```

### HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET/PUT/DELETE |
| 201 | Created | Successful POST (create) |
| 400 | Bad Request | Validation errors, duplicate email |
| 401 | Unauthorized | Missing/invalid/expired JWT |
| 403 | Forbidden | Insufficient role or subscription |
| 404 | Not Found | Resource doesn't exist |
| 500 | Internal Server Error | Unexpected server error |

---

## 8. Postman Testing Guide

### 8.1 Setup

1. **Create a Postman Environment** called `LandGo Local`
2. **Add variables:**
   | Variable | Initial Value |
   |----------|--------------|
   | `BASE_URL` | `http://localhost:8080` |
   | `TOKEN` | *(leave empty)* |
   | `VENDOR_TOKEN` | *(leave empty)* |
   | `LAND_ID` | *(leave empty)* |
   | `VENDOR_ID` | *(leave empty)* |

3. **Add this Test Script to Login/Register requests** (in the "Tests" tab):
   ```javascript
   const response = pm.response.json();
   if (response.success && response.data && response.data.accessToken) {
       pm.environment.set("TOKEN", response.data.accessToken);
   }
   ```

4. **For authenticated requests**, set the Authorization header:
   ```
   Authorization: Bearer {{TOKEN}}
   ```

### 8.2 Test Flow (Step-by-Step)

> Execute these in order. Each step builds on the previous one.

---

#### Step 1: Health Check

```
GET {{BASE_URL}}/actuator/health
```

**Expected:** `{"status": "UP"}`

---

#### Step 2: Register User

```
POST {{BASE_URL}}/api/v1/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "1234567890"
}
```

**Expected:** 201 Created with `accessToken`  
**Action:** Save `accessToken` as `{{TOKEN}}`

---

#### Step 3: Login

```
POST {{BASE_URL}}/api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Expected:** 200 OK with `accessToken`

---

#### Step 3.1: Forgot Password

```
POST {{BASE_URL}}/api/v1/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

**Expected:** 200 OK with message "Password reset link has been sent to your email"  
**Action:** Query the database to get the token:
```bash
docker exec -it landgo-postgres psql -U postgres -d landgo -c "SELECT token FROM password_reset_tokens WHERE used = false ORDER BY created_at DESC LIMIT 1;"
```

---

#### Step 3.2: Validate Reset Token

```
GET {{BASE_URL}}/api/v1/auth/reset-password/validate?token={{RESET_TOKEN}}
```

**Expected:** 200 OK with message "Token is valid"

---

#### Step 3.3: Reset Password

```
POST {{BASE_URL}}/api/v1/auth/reset-password
Content-Type: application/json

{
  "token": "{{RESET_TOKEN}}",
  "newPassword": "NewPass@123"
}
```

**Expected:** 200 OK with message "Password has been reset successfully"

---

#### Step 3.4: Login with New Password

```
POST {{BASE_URL}}/api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "NewPass@123"
}
```

**Expected:** 200 OK with `accessToken`  
**Action:** Save `accessToken` as `{{TOKEN}}`

> ⚠️ **Note:** All subsequent steps should use the new password `NewPass@123` instead of `password123`.

---

#### Step 4: Get Current User

```
GET {{BASE_URL}}/api/v1/auth/me
Authorization: Bearer {{TOKEN}}
```

**Expected:** 200 OK, role = `USER`

---

#### Step 5: Subscribe to PREMIUM Plan

```
POST {{BASE_URL}}/api/v1/subscriptions
Authorization: Bearer {{TOKEN}}
Content-Type: application/json

{
  "plan": "PREMIUM",
  "paymentMethod": "credit_card",
  "autoRenew": true
}
```

**Expected:** 201 Created

---

#### Step 6: Get Current Subscription

```
GET {{BASE_URL}}/api/v1/subscriptions/current
Authorization: Bearer {{TOKEN}}
```

**Expected:** 200 OK with PREMIUM plan details

---

#### Step 7: Register as Vendor

```
POST {{BASE_URL}}/api/v1/vendor/register
Authorization: Bearer {{TOKEN}}
Content-Type: application/json

{
  "companyName": "LandCorp",
  "companyDescription": "Premium land deals across Texas",
  "businessLicense": "TX-LIC-2026-001",
  "businessAddress": "123 Main Street",
  "businessCity": "Austin",
  "businessState": "Texas",
  "businessZipCode": "73301",
  "businessCountry": "USA",
  "website": "https://landcorp.com"
}
```

**Expected:** 201 Created  
**Action:** Save the vendor `id` as `{{VENDOR_ID}}`

---

#### Step 8: ⚠️ Re-Login (Get VENDOR Token)

> **Critical Step!** After vendor registration, your role changed to `VENDOR`. The old token still has `USER` role. You must login again.

```
POST {{BASE_URL}}/api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Action:** Update `{{TOKEN}}` with new `accessToken` (now has VENDOR role)

---

#### Step 9: Create Land Listing

```
POST {{BASE_URL}}/api/v1/vendor/lands
Authorization: Bearer {{TOKEN}}
Content-Type: application/json

{
  "title": "Beautiful Residential Plot in Austin",
  "description": "A stunning 5000 sq ft residential plot with all utilities",
  "landType": "RESIDENTIAL",
  "address": "456 Oak Avenue",
  "city": "Austin",
  "state": "Texas",
  "zipCode": "73301",
  "country": "USA",
  "latitude": 30.2672,
  "longitude": -97.7431,
  "price": 150000.00,
  "areaSqFt": 5000.00,
  "frontage": 50.00,
  "depth": 100.00,
  "hasWaterAccess": true,
  "hasElectricity": true,
  "hasRoadAccess": true,
  "hasSewage": false,
  "zoningInfo": "R-1 Residential",
  "topography": "Flat terrain",
  "soilType": "Loamy",
  "imageUrls": ["https://example.com/land1.jpg", "https://example.com/land2.jpg"],
  "videoUrl": "https://example.com/tour-video.mp4"
}
```

**Expected:** 201 Created, status = `PENDING_APPROVAL`  
**Action:** Save the land `id` as `{{LAND_ID}}`

---

#### Step 10: Get My Vendor Lands

```
GET {{BASE_URL}}/api/v1/vendor/lands?page=0&size=20
Authorization: Bearer {{TOKEN}}
```

**Expected:** 200 OK with list of your listings

---

#### Step 11: Update Land Listing

```
PUT {{BASE_URL}}/api/v1/vendor/lands/{{LAND_ID}}
Authorization: Bearer {{TOKEN}}
Content-Type: application/json

{
  "title": "UPDATED - Premium Residential Plot in Austin",
  "description": "Updated: A stunning 5000 sq ft plot, now with sewage",
  "landType": "RESIDENTIAL",
  "address": "456 Oak Avenue",
  "city": "Austin",
  "state": "Texas",
  "zipCode": "73301",
  "country": "USA",
  "price": 165000.00,
  "areaSqFt": 5000.00,
  "hasWaterAccess": true,
  "hasElectricity": true,
  "hasRoadAccess": true,
  "hasSewage": true
}
```

**Expected:** 200 OK

---

#### Step 12: Get Land by ID (Public)

```
GET {{BASE_URL}}/api/v1/lands/{{LAND_ID}}
```

**Expected:** 200 OK (viewCount incremented)

---

#### Step 13: Browse All Active Lands (Public)

```
GET {{BASE_URL}}/api/v1/lands?page=0&size=20
```

**Expected:** 200 OK  
**Note:** Only `ACTIVE` status lands are returned. Your new listing is `PENDING_APPROVAL`.

---

#### Step 14: Search Lands (Public)

```
GET {{BASE_URL}}/api/v1/lands/search?query=Austin&page=0&size=20
```

---

#### Step 15: Filter Lands (Public)

```
GET {{BASE_URL}}/api/v1/lands/filter?city=Austin&type=RESIDENTIAL&minPrice=100000&maxPrice=200000
```

---

#### Step 16: Get Recent Listings (Public)

```
GET {{BASE_URL}}/api/v1/lands/recent?limit=5
```

---

#### Step 17: Get Popular Listings (Public)

```
GET {{BASE_URL}}/api/v1/lands/popular?limit=5
```

---

#### Step 18: Get Vendor Profile (Self)

```
GET {{BASE_URL}}/api/v1/vendor/profile
Authorization: Bearer {{TOKEN}}
```

---

#### Step 19: Update Vendor Profile

```
PUT {{BASE_URL}}/api/v1/vendor/profile
Authorization: Bearer {{TOKEN}}
Content-Type: application/json

{
  "companyName": "LandCorp Premium",
  "companyDescription": "The #1 land marketplace in Texas",
  "businessAddress": "789 New Street, Suite 200",
  "businessCity": "Austin",
  "businessState": "Texas",
  "businessZipCode": "73301",
  "businessCountry": "USA",
  "website": "https://landcorp-premium.com"
}
```

---

#### Step 20: Browse/Search Vendors (Public)

```
GET {{BASE_URL}}/api/v1/vendors?page=0&size=20
GET {{BASE_URL}}/api/v1/vendors/search?query=LandCorp
```

---

#### Step 21: Get Vendor by ID (Requires Subscription)

```
GET {{BASE_URL}}/api/v1/vendors/{{VENDOR_ID}}
Authorization: Bearer {{TOKEN}}
```

**Expected:** 200 OK (because we have PREMIUM subscription from Step 5)

---

#### Step 22: Cancel Subscription

```
POST {{BASE_URL}}/api/v1/subscriptions/cancel?reason=Testing cancellation flow
Authorization: Bearer {{TOKEN}}
```

**Expected:** 200 OK, status changes to `CANCELLED`

---

#### Step 23: Delete Land Listing

```
DELETE {{BASE_URL}}/api/v1/vendor/lands/{{LAND_ID}}
Authorization: Bearer {{TOKEN}}
```

**Expected:** 200 OK (soft-deleted)

---

#### Step 24: Verify Deletion

```
GET {{BASE_URL}}/api/v1/vendor/lands
Authorization: Bearer {{TOKEN}}
```

**Expected:** The deleted land should no longer appear.

---

### 8.3 Negative Test Cases

| # | Test | Endpoint | Expected |
|---|------|----------|----------|
| 1 | Register with existing email | POST /auth/register | 400 "Email already registered" |
| 2 | Login with wrong password | POST /auth/login | 401 "Invalid email or password" |
| 3 | Access protected API without token | GET /auth/me | 401 |
| 4 | Access vendor API as USER | POST /vendor/lands | 403 |
| 5 | Update another vendor's land | PUT /vendor/lands/{id} | 403 |
| 6 | Subscribe when already subscribed | POST /subscriptions | 400 "Already has active subscription" |
| 7 | Register as vendor when already vendor | POST /vendor/register | 400 "User is already a vendor" |
| 8 | Get non-existent land | GET /lands/{random-uuid} | 404 |
| 9 | Create land with missing required fields | POST /vendor/lands | 400 validation errors |
| 10 | Get vendor details without subscription | GET /vendors/{id} | 403 |
| 11 | Forgot password for OAuth2 user | POST /auth/forgot-password | 200 (silent — no email sent for Google/Apple users) |
| 12 | Validate with expired/invalid token | GET /auth/reset-password/validate?token=invalid | 400 "Invalid or expired password reset token" |
| 13 | Reset password with used token | POST /auth/reset-password | 400 "Invalid or expired password reset token" |
| 14 | Reset password with short password | POST /auth/reset-password | 400 validation error (min 8 chars) |

---

*End of API Documentation*
