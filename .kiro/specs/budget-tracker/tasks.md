# Implementation Plan: Budget Tracker

## Overview

This plan implements a full-stack expense tracking application with a Spring Boot REST API backend (PostgreSQL) and an Angular SPA frontend. Tasks are structured to build the backend first (data layer → service → controller → error handling → CORS), then the frontend (service → components → integration), and finally wiring everything together.

## Tasks

- [x] 1. Set up backend project structure and data layer
  - [x] 1.1 Initialize Spring Boot project and configure dependencies
    - Create a Spring Boot project with dependencies: Spring Web, Spring Data JPA, Spring Validation, PostgreSQL Driver, Lombok
    - Configure `application.properties` with PostgreSQL connection (jdbc:postgresql://localhost:5432/budgettracker), JPA settings (ddl-auto=update, show-sql=true, PostgreSQL dialect)
    - _Requirements: 11.1, 11.3_

  - [x] 1.2 Create Expense entity and DTOs
    - Create `Expense` entity class with fields: id (Long, @Id, @GeneratedValue IDENTITY), amount (BigDecimal, non-null), category (String, non-null), date (LocalDate, non-null), description (String, nullable), createdAt (LocalDateTime, non-null, updatable=false)
    - Add `@PrePersist` method to set createdAt on insert
    - Create `ExpenseCreateDTO` with Jakarta Bean Validation annotations: @NotNull @DecimalMin("0.01") amount, @NotBlank category, @NotNull date, optional description
    - Create `ExpenseDTO` response class with all fields including id and createdAt
    - Create `CategorySummaryDTO` with category (String) and total (BigDecimal)
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_

  - [x] 1.3 Create ExpenseRepository interface
    - Extend JpaRepository<Expense, Long>
    - Add `findByCategory(String category)` method
    - Add `@Query` method `getCategoryTotals()` returning List<CategorySummary> using `SELECT NEW` with GROUP BY category and SUM(amount)
    - _Requirements: 2.1, 2.2, 5.1, 5.2_

- [x] 2. Implement backend service and controller
  - [x] 2.1 Implement ExpenseService
    - Implement `createExpense(ExpenseCreateDTO)`: map DTO to entity, save, return entity
    - Implement `getExpenses(String category)`: if category non-blank call findByCategory, else findAll
    - Implement `updateExpense(Long id, ExpenseCreateDTO)`: find by id or throw ResourceNotFoundException, update fields, save
    - Implement `deleteExpense(Long id)`: check existsById or throw ResourceNotFoundException, then deleteById
    - Implement `getSummary()`: call getCategoryTotals on repository
    - Create `ResourceNotFoundException` extending RuntimeException
    - _Requirements: 1.1, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 4.1, 4.2, 4.3, 5.1, 5.2, 5.3_

  - [x] 2.2 Implement ExpenseController
    - POST /expenses: accept @Valid @RequestBody ExpenseCreateDTO, call service, return 201 with ExpenseDTO
    - GET /expenses: accept optional @RequestParam category, call service, return 200 with List<ExpenseDTO>
    - PUT /expenses/{id}: accept @PathVariable Long id and @Valid @RequestBody ExpenseCreateDTO, call service, return 200 with ExpenseDTO
    - DELETE /expenses/{id}: accept @PathVariable Long id, call service, return 204 No Content
    - GET /expenses/summary: call service, return 200 with List<CategorySummaryDTO>
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 3.1, 3.2, 3.4, 4.1, 4.2, 5.1_

  - [x] 2.3 Implement GlobalExceptionHandler
    - Create @RestControllerAdvice class
    - Handle ResourceNotFoundException → return 404 with ErrorResponse JSON body
    - Handle MethodArgumentNotValidException → return 400 with field-level error messages
    - Handle HttpMessageNotReadableException → return 400 for malformed JSON
    - Handle MethodArgumentTypeMismatchException → return 400 for invalid path variable types
    - Create ErrorResponse class with status code and message
    - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 3.2, 3.4, 3.5, 4.2, 4.4_

  - [x] 2.4 Implement CORS configuration
    - Create CorsConfig class implementing WebMvcConfigurer
    - Override addCorsMappings: allow origin http://localhost:4200, allow methods GET/POST/PUT/DELETE, apply to all paths under /**
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [ ]* 2.5 Write property tests for backend (jqwik)
    - **Property 1: Create-then-retrieve round trip** — For any valid ExpenseCreateDTO, POST then GET returns matching fields with non-null id and createdAt
    - **Validates: Requirements 1.1, 2.1, 11.2, 11.5**

  - [ ]* 2.6 Write property test for category filter correctness
    - **Property 2: Category filter correctness** — For any set of expenses, GET /expenses?category=X returns only expenses with that category
    - **Validates: Requirements 2.2**

  - [ ]* 2.7 Write property test for summary accuracy
    - **Property 3: Summary accuracy** — For any set of expenses, summary totals equal the arithmetic sum per category
    - **Validates: Requirements 5.1, 5.2**

  - [ ]* 2.8 Write property test for delete removes entity
    - **Property 4: Delete removes entity** — After DELETE returns 204, the expense no longer appears in GET /expenses
    - **Validates: Requirements 4.1, 4.3**

  - [ ]* 2.9 Write property test for update preserves identity
    - **Property 5: Update preserves identity and modifies fields** — PUT returns same id and createdAt with updated field values
    - **Validates: Requirements 3.1, 3.3**

  - [ ]* 2.10 Write property test for not-found returns 404
    - **Property 6: Not-found returns 404** — PUT and DELETE on non-existent id return 404
    - **Validates: Requirements 3.2, 4.2**

  - [ ]* 2.11 Write property test for validation rejects invalid input
    - **Property 7: Validation rejects invalid input** — Invalid DTOs (amount ≤ 0, blank category, null date) yield 400 and no data changes
    - **Validates: Requirements 1.2, 1.3, 1.4, 3.4, 11.4**

- [x] 3. Checkpoint - Backend verification
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Set up frontend project and service layer
  - [x] 4.1 Initialize Angular project and configure HttpClient
    - Create Angular project using Angular CLI
    - Configure HttpClientModule (or provideHttpClient) in app configuration
    - Create TypeScript interfaces: Expense, ExpenseCreate, CategorySummary
    - _Requirements: 7.1, 8.1, 9.1_

  - [x] 4.2 Implement Angular ExpenseService
    - Implement getExpenses(category?: string): build HttpParams if category provided, GET /expenses
    - Implement createExpense(expense: ExpenseCreate): POST /expenses
    - Implement updateExpense(id: number, expense: ExpenseCreate): PUT /expenses/{id}
    - Implement deleteExpense(id: number): DELETE /expenses/{id}
    - Implement getSummary(): GET /expenses/summary
    - Configure base API URL as http://localhost:8080
    - _Requirements: 6.2, 6.3, 7.4, 8.2, 8.3, 9.2_

- [x] 5. Implement frontend UI components
  - [x] 5.1 Implement expense form component
    - Create form with inputs: amount (number), category (text), date (date picker), description (text)
    - Mark amount, category, and date as required
    - Implement form submission: call createExpense for new, updateExpense for edit mode
    - Implement edit mode: populate form fields from selected expense, switch to PUT on submit
    - Clear form and exit edit mode on success
    - Display validation indicators on invalid required fields
    - Display error message if API returns error, preserve form values
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [x] 5.2 Implement expense list component
    - Display list of expenses showing amount, category, date, and description for each
    - Add delete button per expense: call deleteExpense, refresh list and summary on success
    - Add edit button per expense: populate form with expense values, switch to edit mode
    - Show empty-state message when no expenses exist
    - Fetch expenses on page load
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

  - [x] 5.3 Implement category filter
    - Add category filter input field above or adjacent to expense list
    - On non-empty filter value: call getExpenses with category parameter
    - On cleared filter: call getExpenses without category parameter to show all
    - Display indication when no expenses match the filter
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [x] 5.4 Implement summary view component
    - Display summary table with columns: category name and total amount (formatted to 2 decimal places)
    - Refresh summary on expense add/update/delete
    - Show empty-state indication when no expenses exist
    - _Requirements: 9.1, 9.2, 9.3_

  - [ ]* 5.5 Write property test for frontend category filter
    - **Property 9: Frontend category filter passes parameter to API** — For any non-empty filter value, the frontend includes it as the category query parameter in the HTTP request
    - **Validates: Requirements 8.2**

  - [ ]* 5.6 Write unit tests for frontend components
    - Test expense form validation behavior
    - Test expense list rendering with mock data
    - Test summary table formatting
    - Test category filter HTTP parameter passing
    - _Requirements: 6.5, 7.1, 8.2, 9.1_

- [x] 6. Integration and wiring
  - [x] 6.1 Wire frontend components together in AppComponent
    - Compose form, list, filter, and summary into single-page layout
    - Implement data refresh flow: on create/update/delete, refresh both expense list and summary
    - Ensure filter state is preserved across data refreshes
    - _Requirements: 6.2, 6.4, 7.4, 9.2_

  - [ ]* 6.2 Write integration tests for full API lifecycle
    - Test complete CRUD cycle: create → read → update → read → delete → read
    - Test summary accuracy after multiple creates
    - Test category filter with mixed categories
    - Test error scenarios: 404 on missing, 400 on invalid input
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1_

- [x] 7. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- Backend uses Java (Spring Boot) with jqwik for property-based testing
- Frontend uses TypeScript (Angular) with standard Angular testing utilities
- PostgreSQL must be running locally on port 5432 with a `budgettracker` database created before running the backend

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "4.1"] },
    { "id": 2, "tasks": ["1.3", "4.2"] },
    { "id": 3, "tasks": ["2.1"] },
    { "id": 4, "tasks": ["2.2", "2.3", "2.4"] },
    { "id": 5, "tasks": ["2.5", "2.6", "2.7", "2.8", "2.9", "2.10", "2.11"] },
    { "id": 6, "tasks": ["5.1", "5.2", "5.3", "5.4"] },
    { "id": 7, "tasks": ["5.5", "5.6", "6.1"] },
    { "id": 8, "tasks": ["6.2"] }
  ]
}
```
