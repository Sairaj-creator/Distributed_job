package com.taskflow.scheduler.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Retry policy using capped exponential backoff.
 */
public final class ExponentialBackoffRetryPolicy extends AbstractRetryPolicy {
    private final Duration initialDelay;
    private final double multiplier;
    private final Duration maxDelay;

    public ExponentialBackoffRetryPolicy(int maxAttempts, Duration initialDelay) {
        this(maxAttempts, initialDelay, 2.0, initialDelay.multipliedBy(16));
    }

    public ExponentialBackoffRetryPolicy(int maxAttempts, Duration initialDelay, double multiplier, Duration maxDelay) {
        super(maxAttempts);
        this.initialDelay = Objects.requireNonNull(initialDelay, "initialDelay");
        this.multiplier = multiplier;
        this.maxDelay = Objects.requireNonNull(maxDelay, "maxDelay");
        requireNonNegative(initialDelay, "initialDelay");
        requireNonNegative(maxDelay, "maxDelay");
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("multiplier must be >= 1.0");
        }
    }

    @Override
    public Duration nextDelay(int attemptNumber) {
        double factor = Math.pow(multiplier, Math.max(0, attemptNumber - 1));
        long millis = Math.round(initialDelay.toMillis() * factor);
        return Duration.ofMillis(Math.min(maxDelay.toMillis(), millis));
    }
}
