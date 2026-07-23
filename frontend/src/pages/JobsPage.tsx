import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchJobRuns } from "@/api/reports";
import { Card } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { Skeleton } from "@/components/common/Skeleton";
import { ErrorState } from "@/components/common/ErrorState";
import { ListFilter, Search, History } from "lucide-react";
import type { JobStatus } from "@/types";

export function JobsPage() {
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [searchTerm, setSearchTerm] = useState<string>("");

  const {
    data: runs,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["jobRuns"],
    queryFn: fetchJobRuns,
    refetchInterval: 4000,
  });

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-48" />
        <Card className="p-6 h-64 flex items-center justify-center">
          <Skeleton className="h-12 w-12 rounded-full" />
        </Card>
      </div>
    );
  }

  if (error) {
    return (
      <ErrorState
        title="Failed to load job execution history"
        message={error instanceof Error ? error.message : "Unknown error"}
        onRetry={() => refetch()}
      />
    );
  }

  const filteredRuns = (runs ?? []).filter((run) => {
    const matchesStatus = statusFilter === "ALL" || run.status === statusFilter;
    const matchesSearch =
      searchTerm === "" ||
      run.jobId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      run.workflowId.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesStatus && matchesSearch;
  });

  const statuses: ("ALL" | JobStatus)[] = ["ALL", "SUCCEEDED", "FAILED", "RUNNING"];

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">Job Execution History</h1>
        <p className="text-sm text-zinc-400 mt-1">
          Historical log of individual job attempts across all workflows.
        </p>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div className="flex items-center gap-1.5 bg-surface border border-border p-1 rounded-lg">
          {statuses.map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors ${
                statusFilter === st
                  ? "bg-zinc-800 text-zinc-100 shadow-sm"
                  : "text-zinc-400 hover:text-zinc-200"
              }`}
            >
              {st}
            </button>
          ))}
        </div>

        <div className="relative w-full sm:w-64">
          <Search className="absolute left-3 top-2.5 h-4 w-4 text-zinc-400" />
          <input
            type="text"
            placeholder="Search job or workflow ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-surface border border-border rounded-lg pl-9 pr-3 py-2 text-xs text-zinc-100 placeholder-zinc-500 focus:outline-none focus:ring-1 focus:ring-zinc-600"
          />
        </div>
      </div>

      {/* Execution Table */}
      <Card className="p-6">
        <h2 className="text-lg font-semibold text-zinc-100 mb-4 flex items-center gap-2">
          <History className="h-5 w-5 text-zinc-400" />
          Job Runs ({filteredRuns.length})
        </h2>

        {filteredRuns.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-zinc-300">
              <thead className="border-b border-border bg-surface-hover/50 text-xs font-semibold uppercase text-zinc-400">
                <tr>
                  <th className="px-4 py-3">Run ID</th>
                  <th className="px-4 py-3">Job ID</th>
                  <th className="px-4 py-3">Workflow ID</th>
                  <th className="px-4 py-3">Attempt</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Started At</th>
                  <th className="px-4 py-3">Finished At</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/50">
                {filteredRuns.map((run) => (
                  <tr key={`${run.runId}-${run.jobId}-${run.attemptNumber}`} className="hover:bg-surface-hover/30 transition-colors">
                    <td className="px-4 py-3 font-mono text-xs text-zinc-400">#{run.runId}</td>
                    <td className="px-4 py-3 font-mono font-medium text-zinc-200">{run.jobId}</td>
                    <td className="px-4 py-3 font-mono text-zinc-400">{run.workflowId}</td>
                    <td className="px-4 py-3 text-zinc-400">#{run.attemptNumber}</td>
                    <td className="px-4 py-3">
                      <Badge status={run.status} />
                    </td>
                    <td className="px-4 py-3 text-xs text-zinc-400">{formatDate(run.startedAt)}</td>
                    <td className="px-4 py-3 text-xs text-zinc-400">{formatDate(run.finishedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="py-8 text-center text-zinc-400">No job execution runs match the selected criteria.</div>
        )}
      </Card>
    </div>
  );
}

function formatDate(iso: string) {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}
