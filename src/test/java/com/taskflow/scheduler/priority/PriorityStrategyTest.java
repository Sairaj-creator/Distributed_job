package com.taskflow.scheduler.priority;

import com.taskflow.core.JobDefinition;
import com.taskflow.core.Workflow;
import com.taskflow.testsupport.WorkflowFixtures;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityStrategyTest {
    @Test
    void fanOutFirstRanksExtractAheadOfLeafJobs() {
        Workflow workflow = WorkflowFixtures.diamondWorkflow();
        List<JobDefinition> jobs = new ArrayList<>(workflow.jobs());

        jobs.sort(new FanOutFirstStrategy().comparator(workflow));

        assertEquals("extract", jobs.get(0).id().value());
    }

    @Test
    void shortestJobFirstUsesEstimatedDurationThenId() {
        Workflow workflow = WorkflowFixtures.diamondWorkflow();
        List<JobDefinition> jobs = new ArrayList<>(workflow.jobs());

        jobs.sort(new ShortestJobFirstStrategy().comparator(workflow));

        assertEquals("extract", jobs.get(0).id().value());
    }
}
