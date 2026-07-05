# TASKFLOW — DISTRIBUTED JOB SCHEDULING & WORKFLOW ORCHESTRATION ENGINE
## Complete Software Requirement Specification, Architecture Guide, Coding Standard & Implementation Roadmap for an AI Coding Agent

**Document Version:** 1.0
**Prepared For:** Autonomous AI Coding Agent (e.g. Claude Code, Cursor Agent, Devin-style agent)
**Prepared By:** Senior Software Architecture Review Panel (Architect / Senior Java Dev / Interviewer / Tech Lead / Resume Reviewer)
**Target Outcome:** A resume-grade, interview-grade, production-styled Java backend project — NOT a CRUD app.

> **READ THIS FIRST (Agent Directive):** This document is the single source of truth. Do not skip sections. Do not invent scope not described here without flagging it in a `DECISIONS.md` log. Build phase-by-phase, in the exact order given in Part 9 (Implementation Roadmap). After every phase, update `README.md` and generate/verify JavaDoc. Do not proceed to the next phase until the current phase compiles, passes its tests, and is committed to Git with a conventional commit message.

---

## TABLE OF CONTENTS

1. Project Idea Brainstorm & Comparative Evaluation
2. Chosen Project & Justification
3. Product Vision & Software Requirement Specification (SRS)
4. Java & Software Engineering Concept Coverage Map
5. Data Structures & Algorithms Usage Map
6. System Architecture
7. Database Design
8. Folder & Package Structure
9. Implementation Roadmap (Phased Plan)
10. Design Patterns Catalog (Applied)
11. Testing Strategy
12. Logging Strategy
13. Configuration & Secrets Management
14. Git Workflow & Code Review Checklist
15. Code Quality Standards
16. Bonus / Stretch Features
17. Documentation Deliverables Checklist
18. Resume & Career Section
19. 100 Interview Questions With Answers (Based On This Project)
20. AI Coding Agent Operating Rules (Recap & Enforcement)

---

# PART 1 — PROJECT IDEA BRAINSTORM & COMPARATIVE EVALUATION

Ten candidate project ideas were generated and scored against ten weighted criteria (1–10 scale, 10 = best). All ideas deliberately avoid Todo Lists, Library/Student/Banking/Employee CRUD, and basic Inventory CRUD, per the mandate.

## 1.1 Candidate Ideas

| # | Idea | One-Line Description |
|---|------|----------------------|
| 1 | **TaskFlow** | A distributed job scheduler / workflow orchestration engine (like a mini Airflow/Quartz) with DAG-based task dependencies, cron scheduling, retries, worker thread pools, and persistence. |
| 2 | **RouteOptima** | A logistics fleet route optimization engine using graph algorithms (Dijkstra/A*) for delivery routing with concurrent simulation. |
| 3 | **CacheForge** | A custom distributed in-memory cache (LRU/LFU) with eviction policies, TTL, and a simple network protocol. |
| 4 | **StreamWatch** | A real-time log ingestion & anomaly detection engine using multithreaded producers/consumers. |
| 5 | **MarketSim** | A stock order-matching engine (limit order book) simulating an exchange. |
| 6 | **RateGuard** | An API Gateway simulator with rate limiting, circuit breaking, and load balancing strategies. |
| 7 | **EventHive** | An event-driven notification/pub-sub broker with topic partitioning. |
| 8 | **ChessEngine** | A chess move-validation and AI opponent engine (minimax + alpha-beta pruning). |
| 9 | **CompressLab** | A custom file compression/deduplication tool (Huffman coding + chunk hashing). |
| 10 | **RecoEngine** | A content/product recommendation engine using collaborative filtering. |

## 1.2 Scoring Matrix

Criteria: Resume Value (RV), Interview Value (IV), Difficulty (DF, higher = more impressive complexity handled), Scalability (SC), Learning Opportunity (LO), Industry Relevance (IR), Portfolio Attractiveness (PA), Real-World Usefulness (RU), Recruiter Impression (RI), Future Extensibility (FE).

| Idea | RV | IV | DF | SC | LO | IR | PA | RU | RI | FE | **Total /100** |
|------|----|----|----|----|----|----|----|----|----|----|------------------|
| TaskFlow | 10 | 10 | 8 | 9 | 10 | 10 | 9 | 9 | 10 | 10 | **95** |
| RouteOptima | 9 | 8 | 8 | 7 | 8 | 7 | 8 | 8 | 8 | 6 | 77 |
| CacheForge | 8 | 9 | 7 | 8 | 8 | 8 | 7 | 6 | 7 | 6 | 74 |
| StreamWatch | 8 | 8 | 7 | 8 | 8 | 8 | 7 | 7 | 7 | 6 | 74 |
| MarketSim | 9 | 9 | 9 | 7 | 9 | 7 | 8 | 6 | 8 | 5 | 77 |
| RateGuard | 8 | 8 | 6 | 7 | 7 | 8 | 7 | 7 | 7 | 6 | 71 |
| EventHive | 8 | 7 | 7 | 8 | 7 | 7 | 7 | 6 | 6 | 7 | 70 |
| ChessEngine | 6 | 7 | 8 | 4 | 8 | 4 | 8 | 4 | 6 | 4 | 59 |
| CompressLab | 6 | 6 | 6 | 5 | 6 | 5 | 6 | 6 | 5 | 4 | 55 |
| RecoEngine | 7 | 7 | 6 | 6 | 7 | 7 | 6 | 6 | 6 | 6 | 64 |

## 1.3 Why TaskFlow Wins

- **Recruiters instantly understand the value.** Every backend team has scheduled jobs, retries, dead-letter handling, and DAG-based pipelines (CI/CD, ETL, cron replacements, Airflow/Temporal/Quartz-style systems). Recruiters pattern-match this to "this person understands production backend systems."
- **It naturally forces the use of nearly every mandatory Java topic** without contrivance: concurrency (worker pools), collections (priority queues for scheduling, concurrent maps for job registry), graphs (DAG dependency resolution + cycle detection + topological sort), Java Time API (cron/interval scheduling), streams (reporting/analytics), JDBC (persisting job runs/state), design patterns (Strategy for retry policies, Factory for job creation, Observer for status events, Builder for job definitions, Command for job execution, Singleton for the scheduler registry).
- **It scales cleanly across 3–6 weeks** — Phase 1 can be a working single-threaded scheduler; Phase 6 can be a fully persistent, multi-worker, DAG-aware, retryable, observable orchestration engine with a CLI and metrics dashboard.
- **It is genuinely differentiated.** Nobody else's resume among a stack of CRUD apps says "I built a workflow orchestration engine similar in spirit to Apache Airflow / Quartz Scheduler, in core Java, with DAG scheduling and fault-tolerant retries."
- **Interview mileage is enormous.** It naturally leads to conversations about concurrency correctness, deadlocks, idempotency, exactly-once vs at-least-once execution, backpressure, and distributed systems trade-offs — all senior-sounding topics from a 4th-year student's project.

**DECISION: Build TaskFlow.**


---

# PART 2 — CHOSEN PROJECT & JUSTIFICATION (EXPANDED)

## 2.1 What TaskFlow Is

**TaskFlow** is a standalone Java application (no Spring — deliberately, so the agent must hand-build DI, layering, and infrastructure to prove Java fundamentals) that lets a user define **Jobs** (units of work), group them into **Workflows** (DAGs of jobs with dependencies), and schedule them to run **once**, **on an interval**, or **on a cron expression**. TaskFlow executes jobs using a configurable pool of worker threads, tracks execution history in a relational database, retries failed jobs according to pluggable retry strategies (fixed delay, exponential backoff, no-retry), supports job timeouts and cancellation, prevents duplicate/overlapping runs via locking, emits status-change events to registered listeners (console logger, file logger, in-memory metrics collector), and exposes a command-line interface plus a small embedded HTTP status endpoint for inspecting workflow state.

It is analogous in spirit — at a much smaller, learnable scale — to **Quartz Scheduler**, **Apache Airflow**, and **Temporal**.

## 2.2 Primary Personas & Use Cases

- **Backend Engineer (the "user" of TaskFlow as a library/tool):** defines jobs in Java (implementing a `Job` interface) or in a declarative YAML/properties-like job-definition file, wires them into a workflow, and starts the engine.
- **Operator:** uses the CLI to list workflows, trigger manual runs, pause/resume schedules, inspect run history, and view failure reasons.

Example real-world analogs TaskFlow demonstrates understanding of: nightly ETL pipelines, report-generation pipelines, CI/CD pipeline stage execution, cron-replacement services, retry/backoff logic in distributed systems, DAG-based build systems (like Make/Bazel's dependency graph, but for runtime jobs).

## 2.3 Non-Goals (Explicitly Out of Scope)

- Not a distributed/multi-node scheduler (single JVM only) — this is called out explicitly as a "Future Scope" item so the agent does not over-build.
- No web UI (a CLI + simple JSON status endpoint is enough; do not build a frontend).
- No Kubernetes/containers orchestration — pure Java, JDBC, and standard library plus JUnit/Mockito/SLF4J/Logback only.

---

# PART 3 — PRODUCT VISION & SOFTWARE REQUIREMENT SPECIFICATION (SRS)

## 3.1 Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-01 | The system shall allow defining a `Job` as a class implementing a `Job` functional-style interface with a `JobResult execute(JobContext ctx) throws JobExecutionException` method. |
| FR-02 | The system shall allow grouping jobs into a `Workflow`, expressed as a Directed Acyclic Graph (DAG) of `JobNode`s with explicit `dependsOn` relationships. |
| FR-03 | The system shall detect cycles in a workflow graph at registration time and reject invalid workflows with a `CyclicWorkflowException`. |
| FR-04 | The system shall compute a valid execution order via topological sort, and shall execute independent jobs concurrently while respecting dependency ordering. |
| FR-05 | The system shall support three schedule types: `ONE_TIME` (run once at/after a given `Instant`), `FIXED_INTERVAL` (run every N seconds/minutes/hours), and `CRON` (a simplified 5-field cron expression parsed with a custom parser, no external libraries). |
| FR-06 | The system shall execute jobs using a bounded, configurable `ExecutorService` thread pool ("worker pool"), decoupled from the scheduling thread. |
| FR-07 | The system shall support per-job retry policies: `NoRetryPolicy`, `FixedDelayRetryPolicy(maxAttempts, delay)`, `ExponentialBackoffRetryPolicy(maxAttempts, initialDelay, multiplier, maxDelay)`. Retry policies are pluggable via the Strategy pattern. |
| FR-08 | The system shall enforce a configurable timeout per job execution; a job exceeding its timeout is interrupted and marked `TIMED_OUT`. |
| FR-09 | The system shall prevent overlapping executions of the same scheduled job (configurable: `SKIP`, `QUEUE`, or `RUN_CONCURRENTLY`), guarded with per-job locks. |
| FR-10 | The system shall persist every job run (job id, workflow id, start time, end time, status, attempt number, output/error message) to a relational database via JDBC (no ORM — hand-written DAO layer, to demonstrate raw JDBC competence). |
| FR-11 | The system shall emit lifecycle events (`SCHEDULED`, `STARTED`, `SUCCEEDED`, `FAILED`, `RETRYING`, `TIMED_OUT`, `CANCELLED`) to an internal event bus; listeners (Observer pattern) may subscribe: a console logger listener, a file/SLF4J logger listener, and an in-memory metrics listener. |
| FR-12 | The system shall provide a CLI (`TaskFlowCli`) supporting: `list-workflows`, `show-workflow <id>`, `trigger <workflowId>`, `pause <workflowId>`, `resume <workflowId>`, `history <jobId> [--limit N]`, `stats`. |
| FR-13 | The system shall provide a minimal embedded HTTP endpoint (`com.sun.net.httpserver.HttpServer`, JDK built-in, no external web framework) exposing `GET /status` (JSON summary) and `GET /workflows/{id}` (JSON detail). |
| FR-14 | The system shall support job configuration via `.properties` files and environment variable overrides, using `ResourceBundle`/`Properties` plus a `ConfigService` abstraction. |
| FR-15 | The system shall generate a human-readable and CSV run report on demand (`ReportService`), using the Streams API for aggregation (success rate, average duration, p95 duration per job). |
| FR-16 | The system shall support pagination and filtering of job-run history (by status, date range, job id) at the DAO layer using SQL `LIMIT/OFFSET` and dynamic `WHERE` clause building. |
| FR-17 | The system shall support graceful shutdown: on SIGINT, stop accepting new scheduled triggers, wait (bounded) for in-flight jobs to finish, then persist final state and exit. |
| FR-18 | The system shall maintain an in-memory LRU cache of recently computed workflow topological orders (`Map` + `LinkedHashMap`-based LRU) to avoid recomputation, invalidated on workflow redefinition. |
| FR-19 | The system shall support serialization of `WorkflowDefinition` objects to disk (Java Serialization *and* a hand-written JSON writer using Streams — no third-party JSON library — to demonstrate both) as a checkpoint/backup mechanism. |
| FR-20 | The system shall rank/recommend the "next best job to run" when multiple ready jobs compete for limited worker slots, using a pluggable `JobPriorityStrategy` (e.g., shortest-estimated-duration-first, or dependency-fan-out-first) — this is the project's "recommendation/ranking" feature. |

## 3.2 Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-01 | The system shall be thread-safe: no data race on shared state (job registry, run history cache, event bus). Documented and enforced with `java.util.concurrent` primitives, not raw `synchronized` sprinkled ad hoc. |
| NFR-02 | The system shall log at appropriate levels (TRACE/DEBUG/INFO/WARN/ERROR) via SLF4J + Logback, with rolling file logs and console output, configurable per-environment. |
| NFR-03 | The system shall achieve ≥80% unit test line coverage on the `core` and `scheduler` packages, measured with JaCoCo. |
| NFR-04 | The system shall start up in under 2 seconds on a typical developer laptop with the default (SQLite/H2 or PostgreSQL) configuration. |
| NFR-05 | The system's public APIs (interfaces in `com.taskflow.api`) shall be documented with complete JavaDoc, including `@param`, `@return`, `@throws`. |
| NFR-06 | The system shall be extensible: adding a new job type, retry policy, priority strategy, or event listener shall require **zero modification** to existing classes (Open/Closed Principle), only new classes plus registration. |
| NFR-07 | The system's configuration shall never hard-code credentials; DB credentials are read from environment variables with `.properties` fallback for local dev only, and a documented `.env.example`. |

## 3.3 Out-of-the-box User Story Set (for the Agent to validate design against)

1. As an operator, I can register a 3-job workflow (`extract → transform → load`) where `transform` depends on `extract`, and `load` depends on `transform`, and trigger it manually.
2. As an operator, I can schedule a workflow to run every day at 2:00 AM using a cron expression `0 2 * * *`.
3. As an operator, if `transform` fails twice, it should retry with exponential backoff before finally marking the workflow run as `FAILED`, without re-running the already-succeeded `extract` job.
4. As an operator, I can view the last 20 runs of a given job, paginated, filtered by status `FAILED`.
5. As an operator, I can see aggregate stats: success rate and average duration per job over the last 7 days.
6. As a developer extending the system, I can add a new `SlackNotificationListener` (Observer) without touching `SchedulerEngine` internals.
7. As an operator, I can gracefully stop the engine and be confident no job is left in a corrupted "STARTED-forever" state.


---

# PART 4 — JAVA & SOFTWARE ENGINEERING CONCEPT COVERAGE MAP

This table is the traceability matrix the agent must satisfy. Every mandatory topic from the brief is mapped to a **concrete place in TaskFlow** where it is used — not bolted on artificially.

## 4.1 Core Java / OOP

| Concept | Where Used in TaskFlow |
|---|---|
| Classes & Objects | Every domain entity: `Job`, `JobRun`, `Workflow`, `JobNode`, `ScheduleSpec`. |
| Abstraction | `Job` interface; `AbstractRetryPolicy` abstract base class. |
| Encapsulation | All domain classes expose immutable state via constructors + getters; mutation only through controlled methods (e.g., `JobRun.markSucceeded()`). |
| Inheritance | `AbstractRetryPolicy` → `FixedDelayRetryPolicy`, `ExponentialBackoffRetryPolicy`, `NoRetryPolicy`. `AbstractEventListener` → `ConsoleEventListener`, `FileEventListener`, `MetricsEventListener`. |
| Polymorphism | `SchedulerEngine` invokes `RetryPolicy.nextDelay(attempt)` and gets different behavior per concrete subclass at runtime. |
| Interfaces | `Job`, `RetryPolicy`, `JobPriorityStrategy`, `EventListener`, `JobRepository`, `WorkflowRepository`, `ConfigService`. |
| Abstract Classes | `AbstractRetryPolicy`, `AbstractEventListener` (template method for common event filtering logic). |
| Method Overloading | `ReportService.generateReport(Duration window)` / `generateReport(Instant from, Instant to)` / `generateReport(String jobId, Duration window)`. |
| Method Overriding | Every concrete `RetryPolicy`/`EventListener`/`JobPriorityStrategy` overrides its interface/abstract methods. |
| Constructor Chaining | `JobRun` builder-backed constructors chain to a canonical all-args private constructor; `ExponentialBackoffRetryPolicy` has a convenience constructor chaining to the full constructor with sane defaults. |
| Access Modifiers | Strict use of `private` fields, `package-private` DAO helper classes, `public` interfaces only in `api` package, `protected` template methods in abstract base classes. |
| Static | `CronExpressionParser` static parsing utility methods; `Constants` class; static factory methods `RetryPolicies.fixedDelay(...)`. |
| Final | Immutable domain objects (`final` fields), `final` classes for value objects (`JobId`, `WorkflowId` as `final class` wrapper types), constants `static final`. |
| Nested Classes | `Workflow.Builder` static nested class (Builder pattern). |
| Inner Classes | `SchedulerEngine.TriggerTask` non-static inner class needing outer instance's worker pool reference. |
| Enums | `JobStatus` (SCHEDULED, RUNNING, SUCCEEDED, FAILED, RETRYING, TIMED_OUT, CANCELLED, SKIPPED), `ScheduleType` (ONE_TIME, FIXED_INTERVAL, CRON), `OverlapPolicy` (SKIP, QUEUE, RUN_CONCURRENTLY). Enums carry behavior (e.g., `JobStatus.isTerminal()`). |
| Packages | `com.taskflow.api`, `.core`, `.scheduler`, `.persistence`, `.concurrency`, `.events`, `.cli`, `.http`, `.config`, `.report`, `.util`, `.exception`. |
| Generics | `Repository<T, ID>` generic base interface; `LruCache<K, V>`; `Result<T>`/`Optional<T>` wrapping job outputs. |
| Collections Framework | `ArrayList`, `HashMap`, `HashSet`, `TreeMap` (sorted run history by timestamp), `TreeSet` (sorted ready-job set by priority), `ArrayDeque` as a `Deque`/`Stack`/`Queue`, `PriorityQueue` (scheduling next-fire-time queue). |
| Comparable | `ScheduledTrigger implements Comparable<ScheduledTrigger>` (ordered by next fire time) for the `PriorityQueue`. |
| Comparator | `Comparator.comparing(...).thenComparing(...)` chains for sorting job runs by status then timestamp; custom `Comparator` for `JobPriorityStrategy`. |
| Streams API | `ReportService` aggregation pipelines (`filter`, `map`, `collect(Collectors.groupingBy(...))`, `summaryStatistics()`). |
| Lambda Expressions | Event listener registration, retry-policy factory lambdas, comparator lambdas. |
| Functional Interfaces | Custom `@FunctionalInterface Job`, use of `Supplier`, `Function`, `Predicate`, `BiFunction` in `ConfigService` and `ReportService`. |
| Optional | `JobRepository.findById(...)` returns `Optional<JobRun>`; `ConfigService.getOptional(key)`. |
| Exception Handling | try/catch/finally around job execution; try-with-resources for JDBC `Connection`/`PreparedStatement`/`ResultSet`. |
| Custom Exceptions | `CyclicWorkflowException`, `JobExecutionException`, `JobTimeoutException`, `InvalidCronExpressionException`, `SchedulerShutdownException`, `PersistenceException`. |
| Serialization | `WorkflowDefinition implements Serializable` with checkpoint save/restore; custom hand-written JSON serializer using Streams for the HTTP status endpoint. |
| File Handling / NIO | `java.nio.file.Files`/`Path` for reading job-definition config files and writing reports/checkpoints; `WatchService` (optional stretch) to hot-reload config. |
| Reflection (advanced/optional) | `JobFactory` uses reflection to instantiate `Job` implementations by fully-qualified class name from a config file (plugin-style loading), guarded with clear validation and exception handling. |
| Annotations | Custom `@Retryable` marker annotation (metadata only, read via reflection) plus standard `@Override`, `@FunctionalInterface`, `@Deprecated`, JUnit `@Test`/`@BeforeEach`. |
| JDBC | Hand-written `JdbcJobRunRepository`, `JdbcWorkflowRepository` using `DriverManager`/`DataSource`, `PreparedStatement`, transactions (`Connection.setAutoCommit(false)` + commit/rollback), connection pooling via HikariCP (allowed 3rd-party infra library) or a minimal hand-rolled pool for extra credit. |
| Multithreading | `SchedulerEngine` runs on a dedicated scheduler thread (`ScheduledExecutorService`) that dispatches ready jobs to a separate bounded worker `ExecutorService`. |
| ExecutorService | Configurable fixed/cached thread pool for job execution; `ScheduledExecutorService` for the ticking scheduler loop. |
| Synchronization | Per-job `ReentrantLock` to prevent overlapping runs (`OverlapPolicy.SKIP`/`QUEUE`); `synchronized` used sparingly and explained where chosen over locks. |
| Locks | `java.util.concurrent.locks.ReentrantLock` + `Condition` for `OverlapPolicy.QUEUE` semantics; `ReadWriteLock` for the workflow registry (many readers, rare writer on redefinition). |
| Concurrent Collections | `ConcurrentHashMap` for job registry and run-history cache; `CopyOnWriteArrayList` for event listener list; `LinkedBlockingQueue` for the internal event bus buffer. |
| Java Time API | `Instant`, `Duration`, `LocalDateTime`, `ZoneId`, `ZonedDateTime` throughout scheduling, cron next-fire-time computation, and reporting windows. |
| Regex | Hand-written cron expression validation/parsing (`Pattern`/`Matcher`) supporting `*`, ranges (`1-5`), steps (`*/2`), lists (`1,3,5`). |
| Logging | SLF4J API + Logback implementation; structured log messages with MDC (job id, run id) for traceability. |
| Configuration Files | `application.properties` (default), `application-dev.properties`, `application-prod.properties`, loaded via a layered `ConfigService`. |
| Resource Bundles | `messages.properties` / `messages_en.properties` for user-facing CLI/report message templates (also demonstrates i18n awareness). |

## 4.2 Software Engineering Principles & Patterns

| Concept | Where Used |
|---|---|
| SOLID — SRP | Each class has one reason to change: `SchedulerEngine` schedules, `JobExecutor` executes, `RetryPolicy` decides retries, `EventBus` dispatches events. |
| SOLID — OCP | New `RetryPolicy`/`EventListener`/`JobPriorityStrategy` types added without editing existing code. |
| SOLID — LSP | Any `RetryPolicy` subtype is substitutable wherever `RetryPolicy` is expected; tested explicitly. |
| SOLID — ISP | Narrow interfaces: `JobRepository` vs `WorkflowRepository` rather than one fat `Repository`. |
| SOLID — DIP | `SchedulerEngine` depends on `JobRepository` interface, not `JdbcJobRunRepository` concretely; wired via manual constructor injection in `Bootstrap`/`AppContext`. |
| Clean Code | Small methods, intention-revealing names, no magic numbers (`Constants`), guard clauses over nested ifs. |
| MVC-ish Layering | Not literal MVC (no UI), but an equivalent **Controller (CLI/HTTP) → Service → Repository** layering. |
| Layered Architecture | `api` (contracts) → `core`/`scheduler` (domain + orchestration) → `persistence` (DAO) → `cli`/`http` (delivery). |
| Repository Pattern | `JobRunRepository`, `WorkflowRepository` interfaces + JDBC implementations. |
| Service Layer | `SchedulingService`, `ReportService`, `WorkflowService` orchestrate use cases, keeping controllers thin. |
| DTO Pattern | `WorkflowStatusDto`, `JobRunSummaryDto` returned from the HTTP layer, decoupled from persistence entities. |
| Factory Pattern | `RetryPolicyFactory`, `JobFactory` (reflection-based instantiation from config). |
| Builder Pattern | `Workflow.Builder`, `JobRun.Builder` for constructing complex immutable objects step by step. |
| Strategy Pattern | `RetryPolicy`, `JobPriorityStrategy`, `OverlapPolicy` handling. |
| Observer Pattern | `EventBus` + `EventListener` subscribers. |
| Singleton | `AppContext` (the manually-wired DI container) — implemented thread-safely, and explicitly discussed as a pattern to use *sparingly* (the agent must document the trade-off, not cargo-cult it). |
| Manual Dependency Injection | Constructor injection wired in a single `Bootstrap` class; no Spring. |
| Validation | `ScheduleSpecValidator`, cron validator, workflow-cycle validator. |
| Error Handling | Centralized exception hierarchy + consistent translation to CLI/HTTP responses. |
| Configuration Management | Layered `ConfigService` (defaults < file < env var). |
| Versioning | Semantic Versioning of the project itself (`v0.1.0` → `v1.0.0` per phase) tracked in `CHANGELOG.md`. |
| Documentation | JavaDoc + `README.md` + `ARCHITECTURE.md` + `docs/` folder (Part 17). |
| Reusable Components | `LruCache<K,V>`, `Repository<T,ID>` generics reusable beyond this project. |


---

# PART 5 — DATA STRUCTURES & ALGORITHMS USAGE MAP

## 5.1 Data Structures

| Data Structure | Used For | File/Class |
|---|---|---|
| `HashMap` / `ConcurrentHashMap` | Job registry (`jobId → Job`), workflow registry (`workflowId → Workflow`) | `JobRegistry`, `WorkflowRegistry` |
| `TreeMap` | Keeping job-run history sorted by start `Instant` for range queries without re-sorting | `InMemoryRunHistoryIndex` |
| `HashSet` | Tracking visited nodes during DAG cycle detection; tracking distinct job ids in a run batch | `DagValidator` |
| `TreeSet` | Maintaining the "ready to run" job set ordered by `JobPriorityStrategy`-derived priority | `SchedulerEngine` |
| `LinkedList` (as `Deque`) | DFS stack for cycle detection (explicit stack, not recursion, to avoid stack overflow on large DAGs) | `DagValidator` |
| `ArrayList` | General ordered collections: job list within a `Workflow`, ordered CLI output | Throughout |
| `PriorityQueue` | The scheduler's "next fire time" min-heap of `ScheduledTrigger` objects, ordered by `Comparable` | `SchedulerEngine` |
| `Deque` (`ArrayDeque`) | BFS/DFS traversal of the DAG for topological sort (Kahn's algorithm uses a queue of zero-indegree nodes) | `TopologicalSorter` |
| `Stack` (via `Deque`) | Iterative DFS for cycle detection | `DagValidator` |
| `Queue` (`LinkedBlockingQueue`) | Internal event bus buffer between producer (job execution threads) and consumer (listener dispatch thread) | `EventBus` |
| Graph (adjacency list, custom) | `Workflow` internally models a DAG: `Map<JobId, List<JobId>>` adjacency + reverse adjacency for dependents | `WorkflowGraph` |
| Custom LRU Cache (`LinkedHashMap` w/ `removeEldestEntry`, or hand-rolled `HashMap` + doubly linked list) | Caching computed topological orders per workflow version | `LruCache<K,V>` |

## 5.2 Algorithms

| Algorithm | Purpose | Where |
|---|---|---|
| **Kahn's Algorithm (Topological Sort via BFS + indegree counting)** | Compute a valid execution order for a workflow DAG | `TopologicalSorter` |
| **Iterative DFS Cycle Detection (3-color: white/gray/black)** | Reject cyclic workflows at registration time | `DagValidator` |
| **Min-heap scheduling** | Efficiently find the next trigger to fire among many scheduled workflows (`O(log n)` poll/insert) | `SchedulerEngine` |
| **Cron next-fire-time computation** | Given a cron expression and "now", compute the next matching `ZonedDateTime` by field-wise candidate search | `CronExpressionParser` / `CronScheduleCalculator` |
| **Exponential backoff calculation** | `delay = min(maxDelay, initialDelay * multiplier^(attempt-1))`, optionally with jitter | `ExponentialBackoffRetryPolicy` |
| **LRU eviction** | Evict least-recently-used cached topological order when cache exceeds capacity | `LruCache` |
| **Ranking / priority scheduling** | When more ready jobs exist than free worker threads, rank ready jobs by strategy (e.g., shortest-estimated-duration-first — a greedy scheduling heuristic akin to SJF; or fan-out-first, prioritizing jobs that unblock the most dependents — akin to critical-path prioritization) | `JobPriorityStrategy` implementations |
| **Filtering & Pagination** | SQL-level filtering (status, job id, date range) + `LIMIT`/`OFFSET` for `history` CLI command and HTTP endpoint | `JdbcJobRunRepository.findRuns(RunQuery, Page)` |
| **Statistics (streams-based)** | Success rate, mean/percentile duration computation via `DoubleSummaryStatistics` and sorted-list percentile lookup | `ReportService` |
| **Binary search (percentile lookup)** | Compute p95 duration from a sorted list of durations | `ReportService.percentile(...)` |
| **String matching (regex)** | Cron field validation | `CronExpressionParser` |


---

# PART 6 — SYSTEM ARCHITECTURE

## 6.1 High-Level Component Diagram

```
                          +------------------------------+
                          |           CLI / HTTP          |
                          |  (TaskFlowCli, StatusHttpApi) |
                          +---------------+----------------+
                                          |
                                          v
                          +------------------------------+
                          |         SERVICE LAYER         |
                          |  WorkflowService, SchedulingService, ReportService |
                          +---------------+----------------+
                                          |
              +---------------------------+---------------------------+
              v                                                       v
+----------------------------+                          +----------------------------+
|      SCHEDULER CORE        |                          |      PERSISTENCE LAYER      |
| SchedulerEngine            |<------------------------>| JobRunRepository (JDBC)     |
| TopologicalSorter          |                          | WorkflowRepository (JDBC)   |
| DagValidator               |                          | ConnectionManager            |
| JobExecutor (worker pool)  |                          +----------------------------+
| RetryPolicy strategies     |
| JobPriorityStrategy        |                          +----------------------------+
+--------------+--------------+                          |         EVENT BUS           |
               |                                          | EventBus, EventListener(s)   |
               +----------------------------------------->| Console/File/Metrics Listener|
                                                            +----------------------------+
                          +------------------------------+
                          |     CROSS-CUTTING CONCERNS    |
                          | ConfigService, Logging (SLF4J),|
                          | Exceptions, LruCache, Util     |
                          +------------------------------+
```

## 6.2 Runtime Sequence — "Trigger a Workflow Manually"

```
Operator -> CLI: trigger workflow "etl-daily"
CLI -> WorkflowService: triggerNow("etl-daily")
WorkflowService -> WorkflowRegistry: get("etl-daily")
WorkflowService -> DagValidator: validate(workflow)      [cycle check, already done at registration; re-checked defensively]
WorkflowService -> TopologicalSorter: sort(workflow)      [LRU-cached]
WorkflowService -> SchedulerEngine: submitRun(workflow, order)
loop for each "ready" job (indegree 0, or dependencies just completed)
    SchedulerEngine -> JobPriorityStrategy: rank(readyJobs)
    SchedulerEngine -> JobExecutor: submit(job)
    JobExecutor -> WorkerThreadPool: execute
    WorkerThreadPool -> Job: execute(context)
    Job --> WorkerThreadPool: JobResult / Exception
    WorkerThreadPool -> EventBus: publish(JobStatusEvent)
    EventBus -> Listeners: onEvent(event)                 [async, non-blocking to worker]
    WorkerThreadPool -> JobRunRepository: save(jobRun)      [JDBC, transactional]
    alt job failed and retries remain
        SchedulerEngine -> RetryPolicy: nextDelay(attempt)
        SchedulerEngine -> ScheduledExecutorService: schedule(retry, delay)
    else job succeeded
        SchedulerEngine -> WorkflowGraph: markComplete(job) -> unlocks dependents
    end
end
SchedulerEngine -> WorkflowService: WorkflowRunResult
WorkflowService -> CLI: print summary
```

## 6.3 Threading Model (Critical Design Decision — Agent Must Implement Exactly This)

- **1 scheduler thread** (`ScheduledExecutorService` with a single thread) — the *only* thread allowed to mutate the `PriorityQueue` of triggers and decide "what fires next." This avoids needing to lock the priority queue across many threads.
- **N worker threads** (configurable bounded `ThreadPoolExecutor`, default = `Runtime.getRuntime().availableProcessors()`), which *execute* job logic. Workers never touch the trigger priority queue directly; they communicate results back via a thread-safe `ConcurrentLinkedQueue` of completion events that the scheduler thread drains each tick, or via direct `EventBus` publication (bus itself is thread-safe).
- **1 event-dispatch thread** draining the `LinkedBlockingQueue` inside `EventBus` and calling listeners — this ensures slow listeners (e.g., a listener that logs to a slow file) never block job-executing worker threads.
- **Per-job `ReentrantLock`** stored in a `ConcurrentHashMap<JobId, ReentrantLock>` guards overlap prevention without a single global lock bottlenecking unrelated jobs.
- **`ReadWriteLock`** on `WorkflowRegistry`: workflow reads (the common case, every tick) use the read lock (many concurrent readers allowed); workflow registration/redefinition (rare) takes the write lock.

This model is explicitly documented in `ARCHITECTURE.md` and must be explained in the resume/interview pitch — it is a strong signal of real concurrency understanding (separating scheduling decisions from execution from event dispatch is a classic production pattern).

## 6.4 Failure & Fault-Tolerance Design

- Jobs are idempotent by contract (documented requirement on the `Job` interface JavaDoc) — TaskFlow does not guarantee exactly-once execution (this is explicitly stated as a known, documented trade-off, mirroring real distributed-systems humility expected at interviews).
- On JVM crash mid-run, any `JobRun` left in `RUNNING` state at startup is reconciled: `ReconciliationService` (run once at boot) marks stale `RUNNING` rows older than a grace period as `UNKNOWN` and logs a warning — demonstrating awareness of crash-recovery/state-reconciliation, a strong interview topic.
- Graceful shutdown: `Runtime.getRuntime().addShutdownHook(...)` triggers `SchedulerEngine.shutdown()`, which stops accepting new triggers, calls `ExecutorService.shutdown()` + `awaitTermination(timeout)`, and force-cancels (`shutdownNow()`) if the timeout elapses, logging any jobs that were forcibly interrupted.

---

# PART 7 — DATABASE DESIGN

## 7.1 Database Choice: SQLite vs MySQL vs PostgreSQL

| Criterion | SQLite | MySQL | PostgreSQL |
|---|---|---|---|
| Setup friction for a resume/portfolio project | None (file-based) | Medium (server + user setup) | Medium (server + user setup) |
| Concurrency support (multiple writer threads) | Weak (single-writer, file locks) | Good | **Best** (MVCC, fine-grained locking) |
| JDBC feature completeness for demonstrating transactions/isolation levels | Basic | Good | **Best** |
| Data types (JSON, arrays, advanced indexing) | Limited | Good | **Best** — supports `JSONB`, partial indexes, `SERIAL`/`IDENTITY` |
| Industry relevance / what companies actually run in production | Low | High | **Very high** (default choice in most modern backend stacks) |
| Resume signal | Weak ("just used SQLite") | Decent | **Strong** ("designed and normalized a PostgreSQL schema with indexes and transactions") |

**DECISION: Use PostgreSQL as the primary/production target**, with an **H2 in-memory database** as the test-time and "zero-setup local demo" fallback (configurable via `ConfigService` / active profile), so the AI Coding Agent must show it understands writing portable, standard SQL (avoiding vendor-specific syntax where avoidable) and configuring a `DataSource` per environment. This also naturally demonstrates the "Configuration Management" and "Environment profiles" requirements. SQLite is explicitly rejected due to weak write-concurrency, which would undercut the concurrency story that is central to this project.

## 7.2 Schema (Normalized to 3NF)

```sql
-- Table: workflows
CREATE TABLE workflows (
    workflow_id     VARCHAR(64)  PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    schedule_type   VARCHAR(20)  NOT NULL,   -- ONE_TIME | FIXED_INTERVAL | CRON
    schedule_spec   VARCHAR(255) NOT NULL,   -- cron expr / ISO-8601 duration / instant
    overlap_policy  VARCHAR(20)  NOT NULL DEFAULT 'SKIP',
    is_paused       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Table: jobs (a job definition belongs to exactly one workflow)
CREATE TABLE jobs (
    job_id          VARCHAR(64)  PRIMARY KEY,
    workflow_id     VARCHAR(64)  NOT NULL REFERENCES workflows(workflow_id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    job_class       VARCHAR(500) NOT NULL,   -- fully-qualified class name, for reflective instantiation
    retry_policy    VARCHAR(30)  NOT NULL DEFAULT 'NONE',
    max_attempts    INT          NOT NULL DEFAULT 1,
    timeout_seconds INT          NOT NULL DEFAULT 300,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Table: job_dependencies (edges of the DAG; many-to-many self-relationship on jobs)
CREATE TABLE job_dependencies (
    job_id           VARCHAR(64) NOT NULL REFERENCES jobs(job_id) ON DELETE CASCADE,
    depends_on_job_id VARCHAR(64) NOT NULL REFERENCES jobs(job_id) ON DELETE CASCADE,
    PRIMARY KEY (job_id, depends_on_job_id),
    CHECK (job_id <> depends_on_job_id)
);

-- Table: job_runs (execution history — the largest, most-queried table)
CREATE TABLE job_runs (
    run_id          BIGSERIAL    PRIMARY KEY,
    job_id          VARCHAR(64)  NOT NULL REFERENCES jobs(job_id) ON DELETE CASCADE,
    workflow_run_id BIGINT       NOT NULL REFERENCES workflow_runs(workflow_run_id) ON DELETE CASCADE,
    attempt_number  INT          NOT NULL DEFAULT 1,
    status          VARCHAR(20)  NOT NULL,  -- SCHEDULED|RUNNING|SUCCEEDED|FAILED|RETRYING|TIMED_OUT|CANCELLED|SKIPPED|UNKNOWN
    started_at      TIMESTAMPTZ,
    finished_at     TIMESTAMPTZ,
    duration_ms     BIGINT,
    error_message   TEXT,
    output_summary  TEXT
);

-- Table: workflow_runs (one row per triggered execution of a whole workflow)
CREATE TABLE workflow_runs (
    workflow_run_id BIGSERIAL    PRIMARY KEY,
    workflow_id     VARCHAR(64)  NOT NULL REFERENCES workflows(workflow_id) ON DELETE CASCADE,
    trigger_type    VARCHAR(20)  NOT NULL,  -- MANUAL | SCHEDULED
    status          VARCHAR(20)  NOT NULL,
    started_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    finished_at     TIMESTAMPTZ
);

-- Indexes
CREATE INDEX idx_job_runs_job_id_started_at ON job_runs (job_id, started_at DESC);
CREATE INDEX idx_job_runs_status ON job_runs (status);
CREATE INDEX idx_workflow_runs_workflow_id ON workflow_runs (workflow_id, started_at DESC);
CREATE INDEX idx_jobs_workflow_id ON jobs (workflow_id);
```

> Note: `job_runs` references `workflow_runs`, which is defined after it — in the actual migration script (Phase 2, Flyway-style numbered SQL files: `V1__init_schema.sql`), declare `workflow_runs` first, then `job_runs`, or add the FK via `ALTER TABLE` after both exist. The agent must fix ordering when writing the real migration file.

## 7.3 Normalization Notes

- 1NF: all columns atomic (no comma-separated dependency lists — dependencies are modeled as proper rows in `job_dependencies`).
- 2NF: no partial dependency on a composite key (only `job_dependencies` has a composite PK, and both columns are needed to identify the edge).
- 3NF: no transitive dependencies — e.g., workflow `name`/`description` live only on `workflows`, not duplicated onto `jobs`.

## 7.4 Transactions

- Registering a full workflow (workflow row + N job rows + M dependency edges) is wrapped in a single JDBC transaction (`setAutoCommit(false)`); on any failure, full rollback — demonstrating atomicity of multi-statement operations.
- Recording a job run uses a transaction spanning the `job_runs` insert/update and, when the job is the last one in a workflow run, the `workflow_runs` status update — both committed together or rolled back together.

## 7.5 Connection Management

- `ConnectionManager` wraps a `DataSource` (HikariCP recommended as the one pragmatic 3rd-party infra dependency — explicitly justify in `ARCHITECTURE.md` why a hand-rolled pool is not worth reinventing for this layer, while everything else *is* hand-rolled to demonstrate raw Java skill). Alternative: implement a minimal custom `ConnectionPool` class (bonus, Phase 6) using a `BlockingQueue<Connection>` to additionally demonstrate producer/consumer pooling — **recommended as a stretch goal for extra "Java skill" resume value**.


---

# PART 8 — FOLDER & PACKAGE STRUCTURE

```
taskflow/
├── pom.xml                              (Maven; Java 17 LTS target)
├── README.md
├── ARCHITECTURE.md
├── CHANGELOG.md
├── DECISIONS.md                          (agent logs any assumption/decision here)
├── .env.example
├── .gitignore
├── docs/
│   ├── er-diagram.png / .md (mermaid)
│   ├── sequence-diagrams.md
│   ├── api.md                            (CLI + HTTP "API" reference)
│   └── setup.md
├── src/
│   ├── main/
│   │   ├── java/com/taskflow/
│   │   │   ├── api/                      (public contracts only)
│   │   │   │   ├── Job.java
│   │   │   │   ├── JobContext.java
│   │   │   │   ├── JobResult.java
│   │   │   │   ├── RetryPolicy.java
│   │   │   │   ├── JobPriorityStrategy.java
│   │   │   │   └── EventListener.java
│   │   │   ├── core/                     (domain model)
│   │   │   │   ├── JobId.java / WorkflowId.java (value types)
│   │   │   │   ├── JobDefinition.java
│   │   │   │   ├── Workflow.java (+ Workflow.Builder)
│   │   │   │   ├── WorkflowGraph.java
│   │   │   │   ├── JobRun.java (+ JobRun.Builder)
│   │   │   │   ├── WorkflowRun.java
│   │   │   │   ├── JobStatus.java (enum)
│   │   │   │   ├── ScheduleType.java (enum)
│   │   │   │   └── OverlapPolicy.java (enum)
│   │   │   ├── scheduler/
│   │   │   │   ├── SchedulerEngine.java
│   │   │   │   ├── ScheduledTrigger.java
│   │   │   │   ├── TopologicalSorter.java
│   │   │   │   ├── DagValidator.java
│   │   │   │   ├── JobExecutor.java
│   │   │   │   ├── CronExpressionParser.java
│   │   │   │   ├── CronScheduleCalculator.java
│   │   │   │   ├── retry/
│   │   │   │   │   ├── AbstractRetryPolicy.java
│   │   │   │   │   ├── NoRetryPolicy.java
│   │   │   │   │   ├── FixedDelayRetryPolicy.java
│   │   │   │   │   ├── ExponentialBackoffRetryPolicy.java
│   │   │   │   │   └── RetryPolicyFactory.java
│   │   │   │   └── priority/
│   │   │   │       ├── ShortestJobFirstStrategy.java
│   │   │   │       └── FanOutFirstStrategy.java
│   │   │   ├── concurrency/
│   │   │   │   ├── JobLockRegistry.java
│   │   │   │   ├── LruCache.java
│   │   │   │   └── NamedThreadFactory.java
│   │   │   ├── events/
│   │   │   │   ├── EventBus.java
│   │   │   │   ├── JobStatusEvent.java
│   │   │   │   ├── AbstractEventListener.java
│   │   │   │   ├── ConsoleEventListener.java
│   │   │   │   ├── FileEventListener.java
│   │   │   │   └── MetricsEventListener.java
│   │   │   ├── persistence/
│   │   │   │   ├── ConnectionManager.java
│   │   │   │   ├── Repository.java  (generic base interface)
│   │   │   │   ├── JobRunRepository.java (+ Jdbc impl)
│   │   │   │   ├── WorkflowRepository.java (+ Jdbc impl)
│   │   │   │   ├── RunQuery.java / Page.java / PageResult.java
│   │   │   │   └── migration/V1__init_schema.sql
│   │   │   ├── service/
│   │   │   │   ├── WorkflowService.java
│   │   │   │   ├── SchedulingService.java
│   │   │   │   ├── ReportService.java
│   │   │   │   └── ReconciliationService.java
│   │   │   ├── dto/
│   │   │   │   ├── WorkflowStatusDto.java
│   │   │   │   └── JobRunSummaryDto.java
│   │   │   ├── cli/
│   │   │   │   ├── TaskFlowCli.java
│   │   │   │   └── CommandParser.java
│   │   │   ├── http/
│   │   │   │   ├── StatusHttpApi.java
│   │   │   │   └── JsonWriter.java (hand-written, stream-based)
│   │   │   ├── config/
│   │   │   │   ├── ConfigService.java
│   │   │   │   └── AppContext.java (manual DI wiring / Bootstrap)
│   │   │   ├── exception/
│   │   │   │   ├── TaskFlowException.java (base, unchecked)
│   │   │   │   ├── CyclicWorkflowException.java
│   │   │   │   ├── JobExecutionException.java
│   │   │   │   ├── JobTimeoutException.java
│   │   │   │   ├── InvalidCronExpressionException.java
│   │   │   │   └── PersistenceException.java
│   │   │   ├── util/
│   │   │   │   ├── Constants.java
│   │   │   │   └── Preconditions.java
│   │   │   └── Main.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── logback.xml
│   │       └── messages.properties
│   └── test/
│       └── java/com/taskflow/
│           ├── core/ (WorkflowTest, JobRunTest, ...)
│           ├── scheduler/ (TopologicalSorterTest, DagValidatorTest, CronExpressionParserTest,
│           │              retry/ExponentialBackoffRetryPolicyTest, SchedulerEngineIntegrationTest)
│           ├── concurrency/ (LruCacheTest, JobLockRegistryConcurrencyTest)
│           ├── persistence/ (JdbcJobRunRepositoryTest — using H2)
│           ├── service/ (ReportServiceTest)
│           └── testsupport/ (fakes/fixtures, e.g. InMemoryJobRunRepository for fast unit tests)
```

**Package Dependency Rule (enforced conceptually, and via module-info.java if using Java Modules as a stretch goal):** `api` depends on nothing else in the project. `core` depends only on `api`. `scheduler`/`concurrency`/`events` depend on `core`+`api`. `persistence` depends on `core`+`api`. `service` depends on `scheduler`+`persistence`+`events`+`core`. `cli`/`http` depend on `service` only — never reach into `persistence` or `scheduler` directly. This is the Layered Architecture / Dependency Inversion rule the agent must not violate.


---

# PART 9 — IMPLEMENTATION ROADMAP (PHASED PLAN)

**Total estimated duration: 4–6 weeks** at a realistic student pace (10–15 focused hours/week). The AI Coding Agent must complete phases **in order**, and after each phase: (1) run all tests, (2) update `README.md` "Progress" section, (3) update `CHANGELOG.md`, (4) commit with a conventional-commit message (see Part 14), (5) tag the repo (`v0.1.0`, `v0.2.0`, ... `v1.0.0`).

## PHASE 0 — Project Bootstrap (Est. 0.5–1 day)

**Objectives:** Initialize a buildable, empty-but-correct skeleton.

- Files: `pom.xml` (Java 17, JUnit 5, Mockito, SLF4J+Logback, HikariCP, H2 [test scope], PostgreSQL JDBC driver), `.gitignore`, `README.md` (stub), `LICENSE` (MIT).
- Classes: `Main.java` (prints a banner + version).
- Database: none yet.
- Features: `mvn clean install` succeeds; `mvn -q exec:java` prints banner.
- Testing: one smoke test (`MainSmokeTest`) asserting the app starts without throwing.
- Expected Output: green build, first commit `chore: bootstrap project skeleton`.
- Dependencies: none.
- Time Estimate: 2–4 hours.

## PHASE 1 — Domain Model & Core Enums (Est. 2–3 days)

**Objectives:** Build the pure, dependency-free domain model — no I/O, no threads yet.

- Files/Classes: `api/Job.java`, `api/JobContext.java`, `api/JobResult.java`; `core/JobId.java`, `core/WorkflowId.java`, `core/JobDefinition.java`, `core/JobStatus.java` (enum with `isTerminal()`), `core/ScheduleType.java`, `core/OverlapPolicy.java`, `core/Workflow.java` + `Workflow.Builder` (nested static class), `core/WorkflowGraph.java`, `core/JobRun.java` + `JobRun.Builder`, `core/WorkflowRun.java`.
- Interfaces: `Job` (functional interface: `JobResult execute(JobContext ctx) throws JobExecutionException`).
- Database: none yet (in-memory only).
- Features: Construct a `Workflow` via `Builder`, add jobs with `dependsOn(...)`, retrieve adjacency lists from `WorkflowGraph`.
- Testing: `WorkflowBuilderTest`, `JobRunTest` (state transition rules — e.g., cannot go from `SUCCEEDED` back to `RUNNING`), `JobStatusTest` (enum behavior).
- Expected Output: 100% of domain classes immutable where appropriate, fully JavaDoc'd, ≥90% test coverage on this package (small, easy to fully cover).
- Dependencies: JUnit 5 only.
- Time Estimate: 2–3 days.

## PHASE 2 — DAG Algorithms: Validation & Topological Sort (Est. 2–3 days)

**Objectives:** Implement and rigorously test the graph algorithms that are the technical heart of the "interview value" of this project.

- Files/Classes: `scheduler/DagValidator.java` (iterative 3-color DFS cycle detection using an explicit `Deque` as a stack — no recursion, to demonstrate awareness of stack-overflow risk on deep graphs), `scheduler/TopologicalSorter.java` (Kahn's algorithm using `ArrayDeque` as a queue + `HashMap` indegree count), `exception/CyclicWorkflowException.java`.
- Data Structures used: adjacency `HashMap<JobId, List<JobId>>`, indegree `HashMap<JobId, Integer>`, `ArrayDeque<JobId>` as BFS queue, `HashSet<JobId>` visited/in-progress sets.
- Features: `DagValidator.validate(workflow)` throws `CyclicWorkflowException` with a message naming the exact cycle path found; `TopologicalSorter.sort(workflow)` returns a `List<List<JobId>>` of "levels" (jobs within a level have no dependency on each other and can run concurrently).
- Testing: table-driven tests covering: linear chain, diamond dependency (A→B, A→C, B→D, C→D), self-loop rejection, disjoint components, a 2-cycle, a larger random DAG (property-based sanity check: every job appears exactly once in the sort output, and for every edge u→v, level(u) < level(v)).
- Expected Output: Both algorithms are `O(V+E)`; this complexity claim must be verifiable and stated in JavaDoc.
- Dependencies: Phase 1 domain classes.
- Time Estimate: 2–3 days (this phase is intellectually the hardest — budget extra buffer).

## PHASE 3 — Cron Parsing & Scheduling Primitives (Est. 2 days)

**Objectives:** Implement schedule-type computation without external cron libraries.

- Files/Classes: `scheduler/CronExpressionParser.java` (regex-based field parser supporting `*`, `a-b`, `*/n`, `a,b,c` for the 5 standard fields: minute, hour, day-of-month, month, day-of-week), `scheduler/CronScheduleCalculator.java` (given a parsed cron + a `ZonedDateTime "now"`, compute the next matching `ZonedDateTime` by incrementing candidate minutes and checking field membership — bounded search, e.g., max 4 years lookahead to avoid infinite loop on invalid combos like Feb 30), `scheduler/ScheduledTrigger.java implements Comparable<ScheduledTrigger>` (ordered by next-fire `Instant`).
- Testing: `CronExpressionParserTest` (valid/invalid expressions via `@ParameterizedTest`), `CronScheduleCalculatorTest` (known expressions like `0 2 * * *`, `*/15 * * * *`, `0 9 * * 1-5` verified against manually computed expected next-fire times).
- Expected Output: Parser rejects malformed cron with `InvalidCronExpressionException` including the offending field.
- Dependencies: Phase 1.
- Time Estimate: 2 days.

## PHASE 4 — Concurrency Core: Scheduler Engine & Job Executor (Est. 4–5 days)

**Objectives:** Wire the threading model described in Part 6.3 — the single riskiest, most interview-relevant phase.

- Files/Classes: `scheduler/SchedulerEngine.java` (owns the `PriorityQueue<ScheduledTrigger>`, runs on a single-thread `ScheduledExecutorService` tick loop), `scheduler/JobExecutor.java` (wraps the bounded worker `ExecutorService`, applies per-job timeout via `Future.get(timeout, unit)` + `Future.cancel(true)`), `concurrency/JobLockRegistry.java` (`ConcurrentHashMap<JobId, ReentrantLock>` + `Condition` for `QUEUE` overlap policy), `concurrency/NamedThreadFactory.java` (for debuggable thread names in logs/thread dumps), `retry/*` (all four retry policy classes + `RetryPolicyFactory`), `priority/*` (both priority strategies).
- Features: submit a workflow run; independent jobs execute in parallel; dependent jobs wait; a job that throws is retried per its policy; a job exceeding its timeout is interrupted and marked `TIMED_OUT`; overlapping triggers for the same workflow are handled per `OverlapPolicy`.
- Testing: `SchedulerEngineIntegrationTest` using real thread pools with short (millisecond-scale) fake jobs and `CountDownLatch`/`CyclicBarrier` synchronization in the test to avoid flaky `Thread.sleep`-based assertions; `JobLockRegistryConcurrencyTest` (spin up N threads hammering the same job id, assert no two acquire the lock simultaneously using an `AtomicInteger` concurrent-access counter); retry policy unit tests asserting exact delay sequences.
- Expected Output: Demonstrable, testable proof of correct concurrent DAG execution — this is the centerpiece to show in interviews and in a project demo video/gif for the resume/GitHub README.
- Dependencies: Phases 1–3.
- Time Estimate: 4–5 days (do not rush; concurrency bugs are the #1 source of embarrassment in a technical interview if the candidate can't explain a race they shipped).

## PHASE 5 — Event Bus & Listeners (Est. 1–2 days)

**Objectives:** Decouple execution from side-effecting notifications (Observer pattern).

- Files/Classes: `events/EventBus.java` (internal `LinkedBlockingQueue<JobStatusEvent>` + single dispatch thread + `CopyOnWriteArrayList<EventListener>` subscribers), `events/JobStatusEvent.java` (immutable event record: job id, run id, old status, new status, timestamp, optional error), `events/AbstractEventListener.java` (template method: `onEvent()` calls abstract `handle()` after common filtering/logging), `events/ConsoleEventListener.java`, `events/FileEventListener.java` (writes to a rotating log file via SLF4J), `events/MetricsEventListener.java` (in-memory `ConcurrentHashMap<JobId, RunningStats>` — counts, success/fail totals, running average duration via Welford's or simple incremental mean).
- Testing: `EventBusTest` (publish N events from M concurrent threads, assert all N are eventually delivered to a test spy listener, in an order per-job-id preserving causal order), `MetricsEventListenerTest`.
- Dependencies: Phase 4 (integrates with the executor's publish calls).
- Time Estimate: 1–2 days.

## PHASE 6 — Persistence Layer (JDBC, No ORM) (Est. 3–4 days)

**Objectives:** Real, hand-written JDBC DAO layer with transactions, pagination, filtering.

- Files/Classes: `persistence/ConnectionManager.java` (wraps HikariCP `DataSource`, reads config from `ConfigService`), `persistence/Repository.java` (generic `interface Repository<T, ID> { Optional<T> findById(ID id); T save(T entity); List<T> findAll(); }`), `persistence/WorkflowRepository.java` + `JdbcWorkflowRepository.java`, `persistence/JobRunRepository.java` + `JdbcJobRunRepository.java`, `persistence/RunQuery.java` (filter criteria: status, jobId, date range), `persistence/Page.java`/`PageResult.java` (pagination request/response), `persistence/migration/V1__init_schema.sql` (the schema from Part 7.2, corrected FK ordering).
- Features: Save/find workflows and job runs; `findRuns(RunQuery, Page)` builds a dynamic parameterized SQL `WHERE` clause safely (no string concatenation of user input — always `PreparedStatement` parameters, explicitly called out as a SQL-injection-prevention decision in `ARCHITECTURE.md`); transactional multi-row workflow registration; startup `ReconciliationService` sweeping stale `RUNNING` rows.
- Testing: `JdbcJobRunRepositoryTest` running against an in-memory H2 database configured with PostgreSQL compatibility mode, using JUnit 5 `@BeforeEach` to re-run the migration script fresh per test (fast, isolated, no shared state); `PaginationTest` seeding 50 rows and asserting correct paging math and ordering; `TransactionRollbackTest` (force a failure mid-transaction, assert nothing was persisted).
- Dependencies: Phase 1 (entities), Part 7 schema.
- Time Estimate: 3–4 days.

## PHASE 7 — Service Layer, Reporting & Streams (Est. 2–3 days)

**Objectives:** Orchestration + analytics using the Streams API; the "recommendation/ranking/statistics" functional requirements.

- Files/Classes: `service/WorkflowService.java`, `service/SchedulingService.java`, `service/ReportService.java` (`generateReport` overloads — method overloading requirement — using `Collectors.groupingBy`, `Collectors.averagingDouble`, `DoubleSummaryStatistics`, sorted-list percentile calculation), `service/ReconciliationService.java`, `dto/WorkflowStatusDto.java`, `dto/JobRunSummaryDto.java`.
- Features: success-rate-per-job report; average/percentile duration report; CSV export via NIO `Files.write`; console/table-formatted report.
- Testing: `ReportServiceTest` with a seeded fixture of job runs, asserting exact aggregate numbers (including edge cases: zero runs, all-failed, single run).
- Dependencies: Phase 6.
- Time Estimate: 2–3 days.

## PHASE 8 — CLI & Embedded HTTP Status API (Est. 2 days)

**Objectives:** Delivery/interface layer — thin controllers only.

- Files/Classes: `cli/TaskFlowCli.java`, `cli/CommandParser.java` (hand-written arg parsing, no external CLI library, to keep dependency surface small and demonstrate basic parsing/regex skill), `http/StatusHttpApi.java` (JDK's built-in `com.sun.net.httpserver.HttpServer`), `http/JsonWriter.java` (hand-rolled JSON serialization using `StringBuilder`/streams — explicitly documented as "why we didn't just pull in Jackson: to prove we understand what a JSON serializer actually does," while noting in `ARCHITECTURE.md` that Jackson would be the pragmatic real-world choice).
- Features: all CLI commands from FR-12; `GET /status`, `GET /workflows/{id}` JSON endpoints.
- Testing: `CommandParserTest`; `StatusHttpApiTest` (spin up the embedded server on a random free port, issue real `HttpClient` requests, assert JSON shape).
- Dependencies: Phase 7.
- Time Estimate: 2 days.

## PHASE 9 — Config, Logging, Bootstrap/DI Wiring (Est. 1–2 days)

**Objectives:** Cross-cutting concerns finalized; assemble `AppContext`/`Bootstrap` wiring every layer via manual constructor injection.

- Files/Classes: `config/ConfigService.java` (layered: defaults → `application-{profile}.properties` → environment variable overrides), `config/AppContext.java` (the Singleton-but-documented composition root), `resources/logback.xml` (console appender + rolling file appender, MDC pattern including job id/run id), `resources/messages.properties`.
- Features: `TASKFLOW_PROFILE=dev|prod` environment variable selects config file; DB credentials only from env vars in prod profile (never committed); graceful shutdown hook wired here.
- Testing: `ConfigServiceTest` (override precedence order verified).
- Dependencies: all previous phases (this is the integration/glue phase).
- Time Estimate: 1–2 days.

## PHASE 10 — Testing Hardening, Coverage, Polish, Documentation (Est. 3–4 days)

**Objectives:** Hit NFR-03 (≥80% coverage on `core`/`scheduler`), write missing edge-case tests, finalize all documentation, record a demo.

- Add JaCoCo Maven plugin; generate coverage report; backfill any gaps.
- Write `docs/api.md`, `docs/setup.md`, finalize `ARCHITECTURE.md` with the diagrams from Part 6.
- Record a short terminal-cast/gif of TaskFlow running a sample 5-job diamond-DAG workflow with one simulated failure + retry, for the GitHub README (huge resume-portfolio value — recruiters look at README media).
- Final `CHANGELOG.md` entry, tag `v1.0.0`.
- Dependencies: all previous phases.
- Time Estimate: 3–4 days.

**TOTAL: ~10 phases, ~24–30 focused working days ≈ 4–6 weeks part-time**, matching the brief's target project size.


---

# PART 10 — DESIGN PATTERNS CATALOG (APPLIED, WITH RATIONALE)

| Pattern | Class(es) | Why This Pattern, Specifically Here (agent must include this reasoning in JavaDoc/ARCHITECTURE.md) |
|---|---|---|
| **Strategy** | `RetryPolicy`, `JobPriorityStrategy` | Retry behavior and scheduling priority are policies that vary independently of the engine; encoding them as interchangeable strategy objects avoids `if/else` branching on policy type scattered through `SchedulerEngine`, satisfying OCP. |
| **Observer** | `EventBus` + `EventListener` | Job execution must not know or care who's listening (console, file, metrics, future Slack webhook). Decoupling publisher from subscribers is the textbook Observer use case. |
| **Builder** | `Workflow.Builder`, `JobRun.Builder` | Both objects have many optional/required fields and must be immutable once built; a builder avoids telescoping constructors and enforces validation (e.g., DAG cycle check) at `build()` time. |
| **Factory Method / Simple Factory** | `RetryPolicyFactory`, `JobFactory` | Job classes are instantiated by fully-qualified name from configuration (reflection); a factory centralizes that reflective, exception-prone logic in one place instead of scattering `Class.forName` calls. |
| **Template Method** | `AbstractEventListener.onEvent()` | Common cross-cutting logic (logging that an event arrived, null-checking) belongs in the base class; subclasses only implement the specific `handle()` behavior. |
| **Repository** | `JobRunRepository`, `WorkflowRepository` | Isolates persistence/SQL details from the service layer; enables swapping JDBC for an in-memory fake in fast unit tests (`InMemoryJobRunRepository`). |
| **Singleton (documented, justified, thread-safe)** | `AppContext` | A single composition root per process is appropriate here (this is a single-JVM app); implemented via a `private static final` eagerly-initialized instance (thread-safe by JVM classloading guarantees) rather than a double-checked-locking anti-pattern, and the agent must document *why* Singleton is acceptable here but should be avoided for stateful domain objects. |
| **DTO** | `WorkflowStatusDto`, `JobRunSummaryDto` | The HTTP/CLI layer must never leak persistence entities (e.g., DB-specific fields) to external consumers; DTOs are the seam. |
| **Command (implicit)** | `Job.execute(JobContext)` itself is a Command object — encapsulating a request as an object that can be queued, logged, and retried. | Explicitly call this out in interview prep: "every `Job` is effectively a Command pattern instance." |

---

# PART 11 — TESTING STRATEGY

## 11.1 Test Pyramid

- **Unit tests (majority):** pure logic — domain model, DAG algorithms, cron parser, retry-delay math, report aggregation math, LRU cache eviction — using JUnit 5 + AssertJ-style fluent assertions (or plain JUnit assertions) + Mockito for isolating collaborators (e.g., mock `JobRunRepository` when testing `SchedulingService`).
- **Integration tests (moderate):** `SchedulerEngineIntegrationTest` (real thread pools, fake fast jobs), `JdbcJobRunRepositoryTest` (real H2 DB), `StatusHttpApiTest` (real embedded HTTP server on ephemeral port).
- **Concurrency-focused tests:** `JobLockRegistryConcurrencyTest`, `EventBusConcurrencyTest` — use `ExecutorService` + `CountDownLatch`/`CyclicBarrier` to force interleavings, and `AtomicInteger`/`AtomicBoolean` to detect races deterministically rather than relying on timing.
- **No end-to-end/UI tests** (there is no UI).

## 11.2 Mockito Usage Guidelines

- Mock only true collaborators at a boundary (repositories, event bus, clock/time source via an injectable `Clock` abstraction for deterministic time-based tests) — never mock simple value objects or the class under test.
- Prefer `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks` for services with 2–4 collaborators; wire manually where clarity benefits.
- Use `ArgumentCaptor` to assert exactly what was passed to `JobRunRepository.save(...)` after a simulated failure+retry sequence.

## 11.3 Coverage Targets

- `core`, `scheduler` packages: **≥80% line coverage** (NFR-03), enforced via the JaCoCo Maven plugin's `check` goal failing the build below threshold.
- `persistence`, `service`: ≥70%.
- `cli`, `http`: ≥50% (thin layers, lower marginal value, but must cover the "happy path" of every command/endpoint).

## 11.4 Edge Cases Checklist (Agent Must Explicitly Write Tests For)

- Empty workflow (zero jobs) — should register but produce a no-op run.
- Single job with no dependencies.
- Diamond dependency graph (already noted).
- Self-dependency (job depends on itself) — rejected.
- Two-node cycle and a longer N-node cycle.
- Retry exhausting all attempts — final status `FAILED`, not stuck in `RETRYING`.
- Job exceeding timeout exactly at the boundary (use a controllable fake `Clock`, not real sleeps, where feasible).
- Overlap policy `SKIP` when a previous run is still in progress.
- Pagination: page size larger than total rows; empty result set; last partial page.
- Cron expression at DST transition boundaries (documented as a known tricky edge case; a reasonable, tested, and documented behavior is acceptable — perfection is not required, but the trade-off must be written down).

---

# PART 12 — LOGGING STRATEGY

- **Framework:** SLF4J API, Logback implementation (`logback.xml` in `resources/`).
- **Levels:**
  - `TRACE`: extremely verbose scheduler tick internals (off by default).
  - `DEBUG`: per-job lock acquisition/release, retry delay calculations.
  - `INFO`: workflow triggered, job started/succeeded, scheduler startup/shutdown.
  - `WARN`: retry attempts, stale `RUNNING` rows reconciled at boot, timeout approaching.
  - `ERROR`: job permanently failed after exhausting retries, persistence failures, unhandled exceptions bubbling to the top-level catch.
- **MDC (Mapped Diagnostic Context):** every job execution log line is tagged with `jobId`, `runId`, `workflowId` via `MDC.put(...)`/`MDC.remove(...)` in a try/finally, so that concurrent job logs interleaved in the console/file remain traceable per-job (a genuinely valuable, senior-sounding technique to mention in interviews).
- **Appenders:** `ConsoleAppender` (dev profile, colored pattern), `RollingFileAppender` (prod profile, size + time based rolling policy, e.g. `taskflow.%d{yyyy-MM-dd}.%i.log`, max 10MB per file, 30-day retention).
- **Log rotation:** configured via Logback's `SizeAndTimeBasedRollingPolicy`.

---

# PART 13 — CONFIGURATION & SECRETS MANAGEMENT

- `application.properties` — safe committed defaults (thread pool sizes, default retry settings, H2 test DB URL).
- `application-dev.properties` — local Postgres/H2 connection details for development.
- `application-prod.properties` — **no literal secrets**; every sensitive value is `${ENV_VAR_NAME}`-style, resolved by `ConfigService` reading `System.getenv()` first, falling back to the properties file only for non-sensitive values.
- `.env.example` — documents required environment variables (`TASKFLOW_DB_URL`, `TASKFLOW_DB_USER`, `TASKFLOW_DB_PASSWORD`, `TASKFLOW_PROFILE`, `TASKFLOW_WORKER_THREADS`) without real values.
- `.gitignore` must exclude any real `.env` file and any `*.properties` file suffixed `-local`.
- `ConfigService` precedence, highest to lowest: **environment variable > profile-specific properties file > default properties file**.

---

# PART 14 — GIT WORKFLOW & CODE REVIEW CHECKLIST

## 14.1 Branch Strategy

- `main` — always green/buildable, tagged per phase (`v0.1.0` … `v1.0.0`).
- `feature/<phase-number>-<short-description>` (e.g., `feature/04-scheduler-engine`) — one branch per phase or sub-feature, merged via PR (even solo, self-review the diff before merging — good habit to demonstrate).
- `fix/<short-description>` for post-merge bug fixes.

## 14.2 Commit Convention (Conventional Commits)

```
feat: add topological sort for workflow DAG
fix: correct off-by-one in cron minute field parser
test: add concurrency test for JobLockRegistry
docs: update ARCHITECTURE.md with threading model diagram
refactor: extract RetryPolicyFactory from SchedulerEngine
chore: bump JUnit to 5.10
```

## 14.3 Pull Request / Self-Review Checklist

- [ ] Does it compile and do all tests pass locally?
- [ ] Is coverage for touched packages at/above target?
- [ ] Are all new public classes/methods JavaDoc'd?
- [ ] Any new `TODO`/`FIXME` resolved or explicitly logged in `DECISIONS.md`?
- [ ] No secrets committed (`git diff` scanned for connection strings/passwords).
- [ ] No `System.out.println` left in production code paths (logging only, except intentional CLI user-facing prints).
- [ ] Any new dependency justified in `ARCHITECTURE.md`?
- [ ] `CHANGELOG.md` updated?

---

# PART 15 — CODE QUALITY STANDARDS

- **Naming:** classes `PascalCase` nouns; methods `camelCase` verbs (`computeNextFireTime`, not `nextFireTimeCompute`); booleans read as predicates (`isTerminal()`, `hasDependents()`); constants `UPPER_SNAKE_CASE`.
- **JavaDoc:** every `public` class and method in `api`, `core`, `scheduler`, `service` packages must have a JavaDoc comment: summary line, `@param` for every parameter, `@return` (if non-void), `@throws` for every checked/relevant unchecked exception documented as part of the contract.
- **Comments:** explain *why*, not *what* (the code already says what); e.g., "// Using iterative DFS instead of recursive to avoid StackOverflowError on workflows with 10k+ jobs" is a good comment; "// increment i" is not.
- **Formatting:** 4-space indentation, 120-character line limit, one top-level class per file, imports organized (no wildcard imports), consistent brace style (opening brace same line).
- **Code Smells To Avoid / Refactor On Sight:** long methods (>40 lines — extract), deep nesting (>3 levels — use guard clauses/early returns), primitive obsession (use `JobId`/`WorkflowId` wrapper types instead of raw `String` everywhere), duplicated logic (extract to a shared utility or base class), god classes (if `SchedulerEngine` starts exceeding ~300–400 lines, extract collaborators).
- **Static analysis (recommended, optional):** SpotBugs / Checkstyle Maven plugins configured with a reasonable ruleset; not a hard requirement but adds resume polish ("configured static analysis in CI").

---

# PART 16 — BONUS / STRETCH FEATURES (Priority-Ordered)

Only attempt these **after** Phase 10 is fully complete and stable. Each is independently valuable for the resume "Future Scope" / "What I'd add next" section, which interviewers love asking about.

1. **Authentication + Role Management** for the HTTP status API (simple API-key header check → `ADMIN` can trigger/pause, `VIEWER` read-only) — demonstrates security awareness.
2. **Audit Log** — a dedicated `audit_log` table recording who paused/resumed/triggered what and when.
3. **Command Pattern + Undo** for CLI actions (e.g., `pause` can be `undo`ne within the session) — nice, concrete Command-pattern showcase distinct from the `Job` interface's implicit use of it.
4. **Plugin Architecture** — formalize the reflective `JobFactory` into a documented plugin contract (`META-INF/services`-based `ServiceLoader` discovery of `Job` implementations) — great for demonstrating SPI knowledge.
5. **Hand-rolled Connection Pool** (`BlockingQueue<Connection>`-based) replacing HikariCP, purely to showcase producer/consumer pooling mastery (mentioned in Part 7.5).
6. **PDF/CSV Report Export** — extend `ReportService` to emit a PDF summary (a small, dependency-light PDF writer, or CSV only if PDF proves too heavy a dependency for the "minimal dependencies" ethos).
7. **Simple Web Dashboard** (explicitly optional/out-of-scope for the core project — only attempt if all else is done and there's spare time; keep it to a single static HTML page polling the JSON status endpoint, no framework).
8. **Notification Webhook Listener** (`SlackWebhookEventListener` / generic `HttpWebhookEventListener`) — a second real-world `EventListener` implementation.
9. **Background Compaction Job** — TaskFlow using itself: register a built-in `Job` that periodically archives/deletes `job_runs` rows older than N days (dogfooding the scheduler to maintain its own database — a great interview anecdote: "the scheduler schedules its own housekeeping").
10. **Caching layer for `ReportService`** results with TTL, reusing the `LruCache` abstraction (search/caching bonus box, ticked cleanly).

---

# PART 17 — DOCUMENTATION DELIVERABLES CHECKLIST

The agent must produce and keep up to date:

- [ ] `README.md` — project overview, badges (build status if CI is set up), quick start, feature list, architecture diagram (embedded from `docs/`), sample CLI session transcript/gif, license.
- [ ] `ARCHITECTURE.md` — the full Part 6 content, expanded with any decisions made during actual implementation.
- [ ] `docs/setup.md` — prerequisites (JDK 17, Maven, Postgres or Docker), step-by-step local run instructions, environment variable table.
- [ ] `docs/api.md` — every CLI command and every HTTP endpoint documented with example input/output.
- [ ] `docs/er-diagram` (mermaid or image) — the schema from Part 7.
- [ ] `docs/sequence-diagrams.md` — the sequence from Part 6.2 plus any additional key flows (retry sequence, graceful shutdown sequence).
- [ ] `CHANGELOG.md` — one entry per phase/tag.
- [ ] `DECISIONS.md` — running log of every non-obvious decision and its rationale (this single file is gold for interview prep — it's literally pre-written interview answers).
- [ ] Full JavaDoc, buildable via `mvn javadoc:javadoc`.


---

# PART 18 — RESUME & CAREER SECTION

## 18.1 Exactly Which Skills This Project Demonstrates

**Core Java:** OOP (all four pillars), interfaces & abstract classes, generics, enums with behavior, exception hierarchies, the Collections Framework (7+ distinct structures used purposefully), Streams API, Optional, Java Time API, regex, NIO file handling, serialization, reflection, annotations.
**Concurrency:** `ExecutorService`/`ScheduledExecutorService`, thread pools, `ReentrantLock`/`ReadWriteLock`, concurrent collections, race-condition-safe design, graceful shutdown.
**Data Persistence:** raw JDBC, transactions, connection pooling, schema design, normalization, indexing, pagination.
**Software Engineering:** SOLID, layered architecture, 6+ design patterns applied purposefully (not decoratively), manual dependency injection, unit + integration testing (JUnit5 + Mockito), ≥80% coverage, structured logging, environment-based configuration, Git discipline.
**Computer Science Fundamentals:** graph algorithms (topological sort, cycle detection), heap/priority-queue-based scheduling, complexity-aware algorithm choices, greedy ranking heuristics.

## 18.2 How Recruiters Will Evaluate It

Recruiters scanning a resume/GitHub in ~30 seconds look for: (1) a project title that isn't "Todo App #4,281," (2) a README with a clear problem statement and an architecture diagram, (3) commit history showing sustained, incremental work (not one giant commit), (4) tests present, (5) technology keywords matching the job description (Java, JDBC, multithreading, design patterns, SQL — all present here). TaskFlow is built specifically to pass this 30-second scan and reward a deeper 5-minute read.

## 18.3 Resume Bullet Points (Copy-Paste Ready — Choose 3–5)

- Designed and implemented **TaskFlow**, a multithreaded job-scheduling and workflow-orchestration engine in core Java, supporting DAG-based task dependencies, cron/interval scheduling, and configurable retry strategies (exponential backoff, fixed delay).
- Implemented DAG validation and topological sort algorithms (Kahn's algorithm, iterative DFS cycle detection) to safely schedule 100+ interdependent jobs with automatic parallelization of independent tasks.
- Built a thread-safe scheduling core using `ExecutorService`, `ReentrantLock`, and concurrent collections, decoupling scheduling decisions, job execution, and event dispatch across dedicated thread pools to prevent contention and blocking.
- Designed and normalized a PostgreSQL schema (5 tables, 4 indexes) and implemented a hand-written JDBC persistence layer with transaction management, dynamic filtering, and pagination — no ORM.
- Applied 6 GoF design patterns (Strategy, Observer, Builder, Factory, Template Method, Repository) to keep the system open for extension without modifying existing code (SOLID/OCP).
- Achieved 80%+ unit/integration test coverage using JUnit 5 and Mockito, including deterministic concurrency tests validating lock correctness under simulated contention.
- Built a CLI and an embedded JSON status API (JDK `HttpServer`) for operating and inspecting running workflows in real time.

## 18.4 ATS Keywords To Include

`Java`, `Multithreading`, `Concurrency`, `ExecutorService`, `Thread Pool`, `JDBC`, `SQL`, `PostgreSQL`, `Transactions`, `Design Patterns`, `SOLID Principles`, `Data Structures`, `Algorithms`, `Graph Algorithms`, `DAG`, `Topological Sort`, `JUnit`, `Mockito`, `Unit Testing`, `Integration Testing`, `SLF4J`, `Logback`, `Object-Oriented Design`, `Streams API`, `Collections Framework`, `Dependency Injection`, `Repository Pattern`, `Maven`, `Git`, `REST/HTTP`, `Scheduler`, `Distributed Systems Concepts`.

## 18.5 GitHub Description (Short)

> **TaskFlow** — A multithreaded workflow orchestration engine in core Java. Define jobs as a DAG, schedule with cron/interval expressions, and execute with configurable retries, timeouts, and fault-tolerant recovery — no frameworks, just Java, JDBC, and concurrency done right.

## 18.6 LinkedIn Project Description (Medium Length)

> TaskFlow is a job-scheduling and workflow-orchestration engine I built in core Java, inspired by tools like Airflow and Quartz Scheduler. It lets you define jobs with dependencies (as a DAG), schedule them with cron or interval expressions, and run them safely and concurrently using a hand-built threading model — a dedicated scheduler thread, a bounded worker pool, and an async event bus, coordinated with `ReentrantLock`s and concurrent collections. Job history is persisted via hand-written JDBC (PostgreSQL), with transactions, pagination, and dynamic filtering. The project applies SOLID principles and six classic design patterns, and is backed by 80%+ test coverage including deterministic concurrency tests. Built entirely without Spring or an ORM, to prove out core Java and systems-design fundamentals from first principles.

## 18.7 Project Explanation (For a Cover Letter / Application Form)

> I built TaskFlow to deepen my understanding of concurrent systems and to have a portfolio project that reflects real backend engineering problems rather than another CRUD app. It solves the same class of problem as Airflow or cron+scripts-in-production: reliably running dependent, scheduled units of work with retries and observability. Building it required designing graph algorithms (for dependency resolution), a correct threading model (to execute jobs in parallel without races), and a persistence layer from raw JDBC (to understand exactly what an ORM usually hides). It's the project I'm most proud to walk an interviewer through.

## 18.8 Interview Pitch — 30 Seconds

> "I built TaskFlow, a Java workflow-orchestration engine similar in spirit to Airflow or Quartz. You define jobs as a dependency graph, and it schedules and runs them concurrently — handling retries, timeouts, and failure recovery — using a hand-built threading model and a raw JDBC persistence layer, no frameworks. I built it to really understand concurrency and systems design instead of just calling library methods."

## 18.9 Interview Pitch — 2 Minutes

> "TaskFlow is a workflow orchestration engine I built in core Java — think a lightweight version of Airflow or Quartz Scheduler. The problem it solves: you have jobs with dependencies — like extract, then transform, then load — and you want to schedule them, run independent jobs in parallel, retry failures intelligently, and know exactly what happened and when.
>
> Architecturally, I split it into layers: an `api` package with the public contracts, a `core` domain model, a `scheduler` package with the actual orchestration logic, a hand-written JDBC persistence layer, and thin CLI/HTTP delivery layers on top — all wired together with manual dependency injection, no Spring.
>
> The two hardest parts were, first, the graph algorithms — I implemented topological sort with Kahn's algorithm and iterative cycle detection so a workflow with a dependency cycle gets rejected up front with a clear error, rather than deadlocking at runtime. Second, and honestly the part I learned the most from, was the concurrency model. I run scheduling decisions on one dedicated thread, job execution on a separate bounded worker pool, and event notification on yet another thread, all coordinated with `ReentrantLock`s per job and concurrent collections — so a slow event listener can never block a worker thread, and two triggers for the same job can never run concurrently by accident.
>
> I persisted everything with raw JDBC — no ORM — because I wanted to actually understand transactions, connection pooling, and query design rather than have it abstracted away. And I applied patterns like Strategy for retry policies, Observer for the event bus, and Builder for constructing workflows, specifically where they solved a real extensibility problem, not just to tick a box.
>
> It's got 80%+ test coverage, including deterministic concurrency tests using latches and barriers instead of `Thread.sleep`, which was its own learning curve. If I kept extending it, the natural next step would be making it distributed — multiple TaskFlow nodes coordinating via something like a leader election or a shared lock service — which is explicitly in my 'future scope' notes."


---

# PART 19 — 100 INTERVIEW QUESTIONS WITH ANSWERS (BASED ON THIS PROJECT)

*Organized into 10 categories of 10 questions each. Answers are intentionally concise — expand verbally in a real interview using specifics from your own implementation and `DECISIONS.md`.*

## 19.1 Java & OOP Fundamentals (Q1–Q10)

**Q1. Why did you model `JobId`/`WorkflowId` as wrapper classes instead of plain `String`s?**
A: To avoid "primitive obsession" — a `String` could accidentally be a job name or a SQL fragment; a dedicated `JobId` type makes the compiler catch mix-ups and lets me add validation/equality semantics in one place.

**Q2. Where did you use inheritance vs. composition, and why?**
A: Inheritance for closely related variants sharing a template (`AbstractRetryPolicy` → concrete policies, `AbstractEventListener` → concrete listeners). Composition everywhere else (e.g., `SchedulerEngine` *has a* `JobExecutor`, not *is a* `JobExecutor`) — composition is generally preferred; inheritance was reserved for genuine "is-a-kind-of" template relationships.

**Q3. Explain polymorphism as used in your `RetryPolicy` design.**
A: `SchedulerEngine` calls `retryPolicy.nextDelay(attempt)` through the `RetryPolicy` interface reference; at runtime the actual object could be `FixedDelayRetryPolicy` or `ExponentialBackoffRetryPolicy` — the call site doesn't know or care which, satisfying dynamic dispatch.

**Q4. Why an interface (`Job`) rather than an abstract class for job definitions?**
A: `Job` has exactly one behavior contract (`execute`) and no shared state/implementation to provide — a pure `@FunctionalInterface` is the right fit and also allows lambda-based job definitions in tests.

**Q5. What's the difference between method overloading and overriding, and where does each appear in TaskFlow?**
A: Overloading = same name, different parameter list, resolved at compile time (`ReportService.generateReport(Duration)` vs `generateReport(Instant, Instant)`). Overriding = subclass supplies its own implementation of a superclass/interface method, resolved at runtime (every concrete `RetryPolicy.nextDelay(...)`).

**Q6. Why are your domain objects (e.g., `JobRun`) largely immutable?**
A: Immutable objects are inherently thread-safe (no synchronization needed to read them), easier to reason about, and safe to share across the scheduler/worker/event-bus threads without defensive copying.

**Q7. What are the trade-offs of using enums with behavior (like `JobStatus.isTerminal()`) vs. constants + a utility method?**
A: Enums keep the behavior colocated with the data it describes, are exhaustively checkable in `switch` statements (compiler warns on missing cases), and prevent invalid values entirely — a plain `int`/`String` constant set can't offer that type safety.

**Q8. Where did you use `Optional`, and why not just return `null`?**
A: `JobRunRepository.findById` returns `Optional<JobRun>` so callers are forced to explicitly handle the "not found" case rather than risk an unchecked `NullPointerException` deep in calling code.

**Q9. Explain a place you used generics and why raw types would've been worse.**
A: `Repository<T, ID>` and `LruCache<K, V>` — generics give compile-time type safety (you can't accidentally put a `WorkflowRun` into a `JobRun` cache) and eliminate manual casting.

**Q10. Why is `Workflow` built via a `Builder` rather than a big constructor?**
A: `Workflow` has several required/optional fields plus a cross-field invariant (must be acyclic) that's only checkable once the whole graph is assembled — a builder lets me validate once at `build()` and keep the final object immutable and always valid.

## 19.2 Collections & Data Structures (Q11–Q20)

**Q11. Why `PriorityQueue` for the scheduler's trigger queue instead of sorting a `List` each tick?**
A: `PriorityQueue` gives `O(log n)` insert/poll for the "what fires next" min-heap operation, versus `O(n log n)` re-sorting a list every tick — critical as the number of scheduled workflows grows.

**Q12. Why `ConcurrentHashMap` instead of `Collections.synchronizedMap(new HashMap<>())` for the job registry?**
A: `ConcurrentHashMap` uses fine-grained (segment/bucket-level) locking, allowing genuinely concurrent reads and writes to different keys, whereas `synchronizedMap` serializes *all* access behind one lock — a needless bottleneck for a registry read on every scheduler tick.

**Q13. Why `TreeMap`/`TreeSet` for run-history indexing instead of `HashMap`/`HashSet`?**
A: `TreeMap`/`TreeSet` keep keys in sorted order automatically (by timestamp/priority), enabling efficient range queries and always-sorted iteration without a separate sort step — at the cost of `O(log n)` vs `O(1)` average operations.

**Q14. How does `ArrayDeque` serve as both a stack and a queue in your DAG algorithms?**
A: `ArrayDeque` implements `Deque`, which supports push/pop (stack, used for iterative DFS cycle detection) and offer/poll (queue, used for Kahn's algorithm's zero-indegree frontier) — one class covering both traversal styles.

**Q15. Why did you choose `LinkedBlockingQueue` for the event bus buffer?**
A: It's a thread-safe, blocking producer-consumer queue built for exactly this handoff: many producer threads (workers publishing events) and one consumer thread (the dispatcher), decoupling their speeds.

**Q16. What is the time complexity of your topological sort, and why does that matter here?**
A: `O(V + E)` — linear in jobs plus dependency edges — using Kahn's algorithm. It matters because workflow validation happens on every registration/redefinition and potentially on every trigger, so it must scale to large DAGs without becoming a bottleneck.

**Q17. Explain your LRU cache implementation.**
A: Backed by a `LinkedHashMap` in access-order mode with `removeEldestEntry` overridden to evict past a capacity, or a hand-rolled `HashMap` + doubly linked list for full control — it caches computed topological orders per workflow version so unchanged workflows don't get re-sorted on every trigger.

**Q18. Why not just use recursion for DFS cycle detection?**
A: Recursive DFS risks a `StackOverflowError` on very deep or pathological workflow graphs; an explicit `Deque`-backed iterative stack removes that ceiling and is the standard production-safe approach.

**Q19. How do you prevent a `ConcurrentModificationException` when listeners might be added while events are being dispatched?**
A: The listener list is a `CopyOnWriteArrayList` — safe for concurrent iteration (dispatch) while occasional mutation (subscribe/unsubscribe) happens rarely, which is the ideal use case for copy-on-write.

**Q20. When would you *not* use a `PriorityQueue` for scheduling, at larger scale?**
A: At true distributed scale, a single in-JVM heap doesn't coordinate across nodes; you'd need a distributed priority mechanism (e.g., a database-backed "claim the next due row" pattern with row-level locking, or a proper distributed queue like Kafka/SQS) — noted explicitly in "Future Scope."

## 19.3 Concurrency & Multithreading (Q21–Q30)

**Q21. Walk me through your threading model.**
A: One dedicated scheduler thread owns the trigger priority queue and decides what's due; a separate bounded worker `ExecutorService` executes job logic; a single event-dispatch thread drains a blocking queue and calls listeners — three concerns, three thread pools, explicitly to avoid one slow component blocking another.

**Q22. Why use `ReentrantLock` instead of `synchronized` for per-job overlap prevention?**
A: `ReentrantLock` supports `tryLock(timeout)` (needed for the `QUEUE` overlap policy, waiting up to a bound rather than forever) and paired `Condition`s, which plain `synchronized`/`wait`/`notify` make far more awkward to express correctly.

**Q23. How do you avoid deadlock between the workflow `ReadWriteLock` and per-job `ReentrantLock`s?**
A: By strict lock ordering and scope minimization — the read lock on the registry is held only briefly to fetch a workflow snapshot, then released before any per-job lock is acquired; locks are never held while calling into another component that might acquire a different lock, avoiding circular wait.

**Q24. What happens if a job's thread throws an unchecked exception?**
A: The `Future` wrapping the job's execution captures it; `JobExecutor` unwraps it in `Future.get()`'s `ExecutionException`, translates it to a `JobExecutionException`, and routes it through the same retry/failure path as a declared `JobExecutionException` — no silent thread death.

**Q25. How do you implement job timeouts?**
A: `Future<JobResult> future = workerPool.submit(job)`, then `future.get(timeoutSeconds, TimeUnit.SECONDS)`; on `TimeoutException`, call `future.cancel(true)` to interrupt the running thread and mark the run `TIMED_OUT`.

**Q26. Why test concurrency with `CountDownLatch`/`CyclicBarrier` instead of `Thread.sleep`?**
A: Sleep-based tests are flaky and slow — they either sleep too little (race, test flakes) or too long (test suite crawls). Latches/barriers give deterministic synchronization points, making concurrency tests both fast and reliable.

**Q27. How do you guarantee graceful shutdown doesn't lose in-flight work?**
A: A JVM shutdown hook calls `SchedulerEngine.shutdown()`, which stops accepting new triggers, then `ExecutorService.shutdown()` (finish queued/running tasks, refuse new ones) + `awaitTermination(boundedTimeout)`, escalating to `shutdownNow()` only if the bound is exceeded, with any forcibly-cancelled job explicitly logged.

**Q28. What's the risk of using a global lock for "no overlapping jobs anywhere," and how did you avoid it?**
A: A single global lock would serialize *all* jobs, even unrelated ones, destroying the whole point of a worker pool. I keep a `ConcurrentHashMap<JobId, ReentrantLock>` — one lock per job — so only truly overlapping instances of the *same* job contend.

**Q29. How would race conditions manifest if you used a plain `HashMap` for the job registry instead of `ConcurrentHashMap`?**
A: Under concurrent modification, `HashMap` can corrupt its internal bucket structure (in old JDKs this could even infinite-loop on resize) or silently lose entries — undefined, non-thread-safe behavior; `ConcurrentHashMap` guarantees safe concurrent access with documented semantics.

**Q30. What does "at-least-once, not exactly-once" execution mean in your system, and why did you accept that trade-off?**
A: If the JVM crashes mid-execution, on restart the reconciliation service can't know for certain whether the job's side effects actually completed, so it can't safely guarantee it wasn't already done — hence jobs must be idempotent by contract; achieving true exactly-once would need distributed transactional coordination beyond a single-JVM scheduler's scope, which is explicitly out of scope and documented as such.

## 19.4 Design Patterns & SOLID (Q31–Q40)

**Q31. Give an example of the Open/Closed Principle in your code.**
A: Adding a new `RetryPolicy` (e.g., a jittered backoff) requires only a new class implementing the interface plus a registry/factory entry — zero changes to `SchedulerEngine`, which depends only on the `RetryPolicy` abstraction.

**Q32. Where does Dependency Inversion show up?**
A: `SchedulingService` depends on the `JobRunRepository` interface, not `JdbcJobRunRepository` directly; the concrete implementation is injected at composition-root time in `AppContext`, so swapping to an in-memory fake for tests requires no service code changes.

**Q33. Why Observer for the event bus instead of having `SchedulerEngine` call each listener directly?**
A: Direct calls would couple the scheduler to every listener's existence and failure modes (a slow/broken listener could stall scheduling); Observer plus an async dispatch thread fully decouples "a status changed" from "what happens because of it."

**Q34. Why is your `AppContext` Singleton justified here, when Singletons are often criticized?**
A: Because there is genuinely one composition root per JVM process — it's not being used to smuggle mutable global state through business logic, and it's exposed once at `Main`, not reached for ad hoc throughout the codebase, which is the usual complaint about Singleton misuse.

**Q35. How does the Repository pattern help your testing story?**
A: Because `SchedulingService` depends on the `JobRunRepository` interface, tests substitute a fast, deterministic `InMemoryJobRunRepository` fake instead of standing up a real database for every unit test, keeping the test suite fast.

**Q36. What's the difference between Strategy and Template Method, and where did you use each?**
A: Strategy composes a whole interchangeable *behavior* as an object (`RetryPolicy` swapped in wholesale); Template Method fixes the *skeleton* of an algorithm in a base class and lets subclasses override specific *steps* (`AbstractEventListener.onEvent()` fixes "log then handle," subclasses only supply `handle()`).

**Q37. Why DTOs instead of just returning your JDBC-backed entities from the HTTP layer?**
A: DTOs decouple the external API's shape from internal persistence details — if I add a column to `job_runs` tomorrow, I don't want that automatically, silently changing the public JSON contract.

**Q38. Which SOLID principle did you find hardest to honor, and why?**
A: Interface Segregation — early on I was tempted to make one fat `Repository` interface for everything; splitting `JobRunRepository` from `WorkflowRepository` meant a bit more boilerplate but kept each interface focused and easier to fake in tests.

**Q39. How would you add a brand-new schedule type (e.g., "run after another workflow completes") without breaking existing code?**
A: Add a new `ScheduleType` enum value plus a corresponding schedule-calculation strategy class registered in the scheduling factory — existing `ONE_TIME`/`FIXED_INTERVAL`/`CRON` handling is untouched, consistent with OCP.

**Q40. Why Builder pattern specifically for `Workflow` rather than a static factory method?**
A: A static factory method with many parameters would suffer the same telescoping-constructor problem; the Builder additionally lets me add jobs one at a time via repeated `.addJob(...)` calls before a single validating `.build()`.

## 19.5 Architecture & System Design (Q41–Q50)

**Q42. Why no Spring Framework?**
A: Deliberately — the goal was to prove I understand what Spring automates (DI wiring, transaction management, bean lifecycle) by hand-building a small but correct version of it, which is a stronger signal to interviewers of fundamentals than "I called `@Autowired`."

**Q41. Describe your layering and why the CLI never talks directly to the persistence layer.**
A: Layers are `api → core → scheduler/persistence/events → service → cli/http`; the CLI only calls `service` classes, which orchestrate `scheduler` + `persistence` + `events` — this keeps delivery mechanisms swappable (I could add a gRPC frontend tomorrow without touching business logic).

**Q43. How would TaskFlow need to change to run across multiple machines?**
A: The single in-JVM `PriorityQueue` and per-job `ReentrantLock`s would need to become distributed primitives — e.g., a shared database row with `SELECT ... FOR UPDATE SKIP LOCKED` to claim due jobs, or a coordination service like ZooKeeper/etcd for leader election so only one node schedules at a time while all nodes can execute.

**Q44. Why an embedded `HttpServer` instead of a full REST framework?**
A: The status API is a small, read-mostly diagnostic surface — pulling in Spring MVC or similar for two GET endpoints would be disproportionate; the JDK's built-in `HttpServer` is enough and keeps the "minimal dependency" story honest, with the trade-off explicitly documented.

**Q45. How do you avoid the CLI/HTTP layers becoming "fat controllers"?**
A: All decision logic (validation, orchestration, retries) lives in `service`/`scheduler`; `cli`/`http` classes only parse input, call one service method, and format output.

**Q46. What would you monitor/alert on if this ran in production?**
A: Job failure rate per workflow (spike = likely upstream data/dependency issue), queue depth in the event bus (backpressure sign), worker pool saturation (all threads busy = under-provisioned), and stale-`RUNNING` reconciliation counts at startup (crash frequency signal).

**Q47. Why is the reconciliation step run only at startup rather than continuously?**
A: A stale `RUNNING` row can only be created by an ungraceful shutdown, which by definition means the process just (re)started — checking at boot is sufficient and cheaper than a continuous background sweep; a periodic sweep is noted as a reasonable future enhancement if multiple TaskFlow instances could share one database.

**Q48. How does your schema support "show me the last 20 failed runs for job X, paginated"?**
A: `idx_job_runs_job_id_started_at` supports an indexed `WHERE job_id = ? AND status = ? ORDER BY started_at DESC LIMIT ? OFFSET ?` query built dynamically but always via `PreparedStatement` parameters.

**Q49. What's your disaster-recovery story if the database is temporarily unreachable?**
A: Persistence failures are wrapped in a `PersistenceException`; the scheduler logs the failure at `ERROR`, does not crash the JVM, and (documented future enhancement) could buffer pending writes in memory with bounded capacity and retry — the current version fails the specific run's persistence but keeps scheduling alive, which is explicitly called a known limitation.

**Q50. Why PostgreSQL over MongoDB/NoSQL for this domain?**
A: The data is inherently relational (jobs belong to workflows, dependencies are edges between jobs, runs reference both) with strong consistency and transactional needs (atomic multi-row workflow registration) — a document store would force denormalization or application-level joins for no benefit here.

## 19.6 Database, JDBC & SQL (Q51–Q60)

**Q51. Why raw JDBC instead of an ORM like Hibernate?**
A: To demonstrate I understand exactly what happens under an ORM's hood — connection handling, statement preparation, transaction boundaries, result-set mapping — a decision explicitly documented as a learning trade-off; in a real production team I'd likely use an ORM or a lightweight mapper like JOOQ/MyBatis for the productivity win.

**Q52. How do you prevent SQL injection?**
A: Every dynamic value is bound via `PreparedStatement` parameters, never string-concatenated into SQL text, including in the dynamic `WHERE` clause builder for filtered run queries.

**Q53. Explain the transaction boundary when registering a new workflow.**
A: One JDBC `Connection` with `setAutoCommit(false)`; insert the `workflows` row, then all `jobs` rows, then all `job_dependencies` rows; `commit()` only if all succeed, otherwise `rollback()` in a `catch`/`finally` — ensuring the workflow is never partially visible.

**Q54. Why index `(job_id, started_at DESC)` together rather than separate single-column indexes?**
A: The common query filters by `job_id` *and* sorts/limits by `started_at` — a composite index lets the database satisfy both the filter and the sort order from one index scan, versus needing a separate sort step with single-column indexes.

**Q55. How did you test JDBC code without a running PostgreSQL server in CI?**
A: H2 in PostgreSQL-compatibility mode as an in-memory test database, with the same migration script re-run fresh per test for isolation — fast, no external dependency, close enough to catch most SQL portability issues.

**Q56. What isolation level do your transactions use, and why?**
A: The JDBC driver's default (`READ_COMMITTED` for PostgreSQL) is sufficient here since writes are narrowly scoped per job-run/workflow-run and don't require serializable guarantees across unrelated rows; this trade-off is documented rather than defaulted to blindly.

**Q57. How would you handle a schema migration in production without downtime?**
A: Additive-first migrations (add nullable columns, backfill, then enforce constraints in a later release), using a migration tool with numbered scripts (Flyway-style naming already used, `V1__init_schema.sql`) so every environment applies the same ordered set.

**Q58. Why `BIGSERIAL` for `run_id` but a `VARCHAR` for `workflow_id`/`job_id`?**
A: Run IDs are pure internal, high-volume, auto-incrementing identifiers with no external meaning — a sequence-backed integer is efficient. Workflow/job IDs are meant to be human-assigned, stable, referenceable identifiers (e.g., `"etl-daily"`), so a readable string key is more appropriate for a config-driven system.

**Q59. What would you change about the schema if `job_runs` grew to hundreds of millions of rows?**
A: Consider table partitioning by date range (e.g., monthly partitions) since queries are almost always time-bounded, and archiving/dropping old partitions instead of `DELETE` statements, which is far cheaper at that scale.

**Q60. Why did you choose connection pooling (HikariCP) instead of opening a new `Connection` per operation?**
A: Establishing a raw TCP+auth database connection is expensive relative to a query; pooling amortizes that cost and bounds the number of concurrent connections the app can open, protecting the database from being overwhelmed under load.

## 19.7 Algorithms & Complexity (Q61–Q70)

**Q61. Why Kahn's algorithm over DFS-based topological sort?**
A: Kahn's algorithm (BFS with indegree counting) naturally produces "levels" of jobs with no interdependency — directly usable for "run these in parallel," whereas DFS-based sort gives a single linear order that would need extra work to identify parallelizable batches.

**Q62. Walk through your cycle-detection algorithm.**
A: Iterative DFS with three states per node — white (unvisited), gray (on the current path), black (fully processed) — pushed/popped via an explicit `Deque`; encountering a gray node while exploring means a back-edge, i.e., a cycle, whose exact path I can reconstruct from the current stack contents.

**Q63. What's the time and space complexity of your DAG validation and topological sort?**
A: Both `O(V + E)` time; space is `O(V + E)` for the adjacency structure plus `O(V)` for indegree/visited tracking.

**Q64. How do you compute the next fire time for a cron expression efficiently?**
A: Field-wise candidate incrementing (start from "now," bump the minute field to the next valid value per the parsed expression, cascading up to hour/day/month/day-of-week as needed) bounded to a maximum lookahead window (e.g., 4 years) to avoid infinite loops on impossible combinations like Feb 30th.

**Q65. Explain your exponential backoff formula and why you cap it.**
A: `delay = min(maxDelay, initialDelay * multiplier^(attempt-1))` — capping prevents unbounded wait times for jobs with very high `maxAttempts`, keeping retry behavior predictable and bounded.

**Q66. What ranking heuristic does your `ShortestJobFirstStrategy` use, and what's its known weakness?**
A: It prioritizes ready jobs with the lowest historical average duration, approximating Shortest-Job-First scheduling to minimize average wait time; its known weakness (documented) is potential starvation of long-running jobs under sustained contention — a priority-aging mechanism would be the next improvement.

**Q67. What does `FanOutFirstStrategy` optimize for instead?**
A: It prioritizes jobs that unblock the most downstream dependents (highest out-degree in the remaining DAG), approximating a critical-path-first heuristic to unblock the widest set of future work as early as possible.

**Q68. How do you compute p95 duration in your reporting?**
A: Collect durations into a sorted list (or use a streaming approach for very large datasets), then index at `ceil(0.95 * n) - 1` — a standard nearest-rank percentile method; documented as an approximation, not a true continuous percentile.

**Q69. Why use `DoubleSummaryStatistics` from the Streams API instead of manual accumulation?**
A: It computes count, sum, min, max, and average in a single pass via `collect(Collectors.summarizingDouble(...))`, which is both more concise and less error-prone than hand-rolled accumulator variables.

**Q70. If a workflow had 100,000 jobs, what would break first, and how would you fix it?**
A: The in-memory `PriorityQueue`/DAG structures would still handle it algorithmically (`O(V+E)` scales), but a single-JVM worker pool would become the bottleneck — the fix is either scaling worker threads to available cores (already configurable) and/or moving to distributed execution across multiple TaskFlow nodes, which is explicitly future scope.

## 19.8 Exception Handling & Reliability (Q71–Q80)

**Q71. Why a custom exception hierarchy instead of just throwing `RuntimeException`?**
A: Distinct exception types (`CyclicWorkflowException`, `JobTimeoutException`, etc.) let calling code (and logs) distinguish *why* something failed and react differently (e.g., a timeout might warrant an automatic retry; a cyclic-workflow error should never be retried, only fixed by the operator).

**Q72. Checked vs. unchecked — which did you choose for your custom exceptions, and why?**
A: Unchecked (`RuntimeException`-based), because most failure modes here (cycle detected, job execution failure) represent programming/configuration errors or exceptional runtime conditions that the vast majority of call sites can't meaningfully recover from inline — forcing `throws` clauses everywhere would add noise without safety benefit; this decision is documented, including the counter-argument for checked exceptions.

**Q73. How do you ensure a `PreparedStatement`/`Connection`/`ResultSet` is always closed, even on exception?**
A: Try-with-resources on all three JDBC resource types, guaranteeing `close()` is called via the implicit `finally`, even if an exception propagates mid-query.

**Q74. What happens to a job that throws an exception not declared by `Job.execute`'s `throws` clause (e.g., a runtime NPE)?**
A: It's still caught by the surrounding executor code (catching `Throwable`/`Exception` broadly at that one boundary — the only place broad catching is acceptable, and documented as such) and converted into the same failure/retry pathway as a declared `JobExecutionException`.

**Q75. How do you avoid swallowing exceptions silently?**
A: Every catch block either rethrows (possibly wrapped), logs at `ERROR`/`WARN` with the original exception as the cause, or explicitly documents (in a comment) why it's intentionally ignored — an empty catch block is treated as a code-review-blocking smell.

**Q76. What's your strategy for jobs that fail non-deterministically (flaky external dependency)?**
A: Exactly why retry policies exist — `ExponentialBackoffRetryPolicy` gives transient failures room to resolve (e.g., a downstream API recovering) without hammering it immediately; permanent failures still eventually exhaust retries and surface as `FAILED` for operator attention.

**Q77. How do you distinguish a "real" failure from a timeout in your run history?**
A: Separate `JobStatus` values — `FAILED` (exception thrown) vs. `TIMED_OUT` (exceeded configured duration, thread interrupted) — stored distinctly so reporting/alerting can treat them differently (timeouts often indicate a *performance* regression, not a *logic* bug).

**Q78. What logging do you emit specifically to make debugging a failed job easy after the fact?**
A: MDC-tagged (`jobId`, `runId`, `workflowId`) log lines at each lifecycle transition, plus the persisted `error_message`/`output_summary` columns in `job_runs`, so a failure can be traced end-to-end from logs *and* the database without needing to reproduce it live.

**Q79. How do you handle a job whose retry policy would retry forever (misconfigured `maxAttempts`)?**
A: `ScheduleSpecValidator`/config validation rejects nonsensical configurations (e.g., `maxAttempts <= 0` where retries are configured) at workflow-registration time rather than at runtime, failing fast with a clear error instead of silently misbehaving in production.

**Q80. What would you add to make this more resilient in production (beyond current scope)?**
A: A dead-letter mechanism for permanently failed runs (surfacing them to a separate reviewable queue), circuit-breaking around consistently-failing jobs (auto-pause after N consecutive failures), and buffered/retryable persistence writes during transient DB outages — all explicitly listed in "Future Scope."

## 19.9 Testing & Code Quality (Q81–Q90)

**Q81. How did you make concurrency tests deterministic instead of flaky?**
A: By using `CountDownLatch`/`CyclicBarrier` to force specific thread interleavings and `AtomicInteger`/`AtomicBoolean` counters to detect violations directly, instead of relying on `Thread.sleep` timing, which is both slow and inherently racy.

**Q82. Give an example of a test that would catch a regression in your retry logic.**
A: A test asserting the *exact sequence* of delays produced by `ExponentialBackoffRetryPolicy.nextDelay(1..5)` against hand-computed expected values, and a `SchedulerEngineIntegrationTest` asserting a job configured for 3 attempts is invoked exactly 3 times before being marked `FAILED`.

**Q83. Why mock the repository in `SchedulingServiceTest` but use a real H2 database in `JdbcJobRunRepositoryTest`?**
A: `SchedulingServiceTest` is testing orchestration logic, not SQL — mocking the repository isolates that logic and keeps the test fast; `JdbcJobRunRepositoryTest` exists specifically to verify the SQL/mapping code itself, which requires a real (if in-memory) database engine.

**Q84. How do you avoid over-mocking?**
A: I mock only true external collaborators at architectural boundaries (repositories, event bus, clock) — I never mock simple immutable value objects, and I never mock the very class under test.

**Q85. What edge cases did you specifically write tests for around pagination?**
A: Page size larger than total rows, an empty result set, and the last (partial) page — the three classic pagination boundary conditions.

**Q86. How do you measure and enforce test coverage?**
A: JaCoCo Maven plugin generates line-coverage reports and its `check` goal fails the build if `core`/`scheduler` drop below the 80% threshold defined in NFR-03, so coverage regressions are caught in CI, not discovered later.

**Q87. What's a code smell you actively refactored away during this project?**
A: An early version of `SchedulerEngine` had a growing `if/else` chain dispatching on retry-policy *type*; refactoring to the `RetryPolicy` Strategy interface eliminated the branching entirely and made adding new policies additive rather than invasive.

**Q88. Why avoid wildcard imports and enforce a line-length limit?**
A: Wildcard imports obscure exactly which classes are in use and risk silent name clashes as dependencies grow; a line-length limit keeps diffs and side-by-side code review readable.

**Q89. How do you decide when a class has become a "god class" needing to be split?**
A: A rough heuristic of ~300–400 lines or "does this class have more than one clear reason to change" — e.g., if `SchedulerEngine` started also formatting reports, that reporting responsibility should be extracted immediately.

**Q90. What's your policy on comments, and why?**
A: Comments explain *why* a non-obvious decision was made (e.g., "iterative DFS to avoid stack overflow on large graphs"), never restate *what* the code already clearly says — redundant comments rot and mislead as code changes.

## 19.10 Project Decisions, Trade-offs & Behavioral (Q91–Q100)

**Q91. What was the hardest bug you hit building this, and how did you find it?**
A: (Answer from your actual experience — likely a concurrency race in early overlap-prevention logic, found by writing the `JobLockRegistryConcurrencyTest` with an `AtomicInteger` concurrent-access counter that failed intermittently until the locking scope was corrected.)

**Q92. If you had to cut scope to ship in half the time, what would you cut first, and why?**
A: The embedded HTTP status API and the priority-ranking strategies — both are valuable but additive; the DAG algorithms, threading model, and persistence layer are the non-negotiable technical core the rest of the resume value depends on.

**Q93. What would you do differently if you rebuilt this from scratch?**
A: (Genuine reflection prompt — good candidates name something concrete, e.g., "I'd introduce the `Clock` abstraction for testable time in Phase 1 instead of retrofitting it later, since several tests needed reworking once I realized real wall-clock time made them flaky.")

**Q94. Why did you avoid Spring/an ORM/a CLI library — doesn't that make the project less "production-realistic"?**
A: The explicit goal was demonstrating fundamentals for interviews and resume signal, not replicating what a team would choose day one in production; `ARCHITECTURE.md` documents exactly where I'd swap in Spring/an ORM/Jackson in a real team setting and why, showing I understand the trade-off rather than being unaware of it.

**Q95. How does this project show you can work with ambiguity?**
A: The original brief only said "build something impressive using these Java concepts" — I ran a structured comparative evaluation of 10 project ideas against 10 weighted criteria before committing, rather than picking the first idea that came to mind.

**Q96. What's a decision you made that you're not 100% sure was correct, and why?**
A: Choosing `READ_COMMITTED` isolation without stress-testing concurrent writers at scale — it's a reasonable default, but I've documented it as an assumption I'd want to load-test before trusting in a high-write-throughput production deployment.

**Q97. How would you explain this project to a non-technical recruiter in one sentence?**
A: "It's a tool that automatically runs a sequence of dependent tasks — like a mini version of the systems that run nightly data pipelines at companies — reliably, in parallel where possible, and with automatic retries when something fails."

**Q98. What did you learn about concurrency that you didn't know before this project?**
A: (Personalize — a strong answer names something specific and true, e.g., "That `Thread.sleep`-based tests for concurrent code are almost always a trap, and that separating 'what should happen' from 'when it's safe to check' via latches/barriers is the actual fix.")

**Q99. How is this project extensible for a future employer's use case?**
A: New job types are just new `Job` implementations; new notification channels are just new `EventListener`s; new scheduling heuristics are just new `JobPriorityStrategy` implementations — none require touching the scheduler core, which is exactly the extensibility a real team would need.

**Q100. Why should this project make us more confident in hiring you as a backend/Java engineer than someone with only CRUD-app projects on their resume?**
A: Because it demonstrates I can reason about concurrency correctness, design and defend a layered architecture, write and justify graph algorithms, hand-roll persistence with correct transaction boundaries, and make and document real engineering trade-offs — the actual day-to-day substance of backend engineering work, not just wiring a form to a database table.


---

# PART 20 — AI CODING AGENT OPERATING RULES (RECAP & ENFORCEMENT)

The building agent must follow these rules for the entire duration of the project, without exception:

1. **Never skip planning.** Before writing any class in a new phase, restate (in a short comment block or `DECISIONS.md` entry) what the phase's objective is and which classes will be created, matching Part 9.
2. **Always explain decisions.** Any deviation from this document, any new third-party dependency, and any non-obvious trade-off must be recorded in `DECISIONS.md` with a one-paragraph rationale.
3. **Always write scalable, non-duplicated code.** If similar logic appears in two places, extract a shared abstraction before proceeding.
4. **Always follow SOLID** as detailed in Part 4.2 and Part 10.
5. **Always use precise, intention-revealing naming** per Part 15.
6. **Always document code.** Every `public` class/method in `api`, `core`, `scheduler`, `service` requires complete JavaDoc before a phase is considered done.
7. **Always generate UML/diagrams where useful** — at minimum the component diagram, sequence diagram, and ER diagram from Parts 6–7 belong in `docs/`, kept current as the design evolves.
8. **Always update `README.md`** after each phase: mark completed features, update the architecture diagram if changed, keep the quick-start instructions accurate.
9. **Always explain why every non-trivial design choice was made** — this document's own justification style (see Parts 1.3, 7.1, 10) is the expected standard for all future decisions the agent makes that aren't explicitly dictated here.
10. **Do not build a UI, do not adopt Spring/an ORM, do not attempt multi-node distribution** — these are explicit non-goals (Part 2.3) unless the human developer explicitly asks to expand scope after v1.0.0 is complete.
11. **Do not proceed to the next phase** until the current phase compiles, its tests pass, coverage targets for touched packages are met, and the phase's commit + tag (where applicable) is made.
12. **When in doubt about an ambiguous requirement, choose the option most consistent with the mandatory Java-topic coverage map in Part 4** — this document exists precisely so the agent never needs to ask the human for clarification on scope; if a genuine contradiction is found within this document, log it in `DECISIONS.md`, make the most reasonable resolution, and continue.

---

## FINAL NOTE FOR THE HUMAN DEVELOPER

This document is intentionally exhaustive so that an AI coding agent (or you, working manually) can build TaskFlow phase-by-phase without needing to re-derive scope, architecture, or rationale at each step. Hand this file to your coding agent as its system/task context, and instruct it to proceed with **Phase 0** first. Revisit Part 18 and Part 19 once the project is complete and fresh in your memory — rehearse the 2-minute pitch out loud, and pressure-test your own answers to the 100 interview questions against your *actual* implementation choices, since interviewers will probe exactly these seams.

**Good luck — this project, built to the standard described above, is genuinely resume- and interview-differentiating for a 4th-year Software/Backend Engineer candidate.**
