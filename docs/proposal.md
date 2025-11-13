Project Proposition: "Momentum" Digital Wallet & Payment Switch

Version: 1.0
Date: November 13, 2025
Owner: (Your Name)
Status: Proposed

1. Executive Summary

"Momentum" is a high-availability, high-reliability digital wallet and payment switching system designed to simulate a real-world Fintech payment platform. The project's primary goal is to build a distributed system based on a microservices architecture that can handle payment transactions reliably, overcoming the challenges of network failure, race conditions, and data consistency.

This project will leverage an enterprise-grade Java stack (Spring Boot) combined with modern middleware (Redis, RabbitMQ) and a robust database (PostgreSQL) to create a blueprint for a scalable and resilient payment infrastructure.

2. Business Case & Real-Life Significance

2.1. The Problem (The "Why")

In a monolithic application, transferring funds is a simple, atomic (ACID) database transaction. In a modern, scalable microservices architecture, this is impossible. The "User Balance" and "Transaction History" may live on different services, or even different databases.

This distribution creates critical failure points:

What if the network fails after money is deducted from User A but before it's credited to User B?

What if the user retries a "failed" request and sends money twice (a non-idempotent system)?

What if a user "double-spends" by firing two requests at the exact same millisecond (a race condition)?

2.2. The Solution & Value

"Momentum" directly solves these problems by implementing patterns used by real-world companies like Stripe, PayPal, and Square. It provides a reliable, auditable, and scalable platform for managing digital payments. For a developer, this project serves as a "Master Class" in building systems that cannot lose data or money—the single most valuable skill in the Fintech and E-commerce sectors.

3. Target Users (Actors)

Consumer (End-User): The primary user. Can top-up their wallet, send/receive funds, and view their balance and transaction history.

Internal Auditor (Support): A back-office persona who can view the full ledger, track the status of a failed transaction, and (in a future version) issue refunds.

4. High-Level Architecture

The system will be composed of several independent, containerized microservices that communicate asynchronously.

Service A: Wallet Service (The Source of Truth)

Responsibility: Manages the core ledger and balances. This is the only service allowed to directly debit/credit a user's account.

Database: PostgreSQL.

Service B: Transfer Service (The Orchestrator)

Responsibility: Manages the lifecycle of a payment. It coordinates the steps of a transfer by communicating with other services. It implements the SAGA pattern.

Database: (None, or a simple state DB).

Service C: Notification Service (The "Fire-and-Forget" Service)

Responsibility: Listens for TransferCompleted or TransferFailed events and sends an email/SMS to the user. (Can be mocked by logging).

Service D: API Gateway

Responsibility: The single entry point for all client requests. Manages routing, authentication (mocked), and rate-limiting.

Middleware:

Message Queue (RabbitMQ): Used for asynchronous communication between services (e.g., Transfer Service -> Wallet Service).

Cache & Lock (Redis): Used for distributed locking (to prevent double-spend) and caching hot data (like user balances for quick display).

5. Functional Requirements (FRs)

FR-1 (Wallet): A user must be able to top-up their wallet (i.e., add funds).

FR-2 (Wallet): A user must be able to see their current wallet balance.

FR-3 (Transfer): A user must be able to initiate a transfer of funds to another user.

FR-4 (Transfer): A transfer must fail if the user has insufficient funds.

FR-5 (History): A user must be able to view a chronological history of all their transactions (top-ups, sends, receives).

6. Non-Functional Requirements (NFRs) - The "Enterprise" Requirements

This is the core of the project.

NFR-1 (Consistency): The system must guarantee "Eventual Atomicity." A transfer must either fully complete (debit from A, credit to B, history logged) or fully fail (no state change), even if services crash mid-process.

Implementation: SAGA Orchestration Pattern.

NFR-2 (Idempotency): All fund-moving API endpoints (/transfer, /topup) must be idempotent. A client retrying the exact same request due to a network error must not result in a double-charge or double-transfer.

Implementation: Idempotency-Key (UUID) in the request header, stored and checked in Redis.

NFR-3 (Concurrency): The system must prevent a "double-spend" race condition where a user with $50 attempts to send two $50 transfers simultaneously.

Implementation: Redis-based Distributed Lock (e.g., Redlock) on the userId during a transfer operation.

NFR-4 (Durability): All settled transactions are permanent. The core ledger must be an immutable, append-only log.

Implementation: PostgreSQL ledger table.

NF-5 (Performance): P95 (95th percentile) latency for a transfer request acknowledgement must be < 500ms. P95 latency for a balance check must be < 100ms.

Implementation: Asynchronous processing. Balance checks are served from a Redis cache.

NFR-6 (Availability): The system must be horizontally scalable. A failure in the Notification Service (non-critical) must have zero impact on the ability to process payments.

Implementation: Decoupled services (Docker) and asynchronous messaging (RabbitMQ).

NFR-7 (Observability): The system must be "debuggable." We need to trace a single transaction as it flows through all microservices.

Implementation: Spring Cloud Sleuth / OpenTelemetry for distributed tracing (e.g., Jaeger) and Prometheus/Grafana for metrics.

7. Core Technology Stack

Framework: Spring Boot 3+ (Java 17+)

Microservices: Spring Cloud (Gateway, OpenTelemetry)

Database (Ledger): PostgreSQL

Cache & Locking: Redis

Message Broker: RabbitMQ

Containerization: Docker & Docker Compose

Observability: Prometheus, Grafana, Jaeger

8. Key Challenges & Interview Talking Points

SAGA Pattern Implementation: Correctly implementing the orchestration logic, including the "Compensating Transactions" (rollbacks) if a step in the chain fails.

Distributed Lock Efficiency: Choosing and implementing the right locking strategy. A naive lock on all transfers would kill performance. A fine-grained lock (per-user) is required.

Data Modeling for Money: Designing the PostgreSQL schema to never use floating-point numbers. All monetary values will be stored as BIGINT representing cents (e.g., $10.50 is stored as 1050).

Integration Testing: How do you test a failed payment? You will need to build integration tests that mock a failure (e.g., the Wallet Service returns "Insufficient Funds") and verify that the Transfer Service correctly handles the failure event.

9. Phased Rollout Plan (MVP)

Phase 1: The Core Ledger (Monolith-First)

Build one Spring Boot service (Wallet Service).

Define the PostgreSQL schema for users and ledger_entries.

Implement "Top-Up" and "Get Balance" REST APIs.

Goal: Get the core database model right.

Phase 2: The Distributed Transfer

Add the Transfer Service and RabbitMQ.

Implement the SAGA. The /transfer endpoint now talks to the Transfer Service, which sends a message to the Wallet Service.

Goal: Achieve asynchronous, multi-step transaction processing.

Phase 3: Hardening (The NFRs)

Add Redis.

Implement NFR-2 (Idempotency) on the Transfer Service.

Implement NFR-3 (Distributed Locking) on the Wallet Service.

Goal: Make the system reliable and safe from race conditions.

Phase 4: Observability

Integrate Jaeger and Prometheus.

Add dashboards to Grafana.

Goal: Be able to "see" the system working and trace a failed transaction.
