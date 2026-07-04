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

```bash
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

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
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

## Example API Calls

Create vehicle:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles \
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
curl http://localhost:8080/api/v1/vehicles/{vehicleId}
```

Get vehicle by VIN:

```bash
curl http://localhost:8080/api/v1/vehicles/by-vin/XTA217030C0000000
```

Add maintenance event:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/events \
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
curl http://localhost:8080/api/v1/vehicles/{vehicleId}/events
```

## Public Vehicle Report

A public vehicle report is a public-safe, shareable vehicle history report. It is intended for sharing with a buyer through a link or QR code when the vehicle is being sold.

The public report does not expose owner/user data. Public vehicle data does not include the internal vehicle UUID, and public event data does not include internal event UUIDs.

1. Create vehicle:

```bash
curl -X POST http://localhost:8080/api/v1/vehicles \
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
curl -X POST http://localhost:8080/api/v1/vehicles/{vehicleId}/public-report
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

## API

- `POST /api/v1/vehicles`
- `GET /api/v1/vehicles/{vehicleId}`
- `GET /api/v1/vehicles/by-vin/{vin}`
- `POST /api/v1/vehicles/{vehicleId}/events`
- `GET /api/v1/vehicles/{vehicleId}/events`
- `POST /api/v1/vehicles/{vehicleId}/public-report`
- `GET /api/v1/public/reports/{publicToken}`
- `GET /api/v1/public/reports/{publicToken}/qr`

Vehicle events are append-only. There are no update or delete endpoints for vehicle events in this stage.
