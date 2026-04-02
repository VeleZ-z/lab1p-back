# lab1p Banking Simulation API
[![CI/CD Pipeline](https://github.com/VeleZ-z/lab1p-back/actions/workflows/build.yml/badge.svg)](https://github.com/VeleZ-z/lab1p-back/actions/workflows/build.yml) [![Coverage Status](https://coveralls.io/repos/github/VeleZ-z/lab1p-back/badge.svg?branch=main)](https://coveralls.io/github/VeleZ-z/lab1p-back?branch=main) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=VeleZ-z_lab1p-back)](https://sonarcloud.io/summary/new_code?id=VeleZ-z_lab1p-back) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=VeleZ-z_lab1p-back&metric=bugs)](https://sonarcloud.io/summary/new_code?id=VeleZ-z_lab1p-back) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=VeleZ-z_lab1p-back&metric=coverage)](https://sonarcloud.io/summary/new_code?id=VeleZ-z_lab1p-back) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=VeleZ-z_lab1p-back&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=VeleZ-z_lab1p-back) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=VeleZ-z_lab1p-back&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=VeleZ-z_lab1p-back) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=VeleZ-z_lab1p-back&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=VeleZ-z_lab1p-back) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=VeleZ-z_lab1p-back&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=VeleZ-z_lab1p-back) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=VeleZ-z_lab1p-back&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=VeleZ-z_lab1p-back)

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