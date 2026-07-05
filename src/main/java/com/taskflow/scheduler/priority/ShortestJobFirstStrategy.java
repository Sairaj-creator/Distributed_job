package com.taskflow.scheduler.priority;

import com.taskflow.api.JobPriorityStrategy;
import com.taskflow.core.JobDefinition;
import com.taskflow.core.Workflow;

import java.util.Comparator;

/**
 * Prioritizes jobs with the shortest estimated duration first.
 */
public final class ShortestJobFirstStrategy implements JobPriorityStrategy {
    @Override
    public Comparator<JobDefinition> comparator(Workflow workflow) {
        return Comparator.comparing(JobDefinition::estimatedDuration).thenComparing(JobDefinition::id);
    }
}
