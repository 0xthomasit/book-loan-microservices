# Approach Comparison: Option (a) vs Option (b)
## Cross-Service Serialization & Event Wiring in Book-Loan Microservices

---

## Background

After removing Axon Framework, two independent improvements were proposed for the
replacement Spring Boot architecture:

| Label | Description |
|-------|-------------|
| **Option (a)** | Wire up `ApplicationEventPublisher` / `@EventListener` inside each command service so that in-process Spring events are published after every JPA operation |
| **Option (b)** | Harden cross-service HTTP serialization by adding `@JsonProperty` to shared Boolean fields and replacing the ad-hoc `BookStatusRequest` record with the shared command objects |

Both options are **not mutually exclusive** — they solve different problems.
This document explains what each one does, where it lives in the code, and why
**option (b) is the higher-priority fix**.

---

## Option (a) — `ApplicationEventPublisher` / `@EventListener`

### What it does

Each command service injects Spring's `ApplicationEventPublisher` and calls
`publishEvent()` after every successful JPA operation. A matching `@EventListener`
component in the same service receives the event and acts on it (currently: logging).

### Where it lives in this codebase

```
book_service
└── command
    ├── service/BookCommandService.java        ← publishes BookCreatedEvent,
    │                                             BookUpdatedEvent, BookDeletedEvent
    └── event/BookEventListener.java           ← @EventListener — consumes all 3

employee_service
└── command
    ├── service/EmployeeCommandService.java    ← publishes EmployeeCreatedEvent,
    │                                             EmployeeUpdatedEvent, EmployeeDeletedEvent
    └── event/EmployeeEventListener.java       ← @EventListener — consumes all 3

borrowing_service
└── command
    ├── service/BorrowingCommandService.java   ← publishes BorrowingCreatedEvent,
    │                                             BorrowingDeletedEvent
    └── event/BorrowingEventListener.java      ← @EventListener — consumes both
```

### Code example (BookCommandService)

```java
// Before option (a) — event DTOs existed but were never used
public String createBook(CreateBookCommand command) {
    Book book = new Book();
    BeanUtils.copyProperties(command, book);
    bookRepository.save(book);
    return command.getId();
}

// After option (a) — event is now published after JPA save
public String createBook(CreateBookCommand command) {
    Book book = new Book();
    BeanUtils.copyProperties(command, book);
    bookRepository.save(book);
    eventPublisher.publishEvent(
        new BookCreatedEvent(book.getId(), book.getName(),
                             book.getAuthor(), book.getIsReady()));
    return command.getId();
}
```

### What it solves

- The event DTOs (`BookCreatedEvent`, `EmployeeUpdatedEvent`, `BorrowingCreatedEvent`,
  etc.) were **dead/unused code** after Axon was removed. Option (a) brings them back
  to life as in-process Spring events.
- Decouples the command service from its side effects — adding audit logging, Kafka
  publishing, or cache invalidation later only requires a new `@EventListener` method,
  with **zero changes** to the command service itself.
- Mirrors the plan's stated mapping:
  `AggregateLifecycle.apply()` → `ApplicationEventPublisher.publishEvent()`.

### What it does NOT solve

- It is **entirely in-process** (same JVM, same thread by default). It does not fix
  anything about how data travels **between** services over HTTP.
- It does not address JSON serialization of `Boolean isReady` / `Boolean isDisciplined`
  across service boundaries.
- The `BookStatusRequest` ad-hoc record in `BorrowingCommandService` is still there —
  a fragile, locally-defined type that duplicates the shared `UpdateBookStatusCommand`.

---

## Option (b) — `@JsonProperty` Hardening + Shared Command Objects

### What it does

1. Adds `@JsonProperty("isReady")` to `BookResponseCommonModel.isReady` and
   `@JsonProperty("isDisciplined")` to `EmployeeResponseCommonModel.isDisciplined`.
2. Adds `@JsonProperty("isReady")` to `UpdateBookStatusCommand.isReady` and
   `RollBackBookStatusCommand.isReady`.
3. Replaces the ad-hoc `BookStatusRequest` inner record in `BorrowingCommandService`
   with the shared `UpdateBookStatusCommand` and `RollBackBookStatusCommand` objects.
4. Hardens the null-safety of boolean checks:
   `!book.getIsReady()` → `!Boolean.TRUE.equals(book.getIsReady())`.

### Where it lives in this codebase

```
common_service
├── model/BookResponseCommonModel.java         ← @JsonProperty("isReady") on isReady
├── model/EmployeeResponseCommonModel.java     ← @JsonProperty("isDisciplined") on isDisciplined
├── command/UpdateBookStatusCommand.java       ← @JsonProperty("isReady") on isReady
└── command/RollBackBookStatusCommand.java     ← @JsonProperty("isReady") on isReady

borrowing_service
└── command/service/BorrowingCommandService.java
    ├── Uses UpdateBookStatusCommand instead of BookStatusRequest  (PUT /status)
    ├── Uses RollBackBookStatusCommand instead of BookStatusRequest (PUT /status/rollback)
    ├── !book.getIsReady()           → !Boolean.TRUE.equals(book.getIsReady())
    └── employee.getIsDisciplined()  → Boolean.TRUE.equals(employee.getIsDisciplined())
```

### The Lombok + Jackson Boolean serialization bug

Lombok generates **two** accessors for a `Boolean isReady` field:

| Accessor | Jackson reads it as | JSON key produced |
|----------|--------------------|--------------------|
| `getIsReady()` | standard getter | `"isReady"` ✔ |
| `isReady()` | is-accessor | `"ready"` ✘ |

Without `@JsonProperty`, Jackson's behaviour depends on which accessor it discovers
first — this is **non-deterministic** across Jackson versions and Lombok configurations.
The result: `borrowing_service` deserializes the response from `book-service` into
`BookResponseCommonModel`, and `getIsReady()` may silently return `null` instead of
`true`/`false`.

```java
// Without @JsonProperty — silent null, then NullPointerException on unboxing
if (book == null || !book.getIsReady()) {
    //                 ^^^^^^^^^^^^^^^^ NPE if isReady deserialized as null!
}

// With @JsonProperty("isReady") + Boolean.TRUE.equals null-safety
if (book == null || !Boolean.TRUE.equals(book.getIsReady())) {  // safe ✔
    throw new RuntimeException("This book has been borrowed by someone!!");
}
```

### The `BookStatusRequest` problem

Before option (b), `BorrowingCommandService` defined a local record as the HTTP
request body sent to `BookInternalController`:

```java
// borrowing_service — ad-hoc, locally-defined, no @JsonProperty guarantee
public record BookStatusRequest(Boolean isReady, String employeeId, String borrowingId) {}
```

`BookInternalController` on the receiving end deserializes it into
`UpdateBookStatusCommand`:

```java
// book_service — BookInternalController
@PutMapping("/{bookId}/status")
public String updateBookStatus(@PathVariable String bookId,
                               @RequestBody UpdateBookStatusCommand command) { ... }
```

The field names happen to match today, but this arrangement has three problems:

1. `BookStatusRequest` is a **private, unnamed contract** — any rename or reorder
   silently breaks the cross-service call with no compile-time error.
2. `UpdateBookStatusCommand` already exists in `common_service` precisely to be the
   **shared contract** between services. Not using it defeats its purpose.
3. `BookStatusRequest` has no `bookId` field while `UpdateBookStatusCommand` does —
   the two types are structurally inconsistent from the start.

After option (b), the shared type is used directly:

```java
// borrowing_service — uses the shared type, consistent contract
UpdateBookStatusCommand updateCommand = new UpdateBookStatusCommand(
        command.getBookId(), false, command.getEmployeeId(), command.getId());
webClientBuilder.build()
        .put()
        .uri("http://book-service/api/internal/books/" + command.getBookId() + "/status")
        .bodyValue(updateCommand)   // ← shared type, @JsonProperty guaranteed
        ...
```

---

## Side-by-Side Comparison

| Dimension | Option (a) | Option (b) |
|-----------|-----------|-----------|
| **Scope** | In-process, within a single service JVM | Cross-service, over HTTP between services |
| **Problem solved** | Dead event DTOs; decoupling command service from side effects | Silent JSON serialization bug; fragile ad-hoc request type |
| **Failure mode if skipped** | Event DTOs remain unused; adding side effects later requires touching the service | `NullPointerException` at runtime when `isReady` deserializes as `null` |
| **When does it break?** | Never breaks at runtime (it is additive) | Breaks at runtime on the first borrow request in certain Jackson/Lombok version combinations |
| **Detectability** | Immediately visible — unused code, no runtime impact | **Silent** — no compile error, no startup error, fails only under live HTTP traffic |
| **Shared contract usage** | No cross-service contracts involved | Enforces `common_service` shared types as the single source of truth |
| **Null safety** | Not applicable | `Boolean.TRUE.equals()` guards against `null` unboxing NPE |
| **Compile-time safety** | High — Spring wires `@EventListener` at startup | High after fix — shared type mismatch caught at compile time |
| **Risk if not applied** | Low (cosmetic / architectural) | **High** (runtime crash in production) |

---

## Why Option (b) is the Higher Priority

### 1. It fixes a real runtime defect, not a style issue

Option (a) is an architectural improvement — it makes the code cleaner and more
extensible. But the codebase **works correctly** without it (the event DTOs are simply
unused dead code).

Option (b) fixes a **latent runtime bug**. The Lombok + Jackson `Boolean isReady`
serialization issue is a well-known footgun. Without `@JsonProperty("isReady")`,
the JSON key emitted by `book-service` may be `"ready"` instead of `"isReady"`.
When `borrowing-service` deserializes that response into `BookResponseCommonModel`,
`getIsReady()` returns `null`. The subsequent unboxing `!book.getIsReady()` throws
a `NullPointerException` — **crashing every single borrow request**.

### 2. The failure is silent until production

There is no compile-time warning. The application starts up fine. The bug only
surfaces when a client actually calls `POST /api/v1/borrowing`. In a microservices
environment where services are deployed independently, this is exactly the kind of
defect that slips through unit tests (which mock the HTTP call) and only appears
in integration or production.

### 3. It enforces the shared contract that `common_service` was designed for

The entire purpose of `common_service` is to be the **single source of truth** for
types shared across services. The `BookStatusRequest` inner record in
`BorrowingCommandService` directly violates this principle — it is a shadow copy of
`UpdateBookStatusCommand` with no `@JsonProperty` guarantees. Option (b) removes it
and uses the canonical shared type, so any future change to the status update contract
only needs to happen in one place.

### 4. `Boolean.TRUE.equals()` is a correctness fix, not a style preference

Java's auto-unboxing of a `null` `Boolean` to a primitive `boolean` throws a
`NullPointerException`. `!Boolean.TRUE.equals(null)` returns `true` safely.
In a distributed system where a downstream service can return an unexpected payload,
defensive null handling is not optional.

---

## Recommendation

Apply **both** options — they are complementary, not competing:

- **Option (b) first** — it is a correctness fix that prevents a production crash.
- **Option (a) second** — it is an architectural improvement that makes the event
  pipeline extensible (e.g., adding Kafka publishing later only requires a new
  `@EventListener`, zero changes to the command service).

Together they bring the codebase fully in line with the `implementation_plan.md`
specification and make the cross-service orchestration in `BorrowingCommandService`
both **correct** and **robust**.
