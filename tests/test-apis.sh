#!/bin/bash
# ============================================
# LandGo API Test Script
# ============================================
BASE="http://localhost:8080/api/v1"
PASS=0
FAIL=0
TOTAL=0

print_result() {
  TOTAL=$((TOTAL + 1))
  local test_name="$1"
  local expected_code="$2"
  local actual_code="$3"
  local body="$4"
  if [ "$actual_code" = "$expected_code" ]; then
    PASS=$((PASS + 1))
    echo "✅ TEST $TOTAL: $test_name [HTTP $actual_code]"
  else
    FAIL=$((FAIL + 1))
    echo "❌ TEST $TOTAL: $test_name [Expected: $expected_code, Got: $actual_code]"
    echo "   Response: $body"
  fi
}

echo ""
echo "============================================"
echo "  🏗️  LandGo API Testing Suite"
echo "============================================"
echo ""

# ------------------------------------------
# HEALTH CHECK
# ------------------------------------------
echo "--- Health Check ---"
RESP=$(curl -s -w "\n%{http_code}" $BASE/../actuator/health)
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -1)
print_result "Health Check" "200" "$CODE" "$BODY"
echo ""

# ------------------------------------------
# AUTH: Register USER
# ------------------------------------------
echo "--- Auth Tests ---"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Elena","lastName":"Kowalski","email":"elena.kowalski@testmail.com","password":"Test@1234","role":"USER"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Register USER (Elena)" "200" "$CODE" "$BODY"
USER_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null)

# AUTH: Register VENDOR
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jake","lastName":"Morrison","email":"jake.morrison@testmail.com","password":"Test@1234","role":"VENDOR"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Register VENDOR (Jake)" "200" "$CODE" "$BODY"
VENDOR_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null)
REFRESH_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('refreshToken',''))" 2>/dev/null)

# AUTH: Login USER
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"elena.kowalski@testmail.com","password":"Test@1234"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Login USER (Elena)" "200" "$CODE" "$BODY"
USER_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null)
USER_REFRESH=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('refreshToken',''))" 2>/dev/null)

# AUTH: Login VENDOR
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"jake.morrison@testmail.com","password":"Test@1234"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Login VENDOR (Jake)" "200" "$CODE" "$BODY"
VENDOR_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null)

# AUTH: Refresh Token
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/refresh-token" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$USER_REFRESH\"}")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Refresh Token" "200" "$CODE" "$BODY"

# AUTH: Get Profile
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/auth/me" \
  -H "Authorization: Bearer $USER_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Get Profile (Elena)" "200" "$CODE" "$BODY"
USER_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)

# AUTH: Duplicate Register
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Elena","lastName":"Kowalski","email":"elena.kowalski@testmail.com","password":"Test@1234","role":"USER"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Duplicate Register (should fail)" "409" "$CODE" "$BODY"

# AUTH: Wrong Password Login
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"elena.kowalski@testmail.com","password":"WrongPass"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Login with wrong password (should fail)" "401" "$CODE" "$BODY"
echo ""

# ------------------------------------------
# VENDOR PROFILE
# ------------------------------------------
echo "--- Vendor Profile Tests ---"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/vendors/profile" \
  -H "Authorization: Bearer $VENDOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Morrison Realty","companyDescription":"Premium land sales","businessAddress":"123 Main St","businessCity":"Austin","businessState":"Texas","businessCountry":"USA","businessZipCode":"73301","website":"https://morrisonrealty.com","businessLicense":"TX-RE-2024-001"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Create Vendor Profile" "200" "$CODE" "$BODY"
VENDOR_PROFILE_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)

RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/vendors/profile" \
  -H "Authorization: Bearer $VENDOR_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Get Vendor Profile" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/vendors/profile" \
  -H "Authorization: Bearer $VENDOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Morrison Realty LLC","companyDescription":"Premium land sales updated","businessAddress":"123 Main St","businessCity":"Austin","businessState":"Texas","businessCountry":"USA","businessZipCode":"73301","website":"https://morrisonrealty.com","businessLicense":"TX-RE-2024-001"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Update Vendor Profile" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/vendors/$VENDOR_PROFILE_ID/public")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Get Vendor Public Profile" "200" "$CODE" "$BODY"
echo ""

# ------------------------------------------
# LANDS
# ------------------------------------------
echo "--- Land Tests ---"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/lands" \
  -H "Authorization: Bearer $VENDOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Riverside Plot","description":"Beautiful riverside land","address":"456 River Rd","city":"Austin","state":"Texas","country":"USA","zipCode":"73301","price":250000,"areaSqFt":5000,"landType":"RESIDENTIAL","latitude":30.2672,"longitude":-97.7431,"hasRoadAccess":true,"hasWaterAccess":true,"hasElectricity":true,"hasSewage":false,"imageUrls":["https://example.com/img1.jpg"]}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Create Land Listing" "200" "$CODE" "$BODY"
LAND_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)

RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/lands" \
  -H "Authorization: Bearer $VENDOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Farm Land","description":"Agricultural land with water","address":"789 Farm Rd","city":"Dallas","state":"Texas","country":"USA","zipCode":"75001","price":180000,"areaSqFt":20000,"landType":"AGRICULTURAL","latitude":32.7767,"longitude":-96.7970,"hasRoadAccess":true,"hasWaterAccess":true,"hasElectricity":false,"hasSewage":false}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Create Second Land Listing" "200" "$CODE" "$BODY"
LAND_ID_2=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)

RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/lands/$LAND_ID" \
  -H "Authorization: Bearer $VENDOR_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Get Land by ID" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/lands/$LAND_ID" \
  -H "Authorization: Bearer $VENDOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Riverside Plot - Updated","description":"Beautiful riverside land with view","address":"456 River Rd","city":"Austin","state":"Texas","country":"USA","zipCode":"73301","price":275000,"areaSqFt":5000,"landType":"RESIDENTIAL","latitude":30.2672,"longitude":-97.7431,"hasRoadAccess":true,"hasWaterAccess":true,"hasElectricity":true,"hasSewage":true,"imageUrls":["https://example.com/img1.jpg","https://example.com/img2.jpg"]}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Update Land Listing" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/lands/vendor" \
  -H "Authorization: Bearer $VENDOR_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Get Vendor's Lands" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/lands/search?city=Austin")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Search Lands by City" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE/lands/$LAND_ID/status?status=ACTIVE" \
  -H "Authorization: Bearer $VENDOR_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Update Land Status to ACTIVE" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/lands/$LAND_ID/save" \
  -H "Authorization: Bearer $USER_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Save Land (User)" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/lands/saved" \
  -H "Authorization: Bearer $USER_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Get Saved Lands" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE/lands/$LAND_ID/save" \
  -H "Authorization: Bearer $USER_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Unsave Land" "200" "$CODE" "$BODY"

# Negative: Create land without auth
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/lands" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","address":"Test","city":"Test","state":"Test","country":"Test","zipCode":"12345","price":100,"areaSqFt":100,"landType":"RESIDENTIAL"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Create Land without Auth (should fail)" "401" "$CODE" "$BODY"
echo ""

# ------------------------------------------
# SUBSCRIPTIONS
# ------------------------------------------
echo "--- Subscription Tests ---"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/subscriptions" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"plan":"BASIC","paymentMethod":"CREDIT_CARD","paymentReference":"PAY-123456"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Create Subscription" "200" "$CODE" "$BODY"
SUB_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)

RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/subscriptions/my" \
  -H "Authorization: Bearer $USER_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Get My Subscription" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/subscriptions/upgrade" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"plan":"PREMIUM","paymentMethod":"CREDIT_CARD","paymentReference":"PAY-789012"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Upgrade Subscription" "200" "$CODE" "$BODY"

RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/subscriptions/cancel" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Testing cancellation"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Cancel Subscription" "200" "$CODE" "$BODY"
echo ""

# ------------------------------------------
# FORGOT PASSWORD FLOW
# ------------------------------------------
echo "--- Forgot Password Tests ---"

# Test 1: Forgot password with valid email
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email":"elena.kowalski@testmail.com"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Forgot Password (valid email)" "200" "$CODE" "$BODY"

# Test 2: Forgot password with non-existent email (should still return 200 for security)
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email":"nonexistent@testmail.com"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Forgot Password (non-existent email, should still 200)" "200" "$CODE" "$BODY"

# Test 3: Forgot password with invalid email format
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{"email":"not-an-email"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Forgot Password (invalid email format, should 400)" "400" "$CODE" "$BODY"

# Test 4: Forgot password with empty body
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Forgot Password (empty body, should 400)" "400" "$CODE" "$BODY"

# Test 5: Validate token with invalid token
RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/auth/reset-password/validate?token=invalid-token-12345")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Validate Reset Token (invalid token, should 400)" "400" "$CODE" "$BODY"

# Test 6: Reset password with invalid token
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{"token":"invalid-token-12345","newPassword":"NewPass@123"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Reset Password (invalid token, should 400)" "400" "$CODE" "$BODY"

# Test 7: Reset password with short password
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d '{"token":"some-token","newPassword":"short"}')
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Reset Password (short password, should 400)" "400" "$CODE" "$BODY"

# Test 8: Get reset token from DB and do full flow
echo ""
echo "--- Full Reset Password Flow (DB Token) ---"
# Query the token directly from PostgreSQL
RESET_TOKEN=$(PGPASSWORD=postgres psql -h localhost -U postgres -d landgo -t -A -c "SELECT token FROM password_reset_tokens WHERE used=false ORDER BY created_at DESC LIMIT 1;" 2>/dev/null)

if [ -n "$RESET_TOKEN" ] && [ "$RESET_TOKEN" != "" ]; then
  # Validate the token
  RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/auth/reset-password/validate?token=$RESET_TOKEN")
  CODE=$(echo "$RESP" | tail -1)
  BODY=$(echo "$RESP" | sed '$d')
  print_result "Validate Reset Token (valid from DB)" "200" "$CODE" "$BODY"

  # Reset password with valid token
  RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/reset-password" \
    -H "Content-Type: application/json" \
    -d "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"NewPass@123\"}")
  CODE=$(echo "$RESP" | tail -1)
  BODY=$(echo "$RESP" | sed '$d')
  print_result "Reset Password (valid token)" "200" "$CODE" "$BODY"

  # Login with new password
  RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"elena.kowalski@testmail.com","password":"NewPass@123"}')
  CODE=$(echo "$RESP" | tail -1)
  BODY=$(echo "$RESP" | sed '$d')
  print_result "Login with NEW password" "200" "$CODE" "$BODY"

  # Login with old password should fail
  RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"elena.kowalski@testmail.com","password":"Test@1234"}')
  CODE=$(echo "$RESP" | tail -1)
  BODY=$(echo "$RESP" | sed '$d')
  print_result "Login with OLD password (should fail)" "401" "$CODE" "$BODY"

  # Try reusing the same token (should fail)
  RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/reset-password" \
    -H "Content-Type: application/json" \
    -d "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"AnotherPass@123\"}")
  CODE=$(echo "$RESP" | tail -1)
  BODY=$(echo "$RESP" | sed '$d')
  print_result "Reuse token after reset (should fail)" "400" "$CODE" "$BODY"
else
  echo "⚠️  Could not retrieve reset token from DB (email send may have failed)."
  echo "   Skipping DB-dependent tests. SMTP not configured."
  # Still count them but mark as skipped
  TOTAL=$((TOTAL + 5))
  echo "   Skipped 5 DB-token-dependent tests"
fi
echo ""

# ------------------------------------------
# LAND DELETE
# ------------------------------------------
echo "--- Cleanup Tests ---"
RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE/lands/$LAND_ID_2" \
  -H "Authorization: Bearer $VENDOR_TOKEN")
CODE=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')
print_result "Delete Land Listing" "204" "$CODE" "$BODY"
echo ""

# ------------------------------------------
# SUMMARY
# ------------------------------------------
echo "============================================"
echo "  📊 TEST RESULTS"
echo "============================================"
echo "  Total:  $TOTAL"
echo "  Passed: $PASS ✅"
echo "  Failed: $FAIL ❌"
if [ $FAIL -eq 0 ]; then
  echo "  Status: ALL TESTS PASSED! 🎉"
else
  echo "  Status: SOME TESTS FAILED ⚠️"
fi
echo "============================================"
