package com.taskflow.persistence;

import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;

/**
 * Repository contract for workflow definitions.
 */
public interface WorkflowRepository extends Repository<Workflow, WorkflowId> {
}
