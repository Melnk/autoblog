# AutoBlog production architecture

## Decision

AutoBlog remains a modular monolith. The vehicle is the central aggregate; identity/access, attachments, reminders, public reports, and trust score are business capabilities around it. Each capability keeps HTTP mapping in `api`, use-case orchestration in `application`, business vocabulary in `domain`, and persistence/adapters in `infrastructure`.

This shape is intentionally retained instead of introducing microservices. The current scale does not justify distributed transactions, duplicated infrastructure, or operational coupling.

Architecture tests enforce the dependency boundaries that matter today:

- domain code cannot depend on Spring, JPA, API, application, or infrastructure packages;
- API code cannot call persistence infrastructure directly;
- persistence infrastructure cannot depend on HTTP/API code.

## Runtime flow

```text
Browser
  -> Next.js UI and same-origin /api/bff
     -> HttpOnly SameSite=Strict session cookie
     -> Spring Boot API with short-lived JWT
        -> PostgreSQL (transactional data and locking)
        -> S3-compatible object storage (attachments)
        -> Actuator/Micrometer -> Prometheus
```

The backend JWT is never exposed to browser JavaScript. Unsafe BFF requests require a matching `Origin` header. In production only the Next.js container publishes a host port; the backend, database, object storage, management port, and Prometheus stay on the internal network.

## API evolution

Existing `/api/v1` collection responses stay unchanged for backward compatibility. New UI collection reads use `/api/v2` and a stable page envelope:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Page sizes are limited to 100 and invalid pagination returns `422`. OpenAPI annotations remain next to controllers and `/v3/api-docs` is the generated contract used by clients.

## Consistency and failure handling

- Event creation takes a pessimistic lock on the vehicle row before reading the latest event, so concurrent writers cannot reuse a `sequenceNumber` or fork the hash chain.
- Public report creation/rotation uses the same aggregate lock and keeps at most one active link.
- Attachment bytes are written before metadata. A transaction synchronization deletes the object if the database transaction rolls back, avoiding orphaned S3/local objects.
- Production attachments use the S3 adapter. Local filesystem storage remains a local/test adapter only.

## Operations

- `application-prod.yml` requires database and JWT secrets from the environment and selects S3 storage.
- `/actuator/health/**` supports liveness/readiness probes; `/actuator/prometheus` exposes metrics on the production management port.
- The production Compose topology is a reproducible single-host reference, not a substitute for managed backups, TLS termination, alert routing, secret management, or an orchestrator.
- CI runs backend tests, frontend lint/tests/build, and both container builds. Version tags publish images to GHCR; deployment to a real environment remains environment-specific.
