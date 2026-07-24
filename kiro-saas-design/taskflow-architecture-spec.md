# TaskFlow — Multi-Tenant SaaS Architecture

**Purpose:** Reference architecture and core request flow for a multi-tenant project-management SaaS product ("TaskFlow"), provided as design-first input for spec generation.

## System Overview

TaskFlow is a multi-tenant B2B SaaS application (comparable in scope to Asana/Linear). Tenants sign up, invite team members via SSO or email/password, manage projects and tasks, and upgrade to paid plans billed through Stripe. The system separates synchronous request handling (API layer) from asynchronous work (billing webhooks, email, search indexing) via a message queue, and isolates tenant data at the row level in a shared PostgreSQL cluster.

## Architecture Diagram

![Architecture Diagram](architecture.png)

```mermaid
flowchart TB

    subgraph CLIENTS["Client Layer"]
        WEB["Web App (React SPA)"]
        MOBILE["Mobile App (iOS/Android)"]
    end

    CDN["CDN / Static Assets<br/>(CloudFront + S3)"]

    subgraph EDGE["Edge / Ingress"]
        LB["Load Balancer (ALB)"]
        GW["API Gateway<br/>rate limiting, routing, TLS termination"]
    end

    subgraph AUTHZ["Identity & Access"]
        AUTH["Auth Service<br/>OAuth2 / OIDC, JWT issuance"]
        SSO["External IdP<br/>(Okta / Google Workspace SAML)"]
    end

    subgraph CORE["Core Application Services"]
        API["Core API Service<br/>REST/GraphQL, business logic"]
        TENANT["Tenant Service<br/>tenant provisioning, plan limits"]
        NOTIF["Notification Service"]
        BILLING["Billing Service<br/>subscriptions, invoices"]
        SEARCHSVC["Search Service"]
    end

    subgraph ASYNC["Async / Background Processing"]
        QUEUE["Message Queue<br/>(SQS / Kafka)"]
        WORKER["Worker Fleet<br/>(Sidekiq / Celery)"]
        SCHED["Scheduler<br/>(cron jobs, reminders)"]
    end

    subgraph DATA["Data Stores"]
        PG[("Primary DB<br/>PostgreSQL, row-level multi-tenancy")]
        REDIS[("Cache<br/>Redis")]
        ES[("Search Index<br/>OpenSearch")]
        S3[("Object Storage<br/>S3 - uploads, exports")]
    end

    subgraph THIRDPARTY["Third-Party Integrations"]
        STRIPE["Stripe<br/>payments & webhooks"]
        EMAIL["SendGrid<br/>transactional email"]
        SLACK["Slack<br/>webhook notifications"]
    end

    subgraph OBS["Observability & Ops"]
        LOGS["Centralized Logging<br/>(CloudWatch / Datadog)"]
        METRICS["Metrics & Alerting"]
        SENTRY["Error Tracking<br/>(Sentry)"]
    end

    WEB --> CDN
    WEB --> LB
    MOBILE --> LB
    LB --> GW
    GW --> AUTH
    AUTH --> SSO
    AUTH --> PG
    GW --> API
    API --> TENANT
    API --> BILLING
    API --> SEARCHSVC
    API --> PG
    API --> REDIS
    API --> S3
    SEARCHSVC --> ES
    API --> QUEUE
    BILLING --> QUEUE
    QUEUE --> WORKER
    WORKER --> NOTIF
    WORKER --> PG
    SCHED --> QUEUE
    NOTIF --> EMAIL
    NOTIF --> SLACK
    BILLING <--> STRIPE
    API -.-> LOGS
    WORKER -.-> LOGS
    AUTH -.-> LOGS
    API -.-> METRICS
    WORKER -.-> METRICS
    API -.-> SENTRY
    WORKER -.-> SENTRY
```

## Components

| Component | Responsibility | Notes |
|---|---|---|
| Web App / Mobile App | User-facing clients | React SPA + native mobile clients share the same API |
| CDN | Serves static assets | CloudFront in front of S3 |
| Load Balancer | TLS termination, traffic distribution | Layer 7 ALB |
| API Gateway | Routing, rate limiting, request validation | Single entry point for all API traffic |
| Auth Service | Issues/validates JWTs, manages OAuth2/OIDC flows | Delegates enterprise SSO to external IdP (SAML) |
| Core API Service | Primary business logic (projects, tasks, teams) | REST/GraphQL; stateless, horizontally scaled |
| Tenant Service | Tenant provisioning, plan/seat limits | Enforces multi-tenancy boundaries |
| Billing Service | Subscription lifecycle, invoicing | Owns all Stripe interaction; source of truth for plan state |
| Search Service | Full-text search over tasks/projects | Backed by OpenSearch, updated via async indexing |
| Notification Service | Fan-out of user-facing notifications | Emits to email and Slack channels |
| Message Queue | Decouples sync API from async workers | SQS or Kafka depending on throughput needs |
| Worker Fleet | Executes async jobs (indexing, provisioning, email triggers) | Horizontally scaled consumers |
| Scheduler | Cron-based jobs (reminders, trial expirations) | Publishes to the same queue as other async work |
| Primary DB | System of record | PostgreSQL, tenant_id on every row, indexed |
| Cache | Read-through cache for hot paths | Redis |
| Object Storage | File uploads, exports | S3 |
| Stripe / SendGrid / Slack | Payments, email, chat notifications | Third-party, webhook-driven where applicable |
| Logging / Metrics / Error Tracking | Cross-cutting observability | Every service instruments logs, metrics, and error traces |

## Key Flow: New Paid Subscription

This sequence covers the highest-risk flow in the system: a user upgrading to a paid plan, including token refresh, Stripe checkout, webhook-driven activation, idempotency handling, and async fan-out to provisioning and email.

![Sequence Diagram](sequence.png)

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant WEB as Web App
    participant GW as API Gateway
    participant AUTH as Auth Service
    participant API as Core API
    participant BILL as Billing Service
    participant STRIPE as Stripe
    participant Q as Message Queue
    participant WORK as Worker
    participant DB as Primary DB
    participant NOTIF as Notification Service
    participant EMAIL as SendGrid

    U->>WEB: Click "Upgrade to Pro"
    WEB->>GW: POST /subscriptions (JWT)
    GW->>AUTH: Validate JWT
    AUTH-->>GW: 200 OK (claims: tenant_id, user_id)

    alt Token expired
        AUTH-->>GW: 401 Unauthorized
        GW-->>WEB: 401 Unauthorized
        WEB->>AUTH: POST /oauth/token (refresh_token)
        AUTH-->>WEB: New access token
        WEB->>GW: Retry POST /subscriptions (new JWT)
    end

    GW->>API: POST /subscriptions
    API->>BILL: createSubscription(tenant_id, plan_id)
    BILL->>STRIPE: Create Customer + Subscription
    STRIPE-->>BILL: subscription.id, client_secret (status: incomplete)
    BILL-->>API: 202 Accepted (pending payment confirmation)
    API-->>GW: 202 Accepted
    GW-->>WEB: 202 Accepted
    WEB-->>U: Show payment confirmation (Stripe Elements)

    U->>WEB: Confirm card payment
    WEB->>STRIPE: confirmCardPayment(client_secret)
    STRIPE-->>WEB: payment succeeded

    Note over STRIPE,BILL: Async webhook, independent of user's browser session
    STRIPE->>BILL: Webhook: invoice.payment_succeeded
    BILL->>BILL: Verify webhook signature

    alt Signature invalid
        BILL-->>STRIPE: 400 Bad Request (dropped)
    else Signature valid
        BILL->>DB: Check idempotency key (event.id)
        alt Already processed
            BILL-->>STRIPE: 200 OK (no-op)
        else Not yet processed
            BILL->>DB: Update tenant plan = "pro", status = "active"
            BILL->>Q: Publish event: subscription.activated
            BILL-->>STRIPE: 200 OK
            Q->>WORK: Consume subscription.activated
            WORK->>DB: Provision plan features / raise limits
            WORK->>NOTIF: sendReceiptEmail(tenant_id)
            NOTIF->>EMAIL: Send "Payment receipt" email
            EMAIL-->>NOTIF: 202 Accepted
        end
    end

    par Real-time UI update
        WEB->>GW: Poll GET /subscriptions/status (or WebSocket push)
        GW->>API: GET /subscriptions/status
        API->>DB: Read subscription status
        DB-->>API: status = "active"
        API-->>WEB: status = "active"
        WEB-->>U: Show "Welcome to Pro" confirmation
    end
```

## Design Considerations for Spec Generation

Multi-tenancy is enforced at the row level (`tenant_id` on every table) rather than schema- or database-per-tenant, trading strict isolation for lower operational overhead at moderate scale. Billing state changes are driven by Stripe webhooks rather than client confirmation, since the client's `confirmCardPayment` success does not guarantee the subscription is actually active — the webhook with signature verification and an idempotency check (keyed on `event.id`) is the single source of truth for plan activation. Any spec derived from this diagram should treat the webhook handler as the critical path and specify retry/backoff behavior for Stripe's webhook redelivery, plus dead-letter handling if the queue publish step fails after the DB write succeeds (a partial-failure case not shown in the happy-path diagram above).

## Assumptions and Open Questions

This diagram assumes a single-region deployment, synchronous REST between the gateway and Core API, and Stripe as the sole payment processor. It does not specify SLAs, exact scaling thresholds, disaster recovery/backup strategy, or a data residency model for tenants with regional compliance requirements — these should be clarified before Kiro (or any downstream spec) treats this as final.
