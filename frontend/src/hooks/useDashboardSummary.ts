import { useQuery } from "@tanstack/react-query";
import { fetchStatus } from "@/api/dashboard";

export function useDashboardSummary() {
  return useQuery({
    queryKey: ["dashboard", "summary"],
    queryFn: fetchStatus,
    refetchInterval: 4000,
    retry: 1,
  });
}
