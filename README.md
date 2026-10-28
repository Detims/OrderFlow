# OrderFlow

OrderFlow is a Java 21 / Spring Boot backend portfolio project built around two microservices: `order-service` and `inventory-service`. It uses separate PostgreSQL databases and RabbitMQ for asynchronous messaging. **The primary deployment target is a local Kubernetes cluster; the project does not use AWS.**

## Order flow

1. `order-service` validates an order, stores it as `PENDING`, and publishes an order request to RabbitMQ.
2. `inventory-service` reserves stock with a conditional database update inside a transaction. A processed-message record makes redelivery idempotent.
3. Inventory publishes the result; `order-service` changes the order to `CONFIRMED` or `REJECTED`.

The order database write and initial message publish are separate operations. Retrying a pending order with the same idempotency key republishes the request; a transactional outbox remains a stretch goal.

## Run on local Kubernetes (Kind)

Install Docker Desktop, `kubectl`, and [Kind](https://kind.sigs.k8s.io/). No AWS account or Java/Maven installation is needed to run the containers. Maven is needed to run the tests locally.

Create a local cluster and namespace:

```powershell
kind create cluster --name orderflow
kubectl apply -f k8s/namespace.yaml
```

Create local development credentials as a Kubernetes Secret. These values are only for this local demo; they are not committed to the repository.

```powershell
kubectl create secret generic orderflow-secrets -n orderflow `
  --from-literal=postgres-user=orderflow `
  --from-literal=postgres-password=local-dev-only `
  --from-literal=rabbitmq-user=orderflow `
  --from-literal=rabbitmq-password=local-dev-only
```

Build both container images and load them into Kind:

```powershell
docker build -f services/order-service/Dockerfile -t order-service:dev .
docker build -f services/inventory-service/Dockerfile -t inventory-service:dev .
kind load docker-image order-service:dev inventory-service:dev --name orderflow
```

Apply the databases, RabbitMQ, and app deployments:

```powershell
kubectl apply -f k8s/platform.yaml
kubectl apply -f k8s/apps.yaml
kubectl rollout status statefulset/order-db -n orderflow
kubectl rollout status statefulset/inventory-db -n orderflow
kubectl rollout status statefulset/rabbitmq -n orderflow
kubectl rollout status deployment/order-service -n orderflow
kubectl rollout status deployment/inventory-service -n orderflow
kubectl get pods,pvc -n orderflow
```

Forward the service ports to your PC. Keep each command running in its own PowerShell window:

```powershell
kubectl port-forward -n orderflow service/order-service 8080:8080
kubectl port-forward -n orderflow service/inventory-service 8081:8081
```

Use the API examples below. To inspect the RabbitMQ management console, run `kubectl port-forward -n orderflow service/rabbitmq 15672:15672` and open `http://localhost:15672` with the local credentials above.

Kind stores cluster data on your computer. Deleting the cluster also deletes its local databases and message broker data:

```powershell
kind delete cluster --name orderflow
```

## Kubernetes concepts demonstrated

The manifests in `k8s/` use Deployments and ClusterIP Services for the applications, StatefulSets and persistent volume claims for PostgreSQL and RabbitMQ, ConfigMaps for non-sensitive settings, Kubernetes Secrets for local credentials, and startup/readiness/liveness probes plus container resource requests and limits. App services are exposed to the host with `kubectl port-forward`; there is no public ingress.

## Alternative: Docker Compose

For a quicker local run without Kubernetes, use `docker compose up --build`. Compose starts both services, separate PostgreSQL databases, and RabbitMQ. The API ports are `8080` and `8081`, and RabbitMQ's management UI is at `http://localhost:15672`. Stop with `Ctrl+C`; `docker compose down -v` also deletes local database and queue data.

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

The request returns `PENDING`; poll until it becomes `CONFIRMED` or `REJECTED`. Repeating the same idempotency key and body returns the same order. Reusing a key with different order details returns a conflict.

## Tests

From the repository root, run `mvn test` when Maven is installed. The current tests cover reservation outcomes and duplicate-message handling.

## Configuration and AWS

The local credential defaults in `.env.example` and the Kubernetes instructions are for development only. Do not put production credentials in `.env`, YAML manifests, source code, or committed files. The application has no AWS SDK dependencies and does not call AWS services. Local Kubernetes uses only your computer's resources; AWS costs arise only if you separately deploy resources to AWS.

## Stretch goals

- Add a transactional outbox and dead-letter queues.
- Add Testcontainers integration tests and a CI workflow that builds and scans the images.
- Add Helm packaging, autoscaling, and network policies after the direct Kubernetes manifests are understood.
- Consider EKS only if cloud Kubernetes deployment becomes a separate goal.
