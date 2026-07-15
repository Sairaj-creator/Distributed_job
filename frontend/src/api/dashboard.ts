import { apiClient } from "./client";
import type { StatusResponse } from "@/types";

/**
 * Real endpoint: GET /status
 */
export async function fetchStatus(): Promise<StatusResponse> {
  const { data } = await apiClient.get<StatusResponse>("/status");
  return data;
}
