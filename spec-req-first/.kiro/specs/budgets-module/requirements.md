# Requirements Document

## Introduction

The Budgets Module extends the Budget Tracker application with the ability to set monthly spending limits per category. Users can create, view, update, and delete budgets, and compare actual spending (derived from existing expenses) against each budget's limit. The frontend provides a dedicated Budgets tab with a form for managing budgets and a visual overview of budget status including over-budget indicators.

## Glossary

- **Budget_API**: The Spring Boot REST controller handling HTTP requests at the `/budgets` path.
- **Budget_Service**: The backend service layer responsible for budget business logic and spend calculations.
- **Budget_Repository**: The Spring Data JPA repository for persisting and querying Budget entities.
- **Budget_Entity**: The JPA entity representing a row in the `budget` database table, containing category, month, and limit_amount fields.
- **Budget_Status**: A computed view combining a budget's limit with the actual spend for that category/month (summed from expenses) and the remaining amount.
- **Budgets_Tab**: The Angular standalone component providing the budgets user interface within the single-page application.
- **Budget_Form**: The Angular reactive form component for creating and editing budgets.
- **Budget_List**: The Angular component displaying all budgets with their status information.
- **Expense_Repository**: The existing Spring Data JPA repository for querying expense records.

## Requirements

### Requirement 1: Create a Budget

**User Story:** As a user, I want to set a monthly budget for a category, so that I can track my spending against a planned limit.

#### Acceptance Criteria

1. WHEN a POST request is received at `/budgets` with a non-blank category (maximum 50 characters), a month value matching YYYY-MM format, and a limit_amount of at least 0.01, THE Budget_API SHALL create a new Budget_Entity and return HTTP status 201 with a JSON body containing id, category, month, limitAmount, and createdAt.
2. WHEN a POST request is received with a missing or blank category, THE Budget_API SHALL return HTTP status 400 with a field-level error message identifying the invalid field.
3. WHEN a POST request is received with a null limit_amount or a limit_amount less than 0.01, THE Budget_API SHALL return HTTP status 400 with a field-level error message identifying the invalid field.
4. WHEN a POST request is received with a null month value or a month value that does not match YYYY-MM format, THE Budget_API SHALL return HTTP status 400 with a field-level error message identifying the invalid field.
5. THE Budget_Entity SHALL store limit_amount as a BigDecimal with precision 12 and scale 2.
6. THE Budget_Entity SHALL store month as a `java.time.YearMonth` representing the budget period.
7. IF a POST /budgets request arrives for a category and month combination that already has an existing budget, THEN THE Budget_API SHALL return HTTP status 409 with an error message indicating a duplicate budget exists.
8. WHEN a POST request contains multiple validation errors, THE Budget_API SHALL return HTTP status 400 with a response listing all field-level errors.

### Requirement 2: List All Budgets

**User Story:** As a user, I want to view all my budgets, so that I can see what spending limits I have set.

#### Acceptance Criteria

1. WHEN a GET request is received at `/budgets`, THE Budget_API SHALL return HTTP status 200 with a JSON array containing one element for each persisted Budget_Entity record.
2. WHEN no budgets exist, THE Budget_API SHALL return HTTP status 200 with an empty JSON array (`[]`).
3. THE Budget_API SHALL return each budget element with the fields: id, category, month, limitAmount, and createdAt.
4. IF the data store is unreachable when a GET request is received at `/budgets`, THEN THE Budget_API SHALL return HTTP status 500 with an error message indicating a server-side failure.

### Requirement 3: Update a Budget

**User Story:** As a user, I want to update an existing budget, so that I can adjust my spending limit for a category and month.

#### Acceptance Criteria

1. WHEN a valid PUT request is received at `/budgets/{id}` with a non-blank category (maximum 50 characters), a month in YYYY-MM format, and a limit_amount of at least 0.01, THE Budget_API SHALL update the corresponding Budget_Entity and return the updated record with HTTP status 200.
2. IF a PUT request is received at `/budgets/{id}` with an id that does not match any existing Budget_Entity, THEN THE Budget_API SHALL return HTTP status 404 with an error message indicating which id was not found.
3. WHEN a PUT request is received with a blank category, null or non-positive limit_amount, or invalid month format, THE Budget_API SHALL return HTTP status 400 with field-level error messages identifying each invalid field.
4. IF a PUT request would result in a category and month combination that already exists on a different Budget_Entity (excluding the budget being updated), THEN THE Budget_Service SHALL return HTTP status 409 (Conflict) with an error message indicating a duplicate budget exists.

### Requirement 4: Delete a Budget

**User Story:** As a user, I want to delete a budget, so that I can remove spending limits I no longer need.

#### Acceptance Criteria

1. WHEN a DELETE request is received at `/budgets/{id}` with an id that matches an existing Budget_Entity, THE Budget_API SHALL permanently remove that Budget_Entity and return HTTP status 204 with an empty response body.
2. IF a DELETE request is received at `/budgets/{id}` with an id that does not match any existing Budget_Entity, THEN THE Budget_API SHALL return HTTP status 404 with an error message indicating which id was not found.
3. IF a DELETE request is received at `/budgets/{id}` where the id path parameter is not a valid numeric value, THEN THE Budget_API SHALL return HTTP status 400 with an error message indicating the type mismatch.
4. WHEN a Budget_Entity has been successfully deleted, THE Budget_API SHALL exclude that entity from all subsequent GET `/budgets` and GET `/budgets/status` responses.

### Requirement 5: View Budget Status

**User Story:** As a user, I want to see how my actual spending compares to each budget limit, so that I can identify categories where I am over budget.

#### Acceptance Criteria

1. WHEN a GET request is received at `/budgets/status`, THE Budget_API SHALL return HTTP status 200 with a JSON array of Budget_Status objects, one per persisted budget.
2. THE Budget_Service SHALL calculate actual_spend for each budget by summing all expense amounts from the Expense_Repository where the expense category matches the budget category (case-insensitive) and the expense date's year and month equal the budget month's year and month.
3. THE Budget_Service SHALL calculate remaining_amount as limit_amount minus actual_spend for each budget, computed to 2 decimal places without intermediate rounding.
4. WHEN actual_spend exceeds limit_amount, THE Budget_Status SHALL represent remaining_amount as a negative value indicating the over-budget amount.
5. WHEN no expenses exist for a budget's category and month, THE Budget_Service SHALL return actual_spend as 0.00 and remaining_amount equal to limit_amount.
6. THE Budget_Status SHALL include the fields: budget id, category, month, limit_amount, actual_spend, and remaining_amount.
7. THE Budget_Service SHALL include all matching expense amounts in the sum regardless of whether individual amounts are positive, negative, or zero.

### Requirement 6: Budget Form Component

**User Story:** As a user, I want a form to create budgets, so that I can easily set spending limits for categories and months.

#### Acceptance Criteria

1. THE Budget_Form SHALL provide a text input for category (maximum 50 characters), a month picker accepting "YYYY-MM" format, and a numeric input for limit amount (accepting values from 0.01 to 9,999,999,999.99).
2. IF the category field is empty or contains only whitespace at submission, THEN THE Budget_Form SHALL display a validation error message adjacent to the category field indicating that category is required.
3. IF the limit amount is not a positive number or is less than 0.01 at submission, THEN THE Budget_Form SHALL display a validation error message adjacent to the limit amount field indicating that a positive amount is required.
4. IF no month is selected at submission, THEN THE Budget_Form SHALL display a validation error message adjacent to the month field indicating that month selection is required.
5. WHEN the form is submitted with valid data, THE Budget_Form SHALL send a POST request to `/budgets` via the Angular HttpClient and emit a budgetCreated event on success.
6. WHEN the API returns an error, THE Budget_Form SHALL display the error message returned by the API in a visible error area within the form until the user modifies any field or resubmits.
7. WHEN a budget is successfully created, THE Budget_Form SHALL reset the category field to empty, the month field to unselected, and the limit amount field to empty.
8. WHILE a budget creation request is in progress, THE Budget_Form SHALL disable the submit button to prevent duplicate submissions.

### Requirement 7: Budget List Component

**User Story:** As a user, I want to see all budgets with their spending status, so that I can monitor my financial health at a glance.

#### Acceptance Criteria

1. THE Budget_List SHALL display each budget's category, month, limit amount, actual spend, and remaining amount in a tabular layout with one row per budget.
2. THE Budget_List SHALL display a progress bar on each row representing the proportion of the limit amount that has been spent, calculated as (actual_spend / limit_amount) * 100, capped at 100%.
3. IF remaining_amount is negative for a budget, THEN THE Budget_List SHALL apply a visually distinct over-budget style (red color) to the row's remaining amount and progress bar.
4. IF remaining_amount is zero or positive for a budget, THEN THE Budget_List SHALL apply a normal style (green color) to the row's remaining amount and progress bar.
5. THE Budget_List SHALL provide a delete button for each budget entry.
6. WHEN the delete button is clicked, THE Budget_List SHALL send a DELETE request to `/budgets/{id}` and emit a budgetDeleted event on success.
7. IF the DELETE request fails, THEN THE Budget_List SHALL display an error message indicating the deletion was unsuccessful and SHALL leave the budget entry unchanged in the list.
8. THE Budget_List SHALL provide an edit button for each budget entry.
9. WHEN the edit button is clicked, THE Budget_List SHALL emit a budgetEdit event containing the budget's id, category, month, and limitAmount to the parent component.
10. WHEN no budgets exist, THE Budget_List SHALL display an empty-state message indicating that no budgets have been created.

### Requirement 8: Budgets Tab Integration

**User Story:** As a user, I want a dedicated Budgets tab in the application, so that I can navigate to budget management separately from expense tracking.

#### Acceptance Criteria

1. THE Budgets_Tab SHALL be a standalone Angular component containing the Budget_Form and Budget_List components.
2. WHEN a budget is created, updated, or deleted, THE Budgets_Tab SHALL re-fetch budget status data from the `/budgets/status` endpoint and re-render the Budget_List with the response.
3. THE Budgets_Tab SHALL be selectable via a labeled "Budgets" tab button in the main application tab bar, rendered alongside the "Expenses" tab button.
4. WHEN the Budgets_Tab is initialized, THE Budgets_Tab SHALL fetch and display budget status data from the `/budgets/status` endpoint.
5. IF the `/budgets/status` endpoint returns an error or is unreachable during data loading, THEN THE Budgets_Tab SHALL display an error message indicating that budget data could not be loaded.

### Requirement 9: Budget Data Persistence

**User Story:** As a user, I want my budgets stored reliably, so that they persist across application restarts.

#### Acceptance Criteria

1. THE Budget_Entity SHALL be persisted in a PostgreSQL table named `budget` with columns: id (bigint, auto-generated primary key), category (varchar 50, not null), month (varchar 7, not null, stores YYYY-MM format), limit_amount (numeric 12,2, not null), and created_at (timestamp, not null, non-updatable).
2. THE Budget_Repository SHALL extend JpaRepository<Budget, Long>.
3. THE Budget_Repository SHALL provide a query method to find a budget by category (case-insensitive) and month, returning Optional<Budget>.
4. THE Budget_Entity SHALL use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for the primary key.
5. THE Budget_Entity SHALL set created_at via `@PrePersist` using `java.time.Instant`.
6. THE `budget` table SHALL have a unique constraint on the combination of (category, month) to prevent duplicate budgets at the database level.

### Requirement 10: Budget Status Calculation Correctness

**User Story:** As a user, I want budget status calculations to be accurate, so that I can trust the reported spending data.

#### Acceptance Criteria

1. FOR ALL budgets with one or more matching expenses, THE Budget_Service SHALL produce a remaining_amount equal to limit_amount minus the sum of all matching expense amounts, calculated to 2 decimal places without intermediate rounding.
2. FOR ALL budgets where no matching expenses exist, THE Budget_Service SHALL produce actual_spend of 0.00 and remaining_amount equal to limit_amount.
3. THE Budget_Service SHALL match expenses to budgets using case-insensitive category comparison such that "Food", "food", and "FOOD" all match the same budget category.
4. THE Budget_Service SHALL match expenses to budgets by verifying that the year and month extracted from the expense date equal the year and month encoded in the budget month (YYYY-MM format).
5. THE Budget_Service SHALL include all matching expense amounts in the sum regardless of whether individual amounts are positive, negative, or zero.
