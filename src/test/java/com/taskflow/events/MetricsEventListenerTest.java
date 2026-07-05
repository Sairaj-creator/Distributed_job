package com.taskflow.events;

import com.taskflow.core.JobId;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsEventListenerTest {
    @Test
    void countsTerminalStatuses() {
        MetricsEventListener listener = new MetricsEventListener();
        JobId jobId = JobId.of("job");

        listener.onEvent(new JobStatusEvent(jobId, WorkflowId.of("wf"), 1, 1,
                JobStatus.RUNNING, JobStatus.SUCCEEDED, Instant.now(), "ok", null));
        listener.onEvent(new JobStatusEvent(jobId, WorkflowId.of("wf"), 2, 1,
                JobStatus.RUNNING, JobStatus.TIMED_OUT, Instant.now(), "slow", null));

        MetricsEventListener.Snapshot snapshot = listener.snapshots().get(jobId);
        assertEquals(1, snapshot.succeeded());
        assertEquals(1, snapshot.timedOut());
        assertEquals(2, snapshot.total());
    }
}
