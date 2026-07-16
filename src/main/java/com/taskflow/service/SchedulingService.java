package com.taskflow.service;

import com.taskflow.core.WorkflowId;
import com.taskflow.core.WorkflowRun;
import com.taskflow.scheduler.SchedulerEngine;

/**
 * Application service for manual workflow triggers.
 */
public final class SchedulingService {
    private final WorkflowService workflowService;
    private final SchedulerEngine schedulerEngine;

    public SchedulingService(WorkflowService workflowService, SchedulerEngine schedulerEngine) {
        this.workflowService = workflowService;
        this.schedulerEngine = schedulerEngine;
    }

    public java.util.concurrent.CompletableFuture<WorkflowRun> triggerNowAsync(WorkflowId workflowId) {
        return schedulerEngine.submitRunAsync(workflowService.find(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("unknown workflow " + workflowId)));
    }

    public long getDroppedTriggers() {
        return schedulerEngine.getDroppedTriggers();
    }
}
