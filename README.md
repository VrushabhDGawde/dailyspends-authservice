# Spend Sense Backend

Spend Sense Backend is a modern personal finance tracking application built with **Spring Boot 3** and **Java 21**. It features JWT-based authentication, database migration support via Flyway, interactive API documentation using Swagger UI, and is fully containerized using Docker. 

## Tech Stack
- **Core Framework**: Spring Boot 3.5.x
- **Language**: Java 21
- **Database**: PostgreSQL 17
- **Migrations**: Flyway
- **Security**: Spring Security + JSON Web Tokens (JWT)
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Containerization**: Docker & Docker Compose

---

## Features
- **User Authentication**: Secure user registration, login, token refresh, and logout using JWT (Access & Refresh tokens).
- **Secure Endpoints**: Out-of-the-box token verification filter for sensitive API requests.
- **Database Versioning**: Flyway database migrations run automatically on startup.
- **Interactive Documentation**: Beautiful Swagger UI for developers to explore and test the endpoints directly from the browser.
- **Docker Ready**: Run the entire project (database and app) locally using a single command.

---

## Getting Started

### Prerequisites
- Java 21 JDK
- Maven 3.x (or use the packaged `./mvnw` wrapper)
- Docker & Docker Compose (optional, for containerization)
- PostgreSQL (optional, if running locally outside Docker)

### Run Option 1: Docker Compose (Recommended)
This starts both PostgreSQL and the Spring Boot application in containers.

1. Ensure Docker Desktop is installed and running.
2. Clone the repository and navigate to the project directory:
   ```bash
   git clone <your-repository-url>
   cd spend-sense-backend
   ```
3. Initialize the environment variables file:
   - Create a `.env` file in the root directory (based on `.env` settings below or copy existing).
4. Run the containers:
   ```bash
   docker compose up --build -d
   ```
5. Access the API documentation at:
   - **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

### Run Option 2: Running Locally
If you want to run the application on your host machine:

1. Start your local PostgreSQL database server and create a database named `spendsense`.
2. Configure the database credentials in a `.env` file or in `src/main/resources/application-dev.yml`:
   ```properties
   DB_NAME=spendsense
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   ```
3. Run the application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
4. The application will start on port `8080`.

---

## Environment Variables Config
The database configuration and JWT settings are loaded via environment variables or Spring defaults.

| Environment Variable | Description | Default / Example |
|---|---|---|
| `DB_NAME` | The PostgreSQL database name | `spendsense` |
| `DB_USERNAME` | The database username | `spendsense` |
| `DB_PASSWORD` | The database password | `spendsense` |
| `JWT_SECRET_KEY` | HS256 Secret key for signing JWTs | *Base64 Encoded Key* |
| `JWT_EXPIRATION` | Access token expiration in ms | `900000` (15 mins) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration in ms | `604800000` (7 days) |

---

## API Endpoints

### Authentication `/api/v1/auth`
- `POST /register`: Register a new user
- `POST /login`: Log in to get access & refresh tokens
- `POST /refresh-token`: Exchange a valid refresh token for a new access token
- `POST /logout`: Invalidate user session
