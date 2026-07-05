package com.taskflow.service;

import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.scheduler.DagValidator;
import com.taskflow.testsupport.InMemoryWorkflowRepository;
import com.taskflow.testsupport.WorkflowFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowServiceTest {
    @Test
    void registersPausesAndResumesWorkflow() {
        InMemoryWorkflowRepository repository = new InMemoryWorkflowRepository();
        WorkflowService service = new WorkflowService(repository, new DagValidator());
        Workflow workflow = WorkflowFixtures.diamondWorkflow();

        service.register(workflow);
        service.pause(workflow.id());
        assertTrue(service.find(workflow.id()).orElseThrow().isPaused());

        service.resume(WorkflowId.of("etl"));
        assertFalse(service.find(workflow.id()).orElseThrow().isPaused());
    }
}
