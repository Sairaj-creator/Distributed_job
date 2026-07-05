package com.taskflow.testsupport;

import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.persistence.WorkflowRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryWorkflowRepository implements WorkflowRepository {
    private final Map<WorkflowId, Workflow> workflows = new ConcurrentHashMap<>();

    @Override
    public Optional<Workflow> findById(WorkflowId id) {
        return Optional.ofNullable(workflows.get(id));
    }

    @Override
    public Workflow save(Workflow entity) {
        workflows.put(entity.id(), entity);
        return entity;
    }

    @Override
    public List<Workflow> findAll() {
        return new ArrayList<>(workflows.values());
    }
}
