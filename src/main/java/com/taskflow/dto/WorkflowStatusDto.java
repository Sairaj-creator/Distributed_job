package com.taskflow.dto;

import com.taskflow.core.WorkflowId;

/**
 * DTO exposed to CLI/HTTP callers for workflow status.
 */
public record WorkflowStatusDto(
        WorkflowId workflowId,
        String name,
        int jobCount,
        boolean paused,
        String scheduleType,
        String scheduleSpec) {
}
