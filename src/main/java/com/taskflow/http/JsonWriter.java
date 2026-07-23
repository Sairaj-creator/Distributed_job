package com.taskflow.http;

import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.Workflow;
import com.taskflow.dto.WorkflowStatusDto;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Small JSON writer for DTOs used by the embedded status API.
 */
public final class JsonWriter {
    public String statusResponse(Collection<WorkflowStatusDto> statuses, long runningJobs, double successRate, long droppedTriggers) {
        long totalWorkflows = statuses.size();
        long totalJobs = statuses.stream().mapToLong(WorkflowStatusDto::jobCount).sum();
        
        String list = statuses.stream().map(this::workflowStatus).collect(Collectors.joining(",", "[", "]"));
        return "{"
                + "\"summary\": {"
                + "\"totalWorkflows\":" + totalWorkflows + ","
                + "\"totalJobs\":" + totalJobs + ","
                + "\"runningJobs\":" + runningJobs + ","
                + "\"successRate\":" + String.format(java.util.Locale.US, "%.4f", successRate) + ","
                + "\"droppedTriggers\":" + droppedTriggers
                + "},"
                + "\"workflows\":" + list
                + "}";
    }

    public String workflowStatuses(Collection<WorkflowStatusDto> statuses) {
        return statuses.stream().map(this::workflowStatus).collect(Collectors.joining(",", "[", "]"));
    }

    public String workflowStatus(WorkflowStatusDto status) {
        return "{"
                + "\"workflowId\":\"" + escape(status.workflowId().value()) + "\","
                + "\"name\":\"" + escape(status.name()) + "\","
                + "\"jobCount\":" + status.jobCount() + ","
                + "\"paused\":" + status.paused() + ","
                + "\"scheduleType\":\"" + escape(status.scheduleType()) + "\","
                + "\"scheduleSpec\":\"" + escape(status.scheduleSpec()) + "\""
                + "}";
    }
    
    public String workflowDetail(Workflow workflow, Function<JobId, Optional<JobRun>> latestRunLookup) {
        String jobsJson = workflow.jobs().stream().map(job -> {
            String dependsOnJson = workflow.graph().dependenciesOf(job.id()).stream()
                    .map(dep -> "\"" + escape(dep.value()) + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
            
            String lastStatus = "NO_RUNS";
            var latestRun = latestRunLookup.apply(job.id());
            if (latestRun.isPresent()) {
                lastStatus = latestRun.get().status().name();
            }
            
            return "{"
                    + "\"jobId\":\"" + escape(job.id().value()) + "\","
                    + "\"name\":\"" + escape(job.name()) + "\","
                    + "\"lastStatus\":\"" + escape(lastStatus) + "\","
                    + "\"dependsOn\":" + dependsOnJson
                    + "}";
        }).collect(Collectors.joining(",", "[", "]"));
        
        return "{"
                + "\"workflowId\":\"" + escape(workflow.id().value()) + "\","
                + "\"name\":\"" + escape(workflow.name()) + "\","
                + "\"description\":\"" + escape(workflow.description()) + "\","
                + "\"jobs\":" + jobsJson
                + "}";
    }

    public String jobStats(java.util.List<com.taskflow.dto.JobRunSummaryDto> summaries) {
        return summaries.stream().map(this::jobSummary).collect(Collectors.joining(",", "[", "]"));
    }

    public String jobSummary(com.taskflow.dto.JobRunSummaryDto summary) {
        return "{"
                + "\"jobId\":\"" + escape(summary.jobId().value()) + "\","
                + "\"totalRuns\":" + summary.totalRuns() + ","
                + "\"succeeded\":" + summary.succeeded() + ","
                + "\"failed\":" + summary.failed() + ","
                + "\"successRate\":" + String.format(java.util.Locale.US, "%.4f", summary.successRate()) + ","
                + "\"averageDurationMs\":" + summary.averageDuration().toMillis() + ","
                + "\"p95DurationMs\":" + summary.p95Duration().toMillis()
                + "}";
    }

    public String jobRuns(java.util.List<JobRun> runs) {
        return runs.stream().map(this::jobRun).collect(Collectors.joining(",", "[", "]"));
    }

    public String jobRun(JobRun run) {
        String startedAt = run.startedAt() != null ? run.startedAt().toString() : "";
        String finishedAt = run.finishedAt() != null ? run.finishedAt().toString() : "";
        Long runId = run.runId();
        return "{"
                + "\"runId\":" + (runId != null ? runId : 0) + ","
                + "\"jobId\":\"" + escape(run.jobId().value()) + "\","
                + "\"workflowId\":\"" + escape(run.workflowId().value()) + "\","
                + "\"workflowRunId\":" + run.workflowRunId() + ","
                + "\"attemptNumber\":" + run.attemptNumber() + ","
                + "\"status\":\"" + escape(run.status().name()) + "\","
                + "\"startedAt\":\"" + escape(startedAt) + "\","
                + "\"finishedAt\":\"" + escape(finishedAt) + "\","
                + "\"errorMessage\":\"" + escape(run.errorMessage()) + "\""
                + "}";
    }

    public String message(String message) {
        return "{\"message\":\"" + escape(message) + "\"}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
