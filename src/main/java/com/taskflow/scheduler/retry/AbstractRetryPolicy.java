package com.taskflow.scheduler.retry;

import com.taskflow.api.RetryPolicy;

import java.time.Duration;

/**
 * Base retry policy that validates common attempt bounds.
 */
public abstract class AbstractRetryPolicy implements RetryPolicy {
    private final int maxAttempts;

    protected AbstractRetryPolicy(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        this.maxAttempts = maxAttempts;
    }

    @Override
    public int maxAttempts() {
        return maxAttempts;
    }

    @Override
    public boolean shouldRetry(int attemptNumber, Throwable failure) {
        return attemptNumber < maxAttempts;
    }

    protected void requireNonNegative(Duration duration, String field) {
        if (duration.isNegative()) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
    }
}
