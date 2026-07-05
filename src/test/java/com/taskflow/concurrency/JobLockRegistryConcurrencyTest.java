package com.taskflow.concurrency;

import com.taskflow.core.JobId;
import com.taskflow.core.OverlapPolicy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobLockRegistryConcurrencyTest {
    @Test
    void queuePolicyPreventsConcurrentAccess() throws Exception {
        JobLockRegistry registry = new JobLockRegistry();
        ExecutorService executor = Executors.newFixedThreadPool(6);
        CountDownLatch ready = new CountDownLatch(6);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger inside = new AtomicInteger();
        AtomicBoolean overlapDetected = new AtomicBoolean();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(1, TimeUnit.SECONDS));
                try (JobLockRegistry.Lease ignored = registry.acquire(JobId.of("same"), OverlapPolicy.QUEUE).orElseThrow()) {
                    if (inside.incrementAndGet() > 1) {
                        overlapDetected.set(true);
                    }
                    inside.decrementAndGet();
                }
                return null;
            }));
        }
        assertTrue(ready.await(1, TimeUnit.SECONDS));
        start.countDown();
        for (Future<?> future : futures) {
            future.get(1, TimeUnit.SECONDS);
        }
        executor.shutdownNow();

        assertFalse(overlapDetected.get());
    }

    @Test
    void skipPolicyReturnsEmptyWhenAlreadyLocked() {
        JobLockRegistry registry = new JobLockRegistry();
        JobId jobId = JobId.of("same");

        try (JobLockRegistry.Lease ignored = registry.acquire(jobId, OverlapPolicy.SKIP).orElseThrow()) {
            assertTrue(registry.acquire(jobId, OverlapPolicy.SKIP).isEmpty());
        }
    }
}
