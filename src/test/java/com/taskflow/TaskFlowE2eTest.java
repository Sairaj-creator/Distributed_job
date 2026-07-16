package com.taskflow;

import com.taskflow.config.AppContext;
import com.taskflow.config.ConfigService;
import com.taskflow.config.DemoSeeder;
import com.taskflow.core.JobRegistry;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;
import com.taskflow.persistence.ConnectionManager;
import com.taskflow.persistence.JdbcJobRunRepository;
import com.taskflow.persistence.JdbcWorkflowRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskFlowE2eTest {

    private ConnectionManager connectionManager;
    private AppContext appContext;
    private JobRegistry jobRegistry;

    @BeforeEach
    void setUp() throws Exception {
        connectionManager = com.taskflow.testsupport.DatabaseTestSupport.migratedConnectionManager();
        jobRegistry = new JobRegistry();
        JdbcWorkflowRepository workflowRepository = new JdbcWorkflowRepository(connectionManager, jobRegistry);
        JdbcJobRunRepository jobRunRepository = new JdbcJobRunRepository(connectionManager);
        System.setProperty("TASKFLOW_WORKFLOW_QUEUE_LIMIT", "1");
        
        DemoSeeder seeder = new DemoSeeder(connectionManager, workflowRepository, jobRunRepository, jobRegistry);
        seeder.seedIfEmpty();
        
        appContext = new AppContext(workflowRepository, jobRunRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        System.clearProperty("TASKFLOW_WORKFLOW_QUEUE_LIMIT");
        if (appContext != null) {
            appContext.close();
        }
        if (connectionManager != null) {
            connectionManager.close();
        }
    }

    @Test
    void testConcurrencyOverlapGuard() throws Exception {
        WorkflowId wfId = WorkflowId.of("nightly-etl");

        // Make the first job slow so that it overlaps
        jobRegistry.register("com.taskflow.demo.ExtractDataJob", ctx -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return com.taskflow.api.JobResult.success("Slow extraction");
        });

        CountDownLatch startLatch = new CountDownLatch(1);
        
        CompletableFuture<com.taskflow.core.WorkflowRun> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                startLatch.await();
                return appContext.schedulingService().triggerNowAsync(wfId).join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<com.taskflow.core.WorkflowRun> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                startLatch.await();
                return appContext.schedulingService().triggerNowAsync(wfId).join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Release the barrier so both triggers happen simultaneously
        startLatch.countDown();

        com.taskflow.core.WorkflowRun run1 = future1.get(5, TimeUnit.SECONDS);
        com.taskflow.core.WorkflowRun run2 = future2.get(5, TimeUnit.SECONDS);

        boolean oneSkipped = (run1.status() == JobStatus.SKIPPED) || (run2.status() == JobStatus.SKIPPED);
        boolean oneSucceeded = (run1.status() == JobStatus.SUCCEEDED) || (run2.status() == JobStatus.SUCCEEDED);

        assertTrue(oneSkipped, "One run should be skipped due to overlap policy SKIP");
        assertTrue(oneSucceeded, "One run should succeed");
    }
}
