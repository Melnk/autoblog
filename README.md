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
    "eventDate": "2026-06-28",
    "odometerKm": 180000,
    "title": "Замена масла",
    "description": "Масло 5W-40, масляный фильтр",
    "costAmount": 4500,
    "costCurrency": "RUB",
    "serviceName": "Гаражный сервис",
    "payload": {
      "oil": "5W-40",
      "parts": ["oil_filter"]
    }
  }'
```

Get vehicle events:

```bash
curl http://localhost:8080/api/v1/vehicles/{vehicleId}/events
```

## API

- `POST /api/v1/vehicles`
- `GET /api/v1/vehicles/{vehicleId}`
- `GET /api/v1/vehicles/by-vin/{vin}`
- `POST /api/v1/vehicles/{vehicleId}/events`
- `GET /api/v1/vehicles/{vehicleId}/events`

Vehicle events are append-only. There are no update or delete endpoints for vehicle events in this stage.
