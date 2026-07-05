package com.taskflow.exception;

/**
 * Thrown by user jobs when execution cannot complete successfully.
 */
public class JobExecutionException extends TaskFlowException {
    public JobExecutionException(String message) {
        super(message);
    }

    public JobExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
