# Autoblog

Stage 1 backend for storing vehicles and tamper-evident timeline events.

## Requirements

- Java 21+
- Maven 3.9+
- Docker Compose, for local PostgreSQL

## Run Locally

Start PostgreSQL:

```bash
docker compose --profile local up -d postgres
```

Run the API:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Run tests:

```bash
mvn test
```

The test profile uses an in-memory H2 database. The local profile uses PostgreSQL at `localhost:5432` with database, username, and password all set to `autoblog`.

## API

- `POST /api/vehicles` creates a vehicle.
- `GET /api/vehicles/{vehicleId}` returns one vehicle.
- `POST /api/vehicles/{vehicleId}/events` appends an event to the vehicle hash chain.
- `GET /api/vehicles/{vehicleId}/timeline` returns timeline events in chain order.

Swagger UI is available at `/swagger-ui.html` when the app is running.

## Notes

- Controllers return DTOs only; JPA entities stay behind the application service.
- Validation errors and domain errors return structured JSON from the global exception handler.
- Stage 1 intentionally stops before frontend and auth.
