## Goal
Learn Java, Spring Boot, and GraphQL by iteratively evolving the existing task-manager app.

## Milestones and Hands-on Tasks

### 1) Java foundations (1–2 days)
- **Goal**: Solidify OOP, collections, streams, exceptions.
- **Do**: Refactor `Task` with constructors, equals/hashCode/toString; add a `TaskStatus` enum and migrate usage.

### 2) Spring Boot basics (0.5–1 day)
- **Goal**: Understand DI, configuration, profiles.
- **Do**: Add `application-local.properties` and `application-prod.properties`; inject config into `TaskService` (e.g., max title length).

Profile usage:
- Local dev: set `SPRING_PROFILES_ACTIVE=local` (already in `run-dev.sh`).
- Prod: set `SPRING_PROFILES_ACTIVE=prod` and provide datasource env vars.
- Tunables: `task.title.max-length` controls title length validation.

### 3) JPA & H2 persistence (1 day)
- **Goal**: CRUD with Spring Data JPA.
- **Do**: Add fields to `Task` (e.g., `createdAt`, `updatedAt`, `dueDate`); use JPA auditing for timestamps; write a repository method `findByCompleted(boolean)` and expose it via GraphQL.

### 4) GraphQL essentials (1 day)
- **Goal**: Queries, mutations, schema design.
- **Do**:
  - Convert to input types: `CreateTaskInput`, `UpdateTaskInput`
  - Change schema to: `createTask(input: CreateTaskInput!): Task!` and `updateTask(id: ID!, input: UpdateTaskInput!): Task!`
  - Update `TaskController` to accept `@Argument("input")` DTOs.

### 5) Validation and errors (0.5–1 day)
- **Goal**: Validate inputs and return structured errors.
- **Do**: Add `jakarta.validation` annotations to input DTOs; add a `@ControllerAdvice` with Spring GraphQL error mapping; return clear messages for validation failures and not-found cases.

### 6) Testing strategy (1–2 days)
- **Goal**: Unit and integration tests.
- **Do**:
  - Unit test `TaskService` with mocked repository (happy/edge paths).
  - Integration test GraphQL API with `GraphQlTester` (create, update partial, delete, query).
  - Seed test data with an embedded H2 profile.

### 7) Advanced GraphQL (1–2 days)
- **Goal**: Performance and API ergonomics.
- **Do**:
  - Add pagination: `tasks(page: Int = 0, size: Int = 10)`.
  - Add filtering/sorting: e.g., by `completed`, `titleContains`, `sortBy`.
  - Introduce DataLoader patterns when you add relations (e.g., `User` → `tasks`) to avoid N+1.

### 8) Security (1–2 days)
- **Goal**: Secure mutations and sensitive queries.
- **Do**:
  - Add Spring Security; start with in-memory users/roles.
  - Secure mutations with method-level security (`@PreAuthorize`).
  - Later, swap to JWT.

### 9) Observability (0.5 day)
- **Goal**: Visibility and diagnostics.
- **Do**: Enable Actuator; add request logging around GraphQL; add metrics (e.g., mutation counts, latency).

### 10) CI/CD & Docker (1 day)
- **Goal**: Build and run anywhere.
- **Do**:
  - Use your `Dockerfile` to build an image; run locally.
  - Add GitHub Actions: build, run tests, and publish Docker image on push.

### 11) Stretch features (as time allows)
- **Goal**: Explore real-world patterns.
- **Do**:
  - Add `Tag` entity with many-to-many to `Task`.
  - Add `Subtask` entity (one-to-many).
  - Add `dueDate` and a `DateTime` scalar; implement “overdue” queries.
  - Add subscriptions (WebSocket) for real-time task updates.

## Repository Checkpoints
- Create a branch per milestone: `feature/mX-topic`.
- Open a small PR and write a short README section for what you learned.
- Maintain a `CHANGELOG.md` to document schema changes (inputs, pagination, etc.).

## Reference GraphQL Queries/Mutations

Create using input types (after milestone 4):
```graphql
mutation {
  createTask(input: { title: "Read docs", description: "Spring GraphQL", completed: false }) {
    id
    title
    completed
  }
}
```

Partial update:
```graphql
mutation {
  updateTask(id: 1, input: { completed: true }) {
    id
    title
    completed
  }
}
```

List with pagination/filter:
```graphql
query {
  tasks(page: 0, size: 5, completed: false, titleContains: "doc") {
    id
    title
    completed
  }
}
```

## What to Measure
- Code coverage for `TaskService` and GraphQL controllers.
- p95 latency for create/update queries (Actuator + Micrometer timers).
- Security: verify unauthorized mutations are blocked.

---

If you want, start with Milestone 4 (switch to input types) or Milestone 6 (add tests), and iterate.


