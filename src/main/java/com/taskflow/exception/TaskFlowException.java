package com.taskflow.exception;

/**
 * Base unchecked exception for TaskFlow failures.
 */
public class TaskFlowException extends RuntimeException {
    public TaskFlowException(String message) {
        super(message);
    }

    public TaskFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
