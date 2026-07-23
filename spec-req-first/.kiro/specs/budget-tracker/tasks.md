# Implementation Plan: Budget Tracker

## Overview

Incremental implementation of a full-stack expense tracker. The plan starts with the backend foundation (entity, repository, service, controller) tested with property-based and unit tests, then builds the Angular frontend with form, list, filter, and summary components wired to the API.

## Tasks

- [x] 1. Set up backend project structure and data layer
  - [x] 1.1 Initialize Spring Boot project with dependencies and configuration
    - Create Maven project with Spring Boot 3.x, Spring Web, Spring Data JPA, PostgreSQL driver, Bean Validation
    - Add jqwik dependency for property-based testing
    - Configure `application.properties` with PostgreSQL connection (localhost:5432), `spring.jpa.hibernate.ddl-auto=update`
    - Configure CORS to allow requests from Angular dev server (http://localhost:4200)
    - _Requirements: 7.1, 7.2, 7.3_

  - [x] 1.2 Create Expense JPA entity and database schema
    - Create `Expense` entity class with fields: id (Long, IDENTITY generation), amount (BigDecimal, precision 12 scale 2), category (String, max 50), date (LocalDate), description (String, max 255 nullable), createdAt (Instant, non-updatable)
    - Add `@PrePersist` callback to set `createdAt` on insert
    - Add NOT NULL constraints on id, amount, category, date, created_at columns
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [x] 1.3 Create ExpenseRepository with custom queries
    - Define `ExpenseRepository` extending `JpaRepository<Expense, Long>`
    - Add `findByCategoryOrderByDateDesc(String category)` method
    - Add `findAllByOrderByDateDesc()` method
    - Add JPQL query method `findCategorySummaries()` that groups by category and sums amounts
    - _Requirements: 2.1, 2.2, 2.5, 5.1, 5.3, 6.3_

  - [x] 1.4 Create request/response DTOs
    - Create `ExpenseRequest` DTO with Jakarta Validation annotations (@NotNull, @DecimalMin, @DecimalMax, @Size, @NotBlank)
    - Create `ExpenseResponse` DTO with id, amount, category, date, description, createdAt
    - Create `CategorySummary` DTO with category and totalAmount fields
    - Create `ErrorResponse` DTO with status, message, and errors list
    - _Requirements: 1.1, 1.2, 1.3, 1.6, 5.1_

- [x] 2. Implement backend business logic and API endpoints
  - [x] 2.1 Implement ExpenseService with CRUD and summary logic
    - Implement `createExpense`: validate, map request to entity, persist, return response
    - Implement `listExpenses`: return all (sorted by date desc) or filter by category
    - Implement `getSummary`: delegate to repository summary query, round to 2 decimal places
    - Implement `updateExpense`: find by id or throw 404, update fields except createdAt, persist
    - Implement `deleteExpense`: find by id or throw 404, delete
    - Create `ExpenseNotFoundException` custom exception
    - _Requirements: 1.1, 1.5, 2.1, 2.2, 2.5, 3.1, 3.6, 4.1, 5.1, 5.3, 5.4_

  - [x] 2.2 Implement ExpenseController REST endpoints
    - POST /expenses → 201 with created expense
    - GET /expenses (optional ?category param) → 200 with list
    - GET /expenses/summary → 200 with category summaries
    - PUT /expenses/{id} → 200 with updated expense
    - DELETE /expenses/{id} → 204 no body
    - Validate empty category query param returns 400
    - _Requirements: 1.1, 2.1, 2.2, 2.4, 3.1, 4.1, 5.1_

  - [x] 2.3 Implement GlobalExceptionHandler
    - Handle `MethodArgumentNotValidException` → 400 with field-specific messages
    - Handle `ExpenseNotFoundException` → 404 with not-found message
    - Handle invalid id format and empty category param → 400
    - Handle generic exceptions → 500 with generic message (no stack trace exposure)
    - _Requirements: 1.2, 1.3, 1.4, 1.6, 2.4, 2.6, 3.2, 3.3, 3.4, 3.5, 4.2, 4.3_

- [x] 3. Checkpoint - Backend compilation and basic verification
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Backend testing
  - [x] 4.1 Write unit tests for ExpenseService
    - Test create with valid data returns expense with id and createdAt
    - Test create with invalid data throws validation error
    - Test list returns sorted expenses
    - Test filter by category returns correct subset
    - Test update preserves createdAt
    - Test update non-existent id throws 404
    - Test delete non-existent id throws 404
    - Use Mockito to mock ExpenseRepository
    - _Requirements: 1.1, 1.5, 2.1, 2.5, 3.1, 3.6, 4.1, 4.2_

  - [x] 4.2 Write unit tests for ExpenseController using @WebMvcTest
    - Test each endpoint returns correct HTTP status codes
    - Test validation error responses for missing/invalid fields
    - Test 404 responses for non-existent expenses
    - Test category filter parameter handling
    - _Requirements: 1.2, 1.3, 1.4, 1.6, 2.4, 3.2, 3.3, 4.2, 4.3_

  - [x] 4.3 Write property test: Create-then-retrieve round trip
    - **Property 1: Create-then-retrieve round trip**
    - Use jqwik to generate random valid ExpenseRequest values (amount in [0.01, 999999999.99], category 1-50 chars, valid date, optional description)
    - Assert POST then GET returns matching fields plus non-null id and createdAt
    - **Validates: Requirements 1.1, 1.5, 2.1**

  - [ ]* 4.4 Write property test: Invalid expense rejection preserves state
    - **Property 2: Invalid expense rejection preserves state**
    - Use jqwik to generate invalid requests (missing fields, out-of-range amounts, invalid dates, empty/overlong categories)
    - Assert POST/PUT returns 400 and total expense count is unchanged
    - **Validates: Requirements 1.2, 1.3, 1.4, 1.6, 3.3, 3.4, 3.5**

  - [ ]* 4.5 Write property test: Category filter returns correct subset
    - **Property 3: Category filter returns correct subset**
    - Generate a set of expenses with varied categories, persist them, then filter by each category
    - Assert filtered results contain exactly those expenses with matching category
    - **Validates: Requirements 2.2, 2.3**

  - [ ]* 4.6 Write property test: Summary totals equal sum of individual amounts
    - **Property 4: Summary totals equal sum of individual amounts**
    - Generate a set of expenses, persist them, fetch summary
    - Assert each category's totalAmount equals the sum of its expenses' amounts (2 decimal places)
    - Assert summary categories equal distinct categories in expenses
    - **Validates: Requirements 5.1, 5.3, 5.4**

  - [ ]* 4.7 Write property test: Update preserves created_at and modifies content
    - **Property 5: Update preserves created_at and modifies content**
    - Create an expense, then generate a random valid update payload, PUT it
    - Assert id and createdAt unchanged, other fields match update payload
    - **Validates: Requirements 3.1, 3.6**

  - [ ]* 4.8 Write property test: Delete removes exactly one expense
    - **Property 6: Delete removes exactly one expense**
    - Create N expenses, delete one by id, assert count is N-1 and deleted id not in GET response
    - **Validates: Requirements 4.1**

  - [ ]* 4.9 Write property test: Expenses are sorted by date descending
    - **Property 7: Expenses are sorted by date descending**
    - Create expenses with random dates, fetch list, assert each consecutive pair has date[i] >= date[i+1]
    - **Validates: Requirements 2.5**

- [x] 5. Checkpoint - Backend tests green
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Set up frontend project and shared infrastructure
  - [x] 6.1 Initialize Angular project and configure proxy
    - Create Angular 17+ project using Angular CLI
    - Configure `proxy.conf.json` to forward `/api/*` requests to `http://localhost:8080`
    - Set up basic app module/component structure
    - Add Angular Material or basic CSS per Requirement 12.3
    - _Requirements: 12.1, 12.2, 12.3, 12.4_

  - [x] 6.2 Create TypeScript models and ExpenseService
    - Define `Expense`, `ExpenseRequest`, `CategorySummary`, and `ErrorResponse` interfaces
    - Implement `ExpenseService` with methods: `getAll()`, `getByCategory(category)`, `getSummary()`, `create(expense)`, `update(id, expense)`, `delete(id)`
    - All methods return Observables wrapping HTTP calls to `/api/expenses`
    - _Requirements: 8.2, 9.4, 9.6, 10.3, 11.3_

- [x] 7. Implement frontend components
  - [x] 7.1 Implement ExpenseFormComponent
    - Create reactive form with amount (number, required), category (text, required), date (date picker, required), description (text, optional)
    - Add inline validation messages shown on field touch/blur for required fields
    - On valid submit: call `ExpenseService.create()`, on success clear form and emit event to refresh list
    - On API error: display error message, preserve form data
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [x] 7.2 Implement ExpenseListComponent
    - Display expenses as a list/table showing amount, category, date, description
    - Add delete button per row, calls `ExpenseService.delete()` and removes item from list
    - Add edit action per row, enables inline editing or pre-fills form, submits via PUT
    - Show empty-state message when no expenses exist
    - Listen for create/update/delete events to refresh list
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

  - [x] 7.3 Implement CategoryFilterComponent
    - Render dropdown/select populated with distinct categories from current expenses
    - Default to "All Categories" on page load
    - On selection: call `ExpenseService.getByCategory(category)` and update displayed list
    - On clear: reset to all expenses, return filter control to initial state
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

  - [x] 7.4 Implement SpendingSummaryComponent
    - Display table/list of category totals from `ExpenseService.getSummary()`
    - Format amounts to two decimal places
    - Sort rows alphabetically by category name
    - Hide summary and show empty-state message when no expenses exist
    - Refresh after any create/update/delete operation
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

- [x] 8. Wire components together and integrate
  - [x] 8.1 Compose AppComponent layout and component communication
    - Place ExpenseFormComponent at top, CategoryFilterComponent below, ExpenseListComponent below that, SpendingSummaryComponent at bottom
    - Wire event emitters: form success → refresh list + summary, delete → refresh list + summary, filter change → update list
    - Ensure all state changes propagate without full page reload
    - _Requirements: 8.2, 9.4, 11.3, 12.1_

  - [ ]* 8.2 Write frontend unit tests
    - Test ExpenseFormComponent: form validation, submission, error display, form clearing
    - Test ExpenseListComponent: rendering, delete action, edit action, empty state
    - Test CategoryFilterComponent: population, selection, clearing
    - Test SpendingSummaryComponent: rendering, formatting, empty state, sorting
    - Test ExpenseService: HTTP call construction, error handling
    - Mock HttpClient responses
    - _Requirements: 8.1, 8.3, 8.4, 8.5, 9.1, 9.3, 9.7, 10.1, 10.2, 11.1, 11.2, 11.4_

- [x] 9. Final checkpoint - Full stack verification
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document using jqwik
- Unit tests validate specific examples and edge cases
- Backend is built first to provide a working API before frontend development begins
- Frontend proxy configuration avoids CORS issues during development

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.4"] },
    { "id": 2, "tasks": ["1.3"] },
    { "id": 3, "tasks": ["2.1"] },
    { "id": 4, "tasks": ["2.2", "2.3"] },
    { "id": 5, "tasks": ["4.1", "4.2"] },
    { "id": 6, "tasks": ["4.3", "4.4", "4.5", "4.6", "4.7", "4.8", "4.9"] },
    { "id": 7, "tasks": ["6.1"] },
    { "id": 8, "tasks": ["6.2"] },
    { "id": 9, "tasks": ["7.1", "7.2", "7.3", "7.4"] },
    { "id": 10, "tasks": ["8.1"] },
    { "id": 11, "tasks": ["8.2"] }
  ]
}
```
