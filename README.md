# OrderFlow

OrderFlow is a Spring Boot backend project built around two microservices: `order-service` and `inventory-service`. It uses separate PostgreSQL databases and asynchronous messaging through Amazon SQS.

## What happens when an order is submitted

1. `order-service` validates the request, writes a `PENDING` order, and publishes an `OrderRequested` message.
2. `inventory-service` handles the message in a database transaction. A conditional SQL update reserves stock only when enough is available; a processed-message row records the result so redelivery cannot decrement stock twice.
3. Inventory publishes an `OrderResult`. `order-service` consumes it and changes the order to `CONFIRMED` or `REJECTED`.
4. SQS messages are deleted only after the database work and result publication succeed. A crash can cause redelivery, which is why both sides are idempotent.

This is an educational MVP. The database write and first SQS publish are separate operations, so a process crash between them can leave an order in `PENDING` until the client retries with the same idempotency key (a pending retry republishes safely). A transactional outbox is a good stretch goal once the core flow is understood.

## Requirements

- Java 21
- Maven 3.6.3+
- Docker Desktop with Docker Compose

## Run locally

From this repository directory:

```powershell
docker compose up --build
```

Compose starts two separate PostgreSQL databases, LocalStack with the two SQS queues, and both services. The first build downloads Maven dependencies and container images. Leave the terminal open; use `Ctrl+C` to stop the services.

Check health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
```

## Try an order

Set available stock for a SKU:

```powershell
Invoke-RestMethod -Method Put -Uri http://localhost:8081/inventory/BOOK-1 `
  -ContentType 'application/json' -Body '{"available":5}'
```

Submit an order. Use a fresh `Idempotency-Key` for a new request; repeating the same key and body returns the original order.

```powershell
$headers = @{ 'Idempotency-Key' = 'demo-order-001' }
$order = Invoke-RestMethod -Method Post -Uri http://localhost:8080/orders `
  -Headers $headers -ContentType 'application/json' -Body '{"sku":"BOOK-1","quantity":2}'
$order
```

The create endpoint responds with status `PENDING` and an ID. Poll that order until the status becomes `CONFIRMED` or `REJECTED`:

```powershell
Invoke-RestMethod "http://localhost:8080/orders/$($order.id)"
```

Try requesting more units than remain to see `REJECTED`; inventory should never drop below zero:

```powershell
$headers = @{ 'Idempotency-Key' = 'demo-order-002' }
$order2 = Invoke-RestMethod -Method Post -Uri http://localhost:8080/orders `
  -Headers $headers -ContentType 'application/json' -Body '{"sku":"BOOK-1","quantity":99}'
Invoke-RestMethod "http://localhost:8080/orders/$($order2.id)"
Invoke-RestMethod http://localhost:8081/inventory
```

## Run tests

With Java and Maven installed, run from the repository root:

```powershell
mvn test
```

The tests include focused checks for reservation outcomes and duplicate-message handling. The end-to-end flow can be exercised with the Compose setup above.

## Run services from the IDE

Start only infrastructure first with `docker compose up order-db inventory-db localstack`. Set these environment variables in the run configuration for both Spring Boot applications:

```text
AWS_REGION=us-west-2
AWS_ENDPOINT_URL=http://localhost:4566
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
```

For order-service, also set `DB_URL=jdbc:postgresql://localhost:5433/orders`. For inventory-service, set `DB_URL=jdbc:postgresql://localhost:5434/inventory`. Both use `DB_USER=orderflow` and `DB_PASSWORD=local-dev-only` by default.

LocalStack's `test` credentials are fake local-only values. Never put real AWS keys in `.env`, `application.yaml`, source code, Dockerfiles, or committed files. For future AWS development, use AWS IAM Identity Center / SSO and the AWS SDK default credential provider chain (for example, an `AWS_PROFILE` set in your local shell). In AWS, prefer an IAM role attached to the runtime. Do not pass personal AWS credentials into these containers. This MVP defaults to LocalStack and makes no AWS calls.

## Endpoints

| Service | Endpoint | Purpose |
|---|---|---|
| order-service | `POST /orders` | Create an order; requires `Idempotency-Key` |
| order-service | `GET /orders/{id}` | Read order status |
| inventory-service | `PUT /inventory/{sku}` | Set stock for local demo/testing |
| inventory-service | `GET /inventory` | List stock levels |
| both | `/actuator/health` | Health status |

## Configuration

Each service supports environment overrides for `DB_URL`, `DB_USER`, `DB_PASSWORD`, `AWS_REGION`, `AWS_ENDPOINT_URL`, `ORDER_REQUEST_QUEUE`, and `ORDER_RESULT_QUEUE`. An empty `AWS_ENDPOINT_URL` uses AWS's normal endpoint resolution and the AWS SDK default credentials provider chain. Local development should use the Compose LocalStack endpoint.

## Next steps (stretch goals)

- Add a transactional outbox and a publisher retry loop to close the order-write/message-publish gap.
- Add Testcontainers integration tests for PostgreSQL and LocalStack.
- Add SQS dead-letter queues, visibility timeout tuning, structured logs, metrics, and tracing.
- Add authentication and authorization before exposing write endpoints.
- Deploy to AWS and add infrastructure as code only after the local flow and cost controls are understood.
