package com.taskflow.service;

import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.persistence.JobRunRepository;
import com.taskflow.persistence.Page;
import com.taskflow.persistence.RunQuery;

import java.time.Duration;
import java.time.Instant;

/**
 * Repairs stale RUNNING rows on startup after an unclean shutdown.
 */
public final class ReconciliationService {
    private final JobRunRepository jobRunRepository;

    public ReconciliationService(JobRunRepository jobRunRepository) {
        this.jobRunRepository = jobRunRepository;
    }

    public int markStaleRunningUnknown(Duration gracePeriod) {
        Instant cutoff = Instant.now().minus(gracePeriod);
        int updated = 0;
        for (JobRun run : jobRunRepository.findRuns(
                RunQuery.builder().status(JobStatus.RUNNING).startedTo(cutoff).build(),
                Page.first(10_000)).items()) {
            jobRunRepository.save(JobRun.builder(run.jobId(), run.workflowId(), run.workflowRunId())
                    .runId(run.runId())
                    .attemptNumber(run.attemptNumber())
                    .status(JobStatus.UNKNOWN)
                    .startedAt(run.startedAt())
                    .finishedAt(Instant.now())
                    .errorMessage("reconciled stale RUNNING row")
                    .build());
            updated++;
        }
        return updated;
    }
}
