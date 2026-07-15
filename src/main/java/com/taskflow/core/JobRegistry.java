package com.taskflow.core;

import com.taskflow.api.Job;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for mapping job class names to actual executable logic.
 */
public class JobRegistry {
    private final Map<String, Job> registry = new ConcurrentHashMap<>();

    public void register(String jobClassName, Job job) {
        registry.put(jobClassName, job);
    }

    public Optional<Job> getJob(String jobClassName) {
        return Optional.ofNullable(registry.get(jobClassName));
    }
}
