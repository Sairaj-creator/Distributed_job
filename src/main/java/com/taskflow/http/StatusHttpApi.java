package com.taskflow.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.taskflow.core.WorkflowId;
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
    private final JsonWriter jsonWriter;
    private final HttpServer server;

    public StatusHttpApi(WorkflowService workflowService, int port) throws IOException {
        this.workflowService = workflowService;
        this.jsonWriter = new JsonWriter();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/status", this::status);
        server.createContext("/workflows", this::workflow);
    }

    public void start() {
        server.start();
    }

    private void status(HttpExchange exchange) throws IOException {
        write(exchange, 200, jsonWriter.workflowStatuses(workflowService.listStatuses()));
    }

    private void workflow(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String prefix = "/workflows/";
        if (!path.startsWith(prefix) || path.length() == prefix.length()) {
            write(exchange, 400, jsonWriter.message("workflow id required"));
            return;
        }
        String id = path.substring(prefix.length());
        String body = workflowService.status(WorkflowId.of(id))
                .map(jsonWriter::workflowStatus)
                .orElseGet(() -> jsonWriter.message("not found"));
        write(exchange, body.contains("not found") ? 404 : 200, body);
    }

    private void write(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
