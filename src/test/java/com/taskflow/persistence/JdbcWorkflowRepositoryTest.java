package com.taskflow.persistence;

import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.testsupport.DatabaseTestSupport;
import com.taskflow.testsupport.WorkflowFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcWorkflowRepositoryTest {
    @Test
    void savesAndLoadsWorkflowWithScopedDependencies() {
        try (ConnectionManager connectionManager = DatabaseTestSupport.migratedConnectionManager()) {
            JdbcWorkflowRepository repository = new JdbcWorkflowRepository(connectionManager);
            Workflow first = WorkflowFixtures.diamondWorkflow();
            Workflow second = Workflow.builder(WorkflowId.of("solo"), "Solo")
                    .addJob(WorkflowFixtures.job("only"))
                    .build();

            repository.save(first);
            repository.save(second);

            Workflow loaded = repository.findById(first.id()).orElseThrow();
            Workflow solo = repository.findById(second.id()).orElseThrow();

            assertEquals(4, loaded.jobs().size());
            assertEquals(first.graph().dependencies(), loaded.graph().dependencies());
            assertEquals(1, solo.jobs().size());
            assertTrue(solo.graph().dependenciesOf(com.taskflow.core.JobId.of("only")).isEmpty());
        }
    }
}
