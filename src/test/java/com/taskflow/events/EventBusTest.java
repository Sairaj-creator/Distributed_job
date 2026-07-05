package com.taskflow.events;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.taskflow.core.JobId;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EventBusTest {
    @Test
    void deliversPublishedEvents() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);
        try (EventBus bus = new EventBus()) {
            bus.register(event -> latch.countDown());

            for (int i = 0; i < 3; i++) {
                bus.publish(new JobStatusEvent(
                        JobId.of("job"),
                        WorkflowId.of("wf"),
                        1,
                        i + 1,
                        JobStatus.SCHEDULED,
                        JobStatus.SUCCEEDED,
                        Instant.now(),
                        "ok",
                        null));
            }

            assertTrue(latch.await(2, TimeUnit.SECONDS));
        }
    }
}
