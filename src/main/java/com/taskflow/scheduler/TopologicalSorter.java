package com.taskflow.scheduler;

import com.taskflow.core.JobId;
import com.taskflow.core.Workflow;
import com.taskflow.exception.CyclicWorkflowException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Computes parallelizable topological levels with Kahn's algorithm in O(V + E) time.
 */
public final class TopologicalSorter {
    /**
     * Sorts the workflow into execution levels.
     *
     * @param workflow workflow to sort
     * @return topological levels where jobs in one level can execute concurrently
     * @throws CyclicWorkflowException if not every job can be ordered
     */
    public List<List<JobId>> sort(Workflow workflow) {
        Map<JobId, Integer> indegree = new HashMap<>();
        for (JobId job : workflow.graph().jobs()) {
            indegree.put(job, workflow.graph().dependenciesOf(job).size());
        }
        Queue<JobId> ready = new ArrayDeque<>(sorted(indegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .toList()));
        List<List<JobId>> levels = new ArrayList<>();
        int visited = 0;

        while (!ready.isEmpty()) {
            int levelSize = ready.size();
            List<JobId> level = new ArrayList<>();
            for (int i = 0; i < levelSize; i++) {
                JobId current = ready.remove();
                level.add(current);
                visited++;
                for (JobId dependent : sorted(workflow.graph().dependentsOf(current))) {
                    int updated = indegree.merge(dependent, -1, Integer::sum);
                    if (updated == 0) {
                        ready.add(dependent);
                    }
                }
            }
            levels.add(Collections.unmodifiableList(level));
        }

        if (visited != workflow.graph().jobs().size()) {
            throw new CyclicWorkflowException("workflow contains a cycle or unresolved dependency");
        }
        return Collections.unmodifiableList(levels);
    }

    private List<JobId> sorted(Set<JobId> ids) {
        List<JobId> copy = new ArrayList<>(ids);
        Collections.sort(copy);
        return copy;
    }

    private List<JobId> sorted(List<JobId> ids) {
        List<JobId> copy = new ArrayList<>(ids);
        Collections.sort(copy);
        return copy;
    }
}
