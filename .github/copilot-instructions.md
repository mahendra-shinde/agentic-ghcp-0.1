# Repository Agent Guidelines: Order Service

## Technical Stack
- Java 17, Spring Boot 3.2.x, Apache Maven.
- All DTOs must be implemented as immutable Java 17 records in package `com.example.orderservice.dto`.
- Business rules, volume discount logic, and SKU validations must reside exclusively in service implementations (`com.example.orderservice.service`).
- Keep controller endpoints thin; delegate validation and transformation logic downstream.

## Concurrency & Data Storage
- Use thread-safe data structures (`ConcurrentHashMap`, `AtomicInteger`) for any in-memory state.
- Ensure atomic operations are executed safely using compute functions.

## Automated Testing & CI Policy
- All modifications must be accompanied by JUnit 5 and AssertJ tests in `src/test/java`.
- Every pull request triggers `.github/workflows/ci.yml`. Never skip tests or configure `maven.test.skip=true`.
- Any assertion or compilation failures reported by GitHub Actions CI must be addressed before requesting human review.

