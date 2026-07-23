# Requirements Document: Budgets Module

## Introduction

The Budgets Module extends the existing Budget Tracker application to allow users to set monthly spending limits per category and track actual vs. budgeted spend. The backend adds a new `budgets` table and a `/budgets` REST resource. The frontend adds a Budgets tab with a form and a status list that shows limit, actual spend, and remaining amount with a visual over-budget indicator.

## Glossary

- **Budget**: A record combining a category, a calendar month (YYYY-MM), and a spending limit amount
- **Limit amount**: The maximum planned spend for a category in a given month
- **Actual spend**: The sum of all expense amounts for the same category and calendar month
- **Remaining**: `limitAmount − actual`; negative values indicate over-budget
- **Over-budget**: When `actual > limitAmount` (remaining is negative)
- **Budget status**: A computed view combining a budget's limit with the actual spend derived from expenses

## Requirements

### Requirement B1: Create Budget

**User Story:** As a user, I want to set a monthly budget for a category, so that I can limit my spending in that area.

#### Acceptance Criteria

1. WHEN a valid BudgetCreateDTO is submitted via POST /budgets, THE API SHALL persist a new budget and return HTTP 201 with a JSON body containing id, category, month, limitAmount, and createdAt
2. WHEN a BudgetCreateDTO with a limitAmount less than 0.01 is submitted, THE API SHALL reject the request and return HTTP 400 with a field-level error identifying the invalid field
3. WHEN a BudgetCreateDTO with a blank category is submitted, THE API SHALL reject the request and return HTTP 400 with a field-level error
4. WHEN a BudgetCreateDTO with a month string that does not match YYYY-MM format is submitted, THE API SHALL reject the request and return HTTP 400 with a field-level error
5. WHEN a POST /budgets request arrives for a (category, month) combination that already has a budget, THE API SHALL return HTTP 409 Conflict with a JSON error message
6. WHEN multiple validation errors exist in the submitted BudgetCreateDTO, THE API SHALL return HTTP 400 listing all field-level errors

### Requirement B2: List Budgets

**User Story:** As a user, I want to see all the budgets I have set, so that I can review my spending limits.

#### Acceptance Criteria

1. WHEN a GET request is made to /budgets, THE API SHALL return HTTP 200 with a JSON array where each element contains id, category, month, limitAmount, and createdAt for every persisted budget
2. WHEN no budgets exist, THE API SHALL return HTTP 200 with an empty JSON array

### Requirement B3: Update Budget

**User Story:** As a user, I want to update an existing budget, so that I can adjust my spending limits.

#### Acceptance Criteria

1. WHEN a valid BudgetCreateDTO is submitted via PUT /budgets/{id} for an existing budget, THE API SHALL update the budget and return HTTP 200 with the full updated budget including the unchanged id and createdAt
2. WHEN a PUT request targets a non-existent budget id, THE API SHALL return HTTP 404 with an error message
3. WHEN a BudgetCreateDTO with invalid fields (limitAmount ≤ 0, blank category, or invalid month format) is submitted via PUT /budgets/{id}, THE API SHALL return HTTP 400 with field-level error details
4. WHEN a PUT /budgets/{id} would create a duplicate (category, month) combination (different id, same category+month), THE API SHALL return HTTP 409 Conflict
5. IF a PUT request is made to /budgets/{id} where id is not a valid numeric identifier, THEN THE API SHALL return HTTP 400

### Requirement B4: Delete Budget

**User Story:** As a user, I want to delete a budget, so that I can remove spending limits I no longer need.

#### Acceptance Criteria

1. WHEN a DELETE request is made to /budgets/{id} for an existing budget, THE API SHALL remove the budget and return HTTP 204 No Content
2. WHEN a DELETE request targets a non-existent budget id, THE API SHALL return HTTP 404 with a JSON error message
3. WHEN a budget is deleted, THE API SHALL ensure it no longer appears in subsequent GET /budgets or GET /budgets/status responses
4. IF a DELETE request is made to /budgets/{id} where id is not a valid numeric identifier, THEN THE API SHALL return HTTP 400

### Requirement B5: Budget Status

**User Story:** As a user, I want to see for each budget how much I have spent vs. my limit, so that I know whether I am on track.

#### Acceptance Criteria

1. WHEN a GET request is made to /budgets/status, THE API SHALL return HTTP 200 with a JSON array containing one entry per budget, each entry including id, category, month, limitAmount, actual, and remaining
2. FOR each budget status entry, `actual` SHALL equal the arithmetic sum of all expense amounts whose category exactly matches the budget category and whose date falls within the same calendar month (year and month of the expense date match the budget month string)
3. FOR each budget status entry, `remaining` SHALL equal `limitAmount - actual`
4. WHEN a budget has no matching expenses, `actual` SHALL be 0.00 and `remaining` SHALL equal `limitAmount`
5. WHEN no budgets exist, THE API SHALL return HTTP 200 with an empty JSON array for the status endpoint

### Requirement B6: Frontend Budgets Tab

**User Story:** As a user, I want a dedicated Budgets tab in the application, so that budget management is clearly separated from expense tracking.

#### Acceptance Criteria

1. THE Frontend SHALL display a tab bar with at least two tabs: "Expenses" (default active) and "Budgets"
2. WHEN the user clicks the "Expenses" tab, THE Frontend SHALL show the existing expense form, filter, list, and summary components
3. WHEN the user clicks the "Budgets" tab, THE Frontend SHALL show the budget form and budget status list
4. WHEN the user switches tabs, THE Frontend SHALL preserve the state of the inactive tab (e.g., unsaved form values are not cleared by tab switching alone)

### Requirement B7: Frontend Budget Form

**User Story:** As a user, I want a form to set a budget, so that I can quickly add or edit spending limits.

#### Acceptance Criteria

1. THE Frontend SHALL display a form with input fields for category (text), month (month picker), and limitAmount (number); all three are required
2. WHEN the user submits the form with valid data, THE Frontend SHALL send a POST /budgets request and refresh the budget status list on success
3. WHEN the user edits an existing budget, THE Frontend SHALL populate the form fields with existing values and send a PUT /budgets/{id} request on submission
4. WHEN a create or update succeeds, THE Frontend SHALL clear the form fields and exit edit mode
5. THE Frontend SHALL prevent form submission when required fields are empty or invalid and SHALL display validation indicators on invalid fields
6. IF the API returns an error response on form submission, THE Frontend SHALL display an error message and preserve the form values

### Requirement B8: Frontend Budget Status List

**User Story:** As a user, I want to see my budgets with limit, actual spend, and remaining amount, so that I can manage my finances at a glance.

#### Acceptance Criteria

1. THE Frontend SHALL display a list (or table) of all budget statuses fetched from GET /budgets/status, showing category, month, limitAmount, actual, and remaining for each row
2. THE Frontend SHALL display a visual indicator (progress bar or color) on each row showing the proportion of the limit that has been spent
3. WHEN `remaining < 0` (over budget), THE Frontend SHALL display the row (or at minimum the remaining amount) with a visually distinct style (e.g., red color)
4. WHEN `remaining >= 0`, THE Frontend SHALL display the row with a normal or positive style (e.g., green color)
5. THE Frontend SHALL provide an edit button per row that populates the budget form with that row's values and switches to edit mode
6. THE Frontend SHALL provide a delete button per row that sends a DELETE /budgets/{id} request and refreshes the status list on success
7. WHEN no budgets exist, THE Frontend SHALL display an empty-state message in the budget list area
8. THE Frontend SHALL refresh the budget status list after any successful create, update, or delete operation on a budget

### Requirement B9: Database Schema

**User Story:** As a developer, I want a well-defined database schema for budgets, so that budget data is stored consistently.

#### Acceptance Criteria

1. THE System SHALL store budgets in a PostgreSQL table named "budgets" with columns: id (BIGSERIAL, primary key), category (VARCHAR(255), non-null), month (VARCHAR(7), non-null), limit_amount (NUMERIC(19,2), non-null), created_at (TIMESTAMP, non-null)
2. THE System SHALL enforce a unique constraint on the (category, month) column pair
3. THE System SHALL auto-generate a unique numeric id for each budget record using an identity generation strategy
4. WHEN a new budget is inserted, THE System SHALL automatically set created_at to the current timestamp
5. THE System SHALL NOT allow created_at to be modified after initial insertion
