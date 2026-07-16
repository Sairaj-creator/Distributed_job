package com.taskflow.scheduler;

import com.taskflow.api.JobPriorityStrategy;
import com.taskflow.concurrency.LruCache;
import com.taskflow.core.JobDefinition;
import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.core.WorkflowRun;
import com.taskflow.exception.SchedulerShutdownException;
import com.taskflow.persistence.JobRunRepository;
import com.taskflow.scheduler.priority.FanOutFirstStrategy;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Coordinates workflow execution by validating DAGs, executing topological levels, and stopping on failures.
 */
public final class SchedulerEngine implements AutoCloseable {
    private final DagValidator dagValidator;
    private final TopologicalSorter topologicalSorter;
    private final JobExecutor jobExecutor;
    private final JobPriorityStrategy priorityStrategy;
    private final LruCache<String, List<List<JobId>>> topoCache;
    private final JobRunRepository jobRunRepository;
    private final Clock clock;
    private final int queueLimit;
    private final AtomicLong droppedTriggers = new AtomicLong(0);
    private final AtomicLong workflowRunIds = new AtomicLong(1);
    private final AtomicBoolean acceptingWork = new AtomicBoolean(true);
    private final ConcurrentHashMap<WorkflowId, java.util.concurrent.atomic.AtomicInteger> activeWorkflows = new ConcurrentHashMap<>();

    public SchedulerEngine(JobExecutor jobExecutor, Clock clock) {
        this(jobExecutor, clock, null);
    }

    public SchedulerEngine(JobExecutor jobExecutor, Clock clock, JobRunRepository jobRunRepository) {
        this(new DagValidator(), new TopologicalSorter(), jobExecutor, new FanOutFirstStrategy(),
                new LruCache<>(128), jobRunRepository, clock, 100);
    }

    public SchedulerEngine(
            DagValidator dagValidator,
            TopologicalSorter topologicalSorter,
            JobExecutor jobExecutor,
            JobPriorityStrategy priorityStrategy,
            LruCache<String, List<List<JobId>>> topoCache,
            JobRunRepository jobRunRepository,
            Clock clock,
            int queueLimit) {
        this.dagValidator = Objects.requireNonNull(dagValidator, "dagValidator");
        this.topologicalSorter = Objects.requireNonNull(topologicalSorter, "topologicalSorter");
        this.jobExecutor = Objects.requireNonNull(jobExecutor, "jobExecutor");
        this.priorityStrategy = Objects.requireNonNull(priorityStrategy, "priorityStrategy");
        this.topoCache = Objects.requireNonNull(topoCache, "topoCache");
        this.jobRunRepository = jobRunRepository;
        this.clock = Objects.requireNonNull(clock, "clock");
        this.queueLimit = queueLimit;
    }

    public long getDroppedTriggers() {
        return droppedTriggers.get();
    }

    /**
     * Executes a workflow asynchronously.
     *
     * @param workflow workflow definition
     * @return future completed workflow run
     */
    public CompletableFuture<WorkflowRun> submitRunAsync(Workflow workflow) {
        if (!acceptingWork.get()) {
            throw new SchedulerShutdownException("scheduler is shutting down");
        }
        if (workflow.isPaused()) {
            return CompletableFuture.completedFuture(
                    new WorkflowRun(nextRunId(), workflow.id(), "MANUAL", JobStatus.SKIPPED, clock.instant(), clock.instant())
            );
        }

        // Overlap Guard
        int count = activeWorkflows.computeIfAbsent(workflow.id(), k -> new java.util.concurrent.atomic.AtomicInteger(0)).incrementAndGet();
        if (count > queueLimit) {
            activeWorkflows.get(workflow.id()).decrementAndGet();
            droppedTriggers.incrementAndGet();
            return CompletableFuture.completedFuture(
                    new WorkflowRun(nextRunId(), workflow.id(), "MANUAL", JobStatus.SKIPPED, clock.instant(), clock.instant())
            );
        }

        return executeDagAsync(workflow).whenComplete((run, ex) -> {
            activeWorkflows.get(workflow.id()).decrementAndGet();
        });
    }

    private CompletableFuture<WorkflowRun> executeDagAsync(Workflow workflow) {
        try {
            dagValidator.validate(workflow);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
        String cacheKey = workflow.id().value() + "_" + workflow.contentHash();
        List<List<JobId>> levels = topoCache.get(cacheKey)
                .orElseGet(() -> {
                    List<List<JobId>> sorted = topologicalSorter.sort(workflow);
                    topoCache.put(cacheKey, sorted);
                    return sorted;
                });
        long workflowRunId = nextRunId();
        Instant started = clock.instant();

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        java.util.concurrent.atomic.AtomicReference<JobStatus> finalStatusRef = new java.util.concurrent.atomic.AtomicReference<>(JobStatus.SUCCEEDED);

        for (List<JobId> level : levels) {
            chain = chain.thenCompose(ignored -> {
                if (finalStatusRef.get() != JobStatus.SUCCEEDED) {
                    return CompletableFuture.completedFuture(null);
                }
                
                List<JobDefinition> ready = new ArrayList<>();
                for (JobId jobId : level) {
                    workflow.findJob(jobId).ifPresent(ready::add);
                }
                Comparator<JobDefinition> comparator = priorityStrategy.comparator(workflow);
                ready.sort(comparator);
                List<CompletableFuture<JobRun>> futures = ready.stream()
                        .map(job -> jobExecutor.executeWithRetry(workflow, job, workflowRunId).thenApply(this::persistRun))
                        .toList();
                        
                return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenAccept(v -> {
                    boolean failed = futures.stream()
                            .map(CompletableFuture::join)
                            .map(JobRun::status)
                            .anyMatch(status -> status == JobStatus.FAILED
                                    || status == JobStatus.TIMED_OUT
                                    || status == JobStatus.CANCELLED);
                    if (failed) {
                        finalStatusRef.set(JobStatus.FAILED);
                    }
                });
            });
        }
        
        return chain.thenApply(ignored -> 
            new WorkflowRun(workflowRunId, workflow.id(), "MANUAL", finalStatusRef.get(), started, clock.instant())
        );
    }

    private long nextRunId() {
        return workflowRunIds.getAndIncrement();
    }

    private JobRun persistRun(JobRun run) {
        return jobRunRepository == null ? run : jobRunRepository.save(run);
    }

    @Override
    public void close() {
        acceptingWork.set(false);
        jobExecutor.close();
    }
}
