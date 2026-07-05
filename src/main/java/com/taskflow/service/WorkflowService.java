package com.taskflow.service;

import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.dto.WorkflowStatusDto;
import com.taskflow.persistence.WorkflowRepository;
import com.taskflow.scheduler.DagValidator;

import java.util.List;
import java.util.Optional;

/**
 * Application service for registering and inspecting workflows.
 */
public final class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final DagValidator dagValidator;

    public WorkflowService(WorkflowRepository workflowRepository, DagValidator dagValidator) {
        this.workflowRepository = workflowRepository;
        this.dagValidator = dagValidator;
    }

    public Workflow register(Workflow workflow) {
        dagValidator.validate(workflow);
        return workflowRepository.save(workflow);
    }

    public Optional<Workflow> find(WorkflowId id) {
        return workflowRepository.findById(id);
    }

    public List<WorkflowStatusDto> listStatuses() {
        return workflowRepository.findAll().stream().map(this::toStatus).toList();
    }

    public Optional<WorkflowStatusDto> status(WorkflowId id) {
        return find(id).map(this::toStatus);
    }

    public Workflow pause(WorkflowId id) {
        Workflow workflow = workflowRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("unknown workflow " + id));
        return workflowRepository.save(workflow.withPaused(true));
    }

    public Workflow resume(WorkflowId id) {
        Workflow workflow = workflowRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("unknown workflow " + id));
        return workflowRepository.save(workflow.withPaused(false));
    }

    private WorkflowStatusDto toStatus(Workflow workflow) {
        return new WorkflowStatusDto(
                workflow.id(),
                workflow.name(),
                workflow.jobs().size(),
                workflow.isPaused(),
                workflow.scheduleType().name(),
                workflow.scheduleSpec());
    }
}
