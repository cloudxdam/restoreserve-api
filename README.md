# RestoReserve API

[![CI](https://github.com/cloudxdam/restoreserve-api/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/cloudxdam/restoreserve-api/actions/workflows/ci.yml)

RestoReserve API is a Spring Boot REST API for managing restaurant tables, reservations, and user access with JWT authentication.

The project started as an academic project and has since been refined with a focus on backend fundamentals: layered architecture, validation, security, persistence, testing, and business rules.

## Tech Stack

- Java 17
- Spring Boot 3.5.14
- Spring Web
- Spring Security
- Spring Data JPA
- H2 Database (default profile)
- PostgreSQL (`postgres` profile, optional)
- JJWT
- Spring Validation
- Springdoc OpenAPI
- Lombok
- JUnit 5 + Mockito
- Spring Security Test
- Maven
- Docker Compose

## Main Features

- User registration and login with JWT-based authentication
- BCrypt password hashing
- Stateless authentication with Spring Security
- Role-based access control for `ADMIN` and `USER`
- Restaurant table management with filtering by status, location, and capacity
- Reservation management with business validations
- Reservation ownership and access control
- Penalization policy for late cancellations
- Automatic user banning after exceeding the penalization threshold
- Admin endpoint to reset penalization points and reactivate users
- Global exception handling
- Swagger / OpenAPI documentation

## Architecture

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Controllers handle HTTP requests and validation, services contain business rules, and repositories provide persistence through Spring Data JPA.

Security is handled separately through Spring Security and a custom JWT authentication filter.

## Business Rules

The reservation service implements several domain rules:

- Reservations must be created for a future date and time.
- A reservation cannot exceed the maximum capacity of the selected table.
- A table cannot have overlapping confirmed reservations.
- Each confirmed reservation occupies a two-hour time window.
- Users with status `BANNED` cannot create new reservations.
- Cancelling a reservation less than two hours before its scheduled time adds 2 penalty points.
- Users exceeding 6 penalty points are moved to `BANNED`.
- Administrators can reset a user's penalization and restore `ACTIVE` status.

## API Overview

### Authentication

- `POST /api/v1/auth/register` — register a new user
- `POST /api/v1/auth/login` — authenticate and receive a JWT token

### Reservations

- `POST /api/v1/reservations` — create a reservation for the authenticated user (`USER`, `ADMIN`)
- `GET /api/v1/reservations` — list reservations accessible to the current user (`USER`, `ADMIN`)
- `GET /api/v1/reservations/{id}` — get a reservation by ID (`USER`, `ADMIN`)
- `GET /api/v1/reservations/status/{status}` — filter reservations by status (`USER`, `ADMIN`)
- `DELETE /api/v1/reservations/{id}` — cancel a reservation (`USER`, `ADMIN`)

### Restaurant Tables

- `POST /api/v1/tables` — create a table (`ADMIN`)
- `GET /api/v1/tables` — list all tables (`ADMIN`)
- `GET /api/v1/tables/{id}` — get a table by ID (`ADMIN`)
- `PUT /api/v1/tables/{id}` — update a table (`ADMIN`)
- `DELETE /api/v1/tables/{id}` — delete a table (`ADMIN`)
- `GET /api/v1/tables/capacity/{pax}` — filter by minimum capacity (`ADMIN`)
- `GET /api/v1/tables/status/{status}` — filter by status (`ADMIN`)
- `GET /api/v1/tables/location/{location}` — filter by location (`ADMIN`)
- `GET /api/v1/tables/location-status?location=...&status=...` — combined filter (`ADMIN`)
- `GET /api/v1/tables/available?status=...&maxPax=...` — filter by status and capacity (`ADMIN`)

### Users

- `PATCH /api/v1/users/{id}/reset-penalization` — reset penalization points and restore `ACTIVE` status (`ADMIN`)

## Validation and Error Handling

The API uses Bean Validation on request DTOs and a global exception handler to provide consistent error responses for:

- Validation errors
- Missing resources
- Business rule violations
- Banned users
- Other application-specific exceptions

Custom exceptions include:

- `ResourceNotFoundException`
- `BusinessLogicException`
- `BannedUserException`
- `NotVipUserException`

## Local Run

### Requirements

- Java 17
- Maven Wrapper included in the project
- Docker (only for the optional PostgreSQL profile)

### Configure the environment

The application signs its tokens with a key read from the `JWT_SECRET` environment
variable. Neither the key nor the database credentials are stored in the repository,
so the application will not start until you provide them.

Create a local `.env` file (already ignored by git) with freshly generated values:

```bash
printf 'JWT_SECRET=%s\nPOSTGRES_DB=restoreserve\nPOSTGRES_USER=restoreserve\nPOSTGRES_PASSWORD=%s\n' \
  "$(openssl rand -base64 64)" "$(openssl rand -hex 16)" > .env
```

The command overwrites any existing `.env`. The required variables are documented in
`.env.example`. Never commit `.env`.

### Start the application (H2, default)

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
mvnw.cmd spring-boot:run
```

### Start the application with PostgreSQL (optional)

Start the database and wait until it reports `healthy`:

```bash
docker compose up -d
docker compose ps
```

Then start the application:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Stop the database when you are done:

```bash
docker compose down
```

Data lives in a named volume and survives container restarts. Use `docker compose down -v`
only if you want to delete it.

## Swagger UI

Once the application is running, open:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI documentation includes Bearer JWT authentication, allowing protected endpoints to be tested directly from Swagger UI.

## Authentication Flow

1. Register a user with `POST /api/v1/auth/register` or use the seeded users for local testing.
2. Login with `POST /api/v1/auth/login`.
3. Copy the returned JWT token.
4. In Swagger UI, click `Authorize`.
5. Enter `Bearer <your-token>`.
6. Call the protected endpoints.

## Demo Users

The project includes seeded users for local development.

### User

```text
Username: armymoves
Password: 1234
Role: ROLE_USER
```

### Administrator

```text
Username: admin
Password: Admin1234
Role: ROLE_ADMIN
```

These credentials are intended for local development and demonstration only.

## Project Structure

```text
src/main/java/com/pachedev/restoreserve
├── config
├── controller
├── dto
├── exception
├── model
│   ├── entity
│   └── enums
├── repository
├── security
└── service
```

## Testing

The project uses:

- JUnit 5
- Mockito
- Spring Boot Test
- Spring Security Test
- MockMvc

Current tests cover three layers: the application context, reservation-service business rules (validation, banned users, reservation ownership, cancellation, penalization, and automatic banning), and the HTTP layer through MockMvc with real JWTs (login and registration responses, failed-login status codes, role-based access control, per-user data isolation, and validation errors).

Run the test suite with:

```bash
./mvnw test
```

## Notes About the Current Setup

- The default profile runs on an in-memory H2 database; the optional `postgres` profile runs on PostgreSQL through Docker Compose.
- Demo data is seeded on startup by `config/DataSeeder` and is skipped when users already exist, so restarting the application does not duplicate it.
- JWT authentication is implemented with JJWT and Spring Security.
- The project is currently focused on local execution and portfolio presentation.

## Future Improvements

- Containerize the API with a Dockerfile and an `app` service in compose.
- Replace `ddl-auto=update` with Flyway migrations.
- Add production-oriented configuration and observability.

## Why This Project Matters

This project goes beyond a basic CRUD application.

It demonstrates practical backend concepts including:

- REST API design
- Authentication and authorization
- JWT security
- Role-based access control
- DTO-based request and response handling
- Bean Validation
- JPA persistence
- Business-rule implementation
- Centralized exception handling
- Unit and integration testing
- API documentation with OpenAPI

It provides a foundation for demonstrating junior backend development skills with Java and Spring Boot while continuing to evolve toward a more production-oriented architecture.

## Author

**Daniel Pacheco**

GitHub: [cloudxdam](https://github.com/cloudxdam)
