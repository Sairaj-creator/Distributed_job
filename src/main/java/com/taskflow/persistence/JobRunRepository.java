package com.taskflow.persistence;

import com.taskflow.core.JobRun;

/**
 * Repository contract for job run history.
 */
public interface JobRunRepository extends Repository<JobRun, Long> {
    PageResult<JobRun> findRuns(RunQuery query, Page page);
}
