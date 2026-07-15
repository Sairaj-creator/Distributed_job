import { apiClient } from "./client";
import type { WorkflowDetail, WorkflowStatus } from "@/types";
import { fetchStatus } from "./dashboard";

export async function listWorkflows(): Promise<WorkflowStatus[]> {
  const { workflows } = await fetchStatus();
  return workflows;
}

export async function fetchWorkflow(id: string): Promise<WorkflowDetail> {
  const { data } = await apiClient.get<WorkflowDetail>(`/workflows/${id}`);
  return data;
}

export async function triggerWorkflow(id: string): Promise<void> {
  await apiClient.post(`/workflows/${id}/trigger`);
}

export async function pauseWorkflow(id: string): Promise<void> {
  await apiClient.post(`/workflows/${id}/pause`);
}

export async function resumeWorkflow(id: string): Promise<void> {
  await apiClient.post(`/workflows/${id}/resume`);
}

// TODO: Stub endpoints for future
export async function createWorkflow(_payload: unknown): Promise<never> {
  throw new Error("createWorkflow: backend endpoint not implemented");
}

export async function updateWorkflow(_id: string, _payload: unknown): Promise<never> {
  throw new Error("updateWorkflow: backend endpoint not implemented");
}

export async function deleteWorkflow(_id: string): Promise<never> {
  throw new Error("deleteWorkflow: backend endpoint not implemented");
}

export async function cloneWorkflow(_id: string): Promise<never> {
  throw new Error("cloneWorkflow: backend endpoint not implemented");
}

export async function fetchWorkflowRuns(_id: string): Promise<never[]> {
  return [];
}
