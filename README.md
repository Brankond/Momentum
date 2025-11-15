# Momentum

"Momentum" is a high-availability, high-reliability digital wallet and payment switching system designed to simulate a real-world Fintech payment platform.

## Phase 1 – Wallet Service

During Phase 1 we focus on the wallet (ledger) monolith. The service exposes REST endpoints via the API Gateway and persists data in PostgreSQL (H2 in tests). Flyway migrations under `services/wallet-service/src/main/resources/db/migration` create the `users`, `wallets`, and `ledger_entries` tables, and `WalletEntity.currency` is explicitly mapped with `@JdbcTypeCode(Types.CHAR)` to match the `CHAR(3)` schema enforced by Hibernate 6 validation.

### What’s Included

- Flyway baseline (`V1__wallet_core_tables.sql`) plus Gradle Flyway dependencies so the service auto-validates/migrates Postgres on startup.
- Persistence/domain/api layers for wallets, with REST controllers (`WalletController`), DTOs, idempotent commands, and pessimistic locking helpers.
- Structured error responses via `RestExceptionHandler`.
- Tests covering the domain service and controller; when running the `test` profile, Flyway is disabled and `spring.jpa.hibernate.ddl-auto=create-drop` spins up an in-memory H2 schema mirroring Postgres types (H2 is configured in PostgreSQL compatibility mode).

### Run & Test

```bash
# start local deps if needed
docker-compose up -d postgres

# boot the wallet service
./backend/gradlew :services:wallet-service:bootRun

# run the wallet unit/integration + controller tests (uses the H2 test profile)
./backend/gradlew :services:wallet-service:test
```

### Wallet API Overview

All routes are rooted at `/api/v1/wallets`. Clients are responsible for providing an idempotency `reference` when mutating balances so retries don’t double-charge.

| Method | Path                                | Description                                                                                                                                    |
| ------ | ----------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| `POST` | `/api/v1/wallets`                   | Create a wallet for a user/currency. Body: `{ "userId": "<uuid>", "externalUserId": "...", "currency": "USD", "initialBalanceMinorUnits": 0 }` |
| `GET`  | `/api/v1/wallets/{walletId}`        | Fetch wallet snapshot (balance, currency, status).                                                                                             |
| `GET`  | `/api/v1/wallets/{walletId}/ledger` | List ledger entries (credit/debit history).                                                                                                    |
| `POST` | `/api/v1/wallets/{walletId}/credit` | Credit wallet. Body: `{ "amountMinorUnits": 1000, "reference": "client-key", "description": "...", "metadata": "..." }`                        |
| `POST` | `/api/v1/wallets/{walletId}/debit`  | Debit wallet (fails if insufficient funds). Body matches credit.                                                                               |

Responses include immutable snapshots of wallets or ledger entries; errors are returned as `ApiError` (`{ timestamp, status, message, details }`). Use the `test` profile (H2) during automated tests; Postgres via Docker Compose for local dev. When Flyway validation fails (e.g., schema drift), inspect the migration files or rebuild the Dockerized Postgres volume before re-running `bootRun`.
