#!/bin/bash
set -e

BASE="http://localhost:8080/api/v1"
PASS=0
FAIL=0
TOTAL=0

check() {
  TOTAL=$((TOTAL + 1))
  local desc="$1" expected="$2" actual="$3"
  if echo "$actual" | grep -q "$expected"; then
    PASS=$((PASS + 1))
    echo "✅ #$TOTAL $desc"
  else
    FAIL=$((FAIL + 1))
    echo "❌ #$TOTAL $desc"
    echo "   Expected: $expected"
    echo "   Got: $(echo "$actual" | head -c 300)"
  fi
}

echo "========================================="
echo " PROFILE & EDIT PROFILE API TESTS"
echo "========================================="
echo ""

# --- 1. Register a seller ---
echo "--- Register seller ---"
REG=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" -d '{
  "userType": "SELLER",
  "fullName": "Alex Sterling",
  "email": "alex.profile2@test.com",
  "password": "Test@1234"
}')
TOKEN=$(echo "$REG" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])" 2>/dev/null)
check "Register seller" '"fullName":"Alex Sterling"' "$REG"

# --- 2. GET /auth/me returns new fields ---
echo ""
echo "--- GET /auth/me (initial profile) ---"
ME=$(curl -s -X GET "$BASE/auth/me" -H "Authorization: Bearer $TOKEN")
check "GET /me returns professionalBio field" '"professionalBio"' "$ME"
check "GET /me returns location field" '"location"' "$ME"
check "GET /me returns activeListingsCount" '"activeListingsCount"' "$ME"
check "GET /me returns totalViews" '"totalViews"' "$ME"
check "GET /me activeListingsCount is 0" '"activeListingsCount":0' "$ME"
check "GET /me totalViews is 0" '"totalViews":0' "$ME"

# --- 3. GET /user/profile returns same enriched data ---
echo ""
echo "--- GET /user/profile ---"
PROF=$(curl -s -X GET "$BASE/user/profile" -H "Authorization: Bearer $TOKEN")
check "GET /user/profile works" '"fullName":"Alex Sterling"' "$PROF"
check "GET /user/profile has stats" '"activeListingsCount":0' "$PROF"

# --- 4. Update profile - full name + phone ---
echo ""
echo "--- Update fullName & phone ---"
UPD1=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "fullName": "Alexander Sterling",
  "phone": "+1 (555) 0123-4567"
}')
check "Update fullName" '"fullName":"Alexander Sterling"' "$UPD1"
check "Update phone" '"phone":"+1 (555) 0123-4567"' "$UPD1"
check "Update profile success msg" '"Profile updated successfully"' "$UPD1"

# --- 5. Update profile - add professional bio (optional) ---
echo ""
echo "--- Add professional bio ---"
UPD2=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "professionalBio": "Specializing in commercial land acquisition and agricultural zoning AI analysis. 10+ years of experience in the Pacific Northwest market."
}')
check "Professional bio set" 'Specializing in commercial land' "$UPD2"
check "Previous fullName preserved" '"fullName":"Alexander Sterling"' "$UPD2"
check "Previous phone preserved" '"phone":"+1 (555) 0123-4567"' "$UPD2"

# --- 6. Update profile - add location ---
echo ""
echo "--- Add location ---"
UPD3=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "location": "California, USA"
}')
check "Location set" '"location":"California, USA"' "$UPD3"
check "Bio still preserved" 'Specializing in commercial land' "$UPD3"

# --- 7. Update profile - add profile image URL ---
echo ""
echo "--- Add profile image URL ---"
UPD4=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "profileImageUrl": "https://cdn.landgo.ai/profiles/alex-sterling.jpg"
}')
check "Profile image URL set" 'alex-sterling.jpg' "$UPD4"

# --- 8. Full update (all fields at once like Edit Profile screen) ---
echo ""
echo "--- Full edit profile screen save ---"
UPD_FULL=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "fullName": "Alexander Sterling",
  "phone": "+1 (555) 0123-4567",
  "professionalBio": "Specializing in commercial land acquisition and agricultural zoning AI analysis. 10+ years of experience in the Pacific Northwest market.",
  "profileImageUrl": "https://cdn.landgo.ai/profiles/alex-sterling-v2.jpg",
  "location": "California, USA"
}')
check "Full update - name" '"fullName":"Alexander Sterling"' "$UPD_FULL"
check "Full update - phone" '"phone":"+1 (555) 0123-4567"' "$UPD_FULL"
check "Full update - bio" 'Specializing in commercial land' "$UPD_FULL"
check "Full update - location" '"location":"California, USA"' "$UPD_FULL"
check "Full update - image" 'alex-sterling-v2.jpg' "$UPD_FULL"

# --- 9. Verify GET /auth/me reflects all updates ---
echo ""
echo "--- GET /auth/me (after all updates) ---"
ME2=$(curl -s -X GET "$BASE/auth/me" -H "Authorization: Bearer $TOKEN")
check "GET /me fullName updated" '"fullName":"Alexander Sterling"' "$ME2"
check "GET /me phone updated" '"phone":"+1 (555) 0123-4567"' "$ME2"
check "GET /me bio updated" 'Specializing in commercial land' "$ME2"
check "GET /me location updated" '"location":"California, USA"' "$ME2"
check "GET /me profileImageUrl updated" 'alex-sterling-v2.jpg' "$ME2"

# --- 10. Clear professional bio (optional - can be empty) ---
echo ""
echo "--- Clear professional bio (optional field) ---"
UPD5=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "professionalBio": ""
}')
check "Bio cleared to null" '"professionalBio":null' "$UPD5"

# --- 11. Set bio back ---
UPD6=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "professionalBio": "Updated bio for testing"
}')
check "Bio re-set" '"professionalBio":"Updated bio for testing"' "$UPD6"

# --- NEGATIVE TESTS ---
echo ""
echo "--- Negative Tests ---"

# 12. Update profile without auth => 401
UPD_NOAUTH=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE/user/profile" -H "Content-Type: application/json" -d '{
  "fullName": "Hacker"
}')
check "Update profile without auth returns 403" "403" "$UPD_NOAUTH"

# 13. GET profile without auth => 401
GET_NOAUTH=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE/user/profile")
check "GET profile without auth returns 403" "403" "$GET_NOAUTH"

# 14. Full name too short
UPD_SHORT=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
  "fullName": "A"
}')
check "FullName too short returns error" "must be between 2 and 100" "$UPD_SHORT"

# 15. Bio too long (>2000 chars)
LONG_BIO=$(python3 -c "print('x' * 2001)")
UPD_LONG=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{
  \"professionalBio\": \"$LONG_BIO\"
}")
check "Bio >2000 chars returns error" "must be at most 2000" "$UPD_LONG"

# --- 16. Register agent and test profile ---
echo ""
echo "--- Register agent and test profile ---"
REG_AGENT=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" -d '{
  "userType": "AGENT",
  "fullName": "Jane Agent",
  "email": "jane.profile2@test.com",
  "password": "Test@1234",
  "agencyName": "TopLand Realty",
  "recoLicenseNumber": "RECO-2026-001",
  "agentAuthorizationAccepted": true
}')
AGENT_TOKEN=$(echo "$REG_AGENT" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])" 2>/dev/null)
check "Register agent" '"fullName":"Jane Agent"' "$REG_AGENT"

# 17. Agent can update profile with bio
UPD_AGENT=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $AGENT_TOKEN" -H "Content-Type: application/json" -d '{
  "professionalBio": "Licensed real estate agent with 15 years experience.",
  "location": "Toronto, Canada",
  "phone": "+1 (416) 555-0199"
}')
check "Agent bio set" 'Licensed real estate agent' "$UPD_AGENT"
check "Agent location set" '"location":"Toronto, Canada"' "$UPD_AGENT"

# 18. Agent GET /user/profile shows all fields
ME_AGENT=$(curl -s -X GET "$BASE/user/profile" -H "Authorization: Bearer $AGENT_TOKEN")
check "Agent profile has bio" 'Licensed real estate agent' "$ME_AGENT"
check "Agent profile has agencyName" '"agencyName":"TopLand Realty"' "$ME_AGENT"
check "Agent profile has agent:true" '"agent":true' "$ME_AGENT"

# 19. Update without changing bio (should preserve it)
UPD_NOBIO=$(curl -s -X PUT "$BASE/user/profile" -H "Authorization: Bearer $AGENT_TOKEN" -H "Content-Type: application/json" -d '{
  "fullName": "Jane Agent Updated"
}')
check "Update name preserves bio" 'Licensed real estate agent' "$UPD_NOBIO"
check "Name was updated" '"fullName":"Jane Agent Updated"' "$UPD_NOBIO"

echo ""
echo "========================================="
echo " RESULTS: $PASS/$TOTAL passed, $FAIL failed"
echo "========================================="
