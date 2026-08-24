# WebApp Design Spec

Date: 2026-08-21

## Goal

Build a small Java/Spring Boot web application, `WebApp`, that serves as the
System Under Test for a separate QA automation portfolio project (Selenium +
Java test framework practice). The app must be realistic but simple, and this
project does **not** include the Selenium/system test framework itself —
only scaffolding for CI to plug it in later.

## Stack

- Java 21, Spring Boot 3, Maven (single module at `WebApp/`).
- `spring-boot-starter-web`, `spring-boot-starter-thymeleaf`,
  `spring-boot-starter-data-jpa`, `spring-boot-starter-security`
- `org.xerial:sqlite-jdbc` + `hibernate-community-dialects` (SQLite dialect)
- `spring-boot-starter-test` for unit/integration tests written during TDD
  (no Selenium/system-test code is written as part of this project)
- SQLite DB file (`webapp.db`) written to the working directory at runtime;
  gitignored. `spring.jpa.hibernate.ddl-auto=update`.

## Domain model

- **User**: id, username (unique), passwordHash (BCrypt), `manageUsers`,
  `manageStock`, `viewAllOrders` (booleans) — permission flags directly on
  the user record, not separate role entities.
- **StockItem**: id, name, quantity.
- **Order**: id, creator (User FK), timestamp, status (enum; `COMPLETED` is
  the only value needed since stock changes are synchronous/atomic at
  creation — no pending/failed state).
- **OrderLine**: order FK, stockItem FK, quantity — represents the
  items/quantities on an order (join entity, one-to-many from Order).

## Auth

- Spring Security form login (`/login`), BCrypt password hashing,
  session-based auth (framework default).
- No self-registration. Users are created via the Users page (requires
  `MANAGE_USERS`) or via seed data.
- The three permission flags are exposed as Spring Security authorities
  (e.g. `MANAGE_USERS`, `MANAGE_STOCK`, `VIEW_ALL_ORDERS`) derived from the
  boolean columns at authentication time, and enforced via
  `@PreAuthorize`/route security config.

## Pages / Controllers

Server-rendered Thymeleaf, minimal styling — functionality over polish.

- `/login` — login form.
- `/orders`
  - GET: list the current user's own orders. If the user has
    `VIEW_ALL_ORDERS`, show a link/toggle to view all users' orders.
  - POST: create an order (a list of stockItem + quantity lines). Requires
    login (any authenticated user). Validates sufficient stock for **all**
    lines before decrementing **any** — the whole operation runs in one
    `@Transactional` method so a failure leaves stock untouched and returns
    a clear error to the user.
- `/stock`
  - GET: list all stock items — any logged-in user.
  - POST/PUT: create a stock item / update a quantity — requires
    `MANAGE_STOCK`.
- `/users`
  - GET/POST: create users and toggle their three permission flags —
    requires `MANAGE_USERS`.

## Seed data

On startup (`CommandLineRunner`, only runs if the `User` table is empty),
seed four demo users, all with password `password` for QA convenience:

| username | manageUsers | manageStock | viewAllOrders |
|---|---|---|---|
| admin | yes | yes | yes |
| user | no | no | no |
| stockmanager | no | yes | no |
| orderviewer | no | no | yes |

Plus a handful of `StockItem` rows (3-4 items, varied quantities, including
at least one low-stock item so the "insufficient stock" error path is easy
to exercise from the UI).

## CI/CD (GitHub Actions)

Two workflow files under `.github/workflows/`.

### `ci.yml` — PR and push-to-main gate

Triggers: `pull_request` targeting `main`, and `push` to `main`.

- `smoke-tests` job: placeholder step running
  `mvn test -Dtest=*SmokeTest` (matches nothing yet, Surefire passes with
  zero tests — a `# TODO` comment marks where the real Selenium/Java test
  framework drops in classes named `*SmokeTest`). Always required.
- `system-tests` job: same idea with `-Dtest=*SystemTest`. Always runs, and
  always uploads its result (`if: always()`) as a workflow artifact for
  visibility — but only **blocks** the build on pull requests, not on
  pushes to main.
- `build` job: needs `[smoke-tests, system-tests]`, but gates on
  `if: always() && needs.smoke-tests.result == 'success' && (github.event_name != 'pull_request' || needs.system-tests.result == 'success')`.
  This lets `system-tests` run and publish its artifact on every push while
  only failing the pipeline on PRs. Runs `mvn -B package` and uploads the
  jar via `actions/upload-artifact`.

### `release.yml` — tagged release gate

Triggers: push of a tag matching `v*.*.*` (semantic version).

- `smoke-tests` and `system-tests` jobs (same placeholders as above), both
  **required** with no bypass.
- `build` job: needs both, packages, uploads the jar as a workflow
  artifact. No GitHub Release object is created — the artifact is the
  deliverable, downloaded and self-hosted by the user.

## Testing (this project's own, not Selenium)

Standard `spring-boot-starter-test` unit/integration tests are used during
TDD to verify the order/stock-decrement transaction logic and the
permission checks. No system/Selenium-level testing is performed as part
of this project — that arrives later from the separate test framework
project, which will drop `*SmokeTest`/`*SystemTest` classes into the CI
placeholders described above.

## Out of scope

- Selenium/system test framework code itself.
- GitHub Release objects, live hosting/deployment.
- Self-registration, password reset, or any auth beyond session-based form
  login.
- Order editing/cancellation, order statuses beyond `COMPLETED`.
