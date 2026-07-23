import { useDashboardSummary } from "@/hooks/useDashboardSummary";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/common/Card";
import { Skeleton } from "@/components/common/Skeleton";
import { ErrorState } from "@/components/common/ErrorState";
import { Activity, Play, CheckCircle, Percent, Zap } from "lucide-react";
import { Link } from "react-router-dom";
import { useWorkflows, useTriggerWorkflow } from "@/hooks/useWorkflows";
import { Badge } from "@/components/common/Badge";

export function DashboardPage() {
  const { data, isPending, error, refetch } = useDashboardSummary();
  const { data: workflows } = useWorkflows();
  const trigger = useTriggerWorkflow();

  if (isPending) {
    return (
      <div className="space-y-6">
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">Overview</h1>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Card key={i}><CardContent className="p-6"><Skeleton className="h-12 w-full" /></CardContent></Card>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error.message} onRetry={() => refetch()} className="mt-8" />;
  }

  const { totalWorkflows, totalJobs, runningJobs, successRate } = data!.summary;

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">Overview</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <StatCard title="Total Workflows" value={totalWorkflows} icon={<Activity size={18} className="text-zinc-400" />} />
        <StatCard title="Total Jobs" value={totalJobs} icon={<CheckCircle size={18} className="text-zinc-400" />} />
        <StatCard title="Running Jobs" value={runningJobs} icon={<Play size={18} className="text-status-running" />} valueClassName={runningJobs > 0 ? "text-status-running" : ""} />
        <StatCard title="Success Rate" value={`${(successRate * 100).toFixed(1)}%`} icon={<Percent size={18} className="text-status-succeeded" />} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Recent Workflows</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            {workflows && workflows.length > 0 ? (
              <div className="divide-y divide-border">
                {workflows.slice(0, 5).map((wf) => (
                  <div key={wf.workflowId} className="flex items-center justify-between px-6 py-4 hover:bg-raised/50 transition-colors">
                    <div>
                      <Link to={`/workflows/${wf.workflowId}`} className="text-sm font-medium text-accent hover:underline">
                        {wf.name}
                      </Link>
                      <div className="text-xs text-zinc-500 mt-1">{wf.jobCount} jobs</div>
                    </div>
                    <div className="flex items-center gap-3">
                      <Badge status={wf.paused ? "paused" : "running" as any} />
                      <button
                        onClick={() => trigger.mutate(wf.workflowId)}
                        disabled={trigger.isPending}
                        className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-amber-400 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 rounded transition-colors disabled:opacity-50"
                        title="Trigger Execution"
                      >
                        <Zap size={13} />
                        Trigger
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="p-6 text-sm text-zinc-500">No workflows available.</div>
            )}
            <div className="px-6 py-4 border-t border-border bg-raised/30">
              <Link to="/workflows" className="text-sm text-zinc-400 hover:text-zinc-200 transition-colors">
                View all workflows &rarr;
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function StatCard({ title, value, icon, valueClassName }: { title: string; value: string | number; icon: React.ReactNode; valueClassName?: string }) {
  return (
    <Card>
      <CardContent className="p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-medium text-zinc-400 tracking-tight">{title}</h3>
          {icon}
        </div>
        <div className={`text-3xl font-semibold text-zinc-100 ${valueClassName || ""}`}>{value}</div>
      </CardContent>
    </Card>
  );
}
