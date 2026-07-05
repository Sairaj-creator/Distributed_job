package com.taskflow.core;

/**
 * Policy applied when a job or workflow is triggered while a previous execution is still active.
 */
public enum OverlapPolicy {
    SKIP,
    QUEUE,
    RUN_CONCURRENTLY
}
