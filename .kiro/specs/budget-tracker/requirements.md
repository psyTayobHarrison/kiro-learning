# Requirements Document

## Introduction

Budget Tracker is a full-stack expense tracking application consisting of a Spring Boot REST API backend with PostgreSQL persistence and an Angular single-page application frontend. The system enables users to create, view, update, and delete expenses, filter expenses by category, and view spending summaries grouped by category. There is no authentication or pagination; the focus is on simplicity and clarity.

## Glossary

- **System**: The Budget Tracker application as a whole (backend API + frontend SPA)
- **API**: The Spring Boot REST API backend exposing HTTP endpoints
- **Frontend**: The Angular single-page application that communicates with the API
- **Expense**: A financial record containing an amount, category, date, and optional description
- **Category**: A non-blank string label used to classify expenses (e.g., "food", "transport")
- **Summary**: An aggregation showing the total amount spent per category
- **ExpenseCreateDTO**: The data transfer object containing amount, category, date, and optional description used for creating or updating expenses
- **CategorySummaryDTO**: The data transfer object containing a category name and its total spending amount

## Requirements

### Requirement 1: Create Expense

**User Story:** As a user, I want to create a new expense record, so that I can track my spending.

#### Acceptance Criteria

1. WHEN a valid ExpenseCreateDTO is submitted via POST /expenses, THE API SHALL persist a new expense and return HTTP 201 with a JSON body containing the expense's generated id, amount, category, date, description, and createdAt timestamp
2. WHEN an ExpenseCreateDTO with amount less than 0.01 is submitted, THE API SHALL reject the request and return HTTP 400 with a JSON response indicating which field failed validation and the reason
3. WHEN an ExpenseCreateDTO with a blank category is submitted, THE API SHALL reject the request and return HTTP 400 with a JSON response indicating which field failed validation and the reason
4. WHEN an ExpenseCreateDTO with a null date is submitted, THE API SHALL reject the request and return HTTP 400 with a JSON response indicating which field failed validation and the reason
5. WHEN a malformed JSON body is submitted via POST /expenses, THE API SHALL return HTTP 400 with a JSON response containing an error message indicating the parse failure
6. WHEN an ExpenseCreateDTO with a category longer than 255 characters or a description longer than 1000 characters is submitted, THE API SHALL reject the request and return HTTP 400 with a JSON response indicating which field failed validation and the reason
7. WHEN multiple validation errors exist in the submitted ExpenseCreateDTO, THE API SHALL return HTTP 400 with a JSON response listing all field-level validation errors

### Requirement 2: List Expenses

**User Story:** As a user, I want to view all my expenses, so that I can review my spending history.

#### Acceptance Criteria

1. WHEN a GET request is made to /expenses without a category parameter, THE API SHALL return HTTP 200 with a JSON array where each element contains the fields id, amount, category, date, description, and createdAt for every persisted expense
2. WHEN a GET request is made to /expenses with a category query parameter, THE API SHALL return HTTP 200 with a JSON array containing only expenses whose category field is an exact case-sensitive match to the provided parameter value
3. WHEN no expenses exist in the database, THE API SHALL return HTTP 200 with an empty JSON array
4. WHEN a GET request is made to /expenses with a blank or empty category query parameter, THE API SHALL treat it as if no category filter was provided and return all expenses

### Requirement 3: Update Expense

**User Story:** As a user, I want to update an existing expense, so that I can correct mistakes in my records.

#### Acceptance Criteria

1. WHEN a valid ExpenseCreateDTO is submitted via PUT /expenses/{id} for an existing expense, THE API SHALL update the expense fields (amount, category, date, description) and return HTTP 200 with the full updated expense including id, amount, category, date, description, and createdAt, where the returned amount, category, date, and description match the submitted values
2. WHEN a PUT request targets a non-existent expense id, THE API SHALL return HTTP 404 with an error message indicating the expense was not found
3. WHEN an expense is successfully updated via PUT /expenses/{id}, THE API SHALL preserve the original id and createdAt values unchanged in both the response and the persisted record
4. WHEN an ExpenseCreateDTO with amount less than or equal to zero, a blank category, or a null date is submitted via PUT /expenses/{id}, THE API SHALL reject the request and return HTTP 400 with field-level error details identifying the invalid fields
5. IF a PUT request is made to /expenses/{id} where id is not a valid numeric identifier, THEN THE API SHALL return HTTP 400

### Requirement 4: Delete Expense

**User Story:** As a user, I want to delete an expense, so that I can remove records I no longer need.

#### Acceptance Criteria

1. WHEN a DELETE request is made to /expenses/{id} for an existing expense, THE API SHALL remove the expense from the database and return HTTP 204 No Content with an empty response body
2. WHEN a DELETE request targets a non-existent expense id, THE API SHALL return HTTP 404 with a JSON error message indicating the expense was not found
3. WHEN an expense is deleted, THE API SHALL ensure that expense no longer appears in subsequent GET /expenses responses
4. IF a DELETE request is made to /expenses/{id} where id is not a valid numeric identifier, THEN THE API SHALL return HTTP 400

### Requirement 5: Spending Summary

**User Story:** As a user, I want to view a summary of spending by category, so that I can understand where my money goes.

#### Acceptance Criteria

1. WHEN a GET request is made to /expenses/summary, THE API SHALL return HTTP 200 with a JSON array containing exactly one CategorySummaryDTO object per distinct category present in the database
2. THE API SHALL compute each category total as the arithmetic sum of all expense amounts belonging to that category, including all persisted expenses regardless of date
3. WHEN no expenses exist in the database, THE API SHALL return HTTP 200 with an empty JSON array for the summary endpoint

### Requirement 6: Frontend Expense Form

**User Story:** As a user, I want a form to enter expense details, so that I can quickly add new expenses.

#### Acceptance Criteria

1. THE Frontend SHALL display a form with input fields for amount (numeric), category (text), date (date picker), and description (text), where amount, category, and date are marked as required
2. WHEN the user submits the form with valid data (amount > 0, non-blank category, non-null date), THE Frontend SHALL send a POST request to the API and refresh the expense list upon success
3. WHEN the user edits an existing expense, THE Frontend SHALL populate the form fields with the existing expense values and send a PUT request to /expenses/{id} on submission
4. WHEN a create or update operation succeeds, THE Frontend SHALL clear the form fields, reset the amount to empty, and exit edit mode
5. THE Frontend SHALL prevent form submission when required fields (amount, category, date) are empty or invalid and SHALL display validation indicators on the invalid fields
6. IF the API returns an error response on form submission, THEN THE Frontend SHALL display an error message to the user and preserve the form values

### Requirement 7: Frontend Expense List

**User Story:** As a user, I want to see all my expenses in a list, so that I can browse and manage them.

#### Acceptance Criteria

1. WHEN the page loads, THE Frontend SHALL fetch all expenses from the API and display them in a list showing amount, category, date, and description for each expense
2. THE Frontend SHALL provide a delete button for each expense in the list
3. THE Frontend SHALL provide an edit button for each expense in the list
4. WHEN the user clicks delete on an expense, THE Frontend SHALL send a DELETE request to the API and refresh the expense list and summary view upon success
5. WHEN the user clicks edit on an expense, THE Frontend SHALL populate the expense form with that expense's values and switch the form to edit mode
6. IF no expenses exist, THEN THE Frontend SHALL display the list area with an empty-state message indicating no expenses have been recorded

### Requirement 8: Frontend Category Filter

**User Story:** As a user, I want to filter expenses by category, so that I can focus on specific types of spending.

#### Acceptance Criteria

1. THE Frontend SHALL display a category filter input field above or adjacent to the expense list
2. WHEN the user enters a non-empty category filter value, THE Frontend SHALL send a GET request to /expenses?category={value} using the exact entered value and display only the returned expenses
3. WHEN the category filter is cleared (empty string or blank value), THE Frontend SHALL send a GET request to /expenses without a category parameter and display all expenses
4. IF no expenses match the current category filter, THEN THE Frontend SHALL display the list area with an indication that no expenses match the filter

### Requirement 9: Frontend Summary View

**User Story:** As a user, I want to see spending totals by category, so that I can understand my spending distribution.

#### Acceptance Criteria

1. THE Frontend SHALL display a summary table with two columns: category name and total amount, showing one row per category with its total formatted to exactly 2 decimal places
2. WHEN expenses are added, updated, or deleted, THE Frontend SHALL fetch updated summary data from the API and re-render the summary table to reflect current totals
3. IF no expenses exist, THEN THE Frontend SHALL display the summary table structure with no data rows or an empty-state indication

### Requirement 10: CORS Configuration

**User Story:** As a developer, I want the backend to allow cross-origin requests from the Angular dev server, so that the frontend can communicate with the API during development.

#### Acceptance Criteria

1. THE API SHALL include the Access-Control-Allow-Origin header with value http://localhost:4200 in responses to requests originating from http://localhost:4200
2. THE API SHALL permit GET, POST, PUT, and DELETE HTTP methods for cross-origin requests from http://localhost:4200
3. WHEN the browser sends a preflight OPTIONS request for a cross-origin request from http://localhost:4200, THE API SHALL respond with appropriate CORS headers permitting the actual request to proceed
4. THE API SHALL apply CORS permissions to all API endpoints under the /expenses path
5. IF a cross-origin request originates from an origin other than http://localhost:4200, THEN THE API SHALL not include permissive CORS headers in the response

### Requirement 11: Database Schema

**User Story:** As a developer, I want a well-defined database schema, so that expense data is stored consistently and reliably.

#### Acceptance Criteria

1. THE System SHALL store expenses in a PostgreSQL table named "expenses" with columns: id (BIGINT, primary key), amount (NUMERIC, non-null), category (VARCHAR(255), non-null), date (DATE, non-null), description (VARCHAR(1000), nullable), created_at (TIMESTAMP, non-null)
2. THE System SHALL auto-generate a unique numeric id for each expense record using an identity generation strategy
3. THE System SHALL store the amount as NUMERIC type preserving at least 2 decimal places of precision
4. THE System SHALL enforce that amount, category, and date columns are non-nullable at the database level
5. WHEN a new expense record is inserted, THE System SHALL automatically set the created_at column to the current timestamp at the moment of insertion
6. THE System SHALL NOT allow the created_at column to be modified after initial insertion

