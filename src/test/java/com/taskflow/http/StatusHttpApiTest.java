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

class StatusHttpApiTest {
    @Test
    void servesStatusAndWorkflowDetail() throws Exception {
        InMemoryWorkflowRepository repository = new InMemoryWorkflowRepository();
        WorkflowService service = new WorkflowService(repository, new DagValidator());
        com.taskflow.service.ReportService reportService = new com.taskflow.service.ReportService(new com.taskflow.testsupport.InMemoryJobRunRepository());
        service.register(WorkflowFixtures.diamondWorkflow());
        int port = freePort();

        try (StatusHttpApi api = new StatusHttpApi(service, reportService, port)) {
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
        }
    }

    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
