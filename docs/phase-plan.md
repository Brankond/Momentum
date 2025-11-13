# Momentum Phase Plan

## Overview

- **Goal:** Deliver a resilient digital wallet and payment switch that showcases microservice fundamentals (SAGA, idempotency, observability) while remaining manageable alongside a master’s workload.
- **Cadence assumption:** ~10 focused hours/week split across two sessions (e.g., weeknight + weekend).
- **Definition of done:** Each phase ships runnable code, accompanying tests, and doc updates so that progress can pause/restart without context loss.

## Phases

### Phase 0 – Environment & Research (1 week)

- **Deliverables:** Local dev container stack (PostgreSQL + Redis + RabbitMQ), project skeleton (Spring Boot multi-module or separate repos), initial architecture diagram, ADR for monolith-first approach.
- **Success metrics:** `docker compose up` boots all dependencies; service template builds/tests in CI; architectural risks logged.
- **Dependencies:** None.

### Phase 1 – Core Ledger Monolith (3 weeks)

- **Deliverables:** Wallet Service with user + ledger schema, REST APIs for top-up and balance, monetary precision via BIGINT cents, basic unit/integration tests, Postman collection.
- **Success metrics:** 100% monetary mutations recorded in ledger table; test suite run < 2 min; README quick-start instructions verified.
- **Dependencies:** Phase 0 environment assets.

### Phase 2 – Distributed Transfer & SAGA (4 weeks)

- **Deliverables:** Transfer Service, RabbitMQ messaging contracts, orchestrated transfer workflow with compensating actions, failure-case integration tests (insufficient funds, downstream timeout), API Gateway routing.
- **Success metrics:** End-to-end transfer demo covering success + rollback; retry-safe message handling; sequence diagrams/doc updates.
- **Dependencies:** Stable Wallet Service APIs.

### Phase 3 – Reliability Hardening (3 weeks)

- **Deliverables:** Redis integration for idempotency keys and per-user locks, caching layer for hot balances, latency instrumentation, automated load test script (e.g., Gatling/JMeter) hitting P95 targets (<500 ms transfer ack, <100 ms balance).
- **Success metrics:** Load test report stored in `/docs/tests`; alerts when lock acquisition fails; resilience checklist completed.
- **Dependencies:** Messaging workflow from Phase 2.

### Phase 4 – Observability & Ops (2 weeks)

- **Deliverables:** OpenTelemetry tracing (Jaeger), Prometheus metrics + Grafana dashboards, structured logging with correlation IDs, basic runbook (start/stop, common failures).
- **Success metrics:** Single transfer trace viewable end-to-end; dashboard panels for latency, message backlog, error rates; runbook validated by dry-run incident drill.
- **Dependencies:** Stable services + telemetry hooks.

### Phase 5 – Polish & Storytelling (ongoing, 1 week buffer)

- **Deliverables:** Public-facing write-up, architecture README updates, demo script/video outline, backlog of stretch ideas (auth, fraud checks, DR).
- **Success metrics:** Recruiter-ready summary; clear next-step list; repository tagged for MVP release.
- **Dependencies:** Completion of earlier phases.

## Cross-Cutting Practices

- Timebox each session with a written objective + retrospective notes in `/docs/dev-journal.md` (optional) to ease context switches during academic crunch weeks.
- Automate quality gates early (maven/gradle checks, linting) and run them before ending each work session.
- Use feature toggles to merge partially complete services without blocking progress.

## Risk & Mitigation Snapshot

- **Academic load spikes:** Keep backlog groomed with “small wins” (<2 hrs) so progress can continue during exam weeks.
- **Infrastructure drift:** Standardize via Docker Compose + infrastructure-as-code snippets; pin versions in `.tool-versions` or similar.
- **Complexity creep:** Reassess scope at phase boundaries; postpone advanced features (fraud, multi-region) until MVP stabilizes.
