# AutoBlog

AutoBlog is a Spring Boot backend for vehicle-centric digital history. The main aggregate is a vehicle, and every vehicle event is append-only and hash-linked to the previous event so the timeline can be verified.

## Stack

- Java 21
- Spring Boot 3
- Spring Web, Validation, Data JPA
- PostgreSQL
- Flyway
- Swagger/OpenAPI
- Maven

## Start PostgreSQL

Run Docker Compose commands from the project root where `docker-compose.yml` exists:

```bash
cd autoblog
docker compose --profile local up -d postgres
```

The local database uses:

- database: `autoblog`
- username: `autoblog`
- password: `autoblog`
- port: `5432`

Open psql:

```bash
docker compose exec postgres psql -U autoblog -d autoblog
```

Useful psql commands:

```text
\dt
\q
```

## Run Tests

```bash
mvn test
```

Tests use the `test` profile and an in-memory H2 database with Flyway migrations.

## Run Backend

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile contains a development JWT secret. For non-local environments set `AUTOBLOG_JWT_SECRET` and optionally `AUTOBLOG_JWT_TTL_MINUTES`.

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Run Frontend

The Web MVP lives in `frontend/`.

```bash
cd frontend
cp .env.example .env.local
pnpm install
pnpm dev
```

Frontend URL:

```text
http://localhost:3000
```

Required frontend env var:

```text
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## API Contract

Vehicle events require:

- `type`
- `eventDate`
- `title`

Canonical queryable event fields must be sent top-level:

- `odometerKm`
- `costAmount`
- `costCurrency`
- `serviceName`

`payload` is extension data only for event-specific details.

Correct:

```json
{
  "odometerKm": 120000,
  "costAmount": 5000,
  "costCurrency": "RUB",
  "serviceName": "Гаражный сервис",
  "payload": {
    "oil": "5W-40",
    "parts": ["oil_filter"]
  }
}
```

Avoid:

```json
{
  "payload": {
    "mileageKm": 120000,
    "cost": 5000,
    "currency": "RUB"
  }
}
```

Reason: AutoBlog needs top-level canonical fields for analytics, ownership cost, timeline filtering, trust scoring, and future marketplace integrations.

## Authentication

Private vehicle APIs require a stateless JWT access token:

```text
Authorization: Bearer <token>
```

Public report read endpoints do not require authentication.

Register a user:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "owner@example.com",
    "password": "StrongPassword123!",
    "displayName": "Owner"
  }'
```

Login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "owner@example.com",
    "password": "StrongPassword123!"
  }'
```

Store the access token:

```bash
TOKEN="<accessToken>"
```

Get the current user:

```bash
curl http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

## Example API Calls

Create vehicle:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "vin": "XTA217030C0000000",
    "make": "Lada",
    "model": "Priora",
    "generation": "2170",
    "year": 2012,
    "engine": "1.6",
    "transmission": "MT",
    "trim": "Norma",
    "market": "RU"
  }'
```

Get vehicle by id:

```bash
curl http://localhost:8080/api/v1/vehicles/{vehicleId} \
  -H "Authorization: Bearer $TOKEN"
```

Get vehicle by VIN:

```bash
curl http://localhost:8080/api/v1/vehicles/by-vin/XTA217030C0000000 \
  -H "Authorization: Bearer $TOKEN"
```

Add maintenance event:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "MAINTENANCE",
    "eventDate": "2026-07-02",
    "odometerKm": 120000,
    "title": "Замена масла",
    "description": "Масло 5W-40, масляный фильтр",
    "costAmount": 5000,
    "costCurrency": "RUB",
    "serviceName": "Гаражный сервис",
    "payload": {
      "oil": "5W-40",
      "parts": ["oil_filter"]
    }
  }'
```

Add repair event:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "REPAIR",
    "eventDate": "2026-07-10",
    "odometerKm": 120500,
    "title": "Замена передних тормозных колодок",
    "description": "Заменены передние тормозные колодки",
    "costAmount": 3500,
    "costCurrency": "RUB",
    "serviceName": "Гаражный сервис",
    "payload": {
      "parts": ["front_brake_pads"]
    }
  }'
```

Get vehicle events:

```bash
curl http://localhost:8080/api/v1/vehicles/{vehicleId}/events \
  -H "Authorization: Bearer $TOKEN"
```

List vehicles current user can access:

```bash
curl http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer $TOKEN"
```

## Public Vehicle Report

A public vehicle report is a public-safe, shareable vehicle history report. It is intended for sharing with a buyer through a link or QR code when the vehicle is being sold.

The public report does not expose owner/user data. Public vehicle data does not include the internal vehicle UUID, and public event data does not include internal event UUIDs.

1. Create vehicle:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "vin": "XTA217030C0000000",
    "make": "Lada",
    "model": "Priora",
    "generation": "2170",
    "year": 2012,
    "engine": "1.6",
    "transmission": "MT",
    "trim": "Norma",
    "market": "RU"
  }'
```

2. Add first event:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "MAINTENANCE",
    "eventDate": "2026-07-02",
    "odometerKm": 120000,
    "title": "Замена масла",
    "description": "Масло 5W-40, масляный фильтр",
    "costAmount": 5000,
    "costCurrency": "RUB",
    "serviceName": "Гаражный сервис",
    "payload": {
      "oil": "5W-40",
      "parts": ["oil_filter"]
    }
  }'
```

3. Add second event:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "REPAIR",
    "eventDate": "2026-07-10",
    "odometerKm": 120500,
    "title": "Замена передних тормозных колодок",
    "description": "Заменены передние тормозные колодки",
    "costAmount": 3500,
    "costCurrency": "RUB",
    "serviceName": "Гаражный сервис",
    "payload": {
      "parts": ["front_brake_pads"]
    }
  }'
```

4. Create or get the active public report:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/public-report \
  -H "Authorization: Bearer $TOKEN"
```

Response contains `publicToken` and `publicUrl`. Calling the same endpoint again returns the same active report instead of creating duplicates.

5. Get public report by token:

```bash
curl http://localhost:8080/api/v1/public/reports/{publicToken}
```

6. Get QR SVG:

```bash
curl http://localhost:8080/api/v1/public/reports/{publicToken}/qr
```

The QR endpoint returns `image/svg+xml`. By default the QR encodes `/api/v1/public/reports/{publicToken}`. Set `autoblog.public-base-url` to encode an absolute public URL.

## Event Evidence / Attachments

Attachments are evidence for a specific vehicle event: repair photos, before/after photos, receipts, work orders, diagnostics PDFs, and part photos.

`payload` is not for files. Put structured event details in `payload`, and upload files through attachment endpoints. `PRIVATE` is the default visibility. Only `PUBLIC` attachments appear in public vehicle reports.

Allowed content types:

- `image/jpeg`
- `image/png`
- `image/webp`
- `application/pdf`

Local files are stored under `./data/uploads` by default. This local storage is temporary and intentionally isolated behind an `AttachmentStorage` abstraction so it can be replaced with S3/MinIO later.

Upload a public receipt PDF to the first event:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments \
  -H "Authorization: Bearer $TOKEN" \
  -F 'file=@./receipt.pdf;type=application/pdf' \
  -F 'type=RECEIPT' \
  -F 'visibility=PUBLIC' \
  -F 'description=Чек за замену масла'
```

Upload a private photo to the first event:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments \
  -H "Authorization: Bearer $TOKEN" \
  -F 'file=@./repair-photo.png;type=image/png' \
  -F 'type=PHOTO' \
  -F 'description=Фото после ремонта'
```

List event attachments:

```bash
curl http://localhost:8080/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments \
  -H "Authorization: Bearer $TOKEN"
```

Download an internal attachment:

```bash
curl -L -o receipt.pdf \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/vehicles/{vehicleId}/events/{eventId}/attachments/{attachmentId}/download
```

Get the public report and check that only `PUBLIC` attachments appear:

```bash
curl http://localhost:8080/api/v1/public/reports/{publicToken}
```

Download a public attachment from the public report:

```bash
curl -L -o public-receipt.pdf \
  http://localhost:8080/api/v1/public/reports/{publicToken}/attachments/{attachmentId}
```

## Maintenance Reminders

Reminders are planning objects for future service actions. They belong to a vehicle and can be completed or cancelled. A reminder does not become part of the append-only vehicle history until the user actually creates a `VehicleEvent`.

`OWNER` and `EDITOR` can create, complete, and cancel reminders. `VIEWER` can list reminders but cannot modify them.

1. Create a vehicle and add an event with odometer:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "MAINTENANCE",
    "eventDate": "2026-07-02",
    "odometerKm": 127842,
    "title": "Замена масла"
  }'
```

2. Create reminder by date and odometer:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/reminders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Заменить масло",
    "description": "Следующая замена масла после ТО",
    "type": "OIL_CHANGE",
    "dueDate": "2026-09-01",
    "dueOdometerKm": 135000
  }'
```

3. Create reminder by odometer only:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/reminders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Проверить тормоза",
    "type": "BRAKE_SERVICE",
    "dueOdometerKm": 128300
  }'
```

4. List reminders:

```bash
curl http://localhost:8080/api/v1/vehicles/{vehicleId}/reminders \
  -H "Authorization: Bearer $TOKEN"
```

Optional filters:

```bash
curl 'http://localhost:8080/api/v1/vehicles/{vehicleId}/reminders?status=ACTIVE&dueState=DUE_SOON' \
  -H "Authorization: Bearer $TOKEN"
```

5. Complete reminder:

```bash
curl -X PATCH http://localhost:8080/api/v1/vehicles/{vehicleId}/reminders/{reminderId}/complete \
  -H "Authorization: Bearer $TOKEN"
```

6. Cancel reminder:

```bash
curl -X PATCH http://localhost:8080/api/v1/vehicles/{vehicleId}/reminders/{reminderId}/cancel \
  -H "Authorization: Bearer $TOKEN"
```

7. Login as a `VIEWER` and confirm listing works but create/complete/cancel returns `403 Forbidden`.

## Vehicle Access Control

When a user creates a vehicle, AutoBlog grants that user `OWNER` access automatically.

Roles:

- `OWNER`: can read, modify history, create public reports, and manage access.
- `EDITOR`: can read, modify history, and create public reports.
- `VIEWER`: can read private vehicle data but cannot modify history or manage access.

Register another user before granting access:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "editor@example.com",
    "password": "StrongPassword123!",
    "displayName": "Editor"
  }'
```

Grant access uses the target user's `email`, not `userId`. The target user must already exist. Use `userId` only in the revoke path after listing current access rows.

Grant `EDITOR` access to an existing user:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/access \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "editor@example.com",
    "role": "EDITOR"
  }'
```

Grant `VIEWER` access:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/access \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "viewer@example.com",
    "role": "VIEWER"
  }'
```

List vehicle access:

```bash
curl http://localhost:8080/api/v1/vehicles/{vehicleId}/access \
  -H "Authorization: Bearer $TOKEN"
```

Revoke access:

```bash
curl -X DELETE http://localhost:8080/api/v1/vehicles/{vehicleId}/access/{userId} \
  -H "Authorization: Bearer $TOKEN"
```

A `VIEWER` token can list events but cannot add events:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events \
  -H "Authorization: Bearer $VIEWER_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "MAINTENANCE",
    "eventDate": "2026-07-02",
    "title": "Viewer attempt"
  }'
```

This returns `403 Forbidden`.

## Database Checks

The public API returns `year`, but the PostgreSQL column is `model_year`.

```sql
SELECT id, vin, make, model, model_year, market, created_at, updated_at
FROM vehicles;
```

```sql
SELECT vehicle_id, sequence_number, event_type, event_date, previous_event_hash, event_hash
FROM vehicle_events
ORDER BY vehicle_id, sequence_number;
```

For the second event, `previous_event_hash` should equal the first event's `event_hash`.

```sql
SELECT vehicle_id, event_id, type, visibility, original_filename, content_type, size_bytes, checksum_sha256, created_at
FROM event_attachments
ORDER BY created_at;
```

```sql
SELECT id, email, display_name, status, created_at, updated_at
FROM user_accounts;
```

```sql
SELECT vehicle_id, user_id, role, created_at
FROM vehicle_access
ORDER BY vehicle_id, created_at;
```

```sql
SELECT vehicle_id, title, type, due_date, due_odometer_km, status, completed_at, cancelled_at
FROM maintenance_reminders
ORDER BY vehicle_id, created_at;
```

## API

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/vehicles`
- `GET /api/v1/vehicles`
- `GET /api/v1/vehicles/{vehicleId}`
- `GET /api/v1/vehicles/by-vin/{vin}`
- `POST /api/v1/vehicles/{vehicleId}/events`
- `GET /api/v1/vehicles/{vehicleId}/events`
- `POST /api/v1/vehicles/{vehicleId}/events/{eventId}/attachments`
- `GET /api/v1/vehicles/{vehicleId}/events/{eventId}/attachments`
- `GET /api/v1/vehicles/{vehicleId}/events/{eventId}/attachments/{attachmentId}/download`
- `POST /api/v1/vehicles/{vehicleId}/reminders`
- `GET /api/v1/vehicles/{vehicleId}/reminders`
- `PATCH /api/v1/vehicles/{vehicleId}/reminders/{reminderId}/complete`
- `PATCH /api/v1/vehicles/{vehicleId}/reminders/{reminderId}/cancel`
- `POST /api/v1/vehicles/{vehicleId}/public-report`
- `POST /api/v1/vehicles/{vehicleId}/access`
- `GET /api/v1/vehicles/{vehicleId}/access`
- `DELETE /api/v1/vehicles/{vehicleId}/access/{userId}`
- `GET /api/v1/public/reports/{publicToken}`
- `GET /api/v1/public/reports/{publicToken}/qr`
- `GET /api/v1/public/reports/{publicToken}/attachments/{attachmentId}`

Vehicle events are append-only. There are no update or delete endpoints for vehicle events in this stage.
