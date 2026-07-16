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
        }
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
