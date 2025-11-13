# Momentum Architecture Overview

## 1. System Context

Momentum is a microservice-based digital wallet and payment switch designed to demonstrate resilient, event-driven financial flows. External actors (consumer apps, internal auditors, automation scripts) interact solely through the API Gateway. Core domain services (Wallet, Transfer, Notification) and shared infrastructure (PostgreSQL, Redis, RabbitMQ) run as independent containers orchestrated via Docker Compose or a future orchestration platform.

## 2. Service Responsibilities

- **API Gateway** – Single ingress that terminates TLS, enforces authentication/rate limits (future), exposes `/api/*` routes, and proxies to downstream services. Built on Spring Cloud Gateway for declarative routing and filter policies.
- **Wallet Service** – System of record for ledger entries and wallet balances. Owns the PostgreSQL schema (users, wallets, ledger_entries) and enforces monetary invariants (idempotent top-ups, balance reads, double-entry persistence). Publishes domain events (e.g., `WalletDebited`, `WalletCredited`) to RabbitMQ.
- **Transfer Service** – Orchestrates multi-step transfers using the Saga pattern. Accepts client transfer requests (through the gateway), persists orchestration state, issues commands to Wallet Service via RabbitMQ, and triggers compensating actions upon failure. Uses Redis both for idempotency keys and per-user locks that prevent double-spend races.
- **Notification Service** – Consumes `TransferCompleted` / `TransferFailed` events and dispatches side effects (email/SMS/push). Runs statelessly and scales horizontally; outages must not block payments.

## 3. Data & Messaging Layer

- **PostgreSQL** – Primary ledger datastore. All monetary amounts stored as BIGINT minor units; mutations append to immutable ledger rows. Wallet Service is the exclusive writer.
- **Redis** – Provides low-latency cache access for hot balance reads, distributed locks keyed by wallet ID, and idempotency token storage keyed by client request UUID.
- **RabbitMQ** – Implements asynchronous command/event channels. Transfer Service publishes debit/credit commands; Wallet Service replies with completion/failure events; Notification Service passively consumes transfer outcome events. Dead-letter exchanges buffer poison messages for manual review.

## 4. Cross-Cutting Concerns

- **Consistency & Idempotency** – Saga orchestration plus compensating actions guarantee eventual atomicity. Redis-backed idempotency ensures retries cannot double-charge users.
- **Observability** – Spring Boot actuators expose health/metrics. Future phases add OpenTelemetry tracing (Jaeger) and Prometheus/Grafana dashboards for latency, backlog, and error-rate visibility. Correlation IDs propagate via gateway headers.
- **Security** – Secrets supplied via environment variables (`.env` excluded from VCS). Gateway to host auth later; services validate internal tokens before processing commands.
- **Documentation & Testing** – Shared kernel ships source/Javadoc JARs; public APIs require comments and unit coverage (JUnit + AssertJ). Integration tests leverage H2 for wallet service and Testcontainers for messaging/database scenarios.

## 5. Deployment & Operations

Local development runs via `docker-compose` (postgres:17, redis:8, rabbitmq:4). Each service is packaged as an independent Spring Boot artifact, versioned together through the Gradle multi-module build. CI (GitHub Actions) executes `./backend/gradlew clean build` on every push/PR, ensuring reproducible builds and preventing undocumented drift. Runbooks (Phase 4) will document startup order, failure modes, and alert thresholds.

## 6. Phase Alignment

- **Phase 1** delivers the Wallet Service monolith plus ledger schema.
- **Phase 2** adds Transfer Service, RabbitMQ contracts, and gateway routing.
- **Phase 3** layers Redis-based idempotency/locking and latency optimizations.
- **Phase 4** introduces tracing/metrics dashboards; **Phase 5** focuses on storytelling and polish.

This architecture balances instructional clarity with production-grade patterns, ensuring future contributors can extend services (fraud detection, KYC, reconciliation) without redefining core infrastructure.
