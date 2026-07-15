package com.taskflow.scheduler;

import com.taskflow.core.WorkflowId;
import com.taskflow.dto.WorkflowStatusDto;
import com.taskflow.service.SchedulingService;
import com.taskflow.service.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulingDaemon implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(SchedulingDaemon.class);

    private final WorkflowService workflowService;
    private final SchedulingService schedulingService;
    private final CronScheduleCalculator cronCalculator;
    private final Clock clock;
    private final ScheduledExecutorService executorService;
    private final Map<WorkflowId, ZonedDateTime> lastFireTimes = new ConcurrentHashMap<>();

    public SchedulingDaemon(WorkflowService workflowService, SchedulingService schedulingService, Clock clock) {
        this.workflowService = workflowService;
        this.schedulingService = schedulingService;
        this.cronCalculator = new CronScheduleCalculator();
        this.clock = clock;
        this.executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "taskflow-daemon");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        executorService.scheduleAtFixedRate(this::poll, 10, 10, TimeUnit.SECONDS);
        log.info("Scheduling daemon started, polling every 10 seconds.");
    }

    private void poll() {
        try {
            List<WorkflowStatusDto> workflows = workflowService.listStatuses();
            ZonedDateTime now = ZonedDateTime.now(clock);

            for (WorkflowStatusDto workflow : workflows) {
                if (workflow.paused() || !"CRON".equals(workflow.scheduleType())) {
                    continue;
                }

                try {
                    ParsedCronExpression expression = new CronExpressionParser().parse(workflow.scheduleSpec());
                    ZonedDateTime lastFireTime = lastFireTimes.computeIfAbsent(workflow.workflowId(), id -> now.minusMinutes(1));
                    ZonedDateTime nextFireTime = cronCalculator.nextFireTime(expression, lastFireTime);

                    if (!nextFireTime.isAfter(now)) {
                        log.info("Triggering workflow {} as it is due (scheduled for {})", workflow.workflowId(), nextFireTime);
                        lastFireTimes.put(workflow.workflowId(), now);
                        schedulingService.triggerNowAsync(workflow.workflowId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process cron schedule for workflow {}", workflow.workflowId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error during scheduling daemon poll loop", e);
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}
