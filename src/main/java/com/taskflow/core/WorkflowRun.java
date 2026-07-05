package com.taskflow.core;

import java.time.Instant;
import java.util.Objects;

/**
 * Execution history for one workflow trigger.
 */
public final class WorkflowRun {
    private final long workflowRunId;
    private final WorkflowId workflowId;
    private final String triggerType;
    private final JobStatus status;
    private final Instant startedAt;
    private final Instant finishedAt;

    public WorkflowRun(
            long workflowRunId,
            WorkflowId workflowId,
            String triggerType,
            JobStatus status,
            Instant startedAt,
            Instant finishedAt) {
        this.workflowRunId = workflowRunId;
        this.workflowId = Objects.requireNonNull(workflowId, "workflowId");
        this.triggerType = Objects.requireNonNull(triggerType, "triggerType");
        this.status = Objects.requireNonNull(status, "status");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.finishedAt = finishedAt;
    }

    public WorkflowRun finish(JobStatus status, Instant finishedAt) {
        return new WorkflowRun(workflowRunId, workflowId, triggerType, status, startedAt, finishedAt);
    }

    public long workflowRunId() {
        return workflowRunId;
    }

    public WorkflowId workflowId() {
        return workflowId;
    }

    public String triggerType() {
        return triggerType;
    }

    public JobStatus status() {
        return status;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant finishedAt() {
        return finishedAt;
    }
}
