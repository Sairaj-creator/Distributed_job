package com.taskflow.scheduler.retry;

import com.taskflow.api.RetryPolicy;

import java.time.Duration;
import java.util.Locale;

/**
 * Simple factory for retry policies named in configuration.
 */
public final class RetryPolicyFactory {
    private RetryPolicyFactory() {
    }

    public static RetryPolicy fromName(String name, int maxAttempts, Duration delay) {
        String normalized = name == null ? "NONE" : name.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "NONE", "NO_RETRY" -> NoRetryPolicy.INSTANCE;
            case "FIXED", "FIXED_DELAY" -> new FixedDelayRetryPolicy(maxAttempts, delay);
            case "EXPONENTIAL", "EXPONENTIAL_BACKOFF" -> new ExponentialBackoffRetryPolicy(maxAttempts, delay);
            default -> throw new IllegalArgumentException("unknown retry policy: " + name);
        };
    }
}
