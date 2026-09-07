# AutoBlog Frontend

Next.js Web MVP for AutoBlog.

## Дизайн и развитие

Интерфейс ориентирован в первую очередь на владельцев автомобилей в России. Визуальный референс — [Домиленд](https://domyland.ru/): светлые поверхности, крупные скругления, понятная навигация и спокойные формы. AutoBlog использует собственную чёрно-жёлтую палитру и автомобильные иллюстрации.

- Цвета определены в `tailwind.config.ts`: `canvas` — фон, `ink` — основной текст, `muted` — вторичный текст, `line` — границы, `brand.yellow` — действия, `brand.soft` — мягкие акценты. Жёлтые кнопки всегда используют тёмный текст.
- Общие кнопки, карточки, поля и статусы находятся в `src/components/ui`. Декоративные иллюстрации не являются фотографиями конкретного автомобиля.
- На телефоне используется нижняя навигация; формы входа имеют компактное приветствие. Фокус клавиатуры видим, анимации учитывают `prefers-reduced-motion`.
- Технические сведения о хешах и дополнительных данных раскрываются отдельно. Оценка истории не заменяет осмотр автомобиля.
- Поиск в гараже пока ограничен текущей страницей; при нескольких страницах интерфейс явно сообщает это.

Следующие этапы и критерии готовности: [план развития AutoBlog](../docs/ROADMAP.md).

Проверка редизайна 07.09.2026: production-сборка, линтер и 7 frontend-тестов прошли; браузерный проход с реальным локальным API и временной H2-базой — регистрация, пустой гараж, создание автомобиля, запись ТО, напоминание, публичный отчёт, пустые результаты поиска и сброс фильтра. Проверены настольная раскладка и узкие экраны 360–390 px. Это проверка UI, не замена полному E2E и испытаниям PostgreSQL/S3, запланированным в roadmap.

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
AUTOBLOG_BACKEND_URL=http://localhost:8080
AUTOBLOG_PUBLIC_ORIGIN=http://localhost:3000
```

These variables are read only by the Next.js server. Browser requests use the same-origin `/api/bff` route. Set the public origin explicitly when TLS is terminated by a reverse proxy so BFF origin validation uses the external URL.

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
pnpm test
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
16. As OWNER, grant EDITOR/VIEWER access by email and revoke it again.
17. Verify VIEWER sees no event, upload, reminder, report, or access-management actions.
18. Rotate the public report and verify the old link no longer opens.
19. Disable the public report and verify the active link no longer opens.

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
- Attachment and timeline pagination works.
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
- OWNER can grant and revoke vehicle access by email.
- VIEWER cannot see mutating actions or the access list.
- Public report rotation invalidates the previous token.
- Public report disable invalidates the active token.
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

The browser never receives the backend JWT. Login and registration go through the same-origin Next.js BFF, which stores the token in an `HttpOnly`, `SameSite=Strict`, `Secure` production cookie. Unsafe BFF requests also require a matching `Origin` header.
