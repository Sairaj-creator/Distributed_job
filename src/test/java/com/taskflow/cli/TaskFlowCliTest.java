package com.taskflow.cli;

import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;
import com.taskflow.testsupport.InMemoryJobRunRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskFlowCliTest {
    @Test
    void historyReadsFromRepository() {
        InMemoryJobRunRepository repository = new InMemoryJobRunRepository();
        repository.save(JobRun.builder(JobId.of("job"), WorkflowId.of("wf"), 1)
                .status(JobStatus.SUCCEEDED)
                .startedAt(Instant.parse("2026-07-05T00:00:00Z"))
                .finishedAt(Instant.parse("2026-07-05T00:00:01Z"))
                .build());
        TaskFlowCli cli = new TaskFlowCli(null, null, null, repository);

        String output = cli.run(new String[]{"history", "job", "--limit", "5"});

        assertTrue(output.contains("runId,jobId,status"));
        assertTrue(output.contains("job,SUCCEEDED"));
    }
}
