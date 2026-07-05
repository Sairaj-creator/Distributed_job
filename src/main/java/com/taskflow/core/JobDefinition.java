package com.taskflow.core;

import com.taskflow.api.Job;
import com.taskflow.api.RetryPolicy;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable definition of a job inside a workflow.
 */
public final class JobDefinition implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final RetryPolicy DEFAULT_NO_RETRY = new RetryPolicy() {
        @Override
        public int maxAttempts() {
            return 1;
        }

        @Override
        public boolean shouldRetry(int attemptNumber, Throwable failure) {
            return false;
        }

        @Override
        public Duration nextDelay(int attemptNumber) {
            return Duration.ZERO;
        }
    };

    private final JobId id;
    private final String name;
    private final String jobClassName;
    private final transient Job job;
    private final RetryPolicy retryPolicy;
    private final Duration timeout;
    private final Duration estimatedDuration;
    private final Map<String, String> parameters;

    private JobDefinition(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = requireText(builder.name, "name");
        this.job = Objects.requireNonNull(builder.job, "job");
        this.jobClassName = builder.jobClassName == null ? builder.job.getClass().getName() : builder.jobClassName;
        this.retryPolicy = builder.retryPolicy == null ? DEFAULT_NO_RETRY : builder.retryPolicy;
        this.timeout = builder.timeout == null ? Duration.ofMinutes(5) : builder.timeout;
        this.estimatedDuration = builder.estimatedDuration == null ? Duration.ofSeconds(1) : builder.estimatedDuration;
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.parameters));
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    public static Builder builder(JobId id, String name, Job job) {
        return new Builder(id, name, job);
    }

    private static String requireText(String value, String field) {
        String trimmed = Objects.requireNonNull(value, field).trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return trimmed;
    }

    public JobId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String jobClassName() {
        return jobClassName;
    }

    public Job job() {
        return job;
    }

    public RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    public Duration timeout() {
        return timeout;
    }

    public Duration estimatedDuration() {
        return estimatedDuration;
    }

    public Map<String, String> parameters() {
        return parameters;
    }

    /**
     * Builder for immutable job definitions.
     */
    public static final class Builder {
        private final JobId id;
        private final String name;
        private final Job job;
        private String jobClassName;
        private RetryPolicy retryPolicy;
        private Duration timeout;
        private Duration estimatedDuration;
        private final Map<String, String> parameters = new LinkedHashMap<>();

        private Builder(JobId id, String name, Job job) {
            this.id = id;
            this.name = name;
            this.job = job;
        }

        public Builder jobClassName(String jobClassName) {
            this.jobClassName = requireText(jobClassName, "jobClassName");
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = Objects.requireNonNull(timeout, "timeout");
            return this;
        }

        public Builder estimatedDuration(Duration estimatedDuration) {
            this.estimatedDuration = Objects.requireNonNull(estimatedDuration, "estimatedDuration");
            return this;
        }

        public Builder parameter(String key, String value) {
            this.parameters.put(requireText(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public JobDefinition build() {
            return new JobDefinition(this);
        }
    }
}
