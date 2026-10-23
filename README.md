# Pede Aí

REST API for delivery orders of a single restaurant. Customers build orders from the menu, and the restaurant moves each order through its status until delivery.

> Work in progress.

## Tech stack

- Java 21
- Spring Boot 4 (Web MVC, Data JPA, Validation)
- PostgreSQL 16
- Flyway
- Maven
- Lombok

## Running locally

Requirements: Java 21 and a running PostgreSQL instance.

1. Create the database and user:

   ```sql
   CREATE USER pedeai WITH PASSWORD 'pedeai';
   CREATE DATABASE pedeai OWNER pedeai;
   ```

2. Start the application (Flyway creates the tables on startup):

   ```bash
   ./mvnw spring-boot:run
   ```

The API runs at `http://localhost:8080/api/v1`.

The connection can be changed with the `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` environment variables.

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/users` | Register a customer |
| GET | `/users/{id}` | Get a user |
| GET | `/categories` | List categories |
| POST | `/categories` | Create a category |
| PUT | `/categories/{id}` | Update a category |
| DELETE | `/categories/{id}` | Delete a category |
| GET | `/menu-items?categoryId=&available=` | List menu items |
| GET | `/menu-items/{id}` | Get a menu item |
| POST | `/menu-items` | Create a menu item |
| PUT | `/menu-items/{id}` | Update a menu item |
| PATCH | `/menu-items/{id}/availability` | Change availability |
| DELETE | `/menu-items/{id}` | Delete a menu item |
| POST | `/orders` | Place an order |
| GET | `/orders?status=` | List orders |
| GET | `/orders/{id}` | Get an order |
| PATCH | `/orders/{id}/cancel` | Cancel an order |
| PATCH | `/orders/{id}/advance` | Advance the order status |

## Roadmap

- [x] Core features
- [x] Global error handling, logging and Flyway migrations
- [ ] Unit and integration tests
- [ ] Authentication with JWT
- [ ] Performance improvements
- [ ] Docker, CI/CD and AWS deploy
