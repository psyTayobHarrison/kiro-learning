# Tech Stack

## Backend

- **Language:** Java 17
- **Framework:** Spring Boot 3.2.5
- **Build tool:** Maven (wrapper not included, use system `mvn`)
- **Database:** PostgreSQL (localhost:5432, database `budgettracker`)
- **ORM:** Spring Data JPA / Hibernate
- **Validation:** Jakarta Bean Validation (spring-boot-starter-validation)
- **Testing:** JUnit 5 (via spring-boot-starter-test), Mockito, jqwik 1.8.4 (property-based testing), H2 (in-memory test DB)

## Frontend

- **Language:** TypeScript ~5.7
- **Framework:** Angular 19 (standalone components, no NgModules)
- **Forms:** Angular Reactive Forms
- **HTTP:** Angular HttpClient with RxJS Observables
- **Styling:** Plain CSS (no UI framework)
- **Testing:** Karma + Jasmine
- **Package manager:** npm

## Common Commands

### Backend (run from `backend/`)

| Action | Command |
|--------|---------|
| Build (skip tests) | `mvn clean package -DskipTests` |
| Run tests | `mvn test` |
| Run application | `mvn spring-boot:run` |
| Clean | `mvn clean` |

### Frontend (run from `frontend/`)

| Action | Command |
|--------|---------|
| Install dependencies | `npm install` |
| Start dev server (port 4200, proxied to backend) | `npm start` |
| Production build | `npm run build` |
| Run tests | `npm test` |

## Dev Environment Notes

- Frontend dev server proxies `/api/*` to `http://localhost:8080` (path rewrite strips `/api` prefix).
- Backend listens on port 8080; CORS is configured to allow `http://localhost:4200`.
- PostgreSQL must be running locally before starting the backend.
- Test profile uses H2 in-memory database (see `backend/src/test/resources/application.properties`).
