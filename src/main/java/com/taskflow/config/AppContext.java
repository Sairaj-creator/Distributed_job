package com.taskflow.config;

import com.taskflow.concurrency.JobLockRegistry;
import com.taskflow.events.EventBus;
import com.taskflow.events.FileEventListener;
import com.taskflow.events.MetricsEventListener;
import com.taskflow.persistence.JobRunRepository;
import com.taskflow.persistence.WorkflowRepository;
import com.taskflow.scheduler.DagValidator;
import com.taskflow.scheduler.JobExecutor;
import com.taskflow.scheduler.SchedulerEngine;
import com.taskflow.service.ReportService;
import com.taskflow.service.SchedulingService;
import com.taskflow.service.WorkflowService;

import java.time.Clock;

/**
 * Process-wide composition root. The singleton is restricted to wiring infrastructure, not domain state.
 */
public final class AppContext implements AutoCloseable {
    private final ConfigService configService;
    private final EventBus eventBus;
    private final MetricsEventListener metricsEventListener;
    private final JobExecutor jobExecutor;
    private final SchedulerEngine schedulerEngine;

    public AppContext(WorkflowRepository workflowRepository, JobRunRepository jobRunRepository) {
        this.configService = new ConfigService();
        this.eventBus = new EventBus();
        this.metricsEventListener = new MetricsEventListener();
        this.eventBus.register(new FileEventListener());
        this.eventBus.register(metricsEventListener);
        this.jobExecutor = new JobExecutor(
                configService.getInt("taskflow.workerThreads", Runtime.getRuntime().availableProcessors()),
                eventBus,
                new JobLockRegistry(),
                Clock.systemUTC());
        this.schedulerEngine = new SchedulerEngine(jobExecutor, Clock.systemUTC());
        this.workflowService = new WorkflowService(workflowRepository, new DagValidator());
        this.schedulingService = new SchedulingService(workflowService, schedulerEngine);
        this.reportService = new ReportService(jobRunRepository);
    }

    private final WorkflowService workflowService;
    private final SchedulingService schedulingService;
    private final ReportService reportService;

    public WorkflowService workflowService() {
        return workflowService;
    }

    public SchedulingService schedulingService() {
        return schedulingService;
    }

    public ReportService reportService() {
        return reportService;
    }

    public MetricsEventListener metricsEventListener() {
        return metricsEventListener;
    }

    @Override
    public void close() {
        schedulerEngine.close();
        eventBus.close();
    }
}
