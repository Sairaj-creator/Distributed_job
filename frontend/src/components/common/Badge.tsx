import { cn } from "@/utils/cn";
import type { JobStatus } from "@/types";

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  status: JobStatus;
}

export function Badge({ status, className, ...props }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border",
        {
          "bg-status-succeeded/10 text-status-succeeded border-status-succeeded/20": status === "SUCCEEDED",
          "bg-status-failed/10 text-status-failed border-status-failed/20": status === "FAILED",
          "bg-status-running/10 text-status-running border-status-running/20": status === "RUNNING",
          "bg-status-retrying/10 text-status-retrying border-status-retrying/20": status === "RETRY",
          "bg-status-queued/10 text-status-queued border-status-queued/20": status === "QUEUED" || status === "SCHEDULED" || status === "NO_RUNS",
        },
        className
      )}
      {...props}
    >
      {status}
    </span>
  );
}
