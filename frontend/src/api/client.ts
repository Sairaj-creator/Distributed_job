import axios, { AxiosError } from "axios";
import type { ApiError } from "@/types";

export const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8081";
export const API_KEY = import.meta.env.VITE_API_KEY;

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 10_000,
  headers: API_KEY ? { Authorization: `Bearer ${API_KEY}` } : undefined,
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const normalized: ApiError = normalizeError(error);
    return Promise.reject(normalized);
  }
);

function normalizeError(error: AxiosError): ApiError {
  if (!error.response) {
    return {
      status: null,
      kind: "network",
      message: "Could not reach the TaskFlow engine. Check that it's running and reachable.",
    };
  }

  const status = error.response.status;
  const body = error.response.data as { message?: string } | undefined;
  // Per user feedback, the backend returns 500 with e.getMessage() as the body for mutator errors.
  // We'll try to extract `message` if it's JSON, or fall back to the raw string if it isn't.
  let message = "Unexpected error";
  if (body?.message) {
    message = body.message;
  } else if (typeof error.response.data === "string") {
    message = error.response.data;
  } else if (error.message) {
    message = error.message;
  }

  return {
    status,
    kind: status >= 500 ? "server" : status >= 400 ? "client" : "unknown",
    message,
  };
}
