package com.taskflow.scheduler;

import com.taskflow.api.JobContext;
import com.taskflow.api.JobResult;
import com.taskflow.concurrency.JobLockRegistry;
import com.taskflow.concurrency.NamedThreadFactory;
import com.taskflow.core.JobDefinition;
import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.core.Workflow;
import com.taskflow.events.EventBus;
import com.taskflow.events.JobStatusEvent;
import com.taskflow.exception.JobExecutionException;
import com.taskflow.exception.JobTimeoutException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Executes jobs on a bounded worker pool with timeout, cancellation, overlap, and retry handling.
 */
public final class JobExecutor implements AutoCloseable {
    private final ExecutorService workers;
    private final ScheduledExecutorService timers;
    private final EventBus eventBus;
    private final JobLockRegistry lockRegistry;
    private final Clock clock;
    private final long lockWaitTimeoutMs;

    public JobExecutor(int workerThreads, long lockWaitTimeoutMs, EventBus eventBus, JobLockRegistry lockRegistry, Clock clock) {
        if (workerThreads < 1) {
            throw new IllegalArgumentException("workerThreads must be positive");
        }
        this.workers = Executors.newFixedThreadPool(workerThreads, new NamedThreadFactory("taskflow-worker"));
        this.timers = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("taskflow-timer"));
        this.lockWaitTimeoutMs = lockWaitTimeoutMs;
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.lockRegistry = Objects.requireNonNull(lockRegistry, "lockRegistry");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public ExecutorService getWorkers() {
        return workers;
    }

    /**
     * Executes a job and all configured retries asynchronously.
     *
     * @param workflow workflow containing the job
     * @param job job definition
     * @param workflowRunId workflow run id
     * @return future containing the final attempt run
     */
    public CompletableFuture<JobRun> executeWithRetry(Workflow workflow, JobDefinition job, long workflowRunId) {
        return attempt(workflow, job, workflowRunId, 1);
    }

    private CompletableFuture<JobRun> attempt(Workflow workflow, JobDefinition job, long workflowRunId, int attemptNumber) {
        return runOnce(workflow, job, workflowRunId, attemptNumber).thenCompose(run -> {
            if (shouldRetry(job, run)) {
                publish(job, workflow, workflowRunId, attemptNumber, run.status(), JobStatus.RETRYING, "retry scheduled", null);
                CompletableFuture<JobRun> next = new CompletableFuture<>();
                Duration delay = job.retryPolicy().nextDelay(attemptNumber);
                timers.schedule(
                        () -> attempt(workflow, job, workflowRunId, attemptNumber + 1)
                                .whenComplete((value, error) -> complete(next, value, error)),
                        delay.toMillis(),
                        TimeUnit.MILLISECONDS);
                return next;
            }
            return CompletableFuture.completedFuture(run);
        });
    }

    private boolean shouldRetry(JobDefinition job, JobRun run) {
        return (run.status() == JobStatus.FAILED || run.status() == JobStatus.TIMED_OUT)
                && job.retryPolicy().shouldRetry(run.attemptNumber(), new JobExecutionException(run.errorMessage()));
    }

    private void complete(CompletableFuture<JobRun> target, JobRun value, Throwable error) {
        if (error == null) {
            target.complete(value);
        } else {
            target.completeExceptionally(error);
        }
    }

    private CompletableFuture<JobRun> runOnce(Workflow workflow, JobDefinition job, long workflowRunId, int attemptNumber) {
        CompletableFuture<JobRun> result = new CompletableFuture<>();
        JobRun scheduled = JobRun.builder(job.id(), workflow.id(), workflowRunId)
                .attemptNumber(attemptNumber)
                .status(JobStatus.SCHEDULED)
                .build();
        publish(job, workflow, workflowRunId, attemptNumber, null, JobStatus.SCHEDULED, "scheduled", null);
        
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicReference<JobRun> runningRef = new AtomicReference<>();
        AtomicReference<Future<?>> workerFuture = new AtomicReference<>();

        var leaseFuture = lockRegistry.acquireAsync(job.id(), workflow.overlapPolicy());
        
        // Lock wait timeout mechanism
        Future<?> lockTimeoutFuture = timers.schedule(() -> {
            if (!leaseFuture.isDone() && completed.compareAndSet(false, true)) {
                leaseFuture.cancel(false);
                JobTimeoutException timeout = new JobTimeoutException("job lock wait timed out after " + lockWaitTimeoutMs + "ms");
                JobRun failed = scheduled.markFailed(clock.instant(), timeout.getMessage());
                publish(job, workflow, workflowRunId, attemptNumber, JobStatus.SCHEDULED,
                        JobStatus.FAILED, timeout.getMessage(), timeout);
                result.complete(failed);
            }
        }, lockWaitTimeoutMs, TimeUnit.MILLISECONDS);

        leaseFuture.thenAcceptAsync(leaseOpt -> {
            lockTimeoutFuture.cancel(false);
            if (completed.get()) {
                leaseOpt.ifPresent(JobLockRegistry.Lease::close);
                return;
            }

            if (leaseOpt.isEmpty()) {
                JobRun skipped = scheduled.markSkipped(clock.instant(), "overlap policy SKIP");
                if (completed.compareAndSet(false, true)) {
                    publish(job, workflow, workflowRunId, attemptNumber, JobStatus.SCHEDULED,
                            JobStatus.SKIPPED, "overlap skipped", null);
                    result.complete(skipped);
                }
                return;
            }

            Future<?> submitted = workers.submit(() -> {
                JobRun running = scheduled.markRunning(clock.instant());
                runningRef.set(running);
                publish(job, workflow, workflowRunId, attemptNumber, JobStatus.SCHEDULED, JobStatus.RUNNING, "started", null);
                
                Future<?> execTimeoutFuture = timers.schedule(() -> {
                    if (completed.compareAndSet(false, true)) {
                        Future<?> future = workerFuture.get();
                        if (future != null) {
                            future.cancel(true);
                        }
                        JobTimeoutException timeout = new JobTimeoutException("job timed out after " + job.timeout());
                        JobRun r = runningRef.get();
                        if (r == null) {
                            r = scheduled.markRunning(clock.instant());
                        }
                        JobRun timedOut = r.markTimedOut(clock.instant(), timeout.getMessage());
                        publish(job, workflow, workflowRunId, attemptNumber, JobStatus.RUNNING,
                                JobStatus.TIMED_OUT, timeout.getMessage(), timeout);
                        result.complete(timedOut);
                    }
                }, Math.max(1, job.timeout().toMillis()), TimeUnit.MILLISECONDS);

                try (JobLockRegistry.Lease ignored = leaseOpt.get()) {
                    JobContext context = new JobContext(
                            job.id(),
                            workflow.id(),
                            workflowRunId,
                            attemptNumber,
                            clock.instant(),
                            Map.copyOf(job.parameters()));
                    JobResult jobResult = job.job().execute(context);
                    execTimeoutFuture.cancel(false);
                    Instant finished = clock.instant();
                    JobRun finishedRun = jobResult.isSuccess()
                            ? running.markSucceeded(finished, jobResult.outputSummary())
                            : running.markFailed(finished, jobResult.outputSummary());
                    if (completed.compareAndSet(false, true)) {
                        publish(job, workflow, workflowRunId, attemptNumber, JobStatus.RUNNING,
                                finishedRun.status(), jobResult.outputSummary(), null);
                        result.complete(finishedRun);
                    }
                } catch (Throwable ex) {
                    execTimeoutFuture.cancel(false);
                    Instant finished = clock.instant();
                    JobRun failed = running.markFailed(finished, ex.getMessage());
                    if (completed.compareAndSet(false, true)) {
                        publish(job, workflow, workflowRunId, attemptNumber, JobStatus.RUNNING,
                                JobStatus.FAILED, ex.getMessage(), ex);
                        result.complete(failed);
                    }
                }
            });
            workerFuture.set(submitted);
        }, workers).exceptionally(ex -> {
            if (completed.compareAndSet(false, true)) {
                JobRun failed = scheduled.markFailed(clock.instant(), ex.getMessage());
                publish(job, workflow, workflowRunId, attemptNumber, JobStatus.SCHEDULED,
                        JobStatus.FAILED, ex.getMessage(), ex);
                result.complete(failed);
            }
            return null;
        });

        return result;
    }

    private void publish(
            JobDefinition job,
            Workflow workflow,
            long workflowRunId,
            int attemptNumber,
            JobStatus oldStatus,
            JobStatus newStatus,
            String message,
            Throwable error) {
        eventBus.publish(new JobStatusEvent(
                job.id(),
                workflow.id(),
                workflowRunId,
                attemptNumber,
                oldStatus,
                newStatus,
                clock.instant(),
                message,
                error));
    }

    @Override
    public void close() {
        workers.shutdown();
        timers.shutdown();
        try {
            if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
            if (!timers.awaitTermination(5, TimeUnit.SECONDS)) {
                timers.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            workers.shutdownNow();
            timers.shutdownNow();
        }
    }
}
