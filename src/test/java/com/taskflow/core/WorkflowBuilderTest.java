package com.taskflow.core;

import com.taskflow.testsupport.WorkflowFixtures;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowBuilderTest {
    @Test
    void buildsWorkflowGraphWithDependenciesAndDependents() {
        Workflow workflow = WorkflowFixtures.diamondWorkflow();
        JobId extract = JobId.of("extract");
        JobId transformA = JobId.of("transform-a");
        JobId load = JobId.of("load");

        assertEquals(List.of(extract), workflow.graph().roots());
        assertTrue(workflow.graph().dependenciesOf(transformA).contains(extract));
        assertTrue(workflow.graph().dependentsOf(transformA).contains(load));
        assertEquals(4, workflow.jobs().size());
    }

    @Test
    void rejectsUnknownDependencyReferencesAtBuildTime() {
        JobDefinition job = WorkflowFixtures.job("job");
        Workflow.Builder builder = Workflow.builder(WorkflowId.of("wf"), "WF")
                .addJob(job)
                .dependsOn(job.id(), JobId.of("missing"));

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void withPausedReturnsIndependentCopy() {
        Workflow workflow = WorkflowFixtures.diamondWorkflow();

        Workflow paused = workflow.withPaused(true);

        assertTrue(paused.isPaused());
        assertEquals(workflow.jobs().size(), paused.jobs().size());
        assertEquals(workflow.graph().dependencies(), paused.graph().dependencies());
    }
}
