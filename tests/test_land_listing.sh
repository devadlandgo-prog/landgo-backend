#!/bin/bash
# ==========================================
# 🧪 LAND LISTING (CONVEX SCHEMA) TEST SUITE
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
echo "🧪 LAND LISTING (CONVEX SCHEMA) TEST SUITE"
echo "=========================================="
echo ""

# ---- SETUP: Register Agent + Vendor Profile ----
echo "--- SETUP: Register Agent ---"
R_AGENT=$(curl -s -X POST "$BASE/auth/register" -H "Content-Type: application/json" \
  -d '{"userType":"AGENT","fullName":"Test Agent","email":"land_agent@test.com","phone":"5551234567","password":"password123","agencyName":"Test Realty","recoLicenseNumber":"RECO-TEST-001","agentAuthorizationAccepted":true}')
AGENT_TOKEN=$(echo "$R_AGENT" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('accessToken',''))" 2>/dev/null)
echo "   Agent token: ${AGENT_TOKEN:0:30}..."

echo ""
echo "--- SETUP: Create Vendor Profile ---"
curl -s -X POST "$BASE/vendor/register" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{"companyName":"Test Realty Inc","companyDescription":"Test agency","businessAddress":"1 Test St","businessCity":"Toronto","businessState":"Ontario","businessZipCode":"M5H 1A1","businessCountry":"Canada"}' > /dev/null
echo "   Vendor profile created"
echo ""

# ==================================================
# TEST 1: Create RAW_LAND listing (minimal fields)
# ==================================================
echo "--- TEST 1: Create RAW_LAND listing ---"
R1=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{
    "projectStage": "RAW_LAND",
    "projectDetails": {
      "address": "123 Rural Road",
      "city": "Muskoka",
      "postalCode": "P1L 1A1",
      "lotSize": 43560,
      "lotUnit": "sqft",
      "frontage": "150 ft",
      "depth": "290 ft",
      "currentZoningCodes": "R1",
      "pinNumber": "PIN-12345",
      "coordinates": { "lat": 44.9999, "lng": -79.2222 }
    },
    "pricing": {
      "askingPrice": 250000,
      "currency": "CAD",
      "description": "Negotiable for cash buyers"
    },
    "photos": [
      {"name": "front.jpg", "type": "image/jpeg", "url": "https://cdn.example.com/front.jpg"},
      {"name": "aerial.jpg", "type": "image/jpeg", "url": "https://cdn.example.com/aerial.jpg"}
    ],
    "documents": [
      {"name": "survey.pdf", "type": "application/pdf", "url": "https://cdn.example.com/survey.pdf"}
    ],
    "ownershipVerification": "https://cdn.example.com/deed.pdf"
  }')
T1_STAGE=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('projectStage',''))" 2>/dev/null)
T1_CITY=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('city',''))" 2>/dev/null)
T1_PRICE=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('askingPrice',''))" 2>/dev/null)
T1_PHOTOS=$(echo "$R1" | python3 -c "import sys,json; print(len(json.load(sys.stdin).get('data',{}).get('photos',[])))" 2>/dev/null)
T1_DOCS=$(echo "$R1" | python3 -c "import sys,json; print(len(json.load(sys.stdin).get('data',{}).get('documents',[])))" 2>/dev/null)
LAND1_ID=$(echo "$R1" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('id',''))" 2>/dev/null)
[ "$T1_STAGE" = "RAW_LAND" ] && [ "$T1_CITY" = "Muskoka" ] && T1_OK="true" || T1_OK="false"
check "Create RAW_LAND listing" "$T1_OK" "Stage=$T1_STAGE | City=$T1_CITY | Price=$T1_PRICE | Photos=$T1_PHOTOS | Docs=$T1_DOCS"

# ==================================================
# TEST 2: Create SITE_PLAN_APPROVAL listing
# ==================================================
echo "--- TEST 2: Create SITE_PLAN_APPROVAL listing ---"
R2=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{
    "projectStage": "SITE_PLAN_APPROVAL",
    "projectDetails": {
      "address": "456 Urban Ave",
      "city": "Toronto",
      "postalCode": "M5V 3L9",
      "lotSize": 5000,
      "lotUnit": "sqft",
      "currentZoningCodes": "C2",
      "officialPlanDesignation": "Mixed Use"
    },
    "projectSpecification": {
      "buildingType": "Mixed-Use Condo",
      "proposedUse": "Residential + Commercial",
      "sitePlanStatus": "Approved",
      "services": {
        "gas": true,
        "hydro": true,
        "municipalSewer": true,
        "municipalWater": true,
        "septic": false,
        "well": false,
        "none": false
      }
    },
    "pricing": {
      "askingPrice": 1200000,
      "currency": "CAD",
      "description": "Prime downtown location"
    },
    "photos": [],
    "documents": []
  }')
T2_STAGE=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('projectStage',''))" 2>/dev/null)
T2_SPEC=$(echo "$R2" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}).get('projectSpecification',{}); print(d.get('sitePlanStatus',''))" 2>/dev/null)
T2_SVC=$(echo "$R2" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}).get('projectSpecification',{}).get('services',{}); print(d.get('gas',False))" 2>/dev/null)
LAND2_ID=$(echo "$R2" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('id',''))" 2>/dev/null)
[ "$T2_STAGE" = "SITE_PLAN_APPROVAL" ] && [ "$T2_SPEC" = "Approved" ] && T2_OK="true" || T2_OK="false"
check "Create SITE_PLAN_APPROVAL listing" "$T2_OK" "Stage=$T2_STAGE | SitePlanStatus=$T2_SPEC | Gas=$T2_SVC"

# ==================================================
# TEST 3: Create DRAFT_PLAN_APPROVAL listing
# ==================================================
echo "--- TEST 3: Create DRAFT_PLAN_APPROVAL listing ---"
R3=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{
    "projectStage": "DRAFT_PLAN_APPROVAL",
    "projectDetails": {
      "address": "789 Subdivision Dr",
      "city": "Vaughan",
      "postalCode": "L4K 5N3",
      "lotSize": 100000,
      "lotUnit": "sqft"
    },
    "projectSpecification": {
      "subdivisionType": "Residential Plan of Subdivision",
      "lotBlockType": "Single Detached",
      "draftPlanStatus": "Conditionally Approved",
      "services": {
        "gas": true,
        "hydro": true,
        "municipalSewer": true,
        "municipalWater": true
      }
    },
    "pricing": {
      "askingPrice": 5000000,
      "currency": "CAD",
      "description": "Full subdivision, 40 lots"
    },
    "photos": [],
    "documents": [
      {"name": "draft_plan.pdf", "type": "application/pdf", "url": "https://cdn.example.com/draft_plan.pdf"}
    ]
  }')
T3_STAGE=$(echo "$R3" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('projectStage',''))" 2>/dev/null)
T3_SPEC=$(echo "$R3" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}).get('projectSpecification',{}); print(d.get('draftPlanStatus',''))" 2>/dev/null)
[ "$T3_STAGE" = "DRAFT_PLAN_APPROVAL" ] && [ "$T3_SPEC" = "Conditionally Approved" ] && T3_OK="true" || T3_OK="false"
check "Create DRAFT_PLAN_APPROVAL listing" "$T3_OK" "Stage=$T3_STAGE | DraftPlanStatus=$T3_SPEC"

# ==================================================
# TEST 4: Create UNDER_CITY_SUBMISSION listing
# ==================================================
echo "--- TEST 4: Create UNDER_CITY_SUBMISSION listing ---"
R4=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{
    "projectStage": "UNDER_CITY_SUBMISSION",
    "projectDetails": {
      "address": "1010 Development Blvd",
      "city": "Mississauga",
      "postalCode": "L5B 1M2",
      "lotSize": 20000,
      "lotUnit": "sqft"
    },
    "projectSpecification": {
      "proposedDevelopmentType": {
        "rezoning": true,
        "sitePlan": true,
        "subdivision": false
      },
      "submissionStatus": "Under Review",
      "buildingType": "Mid-Rise Residential",
      "proposedUse": "Residential"
    },
    "pricing": {
      "askingPrice": 3500000,
      "currency": "CAD",
      "description": "Zoning change expected Q3"
    },
    "photos": [],
    "documents": []
  }')
T4_STAGE=$(echo "$R4" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('projectStage',''))" 2>/dev/null)
T4_SPEC=$(echo "$R4" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}).get('projectSpecification',{}); print(d.get('submissionStatus',''))" 2>/dev/null)
T4_DEV=$(echo "$R4" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}).get('projectSpecification',{}).get('proposedDevelopmentType',{}); print(d.get('rezoning',False))" 2>/dev/null)
[ "$T4_STAGE" = "UNDER_CITY_SUBMISSION" ] && [ "$T4_SPEC" = "Under Review" ] && T4_OK="true" || T4_OK="false"
check "Create UNDER_CITY_SUBMISSION listing" "$T4_OK" "Stage=$T4_STAGE | SubmissionStatus=$T4_SPEC | Rezoning=$T4_DEV"

# ==================================================
# TEST 5: Create READY_TO_SHOVEL listing
# ==================================================
echo "--- TEST 5: Create READY_TO_SHOVEL listing ---"
R5=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{
    "projectStage": "READY_TO_SHOVEL",
    "projectDetails": {
      "address": "2020 Builder Lane",
      "city": "Oakville",
      "postalCode": "L6H 0A1",
      "lotSize": 8000,
      "lotUnit": "sqft",
      "frontage": "60 ft",
      "depth": "133 ft"
    },
    "projectSpecification": {
      "projectType": "Custom Home Build",
      "sellingType": "Lot Only",
      "constructionStartTimeline": "Immediate",
      "approvalStatus": "All Approvals Complete",
      "services": {
        "gas": true,
        "hydro": true,
        "municipalSewer": true,
        "municipalWater": true
      }
    },
    "pricing": {
      "askingPrice": 890000,
      "currency": "CAD",
      "description": "Shovel-ready, all permits in place"
    },
    "photos": [
      {"name": "lot.jpg", "type": "image/jpeg", "url": "https://cdn.example.com/lot.jpg"}
    ],
    "documents": [
      {"name": "building_permit.pdf", "type": "application/pdf", "url": "https://cdn.example.com/permit.pdf"},
      {"name": "site_plan.pdf", "type": "application/pdf", "url": "https://cdn.example.com/siteplan.pdf"}
    ]
  }')
T5_STAGE=$(echo "$R5" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('projectStage',''))" 2>/dev/null)
T5_SPEC=$(echo "$R5" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}).get('projectSpecification',{}); print(d.get('approvalStatus',''))" 2>/dev/null)
T5_TIMELINE=$(echo "$R5" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}).get('projectSpecification',{}); print(d.get('constructionStartTimeline',''))" 2>/dev/null)
LAND5_ID=$(echo "$R5" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('id',''))" 2>/dev/null)
[ "$T5_STAGE" = "READY_TO_SHOVEL" ] && [ "$T5_SPEC" = "All Approvals Complete" ] && T5_OK="true" || T5_OK="false"
check "Create READY_TO_SHOVEL listing" "$T5_OK" "Stage=$T5_STAGE | ApprovalStatus=$T5_SPEC | Timeline=$T5_TIMELINE"

# ==================================================
# TEST 6: Get land by ID (verify all nested fields)
# ==================================================
echo "--- TEST 6: Get land by ID ---"
R6=$(curl -s -X GET "$BASE/lands/$LAND1_ID")
T6_STAGE=$(echo "$R6" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('projectStage',''))" 2>/dev/null)
T6_PIN=$(echo "$R6" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('pinNumber',''))" 2>/dev/null)
T6_OWN=$(echo "$R6" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('ownershipVerification',''))" 2>/dev/null)
T6_LAT=$(echo "$R6" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('latitude',''))" 2>/dev/null)
[ "$T6_STAGE" = "RAW_LAND" ] && [ "$T6_PIN" = "PIN-12345" ] && T6_OK="true" || T6_OK="false"
check "Get land by ID (nested fields)" "$T6_OK" "Stage=$T6_STAGE | PIN=$T6_PIN | Lat=$T6_LAT | Ownership=$T6_OWN"

# ==================================================
# TEST 7: Get vendor's own listings
# ==================================================
echo "--- TEST 7: Get vendor's own listings ---"
R7=$(curl -s -X GET "$BASE/vendor/lands" -H "Authorization: Bearer $AGENT_TOKEN")
T7_TOTAL=$(echo "$R7" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('totalElements',0))" 2>/dev/null)
[ "$T7_TOTAL" = "5" ] && T7_OK="true" || T7_OK="false"
check "Get vendor's own listings" "$T7_OK" "Total listings=$T7_TOTAL (expected 5)"

# ==================================================
# TEST 8: Update land listing
# ==================================================
echo "--- TEST 8: Update land listing ---"
R8=$(curl -s -X PUT "$BASE/vendor/lands/$LAND5_ID" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{
    "projectStage": "READY_TO_SHOVEL",
    "projectDetails": {
      "address": "2020 Builder Lane",
      "city": "Oakville",
      "postalCode": "L6H 0A1",
      "lotSize": 8500,
      "lotUnit": "sqft",
      "frontage": "65 ft",
      "depth": "130 ft"
    },
    "projectSpecification": {
      "projectType": "Custom Home Build",
      "sellingType": "Lot + Build Package",
      "constructionStartTimeline": "30 days",
      "approvalStatus": "All Approvals Complete"
    },
    "pricing": {
      "askingPrice": 920000,
      "currency": "CAD",
      "description": "Updated: now includes build package option"
    },
    "photos": [
      {"name": "lot_updated.jpg", "type": "image/jpeg", "url": "https://cdn.example.com/lot_updated.jpg"}
    ],
    "documents": [
      {"name": "building_permit.pdf", "type": "application/pdf", "url": "https://cdn.example.com/permit.pdf"},
      {"name": "site_plan.pdf", "type": "application/pdf", "url": "https://cdn.example.com/siteplan.pdf"},
      {"name": "build_package.pdf", "type": "application/pdf", "url": "https://cdn.example.com/build.pdf"}
    ]
  }')
T8_PRICE=$(echo "$R8" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('askingPrice',''))" 2>/dev/null)
T8_SELLING=$(echo "$R8" | python3 -c "import sys,json; d=json.load(sys.stdin).get('data',{}).get('projectSpecification',{}); print(d.get('sellingType',''))" 2>/dev/null)
T8_DOCS=$(echo "$R8" | python3 -c "import sys,json; print(len(json.load(sys.stdin).get('data',{}).get('documents',[])))" 2>/dev/null)
[ "$T8_PRICE" = "920000" ] && [ "$T8_SELLING" = "Lot + Build Package" ] && T8_OK="true" || T8_OK="false"
check "Update land listing" "$T8_OK" "Price=$T8_PRICE | SellingType=$T8_SELLING | Docs=$T8_DOCS"

# ==================================================
# TEST 9: Delete land listing
# ==================================================
echo "--- TEST 9: Delete land listing ---"
R9=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE/vendor/lands/$LAND1_ID" -H "Authorization: Bearer $AGENT_TOKEN")
[ "$R9" = "200" ] && T9_OK="true" || T9_OK="false"
check "Delete land listing" "$T9_OK" "HTTP Status=$R9"

# Verify count dropped
R9B=$(curl -s -X GET "$BASE/vendor/lands" -H "Authorization: Bearer $AGENT_TOKEN")
T9B_TOTAL=$(echo "$R9B" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('totalElements',0))" 2>/dev/null)
[ "$T9B_TOTAL" = "4" ] && T9B_OK="true" || T9B_OK="false"
check "Verify listing count after delete" "$T9B_OK" "Total=$T9B_TOTAL (expected 4)"

# ==================================================
# TEST 10: NEGATIVE - Missing required fields
# ==================================================
echo "--- TEST 10: NEGATIVE - Missing projectStage ---"
R10=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{
    "projectDetails": { "address": "No stage", "city": "Test", "postalCode": "A1A 1A1", "lotSize": 1000, "lotUnit": "sqft" },
    "pricing": { "askingPrice": 100000, "currency": "CAD", "description": "test" },
    "photos": [], "documents": []
  }')
T10_OK=$(echo "$R10" | python3 -c "import sys,json; print('true' if not json.load(sys.stdin).get('success',True) else 'false')" 2>/dev/null)
T10_MSG=$(echo "$R10" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
check "Missing projectStage rejected" "$T10_OK" "Message=$T10_MSG"

echo "--- TEST 11: NEGATIVE - Missing pricing ---"
R11=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d '{
    "projectStage": "RAW_LAND",
    "projectDetails": { "address": "No price", "city": "Test", "postalCode": "A1A 1A1", "lotSize": 1000, "lotUnit": "sqft" },
    "photos": [], "documents": []
  }')
T11_OK=$(echo "$R11" | python3 -c "import sys,json; print('true' if not json.load(sys.stdin).get('success',True) else 'false')" 2>/dev/null)
T11_MSG=$(echo "$R11" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
check "Missing pricing rejected" "$T11_OK" "Message=$T11_MSG"

echo "--- TEST 12: NEGATIVE - Too many photos (>10) ---"
PHOTOS_JSON=$(python3 -c "import json; print(json.dumps([{'name':f'p{i}.jpg','type':'image/jpeg','url':f'https://x.com/{i}.jpg'} for i in range(11)]))")
R12=$(curl -s -X POST "$BASE/vendor/lands" -H "Content-Type: application/json" -H "Authorization: Bearer $AGENT_TOKEN" \
  -d "{
    \"projectStage\": \"RAW_LAND\",
    \"projectDetails\": { \"address\": \"Too many pics\", \"city\": \"Test\", \"postalCode\": \"A1A 1A1\", \"lotSize\": 1000, \"lotUnit\": \"sqft\" },
    \"pricing\": { \"askingPrice\": 100000, \"currency\": \"CAD\", \"description\": \"test\" },
    \"photos\": $PHOTOS_JSON,
    \"documents\": []
  }")
T12_OK=$(echo "$R12" | python3 -c "import sys,json; print('true' if not json.load(sys.stdin).get('success',True) else 'false')" 2>/dev/null)
T12_MSG=$(echo "$R12" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null)
check "Too many photos rejected" "$T12_OK" "Message=$T12_MSG"

# ---- SUMMARY ----
echo "=========================================="
echo "📊 RESULTS: $PASS passed, $FAIL failed (out of $((PASS+FAIL)) tests)"
echo "=========================================="
