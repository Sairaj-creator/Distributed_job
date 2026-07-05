package com.taskflow.http;

import com.taskflow.core.WorkflowId;
import com.taskflow.dto.WorkflowStatusDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonWriterTest {
    @Test
    void escapesWorkflowStatusFields() {
        String json = new JsonWriter().workflowStatus(new WorkflowStatusDto(
                WorkflowId.of("wf"),
                "A \"quoted\" workflow",
                2,
                false,
                "ONE_TIME",
                "now"));

        assertTrue(json.contains("\\\"quoted\\\""));
        assertTrue(json.contains("\"jobCount\":2"));
    }
}
