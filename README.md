# Nuledger

Nuledger is a multi-tenant double-entry ledger service built with Spring Boot and PostgreSQL.
It provides tenant-isolated accounts, journal posting, idempotent transaction handling, and reversal support.

## Highlights

- Multi-tenant isolation via PostgreSQL Row Level Security (RLS)
- Double-entry accounting validation (debits must equal credits)
- Idempotent transaction posting and reversal APIs
- Append-only journal tables
- RFC 9457 Problem Details error responses
- OpenAPI docs, Swagger UI, and Scalar API reference

## Tech Stack

- Java 26
- Spring Boot 4.1
- PostgreSQL 18
- Flyway migrations
- Spring Security OAuth2 Resource Server (JWT)
- Maven Wrapper (`mvnw`)

## Project Structure

- `/src/main/java/dev/iamkavindu/nuledger/ledger/api` – REST controllers and DTOs
- `/src/main/java/dev/iamkavindu/nuledger/ledger/service` – ledger and account business logic
- `/src/main/java/dev/iamkavindu/nuledger/ledger/persistence` – JPA repositories and entities
- `/src/main/java/dev/iamkavindu/nuledger/tenant` – tenant context and JWT tenant filter
- `/src/main/resources/db/migration` – Flyway schema and ownership migrations
- `/docker/postgres/init` – database bootstrap roles for local development

## Prerequisites

- JDK 26 installed and available on `PATH`
- Docker (for local PostgreSQL)
- Bash shell (for running `mvnw`)

## Local Setup

### 1) Start PostgreSQL

```bash
docker compose up -d postgres
```

This starts PostgreSQL on `localhost:5432` and initializes:

- `nuledger_owner` (migration/DDL role)
- `nuledger_app` (runtime app role with RLS enforcement)

### 2) Configure application properties

Set valid credentials in:

- `/home/runner/work/nuledger/nuledger/src/main/resources/application.properties`
- `/home/runner/work/nuledger/nuledger/src/main/resources/application-dev.properties`

At minimum, confirm datasource and Flyway credentials match your local database roles.

### 3) Run the application

```bash
bash ./mvnw spring-boot:run
```

Default base URL: `http://localhost:8080`

## Security and Tenant Model

- Nuledger is configured as a JWT resource server.
- All ledger endpoints require `Authorization: ******
- The JWT must include tenant claim `tenant_id`.
- That claim is mapped to `app.tenant_id` and enforced by PostgreSQL RLS policies.

If `tenant_id` is missing, the request is rejected with `403 Forbidden`.

## API Versioning

Nuledger exposes versioned paths. Current endpoints are used as:

- `/api/v1/accounts`
- `/api/v1/transactions`

## API Documentation

- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `/swagger-ui.html`
- Scalar UI: `/scalar`

## Core Endpoints

### Accounts

- `POST /api/v1/accounts` – create account
- `GET /api/v1/accounts/{id}` – get account by ID
- `GET /api/v1/accounts?code={code}` – get account by code
- `GET /api/v1/accounts/{id}/balances` – get account balances

### Transactions

- `POST /api/v1/transactions` – post double-entry transaction
- `POST /api/v1/transactions/{entryId}/reverse` – reverse a posted transaction

## Request Examples

Create an account:

```json
{
  "code": "cash-main",
  "name": "Main Cash",
  "accountType": "ASSET",
  "allowNegative": false
}
```

Post a transaction:

```json
{
  "idempotencyKey": "tx-2026-07-19-001",
  "correlationId": "invoice-1001",
  "lines": [
    {"accountId": "00000000-0000-0000-0000-000000000001", "direction": "DEBIT", "amountMinor": 10000, "currency": "LKR"},
    {"accountId": "00000000-0000-0000-0000-000000000002", "direction": "CREDIT", "amountMinor": 10000, "currency": "LKR"}
  ]
}
```

Reverse a transaction:

```json
{
  "idempotencyKey": "rev-2026-07-19-001",
  "correlationId": "correction-1001"
}
```

## Validation Rules

- Posting requires at least 2 lines.
- All lines in one transaction must use the same currency.
- Debits and credits must balance.
- `amountMinor` must be positive.
- Account codes are unique per tenant.

## Idempotency Behavior

- Reusing the same `idempotencyKey` for the same tenant returns the original result.
- First successful request returns `201 Created`.
- Replay returns `200 OK` with `replayed: true`.

## Health Endpoints

- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/info`

These are publicly accessible.

## Build, Lint, and Test

```bash
bash ./mvnw spotless:check
bash ./mvnw -DskipTests compile
bash ./mvnw test
```

Note: compilation and tests require a Java runtime that supports `--release 26`.
