package com.taskflow.config;

import com.taskflow.core.JobDefinition;
import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.core.OverlapPolicy;
import com.taskflow.core.ScheduleType;
import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.persistence.ConnectionManager;
import com.taskflow.persistence.JobRunRepository;
import com.taskflow.persistence.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class DemoSeeder {
    private static final Logger log = LoggerFactory.getLogger(DemoSeeder.class);

    private final ConnectionManager connectionManager;
    private final WorkflowRepository workflowRepository;
    private final JobRunRepository jobRunRepository;

    public DemoSeeder(ConnectionManager connectionManager, WorkflowRepository workflowRepository, JobRunRepository jobRunRepository) {
        this.connectionManager = connectionManager;
        this.workflowRepository = workflowRepository;
        this.jobRunRepository = jobRunRepository;
    }

    public void seedIfEmpty() {
        try {
            bootstrapSchema();
            if (workflowRepository.findAll().isEmpty()) {
                log.info("Database is empty. Seeding demo workflow...");
                seedDemoWorkflow();
            }
        } catch (Exception e) {
            log.error("Failed to seed demo data", e);
        }
    }

    private void bootstrapSchema() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/com/taskflow/persistence/migration/V1__init_schema.sql")) {
            if (is != null) {
                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                try (Connection conn = connectionManager.getConnection();
                     Statement stmt = conn.createStatement()) {
                    for (String statement : sql.split(";")) {
                        if (!statement.trim().isEmpty()) {
                            stmt.execute(statement.trim());
                        }
                    }
                }
            }
        }
    }

    private void seedDemoWorkflow() {
        WorkflowId wfId = WorkflowId.of("nightly-etl");
        JobId extractId = JobId.of("extract-source-data");
        JobId transformId = JobId.of("transform-data");
        JobId analyzeId = JobId.of("analyze-metrics");
        JobId generateReportId = JobId.of("generate-report");
        JobId notifyId = JobId.of("notify-stakeholders");

        Workflow workflow = Workflow.builder(wfId, "Nightly Analytics")
                .description("Runs nightly analytics and notifies stakeholders")
                .schedule(ScheduleType.CRON, "0 2 * * *")
                .overlapPolicy(OverlapPolicy.SKIP)
                .addJob(JobDefinition.builder(extractId, "Extract Data", ctx -> com.taskflow.api.JobResult.success("demo job - no-op")).timeout(Duration.ofMinutes(10)).build())
                .addJob(JobDefinition.builder(transformId, "Transform Data", ctx -> com.taskflow.api.JobResult.success("demo job - no-op")).timeout(Duration.ofMinutes(5)).build(), extractId)
                .addJob(JobDefinition.builder(analyzeId, "Analyze Metrics", ctx -> com.taskflow.api.JobResult.success("demo job - no-op")).timeout(Duration.ofMinutes(2)).build(), transformId)
                .addJob(JobDefinition.builder(generateReportId, "Generate Report", ctx -> com.taskflow.api.JobResult.success("demo job - no-op")).timeout(Duration.ofMinutes(2)).build(), analyzeId)
                .addJob(JobDefinition.builder(notifyId, "Notify Stakeholders", ctx -> com.taskflow.api.JobResult.success("demo job - no-op")).timeout(Duration.ofMinutes(15)).build(), generateReportId)
                .build();

        workflowRepository.save(workflow);

        // Seed 3 nights of history
        Instant now = Instant.now();
        seedRun(extractId, wfId, 1, now.minus(3, ChronoUnit.DAYS), JobStatus.SUCCEEDED, 5);
        seedRun(transformId, wfId, 1, now.minus(3, ChronoUnit.DAYS).plusSeconds(300), JobStatus.SUCCEEDED, 2);
        
        // A failure and retry on day 2
        seedRun(extractId, wfId, 2, now.minus(2, ChronoUnit.DAYS), JobStatus.FAILED, 1);
        seedRun(extractId, wfId, 2, now.minus(2, ChronoUnit.DAYS).plusSeconds(60), JobStatus.SUCCEEDED, 6);
        
        // A currently running job for day 1
        seedRun(extractId, wfId, 3, now.minus(1, ChronoUnit.HOURS), JobStatus.RUNNING, 60);
    }
    
    private void seedRun(JobId jobId, WorkflowId wfId, long runId, Instant startedAt, JobStatus status, long durationMinutes) {
        JobRun run = JobRun.builder(jobId, wfId, runId)
            .status(status)
            .startedAt(startedAt)
            .finishedAt(status == JobStatus.RUNNING ? null : startedAt.plus(Duration.ofMinutes(durationMinutes)))
            .build();
        jobRunRepository.save(run);
    }
}
