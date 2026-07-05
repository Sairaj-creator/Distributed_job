package com.taskflow.http;

import com.taskflow.dto.WorkflowStatusDto;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Small JSON writer for DTOs used by the embedded status API.
 */
public final class JsonWriter {
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

    public String message(String message) {
        return "{\"message\":\"" + escape(message) + "\"}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
