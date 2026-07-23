# Requirements Document

## Introduction

Budget Tracker is a full-stack expense tracking application. The backend is a Spring Boot REST API backed by PostgreSQL, and the frontend is a single-page Angular application. Users can create, read, update, and delete expenses, filter expenses by category, and view a spending summary grouped by category. There is no authentication or pagination.

## Glossary

- **API**: The Spring Boot REST API that exposes expense management endpoints
- **Expense**: A record representing a single financial expenditure, consisting of an amount, category, date, and optional description
- **Expense_Table**: The PostgreSQL table named "expenses" storing all expense records
- **Frontend**: The Angular single-page application that provides the user interface
- **Summary**: An aggregation of total spending grouped by category

## Requirements

### Requirement 1: Create an Expense

**User Story:** As a user, I want to create a new expense record, so that I can track my spending.

#### Acceptance Criteria

1. WHEN a POST request with a valid payload containing amount, category, and date is received at /expenses, THE API SHALL persist the expense to the Expense_Table and return the created expense including id, amount, category, date, description, and created_at fields with HTTP status 201.
2. IF a POST request is missing amount, category, or date, THEN THE API SHALL perform atomic validation and return an HTTP 400 response with an error message indicating which required fields are missing, without persisting any partial data to the Expense_Table.
3. IF the amount field is not a positive number between 0.01 and 999,999,999.99, THEN THE API SHALL return an HTTP 400 response with an error message indicating the valid amount range.
4. IF the date field is not a valid date in ISO 8601 format (YYYY-MM-DD), THEN THE API SHALL return an HTTP 400 response with an error message indicating the expected date format.
5. WHEN a valid expense is persisted, THE API SHALL automatically populate the id field with a unique identifier and the created_at field with the current UTC timestamp.
6. IF the category value exceeds 50 characters or is empty, THEN THE API SHALL return an HTTP 400 response with an error message indicating the valid category length between 1 and 50 characters.

### Requirement 2: List Expenses

**User Story:** As a user, I want to list all my expenses, so that I can review my spending history.

#### Acceptance Criteria

1. WHEN a GET request is received at /expenses without query parameters, THE API SHALL return all expenses from the Expense_Table as a JSON array with HTTP status 200, where each element contains the expense id, amount, category, description, and date fields.
2. WHEN a GET request is received at /expenses with a category query parameter, THE API SHALL return only expenses whose category field exactly matches the specified category value as a JSON array with HTTP status 200.
3. WHEN no expenses exist matching the request criteria, THE API SHALL return an empty JSON array [] with HTTP status 200.
4. IF a GET request is received at /expenses with a category query parameter that contains an empty string, THEN THE API SHALL return a 400 HTTP status with an error message indicating the category parameter must be non-empty.
5. WHEN a GET request is received at /expenses, THE API SHALL return expenses sorted by date in descending order (most recent first).
6. IF an internal error occurs while retrieving expenses from the Expense_Table, THEN THE API SHALL return an HTTP status 500 with an error message indicating the retrieval failure, without returning any partial data.

### Requirement 3: Update an Expense

**User Story:** As a user, I want to update an existing expense, so that I can correct mistakes in my records.

#### Acceptance Criteria

1. WHEN a PUT request with a valid payload containing amount, category, and date is received at /expenses/{id} for an existing expense, THE API SHALL update all provided fields of the expense in the Expense_Table and return the updated expense including id, amount, category, date, description, and created_at fields with HTTP status 200.
2. IF a PUT request is received at /expenses/{id} for a non-existent expense, THEN THE API SHALL return an HTTP 404 response with an error message indicating the expense was not found.
3. IF a PUT request is missing amount, category, or date, THEN THE API SHALL return an HTTP 400 response with an error message indicating which required fields are missing.
4. IF the amount field is not a positive number between 0.01 and 999,999,999.99, THEN THE API SHALL return an HTTP 400 response with an error message indicating the valid amount range.
5. IF the date field is not a valid date in ISO 8601 format (YYYY-MM-DD), THEN THE API SHALL return an HTTP 400 response with an error message indicating the expected date format.
6. THE API SHALL NOT modify the created_at field when updating an expense.

### Requirement 4: Delete an Expense

**User Story:** As a user, I want to delete an expense, so that I can remove incorrect or unwanted records.

#### Acceptance Criteria

1. WHEN a DELETE request is received at /expenses/{id} for an existing expense, THE API SHALL remove the expense from the Expense_Table and return HTTP status 204 with no response body.
2. IF a DELETE request is received at /expenses/{id} for a non-existent expense, THEN THE API SHALL return an HTTP 404 response with an error message indicating the expense was not found.
3. IF a DELETE request is received at /expenses/{id} where id is not a valid identifier format, THEN THE API SHALL return an HTTP 400 response with an error message indicating the invalid identifier.

### Requirement 5: Spending Summary by Category

**User Story:** As a user, I want to see my total spending grouped by category, so that I can understand where my money goes.

#### Acceptance Criteria

1. WHEN a GET request is received at /expenses/summary, THE API SHALL return a JSON array where each element contains a category field and a totalAmount field representing the sum of all expense amounts for that category, with HTTP status 200.
2. WHEN a GET request is received at /expenses/summary and no expenses exist, THE API SHALL return an empty JSON array [] with HTTP status 200.
3. THE API SHALL calculate totalAmount by summing all amount values for each distinct category in the Expense_Table.
4. THE API SHALL return totalAmount values rounded to two decimal places.

### Requirement 6: Database Schema

**User Story:** As a developer, I want a well-defined database schema, so that expense data is stored reliably and consistently.

#### Acceptance Criteria

1. THE Expense_Table SHALL contain the columns: id (auto-generated primary key), amount (numeric/decimal), category (varchar), date (date type), description (varchar, nullable), and created_at (timestamp).
2. THE Expense_Table SHALL enforce NOT NULL constraints on the id, amount, category, date, and created_at columns.
3. THE API SHALL use Spring Data JPA for the entity and repository layer to interact with the Expense_Table.
4. THE amount column SHALL store values with precision sufficient for two decimal places.

### Requirement 7: Database Configuration

**User Story:** As a developer, I want the application configured to connect to a local PostgreSQL instance, so that the backend can persist data.

#### Acceptance Criteria

1. THE API SHALL include an application.properties or application.yml file with PostgreSQL connection configuration specifying host (localhost), port (5432), database name, username, and password.
2. THE API SHALL configure Spring Data JPA to auto-generate or validate the database schema on startup using the spring.jpa.hibernate.ddl-auto property set to update or validate.
3. THE API SHALL include the PostgreSQL JDBC driver as a dependency.

### Requirement 8: Frontend Expense Form

**User Story:** As a user, I want a form to add new expenses, so that I can quickly record my spending.

#### Acceptance Criteria

1. THE Frontend SHALL display a form at the top of the page with fields for amount (numeric input), category (text input), date (date picker), and description (text input).
2. WHEN the user submits the form with valid data, THE Frontend SHALL send a POST request to the API and add the new expense to the displayed list without requiring a full page reload.
3. IF the user interacts with a required field (amount, category, or date) and leaves it empty, THEN THE Frontend SHALL display inline validation errors next to each invalid field in real-time and prevent submission.
4. IF the API returns an error in response to the POST request, THEN THE Frontend SHALL display an error message indicating the expense was not saved and preserve the user's entered data in the form.
5. WHEN the expense is successfully added to the displayed list, THE Frontend SHALL clear all form fields.

### Requirement 9: Frontend Expense List

**User Story:** As a user, I want to see all my expenses in a list, so that I can review them at a glance.

#### Acceptance Criteria

1. THE Frontend SHALL display all expenses in a list below the expense form, sorted by date descending (most recent first).
2. THE Frontend SHALL display each expense with its amount, category, date, and description.
3. THE Frontend SHALL provide a delete button for each expense in the list.
4. WHEN the user clicks the delete button, THE Frontend SHALL send a DELETE request to the API and remove the expense from the displayed list.
5. THE Frontend SHALL provide an edit option for each expense in the list.
6. WHEN the user activates the edit option, THE Frontend SHALL allow the user to modify the expense fields and submit the changes via a PUT request to the API.
7. WHEN no expenses exist, THE Frontend SHALL display an empty-state message indicating no expenses have been recorded.

### Requirement 10: Frontend Category Filter

**User Story:** As a user, I want to filter expenses by category, so that I can focus on specific types of spending.

#### Acceptance Criteria

1. THE Frontend SHALL provide a category filter control (dropdown or select element) populated with the distinct categories from current expenses.
2. WHEN the page loads, THE Frontend SHALL default the filter control to display "all categories" and show all expenses (no filtering applied), with both the displayed expenses and the filter control's visual state indicating that no category filter is active.
3. WHEN the user selects a category in the filter, THE Frontend SHALL display only expenses matching the selected category.
4. WHEN the user clears the category filter, THE Frontend SHALL display all expenses and the filter control SHALL return to a state visually identical to the initial page load state.

### Requirement 11: Frontend Spending Summary

**User Story:** As a user, I want to see a summary of my total spending per category, so that I can understand my spending distribution.

#### Acceptance Criteria

1. THE Frontend SHALL display a summary view showing total spend per category as a table or list, where each row contains the category name and the summed monetary amount formatted to two decimal places.
2. WHEN no expenses exist, THE Frontend SHALL hide the summary table and display only an empty-state message indicating no spending data is available.
3. WHEN expenses are added, updated, or deleted, THE Frontend SHALL refresh the summary to reflect the current data.
4. THE Frontend SHALL sort the summary rows alphabetically by category name.

### Requirement 12: Frontend Simplicity

**User Story:** As a developer, I want the frontend to remain simple and focused, so that the codebase is easy to maintain.

#### Acceptance Criteria

1. THE Frontend SHALL operate as a single-page application without authentication mechanisms.
2. THE Frontend SHALL not implement pagination for the expense list.
3. THE Frontend SHALL use basic CSS or Angular Material for styling without additional UI framework dependencies.
4. THE Frontend SHALL not include any charting or data visualization libraries.
