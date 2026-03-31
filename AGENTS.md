# AGENTS.md

## 1. System Overview

### What this backend actually does

This project is a Spring Boot backend for a simple banking simulation. Based on the implemented code, it supports:

- Listing all customers
- Fetching a customer by database ID
- Creating a customer
- Registering a money transfer between two existing customer accounts
- Listing all transactions where an account participated as sender or receiver

The current backend does not model a separate `Account` entity. An account is represented implicitly by the `accountNumber` field stored on `Customer`.

### Core domain

The core domain implemented in code is:

- `Customer`: a bank customer who also owns exactly one balance-bearing account
- `Transaction`: a transfer record between two account numbers

This is therefore a small banking ledger simulation centered on:

- customer identity
- account number ownership
- current balance mutation
- transfer history

### High-level architecture style

The real architecture is a simple layered CRUD/service architecture:

- `controller` layer exposes HTTP endpoints
- `service` layer contains business logic
- `repository` layer persists entities through Spring Data JPA
- `entity` layer maps domain objects to MySQL tables
- `dto` layer defines API payloads
- `mapper` layer converts between entities and DTOs

This is not hexagonal or domain-driven in the strict sense. It is a straightforward Spring educational architecture optimized for readability and low ceremony.

---

## 2. Project Structure

### Package layout

Under `src/main/java/com/veor/lab1p` the code is organized as:

- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `mapper`

### Controllers

Files:

- `controller/CustomerController.java`
- `controller/TransactionController.java`

Responsibilities:

- define REST endpoints
- deserialize request payloads
- delegate to services
- build HTTP responses

Observed interaction pattern:

- controllers call services directly
- controllers do not call repositories
- error handling is mixed:
  - `TransactionController` catches `IllegalArgumentException` locally and returns `400`
  - `CustomerController` lets exceptions propagate

### Services

Files:

- `service/CustomerService.java`
- `service/TransactionService.java`

Responsibilities:

- execute use cases
- query and persist repositories
- perform business validations
- convert between entities and DTOs

Observed behavior:

- `CustomerService` is thin and mostly CRUD-oriented
- `TransactionService` contains the main business logic for transfers
- `TransactionService` constructs DTOs manually instead of using `TransactionMapper`

### Repositories

Files:

- `repository/CustomerRepository.java`
- `repository/TransactionRepository.java`

Responsibilities:

- abstract persistence through Spring Data JPA
- provide generated CRUD operations
- expose simple derived queries

Actual repository methods:

- `CustomerRepository.findByAccountNumber(String accountNumber)`
- `TransactionRepository.findBySenderAccountNumberOrReceiverAccountNumber(String senderAccountNumber, String receiverAccountNumber)`

### Models / Entities

Files:

- `entity/Customer.java`
- `entity/Transaction.java`

Responsibilities:

- define JPA tables and columns
- represent persisted business state

Important implementation detail:

- there is no explicit relationship annotation between `Customer` and `Transaction`
- transfers are linked only by plain `String accountNumber` fields

### DTOs

Files:

- `dto/CustomerDTO.java`
- `dto/TransactionDTO.java`
- `dto/TransferRequestDTO.java`

Responsibilities:

- define HTTP payload shapes exposed to clients
- decouple API payloads from JPA entities

Important observations:

- `TransferRequestDTO` is currently unused
- `TransactionController` accepts `TransactionDTO` directly for transfer creation
- `TransactionDTO.timestamp` defaults to `LocalDateTime.now()`
- `CustomerDTO` has a constructor with parameters but an empty body, so only setters/population by Jackson currently matter

### Mappers

Files:

- `mapper/CustomerMapper.java`
- `mapper/TransactionMapper.java`

Responsibilities:

- convert entities to DTOs and back through MapStruct

Important observations:

- `CustomerMapper` is a Spring bean (`componentModel = "spring"`) and is used by `CustomerService`
- `TransactionMapper` is not configured as a Spring bean and is not used anywhere
- transaction mapping is currently duplicated manually in `TransactionService`

### Configuration

Files:

- `Lab1pApplication.java`
- `src/main/resources/application.properties`

Responsibilities:

- bootstrap Spring Boot
- configure HTTP port, datasource, Hibernate, and logging

Current runtime configuration:

- port `8080`
- MySQL datasource at `jdbc:mysql://localhost:3306/banco2025`
- username `root`
- password `root`
- Hibernate DDL mode `update`
- SQL logging enabled

---

## 3. API Contract

This section reflects the real controller behavior, not the intended design.

### `GET /api/customers`

Purpose:

- return all customers

Controller:

- `CustomerController.getAllCustomers()`

Response:

- HTTP `200 OK`
- body: array of `CustomerDTO`

Response item shape:

```json
{
  "id": 1,
  "firstName": "Ana",
  "lastName": "Lopez",
  "accountNumber": "ACC-001",
  "balance": 1500.0
}
```

Error cases:

- no explicit error contract
- repository/data access failures will bubble up as default Spring errors

Frontend relevance:

- this endpoint matches the expected frontend route `GET /api/customers`

### `GET /api/customers/{id}`

Purpose:

- return one customer by database ID

Controller:

- `CustomerController.getCustomerById(Long id)`

Response:

- HTTP `200 OK`
- body: `CustomerDTO`

Error cases:

- if the customer does not exist, `CustomerService` throws `RuntimeException("Cliente no encontrado")`
- because there is no global exception handler, this currently becomes a generic server error instead of a proper `404`

Notes:

- this endpoint was not listed as frontend-required, but it exists

### `POST /api/customers`

Purpose:

- create a new customer record

Controller:

- `CustomerController.createCustomer(CustomerDTO customerDTO)`

Request body:

```json
{
  "firstName": "Ana",
  "lastName": "Lopez",
  "accountNumber": "ACC-001",
  "balance": 1500.0
}
```

Actual required fields by code/database:

- `accountNumber`: required by entity column and should be unique
- `firstName`: required by entity column
- `lastName`: required by entity column
- `balance`: controller explicitly rejects `null`

Response:

- HTTP `200 OK`
- body: created `CustomerDTO`

Error cases:

- `balance == null`: throws `IllegalArgumentException("Balance cannot be null")`
- duplicate `accountNumber`: likely surfaces as a database constraint exception
- missing required fields: likely surface as persistence/database exceptions

Important mismatch:

- creation returns `200 OK`; REST-wise `201 Created` would be more correct, but that is not what the current code does

### `POST /api/transactions`

Purpose:

- transfer money from one account to another and persist the transaction

Controller:

- `TransactionController.transferMoney(TransactionDTO transactionDTO)`

Request body expected by current code:

```json
{
  "senderAccountNumber": "ACC-001",
  "receiverAccountNumber": "ACC-002",
  "amount": 250.0,
  "timestamp": "2026-03-31T16:00:00"
}
```

Actual minimum payload:

- `senderAccountNumber`
- `receiverAccountNumber`
- `amount`

Notes:

- if `timestamp` is omitted, `TransactionDTO` initializes it with `LocalDateTime.now()`
- there is no Bean Validation annotation such as `@Valid`, `@NotNull`, or `@Positive`

Success response:

- HTTP `200 OK`
- body: persisted `TransactionDTO`

Response shape:

```json
{
  "id": 10,
  "senderAccountNumber": "ACC-001",
  "receiverAccountNumber": "ACC-002",
  "amount": 250.0,
  "timestamp": "2026-03-31T16:00:00"
}
```

Implemented validation/error cases:

- missing sender or receiver account number:
  - HTTP `400`
  - body: plain string message
- sender account not found:
  - HTTP `400`
  - body: plain string message
- receiver account not found:
  - HTTP `400`
  - body: plain string message
- insufficient sender balance:
  - HTTP `400`
  - body: plain string message

Unvalidated but risky cases:

- `amount == null` can cause runtime failure
- `amount <= 0` is not blocked
- sender and receiver can be the same account
- concurrent transfers can race and overspend

Frontend relevance:

- this endpoint matches the expected frontend route `POST /api/transactions`

### `GET /api/transactions/{accountNumber}`

Purpose:

- return all transfers where the account number was sender or receiver

Controller:

- `TransactionController.getTransactionsByAccount(String accountNumber)`

Response:

- HTTP `200 OK`
- body: array of `TransactionDTO`

Response item shape:

```json
{
  "id": 10,
  "senderAccountNumber": "ACC-001",
  "receiverAccountNumber": "ACC-002",
  "amount": 250.0,
  "timestamp": "2026-03-31T16:00:00"
}
```

Error cases:

- no explicit validation or error contract
- non-existing account numbers simply return an empty list

Frontend relevance:

- this endpoint matches the expected frontend route `GET /api/transactions/{accountNumber}`

### CORS behavior

Both controllers declare:

- `@CrossOrigin(origins = "http://localhost:5173")`

Implication:

- the current backend is explicitly prepared for a Vite frontend running locally on port `5173`

---

## 4. Domain Model

### `Customer`

Business meaning:

- represents a bank customer and, in the current model, also their account record

Persisted fields:

- `id`: generated primary key
- `accountNumber`: unique account identifier used in transfers
- `firstName`: customer first name
- `lastName`: customer last name
- `balance`: current available balance

Domain meaning:

- `balance` is the mutable ledger state used to authorize outgoing transfers
- `accountNumber` is the business key used by the frontend and by the transfer logic

Important modeling limitation:

- the system currently assumes one customer equals one account
- if multiple accounts per customer are needed later, the model will need to split `Customer` and `Account`

### `Transaction`

Business meaning:

- immutable transfer record between two account numbers

Persisted fields:

- `id`: generated primary key
- `senderAccountNumber`: originating account number
- `receiverAccountNumber`: destination account number
- `amount`: transferred amount
- `timestamp`: transfer timestamp

Domain meaning:

- serves as the transfer history used by `/api/transactions/{accountNumber}`
- acts as an audit-like log, but without richer metadata such as status, description, or transfer type

### Relationships

Current relationships are implicit, not JPA-managed:

- `Transaction.senderAccountNumber` references `Customer.accountNumber` by value
- `Transaction.receiverAccountNumber` references `Customer.accountNumber` by value

Implications:

- referential integrity is enforced only in service logic, not by foreign keys in the entity model
- renaming an `accountNumber` would break historical consistency unless all transactions were updated too

---

## 5. Business Logic

### How transaction processing works

Implemented in `TransactionService.transferMoney(TransactionDTO transactionDTO)`:

1. Validate sender and receiver account numbers are not `null`
2. Load sender `Customer` by sender account number
3. Load receiver `Customer` by receiver account number
4. Check sender balance is at least transfer amount
5. Subtract amount from sender balance
6. Add amount to receiver balance
7. Save both customers
8. Create and save a `Transaction`
9. Return the persisted transaction as `TransactionDTO`

### Critical rules currently enforced

- both account numbers must exist
- sender must have enough balance

### Rules currently missing

- amount must be non-null
- amount must be positive
- sender and receiver should differ
- transfer should be atomic under concurrent access
- transaction timestamp should likely be server-generated, not client-controlled

### Important transactional risk

`TransactionService.transferMoney` is not annotated with `@Transactional`.

That means:

- sender and receiver balances are saved in separate repository calls
- the transaction record is saved afterward
- if a failure occurs between those saves, the system can end in a partially updated state

Example failure mode:

- sender balance debited
- receiver balance credited
- transaction insert fails
- balances change with no matching transaction history

### Customer logic

`CustomerService` currently supports:

- list all customers
- fetch customer by ID
- create customer

There are no service-level rules for:

- account number format
- duplicate prevention before persistence
- minimum opening balance
- negative initial balance prevention

---

## 6. Data Layer

### Database type

The configured database is MySQL:

- JDBC URL: `jdbc:mysql://localhost:3306/banco2025`
- dialect configured as `org.hibernate.dialect.MySQL8Dialect`

This is not H2. Tests also start against the configured MySQL database because no test profile is defined.

### Persistence stack

- Spring Data JPA
- Hibernate ORM
- MySQL Connector/J

### Entity mapping

#### `Customer`

- table: `customers`
- `id`: `@GeneratedValue(strategy = GenerationType.AUTO)`
- `accountNumber`: unique, non-null
- `firstName`: non-null, max length 50
- `lastName`: non-null, max length 50
- `balance`: non-null

#### `Transaction`

- table: `transaction`
- `id`: `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `senderAccountNumber`: non-null
- `receiverAccountNumber`: non-null
- `amount`: non-null
- `timestamp`: persisted with default Java-side initialization

### Repository style

Both repositories extend `JpaRepository`, so the project relies on:

- derived query methods
- standard CRUD
- minimal handwritten persistence logic

### Schema management

`spring.jpa.hibernate.ddl-auto=update`

Implications:

- schema changes are applied automatically at startup
- convenient for a university project
- risky for production because schema evolution is not explicitly versioned

---

## 7. Error Handling

### Current state

There is no global exception handler such as `@ControllerAdvice`.

Error handling is inconsistent:

- `TransactionController` catches `IllegalArgumentException` and returns `400` with a plain string body
- `CustomerController` does not catch service exceptions
- persistence exceptions are not normalized

### Response formats

Current error formats are not uniform:

- some errors return a raw plain-text message
- some errors would return Spring Boot's default JSON error payload
- some domain failures incorrectly become `500 Internal Server Error`

### Consequences

- frontend error handling must account for inconsistent payloads
- API consumers cannot rely on a stable error schema
- missing resources are not properly represented as `404`

### Recommended direction

Future work should centralize:

- `IllegalArgumentException` -> `400`
- not found cases -> `404`
- data integrity violations -> `409`
- validation failures -> structured `400`

Suggested response shape:

```json
{
  "timestamp": "2026-03-31T17:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance",
  "path": "/api/transactions"
}
```

---

## 8. Architectural Decisions

### Why this architecture was likely chosen

Based on the code, the project appears intentionally optimized for teaching and speed of implementation:

- few classes
- direct controller-to-service-to-repository flow
- simple DTOs
- minimal indirection
- no advanced patterns that would obscure core Spring concepts

This fits the stated educational priorities:

- clarity over complexity
- simple and readable architecture
- educational value

### Trade-offs

Benefits:

- easy to understand for students
- low ceremony
- quick to extend for basic use cases
- frontend contract is easy to trace

Costs:

- business rules are under-validated
- transaction safety is weak
- errors are inconsistent
- domain model is oversimplified
- mapping strategy is inconsistent across modules

### Current limitations

- no separate `Account` aggregate
- no test coverage for services or controllers
- no global exception model
- no database migration tool
- no transactional boundary for money transfer
- no input validation annotations
- no authentication/authorization

---

## 9. How to Extend the System

This section should be followed to preserve the current style while improving safety.

### Add a new endpoint

Recommended process:

1. Define or reuse a DTO in `dto/`
2. Add the service method in `service/`
3. Add repository query methods only if the service needs new persistence access
4. Add the controller endpoint in `controller/`
5. Return DTOs, not JPA entities
6. Add tests for the new behavior

Example pattern:

- controller receives request DTO
- service performs validation and orchestration
- repository loads/saves entities
- service returns response DTO
- controller wraps response in `ResponseEntity`

Rules to keep:

- do not call repositories directly from controllers
- keep HTTP concerns in controllers
- keep business rules in services

### Add a new entity

Recommended process:

1. Create a new JPA entity in `entity/`
2. Add table/column constraints that reflect the real domain
3. Create a `JpaRepository` in `repository/`
4. Create DTOs for API exposure if the entity is part of the contract
5. Add a MapStruct mapper if entity-to-DTO conversion is needed repeatedly
6. Add service methods that manage the entity lifecycle
7. Expose endpoints only after service behavior is stable

If adding an `Account` entity specifically:

1. Keep `Customer` focused on identity
2. Move `accountNumber` and `balance` into `Account`
3. Add a relationship such as `Customer 1..n Account` if that is the new requirement
4. Update `Transaction` to reference accounts more explicitly
5. Migrate existing logic carefully because current transfer behavior depends on `Customer.accountNumber`

### Modify business logic safely

Recommended process:

1. Locate the use case in `service/`
2. Identify all controller endpoints and repository methods that depend on it
3. Add or update tests before changing behavior if the rule is critical
4. Keep API DTO shapes stable unless the frontend is also updated
5. Prefer adding validation before persistence
6. For any multi-entity write, use `@Transactional`

For transfer logic changes, always check:

- balance update correctness
- null and negative amount handling
- sender/receiver existence
- same-account behavior
- transaction history integrity
- concurrent request behavior

### Recommended immediate refactor when extending transfers

Before adding more transfer features, do this first:

1. Add `@Transactional` to `TransactionService.transferMoney`
2. Validate `amount != null && amount > 0`
3. Reject same sender and receiver account
4. Standardize exceptions and response bodies
5. Introduce tests around transfer success and failure scenarios

---

## 10. Improvement Opportunities

These are grounded in the current codebase, ordered by practical importance.

### 1. Add transactional safety to transfers

Problem:

- money movement updates two customers and one transaction record without a Spring transaction boundary

Impact:

- partial writes can corrupt balances or audit history

Improvement:

- annotate the transfer method or service class with `@Transactional`

### 2. Add real request validation

Problem:

- DTOs have no validation annotations
- controllers do not use `@Valid`

Impact:

- null or invalid amounts can fail late or unpredictably

Improvement:

- add `@NotBlank`, `@NotNull`, `@Positive`
- validate account number format if there is one

### 3. Standardize error handling

Problem:

- mixed plain-text and default Spring error responses

Impact:

- hard for frontend and future clients to consume consistently

Improvement:

- add `@ControllerAdvice`
- define a common error DTO

### 4. Fix not-found semantics

Problem:

- customer lookup by ID throws generic `RuntimeException`

Impact:

- missing customers become `500` instead of `404`

Improvement:

- introduce a specific not-found exception and map it centrally

### 5. Stop using client-provided timestamps for persisted transfers

Problem:

- transfer timestamp is accepted from the request body

Impact:

- clients can forge transaction time

Improvement:

- generate timestamp server-side in the service

### 6. Replace `Double` with `BigDecimal` for money

Problem:

- money uses floating-point arithmetic

Impact:

- precision errors are possible

Improvement:

- use `BigDecimal` in DTOs, entities, and calculations

### 7. Unify mapping strategy

Problem:

- customers use MapStruct
- transactions are mapped manually
- `TransactionMapper` exists but is unused

Impact:

- duplicated mapping logic

Improvement:

- either use MapStruct consistently or remove unused mapper infrastructure

### 8. Remove or use dead code

Observed dead/underused elements:

- `TransferRequestDTO` is unused
- `TransactionMapper` is unused
- `CustomerDTO` all-args constructor does nothing
- `@Data` on `Transaction` is redundant because getters/setters are already written manually

### 9. Add dedicated test configuration

Problem:

- `mvn test` currently loads the real MySQL configuration

Impact:

- tests depend on local database availability
- test execution is not isolated

Improvement:

- add `src/test/resources/application-test.properties`
- use H2 or Testcontainers

### 10. Revisit persistence naming

Problem:

- `transaction` can be an awkward table name depending on SQL dialect and tooling

Improvement:

- prefer `transactions`

### 11. Externalize sensitive configuration

Problem:

- database credentials are hardcoded in `application.properties`

Improvement:

- move credentials to environment variables or profile-specific config

---

## 11. Coding Guidelines for This Project

These guidelines are based on the current code and the improvements it most needs.

### Naming conventions

- classes use singular nouns: `Customer`, `Transaction`
- controllers end with `Controller`
- services end with `Service`
- repositories end with `Repository`
- DTOs end with `DTO`
- mapper interfaces end with `Mapper`

For new code:

- keep package names lowercase
- keep endpoint paths under `/api/...`
- prefer clear business method names such as `transferMoney`, `getTransactionsForAccount`

### Layering rules

- controllers should only handle HTTP concerns
- services should own business rules and orchestration
- repositories should only provide persistence access
- entities should model persisted state
- DTOs should represent external contracts

Do not:

- return entities directly from controllers
- inject repositories into controllers
- place validation-heavy business logic inside controllers

### Controllers

Preferred style for future controllers:

- constructor injection only
- accept DTOs, not entities
- use `@Valid` for request validation
- return `ResponseEntity<T>` for explicit status codes
- avoid inline `try/catch` for domain errors once global exception handling exists

Current inconsistency to avoid repeating:

- `TransactionController` uses field injection while `CustomerController` uses constructor injection

For future code, prefer the `CustomerController` style.

### Services

- services should be the only place that mutates balances or other business-critical state
- annotate multi-write operations with `@Transactional`
- perform all domain validation before saving
- prefer specific exceptions over generic `RuntimeException`

### Repositories

- keep repository interfaces thin
- use derived query methods for simple lookups
- move complex querying to JPQL or specifications only if needed

### DTO usage

- use separate request and response DTOs when semantics differ
- for transfers, consider replacing `TransactionDTO` request usage with `TransferRequestDTO`
- do not expose internal entity implementation details unless the frontend needs them

Recommended split for transfers:

- request DTO: sender, receiver, amount
- response DTO: id, sender, receiver, amount, timestamp

### Validation

For any new input DTO:

- add Bean Validation annotations
- validate at controller boundary with `@Valid`
- keep business rule validation in services

Example rules that belong in DTOs:

- required text fields
- positive amounts

Example rules that belong in services:

- account existence
- sufficient balance
- transfer not allowed to same account

### Mapping

- if MapStruct is kept, configure all mappers consistently with `componentModel = "spring"`
- use mappers from services, not controllers
- remove unused mappers and DTOs if not needed

### Persistence and schema changes

- reflect real invariants in entity annotations
- avoid relying only on service checks for uniqueness or nullability
- if the project grows, introduce Flyway or Liquibase instead of `ddl-auto=update`

### Testing expectations

At minimum, new features should add:

- service tests for business rules
- controller tests for endpoint contract and status codes

For transfers, tests should cover:

- successful transfer
- sender missing
- receiver missing
- insufficient balance
- invalid amount
- same sender and receiver

---

## 12. Practical Notes for New Developers

### What to understand first

If you are new to this project, read in this order:

1. `controller/TransactionController.java`
2. `service/TransactionService.java`
3. `entity/Customer.java`
4. `entity/Transaction.java`
5. `repository/CustomerRepository.java`
6. `repository/TransactionRepository.java`

That sequence gives the fastest understanding of the main business flow.

### Current frontend-critical contract

Do not break these routes without coordinating frontend changes:

- `GET /api/customers`
- `POST /api/transactions`
- `GET /api/transactions/{accountNumber}`

Also keep these response fields stable unless both sides are updated:

- customer: `id`, `firstName`, `lastName`, `accountNumber`, `balance`
- transaction: `id`, `senderAccountNumber`, `receiverAccountNumber`, `amount`, `timestamp`

### Verified project behavior

On `2026-03-31`, `./mvnw test` passed in this repository. However, the test suite currently only checks Spring context startup and depends on the configured MySQL datasource because no isolated test profile exists.
