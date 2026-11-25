# Finance Analytics Java Spring Boot

A read-only REST API that serves stock and macroeconomic analytics from BigQuery. It is written in Java 21 with Spring Boot 3.3 and queries the `finance_marts` dataset produced by [data-engineer-finance-analytics](https://github.com/calemccammon/data-engineer-finance-analytics), which loads Yahoo Finance stock prices and FRED macro indicators and transforms them with dbt.

API reference (static Swagger UI on GitHub Pages): https://calemccammon.github.io/finance-analytics-java-springboot/

No public instance of the API is hosted, so browsing the reference runs no BigQuery queries. The OpenAPI spec lists `http://localhost:8080` as its only server. The application has no CORS configuration, so to execute requests use the Swagger UI that the application serves itself when it runs locally.

## Running locally

Prerequisites:

- Java 21 and Maven, or Docker
- GCP credentials with BigQuery read access to the `finance_marts` dataset
- At least one completed run of the `data-engineer-finance-analytics` pipeline, so that the tables exist

With Maven:

```bash
export BQ_PROJECT=your-gcp-project-id
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json

mvn spring-boot:run
```

With Docker Compose, which builds the image from the `Dockerfile` and mounts the credentials file read-only into the container:

```bash
export BQ_PROJECT=your-gcp-project-id
export GOOGLE_APPLICATION_CREDENTIALS=~/.config/gcloud/application_default_credentials.json

docker compose up
```

Then open:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/stocks/snapshot` | Latest trading-day row for every ticker, joined to company details |
| GET | `/api/stocks/performance` | Return over the last 365 days per ticker, sorted from best to worst |
| GET | `/api/stocks/{ticker}/monthly-returns` | Monthly returns for one ticker, most recent month first |
| GET | `/api/macro/indicators` | Daily history of the FRED macro indicators |
| GET | `/api/macro/vs-stocks` | Daily stock return statistics grouped by Federal Funds Rate regime |
| GET | `/api/sectors/summary` | Sector averages for the latest trading day |
| GET | `/actuator/health` | Health check |

All `/api` endpoints return a JSON array and take no query parameters. The ticker path variable is upper-cased before it is used. An unknown ticker returns an empty array.

## How it works

```
src/main/java/com/calemccammon/financeanalytics/
├── FinanceAnalyticsApplication.java
├── config/
│   ├── BigQueryConfig.java        # BigQuery client bean (all profiles except docs)
│   ├── BigQueryDocsConfig.java    # Stub BigQuery bean for the docs profile
│   └── OpenApiConfig.java         # Sets the server URL in the OpenAPI spec
├── controller/
│   ├── StocksController.java
│   ├── MacroController.java
│   └── SectorsController.java
├── exception/
│   └── GlobalExceptionHandler.java
├── model/                          # Java records returned as JSON
│   ├── StockSnapshot.java
│   ├── Performance52w.java
│   ├── MonthlyReturn.java
│   ├── MacroIndicator.java
│   ├── MacroVsStocks.java
│   └── SectorSummary.java
└── service/                        # BigQuery SQL and row mapping
    ├── StocksService.java
    ├── MacroService.java
    └── SectorsService.java
```

Each controller delegates to one service. Each service method holds one SQL statement as a text block, runs it through the `google-cloud-bigquery` client, and maps the result rows to a record. Every request runs a BigQuery query. There is no cache and no database other than BigQuery.

The queries read two tables in `finance_marts`: `fct_daily_trading` (one row per ticker per trading date, with prices, moving averages, volatility and the macro indicators for that date) and `dim_company` (industry, market cap category and exchange, used only by the snapshot endpoint). The project comes from the `BQ_PROJECT` environment variable. The dataset name comes from `BQ_DATASET` and defaults to `finance_marts`.

Errors are returned as RFC 7807 problem responses. `GlobalExceptionHandler` extends Spring's `ResponseEntityExceptionHandler`, which answers framework errors such as an unknown path or an unsupported method with their own status. Any other exception is logged and returned as a 500 with a generic detail message.

## Design notes

- **Percentages are returned as decimal fractions.** The marts store rates and returns as whole percentages (5.0 means 5%). The SQL divides by 100, so a field such as `dailyReturnPct` or `fedFundsRate` holds 0.05 for 5%. `cpi` is an index value and is returned unchanged.
- **Rate regimes use the stored percentage values.** `/api/macro/vs-stocks` buckets rows by `fed_funds_rate` at thresholds of 5, 3 and 1 percent.
- **Macro history is read from one ticker's rows.** The macro indicators are repeated on every ticker's row in `fct_daily_trading`. `/api/macro/indicators` filters to `ticker = 'AAPL'` to get one row per trading date, so it depends on AAPL being in the dataset.
- **The ticker is a bound query parameter.** `/api/stocks/{ticker}/monthly-returns` passes the ticker to BigQuery as a named parameter and does not interpolate it into the SQL.
- **The docs profile starts the application without credentials.** Under the `docs` Spring profile the BigQuery bean is a proxy that throws on any call. The `deploy-docs.yml` workflow starts the application with this profile, downloads `/v3/api-docs`, and publishes it with a Swagger UI page to GitHub Pages.

## Testing

```bash
mvn test
```

Without `BQ_PROJECT` set, this runs the controller tests, which use `@WebMvcTest` and MockMvc with the services mocked. They check routing, status codes, JSON field names and the error responses, and need no GCP credentials.

With `BQ_PROJECT` and `GOOGLE_APPLICATION_CREDENTIALS` set, the same command also runs the integration tests in `FinanceApiIntegrationTest`. They start the application on a random port, call each endpoint against BigQuery, and assert a 200 response with a non-empty body. The class is gated by `@EnabledIfEnvironmentVariable(named = "BQ_PROJECT")` and is skipped otherwise.

```bash
export BQ_PROJECT=your-gcp-project-id
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
mvn test
```

## CI/CD

All four workflows run on push to `main`. `ci.yml` also runs on pull requests.

| Workflow | Description |
|----------|-------------|
| `ci.yml` | Runs `mvn test` without credentials (unit tests only) |
| `integration-test.yml` | Runs `mvn test` with BigQuery credentials from repository secrets (unit and integration tests) |
| `deploy-docs.yml` | Generates the OpenAPI spec under the `docs` profile and deploys Swagger UI to GitHub Pages |
| `docker-publish.yml` | Builds the Docker image and pushes it to GHCR, tagged `latest` and `sha-<commit>` |

## Docker and Kubernetes

The `Dockerfile` is a two-stage build: Maven on Temurin 21 builds the JAR, and the runtime image is `eclipse-temurin:21-jre-alpine`. The image is published at `ghcr.io/calemccammon/finance-analytics-java-springboot`.

The `k8s/` directory has a namespace, a single-replica Deployment with readiness and liveness probes on `/actuator/health`, a ClusterIP Service and a ConfigMap that sets `BQ_DATASET`. The Deployment uses `imagePullPolicy: IfNotPresent`, so an image built inside minikube is used in place of the one on GHCR. [k8s/README.md](k8s/README.md) gives the steps for minikube, with credentials supplied as Kubernetes secrets, and describes the changes needed to use Workload Identity on GKE.

## Limitations

- There is no authentication, rate limiting, caching or pagination. `/api/macro/indicators` returns the full history in one response.
- The unit tests mock the services, so the SQL and row mapping are exercised only by the integration tests against BigQuery.
