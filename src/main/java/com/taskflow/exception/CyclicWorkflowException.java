package com.taskflow.exception;

/**
 * Thrown when a workflow dependency graph contains a cycle.
 */
public class CyclicWorkflowException extends TaskFlowException {
    public CyclicWorkflowException(String message) {
        super(message);
    }
}
