package com.taskflow.core;

/**
 * Lifecycle status for job and workflow runs.
 */
public enum JobStatus {
    SCHEDULED(false),
    RUNNING(false),
    SUCCEEDED(true),
    FAILED(true),
    RETRYING(false),
    TIMED_OUT(true),
    CANCELLED(true),
    SKIPPED(true),
    UNKNOWN(true);

    private final boolean terminal;

    JobStatus(boolean terminal) {
        this.terminal = terminal;
    }

    /**
     * Returns whether this status represents a finished state.
     *
     * @return true for terminal statuses
     */
    public boolean isTerminal() {
        return terminal;
    }
}
