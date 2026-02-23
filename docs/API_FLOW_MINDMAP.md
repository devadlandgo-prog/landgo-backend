# LandGo Backend - API Flow Mind Map

> **Version:** 1.2.0  
> **Last Updated:** 23 February 2026

---

## Complete API Flow Map

```
                            ┌─────────────────────────────────────┐
                            │          LandGo Platform            │
                            │    http://localhost:8080             │
                            └───────────────┬─────────────────────┘
                                            │
                   ┌────────────────────────┼────────────────────────┐
                   │                        │                        │
                   ▼                        ▼                        ▼
          ┌────────────────┐     ┌──────────────────┐     ┌──────────────────┐
          │  PUBLIC ZONE   │     │  AUTH REQUIRED    │     │  ROLE REQUIRED   │
          │  (No Auth)     │     │  (Any User)       │     │  (VENDOR/ADMIN)  │
          └────────┬───────┘     └────────┬─────────┘     └────────┬─────────┘
                   │                      │                        │
                   ▼                      ▼                        ▼
     ┌──────────────────────┐  ┌────────────────────┐  ┌────────────────────────┐
     │ • Auth (register,    │  │ • Auth (me)        │  │ • Vendor land CRUD     │
     │   login, oauth2)     │  │ • Subscriptions    │  │ • Vendor profile mgmt  │
     │ • Email verification │  │ • Vendor details   │  │ • Admin (future)       │
     │ • Forgot/Reset       │  │ • Vendor register  │  │                        │
     │   password            │  │                    │  │                        │
     │ • Browse lands       │  │                    │  │                        │
     │ • Search & Filter    │  │                    │  │                        │
     │ • Browse vendors     │  │                    │  │                        │
     │ • Swagger UI         │  │                    │  │                        │
     │ • Health check       │  │                    │  │                        │
     └──────────────────────┘  └────────────────────┘  └────────────────────────┘
```

---

## 1. User Journey Flow

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           USER JOURNEY                                       │
│                                                                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐│
│  │ Register │───▶│  Verify  │───▶│  Login   │───▶│ Get JWT  │───▶│ Browse   ││
│  │ Account  │    │  Email   │    │          │    │  Token   │    │  Lands   ││
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘    └─────┬────┘│
│                                                        │                     │
│                                   ┌────────────────────┤                     │
│                                   │                    │                     │
│                                   ▼                    ▼                     │
│                          ┌──────────────┐    ┌─────────────────┐            │
│                          │  Subscribe   │    │ Search/Filter   │            │
│                          │  to a Plan   │    │    Lands        │            │
│                          └──────┬───────┘    └─────────────────┘            │
│                                 │                                            │
│                                 ▼                                            │
│                        ┌────────────────┐                                   │
│                        │ View Vendor    │                                   │
│                        │  Details       │                                   │
│                        │ (Requires sub) │                                   │
│                        └────────────────┘                                   │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Vendor Journey Flow

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          VENDOR JOURNEY                                      │
│                                                                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────────┐    ┌──────────┐          │
│  │ Register │───▶│  Verify  │───▶│  Login   │───▶│ Register as  │───▶│ RE-LOGIN │          │
│  │ as User  │    │  Email   │    │          │    │   VENDOR     │    │ (new JWT)│          │
│  └──────────┘    └──────────┘    └──────────┘    └──────────────┘    └─────┬────┘          │
│                                                            │                │
│                              ┌─────────────────────────────┤                │
│                              │              │              │                │
│                              ▼              ▼              ▼                │
│                     ┌──────────────┐ ┌───────────┐ ┌─────────────┐         │
│                     │ Create Land  │ │  Update   │ │   Manage    │         │
│                     │  Listing     │ │  Profile  │ │   Lands     │         │
│                     └──────┬───────┘ └───────────┘ └──────┬──────┘         │
│                            │                              │                 │
│                            ▼                    ┌─────────┼─────────┐      │
│                   ┌─────────────────┐           │         │         │      │
│                   │  Status:        │           ▼         ▼         ▼      │
│                   │  PENDING_       │       ┌───────┐ ┌───────┐ ┌───────┐ │
│                   │  APPROVAL       │       │ View  │ │Update │ │Delete │ │
│                   └─────────────────┘       │ List  │ │ Land  │ │ Land  │ │
│                                             └───────┘ └───────┘ └───────┘ │
│                                                                            │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Authentication Flow

```
                        ┌──────────────┐
                        │   Client     │
                        └──────┬───────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
     ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
     │   Email     │  │   Google    │  │   Apple     │
     │  Register   │  │   OAuth2   │  │   OAuth2   │
     └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
            │                │                │
            │         ┌──────┴──────┐  ┌──────┴──────┐
            │         │ Validate   │  │ Validate   │
            │         │ ID Token   │  │ ID Token   │
            │         │ w/ Google  │  │ w/ Apple   │
            │         └──────┬──────┘  └──────┬──────┘
            │                │                │
            ▼                ▼                ▼
     ┌─────────────────────────────────────────────┐
     │            AuthService                       │
     │  ┌─────────────────────────────────────┐    │
     │  │ 1. Find/Create User in Database     │    │
     │  │ 2. Hash password (EMAIL only)       │    │
     │  │ 3. Generate Access Token (1 hr)     │    │
     │  │ 4. Generate Refresh Token (7 days)  │    │
     │  └─────────────────────────────────────┘    │
     └─────────────────────┬───────────────────────┘
                           │
                           ▼
                ┌──────────────────┐
                │  AuthResponse    │
                │  {accessToken,   │
                │   refreshToken,  │
                │   user}          │
                └──────────────────┘
```

---

## 4. Email Verification Flow

```
    Client                    Server                     Database              Email
      │                         │                           │                    │
      │  1. POST /auth/         │                           │                    │
      │     register            │                           │                    │
      │  {fullName, email,      │                           │                    │
      │   password, userType}   │                           │                    │
      │ ────────────────────▶   │                           │                    │
      │                         │  INSERT user              │                    │
      │                         │  (emailVerified=false)    │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │                           │                    │
      │                         │  Generate 6-digit code    │                    │
      │                         │  (15-min expiry)          │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │  INSERT email_            │                    │
      │                         │  verification_tokens      │                    │
      │                         │                           │                    │
      │                         │  Send email (async)       │                    │
      │                         │ ──────────────────────────│───────────────▶    │
      │                         │                           │ HTML email with    │
      │  ◀── JWT tokens +      │                           │ 6-digit code       │
      │   "Verification code   │                           │                    │
      │    sent to email"       │                           │                    │
      │                         │                           │                    │
      │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ │─ ─ ─ ─ ─ ─ ─ ─   │
      │  User reads code from email                        │                    │
      │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ │─ ─ ─ ─ ─ ─ ─ ─   │
      │                         │                           │                    │
      │  2. POST /auth/         │                           │                    │
      │     verify-email        │                           │                    │
      │  {email, code}          │                           │                    │
      │ ────────────────────▶   │                           │                    │
      │                         │  Find latest token        │                    │
      │                         │  (unused, for user)       │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │  Check: !expired          │                    │
      │                         │  Check: attempts < 5      │                    │
      │                         │  Check: code matches      │                    │
      │                         │                           │                    │
      │                         │  ✅ Mark token used       │                    │
      │                         │  UPDATE user              │                    │
      │                         │  emailVerified=true       │                    │
      │                         │ ─────────────────────▶    │                    │
      │  ◀── 200 "Email        │                           │                    │
      │       verified"         │                           │                    │
      │                         │                           │                    │

     ┌───────────────────────────────────────────────────────────────┐
     │ EMAIL VERIFICATION RULES                                     │
     │                                                              │
     │  • 6-digit numeric code (100000–999999)                      │
     │  • Code expires after 15 minutes                             │
     │  • Maximum 5 verification attempts per code                  │
     │  • Wrong code increments attempt counter                     │
     │  • Resend generates a new code & invalidates old ones        │
     │  • OAuth2 users (Google/Apple) are auto-verified             │
     │  • Already-verified users cannot re-verify or resend         │
     │  • Email sent asynchronously (@Async) for fast response      │
     └───────────────────────────────────────────────────────────────┘
```

---

## 5. Password Reset Flow

```
    Client                    Server                     Database              Email
      │                         │                           │                    │
      │  1. POST /auth/         │                           │                    │
      │     forgot-password     │                           │                    │
      │  {email: "user@..."}   │                           │                    │
      │ ────────────────────▶   │                           │                    │
      │                         │  Find user by email       │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │  ◀─── user (EMAIL only)   │                    │
      │                         │                           │                    │
      │                         │  Invalidate old tokens    │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │                           │                    │
      │                         │  Generate UUID token      │                    │
      │                         │  (30-min expiry)          │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │  INSERT password_reset_   │                    │
      │                         │  tokens                   │                    │
      │                         │                           │                    │
      │                         │  Send email (async)       │                    │
      │                         │ ──────────────────────────│───────────────▶    │
      │                         │                           │    HTML email with │
      │  ◀── 200 "Reset link   │                           │    reset link      │
      │       sent to email"    │                           │                    │
      │                         │                           │                    │
      │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ │─ ─ ─ ─ ─ ─ ─ ─   │
      │  User clicks link from email                       │                    │
      │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ │─ ─ ─ ─ ─ ─ ─ ─   │
      │                         │                           │                    │
      │  2. GET /auth/reset-    │                           │                    │
      │     password/validate   │                           │                    │
      │     ?token={uuid}       │                           │                    │
      │ ────────────────────▶   │                           │                    │
      │                         │  Find token (unused)      │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │  ◀─── token record        │                    │
      │                         │  Check: !expired          │                    │
      │  ◀── 200 "Token valid"  │                           │                    │
      │                         │                           │                    │
      │  3. POST /auth/         │                           │                    │
      │     reset-password      │                           │                    │
      │  {token, newPassword}   │                           │                    │
      │ ────────────────────▶   │                           │                    │
      │                         │  Validate token again     │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │                           │                    │
      │                         │  UPDATE user.password     │                    │
      │                         │  (BCrypt encoded)         │                    │
      │                         │ ─────────────────────▶    │                    │
      │                         │                           │                    │
      │                         │  Mark token used=true     │                    │
      │                         │  Invalidate other tokens  │                    │
      │                         │ ─────────────────────▶    │                    │
      │  ◀── 200 "Password     │                           │                    │
      │       reset successful" │                           │                    │
      │                         │                           │                    │
      │  4. POST /auth/login    │                           │                    │
      │  {email, newPassword}   │                           │                    │
      │ ────────────────────▶   │                           │                    │
      │  ◀── JWT tokens ─────── │                           │                    │
      │                         │                           │                    │

     ┌───────────────────────────────────────────────────────────────┐
     │ PASSWORD RESET RULES                                         │
     │                                                              │
     │  • Only EMAIL auth provider users (not Google/Apple)         │
     │  • Token expires after 30 minutes                            │
     │  • Token is single-use (marked as used after reset)          │
     │  • Old unused tokens are invalidated on new request          │
     │  • New password must be at least 8 characters                │
     │  • Response is always 200 to prevent email enumeration       │
     │  • Email sent asynchronously (@Async) for fast response      │
     │  • Reset URL: http://localhost:3000/reset-password?token=    │
     └───────────────────────────────────────────────────────────────┘
```

---

## 6. JWT Security Filter Flow

```
        ┌─────────────────────────────────────────────────────────┐
        │                 Every HTTP Request                      │
        └────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │ JwtAuthenticationFilter │
                    └────────────┬───────────┘
                                 │
                    ┌────────────▼───────────┐
                    │ Has Authorization      │
                    │ header with "Bearer "? │
                    └────────┬───────┬───────┘
                             │       │
                         YES │       │ NO
                             │       │
                             ▼       ▼
                   ┌──────────────┐  │
                   │ Extract JWT  │  │
                   │ Token        │  │
                   └──────┬───────┘  │
                          │          │
                          ▼          │
                   ┌──────────────┐  │
                   │ Validate     │  │
                   │ Token        │  │
                   │ (Signature,  │  │
                   │  Expiry)     │  │
                   └──────┬───────┘  │
                          │          │
                    VALID │ INVALID  │
                          │   │      │
                          ▼   ▼      ▼
               ┌──────────────┐  ┌────────────────────────┐
               │ Load User    │  │ Continue Filter Chain  │
               │ from DB by   │  │ (No Authentication)    │
               │ userId in    │  └────────────┬───────────┘
               │ JWT 'sub'    │               │
               └──────┬───────┘               │
                      │                       │
                      ▼                       │
               ┌──────────────┐               │
               │ Set Security │               │
               │ Context with │               │
               │ UserPrincipal│               │
               └──────┬───────┘               │
                      │                       │
                      ▼                       ▼
               ┌──────────────────────────────────────────┐
               │          SecurityConfig Rules            │
               │                                          │
               │  /api/v1/auth/**     → permitAll         │
               │  /api/v1/lands (GET) → permitAll         │
               │  /api/v1/vendors (GET) → permitAll       │
               │  /api/v1/vendor/**  → VENDOR role        │
               │  /api/v1/admin/**   → ADMIN role         │
               │  everything else    → authenticated      │
               └──────────────────────────────────────────┘
```

---

## 7. Land Listing Lifecycle

```
     ┌────────────┐        ┌───────────────────┐        ┌────────────┐
     │   DRAFT    │───────▶│ PENDING_APPROVAL  │───────▶│   ACTIVE   │
     └────────────┘        └───────────────────┘        └─────┬──────┘
                                    │                         │
                                    │                    ┌────┼────┐
                                    ▼                    │         │
                            ┌──────────────┐             ▼         ▼
                            │  (Rejected)  │      ┌──────────┐ ┌──────────┐
                            │   INACTIVE   │      │   SOLD   │ │ INACTIVE │
                            └──────────────┘      └──────────┘ └──────────┘

     ┌───────────────────────────────────────────────────────────────┐
     │ Current Implementation:                                      │
     │ • Vendor creates → status = PENDING_APPROVAL                 │
     │ • No admin approval API exists yet (future scope)            │
     │ • Public listing page shows only ACTIVE lands                │
     │ • Vendor can view their own lands of ANY status              │
     │ • Delete = soft delete (sets deleted = true)                 │
     └───────────────────────────────────────────────────────────────┘
```

---

## 8. Subscription Access Control

```
                          ┌────────────────┐
                          │  User Action   │
                          └───────┬────────┘
                                  │
                                  ▼
                    ┌─────────────────────────────┐
                    │ What does the user want?     │
                    └──────┬──────────────┬────────┘
                           │              │
                  ┌────────▼───────┐  ┌───▼────────────────┐
                  │ Browse Lands   │  │ View Vendor Detail │
                  │ (search,filter)│  │ (contact info)     │
                  └────────┬───────┘  └───┬────────────────┘
                           │              │
                           ▼              ▼
                  ┌─────────────┐  ┌──────────────────────┐
                  │  FREE /     │  │ Check Subscription   │
                  │  No Login   │  │ Plan                 │
                  │  Required   │  └──────┬───────────────┘
                  └─────────────┘         │
                                ┌─────────┼──────────────┐
                                │         │              │
                                ▼         ▼              ▼
                          ┌──────┐  ┌──────────┐  ┌───────────┐
                          │ FREE │  │  BASIC   │  │ PREMIUM/  │
                          │ plan │  │  plan    │  │ENTERPRISE │
                          └──┬───┘  └────┬─────┘  └─────┬─────┘
                             │           │              │
                             ▼           ▼              ▼
                        ┌────────┐  ┌─────────┐   ┌─────────────┐
                        │ ❌ 403 │  │ ✅ View │   │ ✅ View     │
                        │Denied  │  │ Vendor  │   │ + Premium   │
                        └────────┘  │ Details │   │   Listings  │
                                    └─────────┘   │ + Direct    │
                                                  │   Contact   │
                                                  └─────────────┘

     ┌───────────────────────────────────────────────────────────────────┐
     │ PLAN COMPARISON                                                  │
     │                                                                  │
     │  Feature              │ FREE  │ BASIC │ PREMIUM │ ENTERPRISE   │
     │  ─────────────────────┼───────┼───────┼─────────┼────────────  │
     │  Browse/Search Lands  │  ✅   │  ✅   │   ✅    │    ✅       │
     │  Vendor Views/Month   │   5   │  20   │  100    │  Unlimited  │
     │  Saved Lands          │  10   │  50   │  200    │  Unlimited  │
     │  Premium Listings     │  ❌   │  ❌   │   ✅    │    ✅       │
     │  Direct Contact       │  ❌   │  ✅   │   ✅    │    ✅       │
     │  Price                │ $0    │ $9.99 │ $29.99  │  $99.99     │
     │  Duration             │  ∞    │ 30d   │  30d    │   365d      │
     └───────────────────────────────────────────────────────────────────┘
```

---

## 9. OAuth2 Strategy Pattern

```
                       ┌──────────────────────┐
                       │ POST /auth/oauth2    │
                       │ {token, authProvider} │
                       └──────────┬───────────┘
                                  │
                                  ▼
                    ┌─────────────────────────────┐
                    │    OAuth2StrategyFactory     │
                    │    getStrategy(provider)     │
                    └──────────┬──────────────────┘
                               │
                    ┌──────────┼──────────┐
                    │                     │
                    ▼                     ▼
          ┌──────────────────┐  ┌──────────────────┐
          │  GoogleAuth      │  │  AppleAuth       │
          │  Strategy        │  │  Strategy        │
          │                  │  │                  │
          │ • Calls Google   │  │ • Calls Apple    │
          │   tokeninfo API  │  │   keys endpoint  │
          │ • Extracts email,│  │ • Verifies JWT   │
          │   name, picture  │  │ • Extracts email,│
          │                  │  │   name           │
          └────────┬─────────┘  └────────┬─────────┘
                   │                     │
                   └──────────┬──────────┘
                              │
                              ▼
                   ┌──────────────────────┐
                   │    OAuth2UserInfo    │
                   │  {email, firstName,  │
                   │   lastName, picture} │
                   └──────────┬───────────┘
                              │
                              ▼
                   ┌──────────────────────┐
                   │  AuthService         │
                   │  processOAuth2User() │
                   │  • Find or create    │
                   │  • Return JWT tokens │
                   └──────────────────────┘
```

---

## 10. Complete Endpoint Map

```
┌────────────────────────────────────────────────────────────────────────────┐
│                                                                            │
│  🔓 PUBLIC ENDPOINTS                                                      │
│  ─────────────────                                                        │
│  POST   /api/v1/auth/register           Register new user                 │
│  POST   /api/v1/auth/verify-email      Verify email (6-digit code)       │
│  POST   /api/v1/auth/resend-verification Resend verification code        │
│  POST   /api/v1/auth/login              Email/password login              │
│  POST   /api/v1/auth/oauth2             Google/Apple OAuth2               │
│  POST   /api/v1/auth/forgot-password    Request password reset            │
│  GET    /api/v1/auth/reset-password/validate  Validate reset token        │
│  POST   /api/v1/auth/reset-password     Reset password with token         │
│  GET    /api/v1/lands                   Browse all active lands           │
│  GET    /api/v1/lands/{id}              View single land (+ view count)   │
│  GET    /api/v1/lands/search            Search lands by keyword           │
│  GET    /api/v1/lands/filter            Filter lands by criteria          │
│  GET    /api/v1/lands/recent            Recent listings                   │
│  GET    /api/v1/lands/popular           Popular listings                  │
│  GET    /api/v1/vendors                 Browse verified vendors           │
│  GET    /api/v1/vendors/search          Search vendors by name/city       │
│  GET    /swagger-ui.html                API documentation UI              │
│  GET    /actuator/health                Health check                      │
│                                                                            │
│  🔐 AUTHENTICATED ENDPOINTS (any logged-in user)                          │
│  ────────────────────────────────────────────────                          │
│  GET    /api/v1/auth/me                 Get current user profile          │
│  POST   /api/v1/vendor/register         Register as vendor                │
│  GET    /api/v1/vendors/{id}            View vendor details (needs sub)   │
│  POST   /api/v1/subscriptions           Subscribe to a plan              │
│  GET    /api/v1/subscriptions/current   Get current subscription          │
│  POST   /api/v1/subscriptions/cancel    Cancel subscription               │
│                                                                            │
│  🏪 VENDOR ENDPOINTS (VENDOR role required)                               │
│  ──────────────────────────────────────────                               │
│  POST   /api/v1/vendor/lands            Create land listing               │
│  GET    /api/v1/vendor/lands            Get my land listings              │
│  PUT    /api/v1/vendor/lands/{id}       Update my listing                 │
│  DELETE /api/v1/vendor/lands/{id}       Delete my listing (soft)          │
│  GET    /api/v1/vendor/profile          Get my vendor profile             │
│  PUT    /api/v1/vendor/profile          Update my vendor profile          │
│                                                                            │
│  🛡️ ADMIN ENDPOINTS (ADMIN role required — future scope)                  │
│  ─────────────────────────────────────────────────────                    │
│  (Not yet implemented)                                                    │
│  • Approve/reject land listings                                           │
│  • Verify vendors                                                         │
│  • User management                                                        │
│                                                                            │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 11. Data Model Relationship Map

```
                           ┌──────────────────┐
                           │      User        │
                           │                  │
                           │  id (UUID, PK)   │
                           │  firstName       │
                           │  lastName        │
                           │  email (unique)  │
                           │  password        │
                           │  phone           │
                           │  profileImageUrl │
                           │  authProvider    │
                           │  providerId      │
                           │  role            │
                           │  emailVerified   │
                           │  active          │
                           └────┬───┬───┬─────┘
                                │   │   │
               ┌────────────────┘   │   └────────────────┐
               │                    │                    │
               ▼                    ▼                    ▼
    ┌──────────────────┐  ┌─────────────────┐  ┌────────────────┐
    │  VendorProfile   │  │  Subscription   │  │  Saved Lands   │
    │                  │  │                 │  │  (Join Table)  │
    │  id (UUID, PK)   │  │  id (UUID, PK)  │  │                │
    │  companyName     │  │  plan           │  │  user_id (FK)  │
    │  companyDesc     │  │  status         │  │  land_id (FK)  │
    │  businessLicense │  │  startDate      │  └────────┬───────┘
    │  businessAddress │  │  endDate        │           │
    │  businessCity    │  │  amount         │           │
    │  verified        │  │  autoRenew      │           │
    │  rating          │  │  maxVendorViews │           │
    │  totalReviews    │  │  maxSavedLands  │           │
    │  totalLandsListed│  └─────────────────┘           │
    │  totalLandsSold  │                                │
    └────────┬─────────┘                                │
             │                                          │
             │ (one-to-many)                            │
             ▼                                          │
    ┌──────────────────┐                                │
    │      Land        │◀───────────────────────────────┘
    │                  │
    │  id (UUID, PK)   │
    │  title           │
    │  description     │
    │  landType        │
    │  status          │
    │  address/city/   │
    │  state/zip/      │
    │  country         │
    │  lat/lng         │
    │  price           │
    │  areaSqFt        │
    │  has* features   │
    │  imageUrls       │
    │  viewCount       │
    │  inquiryCount    │
    └──────────────────┘

    ┌───────────────────────────────────────────────────┐
    │ RELATIONSHIPS                                     │
    │                                                   │
    │  User ──(1:1)──▶ VendorProfile                   │
    │  User ──(1:1)──▶ Subscription                    │
    │  User ──(1:N)──▶ PasswordResetToken              │
    │  User ──(M:N)──▶ Saved Lands (via join table)    │
    │  VendorProfile ──(1:N)──▶ Land                   │
    └───────────────────────────────────────────────────┘
```

---

## 12. Testing Flow Sequence Diagram

```
    Client                     Server                    Database
      │                          │                          │
      │  1. POST /auth/register  │                          │
      │ ─────────────────────▶   │                          │
      │                          │  INSERT user              │
      │                          │  + verification code      │
      │                          │ ─────────────────────▶   │
      │  ◀───── JWT tokens ───── │  (+ email sent async)    │
      │                          │                          │
      │  1b. POST /auth/         │                          │
      │      verify-email        │                          │
      │  {email, 6-digit code}   │                          │
      │ ─────────────────────▶   │                          │
      │                          │  Verify code, set        │
      │                          │  emailVerified=true      │
      │                          │ ─────────────────────▶   │
      │  ◀── 200 "verified" ─── │                          │
      │                          │                          │
      │  2. POST /auth/login     │                          │
      │ ─────────────────────▶   │                          │
      │                          │  SELECT user by email    │
      │                          │ ─────────────────────▶   │
      │                          │  ◀── user with bcrypt pw │
      │                          │  verify password         │
      │  ◀───── JWT tokens ───── │                          │
      │                          │                          │
      │  3. POST /auth/          │                          │
      │     forgot-password      │                          │
      │ ─────────────────────▶   │                          │
      │                          │  Find user, gen token    │
      │                          │ ─────────────────────▶   │
      │                          │  Send email (async)      │
      │  ◀── 200 "link sent" ── │                          │
      │                          │                          │
      │  4. GET /auth/reset-     │                          │
      │     password/validate    │                          │
      │ ─────────────────────▶   │                          │
      │                          │  SELECT token (unused)   │
      │                          │ ─────────────────────▶   │
      │  ◀── 200 "valid" ────── │                          │
      │                          │                          │
      │  5. POST /auth/          │                          │
      │     reset-password       │                          │
      │ ─────────────────────▶   │                          │
      │                          │  UPDATE password         │
      │                          │  Mark token used         │
      │                          │ ─────────────────────▶   │
      │  ◀── 200 "reset ok" ─── │                          │
      │                          │                          │
      │  6. GET /lands (public)  │                          │
      │ ─────────────────────▶   │                          │
      │                          │  SELECT lands            │
      │                          │  WHERE status=ACTIVE     │
      │                          │ ─────────────────────▶   │
      │  ◀───── land list ────── │                          │
      │                          │                          │
      │  7. POST /subscriptions  │                          │
      │  (Bearer token)         │                          │
      │ ─────────────────────▶   │                          │
      │                          │  INSERT subscription     │
      │                          │ ─────────────────────▶   │
      │  ◀── subscription ────── │                          │
      │                          │                          │
      │  8. POST /vendor/register│                          │
      │  (Bearer token)         │                          │
      │ ─────────────────────▶   │                          │
      │                          │  INSERT vendor_profile   │
      │                          │  UPDATE user.role=VENDOR │
      │                          │ ─────────────────────▶   │
      │  ◀── vendor profile ──── │                          │
      │                          │                          │
      │  9. POST /auth/login     │  ⚠️ Must re-login to    │
      │ ─────────────────────▶   │  get VENDOR role token   │
      │  ◀── JWT (role=VENDOR) ─ │                          │
      │                          │                          │
      │  10. POST /vendor/lands  │                          │
      │  (Bearer vendor token)  │                          │
      │ ─────────────────────▶   │                          │
      │                          │  INSERT land             │
      │                          │  status=PENDING_APPROVAL │
      │                          │ ─────────────────────▶   │
      │  ◀── land listing ────── │                          │
      │                          │                          │
```

---

## 13. Error Flow

```
                    ┌─────────────┐
                    │   Request   │
                    └──────┬──────┘
                           │
                           ▼
               ┌───────────────────────┐
               │ Validation Check      │
               │ (@Valid annotations)  │
               └─────┬───────┬────────┘
                     │       │
                  PASS     FAIL
                     │       │
                     │       ▼
                     │  ┌─────────────────────────┐
                     │  │ 400 Bad Request          │
                     │  │ {errors: {field: msg}}   │
                     │  └─────────────────────────┘
                     ▼
              ┌──────────────────────┐
              │ Security Check       │
              │ (JWT + Role)         │
              └──────┬────────┬──────┘
                     │        │
                  PASS     FAIL
                     │        │
                     │        ▼
                     │   ┌────────────────────────┐
                     │   │ 401 Unauthorized OR    │
                     │   │ 403 Forbidden          │
                     │   └────────────────────────┘
                     ▼
              ┌──────────────────────┐
              │ Business Logic       │
              │ (Service Layer)      │
              └──────┬────────┬──────┘
                     │        │
                  PASS     FAIL
                     │        │
                     │        ▼
                     │   ┌────────────────────────┐
                     │   │ 400 / 404 / 403        │
                     │   │ Custom ApiException     │
                     │   └────────────────────────┘
                     ▼
              ┌──────────────────────┐
              │ 200/201 Success      │
              │ {success: true,      │
              │  data: {...}}        │
              └──────────────────────┘
```

---

*End of API Flow Mind Map*
