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

1. Start PostgreSQL and backend.
2. Start the frontend.
3. Register a user with password confirmation.
4. Create a vehicle.
5. Open the vehicle detail page.
6. Add a maintenance or repair event.
7. Add a maintenance reminder by date or odometer.
8. Complete or cancel a reminder.
9. Check the vehicle Trust Score card.
10. Upload one PUBLIC attachment.
11. Upload one PRIVATE attachment.
12. Generate a public report.
13. Open `/reports/{publicToken}` without logging in.
14. Verify Trust Score appears in the public report.
15. Verify only the PUBLIC attachment appears in the public report.

## Trust Score

Trust Score v0 is a rule-based, explainable score. It is not AI and it is not a guarantee.

The frontend displays Trust Score:

- on private vehicle detail pages;
- on public reports sent to a buyer.

The score uses backend signals for event count, evidence attachments, hash-chain validity, odometer consistency, recency, reminders, and overdue reminders. Known signal codes are localized in Russian and English; unknown codes fall back to the backend message.

## Language Settings

Open `/settings` to switch the interface language between Russian and English.

- The selected language is stored in `localStorage` under `autoblog.language`.
- The frontend displays localized enum labels for event types, attachment types, visibility, roles, and report statuses.
- Backend enum values remain unchanged and are still sent to the API, for example `MAINTENANCE`, `RECEIPT`, `PRIVATE`, `PUBLIC`.
- Language preference is not stored in the backend in this stage.

## Manual QA Checklist

- Register works.
- Registration requires matching password confirmation.
- Login works.
- Unauthorized user is redirected to `/login`.
- Vehicle creation works.
- Vehicles list shows backend data only.
- Vehicles list search works by VIN, make, and model.
- Events appear in the timeline.
- Event `title` is required.
- Attachment upload works.
- Reminder creation works with date, odometer, or both.
- Reminder type and due-state labels are localized.
- Reminder complete/cancel works for OWNER or EDITOR.
- VIEWER can see reminders but cannot complete/cancel them.
- Trust Score appears on vehicle detail.
- Trust Score signals and metrics are readable.
- Trust Score appears on public report without login.
- PUBLIC attachment appears in public report.
- PRIVATE attachment does not appear in public report.
- Public report opens without token.
- Open `/settings`.
- Switch to English.
- Vehicle event type select displays English labels.
- Timeline displays English labels.
- Attachment type and visibility display English labels.
- Public report displays English labels.
- Switch back to Russian.
- Vehicle event type select displays Russian labels.
- Timeline displays Russian labels.
- Attachment type and visibility display Russian labels.
- Event creation still sends backend enum values.
- Attachment upload still sends backend enum values.

## Auth Note

The MVP stores the access token in memory and `localStorage`.

TODO: Move production token storage to HttpOnly cookies through a BFF/API gateway.
