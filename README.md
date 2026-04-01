# lab1p Banking Simulation API
[![CI/CD Pipeline](https://github.com/VeleZ-z/lab1p-back/actions/workflows/build.yml/badge.svg)](https://github.com/VeleZ-z/lab1p-back/actions/workflows/build.yml)

Implementation of a Simple Banking App with the next operations:

* Get all customers
* Get customer by ID
* Create a new customer
* Transfer money between accounts
* Get transactions by account number
* health check (Spring Boot default)

Including integration with GitHub Actions for CI/CD and unit testing.

---

## Folders Structure

In the folder `src/main` is located the main code of the app:

- `controller` → REST endpoints
- `service` → business logic
- `repository` → data access (JPA)
- `entity` → database models
- `dto` → request/response objects
- `mapper` → entity-DTO mapping

In the folder `src/test` is located the unit tests of the application.

---

## How to install it

Execute:

```shell
$ ./mvnw spring-boot:run
```
### How to get coverage test
Execute:
```shell
$ mvwn -B package -DskipTests --file pom.xml
```