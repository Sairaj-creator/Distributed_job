import { apiClient } from "./client";
import type { JobRunRecord, JobRunSummary } from "@/types";

export async function fetchJobStats(): Promise<JobRunSummary[]> {
  const { data } = await apiClient.get<JobRunSummary[]>("/reports/stats");
  return data;
}

export async function fetchJobRuns(workflowId?: string): Promise<JobRunRecord[]> {
  const url = workflowId ? `/runs?workflowId=${encodeURIComponent(workflowId)}` : "/runs";
  const { data } = await apiClient.get<JobRunRecord[]>(url);
  return data;
}

export async function fetchJobHistory(jobId: string): Promise<JobRunRecord[]> {
  const { data } = await apiClient.get<JobRunRecord[]>(`/jobs/${jobId}/history`);
  return data;
}
