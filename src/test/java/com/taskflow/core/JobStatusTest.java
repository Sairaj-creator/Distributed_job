package com.taskflow.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobStatusTest {
    @Test
    void terminalStatusesAreMarked() {
        assertFalse(JobStatus.SCHEDULED.isTerminal());
        assertFalse(JobStatus.RUNNING.isTerminal());
        assertFalse(JobStatus.RETRYING.isTerminal());
        assertTrue(JobStatus.SUCCEEDED.isTerminal());
        assertTrue(JobStatus.FAILED.isTerminal());
        assertTrue(JobStatus.TIMED_OUT.isTerminal());
        assertTrue(JobStatus.CANCELLED.isTerminal());
        assertTrue(JobStatus.SKIPPED.isTerminal());
        assertTrue(JobStatus.UNKNOWN.isTerminal());
    }
}
