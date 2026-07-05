package com.taskflow.scheduler.priority;

import com.taskflow.api.JobPriorityStrategy;
import com.taskflow.core.JobDefinition;
import com.taskflow.core.Workflow;

import java.util.Comparator;

/**
 * Prioritizes jobs that unblock the most downstream dependents.
 */
public final class FanOutFirstStrategy implements JobPriorityStrategy {
    @Override
    public Comparator<JobDefinition> comparator(Workflow workflow) {
        return Comparator
                .<JobDefinition>comparingInt(job -> workflow.graph().dependentsOf(job.id()).size())
                .reversed()
                .thenComparing(JobDefinition::id);
    }
}
