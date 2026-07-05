package com.taskflow.dto;

import com.taskflow.core.JobId;
import com.taskflow.core.JobStatus;

import java.time.Duration;

/**
 * DTO for reporting one job's aggregate run metrics.
 */
public record JobRunSummaryDto(
        JobId jobId,
        long totalRuns,
        long succeeded,
        long failed,
        double successRate,
        Duration averageDuration,
        Duration p95Duration) {
    public JobStatus dominantStatus() {
        return succeeded >= failed ? JobStatus.SUCCEEDED : JobStatus.FAILED;
    }
}
