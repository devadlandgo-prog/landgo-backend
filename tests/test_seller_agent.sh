#!/bin/bash
# ==========================================
# 🧪 SELLER / AGENT REGISTRATION TEST SUITE
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
echo "🧪 SELLER / AGENT REGISTRATION TEST SUITE"
echo "=========================================="
echo ""

# ---- TEST 1: Register SELLER ----
echo "--- TEST 1: Register SELLER ---"
R1=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
  -d '{"userType":"SELLER","fullName":"John Seller","email":"seller@test.com","phone":"1234567890","password":"password123"}')
T1_ROLE=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('role',''))" 2>/dev/null)
T1_TYPE=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('userType',''))" 2>/dev/null)
T1_NAME=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('fullName',''))" 2>/dev/null)
SELLER_TOKEN=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('accessToken',''))" 2>/dev/null)
[ "$T1_ROLE" = "SELLER" ] && [ "$T1_TYPE" = "SELLER" ] && T1_OK="true" || T1_OK="false"
check "Register SELLER" "$T1_OK" "Role=$T1_ROLE | UserType=$T1_TYPE | Name=$T1_NAME"

# ---- TEST 2: Register AGENT ----
echo "--- TEST 2: Register AGENT ---"
R2=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
  -d '{"userType":"AGENT","fullName":"Jane Agent","email":"agent@test.com","phone":"9876543210","password":"password123","agencyName":"Premier Realty","recoLicenseNumber":"RECO-2026-001","agentAuthorizationAccepted":true}')
T2_ROLE=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('role',''))" 2>/dev/null)
T2_TYPE=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('userType',''))" 2>/dev/null)
T2_AGENCY=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('agencyName',''))" 2>/dev/null)
T2_RECO=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('user',{}).get('recoLicenseNumber',''))" 2>/dev/null)
AGENT_TOKEN=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('accessToken',''))" 2>/dev/null)
[ "$T2_ROLE" = "AGENT" ] && [ "$T2_TYPE" = "AGENT" ] && [ "$T2_AGENCY" = "Premier Realty" ] && T2_OK="true" || T2_OK="false"
check "Register AGENT" "$T2_OK" "Role=$T2_ROLE | UserType=$T2_TYPE | Agency=$T2_AGENCY | RECO=$T2_RECO"

# ---- TEST 3: Agent registers vendor profile ----
echo "--- TEST 3: Agent registers vendor profile ---"
R3=$(curl -s -X POST "$BASE/vendor/register" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{"companyName":"Premier Realty Inc","companyDescription":"Top agency","businessAddress":"100 Bay St","businessCity":"Toronto","businessState":"Ontario","businessZipCode":"M5H 2N2","businessCountry":"Canada"}')
T3_OK=$(echo "$R3" | python3 -c "import sys,json; d=json.load(sys.stdin); print('true' if d.get('success') and d.get('data',{}).get('companyName')=='Premier Realty Inc' else 'false')" 2>/dev/null)
T3_CO=$(echo "$R3" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('companyName',''))" 2>/dev/null)
check "Agent registers vendor profile" "$T3_OK" "Company=$T3_CO"

# ---- TEST 4: Agent creates land listing ----
echo "--- TEST 4: Agent creates land listing ---"
R4=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{"title":"Lakefront Land","description":"10 acres prime lakefront","address":"123 Lake Rd","city":"Muskoka","state":"Ontario","zipCode":"P1L 1A1","country":"Canada","price":450000,"areaSqFt":435600,"landType":"RESIDENTIAL","latitude":44.9999,"longitude":-79.2222}')
T4_OK=$(echo "$R4" | python3 -c "import sys,json; d=json.load(sys.stdin); print('true' if d.get('success') and d.get('data',{}).get('title')=='Lakefront Land' else 'false')" 2>/dev/null)
T4_TITLE=$(echo "$R4" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('title',''))" 2>/dev/null)
T4_PRICE=$(echo "$R4" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('price',''))" 2>/dev/null)
check "Agent creates land listing" "$T4_OK" "Title=$T4_TITLE | Price=$T4_PRICE"

# ---- TEST 5: Agent gets vendor profile ----
echo "--- TEST 5: Agent gets vendor profile ---"
R5=$(curl -s -X GET "$BASE/vendor/profile" -H "Authorization: Bearer $AGENT_TOKEN")
T5_OK=$(echo "$R5" | python3 -c "import sys,json; print('true' if json.load(sys.stdin).get('success') else 'false')" 2>/dev/null)
T5_CO=$(echo "$R5" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('companyName',''))" 2>/dev/null)
check "Agent gets vendor profile" "$T5_OK" "Company=$T5_CO"

# ---- TEST 6: Agent gets own land listings ----
echo "--- TEST 6: Agent gets own land listings ---"
R6=$(curl -s -X GET "$BASE/vendor/lands" -H "Authorization: Bearer $AGENT_TOKEN")
T6_TOTAL=$(echo "$R6" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('totalElements',0))" 2>/dev/null)
[ "$T6_TOTAL" = "1" ] && T6_OK="true" || T6_OK="false"
check "Agent gets own land listings" "$T6_OK" "Total lands=$T6_TOTAL"

# ---- TEST 7: Seller CANNOT access vendor profile (no vendor profile yet) ----
echo "--- TEST 7: Seller cannot access vendor profile (403) ---"
R7=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE/vendor/profile" -H "Authorization: Bearer $SELLER_TOKEN")
[ "$R7" = "403" ] && T7_OK="true" || T7_OK="false"
check "Seller gets 403 on vendor profile" "$T7_OK" "HTTP Status=$R7"

# ---- TEST 8: NEGATIVE - Agent without agencyName ----
echo "--- TEST 8: NEGATIVE - Agent missing agencyName ---"
R8=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
  -d '{"userType":"AGENT","fullName":"Bad Agent","email":"bad1@test.com","password":"password123","recoLicenseNumber":"RECO-001","agentAuthorizationAccepted":true}')
T8_OK=$(echo "$R8" | python3 -c "import sys,json; print('true' if not json.load(sys.stdin).get('success',True) else 'false')" 2>/dev/null)
T8_MSG=$(echo "$R8" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
check "Agent missing agencyName rejected" "$T8_OK" "Message=$T8_MSG"

# ---- TEST 9: NEGATIVE - Agent without RECO license ----
echo "--- TEST 9: NEGATIVE - Agent missing RECO license ---"
R9=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
  -d '{"userType":"AGENT","fullName":"Bad Agent","email":"bad2@test.com","password":"password123","agencyName":"My Agency","agentAuthorizationAccepted":true}')
T9_OK=$(echo "$R9" | python3 -c "import sys,json; print('true' if not json.load(sys.stdin).get('success',True) else 'false')" 2>/dev/null)
T9_MSG=$(echo "$R9" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
check "Agent missing RECO license rejected" "$T9_OK" "Message=$T9_MSG"

# ---- TEST 10: NEGATIVE - Agent without authorization accepted ----
echo "--- TEST 10: NEGATIVE - Agent authorization not accepted ---"
R10=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
  -d '{"userType":"AGENT","fullName":"Bad Agent","email":"bad3@test.com","password":"password123","agencyName":"My Agency","recoLicenseNumber":"RECO-001","agentAuthorizationAccepted":false}')
T10_OK=$(echo "$R10" | python3 -c "import sys,json; print('true' if not json.load(sys.stdin).get('success',True) else 'false')" 2>/dev/null)
T10_MSG=$(echo "$R10" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
check "Agent authorization not accepted rejected" "$T10_OK" "Message=$T10_MSG"

# ---- SUMMARY ----
echo "=========================================="
echo "📊 RESULTS: $PASS passed, $FAIL failed (out of $((PASS+FAIL)) tests)"
echo "=========================================="
