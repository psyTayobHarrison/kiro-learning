---
inclusion: fileMatch
fileMatchPattern: ['**/model/**/*.java', '**/repository/**/*.java', '**/resources/application*']
---

# Database Conventions

## Database

- PostgreSQL running locally on `localhost:5432`, database name `budgettracker`.
- Test profile uses H2 in-memory database (`create-drop` DDL mode).
- Connection config lives in `application.properties` (or YAML); never hardcode credentials in Java source.

## Naming

- **Table names:** singular form, lowercase (e.g., `expense`, not `expenses`).
- **Column names:** `snake_case` in the database, mapped to `camelCase` Java fields.
- Do NOT rely on Hibernate's default implicit naming strategy. Always use explicit `@Column(name = "...")` when the Java field name differs from the desired column name (e.g., `createdAt` -> `created_at`).
- Use explicit `@Table(name = "...")` on every entity.

## Entity Conventions

- Primary keys use `@GeneratedValue(strategy = GenerationType.IDENTITY)` (database-managed auto-increment).
- Monetary values use `BigDecimal` with defined `precision` and `scale` on the `@Column`.
- Timestamps for audit fields (e.g., `created_at`) use `java.time.Instant` and are set via `@PrePersist`.
- Mark audit columns as `updatable = false` where appropriate.

## JPA / Hibernate Settings

- `ddl-auto=update` in development (schema evolves with entity changes).
- `ddl-auto=create-drop` in tests (clean state per run).
- Dialect is set explicitly: `PostgreSQLDialect` for main, `H2Dialect` for tests.

## Repository Conventions

- Repositories extend `JpaRepository<Entity, Long>`.
- Prefer derived query methods for simple lookups (e.g., `findByCategoryOrderByDateDesc`).
- Use `@Query` with JPQL for aggregation or complex queries.
- Return projections as DTO classes constructed in JPQL (`SELECT new com.budgettracker.dto.ClassName(...)`).
