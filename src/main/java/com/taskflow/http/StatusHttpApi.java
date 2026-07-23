package com.taskflow.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;
import com.taskflow.service.ReportService;
import com.taskflow.service.SchedulingService;
import com.taskflow.service.WorkflowService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedded HTTP status API using JDK HttpServer.
 */
public final class StatusHttpApi implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(StatusHttpApi.class);
    private final WorkflowService workflowService;
    private final SchedulingService schedulingService;
    private final ReportService reportService;
    private final com.taskflow.persistence.JobRunRepository jobRunRepository;
    private final JsonWriter jsonWriter;
    private final HttpServer server;
    private final ExecutorService executor;
    private final String apiKey;
    private final String corsAllowlist;

    public StatusHttpApi(WorkflowService workflowService, SchedulingService schedulingService, ReportService reportService, int port, String apiKey, String corsAllowlist) throws IOException {
        this(workflowService, schedulingService, reportService, null, port, apiKey, corsAllowlist);
    }

    public StatusHttpApi(WorkflowService workflowService, SchedulingService schedulingService, ReportService reportService, com.taskflow.persistence.JobRunRepository jobRunRepository, int port, String apiKey, String corsAllowlist) throws IOException {
        this.workflowService = workflowService;
        this.schedulingService = schedulingService;
        this.reportService = reportService;
        this.jobRunRepository = jobRunRepository;
        this.apiKey = apiKey;
        this.corsAllowlist = corsAllowlist;
        this.jsonWriter = new JsonWriter();
        this.executor = Executors.newFixedThreadPool(10);
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.setExecutor(this.executor);
        server.createContext("/status", this::status);
        server.createContext("/workflows", this::workflow);
        server.createContext("/reports/stats", this::reportsStats);
        server.createContext("/runs", this::runs);
        server.createContext("/jobs", this::jobs);
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
                overallSuccessRate(),
                schedulingService.getDroppedTriggers()
        ));
    }

    private double overallSuccessRate() {
        var report = reportService.generateReport(java.time.Duration.ofDays(7));
        long succeeded = report.stream().mapToLong(com.taskflow.dto.JobRunSummaryDto::succeeded).sum();
        long total = report.stream().mapToLong(com.taskflow.dto.JobRunSummaryDto::totalRuns).sum();
        return total == 0 ? 1.0 : (double) succeeded / total;
    }

    private void workflow(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            write(exchange, 204, "");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String prefix = "/workflows/";
        if (!path.startsWith(prefix) || path.length() == prefix.length()) {
            write(exchange, 400, jsonWriter.message("workflow id required"));
            return;
        }
        
        String rest = path.substring(prefix.length());
        int slash = rest.indexOf('/');
        String idStr = slash == -1 ? rest : rest.substring(0, slash);
        String action = slash == -1 ? "" : rest.substring(slash + 1);
        WorkflowId id = WorkflowId.of(idStr);

        if ("POST".equalsIgnoreCase(method)) {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ") || !authHeader.substring(7).equals(apiKey)) {
                write(exchange, 401, jsonWriter.message("unauthorized"));
                return;
            }
            try {
                switch (action) {
                    case "trigger" -> {
                        schedulingService.triggerNowAsync(id).exceptionally(ex -> {
                            log.error("workflow trigger failed", ex);
                            return null;
                        });
                        write(exchange, 202, jsonWriter.message("triggered"));
                    }
                    case "pause" -> {
                        workflowService.pause(id);
                        write(exchange, 200, jsonWriter.message("paused"));
                    }
                    case "resume" -> {
                        workflowService.resume(id);
                        write(exchange, 200, jsonWriter.message("resumed"));
                    }
                    default -> write(exchange, 400, jsonWriter.message("unknown action " + action));
                }
            } catch (Exception e) {
                log.error("API error", e);
                write(exchange, 500, jsonWriter.message(e.getMessage()));
            }
            return;
        }

        // Return detailed workflow including jobs and dependencies
        String body = workflowService.find(id)
                .map(wf -> jsonWriter.workflowDetail(wf, reportService::latestRun))
                .orElseGet(() -> jsonWriter.message("not found"));
                
        write(exchange, body.contains("not found") ? 404 : 200, body);
    }

    private void reportsStats(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(exchange, 204, "");
            return;
        }
        var summaries = reportService.generateReport(java.time.Duration.ofDays(30));
        write(exchange, 200, jsonWriter.jobStats(summaries));
    }

    private void runs(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(exchange, 204, "");
            return;
        }
        if (jobRunRepository == null) {
            write(exchange, 200, "[]");
            return;
        }
        Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getRawQuery());
        var builder = com.taskflow.persistence.RunQuery.builder();
        if (queryParams.containsKey("workflowId") && !queryParams.get("workflowId").isBlank()) {
            builder.workflowId(WorkflowId.of(queryParams.get("workflowId")));
        }
        if (queryParams.containsKey("jobId") && !queryParams.get("jobId").isBlank()) {
            builder.jobId(com.taskflow.core.JobId.of(queryParams.get("jobId")));
        }
        var page = com.taskflow.persistence.Page.first(100);
        var runs = jobRunRepository.findRuns(builder.build(), page).items();
        write(exchange, 200, jsonWriter.jobRuns(runs));
    }

    private Map<String, String> parseQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of();
        }
        return java.util.Arrays.stream(rawQuery.split("&"))
                .map(p -> p.split("=", 2))
                .filter(p -> p.length > 0 && !p[0].isBlank())
                .collect(java.util.stream.Collectors.toMap(
                        p -> java.net.URLDecoder.decode(p[0], StandardCharsets.UTF_8),
                        p -> p.length > 1 ? java.net.URLDecoder.decode(p[1], StandardCharsets.UTF_8) : "",
                        (v1, v2) -> v2));
    }

    private void jobs(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            write(exchange, 204, "");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        // Path format: /jobs/{id}/history or /jobs/{id}/stats
        String prefix = "/jobs/";
        if (!path.startsWith(prefix) || path.length() <= prefix.length()) {
            write(exchange, 400, jsonWriter.message("job id path required"));
            return;
        }
        String rest = path.substring(prefix.length());
        int slash = rest.indexOf('/');
        if (slash == -1) {
            write(exchange, 400, jsonWriter.message("action required: history or stats"));
            return;
        }
        String jobIdStr = rest.substring(0, slash);
        String action = rest.substring(slash + 1);

        if ("history".equalsIgnoreCase(action)) {
            if (jobRunRepository == null) {
                write(exchange, 200, "[]");
                return;
            }
            var query = com.taskflow.persistence.RunQuery.builder().jobId(com.taskflow.core.JobId.of(jobIdStr)).build();
            var page = com.taskflow.persistence.Page.first(100);
            var runs = jobRunRepository.findRuns(query, page).items();
            write(exchange, 200, jsonWriter.jobRuns(runs));
        } else if ("stats".equalsIgnoreCase(action)) {
            var summaries = reportService.generateReport(jobIdStr, java.time.Duration.ofDays(30));
            write(exchange, 200, jsonWriter.jobStats(summaries));
        } else {
            write(exchange, 400, jsonWriter.message("unknown action " + action));
        }
    }

    private void write(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (corsAllowlist != null && !corsAllowlist.isEmpty()) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", corsAllowlist);
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        }
        
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
        executor.shutdownNow();
    }
}
