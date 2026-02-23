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
    echo "   Got: $(echo "$actual" | head -c 400)"
  fi
}

check_status() {
  TOTAL=$((TOTAL + 1))
  local desc="$1" expected="$2" actual="$3"
  if [ "$actual" = "$expected" ]; then
    PASS=$((PASS + 1))
    echo "✅ #$TOTAL $desc"
  else
    FAIL=$((FAIL + 1))
    echo "❌ #$TOTAL $desc (expected $expected, got $actual)"
  fi
}

echo "============================================="
echo "   SAVED SEARCHES FEATURE TESTS"
echo "============================================="
echo ""

# --- Setup: Register a seller ---
echo "--- Setup: Register user ---"
REG=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" -d '{
  "userType": "SELLER",
  "fullName": "SavedSearch Tester",
  "email": "savedsearch@test.com",
  "password": "Test@1234"
}')
TOKEN=$(echo "$REG" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])" 2>/dev/null)
check "Register user" '"accessToken"' "$REG"

AUTH="Authorization: Bearer $TOKEN"

# =============================================
# 1. CREATE SAVED SEARCH
# =============================================
echo ""
echo "--- Create Saved Search ---"

CREATE1=$(curl -s -X POST "$BASE/user/saved-searches" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "name": "Toronto Under 500K",
  "city": "Toronto",
  "maxPrice": 500000
}')
echo "$CREATE1" | python3 -m json.tool 2>/dev/null | head -20
SEARCH1_ID=$(echo "$CREATE1" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])" 2>/dev/null)
check "Create saved search" '"name":"Toronto Under 500K"' "$CREATE1"
check "City filter saved" '"city":"Toronto"' "$CREATE1"
check "MaxPrice filter saved" '"maxPrice":500000' "$CREATE1"
check "Notifications enabled by default" '"notificationsEnabled":true' "$CREATE1"
check "Match count returned" '"matchCount"' "$CREATE1"
check "Success message" '"Saved search created successfully"' "$CREATE1"

# =============================================
# 2. CREATE SECOND SAVED SEARCH (more criteria)
# =============================================
echo ""
echo "--- Create second saved search (multiple criteria) ---"

CREATE2=$(curl -s -X POST "$BASE/user/saved-searches" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "name": "Raw Land Ottawa",
  "keyword": "Ottawa",
  "city": "Ottawa",
  "projectStage": "RAW_LAND",
  "minPrice": 100000,
  "maxPrice": 1000000,
  "minLotSize": 5,
  "maxLotSize": 100,
  "notificationsEnabled": false
}')
SEARCH2_ID=$(echo "$CREATE2" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])" 2>/dev/null)
check "Create second search" '"name":"Raw Land Ottawa"' "$CREATE2"
check "Project stage saved" '"projectStage":"RAW_LAND"' "$CREATE2"
check "Notifications disabled" '"notificationsEnabled":false' "$CREATE2"
check "Min price saved" '"minPrice":100000' "$CREATE2"
check "Lot size range saved" '"minLotSize":5' "$CREATE2"

# =============================================
# 3. LIST SAVED SEARCHES
# =============================================
echo ""
echo "--- List my saved searches ---"

LIST=$(curl -s -X GET "$BASE/user/saved-searches" -H "$AUTH")
check "List returns content" '"content"' "$LIST"
check "Total 2 saved searches" '"totalElements":2' "$LIST"
check "Most recent first" '"Raw Land Ottawa"' "$LIST"

# =============================================
# 4. GET SAVED SEARCH BY ID
# =============================================
echo ""
echo "--- Get saved search by ID ---"

GET1=$(curl -s -X GET "$BASE/user/saved-searches/$SEARCH1_ID" -H "$AUTH")
check "Get by ID returns correct search" '"name":"Toronto Under 500K"' "$GET1"
check "Get by ID has city" '"city":"Toronto"' "$GET1"

# =============================================
# 5. UPDATE SAVED SEARCH
# =============================================
echo ""
echo "--- Update saved search ---"

UPD=$(curl -s -X PUT "$BASE/user/saved-searches/$SEARCH1_ID" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "name": "Toronto Under 750K",
  "city": "Toronto",
  "maxPrice": 750000,
  "projectStage": "SITE_PLAN_APPROVAL"
}')
check "Updated name" '"name":"Toronto Under 750K"' "$UPD"
check "Updated max price" '"maxPrice":750000' "$UPD"
check "Added project stage" '"projectStage":"SITE_PLAN_APPROVAL"' "$UPD"
check "Update success msg" '"Saved search updated successfully"' "$UPD"

# =============================================
# 6. TOGGLE NOTIFICATIONS
# =============================================
echo ""
echo "--- Toggle notifications ---"

TOGGLE=$(curl -s -X PATCH "$BASE/user/saved-searches/$SEARCH2_ID/notifications" -H "$AUTH")
check "Toggled to enabled" '"notificationsEnabled":true' "$TOGGLE"
check "Toggle msg" '"Notifications enabled"' "$TOGGLE"

TOGGLE2=$(curl -s -X PATCH "$BASE/user/saved-searches/$SEARCH2_ID/notifications" -H "$AUTH")
check "Toggled back to disabled" '"notificationsEnabled":false' "$TOGGLE2"
check "Toggle msg disabled" '"Notifications disabled"' "$TOGGLE2"

# =============================================
# 7. EXECUTE SAVED SEARCH (get results)
# =============================================
echo ""
echo "--- Execute saved search (get results) ---"

RESULTS=$(curl -s -X GET "$BASE/user/saved-searches/$SEARCH1_ID/results" -H "$AUTH")
check "Execute returns paginated results" '"content"' "$RESULTS"
check "Execute returns totalElements" '"totalElements"' "$RESULTS"

# =============================================
# 8. NEGATIVE TESTS
# =============================================
echo ""
echo "--- Negative Tests ---"

# No auth
STATUS_NOAUTH=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE/user/saved-searches")
check_status "List without auth returns 403" "403" "$STATUS_NOAUTH"

# No criteria
NOCRIT=$(curl -s -X POST "$BASE/user/saved-searches" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "name": "Empty Search"
}')
check "No criteria returns error" "At least one search criterion" "$NOCRIT"

# Missing name
NONAME=$(curl -s -X POST "$BASE/user/saved-searches" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "city": "Toronto"
}')
check "Missing name returns error" "name" "$NONAME"

# Duplicate name
DUP=$(curl -s -X POST "$BASE/user/saved-searches" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "name": "Toronto Under 750K",
  "city": "Toronto"
}')
check "Duplicate name returns error" "already exists" "$DUP"

# Min > Max price
BADPRICE=$(curl -s -X POST "$BASE/user/saved-searches" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "name": "Bad Price Range",
  "minPrice": 1000000,
  "maxPrice": 500000
}')
check "MinPrice > MaxPrice returns error" "Minimum price cannot be greater" "$BADPRICE"

# Min > Max lot size
BADLOT=$(curl -s -X POST "$BASE/user/saved-searches" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "name": "Bad Lot Range",
  "minLotSize": 100,
  "maxLotSize": 10
}')
check "MinLotSize > MaxLotSize returns error" "Minimum lot size cannot be greater" "$BADLOT"

# Get non-existent search
NOTFOUND=$(curl -s -X GET "$BASE/user/saved-searches/00000000-0000-0000-0000-000000000000" -H "$AUTH")
check "Non-existent search returns 404" "not found" "$NOTFOUND"

# =============================================
# 9. CROSS-USER ISOLATION
# =============================================
echo ""
echo "--- Cross-user isolation ---"

REG2=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" -d '{
  "userType": "SELLER",
  "fullName": "Other User",
  "email": "otheruser.ss@test.com",
  "password": "Test@1234"
}')
TOKEN2=$(echo "$REG2" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])" 2>/dev/null)

# Other user tries to get first user's saved search
CROSS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE/user/saved-searches/$SEARCH1_ID" -H "Authorization: Bearer $TOKEN2")
check_status "Other user can't access my search (404)" "404" "$CROSS"

# Other user's list should be empty
LIST2=$(curl -s -X GET "$BASE/user/saved-searches" -H "Authorization: Bearer $TOKEN2")
check "Other user has 0 saved searches" '"totalElements":0' "$LIST2"

# =============================================
# 10. DELETE SAVED SEARCH
# =============================================
echo ""
echo "--- Delete saved search ---"

DEL=$(curl -s -X DELETE "$BASE/user/saved-searches/$SEARCH2_ID" -H "$AUTH")
check "Delete success" '"Saved search deleted successfully"' "$DEL"

# Verify deleted
LIST3=$(curl -s -X GET "$BASE/user/saved-searches" -H "$AUTH")
check "After delete only 1 remains" '"totalElements":1' "$LIST3"

# Can't get deleted search
GETDEL=$(curl -s -X GET "$BASE/user/saved-searches/$SEARCH2_ID" -H "$AUTH")
check "Deleted search returns not found" "not found" "$GETDEL"

# =============================================
# 11. CREATE SEARCH WITH ONLY KEYWORD
# =============================================
echo ""
echo "--- Keyword-only search ---"

CREATE3=$(curl -s -X POST "$BASE/user/saved-searches" \
  -H "$AUTH" -H "Content-Type: application/json" -d '{
  "name": "Waterfront Properties",
  "keyword": "waterfront"
}')
check "Keyword-only search created" '"name":"Waterfront Properties"' "$CREATE3"
check "Keyword saved" '"keyword":"waterfront"' "$CREATE3"

echo ""
echo "============================================="
echo "   RESULTS: $PASS/$TOTAL passed, $FAIL failed"
echo "============================================="
