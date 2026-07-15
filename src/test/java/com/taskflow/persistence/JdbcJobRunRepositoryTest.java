package com.taskflow.persistence;

import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.testsupport.DatabaseTestSupport;
import com.taskflow.testsupport.WorkflowFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcJobRunRepositoryTest {
    @Test
    void savesFindsFiltersAndPaginatesRuns() {
        try (ConnectionManager connectionManager = DatabaseTestSupport.migratedConnectionManager()) {
            Workflow workflow = Workflow.builder(WorkflowId.of("wf"), "WF")
                    .addJob(WorkflowFixtures.job("job"))
                    .build();
            com.taskflow.core.JobRegistry registry = new com.taskflow.core.JobRegistry();
            JdbcWorkflowRepository workflowRepository = new JdbcWorkflowRepository(connectionManager, registry);
            workflowRepository.save(workflow);
            JdbcJobRunRepository repository = new JdbcJobRunRepository(connectionManager);

            Instant base = Instant.parse("2026-07-05T00:00:00Z");
            for (int i = 0; i < 5; i++) {
                JobRun run = JobRun.builder(JobId.of("job"), WorkflowId.of("wf"), 1)
                        .status(i % 2 == 0 ? JobStatus.SUCCEEDED : JobStatus.FAILED)
                        .startedAt(base.plusSeconds(i))
                        .finishedAt(base.plusSeconds(i + 1))
                        .outputSummary("run-" + i)
                        .build();
                repository.save(run);
            }

            PageResult<JobRun> failed = repository.findRuns(
                    RunQuery.builder().jobId(JobId.of("job")).status(JobStatus.FAILED).build(),
                    new Page(0, 10));

            assertEquals(2, failed.total());
            assertEquals(2, failed.items().size());
            assertTrue(repository.findById(failed.items().get(0).runId()).isPresent());
            PageResult<JobRun> lastPartial = repository.findRuns(RunQuery.builder().build(), new Page(4, 3));
            assertEquals(1, lastPartial.items().size());
        }
    }
}
