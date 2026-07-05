package com.taskflow.exception;

/**
 * Wraps checked SQL exceptions raised by repository implementations.
 */
public class PersistenceException extends TaskFlowException {
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
