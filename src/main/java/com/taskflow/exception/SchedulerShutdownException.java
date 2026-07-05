package com.taskflow.exception;

/**
 * Thrown when callers submit work after the scheduler has begun shutting down.
 */
public class SchedulerShutdownException extends TaskFlowException {
    public SchedulerShutdownException(String message) {
        super(message);
    }
}
