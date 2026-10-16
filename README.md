# E-Commerce REST API

A Spring Boot e-commerce backend built as a deep dive into backend engineering: authentication and authorization, transactional checkout, Stripe payment webhooks, Redis-backed email verification, rate limiting, and containerized setup with Docker Compose.

**API reference (static):** https://tchiwara.github.io/ecommerce-api/

## Tech stack
- Java 17, Spring Boot 4.1, Spring Security (JWT), Spring Data JPA
- MySQL 8 with Flyway migrations
- Redis (email OTP verification state)
- Stripe Checkout with signed webhooks
- MapStruct, Lombok, springdoc-openapi (Swagger UI)
- JUnit 5 and Mockito (unit tests for the category service so far)
- Docker and Docker Compose

## Features
- Register with email OTP verification, login with JWT, role-based access (customer and admin)
- Product and category catalog with pagination and sorting
- Per-user shopping cart
- Checkout with stock handling and order history
- Stripe payments: an order is marked paid by a verified webhook, then a confirmation email is sent
- Rate limiting on login, registration, OTP and public endpoints (the Stripe webhook is exempt)
- Centralized error handling with consistent responses

## Design decisions
- **Feature-based packages** (cart, order, product, user) rather than layer-based, so each slice is self-contained.
- **Rich domain model for Cart:** the cart is the aggregate root and owns the add, remove and merge rules.
- **Payment confirmed only by webhook:** the success redirect page never marks an order paid; the signed Stripe event does.
- **OTP state in Redis:** short-lived keys with TTLs and atomic counters for attempts and resend limits, with the code stored as an HMAC hash.
- **Role carried in the JWT claim:** avoids a database lookup on every request, at the cost of role changes taking effect when the token expires.
- **Flyway for the schema:** the database is built from migrations on a fresh start.

## Running locally with Docker Compose

**Requirements:** Docker, the [Stripe CLI](https://docs.stripe.com/stripe-cli), a free Stripe account in test mode, and a Gmail account with an App Password.

### 1. Configure
```bash
cp .env.example .env
```
Fill in `.env`:
- `JWT_SECRET` and `OTP_SECRET`: generate each with `openssl rand -base64 48`
- `MAIL_USERNAME` / `MAIL_PASSWORD`: Gmail address and an App Password (needed for OTP verification)
- `STRIPE_SECRET_KEY`: your test key (`sk_test_...`)
- `STRIPE_WEBHOOK_SECRET`: see step 3

### 2. Start the stack
```bash
docker compose up --build
```
Swagger UI: http://localhost:8080/swagger-ui.html

### 3. Forward Stripe webhooks
In a separate terminal:
```bash
stripe listen \
  --events checkout.session.completed,checkout.session.expired,payment_intent.payment_failed \
  --forward-to localhost:8080/payments/webhook
```
The CLI prints a signing secret starting with `whsec_`. Put it in `STRIPE_WEBHOOK_SECRET` in `.env`, then restart the API:
```bash
docker compose up -d
```

### 4. Try the full flow in Swagger UI
1. `POST /users` to register, then verify the OTP sent to your email
2. `POST /auth/login`, copy the token, click **Authorize** and paste it
3. Add items with `POST /cart/items`
4. `POST /orders/checkout` returns a Stripe Checkout URL. Open it in your browser and pay with Stripe's test card `4242 4242 4242 4242` (any future expiry, any CVC).
5. After you pay, Stripe sends a webhook to your API: the order flips to PAID and a confirmation email is sent.

### Creating an admin
Register and verify a user, then promote them:
```bash
docker compose exec mysql mysql -uroot -p ecommerce_db \
  -e "UPDATE users SET role='ADMIN' WHERE email='you@example.com';"
```
Log in again to get a token with the new role.

### Notes
- Browsing, cart and checkout creation work without Stripe. Only payment confirmation needs the webhook.
- `DB_PASSWORD` only takes effect when the MySQL volume is first created. `docker compose down -v` deletes the database.

## Tests
```bash
mvn test
```