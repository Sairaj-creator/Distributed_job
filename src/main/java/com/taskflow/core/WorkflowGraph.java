package com.taskflow.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable graph view for workflow dependencies.
 */
public final class WorkflowGraph {

    private final Set<JobId> jobs;
    private final Map<JobId, Set<JobId>> dependencies;
    private final Map<JobId, Set<JobId>> dependents;

    WorkflowGraph(Set<JobId> jobs, Map<JobId, Set<JobId>> dependencies) {
        this.jobs = Collections.unmodifiableSet(new LinkedHashSet<>(jobs));
        Map<JobId, Set<JobId>> depsCopy = new LinkedHashMap<>();
        Map<JobId, Set<JobId>> reverse = new LinkedHashMap<>();
        for (JobId job : jobs) {
            depsCopy.put(job, new LinkedHashSet<>());
            reverse.put(job, new LinkedHashSet<>());
        }
        dependencies.forEach((job, prerequisites) -> {
            depsCopy.computeIfAbsent(job, ignored -> new LinkedHashSet<>()).addAll(prerequisites);
            reverse.computeIfAbsent(job, ignored -> new LinkedHashSet<>());
            for (JobId prerequisite : prerequisites) {
                reverse.computeIfAbsent(prerequisite, ignored -> new LinkedHashSet<>()).add(job);
            }
        });
        this.dependencies = freeze(depsCopy);
        this.dependents = freeze(reverse);
    }

    private static Map<JobId, Set<JobId>> freeze(Map<JobId, Set<JobId>> source) {
        Map<JobId, Set<JobId>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, Collections.unmodifiableSet(new LinkedHashSet<>(value))));
        return Collections.unmodifiableMap(copy);
    }

    public Set<JobId> jobs() {
        return jobs;
    }

    public Set<JobId> dependenciesOf(JobId jobId) {
        return dependencies.getOrDefault(jobId, Set.of());
    }

    public Set<JobId> dependentsOf(JobId jobId) {
        return dependents.getOrDefault(jobId, Set.of());
    }

    public Map<JobId, Set<JobId>> dependencies() {
        return dependencies;
    }

    public Map<JobId, Set<JobId>> adjacency() {
        return dependents;
    }

    public List<JobId> roots() {
        List<JobId> roots = new ArrayList<>();
        for (JobId job : jobs) {
            if (dependenciesOf(job).isEmpty()) {
                roots.add(job);
            }
        }
        Collections.sort(roots);
        return roots;
    }
}
