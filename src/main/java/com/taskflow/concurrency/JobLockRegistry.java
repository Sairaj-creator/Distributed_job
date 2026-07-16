package com.taskflow.concurrency;

import com.taskflow.core.JobId;
import com.taskflow.core.OverlapPolicy;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Per-job lock registry used to prevent unwanted overlapping executions.
 */
public final class JobLockRegistry {
    private final ConcurrentMap<JobId, JobLock> locks = new ConcurrentHashMap<>();

    /**
     * Acquires a lock asynchronously according to the overlap policy.
     *
     * @param jobId job identifier
     * @param policy overlap policy
     * @return a future yielding a lease if execution may proceed, otherwise empty for SKIP
     */
    public CompletableFuture<Optional<Lease>> acquireAsync(JobId jobId, OverlapPolicy policy) {
        if (policy == OverlapPolicy.RUN_CONCURRENTLY) {
            return CompletableFuture.completedFuture(Optional.of(Lease.noop()));
        }
        JobLock lock = locks.computeIfAbsent(jobId, ignored -> new JobLock());
        return lock.acquire(policy);
    }

    private static final class JobLock {
        private boolean locked = false;
        private final Queue<CompletableFuture<Optional<Lease>>> queue = new LinkedList<>();

        public synchronized CompletableFuture<Optional<Lease>> acquire(OverlapPolicy policy) {
            if (!locked) {
                locked = true;
                return CompletableFuture.completedFuture(Optional.of(new Lease(this)));
            }
            if (policy == OverlapPolicy.SKIP) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            // QUEUE
            CompletableFuture<Optional<Lease>> future = new CompletableFuture<>();
            queue.add(future);
            return future;
        }

        public synchronized void release() {
            CompletableFuture<Optional<Lease>> next;
            while ((next = queue.poll()) != null) {
                if (!next.isDone()) {
                    next.complete(Optional.of(new Lease(this)));
                    return;
                }
            }
            locked = false;
        }
    }

    /**
     * Auto-closeable lock lease.
     */
    public static final class Lease implements AutoCloseable {
        private final JobLock lock;

        private Lease(JobLock lock) {
            this.lock = lock;
        }

        private static Lease noop() {
            return new Lease(null);
        }

        @Override
        public void close() {
            if (lock != null) {
                lock.release();
            }
        }
    }
}
