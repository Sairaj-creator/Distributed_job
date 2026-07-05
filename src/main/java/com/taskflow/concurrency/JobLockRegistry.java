package com.taskflow.concurrency;

import com.taskflow.core.JobId;
import com.taskflow.core.OverlapPolicy;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Per-job lock registry used to prevent unwanted overlapping executions.
 */
public final class JobLockRegistry {
    private final ConcurrentMap<JobId, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Acquires a lock according to the overlap policy.
     *
     * @param jobId job identifier
     * @param policy overlap policy
     * @return a lease if execution may proceed, otherwise empty for SKIP
     */
    public Optional<Lease> acquire(JobId jobId, OverlapPolicy policy) {
        if (policy == OverlapPolicy.RUN_CONCURRENTLY) {
            return Optional.of(Lease.noop());
        }
        ReentrantLock lock = locks.computeIfAbsent(jobId, ignored -> new ReentrantLock());
        if (policy == OverlapPolicy.SKIP) {
            if (lock.isLocked() || !lock.tryLock()) {
                return Optional.empty();
            }
        }
        if (policy == OverlapPolicy.QUEUE) {
            lock.lock();
        }
        return Optional.of(new Lease(lock));
    }

    /**
     * Auto-closeable lock lease.
     */
    public static final class Lease implements AutoCloseable {
        private final ReentrantLock lock;

        private Lease(ReentrantLock lock) {
            this.lock = lock;
        }

        private static Lease noop() {
            return new Lease(null);
        }

        @Override
        public void close() {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
