# Finance Analytics Java Spring Boot

A read-only REST API that serves finance analytics data from BigQuery, built as a showcase of Java 21 + Spring Boot 3 + Google Cloud BigQuery integration.

Data is sourced from [data-engineer-finance-analytics](https://github.com/calemccammon/data-engineer-finance-analytics) — Yahoo Finance stock prices and FRED macro indicators, transformed by dbt.

## API Documentation

Browse the interactive Swagger UI (no server required):
👉 **https://calemccammon.github.io/finance-analytics-java-springboot/**

> **"Try it out"** only works when you're running the app locally — the spec points to `http://localhost:8080`. No public server is exposed, so browsing the docs incurs no BigQuery costs.

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/stocks/snapshot` | Latest trading-day snapshot for all tickers |
| GET | `/api/stocks/performance` | 52-week return leaders & laggards |
| GET | `/api/stocks/{ticker}/monthly-returns` | Monthly returns for a single ticker |
| GET | `/api/macro/indicators` | FRED macro indicator history |
| GET | `/api/macro/vs-stocks` | Stock returns grouped by rate regime |
| GET | `/api/sectors/summary` | Sector-level performance for the latest trading day |
| GET | `/actuator/health` | Health check |

## Prerequisites

- Java 21 + Maven 3.9+ (or Docker)
- A GCP service account with BigQuery read access to the `finance_marts` dataset
- The `data-engineer-finance-analytics` pipeline must have run at least once

## Running Locally

**Option A — Maven:**
```bash
export BQ_PROJECT=your-gcp-project-id
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json

mvn spring-boot:run
```

**Option B — Docker Compose:**
```bash
# Windows (PowerShell)
$env:BQ_PROJECT = "your-gcp-project-id"
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\Users\<you>\AppData\Roaming\gcloud\application_default_credentials.json"

# macOS/Linux
export BQ_PROJECT=your-gcp-project-id
export GOOGLE_APPLICATION_CREDENTIALS=~/.config/gcloud/application_default_credentials.json

docker compose up
```

Then open:
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health

## Running Tests

**Unit tests** (no GCP credentials required — BigQuery is mocked):
```bash
mvn test
```

**Integration tests** (require real BigQuery):
```bash
export BQ_PROJECT=your-gcp-project-id
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
mvn test
```
Integration tests are gated by `@EnabledIfEnvironmentVariable(named = "BQ_PROJECT")` — they are skipped automatically when `BQ_PROJECT` is not set.

## CI/CD

| Workflow | Trigger | Description |
|----------|---------|-------------|
| `ci.yml` | Push + PR to main | Build + unit tests (no credentials needed) |
| `integration-test.yml` | Push to main | Integration tests against real BigQuery |
| `deploy-docs.yml` | Push to main | Generates OpenAPI spec → deploys Swagger UI to GitHub Pages |
| `docker-publish.yml` | Push to main | Builds Docker image → pushes to GHCR |

## Docker & Kubernetes

The app ships as a Docker image published to [GitHub Container Registry](https://ghcr.io/calemccammon/finance-analytics-java-springboot).

For Kubernetes deployment instructions (minikube + GKE Workload Identity), see [k8s/README.md](k8s/README.md).

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Data source | Google BigQuery (`finance_marts` dataset) |
| API docs | springdoc-openapi + Swagger UI |
| Build | Maven |
| Container | Docker (multi-stage) |
| Orchestration | Kubernetes |
| CI/CD | GitHub Actions |
| Image registry | GitHub Container Registry (GHCR) |
| Docs hosting | GitHub Pages |

## Project Structure

```
src/main/java/com/calemccammon/financeanalytics/
├── FinanceAnalyticsApplication.java
├── config/
│   ├── BigQueryConfig.java        # Real BigQuery bean (requires GOOGLE_APPLICATION_CREDENTIALS)
│   ├── BigQueryDocsConfig.java    # Stub bean for docs profile (no credentials needed)
│   └── OpenApiConfig.java         # Swagger UI server configuration
├── controller/
│   ├── StocksController.java
│   ├── MacroController.java
│   └── SectorsController.java
├── exception/
│   └── GlobalExceptionHandler.java
├── model/                          # Java records (DTOs)
│   ├── StockSnapshot.java
│   ├── Performance52w.java
│   ├── MonthlyReturn.java
│   ├── MacroIndicator.java
│   ├── MacroVsStocks.java
│   └── SectorSummary.java
└── service/
    ├── StocksService.java          # BigQuery SQL for stocks endpoints
    ├── MacroService.java
    └── SectorsService.java
```
