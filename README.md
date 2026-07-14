# 🌊 TaskFlow

> **A lightweight, embedded Java workflow orchestration engine with a modern React dashboard.**

TaskFlow is a robust distributed job scheduling and workflow orchestration engine designed to run seamlessly inside your Java applications. With native PostgreSQL support, bulletproof concurrency locks, and an elegant real-time UI, TaskFlow manages complex DAG (Directed Acyclic Graph) job dependencies so you can focus on writing business logic.

---

## ✨ Features

- **Embedded Engine**: Runs directly within your Java application footprint without requiring heavy external orchestrators.
- **DAG Resolution**: Define complex workflows using Directed Acyclic Graphs, and TaskFlow will automatically resolve dependency chains.
- **Robust Persistence**: Safely orchestrate distributed environments using PostgreSQL `ON CONFLICT` semantics to avoid race conditions.
- **Resilient Execution**: Gracefully handles retries, configurable timeouts, and overlap policies (`SKIP`, `ENQUEUE`, `REPLACE`).
- **Modern Dashboard**: A real-time, glassmorphic React dashboard (Vite) that beautifully visualizes your active workflows, dependency chains, and KPI metrics.

---

## 🚀 Quick Start

### 1. Start the Engine (Backend)

By default, the backend will initialize the database schema and seed a demo `Nightly Analytics` workflow. It runs locally on port `8081`.

```bash
# Compile and start the embedded server
mvn clean compile exec:java
```

### 2. Start the Dashboard (Frontend)

The frontend is built with React and Vite. It connects to the backend API to provide a live view of your pipelines.

```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies and run the dev server
npm install
npm run dev
```

The dashboard will be available at [http://localhost:5173](http://localhost:5173).

---

## 🏗️ Architecture

### Java Core Engine
At its heart, TaskFlow leverages an embedded `HttpServer` and `HikariCP` connection pooling.
- **`StatusHttpApi`**: Exposes `/status` and `/workflows/{id}` endpoints for the frontend.
- **`DemoSeeder`**: Seeds a perfect 5-step linear pipeline demonstrating real-time statuses and DAG evaluation.
- **`JdbcWorkflowRepository`**: Handles robust state management against a real database using standard SQL patterns.

### React Dashboard
The frontend relies on standard modern web tooling.
- **Framework**: Vite + React
- **Styling**: Pure CSS with glassmorphic elements, modern gradients, and dynamic status-aware UI components.
- **Lucide Icons**: Clean, scalable vector graphics.

---

## 🎨 Visualization

TaskFlow elegantly renders workflows using color-coded nodes:
- 🔵 **RUNNING**: The job is currently executing.
- 🟢 **SUCCEEDED**: The job finished successfully.
- 🔴 **FAILED**: The job encountered an error.
- 🟠 **NO_RUNS / SCHEDULED**: The job is waiting in the queue.

---

## 🛠️ Configuration

Configure your environment easily by dropping a `.env` file in the root:
```env
TASKFLOW_HTTP_PORT=8081
```
