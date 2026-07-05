package com.taskflow.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobRunTest {
    @Test
    void supportsValidLifecycleTransitions() {
        Instant start = Instant.parse("2026-07-05T00:00:00Z");
        Instant finish = start.plusSeconds(3);

        JobRun run = JobRun.builder(JobId.of("job"), WorkflowId.of("wf"), 10)
                .build()
                .markRunning(start)
                .markSucceeded(finish, "done");

        assertEquals(JobStatus.SUCCEEDED, run.status());
        assertEquals(3000, run.duration().toMillis());
        assertEquals("done", run.outputSummary());
    }

    @Test
    void rejectsTransitionFromTerminalStatus() {
        JobRun run = JobRun.builder(JobId.of("job"), WorkflowId.of("wf"), 10)
                .build()
                .markRunning(Instant.EPOCH)
                .markSucceeded(Instant.EPOCH.plusMillis(1), "done");

        assertThrows(IllegalStateException.class, () -> run.markRunning(Instant.now()));
    }

    @Test
    void failedAttemptCapturesErrorMessage() {
        JobRun run = JobRun.builder(JobId.of("job"), WorkflowId.of("wf"), 10)
                .attemptNumber(2)
                .build()
                .markRunning(Instant.EPOCH)
                .markFailed(Instant.EPOCH.plusSeconds(1), "boom");

        assertEquals(2, run.attemptNumber());
        assertEquals(JobStatus.FAILED, run.status());
        assertEquals("boom", run.errorMessage());
    }
}
