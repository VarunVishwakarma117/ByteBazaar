# Byte Bazaar

Byte Bazaar is a small electronics shopping website made with Spring Boot and a simple AngularJS frontend.

It has login, product listing, cart and order placement. The app runs with an in-memory H2 database by default, so it can be tested without installing MySQL.

## Tech Used

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- H2 database for local run
- MySQL support
- AngularJS

## How to Run

Run this command from the project folder:

```powershell
.\gradlew.bat bootRun
```

Then open:

```text
http://localhost:8080
```

Demo user:

```text
Email: customer@bytebazaar.dev
Password: password
```

## MySQL Run

The default run uses H2. To run with MySQL, set these values and start the app with the `mysql` profile:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/byte_bazaar?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
.\gradlew.bat bootRun --args="--spring.profiles.active=mysql"
```

Change the username and password if your MySQL setup is different.

## Main Features

- Register and login
- View products
- Search and filter products
- Add products to cart
- Update or remove cart items
- Place an order
- View previous orders

## API Endpoints

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`

Products:

- `GET /api/products`
- `GET /api/products/{id}`

Cart:

- `GET /api/cart`
- `POST /api/cart`
- `PATCH /api/cart/{itemId}`
- `DELETE /api/cart/{itemId}`

Orders:

- `GET /api/orders`
- `POST /api/orders`

For cart and order APIs, send the login token like this:

```text
Authorization: Bearer <token>
```

## Tests

Run:

```powershell
.\gradlew.bat test
```
