package com.taskflow.scheduler.retry;

import java.time.Duration;

/**
 * Retry policy that never schedules a second attempt.
 */
public final class NoRetryPolicy extends AbstractRetryPolicy {
    public static final NoRetryPolicy INSTANCE = new NoRetryPolicy();

    private NoRetryPolicy() {
        super(1);
    }

    @Override
    public boolean shouldRetry(int attemptNumber, Throwable failure) {
        return false;
    }

    @Override
    public Duration nextDelay(int attemptNumber) {
        return Duration.ZERO;
    }
}
