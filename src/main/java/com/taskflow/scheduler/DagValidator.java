package com.taskflow.scheduler;

import com.taskflow.core.JobId;
import com.taskflow.core.Workflow;
import com.taskflow.exception.CyclicWorkflowException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates workflow DAGs in O(V + E) time using iterative three-color DFS.
 */
public final class DagValidator {
    private enum Color {
        WHITE,
        GRAY,
        BLACK
    }

    private record Frame(JobId jobId, Iterator<JobId> children) {
    }

    /**
     * Throws if the workflow contains a cycle.
     *
     * @param workflow workflow to validate
     * @throws CyclicWorkflowException when a back-edge is found
     */
    public void validate(Workflow workflow) {
        Map<JobId, Set<JobId>> adjacency = workflow.graph().adjacency();
        Map<JobId, Color> colors = new HashMap<>();
        for (JobId job : workflow.graph().jobs()) {
            colors.put(job, Color.WHITE);
        }
        for (JobId job : workflow.graph().jobs()) {
            if (colors.get(job) == Color.WHITE) {
                validateFrom(job, adjacency, colors);
            }
        }
    }

    private void validateFrom(JobId start, Map<JobId, Set<JobId>> adjacency, Map<JobId, Color> colors) {
        Deque<Frame> stack = new ArrayDeque<>();
        List<JobId> path = new ArrayList<>();
        stack.push(new Frame(start, adjacency.getOrDefault(start, Set.of()).iterator()));
        colors.put(start, Color.GRAY);
        path.add(start);

        while (!stack.isEmpty()) {
            Frame current = stack.peek();
            if (!current.children().hasNext()) {
                stack.pop();
                colors.put(current.jobId(), Color.BLACK);
                path.remove(path.size() - 1);
                continue;
            }
            JobId next = current.children().next();
            Color color = colors.getOrDefault(next, Color.WHITE);
            if (color == Color.GRAY) {
                throw new CyclicWorkflowException("workflow contains cycle: " + cyclePath(path, next));
            }
            if (color == Color.WHITE) {
                colors.put(next, Color.GRAY);
                path.add(next);
                stack.push(new Frame(next, adjacency.getOrDefault(next, Set.of()).iterator()));
            }
        }
    }

    private String cyclePath(List<JobId> path, JobId repeated) {
        int start = path.indexOf(repeated);
        List<JobId> cycle = new ArrayList<>(path.subList(Math.max(start, 0), path.size()));
        cycle.add(repeated);
        return String.join(" -> ", cycle.stream().map(JobId::value).toList());
    }
}
