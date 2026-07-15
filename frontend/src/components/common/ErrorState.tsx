import { AlertTriangle, RefreshCw } from "lucide-react";
import { cn } from "@/utils/cn";

interface ErrorStateProps {
  title?: string;
  message: string;
  onRetry?: () => void;
  className?: string;
}

export function ErrorState({ title = "Error", message, onRetry, className }: ErrorStateProps) {
  return (
    <div className={cn("flex flex-col items-center justify-center py-12 px-4 text-center rounded-lg border border-status-failed/20 bg-status-failed/5", className)}>
      <AlertTriangle className="text-status-failed mb-3" size={32} />
      <h3 className="text-base font-medium text-status-failed mb-1">{title}</h3>
      <p className="text-sm text-zinc-400 mb-4 max-w-md">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium bg-raised hover:bg-border transition-colors rounded-md text-zinc-200"
        >
          <RefreshCw size={16} />
          Try Again
        </button>
      )}
    </div>
  );
}
