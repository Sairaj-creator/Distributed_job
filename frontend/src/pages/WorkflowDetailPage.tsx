import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useWorkflow } from "@/hooks/useWorkflows";
import { DagCanvas } from "@/components/dag/DagCanvas";
import { Card, CardContent } from "@/components/common/Card";
import { ErrorState } from "@/components/common/ErrorState";
import { Skeleton } from "@/components/common/Skeleton";
import { ArrowLeft } from "lucide-react";

export function WorkflowDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: workflow, isPending, error, refetch } = useWorkflow(id);
  const [activeTab, setActiveTab] = useState<"overview" | "dag" | "history" | "timeline">("overview");

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
        {(activeTab === "history" || activeTab === "timeline") && (
          <div className="h-full flex items-center justify-center p-6 text-center border border-dashed border-border rounded-lg text-zinc-500">
            This view is not yet available in the backend API.
          </div>
        )}
      </div>
    </div>
  );
}
