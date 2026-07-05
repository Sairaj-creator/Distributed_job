package com.taskflow.service;

import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;
import com.taskflow.dto.JobRunSummaryDto;
import com.taskflow.testsupport.InMemoryJobRunRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportServiceTest {
    @Test
    void aggregatesSuccessRateAverageAndP95() {
        InMemoryJobRunRepository repository = new InMemoryJobRunRepository();
        Instant base = Instant.now().minusSeconds(60);
        save(repository, "job", JobStatus.SUCCEEDED, base, 10);
        save(repository, "job", JobStatus.SUCCEEDED, base.plusSeconds(1), 20);
        save(repository, "job", JobStatus.FAILED, base.plusSeconds(2), 30);
        ReportService service = new ReportService(repository);

        List<JobRunSummaryDto> report = service.generateReport("job", java.time.Duration.ofHours(1));

        assertEquals(1, report.size());
        JobRunSummaryDto summary = report.get(0);
        assertEquals(3, summary.totalRuns());
        assertEquals(2, summary.succeeded());
        assertEquals(1, summary.failed());
        assertEquals(2.0 / 3.0, summary.successRate(), 0.001);
        assertEquals(20, summary.averageDuration().toMillis());
        assertEquals(30, summary.p95Duration().toMillis());
    }

    private void save(InMemoryJobRunRepository repository, String jobId, JobStatus status, Instant start, long durationMillis) {
        repository.save(JobRun.builder(JobId.of(jobId), WorkflowId.of("wf"), 1)
                .status(status)
                .startedAt(start)
                .finishedAt(start.plusMillis(durationMillis))
                .build());
    }
}
