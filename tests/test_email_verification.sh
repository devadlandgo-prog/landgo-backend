#!/bin/bash
# ==========================================
# 🧪 EMAIL VERIFICATION TEST SUITE
# ==========================================

BASE="http://localhost:8080/api/v1"
PASS=0
FAIL=0

check() {
  local test_name="$1"
  local condition="$2"
  local detail="$3"
  if [ "$condition" = "true" ]; then
    echo "✅ PASS: $test_name"
    [ -n "$detail" ] && echo "   $detail"
    PASS=$((PASS+1))
  else
    echo "❌ FAIL: $test_name"
    [ -n "$detail" ] && echo "   $detail"
    FAIL=$((FAIL+1))
  fi
  echo ""
}

echo "=========================================="
echo "🧪 EMAIL VERIFICATION TEST SUITE"
echo "=========================================="
echo ""

# ==================================================
# TEST 1: Register seller — response includes emailVerified=false
# ==================================================
echo "--- TEST 1: Register seller, emailVerified=false ---"
R1=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
  -d '{"userType":"SELLER","fullName":"Verify Seller","email":"verify_seller@test.com","phone":"5551110001","password":"password123"}')
T1_VERIFIED=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('emailVerified',True))" 2>/dev/null)
T1_MSG=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
SELLER_TOKEN=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('accessToken',''))" 2>/dev/null)
[ "$T1_VERIFIED" = "False" ] && T1_OK="true" || T1_OK="false"
check "Register seller — emailVerified=false" "$T1_OK" "emailVerified=$T1_VERIFIED | msg=$T1_MSG"

# ==================================================
# TEST 2: Register agent — response includes emailVerified=false
# ==================================================
echo "--- TEST 2: Register agent, emailVerified=false ---"
R2=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
  -d '{"userType":"AGENT","fullName":"Verify Agent","email":"verify_agent@test.com","phone":"5551110002","password":"password123","agencyName":"Test Realty","recoLicenseNumber":"RECO-V-001","agentAuthorizationAccepted":true}')
T2_VERIFIED=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('emailVerified',True))" 2>/dev/null)
AGENT_TOKEN=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('accessToken',''))" 2>/dev/null)
[ "$T2_VERIFIED" = "False" ] && T2_OK="true" || T2_OK="false"
check "Register agent — emailVerified=false" "$T2_OK" "emailVerified=$T2_VERIFIED"

# ==================================================
# TEST 3: Verify email — wrong code (bad request)
# ==================================================
echo "--- TEST 3: Wrong verification code ---"
R3=$(curl -s -X POST "$BASE/auth/verify-email" -H "Content-Type: application/json" \
  -d '{"email":"verify_seller@test.com","code":"000000"}')
T3_SUCCESS=$(echo "$R3" | python3 -c "import sys,json; print(json.load(sys.stdin).get('success',True))" 2>/dev/null)
T3_MSG=$(echo "$R3" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
[ "$T3_SUCCESS" = "False" ] && T3_OK="true" || T3_OK="false"
check "Wrong verification code rejected" "$T3_OK" "Message=$T3_MSG"

# ==================================================
# TEST 4: Verify email — invalid format (not 6 digits)
# ==================================================
echo "--- TEST 4: Invalid code format ---"
R4=$(curl -s -X POST "$BASE/auth/verify-email" -H "Content-Type: application/json" \
  -d '{"email":"verify_seller@test.com","code":"ABC"}')
T4_SUCCESS=$(echo "$R4" | python3 -c "import sys,json; print(json.load(sys.stdin).get('success',True))" 2>/dev/null)
T4_MSG=$(echo "$R4" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
[ "$T4_SUCCESS" = "False" ] && T4_OK="true" || T4_OK="false"
check "Invalid code format rejected" "$T4_OK" "Message=$T4_MSG"

# ==================================================
# TEST 5: Verify email — non-existent email
# ==================================================
echo "--- TEST 5: Non-existent email ---"
R5=$(curl -s -X POST "$BASE/auth/verify-email" -H "Content-Type: application/json" \
  -d '{"email":"nobody@test.com","code":"123456"}')
T5_SUCCESS=$(echo "$R5" | python3 -c "import sys,json; print(json.load(sys.stdin).get('success',True))" 2>/dev/null)
[ "$T5_SUCCESS" = "False" ] && T5_OK="true" || T5_OK="false"
check "Non-existent email rejected" "$T5_OK"

# ==================================================
# TEST 6: Resend verification code
# ==================================================
echo "--- TEST 6: Resend verification code ---"
R6=$(curl -s -X POST "$BASE/auth/resend-verification" -H "Content-Type: application/json" \
  -d '{"email":"verify_seller@test.com"}')
T6_SUCCESS=$(echo "$R6" | python3 -c "import sys,json; print(json.load(sys.stdin).get('success',True))" 2>/dev/null)
T6_MSG=$(echo "$R6" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
[ "$T6_SUCCESS" = "True" ] && T6_OK="true" || T6_OK="false"
check "Resend verification code" "$T6_OK" "Message=$T6_MSG"

# ==================================================
# TEST 7: Verify email with REAL code from DB
# ==================================================
echo "--- TEST 7: Verify email with real code from DB ---"
# Get the verification code directly from the database
REAL_CODE=$(docker exec landgo-postgres psql -U postgres -d landgo -t -A -c \
  "SELECT code FROM email_verification_tokens WHERE user_id = (SELECT id FROM users WHERE email = 'verify_seller@test.com') AND used = false ORDER BY created_at DESC LIMIT 1;" 2>/dev/null)
REAL_CODE=$(echo "$REAL_CODE" | tr -d '[:space:]')

if [ -z "$REAL_CODE" ]; then
  echo "   ⚠️  Could not retrieve code from DB, skipping real verification test"
  check "Verify email with real code" "false" "Code not found in DB"
else
  echo "   Code from DB: $REAL_CODE"
  R7=$(curl -s -X POST "$BASE/auth/verify-email" -H "Content-Type: application/json" \
    -d "{\"email\":\"verify_seller@test.com\",\"code\":\"$REAL_CODE\"}")
  T7_SUCCESS=$(echo "$R7" | python3 -c "import sys,json; print(json.load(sys.stdin).get('success',True))" 2>/dev/null)
  T7_MSG=$(echo "$R7" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
  [ "$T7_SUCCESS" = "True" ] && T7_OK="true" || T7_OK="false"
  check "Verify email with real code" "$T7_OK" "Message=$T7_MSG"
fi

# ==================================================
# TEST 8: Confirm emailVerified=true via /me
# ==================================================
echo "--- TEST 8: Confirm emailVerified=true via /me ---"
R8=$(curl -s -X GET "$BASE/auth/me" -H "Authorization: Bearer $SELLER_TOKEN")
T8_VERIFIED=$(echo "$R8" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('emailVerified',False))" 2>/dev/null)
[ "$T8_VERIFIED" = "True" ] && T8_OK="true" || T8_OK="false"
check "emailVerified=true after verification" "$T8_OK" "emailVerified=$T8_VERIFIED"

# ==================================================
# TEST 9: Already verified — reject duplicate verify
# ==================================================
echo "--- TEST 9: Already verified — reject duplicate ---"
R9=$(curl -s -X POST "$BASE/auth/verify-email" -H "Content-Type: application/json" \
  -d '{"email":"verify_seller@test.com","code":"123456"}')
T9_SUCCESS=$(echo "$R9" | python3 -c "import sys,json; print(json.load(sys.stdin).get('success',True))" 2>/dev/null)
T9_MSG=$(echo "$R9" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
[ "$T9_SUCCESS" = "False" ] && T9_OK="true" || T9_OK="false"
check "Already verified email rejected" "$T9_OK" "Message=$T9_MSG"

# ==================================================
# TEST 10: Already verified — reject resend
# ==================================================
echo "--- TEST 10: Already verified — reject resend ---"
R10=$(curl -s -X POST "$BASE/auth/resend-verification" -H "Content-Type: application/json" \
  -d '{"email":"verify_seller@test.com"}')
T10_SUCCESS=$(echo "$R10" | python3 -c "import sys,json; print(json.load(sys.stdin).get('success',True))" 2>/dev/null)
T10_MSG=$(echo "$R10" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
[ "$T10_SUCCESS" = "False" ] && T10_OK="true" || T10_OK="false"
check "Resend for already-verified email rejected" "$T10_OK" "Message=$T10_MSG"

# ==================================================
# TEST 11: Verify agent email with real code from DB
# ==================================================
echo "--- TEST 11: Verify agent email with real code ---"
AGENT_CODE=$(docker exec landgo-postgres psql -U postgres -d landgo -t -A -c \
  "SELECT code FROM email_verification_tokens WHERE user_id = (SELECT id FROM users WHERE email = 'verify_agent@test.com') AND used = false ORDER BY created_at DESC LIMIT 1;" 2>/dev/null)
AGENT_CODE=$(echo "$AGENT_CODE" | tr -d '[:space:]')

if [ -z "$AGENT_CODE" ]; then
  check "Verify agent email with real code" "false" "Code not found in DB"
else
  echo "   Code from DB: $AGENT_CODE"
  R11=$(curl -s -X POST "$BASE/auth/verify-email" -H "Content-Type: application/json" \
    -d "{\"email\":\"verify_agent@test.com\",\"code\":\"$AGENT_CODE\"}")
  T11_SUCCESS=$(echo "$R11" | python3 -c "import sys,json; print(json.load(sys.stdin).get('success',True))" 2>/dev/null)
  T11_MSG=$(echo "$R11" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
  [ "$T11_SUCCESS" = "True" ] && T11_OK="true" || T11_OK="false"
  check "Verify agent email with real code" "$T11_OK" "Message=$T11_MSG"
fi

# ==================================================
# TEST 12: Agent emailVerified=true via /me
# ==================================================
echo "--- TEST 12: Agent emailVerified=true via /me ---"
R12=$(curl -s -X GET "$BASE/auth/me" -H "Authorization: Bearer $AGENT_TOKEN")
T12_VERIFIED=$(echo "$R12" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('emailVerified',False))" 2>/dev/null)
[ "$T12_VERIFIED" = "True" ] && T12_OK="true" || T12_OK="false"
check "Agent emailVerified=true after verification" "$T12_OK" "emailVerified=$T12_VERIFIED"

# ---- SUMMARY ----
echo "=========================================="
echo "📊 RESULTS: $PASS passed, $FAIL failed (out of $((PASS+FAIL)) tests)"
echo "=========================================="
