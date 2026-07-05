package com.taskflow.service;

import com.taskflow.core.Workflow;
import com.taskflow.testsupport.WorkflowFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowCheckpointServiceTest {
    private final WorkflowCheckpointService service = new WorkflowCheckpointService();

    @Test
    void savesAndLoadsJavaCheckpoint(@TempDir Path tempDir) throws Exception {
        Workflow workflow = WorkflowFixtures.diamondWorkflow();
        Path checkpoint = tempDir.resolve("workflow.bin");

        service.saveJava(workflow, checkpoint);
        Workflow loaded = service.loadJava(checkpoint);

        assertEquals(workflow.id(), loaded.id());
        assertEquals(workflow.graph().dependencies(), loaded.graph().dependencies());
    }

    @Test
    void writesJsonCheckpoint(@TempDir Path tempDir) throws Exception {
        Workflow workflow = WorkflowFixtures.diamondWorkflow();
        Path json = tempDir.resolve("workflow.json");

        service.saveJson(workflow, json);

        String body = Files.readString(json);
        assertTrue(body.contains("\"workflowId\":\"etl\""));
        assertTrue(body.contains("\"dependencies\""));
        assertTrue(body.contains("\"dependsOn\":\"extract\""));
    }
}
