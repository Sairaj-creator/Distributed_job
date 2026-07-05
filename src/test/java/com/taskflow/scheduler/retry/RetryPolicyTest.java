package com.taskflow.scheduler.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryPolicyTest {
    @Test
    void fixedDelayRetriesUntilMaxAttempts() {
        FixedDelayRetryPolicy policy = new FixedDelayRetryPolicy(3, Duration.ofMillis(50));

        assertTrue(policy.shouldRetry(1, new RuntimeException()));
        assertTrue(policy.shouldRetry(2, new RuntimeException()));
        assertFalse(policy.shouldRetry(3, new RuntimeException()));
        assertEquals(Duration.ofMillis(50), policy.nextDelay(1));
    }

    @Test
    void exponentialBackoffIsCapped() {
        ExponentialBackoffRetryPolicy policy = new ExponentialBackoffRetryPolicy(
                5, Duration.ofMillis(100), 2.0, Duration.ofMillis(250));

        assertEquals(Duration.ofMillis(100), policy.nextDelay(1));
        assertEquals(Duration.ofMillis(200), policy.nextDelay(2));
        assertEquals(Duration.ofMillis(250), policy.nextDelay(3));
    }

    @Test
    void factoryCreatesExpectedPolicy() {
        assertTrue(RetryPolicyFactory.fromName("none", 3, Duration.ofMillis(1)) instanceof NoRetryPolicy);
        assertTrue(RetryPolicyFactory.fromName("fixed", 3, Duration.ofMillis(1)) instanceof FixedDelayRetryPolicy);
        assertTrue(RetryPolicyFactory.fromName("exponential", 3, Duration.ofMillis(1)) instanceof ExponentialBackoffRetryPolicy);
    }
}
