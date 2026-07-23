package com.taskflow.concurrency;

import com.taskflow.core.JobId;
import com.taskflow.core.OverlapPolicy;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobLockRegistryConcurrencyTest {

    @Test
    void queuePolicyEnforcesFifoOrder() throws Exception {
        JobLockRegistry registry = new JobLockRegistry();
        JobId jobId = JobId.of("same");

        // First lock should be acquired immediately
        CompletableFuture<java.util.Optional<JobLockRegistry.Lease>> lease1 = registry.acquireAsync(jobId, OverlapPolicy.QUEUE);
        assertTrue(lease1.isDone());
        assertTrue(lease1.get().isPresent());

        // Second and third should queue
        CompletableFuture<java.util.Optional<JobLockRegistry.Lease>> lease2 = registry.acquireAsync(jobId, OverlapPolicy.QUEUE);
        CompletableFuture<java.util.Optional<JobLockRegistry.Lease>> lease3 = registry.acquireAsync(jobId, OverlapPolicy.QUEUE);
        
        assertFalse(lease2.isDone());
        assertFalse(lease3.isDone());

        // Release first, second should complete
        lease1.get().get().close();
        assertTrue(lease2.isDone());
        assertTrue(lease2.get().isPresent());
        assertFalse(lease3.isDone());

        // Release second, third should complete
        lease2.get().get().close();
        assertTrue(lease3.isDone());
        assertTrue(lease3.get().isPresent());
        
        lease3.get().get().close();
    }

    @Test
    void skipPolicyReturnsEmptyWhenAlreadyLocked() throws Exception {
        JobLockRegistry registry = new JobLockRegistry();
        JobId jobId = JobId.of("same");

        CompletableFuture<java.util.Optional<JobLockRegistry.Lease>> lease1 = registry.acquireAsync(jobId, OverlapPolicy.SKIP);
        assertTrue(lease1.isDone());
        assertTrue(lease1.get().isPresent());

        CompletableFuture<java.util.Optional<JobLockRegistry.Lease>> lease2 = registry.acquireAsync(jobId, OverlapPolicy.SKIP);
        assertTrue(lease2.isDone());
        assertTrue(lease2.get().isEmpty());
        
        lease1.get().get().close();
    }
}
