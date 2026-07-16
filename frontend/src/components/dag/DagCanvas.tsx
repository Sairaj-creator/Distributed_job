import { useMemo } from "react";
import {
  ReactFlow,
  Controls,
  Background,
  Handle,
  Position,
  type Node,
  type Edge,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import dagre from "dagre";
import type { WorkflowDetail, WorkflowJob } from "@/types";
import { Badge } from "../common/Badge";

const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const nodeWidth = 200;
const nodeHeight = 80;

function getLayoutedElements(nodes: Node[], edges: Edge[], direction = "LR") {
  const isHorizontal = direction === "LR";
  dagreGraph.setGraph({ rankdir: direction });

  nodes.forEach((node) => {
    dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
  });

  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  dagre.layout(dagreGraph);

  nodes.forEach((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    node.targetPosition = isHorizontal ? "left" : "top";
    node.sourcePosition = isHorizontal ? "right" : "bottom";
    node.position = {
      x: nodeWithPosition.x - nodeWidth / 2,
      y: nodeWithPosition.y - nodeHeight / 2,
    };
  });

  return { nodes, edges };
}

// Custom Node component
function JobNode({ data }: { data: { job: WorkflowJob } }) {
  return (
    <div className="bg-surface border border-border rounded-lg p-3 w-[200px] shadow-sm relative">
      <Handle type="target" position={Position.Left} className="w-2 h-2 !bg-zinc-500 border-none" />
      <div className="text-sm font-medium text-zinc-200 truncate" title={data.job.name}>
        {data.job.name}
      </div>
      <div className="text-xs text-zinc-500 font-mono mt-1 mb-2 truncate" title={data.job.jobId}>
        {data.job.jobId}
      </div>
      <Badge status={data.job.lastStatus} />
      <Handle type="source" position={Position.Right} className="w-2 h-2 !bg-zinc-500 border-none" />
    </div>
  );
}

const nodeTypes = {
  job: JobNode,
};

export function DagCanvas({ workflow }: { workflow: WorkflowDetail }) {
  const { nodes, edges } = useMemo(() => {
    const initialNodes: Node[] = workflow.jobs.map((job) => ({
      id: job.jobId,
      type: "job",
      data: { job },
      position: { x: 0, y: 0 },
    }));

    const initialEdges: Edge[] = workflow.jobs.flatMap((job) =>
      job.dependsOn.map((depId) => ({
        id: `${depId}->${job.jobId}`,
        source: depId,
        target: job.jobId,
        type: "smoothstep",
        animated: job.lastStatus === "RUNNING",
        style: { stroke: "#5b8cff" },
      }))
    );

    return getLayoutedElements(initialNodes, initialEdges, "LR");
  }, [workflow]);

  return (
    <div className="w-full h-full bg-canvas rounded-lg border border-border overflow-hidden">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        fitView
        className="bg-canvas"
        proOptions={{ hideAttribution: true }}
      >
        <Background color="#22262b" gap={16} />
        <Controls showInteractive={false} className="bg-surface border-border fill-zinc-400" />
      </ReactFlow>
    </div>
  );
}
