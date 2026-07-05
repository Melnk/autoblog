# AutoBlog Frontend

Next.js Web MVP for AutoBlog.

## Stack

- Next.js
- TypeScript
- Tailwind CSS
- React Hook Form
- Zod
- Fetch API with a small typed client

## Environment

Create `.env.local`:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## Run Backend

From the repository root:

```bash
docker compose --profile local up -d postgres
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Run Frontend

From `frontend/`:

```bash
pnpm install
pnpm dev
```

Open:

```text
http://localhost:3000
```

## Scripts

```bash
pnpm dev
pnpm build
pnpm lint
```

## Test User Flow

1. Register a user.
2. Create a vehicle.
3. Open the vehicle detail page.
4. Add a maintenance or repair event.
5. Upload an attachment to the event.
6. Generate a public report.
7. Open `/reports/{publicToken}` without logging in.

## Manual QA Checklist

- Register works.
- Login works.
- Unauthorized user is redirected to `/login`.
- Vehicle creation works.
- Vehicles list shows backend data only.
- Events appear in the timeline.
- Event `title` is required.
- Attachment upload works.
- Public report opens without token.
- Private attachment does not appear in public report.
- Public attachment appears in public report.

## Auth Note

The MVP stores the access token in memory and `localStorage`.

TODO: Move production token storage to HttpOnly cookies through a BFF/API gateway.
