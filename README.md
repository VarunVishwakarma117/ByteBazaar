# Byte Bazaar

Full-stack electronics storefront built with Java, Spring Boot, AngularJS, and MySQL.

## Features

- Token-based user registration, login, logout, and current-user API.
- Public electronics product catalog with seeded sample products.
- Authenticated cart APIs for add, update, remove, and cart totals.
- Authenticated checkout flow that creates orders, snapshots order items, and decrements stock.
- AngularJS single-page frontend served from Spring Boot static resources.

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- MySQL
- AngularJS 1.8

## Run Locally

For a no-MySQL demo run:

```powershell
.\gradlew.bat bootRun
```

Open `http://localhost:8080`.

Demo login:

```text
customer@bytebazaar.dev
password
```

For MySQL, start MySQL and run with the `mysql` profile. The application can create the database if the configured user has permission.

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/byte_bazaar?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
.\gradlew.bat bootRun --args="--spring.profiles.active=mysql"
```

## REST API

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/cart`
- `POST /api/cart`
- `PATCH /api/cart/{itemId}`
- `DELETE /api/cart/{itemId}`
- `GET /api/orders`
- `POST /api/orders`

Authenticated endpoints require:

```text
Authorization: Bearer <token>
```

## Tests

Tests use the `test` profile and an in-memory H2 database in MySQL compatibility mode.

```powershell
.\gradlew.bat test
```
