package com.taskflow.api;

import com.taskflow.core.JobId;
import com.taskflow.core.WorkflowId;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable execution context passed to a {@link Job}.
 */
public final class JobContext {
    private final JobId jobId;
    private final WorkflowId workflowId;
    private final long workflowRunId;
    private final int attemptNumber;
    private final Instant scheduledAt;
    private final Map<String, String> parameters;

    /**
     * Creates a context for a job attempt.
     *
     * @param jobId job identifier
     * @param workflowId workflow identifier
     * @param workflowRunId workflow run identifier
     * @param attemptNumber one-based attempt number
     * @param scheduledAt scheduled execution time
     * @param parameters immutable job parameters
     */
    public JobContext(
            JobId jobId,
            WorkflowId workflowId,
            long workflowRunId,
            int attemptNumber,
            Instant scheduledAt,
            Map<String, String> parameters) {
        this.jobId = jobId;
        this.workflowId = workflowId;
        this.workflowRunId = workflowRunId;
        this.attemptNumber = attemptNumber;
        this.scheduledAt = scheduledAt;
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
    }

    public JobId jobId() {
        return jobId;
    }

    public WorkflowId workflowId() {
        return workflowId;
    }

    public long workflowRunId() {
        return workflowRunId;
    }

    public int attemptNumber() {
        return attemptNumber;
    }

    public Instant scheduledAt() {
        return scheduledAt;
    }

    public Map<String, String> parameters() {
        return parameters;
    }
}
