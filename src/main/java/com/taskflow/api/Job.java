package com.taskflow.api;

import com.taskflow.exception.JobExecutionException;

/**
 * A unit of executable work. Implementations should be idempotent because TaskFlow may retry after failures.
 */
@FunctionalInterface
public interface Job {
    /**
     * Executes the job.
     *
     * @param ctx immutable context for the current attempt
     * @return the execution result
     * @throws JobExecutionException when the job fails in a domain-specific way
     */
    JobResult execute(JobContext ctx) throws JobExecutionException;
}
