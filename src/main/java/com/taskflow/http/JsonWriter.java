package com.taskflow.http;

import com.taskflow.core.JobDefinition;
import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.Workflow;
import com.taskflow.dto.WorkflowStatusDto;
import com.taskflow.service.ReportService;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Small JSON writer for DTOs used by the embedded status API.
 */
public final class JsonWriter {
    public String statusResponse(Collection<WorkflowStatusDto> statuses, long runningJobs, double successRate) {
        long totalWorkflows = statuses.size();
        long totalJobs = statuses.stream().mapToLong(WorkflowStatusDto::jobCount).sum();
        
        String list = statuses.stream().map(this::workflowStatus).collect(Collectors.joining(",", "[", "]"));
        return "{"
                + "\"summary\": {"
                + "\"totalWorkflows\":" + totalWorkflows + ","
                + "\"totalJobs\":" + totalJobs + ","
                + "\"runningJobs\":" + runningJobs + ","
                + "\"successRate\":" + String.format(java.util.Locale.US, "%.4f", successRate)
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

    public String message(String message) {
        return "{\"message\":\"" + escape(message) + "\"}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
