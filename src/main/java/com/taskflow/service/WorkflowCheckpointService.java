package com.taskflow.service;

import com.taskflow.core.JobDefinition;
import com.taskflow.core.JobId;
import com.taskflow.core.Workflow;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Writes workflow checkpoint backups using Java serialization and a hand-written JSON format.
 */
public final class WorkflowCheckpointService {
    /**
     * Saves a workflow using Java object serialization.
     *
     * @param workflow workflow to save
     * @param path target file
     * @throws IOException if writing fails
     */
    public void saveJava(Workflow workflow, Path path) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path))) {
            output.writeObject(workflow);
        }
    }

    /**
     * Loads a workflow checkpoint written by {@link #saveJava(Workflow, Path)}.
     *
     * @param path source file
     * @return deserialized workflow metadata
     * @throws IOException if reading fails
     * @throws ClassNotFoundException if the serialized class cannot be resolved
     */
    public Workflow loadJava(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
            return (Workflow) input.readObject();
        }
    }

    /**
     * Saves a workflow definition as human-readable JSON.
     *
     * @param workflow workflow to save
     * @param path target file
     * @throws IOException if writing fails
     */
    public void saveJson(Workflow workflow, Path path) throws IOException {
        Files.writeString(path, toJson(workflow), StandardCharsets.UTF_8);
    }

    /**
     * Converts a workflow to JSON without a third-party JSON library.
     *
     * @param workflow workflow to serialize
     * @return JSON document
     */
    public String toJson(Workflow workflow) {
        String jobsJson = workflow.jobs().stream()
                .sorted(Comparator.comparing(JobDefinition::id))
                .map(job -> "{"
                        + field("jobId", job.id().value()) + ","
                        + field("name", job.name()) + ","
                        + field("jobClass", job.jobClassName()) + ","
                        + "\"timeoutSeconds\":" + job.timeout().toSeconds()
                        + "}")
                .collect(Collectors.joining(","));
        String dependenciesJson = workflow.graph().dependencies().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().value()))
                .flatMap(entry -> entry.getValue().stream()
                        .sorted()
                        .map(dependency -> dependencyJson(entry.getKey(), dependency)))
                .collect(Collectors.joining(","));
        return "{"
                + field("workflowId", workflow.id().value()) + ","
                + field("name", workflow.name()) + ","
                + field("description", workflow.description()) + ","
                + field("scheduleType", workflow.scheduleType().name()) + ","
                + field("scheduleSpec", workflow.scheduleSpec()) + ","
                + field("overlapPolicy", workflow.overlapPolicy().name()) + ","
                + "\"paused\":" + workflow.isPaused() + ","
                + "\"jobs\":[" + jobsJson + "],"
                + "\"dependencies\":[" + dependenciesJson + "]"
                + "}";
    }

    private String dependencyJson(JobId jobId, JobId dependency) {
        return "{" + field("jobId", jobId.value()) + "," + field("dependsOn", dependency.value()) + "}";
    }

    private String field(String key, String value) {
        return "\"" + escape(key) + "\":\"" + escape(value) + "\"";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
