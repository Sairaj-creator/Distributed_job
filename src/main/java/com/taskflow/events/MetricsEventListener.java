package com.taskflow.events;

import com.taskflow.core.JobId;
import com.taskflow.core.JobStatus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Listener that maintains in-memory job status counters.
 */
public final class MetricsEventListener extends AbstractEventListener {
    private final ConcurrentMap<JobId, RunningStats> stats = new ConcurrentHashMap<>();

    @Override
    protected void handle(JobStatusEvent event) {
        if (event.newStatus().isTerminal()) {
            stats.computeIfAbsent(event.jobId(), ignored -> new RunningStats()).record(event.newStatus());
        }
    }

    public Map<JobId, Snapshot> snapshots() {
        Map<JobId, Snapshot> copy = new LinkedHashMap<>();
        stats.forEach((jobId, runningStats) -> copy.put(jobId, runningStats.snapshot()));
        return Collections.unmodifiableMap(copy);
    }

    private static final class RunningStats {
        private final LongAdder succeeded = new LongAdder();
        private final LongAdder failed = new LongAdder();
        private final LongAdder timedOut = new LongAdder();
        private final LongAdder skipped = new LongAdder();

        private void record(JobStatus status) {
            switch (status) {
                case SUCCEEDED -> succeeded.increment();
                case FAILED, CANCELLED, UNKNOWN -> failed.increment();
                case TIMED_OUT -> timedOut.increment();
                case SKIPPED -> skipped.increment();
                default -> {
                }
            }
        }

        private Snapshot snapshot() {
            return new Snapshot(succeeded.sum(), failed.sum(), timedOut.sum(), skipped.sum());
        }
    }

    public record Snapshot(long succeeded, long failed, long timedOut, long skipped) {
        public long total() {
            return succeeded + failed + timedOut + skipped;
        }
    }
}
