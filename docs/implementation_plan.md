# Replace Axon Framework with Standard Spring Boot Patterns

## Background

The codebase currently uses **Axon Framework** (v4.11.1) to implement CQRS (Command Query Responsibility Segregation) and Event Sourcing with a Saga pattern across 4 microservices: `book_service`, `employee_service`, `borrowing_service`, and `common_service`. Axon requires a dedicated **Axon Server** infrastructure component.

The goal is to **remove Axon entirely** and replace it with idiomatic Spring Boot patterns while **keeping the same CQRS-inspired package architecture** (command/query separation, event DTOs, service layers).

## User Review Required

> [!IMPORTANT]
> **Saga → Orchestration Service**: The `BorrowingSaga` currently coordinates a distributed transaction across `borrowing_service` and `book_service` using Axon's saga mechanism. The replacement will be an **orchestration-style service** within `borrowing_service` that uses **synchronous REST calls** (via `WebClient`/`RestClient`) to the `book_service` API to check availability and update status, with programmatic rollback logic. This is the most common Spring Boot approach for this type of coordination. If you prefer an event-driven approach using Kafka instead, please let me know.

> [!IMPORTANT]
> **Event Sourcing is dropped**: Axon's event sourcing (aggregate state rebuilt from events) will be replaced with **direct JPA persistence** (the standard Spring Boot approach). The `Aggregate` classes will become `@Service` classes. Events will still exist as in-process Spring `ApplicationEvent` objects for decoupling command handlers from side effects (like DB writes in event handlers), but there is no event store.

> [!WARNING]
> **Cross-service queries**: The current `BorrowingSaga` uses Axon's `QueryGateway` to query book/employee details across services. These will be replaced with **REST calls via Spring's `RestClient`** (or `WebClient`) through Eureka service discovery — the standard Spring Boot microservice pattern.

## Proposed Changes

### Axon Concept → Spring Boot Replacement Mapping

| Axon Concept | Spring Boot Replacement |
|---|---|
| `@Aggregate` | `@Service` (command handler service) |
| `@CommandHandler` | Regular service methods called by controllers |
| `@EventSourcingHandler` | Removed (direct JPA persistence in service) |
| `@EventHandler` | `@EventListener` (Spring's `ApplicationEventPublisher`) |
| `@QueryHandler` | Regular service/repository methods called by controllers |
| `CommandGateway` | Direct service method injection |
| `QueryGateway` | Direct service/repository injection (local) or `RestClient` (cross-service) |
| `AggregateLifecycle.apply()` | `ApplicationEventPublisher.publishEvent()` |
| `@Saga` + `@SagaEventHandler` | `@Service` orchestration with REST calls + `@Transactional` |
| `@TargetAggregateIdentifier` | Removed (plain field) |
| Axon Server | Removed entirely |

---

### common_service

Summary: Remove Axon dependency, remove `@TargetAggregateIdentifier` from commands, and add REST client configuration.

#### [MODIFY] [pom.xml](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/common_service/pom.xml)
- Remove `axon-spring-boot-starter` dependency
- Remove `guava` dependency (only needed for Axon)

#### [MODIFY] [UpdateBookStatusCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/common_service/src/main/java/com/ion/common_service/command/UpdateBookStatusCommand.java)
- Remove `@TargetAggregateIdentifier` annotation and Axon import

#### [MODIFY] [RollBackBookStatusCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/common_service/src/main/java/com/ion/common_service/command/RollBackBookStatusCommand.java)
- Remove `@TargetAggregateIdentifier` annotation and Axon import

---

### book_service

Summary: Replace Axon aggregate with a service layer, replace event sourcing with direct JPA + Spring events, replace query projection with a service, and merge command/query controllers.

#### [MODIFY] [pom.xml](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/pom.xml)
- Remove `axon-spring-boot-starter` dependency
- Remove `guava` dependency

#### [DELETE] [BookAggregate.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/java/com/ion/book_service/command/aggregate/BookAggregate.java)
- Aggregate functionality moves to a new `BookCommandService`

#### [NEW] `book_service/.../command/service/BookCommandService.java`
- `@Service` + `@Transactional` class
- Methods: `createBook()`, `updateBook()`, `deleteBook()`, `updateBookStatus()`, `rollbackBookStatus()`
- Uses `BookRepository` for direct JPA persistence
- Uses `ApplicationEventPublisher` to publish events after DB operations (preserving the event handler pattern for side effects)

#### [MODIFY] [BookCommandController.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/java/com/ion/book_service/command/controller/BookCommandController.java)
- Replace `CommandGateway` with `BookCommandService` injection
- Call service methods directly

#### [MODIFY] [BookEventHandler.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/java/com/ion/book_service/command/event/BookEventHandler.java)
- Remove class entirely — DB persistence is now handled directly in `BookCommandService`
- (Event handler pattern for DB writes is no longer needed since the service does CRUD directly)

#### [DELETE] [BookProjection.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/java/com/ion/book_service/query/projection/BookProjection.java)
- Query handling moves to a new `BookQueryService`

#### [NEW] `book_service/.../query/service/BookQueryService.java`
- `@Service` class
- Methods: `getAllBooks()`, `getBookDetail()`
- Uses `BookRepository` directly

#### [MODIFY] [BookQueryController.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/java/com/ion/book_service/query/controller/BookQueryController.java)
- Replace `QueryGateway` with `BookQueryService` injection

#### Remove Axon imports from all command classes:
#### [MODIFY] [CreateBookCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/java/com/ion/book_service/command/command/CreateBookCommand.java)
#### [MODIFY] [UpdateBookCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/java/com/ion/book_service/command/command/UpdateBookCommand.java)
#### [MODIFY] [DeleteBookCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/java/com/ion/book_service/command/command/DeleteBookCommand.java)
- Remove `@TargetAggregateIdentifier` and its import

#### [NEW] `book_service/.../command/controller/BookInternalController.java`
- A REST controller at `/api/internal/books` for cross-service calls from `borrowing_service`
- Endpoints: `PUT /{bookId}/status` (update book status), `PUT /{bookId}/status/rollback` (rollback status)
- This replaces the cross-service command dispatching that Axon Server handled

#### [MODIFY] [application.properties](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/book_service/src/main/resources/application.properties)
- Remove `axon.axonserver.servers` config

---

### employee_service

Summary: Same pattern as book_service — replace aggregate with service, replace projection with service.

#### [MODIFY] [pom.xml](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/pom.xml)
- Remove `axon-spring-boot-starter` dependency
- Remove `guava` dependency

#### [DELETE] [EmployeeAggregate.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/java/com/ion/employee_service/command/aggregate/EmployeeAggregate.java)

#### [NEW] `employee_service/.../command/service/EmployeeCommandService.java`
- `@Service` + `@Transactional` class
- Methods: `createEmployee()`, `updateEmployee()`, `deleteEmployee()`
- Uses `EmployeeRepository` directly

#### [MODIFY] [EmployeeCommandController.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/java/com/ion/employee_service/command/controller/EmployeeCommandController.java)
- Replace `CommandGateway` with `EmployeeCommandService`

#### [DELETE] [EmployeeEventHandler.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/java/com/ion/employee_service/command/event/EmployeeEventHandler.java)
- DB persistence is handled in `EmployeeCommandService`

#### [DELETE] [EmployeeProjection.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/java/com/ion/employee_service/query/projection/EmployeeProjection.java)

#### [NEW] `employee_service/.../query/service/EmployeeQueryService.java`
- `@Service` class
- Methods: `getAllEmployees()`, `getEmployeeDetail()`

#### [MODIFY] [EmployeeQueryController.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/java/com/ion/employee_service/query/controller/EmployeeQueryController.java)
- Replace `QueryGateway` with `EmployeeQueryService`

#### Remove Axon imports from all command classes:
#### [MODIFY] [CreateEmployeeCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/java/com/ion/employee_service/command/command/CreateEmployeeCommand.java)
#### [MODIFY] [UpdateEmployeeCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/java/com/ion/employee_service/command/command/UpdateEmployeeCommand.java)
#### [MODIFY] [DeleteEmployeeCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/java/com/ion/employee_service/command/command/DeleteEmployeeCommand.java)

#### [MODIFY] [application.properties](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/employee_service/src/main/resources/application.properties)
- Remove `axon.axonserver.servers` config

---

### borrowing_service (most complex — has Saga)

Summary: Replace Axon aggregate with a service, replace the **BorrowingSaga** with an orchestration service that uses REST calls.

#### [MODIFY] [pom.xml](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/borrowing_service/pom.xml)
- Remove `axon-spring-boot-starter` dependency
- Remove `guava` dependency
- Add `spring-boot-starter-webflux` (for `WebClient` to make REST calls to book/employee services via Eureka)

#### [DELETE] [BorrowingAggregate.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/borrowing_service/src/main/java/com/ion/borrowing_service/command/aggregate/BorrowingAggregate.java)

#### [DELETE] [BorrowingSaga.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/borrowing_service/src/main/java/com/ion/borrowing_service/command/saga/BorrowingSaga.java)

#### [DELETE] [BorrowingEventHandler.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/borrowing_service/src/main/java/com/ion/borrowing_service/command/event/BorrowingEventHandler.java)

#### [NEW] `borrowing_service/.../command/service/BorrowingCommandService.java`
- `@Service` + `@Transactional` class
- `createBorrowing()` implements the saga logic:
  1. Call book-service REST API to get book details → check `isReady`
  2. If book is not ready → throw exception (no borrowing created)
  3. Call employee-service REST API to get employee details → check `isDisciplined`
  4. If employee is disciplined → throw exception (no borrowing created)
  5. Call book-service REST API to update book status to `isReady=false`
  6. Save borrowing record to DB
  7. If any step fails after book status was updated → call book-service to rollback
- `deleteBorrowing()` — simple JPA delete

#### [NEW] `borrowing_service/.../command/config/WebClientConfig.java`
- Configure `WebClient.Builder` bean (load-balanced via `@LoadBalanced` for Eureka)

#### [MODIFY] [BorrowingCommandController.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/borrowing_service/src/main/java/com/ion/borrowing_service/command/controller/BorrowingCommandController.java)
- Replace `CommandGateway` with `BorrowingCommandService`

#### Remove Axon imports from command classes:
#### [MODIFY] [CreateBorrowingCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/borrowing_service/src/main/java/com/ion/borrowing_service/command/command/CreateBorrowingCommand.java)
#### [MODIFY] [DeleteBorrowingCommand.java](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/borrowing_service/src/main/java/com/ion/borrowing_service/command/command/DeleteBorrowingCommand.java)

#### [MODIFY] [application.properties](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/borrowing_service/src/main/resources/application.properties)
- Remove `axon.axonserver.servers` config

---

### docker-compose.yml

#### [MODIFY] [docker-compose.yml](file:///c:/Workspace/Personal/Spring-Boot/book-loan-microservices/docker-compose.yml)
- Remove `axonserver` service entirely
- Remove `axonserver` from `depends_on` of `book-service` and `notification-service`
- Remove `AXONIQ_AXONSERVER_SERVERS` environment variables

---

## Files Unchanged

The following files need **no changes**:
- All event DTOs (`BookCreatedEvent`, `BookUpdatedEvent`, etc.) — they have no Axon imports
- `BookStatusUpdatedEvent`, `BookStatusRollBackedEvent` — pure POJOs, no Axon
- `BookResponseCommonModel`, `EmployeeResponseCommonModel` — pure POJOs
- `GetBookDetailQuery`, `GetEmployeeDetailQuery`, `GetAllBookQuery`, `GetAllEmployeeQuery` — remain as query parameter objects
- Entity classes (`Book`, `Employee`, `Borrowing`) — unchanged JPA entities
- Repository interfaces — unchanged
- `KafkaService`, `EmailService`, Kafka config — unchanged
- `notification_service` — doesn't use Axon
- `api_gateway`, `discovery_server` — don't use Axon

## Verification Plan

### Automated Tests
```bash
# Build all services to verify compilation
cd common_service && mvnw.cmd clean install -DskipTests
cd book_service && mvnw.cmd clean compile
cd employee_service && mvnw.cmd clean compile
cd borrowing_service && mvnw.cmd clean compile
```

### Manual Verification
- Confirm no `org.axonframework` imports remain in any Java file
- Confirm no `axon` references remain in `application.properties` files
- Confirm `axonserver` service is removed from `docker-compose.yml`
- Verify the API contracts (endpoint paths, request/response models) remain identical
