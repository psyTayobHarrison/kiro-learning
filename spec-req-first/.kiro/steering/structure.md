# Project Structure

Monorepo with two top-level directories: `backend/` (Spring Boot API) and `frontend/` (Angular SPA).

## Backend (`backend/`)

```
backend/
├── pom.xml
├── src/main/java/com/budgettracker/
│   ├── BudgetTrackerApplication.java      # Spring Boot entry point
│   ├── config/
│   │   └── CorsConfig.java               # CORS configuration
│   ├── controller/
│   │   └── ExpenseController.java         # REST endpoints (/expenses)
│   ├── dto/
│   │   ├── CategorySummary.java           # Summary projection DTO
│   │   ├── ErrorResponse.java             # Standardized error body
│   │   ├── ExpenseRequest.java            # Inbound request with validation
│   │   └── ExpenseResponse.java           # Outbound response shape
│   ├── exception/
│   │   ├── ExpenseNotFoundException.java  # 404 custom exception
│   │   └── GlobalExceptionHandler.java    # @ControllerAdvice error handling
│   ├── model/
│   │   └── Expense.java                   # JPA entity
│   ├── repository/
│   │   └── ExpenseRepository.java         # Spring Data JPA repository
│   └── service/
│       └── ExpenseService.java            # Business logic layer
├── src/main/resources/
│   └── application.properties             # DB and JPA config
└── src/test/
    ├── java/com/budgettracker/
    │   ├── controller/
    │   │   └── ExpenseControllerTest.java # @WebMvcTest controller tests
    │   ├── property/
    │   │   └── CreateRetrieveRoundTripProperties.java  # jqwik property tests
    │   └── service/
    │       └── ExpenseServiceTest.java    # Unit tests with Mockito
    └── resources/
        └── application.properties         # H2 test config
```

## Frontend (`frontend/`)

```
frontend/
├── angular.json
├── package.json
├── proxy.conf.json                        # Dev proxy /api → localhost:8080
├── tsconfig.json
├── src/
│   ├── index.html
│   ├── main.ts                            # Bootstrap standalone app
│   ├── styles.css                         # Global styles
│   └── app/
│       ├── app.component.ts/html/css      # Root component (layout)
│       ├── app.config.ts                  # App-level providers (HttpClient, etc.)
│       ├── models/
│       │   └── expense.model.ts           # TypeScript interfaces
│       ├── services/
│       │   └── expense.service.ts         # HTTP service (Observables)
│       └── components/
│           ├── expense-form/              # Create/edit expense form
│           ├── expense-list/              # Expense table with delete/edit
│           ├── category-filter/           # Category dropdown filter
│           └── spending-summary/          # Category totals table
```

## Architecture Patterns

- **Backend layering:** Controller → Service → Repository. No logic in controllers beyond delegation and input wiring.
- **DTOs separate from entities:** Request/response objects live in `dto/`; JPA entity in `model/`. Manual mapping in the service layer (no MapStruct or Lombok).
- **Centralized error handling:** `GlobalExceptionHandler` translates exceptions to consistent `ErrorResponse` bodies.
- **Frontend component structure:** Each feature is a standalone component in its own directory under `components/`. Shared models and services live in `models/` and `services/`.
- **Communication pattern:** Components emit events upward; the root `AppComponent` orchestrates refresh flows between siblings.
