<div align="center">
  <h1>🚀 TaskFlow</h1>
  <p><strong>A production-styled, high-performance Java 17 Workflow Orchestration Engine.</strong></p>
  
  [![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
  [![Maven](https://img.shields.io/badge/build-Maven-blue.svg)](https://maven.apache.org/)
  [![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
</div>

<br/>

**TaskFlow** is a robust backend orchestration engine built in core Java. It allows you to define complex background workflows as Directed Acyclic Graphs (DAGs), validate dependencies, compute parallel execution levels, and run jobs on bounded worker pools.

Say goodbye to manual chron jobs and complex orchestrators! TaskFlow brings retry policies, lifecycle events, JDBC persistence, and a built-in CLI/HTTP API—all without the bloat of Spring or massive ORMs.

## ✨ Key Features

- 🏗️ **Immutable Domain Model:** Built with `Job`, `Workflow`, `JobRun`, and behavior-rich value objects.
- 🔗 **Advanced DAG Validation:** Iterative DFS and topological sorting via Kahn's algorithm.
- 🕒 **Smart Scheduling:** Simplified 5-field cron parser and next-fire calculation.
- ⚡ **High Concurrency:** Bounded worker execution pools with timeouts, cancellation, overlap locks, and priority queues.
- 📡 **Event-Driven Architecture:** Async event bus with console, SLF4J, and metrics listeners.
- 💾 **JDBC Persistence:** Highly optimized database interactions with dynamic filtering & pagination.
- 🛠️ **CLI & HTTP API:** Embedded JDK `HttpServer` and command-line interfaces for easy interaction.

## 🚀 Quick Start

Ensure you have **Java 17** and **Maven** installed, then clone the repository:

```bash
git clone https://github.com/Sairaj-creator/Distributed_job.git
cd Distributed_job
```

### 1. Configure the Environment
Copy the example environment file and update the PostgreSQL connection details if necessary:
```bash
cp .env.example .env
```
*(Note: By default, the engine connects to a local PostgreSQL instance.)*

### 2. Build & Test
```bash
mvn clean verify
```

### 3. Run the Engine
To start the background engine and the HTTP API:
```bash
mvn -q exec:java
```

Once running, the HTTP API listens on `http://localhost:8080/status`. You can also use CLI arguments in a separate terminal to view statistics:
```bash
mvn -q exec:java -Dexec.args="stats"
```

## 🏗️ Architecture
TaskFlow is designed to be completely framework-agnostic. Check out our detailed [Architecture Guide](ARCHITECTURE.md) for sequence diagrams and design decisions.

## 📜 Progress Roadmap
- [x] Phase 0-4: Domain models, DAG algorithms, Cron parsing, Concurrency
- [x] Phase 5-8: Event bus, JDBC, Service layers, CLI & HTTP API
- [x] Phase 9-10: Config, bootstrap wiring, tests & documentation

## 🤝 Contributing
Contributions are always welcome! Feel free to open an issue or submit a pull request.

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
