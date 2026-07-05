package com.taskflow.scheduler;

import com.taskflow.api.JobResult;
import com.taskflow.concurrency.JobLockRegistry;
import com.taskflow.core.JobDefinition;
import com.taskflow.core.JobId;
import com.taskflow.core.JobStatus;
import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.core.WorkflowRun;
import com.taskflow.events.EventBus;
import com.taskflow.scheduler.retry.FixedDelayRetryPolicy;
import com.taskflow.testsupport.InMemoryJobRunRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerEngineIntegrationTest {
    @Test
    void executesIndependentJobsInSameLevel() throws Exception {
        CountDownLatch bothStarted = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        JobDefinition a = JobDefinition.builder(JobId.of("a"), "a", ctx -> {
            bothStarted.countDown();
            await(release);
            return JobResult.success("a");
        }).timeout(Duration.ofSeconds(2)).build();
        JobDefinition b = JobDefinition.builder(JobId.of("b"), "b", ctx -> {
            bothStarted.countDown();
            await(release);
            return JobResult.success("b");
        }).timeout(Duration.ofSeconds(2)).build();
        Workflow workflow = Workflow.builder(WorkflowId.of("wf"), "WF").addJob(a).addJob(b).build();

        try (EventBus bus = new EventBus();
             JobExecutor executor = new JobExecutor(2, bus, new JobLockRegistry(), Clock.systemUTC());
             SchedulerEngine engine = new SchedulerEngine(executor, Clock.systemUTC())) {
            Thread trigger = new Thread(() -> {
                WorkflowRun run = engine.submitRun(workflow);
                assertEquals(JobStatus.SUCCEEDED, run.status());
            });
            trigger.start();
            assertTrue(bothStarted.await(1, TimeUnit.SECONDS), "both jobs should start before either is released");
            release.countDown();
            trigger.join(2000);
        }
    }

    private void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
    }

    @Test
    void retriesFailingJobAndEventuallySucceeds() {
        AtomicInteger attempts = new AtomicInteger();
        JobDefinition flaky = JobDefinition.builder(JobId.of("flaky"), "flaky", ctx -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("temporary");
            }
            return JobResult.success("ok");
        }).retryPolicy(new FixedDelayRetryPolicy(3, Duration.ZERO)).timeout(Duration.ofSeconds(2)).build();
        Workflow workflow = Workflow.builder(WorkflowId.of("wf"), "WF").addJob(flaky).build();

        try (EventBus bus = new EventBus();
             JobExecutor executor = new JobExecutor(1, bus, new JobLockRegistry(), Clock.systemUTC());
             SchedulerEngine engine = new SchedulerEngine(executor, Clock.systemUTC())) {
            WorkflowRun run = engine.submitRun(workflow);

            assertEquals(JobStatus.SUCCEEDED, run.status());
            assertEquals(3, attempts.get());
        }
    }

    @Test
    void stopsWorkflowAfterTerminalFailure() {
        JobDefinition fail = JobDefinition.builder(JobId.of("fail"), "fail", ctx -> {
            throw new RuntimeException("boom");
        }).timeout(Duration.ofSeconds(2)).build();
        AtomicInteger downstreamRuns = new AtomicInteger();
        JobDefinition downstream = JobDefinition.builder(JobId.of("downstream"), "downstream", ctx -> {
            downstreamRuns.incrementAndGet();
            return JobResult.success("should-not-run");
        }).timeout(Duration.ofSeconds(2)).build();
        Workflow workflow = Workflow.builder(WorkflowId.of("wf"), "WF")
                .addJob(fail)
                .addJob(downstream)
                .dependsOn(downstream.id(), fail.id())
                .build();

        try (EventBus bus = new EventBus();
             JobExecutor executor = new JobExecutor(1, bus, new JobLockRegistry(), Clock.systemUTC());
             SchedulerEngine engine = new SchedulerEngine(executor, Clock.systemUTC())) {
            WorkflowRun run = engine.submitRun(workflow);

            assertEquals(JobStatus.FAILED, run.status());
            assertEquals(0, downstreamRuns.get());
        }
    }

    @Test
    void persistsFinalJobRunsWhenRepositoryConfigured() {
        InMemoryJobRunRepository repository = new InMemoryJobRunRepository();
        JobDefinition job = JobDefinition.builder(JobId.of("persisted"), "persisted", ctx -> JobResult.success("ok"))
                .timeout(Duration.ofSeconds(2))
                .build();
        Workflow workflow = Workflow.builder(WorkflowId.of("wf"), "WF").addJob(job).build();

        try (EventBus bus = new EventBus();
             JobExecutor executor = new JobExecutor(1, bus, new JobLockRegistry(), Clock.systemUTC());
             SchedulerEngine engine = new SchedulerEngine(executor, Clock.systemUTC(), repository)) {
            WorkflowRun run = engine.submitRun(workflow);

            assertEquals(JobStatus.SUCCEEDED, run.status());
            assertEquals(1, repository.findAll().size());
            assertEquals(JobStatus.SUCCEEDED, repository.findAll().get(0).status());
        }
    }
}
