import { useWorkflows, useTriggerWorkflow, usePauseWorkflow, useResumeWorkflow } from "@/hooks/useWorkflows";
import { Card, CardContent } from "@/components/common/Card";
import { Skeleton } from "@/components/common/Skeleton";
import { ErrorState } from "@/components/common/ErrorState";
import { Link } from "react-router-dom";
import { Badge } from "@/components/common/Badge";
import { Play, Pause, Zap } from "lucide-react";

export function WorkflowsListPage() {
  const { data: workflows, isPending, error, refetch } = useWorkflows();
  const trigger = useTriggerWorkflow();
  const pause = usePauseWorkflow();
  const resume = useResumeWorkflow();

  if (isPending) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">Workflows</h1>
        <div className="space-y-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i}><CardContent className="p-6"><Skeleton className="h-8 w-full" /></CardContent></Card>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={() => refetch()} className="mt-8" />;
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">Workflows</h1>
        <span className="text-sm text-zinc-500 bg-raised px-3 py-1 rounded-full">{workflows?.length || 0} total</span>
      </div>

      <div className="bg-surface border border-border rounded-lg overflow-hidden">
        <table className="w-full text-sm text-left">
          <thead className="text-xs text-zinc-400 uppercase bg-raised/50 border-b border-border">
            <tr>
              <th className="px-6 py-4 font-medium">Name</th>
              <th className="px-6 py-4 font-medium">Schedule</th>
              <th className="px-6 py-4 font-medium">Status</th>
              <th className="px-6 py-4 font-medium">Jobs</th>
              <th className="px-6 py-4 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {workflows?.map((wf) => (
              <tr key={wf.workflowId} className="hover:bg-raised/30 transition-colors group">
                <td className="px-6 py-4 font-medium text-zinc-200">
                  <Link to={`/workflows/${wf.workflowId}`} className="text-accent hover:underline">
                    {wf.name}
                  </Link>
                  <div className="text-xs font-mono text-zinc-500 mt-1">{wf.workflowId}</div>
                </td>
                <td className="px-6 py-4 text-zinc-400">
                  <div className="flex items-center gap-2">
                    <span className="capitalize">{wf.scheduleType.toLowerCase()}</span>
                    {wf.scheduleSpec && <span className="bg-raised px-2 py-0.5 rounded text-xs font-mono">{wf.scheduleSpec}</span>}
                  </div>
                </td>
                <td className="px-6 py-4">
                  <Badge status={wf.paused ? "paused" : "running" as any} />
                </td>
                <td className="px-6 py-4 text-zinc-400">{wf.jobCount}</td>
                <td className="px-6 py-4 text-right">
                  <div className="flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => trigger.mutate(wf.workflowId)}
                      disabled={trigger.isPending}
                      className="p-1.5 text-zinc-400 hover:text-accent hover:bg-accent/10 rounded transition-colors"
                      title="Trigger Now"
                    >
                      <Zap size={16} />
                    </button>
                    {wf.paused ? (
                      <button
                        onClick={() => resume.mutate(wf.workflowId)}
                        disabled={resume.isPending}
                        className="p-1.5 text-zinc-400 hover:text-status-succeeded hover:bg-status-succeeded/10 rounded transition-colors"
                        title="Resume"
                      >
                        <Play size={16} />
                      </button>
                    ) : (
                      <button
                        onClick={() => pause.mutate(wf.workflowId)}
                        disabled={pause.isPending}
                        className="p-1.5 text-zinc-400 hover:text-status-retrying hover:bg-status-retrying/10 rounded transition-colors"
                        title="Pause"
                      >
                        <Pause size={16} />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {workflows?.length === 0 && (
              <tr>
                <td colSpan={5} className="px-6 py-12 text-center text-zinc-500">
                  No workflows configured.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
