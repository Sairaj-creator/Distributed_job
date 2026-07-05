# TaskFlow

TaskFlow is a production-styled Java 17 workflow orchestration engine. It can define workflows as DAGs, validate dependency graphs, compute parallel execution levels, run jobs on bounded worker pools, apply retry policies, publish lifecycle events, persist run history with JDBC, and expose CLI/HTTP status surfaces without Spring or an ORM.

## Quick Start

```bash
mvn clean verify
mvn -q exec:java
```

The default profile uses safe local settings. PostgreSQL details can be supplied through environment variables shown in `.env.example`; tests use H2.

## Features

- Immutable core domain model with `Job`, `Workflow`, `JobRun`, value-object identifiers, and behavior-rich enums.
- DAG validation with iterative DFS and topological sorting with Kahn's algorithm.
- Simplified 5-field cron parser and next-fire calculator.
- Bounded worker execution with timeouts, cancellation, per-job overlap locks, and retry strategies.
- Async event bus with console, SLF4J, and metrics listeners.
- JDBC repositories with dynamic filtering and pagination support.
- Thin CLI, repository-backed history output, and embedded JDK `HttpServer` JSON status API.
- Java serialization and hand-written JSON workflow checkpoints.
- Documentation in `docs/` plus running decision and changelog files.

## Progress

- [x] Phase 0: Project bootstrap
- [x] Phase 1: Domain model and core enums
- [x] Phase 2: DAG algorithms
- [x] Phase 3: Cron parsing and scheduling primitives
- [x] Phase 4: Concurrency core, scheduler engine, retry, priority
- [x] Phase 5: Event bus and listeners
- [x] Phase 6: JDBC persistence layer
- [x] Phase 7: Service layer and reporting
- [x] Phase 8: CLI and embedded HTTP API
- [x] Phase 9: Config, logging, bootstrap wiring
- [x] Phase 10: Testing, coverage, and documentation polish

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) and the diagrams in [docs/](docs/).

## License

MIT.
