# NightBite

A late-night food ordering platform for campus hostels.

## Features
- Role-based access for students, vendors and admins
- Batch delivery: orders grouped by hostel block and delivery time slot
- Scheduled batch locking
- Real-time order tracking
- Vendor menus, order history and reviews
- JWT authentication and authorization

## Tech Stack
Java, Spring Boot, Spring Security, JWT, Spring Data JPA, MySQL, H2, HTML, CSS, JavaScript

## Run locally (no database setup needed)
./mvnw spring-boot:run

The app starts on http://localhost:8080 with an in-memory H2 database.
H2 console: http://localhost:8080/h2-console

## Run with MySQL
Set these environment variables, then run the command above:
DB_URL, DB_USERNAME, DB_PASSWORD, DB_DRIVER, DB_DIALECT, JWT_SECRET