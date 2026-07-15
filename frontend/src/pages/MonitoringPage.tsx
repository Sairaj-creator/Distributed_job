import { EmptyState } from "@/components/common/EmptyState";
import { Activity } from "lucide-react";

export function MonitoringPage() {
  return (
    <div className="space-y-6 animate-in fade-in duration-500">
      <h1 className="text-2xl font-semibold tracking-tight text-zinc-100">Monitoring & Metrics</h1>
      <EmptyState
        icon={<Activity size={32} />}
        title="Metrics not available"
        description="The backend does not currently expose time-series metrics, scheduler health, or a worker node registry. This page will be populated when the backend telemetry APIs are implemented."
        className="mt-8 border border-dashed border-border rounded-lg bg-surface/50"
      />
    </div>
  );
}
