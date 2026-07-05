package com.taskflow.scheduler;

import com.taskflow.core.WorkflowId;

import java.time.Instant;
import java.util.Objects;

/**
 * Queue entry representing the next scheduled fire time for a workflow.
 */
public final class ScheduledTrigger implements Comparable<ScheduledTrigger> {
    private final WorkflowId workflowId;
    private final Instant nextFireTime;

    public ScheduledTrigger(WorkflowId workflowId, Instant nextFireTime) {
        this.workflowId = Objects.requireNonNull(workflowId, "workflowId");
        this.nextFireTime = Objects.requireNonNull(nextFireTime, "nextFireTime");
    }

    public WorkflowId workflowId() {
        return workflowId;
    }

    public Instant nextFireTime() {
        return nextFireTime;
    }

    @Override
    public int compareTo(ScheduledTrigger other) {
        int byTime = nextFireTime.compareTo(other.nextFireTime);
        return byTime != 0 ? byTime : workflowId.compareTo(other.workflowId);
    }
}
