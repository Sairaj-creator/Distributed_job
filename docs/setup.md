# Setup

## Prerequisites

- JDK 17 or newer
- Maven 3.9+
- Optional PostgreSQL for a production-like profile

## Build

```bash
mvn clean verify
```

## Run

```bash
mvn -q exec:java
```

## Environment Variables

| Variable | Purpose |
|---|---|
| `TASKFLOW_PROFILE` | `dev` or `prod` profile selection |
| `TASKFLOW_DB_URL` | JDBC URL |
| `TASKFLOW_DB_USER` | Database username |
| `TASKFLOW_DB_PASSWORD` | Database password |
| `TASKFLOW_WORKER_THREADS` | Worker pool size |
| `TASKFLOW_HTTP_PORT` | Embedded status API port |
