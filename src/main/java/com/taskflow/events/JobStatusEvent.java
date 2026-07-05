package com.taskflow.events;

import com.taskflow.core.JobId;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;

import java.time.Instant;
import java.util.Optional;

/**
 * Immutable lifecycle event emitted when a job changes status.
 */
public record JobStatusEvent(
        JobId jobId,
        WorkflowId workflowId,
        long workflowRunId,
        int attemptNumber,
        JobStatus oldStatus,
        JobStatus newStatus,
        Instant timestamp,
        String message,
        Throwable error) {

    public Optional<Throwable> errorOptional() {
        return Optional.ofNullable(error);
    }
}
