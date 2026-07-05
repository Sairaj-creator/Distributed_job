package com.taskflow.api;

import com.taskflow.core.JobDefinition;
import com.taskflow.core.Workflow;

import java.util.Comparator;

/**
 * Strategy that ranks ready jobs when worker capacity is limited.
 */
public interface JobPriorityStrategy {
    /**
     * Builds a comparator for jobs ready to execute.
     *
     * @param workflow workflow containing the jobs
     * @return comparator with highest-priority jobs first
     */
    Comparator<JobDefinition> comparator(Workflow workflow);
}
