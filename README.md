# OrderFlow

OrderFlow is a Spring Boot backend project built around two microservices: `order-service` and `inventory-service`. It uses separate PostgreSQL databases and RabbitMQ for asynchronous messaging.

## Order flow

1. `order-service` validates an order, stores it as `PENDING`, then publishes an order request.
2. `inventory-service` conditionally reserves stock in a database transaction. A processed-message record makes redelivery idempotent.
3. Inventory publishes a result; `order-service` changes the order to `CONFIRMED` or `REJECTED`.

The order database write and initial message publish are separate operations. Retrying a pending order with the same idempotency key republishes the request; a transactional outbox remains a stretch goal.

## Run with Docker Compose

Requirements: Java 21 and Docker Desktop with Compose. Maven is needed only to run tests outside the containers.

```powershell
docker compose up --build
```

Compose starts the two services, two PostgreSQL databases, and RabbitMQ. API ports are `8080` (orders) and `8081` (inventory). RabbitMQ management UI is at `http://localhost:15672` using local development credentials from `.env` or the documented defaults. Stop with `Ctrl+C`; run `docker compose down` to stop and remove containers. `docker compose down -v` also deletes local database and queue data.

Check service health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
```

## Try the API

Set stock:

```powershell
Invoke-RestMethod -Method Put -Uri http://localhost:8081/inventory/BOOK-1 `
  -ContentType 'application/json' -Body '{"available":5}'
```

Create an order and poll for its final status:

```powershell
$headers = @{ 'Idempotency-Key' = 'demo-order-001' }
$order = Invoke-RestMethod -Method Post -Uri http://localhost:8080/orders `
  -Headers $headers -ContentType 'application/json' -Body '{"sku":"BOOK-1","quantity":2}'
Invoke-RestMethod "http://localhost:8080/orders/$($order.id)"
```

The request returns `PENDING`; poll until it becomes `CONFIRMED` or `REJECTED`. Repeating the same idempotency key and body returns the same order. A key reused with different order details returns a conflict.

## Tests

From the repository root, run `mvn test` when Maven is installed. The current test suite covers inventory reservation outcomes and duplicate-message handling.

## Configuration and credentials

Local defaults are in `application.yaml` and Compose. `.env.example` documents optional local database settings. The ignored `.env` file is for local-only values; do not put production credentials there. This project uses RabbitMQ locally and has no AWS SDK, AWS credentials, or AWS service dependency.

## Stretch goals

- Deploy the same containers to a local Kubernetes cluster and configure probes, resources, secrets, and persistent volumes.
- Add a transactional outbox, dead-letter queues, and Testcontainers integration tests.
- Consider cloud deployment only if it serves a separate learning goal.
