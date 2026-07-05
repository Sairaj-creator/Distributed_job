package com.taskflow.scheduler;

import com.taskflow.core.JobId;
import com.taskflow.core.Workflow;
import com.taskflow.testsupport.WorkflowFixtures;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopologicalSorterTest {
    private final TopologicalSorter sorter = new TopologicalSorter();

    @Test
    void sortsDiamondGraphIntoParallelLevels() {
        Workflow workflow = WorkflowFixtures.diamondWorkflow();

        List<List<JobId>> levels = sorter.sort(workflow);

        assertEquals(List.of(JobId.of("extract")), levels.get(0));
        assertEquals(List.of(JobId.of("transform-a"), JobId.of("transform-b")), levels.get(1));
        assertEquals(List.of(JobId.of("load")), levels.get(2));
    }

    @Test
    void everyEdgePointsFromEarlierLevelToLaterLevel() {
        Workflow workflow = WorkflowFixtures.diamondWorkflow();

        List<List<JobId>> levels = sorter.sort(workflow);

        Map<JobId, Integer> index = new HashMap<>();
        for (int i = 0; i < levels.size(); i++) {
            for (JobId jobId : levels.get(i)) {
                index.put(jobId, i);
            }
        }
        workflow.graph().dependencies().forEach((job, dependencies) ->
                dependencies.forEach(dependency -> assertTrue(index.get(dependency) < index.get(job))));
        assertEquals(workflow.jobs().size(), index.size());
    }
}
