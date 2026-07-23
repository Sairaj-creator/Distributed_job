package com.taskflow.http;

import com.taskflow.service.WorkflowService;
import com.taskflow.testsupport.InMemoryWorkflowRepository;
import com.taskflow.testsupport.WorkflowFixtures;
import com.taskflow.scheduler.DagValidator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class StatusHttpApiTest {
    @Test
    void servesStatusAndWorkflowDetail() throws Exception {
        InMemoryWorkflowRepository repository = new InMemoryWorkflowRepository();
        WorkflowService service = new WorkflowService(repository, new DagValidator());
        com.taskflow.service.ReportService reportService = new com.taskflow.service.ReportService(new com.taskflow.testsupport.InMemoryJobRunRepository());
        com.taskflow.service.SchedulingService schedulingService = mock(com.taskflow.service.SchedulingService.class);
        when(schedulingService.triggerNowAsync(any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
        service.register(WorkflowFixtures.diamondWorkflow());
        int port = freePort();

        try (StatusHttpApi api = new StatusHttpApi(service, schedulingService, reportService, port, "test-key", "*")) {
            api.start();
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> status = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/status")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> detail = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/workflows/etl")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(200, status.statusCode());
            assertTrue(status.body().contains("\"totalWorkflows\":1"));
            assertEquals(200, detail.statusCode());
            assertTrue(detail.body().contains("\"jobs\":["));

            HttpResponse<String> triggerResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/workflows/etl/trigger"))
                            .header("Authorization", "Bearer test-key")
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(202, triggerResponse.statusCode());
            verify(schedulingService).triggerNowAsync(com.taskflow.core.WorkflowId.of("etl"));

            HttpResponse<String> statsResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/reports/stats")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, statsResponse.statusCode());

            HttpResponse<String> runsResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/runs")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, runsResponse.statusCode());

            HttpResponse<String> jobHistoryResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/jobs/extract-data/history")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, jobHistoryResponse.statusCode());
        }
    }

    @Test
    void servesReportingStatsRunsAndJobHistoryWithSeededData() throws Exception {
        InMemoryWorkflowRepository workflowRepo = new InMemoryWorkflowRepository();
        WorkflowService workflowService = new WorkflowService(workflowRepo, new DagValidator());
        com.taskflow.testsupport.InMemoryJobRunRepository jobRunRepo = new com.taskflow.testsupport.InMemoryJobRunRepository();
        
        java.time.Instant now = java.time.Instant.now();
        com.taskflow.core.JobRun run1 = com.taskflow.core.JobRun.builder(com.taskflow.core.JobId.of("extract-data"), com.taskflow.core.WorkflowId.of("etl"), 101L)
                .status(com.taskflow.core.JobStatus.SUCCEEDED)
                .startedAt(now.minus(java.time.Duration.ofMinutes(10)))
                .finishedAt(now.minus(java.time.Duration.ofMinutes(8)))
                .build();

        com.taskflow.core.JobRun run2 = com.taskflow.core.JobRun.builder(com.taskflow.core.JobId.of("extract-data"), com.taskflow.core.WorkflowId.of("etl"), 102L)
                .status(com.taskflow.core.JobStatus.FAILED)
                .startedAt(now.minus(java.time.Duration.ofMinutes(5)))
                .finishedAt(now.minus(java.time.Duration.ofMinutes(4)))
                .build();

        jobRunRepo.save(run1);
        jobRunRepo.save(run2);

        com.taskflow.service.ReportService reportService = new com.taskflow.service.ReportService(jobRunRepo);
        com.taskflow.service.SchedulingService schedulingService = mock(com.taskflow.service.SchedulingService.class);
        int port = freePort();

        try (StatusHttpApi api = new StatusHttpApi(workflowService, schedulingService, reportService, jobRunRepo, port, "test-key", "*")) {
            api.start();
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> statsResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/reports/stats")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, statsResponse.statusCode());
            assertTrue(statsResponse.body().contains("\"jobId\":\"extract-data\""));
            assertTrue(statsResponse.body().contains("\"totalRuns\":2"));
            assertTrue(statsResponse.body().contains("\"succeeded\":1"));
            assertTrue(statsResponse.body().contains("\"failed\":1"));

            HttpResponse<String> runsResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/runs?workflowId=etl")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, runsResponse.statusCode());
            assertTrue(runsResponse.body().contains("\"workflowRunId\":101"));
            assertTrue(runsResponse.body().contains("\"workflowRunId\":102"));

            HttpResponse<String> emptyFilteredRuns = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/runs?workflowId=other")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, emptyFilteredRuns.statusCode());
            assertEquals("[]", emptyFilteredRuns.body());

            HttpResponse<String> historyResponse = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/jobs/extract-data/history")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, historyResponse.statusCode());
            assertTrue(historyResponse.body().contains("\"jobId\":\"extract-data\""));
            assertTrue(historyResponse.body().contains("\"workflowRunId\":101"));
            assertTrue(historyResponse.body().contains("\"workflowRunId\":102"));
        }
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
