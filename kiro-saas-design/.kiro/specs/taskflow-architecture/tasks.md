# Implementation Plan: TaskFlow Multi-Tenant SaaS Architecture

## Overview

This implementation plan covers the complete TaskFlow multi-tenant B2B SaaS platform architecture using Java (Spring Boot ecosystem). The plan is organized into progressive stages: foundational infrastructure first, then core services, async processing, integrations, and finally cross-cutting concerns. Each task builds on previous tasks with no orphaned code.

## Tasks

- [ ] 1. Set up project structure, shared libraries, and core interfaces
  - [ ] 1.1 Create multi-module Maven/Gradle project structure
    - Create root project with modules: `common`, `api-gateway`, `auth-service`, `core-api`, `tenant-service`, `billing-service`, `notification-service`, `search-service`, `worker-fleet`
    - Define shared dependency versions (Spring Boot 3.x, Spring Security, Stripe SDK, OpenTelemetry, Redis client, PostgreSQL driver)
    - Configure common build plugins (spotbugs, checkstyle, jacoco)
    - _Requirements: 14.1, 14.7_

  - [ ] 1.2 Define core domain models and enumerations in the `common` module
    - Create `PlanType` enum (FREE, PRO, ENTERPRISE)
    - Create `TenantStatus` enum (ACTIVE, SUSPENDED, CANCELLED)
    - Create `SubscriptionStatus` enum (INCOMPLETE, ACTIVE, PAST_DUE, INCOMPLETE_EXPIRED, CANCELLED)
    - Create `ProcessingResult` enum (SUCCESS, RETRY, DEAD_LETTER)
    - Create `QueueMessage` record with fields: id, eventType, payload, correlationId, publishedAt, retryCount, maxRetries
    - _Requirements: 8.1, 9.1_

  - [ ] 1.3 Define shared interfaces and DTOs
    - Create `TenantScopedEntity` base class with non-nullable `tenantId` field
    - Create `AuthResult` record (accessToken, refreshToken, expiresIn, tokenType)
    - Create `TokenClaims` record (userId, tenantId, roles, permissions, issuedAt, expiresAt)
    - Create common exception hierarchy: `SecurityError`, `AuthError`, `InvalidTransitionError`, `CrossTenantReferenceError`
    - _Requirements: 1.1, 2.7, 8.2_

  - [ ] 1.4 Configure database migrations and shared PostgreSQL schema
    - Set up Flyway or Liquibase for migration management
    - Create `tenants` table with id, name, slug, plan, status, stripe_customer_id, seat_limit, created_at, updated_at
    - Create `users` table with id, tenant_id (NOT NULL FK), email, password_hash, name, role, sso_provider, sso_subject_id, created_at
    - Create `projects` table with id, tenant_id (NOT NULL FK), name, description, status, owner_id, created_at, updated_at
    - Create `tasks` table with id, tenant_id (NOT NULL FK), project_id, title, description, status, priority, assignee_id, due_date, created_at, updated_at
    - Create composite indexes on (tenant_id, ...) columns for all tables
    - _Requirements: 1.1, 15.7_

  - [ ] 1.5 Configure Redis connection and caching infrastructure
    - Set up Spring Data Redis with Lettuce client
    - Create `CacheService` with get/set/invalidate methods and configurable TTLs
    - Implement Redis health check with 200ms timeout fallback logic
    - _Requirements: 15.1, 15.3, 15.5, 15.8_

- [ ] 2. Implement multi-tenant data isolation layer
  - [ ] 2.1 Implement tenant-scoped query enforcement at the repository level
    - Create `TenantContextHolder` (ThreadLocal-based) to store current tenant_id from JWT
    - Create `TenantInterceptor` that extracts tenant_id from JWT claims and populates TenantContextHolder
    - Create `TenantScopedRepository` base class that automatically adds `WHERE tenant_id = ?` to all queries
    - Implement validation that rejects requests with null/empty tenant_id (HTTP 401)
    - _Requirements: 1.2, 1.3, 7.7_

  - [ ] 2.2 Implement cross-tenant access prevention
    - Add request body/path parameter tenant_id mismatch check returning HTTP 403
    - Add result-set verification that all returned rows match authenticated tenant_id
    - Implement foreign key cross-tenant reference validation (HTTP 400 on violation)
    - Log security violations with tenant_id, user_id, and attempted resource
    - _Requirements: 1.4, 1.5, 1.6_

  - [ ]* 2.3 Write property test for tenant data isolation
    - **Property 1: Tenant Data Isolation**
    - Generate arbitrary tenant_id values and queries; verify every result row has matching tenant_id
    - Verify null/empty tenant_id always produces SecurityError before any DB call
    - **Validates: Requirements 1.2, 1.3, 1.4, 1.5**

  - [ ]* 2.4 Write unit tests for tenant isolation edge cases
    - Test cross-tenant FK reference detection
    - Test path parameter tenant_id mismatch
    - Test Worker Fleet tenant_id extraction from job payload
    - _Requirements: 1.6, 1.7_

- [ ] 3. Implement authentication service (Auth Service)
  - [ ] 3.1 Implement email/password authentication with JWT issuance
    - Create `AuthController` with POST `/auth/login` endpoint
    - Implement bcrypt password verification
    - Implement JWT generation using RS256 (2048-bit RSA key pair) with 15-min access_token TTL and 7-day refresh_token TTL
    - Include tenant_id, user_id, roles, permissions in JWT claims
    - Return generic "invalid credentials" error without revealing which field is wrong
    - _Requirements: 2.1, 2.3, 2.7, 2.9_

  - [ ] 3.2 Implement JWT validation with revocation checking
    - Create `JwtValidationService` that verifies RS256 signature, checks expiration, and checks Redis revocation list
    - Implement graceful degradation: skip revocation check if Redis unavailable, rely on signature + expiration only
    - Return specific AuthError reasons (invalid signature, expired, revoked)
    - Cache JWT public keys in-memory with 1-hour TTL; invalidate on key rotation event
    - _Requirements: 2.4, 2.5, 2.8, 15.1, 15.2_

  - [ ] 3.3 Implement refresh token rotation
    - Create POST `/auth/refresh` endpoint
    - Issue new access_token, invalidate previous refresh_token on use
    - Return AuthError for expired, already-invalidated, or not-found refresh tokens
    - _Requirements: 2.6, 2.10_

  - [ ] 3.4 Implement SAML SSO integration
    - Create GET `/auth/sso` endpoint that generates SAML AuthnRequest and redirects to configured IdP
    - Implement SAML assertion validation: signature, audience restriction, NotOnOrAfter expiry, replay detection (assertion ID)
    - Implement user lookup-or-create by SAML NameID mapping to email
    - Return category-specific errors (signature, audience, expiry, replay)
    - Implement 120-second SSO flow timeout
    - Return error if tenant has no configured IdP
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ]* 3.5 Write property test for JWT validation determinism
    - **Property 9: JWT Validation Determinism**
    - Generate arbitrary JWT strings; verify validateToken always returns the same result for the same input
    - Valid tokens produce claims; invalid/expired/revoked tokens produce AuthError
    - **Validates: Requirements 2.4, 2.5, 2.7**

  - [ ]* 3.6 Write unit tests for auth service
    - Test expired token rejection
    - Test invalid signature rejection
    - Test revoked token rejection
    - Test SAML assertion validation failures (each category)
    - Test refresh token rotation and invalidation
    - _Requirements: 2.5, 2.10, 3.4_

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement billing service with Stripe integration
  - [ ] 5.1 Implement subscription creation with Stripe
    - Create `BillingController` with POST `/subscriptions` endpoint
    - Implement Stripe Customer and Subscription creation; return client_secret with status "incomplete"
    - Store monetary amounts as integer cents
    - Ensure no local records created if Stripe API call fails (atomic operation)
    - Handle Stripe 5xx/timeout: return 503 with Retry-After header, no partial records
    - _Requirements: 4.1, 4.6, 4.7, 16.1_

  - [ ] 5.2 Implement webhook signature verification
    - Create POST `/webhooks/stripe` endpoint
    - Verify HMAC-SHA256 signature using constant-time comparison
    - Reject invalid/missing signatures with 400 Bad Request; log warning with source IP
    - _Requirements: 5.1, 5.2, 4.8_

  - [ ] 5.3 Implement idempotent webhook processing
    - Create `idempotency` table (idempotency_key UNIQUE, event_type, processed_at, response_status)
    - Check event.id against idempotency table before processing
    - Return 200 OK for already-processed events without state changes
    - Insert idempotency record + execute state changes in single atomic transaction
    - Rollback all changes (including idempotency record) on transaction failure; return 500
    - Retain idempotency records for minimum 72 hours
    - Complete all processing within 15 seconds
    - _Requirements: 5.3, 5.4, 5.5, 5.6, 5.7, 5.9, 5.10_

  - [ ] 5.4 Implement subscription state machine
    - Create `SubscriptionStateMachine` class with valid_transitions map
    - Implement deterministic state transitions: (incomplete→active), (incomplete→incomplete_expired), (active→past_due), (active→active with cancel_at_period_end), (active→cancelled), (past_due→active), (past_due→cancelled), (cancelled→incomplete)
    - Raise InvalidTransitionError for invalid transitions; preserve current state; log warning
    - Use optimistic locking (version column) to handle concurrent webhook transitions
    - _Requirements: 8.1, 8.2, 8.3, 8.6_

  - [ ] 5.5 Implement payment event handlers
    - Implement `processPaymentSucceeded`: update subscription to "active", update tenant plan, record invoice in single transaction; publish subscription.activated event
    - Implement `processPaymentFailed`: transition active→past_due; notify tenant admin via Notification_Service within 60 seconds
    - Implement max retries exceeded: transition past_due→cancelled; revert tenant plan to FREE
    - _Requirements: 4.2, 4.3, 4.4, 4.5, 8.4, 8.5_

  - [ ]* 5.6 Write property test for webhook idempotency
    - **Property 2: Webhook Idempotency**
    - Process same webhook event N times; verify final DB state identical to single processing
    - **Validates: Requirements 5.3, 5.4, 5.7**

  - [ ]* 5.7 Write property test for transaction atomicity
    - **Property 3: Transaction Atomicity (No Partial State on Failure)**
    - Inject failures during transaction; verify DB state equals pre-processing state
    - **Validates: Requirements 5.5, 5.6, 4.6, 16.1**

  - [ ]* 5.8 Write property test for subscription state machine
    - **Property 4: Subscription State Machine Determinism and Integrity**
    - Generate arbitrary (state, event) pairs; verify deterministic output and InvalidTransitionError for invalid pairs
    - **Validates: Requirements 8.1, 8.2, 8.3, 4.4, 4.5**

- [ ] 6. Implement outbox pattern for queue reliability
  - [ ] 6.1 Create outbox table and write-to-outbox logic
    - Create `outbox` table (id, event_type, payload, destination_queue, status, retry_count, next_retry_at, created_at, published_at)
    - Implement fallback: if queue publish fails after DB commit, write to outbox with status "pending"
    - _Requirements: 6.1_

  - [ ] 6.2 Implement outbox processor scheduler
    - Create scheduled task running every 30 seconds
    - Read up to 50 pending outbox records ordered by creation time
    - On publish success: mark as "published" with published_at timestamp
    - On publish failure: increment retry_count, calculate next_retry_at with exponential backoff (base 5s, max 5 min)
    - After 10 retries: move to "dead_letter" status; send alert within 60 seconds
    - _Requirements: 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 6.3 Write property test for outbox eventual delivery
    - **Property 7: Outbox Eventual Delivery with Exponential Backoff**
    - Verify pending events eventually reach "published" or "dead_letter" status; verify exponential backoff intervals
    - **Validates: Requirements 6.1, 6.3, 6.4, 6.5**

- [ ] 7. Implement async job processing (Worker Fleet)
  - [ ] 7.1 Implement message consumer with retry semantics
    - Create `WorkerConsumer` that reads from SQS/Kafka
    - On success: ACK message to remove from queue
    - On transient failure (network timeout, DB unavailable, upstream 503): NACK with exponential backoff (1s, 4s, 16s)
    - On permanent failure (malformed payload, unknown event type, validation error): route directly to DLQ without retry
    - Implement 30-second visibility timeout (message treated as failed if not ACK/NACK within 30s)
    - _Requirements: 7.1, 7.2, 7.6, 7.7_

  - [ ] 7.2 Implement dead-letter queue handling and alerting
    - Configure DLQ for messages exceeding 3 retries
    - Emit alert to observability system within 60 seconds of DLQ placement (message ID, event type, failure reason)
    - Include correlation_id on every published message for end-to-end tracing
    - _Requirements: 7.3, 7.4, 7.5, 16.4, 16.5_

  - [ ] 7.3 Implement subscription.activated worker handler
    - Consume subscription.activated events
    - Provision plan features and raise tenant limits within 30 seconds of consumption
    - Trigger notification fan-out (receipt email + Slack)
    - Apply tenant_id from job payload for tenant-scoped operations
    - _Requirements: 9.2, 1.7_

  - [ ]* 7.4 Write property test for message delivery guarantee
    - **Property 5: Message Delivery Guarantee (No Silent Loss)**
    - Verify every message reaches terminal state (processed or DLQ); verify retry_count never exceeds MAX_RETRIES
    - **Validates: Requirements 7.1, 7.2, 7.3, 16.4**

- [ ] 8. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. Implement tenant service with plan enforcement
  - [ ] 9.1 Implement tenant provisioning
    - Create `TenantController` with POST endpoint for provisioning
    - Create tenant with FREE plan, seat_limit=5, status="active"
    - Generate URL-safe slug (lowercase, alphanumeric with hyphens)
    - _Requirements: 9.1_

  - [ ] 9.2 Implement plan enforcement and feature access checks
    - Create `PlanFeatureService` with plan-to-feature mapping
    - Implement `checkFeatureAccess(tenantId, feature)` returning boolean within 50ms (use Redis-cached plan data with 5-min TTL)
    - Implement seat limit enforcement: reject invitations (HTTP 403) when active users = seat_limit
    - _Requirements: 9.3, 9.4, 15.3_

  - [ ] 9.3 Implement tenant suspension on subscription cancellation
    - Listen for subscription cancelled events
    - Set tenant status to "suspended" within 60 seconds
    - Enforce read-only access: prevent creation of projects, tasks, and invitations while preserving read access
    - _Requirements: 9.5, 9.6_

  - [ ]* 9.4 Write property test for seat limit enforcement
    - **Property 10: Seat Limit Enforcement**
    - Generate tenants at seat_limit boundary; verify invitations rejected at limit, accepted below limit
    - **Validates: Requirements 9.3**

- [ ] 10. Implement search service with OpenSearch
  - [ ] 10.1 Implement search index event publishing
    - On task/project create or update in Core_API, publish index event (document ID, tenant_id, title, description, comments) to message queue
    - _Requirements: 10.1_

  - [ ] 10.2 Implement search index worker consumer
    - Consume index events and index documents in OpenSearch with tenant_id within 30 seconds
    - Route to DLQ after 3 failed retry attempts; log failure
    - Implement tenant reindexing on demand with completion status reporting
    - _Requirements: 10.2, 10.5, 10.6_

  - [ ] 10.3 Implement search query endpoint
    - Create GET `/search` endpoint scoped to authenticated user's tenant_id
    - Return max 50 results per page ranked by relevance
    - Return results within 2 seconds under normal conditions
    - Degrade to database LIKE queries if OpenSearch unavailable for >5 seconds; indicate incomplete results
    - _Requirements: 10.3, 10.4, 10.7_

  - [ ]* 10.4 Write unit tests for search service
    - Test tenant scoping on search queries
    - Test OpenSearch fallback behavior
    - Test index event publishing on task/project mutations
    - _Requirements: 10.3, 10.4_

- [ ] 11. Implement notification service
  - [ ] 11.1 Implement email notification via SendGrid
    - Create `NotificationService` with SendGrid integration
    - Send payment receipt email to tenant billing contact within 60 seconds of subscription activation
    - Queue failed emails for retry: max 5 attempts over 24-hour retention
    - _Requirements: 11.1, 11.3_

  - [ ] 11.2 Implement Slack webhook notification
    - Implement Slack channel delivery with 10-second connection timeout
    - After 2 failed retries: skip Slack, log failure, continue to remaining channels
    - Skip channels not enabled for tenant without error
    - _Requirements: 11.4, 11.5_

  - [ ] 11.3 Implement notification fan-out orchestration
    - Fan out to all enabled channels per tenant (email, Slack)
    - Complete delivery attempts to all channels within 30 seconds per channel
    - Ensure one channel failure does not block other channels
    - _Requirements: 11.2_

  - [ ]* 11.4 Write property test for notification fan-out completeness
    - **Property 12: Notification Fan-Out Completeness**
    - Verify delivery attempted to all configured channels; verify Slack failure doesn't block email
    - **Validates: Requirements 11.2, 11.4**

- [ ] 12. Implement API gateway
  - [ ] 12.1 Implement request routing and TLS termination
    - Create API Gateway with path-prefix routing table (Auth_Service, Core_API, Billing_Service)
    - Terminate TLS for all incoming connections
    - Return 404 for unmatched routes
    - Strip hop-by-hop headers and non-allowlisted headers before forwarding
    - _Requirements: 13.1, 13.2, 13.4, 13.6_

  - [ ] 12.2 Implement JWT validation delegation
    - Delegate JWT validation to Auth_Service for all requests except public endpoints (health checks, OAuth token endpoints, Stripe webhook callbacks)
    - Propagate Auth_Service 401 within 200ms
    - Return 503 if Auth_Service doesn't respond within 3 seconds
    - _Requirements: 13.3, 13.5, 13.7_

  - [ ] 12.3 Implement per-tenant and per-IP rate limiting
    - Implement sliding 60-second window rate limits by plan tier: free=100, Pro=1000, Enterprise=10000 requests
    - Implement per-IP rate limit on auth endpoints: 10 requests per 60-second window
    - Return 429 with Retry-After header when exceeded
    - Ensure rate-limited tenants don't degrade other tenants' response times (within 10% of median)
    - If rate-limiting subsystem unavailable: serve without enforcement, emit alert within 5 seconds
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 13.8_

  - [ ]* 12.4 Write property test for rate limit tenant isolation
    - **Property 8: Rate Limit Tenant Isolation**
    - Verify one tenant exceeding limits doesn't affect another tenant's response times
    - Verify graduated limits (free < pro < enterprise)
    - **Validates: Requirements 12.1, 12.3, 12.4, 12.5**

- [ ] 13. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 14. Implement observability and structured logging
  - [ ] 14.1 Implement structured JSON logging across all services
    - Configure logback/log4j2 with JSON layout
    - Include consistent fields: timestamp (ISO 8601), service, tenant_id, user_id, correlation_id, level (DEBUG/INFO/WARN/ERROR/FATAL), message
    - Implement PII redaction filter (email, personal names, phone numbers) — log only system identifiers
    - _Requirements: 14.1, 14.3_

  - [ ] 14.2 Implement correlation ID generation and propagation
    - Generate UUID v4 correlation_id at API Gateway for each incoming request
    - Propagate through all downstream sync calls via request headers
    - Propagate through async messages via correlation_id field in QueueMessage
    - _Requirements: 14.2, 7.5_

  - [ ] 14.3 Implement OpenTelemetry distributed tracing
    - Add OpenTelemetry SDK and auto-instrumentation to all services
    - Configure W3C Trace Context header propagation
    - Create spans at: API Gateway entry, service-to-service calls, DB queries, queue publish/consume, external API calls
    - _Requirements: 14.7_

  - [ ] 14.4 Implement alerting rules
    - Alert when p99 response time exceeds 500ms over 5-minute window (within 60s)
    - Alert when DLQ size exceeds 0 (within 60s)
    - Alert when 5xx error rate exceeds 1% over 5-minute window with minimum 100 requests (within 60s)
    - Configure log retention: 30 days searchable, 90 days archive
    - _Requirements: 14.4, 14.5, 14.6, 14.8_

  - [ ]* 14.5 Write unit tests for observability components
    - Test correlation_id propagation through sync and async paths
    - Test PII redaction in log output
    - Test structured log format compliance
    - _Requirements: 14.1, 14.2, 14.3_

- [ ] 15. Implement caching strategy with invalidation
  - [ ] 15.1 Implement cache-aside pattern for Core API
    - Cache tenant plan/limit data in Redis with 5-minute TTL
    - Cache project/task listings in Redis with 30-second TTL
    - Invalidate plan cache within 2 seconds on plan change event
    - Invalidate listing cache on create/update/delete before returning response
    - Fall back to PostgreSQL on Redis failure (200ms timeout)
    - Rely on TTL-based expiration if invalidation event delivery fails
    - _Requirements: 15.3, 15.4, 15.5, 15.6, 15.8, 15.9_

  - [ ]* 15.2 Write property test for cache invalidation on mutation
    - **Property 11: Cache Invalidation on Mutation**
    - Verify every write mutation invalidates corresponding cache entries
    - Verify subsequent reads reflect updated state
    - **Validates: Requirements 15.2, 15.3**

- [ ] 16. Implement error handling and resilience patterns
  - [ ] 16.1 Implement circuit breaker and resilience for external services
    - Add resilience4j circuit breaker for Stripe API calls (10s timeout, 503 + Retry-After on failure)
    - Implement token-expiry-during-operation handling: complete in-progress ops if token was valid at request start (within 60s window)
    - Ensure every async message reaches terminal state within 5 minutes of publication
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

  - [ ]* 16.2 Write unit tests for error handling scenarios
    - Test Stripe unavailability returns 503 with no partial records
    - Test worker retry backoff intervals (1s, 4s, 16s)
    - Test token expiry during long operation completion
    - _Requirements: 16.1, 16.2, 16.3_

- [ ] 17. Implement webhook signature verification security
  - [ ]* 17.1 Write property test for webhook signature verification
    - **Property 6: Webhook Signature Verification**
    - Generate arbitrary payloads; verify valid signatures pass, tampered payloads rejected with 400 before any state changes
    - Verify constant-time comparison is used
    - **Validates: Requirements 5.1, 5.2**

- [ ] 18. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- Implementation uses Java with Spring Boot 3.x, Spring Security, Spring Data JPA, and Resilience4j
- All services are stateless and horizontally scalable
- PostgreSQL is the system of record; Redis provides caching with graceful degradation

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3"] },
    { "id": 2, "tasks": ["1.4", "1.5"] },
    { "id": 3, "tasks": ["2.1", "3.1"] },
    { "id": 4, "tasks": ["2.2", "3.2", "3.3", "3.4"] },
    { "id": 5, "tasks": ["2.3", "2.4", "3.5", "3.6"] },
    { "id": 6, "tasks": ["5.1", "5.2"] },
    { "id": 7, "tasks": ["5.3", "5.4"] },
    { "id": 8, "tasks": ["5.5", "6.1"] },
    { "id": 9, "tasks": ["5.6", "5.7", "5.8", "6.2"] },
    { "id": 10, "tasks": ["6.3", "7.1"] },
    { "id": 11, "tasks": ["7.2", "7.3"] },
    { "id": 12, "tasks": ["7.4", "9.1"] },
    { "id": 13, "tasks": ["9.2", "9.3"] },
    { "id": 14, "tasks": ["9.4", "10.1"] },
    { "id": 15, "tasks": ["10.2", "10.3", "11.1"] },
    { "id": 16, "tasks": ["10.4", "11.2", "11.3"] },
    { "id": 17, "tasks": ["11.4", "12.1"] },
    { "id": 18, "tasks": ["12.2", "12.3"] },
    { "id": 19, "tasks": ["12.4", "14.1"] },
    { "id": 20, "tasks": ["14.2", "14.3"] },
    { "id": 21, "tasks": ["14.4", "14.5", "15.1"] },
    { "id": 22, "tasks": ["15.2", "16.1"] },
    { "id": 23, "tasks": ["16.2", "17.1"] }
  ]
}
```
