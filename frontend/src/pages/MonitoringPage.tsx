import { useQuery } from "@tanstack/react-query";
import { fetchJobStats } from "@/api/reports";
import { fetchStatus } from "@/api/dashboard";
import { Card } from "@/components/common/Card";
import { Skeleton } from "@/components/common/Skeleton";
import { ErrorState } from "@/components/common/ErrorState";
import { Activity, Clock, CheckCircle2, AlertTriangle, Cpu, Server } from "lucide-react";

export function MonitoringPage() {
  const {
    data: stats,
    isLoading: isLoadingStats,
    error: statsError,
    refetch: refetchStats,
  } = useQuery({
    queryKey: ["jobStats"],
    queryFn: fetchJobStats,
    refetchInterval: 5000,
  });

  const {
    data: statusData,
    isLoading: isLoadingStatus,
  } = useQuery({
    queryKey: ["dashboardStatus"],
    queryFn: fetchStatus,
    refetchInterval: 5000,
  });

  if (isLoadingStats || isLoadingStatus) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Skeleton className="h-28" />
          <Skeleton className="h-28" />
          <Skeleton className="h-28" />
          <Skeleton className="h-28" />
        </div>
      </div>
    );
  }

  if (statsError) {
    return (
      <ErrorState
        title="Failed to load monitoring metrics"
        message={statsError instanceof Error ? statsError.message : "Unknown error"}
        onRetry={() => refetchStats()}
      />
    );
  }

  const summary = statusData?.summary;
  const totalRuns = stats?.reduce((acc, curr) => acc + curr.totalRuns, 0) ?? 0;
  const totalSucceeded = stats?.reduce((acc, curr) => acc + curr.succeeded, 0) ?? 0;
  const totalFailed = stats?.reduce((acc, curr) => acc + curr.failed, 0) ?? 0;

  const avgP95Ms = stats && stats.length > 0
    ? Math.round(stats.reduce((acc, curr) => acc + curr.p95DurationMs, 0) / stats.length)
    : 0;

  const isDisconnected = Boolean(statsError);
  const engineHealthStatus = isDisconnected ? "DEGRADED" : "HEALTHY";

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">System Telemetry & Performance</h1>
        <p className="text-sm text-zinc-400 mt-1">
          Real-time aggregated performance metrics, latency p95 percentiles, and scheduler health.
        </p>
      </div>

      {/* Top KPI Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card className="p-4 border-emerald-500/20 bg-emerald-950/10">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-zinc-400">Success Rate (7-Day)</span>
            <CheckCircle2 className="h-5 w-5 text-emerald-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-emerald-400">
            {summary ? (summary.successRate * 100).toFixed(1) : 100}%
          </div>
          <p className="mt-1 text-xs text-zinc-500">30-day: {totalSucceeded} succeeded / {totalFailed} failed</p>
        </Card>

        <Card className="p-4 border-blue-500/20 bg-blue-950/10">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-zinc-400">Average P95 Latency</span>
            <Clock className="h-5 w-5 text-blue-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-blue-400">{avgP95Ms} ms</div>
          <p className="mt-1 text-xs text-zinc-500">Calculated over past 30 days</p>
        </Card>

        <Card className="p-4 border-purple-500/20 bg-purple-950/10">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-zinc-400">Total Job Runs (30-Day)</span>
            <Activity className="h-5 w-5 text-purple-400" />
          </div>
          <div className="mt-2 text-2xl font-bold text-purple-400">{totalRuns}</div>
          <p className="mt-1 text-xs text-zinc-500">Across {summary?.totalWorkflows ?? 0} active workflows</p>
        </Card>

        <Card className="p-4 border-amber-500/20 bg-amber-950/10">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-zinc-400">Engine Health</span>
            <Server className={`h-5 w-5 ${isDisconnected ? "text-rose-400" : "text-amber-400"}`} />
          </div>
          <div className={`mt-2 text-2xl font-bold ${isDisconnected ? "text-rose-400" : "text-emerald-400"}`}>
            {engineHealthStatus}
          </div>
          <p className="mt-1 text-xs text-zinc-500">
            {isDisconnected ? "Backend API unreachable" : "Embedded JDK HttpServer on port 8081"}
          </p>
        </Card>
      </div>

      {/* Per-Job Stats Table */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-zinc-100 mb-4 flex items-center gap-2">
          <Cpu className="h-5 w-5 text-zinc-400" />
          Per-Job Aggregated Performance Metrics
        </h2>

        {stats && stats.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-zinc-300">
              <thead className="border-b border-border bg-surface-hover/50 text-xs font-semibold uppercase text-zinc-400">
                <tr>
                  <th className="px-4 py-3">Job ID</th>
                  <th className="px-4 py-3">Total Executions</th>
                  <th className="px-4 py-3">Success / Fail</th>
                  <th className="px-4 py-3">Success Rate</th>
                  <th className="px-4 py-3">Avg Duration</th>
                  <th className="px-4 py-3">p95 Latency</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/50">
                {stats.map((job) => {
                  const rate = (job.successRate * 100).toFixed(1);
                  return (
                    <tr key={job.jobId} className="hover:bg-surface-hover/30 transition-colors">
                      <td className="px-4 py-3 font-mono font-medium text-zinc-200">{job.jobId}</td>
                      <td className="px-4 py-3 text-zinc-300">{job.totalRuns}</td>
                      <td className="px-4 py-3">
                        <span className="text-emerald-400">{job.succeeded}</span>
                        {" / "}
                        <span className={job.failed > 0 ? "text-rose-400 font-semibold" : "text-zinc-500"}>
                          {job.failed}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
                            job.successRate >= 0.9
                              ? "bg-emerald-500/10 text-emerald-400"
                              : "bg-rose-500/10 text-rose-400"
                          }`}
                        >
                          {rate}%
                        </span>
                      </td>
                      <td className="px-4 py-3 text-zinc-300">{job.averageDurationMs} ms</td>
                      <td className="px-4 py-3 font-mono text-blue-400">{job.p95DurationMs} ms</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="py-8 text-center text-zinc-400">No job stats recorded yet. Trigger a workflow to generate telemetry.</div>
        )}
      </Card>
    </div>
  );
}
