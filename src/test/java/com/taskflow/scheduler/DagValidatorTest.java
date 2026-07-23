package com.taskflow.scheduler;

import com.taskflow.core.JobDefinition;
import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.exception.CyclicWorkflowException;
import com.taskflow.testsupport.WorkflowFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DagValidatorTest {
    private final DagValidator validator = new DagValidator();

    @Test
    void acceptsDiamondDag() {
        assertDoesNotThrow(() -> validator.validate(WorkflowFixtures.diamondWorkflow()));
    }

    @Test
    void rejectsSelfLoopWithHelpfulMessage() {
        JobDefinition job = WorkflowFixtures.job("self");
        Workflow workflow = Workflow.builder(WorkflowId.of("wf"), "WF")
                .addJob(job)
                .dependsOn(job.id(), job.id())
                .build();

        CyclicWorkflowException exception = assertThrows(CyclicWorkflowException.class, () -> validator.validate(workflow));
        assertTrue(exception.getMessage().contains("self"));
    }

    @Test
    void rejectsTwoNodeCycle() {
        JobDefinition a = WorkflowFixtures.job("a");
        JobDefinition b = WorkflowFixtures.job("b");
        Workflow workflow = Workflow.builder(WorkflowId.of("wf"), "WF")
                .addJob(a)
                .addJob(b)
                .dependsOn(a.id(), b.id())
                .dependsOn(b.id(), a.id())
                .build();

        assertThrows(CyclicWorkflowException.class, () -> validator.validate(workflow));
    }
}
