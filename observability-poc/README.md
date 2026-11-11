# observability-poc

A dummy Spring Boot project that demonstrates **both**:

| Tool | What it does here | URL |
|------|-------------------|-----|
| **Grafana** (+ Prometheus) | Live monitoring: request rate, errors, latency, JVM, custom business metrics | http://localhost:3000 |
| **SonarQube** | Static code quality: bugs, smells, security hotspots, test coverage | http://localhost:9000 |

Stack: Java 17, Spring Boot 3.3.5, H2 (in-memory), Maven, Docker.

---

## 1. Prerequisites

- JDK 17+
- Maven (or IntelliJ's bundled Maven)
- Docker Desktop (running)
- IntelliJ IDEA: **File > Open** the `observability-poc` folder (pom.xml)

## 2. Run the application

```bash
mvn spring-boot:run
```
(or run `ObservabilityPocApplication` from IntelliJ)

Check it works:

- http://localhost:8080/api/products
- http://localhost:8080/actuator/health
- http://localhost:8080/actuator/prometheus  <- the metrics Grafana will use

Sample Postman calls:

```
POST http://localhost:8080/api/products
{ "name": "Pen", "price": 10, "quantity": 5 }

GET    /api/products        GET /api/products/{id}
PUT    /api/products/{id}   DELETE /api/products/{id}
GET    /api/demo/slow       (random 200-1500 ms, for the latency panel)
GET    /api/demo/error      (always HTTP 500, for the error panel)
```

---

## 3. Part A - Grafana

How it fits together:

```
Spring Boot (Actuator + Micrometer)  -->  Prometheus (scrapes every 5s)  -->  Grafana (dashboard)
        /actuator/prometheus                    :9090                            :3000
```

Steps:

1. Start the app (step 2) so port 8080 is up.
2. Start Prometheus and Grafana:
   ```bash
   docker compose up -d prometheus grafana
   ```
3. Check Prometheus target is UP: http://localhost:9090/targets
4. Generate traffic (Git Bash / WSL / Linux / macOS):
   ```bash
   ./scripts/load-test.sh 60
   ```
   On Windows without bash, just hit the endpoints from Postman a few times.
5. Open Grafana: http://localhost:3000 (login `admin` / `admin`)
6. Open dashboard **Spring Boot POC - Overview** (already provisioned, no import needed).

Panels: request rate per URI, 5xx error rate, p95 latency, JVM heap, products in DB,
products created (custom counter), live threads, CPU.

Where the custom metrics come from: `ProductService` registers `products.created`,
`products.deleted` and `products.total` with Micrometer. Prometheus exposes them as
`products_created_total`, `products_deleted_total`, `products_total`.

---

## 4. Part B - SonarQube

1. Start SonarQube (first start takes 1-2 minutes):
   ```bash
   docker compose up -d sonar-db sonarqube
   ```
   Linux only: if it exits, run `sudo sysctl -w vm.max_map_count=262144` first.
2. Open http://localhost:9000 and log in with `admin` / `admin` (you will be asked to change the password).
3. Create a token: **My Account > Security > Generate Tokens** (type: *User* or *Project analysis*). Copy it.
4. Run tests, build coverage, and scan:
   ```bash
   mvn clean verify sonar:sonar -Dsonar.token=YOUR_TOKEN
   ```
   (IntelliJ: Maven tool window > Run Maven Goal, same command.)
5. Open http://localhost:9000/dashboard?id=observability-poc

What to expect:

- JaCoCo writes `target/site/jacoco/jacoco.xml`, which Sonar reads for coverage.
- `LegacyReportGenerator` is **intentionally smelly**. Sonar should flag things like a
  hard-coded password, an empty catch block and an unused variable.
- Its HTML branch has no test, so coverage is below 100%.

Practice loop: fix an issue (or add a test for the HTML branch), re-run the command in step 4,
and refresh the dashboard to see the numbers change.

---

## 5. Stop everything

```bash
docker compose down        # keep data volumes
docker compose down -v     # also delete SonarQube data
```

## 6. Project layout

```
observability-poc
|-- docker-compose.yml                 Prometheus, Grafana, SonarQube + Postgres
|-- monitoring/
|   |-- prometheus.yml                 scrape config (targets host.docker.internal:8080)
|   `-- grafana/
|       |-- provisioning/              auto-wires the datasource and dashboard folder
|       `-- dashboards/spring-boot-dashboard.json
|-- scripts/load-test.sh               traffic generator
|-- pom.xml                            actuator, micrometer-prometheus, jacoco, sonar plugin
`-- src/
    |-- main/java/com/example/observability/
    |   |-- controller/ (Product, Demo)   service/   repository/   model/   exception/
    |   `-- legacy/LegacyReportGenerator  (deliberate smells for Sonar)
    `-- test/java/...                     JUnit 5 + Mockito + MockMvc tests
```

## 7. Troubleshooting

- **Prometheus target DOWN**: the app must be running on port 8080; on Linux the compose file already maps `host.docker.internal`.
- **Grafana panels empty**: run the load script, wait ~15 seconds, set time range to "Last 15 minutes".
- **Sonar shows 0% coverage**: you must run `verify` (not just `compile`) so JaCoCo creates its report.
- **Sonar auth error 401**: token is wrong or expired; generate a new one.
