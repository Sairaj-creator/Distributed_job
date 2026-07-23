import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useWorkflow } from "@/hooks/useWorkflows";
import { fetchJobRuns } from "@/api/reports";
import { DagCanvas } from "@/components/dag/DagCanvas";
import { ExecutionGanttChart } from "@/components/workflow/ExecutionGanttChart";
import { Card, CardContent } from "@/components/common/Card";
import { Badge } from "@/components/common/Badge";
import { ErrorState } from "@/components/common/ErrorState";
import { Skeleton } from "@/components/common/Skeleton";
import { ArrowLeft, History, Clock } from "lucide-react";

export function WorkflowDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: workflow, isPending, error, refetch } = useWorkflow(id);
  const [activeTab, setActiveTab] = useState<"overview" | "dag" | "history" | "timeline">("overview");

  const { data: workflowRuns = [] } = useQuery({
    queryKey: ["workflowJobRuns", id],
    queryFn: () => fetchJobRuns(id),
    refetchInterval: 4000,
  });

  if (isPending) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-64" />
        <Card><CardContent className="h-64 flex items-center justify-center"><Skeleton className="h-8 w-32" /></CardContent></Card>
      </div>
    );
  }

  if (error || !workflow) {
    return <ErrorState message={error?.message || "Workflow not found"} onRetry={() => refetch()} className="mt-8" />;
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-500 h-[calc(100vh-4rem)] flex flex-col">
      <div className="flex flex-col gap-2 shrink-0">
        <Link to="/workflows" className="inline-flex items-center text-sm text-zinc-500 hover:text-zinc-300 transition-colors">
          <ArrowLeft size={16} className="mr-1" /> Back to workflows
        </Link>
        <div className="flex items-end justify-between">
          <div>
            <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">{workflow.name}</h1>
            <p className="text-zinc-500 text-sm mt-1 max-w-2xl">{workflow.description}</p>
          </div>
        </div>
      </div>

      <div className="flex border-b border-border shrink-0">
        {(["overview", "dag", "history", "timeline"] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
              activeTab === tab
                ? "border-accent text-accent"
                : "border-transparent text-zinc-400 hover:text-zinc-200"
            }`}
          >
            <span className="capitalize">{tab}</span>
          </button>
        ))}
      </div>

      <div className="flex-1 min-h-0 overflow-hidden relative">
        {activeTab === "overview" && (
          <div className="h-full overflow-auto space-y-6 pb-6">
            <Card>
              <CardContent className="p-6 space-y-4">
                <h3 className="font-medium text-zinc-200">Configuration</h3>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-zinc-500 block mb-1">Workflow ID</span>
                    <span className="font-mono text-zinc-300">{workflow.workflowId}</span>
                  </div>
                  <div>
                    <span className="text-zinc-500 block mb-1">Total Jobs</span>
                    <span className="text-zinc-300">{workflow.jobs.length}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {activeTab === "dag" && (
          <div className="absolute inset-0">
            <DagCanvas workflow={workflow} />
          </div>
        )}

        {activeTab === "history" && (
          <div className="h-full overflow-auto p-4">
            <Card className="p-6">
              <h3 className="text-lg font-semibold text-zinc-100 mb-4 flex items-center gap-2">
                <History className="h-5 w-5 text-zinc-400" />
                Workflow Execution History ({workflowRuns.length})
              </h3>
              {workflowRuns.length > 0 ? (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm text-zinc-300">
                    <thead className="border-b border-border bg-surface-hover/50 text-xs font-semibold uppercase text-zinc-400">
                      <tr>
                        <th className="px-4 py-3">Run ID</th>
                        <th className="px-4 py-3">Job ID</th>
                        <th className="px-4 py-3">Attempt</th>
                        <th className="px-4 py-3">Status</th>
                        <th className="px-4 py-3">Started At</th>
                        <th className="px-4 py-3">Finished At</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border/50">
                      {workflowRuns.map((run) => (
                        <tr key={`${run.runId}-${run.jobId}-${run.attemptNumber}`} className="hover:bg-surface-hover/30 transition-colors">
                          <td className="px-4 py-3 font-mono text-xs text-zinc-400">#{run.runId}</td>
                          <td className="px-4 py-3 font-mono font-medium text-zinc-200">{run.jobId}</td>
                          <td className="px-4 py-3 text-zinc-400">#{run.attemptNumber}</td>
                          <td className="px-4 py-3"><Badge status={run.status} /></td>
                          <td className="px-4 py-3 text-xs text-zinc-400">{formatDate(run.startedAt)}</td>
                          <td className="px-4 py-3 text-xs text-zinc-400">{formatDate(run.finishedAt)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div className="py-8 text-center text-zinc-400">No execution runs recorded for this workflow yet.</div>
              )}
            </Card>
          </div>
        )}

        {activeTab === "timeline" && (
          <div className="h-full overflow-auto p-4">
            <Card className="p-6">
              <h3 className="text-lg font-semibold text-zinc-100 mb-4 flex items-center gap-2">
                <Clock className="h-5 w-5 text-zinc-400" />
                Execution Gantt Timeline
              </h3>
              <ExecutionGanttChart runs={workflowRuns} />
            </Card>
          </div>
        )}
      </div>
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
