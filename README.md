
# Book-Loan Microservices


The key technologies include Spring Boot (REST APIs, Actuator), Apache Kafka (event streaming), Keycloak (OAuth2/OpenID Connect identity provider), Docker (containerization), and Kubernetes (orchestration). The project follows Domain-Driven Design (DDD) principles and an event-driven architecture to demonstrate scalability and flexibility. It is organized into independent, loosely coupled services, with each service running as its own process/jar. This separation enables rapid deployment and scaling of individual components.

* <b>Spring Boot (Java 21):</b> Used for all microservices (latest Spring Boot 3.x, Java 21 LTS).
* <b>Apache Kafka:</b> A distributed event streaming platform for async communication between services. Services publish/subscribe to Kafka topics to decouple workflows.
* <b>Keycloak:</b> An open-source IAM server (OAuth2/OIDC) providing centralized authentication/authorization. All microservices rely on Keycloak-issued JWTs to secure their APIs.
* <b>Domain-Driven Design (DDD):</b> Each service represents a distinct business domain (bounded context). This ensures clear ownership and a clean domain model per service.
* <b>CQRS / Event-Driven:</b> The design separates commands (writes) and queries (reads) where appropriate, using Kafka to propagate domain events. This improves scalability and eventual consistency.
* <b>Containerization & Orchestration:</b> Services are packaged in Docker images; Docker Compose scripts are provided for local setup. Kubernetes manifests allow deployment to a K8s cluster.
* <b>Observability:</b> All services expose metrics, health checks, and logs (e.g. via Spring Actuator). A monitoring stack (Prometheus/Grafana) can scrape these for dashboards, following the “three pillars” of observability: logs, metrics, and traces.

## Architecture overview:
![image](https://github.com/user-attachments/assets/e29f2d7d-5b93-4c6f-b79a-e24d5d407ba4)

The system follows a microservices architecture of independent, loosely coupled components:
* <b>Independent Services:</b> Each microservice runs and scales on its own. Changes to one service do not require redeploying others.
* <b>Bounded Contexts (DDD):</b> Code and data are organized by business domain. For example, the articles service exclusively manages article data, while an authors service might manage author profiles.
* <b>CQRS / Event-Driven:</b> Write operations (commands) and read operations (queries) can use separate models for efficiency. Services publish events to Kafka rather than invoking each other directly, promoting loose coupling.
* <b>API Gateway / Routing:</b> In a real deployment, an API gateway or load balancer (e.g. Zuul, Spring Cloud Gateway, or Kubernetes Ingress) could route external traffic to the appropriate service. All external calls require a valid JWT from Keycloak.
* <b>Security Layer (Keycloak):</b> Keycloak (OAuth2/OIDC) handles authentication. Each service enforces authorization on its endpoints using Keycloak’s roles and realm settings.
* <b>Observability:</b> Centralized monitoring is set up so that metrics, logs, and distributed traces can be collected from every service for debugging and performance tuning.

## Setup instructions:
<b>Prerequisites:</b> Java 21 JDK, Maven (or Gradle), Docker & Docker Compose. (Optional: Kubernetes cluster with `kubectl` for cloud deployment).

<b>Running Locally (Docker Compose):</b>

1. Clone the repository: `git clone https://github.com/0xthomasit/java_microservices.git && cd java_microservices`.
2. Build all microservices: `mvn clean package` (or use the provided Maven wrapper `./mvnw`).
3. Start everything with Docker Compose:
    ```Powershell
    docker-compose up -d
    ```
    This will launch Kafka, Keycloak, and all the Spring Boot services.
4. Wait a minute for all services to initialize (Keycloak and Kafka may take a bit).
5. Access the services on their configured ports (e.g. Keycloak on 8080, API on 8081, etc). Use `docker-compose logs -f` to see startup logs.
6. To stop the environment: `docker-compose down`.

<b>Running on Kubernetes (optional):</b>
* Ensure your `kubectl` is connected to a cluster (e.g. use Minikube or any K8s).
* Apply the provided K8s manifests (in `k8s/`):
  ```PowerShell
  kubectl apply -f k8s/
  ```
  This deploys all services (Kafka StatefulSet, Keycloak Deployment, and the microservices Deployments/Services).
* Use `kubectl get all` to verify pods are running. Services can be exposed via ClusterIP or port-forward for local testing.
* For cleanup, use `kubectl delete -f k8s/`.
## API Usage:
Some example REST endpoints:
* `GET /api/v1/books` – Retrieve all books.
* `POST /api/v1/books` – Create a book.
* `GET /api/v1/books/{bookId}` – Retrieve a book detail by its `id`.
* Other endpoints would follow similar patterns (e.g. `/api/v1/employees`, `/api/v1/borrowing`, etc., depending on included services).
The API is documented with OpenAPI/Swagger. For instance, after starting the app you can visit `http://localhost:8081/swagger-ui.html` (adjust port per your setup) to explore all endpoints interactively. A Postman collection (`Microservices.postman_collection.json`) is also provided; you can import it to quickly test requests with example data.

## License:
This project is provided as-is for demo and educational purposes only. It does not include a production license. (In real use, you would choose an open-source license such as Apache-2.0 or MIT).
