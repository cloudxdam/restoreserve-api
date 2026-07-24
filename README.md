# RestoReserve API

RestoReserve API is a Spring Boot REST API for managing restaurant tables, reservations, and user access with JWT authentication.

It started as an academic project but is being refined focused on backend fundamentals: layered architecture, validation, security, and business rules.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- H2 Database
- JWT (`jjwt`)
- Swagger / OpenAPI
- JUnit 5 + Mockito
- Maven

## Main Features

- User registration and login with JWT-based authentication
- Role-based access control for `ADMIN` and `USER`
- Restaurant table management with filtering by status, location, and capacity
- Reservation management with business validations
- Penalization policy for late cancellations and no-shows
- Admin endpoint to reset penalization points and reactivate users
- Swagger UI for exploring and testing the API

## Business Rules Highlighted in the Project

- Reservations must be created for a future date and time
- A reservation cannot exceed the maximum capacity of the selected table
- A table cannot be booked if it already has a confirmed reservation in the same 2-hour time slot
- Users with status `BANNED` cannot create new reservations
- Late cancellations increase penalization points
- Users who exceed the penalization threshold are moved to `BANNED`
- VIP tables require an additional eligibility check in the reservation flow

## API Overview

### Authentication

- `POST /api/v1/auth/register` - register a new customer account
- `POST /api/v1/auth/login` - authenticate and receive a JWT token

### Reservations

- `POST /api/v1/reservations` - create a reservation for the authenticated user
- `GET /api/v1/reservations` - list visible reservations based on role
- `GET /api/v1/reservations/{id}` - get a reservation by id
- `GET /api/v1/reservations/status/{status}` - filter reservations by status
- `DELETE /api/v1/reservations/{id}` - cancel a reservation

### Restaurant Tables

- `POST /api/v1/tables` - create a table (`ADMIN`)
- `GET /api/v1/tables` - list all tables (`ADMIN`)
- `GET /api/v1/tables/{id}` - get a table by id (`ADMIN`)
- `PUT /api/v1/tables/{id}` - update a table (`ADMIN`)
- `DELETE /api/v1/tables/{id}` - delete a table (`ADMIN`)
- `GET /api/v1/tables/capacity/{pax}` - filter by minimum capacity (`ADMIN`)
- `GET /api/v1/tables/status/{status}` - filter by status (`ADMIN`)
- `GET /api/v1/tables/location/{location}` - filter by location (`ADMIN`)
- `GET /api/v1/tables/location-status?location=...&status=...` - combined filter (`ADMIN`)
- `GET /api/v1/tables/available?status=...&maxPax=...` - filter by status and capacity (`ADMIN`)

### Users

- `PATCH /api/v1/users/{id}/reset-penalization` - reset penalization points and restore `ACTIVE` status (`ADMIN`)

## Validation and Error Handling

The API uses Bean Validation on DTOs and a global exception handler to return consistent responses for:

- validation errors
- missing resources
- business rule violations
- banned users
- non-VIP users trying to reserve VIP tables

## Local Run

### Requirements

- Java 17
- Maven Wrapper included in the project

### Start the application

```bash
./mvnw spring-boot:run
```

The application currently runs with an in-memory H2 database and seed data loaded at startup.

## Swagger UI

Once the app is running, open:

```text
http://localhost:8080/swagger-ui/index.html
```

## Authentication Flow

1. Register a new user with `POST /api/v1/auth/register` or use seeded data for local testing.
2. Login with `POST /api/v1/auth/login`.
3. Copy the returned JWT token.
4. In Swagger, click `Authorize` and paste `Bearer <your-token>`.
5. Call protected endpoints.

## Notes About the Current Setup

- H2 is used here as a fast local development and testing database.
- Demo data is loaded from `src/main/resources/data.sql`.
- The project is currently optimized for local execution and portfolio presentation.
- Hardening steps such as externalized secrets and environment-specific configuration are planned as next improvements.

## Project Structure

```text
src/main/java/com/pachedev/restoreserve
|- controller
|- service
|- repository
|- model
|- dto
|- security
|- exception
|- config
```

## Future Improvements

- Externalize JWT secret and sensitive configuration
- Add more controller, security, and integration tests
- Refactor reservation service for better transaction boundaries and testability
- Add database profiles for H2 and PostgreSQL
- Improve repository hygiene and developer experience

## Why This Project Matters

This project is not just a CRUD demo. It includes authentication, role-based authorization, validation, custom exception handling, and business rules around reservation conflicts and user penalization, which makes it a solid foundation for demonstrating junior backend skills with Spring Boot.
