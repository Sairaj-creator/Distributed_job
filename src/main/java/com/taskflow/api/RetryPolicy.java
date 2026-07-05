package com.taskflow.api;

import java.io.Serializable;
import java.time.Duration;

/**
 * Strategy deciding if and when a failed job attempt should be retried.
 */
public interface RetryPolicy extends Serializable {
    /**
     * Returns the maximum number of attempts including the first attempt.
     *
     * @return maximum attempts
     */
    int maxAttempts();

    /**
     * Decides whether a failed attempt should be retried.
     *
     * @param attemptNumber one-based failed attempt number
     * @param failure failure cause
     * @return true if another attempt should be scheduled
     */
    boolean shouldRetry(int attemptNumber, Throwable failure);

    /**
     * Computes the delay before the next attempt.
     *
     * @param attemptNumber one-based failed attempt number
     * @return delay before retry
     */
    Duration nextDelay(int attemptNumber);
}
