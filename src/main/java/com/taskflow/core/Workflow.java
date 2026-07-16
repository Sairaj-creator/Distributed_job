package com.taskflow.core;


import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable workflow definition represented as a directed acyclic graph of jobs.
 */
public final class Workflow {

    private final WorkflowId id;
    private final String name;
    private final String description;
    private final ScheduleType scheduleType;
    private final String scheduleSpec;
    private final OverlapPolicy overlapPolicy;
    private final boolean paused;
    private final Map<JobId, JobDefinition> jobs;
    private final WorkflowGraph graph;

    private Workflow(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.name = requireText(builder.name, "name");
        this.description = builder.description == null ? "" : builder.description;
        this.scheduleType = builder.scheduleType;
        this.scheduleSpec = builder.scheduleSpec;
        this.overlapPolicy = builder.overlapPolicy;
        this.paused = builder.paused;
        this.jobs = Collections.unmodifiableMap(new LinkedHashMap<>(builder.jobs));
        validateDependencyReferences(builder.dependencies, jobs.keySet());
        this.graph = new WorkflowGraph(jobs.keySet(), builder.dependencies);
    }

    public static Builder builder(WorkflowId id, String name) {
        return new Builder(id, name);
    }

    private static String requireText(String value, String field) {
        String trimmed = Objects.requireNonNull(value, field).trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return trimmed;
    }

    private static void validateDependencyReferences(Map<JobId, Set<JobId>> dependencies, Set<JobId> jobs) {
        dependencies.forEach((job, prerequisites) -> {
            if (!jobs.contains(job)) {
                throw new IllegalArgumentException("dependency references unknown job " + job);
            }
            for (JobId prerequisite : prerequisites) {
                if (!jobs.contains(prerequisite)) {
                    throw new IllegalArgumentException("dependency references unknown prerequisite " + prerequisite);
                }
            }
        });
    }

    public Workflow withPaused(boolean paused) {
        Builder builder = builder(id, name)
                .description(description)
                .schedule(scheduleType, scheduleSpec)
                .overlapPolicy(overlapPolicy)
                .paused(paused);
        jobs.values().forEach(builder::addJob);
        graph.dependencies().forEach((job, prerequisites) -> prerequisites.forEach(prerequisite -> builder.dependsOn(job, prerequisite)));
        return builder.build();
    }

    public WorkflowId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public ScheduleType scheduleType() {
        return scheduleType;
    }

    public String scheduleSpec() {
        return scheduleSpec;
    }

    public OverlapPolicy overlapPolicy() {
        return overlapPolicy;
    }

    public boolean isPaused() {
        return paused;
    }

    public Collection<JobDefinition> jobs() {
        return jobs.values();
    }

    public Optional<JobDefinition> findJob(JobId jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public WorkflowGraph graph() {
        return graph;
    }

    public String contentHash() {
        int hash = Objects.hash(id, name, scheduleSpec, overlapPolicy);
        for (JobId jobId : jobs.keySet()) {
            hash = 31 * hash + jobId.hashCode();
            hash = 31 * hash + graph.dependenciesOf(jobId).hashCode();
        }
        return Integer.toHexString(hash);
    }

    /**
     * Builder for workflow definitions.
     */
    public static final class Builder {
        private final WorkflowId id;
        private final String name;
        private String description;
        private ScheduleType scheduleType = ScheduleType.ONE_TIME;
        private String scheduleSpec = "now";
        private OverlapPolicy overlapPolicy = OverlapPolicy.SKIP;
        private boolean paused;
        private final Map<JobId, JobDefinition> jobs = new LinkedHashMap<>();
        private final Map<JobId, Set<JobId>> dependencies = new LinkedHashMap<>();

        private Builder(WorkflowId id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder schedule(ScheduleType scheduleType, String scheduleSpec) {
            this.scheduleType = Objects.requireNonNull(scheduleType, "scheduleType");
            this.scheduleSpec = requireText(scheduleSpec, "scheduleSpec");
            return this;
        }

        public Builder overlapPolicy(OverlapPolicy overlapPolicy) {
            this.overlapPolicy = Objects.requireNonNull(overlapPolicy, "overlapPolicy");
            return this;
        }

        public Builder paused(boolean paused) {
            this.paused = paused;
            return this;
        }

        public Builder addJob(JobDefinition job) {
            Objects.requireNonNull(job, "job");
            if (jobs.putIfAbsent(job.id(), job) != null) {
                throw new IllegalArgumentException("duplicate job id " + job.id());
            }
            dependencies.computeIfAbsent(job.id(), ignored -> new LinkedHashSet<>());
            return this;
        }

        public Builder addJob(JobDefinition job, JobId... dependsOn) {
            addJob(job);
            for (JobId dependency : dependsOn) {
                dependsOn(job.id(), dependency);
            }
            return this;
        }

        public Builder dependsOn(JobId jobId, JobId dependency) {
            Objects.requireNonNull(jobId, "jobId");
            Objects.requireNonNull(dependency, "dependency");
            dependencies.computeIfAbsent(jobId, ignored -> new LinkedHashSet<>()).add(dependency);
            return this;
        }

        public Workflow build() {
            return new Workflow(this);
        }
    }
}
