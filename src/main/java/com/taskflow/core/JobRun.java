package com.taskflow.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable execution history row for one job attempt.
 */
public final class JobRun {
    private final Long runId;
    private final JobId jobId;
    private final WorkflowId workflowId;
    private final long workflowRunId;
    private final int attemptNumber;
    private final JobStatus status;
    private final Instant startedAt;
    private final Instant finishedAt;
    private final String errorMessage;
    private final String outputSummary;

    private JobRun(Builder builder) {
        this.runId = builder.runId;
        this.jobId = Objects.requireNonNull(builder.jobId, "jobId");
        this.workflowId = Objects.requireNonNull(builder.workflowId, "workflowId");
        this.workflowRunId = builder.workflowRunId;
        this.attemptNumber = builder.attemptNumber;
        this.status = Objects.requireNonNull(builder.status, "status");
        this.startedAt = builder.startedAt;
        this.finishedAt = builder.finishedAt;
        this.errorMessage = builder.errorMessage;
        this.outputSummary = builder.outputSummary;
        if (attemptNumber < 1) {
            throw new IllegalArgumentException("attemptNumber must be positive");
        }
    }

    public static Builder builder(JobId jobId, WorkflowId workflowId, long workflowRunId) {
        return new Builder(jobId, workflowId, workflowRunId);
    }

    public JobRun withRunId(long runId) {
        return toBuilder().runId(runId).build();
    }

    public JobRun markRunning(Instant startedAt) {
        ensureNotTerminal("mark RUNNING");
        return toBuilder().status(JobStatus.RUNNING).startedAt(startedAt).build();
    }

    public JobRun markSucceeded(Instant finishedAt, String outputSummary) {
        ensureStatus(JobStatus.RUNNING, "mark SUCCEEDED");
        return toBuilder().status(JobStatus.SUCCEEDED).finishedAt(finishedAt).outputSummary(outputSummary).build();
    }

    public JobRun markFailed(Instant finishedAt, String errorMessage) {
        ensureStatus(JobStatus.RUNNING, "mark FAILED");
        return toBuilder().status(JobStatus.FAILED).finishedAt(finishedAt).errorMessage(errorMessage).build();
    }

    public JobRun markRetrying(Instant finishedAt, String errorMessage) {
        ensureStatus(JobStatus.RUNNING, "mark RETRYING");
        return toBuilder().status(JobStatus.RETRYING).finishedAt(finishedAt).errorMessage(errorMessage).build();
    }

    public JobRun markTimedOut(Instant finishedAt, String errorMessage) {
        ensureStatus(JobStatus.RUNNING, "mark TIMED_OUT");
        return toBuilder().status(JobStatus.TIMED_OUT).finishedAt(finishedAt).errorMessage(errorMessage).build();
    }

    public JobRun markCancelled(Instant finishedAt, String errorMessage) {
        ensureNotTerminal("mark CANCELLED");
        return toBuilder().status(JobStatus.CANCELLED).finishedAt(finishedAt).errorMessage(errorMessage).build();
    }

    public JobRun markSkipped(Instant finishedAt, String reason) {
        ensureNotTerminal("mark SKIPPED");
        return toBuilder().status(JobStatus.SKIPPED).finishedAt(finishedAt).errorMessage(reason).build();
    }

    public Duration duration() {
        if (startedAt == null || finishedAt == null) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, finishedAt);
    }

    private void ensureNotTerminal(String action) {
        if (status.isTerminal()) {
            throw new IllegalStateException("cannot " + action + " from terminal status " + status);
        }
    }

    private void ensureStatus(JobStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException("cannot " + action + " from " + status);
        }
    }

    private Builder toBuilder() {
        return new Builder(jobId, workflowId, workflowRunId)
                .runId(runId)
                .attemptNumber(attemptNumber)
                .status(status)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .errorMessage(errorMessage)
                .outputSummary(outputSummary);
    }

    public Long runId() {
        return runId;
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

    public JobStatus status() {
        return status;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant finishedAt() {
        return finishedAt;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public String outputSummary() {
        return outputSummary;
    }

    /**
     * Builder for job run history entries.
     */
    public static final class Builder {
        private final JobId jobId;
        private final WorkflowId workflowId;
        private final long workflowRunId;
        private Long runId;
        private int attemptNumber = 1;
        private JobStatus status = JobStatus.SCHEDULED;
        private Instant startedAt;
        private Instant finishedAt;
        private String errorMessage;
        private String outputSummary;

        private Builder(JobId jobId, WorkflowId workflowId, long workflowRunId) {
            this.jobId = jobId;
            this.workflowId = workflowId;
            this.workflowRunId = workflowRunId;
        }

        public Builder runId(Long runId) {
            this.runId = runId;
            return this;
        }

        public Builder attemptNumber(int attemptNumber) {
            this.attemptNumber = attemptNumber;
            return this;
        }

        public Builder status(JobStatus status) {
            this.status = status;
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder finishedAt(Instant finishedAt) {
            this.finishedAt = finishedAt;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder outputSummary(String outputSummary) {
            this.outputSummary = outputSummary;
            return this;
        }

        public JobRun build() {
            return new JobRun(this);
        }
    }
}
