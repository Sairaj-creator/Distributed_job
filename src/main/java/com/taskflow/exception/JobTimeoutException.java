package com.taskflow.exception;

/**
 * Thrown when a job exceeds its configured timeout.
 */
public class JobTimeoutException extends JobExecutionException {
    public JobTimeoutException(String message) {
        super(message);
    }
}
