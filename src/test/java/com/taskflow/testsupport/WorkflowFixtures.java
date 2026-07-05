package com.taskflow.testsupport;

import com.taskflow.api.Job;
import com.taskflow.api.JobResult;
import com.taskflow.core.JobDefinition;
import com.taskflow.core.JobId;
import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;

import java.time.Duration;

public final class WorkflowFixtures {
    private WorkflowFixtures() {
    }

    public static Job noopJob() {
        return ctx -> JobResult.success("ok-" + ctx.jobId());
    }

    public static JobDefinition job(String id) {
        return JobDefinition.builder(JobId.of(id), id, noopJob())
                .timeout(Duration.ofSeconds(2))
                .build();
    }

    public static Workflow diamondWorkflow() {
        JobDefinition extract = job("extract");
        JobDefinition transformA = job("transform-a");
        JobDefinition transformB = job("transform-b");
        JobDefinition load = job("load");
        return Workflow.builder(WorkflowId.of("etl"), "ETL")
                .addJob(extract)
                .addJob(transformA)
                .addJob(transformB)
                .addJob(load)
                .dependsOn(transformA.id(), extract.id())
                .dependsOn(transformB.id(), extract.id())
                .dependsOn(load.id(), transformA.id())
                .dependsOn(load.id(), transformB.id())
                .build();
    }
}
