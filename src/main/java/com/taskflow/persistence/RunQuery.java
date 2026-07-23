package com.taskflow.persistence;

import com.taskflow.core.JobId;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;

import java.time.Instant;
import java.util.Optional;

/**
 * Filter criteria for querying job run history.
 */
public final class RunQuery {
    private final WorkflowId workflowId;
    private final JobId jobId;
    private final JobStatus status;
    private final Instant startedFrom;
    private final Instant startedTo;

    private RunQuery(Builder builder) {
        this.workflowId = builder.workflowId;
        this.jobId = builder.jobId;
        this.status = builder.status;
        this.startedFrom = builder.startedFrom;
        this.startedTo = builder.startedTo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<WorkflowId> workflowId() {
        return Optional.ofNullable(workflowId);
    }

    public Optional<JobId> jobId() {
        return Optional.ofNullable(jobId);
    }

    public Optional<JobStatus> status() {
        return Optional.ofNullable(status);
    }

    public Optional<Instant> startedFrom() {
        return Optional.ofNullable(startedFrom);
    }

    public Optional<Instant> startedTo() {
        return Optional.ofNullable(startedTo);
    }

    public static final class Builder {
        private WorkflowId workflowId;
        private JobId jobId;
        private JobStatus status;
        private Instant startedFrom;
        private Instant startedTo;

        public Builder workflowId(WorkflowId workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public Builder jobId(JobId jobId) {
            this.jobId = jobId;
            return this;
        }

        public Builder status(JobStatus status) {
            this.status = status;
            return this;
        }

        public Builder startedFrom(Instant startedFrom) {
            this.startedFrom = startedFrom;
            return this;
        }

        public Builder startedTo(Instant startedTo) {
            this.startedTo = startedTo;
            return this;
        }

        public RunQuery build() {
            return new RunQuery(this);
        }
    }
}
