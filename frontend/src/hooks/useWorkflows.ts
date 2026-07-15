import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import {
  fetchWorkflow,
  listWorkflows,
  pauseWorkflow,
  resumeWorkflow,
  triggerWorkflow,
} from "@/api/workflows";
import type { ApiError, WorkflowStatus } from "@/types";

export function useWorkflows() {
  return useQuery({
    queryKey: ["workflows", "list"],
    queryFn: listWorkflows,
    refetchInterval: 5000,
    retry: 1,
  });
}

export function useWorkflow(id: string | undefined) {
  return useQuery({
    queryKey: ["workflows", "detail", id],
    queryFn: () => fetchWorkflow(id as string),
    enabled: Boolean(id),
    refetchInterval: 8000,
    retry: 1,
  });
}

function useWorkflowStatusMutation(
  action: (id: string) => Promise<void>,
  successMessage: (id: string) => string
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: action,
    onMutate: async (id: string) => {
      await queryClient.cancelQueries({ queryKey: ["workflows", "list"] });
      const previous = queryClient.getQueryData<WorkflowStatus[]>(["workflows", "list"]);
      return { previous, id };
    },
    onError: (error: ApiError, _id, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["workflows", "list"], context.previous);
      }
      toast.error(error.message ?? "Action failed");
    },
    onSuccess: (_data, id) => {
      toast.success(successMessage(id));
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ["workflows"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard", "summary"] });
    },
  });
}

export function useTriggerWorkflow() {
  // trigger is async on the backend, so we say "Trigger sent" rather than completion.
  return useWorkflowStatusMutation(triggerWorkflow, (id) => `Trigger sent for ${id}`);
}

export function usePauseWorkflow() {
  return useWorkflowStatusMutation(pauseWorkflow, (id) => `Paused ${id}`);
}

export function useResumeWorkflow() {
  return useWorkflowStatusMutation(resumeWorkflow, (id) => `Resumed ${id}`);
}
