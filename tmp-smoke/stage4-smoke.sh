#!/usr/bin/env bash
set -euo pipefail

BASE="http://localhost:8080"
RUN_ID="$(date +%s)"
PASS="Password123!"

OWNER_EMAIL="owner+$RUN_ID@example.test"
EDITOR_EMAIL="editor+$RUN_ID@example.test"
VIEWER_EMAIL="viewer+$RUN_ID@example.test"
RANDOM_EMAIL="random+$RUN_ID@example.test"

extract_field() {
  local field="$1"
  python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('$field') or '')"
}

extract_token() {
  python3 -c 'import sys,json
d=json.load(sys.stdin)
print(d.get("accessToken") or d.get("access_token") or d.get("token") or d.get("jwt") or "")'
}

extract_user_id() {
  python3 -c 'import sys,json
d=json.load(sys.stdin)
print(d.get("id") or d.get("userId") or d.get("user_id") or "")'
}

register_user() {
  local email="$1"
  local name="$2"

  curl -s -X POST "$BASE/api/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
      \"email\": \"$email\",
      \"password\": \"$PASS\",
      \"displayName\": \"$name\"
    }"
}

login_user() {
  local email="$1"

  curl -s -X POST "$BASE/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
      \"email\": \"$email\",
      \"password\": \"$PASS\"
    }"
}

echo "1. Register users"
echo "OWNER_EMAIL=$OWNER_EMAIL"
echo "EDITOR_EMAIL=$EDITOR_EMAIL"
echo "VIEWER_EMAIL=$VIEWER_EMAIL"
echo "RANDOM_EMAIL=$RANDOM_EMAIL"

OWNER_REGISTER=$(register_user "$OWNER_EMAIL" "Owner User")
EDITOR_REGISTER=$(register_user "$EDITOR_EMAIL" "Editor User")
VIEWER_REGISTER=$(register_user "$VIEWER_EMAIL" "Viewer User")
RANDOM_REGISTER=$(register_user "$RANDOM_EMAIL" "Random User")

echo "$OWNER_REGISTER"
echo "$EDITOR_REGISTER"
echo "$VIEWER_REGISTER"
echo "$RANDOM_REGISTER"

echo
echo "2. Login users"

OWNER_LOGIN=$(login_user "$OWNER_EMAIL")
EDITOR_LOGIN=$(login_user "$EDITOR_EMAIL")
VIEWER_LOGIN=$(login_user "$VIEWER_EMAIL")
RANDOM_LOGIN=$(login_user "$RANDOM_EMAIL")

OWNER_TOKEN=$(echo "$OWNER_LOGIN" | extract_token)
EDITOR_TOKEN=$(echo "$EDITOR_LOGIN" | extract_token)
VIEWER_TOKEN=$(echo "$VIEWER_LOGIN" | extract_token)
RANDOM_TOKEN=$(echo "$RANDOM_LOGIN" | extract_token)

echo "OWNER_TOKEN length: ${#OWNER_TOKEN}"
echo "EDITOR_TOKEN length: ${#EDITOR_TOKEN}"
echo "VIEWER_TOKEN length: ${#VIEWER_TOKEN}"
echo "RANDOM_TOKEN length: ${#RANDOM_TOKEN}"

if [ -z "$OWNER_TOKEN" ] || [ -z "$EDITOR_TOKEN" ] || [ -z "$VIEWER_TOKEN" ] || [ -z "$RANDOM_TOKEN" ]; then
  echo "ERROR: one of tokens is empty. Check login response DTO field names."
  echo "OWNER_LOGIN=$OWNER_LOGIN"
  exit 1
fi

echo
echo "3. /auth/me"

OWNER_ME=$(curl -s "$BASE/api/v1/auth/me" -H "Authorization: Bearer $OWNER_TOKEN")
EDITOR_ME=$(curl -s "$BASE/api/v1/auth/me" -H "Authorization: Bearer $EDITOR_TOKEN")
VIEWER_ME=$(curl -s "$BASE/api/v1/auth/me" -H "Authorization: Bearer $VIEWER_TOKEN")

echo "OWNER_ME=$OWNER_ME"
echo "EDITOR_ME=$EDITOR_ME"
echo "VIEWER_ME=$VIEWER_ME"

EDITOR_USER_ID=$(echo "$EDITOR_ME" | extract_user_id)
VIEWER_USER_ID=$(echo "$VIEWER_ME" | extract_user_id)

echo "EDITOR_USER_ID=$EDITOR_USER_ID"
echo "VIEWER_USER_ID=$VIEWER_USER_ID"

if [ -z "$EDITOR_USER_ID" ] || [ -z "$VIEWER_USER_ID" ]; then
  echo "ERROR: could not extract user id from /auth/me."
  exit 1
fi

echo
echo "4. Owner creates vehicle"

VIN="XTA217030H$(date +%H%M%S)1"

VEHICLE_RESPONSE=$(curl -s -X POST "$BASE/api/v1/vehicles" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"vin\": \"$VIN\",
    \"make\": \"LADA\",
    \"model\": \"Priora\",
    \"year\": 2013
  }")

echo "$VEHICLE_RESPONSE"

VEHICLE_ID=$(echo "$VEHICLE_RESPONSE" | extract_field "id")
echo "VEHICLE_ID=$VEHICLE_ID"

if [ -z "$VEHICLE_ID" ]; then
  echo "ERROR: vehicle id is empty."
  exit 1
fi

echo
echo "5. Owner creates MAINTENANCE event"

OWNER_EVENT_RESPONSE=$(curl -s -X POST "$BASE/api/v1/vehicles/$VEHICLE_ID/events" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MAINTENANCE",
    "eventDate": "2026-07-01",
    "title": "Oil change",
    "odometerKm": 120000,
    "costAmount": 5000,
    "costCurrency": "RUB",
    "serviceName": "LADA Service",
    "payload": {
      "notes": "Changed oil and filter"
    }
  }')

echo "$OWNER_EVENT_RESPONSE"

EVENT1_ID=$(echo "$OWNER_EVENT_RESPONSE" | extract_field "id")
echo "EVENT1_ID=$EVENT1_ID"

if [ -z "$EVENT1_ID" ]; then
  echo "ERROR: event id is empty."
  exit 1
fi

echo
echo "6. Owner uploads PUBLIC attachment"

ATTACHMENT_RESPONSE=$(curl -s -X POST "$BASE/api/v1/vehicles/$VEHICLE_ID/events/$EVENT1_ID/attachments" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -F "file=@tmp-smoke/stage4-receipt.pdf;type=application/pdf" \
  -F "type=RECEIPT" \
  -F "visibility=PUBLIC")

echo "$ATTACHMENT_RESPONSE"

ATTACHMENT_ID=$(echo "$ATTACHMENT_RESPONSE" | extract_field "id")
echo "ATTACHMENT_ID=$ATTACHMENT_ID"

if [ -z "$ATTACHMENT_ID" ]; then
  echo "ERROR: attachment id is empty."
  exit 1
fi

echo
echo "7. Owner creates public report"

REPORT_RESPONSE=$(curl -s -X POST "$BASE/api/v1/vehicles/$VEHICLE_ID/public-report" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}')

echo "$REPORT_RESPONSE"

PUBLIC_TOKEN=$(echo "$REPORT_RESPONSE" | python3 -c 'import sys,json; d=json.load(sys.stdin); print(d.get("publicToken") or d.get("token") or "")')
echo "PUBLIC_TOKEN=$PUBLIC_TOKEN"

if [ -z "$PUBLIC_TOKEN" ]; then
  echo "ERROR: public token is empty."
  exit 1
fi

echo
echo "8. Public report opens without token"

PUBLIC_STATUS=$(curl -s -o /tmp/stage4-public-report.json -w "%{http_code}" "$BASE/api/v1/public/reports/$PUBLIC_TOKEN")
echo "PUBLIC_STATUS=$PUBLIC_STATUS"
cat /tmp/stage4-public-report.json
echo

echo
echo "9. Owner grants EDITOR"

GRANT_EDITOR_RESPONSE=$(curl -s -X POST "$BASE/api/v1/vehicles/$VEHICLE_ID/access" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"$EDITOR_USER_ID\",
    \"role\": \"EDITOR\"
  }")

echo "$GRANT_EDITOR_RESPONSE"

echo
echo "10. Editor creates event"

EDITOR_EVENT_STATUS=$(curl -s -o /tmp/stage4-editor-event.json -w "%{http_code}" -X POST "$BASE/api/v1/vehicles/$VEHICLE_ID/events" \
  -H "Authorization: Bearer $EDITOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "REPAIR",
    "eventDate": "2026-07-02",
    "title": "Editor brake repair",
    "odometerKm": 121000,
    "costAmount": 12000,
    "costCurrency": "RUB",
    "serviceName": "LADA Service",
    "payload": {
      "work": "Front brake pads replacement"
    }
  }')

echo "EDITOR_EVENT_STATUS=$EDITOR_EVENT_STATUS"
cat /tmp/stage4-editor-event.json
echo

echo
echo "11. Owner grants VIEWER"

GRANT_VIEWER_RESPONSE=$(curl -s -X POST "$BASE/api/v1/vehicles/$VEHICLE_ID/access" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"$VIEWER_USER_ID\",
    \"role\": \"VIEWER\"
  }")

echo "$GRANT_VIEWER_RESPONSE"

echo
echo "12. Viewer can list events"

VIEWER_LIST_STATUS=$(curl -s -o /tmp/stage4-viewer-events.json -w "%{http_code}" \
  "$BASE/api/v1/vehicles/$VEHICLE_ID/events" \
  -H "Authorization: Bearer $VIEWER_TOKEN")

echo "VIEWER_LIST_STATUS=$VIEWER_LIST_STATUS"
cat /tmp/stage4-viewer-events.json
echo

echo
echo "13. Viewer cannot create event"

VIEWER_CREATE_STATUS=$(curl -s -o /tmp/stage4-viewer-create.json -w "%{http_code}" -X POST "$BASE/api/v1/vehicles/$VEHICLE_ID/events" \
  -H "Authorization: Bearer $VIEWER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "OTHER",
    "eventDate": "2026-07-03",
    "title": "Viewer forbidden event",
    "payload": {}
  }')

echo "VIEWER_CREATE_STATUS=$VIEWER_CREATE_STATUS"
cat /tmp/stage4-viewer-create.json
echo

echo
echo "14. Random user cannot get vehicle"

RANDOM_GET_STATUS=$(curl -s -o /tmp/stage4-random-get.json -w "%{http_code}" \
  "$BASE/api/v1/vehicles/$VEHICLE_ID" \
  -H "Authorization: Bearer $RANDOM_TOKEN")

echo "RANDOM_GET_STATUS=$RANDOM_GET_STATUS"
cat /tmp/stage4-random-get.json
echo

echo
echo "15. Protected endpoint without token"

NO_TOKEN_STATUS=$(curl -s -o /tmp/stage4-no-token.json -w "%{http_code}" \
  "$BASE/api/v1/vehicles/$VEHICLE_ID")

echo "NO_TOKEN_STATUS=$NO_TOKEN_STATUS"
cat /tmp/stage4-no-token.json
echo

echo
echo "Expected summary:"
echo "PUBLIC_STATUS should be 200"
echo "EDITOR_EVENT_STATUS should be 200 or 201"
echo "VIEWER_LIST_STATUS should be 200"
echo "VIEWER_CREATE_STATUS should be 403 or 404"
echo "RANDOM_GET_STATUS should be 404"
echo "NO_TOKEN_STATUS should be 401"
