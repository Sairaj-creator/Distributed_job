// Types marked with `/* assumed */` are not backed by a confirmed backend response today.
// Grep for "assumed" to find every field that needs to be validated against the real API
// before this app is fully wired up. See src/api/*.ts TODO comments for the matching stubs.

export type JobStatus =
  | "SUCCEEDED"
  | "FAILED"
  | "RUNNING"
  | "SCHEDULED"
  | "QUEUED" /* assumed */
  | "RETRY" /* assumed */
  | "NO_RUNS";

// ---- Confirmed shapes (from GET /status) ----------------------------------

export interface DashboardSummary {
  totalWorkflows: number;
  totalJobs: number;
  runningJobs: number;
  successRate: number; // 0..1, 4 decimal places from backend
}

export interface WorkflowStatus {
  workflowId: string;
  name: string;
  jobCount: number;
  paused: boolean;
  scheduleType: string;
  scheduleSpec: string;
}

export interface StatusResponse {
  summary: DashboardSummary;
  workflows: WorkflowStatus[];
}

// ---- Confirmed shape (from GET /workflows/{id}) ---------------------------

export interface WorkflowJob {
  jobId: string;
  name: string;
  lastStatus: JobStatus;
  dependsOn: string[];
}

export interface WorkflowDetail {
  workflowId: string;
  name: string;
  description: string;
  jobs: WorkflowJob[];
}

// ---- Not backed by any endpoint yet (UI-only placeholders) ----------------
// These exist so components have a stable shape to render against once the
// real backend work lands. Nothing in src/api ever fabricates these values.

export interface WorkerNode /* assumed */ {
  workerId: string;
  cpuPercent: number;
  memPercent: number;
  heartbeatAt: string;
  assignedJobCount: number;
}

export interface JobQueueItem /* assumed */ {
  jobId: string;
  workflowId: string;
  status: JobStatus;
  workerId: string | null;
  durationMs: number | null;
  retryAttempts: number;
  startedAt: string | null;
  endedAt: string | null;
}

export interface SchedulerHealth /* assumed */ {
  uptimeSeconds: number;
  lastTickAt: string;
  dbConnected: boolean;
}

export interface MetricPoint /* assumed */ {
  timestamp: string;
  value: number;
}

export interface ApiError {
  status: number | null; // null = network error, no response received
  message: string;
  kind: "network" | "client" | "server" | "unknown";
}
