package com.taskflow.scheduler.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Retry policy with the same delay before every retry.
 */
public final class FixedDelayRetryPolicy extends AbstractRetryPolicy {
    private final Duration delay;

    public FixedDelayRetryPolicy(int maxAttempts, Duration delay) {
        super(maxAttempts);
        this.delay = Objects.requireNonNull(delay, "delay");
        requireNonNegative(delay, "delay");
    }

    @Override
    public Duration nextDelay(int attemptNumber) {
        return delay;
    }
}
