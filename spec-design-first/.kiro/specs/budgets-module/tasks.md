# Implementation Plan: Budgets Module

## Overview

This plan adds the Budgets feature on top of the existing Budget Tracker application. The backend gains a `budgets` table, a full CRUD REST resource at `/budgets`, and a `/budgets/status` endpoint that computes live spend vs. limit per category/month. The frontend gains a Budgets tab with a form and a status list. All tasks build on the existing Spring Boot + Angular codebase without touching existing expense code.

## Tasks

- [x] 1. Backend data layer — Budget entity, DTOs, and repository
  - [x] 1.1 Create Budget entity and DTOs
    - Create `Budget` entity: id (Long, @Id, @GeneratedValue IDENTITY), category (String, non-null, length 255), month (String "YYYY-MM", non-null, length 7), limitAmount (BigDecimal, non-null), createdAt (LocalDateTime, non-null, updatable=false), @PrePersist sets createdAt, @Table with uniqueConstraints on (category, month)
    - Create `BudgetCreateDTO` with validation: @NotBlank category, @NotBlank @Pattern(regexp="^\\d{4}-(0[1-9]|1[0-2])$") month, @NotNull @DecimalMin("0.01") limitAmount
    - Create `BudgetDTO` response: id, category, month, limitAmount, createdAt
    - Create `BudgetStatusRow` projection class: id, category, month, limitAmount, actual (BigDecimal) — constructor-mapped from JPQL
    - Create `BudgetStatusDTO` response: id, category, month, limitAmount, actual, remaining
    - _Requirements: B1.1, B2.1, B5.1, B9.1, B9.2, B9.3, B9.4, B9.5_

  - [x] 1.2 Create BudgetRepository interface
    - Extend JpaRepository<Budget, Long>
    - Add native query method `getBudgetStatusRows()` returning List<BudgetStatusRow>:
      ```sql
      SELECT b.id, b.category, b.month, b.limit_amount,
             COALESCE(SUM(e.amount), 0) AS actual
      FROM budgets b
      LEFT JOIN expenses e
        ON e.category = b.category
       AND TO_CHAR(e.date, 'YYYY-MM') = b.month
      GROUP BY b.id, b.category, b.month, b.limit_amount
      ```
    - Use `@Query(value = "...", nativeQuery = true)` with a `@SqlResultSetMapping` or a constructor-based projection via interface projection (use an interface with getId(), getCategory(), getMonth(), getLimitAmount(), getActual() if native query constructor mapping is problematic)
    - _Requirements: B5.1, B5.2, B5.3, B5.4_

- [x] 2. Backend service and controller
  - [x] 2.1 Implement BudgetService
    - Inject BudgetRepository and (for status) no direct ExpenseRepository access needed (query is in BudgetRepository)
    - `createBudget(BudgetCreateDTO dto)`: map to entity, save, return entity; let DataIntegrityViolationException bubble up for duplicate handling
    - `getBudgets()`: return findAll()
    - `updateBudget(Long id, BudgetCreateDTO dto)`: find by id or throw ResourceNotFoundException, update fields, save
    - `deleteBudget(Long id)`: check existsById or throw ResourceNotFoundException, deleteById
    - `getBudgetStatus()`: call getBudgetStatusRows(), map each row to BudgetStatusDTO computing remaining = limitAmount - actual
    - _Requirements: B1.1, B2.1, B3.1, B3.2, B4.1, B4.2, B5.1, B5.2, B5.3, B5.4_

  - [x] 2.2 Implement BudgetController
    - POST /budgets: accept @Valid @RequestBody BudgetCreateDTO, call service, return 201 with BudgetDTO
    - GET /budgets: call service, return 200 with List<BudgetDTO>
    - PUT /budgets/{id}: accept @PathVariable Long id and @Valid @RequestBody BudgetCreateDTO, call service, return 200 with BudgetDTO
    - DELETE /budgets/{id}: call service, return 204 No Content
    - GET /budgets/status: call service, return 200 with List<BudgetStatusDTO>
    - _Requirements: B1.1, B2.1, B3.1, B4.1, B5.1_

  - [x] 2.3 Extend GlobalExceptionHandler for 409 Conflict
    - Add handler for `DataIntegrityViolationException` → return HTTP 409 with ErrorResponse body containing message "Budget already exists for this category and month"
    - Ensure existing 404 and 400 handlers are not modified
    - _Requirements: B1.5, B3.4_

- [x] 3. Backend checkpoint
  - Verify the Spring Boot application starts and the `budgets` table is auto-created (ddl-auto=update). Test POST, GET, PUT, DELETE, and GET /budgets/status with curl or an HTTP client. Resolve any issues before proceeding to frontend.

- [x] 4. Frontend service and models
  - [x] 4.1 Add Budget TypeScript models
    - Add to `src/app/models/expense.model.ts` (or create a new `budget.model.ts`):
      - `Budget` interface: id, category, month, limitAmount, createdAt
      - `BudgetCreate` interface: category, month, limitAmount
      - `BudgetStatus` interface: id, category, month, limitAmount, actual, remaining
    - _Requirements: B6.3, B7.1, B8.1_

  - [x] 4.2 Implement Angular BudgetService
    - Create `src/app/services/budget.service.ts` with base URL `http://localhost:8080/budgets`
    - `getBudgets()`: GET /budgets → Observable<Budget[]>
    - `createBudget(b: BudgetCreate)`: POST /budgets → Observable<Budget>
    - `updateBudget(id, b: BudgetCreate)`: PUT /budgets/{id} → Observable<Budget>
    - `deleteBudget(id)`: DELETE /budgets/{id} → Observable<void>
    - `getBudgetStatus()`: GET /budgets/status → Observable<BudgetStatus[]>
    - _Requirements: B7.2, B7.3, B8.5, B8.6, B8.8_

- [x] 5. Frontend UI components
  - [x] 5.1 Implement BudgetFormComponent
    - Create `src/app/components/budget-form/` with `.ts`, `.html`, `.css`
    - Standalone component using FormsModule or ReactiveFormsModule
    - Fields: category (text, required), month (`<input type="month">`, required), limitAmount (number, required, min 0.01)
    - `@Input() budgetToEdit: Budget | null` — populates form when set
    - `@Output() budgetSaved = new EventEmitter<void>()`
    - On submit: call createBudget or updateBudget, emit budgetSaved on success, clear form
    - Show validation indicators on invalid required fields; show API error message on failure
    - _Requirements: B7.1, B7.2, B7.3, B7.4, B7.5, B7.6_

  - [x] 5.2 Implement BudgetListComponent
    - Create `src/app/components/budget-list/` with `.ts`, `.html`, `.css`
    - `@Input() budgetStatuses: BudgetStatus[]`
    - `@Output() editBudget = new EventEmitter<BudgetStatus>()`
    - `@Output() deleteBudget = new EventEmitter<number>()`
    - Renders table: category | month | limit | actual | remaining | progress bar | edit | delete
    - Progress bar: `<div class="bar-fill" [style.width.%]="min(actual/limit*100, 100)" [class.over-budget]="remaining < 0"></div>`
    - Apply CSS class `over-budget` on the remaining cell and bar when remaining < 0 (red), else `on-track` (green)
    - Show empty-state message when budgetStatuses is empty
    - _Requirements: B8.1, B8.2, B8.3, B8.4, B8.5, B8.6, B8.7_

  - [x] 5.3 Implement BudgetsTabComponent
    - Create `src/app/components/budgets-tab/` with `.ts`, `.html`, `.css`
    - Standalone; composes BudgetFormComponent + BudgetListComponent
    - On init: call getBudgetStatus() to load statuses
    - On budgetSaved: reload statuses
    - On deleteBudget: call deleteBudget(id), reload statuses on success
    - On editBudget: pass selected budget to BudgetFormComponent as budgetToEdit
    - _Requirements: B6.3, B8.8_

  - [x] 5.4 Add tab navigation to AppComponent
    - Add a tab bar to `app.html` with two tabs: "Expenses" and "Budgets"
    - Track `activeTab: 'expenses' | 'budgets'` in `app.ts`
    - Conditionally render existing expense components OR BudgetsTabComponent using `@if` blocks
    - Add tab bar CSS in `app.css` (simple styled button tabs, active tab highlighted)
    - _Requirements: B6.1, B6.2, B6.3, B6.4_

- [x] 6. Final checkpoint
  - Start backend and frontend, navigate to Budgets tab, create a budget for an existing expense category and month, verify the status list shows non-zero actual and correct remaining. Test over-budget scenario (set limit below existing spend) and confirm red indicator appears. Resolve any issues.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2"] },
    { "id": 2, "tasks": ["2.1"] },
    { "id": 3, "tasks": ["2.2", "2.3"] },
    { "id": 4, "tasks": ["3"] },
    { "id": 5, "tasks": ["4.1"] },
    { "id": 6, "tasks": ["4.2"] },
    { "id": 7, "tasks": ["5.1", "5.2"] },
    { "id": 8, "tasks": ["5.3"] },
    { "id": 9, "tasks": ["5.4"] },
    { "id": 10, "tasks": ["6"] }
  ]
}
```

## Notes

- The existing `GlobalExceptionHandler`, `ResourceNotFoundException`, and `CorsConfig` are reused without modification (except adding the 409 handler in task 2.3)
- The `TO_CHAR(e.date, 'YYYY-MM') = b.month` join condition is PostgreSQL-specific; a native query is used to avoid JPQL portability concerns
- `ddl-auto=update` in `application.properties` will auto-create the `budgets` table and its unique constraint on startup
- The `<input type="month">` HTML element outputs values in `YYYY-MM` format natively, matching the backend storage format exactly
- Checkpoint tasks (3 and 6) are not optional — they gate the next phase to catch integration issues early
