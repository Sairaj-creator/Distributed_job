package com.taskflow.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;
import com.taskflow.service.ReportService;
import com.taskflow.service.WorkflowService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Embedded HTTP status API using JDK HttpServer.
 */
public final class StatusHttpApi implements AutoCloseable {
    private final WorkflowService workflowService;
    private final ReportService reportService;
    private final JsonWriter jsonWriter;
    private final HttpServer server;

    public StatusHttpApi(WorkflowService workflowService, ReportService reportService, int port) throws IOException {
        this.workflowService = workflowService;
        this.reportService = reportService;
        this.jsonWriter = new JsonWriter();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/status", this::status);
        server.createContext("/workflows", this::workflow);
    }

    public void start() {
        server.start();
    }

    private void status(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(exchange, 204, "");
            return;
        }
        write(exchange, 200, jsonWriter.statusResponse(
                workflowService.listStatuses(),
                reportService.countByStatus(JobStatus.RUNNING),
                overallSuccessRate()
        ));
    }

    private double overallSuccessRate() {
        var report = reportService.generateReport(java.time.Duration.ofDays(7));
        long succeeded = report.stream().mapToLong(com.taskflow.dto.JobRunSummaryDto::succeeded).sum();
        long total = report.stream().mapToLong(com.taskflow.dto.JobRunSummaryDto::totalRuns).sum();
        return total == 0 ? 1.0 : (double) succeeded / total;
    }

    private void workflow(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(exchange, 204, "");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String prefix = "/workflows/";
        if (!path.startsWith(prefix) || path.length() == prefix.length()) {
            write(exchange, 400, jsonWriter.message("workflow id required"));
            return;
        }
        String id = path.substring(prefix.length());
        
        // Return detailed workflow including jobs and dependencies
        String body = workflowService.find(WorkflowId.of(id))
                .map(wf -> jsonWriter.workflowDetail(wf, reportService::latestRun))
                .orElseGet(() -> jsonWriter.message("not found"));
                
        write(exchange, body.contains("not found") ? 404 : 200, body);
    }

    private void write(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
        } else {
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
