# Implementation Plan: Budgets Module

## Overview

This plan implements the Budgets Module for the Budget Tracker application, adding monthly spending-limit tracking with a full CRUD REST API, budget-status calculations comparing limits to actual expenses, and an Angular frontend with a dedicated Budgets tab. The implementation follows the established layered architecture (Controller → Service → Repository) on the backend and standalone Angular component patterns on the frontend.

## Tasks

- [x] 1. Set up backend data layer and core interfaces
  - [x] 1.1 Create the Budget entity with YearMonthAttributeConverter
    - Create `YearMonthAttributeConverter.java` in `com.budgettracker.model` implementing `AttributeConverter<YearMonth, String>` with `@Converter(autoApply = true)`
    - Create `Budget.java` entity in `com.budgettracker.model` with fields: id (Long, IDENTITY), category (String, max 50), month (YearMonth with @Convert), limitAmount (BigDecimal, precision 12 scale 2), createdAt (Instant, @PrePersist)
    - Add `@UniqueConstraint(columnNames = {"category", "month"})` on the entity table annotation
    - _Requirements: 1.5, 1.6, 9.1, 9.4, 9.5, 9.6_

  - [x] 1.2 Create BudgetRepository interface
    - Create `BudgetRepository.java` in `com.budgettracker.repository` extending `JpaRepository<Budget, Long>`
    - Add `findByCategoryIgnoreCaseAndMonth(String category, YearMonth month)` returning `Optional<Budget>`
    - _Requirements: 9.2, 9.3_

  - [x] 1.3 Add sumAmountByCategoryAndMonth query to ExpenseRepository
    - Add `@Query` method to existing `ExpenseRepository.java` that sums expense amounts by case-insensitive category and year/month extracted from expense date
    - Use `COALESCE(SUM(e.amount), 0)` to return 0 when no expenses match
    - _Requirements: 5.2, 10.3, 10.4_

  - [x] 1.4 Create BudgetRequest, BudgetResponse, and BudgetStatusResponse DTOs
    - Create `BudgetRequest.java` in `com.budgettracker.dto` with jakarta.validation annotations: `@NotBlank` + `@Size(max=50)` on category, `@NotNull` + `@Pattern(YYYY-MM)` on month, `@NotNull` + `@DecimalMin("0.01")` on limitAmount
    - Create `BudgetResponse.java` with fields: id, category, month (String), limitAmount, createdAt
    - Create `BudgetStatusResponse.java` with fields: id, category, month (String), limitAmount, actualSpend, remainingAmount
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.3, 5.6_

  - [ ]* 1.5 Write property test for YearMonth converter round-trip
    - **Property 1: YearMonth Converter Round-Trip**
    - Generate random YearMonth values via jqwik `@ForAll` with custom provider, verify `convertToEntityAttribute(convertToDatabaseColumn(ym)).equals(ym)`
    - **Validates: Requirements 1.6**

- [x] 2. Implement backend service and exception handling
  - [x] 2.1 Create BudgetNotFoundException and DuplicateBudgetException
    - Create `BudgetNotFoundException.java` in `com.budgettracker.exception` with constructor accepting `Long id` and message "Budget not found with id: {id}"
    - Create `DuplicateBudgetException.java` in `com.budgettracker.exception` with constructor accepting category and month, message "A budget already exists for category '{category}' and month '{month}'"
    - _Requirements: 1.7, 3.2, 3.4, 4.2_

  - [x] 2.2 Add exception handlers to GlobalExceptionHandler
    - Add `@ExceptionHandler(BudgetNotFoundException.class)` returning 404 with ErrorResponse
    - Add `@ExceptionHandler(DuplicateBudgetException.class)` returning 409 with ErrorResponse
    - _Requirements: 1.7, 3.2, 4.2_

  - [x] 2.3 Implement BudgetService interface and BudgetServiceImpl
    - Create `BudgetService.java` interface in `com.budgettracker.service` with methods: createBudget, listBudgets, updateBudget, deleteBudget, getBudgetStatus
    - Create `BudgetServiceImpl.java` implementing the interface with `@Service`
    - Implement `createBudget`: check for duplicates via repository, map request to entity, save, return response
    - Implement `listBudgets`: return all budgets mapped to BudgetResponse
    - Implement `updateBudget`: find by id (throw BudgetNotFoundException if absent), check duplicate excluding self, update fields, save
    - Implement `deleteBudget`: find by id (throw if absent), delete
    - Implement `getBudgetStatus`: iterate all budgets, for each call `expenseRepository.sumAmountByCategoryAndMonth`, compute remainingAmount = limitAmount - actualSpend with scale 2
    - Catch `DataIntegrityViolationException` on save and rethrow as `DuplicateBudgetException` for race condition safety
    - _Requirements: 1.1, 1.7, 2.1, 3.1, 3.4, 4.1, 5.1, 5.2, 5.3, 5.4, 5.5, 10.1, 10.2, 10.3, 10.4, 10.5_

  - [ ]* 2.4 Write property test for budget creation/listing round-trip
    - **Property 2: Budget Creation/Listing Round-Trip**
    - Generate valid BudgetRequest with unique category+month, create via service, verify element exists in listBudgets with matching fields and non-null id/createdAt
    - **Validates: Requirements 1.1, 2.1, 2.3**

  - [ ]* 2.5 Write property test for duplicate category+month rejection
    - **Property 3: Duplicate Category+Month Rejection**
    - Generate two requests with same category (case variations) + same month, verify second create/update throws conflict, total count for that combo remains 1
    - **Validates: Requirements 1.7, 3.4, 9.6**

  - [ ]* 2.6 Write property test for budget deletion exclusion
    - **Property 4: Budget Deletion Exclusion**
    - Create budget, delete it, verify id absent from listBudgets and getBudgetStatus results
    - **Validates: Requirements 4.1, 4.4**

- [x] 3. Implement BudgetController REST endpoints
  - [x] 3.1 Create BudgetController with CRUD endpoints
    - Create `BudgetController.java` in `com.budgettracker.controller` with `@RestController` and `@RequestMapping("/budgets")`
    - Implement `POST /budgets` → delegates to `budgetService.createBudget`, returns 201
    - Implement `GET /budgets` → delegates to `budgetService.listBudgets`, returns 200
    - Implement `PUT /budgets/{id}` → delegates to `budgetService.updateBudget`, returns 200
    - Implement `DELETE /budgets/{id}` → delegates to `budgetService.deleteBudget`, returns 204
    - Implement `GET /budgets/status` → delegates to `budgetService.getBudgetStatus`, returns 200
    - Use `@Valid @RequestBody` for POST and PUT to trigger validation
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.7, 1.8, 2.1, 2.2, 3.1, 3.2, 3.3, 4.1, 4.2, 4.3, 5.1, 5.6_

  - [ ]* 3.2 Write property tests for budget status calculations
    - **Property 5: Budget Status Actual Spend Calculation**
    - Generate budgets and expenses with varied categories (case variations) and dates, persist them, call getBudgetStatus, verify actualSpend equals expected sum
    - **Validates: Requirements 5.2, 5.5, 5.7, 10.1, 10.2, 10.3, 10.4, 10.5**
    - **Property 6: Remaining Amount Invariant**
    - For any BudgetStatusResponse, verify remainingAmount == limitAmount - actualSpend (scale 2), verify negative when overspent
    - **Validates: Requirements 5.3, 5.4**

- [x] 4. Checkpoint - Backend complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement frontend models and budget service
  - [x] 5.1 Create frontend budget models
    - Create `budget.model.ts` in `frontend/src/app/models/` with interfaces: `Budget`, `BudgetRequest`, `BudgetStatus`
    - _Requirements: 5.6, 6.1_

  - [x] 5.2 Create frontend BudgetService
    - Create `budget.service.ts` in `frontend/src/app/services/` with `@Injectable({ providedIn: 'root' })`
    - Implement methods: `getStatus()`, `getAll()`, `create(request)`, `update(id, request)`, `delete(id)` using HttpClient with base URL `/api/budgets`
    - _Requirements: 6.5, 7.6, 8.2, 8.4_

- [x] 6. Implement frontend budget components
  - [x] 6.1 Create BudgetFormComponent
    - Create standalone component at `frontend/src/app/components/budget-form/`
    - Implement reactive form with fields: category (text, required, maxLength 50), month (month input, required, YYYY-MM), limitAmount (number, required, min 0.01)
    - Display inline validation errors adjacent to each field on submit
    - Handle create (POST) and edit (PUT) modes via `@Input() editBudget`
    - Emit `budgetCreated` and `budgetUpdated` output events on success
    - Display API error messages in visible error area; clear on field change
    - Disable submit button while request is in progress
    - Reset form fields after successful creation
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8_

  - [x] 6.2 Create BudgetListComponent
    - Create standalone component at `frontend/src/app/components/budget-list/`
    - Accept `@Input() budgets: BudgetStatus[]`
    - Display tabular layout with columns: category, month, limit amount, actual spend, remaining amount, progress bar, actions
    - Calculate progress bar width as `min((actualSpend / limitAmount) * 100, 100)%`
    - Apply green styling when remainingAmount >= 0, red styling when remainingAmount < 0
    - Add delete button per row, call BudgetService.delete, emit `budgetDeleted` on success, show inline error on failure
    - Add edit button per row, emit `budgetEdit` event with budget data
    - Display empty-state message when no budgets exist
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10_

  - [ ]* 6.3 Write property test for progress bar percentage calculation
    - **Property 7: Progress Bar Percentage Calculation**
    - Generate random actualSpend (>=0) and limitAmount (>0) pairs, verify percentage = min((actualSpend/limitAmount)*100, 100), never exceeds 100
    - **Validates: Requirements 7.2**

  - [ ]* 6.4 Write property test for over-budget styling determination
    - **Property 8: Over-Budget Styling Determination**
    - Generate random remainingAmount values, verify red class applied when < 0, green when >= 0
    - **Validates: Requirements 7.3, 7.4**

- [x] 7. Implement BudgetsTab and AppComponent integration
  - [x] 7.1 Create BudgetsTabComponent
    - Create standalone component at `frontend/src/app/components/budgets-tab/`
    - Compose BudgetFormComponent and BudgetListComponent
    - On init, fetch budget status from BudgetService.getStatus()
    - Handle `budgetCreated`, `budgetUpdated`, `budgetDeleted` events by re-fetching status
    - Handle `budgetEdit` event by passing budget data to form for editing
    - Display error message if status endpoint fails or is unreachable
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [x] 7.2 Modify AppComponent to add tab bar navigation
    - Add `activeTab` state property (default: 'expenses')
    - Add tab bar to template with "Expenses" and "Budgets" buttons
    - Conditionally render existing expense components when activeTab is 'expenses'
    - Conditionally render BudgetsTabComponent when activeTab is 'budgets'
    - Import BudgetsTabComponent in AppComponent imports array
    - _Requirements: 8.3_

- [x] 8. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik (already in pom.xml)
- Unit tests validate specific examples and edge cases
- Backend property tests (Properties 1-6) use jqwik with Spring Boot test context and H2 in-memory database
- Frontend property tests (Properties 7-8) can be implemented as pure function tests in Jasmine using generated inputs
- The backend implementation follows the existing ExpenseController → ExpenseService → ExpenseRepository pattern
- The frontend follows the existing standalone component pattern with Angular HttpClient

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.4"] },
    { "id": 1, "tasks": ["1.2", "1.3", "2.1"] },
    { "id": 2, "tasks": ["1.5", "2.2", "2.3", "5.1"] },
    { "id": 3, "tasks": ["2.4", "2.5", "2.6", "3.1", "5.2"] },
    { "id": 4, "tasks": ["3.2", "6.1", "6.2"] },
    { "id": 5, "tasks": ["6.3", "6.4", "7.1"] },
    { "id": 6, "tasks": ["7.2"] }
  ]
}
```
