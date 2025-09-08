# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building and Running
- **Start application**: `mvn spring-boot:run` or `./run-dev.sh` (sets local profile)
- **Run tests**: `mvn test`
- **Build**: `mvn clean compile`
- **Package**: `mvn clean package`

### Client Testing
- **Run Python client**: `./run_client.sh` (sets up venv and dependencies automatically)
- **Quick API test**: Use option 1 from the client menu for automated testing
- **Interactive testing**: Use option 2 from the client menu

### Security and Observability Testing
- **Test JWT authentication**: `./test-security.sh`
- **Test monitoring features**: `./test-observability.sh`

## Architecture Overview

This is a **Spring Boot GraphQL Task Manager** with JWT authentication and comprehensive monitoring.

### Core Architecture
- **Framework**: Spring Boot 3.5.4 with Java 21
- **API**: GraphQL (primary) + REST (auth endpoints only)
- **Database**: H2 (in-memory) with JPA/Hibernate
- **Security**: JWT tokens with Spring Security
- **Monitoring**: Micrometer + Prometheus + Spring Actuator

### Package Structure
```
com.projects.taskmanager/
├── controller/          # GraphQL resolvers (TaskController, UserController) + REST auth
├── model/              # JPA entities (Task, User, Role, TaskStatus)
├── service/            # Business logic (TaskService, UserService)
├── repository/         # JPA repositories
├── graphql/            # GraphQL-specific (inputs, context, exception handling)
├── security/           # JWT utilities, filters, user details
├── observability/      # Metrics, logging interceptors
├── config/             # Spring configuration, data loading
├── dto/                # REST DTOs for authentication
└── util/               # Utilities (TextNormalizer)
```

### Key Design Patterns
- **GraphQL Schema-First**: Schema defined in `src/main/resources/schema.graphqls`
- **DataLoader Pattern**: Optimizes N+1 queries for user-task relationships
- **Repository Pattern**: JPA repositories with custom queries
- **JWT Stateless Auth**: Tokens validated on each request
- **Correlation ID Logging**: All requests tracked with unique IDs
- **Custom Metrics**: Task operations, auth events, GraphQL timings

### Database Schema
- **Users**: Many-to-many relationship with Tasks via `user_tasks` junction table
- **Tasks**: Can be assigned to multiple users
- **Roles**: USER and ADMIN with different GraphQL permissions

### Security Model
- **Authentication**: REST endpoints (`/auth/register`, `/auth/login`)
- **Authorization**: JWT required for all GraphQL operations
- **User Context**: GraphQL context contains authenticated user for each request
- **Role-based Access**: Different permissions for USER vs ADMIN roles

### Monitoring Features
- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics` (custom task/user/auth metrics)
- **Prometheus**: `/actuator/prometheus`
- **Structured Logging**: Correlation IDs in all log entries
- **GraphQL Interceptor**: Request/response logging and timing

### Testing Strategy
- **Unit Tests**: Service layer with mocking
- **Integration Tests**: Full GraphQL flow testing
- **Client Testing**: Python CLI client with comprehensive test scenarios
- **Security Testing**: JWT token validation and unauthorized access
- **Observability Testing**: Metrics collection and endpoint availability

### Sample Data Population
The application automatically loads sample data on startup via DataLoader:
- **Default Users**: `admin/admin123` (ADMIN role), `user/user123` (USER role)
- **Sample Tasks**: 5 diverse tasks with various statuses, due dates, estimation hours, and user assignments
- **Task Features**: Demonstrates decimal estimation hours (2.5, 6.5h), different statuses (TODO, IN_PROGRESS, DONE), and multi-user assignments

### Development Notes
- **Profile**: Use `local` profile for development (set by `run-dev.sh`)
- **Hot Reload**: DevTools enabled for automatic restarts
- **H2 Console**: Available when running with appropriate configuration
- **GraphQL Playground**: Available at `/graphiql` when enabled
- **Client Dependencies**: Python client auto-installs dependencies via `run_client.sh`

### Frontend Client (SvelteKit)
**Location**: `../task-manager-web/` (separate repository)
- **Framework**: SvelteKit with TypeScript and TailwindCSS
- **Features**: Authentication, task CRUD, user assignment, filtering, search, sorting
- **API Integration**: GraphQL client with proper error handling and loading states
- **UI Components**: Responsive design with protected routes and real-time updates

### Recent Enhancements
- **User Assignment System**: Complete many-to-many user-task relationships with GraphQL mutations
- **Decimal Hours Support**: Task estimation hours now support decimal values (e.g., 2.5 hours)
- **Enhanced DataLoader**: Automatic population of realistic sample data on startup
- **Status Field**: Fixed GraphQL schema to include status updates in UpdateTaskInput

### Development Roadmap
See `ROADMAP.md` for comprehensive 5-phase development plan covering:
- Phase 1: Enhanced UX (bulk actions, real-time updates, templates)
- Phase 2: Advanced features (dependencies, subtasks, comments, files)
- Phase 3: Data management (pagination, offline, export/import)
- Phase 4: Team collaboration (projects, permissions, notifications)  
- Phase 5: Analytics & reporting (dashboards, time tracking, insights)

### Common Tasks
1. **Adding new GraphQL operations**: Update schema.graphqls, create input DTOs, implement resolver in controller
2. **Adding metrics**: Use MetricsService to increment counters or time operations
3. **Database changes**: Update entity models, repository methods auto-generated by Spring Data
4. **Authentication changes**: Modify JWT utilities and security configuration
5. **Testing new features**: Use Python client's test scenario (option 12) for end-to-end validation
6. **Schema changes**: Remember to restart backend to reload GraphQL schema properly
### Agent Workflows
- Agent task: commit changes ([docs/agent-commit-workflow.md](docs/agent-commit-workflow.md))
