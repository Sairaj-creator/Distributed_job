import type { JobRunRecord } from "@/types";
import { Badge } from "@/components/common/Badge";
import { Clock, AlertCircle } from "lucide-react";

interface ExecutionGanttChartProps {
  runs: JobRunRecord[];
}

interface WorkflowRunGroup {
  workflowRunId: number;
  runs: JobRunRecord[];
  minStart: number;
  maxEnd: number;
  totalDurationMs: number;
}

export function ExecutionGanttChart({ runs }: ExecutionGanttChartProps) {
  if (!runs || runs.length === 0) {
    return (
      <div className="py-8 text-center text-zinc-400">
        No execution runs recorded for this workflow yet.
      </div>
    );
  }

  // Group runs by workflowRunId
  const groupedMap = new Map<number, JobRunRecord[]>();
  runs.forEach((run) => {
    const list = groupedMap.get(run.workflowRunId) || [];
    list.push(run);
    groupedMap.set(run.workflowRunId, list);
  });

  const now = Date.now();

  const groups: WorkflowRunGroup[] = Array.from(groupedMap.entries())
    .map(([workflowRunId, groupRuns]) => {
      const startTimes = groupRuns
        .map((r) => (r.startedAt ? new Date(r.startedAt).getTime() : 0))
        .filter((t) => t > 0);
      const minStart = startTimes.length > 0 ? Math.min(...startTimes) : now;

      const endTimes = groupRuns.map((r) => {
        if (r.finishedAt) return new Date(r.finishedAt).getTime();
        if (r.status === "RUNNING") return now;
        return r.startedAt ? new Date(r.startedAt).getTime() + 1000 : now;
      });
      const maxEnd = endTimes.length > 0 ? Math.max(...endTimes) : minStart + 1000;
      const totalDurationMs = Math.max(1, maxEnd - minStart);

      return {
        workflowRunId,
        runs: groupRuns.sort((a, b) => {
          const tA = a.startedAt ? new Date(a.startedAt).getTime() : 0;
          const tB = b.startedAt ? new Date(b.startedAt).getTime() : 0;
          return tA - tB;
        }),
        minStart,
        maxEnd,
        totalDurationMs,
      };
    })
    .sort((a, b) => b.workflowRunId - a.workflowRunId);

  return (
    <div className="space-y-6">
      {groups.map((group) => (
        <div key={group.workflowRunId} className="rounded-lg border border-border bg-surface p-4 space-y-3">
          <div className="flex items-center justify-between border-b border-border/60 pb-2">
            <div className="flex items-center gap-2">
              <span className="font-mono text-sm font-semibold text-zinc-100">
                Workflow Run #{group.workflowRunId}
              </span>
              <span className="text-xs text-zinc-500">
                ({group.runs.length} {group.runs.length === 1 ? "job" : "jobs"})
              </span>
            </div>
            <div className="text-xs text-zinc-400 font-mono flex items-center gap-1">
              <Clock className="h-3.5 w-3.5 text-zinc-500" />
              Total Span: {formatDuration(group.totalDurationMs)}
            </div>
          </div>

          <div className="space-y-2">
            {group.runs.map((run) => {
              const startMs = run.startedAt ? new Date(run.startedAt).getTime() : group.minStart;
              const endMs = run.finishedAt
                ? new Date(run.finishedAt).getTime()
                : run.status === "RUNNING"
                ? now
                : startMs + 1000;

              const durationMs = Math.max(0, endMs - startMs);
              const leftPercent = Math.max(
                0,
                Math.min(100, ((startMs - group.minStart) / group.totalDurationMs) * 100)
              );
              const widthPercent = Math.max(
                3,
                Math.min(100 - leftPercent, (durationMs / group.totalDurationMs) * 100)
              );

              return (
                <div
                  key={`${run.runId}-${run.jobId}-${run.attemptNumber}`}
                  className="grid grid-cols-12 items-center gap-3 text-xs py-1"
                >
                  <div className="col-span-3 font-mono font-medium text-zinc-200 truncate flex items-center gap-2">
                    <Badge status={run.status} />
                    <span className="truncate">{run.jobId}</span>
                  </div>

                  <div className="col-span-7 relative h-7 bg-zinc-950/60 rounded border border-zinc-800/80 overflow-hidden flex items-center px-1">
                    <div
                      className={`absolute top-1 bottom-1 rounded transition-all duration-300 ${getBarColor(
                        run.status
                      )}`}
                      style={{
                        left: `${leftPercent}%`,
                        width: `${widthPercent}%`,
                      }}
                    >
                      {run.status === "RUNNING" && (
                        <div className="absolute inset-0 bg-white/20 animate-pulse rounded" />
                      )}
                    </div>
                    <span className="relative z-10 font-mono text-[10px] text-zinc-300 ml-2 font-semibold">
                      {formatDuration(durationMs)}
                    </span>
                  </div>

                  <div className="col-span-2 text-right font-mono text-zinc-500 text-[11px] truncate">
                    {run.errorMessage ? (
                      <span className="text-rose-400 flex items-center justify-end gap-1" title={run.errorMessage}>
                        <AlertCircle className="h-3 w-3 shrink-0" /> Failed
                      </span>
                    ) : (
                      `Attempt #${run.attemptNumber}`
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </div>
  );
}

function getBarColor(status: string): string {
  switch (status) {
    case "SUCCEEDED":
      return "bg-emerald-500/80 border border-emerald-400";
    case "FAILED":
      return "bg-rose-500/80 border border-rose-400";
    case "RUNNING":
      return "bg-amber-500/80 border border-amber-400";
    default:
      return "bg-zinc-600/80 border border-zinc-500";
  }
}

function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`;
  const sec = (ms / 1000).toFixed(1);
  return `${sec}s`;
}
